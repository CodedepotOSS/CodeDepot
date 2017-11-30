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
 * Global Class Table registers <package,classList> pair from being referenced programs
 */

package jp.co.sra.codedepot.util.java;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalClassTab {

	private String filename;
	private Map<String, List> pkgClsTab;

        public GlobalClassTab() {
		this.pkgClsTab = new ConcurrentHashMap<String, List>();
        }

        public GlobalClassTab(String filename) {
		this.filename = filename;
		this.pkgClsTab = new ConcurrentHashMap<String, List>();
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename (String filename) {
		this.filename = filename;
	}

	public Map<String, List> getClassTab() {
		return this.pkgClsTab;
	}

	public void setClassTab (Map<String, List> pkgClsTab) {
		this.pkgClsTab = pkgClsTab;
	}

	public synchronized void readClassTab() throws IOException, ClassNotFoundException {
		readClassTab(this.filename);
	}

	public synchronized void readClassTab(String filename) throws IOException, ClassNotFoundException {
		FileInputStream f_in = new FileInputStream(filename);
		ObjectInputStream obj_in = new ObjectInputStream(f_in);
		Map<String, List> map = (Map<String, List>) obj_in.readObject();
		obj_in.close();
		f_in.close();
		setClassTab(map);
	}

	public synchronized void writeClassTab() throws IOException {
		writeClassTab(this.filename);
	}

	public synchronized void writeClassTab(String filename) throws IOException {
		FileOutputStream f_out = new FileOutputStream(filename);
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		Map<String, List> map = getClassTab();
		obj_out.writeObject(map);
		obj_out.close();
		f_out.close();
	}

	public synchronized void readClassFile(String filename) throws IOException {
		FileReader reader = new FileReader(filename);
		BufferedReader buff = new BufferedReader(reader);

		ConcurrentHashMap<String, List> map = new ConcurrentHashMap<String, List>();

		String line;
		while ((line = buff.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " ");
			String key = st.nextToken();
			String cls = st.nextToken();

			ArrayList<String> list;
			if (map.containsKey(key)) {
				list = (ArrayList<String>)map.get(key);
			} else {
				list = new ArrayList<String>();
				map.put(key, (List)list);
			}
			if (!list.contains(cls)) {
				list.add(cls);
			}
		}
		buff.close();
		reader.close();
		setClassTab(map);
	}

	public static void main(String[] args) {
		String usage = " -in file -out outputfile";
		String in_f = "";
		String out_f = "";

		// parse args -in file -out outputfile
		if (args.length < 4) {
			System.out.println("Usage is: " + usage);
			System.exit(-1);
		}

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-in")) {
				in_f = args[++i];
			} else if (args[i].equals("-out")) {
				out_f = args[++i];
			} else {
				System.out.println("Usage is: " + usage);
				System.exit(-1);
			}
		}

		GlobalClassTab gct = new GlobalClassTab();

		try {
			gct.readClassFile(in_f);
		} catch (IOException e) {
			System.err.println("Cannot read file; " + in_f);
			System.exit(1);
		}

		try {
			gct.writeClassTab(out_f);
		} catch (IOException e) {
			System.err.println("Cannot save file; " + out_f);
			System.exit(2);
		}

		try {
			gct.readClassTab(out_f);
		} catch (IOException e) {
			System.err.println("Cannot read file; " + out_f);
			System.exit(3);
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot load file; " + out_f);
			System.exit(4);
		}

		Map<String, List> map = gct.getClassTab();

		int pkg_count = 0;
		int cls_count = 0;

		if (map != null) {
			pkg_count = map.size();
			for(Map.Entry<String, List> e : map.entrySet()) {
				List list = e.getValue();
				cls_count += list.size();
			}
		}

		System.out.println("Total " + pkg_count + " packages " + cls_count + " classes.");
	}
}
