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
/*
 * Local Class Table regist <cls,package> pair for  the current program
 */
package jp.co.sra.codedepot.util.java;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalClassTab {
	private String filename;
	private Map<String, String> clsPkgTab;

	public LocalClassTab() {
		this.clsPkgTab = new ConcurrentHashMap<String, String>();
	}

	public LocalClassTab(String filename) {
		this.filename = filename;
		this.clsPkgTab = new ConcurrentHashMap<String, String>();
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Map<String, String> getClassTab() {
		return this.clsPkgTab;
	}

	public void setClassTab(Map<String, String> clsPkgTab) {
		this.clsPkgTab = clsPkgTab;
	}

	public void readClassTab() throws IOException, ClassNotFoundException {
		readClassTab(this.filename);
	}

	public void readClassTab(String filename) throws IOException, ClassNotFoundException {
		FileInputStream f_in = new FileInputStream(filename);
		ObjectInputStream obj_in = new ObjectInputStream(f_in);
		Map<String, String> map = (Map<String, String>) obj_in.readObject();
		obj_in.close();
		f_in.close();
		setClassTab(map);
	}

	public void writeClassTab() throws IOException {
		writeClassTab(this.filename);
	}

	public void writeClassTab(String filename) throws IOException {
		FileOutputStream f_out = new FileOutputStream(filename);
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		Map<String, String> map = getClassTab();
		obj_out.writeObject(map);
		obj_out.close();
		f_out.close();
	}
}
