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

import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.analysis.payloads.PayloadHelper;

/**
 * Payload info of a term is the offset of the term
 * in its original document.
 *
 * This class provides information to get the start
 * offset and the end offset of the term or terms.
 *
 * @author ye
 *
 */
public class OffsetAsPayloadDecoder {

	private Collection<byte[]> _payloads;
	private int _start;
	private int _end;
	private byte[] _encodedBytes = new byte[8];
	/* bs[0:4} encodes _start, bs[4:8] encodes _end */

	private boolean _calculated = false;

	public OffsetAsPayloadDecoder(Collection payloads) {
		_payloads = payloads;
		init();
	}

	/**
	 * This is provided to reuse the object when
	 * there are many decoding to do
	 * @param payloads
	 */
	public void setPayloads(Collection payloads) {
		_payloads = payloads;
		init();
	}

	private void init() {
		_calculated = false;
		_start = Integer.MAX_VALUE;
		_end = Integer.MIN_VALUE;
		_calculated = false;
	}

	/**
	 * return the merged payloads
	 * @return
	 */
	public Collection<byte[]> getPayloads() {
		if (!_calculated) {
			doDecoding();
		}
		return Collections.singleton(_encodedBytes);
	}
	/**
	 * return the start offset of the payload
	 * @return
	 */
	public int getStart() {
		if (!_calculated) {
			doDecoding();
		}
		return _start;
	}

	/**
	 * return the largest offset of the payload
	 * @return
	 */
	public int getEnd() {
		if (!_calculated) {
			doDecoding();
		}
		return _end;
	}

	private void doDecoding() {
		for (byte[] bs: _payloads) {
			int cStart = PayloadHelper.decodeInt(bs, 0);
			int cEnd = PayloadHelper.decodeInt(bs, 4);
			if (cStart < _start) {
				_start = cStart;
				for (int i=0; i<4; i++) {
					_encodedBytes[i] = bs[i];
				}
			}
			if (cEnd > _end) {
				_end = cEnd;
				for (int i=4; i<8; i++) {
					_encodedBytes[i] = bs[i];
				}
			}
		}
	}
}
