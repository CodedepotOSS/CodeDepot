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
/**
 *  This Class should be refactored as SourcePosition
 */
package jp.co.sra.codedepot.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class LineRead {

	/*
	 * Instance Variables.
	 */

	private char[] text;
	private List<Integer> locList = new ArrayList<Integer>();
	private List<String> srcList = new ArrayList<String>();

	/*
	 * Constructor Methods
	 */

	public LineRead(char[] text) {
		setText(text);
		setArrayList();
	}

	public LineRead(String text) {
		setText(text.toCharArray());
		setArrayList();
	}

	/*
	 * Instance Methods
	 */

	public void setText(char[] text) {
		this.text = text;
	}

	public char[] getText() {
		return this.text;
	}

	private void setArrayList() {
		locList.clear();
		srcList.clear();

		int pos = 0;
		for (int i = 0; i < text.length; i++) {
			if (text[i] == '\n') {
				locList.add(pos);
				srcList.add(new String(text, pos, i - pos));
				pos = i + 1;
			}
		}
		if (pos != text.length) {
			locList.add(pos);
			srcList.add(new String(text, pos, text.length - pos));
		}
	}

	/*
	 * return the list of position of beginning of line.
	 */

	public List<Integer> getLocList() {
		return locList;
	}

	/*
	 * return the line number of position.
	 */

	public int getLOC(int pos) {
		List<Integer> list = getLocList();
		int maxLine = list.size();

		int line = 0;
		if (pos == 0) {
			line = 1;
		} else {
			while (line < maxLine && pos >= (list.get(line)).intValue()) {
				line++;
			}
		}
		return line;

	}

	/*
	 * return total lines of the text.
	 */

	public int getLOC() {
		return srcList.size();
	}

	/*
	 * return total lines of the text.
	 */

	public boolean isEmptyLine(int line) {
		boolean emptyLine = false;
		if (srcList != null && line > 0 && line <= srcList.size()) {
			if (srcList.get(line - 1).length() == 0) {
				emptyLine = true;
			}
		}
		return emptyLine;

	}

	/*
	 * return list of line contents.
	 */

	public List<String> getSrcList() {
		return srcList;
	}

	/*
	 * return the contents of line.
	 */

	public String getSrcLine(int num) {
		String line = "";
		if (srcList != null && num > 0 && num <= srcList.size()) {
			line = (String) srcList.get(num - 1);
		}
		return line;
	}

	/*
	 * return count of line.
	 */

	public int countLines(int startPos, int length) {
		int sl = startPos;
		int el = startPos + length;
		int lines = getLOC(el) - getLOC(sl) + 1;
		int sln = getSpaceLines(getLOC(el + 1)); // space lines
		return lines + sln;
	}

	/*
	 * return number of empty line.
	 */

	private int getSpaceLines(int ln) {
		int lnId = ln - 1;
		int sln = 0;
		while (lnId < srcList.size() &&  srcList.get(lnId).length() == 0) {
			sln = sln + 1;
			lnId = lnId + 1;
		}
		return sln;
	}
}
