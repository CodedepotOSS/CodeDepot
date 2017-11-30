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

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;

import jp.co.sra.codedepot.index.ProgramTokenizer;

/**
 * Separate C++ program into token streams. Whitespace characters and ending characters
 * are used to split the tokens. Whitespace characters are deleted; while ending characters
 * are included as an individual token.
 * @author matubara
 */
public class CppProgramTokenizer extends ProgramTokenizer {
    private boolean inMacroStatement;
    private String MACRO_END = "macro_end";

    public CppProgramTokenizer(Reader in, String end_tokens) {
        super(in, end_tokens);
    }

    public Token next(final Token reusableToken) throws IOException {
        assert reusableToken != null;
        reusableToken.clear();
        int length = 0;
        int start = this.bufferIndex();
        char[] buffer = reusableToken.termBuffer();
        char c = ' ';
        boolean inLineComment = false, inBlockComment = false, inCharLiteral = false, inStringLiteral = false;
        boolean inEscape = false;
        char previousChar = ' ';

        while (true) {
	    if (inMacroStatement & isLineEnd(c))
		break;

            previousChar = c;
            if (this.isEOS((c = nextChar())))
                break; //current char being looked

            if (inLineComment || inBlockComment) {
                if ((inLineComment && isLineEnd(c)) || (inBlockComment && isBlockCommentEnd(c, previousChar))) {
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
                } else if ((inStringLiteral && isStringLiteralEnd(c, previousChar)) || (inCharLiteral && isCharLiteralEnd(c, previousChar))) {
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
                    if (debug)
                        System.out.println(reusableToken);
                    return reusableToken;
                }
                continue;
            }

            // no need to worry that / is escaped, because we are
            // sure not in string here
            if (c == '/') {
                char nch;
                if (this.isEOS((nch = peekNextChar())))
                    break;
                if ((inBlockComment = nch == '*') || (inLineComment = nch == '/')) {
                    c = nextChar(); // have to consume the current char, otherwise /*/ will cause trouble
                    continue;
                }
            }

            if ((inStringLiteral = isStringLiteralStart(c)) || (inCharLiteral = isCharLiteralStart(c))) {
                start = this.offset() + this.bufferIndex() - 1;
                length = 1;
                // buffer[length++] = c;
                continue;
            }

            if (isTokenChar(c) && !isTokenEndingChar(c)) {
                // a normal token character
                if (length == 0) // start of token
                    start = this.offset() + this.bufferIndex() - 1;
                else if (length == buffer.length)
                    buffer = reusableToken.resizeTermBuffer(1 + length);

                buffer[length++] = normalize(c); // buffer it, normalized

                if (length == MAX_WORD_LEN) // buffer overflow!
                    break;
            } else if (isTokenEndingChar(c)) { //reaching end of a token because of tokenEnding
                this.movePreviousBuffer(c); // we need this token next time, push it back.
                break;
            } else {
                // reaching the end of a token because of nonToken
                if (length > 0)
                    break;
                if (inMacroStatement == true && this.isLineEnd(c))
                    break;
            }

            if (c == '#') {
                inMacroStatement = true;
            }
        }

        if (isTokenEndingChar(c) && length == 0) {
            // return the token ending char as a token,
            // bufferIndex has already been pushed back, no -1 needed
            start = this.offset() + this.bufferIndex();
            if (length == buffer.length) {
                buffer = reusableToken.resizeTermBuffer(1 + length);
            }
            buffer[length++] = normalize(c);
            this.moveNextBuffer();
            reusableToken.setType(SEPARATOR);
        } else if (inMacroStatement == true && this.isLineEnd(c)) {
            inMacroStatement = false;
            reusableToken.setType(MACRO_END);
        } else if (length == 0) {
            reusableToken.setType(BLANK);
        } else {
            reusableToken.setType(NON_SEPARATOR);
        }
        // System.out.println(buffer);
        if (this.isEOS(c) && (length == 0)) {
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
            System.out.format("start=%d, end=%d, length=%d%n", start, start + length, length);
            System.out.println(buffer);

        }
        if (debug)
            System.out.println(reusableToken);
        return reusableToken;
    }
}
