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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.spans.Spans;

import java.util.List;

/**
 * An implementation of Spans that buffers all spans in
 * memory.
 *
 * WARNING:
 * for performance reason, SpanInfo added into the
 * buffer list _spans are not copied.
 *
 * TODO:
 *    Should create a limited size buffer with a backup
 *    TermSpans that read from the index
 * @author ye
 *
 */
public class BufferedSpans extends Spans {

	private List<SpanElement> _spans;
	private ListIterator<SpanElement> _iterator = null;
	private SpanElement _currentSpanElement = null;

	//We can do without these two fields, they are
	//kept around for the toString() function
	//so that we can output in a similar way as other Spans
	private boolean _firstTime = true;
		//true if we are at the beginning of the spans
	private boolean _more = false;
		//meaning we are not at the end yet when _more is true

	public BufferedSpans() {
		_spans = new ArrayList<SpanElement>();
	}

	/**
	 * Creating a BufferedSpan from an existing Span.
	 *
	 * TODO:
	 * (1) a more effective way of doing this is
	 * using the existing Span as the backing span,
	 * by filling the _spans along with the call
	 * of next().
	 * (2) Limiting the size of buffers in case when
	 * memory is tight, and the unbuffered spans are
	 * accessed as the original one.
	 *
	 * @param spans
	 * @throws IOException
	 */
	public BufferedSpans(Spans spans) throws IOException {
		if (spans instanceof BufferedSpans) {
			_spans = ((BufferedSpans) spans)._spans;
		} else {
			_spans = new ArrayList<SpanElement>();
			while (spans.next()) {
				int doc = spans.doc();
				int start = spans.start();
				int end = spans.end();
				Collection payload = spans.getPayload();
				add(doc, start, end, payload);
				//add(spans.doc(), spans.start(), spans.end(), spans.getPayload());
			}
		}
		_currentSpanElement = null;
	}

	public void add(int docId, int startPosition, int endPosition, Collection payloads) {
		_spans.add(new SpanElement(docId, startPosition, endPosition, payloads));
	}

	//methods for creating the BufferedSpans
	public void add(SpanElement spanElement) {
		_spans.add(spanElement);
	}

	//methods for using BufferedSpans
	/**
	 * reset the spans to its initial state
	 */
	public void reset() {
		_iterator = null;
		_currentSpanElement = null;
		_firstTime = true;
		_more = false;
	}

	/**
	 * Since SpanInfo is immutable, okay to return just the reference
	 * @return
	 */
	public SpanElement getSpanInfo() {
		return _currentSpanElement;
	}

	@Override
	/** doc() can only be called only after next() is called
	 *  This is the convention of all Spans class
	 */
	public int doc() {
		return _currentSpanElement == null ? -1 : _currentSpanElement.getDocId();
	}

	@Override
	/**
	 * end() returns the end of position of the token.
	 * must be called after next() is called.
	 */
	public int end() {
		return _currentSpanElement == null ? -1 : _currentSpanElement.getEndPosition();
	}

	@Override
	public int start() {
		return _currentSpanElement == null ? -1 : _currentSpanElement.getStartPosition();
	}

	@Override
	public Collection getPayload() throws IOException {
		return _currentSpanElement.getPayload();
	}

	@Override
	public boolean isPayloadAvailable() {
		return true; //always true for this
	}

	@Override
	/**
	 * before next() is called, the Spans points to nothing
	 */
	public boolean next() throws IOException {
		if (_firstTime == true) {
			_firstTime = false;
			//first element, initialize the iterator
			_iterator = _spans.listIterator();
		}
		if (_iterator.hasNext()) {
			_currentSpanElement = _iterator.next();
			_more = true;
			//for Spans, calling next() advances
			return true;
		} else {
			//at the end now
			_currentSpanElement = null;
			_more = false;
			return false;
		}
	}

	/**
	 * If we want to use this class as a cache for spans,
	 * we need to fill or remember those skipped docs.
	 */
	@Override
	public boolean skipTo(int target) throws IOException {
		//TODO: use binary-search-like way to skip ahead to speed up.
		while (next()) {
			if (_currentSpanElement.getDocId() >= target) {
				return true;
			}
		}
		_more = false;
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName() + "@");
		//sb.append("(" + query.toString()+")");
		if (_firstTime == true) {
			return sb.append("START").toString();
		} else {
			if (!_more) {
				return sb.append("END").toString();
			}
		}
		sb.append(doc());
		sb.append(":");
		sb.append(start());
		sb.append("-");
		sb.append(end());
		sb.append("");
		sb.append("[");
		Collection<byte[]> payload;
		try {
			payload = getPayload();
			for (Iterator<byte[]> iterator = payload.iterator(); iterator.hasNext(); ) {
					byte[] bs= iterator.next();
					int currentStart = PayloadHelper.decodeInt(bs, 0);
					int currentEnd = PayloadHelper.decodeInt(bs, 4);
					sb.append("("+currentStart+", "+currentEnd+")");
					sb.append(", ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sb.charAt(sb.length()-1) == ' ') {
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("] ");
		return sb.toString();
	}


}
