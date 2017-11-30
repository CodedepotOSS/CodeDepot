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

import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

public class CloneTokenEncoder {
	public static String toString(TokenStream ts) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Token token = ts.next(); token != null; token = ts.next()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(token.startOffset());
			sb.append(".");
			sb.append(token.endOffset());
			sb.append("@");
			sb.append(token.termText());
		}
		return sb.toString();
	}
}
