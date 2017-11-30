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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause;

/**
 * Field spec.
 * @author tomohiro
 */

public class FieldSpec {
  public String field;
  public float boost;
  public int slop;

  static int DEFAULT_SLOP = 0;
  static float DEFAULT_BOOST = 1.0f;

  /** constructs a logical field */
  public FieldSpec(String field) {
    this(field, DEFAULT_SLOP, DEFAULT_BOOST);
  }
  /** constructs a logical field with a boost value */
  public FieldSpec(String field, float boost) {
    this(field, DEFAULT_SLOP, boost);
  }
  /** constructs a logical field with a slop value */
  public FieldSpec(String field, int slop) {
    this(field, slop, DEFAULT_BOOST);
  }
  /** constructs a logical field with a slop and boost values */
  public FieldSpec(String field, float boost, int slop) {
    this(field, slop, boost);
  }

  /** constructs a logical field with a slop and boost values */
  public FieldSpec(String field, int slop, float boost) {
    this.field = new String(field);
    this.boost = boost;
    this.slop = slop;
  }

  /** parse and constract the given logical field spec. */
  public static FieldSpec parse(String spec) throws Exception {
    String field;
    float boost = DEFAULT_BOOST;
    int slop = DEFAULT_SLOP;
    String[] subspecs = spec.split("~");
    switch (subspecs.length) {
      case 1:
        // there's no slop value. look for a boost value.
        subspecs = subspecs[0].split("\\^");
        field = subspecs[0];
        switch (subspecs.length) {
          case 1:
            break;
          case 2:
            boost = new Float(subspecs[1]).floatValue();
            break;
          default:
            throw new Exception("A physical field spec can have only one boost value(^): "+spec);
        }
        break;
      case 2:
        // a slop value is found. look for a boost value in the latter piece.
        field = subspecs[0];
        subspecs = subspecs[1].split("\\^");
        slop = new Integer(subspecs[0]).intValue();
        switch (subspecs.length) {
          case 1:
            break;
          case 2:
            boost = new Float(subspecs[1]).floatValue();
            break;
          default:
            throw new Exception("A physical field spec can have only one boost value(^): "+spec);
        }
        break;
      default:
        throw new Exception("A physical field spec can have only one slop value(~): "+spec);
    }
    if (field.indexOf('^') >= 0)
      throw new Exception("A physical field spec must be in the form of <field>[~<slop>][^<boost>]"+spec);
    if (field.indexOf('~') >= 0)
      throw new Exception("A physical field spec must be in the form of <field>[~<slop>][^<boost>]"+spec);
    return new FieldSpec(field, slop, boost);
  }

  /**
   * convert a query into a logical one.
   */
  public Query applyTo(Query query) {
    // can't do double-dispatching.
    // instead, do a series of type discriminations
    if (query instanceof TermQuery)
      return applyTo((TermQuery)query);
    if (query instanceof PhraseQuery)
      return applyTo((PhraseQuery)query);
    if (query instanceof BooleanQuery)
      return applyTo((BooleanQuery)query);
    if (query instanceof FuzzyQuery)
      return applyTo((FuzzyQuery)query);
    if (query instanceof MultiPhraseQuery)
      return applyTo((MultiPhraseQuery)query);
    if (query instanceof PrefixQuery)
      return applyTo((PrefixQuery)query);
    if (query instanceof TermRangeQuery)
      return applyTo((TermRangeQuery)query);
    if (query instanceof WildcardQuery)
      return applyTo((WildcardQuery)query);
    return query;
  }

  /**
   * convert a term query into a logical query.
   * The boost value is multiplied.
   */
  public TermQuery applyTo(TermQuery query) {
    TermQuery new_query = new TermQuery(
      new Term(field, query.getTerm().text()));
    new_query.setBoost(query.getBoost() * this.boost);
    return new_query;
  }

  /**
   * convert a phrase query into a logical query.
   * The boost value is multiplied.
   * The slop value is overriden.
   */
  public PhraseQuery applyTo(PhraseQuery query) {
    PhraseQuery new_query = new PhraseQuery();
    new_query.setBoost(query.getBoost() * this.boost);
    new_query.setSlop(Math.max(query.getSlop(), slop));
    Term terms[] = query.getTerms();
    for (int i = 0; i < terms.length; i++)
      new_query.add(new Term(field, terms[i].text()));
    return new_query;
  }

  /**
   * convert a boolean query into a logical query.
   * The boost value is multiplied.
   */
  public BooleanQuery applyTo(BooleanQuery query) {
    BooleanQuery new_query = new BooleanQuery(query.isCoordDisabled());
    new_query.setMaxClauseCount(query.getMaxClauseCount());
    new_query.setMinimumNumberShouldMatch(query.getMinimumNumberShouldMatch());
    new_query.setBoost(query.getBoost() * this.boost);

    for (Iterator i = query.clauses().iterator(); i.hasNext();) {
      BooleanClause clause = (BooleanClause)i.next();
      new_query.add(applyTo(clause.getQuery()), clause.getOccur());
    }
    return new_query;
  }

  /**
   * convert a fuzzy query into a logical query.
   * The boost value is multiplied.
   */
  public FuzzyQuery applyTo(FuzzyQuery query) {
    FuzzyQuery new_query = new FuzzyQuery(
      new Term(field, query.getTerm().text()),
      query.getMinSimilarity(),
      query.getPrefixLength());
    new_query.setBoost(query.getBoost() * this.boost);
    return new_query;
  }

  /**
   * convert a multi phrase query into a logical query.
   * The boost value is multiplied.
   * The slop value is overriden.
   */
  public MultiPhraseQuery applyTo(MultiPhraseQuery query) {
    MultiPhraseQuery new_query = new MultiPhraseQuery();
    new_query.setBoost(query.getBoost() * this.boost);
    new_query.setSlop(Math.max(query.getSlop(), slop));
    for (Iterator it=query.getTermArrays().iterator(); it.hasNext();) {
      Term[] terms = (Term[])it.next();
      Term[] new_terms = new Term[terms.length];
      for (int i = 0; i < terms.length; i++)
        new_terms[i] = new Term(field, terms[i].text());
      new_query.add(new_terms);
    }
    return new_query;
  }

  /**
   * convert a prefix query into a logical query.
   * The boost value is multiplied.
   */
  public PrefixQuery applyTo(PrefixQuery query) {
    PrefixQuery new_query = new PrefixQuery(
      new Term(field, query.getPrefix().text()));
    new_query.setBoost(query.getBoost() * this.boost);
    return new_query;
  }

  /**
   * convert a term range query into a logical query.
   * The boost value is multiplied.
   */
  public TermRangeQuery applyTo(TermRangeQuery query) {
    TermRangeQuery new_query = new TermRangeQuery(
      field,
      query.getLowerTerm(),
      query.getUpperTerm(),
      query.includesLower(),
      query.includesUpper(),
      query.getCollator());
    new_query.setBoost(query.getBoost() * this.boost);
    return new_query;
  }

  /** string representation */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.field);
    if (this.slop != DEFAULT_SLOP)
      buffer.append("~"+this.slop);
    if (this.boost != DEFAULT_BOOST)
      buffer.append("^"+this.boost);
    return buffer.toString();
  }

  /**
   * convert a wildcard query into a logical query.
   * The boost value is multiplied.
   */
  public WildcardQuery applyTo(WildcardQuery query) {
    WildcardQuery new_query = new WildcardQuery(
      new Term(field, query.getTerm().text()));
    new_query.setBoost(query.getBoost() * this.boost);
    return new_query;
  }

  public static void main(String[] args) throws Exception {
    FieldSpec spec = FieldSpec.parse(args[0]);
    System.out.println("field:"+spec.field+" slop:"+spec.slop+" boost:"+spec.boost);
  }
}
