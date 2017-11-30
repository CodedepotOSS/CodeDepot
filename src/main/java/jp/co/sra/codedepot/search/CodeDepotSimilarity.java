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

import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.DefaultSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * シグネチャー検索のために導入した自前のSimilarityクラス。
 * <p>
 * <strong>Warning:</strong> if solr starts to use its own SolrSimilarity,
 * we may need to change to inherit from SolrSimilarity to
 * take advantage of solr's extension. But now, SolrSimiarility
 * is package private, we cannot extend it. SolrSimilarity
 * for the moment does nothing.
 *
 * <p><strong>制限</strong></p>
 * <p>
 * 	シグネチャーフィールドのlengthNormが、タイプの数（フィールド長さ）の逆数を
 * 採用するように変更。(一般フィールドはいままでどおり、DefaultSimilairtyの
 * 1/sqrt(numTerms))
 * しかし、LuceneがlengthNormを一バイトに転換するため、精度が0.125しか
 * ない。タイプの数が7個以上になると、個数が増えると、lengthNormが減るとは
 * 	限らない。たとえば
 *  <pre>
 * 		lengthNorm(7) == lengthNorm(8)
 *  </pre>
 *  しかし、8, 9, 10, 11の差がでる
 *
 *  lengthNormに関するテストクラスは{@link SignatureLengthNormTest.java}
 *  を参照。いまは通らない{@link SignatureLengthNormTest#testLengthNorm78()}
 *  をコメントアウトしている。
 * @author yunwen
 *
 */
public class CodeDepotSimilarity extends DefaultSimilarity {

	private static final long serialVersionUID = 1714983321133542750L;
	public static final Logger log = LoggerFactory.getLogger(CodeDepotSimilarity.class);

	//TODO: remove this and fold this into computerNorm when everything is done
	@Override
	public float lengthNorm(String fieldName, int numTerms) {
		if (fieldName.equals(Indexer.IN_TYPES) || fieldName.equals(Indexer.OUT_TYPE)) {
			return 1.0f / numTerms;
		} else
			return super.lengthNorm(fieldName, numTerms);
	}

	 /** 抽象クラスのSimilarityでは
	   *  <code>state.getBoost()*lengthNorm(numTerms)</code>
	   * の実装をしている。
	   *
	   * FieldInverState#getLength()
	   *  <code>numTerms</code> is {@link FieldInvertState#getLength()} if {@link
	   *  #setDiscountOverlaps} is false, else it's {@link
	   *  FieldInvertState#getLength()} - {@link
	   *  FieldInvertState#getNumOverlap()}.
	   *
	   *  <p><b>WARNING</b>: This API is new and experimental, and may suddenly
	   *  change.</p>
	   *
	   */
	@Override
	public float computeNorm(String fieldName, FieldInvertState state) {
		if (fieldName.equals(Indexer.IN_TYPES) || fieldName.equals(Indexer.OUT_TYPE)) {
			/* for debugging only
			if (log.isDebugEnabled() && (state.getNumOverlap() != 0 )) {
				//print a message when it matters
				System.out.println("state.getLength()=" + state.getLength());
				System.out.println("state.getNumOverlap()=" + state.getNumOverlap());
			}
			*/
			int numTerms = state.getLength() 	/* Tokenの数、同じpositionにあるTokenも計上される */
						- state.getNumOverlap(); /* 同じpositionに計上されたTokenの数 */
			return (state.getBoost() * lengthNorm(fieldName, numTerms));
		} else {
			return super.computeNorm(fieldName, state);
		}
	}
}
