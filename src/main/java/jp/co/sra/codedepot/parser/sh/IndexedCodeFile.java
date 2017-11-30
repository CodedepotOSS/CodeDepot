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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	private List<Integer>comment;

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
		return this.text;
	}

	public void setComment(List<Integer> comment) {
		this.comment = comment;
 	}

	public List<Integer>getComment() {
		return this.comment;
 	}

	public String toXML() {
		return toXml(this);
	}

	@SuppressWarnings("unchecked")
	public String toHTML() {
		StringBuilder html = new StringBuilder();
		List<String> srclst = lineRead.getSrcList();
		List<Integer> loclst = lineRead.getLocList();

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

		int idx = 0;
		for (int i = 1; i <= lines; i++) {
			Integer lnkey = new Integer(i);
			String srcline = srclst.get(i - 1);
			int bol = loclst.get(i - 1).intValue();
			int eol = bol + srcline.length();

			int cur = bol;
			html.append(HTMLConvert.markupstmt(i, uuid));
			while (idx < this.comment.size()) {
				int pos = this.comment.get(idx).intValue();
				if (pos >= bol && pos <= eol) {
					String s = srcline.substring(cur - bol, pos - bol);
					if (idx % 2 == 0) {
						html.append(HTMLConvert.convertIntoHTML(s));
						String v = srcline.substring(pos - bol, eol - bol);
					} else {
						html.append("<span class='cmt'>");
						html.append(HTMLConvert.convertIntoHTML(s));
						html.append("</span>");
					}
					cur = pos;
					idx++;
				} else {
					break;
				}
			}
			if (cur < eol) {
				String s = srcline.substring(cur - bol, eol - bol);
				if (idx % 2 == 0) {
					html.append(HTMLConvert.convertIntoHTML(s));
				} else {
					html.append("<span class='cmt'>");
					html.append(HTMLConvert.convertIntoHTML(s));
					html.append("</span>");
				}
			}
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
            	map.put(Indexer.SRC,  getSourceText());
            	map.put(Indexer.CODE, removeComment());
            	map.put(Indexer.COMMENT, extractComment());
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

	private String removeComment() {
		String s = new String(this.text);
		StringBuilder sb = new StringBuilder();

		int cur = 0;
		Iterator<Integer> it = this.comment.iterator();
		while (it.hasNext()) {
			int pos = it.next().intValue();
			if (cur != pos) {
				sb.append(s.substring(cur, pos));
			}
			if (it.hasNext()) {
				cur = it.next().intValue();
			} else {
				cur = pos;
			}
		}
		if (cur < s.length()) {
			sb.append(s.substring(cur));
		}
		return sb.toString();
	}

	private String extractComment() {
		String s = new String(this.text);
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> it = this.comment.iterator();
		while (it.hasNext()) {
			int beg = it.next().intValue();
			if (it.hasNext()) {
				int end = it.next().intValue();
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(s.substring(beg, end));
			}
		}
		return sb.toString();
	}
}
