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
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;

import org.slf4j.*;

/**
 * SignatureQueryに関わる重みを計算し、もつ。Searcherに依存する。
 * このクラスの計算結果は、一個のSignatureQueryだけのランキングには影響が与えない
 * ものの、複数の検索Queryを組み合わせるときに、値が最後のランキングに影響を与える。
 * code:String OR inType:String
 * がcode:intにマッチするドキュメントとinType:intにマッチするドキュメントのどっちが
 * 最初にランキングするのは、影響がある思う。
 *
 * @author yunwen
 * @$Id: SignatureWeight.java 2356 2017-11-10 07:50:30Z fang $
 */
public class SignatureWeight extends Weight {
	private static final long serialVersionUID = 4942225155433737331L;
	private Searcher _searcher;
	private SignatureQuery _query;
	private float _weightValue;

	static final Logger log = LoggerFactory.getLogger(SignatureWeight.class);

	public SignatureWeight(Searcher searcher, SignatureQuery q) {
		log.debug("SignatureWeight {}, {}", searcher ,q);
		_searcher = searcher;
		_query = q;
	}

	@Override
	public Explanation explain(IndexReader reader, int doc) throws IOException {
		System.out.println("explain not implemented yet");
		throw new UnsupportedOperationException();
	}

	@Override
	public Query getQuery() {
		return _query;
	}

	@Override
	public float getValue() {
		//log.debug("getValue");
		return _weightValue;
	}

	/**
	 * sumOfSquaredWights()の結果にさらに影響を与えないときに、このメソッドを呼び出す。
	 * おそらくこれは Query.weight()以外には呼ぶことがないでしょう。
	 * ここで渡されたnormはSimilarity.queryNorm(sumOfSquaredWeights())から計算される。
	 * 現況では、1/sqrt(sumOfSquaredWeights())
	 * <p>
	 * Signature では、normalizeする必要がないので、何もしない方がよい。
	 */
	@Override
	public void normalize(float norm) {
		/*
		log.debug("normalize");
		_weightValue *= norm;
		*/
	}

	@Override
	public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,
			boolean topScorer) throws IOException {
		return new SignatureScorer(_searcher.getSimilarity(), reader, this, _query);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Weight#sumOfSquaredWeights()
	 */

	/**
	 * 検索タームの、IDFにより重みを付ける。TermQueryの計算仕方と同じ
	 * この値は、SignatureQueryだけの検索ではランキングに影響を与えない。
	 * SIGMA( getSimilarity.idfExplain(term, _searcher).getIdf() * term.getBoost() )^2
	 *
	 * IDFの重みをつけないで、タームの数だけを返すなら、長さの計算をSignatureQuery.getLength()を足した
	 * 方がよい。
	 *
	 * TODO check to see if the current weight is appropriate when
	 * it is combined with other queries (should not be too overpowering)
	 */
	@Override
	public float sumOfSquaredWeights() throws IOException {
		float weight = 0.0f;
		for (Iterator<Map.Entry<Term, Integer>> iter = _query.getTermFreqs().entrySet().iterator();
			iter.hasNext(); ) {
			Map.Entry<Term, Integer> termFreqPair = iter.next();
			Term term = termFreqPair.getKey();
			float idf = _searcher.getSimilarity().idfExplain(term, _searcher).getIdf();
			//そのままを使うと、大きすぎるので、ｓｑｒｔをつかう
			//TODO still seems too big, adjust this later.
			idf = (float) Math.sqrt(idf);
			weight += (float) termFreqPair.getValue() * idf; //そのタームのidfを掛け算 * idf(iter.getKey());
		}
		_weightValue = weight * _query.getBoost(); //query にboostの値があれば。
		//System.out.format("SignatureWeight#sumOfSquareWeights() = %f%n", _weightValue);
		return _weightValue;
	}

}
