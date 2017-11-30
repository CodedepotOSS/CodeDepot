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
package jp.co.sra.codedepot.parser.doc;

import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.File;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.UniversalReader;
import jp.co.sra.codedepot.util.FileTypeDetect;

import org.apache.tika.detect.Detector;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.metadata.Metadata;

import org.apache.tika.parser.Parser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.exception.TikaException;

import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.commons.io.FilenameUtils;
import java.util.logging.Logger;


public class DocParser extends jp.co.sra.codedepot.parser.Parser {

  	private static Logger logger = Logger.getLogger(DocParser.class.getName());
	private static String languageName = new String("doc");
        private String[] pattern = {"*.doc", "*.xls", "*.ppt", "*.docx", "*.xlsx", "*.pptx",
                                    "*.odt", "*.ods", "*.odp", "*.sxw", "*.sxc", "*.sxi",
                                    "*.pdf" };
        private String[] ignores = {"*.exe", "*.dll", "*.cab", "*.msi",
                                    "*.zip", "*.jar", "*.rar", "*.tar",
                                    "*.lzh", "*.7z", "*.gz", "*.bz2"};

        /*
        * Constructor.
        */

        public DocParser(String[] pattern) {
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
				return isDocFile(file);
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
	public IndexedCodeFile parse(String fid, String uuid, File file) throws Exception {
		IndexedCodeFile indexedCodeFile = null;
		InputStream stream = null;

		try {
			stream = new FileInputStream(file);
			InputStream input = new BufferedInputStream(stream);

			Detector detector = new DefaultDetector();
			Parser parser = new AutoDetectParser(detector);

			StringWriter writer = new StringWriter();
			ContentHandler handler = new BodyContentHandler(writer);

			Metadata metadata = new Metadata();

			ParseContext context = new ParseContext();
			context.set(Parser.class, parser);

			parser.parse(input, handler, metadata, context);
			String text = handler.toString();

			indexedCodeFile = new IndexedCodeFile();
			indexedCodeFile.setId(fid);
			indexedCodeFile.setUuid(uuid);
			indexedCodeFile.setName(file.getName());
			indexedCodeFile.setText(text.trim());
			indexedCodeFile.setMeta(metadata);
		} catch (IOException e) {
			logger.warning("Error in reading file " + file.getAbsolutePath());
		} catch (NoClassDefFoundError e) {
			logger.warning("Error in parsing file " + file.getAbsolutePath());
		} catch (NoSuchMethodError e) {
			logger.warning("Error in parsing file " + file.getAbsolutePath());
		} catch (SAXException e) {
			logger.warning("Error in parsing file " + file.getAbsolutePath());
		} catch (TikaException e) {
			logger.warning("Error in parsing file " + file.getAbsolutePath());
		} finally {
			try {
				if (stream != null) {
		 			stream.close();
				}
			} catch (IOException e) {
				;
			}
		}
		return indexedCodeFile;
	}

	@Override
	public void close() {
	}

        /*
         * check document type
         */
	public boolean isDocFile(java.io.File file) {
		try {
			String mtype = FileTypeDetect.detect(file);
			if (mtype == null) {
				return false;
			}

			if (!mtype.startsWith("application/")) {
				return false;
			}

			String subtype = mtype.substring(12);
			if (subtype.compareTo("pdf") == 0) {
				return true;
			}
			if (subtype.compareTo("x-tika-msoffice") == 0) {
				return true;
			}
			if (subtype.compareTo("x-tika-ooxml") == 0) {
				return true;
			}
			if (subtype.startsWith("vnd.oasis.opendocument.")) {
				return true;
			}
		} catch (IOException e) {
			logger.warning("Error in reading file " + file.getAbsolutePath());
			return false;
		}
		return false;
        }
}
