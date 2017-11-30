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
	private List<Integer>code;

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

	public void setCode(List<Integer> code) {
		this.code = code;
	}

	public List<Integer> getCode() {
		return code;
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

		String cls = "";
		for (int i = 1; i <= lines; i++) {
			Integer lnkey = new Integer(i);
			String srcline = srclst.get(i - 1);
			int bol = loclst.get(i - 1).intValue();
			int eol = bol + srcline.length();

			int cur = bol;
			html.append(HTMLConvert.markupstmt(i, uuid));
			while (idx < this.code.size()) {
				int pos = this.code.get(idx).intValue();
				if (pos >= bol && pos <= eol) {
					String s = srcline.substring(cur - bol, pos - bol);
					if (idx % 2 == 0) {
						html.append(HTMLConvert.convertIntoHTML(s));
						String v = srcline.substring(pos - bol, eol - bol);
						if (v.startsWith("<%--")) {
							cls = "cmt";
						} else {
							cls = "script";
						}
					} else {
						html.append("<span class='" + cls + "'>");
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
					html.append("<span class='" + cls + "'>");
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
		map.put(Indexer.LANG, "java");
            	map.put(Indexer.BEGIN, Integer.toString(1));
            	map.put(Indexer.SRC,  getSourceText());
            	map.put(Indexer.CODE,  removeComment(getCodeText()));
            	map.put(Indexer.COMMENT,  getCommentText());
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

	public String getCodeText() {
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> it = this.code.iterator();
		while (it.hasNext()) {
			int start = it.next().intValue();
			if (it.hasNext()) {
				int end = it.next().intValue();
				String s = new String(this.text, start, end - start);
				if (!isComment(s)) {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append(s);
				}
			}
		}
		return sb.toString();
	}

	public String getCommentText() {
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> it = this.code.iterator();
		while (it.hasNext()) {
			int start = it.next().intValue();
			if (it.hasNext()) {
				int end = it.next().intValue();
				String s = new String(this.text, start, end - start);
				if (isComment(s)) {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append(s);
				}
			}
		}
		return sb.toString();
	}

	private boolean isComment(String s) {
		if (s.length() < 8) {
			return false;
		}
		if (s.startsWith("<%--") && s.endsWith("--%>")) {
			return true;
		}
		return false;
	}

	private String removeComment(String s) {
		JspParser parser = new JspParser();
		List<Integer>comment = parser.parseComment(s.toCharArray());
		StringBuilder sb = new StringBuilder();

		int cur = 0;
		Iterator<Integer> it = comment.iterator();
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

	private String extractComment(String s) {
		JspParser parser = new JspParser();
		List<Integer>comment = parser.parseComment(s.toCharArray());
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> it = comment.iterator();
		while (it.hasNext()) {
			int sp = it.next().intValue();
			if (it.hasNext()) {
				int ep = it.next().intValue();
				sb.append(s.substring(sp, ep));
			}
		}
		return sb.toString();
	}
}
