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

import java.util.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrQueryParser;
/**
 *
 * @author ye
 *
 */
public class CodeDepotQParser extends QParser {
  public static final String UNIT_QUERY = "dq";
  //Logic search field names
  public static final String LOGIC_FIELD_DEFAULT = "any";
  public static final String LOGIC_FIELD_CLASS = "cls";
  public static final String LOGIC_FIELD_FDEF = "fdef";
  public static final String LOGIC_FIELD_CODE = "code";
  public static final String LOGIC_FIELD_FILE = "file";
  public static final String LOGIC_FIELD_NS = "ns";

  //Field translation alias, from "logic field" (user query field) to "physical fields" (real field in solr)
  //TODO replace strings with constant names from the Indexer class
  private static String DEFAULT_CFA = LOGIC_FIELD_DEFAULT + ":src,key,code^2.0,comment,cls^4.0,fdef^4.0,locationtxt " +
  		LOGIC_FIELD_CLASS + ":cls,clsstr^4.0 " +
  		LOGIC_FIELD_FDEF + ":fdef,fdefstr^4.0 " +
  		LOGIC_FIELD_CODE + ":code,codestr^4.0 " +
  		LOGIC_FIELD_FILE + ":location,locationtxt " +
  		LOGIC_FIELD_NS + ":pkg";
  //Field translation for quoted queries (user input phrase query)
  private static String DEFAULT_CPFA = LOGIC_FIELD_DEFAULT + ":src,clsstr^4.0,fdefstr^4.0,location " +
  		LOGIC_FIELD_CLASS + ":clsstr " +
  		LOGIC_FIELD_FDEF + ":fdefstr " +
  		LOGIC_FIELD_CODE + ":codestr " +
  		LOGIC_FIELD_FILE + ":location,locationtxt " +
  		LOGIC_FIELD_NS + ":pkg";
  //Field boost alias, additional phrase quries created to boost that fields' query
  //private static String DEFAULT_CFBA = "any:src~5^2.0,code~5,comment~5 comment:comment~5";
  private static String DEFAULT_CFBA = "";
  private static String DEFAULT_DQ = "+unit:class";

  //to control if DQ is added
  //DQ should not be processed for fq query
  private boolean doDefaultQueryAddition = true;

  private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CodeDepotQParserPlugin.class);

  /* following attributes are defined in the super class but are package private */
  private String qstr;
  private SolrParams params;
  private SolrParams localParams;
  private SolrQueryRequest req;
  private int recurseCount;

  private BooleanQuery query;

  public CodeDepotQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    //TODO do we need call this super?
    super(qstr, localParams, params, req);
    this.qstr = qstr;
    this.localParams = localParams;
    this.params = params;
    this.req = req;
  }

  /**
   * set doUnitQueryAddition
   * @param b
   */
  public void setDoUnitQueryAddition(boolean b) {
	  doDefaultQueryAddition = b;
  }

  @Override
  public Query parse() throws ParseException {
    // the string to be parsed is stored in the instance attributes,
    // together the solrQueryRequest
    //TODO get the right default field, set it to null,
    //SolrQueryParser will fill with the default field from schema.xml
    //if CodeDepotQueryParser does not inherit from SolrQueryParser, we need
    //do it ourselves.
    CodeDepotQueryParser parser = new CodeDepotQueryParser(req.getSchema(), null);
    /* set up the default operator */
    String operatorString = getParam(QueryParsing.OP);
    if (operatorString != null)
      parser.setDefaultOperator("AND".equals(operatorString) ? CodeDepotQueryParser.Operator.AND : CodeDepotQueryParser.Operator.OR);
    else {
      SolrQueryParser dummy_parser = getReq().getSchema().getSolrQueryParser(null);
      SolrQueryParser.Operator operator = dummy_parser.getDefaultOperator();
      if (operator == SolrQueryParser.Operator.AND)
        parser.setDefaultOperator(CodeDepotQueryParser.Operator.AND);
      else
        parser.setDefaultOperator(CodeDepotQueryParser.Operator.OR);
    }
    // read CFA definition.
    String cfa = this.req.getParams().get("cfa", DEFAULT_CFA);
    if (cfa != null) {
      try {
        parser.setCfa(cfa);
      } catch (Exception e) {
        logger.debug("Caught exception when parsing cfa parameter: "+e);
      }
    }
    // read CPFA definition.
    String cpfa = this.req.getParams().get("cpfa", DEFAULT_CPFA);
    if (cpfa != null) {
      try {
        parser.setCpfa(cpfa);
      } catch (Exception e) {
        logger.debug("Caught exception when parsing cpfa parameter: "+e);
      }
    }
    // read CFBA definition.
    String cfba = this.req.getParams().get("cfba", DEFAULT_CFBA);
    if (cfba != null) {
      try {
        parser.setCfba(cfba);
      } catch (Exception e) {
        logger.debug("Caught exception when parsing cfba parameter: "+e);
      }
    }
    logger.debug("loaded cfa="+parser.getCfaTranslator());
    logger.debug("loaded cpfa="+parser.getCpfaTranslator());
    logger.debug("loaded cfba="+parser.getCfbaTranslator());
    // parse the query string and ensure it a boolean query.
    try {
      Query q = parser.parse(qstr);
      if (q instanceof BooleanQuery) {
        query = (BooleanQuery)q;
      }
      else {
        query = new BooleanQuery();
        query.add(q, Occur.MUST);
      }
    } catch (jp.co.sra.codedepot.queryParser.ParseException e) {
      ParseException e1 = new ParseException(e.getMessage());
      e1.initCause(e);
      throw e1;
    }

    Utils.adjustMinimumShouldMatch(query);

    if ( ! doDefaultQueryAddition) {
    	return query;
    } else {

	    //the remaining is to add dq if unit is not specified in the
	    //q string
	    //TODO this is better to be moved to the process method of CodeDepotQueryComponent
	    BooleanQuery finalQuery; //final query to be returned
	    Set<String> fields_in_query = parser.getFieldSet();

	    if (query.clauses().size() == 1)
	      finalQuery=query;
	    else {
	      finalQuery = new BooleanQuery();
	      finalQuery.add(query, Occur.MUST);
	    }

	    BooleanQuery dq;
	    try {
	      Query q = parser.parse(this.req.getParams().get(UNIT_QUERY, DEFAULT_DQ));
	      if (q instanceof BooleanQuery)
	        dq = (BooleanQuery)q;
	      else {
	        dq = new BooleanQuery();
	        dq.add(q, Occur.MUST);
	      }
	    } catch (jp.co.sra.codedepot.queryParser.ParseException e) {
	      dq = new BooleanQuery();
	    }


	    // parse the default clauses and ensure it a boolean query.
	    // iterates over the default clauses and append each of them if it is not
	    // specified in the query.
	    for (BooleanClause clause : (List<BooleanClause>)dq.clauses()) {
	      Query q = clause.getQuery();
	      String field = null;
	      if (q instanceof TermQuery)
	        field = ((TermQuery)q).getTerm().field();
	      else if (q instanceof PrefixQuery)
	        field = ((PrefixQuery)q).getPrefix().field();
	      else if (q instanceof WildcardQuery)
	        field = ((WildcardQuery)q).getTerm().field();
	      if (field != null && !fields_in_query.contains(field)) {
	          //query.add(new BooleanClause((Query)q.clone(), clause.getOccur()));
	          finalQuery.add(new BooleanClause((Query)q.clone(), clause.getOccur()));
	      }
	    }
	    //logger.debug("parsed query is: {}", query);
	    //return query;
	    logger.debug("parsed query is: {}", finalQuery);
	    return finalQuery;
    }
  }
}
