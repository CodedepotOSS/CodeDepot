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
import java.util.logging.Logger;

import jp.co.sra.codedepot.util.c.CDTUtils;
import jp.co.sra.codedepot.util.c.DeclarationKinds;
import jp.co.sra.codedepot.util.c.FunctionCallInfo;

import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.dom.*;
import org.eclipse.cdt.core.dom.ast.*;

public class FunctionCallVisitor extends ASTVisitor {
    private static Logger logger = Logger.getLogger(FunctionCallVisitor.class.getName());

    /*
     * Privates.
     */
    private String mFilename = null;

    private String getFilename(IASTNode node) {
        String ret = CDTUtils.getFilename(node);
        if (ret == null) {
            ret = mFilename;
        }
        return ret;
    }
    private void setFilename(String filename) {
        mFilename = filename;
    }

    /*
     * A main container to store function call information.
     */
    private ArrayList<FunctionCallInfo> mCallList =
        new ArrayList<FunctionCallInfo>();
    private HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl =
        new HashMap<IASTFunctionCallExpression, FunctionCallInfo>();

    /*
     * Publics.
     */
    public FunctionCallInfo[] getFunctionCalls() {
        return
            (FunctionCallInfo[])mCallList.toArray(
                new FunctionCallInfo[mCallList.size()]);
    }

    public
    HashMap<IASTFunctionCallExpression, FunctionCallInfo>
        getFunctionCallTable() {
        return mCallTbl;
    }


    /*
     * The visitor.
     */
    public int visit(IASTExpression node) {

        if ((node instanceof IASTFunctionCallExpression) == true) {

        	// ASTメソッド内でのNullPointerExceptionの発生を回避するための暫定対策
			if (null == node.getFileLocation()) {
				return PROCESS_CONTINUE;
			}

            IASTFunctionCallExpression fcExp =
                (IASTFunctionCallExpression)node;

            /*
             * Firstly, determine function full-sccoped name and type.
             */
            IASTExpression fExp = fcExp.getFunctionNameExpression();

            IASTName fNameNode = null;
            String funcName = null;

            /*
             * Determine the function name.
             */
            if (fExp instanceof IASTIdExpression) {
            	IASTIdExpression fId = (IASTIdExpression)fExp;
            	fNameNode = fId.getName();
            } else if (fExp instanceof IASTFieldReference) {
            	IASTFieldReference fr = (IASTFieldReference)fExp;
            	fNameNode = fr.getFieldName();
            }
            if (fNameNode == null) {
            	/*
            	 * Must be one of the above.
            	 */
            	return PROCESS_CONTINUE;
            }

            /* 判断する必要がない, 後の処理はこれと関係ない。
            DeclarationKinds dk = CDTUtils.getKind(fNameNode);
            if (dk != DeclarationKinds.KIND_FUNCTION) {
            	return PROCESS_CONTINUE;
            }
            */

            funcName = CDTUtils.getAbsoluteScopeName(fNameNode, true);


            /*
             * Get the parameters' type from the function's actual
             * parameters.
             */
            String[] callerParams = null;

            IASTExpression pExp = fcExp.getParameterExpression();
            if (pExp == null) {
            	/*
            	 * # of params == 0
            	 */
            	callerParams = null;
            } else if ((pExp instanceof IASTExpressionList) == true) {
            	/*
            	 * # of the params > 1
            	 */
            	IASTExpressionList expList = (IASTExpressionList)pExp;
            	IASTExpression[] pExps = expList.getExpressions();
            	int nExps = pExps.length;

            	if (nExps <= 0) {
            		callerParams = null;
            	} else {
            		callerParams = new String[nExps];
            		IType tmpExpType = null;
            		String tmpStr = null;

            		for (int i = 0; i < nExps; i++) {
            			tmpExpType = pExps[i].getExpressionType();
            			tmpStr = ASTTypeUtil.getType(tmpExpType, false);
            			if (tmpStr != null && tmpStr.length() > 0) {
            				callerParams[i] = tmpStr;
            			} else {
            				callerParams[i] = new String("_UNK");
            			}
            		}
            	}
            } else {
            	/*
            	 * # of the params == 1, any expression.
            	 */
            	IType tmpExpType = pExp.getExpressionType();

            	String tmpStr = ASTTypeUtil.getType(tmpExpType, false);
            	if (tmpStr.equals("void") == true) {
            		callerParams = null;
            	} else {
            		callerParams = new String[1];
            		if (tmpStr != null && tmpStr.length() > 0) {
            			callerParams[0] = tmpStr;
            		} else {
            			callerParams[0] = new String("_UNK");
            		}
            	}
            }

            /*
             * OK now we are ready for store a FunctionCallInfo.
             */
            FunctionCallInfo fi = new FunctionCallInfo();

            /*
             * Fill the info.
             */
            fi.setASTNode(fcExp);
            fi.setFunctionName(funcName);
            //callerParams can be null
            fi.setCallerParameterTypes(callerParams);
            fi.setFilename(getFilename(fcExp));
            fi.setStartLine(CDTUtils.getStartLine(fcExp));

            /*
             * Then determine return type and parameters' type.
             * 関数を宣言するところから戻りタイプと関数タイプをとる、
             * 実際に使われていない。かえって、とれないときには、関数の呼び出し
             * まで捨ててしまう。
            IType fTyp = fExp.getExpressionType();
            if (fTyp instanceof IFunctionType) {
                IType retType = null;
                IType[] paramTypes = null;
                IFunctionType ft = (IFunctionType)fTyp;
                try {
                    retType = ft.getReturnType();
                    paramTypes = ft.getParameterTypes();
                } catch (Exception e) {
                    logger.info("CDT: " + e.toString());
                    return PROCESS_CONTINUE;
                }
                String rtStr = ASTTypeUtil.getType(retType, false);

                int n = paramTypes.length;
                String[] protoParams = null;

                if (n > 0) {
                	String firstParam = ASTTypeUtil.getType(paramTypes[0],
                			false);
                	if (n == 1 &&
                			firstParam.equals("void") == true) {
                		protoParams = null;
                	} else {
                		protoParams = new String[n];
                		for (int i = 0; i < n; i++) {
                			protoParams[i] = ASTTypeUtil.getType(paramTypes[i],
                					false);
                		}
                	}
                } else {
                	protoParams = null;
                }
            }

            //fi.setDeclaredParameterTypes(protoParams);
            //fi.setReturnType(rtStr);
            */

            /*
             * Lastly, store the info.
             */
            mCallList.add(fi);
            mCallTbl.put(fcExp, fi);
        }
        return PROCESS_CONTINUE;
    }


    public int visit(IASTProblem node) {
        /*
         * Just in case.
         */
        System.err.printf("%s: line %5d: '%s'\n",
                          getFilename(node),
                          CDTUtils.getStartLine(node),
                          node.getMessage());
        return PROCESS_CONTINUE;
    }


    /*
     * Constructor.
     */
    public FunctionCallVisitor(String filename) {
        setFilename(filename);

        /*
         * FIXME:
         *	Following initializations are kinda munbo-jumbo. Need
         *	to specify which ones are really needed.
         */

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
}
