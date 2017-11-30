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
import java.io.IOException;

/** Process each file recursively under the tree
 *  Subclass override handle(File f)
 * @author ye
 * @$Id: DirectoryWalker.java 2342 2017-11-09 05:36:32Z fang $
 */
public class DirectoryWalker {
	private File _base;
	private FilenameFilter _fnFilter = null;

	public DirectoryWalker(File basedir) {
		_base = basedir;
	}

	public DirectoryWalker(File basedir, FilenameFilter fnFilter) {
		_fnFilter = fnFilter;
		_base = basedir;
	}

	/**
	 * This method is meant to be rewritten by
	 * @param f
	 * @throws IOException
	 */
	protected void handle(File f) throws IOException {
		System.out.println(f.getAbsolutePath());
	}

	public void walk() throws IOException {
		walk(_base);
	}

	private void walk(File f) throws IOException {
		File[] ff;
		if (f.isDirectory()) {
			ff = f.listFiles(_fnFilter);
			for (File c: ff) {
				walk(c);
			}
		} else {
			handle(f);
		}
	}
}
