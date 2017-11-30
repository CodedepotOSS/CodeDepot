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
package jp.co.sra.codedepot.index;

// $Id: Constants.java 2342 2017-11-09 05:36:32Z fang $

@Deprecated
public class Constants {
	final static String COMMENTS = "comments";
	final static String PKGNM = "pkgnm";
	final static String CLSNM = "clsnm";
	final static String MTDNM = "mtdnm";
	final static String UIDS = "userIds";
	final static String LOCATION = "location";  // store the location of the document

	// type of documents to be indexed
	final static byte MTD_TYPE = 0x01;
	final static byte CLS_TYPE = 0x10;
}
