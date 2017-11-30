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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.solr.Indexer;

public class IndexedClass extends IndexedCode {
	private StringBuilder comment = new StringBuilder("");
	// code ids and string literals
	private StringBuilder codeText = new StringBuilder("");
	// hold the source code of the method, used for hightlighting purpose
	private String src = "";
	// declaring class
	private String className = "";
	private List<IndexedMethod> declaredMethods;
	private IndexedMethod currentMethod;
	private IndexedCodeFile icf;
	private int start = 1;

	// all call invocations
	private StringBuilder methodInvokeSeq = new StringBuilder("");

	public IndexedClass(IndexedCodeFile icf) {
		declaredMethods = new ArrayList<IndexedMethod>();
		currentMethod = null;
		this.icf = icf;
	}

	public String getId() {
		return (icf.getId() + "#"+ className);
	}

	public void setClassName(String cls) {
		className = cls;
	}

	public String getClassName() {
		return this.className;
	}

	public void createNewMethod() {
		currentMethod = new IndexedMethod(icf,this);
		declaredMethods.add(currentMethod);
	}

	public List<IndexedMethod> getDeclaredMethods() {
		return declaredMethods;
	}

	public String getDeclaredMethodIds() {
		StringBuilder mids = new StringBuilder();

		for (IndexedMethod m : getDeclaredMethods()) {
			if (mids.length() > 0) {
				mids.append(" ");
			}
			mids.append(m.getMethodName());
		}
		return mids.toString();
	}

	public String getMethodInvokeSeq() {
		return methodInvokeSeq.toString();
	}

	public void addMethodInvokeSeq(String methodName) {
		if (methodInvokeSeq.length() > 0) {
			methodInvokeSeq.append(" ");
		}
		methodInvokeSeq.append(methodName);
	}

	public void setSrc(int startoffset, int length) {
		src = new String(icf.getProgramText(), startoffset, length);
	}

	public String getSrc() {
		return this.src;
	}

	public void addCodeText(String codeFrag) {
		this.codeText.append(codeFrag);
		this.codeText.append(" ");
	}

	public String getCodeText() {
		return codeText.toString();
	}

	public void setComment(int startOffset, int length) {
		comment.append(icf.getProgramText(), startOffset, length);
	}

	public void setComment(StringBuilder comment) {
		this.comment.append(comment);
		this.comment.append('\n');
	}

	public String getComment() {
		return this.comment.toString();
	}
	public IndexedMethod getCurrentIndexedMethod() {
		return currentMethod;
	}

	public void setStart(int sline) {
		start = sline;
	}

	public String getCloneToken() {
		return icf.getCloneToken(this.src);
	}

	public int getStart(){
		return start;
	}

	public String toXML() {
		return toXml(this);
	}

        @Override
	public Map<String, String> getFields() {
	    HashMap<String, String> map = new HashMap<String,String>();
	    map.put(Indexer.ID, getId());
	    map.put(Indexer.LANG, "java");
	    map.put(Indexer.UNIT, "class");
	    map.put(Indexer.BEGIN, Integer.toString(getStart()));
	    map.put(Indexer.PKG, icf.getPackageName());
	    map.put(Indexer.CLS, className);
	    map.put(Indexer.COMMENT, getComment());
	    map.put(Indexer.FDEF, getDeclaredMethodIds());
	    map.put(Indexer.FCALL, getMethodInvokeSeq());
	    map.put(Indexer.CODE, getCodeText());
	    map.put(Indexer.SRC,  getSrc());
	    map.put(Indexer.CLONETKN,  getCloneToken());
	    return map;
        }

        @Override
        public List<IndexedCode> getChildren() {
                List<IndexedMethod> list = getDeclaredMethods();
                List<IndexedCode> children = new ArrayList();
                if (list != null) {
                        for (IndexedMethod m : list) {
                                children.add((IndexedCode) m);
                        }
                }
                return children;
        }

        @Override
	public String getSourceText() {
	    return getSrc();
	}
}
