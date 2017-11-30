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
package jp.co.sra.codedepot.parser.sh;

import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.UniversalReader;

import org.apache.commons.io.FilenameUtils;

public class ShellParser extends Parser {

  	private static Logger logger = Logger.getLogger(ShellParser.class.getName());
	private static String languageName = new String("text");
	private String[] pattern = { "*.sh", "*.mk", "makefile*" };

	/*
	 * Constructor
	 */

	public ShellParser(String[] pattern) {
		if (pattern != null && pattern.length > 0) {
			this.pattern = pattern;
		}
	}

	@Override
	public String getLanguageName() {
		return languageName;
	}

	@Override
	public boolean accept(java.io.File file) {
		String path = file.getName().toLowerCase();
		for (String s : this.pattern) {
			if (FilenameUtils.wildcardMatch(path, s)) {
                		return true;
            		}
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
			indexedCodeFile.setComment(parseComment(text));
		} catch (IOException e) {
			logger.warning("Error in reading file " + f.getAbsolutePath());
		}
		return indexedCodeFile;
	}

	@Override
	public void close() {
	}

	public List<Integer> parseComment(char[] text) {
		return parseComment(text, 0, text.length);
	}

	public List<Integer> parseComment(char[] text, int sp, int ep) {
		boolean escape = false;
		boolean squote = false;
		boolean dquote = false;
		boolean bquote = false;
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
				/* back quote */
				} else if (bquote) {
					if (ch == '`') {
						bquote = false;
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
				/* back quote */
				} else if (ch == '`') {
					bquote = true;
					continue;
				}
				if (ch == '#') {
					list.add(i);
					state++;
				}
			} else {
				if (ch == '\n') {
					list.add(i);
					state = 0;
				}
			}
		}
		if (state > 1) {
			list.add(ep);
		}
		return list;
	}
}
