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
import java.util.List;

import jp.co.sra.codedepot.index.ProgramAnalyzer;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.search.DocList;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search code clones.
 * Specified through solrconfig.xml
 *
 * replaced by CloneSearchComponent, so that we can stack SearchComponent's over each other.
 * @author ye
 *
 */
@Deprecated
public class CloneSearchHandler extends RequestHandlerBase {

	// clone fragment slop, change this must also change search.html
	//QSLOP: number of clone tokens from the query that may not exist in documents
	public static final String QSLOP = "qslop";
	//DSLOP: number of clone tokens from the document that may not exist in the query
	public static final String DSLOP = "dslop";

	// slop is a concept of SpanQuery in lucene, allowing number of tokens that
	// are not included in the search query
	// while FUZZY here allows documents to returned as search result if
	// they does not contain FUZZY number of tokens in query

	public static Logger log = LoggerFactory.getLogger(CloneSearchHandler.class.getName());

	private AbstractCloneSearcher _cloneSearcher;

	/**
	 * read the initial setting from solrconfig.xml
	 */
	@Override
	public void init(NamedList args) {
		super.init(args);
	}

	@Override
	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
			throws Exception {
		int flags = 0, start, rows;
		SolrParams params = req.getParams();
		SolrIndexSearcher searcher = req.getSearcher();

		start = params.getInt(CommonParams.START, 0);
		rows = params.getInt(CommonParams.ROWS, 10);

		int qslop = params.getInt(QSLOP, 0);
		int dslop = params.getInt(DSLOP, 0);

		//get return fields of fl=
		flags |= SolrPluginUtils.setReturnFields(req, rsp);

		//get filter queries
		List<Query> filters = new ArrayList<Query>(2);
		String[] fqStr = params.getParams(CommonParams.FQ);
		if (fqStr != null && fqStr.length > 0) {
			for (String fq: fqStr) {
				if (fq.trim().length() > 0) {
					QParser qParser = QParser.getParser(fq, null, req);
					filters.add(qParser.getQuery());
				}
			}
		}

		String qStr = params.get(CommonParams.Q);
		/* 27jul2009, each search handler uses its own cloneSearcher
		 * thinking about caching later

		if (_cloneSearcher == null) {
			_cloneSearcher = new CloneSearcher(searcher);
		}
		*/
		ProgramAnalyzer analyzer = ProgramAnalyzer.getAnalyzerWithLang(params.get("lang"));

		//_cloneSearcher  = new CloneSearcherSolr(searcher, analyzer, dslop, qslop);
		_cloneSearcher = AbstractCloneSearcher.getCloneSearcher(searcher, analyzer, dslop, qslop);

		// log.log(_loglevel, "\nSearching starts from " + searcher.maxDoc() + " documents");
		// log.log(_loglevel, "----Query----\n" + qStr);
		// SpanQuery q = SpanQueryUtils.createSpanQuery(qStr, _analyzer, Indexer.CLONETKN, _slop);
		_cloneSearcher.search(qStr, filters, dslop, qslop, start, rows);
		// rsp.add("totalMatched", _cloneSearcher.getTotalHits());
		// rsp.add("matched", _cloneSearcher.listAllMatchDocNums());

		// DocList has a method matches() that returns the total number of matched
		// although getDocList() only returns the list of documents that begins from start and ends
		// at start+rows
		rsp.add("response", getDocList());

		// log.log(_loglevel, "getDocList(): " + getDocList().size());
		log.debug("Get Higlighting");
		// _cloneSearcher.fillMatchingSources(); this must be called after search, combine this with search later
		rsp.add("highlighting", getHighlights());
		// log.log(_loglevel, "getHighlights(): " + getHighlights());
    }

    public DocList getDocList() {
        return _cloneSearcher.getDocList();
    }

    /**
     * return the text fragments that are shown, multiple fragments for each file
     * should be made possible.
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    public NamedList getHighlights() throws CorruptIndexException, IOException {
        return _cloneSearcher.getHighlights();
    }

    ////////////////////////// SolrInfoMBeans methods //////////////////////////////////////

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return "Search for code clones";
    }

    @Override
    public String getSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSourceId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return "$Id: CloneSearchHandler.java 2342 2017-11-09 05:36:32Z fang $";
    }


}
