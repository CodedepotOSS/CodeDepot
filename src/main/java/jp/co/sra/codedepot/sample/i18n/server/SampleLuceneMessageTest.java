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
import java.util.Locale;
import static java.lang.System.out;

public class SampleLuceneMessageTest {
	public static void main(String[] args) {
		//default locale
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg1));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg2, new Object[]{2}));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg3, new Object[]{"+", 1, 2}));

		Locale locale = new Locale("ja");
		out.println("Messages in " + locale);
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg1, locale));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg2, locale, new Object[]{2}));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg3, locale, new Object[]{"+", 1, 2}));

		locale = new Locale("en");
		out.println("Messages in " + locale);
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg1, locale));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg2, locale, new Object[]{2}));
		out.println(NLS.getLocalizedMessage(SampleLuceneMessage.msg3, locale, new Object[]{"+", 1, 2}));
	}
}
