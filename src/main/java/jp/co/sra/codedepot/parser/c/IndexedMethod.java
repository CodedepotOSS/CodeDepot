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
import java.util.Map;
import java.util.HashMap;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.search.SignatureQuery;
import jp.co.sra.codedepot.solr.Indexer;

/**
 * IndexedMethod クラス。
 *
 */
public class IndexedMethod extends IndexedCode {

	/** ネームスペース名 */
	private String _namespace;

	/** クラス名 */
	private String _className;

	/** 関数名 */
	private String _methodName;

	/** 戻り値 */
	private String _returnType;

	/** 引数 */
	private StringBuilder _inputTypes;

	/** 関数呼び出し */
	private StringBuilder _methodInvokeSeq;

	/** コメント */
	private StringBuilder _comment;

	/** ソース */
	private String _src;

	/** コード */
	private StringBuilder _codeText;

	/** 開始行 */
	private int _start = 1;

	/** 定義かどうかのフラグ */
	private boolean _isDefinition;

	/** IndexedCodeFileオブジェクト */
	private IndexedCodeFile _icf;

	/** IndexedClassオブジェクト */
	private IndexedClass _icls;

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFileオブジェクト
     */
	public IndexedMethod(IndexedCodeFile icf) {
		this._icf = icf;
		_inputTypes = new StringBuilder("");
		_methodInvokeSeq = new StringBuilder("");
		_comment = new StringBuilder("");
		_codeText = new StringBuilder("");
	}

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFileオブジェクト
     * @param icls IndexedClassオブジェクト
     */
	public IndexedMethod(IndexedCodeFile icf, IndexedClass icls) {
		this(icf);
		this._icls = icls;
	}

	/**
     * ネームスペース名を設定する。
     *
     * @param namespace ネームスペース名
     */
	public void setNamespace(String namespace) {
		_namespace = namespace;
	}

	/**
     * ネームスペース名を取得する。
     *
     * @return ネームスペース名
     */
	public String getNamespace() {
		return _namespace;
	}

	/**
     * クラス名を取得する。
     *
     * @return クラス名
     */
	public String getClassName() {
		if (_className == null) return "";
		return _className;
	}

	/**
     * クラス名を設定する。
     *
     * @param name クラス名
     */
	public void setClassName(String name) {
		_className = name;
	}

	/**
     * 関数名を取得する。
     *
     * @return 関数名
     */
	public String getMethodName() {
		return _methodName;
	}

	/**
     * 関数名を設定する。
     *
     * @param name 関数名
     */
	public void setMethodName(String name) {
		//remove space for overloaded operator
		//even if there is no space in the source code, CDT adds one
		//if this is modified, modify IndexedFunction.java#setFunctionName too.
		this._methodName = name.replaceAll(" ", "");
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
     * 引数を取得する。
     *
     * @return 引数
     */
	public String getInputTypes() {
		if (_inputTypes.length() == 0) {
			return "";
		} else {
			return _inputTypes.toString().trim();
		}
	}

	/**
     * 引数を追加する。
     *
     * @param inputType 引数
     */
	public void setInputTypes(String inputType) {
		if ( _inputTypes.length() > 0) {
			this._inputTypes.append(SignatureQuery.TYPE_SEPARATOR);
		}
		String s = SignatureQuery.normalizeType(inputType);
		this._inputTypes.append(s);
	}

	/**
     * 関数呼び出しを取得する。
     *
     * @return 関数呼び出し
     */
	public String getMethodInvokeSeq() {
		String retValue = _methodInvokeSeq.toString();
		if (_methodInvokeSeq.length() > 0) {
			retValue = retValue.substring(0, retValue.length() -1);
		}
		return retValue;
	}

	/**
     * 関数呼び出しを追加する。
     *
     * @param methodName 関数呼び出し
     */
	public void addMethodInvokeSeq(String methodName) {
		_methodInvokeSeq.append(SignatureQuery.replaceSeparator(methodName));
		_methodInvokeSeq.append(" ");
		if(null != _icls) {
			_icls.addMethodInvokeSeq(SignatureQuery.replaceSeparator(methodName));
		}
	}

	/**
     * 戻り値を取得する。
     *
     * @return 戻り値
     */
	public String getReturnType() {
		return _returnType;
	}

	/**
     * 戻り値を設定する。
     *
     * @param type 戻り値
     */
	public void setReturnType(String type) {
		_returnType = SignatureQuery.normalizeType(type);
	}

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
     * @param startOffset 初期オフセット
     * @param length 長さ
     */
	public void setSrc(int startOffset, int length) {
		this._src = new String(_icf.getProgramText(), startOffset, length);
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
     * @param codeFrag コード
     */
	public void addCodeText(String codeFrag) {
		this._codeText.append(codeFrag);
		this._codeText.append(" ");
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
     * 定義かどうかを取得する。
     *
     * @return 定義の場合true、宣言の場合false
     */
	public boolean isDefinition() {
		return _isDefinition;
	}

	/**
     * 定義かどうかを設定する。
     *
     * @param definition 定義の場合true、宣言の場合false
     */
	public void setDefinition(boolean definition) {
		_isDefinition = definition;
	}

	/**
     * CloneTokenを取得する。
     */
	public String getCloneToken () {
		return _icf.getCloneToken(getSrc());
	}

	/**
     * IDを取得する。
     *
     * @return ID
     */
	public String getId() {
		return (_icf.getId() + "#" + _className + "#" + _methodName + "(" + getInputTypes() + ")");
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
		map.put(Indexer.CLS, getClassName());
		map.put(Indexer.FDEF, getMethodName());
		map.put(Indexer.OUT_TYPE, getReturnType());
		map.put(Indexer.IN_TYPES, getInputTypes());
		map.put(Indexer.FCALL, getMethodInvokeSeq());
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
