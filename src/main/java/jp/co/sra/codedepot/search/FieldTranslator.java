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

import jp.co.sra.codedepot.queryParser.ParseException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * Field translator.
 * FieldTranslator translates logical fields into physical ones.
 * @author tomohiro
 */

public class FieldTranslator {

  private HashMap<String,FieldSpec[]> aliases;

  /**
   * Construct a default translator
   */
  public FieldTranslator() {
    aliases = new HashMap<String,FieldSpec[]>();
  }

  /**
   * Answers if the translator has an alias rule for the given logical field.
   */
  public boolean containsLogicalField(String field) {
    return aliases.containsKey(field);
  }

  /**
   * getFieldQuery with logical/physical field translation.
   */
  public Query getFieldQuery(String field, String queryText, CodeDepotQueryParser parser) throws ParseException {
  // translating an atomic query
  FieldSpec[] physical_field_specs;
  BooleanQuery physical_query;
  if (!aliases.containsKey(field))
    // no matching logical field. return the query as it is.
    return parser._getFieldQuery(field, queryText);
  physical_field_specs = aliases.get(field);
  if (physical_field_specs.length == 1) {
    FieldSpec spec=physical_field_specs[0];
    // the query is translated into a single logical query. return it.
    return spec.applyTo(parser._getFieldQuery(spec.field, queryText));
  }
  // creates a compound query and return it
  physical_query = new BooleanQuery();
  for (FieldSpec spec : physical_field_specs) {
    Query translated_query = spec.applyTo(parser._getFieldQuery(spec.field, queryText));
    if (translated_query != null)
      physical_query.add(translated_query, Occur.SHOULD);
  }
  return physical_query;
}

  /**
   * getFieldQuery with logical/physical field translation.
   */
  public Query getFieldQuery(String field, String queryText, int slop, CodeDepotQueryParser parser) throws ParseException {
  // translating an atomic query
  FieldSpec[] physical_field_specs;
  BooleanQuery physical_query;
  if (!aliases.containsKey(field))
    // no matching logical field. return the query as it is.
    return parser._getFieldQuery(field, queryText, slop);
  physical_field_specs = aliases.get(field);
  if (physical_field_specs.length == 1) {
    FieldSpec spec=physical_field_specs[0];
    // the query is translated into a single logical query. return it.
    return spec.applyTo(parser._getFieldQuery(spec.field, queryText, slop));
  }
  // creates a compound query and return it
  physical_query = new BooleanQuery();
  for (FieldSpec spec : physical_field_specs) {
    Query translated_query = spec.applyTo(parser._getFieldQuery(spec.field, queryText, slop));
    if (translated_query != null)
      physical_query.add(translated_query, Occur.SHOULD);
  }
  return physical_query;
}

  /**
   * getFuzzyQuery with logical/physical field translation.
   */
  public Query getFuzzyQuery(String field, String queryText, float fms, CodeDepotQueryParser parser) throws ParseException {
    // translating an atomic query
    FieldSpec[] physical_field_specs;
    BooleanQuery physical_query;
    if (!aliases.containsKey(field))
      // no matching logical field. return the query as it is.
      return parser._getFuzzyQuery(field, queryText, fms);
    physical_field_specs = aliases.get(field);
    if (physical_field_specs.length == 1) {
      FieldSpec spec=physical_field_specs[0];
      // the query is translated into a single logical query. return it.
      return spec.applyTo(parser._getFuzzyQuery(spec.field, queryText, fms));
    }
    // creates a compound query and return it
    physical_query = new BooleanQuery();
    for (FieldSpec spec : physical_field_specs)
      physical_query.add(
        spec.applyTo(parser._getFuzzyQuery(spec.field, queryText, fms)),
        Occur.SHOULD);
    return physical_query;
  }

  /**
   * getPrefixQuery with logical/physical field translation.
   */
  public Query getPrefixQuery(String field, String termStr, CodeDepotQueryParser parser)
  throws ParseException {
    // translating an atomic query
    FieldSpec[] physical_field_specs;
    BooleanQuery physical_query;
    if (!aliases.containsKey(field))
      // no matching logical field. return the query as it is.
      return parser._getPrefixQuery(field, termStr);
    physical_field_specs = aliases.get(field);
    if (physical_field_specs.length == 1) {
      FieldSpec spec=physical_field_specs[0];
      // the query is translated into a single logical query. return it.
      return spec.applyTo(parser._getPrefixQuery(spec.field, termStr));
    }
    // creates a compound query and return it
    physical_query = new BooleanQuery();
    for (FieldSpec spec : physical_field_specs)
      physical_query.add(
        spec.applyTo(parser._getPrefixQuery(spec.field, termStr)),
        Occur.SHOULD);
    return physical_query;
  }

  /**
   * getRangeQuery with logical/physical field translation.
   */
  public Query getRangeQuery(String field, String part1, String part2, boolean inclusive, CodeDepotQueryParser parser)
  throws ParseException {
    // translating an atomic query
    FieldSpec[] physical_field_specs;
    BooleanQuery physical_query;
    if (!aliases.containsKey(field))
      // no matching logical field. return the query as it is.
      return parser._getRangeQuery(field, part1, part2, inclusive);
    physical_field_specs = aliases.get(field);
    if (physical_field_specs.length == 1) {
      FieldSpec spec=physical_field_specs[0];
      // the query is translated into a single logical query. return it.
      return spec.applyTo(parser._getRangeQuery(spec.field, part1, part2, inclusive));
    }
    // creates a compound query and return it
    physical_query = new BooleanQuery();
    for (FieldSpec spec : physical_field_specs)
      physical_query.add(
        spec.applyTo(parser._getRangeQuery(spec.field, part1, part2, inclusive)),
        Occur.SHOULD);
    return physical_query;
  }

  /**
   * getWildcardQuery with logical/physical field translation.
   */
  public Query getWildcardQuery(String field, String termStr, CodeDepotQueryParser parser)
  throws ParseException {
    // translating an atomic query
    FieldSpec[] physical_field_specs;
    BooleanQuery physical_query;
    if (!aliases.containsKey(field))
      // no matching logical field. return the query as it is.
      return parser._getWildcardQuery(field, termStr);
    physical_field_specs = aliases.get(field);
    if (physical_field_specs.length == 1) {
      FieldSpec spec=physical_field_specs[0];
      // the query is translated into a single logical query. return it.
      return spec.applyTo(parser._getWildcardQuery(spec.field, termStr));
    }
    // creates a compound query and return it
    physical_query = new BooleanQuery();
    for (FieldSpec spec : physical_field_specs)
      physical_query.add(
        spec.applyTo(parser._getWildcardQuery(spec.field, termStr)),
        Occur.SHOULD);
    return physical_query;
  }

  /** clear aliases mappings */
  public void clearAliases() {
    this.aliases.clear();
  }

  /** add a aliases mapping */
  public void defineAliases(String name, FieldSpec [] field_specs) {
    this.aliases.put(name, field_specs);
  }

  /** parse and set aliases mappings from a string */
  public void setAliases(String specString) throws Exception {
    this.clearAliases();
    if (specString.length() == 0) return;
    for (String fieldString : specString.split(" ")) {
      String[] parts = fieldString.split(":");
      if (parts.length != 2)
        throw new Exception("Invalid syntax in the alias definition: "+specString);
      String logical_field = parts[0];
      parts = parts[1].split(",");
      FieldSpec[] specs=new FieldSpec[parts.length];
      for (int i = 0; i < parts.length; i++)
        specs[i] = FieldSpec.parse(parts[i]);
      this.defineAliases(logical_field, specs);
    }
  }

  /** create a string presentation */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (Map.Entry<String, FieldSpec[]> entry : this.aliases.entrySet()) {
      buffer.append(entry.getKey());
      buffer.append(":");
      for (FieldSpec spec : entry.getValue()) {
        buffer.append(spec.toString());
        buffer.append(",");
      }
      if (entry.getValue().length >= 1)
        buffer.replace(buffer.length() - 1, buffer.length(), " ");
    }
    return buffer.toString();
  }

  /** command line testing */
  public static void main(String[] args) throws Exception {
    FieldTranslator ft = new FieldTranslator();
    if (args.length >= 1)
      ft.setAliases(args[0]);
    System.out.println(ft);
  }
}
