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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SolrIndexSearcher.QueryCommand;
import org.apache.solr.search.SolrIndexSearcher.QueryResult;
import org.slf4j.LoggerFactory;

/**
 * the main class that does the clone search
 * @author ye
 * @version $Id: CloneSearcher.java 524 2009-12-17 07:04:52Z ye $
 */
public class CloneSearcherSolr extends AbstractCloneSearcher {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(CloneSearcherSolr.class);

	private SolrIndexSearcher _solrSearcher;
		// using SolrIndexSearcher can speed up the process of getting
	    // highlights because SolrIndexSearcher pre-loads specified fields
	//fields that are needed for generating highlights

	private static final Set<String> _fieldSet  = new HashSet<String>(3);
	static {
		_fieldSet.add(Indexer.ID);
		_fieldSet.add(Indexer.SRC);
		_fieldSet.add(Indexer.BEGIN);
	}

	public CloneSearcherSolr(SolrIndexSearcher searcher, IndexReader reader,
			Analyzer analyzer, String fieldName, int dslop, int qslop) {
		_solrSearcher = searcher;
		//logger.debug("searcher creation");
		setFields(reader, analyzer, fieldName, dslop, qslop);
	}

	public CloneSearcherSolr(SolrIndexSearcher searcher, Analyzer analyzer) {
		this(searcher, searcher.getReader(), analyzer, Indexer.CLONETKN, 0, 0);
	}

	public CloneSearcherSolr(SolrIndexSearcher searcher, Analyzer analyzer, int dslop, int qslop) {
		this(searcher, searcher.getReader(), analyzer, Indexer.CLONETKN, dslop, qslop);
	}

	@Override
	protected DocListAndSet doSearch(SpanQuery q, List<Query> filterQueries, int start, int rows) throws IOException {

		QueryCommand qCmd1 = new QueryCommand();
		qCmd1.setQuery(q)
				//.setNeedDocSet(true)   //no need, if the result not used as a filter
				.setOffset(start)
				.setLen(rows)
				.setFlags(SolrIndexSearcher.GET_SCORES)
				.setFilterList(filterQueries);
		QueryResult qRes1 = new QueryResult();

		_solrSearcher.search(qRes1, qCmd1);

		return qRes1.getDocListAndSet();
	}

	@Override
	protected Document getDoc(int docId) throws IOException {
		return _solrSearcher.doc(docId, _fieldSet);
	}

	@Override
	protected String getIDField(int docId) throws IOException {
		return _solrSearcher.doc(docId).get(Indexer.ID);
	}
}
