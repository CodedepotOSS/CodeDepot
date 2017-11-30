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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.util.LineRead;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Comment;

public class IndexJavaClassVisitor extends IndexJavaFileVisitor {
	private static Logger logger =
	    Logger.getLogger(IndexJavaClassVisitor.class.getName());
	private IndexedClass icls;
	private IndexedCodeFile icf;
	private LineRead rct;
	private Map<String, String> cSymbTab;
	private Map<String, String> mSymbTab;
	private Map<String, String> clsPkgTab;
	private boolean linkTypeSource = false;
	static {
		// set this to higher level if not debugging
		logger.setLevel(Level.INFO);
	}

	public IndexJavaClassVisitor(IndexedCodeFile icf,
			LineRead rct,
			Map<String, String> cSymbTab,
			Map<String,String> mSymbTab,
			Map<String, String> clsPkgTab) {
		super(icf);
		this.cSymbTab = cSymbTab;
		this.mSymbTab = mSymbTab;
		this.clsPkgTab = clsPkgTab;
		this.icf = icf;
		this.icls = icf.getCurrentIndexedClass();
		this.rct = rct;
		logger.entering("In  ClassVisitor", "Constructor");
		// TODO 自動生成されたコンストラクター・スタブ
	}
	private String symToStr(String str) {
		return str.replaceAll("<", "&lt;");
	}

	/**
	 * <Field , Type> are registed in a Class Symbolic table
	 *
	 */
	public boolean visit(FieldDeclaration fd) {
		if (fd != null) {
			List fragment = fd.fragments();
			Type type = fd.getType();
			String typeName = type.toString();
			if (!type.isPrimitiveType()) {
				typeName = getFullTypeName(type, clsPkgTab);
			}
			for (Iterator it = fragment.iterator(); it.hasNext();) {
				VariableDeclarationFragment vit = (VariableDeclarationFragment) it
						.next();
				if (vit != null) {
					cSymbTab.put((String) vit.getName().getIdentifier(),
							typeName);
					logger.finer("FieldDeclaration -->"
							+ vit.getName().getIdentifier() + " , " + typeName);
				}
			}
			int line = rct.getLOC(fd.getStartPosition());
			logger.finer("FieldDeclarationtest==> " + line + " "
					+ rct.getSrcLine(line));

		}
		return true;
	}

	// this should return all id names, including names of method and class
	public boolean visit(SimpleName nm) {
		icls.addCodeText(nm.getIdentifier());
		return true;
	}

	// By doing so, both qualified name and its constituting simple names are
	// also indexed.
	// This may not be necessary (to be examined for its benefits)

	public boolean visit(QualifiedName qm) {
		logger.entering("FileVisitor", "visitQualifiedName", qm
				.getFullyQualifiedName());
		icls.addCodeText(qm.getFullyQualifiedName());
		return true;
	}

	public boolean visit(StringLiteral sl) {
		icls.addCodeText(sl.getLiteralValue());
		return true;
	}

	public boolean visit(MethodDeclaration md) {
		logger.entering("ClassVisitor","visitMethodDeclaration",md);
		int sline = getLineNum(md);   // method  start line
		int lines = getNodeLines(md); // total lines
		logger.finer("  start: total lines = "+ sline +":"+lines);

		String mtdName = md.getName().getIdentifier();
		mSymbTab = new ConcurrentHashMap<String, String>();

		if (md.isConstructor()) {
			logger.finer("  Ignore Constructor");
			return true;
		}

		icls.createNewMethod();
		IndexedMethod imtd = icls.getCurrentIndexedMethod();
		imtd.setMethodName(mtdName);
		Javadoc docComment = md.getJavadoc();
		int docLen = 0;
		int docLines = 0 ;
		if (docComment != null) {
			docLen = docComment.getLength();
			docLines = getNodeLines(docComment);
			imtd.setComment(docComment.getStartPosition(),docLen);
		}
		int endLine = sline+ lines -1;
		// startLine is the line excluded javaDoc lines
		int startLine = sline + docLines;
		logger.finer("  mstart: mendlines = "+ startLine +":"+endLine);
		int mstart = md.getStartPosition()+docLen;
		int mlen = md.getLength()-docLen+1;

		logger.finer("  docStartt: endlines = "+ mstart +":"+mlen);
		List commentList = new ArrayList<Comment>();
		commentList = icf.getAllCommentList();
		int i = 0;
		int cstart = 0;
		for (i = 0; i < commentList.size(); i++) {
			Comment c = (Comment) commentList.get(i);
			cstart = c.getStartPosition();
			logger.finer("cstart " + cstart + " clen "
					+ c.getLength() + " Method start ="+mstart + " method len=" + mlen);
			if ((cstart > mstart) && (cstart < (mstart+mlen))) {
				imtd.setComment(cstart,c.getLength());
				logger.finer("cstart " + cstart + " clen "
						+ c.getLength() + " Method start ="+mstart + " method len=" + mlen);
			}
		}
		Type retType = md.getReturnType2();
		imtd.setReturnType(getTypeName(retType));
		String clsName = icls.getClassName();

		imtd.setClassName(clsName);
		imtd.setSrc(md.getStartPosition(), md.getLength());

		/*
		 * StringBuilder lineBuf = new StringBuilder();
		 */
		StringBuilder fdef = new StringBuilder();
		fdef.append("<span class=\"fdef\" id=\"");
		fdef.append(HTMLConvert.convertIntoHTML(clsName));
		fdef.append("#");
		fdef.append(HTMLConvert.convertIntoHTML(mtdName));
		fdef.append("(");

		/* linkSource
		if (retType != null) {
			int pst = retType.getStartPosition();
			sline = rct.getLOC(pst + 1);
			logger.finer("MethodDecl pst " + sline + " " + pst);
		}

		List mdfs = md.modifiers();
		List thrownExcps = md.thrownExceptions();

		StringBuilder lineBuf = new StringBuilder();

		lineBuf.append("<span class=\"keyword\">");
		for (Object mdf : mdfs) {
			lineBuf.append(mdf.toString());
			lineBuf.append(" ");
		}
		lineBuf.append("</span>");
		lineBuf.append("<span class=\"ret typ\">" + retType + " </span>");
		lineBuf.append("<span class=\"mtd def\">");
		lineBuf.append(imtd.getMethodName());
		lineBuf.append("</span>");
		icf.setSrcLines(sline, lineBuf);

		lineBuf = new StringBuilder();
		logger.finer("MethodDecl lineBuf==>" + sline + " " + lineBuf);
	    */
		List<SingleVariableDeclaration> params = md.parameters();
		if (!params.isEmpty()) {
			/*
			if (linkTypeSource) {
				sline = getLineNum(params.get(0));
				lineBuf.append("(");
				icf.setSrcLines(sline, lineBuf);
				lineBuf = new StringBuilder();
			}
			*/
			int flg = 0;
			for (SingleVariableDeclaration vd : params) {
				String ptype = getTypeName(vd.getType());
				//get extra dimension int[] x[], the array numbers after x comes from
				//extra dimensions
				int extraDimension = vd.getExtraDimensions();
				for (i = 0; i<extraDimension; i++) {
					ptype += "[]";
				}
				imtd.addInputType(ptype);
				fdef.append(HTMLConvert.convertIntoHTML(ptype));
				fdef.append(" ");

				// parameter names are not added because they will be added
				// when they are used in the body.

				mSymbTab.put(vd.getName().getIdentifier(), getFullTypeName(vd
						.getType(), clsPkgTab));
				logger.finer("mSymbTab: "
						+ getFullTypeName(vd.getType(), clsPkgTab) + ", Name= "
						+ vd.getName());
				/*
				if (linkTypeSource) {
					sline = getLineNum(vd);

					if (flg > 0) {
						lineBuf.append(", ");
					}
					lineBuf.append(getFullTypeName(vd.getType(), clsPkgTab));
					lineBuf.append(" ");

					lineBuf.append(vd.getName());

					icf.setSrcLines(sline, lineBuf);
					lineBuf = new StringBuilder();
					flg++;
				}
				*/
			}
			//lineBuf.append(")");
			fdef.replace(fdef.length()-1, fdef.length()-1, ")"+icf.getUuid()+"\">");

		} else {
			//lineBuf.append("()");
			fdef.append(")"+icf.getUuid()+"\">");
		}
		/*
		if (!thrownExcps.isEmpty()) {
			lineBuf.append(" throws ");
			for (Object thrown : thrownExcps) {
				lineBuf.append(thrown.toString());
				lineBuf.append(" ");
			}
		}
		Block block = md.getBody();
		int[] lbrace = null;
		if (block != null) {
			lbrace = getLBRACEPosition(block, block.toString().toCharArray());
		} else {
			lbrace = getLBRACEPosition(md, md.toString().toCharArray());
		}
		if (lbrace != null) {
			int braceln = rct.getLOC(lbrace[0]);

			if (sline == braceln) {
				lineBuf.append(" { ");
			}

		} else {
			lineBuf.append(" { ");
		}
		icf.setSrcLines(sline, lineBuf);
		*/

		logger.finer("MethodDecl fdef==<" + startLine + ", " + endLine +">"+ fdef);

		// startLine is the line excepted javadoc, sline is ast start line.
		icf.setfdef(startLine,fdef);
		icf.setfdefend(endLine);
		//method start line is set
		//imtd.setstart(startLine);
		imtd.setstart(sline);

		md.accept(new IndexJavaMethodVisitor(icf, icls, rct,cSymbTab, mSymbTab,clsPkgTab));
		return true;
	}

	/*
	 * Regist types used in program
	 */

	/**
	 * ignore inner classes for the moment TODO:
	 */

	public boolean visit(TypeDeclaration td) {
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
			lines = rct.countLines(pst, lsz);
		}
		return lines;
	}



}


