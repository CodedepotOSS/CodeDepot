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
package jp.co.sra.codedepot.parser.pygments;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String lang;
    private char[] text;
    private LineRead lineRead;
    private List<Token> token;
    private List<String> clone;

    private static final Logger logger = LoggerFactory.getLogger(IndexedCodeFile.class);

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

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLang(){
        return this.lang;
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

    public void setToken(List<Token> token) {
        this.token = token;
    }

    public List<Token>getToken() {
        return this.token;
    }

    public void setClone(List<String> clone) {
        this.clone = clone;
    }

    public List<String> getClone() {
        return this.clone;
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
            while (idx < this.token.size()) {
                Token t = this.token.get(idx);
                int sp = t.getOffset();
                int ep = sp + t.getValue().length();
                if (sp > eol) {
                    break;
                }
                if (sp > cur) {
                    String s = srcline.substring(cur - bol, sp - bol);
                    html.append(HTMLConvert.convertIntoHTML(s));
                    cur = sp;
                }
                if (ep >= bol && ep <= eol) {
                    String s = srcline.substring(cur - bol, ep - bol);
                    String css = this.getCssClass(t.getKind());
                    if (css.length() > 0) {
                        html.append("<span class='" + css + "'>");
                        html.append(HTMLConvert.convertIntoHTML(s));
                        html.append("</span>");
                    } else {
                        html.append(HTMLConvert.convertIntoHTML(s));
                    }
                    cur = ep;
                    idx += 1;
                } else {
                    String s = srcline.substring(cur - bol, eol - bol);
                    String css = this.getCssClass(t.getKind());
                    if (css.length() > 0) {
                        html.append("<span class='" + css + "'>");
                        html.append(HTMLConvert.convertIntoHTML(s));
                        html.append("</span>");
                    } else {
                        html.append(HTMLConvert.convertIntoHTML(s));
                    }
                    cur = eol;
                    break;
                }
            }
            if (cur < eol) {
                String s = srcline.substring(cur - bol, eol - bol);
                html.append(HTMLConvert.convertIntoHTML(s));
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
        map.put(Indexer.LANG, this.getLang());
        map.put(Indexer.BEGIN, Integer.toString(1));
        map.put(Indexer.SRC,  getSourceText());
        map.put(Indexer.CODE, extractCode());
        map.put(Indexer.PKG, "");
        map.put(Indexer.CLS, extractClassName());
        map.put(Indexer.FDEF, extractFunctionName());
        map.put(Indexer.FCALL, extractFunctionCall());
        map.put(Indexer.COMMENT, extractComment());
        map.put(Indexer.CLONETKN, getCloneToken());
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

    private String getCssClass (String kind) {
        if (kind.startsWith("Comment")) {
            return "cmt";
        }
        if (kind.endsWith(".Doc")) {
            return "cmt";
        }
        if (kind.startsWith("Keyword")) {
            return "keyword";
        }
        if (kind.startsWith("Name.Call")) {
            return "fcall";
        }
        if (kind.startsWith("Name.")) {
            return "def";
        }
        if (kind.startsWith("Operator")) {
            return "operator";
        }
        if (kind.startsWith("Punctuation")) {
            return "operator";
        }
        if (kind.startsWith("Literal")) {
            return "literal";
        }
        return "";
    }

    private String extractToken(String[] kinds) {
        StringBuilder sb = new StringBuilder();
        Iterator<Token> it = this.token.iterator();

        while (it.hasNext()) {
            Token t = it.next();
            for (int i = 0; i < kinds.length; i++) {
                if (t.getKind().startsWith(kinds[i])) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(t.getValue());
                    break;
                }
            }
        }
        return sb.toString();
    }

    private String extractNamespaceName() {
        String[] kinds = { "Name.Namespace" };
        return extractToken(kinds);
    }

    private String extractClassName() {
        String[] kinds = { "Name.Class" };
        return extractToken(kinds);
    }

    private String extractFunctionName() {
        String[] kinds = { "Name.Function", "Name.Property" };
        return extractToken(kinds);
    }

    private String extractComment() {
        String[] kinds = { "Comment", "Literal.String.Doc" };
        return extractToken(kinds);
    }

    private String extractLiteral() {
        String[] kinds = { "Literal" };
        return extractToken(kinds);
    }

    private String extractFunctionCall() {
        String[] kinds = { "Name.Call" };
        return extractToken(kinds);
    }

    private String extractCode() {
        StringBuilder sb = new StringBuilder();
        Iterator<Token> it = this.token.iterator();

        while (it.hasNext()) {
            Token t = it.next();
            if (t.getKind().endsWith(".Doc")) {
                continue;
            }
            if (t.getKind().startsWith("Comment")) {
                continue;
            }
            if (t.getKind().startsWith("Error")) {
                continue;
            }
            if (t.getKind().startsWith("Text")) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(t.getValue());
        }
        return sb.toString();
    }

    private String getCloneToken() {
        StringBuilder sb = new StringBuilder();

        Iterator<String> it = this.clone.iterator();

        while (it.hasNext()) {
            String t = it.next();
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(t);
        }
        return sb.toString();
    }

}
