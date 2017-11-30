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

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class PrettyPrint {
	public static void pprint(TokenStream tokenStream) throws IOException {
		System.out.println(toString(tokenStream));
	}

	public static void pprint(TokenStream tokenStream, int numberPerline) throws IOException {
		System.out.println(toString(tokenStream, numberPerline));
	}

	public static String toString(String[] ss) {
		StringBuilder result = new StringBuilder("");
		for (String s: ss) {
			result.append(s);
			result.append(" ");
		}
		return result.length() > 1 ? result.substring(0, result.length()-1) : "";
	}

	public static String toString(TokenStream stream) throws IOException {
		return toString(stream, 5);
	}
	public static String toString(TokenStream stream, int numberPerLine) throws IOException {
		StringBuilder result = new StringBuilder();
		int count=0;
		Token t = new Token();
		for(t = stream.next(t); t != null; t = stream.next(t), count++) {
			result.append(t);
			if (count == numberPerLine ) {
				result.append("\n");
				count = 0;
			}
		}
		return result.toString();
	}
}
