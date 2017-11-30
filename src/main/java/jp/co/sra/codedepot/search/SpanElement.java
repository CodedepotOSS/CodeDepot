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

/** Immutable (well, almost, callers can still
 *   change the contents of _payloads)class.
 *  hold information about each span.
 * @author ye
 *
 */
public class SpanElement {

	private int _docId;
	private int _startPosition;
	private int _endPosition;
	/**guarantee that there are only one byte[8] element
	 * with byte[0:4] representing the starting offset of term term
	 * and byte[4:8] representing the ending offset of the term
	 */
	private Collection<byte[]> _payloads;

	public SpanElement() {
	}

	public SpanElement(int docId, int startPosition, int endPosition, Collection<byte[]> payloads) {
		_docId = docId;
		_startPosition = startPosition;
		_endPosition = endPosition;
		OffsetAsPayloadDecoder decoder = new OffsetAsPayloadDecoder(payloads);
		_payloads = decoder.getPayloads();
	}

	public int getDocId() {
		return _docId;
	}

	public int getStartPosition() {
		return _startPosition;
	}

	public int getEndPosition() {
		return _endPosition;
	}

	/**
	 * Return the payload information stored with each
	 * term.
	 *
	 * In general, we should not assume anything about
	 * the data type and length of payload.
	 * But, for CodeDepot, we only use it to store
	 * offset of each term. We always only one
	 * byte[8] array.
	 * @return
	 */
	public Collection<byte[]> getPayload() {
		return _payloads;
	}

	public String toString() {
		StringBuilder sb =
			new StringBuilder("@" + getDocId() + ":(" + _startPosition + "-" + _endPosition + ")");
		sb.append("[");
		OffsetAsPayloadDecoder decoder = new OffsetAsPayloadDecoder(_payloads);
		sb.append(decoder.getStart());
		sb.append("-");
		sb.append(decoder.getEnd());
		sb.append("]");
		return sb.toString();
	}


}
