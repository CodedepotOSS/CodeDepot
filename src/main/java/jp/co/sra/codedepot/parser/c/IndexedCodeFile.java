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
import java.util.ListIterator;
import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.IToken;

import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.util.c.CommentInfo;
import jp.co.sra.codedepot.index.ProgramAnalyzer;

/**
 * IndexedCodeFile クラス。
 * $Id: IndexedCodeFile.java 2356 2017-11-10 07:50:30Z fang $
 */
public class IndexedCodeFile extends IndexedCode {

	public static final String UNIT_FILE = "file";
	public static final String UNIT_CLASS = "tClass";
	public static final String UNIT_STRUCT = "tStruct";
	public static final String UNIT_UNION = "tUnion";
	public static final String UNIT_ENUM = "tEnum";
	public static final String UNIT_METHOD_DECL = "mmDecl";
	public static final String UNIT_METHOD_DEF = "mmDef";
	public static final String UNIT_FUNC_DECL = "mfDecl";
	public static final String UNIT_FUNC_DEF = "mfDef";
	public static final String LANG_C = "C";

	/** プログラムテキスト */
	private char[] _programText;

	/** ID */
	private String _id;

	/** Uuid */
	private String _uuid;

	/** License */
	private String _license;

	/** クラス名 */
	private String _className;

	/** コメント */
	private StringBuilder _comments;

	/** 全コメントのリスト */
	private ArrayList<CommentInfo> _commentList;

	/** コメント行のマップ */
	private HashMap<Integer, StringBuilder> _commentLines;

	/** コード */
	private StringBuilder _codeText;

	/** コードの長さ */
	private short _codeTextLength = 0;

	/** タイプリスト */
	private List<IndexedAbstructType> _declaredTypes;

	/** 現在のタイプ */
	private IndexedAbstructType _currentType;

	/** Functionリスト */
	private List<IndexedFunction> _declaredFunctions;

	/** 現在のFunction */
	private IndexedFunction _currentFunction;

	/** Methodリスト */
	private List<IndexedMethod> _declaredMethods;

	/** 現在のMethod */
	private IndexedMethod _currentMethod;

	/** Indentifier */
	IASTName[] _identifiers;

	/** Indentifier */
        IToken[] _tokens;

	/**
     * コンストラクタ。
     */
	public IndexedCodeFile() {
		_comments = new StringBuilder("");
		_commentList = new ArrayList<CommentInfo>();
		_commentLines = new HashMap<Integer, StringBuilder>();
		_codeText = new StringBuilder("");
		_declaredTypes = new ArrayList<IndexedAbstructType>();
		_declaredFunctions = new ArrayList<IndexedFunction>();
		_declaredMethods = new ArrayList<IndexedMethod>();
	}

	/**
     * プログラムテキストを設定する。
     *
     * @param programText プログラムテキスト
     */
	public void setProgramText(char[] programText) {
		this._programText = programText;
	}

	/**
     * プログラムテキストを取得する。
     *
     * @return プログラムテキスト
     */
	public char[] getProgramText() {
		return this._programText;
	}

	/**
     * IDを設定する。
     *
     * @param id ID
     */
	public void setId(String id) {
		this._id = id;
	}

	/**
     * IDを取得する。
     *
     * @return ID
     */
	public String getId() {
		return _id;
	}

	/**
     * Uuid を設定する。
     *
     * @param id Uuid
     */
	public void setUuid(String uuid) {
		this._uuid = uuid;
	}

	/**
     * Uuidを取得する。
     *
     * @return Uuid
     */
	public String getUuid() {
		return _uuid;
	}

	/**
     * License を設定する。
     *
     */
	public void setLicense(String license) {
		this._license = license;
	}

	/**
     * License を取得する。
     *
     */
	public String getLicense() {
		return _license;
	}

	/**
     * クラス名を設定する。
     *
     * @param license クラス名
     */
	public void setClassName(String className) {
		this._className = className;
	}

	/**
     * クラス名を取得する。
     *
     * @return クラス名
     */
	public String getClassName() {
		return _className;
	}

	/**
     * コメントを追加する。
     *
     * @param start 初期オフセット
     * @param length 長さ
     */
	public void addComment(int start, int length) {
		_comments.append(getProgramText(), start, length);
		_comments.append('\n');
	}

	/**
     * コメントを取得する。
     *
     * @return コメント
     */
	public String getComments() {
		return _comments.toString();
	}

	/**
     * コメント情報を追加する。
     *
     * @param comment コメント情報
     */
	public void addCommentList(CommentInfo comment) {
		_commentList.add(comment);
	}

	/**
     * 全コメントリストを取得する。
     *
     * @return 全コメントリスト
     */
	public ArrayList<CommentInfo> getCommentList() {
		return _commentList;
	}

	/**
     * コメント行のマップを追加する。
     *
     * @param line 行数
     * @param cLineBuf コメント
     */
	public void addCommentLines(int line, StringBuilder cLineBuf) {
		Integer sline = new Integer(line);
		_commentLines.put(sline, cLineBuf);
	}

	/**
     * コメント行のマップを取得する。
     *
     * @return コメント行のマップ
     */
	public HashMap<Integer, StringBuilder> getCommentLines() {
		return _commentLines;
	}

	/**
     * コードを追加する。
     *
     * @param codeText コード
     */
	public void addCodeText(String codeText) {
		this._codeText.append(codeText);
		this._codeText.append(" ");
		if (_codeTextLength == 8) {
			this._codeText.append("\n"); // add readability to xml file
			_codeTextLength = 0;
		} else {
			_codeTextLength++;
		}
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
     * ソースを取得する。
     *
     */
	public String getSrc() {
		return new String(this._programText);
	}
	/**
     * タイプを追加する。
     *
     * @param type 種類
     */
	public void createNewType(IndexedAbstructType.Type type) {
		switch (type) {
		case CLASS:
			_currentType = new IndexedClass(this, type);
			break;
		case UNION:
			_currentType = new IndexedUnionType(this);
			break;
		case STRUCT:
			_currentType = new IndexedClass(this, type);
			break;
		case ENUM:
			_currentType = new IndexedEnumType(this);
			break;
		default:
			// ありえない
			break;
		}
		_declaredTypes.add(_currentType);
	}

	/**
     * タイプを取得する。
     *
     * @return タイプ
     */
	public List<IndexedAbstructType> getDeclaredTypes() {

		List<IndexedAbstructType> types = new ArrayList<IndexedAbstructType>();
		for (ListIterator iter = _declaredTypes.listIterator(); iter.hasNext();) {
			IndexedAbstructType ctype = (IndexedAbstructType) iter.next();

			// ネームスペースが設定されているものを追加
			// 設定されないものはASTエラーの場合なので無視する
			String namespace = ctype.getNamespace();
			if (namespace != null && !namespace.isEmpty()) {
				types.add(ctype);
			}
		}
		return types;
	}

	/**
     * 現在のタイプを取得する。
     *
     * @return 現在のタイプ
     */
	public IndexedAbstructType getCurrentIndexedType() {
		return _currentType;
	}

	/**
     * 現在のFunctionを取得する。
     *
     * @return 現在のFunction
     */
	public IndexedFunction getCurrentIndexedFunction() {
		return _currentFunction;
	}

	/**
     * Functionを追加する。
     */
	public void createNewFunction() {
		_currentFunction = new IndexedFunction(this);
		_declaredFunctions.add(_currentFunction);
	}

	/**
     * Functionを取得する。IDが重複する場合は定義のFunctionとなる。
     *
     * @return Function
     */
	public List<IndexedFunction> getDeclaredFunctions() {

		// 定義のIDを取得する
		List<String> defIds = new ArrayList<String>();
		for (ListIterator iter = _declaredFunctions.listIterator(); iter
				.hasNext();) {
			IndexedFunction cfunc = (IndexedFunction) iter.next();
			if (cfunc.isDefinition()) {
				defIds.add(cfunc.getId());
			}
		}

		List<IndexedFunction> funcs = new ArrayList<IndexedFunction>();
		for (ListIterator iter = _declaredFunctions.listIterator(); iter
				.hasNext();) {
			IndexedFunction cfunc = (IndexedFunction) iter.next();
			// IDが重複する宣言は無視
			if (!cfunc.isDefinition() && defIds.contains(cfunc.getId())) {
				continue;
			}

			// ネームスペースが設定されているものを追加
			// 設定されないものはASTエラーの場合なので無視する
			String namespace = cfunc.getNamespace();
			if (namespace != null && !namespace.isEmpty()) {
				funcs.add(cfunc);
			}
		}

		return funcs;
	}

	/**
     * 現在のMethodを取得する。
     *
     * @return 現在関数
     */
	public IndexedMethod getCurrentIndexedMethod() {
		return _currentMethod;
	}

	/**
     * Methodを追加する。
     */
	public void createNewMethod() {
		_currentMethod = new IndexedMethod(this);
		_declaredMethods.add(_currentMethod);
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
		for (ListIterator iter = _declaredTypes.listIterator(); iter.hasNext();) {
			IndexedAbstructType type = (IndexedAbstructType) iter.next();
			if (type instanceof IndexedClass) {
				for (ListIterator iter2 = ((IndexedClass) type)
						.getAllDeclaredMethods().listIterator(); iter2
						.hasNext();) {
					IndexedMethod cmtd = (IndexedMethod) iter2.next();
					if (cmtd.isDefinition()) {
						defIds.add(cmtd.getId());
					}
				}
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
     * 識別子のマップを取得する。
     *
     * @return 識別子のマップ
     */
	public IASTName[] getIdentifiers() {
		return this._identifiers;
	}

	public void setIdentifiers(IASTName[] identifiers) {
		this._identifiers = identifiers;
	}

	/**
     * Token のマップを取得する。
     *
     * @return Token のマップ
     */
	public IToken[] getTokens() {
		return this._tokens;
	}

	public void setTokens(IToken[] tokens) {
		this._tokens = tokens;
	}


	/**
     * 開始行を取得する。
     *
     * @return 開始行
     */
	public int getStart() {
		// ファイルの場合は1固定
		return 1;
	}

	/**
     * ネームスペースを取得する。
     *
     * @return ネームスペース
     */
	public String getNamespace() {
		return "";
	}

	/**
     * UNIT名を取得する。
     *
     * @param type IndexedAbstructTypeオブジェクト
     * @return UNIT名
     */
	public String getUnitName(IndexedAbstructType type) {

		String unitName = null;

		IndexedAbstructType.Type t = type.getType();
		switch (t) {
		case CLASS:
			unitName = UNIT_CLASS;
			break;
		case STRUCT:
			unitName = UNIT_STRUCT;
			break;
		case ENUM:
			unitName = UNIT_ENUM;
			break;
		case UNION:
			unitName = UNIT_UNION;
			break;
		default:
			// ありえない
			unitName = "unknown";
			break;
		}
		return unitName;
	}

	/**
     * UNIT名を取得する。
     *
     * @param func IndexedFunctionオブジェクト
     * @return UNIT名
     */
	public static String getUnitName(IndexedFunction func) {
		String unitName = null;
		if (func.isDefinition()) {
			unitName = UNIT_FUNC_DEF;
		} else {
			unitName = UNIT_FUNC_DECL;
		}
		return unitName;
	}

	/**
     * UNIT名を取得する。
     *
     * @param cmtd IndexedMethodオブジェクト
     * @return UNIT名
     */
	public static String getUnitName(IndexedMethod cmtd) {
		String unitName = null;
		if (cmtd.isDefinition()) {
			unitName = UNIT_METHOD_DEF;
		} else {
			unitName = UNIT_METHOD_DECL;
		}
		return unitName;
	}

	/**
     * LANG名を取得する。
     *
     * @return LANG名
     */
	public static String getLangName() {
		// とりあえず現状は「C」固定
		return LANG_C;
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
     * CloneTokenを取得する。
     */
	public String getCloneToken (String src) {
		try {
			return ProgramAnalyzer.getTokenText(ProgramAnalyzer.LANG_C, src);
		} catch (java.io.IOException e) {
			return "";
 		}
	}

	public String getClassDef () {
		StringBuilder sb = new StringBuilder("");

		/* DeclaredTypes */
		List<IndexedAbstructType> types = getDeclaredTypes();
		if (types != null) {
			for (IndexedAbstructType c : types) {
				String cname = c.getName();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(cname);
			}
		}
		/* DeclaredMethods */
		List<IndexedMethod> methods = getDeclaredMethods();
		if (methods != null) {
			for (IndexedMethod c : methods) {
				String cname = c.getClassName();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(cname);
			}
		}
		return sb.toString();
	}

	public String getMethodDef () {
		StringBuilder sb = new StringBuilder("");

		/* DeclaredTypes */
		List<IndexedAbstructType> types = getDeclaredTypes();
		if (types != null) {
			for (IndexedAbstructType c : types) {
				if (c instanceof IndexedClass) {
					IndexedClass cls = (IndexedClass)c;
					String fname = cls.getDeclaredMethodIds();
			    		if (sb.length() > 0) {
						sb.append(" ");
					}
					sb.append(fname);
				}
			}
		}

		/* DeclaredFuctions */
		List<IndexedFunction> funcs = getDeclaredFunctions();
		if (funcs != null) {
			for (IndexedFunction c : funcs) {
				String fname = c.getFunctionName();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(fname);
			}
		}

		/* DeclaredMethods */
		List<IndexedMethod> methods = getDeclaredMethods();
		if (methods != null) {
			for (IndexedMethod c : methods) {
				String fname = c.getMethodName();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(fname);
			}
		}
		return sb.toString();
	}

	public String getMethodCall () {
		StringBuilder sb = new StringBuilder("");

		/* DeclaredTypes */
		List<IndexedAbstructType> types = getDeclaredTypes();
		if (types != null) {
			for (IndexedAbstructType c : types) {
				if (c instanceof IndexedClass) {
					IndexedClass cls = (IndexedClass)c;
					String fcall = cls.getMethodInvokeSeq();
			    		if (sb.length() > 0) {
						sb.append(" ");
					}
					sb.append(fcall);
				}
			}
		}

		/* DeclaredFuctions */
		List<IndexedFunction> funcs = getDeclaredFunctions();
		if (funcs != null) {
			for (IndexedFunction c : funcs) {
				String fcall = c.getMethodInvokeSeq();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(fcall);
			}
		}

		/* DeclaredMethods */
		List<IndexedMethod> methods = getDeclaredMethods();
		if (methods != null) {
			for (IndexedMethod c : methods) {
				String fcall = c.getMethodInvokeSeq();
			    	if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(fcall);
			}
		}
		return sb.toString();
	}

	/**
     * 検索フィールドを返す
     */
	@Override
	public Map<String, String> getFields() {
		HashMap<String, String> map = new HashMap<String,String>();
	    map.put(Indexer.ID, getId());
	    map.put(Indexer.LANG, getLangName());
	    map.put(Indexer.UNIT, UNIT_FILE);
	    map.put(Indexer.BEGIN, Integer.toString(getStart()));
	    map.put(Indexer.COMMENT, getComments());
	    map.put(Indexer.CLS, getClassDef());
	    map.put(Indexer.FDEF, getMethodDef());
	    map.put(Indexer.FCALL, getMethodCall());
	    map.put(Indexer.CODE, getCodeText());
	    map.put(Indexer.SRC, getSrc());
	    map.put(Indexer.PKG,  getNamespace());
	    map.put(Indexer.CLONETKN,  getCloneToken(getSrc()));
	    return map;
	}

	/**
     * 子供のノードを返す。
     */
	@Override
	public List<IndexedCode> getChildren() {
		List<IndexedCode> children = new ArrayList();

		/* add DeclaredTypes */
		List<IndexedAbstructType> types = getDeclaredTypes();
		if (types != null) {
			for (IndexedAbstructType c : types) {
				children.add((IndexedCode) c);
			}
		}

		/* add DeclaredFuctions */
		List<IndexedFunction> funcs = getDeclaredFunctions();
		if (funcs != null) {
			for (IndexedFunction c : funcs) {
				children.add((IndexedCode) c);
			}
		}

		/* add DeclaredMethods */
		List<IndexedMethod> methods = getDeclaredMethods();
		if (methods != null) {
			for (IndexedMethod c : methods) {
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
