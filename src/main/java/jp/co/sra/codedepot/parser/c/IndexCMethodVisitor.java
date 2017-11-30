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
import java.util.logging.Logger;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.c.CDTUtils;
import jp.co.sra.codedepot.util.c.CommentInfo;
import jp.co.sra.codedepot.util.c.DeclarationKinds;
import jp.co.sra.codedepot.util.c.FunctionCallInfo;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;

/**
 *
 * $Id: IndexCMethodVisitor.java 2356 2017-11-10 07:50:30Z fang $
 */
public class IndexCMethodVisitor extends IndexCFileVisitor {

	private static Logger logger = Logger.getLogger(IndexCMethodVisitor.class.getName());

	/** ファイル名 */
	private String _fileName;

	/** FunctionCall用テーブル */
	private HashMap<IASTFunctionCallExpression, FunctionCallInfo> _mCallTbl;

	/** IndexedCodeFileオブジェクト */
	private IndexedCodeFile _icf;

	/** IndexedClassオブジェクト */
	private IndexedClass _icls;

	/** IndexedMethodオブジェクト */
	private IndexedMethod _imtd;

	/** IndexedFunctionオブジェクト */
	private IndexedFunction _ifnc;

	/** LineReadオブジェクト */
	private LineRead _rct;

	/** Function flag */
	private boolean _isFunction;

	/** pointer-to-function or local class can appear inside a function definition.*/
	private boolean _isInsideMethod = false;

	/**
     * IndexCClassVisitor から accept された場合のコンストラクタ
     *
     * @param icf IndexedCodeFileオブジェクト
     * @param icls IndexedClassオブジェクト
     * @param rct LineReadオブジェクト
     * @param filename ファイル名
     * @param mCallTbl 関数呼び出しのテーブル
     */
	public IndexCMethodVisitor(IndexedCodeFile icf, IndexedClass icls, LineRead rct,
			String fileName, HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl) {
		super(icf, fileName, mCallTbl);
		this._rct = rct;
		this._icf = icf;
		this._icls = icls;
		this._imtd = _icls.getCurrentIndexedMethod();
		this._isFunction = false;
		this._mCallTbl = mCallTbl;
		this._isInsideMethod = false;
	}

	/**
     * IndexFileVisitor から accept された場合のコンストラクタ
     *
     * @param icf IndexedCodeFileオブジェクト
     * @param rct LineReadオブジェクト
     * @param isFunction 関数か否か
     * @param filename ファイル名
     * @param mCallTbl 関数呼び出しのテーブル
     */
	public IndexCMethodVisitor(IndexedCodeFile icf, LineRead rct, boolean isFunction,
			String fileName, HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl) {
		super(icf, fileName, mCallTbl);
		this._rct = rct;
		this._icf = icf;
		this._imtd = (IndexedMethod) _icf.getCurrentIndexedMethod();
		this._ifnc = (IndexedFunction) _icf.getCurrentIndexedFunction();
		this._isFunction = isFunction;
		this._mCallTbl = mCallTbl;
		this._isInsideMethod = false;
	}

	/**
     * <p>
	 * IndexCMethodVisitor が accept された局面が定義文であるか否かを判断する関数
     * </p>
	 *
	 * @param node IASTDeclaration オブジェクト
	 * @return 定義文か否か
	 */
	private boolean isDefinitionNode(IASTDeclaration node) {
		return (node instanceof IASTFunctionDefinition);
	}

	/**
     * <p>
	 * IndexMethodVisitor が accept された局面が宣言文であるか否かを判断する関数
     * </p>
	 *
	 * @param node IASTDeclaration オブジェクト
	 * @return 宣言文か否か
	 */
	private boolean isDeclarationNode(IASTDeclaration node) {
		boolean ret = false;
		if (node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) node;
			IASTDeclarator[] decls = sDecl.getDeclarators();
			for (int i = 0; i < decls.length; i++) {
				ret = (decls[i] instanceof IASTFunctionDeclarator);
				if (ret) break;
			}
		}
		return ret;
	}

	/**
	 * IASTDeclSpecifier does not include pointer or reference, they
	 * are attached to IASTFunctionDeclarator
	 * IASTPointerOpeartor --> IASTPointer --> ICASTPointer | ICPPASTPointerToMember
	 *                     --> ICPPASTReferenceOperator
	 * @param decl
	 * @param ptAndRefs
	 */
	private void setReturnType(IASTDeclSpecifier decl, IASTPointerOperator[] ptAndRefs) {
		StringBuilder retType = new StringBuilder(ASTSignatureUtil.getSignature(decl));
		int i = 0;
		while(ptAndRefs != null && i < ptAndRefs.length) {
			IASTPointerOperator pr = ptAndRefs[i++];
			if (pr instanceof IASTPointer) {
				retType.append(" *"); //add space to be consistent with CDT expression
			} else if (pr instanceof ICPPASTReferenceOperator) {
				retType.append(" &");
			}
		}
		if (_isFunction) { _ifnc.setReturnType(retType.toString()); }
		else { _imtd.setReturnType(retType.toString()); }
	}

	private void setInTypes(IASTFunctionDeclarator decltor) {
		String[] inTypes = ASTSignatureUtil.getParameterSignatureArray(decltor);
		for (int j = 0; j < inTypes.length; j++ ) {
			if (_isFunction) {
				_ifnc.setInputTypes(inTypes[j]);
			} else {
				_imtd.setInputTypes(inTypes[j]);
			}
		}
	}

	/**
	 * FIXME
	 * I need a few days to fix this namespace resolution problem.
     * <p>
	 * IndexedMethod もしくは IndexedFunction に情報をセットする関数
     * </p>
	 *
	 * @param binding IASTFunctionDeclarator の binding
	 * @throws Exception scope が取得できないとき
	 */
	private void setNameSpace(IASTName dName) throws Exception {
		// dName から関数名のみを取得
		String mName = dName.toString();
		int colPlace = mName.lastIndexOf("::");
		if (colPlace > 0) {
			mName = mName.substring(colPlace + 2);
		}

		if (_isFunction) {
			_ifnc.setFunctionName(mName);
		} else {
			_imtd.setMethodName(mName);
		}
		// ネームスペース情報、クラス情報の取得およびセット
		String namespace = "";
		String className = "";
			IASTName sName = CDTUtils.getScopeNode(dName);
			if (sName != null) {
				DeclarationKinds scopeKind = CDTUtils.getKind(sName);
				if (scopeKind == DeclarationKinds.KIND_NAMESPACE) {
					namespace = CDTUtils.getAbsoluteScopeName(sName, true);
				} else if (scopeKind == DeclarationKinds.KIND_CLASS
						|| scopeKind == DeclarationKinds.KIND_STRUCT) {
					namespace = CDTUtils.getAbsoluteScopeName(sName, false);
					className = sName.toString();
				} else if (scopeKind == DeclarationKinds.KIND_FUNCTION) {
					IASTName spName = CDTUtils.getScopeNode(sName);
					DeclarationKinds parentScopeKind = CDTUtils.getKind(spName);
					if (parentScopeKind == DeclarationKinds.KIND_NAMESPACE) {
						namespace = CDTUtils.getAbsoluteScopeName(spName, true);
					} else if (parentScopeKind == DeclarationKinds.KIND_CLASS) {
						namespace = CDTUtils.getAbsoluteScopeName(spName, false);
						className = spName.toString();
					/* TODO: to be removed after further testing, works for template function
					} else if (parentScopeKind == DeclarationKinds.KIND_FUNCTION) {
						//ICPPFucntionTemplate's scope is itself
						namespace = CDTUtils.getAbsoluteScopeName(spName, true);
					*/
					} else {
						;
					}
				}
			}

			if (namespace == null || namespace == "") {
				namespace = DEFVALUE_NAMESPACE;
			}
			if (_isFunction) {
 				_ifnc.setNamespace(namespace);
			} else {
				_imtd.setNamespace(namespace);
				if ((className == null || className == "") &&
						_imtd.getClassName().length() > 0) {
					/*leave class name as it was set heuristically */
					;
				} else {
					_imtd.setClassName(className);
				}
			}
	}

	/**
     * visit関数
     * <p>
     * 関数定義もしくは関数宣言を検出した際の動作を定義する。
     * </p>
     *
     * @param node IASTDecalaration オブジェクト
     * @return PROCESS_CONTINUE
     */
	public int visit(IASTDeclaration node) {
		try {
			if ( !_isInsideMethod &&
					(isDefinitionNode(node) || isDeclarationNode(node) )) {
				_isInsideMethod = true;

				IASTFileLocation location = node.getFileLocation();
				if (location == null) {
					// 通常ありえないが取得できない場合は無視する
					logger.fine("location is null: " + node.toString());
					return PROCESS_CONTINUE;
				}

				int start = location.getStartingLineNumber();
				int offset = location.getNodeOffset();
				int length = location.getNodeLength();

				// IndexedMethod もしくは IndexedFunction に開始行、ソース情報をセット
				if (_isFunction) {
					_ifnc.setDefinition(false);
					_ifnc.setStart(start);
					_ifnc.setSrc(offset, length);
				} else {
					_imtd.setDefinition(false);
					_imtd.setStart(start);
					_imtd.setSrc(offset, length);
				}

				// コメントの取得
				ArrayList<CommentInfo> commentList = _icf.getCommentList();
				Iterator<CommentInfo> it = commentList.iterator();
				while (it.hasNext()) {
					CommentInfo comment = it.next();
					if (comment.isCommentIn(offset, length)) {
						if (_isFunction) {
							_ifnc.setComment(comment.getOffset(), comment.getLength());
						} else {
							_imtd.setComment(comment.getOffset(), comment.getLength());
						}
					}
				}

				IASTName dName;
				// 関数宣言の場合
				if (node instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration sDecls = (IASTSimpleDeclaration) node;
					IASTDeclarator[] decls = sDecls.getDeclarators();
					for (int i = 0; i < decls.length; i++) {
						IASTDeclarator decl = decls[i];
						if (decl instanceof IASTFunctionDeclarator) {
							setReturnType(sDecls.getDeclSpecifier(), decl.getPointerOperators());
							setInTypes((IASTFunctionDeclarator)decl);
							dName = decl.getName();
							setNameSpace(dName);
							break;
							//FIXME: ignoring complicated cases: extern int f(), g();
							//in this case, only f() is indexed, g() is thrown away
						}
					}
				// 関数定義の場合
				} else if (node instanceof IASTFunctionDefinition) {
					if (_isFunction) { _ifnc.setDefinition(true);}
					else {_imtd.setDefinition(true);}
					IASTFunctionDefinition fDef = (IASTFunctionDefinition) node;
					IASTFunctionDeclarator  fDecl = fDef.getDeclarator();
					setReturnType(fDef.getDeclSpecifier(), fDecl.getPointerOperators());
					setInTypes(fDecl);
					dName = fDecl.getName();
					if (dName instanceof ICPPASTQualifiedName) {
						IASTName[] dNames = ((ICPPASTQualifiedName) dName).getNames();
						dName = dNames[dNames.length - 1];
						/* FIXME Feb 22, 2010 a quick and dirty fix, a complete fix should be done to setNameSpace */
						/* Since namespace resolution is not reliable, we heuristically treat function definitions such as
						 * 	SomeName::someOtherName()
						 * as: SomeName is the class name and someOtherName is the method name.
						 */
						int len = dNames.length;
						if (len > 1) {
							if (!_isFunction) {
								/* class method */
								IASTName clsNameNode = dNames[len - 2]; //next to last is classname
								String clsName = clsNameNode.toString();
								_imtd.setClassName(clsName);
							}
						}
					}
					setNameSpace(dName);
				}
			}
		} catch (Exception e) {
			logger.fine(e.getMessage());
		}
		return PROCESS_CONTINUE;
	}

	/**
     * <p>
	 * 文字列リテラルもしくは関数呼び出しを検出した際の動作を定義する。
     * </p>
	 *
	 * @param node IASTEpression オブジェクト
	 * @return PROCESS_COMTINUE
	 */
	public int visit(IASTExpression node) {
		if (((node instanceof IASTFunctionCallExpression) && _isFunction ) && _ifnc.isDefinition() ||
			((node instanceof IASTFunctionCallExpression) && (!_isFunction)) && _imtd.isDefinition()) {

			// mCallTbl から関数呼び出しの情報を取得し、セット
			String mName = null;;
			String params = null;

			FunctionCallInfo fci = _mCallTbl.get((IASTFunctionCallExpression) node);
			if (fci == null) {
				mName = "";
				params = "";
			} else {
				String tmpName = fci.getFunctionName();
				String firstName = "";
				String secondName = "";
				String[] paramTypes = fci.getCallerParameterTypes();

				// 関数名とクラス名の間の  "::" を "#" に変換
				int colPlace = tmpName.lastIndexOf("::");
				if (colPlace > 0) {
					firstName = tmpName.substring(0, colPlace);
					secondName = tmpName.substring(colPlace + 2);
					mName = firstName + "#" + secondName;
				} else {
					mName = DEFVALUE_NAMESPACE + "#" + tmpName;
				}

				if (paramTypes == null) {
					params = "void";
				} else {
					for (int i = 0; i < paramTypes.length; i++) {
						if (i == 0) {
							params = paramTypes[i];
						} else {
							params = params + "," + paramTypes[i];
						}
					}
				}
				if (_isFunction) {
					_ifnc.addMethodInvokeSeq(mName + "(" + params + ")");
				} else {
					_imtd.addMethodInvokeSeq(mName + "(" + params + ")");
				}
			}
		}

		// リテラルの場合
		if (node instanceof IASTLiteralExpression) {
			IASTLiteralExpression expression = (IASTLiteralExpression) node;
			int kind = expression.getKind();
			// 文字列リテラルの場合
			if (IASTLiteralExpression.lk_string_literal == kind) {
				if (_isFunction) {
					_ifnc.addCodeText(expression.getRawSignature());
				} else {
					_imtd.addCodeText(expression.getRawSignature());
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
		if (!name.isEmpty()) {
			if (_isFunction) {
				_ifnc.addCodeText(name);
			} else {
				_imtd.addCodeText(name);
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

	/**
     * <p>
	 * visit関数。
     * </p>
	 *
	 * @param node IASTNodeオブジェクト
	 * @return PROCESS_CONTINUE
	 */
	public int visit(IASTDeclSpecifier node) {
		return PROCESS_CONTINUE;
	}

	/**
     * <p>
	 * ファイル名を取得する関数
     * </p>
	 *
	 * @param node
	 * @return
	 */
	private String getFilename(IASTNode node) {
		String ret = CDTUtils.getFilename(node);
		if (ret == null) {
			ret = _fileName;
		}
		return ret;
	}

	/*
	 *  Set function input types and output type with IBinding
	 *	What it gets is expanded types, such as
	 *  	typedef char* CharPtr
	 *  	int foo(CharPtr)
	 *  we get
	 *     inTypes={char *}, outType={int}
	 *  Besides, error and exceptions are thrown when resolevBinding() does
	 *  not get what all it needs to resolve types.
	 *  We have to fall down on CDTUtils to do an ad hoc fix for types like
	 *  	vector<string> foo (vector<int>)
	 *  because compiler does not know either vector or string.

	IBinding binding = dName.resolveBinding();
	String typeName = "";
	String[] protoParams = null;


	// 関数 の取得
	if (binding instanceof IFunction) {
		IFunction fnc = (IFunction) binding;
		IType retType = null;
		IType[] paramTypes = null;

		// 関数情報の取得
		IFunctionType fncType;
		try {
			fncType = fnc.getType();
		} catch (Exception e) {
			logger.info("CDT: " + e.toString());
			throw e;
		}

		try {
			// 引数型をセット。
			paramTypes = fncType.getParameterTypes();
		} catch (Exception e) {
			logger.info("CDT: " + e.toString());
		}

		try {
			// 戻り値の型をセット。
			retType = fncType.getReturnType();
			typeName = CDTUtils.getType(retType, false);
		} catch (Exception e) {
			logger.info("CDT: " + e.toString());
		}


		// dName から関数名のみを取得
		String mName = dName.toString();
		int colPlace = mName.lastIndexOf("::");
		if (colPlace > 0) {
			mName = mName.substring(colPlace + 2);
		}

		if (_isFunction) {
			_ifnc.setReturnType(typeName);
			_ifnc.setFunctionName(mName);
		} else {
			_imtd.setReturnType(typeName);
			_imtd.setMethodName(mName);
		}

		int n = paramTypes.length;

		if (n > 0) {
			protoParams = new String[n];

			//18 Feb 2010
			//* convert typedef back
			//*this is very ad hoc solution
			//*but we should be able to use this method get the original signature
			//*FIXME: cleanup code here, extremly ugly
			//*
			IASTNode parent = dName.getParent();
			if (parent instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator decl = (IASTStandardFunctionDeclarator)parent;
				String[] tmpParamTypes = ASTSignatureUtil.getParameterSignatureArray(decl);
				if (tmpParamTypes.length ==n) {
					for (int j =0; j<n; j++) {
						protoParams[j] = tmpParamTypes[j];
						System.out.println("\t SIGNATURE: \t" + protoParams[j]);
					}
				} else {
					System.err.println("ERROR in IndexCMethodVisitor, This should not happen");
				}

			} else {
				// the old way
				// FIXME: these code is dead
				for (int j = 0; j < n; j++) {
					protoParams[j] = CDTUtils.getType(paramTypes[j], false);
				}
			}

			for (int j = 0; j < n; j++ ) {
				// IndexedMethod もしくは IndexedFunction に引数の型をセット。
				if (_isFunction) {
					_ifnc.setInputTypes(protoParams[j]);
				} else {
					_imtd.setInputTypes(protoParams[j]);
				}
			}
		} else {
			protoParams = new String[1];
			protoParams[0] = new String("");

			// IndexedMethod もしくは IndexedFunction に引数の型をセット。
			if (_isFunction) {
				_ifnc.setInputTypes(protoParams[0]);
			} else {
				_imtd.setInputTypes(protoParams[0]);
			}
		}
		*/
}
