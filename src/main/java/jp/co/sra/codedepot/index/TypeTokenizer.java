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
import java.io.Reader;

import org.apache.lucene.analysis.Token;

public class TypeTokenizer extends CharSeparatorTokenizer {
	static { debug=false; }

	public TypeTokenizer(Reader input) {
		super(input);
	}

	@Override
	protected boolean isTokenChar(char c) {
		return ! (c == ',' || Character.isWhitespace(c));
	}

	@Override
	protected boolean isTokenEndingChar(char c) {
		//not used
		return false;
	}

	@Override
	protected char normalize(char c) {
		return c;
	}

	public Token next(final Token reusableToken) throws IOException {
		assert reusableToken != null;
		reusableToken.clear();
		int length = 0;
		int start = this.bufferIndex();
		char[] buffer = reusableToken.termBuffer();
		char c = ' ';
		boolean inAngleBracket = false;
		int angleBracketCount = 0;

		while (true) {

			if (this.isEOS((c = nextChar()))) break; //current char being looked

			if (inAngleBracket) {
				if ( c == '>') {
					angleBracketCount--;
					if (angleBracketCount == 0) {
						inAngleBracket = false;
					}
				} else if ( c == '<') {
					angleBracketCount++;
				} else {
					;
				}
				continue;
			}

			if (c == '<') {
				angleBracketCount = 1;
				inAngleBracket = true;
				continue;
			}

			if (isTokenChar(c)) {
				// a normal token character
				if (length == 0) // start of token
					{ start = this.offset() + this.bufferIndex() - 1; }
				else if (length == buffer.length)
					{ buffer = reusableToken.resizeTermBuffer(1 + length); }
				buffer[length++] = normalize(c); // buffer it, normalized
				if (length == MAX_WORD_LEN) // buffer overflow!
					break;
			} else {
				if (length > 0) {
					break;
				}
			}
		}
		if ((isEOS(c)) && (length == 0)) {
			return null;
		}
		try {
			// start = offset + bufferIndex - 1;
			// TODO why do I need to set start here?
			reusableToken.setTermLength(length);
			reusableToken.setStartOffset(start);
			reusableToken.setEndOffset(start + length);
			//reusableToken.setEndOffset(start + bufferIndex);
			// System.out.println(reusableToken);
		} catch (IllegalArgumentException e) {
			// TODO remove this when debug is done
			System.out.println(reusableToken);
			System.out.format("start=%d, end=%d, length=%d%n", start, start+length, length);
			System.out.println(buffer);

		}
		if (debug) System.out.println(reusableToken);
		return reusableToken;
	}
}
