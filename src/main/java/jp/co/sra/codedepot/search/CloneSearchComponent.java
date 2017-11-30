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
/**
 *
 */
package jp.co.sra.codedepot.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.sra.codedepot.index.ProgramAnalyzer;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matubara
 *
 */
public class CloneSearchComponent extends SearchComponent {
    public static Logger log = LoggerFactory.getLogger(CloneSearchComponent.class.getName());

    // clone fragment slop, change this must also change search.html
    //QSLOP: number of clone tokens from the query that may not exist in documents
    public static final String QSLOP = "qslop";
    //DSLOP: number of clone tokens from the document that may not exist in the query
    public static final String DSLOP = "dslop";

    /*
     * @see org.apache.solr.handler.component.SearchComponent#getDescription()
     */
    public String getDescription() {
        return "Search for code clones";
    }

    /* (non-Javadoc)
     * @see org.apache.solr.handler.component.SearchComponent#getSource()
     */
    public String getSource() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.solr.handler.component.SearchComponent#getSourceId()
     */
    public String getSourceId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see org.apache.solr.handler.component.SearchComponent#getVersion()
     */
    public String getVersion() {
        return "codedepot2.0";
    }

    /*
     * @see org.apache.solr.handler.component.SearchComponent#prepare(org.apache.solr.handler.component.ResponseBuilder)
     */
    public void prepare(ResponseBuilder rb) throws IOException {
    	SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;

        int flags = 0, start, rows;
        SolrParams params = req.getParams();

        //get return fields of fl=
        flags |= SolrPluginUtils.setReturnFields(req, rsp);

        if (rb.getQueryString() == null) {
        	rb.setQueryString(params.get(CommonParams.Q));
        }

        String defType = params.get(QueryParsing.DEFTYPE);
        defType = defType==null ? QParserPlugin.DEFAULT_QTYPE : defType;

        //get filter queries

        String[] fqStr = params.getParams(CommonParams.FQ);
        if (fqStr != null && fqStr.length > 0) {
        	List<Query> filters = rb.getFilters();
            if (filters == null) {
            	filters = new ArrayList<Query>(2);
            	rb.setFilters(filters);
            }
            for (String fq : fqStr) {
                if (fq.trim().length() > 0) {
                    QParser qParser;
                    try {
                        qParser = QParser.getParser(fq, defType, req);
                        filters.add(qParser.getQuery());
                    } catch (ParseException e) {
                        log.error("parse error", e);
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /*
     * @see org.apache.solr.handler.component.SearchComponent#process(org.apache.solr.handler.component.ResponseBuilder)
     */
    public void process(ResponseBuilder rb) throws IOException {
        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;

        int flags = 0, start, rows;
        SolrParams params = req.getParams();
        SolrIndexSearcher searcher = req.getSearcher();

        start = params.getInt(CommonParams.START, 0);
        rows = params.getInt(CommonParams.ROWS, 10);

        int qslop = params.getInt(QSLOP, 0);
        int dslop = params.getInt(DSLOP, 0);

        String qStr = params.get(CommonParams.Q);
        List<Query> filters = rb.getFilters();
        /* 27jul2009, each search handler uses its own cloneSearcher
         * thinking about caching later

        if (_cloneSearcher == null) {
            _cloneSearcher = new CloneSearcher(searcher);
        }
        */
        ProgramAnalyzer analyzer = ProgramAnalyzer.getAnalyzerWithLang(params.get("lang"));

        //_cloneSearcher  = new CloneSearcherSolr(searcher, analyzer, dslop, qslop);
        AbstractCloneSearcher _cloneSearcher = AbstractCloneSearcher.getCloneSearcher(searcher, analyzer, dslop, qslop);

        // log.log(_loglevel, "\nSearching starts from " + searcher.maxDoc() + " documents");
        // log.log(_loglevel, "----Query----\n" + qStr);
        // SpanQuery q = SpanQueryUtils.createSpanQuery(qStr, _analyzer, Indexer.CLONETKN, _slop);
        _cloneSearcher.search(qStr, filters, dslop, qslop, start, rows);
        // rsp.add("totalMatched", _cloneSearcher.getTotalHits());
        // rsp.add("matched", _cloneSearcher.listAllMatchDocNums());

        // DocList has a method matches() that returns the total number of matched
        // although getDocList() only returns the list of documents that begins from start and ends
        // at start+rows
        rsp.add("response", _cloneSearcher.getDocList());

        // log.log(_loglevel, "getDocList(): " + getDocList().size());
        //TODO maybe we should move this a highlight component.
        log.debug("Get Higlighting");
        // _cloneSearcher.fillMatchingSources(); this must be called after search, combine this with search later
	if (params.getBool("hl", true)) {
            rsp.add("highlighting", _cloneSearcher.getHighlights());
            // log.log(_loglevel, "getHighlights(): " + getHighlights());
	}
    }

}
