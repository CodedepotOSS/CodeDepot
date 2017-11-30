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
package jp.co.sra.codedepot.sample.i18n.server;

import org.apache.lucene.messages.NLS;

public class SampleLuceneMessage extends org.apache.lucene.messages.NLS {

	private final static String BUNDLE_NAME = SampleLuceneMessage.class.getName();
		//has to be the same name as the class "jp.co.sra.codedepot.sample.i18n.SampleMessage";

	//static variables must be the same as the property file's keys
	public static String msg1;
	public static String msg2;
	public static String msg3;

	static {
		NLS.initializeMessages(BUNDLE_NAME, SampleLuceneMessage.class);
	}
}
