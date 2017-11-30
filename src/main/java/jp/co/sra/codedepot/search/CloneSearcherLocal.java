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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.co.sra.codedepot.index.java.JavaProgramAnalyzer;
import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SolrIndexSearcher.QueryResult;
import org.apache.solr.util.SimpleOrderedMap;

/**
 * the main class that does the clone search for indexes at local
 * This one uses lucene Searcher only
 *
 * The code is not directly used in codepot. Mainly for testing purpose.
 * Also could be used for local search if a local clone search application
 * is developed when the performance of internet-based search becomes a
 * severe problem.
 *
 * FIXME filter
 * @author ye
 *
 */
public class CloneSearcherLocal extends AbstractCloneSearcher{

	private Searcher _searcher;
	private CloneMatchDocCollector _matchDocCollector;
	//private NamedList<CloneMatchDoc> _pmatchdocs;

	public CloneSearcherLocal(Searcher searcher, IndexReader reader,
			Analyzer analyzer, String fieldName, int dslop, int qslop) {
		_searcher = searcher;
		super.setFields(reader, analyzer, fieldName, dslop, qslop);
	}

	public CloneSearcherLocal(SolrIndexSearcher searcher) {
		this(searcher, searcher.getReader(), new JavaProgramAnalyzer(), Indexer.CLONETKN, 0, 0);
	}

	@Override
	public DocListAndSet doSearch(SpanQuery q, List<Query> filters, int start, int rows)
	throws IOException {
		//FIXME use ChainedFilter or others to use the filter
		Filter filter = null;
		_matchDocCollector = new CloneMatchDocCollector();
		_searcher.search(q, filter, _matchDocCollector);
		//_pmatchdocs = _matchDocCollector.getMatchDocs();
		return getDocListAndSet();
	}

	private DocListAndSet getDocListAndSet() {
		// return _qr.getDocList();
		DocListAndSet ds = new DocListAndSet();
		ds.docList = _matchDocCollector.getDocSlice();
		//todo set DocSet if needed
		return ds;
	}

	@Override
	protected Document getDoc(int docId) throws IOException {
		return _searcher.doc(docId);
	}

	@Override
	protected String getIDField(int docId) throws IOException {
		return _searcher.doc(docId).get(Indexer.ID);
	}
}
