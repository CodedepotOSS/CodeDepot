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
import java.util.Set;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Implements identifier replacement rules for clone search.
 *    Rules:
 *    	(1) DROPPED_KEYWODS that include accessibility modifier and
 *          other keywords are dropped.
 *      (2) Tokens that are not in RETAINED_TOKENS are changed to
 *          the same ID_TOKEN. (mostly identifiers and numbers)
 *
 *      TODO
 *         To improve recall, consider adding the following rules:
 *         	(a) remove "!"
 *          (b) replace all arithmetic operations "+-/*%" with "+"
 *          (c) replace all relational operations with ">"
 *          (e) replace all keywords with k
 *          (f) merge two neighboring pp into p
 * @author ye
 * @$Id: IdentifierReplacementFilter.java 2342 2017-11-09 05:36:32Z fang $
 */
public class IdentifierReplacementFilter extends TokenFilter {

    public static final String ID_TOKEN = "$";
    public static final String OP_TOKEN = "+";
    public static String _operators = "<>&|^+-*/%";
    private Set<String> _dropTokens;
    private Set<String> _retainTokens;

	public IdentifierReplacementFilter(TokenStream input, Set<String> dropTokens, Set<String> retainTokens) {
		super(input);
		_dropTokens = dropTokens;
		_retainTokens = retainTokens;
	}

	@Override
	public Token next(Token reusableToken) throws IOException {
		assert reusableToken != null;
		//System.out.println(token);
		reusableToken.clear();
		Token token;
		for (token = input.next(reusableToken); token != null; token = input.next(reusableToken)) {

			if (this.isDropToken(token)) {
				continue;
			} else if (this.isOperatorToken(token)) {
				token.setTermBuffer(OP_TOKEN);
				if (CharSeparatorTokenizer.debug) System.out.println(token);
				return token;
			} else if (this.isRetainToken(token)) {
				if (CharSeparatorTokenizer.debug) System.out.println(token);
				return token;
			} else {
				token.setTermBuffer(ID_TOKEN);
				if (CharSeparatorTokenizer.debug) System.out.println(token);
				return token;
			}
		}
		return null;
	}

	protected boolean isDropToken(Token token) {
	    return _dropTokens.contains(token.term());
	}

	protected boolean isRetainToken(Token token) {
	    return _retainTokens.contains(token.term());
	}

	protected boolean isOperatorToken(Token token) {
            String term = token.term();
            if (term.length() == 1 && _operators.indexOf(term.charAt(0)) >= 0) {
                return true;
            } else {
                return false;
            }
        }
}
