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
 * Part of the code of this program was copied from org.apache.solr.component.highlight
 * See license notice below.
 */

/**
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


import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.ConstantScorePrefixQuery;
import org.apache.solr.search.WildcardFilter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.co.sra.codedepot.solr.Indexer;


/**
 * 検索結果リスト一覧表に表示するコード断片を返す。
 * その断片にハイライトされる部分（検索ターム）も決め、ハイライトタグ
 * を付ける。付けたタグ対は、TAG_STARTとTAG_ENDに定義されている。
 *
 * コード以外のフィールドに指定した検索タームはハイライトされない。
 *
 * @version $Id: CodeDepotHighlightComponent.java 2342 2017-11-09 05:36:32Z fang $
 */
public class CodeDepotHighlightComponent extends SearchComponent
{

	static Logger _logger = Logger.getLogger(CodeDepotHighlightComponent.class.getName());
	static Level _loglevel = Level.INFO;

	public static final String COMPONENT_NAME = "codedepothighlight";
	// ハイライト用検索ターム
	private Map<String, Set<String>> termMap;
	// ハイライト用オフセット
	private List<Integer> hlOffset;
	//改行コード
	private String crlf = "\n";
	//ハイライト用検索ターム取得時に無視するフィールド
	private static final List<String> ignoreList = Arrays.asList(
			Indexer.LICENSE, Indexer.PRJ, Indexer.PKG, Indexer.LOCATION, Indexer.LOCATIONTXT,
			Indexer.LANG, Indexer.UNIT, Indexer.ID, Indexer.BEGIN);
	// ハイライト用タグ開始文字列
	private static final String tagStart = "<em class=\"hlw\">";
	// ハイライト用タグ終了文字列
	private static final String tagEnd = "</em>";
	// ハイライト用タグの長さ合計（重み）
	private static final int weight = tagStart.length() + tagEnd.length();
	// ハイライト用検索タームをresponseに返す時のキー名
	private static final String hlTermsKey = "hlTerms";
	// ハイライト用オフセット返す時のキー名
	private static final String hlOffsetKey = "hlOffset";
	// ハイライト用正規表現
	private String termRegExp = "";
	private Pattern _termRegExpPattern;
	// ハイライト情報を何行返すか
	private static final int RECODE = 3;

	@Override
	public void prepare(ResponseBuilder rb) throws IOException
	{
		SolrHighlighter highlighter = rb.req.getCore().getHighlighter();
		rb.doHighlights = highlighter.isHighlightingEnabled(rb.req.getParams());

		termMap = new HashMap<String, Set<String>>();
		hlOffset = new ArrayList<Integer>();

		termRegExp = "";

		try {
// Deleted by wubo on 2010/09/01 for V2.1対応 Start
		//	crlf = System.getProperty("line.separator");
// Deleted by wubo on 2010/09/01 for V2.1対応 End
		} catch (Exception e){
		}

	}


	@Override
	public void process(ResponseBuilder rb) throws IOException {

		SolrQueryRequest req = rb.req;

		if (rb.doHighlights) {

			// ハイライト情報
			NamedList myGetSumData;
			// デフォルトのハイライト情報を利用しているか
			boolean useDefaultHighlightComponent;

			// デフォルトのハイライトでセットされた結果を取得
			myGetSumData = (NamedList)rb.rsp.getValues().get("highlighting");
			if(myGetSumData == null){
				myGetSumData = new SimpleOrderedMap();
				useDefaultHighlightComponent = false;
			} else {
				useDefaultHighlightComponent = true;
			}

			SolrHighlighter highlighter = req.getCore().getHighlighter();

			// ハイライトコンポーネントの結果
			NamedList highlightInfo;

			// 検索にヒットしたドキュメントを保持するクラス
			DocList docs = rb.getResults().docList;

			Query highlightQuery = rb.getHighlightQuery();
			if(highlightQuery==null) {
				if (rb.getQparser() != null) {
					try {
						highlightQuery = rb.getQparser().getHighlightQuery();
						rb.setHighlightQuery( highlightQuery );
					} catch (Exception e) {
						throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
					}
				} else {
					highlightQuery = rb.getQuery();
					rb.setHighlightQuery( highlightQuery );
				}
			}

			// No highlighting if there is no query -- consider q.alt="*:*
			if( highlightQuery != null ) {
				Query query = highlightQuery.rewrite(req.getSearcher().getReader());

				// 検索用検索ターム取得用
				Query q = rb.getQuery();
				doQuery(q);

				StringBuffer termRegExpBuf = new StringBuffer();
				// ハイライト用タームの取得
				String[] termArray = getHlTerms();

				// 正規表現の作成
				for(int s=0;s<termArray.length;s++){
					String term = (String)termArray[s];
					// "?"を"."に、"*"を".*?"に変更 --> "." を単語構成文字(\w)に変更。
					term = term.replace("?", "\\S").replace("*", "\\w*?").replace(" ", "\\W*");
					termRegExpBuf.append(term).append("|");
				}
				// ハイライト正規表現をセット
				termRegExp = chop(termRegExpBuf.toString(), "\\|");

				try {
					_termRegExpPattern = Pattern.compile(termRegExp, Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					try {
						// query string contains regular expression offending structure, like
						// operator\[\] or System.out.input\(
						// Chop the characters after the offending ones
						if (e.getIndex() > 0 ) {
							int chopped = e.getIndex() - 1;
							String choppedStr = e.getPattern().substring(0, chopped);
							_termRegExpPattern = Pattern.compile(choppedStr, Pattern.CASE_INSENSITIVE);
						} else {
							_termRegExpPattern = null;
						}
					} catch (Exception e2) {
						// failed the second time, we are done with highlighting
						_logger.info("highlight turned off because user queries is illegal");
						_termRegExpPattern = null;
					}
				}


				// defaulstHighlightFielsの取得
				SolrParams params = req.getParams();
				String[] defaultFields;  //TODO: get from builder by default?

				if (rb.getQparser() != null) {
					defaultFields = rb.getQparser().getDefaultHighlightFields();
				} else {
					defaultFields = params.getParams(CommonParams.DF);
				}

				SolrIndexSearcher searcher = req.getSearcher();
				IndexSchema schema = searcher.getSchema();

	        		String fmt = "html";
				String[] wt = params.getParams(CommonParams.WT);
                                if (wt != null && wt.length > 0 && wt[0].equalsIgnoreCase("csv")) {
					fmt = "csv";
				}

				// ハイライトするフィールドを取得
				String[] fieldNames = highlighter.getHighlightFields(query, req, defaultFields);

				Document[] readDocs = new Document[docs.size()];
				{
					// pre-fetch documents using the Searcher's doc cache
					Set<String> fset = new HashSet<String>();
					for(String f : fieldNames) { fset.add(f); }
					// fetch unique key if one exists.
					SchemaField keyField = schema.getUniqueKeyField();
					if(null != keyField)
						fset.add(keyField.getName());
					searcher.readDocs(readDocs, docs, fset);
				}

				// 検索結果のドキュメントの数でループ
				for (int z = 0; z < docs.size(); z++) {
					Document doc = readDocs[z];
					// デフォルトのハイライトコンポーネントの結果を取得
					if(useDefaultHighlightComponent){
						highlightInfo = (NamedList)myGetSumData.getVal(z);
					} else {
						highlightInfo = new SimpleOrderedMap();
					}
					// ハイライト情報が無い場合
					if(highlightInfo.size() == 0){
						// ハイライト情報（コード最初部分）の作成
						//NamedList myHighlightInfo = getMyHighlightInfoSrc(readDocs[z], fieldNames);
						// ハイライト情報（検索タームにヒットした部分）の作成
						NamedList myHighlightInfo = getMyHighlightInfoHit(doc, fieldNames, fmt);
						// ハイライト情報をセット
						if(useDefaultHighlightComponent){
							myGetSumData.setVal(z, myHighlightInfo);
						} else {
							String printId = schema.printableUniqueKey(doc);
							myGetSumData.add(printId, myHighlightInfo);
						}
					}
				}

			}

			// デフォルトのハイライト情報が無ければセット。
			if(!useDefaultHighlightComponent){
				rb.rsp.add("highlighting", myGetSumData);
			}
			// 画面ハイライト用正規表現をセット
			rb.rsp.add(hlTermsKey, termRegExp);

			if (hlOffset.size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < hlOffset.size(); i++) {
					if (i > 0) { sb.append(","); }
					sb.append(hlOffset.get(i));
				}
				rb.rsp.add(hlOffsetKey, sb.toString());
			} else {
				rb.rsp.add(hlOffsetKey, "");
			}
		}
	}

	/**
	 * 検索タームにヒットした部分を返す
	 *@param readDoc 表示したいドキュメント
	 *@param fieldNames ハイライトフィールド
	 *@return  ハイライト情報
	 */
	public NamedList getMyHighlightInfoHit(Document readDoc, String[] fieldNames, String fmt){
		NamedList myDocSummaries = new SimpleOrderedMap();

		// ハイライトフィールド分のループ
		for(int z=0;z<fieldNames.length;z++){
			List<String> tmpStr = new ArrayList<String>();
			// ハイライトフィールドに対応するドキュメントを取得。改行コード込の1行で取得。
			String[] docTexts = readDoc.getValues(fieldNames[z]);

                        int startPos = 0;
                        String startStr = readDoc.get("begin");
                        if (startStr != null) {
                            startPos = Integer.parseInt(startStr);
                        }

			// 取得したドキュメントの値の数でループ 必ず１？
			for(int y=0;y<docTexts.length;y++){
				tmpStr = getHighlightString(tmpStr, docTexts[y], startPos, fmt);
				if(tmpStr.size() > 0){
					break;
				}
			}
			// 検索タームでのハイライトしてもヒットしなかった場合は先頭行を出力する
			if(tmpStr.size() == 0){
				tmpStr = getSourceTop(docTexts, tmpStr);
			}
			// 作成したハイライト情報をセット
			myDocSummaries.add(fieldNames[z], tmpStr);
		}
		return myDocSummaries;
	}

	private List<String> getHighlightString(List<String> tmpStr, String docText, int startPos, String fmt){

		//should not happen, sanity check
		if (_termRegExpPattern == null) return tmpStr;

		StringBuffer docTextBuf = new StringBuffer(HTMLConvert.convertIntoHTML(docText));
		Matcher m = _termRegExpPattern.matcher(docTextBuf.toString());

		// ヒットしなかったらおしまい。
		if(!m.find()){
			return tmpStr;
		}

		m.reset();

		int count = 0;

		while(m.find()){
			docTextBuf.insert(m.end()+(count*weight), tagEnd);
			docTextBuf.insert(m.start()+(count*weight), tagStart);
			count++;
		}

		String myDocText = docTextBuf.toString();
		String[] docTexts = myDocText.split(crlf);
		String[] docLines = fmt.equals("html") ? null : docText.split(crlf);

		// 入れ込んだ<em>～</em>部分を抽出用
		Pattern pattern = Pattern.compile(".*"+tagStart+".*"+tagEnd+".*");

		// 行数分ループ
		for(int x=0;x<docTexts.length;x++){
			StringBuffer tmpStrBuf = new StringBuffer();
			Matcher matcher = pattern.matcher(docTexts[x]);
			// 最初にヒットしたところ近辺を表示する。
			if(matcher.matches()){
				if(docTexts.length > RECODE){
					// 前後の行をセットする。
					// 先頭行だった場合
					int start = x-1;
					if(x == 0){
						start = 0;
					} else if (x + RECODE-1 > docTexts.length) {
						start = docTexts.length-RECODE;
					}
					hlOffset.add(start);
					for(int r = 0;r<RECODE;r++){
						if (startPos > 0) {
						        String line_no = String.valueOf(startPos + start + r);
						        if (fmt.equals("html")) {
						        	tmpStrBuf.append("<span class=\"ln_no\">" + line_no + "</span>");
							} else {
								tmpStrBuf.append(line_no + ":");
							}
						}
						if (fmt.equals("html")) {
							tmpStrBuf.append(docTexts[start+r]).append(crlf);
						} else {
							tmpStrBuf.append(docLines[start+r]).append(crlf);
						}
					}
				} else {
					hlOffset.add(0);
					for(int r = 0;r<docTexts.length;r++){
						if (startPos > 0) {
						        String line_no = String.valueOf(startPos + r);
						        if (fmt.equals("html")) {
						        	tmpStrBuf.append("<span class=\"ln_no\">" + line_no + "</span>");
							} else {
								tmpStrBuf.append(line_no + ":");
							}
						}
						if (fmt.equals("html")) {
							tmpStrBuf.append(docTexts[r]).append(crlf);
						} else {
							tmpStrBuf.append(docLines[r]).append(crlf);
						}
					}
				}
				//自前で作成したハイライト文字列をセット
				tmpStr.add(chop(tmpStrBuf.toString()));
				return tmpStr;
			}
		}
		return tmpStr;
	}

	private String chop(String str){
		return str.replaceAll(crlf + "$", "");
	}

	private String chop(String str, String target){
		return str.replaceAll(target + "$", "");
	}

	private String[] getHlTerms(){

		Set<String> termSet = new HashSet<String>();

		Iterator termKeyIte = termMap.keySet().iterator();
		while(termKeyIte.hasNext()){
			String key =  (String)termKeyIte.next();
			// ハイライト時に不要なフィールドは飛ばす
			if(ignoreList.contains(key)){
				continue;
			}
			Set<String> termValSet = termMap.get(key);
			Iterator termValIte = termValSet.iterator();
			while(termValIte.hasNext()){
				String val = (String)termValIte.next();
				termSet.add(val);
			}

		}
		String[] termSetArray = new String[termSet.size()];
		return termSet.toArray(termSetArray);
	}

	/**
	 * Termクラス
	 */
	private void doTerm(Term t){
		if (null == t) return;
		if(termMap.containsKey(t.field())){
			termMap.get(t.field()).add(t.text());
		} else {
			Set<String> set = new HashSet<String>();
			set.add(t.text());
			termMap.put(t.field(), set);
		}
	}

	/**
	 * Term[]クラス
	 */
	private void doTermArray(Term[] tArr){
		for(int i=0;i<tArr.length;i++){
			doTerm(tArr[i]);
		}
	}


	/**
	 * Queryクラス
	 */
	private void doQuery(Query q) {
		if (q instanceof BooleanQuery){
			BooleanQuery bq = (BooleanQuery)q;
			doBooleanQuery(bq);
		} else if(q instanceof PhraseQuery){
			PhraseQuery pq = (PhraseQuery)q;
			doPhraseQuery(pq);
		} else if(q instanceof MultiPhraseQuery){
			MultiPhraseQuery mpq = (MultiPhraseQuery)q;
			doMultiPhraseQuery(mpq);
		} else if(q instanceof TermQuery){
			TermQuery tq = (TermQuery)q;
			doTermQuery(tq);
		} else if(q instanceof ConstantScorePrefixQuery){
			ConstantScorePrefixQuery cspq = (ConstantScorePrefixQuery)q;
			doConstantScorePrefixQuery(cspq);
		} else if(q instanceof MultiTermQuery){
			MultiTermQuery mtq = (MultiTermQuery)q;
			doMultiTermQuery(mtq);
		}
	}

	/**
	 * TermQueryクラス
	 */
	private void doTermQuery(TermQuery tq){
		doTerm(tq.getTerm());
	}

	/**
	 * PhraseQueryクラス
	 */
	private void doPhraseQuery(PhraseQuery pq) {
		Term[] terms = pq.getTerms();
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< terms.length; i++){
			if (sb.length() > 0) {
				sb.append("*");
			}
			sb.append(terms[i].text());
		}
		Term t = terms[0];
		String s = "(" + sb.toString().trim() + ")";
		if(termMap.containsKey(t.field())){
			termMap.get(t.field()).add(s);
		} else {
			Set<String> set = new HashSet<String>();
			termMap.put(t.field(), set);
			set.add(s);
		}
	}

	/**
	 * MultiTermQueryクラス
	 * MultiTermQuery currently has 5 subclasses:
	 * 	FuzzyQuery
	 * 	NumericRangeQuery
	 * 	TermRangeQuery
	 * 	WildcardQuery PrefixQuery
	 * For better highlight, treat each differently.
	 *
	 * May return null for NumericRangeQuery
	 */
	private void doMultiTermQuery(MultiTermQuery mtq) {
		doTerm(mtq.getTerm());
	}

	/**
	 * MultiPhraseQueryクラス
	 */
	private void doMultiPhraseQuery(MultiPhraseQuery mpq){
		List<Object> list = mpq.getTermArrays();
		StringBuffer sb = new StringBuffer();
		String field  = null;
		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			if(obj instanceof Term[]){
				Term[] terms = (Term [])obj;
				if (field == null) {
					field = terms[0].field();
				}
				if (sb.length() > 0) {
					sb.append(' ');
				}
				if (terms.length > 1) {
					sb.append("(");
					for (int j = 0; j < terms.length ; j++) {
						if (j != 0) {
							sb.append(" ");
						}
						sb.append(terms[j].text());
					}
					sb.append(")");
				} else {
					sb.append(terms[0].text());
				}
			}
		}
		if (field == null) return;

		String s = "(" + sb.toString() + ")";
		if(termMap.containsKey(field)){
			termMap.get(field).add(s);
		} else {
			Set<String> set = new HashSet<String>();
			termMap.put(field, set);
			set.add(s);
		}
	}

	/**
	 * DisjunctionMaxQueryクラス
	 */
	private void doDisjunctionMaxQuery(DisjunctionMaxQuery dmq){
		Iterator iteDmq = dmq.iterator();
		while(iteDmq.hasNext()){
			Object obj = iteDmq.next();
			if(obj instanceof TermQuery){
				TermQuery tq = (TermQuery)obj;
				doTermQuery(tq);
			} else if(obj instanceof PhraseQuery){
				PhraseQuery pq = (PhraseQuery)obj;
				doPhraseQuery(pq);
			} else if(obj instanceof MultiPhraseQuery){
				MultiPhraseQuery mpq = (MultiPhraseQuery)obj;
				doMultiPhraseQuery(mpq);
			} else if(obj instanceof PhraseQuery){
				PhraseQuery pq = (PhraseQuery)obj;
				doPhraseQuery(pq);
			} else if(obj instanceof BooleanQuery){
				BooleanQuery bq = (BooleanQuery)obj;
				doBooleanQuery(bq);
			} else if(obj instanceof MultiTermQuery){
				MultiTermQuery mtq = (MultiTermQuery)obj;
				doMultiTermQuery(mtq);
			} else {
				//			  logger.log(_loglevel, "///////////////// DisjunctionMaxQuery :"+obj.getClass().getName());
			}
		}
	}

	/**
	 * ConstantScorePrefixQueryクラス
	 */
	private void doConstantScorePrefixQuery(ConstantScorePrefixQuery cspq){
		doTerm(cspq.getPrefix());
	}

	/**
	 * BooleanClauseクラス
	 */
	private void doBooleanClause(BooleanClause bc){
		Query q = bc.getQuery();
		if(q instanceof DisjunctionMaxQuery){
			DisjunctionMaxQuery dmq = (DisjunctionMaxQuery)q;
			doDisjunctionMaxQuery(dmq);
		} else if(q instanceof BooleanQuery){
			BooleanQuery bq = (BooleanQuery)q;
			doBooleanQuery(bq);
		} else if(q instanceof TermQuery){
			TermQuery tq = (TermQuery)q;
			doTermQuery(tq);
		} else if(q instanceof PhraseQuery){
			PhraseQuery pq = (PhraseQuery)q;
			doPhraseQuery(pq);
		} else if(q instanceof MultiPhraseQuery){
			MultiPhraseQuery mpq = (MultiPhraseQuery)q;
			doMultiPhraseQuery(mpq);
		} else if(q instanceof ConstantScorePrefixQuery){
			ConstantScorePrefixQuery cspq = (ConstantScorePrefixQuery)q;
			doConstantScorePrefixQuery(cspq);
		} else if(q instanceof ConstantScoreQuery){
			ConstantScoreQuery csq = (ConstantScoreQuery)q;
			doConstantScoreQuery(csq);
		} else if(q instanceof MultiTermQuery){
			MultiTermQuery mtq = (MultiTermQuery)q;
			doMultiTermQuery(mtq);
		} else {
			//		  logger.log(_loglevel, "///////////////// BooleanClause :"+q.getClass().getName());
		}
	}

	/**
	 * ConstantScoreQueryクラス
	 */
	private void doConstantScoreQuery(ConstantScoreQuery csq){
		Filter f = csq.getFilter();
		if(f instanceof WildcardFilter){
			WildcardFilter wf = (WildcardFilter)f;
			doTerm(wf.getTerm());
		} else {
			//		  logger.log(_loglevel, "///////////////// ConstantScoreQuery :"+csq.getClass().getName());
		}
	}

	/**
	 * BooleanQueryクラス
	 */
	private void doBooleanQuery(BooleanQuery bq){
		Iterator iteBq = bq.clauses().iterator();
		while(iteBq.hasNext()){
			Object obj = iteBq.next();
			if(obj instanceof BooleanClause){
				BooleanClause bc = (BooleanClause)obj;
				//対象外のクエリ以外
				if(!bc.isProhibited()){
					doBooleanClause(bc);
				}
			} else {
				//			  logger.log(_loglevel, "///////////////// BooleanQuery :"+obj.getClass().getName());
			}
		}
	}

	/**
	 * コード最初部分を返す
	 *@param readDoc 表示したいドキュメント
	 *@param fieldNames ハイライトフィールド
	 *@return  ハイライト情報
	 */
	public NamedList getMyHighlightInfoSrc(Document readDoc, String[] fieldNames){

		NamedList myDocSummaries = new SimpleOrderedMap();

		// ハイライトフィールド分のループ
		for(int z=0;z<fieldNames.length;z++){
			// 詰め替えようストリングバッファ
			List<String> tmpStr = new ArrayList<String>();

			// ハイライトフィールドに対応するドキュメントを取得。改行コード込の1行で取得。
			String[] docTexts = readDoc.getValues(fieldNames[z]);

			tmpStr = getSourceTop(docTexts, tmpStr);

			// 作成したハイライト情報をセット
			myDocSummaries.add(fieldNames[z], tmpStr);
		}
		return myDocSummaries;
	}

	private List<String> getSourceTop(String[] docTexts, List<String>tmpStr){

		// コメント削除用正規表現
		// ^//.*           //        をマッチ
		// ^$             空文字     をマッチ
		// ^@.*           @～（注釈）をマッチ
		Pattern pattern = Pattern.compile("^$|^@.*|^//.*");


		// 取得したドキュメントの値の数でループ 必ず１？
		for(int y=0;y<docTexts.length;y++){
			StringBuffer tmpStrBuf = new StringBuffer();
			int count = 0;
			// 行単位に分割
			String[] tmpBuf = HTMLConvert.convertIntoHTML(docTexts[y]).split(crlf);
			int mode = 1;
			for(int x=0;x<tmpBuf.length;x++){
				String line = tmpBuf[x];
				Matcher matcher = pattern.matcher(line.trim());
				if(count == 0){
					if (line.indexOf("*/") != -1) { // コメント終端に達したのでmodeを１に戻し、次へ。
						mode = 1;
						continue;
					} else if (line.indexOf("/*") != -1) { // コメント開始なので modeを２にセットし、次へ。
						mode = 2;
						continue;
					} else if (mode == 2) { // コメント中なので次へ。
						continue;
					} else if(matcher.matches()){ // 空行、注釈、一行コメントなので次へ。
						continue;
					}
					hlOffset.add(x);
				}
				if(count < RECODE){
					tmpStrBuf.append(line).append(crlf);
					count++;
				} else {
					break;
				}

			}
			//自前で作成したハイライト文字列をセット
			tmpStr.add(chop(tmpStrBuf.toString()));
		}
		return tmpStr;
	}

	@Override
	public String getDescription() {
		return "CodeDepotHighlighting";
	}

	@Override
	public String getVersion() {
		return "";
	}

	@Override
	public String getSourceId() {
		return "";
	}

	@Override
	public String getSource() {
		return "";
	}

	@Override
	public URL[] getDocs() {
		return null;
	}
}
