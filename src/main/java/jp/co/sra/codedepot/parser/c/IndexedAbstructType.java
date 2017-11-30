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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.parser.IndexedCode;

/**
 * IndexedAbstructTypeクラス。
 */
public abstract class IndexedAbstructType extends IndexedCode {

	/** タイプ名定義 */
	public enum Type {
		CLASS, STRUCT, UNION, ENUM
	}

	/** 名前 */
	private String _name;

	/** ネームスペース */
	private String _namespace;

	/** ソース */
	private String _src;

	/** コード */
	private StringBuilder _codeText;

	/** 開始行 */
	private int _start = 1;

	/** コメント */
	private StringBuilder _comment;

	/** IndexedCodeFileオブジェクト */
	protected IndexedCodeFile _icf;

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFileオブジェクト
     */
	public IndexedAbstructType(IndexedCodeFile icf) {
		this._icf = icf;
		_codeText = new StringBuilder("");
		_comment = new StringBuilder("");
	}

	/**
     * 名前を取得する。
     *
     * @return 名前
     */
	public String getName() {
		return _name;
	}

	/**
     * 名前を設定する。
     *
     * @param name 名前
     */
	public void setName(String name) {
		this._name = name;
	}

	/**
     * ネームスペースを取得する。
     *
     * @return ネームスペース
     */
	public String getNamespace() {
		return _namespace;
	}

	/**
     * ネームスペースを設定する。
     *
     * @param name ネームスペース
     */
	public void setNamespace(String namespace) {
		this._namespace = namespace;
	}

	/**
     * タイプを取得する。
     *
     * @return タイプ
     */
	public abstract Type getType();

	/**
     * ソースを取得する。
     *
     * @return ソース
     */
	public String getSrc() {
		return _src;
	}

	/**
     * ソースを設定する。
     *
     * @param startoffset 初期オフセット
     * @param length 長さ
     */
	public void setSrc(int startoffset, int length) {
		_src = new String(_icf.getProgramText(), startoffset, length);
	}

	/**
     * コードを取得する。
     *
     * @return コード
     */
	public String getCodeText() {
		return _codeText.toString();
	}

	/**
     * コードを設定する。
     *
     * @param text コード
     */
	public void addCodeText(String text) {
		_codeText.append(text);
		_codeText.append(" ");
	}

	/**
     * 開始行を取得する。
     *
     * @return 開始行
     */
	public int getStart() {
		return _start;
	}

	/**
     * 開始行を設定する。
     *
     * @param start 開始行
     */
	public void setStart(int start) {
		this._start = start;
	}

	/**
     * コメントを取得する。
     *
     * @return コメント
     */
	public String getComment() {
		return _comment.toString();
	}

	/**
     * コメントを設定する。
     *
     * @param startOffset 初期オフセット
     * @param length 長さ
     */
	public void setComment(int startOffset, int length) {
		_comment.append(_icf.getProgramText(), startOffset, length);
	}

	/**
     * CloneTokenを取得する。
     */
	public String getCloneToken() {
		return _icf.getCloneToken(getSrc());
	}

	/**
     * IDを取得する。
     *
     * @return ID
     */
	public String getId() {
		return (_icf.getId() + "#" + _name);
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
		return null;
	}

	/**
     * ソーステキストを返す。
     */
	@Override
	public String getSourceText() {
		return getSrc();
	}

}
