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
package jp.co.sra.codedepot.index.c;

import java.util.Set;

import jp.co.sra.codedepot.index.IdentifierReplacementFilter;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * Implements identifier replacement rules for C++ clone search.
 * @author matubara
 */
public class CppIdentifierReplacementFilter extends IdentifierReplacementFilter {

    public static final String macroWordPattern = "#.+";

	public CppIdentifierReplacementFilter(TokenStream input, Set<String> dropTokens, Set<String> retainTokens) {
		super(input, dropTokens, retainTokens);
	}

    protected boolean isRetainToken(Token token) {
        if (super.isRetainToken(token)) {
            return true;
        }
        if (token.term().matches(macroWordPattern)) {
            return true;
        }
        return false;
    }
}
