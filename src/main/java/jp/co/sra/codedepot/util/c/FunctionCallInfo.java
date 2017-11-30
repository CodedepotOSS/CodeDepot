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
package jp.co.sra.codedepot.util.c;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

public class FunctionCallInfo implements Serializable {
    private static final long serialVersionUID = -200908010843L;

    private IASTFunctionCallExpression mFCExp = null;

    private String mFuncName = null;

    private String[] mCallerParamTypes = null;
    private String mFilename = null;
    private int mStartLine = -1;




    public FunctionCallInfo() {
    }

    public void setASTNode(IASTFunctionCallExpression AST) {
        mFCExp = AST;
    }
    public IASTFunctionCallExpression getASTNode() {
        return mFCExp;
    }

    /* not used anywhere 17Feb2010
    private String mReturnType = null;
    public void setReturnType(String rStr) {
        mReturnType = rStr;
    }
    public String getReturnType() {
        return mReturnType;
    }
    */

    public void setFunctionName(String nStr) {
        mFuncName = nStr;
    }
    public String getFunctionName() {
        return mFuncName;
    }

    /* not used anywhere 17Feb2010
    private String[] mDeclParamTypes = null;
    public void setDeclaredParameterTypes(String[] tStrs) {
        mDeclParamTypes = tStrs;
    }
    public String[] getDeclaredParameterTypes() {
        return mDeclParamTypes;
    }
    */

    public void setCallerParameterTypes(String[] tStrs) {
        mCallerParamTypes = tStrs;
    }
    public String[] getCallerParameterTypes() {
        return mCallerParamTypes;
    }

    public void setFilename(String fStr) {
        mFilename = fStr;
    }
    public String getFilename() {
        return mFilename;
    }

    public void setStartLine(int line) {
        mStartLine = line;
    }
    public int getStartLine() {
        return mStartLine;
    }

}
