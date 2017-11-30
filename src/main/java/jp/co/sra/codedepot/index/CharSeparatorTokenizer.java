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
import org.apache.lucene.analysis.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Code was originally copied from org.apache.lucene.analysis.CharTokenizer
 * and modified.
 * See license notice below.
 *
 * isSeparatorChar is used as the end of Token, and it is
 * retained as the part of the token
 *
 * @author ye
 * @updater matubara
 * @$Id: CharSeparatorTokenizer.java 2342 2017-11-09 05:36:32Z fang $
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public abstract class CharSeparatorTokenizer extends Tokenizer {

	final static Logger logger = LoggerFactory.getLogger(CharSeparatorTokenizer.class);

	public static boolean debug = false;

	public static final String SEPARATOR = "sep"; //separator token
	public static final String NON_SEPARATOR = "nonsep"; //normal token (id, keyword, number)
	public static final String BLANK = "blank"; //a token that has no contents
	public static final String STRING_LITERAL = "str";
	public static final String CHAR_LITERAL = "ch";

	public CharSeparatorTokenizer(Reader input) {
		super(input);
	}

	private int offset = 0, bufferIndex = 0, bufferLength = 0;
	protected static final int MAX_WORD_LEN = 40960;
	private static final int IO_BUFFER_SIZE = 4096;
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	private final char EOS = '\0';
	// private char _nextChar; //must be read before call peekNextChar()

	private boolean isPushedback = false;
	private char pushbackChar = EOS;

	/**
	 * Returns true iff a character should be included in a token. This
	 * tokenizer generates as tokens adjacent sequences of characters which
	 * satisfy this predicate. Characters for which this is false are used to
	 * define token boundaries and are not included in tokens.
	 */
	protected abstract boolean isTokenChar(char c);

	/**
	 * Returns true iff the character marks the end of a token, but unlike
	 * those characters whose isTokenChar is false, the token is not thrown away,
	 * instead, it is returned as a token.
	 * @param c
	 * @return
	 */
	protected abstract boolean isTokenEndingChar(char c);

	/**
	 * Called on each token character to normalize it before it is added to the
	 * token. The default implementation does nothing. Subclasses may use this
	 * to, e.g., lowercase tokens.
	 */
	protected char normalize(char c) {
		return c;
	}

	protected char nextChar() throws IOException {
		if (isPushedback) {
			isPushedback = false;
			return pushbackChar;
		}
		if (bufferIndex >= bufferLength) {
			offset += bufferLength;
			bufferLength = input.read(ioBuffer);
			// System.out.println(ioBuffer);
			if (bufferLength == -1) {
				return EOS;
			}
			bufferIndex = 0;
		}
		return ioBuffer[bufferIndex++];
	}

	/**
	 * peek at the next char, if buffer is empty, get the next one
	 * TODO refactor the buffer reading out
	 * @return
	 * @throws IOException
	 */
	protected char peekNextChar() throws IOException {
		if (isPushedback) {
			isPushedback = false;
			return pushbackChar;
		}
		if (bufferIndex >= bufferLength) {
			offset += bufferLength;
			bufferLength = input.read(ioBuffer);
			// System.out.println(ioBuffer);
			if (bufferLength == -1) {
				return EOS;
			}
			bufferIndex = 0;
			return ioBuffer[0];
		} else {
			return ioBuffer[bufferIndex];
		}
	}

	/**
	 * TODO ugly, needs cleanup
	 */
	public Token next(final Token reusableToken) throws IOException {
		assert reusableToken != null;
		reusableToken.clear();
		int length = 0;
		int start = bufferIndex;
		char[] buffer = reusableToken.termBuffer();
		char c = ' ';
		boolean inLineComment = false, inBlockComment = false,
			inCharLiteral = false, inStringLiteral = false;
		boolean inEscape = false;
		char previousChar = ' ';

		while (true) {

			previousChar = c;
			if ((c = nextChar()) == EOS) break; //current char being looked

			if (inLineComment || inBlockComment) {
				if ((inLineComment && isLineEnd(c)) ||
					(inBlockComment && isBlockCommentEnd(c, previousChar))) {
					// comment ended
					inLineComment = false;
					inBlockComment = false; //whatever their original value, both must be false after this
					}
				// still in comment, do nothing
				continue;
			}

			if (inStringLiteral || inCharLiteral) {
				length++;
				if (inEscape) {
					previousChar = ' ';
					c = ' ';
					inEscape = false;
				} else if (isEscape(c)) {
					inEscape = true;
				} else if ( (inStringLiteral && isStringLiteralEnd(c, previousChar)) ||
						(inCharLiteral && isCharLiteralEnd(c, previousChar))) {
					reusableToken.setStartOffset(start);
					reusableToken.setEndOffset(start + length);
					reusableToken.setTermBuffer("$");
					if (inStringLiteral) {
						inStringLiteral = false;
						reusableToken.setType(STRING_LITERAL);
					} else {
						inCharLiteral = false;
						reusableToken.setType(CHAR_LITERAL);
					}
					if (debug) System.out.println(reusableToken);
					return reusableToken;
				}
				continue;
			}

			// no need to worry that / is escaped, because we are
			// sure not in string here
			if (c == '/') {
				char nch;
				if ((nch = peekNextChar()) == EOS) break;
				if ((inBlockComment = nch == '*') || (inLineComment = nch == '/')) {
					c = nextChar(); // have to consume the current char, otherwise /*/ will cause trouble
					continue;
				}
			}

			if ((inStringLiteral = isStringLiteralStart(c)) || (inCharLiteral = isCharLiteralStart(c))) {
				start = offset + bufferIndex - 1;
				length = 1;
				// buffer[length++] = c;
				continue;
			}

			if (isTokenChar(c) && !isTokenEndingChar(c)) {
				// a normal token character
				if (length == 0) // start of token
					start = offset + bufferIndex - 1;
				else if (length == buffer.length)
					buffer = reusableToken.resizeTermBuffer(1 + length);

				buffer[length++] = normalize(c); // buffer it, normalized

				if (length == MAX_WORD_LEN) // buffer overflow!
					break;
			} else if (isTokenEndingChar(c)) { //reaching end of a token because of tokenEnding
				// we need this token next time, push it back, basically bufferIndex--
				movePreviousBuffer(c);
				break;
			} else {
				// reaching the end of a token because of nonToken
				if (length > 0) break;
			}
		}

		if (isTokenEndingChar(c) && length == 0) {
			// return the token ending char as a token,
			// bufferIndex has already been pushed back, no -1 needed
			start = offset + bufferIndex;
			if (length == buffer.length) {
				buffer=reusableToken.resizeTermBuffer(1 + length);
			}
			buffer[length++] = normalize(c);
			bufferIndex++;
			reusableToken.setType(SEPARATOR);
		} else if (length == 0) {
			reusableToken.setType(BLANK);
		} else {
			reusableToken.setType(NON_SEPARATOR);
		}
		// System.out.println(buffer);
		if ((c == EOS) && (length == 0)) {
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

	public void reset(Reader input) throws IOException {
		super.reset(input);
		bufferIndex = 0;
		offset = 0;
		bufferLength = 0;
	}

	protected boolean isEscape(char c) {
		return c == '\\';
	}

	protected boolean isLineCommentStart(char c, char prev) {
		return c == '/' && prev == '/';
	}

	protected boolean isLineEnd(char c) {
		return c == '\n' || c == '\r';
	}

	protected boolean isStringLiteralStart(char c) {
		return c == '"';
	}

	protected boolean isStringLiteralEnd(char c, char prev) {
		return !isEscape(c) && c == '"';
	}

	protected boolean isCharLiteralStart(char c) {
		return c == '\'';
	}

	protected boolean isCharLiteralEnd(char c, char prev) {
		return !isEscape(prev) && c == '\'';
	}
	protected boolean isBlockCommentEnd(char c, char prev) {
		return c == '/' && prev == '*';
	}

	protected boolean isBlockCommentStart(char c, char prev) {
		return c == '*' && prev == '/';
	}

	protected int bufferIndex() {
	    return bufferIndex;
	}

	protected void moveBuffer(int movement) {
	    bufferIndex += movement;
	}

	protected void moveNextBuffer() {
	    this.moveBuffer(1);
	}

	protected void movePreviousBuffer(char c) {
		if (bufferIndex == 0) {
	    	assert isPushedback == false;
	    	if (isPushedback) {
	    		logger.error("ERROR: trouble, pushing back more than one character: " +
	    				pushbackChar + " and " + c);
	    	}
	    	pushbackChar = c;
	    	isPushedback = true;
		} else {
			--bufferIndex;
		}
	}

	protected boolean hasPushbackChar() {
		return isPushedback;
	}

	protected char pushbackChar() {
		return pushbackChar;
	}

	protected boolean isEOS(char c) {
	    return c == EOS;
	}

	protected int offset() {
	    return offset;
	}
}


