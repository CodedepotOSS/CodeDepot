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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.parser.IndexedCode;

/**
 * Indexing a method
 * @author yunwen,fang
 * @version $Id: IndexedMethod.java 2342 2017-11-09 05:36:32Z fang $
 */
public class IndexedMethod extends IndexedCode {
	// fields for method only
	private String returnType = "";
	// in the order of alphabetic, separated by space
	private StringBuilder inputTypes = new StringBuilder("");
	// method name
	private String methodName = "";
	private String className = "";
	// sequence of method call
	private StringBuilder methodInvokeSeq = new StringBuilder("");
	// doc comment of the method
	private StringBuilder comment = new StringBuilder("");
	// code ids and string literals
	private StringBuilder codeText = new StringBuilder("");
	// hold the source code of the method, used for hightlighting purpose
	private String src = "";
	// declaring class
	private IndexedCodeFile icf;
	private IndexedClass icls;
	private int mstart = 1;

	public IndexedMethod(IndexedCodeFile icf) {
		this.icf = icf;
	}

	public IndexedMethod(IndexedCodeFile icf,IndexedClass icls) {
		this.icf = icf;
		this.icls = icls;
	}

	public void setstart(int sline){
		mstart = sline;
	}
	public int getStart() {
		return mstart;
	}
	public String toXML() {
		return toXml(this);
	}

	public String getId() {
		return (icf.getId() + "#" + className + "#" + methodName + "(" + getInputTypes() + ")");
	}

	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Return the list of types of arguments of the method.
	 * @return
	 */
	public String getInputTypes() {
		//remove the last space if not empty
		if (inputTypes.length() == 0) {
			return "";
		} else {
			return inputTypes.substring(0, inputTypes.length()-1);
		}
	}

	/*
	 * full name of each type is used whenever possible
	 * input types are listed in its defined order, each
	 * type is separated by a space
	 */
	public void addInputType(String inputType) {
		this.inputTypes.append(inputType);
		this.inputTypes.append(" ");
	}
	/*
	public String getClassName() {
		return className;
	}
	*/
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodInvokeSeq() {
		if (methodInvokeSeq.length() > 0) {
			methodInvokeSeq.deleteCharAt(methodInvokeSeq.length()-1);
		}
		return methodInvokeSeq.toString();
	}

	public void addMethodInvokeSeq(String methodName) {
		methodInvokeSeq.append(methodName);
		methodInvokeSeq.append(" ");
		icls.addMethodInvokeSeq(methodName);
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
		comment.append('\n');
	}

	public String getComment() {
		return comment.toString();
	}

	public void setSrc(int startOffset, int length) {
		this.src = new String(icf.getProgramText(), startOffset, length);
	}

	public String getSrc() {
		return this.src;
	}

	public String getCloneToken() {
		return icf.getCloneToken(this.src);
	}

        @Override
	public Map<String, String> getFields() {
	    HashMap<String, String> map = new HashMap<String,String>();
	    map.put(Indexer.ID, getId());
	    map.put(Indexer.LANG, "java");
	    map.put(Indexer.UNIT, "method");
	    map.put(Indexer.BEGIN, Integer.toString(getStart()));
	    map.put(Indexer.PKG, icf.getPackageName());
	    map.put(Indexer.CLS, className);
	    map.put(Indexer.FDEF, methodName);
	    map.put(Indexer.OUT_TYPE, getReturnType());
	    map.put(Indexer.IN_TYPES, getInputTypes());
	    map.put(Indexer.FCALL, getMethodInvokeSeq());
	    map.put(Indexer.COMMENT, getComment());
	    map.put(Indexer.CODE, getCodeText());
	    map.put(Indexer.SRC, getSrc());
	    map.put(Indexer.CLONETKN, getCloneToken());
	    return map;
        }

        @Override
        public List<IndexedCode> getChildren() {
                return null;
        }

        @Override
        public String getSourceText() {
            return getSrc();
        }
}
