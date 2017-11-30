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
package jp.co.sra.codedepot.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Decides what files are acceptable as program files for a given programming language.
 * For example, if "java" is passed to the constructor, then those files whose name ends with
 * "java" are considered as a java program.
 * @author yunwen
 *
 */
public class ProgrammingLanguageFilenameFilter implements FilenameFilter {
	private String suffix = "";

	public ProgrammingLanguageFilenameFilter() {
		suffix = "";
	}

	public ProgrammingLanguageFilenameFilter(String suffix) {
		this.suffix = suffix;
	}

	public void setLanguage(String suffix) {
		this.suffix = suffix;
	}

	public boolean accept(File dir, String name) {
		if (suffix.equals("")) return true;
		if (name.endsWith("."+suffix)) return true;
		return false;
	}

	public boolean accept(File f) {
		if (f.getPath().endsWith("." + suffix)) return true;
		return false;
	}

}
