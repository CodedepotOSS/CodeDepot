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
package jp.co.sra.codedepot.parser.text;

import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.UniversalReader;
import jp.co.sra.codedepot.util.FileTypeDetect;

import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

public class TextParser extends Parser {

  	private static Logger logger = Logger.getLogger(TextParser.class.getName());
	private static String languageName = new String("text");
        private String[] pattern = { "*.txt", "*.md", "*.rst",
                                     "*.js", "*.css", "*.html",
                                     "*.xml", "*.properties", "readme*" };
        private String[] ignores = {"*.exe", "*.dll", "*.cab", "*.msi",
                                    "*.zip", "*.jar", "*.rar", "*.tar",
                                    "*.lzh", "*.7z", "*.gz", "*.bz2"};

	/*
	* Constructor.
	*/

	public TextParser(String[] pattern) {
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

                for (String s : this.ignores) {
                        if (FilenameUtils.wildcardMatch(path, s)) {
                                return false;
                        }
                }
		for (String s : this.pattern) {
			if (FilenameUtils.wildcardMatch(path, s)) {
				return isTextFile(file);
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
		} catch (IOException e) {
			logger.warning("Error in reading file " + f.getAbsolutePath());
		}
		return indexedCodeFile;
	}

	@Override
	public void close() {
	}

	/*
         * check document type
         */
	public boolean isTextFile(java.io.File file) {
                try {
                        FileInputStream stream = new FileInputStream(file);
        		byte[] buf = new byte[8192];
        		int nread = stream.read(buf);
                        stream.close();

			for (int i = 0; i < nread; i++) {
				int ch = buf[i] & 0xFF;
				if (ch < 0x07) {
					return false;
				}
				if (ch > 0x0f && ch < 0x20 && ch != 0x1b) {
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			logger.warning("Error in reading file " + file.getAbsolutePath());
			return false;
		}
	}
}
