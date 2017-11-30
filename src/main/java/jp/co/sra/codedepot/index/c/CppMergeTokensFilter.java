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

import jp.co.sra.codedepot.index.MergeTokensFilter;

import org.apache.lucene.analysis.TokenStream;

/**
 * Merge tokens into one token until the specified endToken is met;
 * endToken itself is included as default. endToken is not if set so.
 *
 * For example, in program clone search, we merged tokens in one
 * statement line into one token.
 *
 * An alternative to this is to use the NGramTokenFilter
 * @author matubara
 */
public class CppMergeTokensFilter extends MergeTokensFilter {
    public CppMergeTokensFilter(TokenStream input, Set<String> endTokens) {
        this(input, endTokens, new String[] {"macro_end"});
    }

    public CppMergeTokensFilter(TokenStream input, Set<String> endTokens, String[] stopTypes) {
        super(input, endTokens, stopTypes);
    }
}
