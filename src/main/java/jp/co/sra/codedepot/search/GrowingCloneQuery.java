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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WARNING:
 *     The clauses (an array of SpanQuery) that are passed into
 *     this class's constructor are not copied. They are used as is.
 *     Therefore, the caller must make sure that such clauses are
 *     not modified.
 *
 * This class is mostly intended to be used by AllClonesQuery
 *
 * Returns the matching spans that matches at least specified number (min)
 * of terms from the beginning.
 * <p>
 * Unlike, SpanNearQuery that requires all of its query terms to
 * match within slop, this query only needs
 * <p>
 * <pre>
 * documents:
 *   d1: t1 t2 t3 t4
 *   d2: t1 t2 t3 t5
 *   d3: t1 t3 t4 t5
 * query:
 *   q: t2 t3 t4  min: 2
 * matches:
 *   d1: t1 <t2 t3 t4>
 *   d2: t1 <t2 t3> t5
 * @author ye
 * @$Id$
 */
public class GrowingCloneQuery {

	public static Logger log = LoggerFactory.getLogger(GrowingCloneQuery.class);

	/*
	protected int slop;
	protected boolean inOrder;

	protected String field;
	private boolean collectPayloads;
	*/

	//the final spans that will be returned
	private BufferedSpans results;
	private SpanQuery[] _clauses;
	private int _minSize = 5; //at least five terms
	private int _length; //number of clauses

	/**
	 * WARNING: clauses are not copied, never modifies it
	 * @param clauses
	 */
	public GrowingCloneQuery(SpanQuery[] clauses) {
		this(clauses, 5);
	}

	/**
	 * WARNING: clauses are not copied, never changes it
	 * @param clauses
	 * @param minimalTermNumber
	 */
	public GrowingCloneQuery(SpanQuery[] clauses, int minimalTermNumber) {
		_clauses = clauses;
		_minSize = Math.max(minimalTermNumber, 1);
		_length = _clauses.length;
	}

	public BufferedSpans getGrowingSpans(final IndexReader reader) throws IOException {
		if (results == null) {
			createGrowingSpans(reader);
		}
		return results;
	}

	private void createGrowingSpans(final IndexReader reader) throws IOException {
		results = new BufferedSpans();
		SpanQuery[] minClauses = new SpanQuery[Math.min(_length, _minSize)];
		for (int i = 0; i < minClauses.length; i++) {
			minClauses[i] = _clauses[i];
		}
		SpanNearQuery firstQuery = new SpanNearQuery(minClauses, 0, true);
		BufferedSpans currentBSpans = new BufferedSpans(firstQuery.getSpans(reader));
		for (int i= _minSize ; i < _length; i++) {
			BufferedSpans nextBSpans = null;
			Spans spans = _clauses[i].getSpans(reader);
			currentBSpans.reset();
			log.debug("i={}", i );
			log.debug("currentBSpans={}", currentBSpans);
			log.debug("new spans={}", spans);
			boolean more = advanceBothSpans(currentBSpans, spans);
			nextBSpans = new BufferedSpans();
			while (more) {
				int currentDoc = currentBSpans.doc();
				int newDoc = spans.doc();
				boolean hasNextMatch = true; //if the new span has next match
				if (currentDoc < newDoc) {
					//spans between currentDoc and newDoc cannot be extended anymore
					//they are added to the result
					log.debug("no more growth: currentDoc={}, newDoc={}", currentDoc, newDoc);
					more = addDocsTill(currentBSpans, newDoc);
				} else if (currentDoc == newDoc) {
					int oldEnd = currentBSpans.end();
					int newStart = spans.start();
					if (oldEnd < newStart) {
						//the current span in currentBSpans cannot be extended anymore
						log.debug("End before next token starts: oldEnd={}, newStart={}", oldEnd, newStart);
						more = addDoc(currentBSpans);
					} else if (oldEnd  == newStart) {
						//got a growing match
						//TODO: should keep Collection to the smallest possible,
						// just 8 bytes: the first 4 bytes are startOffset and the last 4 bytes are endOffset
						Collection<byte[]> payload = new ArrayList<byte[]>();
						payload.addAll(currentBSpans.getPayload());
						payload.addAll(spans.getPayload());
						nextBSpans.add(spans.doc(), currentBSpans.start(), spans.end(), payload);
						//remove the grownOne by using listIterator
						more = advanceBothSpans(currentBSpans, spans);
					} else {
						hasNextMatch = spans.next();
					}
				} else {
					//currentDoc > newDoc
					//the new span has to skip ahead until it has the same
					//docId as the currentDoc
					hasNextMatch = spans.skipTo(currentDoc);
				}
				if (!hasNextMatch) {
					//no more match for the new term
					//report all remaining current match
					addDocsTill(currentBSpans, Integer.MAX_VALUE);
					more = false;
				}
			}
			currentBSpans = nextBSpans;
		}
		/* add the matches that reached the end */
		currentBSpans.reset();
		if (currentBSpans.next()) {
			log.debug("final adding");
			addDocsTill(currentBSpans, Integer.MAX_VALUE);
		}
	}

	private boolean advanceBothSpans(BufferedSpans currentBSpans, Spans newSpans) throws IOException {
		boolean more = newSpans.next();
		if (!more) {
			// no more spans in the newSpans,
			// copy remaining matches to result
			log.debug("newSpans ended");
			more = currentBSpans.next();
			if (more) {
				addDocsTill(currentBSpans, Integer.MAX_VALUE);
			}
			return false;
		} else {
			return currentBSpans.next();
		}
	}

	/**
	 * Add all SpanInfos from the <code>src</code> to the
	 * results until the docId
	 * @param src
	 * @param docId
	 * @return
	 * @throws IOException
	 */
	private boolean addDocsTill(BufferedSpans src, int docId) throws IOException {
		boolean more = true;
		while (more) {
			if (src.doc() < docId) {
				more = addDoc(src);
			} else {
				return true;
			}
		}
		return more;
	}

	/** adding the current SpanInfo pointed by src
	 *  to the results
	 * @param src
	 * @return
	 * @throws IOException
	 */
	private boolean addDoc(BufferedSpans src) throws IOException {
		log.debug("adding " + src);
		results.add(src.getSpanInfo());
		return src.next();
	}

	public String toString(String field) {
		StringBuilder sb = new StringBuilder();
		sb.append("growingCloneQuery([");
		for (int i=0; i < _clauses.length; i++) {
			sb.append(_clauses[i].toString(field));
			if (i < _clauses.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}


