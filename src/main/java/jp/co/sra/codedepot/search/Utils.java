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
package jp.co.sra.codedepot.search;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Utility methods for manipulating strings
 * @author ye
 *
 */
public class Utils {
	/**
	 * Count the number of characters in a string, between start and end (not including end)
	 */
	public static int countChar(CharSequence str, char c, int start, int end) {
		int num = 0;
		for (int i=start; i< Math.min(str.length(), end); i++) {
			if (str.charAt(i) == c) {
				num++;
			}
		}
		return num;
	}

	public static void adjustMinimumShouldMatch(Query q) {
		if (! (q instanceof BooleanQuery)) return;
		BooleanQuery bq = (BooleanQuery) q;
		BooleanClause[] clauses = bq.getClauses();
		boolean hasRequired = false;
		int optionNum = 0;
		for(BooleanClause c: clauses) {
			if (c.isRequired()) { hasRequired = true; }
			else if (! c.isProhibited()) {
				++optionNum;
			}
			adjustMinimumShouldMatch(c.getQuery());
		}
		if (hasRequired && optionNum > 1 && bq.getMinimumNumberShouldMatch() == 0) {
			bq.setMinimumNumberShouldMatch(1);
		}
	}
}
