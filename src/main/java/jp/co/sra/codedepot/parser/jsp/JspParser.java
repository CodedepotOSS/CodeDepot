/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
package jp.co.sra.codedepot.parser.jsp;

import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.UniversalReader;

import org.apache.lucene.analysis.Token;

public class JspParser extends Parser {

  	private static Logger logger = Logger.getLogger(JspParser.class.getName());
	private static String languageName = new String("jsp");
  	private static String suffix = new String(".jsp");

	@Override
	public String getLanguageName() {
		return languageName;
	}

	@Override
	public boolean accept(java.io.File file) {
		String path = file.getName().toLowerCase();
		if (path.endsWith(this.suffix)) {
			return true;
		}
		return false;
	}

	@Override
	public String toHtml(IndexedCode ic) {
		IndexedCodeFile icf;
		try {
			icf = (IndexedCodeFile)ic;
			return icf.toHTML();
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public IndexedCodeFile parse(String fid, String uuid, File f) throws Exception {
		IndexedCodeFile indexedCodeFile = null;

		try {
			String contents = UniversalReader.getContents(f);
			char[] text = contents.toCharArray();

			indexedCodeFile = new IndexedCodeFile();
			indexedCodeFile.setId(fid);
			indexedCodeFile.setUuid(uuid);
			indexedCodeFile.setText(text);
			LineRead rct = new LineRead(text);
			indexedCodeFile.setLineRead(rct);

			List<Integer>code = parseCode(text);
			indexedCodeFile.setCode(code);

		} catch (IOException e) {
			logger.warning("Error in reading file " + f.getAbsolutePath());
		}
		return indexedCodeFile;
	}

	@Override
	public void close() {
	}

	public List<Integer> parseCode(char[] text) {
		return parseCode(text, 0, text.length);
	}

	public List<Integer> parseCode(char[] text, int sp, int ep) {
		boolean escape = false;
		boolean squote = false;
		boolean dquote = false;
		int state = 0;

		List<Integer> list = new ArrayList<Integer>();

		for(int i = sp; i < ep ; i++) {
			char ch = text[i];
			if (state == 0) {
				if (ch == '<') {
					state++;
				}
			} else if (state == 1) {
				if (ch == '%') {
					list.add(i - 1);
					state++;
				} else {
					state--;
				}
			} else if (state == 2) {

				/* escape */
				if (escape) {
					escape = false;
					continue;
				}

				if (ch == '\\') {
					escape = true;
					continue;
				}

				/* single quote */
				if (squote) {
					if (ch == '\'') {
						squote = false;
						continue;
					} else {
						continue;
					}
				/* double quote */
				} else if (dquote) {
					if (ch == '"') {
						dquote = false;
						continue;
					} else {
						continue;
					}
				/* single quote */
				} else if (ch == '\'') {
					squote = true;
					continue;
				/* double quote */
				} else if (ch == '"') {
					dquote = true;
					continue;
				}

				if (ch == '%') {
					state++;
				}
			} else if (state == 3) {
				if (ch == '>') {
					list.add(i + 1);
					state = 0;
				} else {
					state--;
				}
			}
		}
		return list;
	}

	public List<Integer> parseComment(char[] text) {
		return parseComment(text, 0, text.length);
	}

	public List<Integer> parseComment(char[] text, int sp, int ep) {
		boolean escape = false;
		boolean squote = false;
		boolean dquote = false;
		int state = 0;

		List<Integer> list = new ArrayList<Integer>();

		for(int i = sp; i < ep; i++) {
			char ch = text[i];
			if (state == 0) {

				/* escape */
				if (escape) {
					escape = false;
					continue;
				}
				if (ch == '\\') {
					escape = true;
					continue;
				}

				/* single quote */
				if (squote) {
					if (ch == '\'') {
						squote = false;
						continue;
					} else {
						continue;
					}
				/* double quote */
				} else if (dquote) {
					if (ch == '"') {
						dquote = false;
						continue;
					} else {
						continue;
					}
				/* single quote */
				} else if (ch == '\'') {
					squote = true;
					continue;
				/* double quote */
				} else if (ch == '"') {
					dquote = true;
					continue;
				}

				if (ch == '/') {
					state++;
				}
			} else if (state == 1) {
				if (ch == '/') {
					list.add(i - 1);
					state += 1;
				} else if (ch == '*') {
					list.add(i - 1);
					state += 2;
				} else {
					state--;
				}
			} else if (state == 2) {

				if (ch == '\n') {
					list.add(i);
					state = 0;
					state++;
				}
			} else if (state == 3) {
				if (ch == '*') {
					state++;
				}
			} else if (state == 4) {
				if (ch == '/') {
					list.add(i + 1);
					state = 0;
				} else if (ch != '*') {
					state--;
				}
			}
		}
		if (state > 1) {
			list.add(ep);
		}
		return list;
	}

}
