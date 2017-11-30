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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Find all span matches and sub-span matches of a query.
 *
 * Given three documents
 * 0: "t1 t2 t3 t4 t2 t3",
 * 1: "t1 t2 t3 t5 t2 t4",
 * 2: "t1 t3 t4 t2 t3 t4"
 *
 * a AllClonesQuery(field, {"t1", "t2", "t3", "t4"}, 2) returns
"mapped spans\n"+
"	Position 0 matches\n"+  //for terms starting with t1,
                                 //ie:  t1 t2 t3 t4, t1 t2 t3, t1 t2

"		@1:(0-3)[0-8]\n"+  //match 1: <t1 t2 t3> t5 t2 t4
"		@0:(0-4)[0-11]\n"+ //match 0: <t1 t2 t3 t4> t2 t3
"	Position 1 matches\n"+
	                      //for terms starting with t2, ie: t2 t3 t4; t2 t3
"		@0:(4-6)[12-17]\n"+  // match 0:t1 t2 t3 t4 <t2 t3>
"		@1:(1-3)[3-8]\n"+    // match 1: t1 <t2 t3> t5 t2 t4
"		@0:(1-4)[3-11]\n"+   // match 0: t1 <t2 t3 t3> t2 t3
"		@2:(3-6)[9-17]\n" +  // match 2: t1 t3 t4 <t2 t3 t4>
"	Position 2 matches\n" +  // for terms starting with t3, ie: t3 t4
"		@0:(2-4)[6-11]\n" +  // match 0: t1 t2 <t3 t4> t2 t3
"		@2:(1-3)[3-8]\n" +   // match 2: t1 <t3 t4> t2 t3 t4
"		@2:(4-6)[12-17]"     // match 2: t1 t3 t4 t2 <t3 t4>
 *
 *
 * TODO: instead of returning the result as a Map, we
 *       should use a Collector that collects the results,
 *       with a Collector, we can add results in any way
 *       a user wants.
 *
 *       CloneMatchDocCollector needs to be rewritten to be used
 *       here.
 * @author ye
 * @$Id$
 */
public class AllClonesQuery {

	public static Logger log = LoggerFactory.getLogger(AllClonesQuery.class);

	private final List<SpanQuery> clauses;
	private int _minSize;

	//results to be returned
	// starting postion of the tokens -> bufferedSpans
	private Map<Integer, BufferedSpans> allSpans;

	public AllClonesQuery(String field, String[] tokens, int minSize) {
		_minSize = Math.max(1, minSize);
		clauses = new ArrayList<SpanQuery>(tokens.length);
		for (int i=0; i < tokens.length; i++) {
			clauses.add(new SpanTermQuery(new Term(field, tokens[i])));
		}
	}

	public Map<Integer, BufferedSpans> getAllClones(IndexReader reader) throws IOException {
		allSpans = new LinkedHashMap();
		doSearch(reader);
		return allSpans;
	}

	private void doSearch(IndexReader reader) throws IOException {
		int upper = clauses.size() - _minSize;
		for (int i=0; i <= upper; i++) {
			//TODO: for long files, performance can be improved if we do away with this
			//as long as we are careful, we can live with the fact that all clauses refer
			//to the original one
			SpanQuery[] currentQList = clauses.subList(i, clauses.size()).toArray(new SpanQuery[clauses.size() - i]);
			GrowingCloneQuery gq = new GrowingCloneQuery(currentQList, _minSize);
			BufferedSpans matches = gq.getGrowingSpans(reader);
			collectResults(i, matches);
		}
	}

	private void collectResults(int startPosition, BufferedSpans spans) throws IOException {
		allSpans.put(startPosition, spans);
	}

	/**
	 * showing results so far
	 */
	public static String allSpansToString(Map<Integer, BufferedSpans> mappedSpans) {
		StringBuilder sb = new StringBuilder("\nmapped spans");
		for (Map.Entry<Integer, BufferedSpans> elem: mappedSpans.entrySet()) {
			sb.append("\n\tPosition " + elem.getKey() + " matches");
			BufferedSpans s = elem.getValue();
			s.reset();
			try {
				while (s.next()) {
					sb.append("\n\t\t" + s.getSpanInfo());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
