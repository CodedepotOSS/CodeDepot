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

import java.util.HashMap;

import jp.co.sra.codedepot.util.License;
import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.c.CDTUtils;
import jp.co.sra.codedepot.util.c.CommentInfo;
import jp.co.sra.codedepot.util.c.FunctionCallInfo;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

public class IndexCFileVisitor extends ASTVisitor {

	/** IndexedCodeFileオブジェクト */
	private IndexedCodeFile _icf;

	/** LineReadオブジェクト */
	private LineRead _rct;

	/** ファイル名 */
	private String _fileName;

	/** FunctionCall用テーブル */
	private HashMap<IASTFunctionCallExpression, FunctionCallInfo> _mCallTbl;

	/** namespace デフォルト値 */
	protected static final String DEFVALUE_NAMESPACE = ".GLOBALSCOPE";

	/**
     * コンストラクタ。
     *
     * @param icf IndexedCodeFileオブジェクト
     * @param fileName ファイル名
     * @param mCallTbl FunctionCall用テーブル
     */
	public IndexCFileVisitor(IndexedCodeFile icf, String fileName,
			HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl) {

		super();
		this._icf = icf;
		this._fileName = fileName;
		this._mCallTbl = mCallTbl;

		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
		shouldVisitTranslationUnit = true;
		shouldVisitProblems = true;
		shouldVisitDesignators = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
	}

	/**
     * LineReadオブジェクトを取得する。
     *
     * @return LineReadオブジェクト
     */
	public LineRead getLineRead() {
		return _rct;
	}

	/**
     * visit関数。
     * <p>
     * TOPノードを検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTTranslationUnitオブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTTranslationUnit node) {

		IASTComment[] comments = node.getComments();
		String license = "";

		for (int i = 0; i < comments.length; i++) {

			// 自分のファイルではない場合は無視する
			if (isAnothorNode(comments[i])) {
				continue;
			}

			int start = comments[i].getFileLocation().getStartingLineNumber();
			int end = comments[i].getFileLocation().getEndingLineNumber();
			int offset = comments[i].getFileLocation().getNodeOffset();
			int length = comments[i].getFileLocation().getNodeLength();

			_icf.addComment(offset, length);
			_icf.addCommentList(new CommentInfo(start, end, offset, length));

			// 一番最初に見つかったライセンスを有効とする
			if (license.length() == 0) {
			    	license = License.getLicense(comments[i].getRawSignature());
				_icf.setLicense(license);
			}
		}

		return PROCESS_CONTINUE;
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

		// 自分のファイルでない場合は無視する
		if (isAnothorNode(node)) {
			return PROCESS_CONTINUE;
		}

		// ユーザ定義抽象データ型(class, struct, union)定義の場合
		if (node instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier specifier = (IASTCompositeTypeSpecifier) node;

			String name = specifier.getName().getRawSignature();
			// 無名の場合は何もしない
			if (null == name || name.isEmpty()) {
				return PROCESS_CONTINUE;
			}

			int kind = specifier.getKey();
			switch (kind) {
			case ICPPASTCompositeTypeSpecifier.k_class:
				_icf.createNewType(IndexedAbstructType.Type.CLASS);
				break;
			case IASTCompositeTypeSpecifier.k_struct:
				_icf.createNewType(IndexedAbstructType.Type.STRUCT);
				break;
			case IASTCompositeTypeSpecifier.k_union:
				_icf.createNewType(IndexedAbstructType.Type.UNION);
				break;
			default:
				return PROCESS_CONTINUE;
			}
			node.accept(new IndexCClassVisitor(_icf, _rct, _fileName,  _mCallTbl));
		}
		// ユーザ定義抽象データ型(enum)定義の場合
		else if (node instanceof IASTEnumerationSpecifier) {
			IASTEnumerationSpecifier specifier = (IASTEnumerationSpecifier) node;

			String name = specifier.getName().getRawSignature();
			// 無名の場合は何もしない
			if (null == name || name.isEmpty()) {
				return PROCESS_CONTINUE;
			}

			_icf.createNewType(IndexedAbstructType.Type.ENUM);
			node.accept(new IndexCClassVisitor(_icf, _rct, _fileName, _mCallTbl));
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

		// 自分のファイルでない場合は無視する
		if (isAnothorNode(node)) {
			return PROCESS_CONTINUE;
		}

		// クラス内に定義されているかをチェック
		IASTNode tmpNode = node.getParent();
		while (true) {
			if (tmpNode instanceof IASTTranslationUnit) {
				// TOPノードに来たら抜ける
				break;
			} else if (tmpNode instanceof IASTCompositeTypeSpecifier) {
				// クラス内に定義されているものはFileVisitorでは処理しない
				return PROCESS_CONTINUE;
			}
			tmpNode = tmpNode.getParent();
		}

		// 関数定義の場合
		if (node instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition definition = (IASTFunctionDefinition) node;
			String fName = definition.getDeclarator().getName().toString();

			if (isMethod(fName)) {
				_icf.createNewMethod();
				node.accept(new IndexCMethodVisitor(_icf, _rct, false,
						_fileName, _mCallTbl));
			} else {
				_icf.createNewFunction();
				node.accept(new IndexCMethodVisitor(_icf, _rct, true,
						_fileName, _mCallTbl));
			}
		}
		// 関数宣言の場合
		// example: extern char *foo(char s);
		else if (node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) node;
			IASTDeclarator[] declarators = declaration.getDeclarators();
			for (int i = 0; i < declarators.length; i++) {
				IASTDeclarator declarator = declarators[i];
				if (declarator instanceof IASTFunctionDeclarator) {
					String fName = declarator.getName().toString();
					if (null != fName && fName.trim().length() > 0) {
						if (isMethod(fName)) {
							_icf.createNewMethod();
							node.accept(new IndexCMethodVisitor(_icf, _rct, false,
									_fileName, _mCallTbl));
						} else {
							_icf.createNewFunction();
							node.accept(new IndexCMethodVisitor(_icf, _rct, true,
									_fileName, _mCallTbl));
						}
					}
					break;
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

		// 自分のファイルでない場合は無視する
		if (isAnothorNode(node)) {
			return PROCESS_CONTINUE;
		}

		String name = node.getRawSignature();

		// 無名ではない場合のみ追加する
		if (null != name && !name.isEmpty()) {
			_icf.addCodeText(name);
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

		// 自分のファイルでない場合は無視する
		if (isAnothorNode(node)) {
			return PROCESS_CONTINUE;
		}

		// リテラルの場合
		if (node instanceof IASTLiteralExpression) {
			IASTLiteralExpression expression = (IASTLiteralExpression) node;
			int kind = expression.getKind();
			// 文字列リテラルの場合
			if (IASTLiteralExpression.lk_string_literal == kind) {
				_icf.addCodeText(expression.getRawSignature());
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
     * @return PROCESS_CONTINUE 自分のファイルでない場合
     */
	public int visit(IASTProblem node) {

		// 自分のファイルでない場合は無視する
		if (isAnothorNode(node)) {
			return PROCESS_CONTINUE;
		}

		System.err.printf("%s: line %5d: '%s'\n", getFilename(node), CDTUtils
				.getStartLine(node), node.getMessage());
		return PROCESS_SKIP;
	}

	private boolean isMethod(String name) {
		boolean result = false;

		// 名前に「::」が含まれていればMethodと判断する
		if (name.contains("::")) {
			result = true;
		}

		return result;
	}

	private String getFilename(IASTNode node) {
		String ret = CDTUtils.getFilename(node);
		if (ret == null) {
			ret = _fileName;
		}
		return ret;
	}

	/**
	 * 自ノードかどうかを判定する。
	 *
	 * @param node IASTNodeオブジェクト
	 * @return true:自ノードである、false:自ノードではない
	 */
	private boolean isAnothorNode(IASTNode node) {
		boolean res = false;

		if (!_fileName.equals(getFilename(node))) {
			res = true;
		}

		// 現在は自分のファイルのみしかパースしていないので、
		// ここでtrueが返ることはない
		return res;
	}
}
