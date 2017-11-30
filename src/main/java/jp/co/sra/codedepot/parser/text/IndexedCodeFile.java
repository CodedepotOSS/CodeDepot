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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.util.LineRead;

/**
 *
 */
public class IndexedCodeFile extends IndexedCode {
	private String id;
	private String uuid;
	private char[] text;
	private LineRead lineRead;

	IndexedCodeFile() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId(){
		return this.id;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid(){
		return this.uuid;
	}

	public void setLineRead(LineRead lineread) {
		this.lineRead = lineread;
	}

	public LineRead getLineRead() {
		return this.lineRead;
	}


	public void setText(char[] text) {
		this.text = text;
	}

	public char[] getText() {
		return text;
	}

	public String toXML() {
		return toXml(this);
	}

	@SuppressWarnings("unchecked")
	public String toHTML() {
		StringBuilder html = new StringBuilder();
		List<String> srclst = lineRead.getSrcList();
		html.append("<div id=\"ln");
		html.append(uuid);
		html.append("\" class=\"lnPane\">\n");
		html.append("<pre>\n");
		int lines = srclst.size();
		for (int i = 1; i <= lines; i++) {
			html.append("<span id=\"ln");
			html.append(i);
			html.append(uuid);
			html.append("\">");
			html.append(i);
			html.append("</span>\n");
		}
		html.append("</pre>\n");
		html.append("</div>\n");
		html.append("<div id=\"lc");
		html.append(uuid);
		html.append("\" class=\"codePane\">\n");
		html.append("<pre>\n");

		for (int i = 1; i <= lines; i++) {
			Integer lnkey = new Integer(i);
			String srcline = srclst.get(i - 1);
			html.append(HTMLConvert.markupstmt(i, uuid));
			html.append(HTMLConvert.convertIntoHTML(srcline));
			html.append("</span>");
			html.append("\n");
		}
		html.append("</pre>\n");
		html.append("</div>\n");
		return html.toString();
	}

	@Override
	public Map<String, String> getFields() {
		HashMap<String, String> map = new HashMap<String,String>();
		map.put(Indexer.ID, getId());
		map.put(Indexer.UNIT, "file");
		map.put(Indexer.LANG, "text");
            	map.put(Indexer.BEGIN, Integer.toString(1));
            	map.put(Indexer.SRC, getSourceText());
            	map.put(Indexer.COMMENT, getSourceText());
            	return map;
	}

    	@Override
	public List<IndexedCode> getChildren() {
		return null;
	}

    	@Override
	public String getSourceText() {
		return new String(this.text);
	}

}
