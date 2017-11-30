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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.c.CDTUtils;
import jp.co.sra.codedepot.util.c.CommentInfo;
import jp.co.sra.codedepot.util.c.FunctionCallInfo;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class IndexCClassVisitor extends IndexCFileVisitor {

	/** IndexedCodeFileオブジェクト */
	private IndexedCodeFile _icf;

	/** IndexedAbstructTypeオブジェクト */
	private IndexedAbstructType _itype;

	/** LineReadオブジェクト */
	private LineRead _rct;

	/** ファイル名 */
	private String _fileName;

	/** FunctionCall用テーブル */
	private HashMap<IASTFunctionCallExpression, FunctionCallInfo> _mCallTbl;

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFileオブジェクト
     * @param rct LineReadオブジェクト
     * @param fileName ファイル名
     * @param mCallTbl FunctionCall用テーブル
     */
	public IndexCClassVisitor(IndexedCodeFile icf, LineRead rct,
			String fileName,
			HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl) {
		super(icf, fileName, mCallTbl);
		this._rct = rct;
		this._icf = icf;
		this._fileName = fileName;
		this._mCallTbl = mCallTbl;
		this._itype = _icf.getCurrentIndexedType();
	}

	/**
     * visit関数。
     * <p>
     * ユーザ定義抽象データ型の定義を検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTDeclSpecifierオブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTDeclSpecifier node) {

		// ユーザ定義抽象データ型(class, struct, union)定義の場合
		// ユーザ定義抽象データ型(enum)定義の場合
		if (node instanceof IASTCompositeTypeSpecifier
				|| node instanceof IASTEnumerationSpecifier) {
			// ユーザ定義抽象データ型(class, struct, union, enum)宣言の場合
			// || node instanceof IASTElaboratedTypeSpecifier) {

			// 既に設定されている場合は何もしない
			if (_itype.getName() != null) {
				return PROCESS_CONTINUE;
			}

			IASTFileLocation location = node.getFileLocation();
			if (location == null) {
				// 通常ありえないが取得できない場合は無視する
				return PROCESS_CONTINUE;
			}

			int start = location.getStartingLineNumber();
			int offset = location.getNodeOffset();
			int length = location.getNodeLength();

			_itype.setStart(start);
			_itype.setSrc(offset, length);

			ArrayList<CommentInfo> commentList = _icf.getCommentList();
			Iterator<CommentInfo> it = commentList.iterator();
			while (it.hasNext()) {
				CommentInfo comment = it.next();
				if (comment.isCommentIn(offset, length)) {
					_itype.setComment(comment.getOffset(), comment.getLength());
				}
			}

			// ネームスペースの設定
			IASTName astName = null;
			if (node instanceof IASTCompositeTypeSpecifier) {
				astName = ((IASTCompositeTypeSpecifier) node).getName();
			} else if (node instanceof IASTEnumerationSpecifier) {
				astName = ((IASTEnumerationSpecifier) node).getName();
				// } else if (node instanceof IASTElaboratedTypeSpecifier) {
				// astName = ((IASTElaboratedTypeSpecifier) node).getName();
			}
			String namespace = CDTUtils.getAbsoluteScopeName(astName, false);
			if (null == namespace || namespace.isEmpty()) {
				namespace = DEFVALUE_NAMESPACE;
			}
			_itype.setNamespace(namespace);

			String name = astName.toString();
			_itype.setName(name);
		}
		return PROCESS_CONTINUE;

	}

	/**
     * visit関数。
     * <p>
     * 関数定義または関数宣言を検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTDeclarationオブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTDeclaration node) {

		// インナークラス内にあるかをチェック
		IASTNode tmpNode = node.getParent();
		while (true) {
			if (tmpNode instanceof IASTTranslationUnit) {
				// TOPノードに来たら抜ける
				break;
			} else if (tmpNode instanceof IASTCompositeTypeSpecifier) {
				// 自クラスにあるものは処理する
				if (tmpNode.getRawSignature().equals(_itype.getName())) {
					break;
				}
				// インナークラス内にあるものは処理しない
				return PROCESS_CONTINUE;
			}
			tmpNode = tmpNode.getParent();
		}

		// 関数定義の場合
		if (node instanceof IASTFunctionDefinition) {
			if (_itype instanceof IndexedClass) {
				((IndexedClass) _itype).createNewMethod();
				node.accept(new IndexCMethodVisitor(_icf,
						(IndexedClass) _itype, _rct, _fileName, _mCallTbl));
			}
		}
		// 関数宣言の場合
		else if (node instanceof IASTSimpleDeclaration) {
			if (_itype instanceof IndexedClass) {
				IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) node;
				IASTDeclarator[] declarators = declaration.getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator declarator = declarators[i];
					if (declarator instanceof IASTFunctionDeclarator) {
						((IndexedClass) _itype).createNewMethod();
						node.accept(new IndexCMethodVisitor(_icf,
								(IndexedClass) _itype, _rct, _fileName,
								_mCallTbl));
						break;
					}
				}
			}
		}

		return PROCESS_CONTINUE;
	}

	/**
     * visit関数。
     * <p>
     * 識別子を検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTNameオブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTName node) {
		String name = node.getRawSignature();

		// 無名ではない場合のみ追加する
		if (null != name && !name.isEmpty()) {
			_itype.addCodeText(name);
		}

		return PROCESS_CONTINUE;
	}

	/**
     * visit関数。
     * <p>
     * 文字列リテラルを検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTNameオブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTExpression node) {
		// リテラルの場合
		if (node instanceof IASTLiteralExpression) {
			IASTLiteralExpression expression = (IASTLiteralExpression) node;
			int kind = expression.getKind();
			// 文字列リテラルの場合
			if (IASTLiteralExpression.lk_string_literal == kind) {
				_itype.addCodeText(expression.getRawSignature());

			}
		}
		return PROCESS_CONTINUE;
	}

	/**
     * visit関数。
     * <p>
     * IASTProblemを検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTProblemオブジェクト
     * @return PROCESS_SKIP
     */
	public int visit(IASTProblem node) {
		System.err.printf("%s: line %5d: '%s'\n", getFilename(node), CDTUtils
				.getStartLine(node), node.getMessage());
		return PROCESS_SKIP;
	}

	private String getFilename(IASTNode node) {
		String ret = CDTUtils.getFilename(node);
		if (ret == null) {
			ret = _fileName;
		}
		return ret;
	}
}
