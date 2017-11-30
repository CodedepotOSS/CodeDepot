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

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.lucene.search.HitCollector;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.util.OpenBitSet;
import org.apache.solr.util.SimpleOrderedMap;

/**
 * 09Jul2009, not used anymore by solrsearcher
 * Used by CloneSearcherLocal only.
 * Collecting documents and scores that match clone search query
 * @author ye
 *
 */
public class CloneMatchDocCollector extends HitCollector {
	private static Logger logger = Logger.getLogger(CloneMatchDocCollector.class.getName());
	Level _loglevel = Level.FINE;

	private NamedList _matchDocs; //docId -> CloneMatchDoc

	private int _totalHits;

	public CloneMatchDocCollector() {
		super();
		_totalHits = 0;
		_matchDocs = new SimpleOrderedMap<CloneMatchDoc>();
	}

	public NamedList getMatchDocs() {
		return _matchDocs;
	}

	public int getTotalHits() {
		return _totalHits;
	}

	@Override
	public void collect(int doc, float score) {
		addDocAndScore(doc, score);
		_totalHits++;
	}

	/**
	 * Update the score if existing. Create a new one
	 * if not existing.
	 */
	private void addDocAndScore(int docId, float score) {
		CloneMatchDoc d = (CloneMatchDoc) _matchDocs.get(docId+"");
		if (d == null) {
			d = new CloneMatchDoc(docId);
			d.setScore(score);
			_matchDocs.add(docId+"", d);
		} else {
			logger.log(Level.WARNING, "Should not have an existing doc. Old doc: " + d.getDocId() + " New doc: " + docId);
			d.setScore(score);
		}
	}

	public DocList getDocSlice() {
		int size = _matchDocs.size();
		int[] docs = new int[size];
		float[] scores = new float[size];
		float maxScore = 0;
		for (int i=0; i<size; i++) {
			docs[i] = Integer.parseInt(_matchDocs.getName(i));
			scores[i] = ((CloneMatchDoc) _matchDocs.getVal(i)).getScore();
			maxScore = maxScore > scores[i] ? maxScore : scores[i];
			logger.log(_loglevel, "doc: " + i + " " + _matchDocs.getName(i) + " " + scores[i]);
		}
		DocSlice docSlice = new DocSlice(0, size, docs, scores, _totalHits, maxScore);
		return docSlice;
	}


}
