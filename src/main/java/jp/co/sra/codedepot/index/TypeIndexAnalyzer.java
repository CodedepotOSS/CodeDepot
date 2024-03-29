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
/**
 *
 */
package jp.co.sra.codedepot.index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * An analyzer for signature search.
 *
 *
 * @author yunwen ye
 * @Version $Id: TypeIndexAnalyzer.java 2342 2017-11-09 05:36:32Z fang $
 */
public class TypeIndexAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		 TokenStream ts = new TypeTokenizer(reader);
		 return new TypeFQNSplitFilter(new LowerCaseFilter(ts));
	}
}
