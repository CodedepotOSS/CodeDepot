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
package jp.co.sra.codedepot.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.SimpleOrderedMap;

/**
 * Keep information of a matched document for clone search.
 * Such information is returned.
 * @author ye
 *
 */
public class CloneMatchDoc {

	public final static String START = "start";
	public final static String END = "end";

	int _docId; // document number in the index
	List<CloneMatchFragment> _matchFrags;
	float _score;
	// int _paddingLineNo;

	private String _src; //source of the document, a pointer
	private int _offset; //number of the first line of the document

	public CloneMatchDoc(int docNum) {
		//super();
		_docId = docNum;
		// _paddingLineNo = paddingLineNo;
		_matchFrags = new ArrayList<CloneMatchFragment>();
	}

	public int getDocId() {
		return _docId;
	}


	public void addMatchFrag(String src, int offset, int startChar, int endChar) {
		_src = src;
		_offset = offset;
		Iterator<CloneMatchFragment> iter = _matchFrags.iterator();
		while (iter.hasNext()) {
			CloneMatchFragment f = iter.next();
			int s = f.getStartChar(), e = f.getEndChar();
			if (startChar > s) {
				if (endChar <= e) {
					//subsumed, no action
					return;
				} else {
					//  s chStart e chEnd OR s e chStart chEnd
					continue;
				}
			} else if (startChar == s) {
				if (endChar <= e) {
					//subsumed, no action
					return;
				} else {
					//extend end
					f.setEndChar(endChar);
					f.setEndLine(toLineNum(endChar));
					f.setSrcFrag(src.substring(startChar, endChar));
					return;
				}
			} else {
				if (endChar <= e) {
					//current fragment is longer
					f.setStartChar(startChar);
					f.setStartLine(toLineNum(startChar));
					f.setSrcFrag(src.substring(startChar, endChar));
					return;
				} else {
					//chStart < s < e < chEnd
					f.setStartChar(startChar);
					f.setStartLine(toLineNum(startChar));
					f.setEndChar(endChar);
					f.setEndLine(toLineNum(endChar));
					f.setSrcFrag(src.substring(startChar, endChar));
					return;
				}
			}
		}
		_matchFrags.add(new CloneMatchFragment(src.substring(startChar, endChar),
				toLineNum(startChar), toLineNum(endChar),
				startChar, endChar));
		}

	/*
	public void addMatchFrag(CloneMatchFragment frag) {
		_matchFrags.add(frag);
	}
	*/

	private int toLineNum(int chpos) {
		return _offset + Utils.countChar(_src, '\n', 0, chpos);
		//lnEnd = lnStart + StringUtils.countChar(src, '\n', start, end); //this line is inclusive
	}


	public float getScore() {
		return _score;
	}

	public void setScore(float _score) {
		this._score = _score;
	}


	/**
	 * return a list of matching code clones for
	 * one file
	 * [{src: "....", start: line1, end: line2} ... ]
	 * @return
	 */
	public NamedList[] getCloneSummary() {
		int size = _matchFrags.size();
		NamedList[] res = new NamedList[size];
		for (int i = 0; i < size; i++ ) {
			res[i] = _matchFrags.get(i).toNamedList();
		}
		return res;
	}
}

