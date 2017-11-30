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

import java.lang.Character;
import java.io.Reader;

/**
 * Separate Java program into token streams. Whitespace characters and ending characters
 * are used to split the tokens. Whitespace characters are deleted; while ending characters
 * are included as an individual token.
 * TODO (1) Needs to replace this so that nonToken also splits as whitespace but remain
 *      to be processed as tokens.
 *      (2) two character operators like == to be treated as one token (Really necessary?)
 *      (3) comments need to be dropped.
 *      (4) literal strings to be treated as one token
 *
 * @$Id: ProgramTokenizer.java 2342 2017-11-09 05:36:32Z fang $
 */
public class ProgramTokenizer extends CharSeparatorTokenizer {

	/* Characters that indicates the end of a token, but the
	 * character itself needs to treat as an icon
	 */
	private String _end_tokens = "(){}[]+-*/=!<>;,&|^~?:";

	public ProgramTokenizer(Reader in, String end_tokens) {
		super(in);
		_end_tokens = end_tokens;
	}

	@Override
	protected boolean isTokenEndingChar(char c) {
		// return !Character.isWhitespace(c) && (nonToken.indexOf(c) < 0);
		return _end_tokens.indexOf(c) >= 0;
	}

	protected boolean isTokenChar(char c) {
		return !Character.isWhitespace(c);
	}
}
