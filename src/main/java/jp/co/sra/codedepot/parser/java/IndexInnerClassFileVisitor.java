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
 *
 */
package jp.co.sra.codedepot.parser.java;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
/**
 * ASTVistor used for inner and other classes that are not the
 * first class in a file. We just extract all the comments and
 * user-defined ids to add to the first class.
 * @author yunwen
 * @author xuefen
 */
public class IndexInnerClassFileVisitor extends IndexJavaFileVisitor {

	public IndexInnerClassFileVisitor(IndexedCodeFile icf) {
		super(icf);
		// TODO Auto-generated constructor stub

	}

	@Override
	public boolean visit(CompilationUnit cu) {
		// doing nothing
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration md) {
		// doing nothing
		return true;
	}
        /*
	 *  return Expression  Todo for multi lines.
	 */
    	@Override
	public boolean visit(ReturnStatement rs) {
	    return true;
	}


	@Override
	public boolean visit(Modifier mdf) {
		// doing nothing
		return true;
	}
	//@Override

	public boolean visit(TypeDeclaration td) {
		// doing nothing
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fd) {
		// doing nothing
		return true;
	}

	@Override
	public boolean visit(CatchClause cc) {
		// doing nothing
		return true;
	}

}
