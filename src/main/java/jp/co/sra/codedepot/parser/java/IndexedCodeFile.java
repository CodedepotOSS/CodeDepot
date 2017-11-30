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
package jp.co.sra.codedepot.parser.java;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.io.StringReader;
import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;

import jp.co.sra.codedepot.index.ProgramAnalyzer;
import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.util.LineRead;

/**
 * Containing all the text contexts that are going to be indexed. Separated in
 *
 * @author yunwen,fang
 * @version $Id: IndexedCodeFile.java 2342 2017-11-09 05:36:32Z fang $
 */
public class IndexedCodeFile extends IndexedCode {
	private char[] programText;

	// src line number info
	private LineRead lineRead = new LineRead("");
	private HashMap<Integer, StringBuilder> srcLines = new HashMap<Integer, StringBuilder>();

	// fields for indexing
	// common fields for both fields

	private String id = "";
	private String uuid = "";
	private String project = ""; // the name of the projects
	private String license = "";
	// the name of the package
	private String packageName = "";
	// the name of the class
	private String className = "";
	// the name of the file
	private String location = "";
	// all comments concatenated together
	private StringBuilder comments = new StringBuilder("");
	private HashMap<Integer, StringBuilder> commentLines = new HashMap<Integer, StringBuilder>();
	// all variables arguments and string literals, including class names and
	// method names
	private StringBuilder codeText = new StringBuilder("");

	// fields for class only
	// list of methods defined; each method has a full name
	// package.class#method(arg_type1, arg_type2, arg_type3)
	// and the argument types are sorted alphabetically
	private StringBuilder declaredMethodText = new StringBuilder("");

	/*
	 * The following four lines are created for innerHtml.
	 */
	private HashMap<Integer, HashMap> keywordList = new HashMap<Integer, HashMap>();
	private HashMap<Integer, Integer> commentList;
	private HashMap <Integer, String> fdefList = new HashMap<Integer,String>();
	private List<Integer> fdefend = new ArrayList<Integer>();
	private HashMap <Integer,String> clsdefList = new HashMap<Integer,String>();
	private List<Integer> clsEnd = new ArrayList<Integer>();


	private List<IndexedClass> declaredClasses;
	private IndexedClass currentClass;
	private List allCommentList = new ArrayList();

	IndexedCodeFile() {

		declaredClasses = new ArrayList<IndexedClass>();
		currentClass = null;

	//	declaredMethods = new ArrayList<IndexedMethod>();
	//	currentMethod = null;
	}

	public HashMap<Integer, StringBuilder> getSrcLines() {
		return srcLines;
	}

	public StringBuilder getSrcLine(int ln) {
		return srcLines.get(new Integer(ln));
	}

	public void setSrcLines(int line, StringBuilder lineBuf) {
		//StringBuilder currentBuf = new StringBuilder(" ");
		Integer sline = new Integer(line);
		if (srcLines.containsKey(sline)) {
			StringBuilder currentBuf = new StringBuilder(srcLines.get(sline));
			currentBuf.append(lineBuf);
			srcLines.put(sline, currentBuf);
		} else {
			srcLines.put(sline, lineBuf);
		}
	}

	public void insertSrcLines(int line, StringBuilder lineBuf) {
		StringBuilder currentBuf = new StringBuilder(" ");
		Integer sline = new Integer(line);
		if (srcLines.containsKey(sline)) {
			currentBuf = lineBuf;
			currentBuf.append(srcLines.get(sline));
			srcLines.put(sline, currentBuf);
		} else {
			srcLines.put(sline, lineBuf);
		}
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

	/**
	 * Set the name of of the project. Project name is read from command line
	 * arguments passed to
	 *
	 * @link Indexer.java. If not set through command line, it is empty.
	 *       Converted html files of source program from the
	 *       <code>project</code> is stored in
	 *       <code>data/html/project/package/Class.Java.html</code>
	 * @param prj
	 */
	public void setProject(String prj) {
		project = prj;
	}

	/**
	 * Return the name of the project. It may contain space. Searched as a
	 * string
	 *
	 * @return
	 */
	public String getProject() {
		return project;
	}

	public void setProgramText(char[] programText) {

		this.programText = programText;

	}

	/**
	 * changed for processing dos-file
	 */
	public char[] getProgramText() {
		char[] pText = programText;
		for (int i = 0; i < pText.length - 1; i++) {
			if (pText[i] == '\r' && pText[i + 1] == '\n') {
				pText[i] = ' ';
			}
		}
		return pText;
		// return programText;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getComments() {
		return comments.toString();
	}

	public void setCommentList(HashMap commentList) {
		this.commentList = new HashMap();
		this.commentList = commentList;
	}

	public HashMap getCommentList() {
		return commentList;
	}


	public HashMap<Integer, StringBuilder> getCommentLines() {
		return commentLines;
	}

	/**
	 * Contents of non-doc comments are not carried by the AST node, so we need
	 * to get the contents by referring to the program text
	 *
	 * @param start
	 * @param length
	 */
	public void addComment(int start, int length) {
		comments.append(programText, start, length);
		comments.append('\n');
	}

	public void addCommentLines(int line, StringBuilder cLineBuf) {
		Integer sline = new Integer(line);
		commentLines.put(sline, cLineBuf);
	}

	public String getCodeText() {
		return codeText.toString();
	}

	private short codeTextLength = 0;

	public void addCodeText(String codeText) {
		this.codeText.append(codeText);
		this.codeText.append(" ");
		if (codeTextLength == 8) {
			this.codeText.append("\n"); // add readability to xml file
			codeTextLength = 0;
		} else {
			codeTextLength++;
		}
	}

	public String getDeclaredMethodsText() {
		return declaredMethodText.toString();
	}

	public void setDefinedMethodsText(String definedMethodsText) {
		if (declaredMethodText.length() > 0) {
			this.declaredMethodText.append(" ");
		}
		this.declaredMethodText.append(definedMethodsText);
	}

	public void createNewClass() {
		currentClass = new IndexedClass(this);
		declaredClasses.add(currentClass);
	}
	public IndexedClass getCurrentIndexedClass() {
		return currentClass;
	}

	public List<IndexedClass> getDeclaredClasses() {
		return declaredClasses;
	}

	public void setKeywordList(HashMap<Integer, HashMap> kList) {
		//keywordList = new HashMap<Integer, HashMap>();
		keywordList = kList;
	}

	public HashMap<Integer, HashMap> getKeywordList() {
		return keywordList;
	}


	public void setfdef(int sline, StringBuilder fdef) {
		fdefList.put(new Integer(sline), fdef.toString());
	}

	public void setfdefend(int endline) {
		fdefend.add(new Integer(endline));
	}

	public void setclsdef(int sline, StringBuilder clsdef) {
		clsdefList.put(new Integer(sline), clsdef.toString());
	}

	public void setclsend(int endline) {
		clsEnd.add(new Integer(endline));
	}

	public String toXML() {
		return toXml(this);
	}

	@SuppressWarnings("unchecked")
	public String toHTML() {
		StringBuilder html = new StringBuilder();
		//HashMap<Integer, StringBuilder> srclines = getSrcLines();
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

		HashMap<Integer, String> keywords;
		for (int i = 1; i <= lines; i++) {
			Integer lnkey = new Integer(i);
			int ccolstart, scolstart = 0;
			char[] srcchar;
			String srcline = srclst.get(i - 1).toString();
			//append cls tag
			if (clsdefList.containsKey(lnkey)) {
				html.append(clsdefList.get(lnkey).toString());
			}
			// append fdef tag
			if (fdefList.containsKey(lnkey)) {
				html.append(fdefList.get(lnkey).toString());
			}
			html.append(HTMLConvert.markupstmt(i, uuid));
			// process for statement + comment

			if (commentList.containsKey(lnkey)
					&& keywordList.containsKey(lnkey)) {
				ccolstart = ((Integer) commentList.get(lnkey)).intValue();
				keywords = new HashMap<Integer, String>();
				keywords = keywordList.get(lnkey);
				srcchar = srcline.substring(0, ccolstart).toCharArray();
				// System.out.println("line-src-comment
				// "+i+"-"+srcline.substring(0,ccolstart)+" - "+ccolstart);
				html.append(HTMLConvert.convertKeyword(srcchar, keywords));
				// System.out.println(hconvert.convertKeyword(srcchar,keywords)+"cmt
				// -->"+srcline.substring(ccolstart, srcline.length()));
				html.append("<span class=\"cmt\">");
				if (ccolstart < srcline.length()) {
					html.append(HTMLConvert.convertIntoHTML(srcline.substring(
							ccolstart, srcline.length())));
				}
				html.append("</span>");
			} else { //Only comment lines
				if (commentList.containsKey(lnkey)) {
					ccolstart = ((Integer) commentList.get(lnkey)).intValue();
					//html.append("<span class=\"cmt\">");
					if ((ccolstart > 0 )) {
							if (isSpaces(srcline.substring(0, ccolstart))){
								html.append("<span class=\"cmt\">");
								html.append(HTMLConvert.convertIntoHTML(srcline.substring(
										0, ccolstart)));
							} else {
								html.append(HTMLConvert.convertIntoHTML(srcline.substring(
										0, ccolstart)));
								html.append("<span class=\"cmt\">");
							}
					} else {
					   html.append("<span class=\"cmt\">");
					}
					if (ccolstart < srcline.length()) {
						html.append(HTMLConvert.convertIntoHTML(srcline.substring(
								ccolstart, srcline.length())));
					}
					html.append("</span>");
				} else if (keywordList.containsKey(lnkey)) {
					keywords = new HashMap<Integer, String>();
					keywords = keywordList.get(lnkey);
					srcchar = srcline.toCharArray();
					html.append(HTMLConvert.convertKeyword(srcchar, keywords));
				} else {
					html.append(HTMLConvert.convertIntoHTML(srclst.get(i - 1)
							.toString()));
				}
			}
			html.append("</span>");  // close for markupstmt
			if ((fdefend.contains(lnkey)) || (clsEnd.contains(lnkey))) {
				html.append("</span>\n");
			} else {
				html.append("\n");
			}
		}

		html.append("</pre>\n");
		html.append("</div>\n");
		return html.toString();
	}
	protected boolean isSpaces(String str) {
		boolean result = true;
		char[] strchar = str.toCharArray();

		for (int i= 0; i<str.length(); i++){
			if (!isSpace(strchar[i])) {
				result = false ;
			}
		}
		return result;
	}
	 protected boolean isSpace(char c) {
	        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
	  }

	public String getCloneToken (String src) {
                try {
                        return ProgramAnalyzer.getTokenText(ProgramAnalyzer.LANG_JAVA, src);
                } catch (java.io.IOException e) {
                        return "";
                }
        }

	public String extractDeclaredClasses() {
		StringBuilder buf = new StringBuilder();
		List<IndexedClass> list = getDeclaredClasses();
		if (list != null) {
			for (IndexedClass c : list) {
		                if (buf.length() > 0) {
					buf.append(" ");
				}
				buf.append(c.getClassName());
			}
		}
		return buf.toString();
	}

	public String extractDeclaredMethods() {
		StringBuilder buf = new StringBuilder();
		List<IndexedClass> list = getDeclaredClasses();
		if (list != null) {
			for (IndexedClass c : list) {
		                if (buf.length() > 0) {
					buf.append(" ");
				}
				buf.append(c.getDeclaredMethodIds());
			}
		}
		return buf.toString();
	}

	public String extractMethodInvokeSeq() {
		StringBuilder buf = new StringBuilder();
		List<IndexedClass> list = getDeclaredClasses();
		if (list != null) {
			for (IndexedClass c : list) {
		               	if (buf.length() > 0) {
					buf.append(" ");
				}
				buf.append(c.getMethodInvokeSeq());
			}
		}
		return buf.toString();
	}

	@Override
	public Map<String, String> getFields() {
            String src = String.valueOf(programText);

            HashMap<String, String> map = new HashMap<String,String>();
            map.put(Indexer.ID, getId());
            map.put(Indexer.LANG, "java");
            map.put(Indexer.UNIT, "file");
            map.put(Indexer.BEGIN, Integer.toString(1));
            map.put(Indexer.PKG, getPackageName());
            map.put(Indexer.CLS, extractDeclaredClasses());
            map.put(Indexer.FDEF, extractDeclaredMethods());
            map.put(Indexer.FCALL, extractMethodInvokeSeq());
            map.put(Indexer.SRC,  src);
            map.put(Indexer.CODE, getCodeText());
            map.put(Indexer.COMMENT, getComments());
            return map;
	}

    	@Override
	public List<IndexedCode> getChildren() {
		List<IndexedCode> children = new ArrayList();
		List<IndexedClass> list = getDeclaredClasses();
		if (list != null) {
			for (IndexedClass c : list) {
				children.add((IndexedCode) c);
			}
		}
		return children;
	}

    	@Override
	public String getSourceText() {
		return new String(this.programText);
	}

    public List getAllCommentList() {
    	return allCommentList;
    }
    public void setAllCommentList(List commentList) {
    	this.allCommentList = commentList;
    }

  }
