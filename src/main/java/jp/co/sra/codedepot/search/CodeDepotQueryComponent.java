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
 * This program is a modification to the original
 * 	org.apache.solr.handler.component.QueryComponent
 * which is licensed as follows
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.sra.codedepot.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.util.SolrPluginUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override QueryComponent of solr because solr only allows to
 * use the default query parser (which is LuceneQParer).
 * CodeDepot needs to use CodeDepotQParser to parse fq too.
 *
 * Only the prepare method is overwritten, process is not override.
 * @author ye
 *
 */
public class CodeDepotQueryComponent extends QueryComponent {
	private static final Logger logger = LoggerFactory.getLogger(CodeDepotQueryComponent.class);

	public static final String COMPONENT_NAME = "codedepotQuery";

	/**
	 * Use defType to parse both query (q) and filter query (fq)
	 */
	@Override
	public void prepare(ResponseBuilder rb) {
		 SolrQueryRequest req = rb.req;
		 SolrParams params = req.getParams();
		 if (!params.getBool(COMPONENT_NAME, true)) {
			 return;
		 }
		 SolrQueryResponse rsp = rb.rsp;

		 // Set field flags
		 String fl = params.get(CommonParams.FL);
		 int fieldFlags = 0;
		 if (fl != null) {
			 fieldFlags |= SolrPluginUtils.setReturnFields(fl, rsp);
		 }
		 rb.setFieldFlags( fieldFlags );

		 String defType = params.get(QueryParsing.DEFTYPE);
		 defType = defType==null ? QParserPlugin.DEFAULT_QTYPE : defType;

		 if (rb.getQueryString() == null) {
			 rb.setQueryString( params.get( CommonParams.Q ) );
		 }

		 try {
			 QParser parser = QParser.getParser(rb.getQueryString(), defType, req);
			 rb.setQuery( parser.getQuery() );
			 rb.setSortSpec( parser.getSort(true) );
			 rb.setQparser(parser);

			 String[] fqs = req.getParams().getParams(CommonParams.FQ);
			 if (fqs!=null && fqs.length!=0) {
				 List<Query> filters = rb.getFilters();
				 if (filters==null) {
					 filters = new ArrayList<Query>();
					 rb.setFilters( filters );
				 }
				 for (String fq : fqs) {
					 if (fq != null && fq.trim().length()!=0) {

						 //ye-26Jan2010
						 //instead of null, we use defType to parse filter query two.
						 //QParser fqp = QParser.getParser(fq, null, req);
						 QParser fqp = QParser.getParser(fq, defType, req);
						 if (fqp instanceof CodeDepotQParser) {
							 ((CodeDepotQParser) fqp).setDoUnitQueryAddition(false);
						 }
						 Query q = fqp.getQuery();
						 logger.debug(q.toString());
						 filters.add(q);
					 }
				 }
			 }
		 } catch (ParseException e) {
			 throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
		 }

		 // TODO: temporary... this should go in a different component.
		 String shards = params.get(ShardParams.SHARDS);
		 if (shards != null) {
			 List<String> lst = StrUtils.splitSmart(shards, ",", true);
			 rb.shards = lst.toArray(new String[lst.size()]);
		 }
		 String shards_rows = params.get(ShardParams.SHARDS_ROWS);
		 if(shards_rows != null) {
			 rb.shards_rows = Integer.parseInt(shards_rows);
		 }
		 String shards_start = params.get(ShardParams.SHARDS_START);
		 if(shards_start != null) {
			 rb.shards_start = Integer.parseInt(shards_start);
		 }
	}

	// for SolrInfoBean only, not used for functionality
	@Override
	public String getVersion() {
		return "$Revision$";
	}

	@Override
	public String getSourceId() {
		return "$Id$";
	}

	@Override
	public String getDescription() {
		return "codedepotQuery";
	}

}
