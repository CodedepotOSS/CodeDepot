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

import jp.co.sra.codedepot.queryParser.ParseException;

import java.util.*;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;

import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;

/**
 * codepot queryを生成するクラス。
 * getFieldQueryを書き直すことにより、ユーザクエリで書かれたfieldにより、実際の検索フィールドと検索クエリ
 * に書き換える。
 * 初期化するときに、ユーザクエリから検索クエリに変更するスキーマを設定する。
 * @author yunwen
 *
 */

public class CodeDepotQueryParser extends jp.co.sra.codedepot.queryParser.SolrQueryParser {
  private static org.slf4j.Logger logger =
    org.slf4j.LoggerFactory.getLogger(CodeDepotQParserPlugin.class);
  // cfa translates a logical query into a physical query.
  private FieldTranslator cfa = new FieldTranslator();
  // cpfa translates a logical phrase query into a physical one.
  private FieldTranslator cpfa = new FieldTranslator();
  // cfba aggregates terms into a phrase and append it to the query.
  private FieldTranslator cfba = new FieldTranslator();
  // queryTermRecord stores term images for field queries to generate a phrase query for cpfa.
  private HashMap<String,ArrayList<String>> queryTermRecord;
  private HashSet<String> fieldSet;

  //TODO just placeholder
  //defaultField could be null, it then relies on SolrQueryParser to get the
  //defaultField from schema.xml
  public CodeDepotQueryParser(QParser parser, String defaultField, Analyzer analyzer) {
    super(parser, defaultField, analyzer);
    setLowercaseExpandedTerms(true);
  }

  //TODO just placeholder
  public CodeDepotQueryParser(IndexSchema schema, String defaultField) {
    super(schema, defaultField);
    setLowercaseExpandedTerms(true);
  }

  /** get the cfa field translator */
  public FieldTranslator getCfaTranslator() {
    return cfa;
  }

  /** set a cfa field translator */
  public void setCfaTranslator(FieldTranslator ft) {
    cfa = ft;
  }

  /** get the cpfa field translator */
  public FieldTranslator getCpfaTranslator() {
    return cpfa;
  }

  /** set a cpfa field translator */
  public void setCpfaTranslator(FieldTranslator ft) {
    cpfa = ft;
  }

  /** get the cfba field translator */
  public FieldTranslator getCfbaTranslator() {
    return cfba;
  }

  /** set a cfba field translator */
  public void setCfbaTranslator(FieldTranslator ft) {
    cfba = ft;
  }

  /** set cfa */
  public void setCfa(String fieldSpec) throws Exception {
    cfa.setAliases(fieldSpec);
  }

  /** set cpfa */
  public void setCpfa(String fieldSpec) throws Exception {
    cpfa.setAliases(fieldSpec);
  }

  /** set cfba */
  public void setCfba(String fieldSpec) throws Exception {
    cfba.setAliases(fieldSpec);
  }

  /**
   * parse a query string
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  public Query parse(String queryString) throws ParseException {
    queryTermRecord = new HashMap<String,ArrayList<String>>();
    fieldSet = new HashSet<String>();
    BooleanQuery query = newBooleanQuery(false);
    query.add(newBooleanClause(super.parse(queryString), Occur.MUST));
    for (Entry<String, ArrayList<String>> e : queryTermRecord.entrySet()) {
      String field = e.getKey();
      StringBuffer phraseBuffer = new StringBuffer();
      if (!cfba.containsLogicalField(field))
        continue;
//System.out.println("expanding CFBA:"+field);
      for (String phraseTerm : e.getValue()) {
        if (phraseBuffer.length() > 0)
          phraseBuffer.append(" ");
        phraseBuffer.append(phraseTerm);
      }
      query.add(cfba.getFieldQuery(field, phraseBuffer.toString(), this), Occur.SHOULD);
    }
    while (query.clauses().size() == 1
        && ((BooleanClause)query.clauses().get(0)).getOccur() == Occur.MUST
        && ((BooleanClause)query.clauses().get(0)).getQuery() instanceof BooleanQuery)
      query = (BooleanQuery)((BooleanClause)query.clauses().get(0)).getQuery();
    return query;
  }

  /**
   * get a set of physical fields
   */
  public Set<String> getFieldSet() {
    HashSet<String> set = new HashSet<String>(fieldSet.size());
    set.addAll(fieldSet);
    return set;
  }

  /**
   * create a logical field query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getFieldQuery(String field, String queryText)
  throws ParseException {
    logger.debug("Field={}, Text={}", field, queryText);
    if (!queryTermRecord.containsKey(field))
      queryTermRecord.put(field, new ArrayList<String>());
    queryTermRecord.get(field).add(queryText);
    if (parsingPhraseQuery) {
      return cpfa.getFieldQuery(field, queryText, this);
    } else {
      return cfa.getFieldQuery(field, queryText, this);
    }
  }

  protected Query _getFieldQuery(String field, String queryText)
  throws ParseException {
    fieldSet.add(field);
    return super.getFieldQuery(field, queryText);
  }

  /**
   * create a logical field query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getFieldQuery(String field, String queryText, int slop)
  throws ParseException {
    logger.debug("Field={}, Text={}", field, queryText);
    if (!queryTermRecord.containsKey(field))
      queryTermRecord.put(field, new ArrayList<String>());
    queryTermRecord.get(field).add(queryText);
    if (parsingPhraseQuery) {
      return cpfa.getFieldQuery(field, queryText, slop, this);
    } else {
      return cfa.getFieldQuery(field, queryText, slop, this);
    }
  }

  protected Query _getFieldQuery(String field, String queryText, int slop)
  throws ParseException {
    Query query;
    fieldSet.add(field);
    query = super.getFieldQuery(field, queryText);
    if (query instanceof PhraseQuery) {
      ((PhraseQuery) query).setSlop(slop);
    } else if (query instanceof MultiPhraseQuery) {
      ((MultiPhraseQuery) query).setSlop(slop);
    }
    return query;
  }

  /**
   * create a logical fuzzy query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getFuzzyQuery(String field, String queryText, float fms)
  throws ParseException {
    logger.debug("Field={}, Text={}", field, queryText);
    if (!queryTermRecord.containsKey(field))
      queryTermRecord.put(field, new ArrayList<String>());
    queryTermRecord.get(field).add(queryText);
    return cfa.getFuzzyQuery(field, queryText, fms, this);
  }

  protected Query _getFuzzyQuery(String field, String queryText, float fms)
  throws ParseException {
    fieldSet.add(field);
    return super.getFuzzyQuery(field, queryText, fms);
  }

  /**
   * create a logical prefix query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getPrefixQuery(String field, String termStr)
  throws ParseException {
    logger.debug("Field={}, Text={}", field, termStr);
    if (!queryTermRecord.containsKey(field))
      queryTermRecord.put(field, new ArrayList<String>());
    queryTermRecord.get(field).add(termStr);
    return cfa.getPrefixQuery(field, termStr, this);
  }

  protected Query _getPrefixQuery(String field, String termStr)
  throws ParseException {
    logger.debug("Field={}, Text={}", field, termStr);
    fieldSet.add(field);
    return super.getPrefixQuery(field, termStr);
  }

  /**
   * create a logical range query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive)
  throws ParseException {
    return cfa.getRangeQuery(field, part1, part2, inclusive, this);
  }

  protected Query _getRangeQuery(String field, String part1, String part2, boolean inclusive)
  throws ParseException {
    fieldSet.add(field);
    return super.getRangeQuery(field, part1, part2, inclusive);
  }

  /**
   * create a logical wildcard query
   * @throws jp.co.sra.codedepot.queryParser.ParseException
   */
  @Override
  protected Query getWildcardQuery(String field, String termStr)
  throws ParseException {
    if (!queryTermRecord.containsKey(field))
      queryTermRecord.put(field, new ArrayList<String>());
    queryTermRecord.get(field).add(termStr);
    return cfa.getWildcardQuery(field, termStr, this);
  }

  protected Query _getWildcardQuery(String field, String termStr)
  throws ParseException {
    fieldSet.add(field);
    return super.getWildcardQuery(field, termStr);
  }

  /**
   * Command line tool to test QueryParser.
   * Usage:<br>
   * <code>java jp.co.sra.codedepot.search.CodeDepotQueryParser &lt;input&gt;</code>
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java jp.co.sra.codedepot.search.CodeDepotQueryParser <input>");
      System.exit(0);
    }
    CodeDepotQueryParser qp = new CodeDepotQueryParser(
      new IndexSchema(
        new org.apache.solr.core.SolrConfig("conf/solrconfig.xml"),
        "conf/schema.xml"),
      "any");
    Query q = qp.parse(args[0]);
    System.out.println(q.toString());
  }
}
