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

import jp.co.sra.codedepot.solr.Indexer;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.SimpleOrderedMap;

/**
 * Represent the fragment of code that matches clone search
 * If paddings before and after are needed, consider add padding info
 * by subclassing this or delegate to this class
 * @author ye
 *
 */
public class CloneMatchFragment {
	private String _matchingSrcFrag;
	private int _startLine;
	private int _endLine;

	private int _startChar;
	private int _endChar;

	CloneMatchFragment(String match, int startLine, int endLine, int startChar, int endChar) {
		_matchingSrcFrag = match;
		_startLine = startLine;
		_endLine = endLine; //last line, should be shown too
		_startChar = startChar;
		_endChar = endChar;
		//_topPadding = top;
		//_bottomPadding = bottom;
		}

	int getStartChar() { return _startChar; }
	int getEndChar() { return _endChar; }

	void setStartChar(int startChar) { _startChar = startChar; }
	void setEndChar(int endChar) { _endChar = endChar; }

	void setStartLine(int startLine) { _startLine = startLine; }
	void setEndLine(int endLine) { _endLine = endLine; }

	void setSrcFrag(String src) {
		_matchingSrcFrag = src; //src must not be shared by others
	}

	NamedList toNamedList() {
		// NamedList result = new NamedList();
		// using Namedlist will result in an JSON array instead of JSON object
		// JSON array: [Indexer.SRC, _matchingSrcFrag, CloneMatchDoc.START, _startLine, CloneMatchDoc.END, _endLine]
		// JSON object: {Indexer.SRC: _matchingSrcFrag, CloneMatchDoc.START: _startLine, CloneMatchDoc.END: _endLine}
		NamedList result = new SimpleOrderedMap();
		String s = HTMLConvert.convertIntoHTML(_matchingSrcFrag);
		//System.out.println(_matchingSrcFrag + " => " + s);
		result.add(Indexer.SRC, s);
		result.add(CloneMatchDoc.START, _startLine);
		result.add(CloneMatchDoc.END, _endLine);
		return result;
	}
}
