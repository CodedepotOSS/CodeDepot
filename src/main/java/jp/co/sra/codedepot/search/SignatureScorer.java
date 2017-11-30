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
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create scorer for signature query.
 * Has to override the following methods:
 *   score():
 *
 * _iterator iterates over the query terms, for query inType:(int int String),
 * we have an _iterator like
 * 		{int, 2}
 *      {String, 1}
 *
 *nextDoc(): returns the next matching document (according to Signature Query criterion)
 *	(1) create all candidate documents _candidateDocs, which
 *	    are documents that match at least one of the terms in _query.
 *		Because a matching document must contain all terms in _query,
 *      we only need to use the first term in _iterator.
 *      //TODO: should we try to return least matching one?
 *  (2)
 *
 * @author yunwen
 * @$Id: SignatureScorer.java 2356 2017-11-10 07:50:30Z fang $
 */
class SignatureScorer extends Scorer {
	final static Logger logger = LoggerFactory.getLogger(SignatureScorer.class);

	private IndexReader _reader;
	private SignatureWeight _weight;
	private SignatureQuery _query;
	//iterator of the (term, freq) map in the query;
	private int _qlen; //number of unique terms in query;

	private byte[] _norms;

	//document that match _termFreq.getKey() and _termFreq.getValue();
	private int _matchDoc = -1; //currently matching document id
	//whether nextDoc() or skipTo() is called first time
	private boolean firstTime = true;
	//have more documents to work on
	private boolean more = true;
	//are we in the same doc of all sub termDocs;
	private boolean inSameDoc = false;

	//a list of termdocs in correspondence to each term in the query
	//TermDocs[] _subMatches;
	TermPositions[] _subMatches;

	int[] _freqList;


	SignatureScorer(Similarity similarity, IndexReader reader, SignatureWeight weight, SignatureQuery query) throws IOException {
		super(similarity);
		_weight = weight;
		_query = query;
		_reader = reader; //TODO make sure this is needed as a field value
		_norms = _reader.norms(_query.getField());
		Map<Term, Integer> queryTermFreqMap = query.getTermFreqs();
		_qlen = queryTermFreqMap.size();
		//_subMatches = new TermDocs[_qlen];
		_subMatches = new TermPositions[_qlen];
		//_subMatchesSortedByDoc = new TermDocs[_qlen];
		_freqList = new int[_qlen];
		int idx = 0;
		for (Iterator<Map.Entry<Term, Integer>> iterator = queryTermFreqMap.entrySet().iterator();
			iterator.hasNext(); ) {
			Map.Entry<Term, Integer> qtf = iterator.next();
			//_subMatches[idx] = _reader.termDocs(qtf.getKey());
			_subMatches[idx] = _reader.termPositions(qtf.getKey());
			//_subMatchesSortedByDoc[idx] = _subMatches[idx];
			_freqList[idx] = qtf.getValue();
			idx++;
		}
	}

	@Override
	public float score() throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("SignatureScorer#score {}",  _matchDoc);
			for (int i=0; i<_qlen; i++) {
				assert _subMatches[i].doc() == _matchDoc;
			}
		}

		float s = _weight.getValue(); //query's weight value

		float[] decoder = Similarity.getNormDecoder();
		float lengthNorm = decoder[_norms[_matchDoc] & 0xFF]; //norm has only one byte
		logger.debug("\tlength norm of {} is {} ", _query, lengthNorm);
		s *= lengthNorm;
		logger.debug("\ts is: " + s);
		return s;
	}

	//deprecated in 2.9, but may still be used in solr
	@Override
	public int doc() {
		//System.out.println("SignatureScorer#doc() --> " + _matchDoc);
		return _matchDoc;
	}

	@Override
	public int docID() {
		//ystem.out.println("SignatureScorer#docId() --> " + _matchDoc);
		return _matchDoc;
	}

	@Override
	public boolean next() throws IOException {
		//System.out.println("next() is depracated");
		_matchDoc = nextDoc();
		return _matchDoc != Scorer.NO_MORE_DOCS;
	}

	@Override
	public int nextDoc() throws IOException {
		//System.out.println("SignatureScorer#nextDoc");
		if (firstTime) {
			firstTime = false;
			for (int i=0; i<_qlen; i++) {
				if (! _subMatches[i].next()) {
					more = false;
					return Scorer.NO_MORE_DOCS;
				}
			}
			more = true;
		}
		if (!incrementAllSubMatches(_matchDoc + 1)) {
			_matchDoc = Scorer.NO_MORE_DOCS;
		}
		return _matchDoc;
	}

	@Override
	public int advance(int target) throws IOException {
		logger.debug("SignatureScorer#advance({})", target);
		if (!incrementAllSubMatches(target)) {
			_matchDoc = Scorer.NO_MORE_DOCS;
		}
		return _matchDoc;
	}
	/** Advance each termDocs in the subMatches to the same doc number
	 *
	 * @return
	 * @throws IOException
	 */
	private boolean incrementAllSubMatches(int target) throws IOException {
		logger.debug("incrementAllSubMatches({})", target);
		if (target == Scorer.NO_MORE_DOCS) return false;
		int candidateDoc = target; //start from the one after the previous match
		_matchDoc = Scorer.NO_MORE_DOCS;
		int startingIndex = 0; //where the current candidateDoc starts;
		inSameDoc = false;
		while (!inSameDoc) {
			int startDoc = candidateDoc;
			int idx = startingIndex;
			inSameDoc = true;
			int count = 0;
			while( inSameDoc && count<_qlen) {
				TermDocs tds = _subMatches[idx];
				int qtf = _freqList[idx];
				count++;
				if (++idx == _qlen) idx = 0;
				if (tds.doc() < candidateDoc ) {
					if (!tds.skipTo(candidateDoc)) return false; //no match
				}
				boolean notAMatchYet = true;
				while (notAMatchYet) {
					candidateDoc = tds.doc();
					if (tds.freq() >= qtf) {
						notAMatchYet = false; //end of loop,
					} else {
						if (!tds.next()) return false; //no more match;
					}
				}
				if (candidateDoc != startDoc) {
					//we have advanced beyond startDoc,
					//restart matching again
					startingIndex = idx;
					inSameDoc = false;
				}
			}
			if (inSameDoc) {
				assert candidateDoc == startDoc;
				/*
				if (positionOverlapped(candidateDoc)) {
					inSameDoc = false;
				}
				*/
				_matchDoc = startDoc;
			}
		}
		return inSameDoc;
	}

	/**The document of <pre>candidateDoc</pre> is a potential match.
	 * But it is not a match if one position's multiple term is used
	 * multiple times. For example
	 *    Query: java.lang.String String
	 *    Document: java.lang.String
	 * The above Document should not be a match, but counted as one.
	 * Should be excluded here.
	 * <br>
	 * No perfect solution yet. Here is an outline algorithm, time-consuming
	   <pre>
		List<List<int[]>> subListsList = new ArrayList<List<int[]>>(_qlen);
		for (int i=0; i < _qlen; i++) {
			//change TermDocs to TermPositions in _subMatches;
			//get an array of all positions for _subMatches[i];
			int[] positions = getAllPositions(subMathces[i]);
			//get all subListes from the positions of the given query term frequency
			// Query: String String int
			//    String: d1(p1, p2, p3) ==> ((p1, p2), (p2, p3), (p1 p3))
			subListsList.add(getSublistsOfLength(Arrays.asList());
		}
		Loop over the subList set of subMatches and find a matchedPosition that
		doesn't have the same position
	   </pre>
	 * Or, we can try to remove the most obvious problem (the above example)
	 *
	 * @return true if one position is used by multiple terms
	 * @throws IOException
	 */
	private boolean positionOverlapped(int candidateDoc) throws IOException {
		/* not working yet
		Set<Integer> matchedPositions = new HashSet<Integer>();
		for (int i=0; i<_qlen; i++) {
			//for (int j=0; j<_subMatches[i].freq(); j++) {
			//	matchedPositions.add(_subMatches[i].nextPosition());
			//}

			if (_subMatches[i].freq() == 1) {
				int pos = _subMatches[i].nextPosition();
				if (matchedPositions.contains(pos)) return true;
				matchedPositions.add(pos);
			}
		}
		//if (matchedPositions.size() < _query.getTermNumber()) {
		//	return true;
		//}
		*/
		return false;
	}

	public String toString() {
		return super.toString() + "of query " + _query.toString();
	}
}
