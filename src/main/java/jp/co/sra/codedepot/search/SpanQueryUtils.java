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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import jp.co.sra.codedepot.index.java.JavaProgramAnalyzer;
import jp.co.sra.codedepot.solr.java.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;

public class SpanQueryUtils {
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpanQueryUtils.class);

	/**
	 * Create normal SpanNearQuery with the specified slop.
	 * This SpanNearQuery requires all clone tokens in the qText exist in the search result.
	 * @param qText
	 * @param analyzer
	 * @param fieldName
	 * @param slop
	 * @return
	 * @throws IOException
	 */
	public static SpanNearQuery createSpanNearQuery(String qText, Analyzer analyzer, String fieldName, int dslop) throws IOException {
		TokenStream tokens = analyzer.tokenStream(fieldName, new StringReader(qText));
		Term term = new Term(fieldName);
		List<SpanTermQuery> clauses = new ArrayList<SpanTermQuery>();
		Token t = new Token();
		t.clear();
		for (t = tokens.next(t); t != null; t = tokens.next(t)) {
			clauses.add(new SpanTermQuery(term.createTerm(t.term())));
		}
		SpanQuery[] clauses2 = new SpanQuery[clauses.size()];
		SpanNearQuery q = new SpanNearQuery(clauses.toArray(clauses2), dslop, true);
		return q;
	}

	/**
	 * Return an ORed SpanOrQuery that OR's the
	 * 	 (1) original span query, with slop set to dslop
	 *   (2) all new sub queries that are created by removing <code>qslop</code>
	 *       cloneTokens
	 * For v2.0, 0<= dslop == qslop <=2
	 * <pre>
	 * Example:
	 *    a=b;a++;b--;a+=1
	 *    qslop: 2,
	 * Results
	 *    SpanNearQuery(a=b;a++;b--;a+=1, dslop) OR
	 *    SpanNearQuery(a=b;a++;        , dslop) OR
	 *    SpanNearQuery(a=b;    b--;    , dslop) OR
	 *    SpanNearQuery(    a++;b--;    , dslop) OR
	 *    SpanNearQuery(a=b;        a+=1, dslop) OR
	 *    SpanNearQuery(    a++;    a+=1, dslop) OR
	 *    SpanNearQuery(        b--;a+=1, dslop) OR
	 * <pre>
	 * @param qText
	 * @param analyzer
	 * @param fieldName
	 * @param slop
	 * @return
	 * @throws IOException
	 */
	public static SpanQuery createAllowMismatchSpanQuery(String qText, Analyzer analyzer, String fieldName, int dslop, int qslop) throws IOException {
		if (qslop == 0) {
			return createSpanNearQuery(qText, analyzer, fieldName, dslop);
		} else if (qslop == 1) {
			return createLessOneSpanQuery(qText, analyzer, fieldName, dslop);
		} else if (qslop == 2) {
			return createLessTwoSpanQuery(qText, analyzer, fieldName, dslop);
		}
		logger.error("qslop greater than 1 is not supported yet, override it with 1");
		return createLessOneSpanQuery(qText, analyzer, fieldName, dslop);
	}
	/**
	 * Return an ORed SpanOrQuery that OR's the original span query and all span queries
	 * with one clause (statement) removed.
	 * TODO apparently there is a lot of room for improvement.
	 *      If used finally, combine the two methods that it currently calls into one.
	 * @param qText
	 * @param analyzer
	 * @param fieldName
	 * @param slop
	 * @return
	 * @throws IOException
	 */
	public static SpanQuery createLessOneSpanQuery(String qText, Analyzer analyzer, String fieldName, int dslop) throws IOException {
		long stime = System.currentTimeMillis();
		//TODO this should be made more efficient
		SpanNearQuery initQ = createSpanNearQuery(qText, analyzer, fieldName, dslop);
		initQ.setBoost(2.0f); //2.0f increase 1.06 -> 1.47; no difference between 2.0 & 5.0
		SpanQuery[] lessOnes = getLessOneClauses(initQ);
		if (lessOnes == null) return initQ;
		SpanQuery[] all = new SpanQuery[lessOnes.length + 1];
		all[0] = initQ;
		System.arraycopy(lessOnes, 0, all, 1, lessOnes.length);

		logger.debug("createSlopSpanQuery time: {}", System.currentTimeMillis() - stime);
		return new SpanOrQuery(all);
	}

	public static SpanQuery createLessTwoSpanQuery(String qText, Analyzer analyzer, String fieldName, int dslop) throws IOException {
		long stime = System.currentTimeMillis();
		//TODO this should be made more efficient
		SpanNearQuery initQ = createSpanNearQuery(qText, analyzer, fieldName, dslop);
		initQ.setBoost(2.0f); //2.0f increase 1.06 -> 1.47; no difference between 2.0 & 5.0
		SpanQuery[] lessTwos = getLessTwoClauses(initQ);
		if (lessTwos == null) return initQ;
		SpanQuery[] all = new SpanQuery[lessTwos.length + 1];
		all[0] = initQ; //this may not be needed if score is high enough for exact match
		System.arraycopy(lessTwos, 0, all, 1, lessTwos.length);

		logger.debug("createSlopSpanQuery time: {}", System.currentTimeMillis() - stime);
		return new SpanOrQuery(all);
	}

	/**
	 * return a list of SpanQuery with exactly one clause removed in the
	 * original SpanNearQuery
	 * @param origQuery
	 * @return
	 */
	public static SpanQuery[] getLessOneClauses(SpanNearQuery origQuery) {
		SpanQuery[] origClauses = origQuery.getClauses();
		int size = origClauses.length;
		if (size <= 1) return null;
		SpanQuery[] newOrClauses = new SpanQuery[size];
		for (int i=0; i < size; i++) {
			//create a new SpanQuery[] with origQuery[i] removed;
			SpanQuery[] lessOneClauses = new SpanQuery[size-1];
			if (i > 0) {
				System.arraycopy(origClauses, 0, lessOneClauses, 0, i);
			}
			if (i < (size - 1)) {
				System.arraycopy(origClauses, i+1, lessOneClauses, i, size-i-1);
			}
			SpanNearQuery lessOneQuery = new SpanNearQuery(lessOneClauses, origQuery.getSlop(), true);
			newOrClauses[i] = lessOneQuery;
		}
		return newOrClauses;
	}

	public static SpanQuery[] getLessTwoClauses(SpanNearQuery origQuery) {
		SpanQuery[] origClauses = origQuery.getClauses();
		int size = origClauses.length;
		if (size <= 1) return null;
		if (size == 2) return getLessOneClauses(origQuery);
		// size > 2
		SpanQuery[] newOrClauses = new SpanQuery[size * (size-1) / 2]; //C(size,2)
		int idx = size * (size-1) / 2 - 1;
		for (int i=0; i < size - 1; i++) {
			for (int j=i+1; j < size; j++) {
				// remove origCluases[j]
				SpanQuery[] lessTwoClauses = new SpanQuery[size-2];
				//copy 0:i-1 inclusive both
				if (i > 0) {
					System.arraycopy(origClauses, 0, lessTwoClauses, 0, (i-1) - 0 + 1);
				}
				//copy i+1:j-1 inclusive both
				if (j-i-1 > 0) {
					System.arraycopy(origClauses, i+1, lessTwoClauses, i, (j-1) - (i+1) + 1);
				}
				//copy j+1:size-1 inclusive both
				if (j < (size - 1)) {
					System.arraycopy(origClauses, j+1, lessTwoClauses, j-1, (size-1) - (j+1) + 1);
				}
				SpanNearQuery lessTwoQuery = new SpanNearQuery(lessTwoClauses, origQuery.getSlop(), true);
				//System.out.println(lessTwoQuery);
				newOrClauses[idx--] = lessTwoQuery;
			}
		}
		assert idx == -1;
		return newOrClauses;
	}

	public static SpanQuery getLessOneORedSpanQuery(SpanNearQuery origQuery) {
		return new SpanOrQuery(getLessOneClauses(origQuery));
	}

	public static void printDocListAndSet(DocListAndSet dls) {
		System.out.println(docListAndSetToString(dls));
	}

	public static String docListAndSetToString(DocListAndSet dls) {
		StringBuilder sb = new StringBuilder();
		//DocSet s = dls.docSet;
		DocList l = dls.docList;
		sb.append("\n::DocListAndSet(");
		sb.append(" lstSize=" + l.size() + " matches=" + l.matches());
		sb.append(")\n");
		for (DocIterator diter = l.iterator(); diter.hasNext(); ) {
			sb.append("\tdoc=" + diter.next());
			if (l.hasScores()) sb.append("\tscore=" + diter.score() + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Covert all spans to a string. Useful for printout and test.
	 * Spans and its subclass has toString methods but they only print
	 * the current span, not all spans that are linked through next.
	 * The original Spans classes provided by Lucene cannot print
	 * all spans because each span in generated on the fly, with a
	 * pull style.
	 * @param spans
	 * @return
	 * @throws IOException
	 */
	public static String spansToString(Spans spans) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("--All spans--\n");
		while (spans.next()) {
			sb.append(spans);
			sb.append(" ");
		}
		return sb.toString();
	}

	@org.junit.Test
	public void test() throws IOException {
		String result;
		result = createAllowMismatchSpanQuery("a=b+1;", new JavaProgramAnalyzer(), Indexer.CLONETKN, 2, 2).toString();
		assertTrue(result.equals("spanNear([clonetkn:$=$+$;], 2, true)^2.0"));
		result = createAllowMismatchSpanQuery("a=b+1;a=b-2;", new JavaProgramAnalyzer(), Indexer.CLONETKN, 2, 2).toString();
		assertTrue(result.equals("spanOr([spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;], 2, true)^2.0, " +
									"spanNear([clonetkn:$=$-$;], 2, true), " +
									"spanNear([clonetkn:$=$+$;], 2, true)])"));
		result = createAllowMismatchSpanQuery("a=b+1;a=b-2;a=b*3;", new JavaProgramAnalyzer(), Indexer.CLONETKN, 2, 2).toString();
		System.out.println(result);
		assertTrue(result.equals("spanOr([spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;, clonetkn:$=$*$;], 2, true)^2.0, " +
							         "spanNear([clonetkn:$=$+$;], 2, true), " +
							         "spanNear([clonetkn:$=$-$;], 2, true), " +
							         "spanNear([clonetkn:$=$*$;], 2, true)])"));
		result = createAllowMismatchSpanQuery("a=b;a++;b--;a+=1;", new JavaProgramAnalyzer(), Indexer.CLONETKN, 2, 2).toString();
		assertTrue(result.equals("spanOr([spanNear([clonetkn:$=$;, clonetkn:$++;, clonetkn:$--;, clonetkn:$+=$;], 2, true)^2.0, " +
				"spanNear([clonetkn:$=$;, clonetkn:$++;], 2, true), " +
				"spanNear([clonetkn:$=$;, clonetkn:$--;], 2, true), " +
				"spanNear([clonetkn:$=$;, clonetkn:$+=$;], 2, true), " +
				"spanNear([clonetkn:$++;, clonetkn:$--;], 2, true), " +
				"spanNear([clonetkn:$++;, clonetkn:$+=$;], 2, true), " +
				"spanNear([clonetkn:$--;, clonetkn:$+=$;], 2, true)])"));
		result = createAllowMismatchSpanQuery("a=b+1;a=b-2;a=b*3;a=b/3;a=b^3;", new JavaProgramAnalyzer(), Indexer.CLONETKN, 2, 2).toString();
		assertTrue(result.equals("spanOr([spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;, clonetkn:$=$*$;, clonetkn:$=$/$;, clonetkn:$=$^$;], 2, true)^2.0, " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;, clonetkn:$=$*$;], 2, true), " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;, clonetkn:$=$/$;], 2, true), " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$-$;, clonetkn:$=$^$;], 2, true), " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$*$;, clonetkn:$=$/$;], 2, true), " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$*$;, clonetkn:$=$^$;], 2, true), " +
				"spanNear([clonetkn:$=$+$;, clonetkn:$=$/$;, clonetkn:$=$^$;], 2, true), " +
				"spanNear([clonetkn:$=$-$;, clonetkn:$=$*$;, clonetkn:$=$/$;], 2, true), " +
				"spanNear([clonetkn:$=$-$;, clonetkn:$=$*$;, clonetkn:$=$^$;], 2, true), " +
				"spanNear([clonetkn:$=$-$;, clonetkn:$=$/$;, clonetkn:$=$^$;], 2, true), " +
				"spanNear([clonetkn:$=$*$;, clonetkn:$=$/$;, clonetkn:$=$^$;], 2, true)])"));
	}
}
