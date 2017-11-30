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
import java.util.LinkedList;
import java.util.Queue;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** シグネチャー検索用のフィルター、FQNタイプをFQNとSimple Nameの二つに分ける。
 * 　	java.lang.String --> java.lang.String | String
 *  ns1::ns2::string --> ns1::ns2::string | string
 *
 *  For the time being, all instantiated types are treated as same
 *   List<Integer> matches queries of
 *   	List
 *   and queries like List<Integer> are not allowed.
 *
 * TODO
 * 	should consider doing things like
 * 		List<Integer> --> List | List<Integer>
 *  The major problem is dealing with
 *      List<? extends Number>
 *  We cannot use whitepace tokenizer anymore, has to write a new analyzer
 * @author yunwen
 * @version $Id: TypeFQNSplitFilter.java 2342 2017-11-09 05:36:32Z fang $
 */
public class TypeFQNSplitFilter extends TokenFilter {

	public final static Logger log = LoggerFactory.getLogger(TypeFQNSplitFilter.class);

	//NOTE this is not really necessary for Signature only, but
	//     could be easily converted to other word splitter filter
	static class TokenPos {
		private int start;
		private int end;
		char[] termBuffer;
		TokenPos(char[] tb, int s, int e) {
			start = s; end = e; termBuffer = tb;
		}
		private int getStart() { return start; }
		private int getEnd() { return end; }
		private char[] termBuffer() { return termBuffer;}
	}

	private TermAttribute termAttr;
	private PositionIncrementAttribute positionIncrementAttr;
	private OffsetAttribute offsetAttr;
	// For signature search purpose, only one item is cached,
	// so Query may not be needed. This is the module for WordDelimiterFilter
	private Queue<TokenPos> cache;

	protected TypeFQNSplitFilter(TokenStream input) {
		super(input);
		cache = new LinkedList<TokenPos>();
		//avoid downcasting, get attribute types here
		termAttr = (TermAttribute) addAttribute(TermAttribute.class);
		positionIncrementAttr = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		offsetAttr = (OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (! cache.isEmpty()) {
			TokenPos nextTokenPos = cache.remove();
			termAttr.setTermBuffer(nextTokenPos.termBuffer(), 0, nextTokenPos.termBuffer().length);
			offsetAttr.setOffset(nextTokenPos.getStart(), nextTokenPos.getEnd());
			positionIncrementAttr.setPositionIncrement(0);
			log.debug("filter {}", termAttr);
			return true;
		}
		if (! input.incrementToken()) return false;
		eraseGeneric();
		boolean split = false;
		int count = termAttr.termLength() - 1;
		char[] currentTermBuffer = termAttr.termBuffer();
		//int idx = offsetAttr.startOffset(); //only look in the valid characters
		while (!split && count >= 0) {
			if (currentTermBuffer[count] == '.' ||
				//added 20Jan2010 for fcall fqn#method
				currentTermBuffer[count] == '#' ||
				currentTermBuffer[count] == ':' && count > 0 && currentTermBuffer[count-1] == ':') {
				split = true;
				break;
			}
			count--;
			//idx++;
		}
		if (split) {
			//TODO optimize this later, this is a way to see if this works
			char[] tb = new char[termAttr.termLength()-count-1];
				//termLength() is different than termBuffer().length, the former removes non-token chars
			for (int i = 0; i<tb.length; i++) {
				tb[i] = currentTermBuffer[count+1+i]; //skip .
			}
			TokenPos t = new TokenPos(tb, offsetAttr.startOffset()+count+1, offsetAttr.endOffset());
			cache.offer(t);
		}

		//System.out.println("filter" + termAttr);
		return true;
	}

	/**
	 * @deprecated now handled by TypeTokenizer
	 * Remove type parameters, must be well formed
	 * For the time being, all instantiated types are treated as same
	 *   List<Integer> matches queries of
	 *   	List
	 *   and queries like List<Integer> are not allowed.
	 * TODO
	 * 	should consider doing things like
	 * 		List<Integer> --> List | List<Integer>
	 *  The major problem is dealing with
	 *      List<? extends Number>
	 *  We cannot use whitepace tokenizer anymore, has to write a new analyzer
	 */
	@Deprecated
	private void eraseGeneric() {
		char[] currentTermBuffer = termAttr.termBuffer();
		int bufferLength = currentTermBuffer.length;
		int leftAngle = -1, rightAngle = -1;
		int startOffset = offsetAttr.startOffset(), endOffset = offsetAttr.endOffset();
		for (int i = 0; i < bufferLength; i++) {
			if (currentTermBuffer[i] == '<') {
				leftAngle = i;
				break;
			}
		}
		if (leftAngle == -1) { return; }
		int termLength = termAttr.termLength();
		for (int i = bufferLength - 1; i > leftAngle; i--) {
			if (currentTermBuffer[i] == '>') {
				rightAngle = i;
				break;
			}
		}
		if (rightAngle > leftAngle ) {
			//remove chars between leftAngle and rightAngle, inclusive
			System.out.println("Before Erase: " + termAttr);
			int newTermLength = termLength - (rightAngle - leftAngle + 1);
			char[] newTermBuffer = new char[bufferLength-(rightAngle-leftAngle+1)];
			System.arraycopy(currentTermBuffer, 0, newTermBuffer, 0, leftAngle);
			System.arraycopy(currentTermBuffer, rightAngle+1, newTermBuffer, leftAngle, bufferLength-rightAngle-1);
			termAttr.setTermBuffer(newTermBuffer, 0, newTermBuffer.length);
			termAttr.setTermLength(newTermLength);
			//offsetAttr.setOffset(startOffset, startOffset+newTermLength);
			System.out.println("After Erase: " + termAttr);
		}
	}
	/** @deprecated
	 *  But still needed for solr code.
	 */
	@Override
	public final Token next(final Token reusableToken) throws java.io.IOException {
		return super.next(reusableToken);
	}

	/** @deprecated
	 *  But still needed for solr code.
	 */
	@Override
	public final Token next() throws java.io.IOException {
		return super.next();
	}
}
