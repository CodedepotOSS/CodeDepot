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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.solr.Indexer;

import org.apache.tika.metadata.Metadata;

/**
 *
 */
public class IndexedCodeFile extends IndexedCode {
	private String id;
	private String uuid;
	private String name;
	private String text;
	private Metadata meta;

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

	public void setName(String name) {
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setMeta(Metadata meta) {
		this.meta = meta;
	}

	public Metadata getMeta() {
		return this.meta;
	}

	public String toXML() {
		return toXml(this);
	}

	@SuppressWarnings("unchecked")
	public String toHTML() {
		StringBuilder html = new StringBuilder();
		html.append("<h2 class='title'>");
		html.append(HTMLConvert.convertIntoHTML(this.getName()));
		html.append("</h2>\n");

		String text = getText();
		if (text.length() > 0) {
			try {
				text = text.substring(0, 256) + " ...";
			} catch (IndexOutOfBoundsException e) {
				;
			}
			html.append("<h3 class='subtitle'>Text:</h3>");
			html.append("<p class='abstract'>\n");
			html.append(HTMLConvert.convertIntoHTML(text));
			html.append("</p>\n");
		}

		html.append("<h3 class='subtitle'>Properties:</h3>");
		html.append("<dl class='meta'>\n");
		for (String k : this.meta.names()) {
			if (this.meta.get(k).length() > 0) {
				html.append("<dt>");
				html.append(HTMLConvert.convertIntoHTML(k));
				html.append("</dt>\n");
				for (String v : this.meta.getValues(k)) {
					html.append("<dd>");
					html.append(HTMLConvert.convertIntoHTML(v));
					html.append("</dd>\n");
				}
			}
		}
		html.append("</dl>\n");

		return html.toString();
	}

	@Override
	public Map<String, String> getFields() {
		java.lang.String[] values;

		HashMap<String, String> map = new HashMap<String,String>();
		map.put(Indexer.ID, getId());
		map.put(Indexer.UNIT, "file");
		map.put(Indexer.LANG, "text");
            	map.put(Indexer.BEGIN, Integer.toString(0));
            	map.put(Indexer.SRC, getSourceText());
            	map.put(Indexer.COMMENT, getSourceText() + " " + getMeta().toString());
            	return map;
	}

    	@Override
	public List<IndexedCode> getChildren() {
		return null;
	}

    	@Override
	public String getSourceText() {
		return this.text;
	}

}
