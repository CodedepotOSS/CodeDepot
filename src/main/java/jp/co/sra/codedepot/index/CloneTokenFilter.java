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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.index.Payload;

/**
 * CloneToken is passed here by indexer which has done tokenizer (by calling
 * ProgramTokenizer and token merging for each language and already.
 *
 * Nothing needs to be done here except extract the offset info from the
 * token sequence passed by indexer
 *  $=$@0.5  (number after @ are offsets of the token)
 * @author ye
 *
 */
public class CloneTokenFilter extends TokenFilter {
	protected TermAttribute termAtt;
	protected PayloadAttribute payAtt;
	protected OffsetAttribute offsetAtt;

	public CloneTokenFilter (TokenStream input) {
		super(input);
		termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		payAtt = (PayloadAttribute) addAttribute(PayloadAttribute.class);
		offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	@Override
	public boolean incrementToken () throws IOException {
		while (input.incrementToken()) {
			byte[] data = new byte[8];

			char[] buffer = termAtt.termBuffer();
			int length = termAtt.termLength();

			/* find "." mark */

			int i = 0;
			while (i < length && buffer[i] != '.') {
				i++;
			}
			if (i == 0 || i == length) {
				continue;
			}

			/* find "@" mark */

			int j = i + 1;
			while (j < length && buffer[j] != '@') {
				j++;
			}
			if (j == i + 1 || j == length) {
				continue;
			}

			/* extract offset */

			int startOffset = ArrayUtil.parseInt(buffer, 0, i);
			int endOffset = ArrayUtil.parseInt(buffer, i + 1, j - i - 1);

			/* create payload */

			data[0] = (byte)(startOffset >> 24);
			data[1] = (byte)(startOffset >> 16);
			data[2] = (byte)(startOffset >>  8);
			data[3] = (byte)(startOffset);
			data[4] = (byte)(endOffset >> 24);
			data[5] = (byte)(endOffset >> 16);
			data[6] = (byte)(endOffset >>  8);
			data[7] = (byte)(endOffset);
			Payload payload = new Payload(data);

			/* change token attribute */

			offsetAtt.setOffset(startOffset, endOffset);
			termAtt.setTermBuffer(buffer, j + 1, length - j - 1);
			payAtt.setPayload(payload);

			return true;
		}
		return false;
	}
}
