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
package jp.co.sra.codedepot.parser.java;

// $Id: IndexJavaMethodVisitor.java 2342 2017-11-09 05:36:32Z fang $

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.lang.String;

import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;

import jp.co.sra.codedepot.util.LineRead;

/**
 * Extract index information from the a Java method
 * @author yunwen,xuefen
 */
public class IndexJavaMethodVisitor extends IndexJavaFileVisitor {

	private static Logger logger = Logger
			.getLogger(IndexJavaMethodVisitor.class.getName());
	private IndexedMethod imtd;
	private IndexedCodeFile icf;
	private IndexedClass icls;
	private LineRead rct;
	private Map<String, String> cSymbTab;
	private Map<String, String> mSymbTab;
	private Map<String, String> clsPkgTab;
	private List<Integer> srcLocLst = new ArrayList();
	private List<String> srcLines = new ArrayList();

	static {
		// set this to higher level if not debugging
		logger.setLevel(Level.INFO);
	}

	/*
	 * @Todo: The comments for linebuf will be referenced  to
	 *  create type link tag. linebuf -> lineObject
	 */
	public IndexJavaMethodVisitor(IndexedCodeFile icf, IndexedClass icls,
			Map<String, String> cSymbTab, Map<String, String> mSymbTab,
			Map<String, String> clsPkgTab) {
		super(icf);
		this.cSymbTab = cSymbTab;
		this.mSymbTab = mSymbTab;
		this.clsPkgTab = clsPkgTab;
		this.icf = icf;
		this.icls = icls;
		this.imtd = icls.getCurrentIndexedMethod();
		logger.entering("IndexJavaMethodVisitor", "Constructor");

	}

	public IndexJavaMethodVisitor(IndexedCodeFile icf, IndexedClass icls,
			LineRead rct, Map<String, String> cSymbTab,
			Map<String, String> mSymbTab, Map<String, String> clsPkgTab) {

		super(icf);
		this.cSymbTab = cSymbTab;
		this.mSymbTab = mSymbTab;
		this.clsPkgTab = clsPkgTab;
		this.icf = icf;
		this.icls = icls;
		this.imtd = icls.getCurrentIndexedMethod();
		this.rct = rct;
		this.srcLocLst = rct.getLocList();
		this.srcLines = rct.getSrcList();
		logger.entering("IndexJavaMethodVisitor", "MyConstructor");
	}

	public IndexJavaMethodVisitor(IndexedCodeFile icf) {
		super(icf);
		this.icf = icf;
		this.imtd = icls.getCurrentIndexedMethod();
		logger.entering("IndexJavaMethodVisitor", "Constructor");
	}

	/**
	 * <Field , Type> are registed in a Class Symbolic table
	 *
	 */
	public boolean visit(FieldDeclaration fd) {
		if (fd != null) {
			int sline = getLineNum(fd);
			List fragment = fd.fragments();
			Type type = fd.getType();
			String typeName = type.toString();
			if (!type.isPrimitiveType()) {
				typeName = getFullTypeName(type, clsPkgTab);
			}
			for (Iterator it = fragment.iterator(); it.hasNext();) {
				VariableDeclarationFragment vit = (VariableDeclarationFragment) it
						.next();

				mSymbTab.put((String) vit.getName().getIdentifier(), typeName);
				logger.finer("FieldDeclaration -->" + sline + ":"
						+ vit.getName().getIdentifier() + " , " + typeName);

			}
		}
		return true;
	}

	/*
	 * To be done in next page public boolean visit(EnhancedForStatement st) {
	 * Expression exp = st.getExpression();
	 * logger.finer("EnhacnedForStatement"); if (exp != null ) { exp.accept(new
	 * LinkJavaStatementVisitor(icf)); } return true; }
	 */

	/*
	 * Todo: multi ASTNode in the same line with return node
	 */
	/* 20090630
	public boolean visit(ReturnStatement rs) {
		StringBuilder lineBuf = new StringBuilder();
		if (rs != null) {
			int sline = getLineNum(rs);
			int pst = rs.getStartPosition();
			int lsz = rs.getLength();
			Expression exp = rs.getExpression();
			int nodeType;

			if (exp != null) {
				nodeType = exp.getNodeType();
				switch (nodeType) {
				case ASTNode.METHOD_INVOCATION:
					lineBuf.append(new StringBuilder("    return "));
					icf.setSrcLines(sline, lineBuf);
					break;
				case ASTNode.CLASS_INSTANCE_CREATION:
					lineBuf.append(new StringBuilder("    return "));
					icf.setSrcLines(sline, lineBuf);
					break;
				default:
					break;
				}
			}
		}
		return true;
	}
*/
	/*
	 * ThrowStatement
	 */
/* 20090630
	public boolean visit(ThrowStatement thrs) {
		StringBuilder lineBuf = new StringBuilder();
		if (thrs != null) {
			int sline = getLineNum(thrs);
			Expression exp = thrs.getExpression();
			int nodeType;

			if (exp != null) {
				nodeType = exp.getNodeType();
				switch (nodeType) {
				case ASTNode.METHOD_INVOCATION:
					lineBuf.append(new StringBuilder("    return "));
					icf.setSrcLines(sline, lineBuf);
					break;
				case ASTNode.CLASS_INSTANCE_CREATION:
					lineBuf.append(new StringBuilder("    return  "));
					icf.setSrcLines(sline, lineBuf);
					break;
				default:
					break;
				}
			}
		}
		return true;
	}
*/
	/*  ---20090630
	public boolean visit(Assignment asmt) {
		StringBuilder lineBuf = new StringBuilder();
		Expression rexp = asmt.getRightHandSide();
		int sline = getLineNum(asmt);
		int pst = 0;
		int rpst = 0;
		StringBuilder spaces = new StringBuilder();

		switch (rexp.getNodeType()) {
		case ASTNode.METHOD_INVOCATION:
			pst = asmt.getStartPosition();
			rpst = rexp.getStartPosition();
			spaces = getHSpace(asmt);
			if (spaces != null) {
				lineBuf.append(spaces);
			}
			lineBuf.append(icf.getProgramText(), pst, rpst - pst);
			icf.setSrcLines(sline, lineBuf);
			break;
		case ASTNode.CLASS_INSTANCE_CREATION:
			pst = asmt.getStartPosition();
			rpst = rexp.getStartPosition();
			spaces = getHSpace(asmt);
			if (spaces != null) {
				lineBuf.append(spaces);
			}

			lineBuf.append(icf.getProgramText(), pst, rpst - pst);
			icf.setSrcLines(sline, lineBuf);
			break;
		default:
			break;
		}
		return true;
	}
*/
	public boolean visit(MethodInvocation mi) {
		logger.entering("IndexJavaMethod", "MethodInvocation", mi);
		Expression expr = null;
		//int parentNodeType = mi.getParent().getNodeType();
		int mnum = 1;
		StringBuilder params = new StringBuilder();
		expr = mi.getExpression();
		String mtd = mi.getName().getIdentifier();
		List paramList = mi.arguments();
		String cls = getClassName(expr);
		int sline = getLineNum(mi);

		logger.finer("MethodInvocation mtd ==> " + mtd + "(" + paramList.size()+")");
		//logger.info("MethodInvocation cls ==>" + cls);
		if (mi.getParent() != null) {
		}
		/*
		StringBuilder doneLn = icf.getSrcLine(sline);
		StringBuilder lineBuf = new StringBuilder();
		int spaces = 0;

		if (doneLn == null)
			spaces = getSpaces(mi);
		while (spaces != 0) {
			lineBuf.append(' ');
			spaces = spaces - 1;
		}
		lineBuf.append("<span class=\"cls use\" id=\"");
		lineBuf.append(cls);
		lineBuf.append("\">");
		if (expr != null) {
			lineBuf.append(expr.toString());
		} else {
			// ToDo lineBuf.append(cls);
		}
		lineBuf.append(".</span>");

		int pos = 0;
		if (doneLn != null)
			pos = doneLn.length();
		if (pos > 0) {
			if (doneLn.charAt(pos - 1) == ',') {
				mnum = mnum + 1;
			}
		}
		icf.setSrcLines(sline, lineBuf);
		lineBuf = new StringBuilder();
		*/
		// There is not any paramter
		switch (paramList.size()) {

		case 0:
			imtd.addMethodInvokeSeq(cls + "#" + mtd + "(" + ")");
			/*
			lineBuf.append("<span class=\"mtd use\" id=\"");
			lineBuf.append(cls + "#" + mtd + "()\">");
			lineBuf.append(mtd);
			lineBuf.append("</span>");
			lineBuf.append("();");
			icf.setSrcLines(sline, lineBuf);
			lineBuf = new StringBuilder();
			*/
			break;
		default:
			// if (paramList.size() > 0) {
			int flg = 0;
			for (Iterator it = paramList.iterator(); it.hasNext();) {
				Object pObj = it.next();
				String paramType = getParamType(pObj);
				if (flg == 0) {
					params.append(paramType);
				} else {
					params.append(",");
					params.append(paramType);
				}
				flg++;
				logger.finer("ParamList:==> " + paramType);

			}
			imtd.addMethodInvokeSeq(cls + "#" + mtd + "(" + params + ")");
			logger.finer("MethodVisitor " + cls + "#" + mtd + "(" + params+ ")");

		}
		return true;
	}


	/*
	 * The visit is used to create type link in future.

	public boolean visit(ClassInstanceCreation cic) {

		StringBuilder lineBuf = new StringBuilder();
		int sline = getLineNum(cic);
		int spaces = getSpaces(cic);
		if (cic.getParent() != null) {
			logger.finer("paraent=> " + sline + ":" + cic.getParent() + "  "
					+ cic.getParent().getNodeType() + "  Parent: "
					+ cic.getParent().getParent().getNodeType());
		}
		while (spaces != 0) {
			lineBuf.append(' ');
			spaces = spaces - 1;
		}
		lineBuf.append("new <span class=\"cls use\" id=\"");
		lineBuf.append(getClassName(cic));
		lineBuf.append("\">");
		lineBuf.append(cic.getType().toString());
		lineBuf.append("</span>(");
		icf.setSrcLines(sline, lineBuf);
		lineBuf = new StringBuilder();
		List args = cic.arguments();
		if (args.size() > 0) {
			int flg = 0;
			for (Iterator it = args.iterator(); it.hasNext();) {
				Expression paraExp = (Expression) it.next();
				lineBuf = new StringBuilder();
				sline = getLineNum(paraExp);
				if (flg == 0) {
					lineBuf.append(paraExp.toString());
					icf.setSrcLines(sline, lineBuf);
				} else {
					lineBuf.append(",");
					lineBuf.append(paraExp.toString());
					icf.setSrcLines(sline, lineBuf);
				}
				flg++;
			}
			icf.setSrcLines(sline, new StringBuilder(");"));
		} else {
			lineBuf.append(");");
			icf.setSrcLines(sline, lineBuf);
		}
		return true;
	}
*/
	public boolean visit(VariableDeclarationStatement vds) {
		int sline = 0;
		//int spaces = getSpaces(vds);
		//StringBuilder lineBuf = new StringBuilder("");
		if (vds != null) {
			sline = getLineNum(vds);
			logger
					.finer("VariableDeclarationStatement : " + sline + "  "
							+ vds);
		/*	while (spaces > 0) {
				lineBuf.append(' ');
				spaces = spaces - 1;
			}
			lineBuf.append("<span class=\"cls use\" id=\"");
			*/
			List fragment = vds.fragments();
			Type type = vds.getType();

			String typeName = type.toString();

			if (!type.isPrimitiveType()) {
				typeName = getFullTypeName(type, clsPkgTab);
			}
			/*
			lineBuf.append(typeName);
			lineBuf.append("\">");
			lineBuf.append(type.toString());
			lineBuf.append(" </span>");

			logger.finer("VariableStatement=> " + typeName + ".." + fragment);
			 */
			for (Iterator it = fragment.iterator(); it.hasNext();) {
				VariableDeclarationFragment vit = (VariableDeclarationFragment) it
						.next();
				String vname = vit.getName().getIdentifier();
				if (vname != null) {
					mSymbTab.put(vname, typeName);
					//lineBuf.append(vname);
				}
				Expression exp = vit.getInitializer();
				// Three 25 types expression, just for ClassInstance Express
				if (exp != null) {
					//lineBuf.append(" = ");
					switch (exp.getNodeType()) {
					case ASTNode.METHOD_INVOCATION:
						break;
					case ASTNode.CLASS_INSTANCE_CREATION:
						break;
					default:
					//	lineBuf.append(exp.toString());
						break;
					}
				}
				//icf.setSrcLines(sline, lineBuf);
				//lineBuf = new StringBuilder();
			}
		}
		//icf.setSrcLines(sline, lineBuf);
		return true;
	}

	/**
	 * 12/25 add - comment public boolean visit(VariableDeclarationFragment vdf) {
	 * String vname = vdf.getName().getIdentifier(); StringBuilder lineBuf = new
	 * StringBuilder(); int sline = getLineNum(vdf); if (vname != null) {
	 * lineBuf.append(vname); } icf.setSrcLines(sline,lineBuf); lineBuf = new
	 * StringBuilder(); Expression exp = vdf.getInitializer(); // Three 25 types
	 * expression, just for ClassInstance Express if (exp !=null) {
	 * lineBuf.append(" = "); logger.finer("Expression
	 * Type==>:"+exp.getNodeType()+" "+exp+"
	 * "+getClassName(exp)+getClassTag(exp)); sline = getLineNum(exp); switch
	 * (exp.getNodeType()){ case ASTNode.STRING_LITERAL:
	 * lineBuf.append(exp.toString()); break; default: break ; }
	 * icf.setSrcLines(sline,lineBuf); } return true; }
	 */
	/*
	 * Regist variable in Catchclause
	 */
	public boolean visit(SingleVariableDeclaration vd) {
		if (vd != null) {
			logger.entering("IndexJavaMethodVisitor",
					"SingleVariableDeclaration", vd);
			mSymbTab.put(vd.getName().getIdentifier(), getFullTypeName(vd
					.getType(), clsPkgTab));
		}
		logger.finer("End SingleVariableDec" + vd);
		return true;
	}

	/**
	 * ignore inner classes for the moment TODO:
	 */
	public boolean visit(TypeDeclaration td) {
		// we are loosely calling all non-first classes as inner class (bad
		// naming)
		logger.log(Level.INFO, "Met an inner class: " + td.getName());
		td.accept(new IndexInnerClassFileVisitor(icf));
		return true;
	}

	// this should return all id names, including names of method and class
	public boolean visit(SimpleName nm) {
		String id = nm.getIdentifier();
		icf.addCodeText(id);
		imtd.addCodeText(id);
		return true;
	}

	// By doing so, both qualified name and its constituting simple names are
	// also indexed.
	// This may not be necessary (to be examined for its benefits)
	public boolean visit(QualifiedName qm) {
		String qid = qm.getFullyQualifiedName();
		icf.addCodeText(qid);
		imtd.addCodeText(qid);
		return true;
	}

	public boolean visit(StringLiteral sl) {
		String s = sl.getLiteralValue();
		icf.addCodeText(s);
		imtd.addCodeText(s);
		return true;
	}

	// If we don't redefine this, it will cause stack overflow
	public boolean visit(MethodDeclaration md) {
		return true;
	}

	/*
	 * @return The head spaces of ASTnode @input programText position
	 */
	private int getSpaces(ASTNode astNode) {
		int num = 0;
		if (astNode != null) {
			int pst = astNode.getStartPosition();
			int sline = rct.getLOC(pst);
			List loclst = rct.getLocList();
			if (icf.getSrcLine(sline) == null) {
				num = pst - ((Integer) loclst.get(sline - 1)).intValue();
				// System.out.println("sline Space ==>"+sline + ":"+
				// astNode.getNodeType()+" "+ num);
			}
		}
		return num;
	}

	private StringBuilder getHSpace(ASTNode astNode) {
		int num = 0;
		StringBuilder hstr = new StringBuilder();
		if (astNode != null) {
			int pst = astNode.getStartPosition();
			int sline = rct.getLOC(pst);
			List loclst = rct.getLocList();
			if (icf.getSrcLine(sline) == null) {
				num = pst - ((Integer) loclst.get(sline - 1)).intValue();
				hstr.append(icf.getProgramText(), pst - num, num);
				// System.out.println("sline Space ==>"+sline + ":"+
				// astNode.getNodeType()+" "+ hstr);
			}
		}
		return hstr;
	}

	// parameter type solution
	public String getTypeName(String cls) {
		String tcls = "";
		if ((mSymbTab != null) && mSymbTab.containsKey(cls)) {
			tcls = mSymbTab.get(cls);
		} else {
			if ((cSymbTab != null) && cSymbTab.containsKey(cls)) {
				tcls = cSymbTab.get(cls);
			}

		}
		if (tcls == "") {
			String pkName = getPkgName(cls);
			if (pkName != "") {
				tcls = pkName + cls;
			} else {
				//tcls = UNKNOWN + "." + cls;
				tcls = cls;
			}
		}
		return tcls;

	}

	//
	private String getPkgName(String cls) {
		String pName = "";
		if (clsPkgTab != null) {
			if (clsPkgTab.containsKey(cls)) {
				pName = clsPkgTab.get(cls);
			} else {
				logger.finer("getPkgName==>" + cls);
				String[] majorName = cls.split("<", 0);
				if (majorName.equals(cls)) {
					majorName = cls.split("[", 0);
				}
				if ((majorName.length > 1)
						&& clsPkgTab.containsKey(majorName[0])) {
					pName = clsPkgTab.get(majorName[0]);
				}
			}
		}
		if (pName != "") {
			pName = pName + ".";
		}
		return pName;
	}

	// Return type expanding express if expression is a simplename
	public String getClassName(Expression expr) {
		int nodeType = 0;
		boolean fullName = false;
		String cls = "";
		if (expr != null) {
			nodeType = expr.getNodeType();
			//logger.finer("ASTNode=" + nodeType + "   ==>" + expr.toString());
			//logger.info("ASTNode=" + nodeType + "   ==>" +expr.getLength());
			switch (nodeType) {
			case ASTNode.QUALIFIED_NAME:
				String qedName = ((QualifiedName) expr).getName()
						.getIdentifier();
				String qerName = getClassName(((QualifiedName) expr)
						.getQualifier());
				cls = qerName + "." + qedName;
				break;
			case ASTNode.SIMPLE_TYPE:
				// cls = ((SimpleType) expr).getName();
				cls = expr.toString();
				break;
			case ASTNode.SIMPLE_NAME:
				cls = getTypeName(((SimpleName) expr).getIdentifier());
				fullName = true;
				break;
			case ASTNode.METHOD_INVOCATION:
				Expression cexpr = ((MethodInvocation) expr).getExpression();
				//cls = getClassName(cexpr) + expr.toString();
				cls = getClassName(cexpr) ;
				fullName = true;
				break;
			case ASTNode.CLASS_INSTANCE_CREATION:
				cls = ((ClassInstanceCreation) expr).getType().toString();
				break;
			case ASTNode.ARRAY_ACCESS:
				cls = getTypeName(((ArrayAccess) expr).getArray().toString());
				if (cls.replace("[]", "").equals(cls)) {
					cls = getPkgName(cls) + cls + "[]";
				} else {
					cls = getPkgName(cls.replace("[]", "")) + cls;
				}
				fullName = true;
				break;
			default:
				cls = expr.toString();
				break;
			}
			if (!fullName) {

				cls = getPkgName(cls) + cls;
			}
		} else { // It is the current class
			cls = icf.getPackageName() + "." + icf.getClassName();
		}
		return cls;
	}

	// Return type tag for Expression
	public String getClassTag(Expression expr) {
		int nodeType = 0;
		boolean fullName = false;
		String cls = "";
		if (expr != null) {
			nodeType = expr.getNodeType();
			logger.finer("ASTNode=" + nodeType + "==>" + expr.toString());
			switch (nodeType) {
			case ASTNode.QUALIFIED_NAME:
				String qedName = ((QualifiedName) expr).getName()
						.getIdentifier();
				String qerName = getClassTag(((QualifiedName) expr)
						.getQualifier());
				cls = qerName + "." + qedName;
				break;
			case ASTNode.SIMPLE_TYPE:
				// cls = ((SimpleType) expr).getName();
				cls = expr.toString();
				break;
			case ASTNode.SIMPLE_NAME:
				cls = getTypeName(((SimpleName) expr).getIdentifier());
				fullName = true;
				break;
			case ASTNode.METHOD_INVOCATION:
				Expression cexpr = ((MethodInvocation) expr).getExpression();
				// cls = getClassName(cexpr)+expr.toString();
				cls = getClassTag(cexpr);
				fullName = true;
				break;
			case ASTNode.CLASS_INSTANCE_CREATION:
				cls = ((ClassInstanceCreation) expr).getType().toString();
				break;
			case ASTNode.ARRAY_ACCESS:
				cls = getTypeName(((ArrayAccess) expr).getArray().toString());
				if (cls.replace("[]", "").equals(cls)) {
					cls = getPkgName(cls) + cls + "[]";
				} else {
					cls = getPkgName(cls.replace("[]", "")) + cls;
				}
				fullName = true;
				break;
			default:
				//cls = expr.toString();
				cls = UNKNOWN;
				break;
			}
			if (!fullName) {

				cls = getPkgName(cls) + cls;
			}
		} else { // It is the current class
			cls = icf.getPackageName() + "." + icf.getClassName();
		}
		return cls;
	}

	// paramType ::= package.cls
	public boolean isMethodInv(Object obj) {
		boolean isMI = false;
		Class objType = null;
		if (obj != null) {
			objType = obj.getClass();
			String objTypeName = objType.getName();
			String cls = obj.toString();
			String singleName = objTypeName.replace(
					"org.eclipse.jdt.core.dom.", "");
			logger.finer("getparamType ==>" + obj + " " + objType.getName()
					+ " " + singleName);
			if (singleName.equals("MethodInvocation")) {
				isMI = true;
			}
		}
		return isMI;
	}

	/*
	 * parameter maybe mothod invocation expression, no enjoy visiting
	 * methodInvocation in parameter
	 */
	public String getParamType(Object obj) {
		String cls = "";
		boolean fullName = true;
		Class objType = null;
		if (obj != null) {
			objType = obj.getClass();
			String objTypeName = objType.getName();
			cls = obj.toString();
			String singleName = objTypeName.replace(
					"org.eclipse.jdt.core.dom.", "");
			logger.finer("getparamType ==>" + obj + " " + objType.getName()
					+ " " + singleName);
			if (singleName.equals("NumberLiteral")) {
				cls = "java.lang.Number";
			} else if (singleName.equals("StringLiteral")) {
				cls = "java.lang.String";
			} else if (singleName.equals("MethodInvocation")) {
				Expression expr = ((MethodInvocation) obj).getExpression();
				cls = getClassName(expr);
			} else if (singleName.equals("ClassInstanceCreation")) {
				cls = ((ClassInstanceCreation) obj).getType().toString();
				fullName = false;
			} else if (singleName.equals("QualifiedName")) {
				String qedName = ((QualifiedName) obj).getName()
						.getIdentifier();
				String qerName = getClassName(((QualifiedName) obj)
						.getQualifier());
				cls = qerName + "." + qedName;
			} else if (singleName.equals("SimpleName")) {
				cls = ((SimpleName) obj).getIdentifier();

				String tcls = getTypeName(cls);

				if (!tcls.equals(cls)) {
					cls = tcls;
				}
				fullName = false;
			} else {
				// (cls) show debug info
				//cls = UNKNOWN + "(" + cls + ")";
				cls = UNKNOWN ;
			}
			if (!fullName) {
				cls = getPkgName(cls) + cls;
			}
		}
		return cls;
	}

	/**
	 *
	 * @return src line number of a AST NODE
	 */

	private int getLineNum(ASTNode node) {
		int sline = 0;
		if (node != null) {
			int pst = node.getStartPosition();
			sline = rct.getLOC(pst + 1);
		}
		return sline;
	}

	private int getNodeLines(ASTNode node) {
		int lines = 0;
		if (node != null) {
			int pst = node.getStartPosition();
			int lsz = node.getLength();
			lines = rct.countLines(pst + 1, lsz);
		}
		return lines;
	}

	private int getLineNum(Object obj) {
		int sline = 0;
		ASTNode node = (ASTNode) obj;
		if (node != null) {
			int pst = node.getStartPosition();
			sline = rct.getLOC(pst + 1);
		}
		return sline;
	}

}
