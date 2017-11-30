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
package jp.co.sra.codedepot.parser.c;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.solr.Indexer;

/**
 * IndexedClass クラス。 class及びstructを扱う。
 */
public class IndexedClass extends IndexedAbstructType {

	/** 関数宣言 */
	private List<IndexedMethod> _declaredMethods;

	/** 関数呼び出し */
	private StringBuilder _methodInvokeSeq;

	/** 現在関数 */
	private IndexedMethod _currentMethod;

	/** タイプ */
	private Type _type;

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFile オブジェクト
     * @param type タイプ
     */
	public IndexedClass(IndexedCodeFile icf, Type type) {
		super(icf);

		_type = type;
		_declaredMethods = new ArrayList<IndexedMethod>();
		_methodInvokeSeq = new StringBuilder("");
		_currentMethod = null;
	}

	/**
     * Methodリストを取得する。IDが重複する場合は定義のMethodとなる。
     *
     * @return Methodリスト
     */
	public List<IndexedMethod> getDeclaredMethods() {

		// 定義のIDを取得する
		List<String> defIds = new ArrayList<String>();
		for (ListIterator iter = _declaredMethods.listIterator(); iter
				.hasNext();) {
			IndexedMethod cmtd = (IndexedMethod) iter.next();
			if (cmtd.isDefinition()) {
				defIds.add(cmtd.getId());
			}
		}
		for (ListIterator iter = _icf.getAllDeclaredMethods().listIterator(); iter
				.hasNext();) {
			IndexedMethod cmtd = (IndexedMethod) iter.next();
			if (cmtd.isDefinition()) {
				defIds.add(cmtd.getId());
			}
		}

		List<IndexedMethod> methods = new ArrayList<IndexedMethod>();
		for (ListIterator iter = _declaredMethods.listIterator(); iter
				.hasNext();) {
			IndexedMethod cmtd = (IndexedMethod) iter.next();
			// IDが重複する宣言は無視
			if (!cmtd.isDefinition() && defIds.contains(cmtd.getId())) {
				continue;
			}

			// ネームスペースが設定されているものを追加
			// 設定されないものはASTエラーの場合なので無視する
			String namespace = cmtd.getNamespace();
			if (namespace != null && !namespace.isEmpty()) {
				methods.add(cmtd);
			}
		}

		return methods;
	}

	/**
     * 全Methodリストを取得する。
     *
     * @return 全Methodリスト
     */
	public List<IndexedMethod> getAllDeclaredMethods() {
		return _declaredMethods;
	}

	/**
     * 関数宣言を追加する。
     *
     */
	public void createNewMethod() {
		_currentMethod = new IndexedMethod(_icf, this);
		_declaredMethods.add(_currentMethod);
	}

	/**
     * 現在関数を取得する。
     *
     * @return 現在関数
     */
	public IndexedMethod getCurrentIndexedMethod() {
		return _currentMethod;
	}

	/**
     * 宣言された関数名を取得する。
     *
     * @return 宣言された関数名
     */
	public String getDeclaredMethodIds() {
		StringBuilder mids = new StringBuilder();

		for (IndexedMethod m : getDeclaredMethods()) {
			mids.append(m.getMethodName());
			mids.append(" ");
		}
		return mids.toString();
	}

	/**
     * 関数呼び出しを取得する。
     *
     * @return 関数呼び出し
     */
	public String getMethodInvokeSeq() {
		return _methodInvokeSeq.toString();
	}

	/**
     * 関数呼び出しを追加する。
     *
     * @param invokeSeq 関数呼び出し
     */
	public void addMethodInvokeSeq(String methodName) {
		_methodInvokeSeq.append(methodName);
		_methodInvokeSeq.append(" ");
	}

	/**
     * タイプを取得する。
     *
     * @return タイプ
     */
	public Type getType() {
		return _type;
	}

	/**
     * XML文字列を取得する。
     *
     * @return XML文字列
     */
	public String toXML() {
		return toXml(this);
	}

	/**
     * 検索フィールドを返す
     */
	@Override
	public Map<String, String> getFields() {
		HashMap<String, String> map = new HashMap<String,String>();
		map.put(Indexer.ID, getId());
		map.put(Indexer.BEGIN, Integer.toString(getStart()));
		map.put(Indexer.LANG, _icf.getLangName());
		map.put(Indexer.UNIT, _icf.getUnitName(this));
		map.put(Indexer.PKG, getNamespace());
		map.put(Indexer.CLS, getName());
		map.put(Indexer.COMMENT, getComment());
		map.put(Indexer.FDEF, getDeclaredMethodIds());
		map.put(Indexer.FCALL, getMethodInvokeSeq());
		map.put(Indexer.CODE, getCodeText());
		map.put(Indexer.SRC, getSrc());
		map.put(Indexer.CLONETKN, getCloneToken());
	    return map;
	}

	/**
     * 子供のノードを返す。
     */
	@Override
	public List<IndexedCode> getChildren() {
		List<IndexedCode> children = new ArrayList();
		List<IndexedMethod> list = getDeclaredMethods();
		if (list != null) {
			for (IndexedMethod c : list) {
				children.add((IndexedCode) c);
			}
		}
		return children;
	}

	/**
     * ソーステキストを返す。
     */
	@Override
	public String getSourceText() {
		return getSrc();
	}
}
