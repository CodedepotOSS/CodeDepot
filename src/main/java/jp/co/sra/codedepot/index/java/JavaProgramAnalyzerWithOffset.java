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
package jp.co.sra.codedepot.index.java;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.TokenOffsetPayloadTokenFilter;

/**
 * Analyzer used for indexing Java programs for the purpose of
 * clone retrieval. This analyzer will save the startOffset and
 * endOffset of each token in its original source program as payload
 * data in the indexes. These data are used for showing where the
 * match happens. See {@link JavaCloneSearchEx1.java} for examples
 * of how to get the source position.
 * @author ye
 * @updater matubara
 * @$Id: JavaProgramAnalyzerWithOffset.java 2342 2017-11-09 05:36:32Z fang $
 */
public class JavaProgramAnalyzerWithOffset extends JavaProgramAnalyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream ts = super.tokenStream(fieldName, reader);
		return new TokenOffsetPayloadTokenFilter(ts);
	}
}
