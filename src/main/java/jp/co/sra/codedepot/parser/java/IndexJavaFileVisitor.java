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

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.util.License;
import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.TokenParser;

import org.eclipse.jdt.core.Flags;

import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * Extract information that is needed to index the java program. Methods of this
 * class is called by JavaParser when each AST node is met.
 *
 * @author yunwen
 *
 */
public class IndexJavaFileVisitor extends ASTVisitor {

	private IndexedCodeFile icf;
	private LineRead rct;
	private static Logger logger = Logger.getLogger(IndexJavaFileVisitor.class
			.getName());
	private List commentList; // the list of comments (of type Comment) in the
	// program
	private Map<String, String> cSymbTab;
	private Map<String, String> mSymbTab;
	private Map<String, String> clsPkgTab;
	private Map<String, List> gclsTab;
	private List sloclst = new ArrayList();
	private List srcLines = new ArrayList();
	Boolean linkTypeSource = false;
	Boolean index = true;
	static String UNKNOWN = "_UNKNOWN";
	static String DEFAULT = "default";

	static {
		 logger.setLevel(Level.INFO);
	}

	public IndexJavaFileVisitor(IndexedCodeFile icf) {
		super();
		this.icf = icf;
	}

	public IndexJavaFileVisitor(IndexedCodeFile icf,
			Map<String, String> lClassTab, Map<String, List> gClassTab) {
		super();
		this.icf = icf;
		this.rct = icf.getLineRead();
		this.clsPkgTab = lClassTab;
		this.gclsTab = gClassTab;
		cSymbTab = new HashMap<String, String>();
		if (linkTypeSource) {
			sloclst = rct.getLocList();
			srcLines = rct.getSrcList();
		}

	}

	public LineRead getLineRead() {
		return rct;
	}

	/**
	 * Top level ast, the program file this should include all the comments in
	 * the file
	 */
	public boolean visit(CompilationUnit cu) {
		logger.finer("entering visit complication unit");
		PackageDeclaration pkg = cu.getPackage();
		StringBuilder lineBuf = new StringBuilder("");

		if (pkg != null) {
			icf.setPackageName(pkg.getName().getFullyQualifiedName());
			/*
			 * if(linkSource) { lineBuf.append("<span class=\"keyword\">package</span>
			 * <span class=\"pkg def\">");
			 * lineBuf.append(pkg.getName().getFullyQualifiedName());
			 * lineBuf.append("</span>;"); int sline = getLineNum(pkg);
			 * icf.setSrcLines(sline,lineBuf); }
			 */
		} else {
			icf.setPackageName(DEFAULT);
		}

		commentList = cu.getCommentList();
		icf.setAllCommentList(commentList);
		if (commentList.size() > 0) {
			setAndRemoveLicense();
		}
		Iterator iter = commentList.iterator();

		while (iter.hasNext()) {
			Comment c = (Comment) iter.next();
			icf.addComment(c.getStartPosition(), c.getLength());
		}

		if (linkTypeSource) { // it is false always now.

			iter = commentList.iterator();
			while (iter.hasNext()) {
				Comment c = (Comment) iter.next();
				icf.addComment(c.getStartPosition(), c.getLength());
				int pst = c.getStartPosition();
				int lsz = c.getLength();
				// int sline = getLineNum(c);
				int sline = cu.getLineNumber(pst);
				// Call a function symTostr which translate html symbol to
				// string
				String comStr = "";
				if (c.isLineComment()) {
					lineBuf = new StringBuilder("");
					lineBuf.append("<span class=\"cmt\">");
					if (icf.getSrcLine(sline) == null) {
						lineBuf.append(srcLines.get(sline - 1));
					} else {
						lineBuf.append(icf.getProgramText(), pst, lsz);
					}
					lineBuf.append("</span>");
					icf.addCommentLines(sline, lineBuf);
					lineBuf = new StringBuilder();
				} else {
					int mline = getNodeLines(c);
					int offset = 0;
					lineBuf = new StringBuilder();
					for (int i = 1; i <= mline; i++) {
						if (!rct.isEmptyLine(sline)) {
							lineBuf.append("<span class=\"cmt\">");
							comStr = symToStr((String) srcLines.get(sline - 1));
							lineBuf.append(comStr);
							lineBuf.append("</span>");
							icf.addCommentLines(sline, lineBuf);
							lineBuf = new StringBuilder();
						}
						sline = sline + 1;
					}
				}
			}

		}
		return true;
	}

	private String symToStr(String str) {
		return str.replaceAll("<", "&lt;");
	}

	// this should return all id names, including names of method and class
	public boolean visit(SimpleName nm) {
		icf.addCodeText(nm.getIdentifier());
		return true;
	}

	// By doing so, both qualified name and its constituting simple names are
	// also indexed.
	// This may not be necessary (to be examined for its benefits)

	public boolean visit(QualifiedName qm) {
		logger.entering("FileVisitor", "visitQualifiedName", qm
				.getFullyQualifiedName());
		icf.addCodeText(qm.getFullyQualifiedName());
		return true;
	}

	public boolean visit(StringLiteral sl) {
		icf.addCodeText(sl.getLiteralValue());
		return true;
	}

	/**
	 * <Field , Type> are registed in a Class Symbolic table
	 *
	 */

	/*
	 * Regist types used in program
	 */
	public boolean visit(ImportDeclaration id) {
		if (id.getName().isQualifiedName()) {
			QualifiedName idName = (QualifiedName) id.getName();
			String pname = idName.toString();
			String cname = idName.getName().getIdentifier();
			logger.finer("ImportDeclaration: id =" + pname + " ");
			if (gclsTab.containsKey(pname)) {
				List extcls = gclsTab.get(pname);
				// logger.finer("ImportDeclaration: extcls ="+extcls+" " );
				if (extcls != null) {
					for (Iterator it = extcls.iterator(); it.hasNext();) {
						clsPkgTab.put((String) it.next(), pname);
					}
				}
			} else {
				pname = idName.getQualifier().toString();
				clsPkgTab.put(cname, pname);
				logger.finer("ImportDeclaration : " + cname + "  " + pname);
			}
		} else {
			logger.finer("ImportDeclaration : " + id.getName() + "isSimple");
			clsPkgTab.put(((SimpleName) id.getName()).getIdentifier(), id
					.getName().toString());
		}
		// process for srchmtl
		if (linkTypeSource) {
			int sline = getLineNum(id);
			StringBuilder lineBuf = new StringBuilder("");
			lineBuf
					.append("<span class=\"keyword\">import</span> <span class=\"pkg use\">");
			lineBuf.append(id.getName());
			lineBuf.append("</span>;");
			icf.setSrcLines(sline, lineBuf);
		}
		return true;
	}

	/**
	 * ignore inner classes for the moment TODO:
	 */
	public boolean visit(TypeDeclaration td) {
		logger.entering("IndexJavaFileVisitor", "visitTypeDeclaration",
						"ommit");
		icf.createNewClass();
		IndexedClass icls = icf.getCurrentIndexedClass();
		String clsName = td.getName().getIdentifier();
		icls.setClassName(clsName);

		/*
		 * multiClass is processed if (innerClass) { // we are loosely calling
		 * all non-first classes as inner class (bad // naming)
		 * logger.log(Level.INFO, "Met an inner class: " + td.getName());
		 * td.accept(new IndexInnerClassFileVisitor(icf)); return true; }
		 */
		/*
		if (!td.isPackageMemberTypeDeclaration()) {
			// we are loosely calling all non-first classes as inner class (bad
			// naming)
			logger.log(Level.INFO, "Met an inner class: " + td.getName());
			td.accept(new IndexInnerClassFileVisitor(icf));
			return true;
		}
		*/
		logger.finer("Enter ClassVisitor from " + td.getName());
		td.accept(new IndexJavaClassVisitor(icf, rct, cSymbTab, mSymbTab,
				clsPkgTab));
		logger.finer("Back to TypeDeclaration" + td.getName());
		int docLen = 0;
		int docLines = 0;
		Javadoc docComment = td.getJavadoc();
		if (docComment != null) {
			docLen = docComment.getLength();
			docLines = getNodeLines(docComment);
			logger.finer("TypeDeclaration Debug==>" + docComment.getStartPosition());
			icls.setComment(docComment.getStartPosition(), docLen);
		}

		if (td.getModifiers() == Modifier.PUBLIC) {
			icls.setComment(new StringBuilder(icf.getComments()));

		} else {
			logger.finer("the other Class " + td.getName().getIdentifier());
			if (docComment != null) {
				icls.setComment(docComment.getStartPosition(), docLen);
			}
			//doing
			int mstart = td.getStartPosition()+docLen;
			int mlen = td.getLength();

			logger.finer("  docStartt: endlines = "+ mstart +":"+mlen);
			List commentList = new ArrayList<Comment>();
			commentList = icf.getAllCommentList();
			int i = 0;
			int cstart = 0,cend=0;
			for (i = 0; i < commentList.size(); i++) {
				Comment c = (Comment) commentList.get(i);
				cstart = c.getStartPosition();
				cend = cstart+c.getLength();
				logger.finer("cstart " + cstart + " cend "
						+ cend + " Method start ="+mstart + " method len=" + mlen);
				if ((cstart >= mstart) && (cstart < (mstart+mlen))) {
					icls.setComment(cstart,c.getLength());
					logger.finer("from " + c.getStartPosition() + " to "
							+ c.getLength());
				}
			}
			//ending
		}

		// icf.setClassName(td.getName().getIdentifier());
		// icls.setClassName(td.getName().getIdentifier());

		if (icf.getPackageName() != "") {
			clsPkgTab.put(td.getName().getIdentifier(), icf.getPackageName());

		}
		// icf.setSrc(td.getStartPosition(), td.getLength());
		icls.setSrc(td.getStartPosition(), td.getLength());
		// start line number is set
		int sline = getLineNum(td);
		int endLine = getNodeLines(td) + sline - 1;

		StringBuilder clsdef = new StringBuilder();
		clsdef.append("<span class=\"cls\" id=\"");
		clsdef.append(HTMLConvert.convertIntoHTML(clsName));
		clsdef.append(icf.getUuid());
		clsdef.append("\">");
		int clsStart = sline + docLines ;
		icf.setclsdef(clsStart, clsdef);
		logger.finer("icf.setclsdef=> " +clsStart +":"+sline+ " startAt" + clsdef + "," + endLine);
		icf.setclsend(endLine);
		logger.finer("Class start end line =>" +clsName + " startAt" + clsStart + "," + endLine);
		icls.setStart(sline);

		// TODO linkSource are going to be for type link

		// innerClass = true;
		return true;
	}

	/*
	 * ; TokenNameSEMICOLON It is useful detail info
	 *
	 */
	private int[] getOperatorPosition(Expression expression, char[] source) {
		if (expression instanceof InstanceofExpression) {
			IScanner scanner = ToolFactory.createScanner(false, false, false,
					false);
			scanner.setSource(source);
			int start = expression.getStartPosition();
			int end = start + expression.getLength();
			// scanner.resetTo(start, end);
			int token;
			try {
				while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
					switch (token) {
					case ITerminalSymbols.TokenNameinstanceof:
						return new int[] {
								scanner.getCurrentTokenStartPosition() + start,
								scanner.getCurrentTokenEndPosition() + start };
					}
				}
			} catch (InvalidInputException e) {
			}
		}
		return null;
	}

	/**
	 * @return the position of block'{
	 */
	private int[] getLBRACEPosition(Block td, char[] source) {
		if (td instanceof Block) {
			IScanner scanner = ToolFactory.createScanner(false, false, false,
					false);
			// IScanner scanner = ToolFactory.createScanner(false, true, true,
			// true);
			scanner.setSource(source);
			int start = td.getStartPosition();
			int end = start + td.getLength();
			// scanner.resetTo(start, end);
			int token;
			try {
				while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
					switch (token) {
					case ITerminalSymbols.TokenNameLBRACE:
						return new int[] {
								scanner.getCurrentTokenStartPosition() + start
										+ 1,
								scanner.getCurrentTokenEndPosition() + start
										+ 1 };

					}

				}
			} catch (InvalidInputException e) {
			}
		}
		return null;
	}

	/**
	 * @return the position of Method Declaration'{
	 */
	private int[] getLBRACEPosition(MethodDeclaration td, char[] source) {
		if (td instanceof MethodDeclaration) {
			IScanner scanner = ToolFactory.createScanner(false, false, false,
					false);
			// IScanner scanner = ToolFactory.createScanner(false, true, true,
			// true);
			scanner.setSource(source);
			int start = td.getStartPosition();
			int end = start + td.getLength();
			// scanner.resetTo(start, end);
			int token;
			try {
				while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
					switch (token) {
					case ITerminalSymbols.TokenNameLBRACE:
						return new int[] {
								scanner.getCurrentTokenStartPosition() + start
										+ 1,
								scanner.getCurrentTokenEndPosition() + start
										+ 1 };

					}

				}
			} catch (InvalidInputException e) {
			}
		}
		return null;
	}

	/**
	 * return fully qualified name if possible, simple name if not
	 *
	 * @param t
	 * @return
	 */


	public String getTypeName(Type t) {
		if (t == null) {
			return ("");
		}
		ITypeBinding tb = t.resolveBinding();
		if (tb != null) {
			return tb.getQualifiedName();
		} else {
			logger.finer("   Cannot resolve the name of type: " + t.toString());
			return t.toString();
		}
	}
	public String getFullTypeName(Type type, Map<String, String> aclassTab) {

		String typeName = type.toString();
		String fullName = "";
		Map<String, String> classTab = aclassTab;
		logger.entering("IndexJavaFileVisitor", "getFullTypeName", typeName);
		if (type.isSimpleType()) {
			typeName = ((SimpleType) type).getName().getFullyQualifiedName();
			if (classTab != null && classTab.containsKey(typeName)) {
				logger.finer("getFullTypeName: isSimpleType " + fullName + type
						+ " classTab size" + classTab.size());
				fullName = classTab.get(typeName);

			}
		} else if (type.isQualifiedType()) {
			logger.finer("getFullTypeName: isQualifiedType " + type);
			typeName = type.toString();
		} else if (type.isArrayType()) {
			logger.finer("getFullTypeName: isArrayType " + fullName + "."
					+ typeName);
			fullName = getFullPkgName(((ArrayType) type).getComponentType(),
					classTab);

		} else if (type.isParameterizedType()) {
			logger.finer("getFullTypeName: isParameterType " + fullName + "."
					+ typeName);
			fullName = getFullPkgName(((ParameterizedType) type).getType(),
					classTab);

		} else if (type.isWildcardType()) {
			logger.finer("getFullTypeName: isWildcardType " + type);
			typeName = type.toString();
		} else if (type.isPrimitiveType()) {
			return typeName;
		}
		if (fullName != "") {
			typeName = fullName + "." + typeName;
		}
		logger.finer("End getFullTypeName : " + typeName);
		return typeName;

	}

	public String getFullPkgName(Type type, Map<String, String> classTab) {
		String fullName = "";
		String typeName = type.toString();
		if (type.isSimpleType()) {
			typeName = ((SimpleType) type).getName().getFullyQualifiedName();
		}
		if (classTab != null && classTab.containsKey(typeName)) {
			fullName = classTab.get(typeName);
		}
		return fullName;
	}

	/*
	 * TODO 20081219 public String getFullPkgName(Type type){ String fullName =
	 * ""; String typeName = type.toString(); if (type.isSimpleType() ) {
	 * typeName =((SimpleType)type).getName().getFullyQualifiedName(); } if
	 * (classTab != null && classTab.containsKey(typeName)) { fullName =
	 * classTab.get(typeName) ; } else {//GlobaTab } return fullName; }
	 */

	/**
	 *
	 * @return src line number of a AST NODE normal line number 1 ..
	 */
	private int getLineNum(ASTNode node) {
		int sline = 0;
		LineRead rct = null;
		if (node != null) {
			int pst = node.getStartPosition();
			rct = getLineRead();
			sline = rct.getLOC(pst + 1);
		}
		return sline;
	}

	private int getNodeLines(ASTNode node) {
		int lines = 0;
		if (node != null) {
			int pst = node.getStartPosition();
			int lsz = node.getLength();
			lines = rct.countLines(pst, lsz);
		}
		return lines;
	}

	/**
	 * Remove license statement from the list of comments, using heuristic
	 * method Assumption: there is only one comment block that states its
	 * license. Afther this is removed, we stop processing the rest to improve
	 * performance. Notice, commentList is changed in place.
	 */
	void setAndRemoveLicense() {
		String license = "";
		List tmp = new ArrayList<Comment>();
		int i = 0;
		for (i = 0; i < commentList.size(); i++) {
			Comment c = (Comment) commentList.get(i);
			logger.finer("from " + c.getStartPosition() + " to "
					+ c.getLength());
			String commentString = new String(icf.getProgramText(), c
					.getStartPosition(), c.getLength());
			license = License.getLicense(commentString);
			if (license.length() > 0) {
				break;
			}
		}
		icf.setLicense(license);
		logger.finer("end set and move");
	}

}
