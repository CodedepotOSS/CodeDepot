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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;

import jp.co.sra.codedepot.index.WhitespaceCommaTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signature query. Matches documents that contains the query types. For
 * the following documents:
 * 	d1:String, int, int
 *  d2:String, int, int, int
 *  d2:String, int
 *  d3:String, String, int, int
 *  d4:int, int
 *  d5:String, String, int, int, int
 *  d6:java.lang.String, int, int
 *  d7:_UNKNOWN_.String, int, int
 * The SignatureQuery("int,int,String") shall return (in order)
 * 	d1, d6 | d7, d2 | d3, d5  (orders of d6 and d7, as well as d2 and d3 do not matter)
 * The SignatureQuery("int, int, java.lang.String") shall return
 *  d6, d7
 * @author yunwen
 * @$Id: SignatureQuery.java 2356 2017-11-10 07:50:30Z fang $
 */
public class SignatureQuery extends Query {

	private static final long serialVersionUID = 3210432423432943248L;

	final static Logger log = LoggerFactory.getLogger(SignatureQuery.class);

	private Map<Term, Integer> _termFreqs;
	private String _field;

	public SignatureQuery(String field, Map<Term, Integer> termFreqs) {
		_field = field;
		_termFreqs = new TreeMap<Term, Integer>();
		Set entries = termFreqs.entrySet();
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry<Term,Integer> entry = (Map.Entry<Term,Integer>)iterator.next();
			_termFreqs.put(entry.getKey(), entry.getValue());
		}
	}


	//maybe we don't need this, just pass _termFreqs to SignatureScorer
	public Map<Term, Integer> getTermFreqs() {
		return _termFreqs;
	}

	public String getField() {
		return _field;
	}

	@Override
	public Weight createWeight(Searcher s) throws IOException {
		log.debug("{}", s);
		return new SignatureWeight(s, this);
	}

	@Override
	public String toString(String arg0) {
		StringBuilder sb = new StringBuilder();
		sb.append(_field);
		sb.append(":(");
		//_termFreqs.toString();
		for (Iterator<Map.Entry<Term, Integer>> iterator = _termFreqs.entrySet().iterator();
			iterator.hasNext(); ) {
			Map.Entry<Term, Integer> e = iterator.next();
			sb.append(e.getKey().toString() + ",");
			sb.append(e.getValue().toString() + "; ");
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SignatureQuery other = (SignatureQuery) obj;
		return this.getBoost() == other.getBoost() &&
				//this should not be necessary since _termFreqs has field
				this._field.equals(other._field) &&
				this._termFreqs.equals(other._termFreqs);
	}

	@Override
	public int hashCode() {
		int h;
		//_filed.hashCode() is not needed since terms in _termFreqs has field value
		//for robustness, just in case _termFreqs is changed to (text, freq)
		h = Float.floatToIntBits(getBoost()) ^ _termFreqs.hashCode() ^ _field.hashCode();
		log.debug("{}'s hashcode is {}", this, h);
		return h;
	}

	///
	/// utility methods that deal with space in types of C/C++
	///

	/** 複数のワードからなる型の separator eg. unsigned int ==> unsignedSEPARATORint
	 * */
	public static final char SEPARATOR = '\u00a0'; //nbsp
	public static final String TYPE_SEPARATOR = ", "; //separator for two types
	public static final String NONSPACECHARS = "*&[](,)";

	/**
	 * 複数ワードからなる型の文字列に含まれる空白 を separator に置換する関数
	 * 最後のポインターや参照前の空白を無視
	 * foo(const int*&) ==> "const int * &"
	 * foo(int const*&) ==> "const int * &"
	 * @param str solr に登録する文字列
	 * @return 空白が置換された文字列
	 */
	public static String replaceSeparator(String str) {

		StringBuffer result = new StringBuffer();
		boolean skipNextWhitespace = false;
		for (int i=0; i < str.length(); ) {
			char ch = str.charAt(i++);
			if (SignatureQuery.isWhitespace(ch)) {
				if (skipNextWhitespace) continue;
				if (i < str.length()) {
					char nextChar = str.charAt(i);
					if (SignatureQuery.isWhitespace(nextChar) ||
							NONSPACECHARS.indexOf(nextChar) >= 0) {
						continue; //skip current whitespace if followed by another
							//whitespace of * or &
					} else {
						//replace whitespace with SEPARATOR
						result.append(SEPARATOR);
					}
				}
			} else if (NONSPACECHARS.indexOf(ch) >= 0) {
				skipNextWhitespace = true;
				if (ch == ',') {
					result.append(SEPARATOR); //replace this two, otherwise
						//got cut by Analyzer
				} else {
					result.append(ch);
				}
			} else  {
				skipNextWhitespace = false;
				result.append(ch);
			}
		}
		return result.toString();
	}

	public static boolean isWhitespace(char c) {
		return Character.isWhitespace(c) || c == '\u3000' /* 全角空白 */;
	}

	public static final String dropWords =
		"\\b((mutable)|(auto)|(extern)|(register)|(typedef)|(const)" +
		"|(inline)|(volatile)|(restrict)|(explicit)|(friend)|(virtual))\\b";

	/**
	 * This is used by queryparser
	 * @param s
	 * @return
	 */
	public static String normalizeTypePhraseQuery(String s) {
		return(normalizeType(s));
	}
	/**
	 * Remove non essential decl specifiers for types.
	 * 		restrict, register, auto, extern
	 * @param s
	 * @return
	 */
	public static String normalizeType(String s) {
		String p = s.replaceAll(dropWords, "");
		p = p.trim();
		p = replaceSeparator(p);
		//System.out.println(p + "|");
		return p;
	}
}
