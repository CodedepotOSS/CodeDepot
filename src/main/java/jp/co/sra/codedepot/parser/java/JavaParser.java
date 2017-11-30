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

import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;

import jp.co.sra.codedepot.solr.ProgramUID;
import jp.co.sra.codedepot.util.java.GlobalClassTab;
import jp.co.sra.codedepot.util.java.LocalClassTab;
import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.TokenParser;
import jp.co.sra.codedepot.util.ProgrammingLanguageFilenameFilter;
import jp.co.sra.codedepot.util.UniversalReader;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;


/**
 * Parse a java program file, using Eclipse ASTParser. It calls IndexJavaVisitor
 * to deal with each AST node.
 *
 * Each method is added as an indexed document, with the type of
 * Constants.MTD_TYPE The class is also added as an indexed document, with the
 * type of Constants.CLS_TYPE
 *
 * TODO: we assume each file only has one class
 *
 * @version $Id: JavaParser.java 2342 2017-11-09 05:36:32Z fang $
 * @author yunwen
 *
 */
public class JavaParser extends Parser {

	// private ASTParser parser;
	// private IndexedCodeFile indexedCodeFile;
	private Map<String, List> pkgClsTab;
	private Map<String, String> clsPkgTab;
	private GlobalClassTab gclsTab ;
	private LocalClassTab lclsTab ;
	private boolean debug = true;

  	private static Logger logger = Logger.getLogger(JavaParser.class.getName());
	private static String languageName = new String("java");

	private static ProgrammingLanguageFilenameFilter javaFnFilter
		 = new ProgrammingLanguageFilenameFilter("java");
	static {
		logger.setLevel(Level.INFO);
	}

	@Override
	public String getLanguageName() {
		return languageName;
	}

	@Override
	public boolean accept(java.io.File file) {
		return javaFnFilter.accept(file);
	}

	@Override
	public String toHtml(IndexedCode ic) {
		IndexedCodeFile icf;
		try {
			icf = (IndexedCodeFile)ic;
			return icf.toHTML();
		} catch (ClassCastException e) {
			return null;
		}
	}

	public JavaParser(File globalTabFile, File localTabFile) {
		// parser = ASTParser.newParser(AST.JLS3);
		// parser.setResolveBindings(true);
		// parser.setProject(null);

		// Set up globalClass, localClass table for javaparser
		// add args localClassTab and GlobalClassTab to
		// IndexJavaFileVisitor constructor
		// ast.accept(new IndexJavaFileVisitor(indexedCodeFile));

		// The following tables should be in Indexer program
		// GlobalClassTab gTab = new

		//GlobalClassTab gTab = new GlobalClassTab("data/globalClass.data");
		// Get <package, <classList>>
		//pkgClsTab = gTab.getClassTab();

		gclsTab = new GlobalClassTab(globalTabFile.getAbsolutePath());
                try {
                        gclsTab.readClassTab();
                } catch (IOException e) {
                        logger.warning("Cannot read file: " + gclsTab.getFilename());
                } catch (ClassNotFoundException e) {
                        logger.warning("Cannot load file: " + gclsTab.getFilename());
                }
		pkgClsTab = gclsTab.getClassTab();

		lclsTab = new LocalClassTab(localTabFile.getAbsolutePath());
		try {
			lclsTab.readClassTab();
                } catch (IOException e) {
                        logger.warning("Cannot read file: " + lclsTab.getFilename());
                } catch (ClassNotFoundException e) {
                        logger.warning("Cannot load file: " + lclsTab.getFilename());
                }
		clsPkgTab = lclsTab.getClassTab();
	}

	@Override
	public IndexedCodeFile parse(String fid, String uuid, File f) throws Exception {
		// char[] programText;
		// int len = (int) f.length(); // todo: how to deal with file that is
		// longer than int
		// programText = new char[len];
		IndexedCodeFile indexedCodeFile = null;

		try {
			// BufferedReader reader = new BufferedReader(new FileReader(f));
			// int n = reader.read(programText, 0, len);
			// progInnerFile = new ProgInnerFile();

			String contents = UniversalReader.getContents(f);
			char[] programText = contents.toCharArray();
			Map<String, String> options
			    = new HashMap<String, String>();
			options.put(JavaCore.COMPILER_SOURCE,"1.5");
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setCompilerOptions(options);
			parser.setResolveBindings(true);
			parser.setSource(programText);
			CompilationUnit ast = (CompilationUnit) parser.createAST(new NullProgressMonitor());

			indexedCodeFile = new IndexedCodeFile();
			indexedCodeFile.setId(fid);
			indexedCodeFile.setUuid(uuid);
			indexedCodeFile.setProgramText(programText);
			indexedCodeFile.setLocation(f.getAbsolutePath());

			LineRead rct = new LineRead(programText);
			List<String> srclst = rct.getSrcList();
			List<Integer> loclst = rct.getLocList();

			indexedCodeFile.setLineRead(rct);

			// Set keyword info on IndexedCodeFile
			TokenParser tp = new TokenParser(ast, programText);
			ArrayList<int[]> keywordList = tp.getKeywordPosition();
			HashMap<Integer, HashMap> lnKeywordList;
			lnKeywordList = getLineKeywordList(ast, programText, keywordList);
			indexedCodeFile.setKeywordList(lnKeywordList);
			ArrayList<int[]> commentListPos = tp.getCommentPosition();
			HashMap commentList = getCommentLines(ast, programText,
					commentListPos);
			indexedCodeFile.setCommentList(commentList);
			// Prepared for registering local <class,package>

			ast.accept(new IndexJavaFileVisitor(indexedCodeFile, clsPkgTab, pkgClsTab));
			lclsTab.setClassTab(clsPkgTab);
			/*
			 * if (clsPkgTab.size() > 0) { logger.finer("JavaParser
			 * clsPkgTab.size()="+clsPkgTab.size()); writeClsPkgTab(clsPkgTab); }
			 */
			// reader.close();

		} catch (IOException e) {
			logger.warning("Error in reading file " + f.getAbsolutePath());
		}
		return indexedCodeFile;
	}

	@Override
	public void close() {
		if (lclsTab != null) {
			try {
				lclsTab.writeClassTab();
                	} catch (IOException e) {
                        	logger.warning("Cannot save file: " + lclsTab.getFilename());
                	}
		}
	}

	private HashMap getCommentLines(CompilationUnit ast, char[] programText,
			ArrayList<int[]> commentListPst) {
		HashMap commentList = new HashMap();
		if (commentListPst != null) {
			int sline, eline = 0;
			int[] cmtPst;
			int column = 0;
			for (int i = 0; i < commentListPst.size(); i++) {
				cmtPst = commentListPst.get(i);
				sline = ast.getLineNumber(cmtPst[0]);
				eline = ast.getLineNumber(cmtPst[1] - 1);
				column = ast.getColumnNumber(cmtPst[0]);
				commentList.put(new Integer(sline), new Integer(column));
				while (eline - sline > 0) {
					sline = sline + 1;
					commentList.put(new Integer(sline), new Integer(0));
				}
			}
		}
		return commentList;
	}

	private HashMap<Integer, HashMap> getLineKeywordList(CompilationUnit ast,
			char[] programText, ArrayList<int[]> keywordList) {

		HashMap<Integer, HashMap> lnKeywordList;
		HashMap<Integer, String> clKeywords; // keywords are in the same line

		lnKeywordList = new HashMap<Integer, HashMap>();

		int sline = 0;
		/*
		 * Translate token position info to srcline's
		 */
		if (keywordList != null) {
			// clKeywords = new HashMap<Integer,String>();
			clKeywords = new HashMap<Integer, String>();
			HashMap<Integer, String> tmpMap = new HashMap<Integer, String>();

			for (int j = 0; j < keywordList.size(); j++) {
				int kln = 0;
				int[] kwdpst = keywordList.get(j);
				StringBuilder buf = new StringBuilder();
				buf.append(programText, kwdpst[0], kwdpst[1] - kwdpst[0]);
				sline = ast.getLineNumber(kwdpst[0]);
				int column = ast.getColumnNumber(kwdpst[0]);
				Integer lnKey = new Integer(sline);

				if (lnKeywordList.containsKey(lnKey)) {
					tmpMap = new HashMap<Integer, String>();
					tmpMap.putAll(lnKeywordList.get(lnKey));
					tmpMap.put(new Integer(column), buf.toString());
					lnKeywordList.put(lnKey, tmpMap);
				} else {

					clKeywords = new HashMap<Integer, String>();
					clKeywords.put(new Integer(column), buf.toString());
					lnKeywordList.put(lnKey, clKeywords);
				}

			}
			// System.out.println("Key=>"+lnKeywordList.size());
		}
		return lnKeywordList;
	}

	public static void main(String[] args) throws Exception,
			FileNotFoundException {
		logger.info("start JavaParser");
		JavaParser parser = new JavaParser(new File("data/globalClass.data"), new File("data/localClass.data"));
		IJavaProject javaProject = null;

		String usage = "[-log [logfile] ] -src javafile [-out outputfile]";
		String defaultLogFileName = "/tmp/parseLog";
		String logFileName = defaultLogFileName, srcFileName = null, outFileName = null;

		ProgramUID puid = new ProgramUID(3, 3, 6);
		String uuid = "_" + puid.nextUID();

		// parse args -log [logfile] -src javafile -out outputfile
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-log")) {
				if (args[i + 1].startsWith("-")) {
					logFileName = defaultLogFileName;
				} else {
					logFileName = args[++i];
				}
			} else if (args[i].equals("-src")) {
				srcFileName = args[++i];
			} else if (args[i].equals("-out")) {
				outFileName = args[++i];
			} else {
				System.out.println("Usage is: " + usage);
				System.exit(-1);
			}
		}

		if (logFileName != null) {
			try {
				// logger.addHandler(new FileHandler("logs/parseLog"));
				logger.addHandler(new FileHandler(logFileName));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File f = new File(srcFileName);
		// Indexer does't pass through
		logger.info("Indexed Code File: " + srcFileName);
		IndexedCodeFile icf;
		// icf = parser.parse(f);
		icf = parser.parse(f.getAbsolutePath(), uuid, f);
		if (outFileName != null) {
			PrintWriter out = new PrintWriter(new File(outFileName + ".icf"));
			out.println(icf.toXML());
			out.close();
		} else {
			System.out.println(icf.toXML());
		}
		// src2HTML

		LineRead rct = icf.getLineRead();
		List<String> srclst = rct.getSrcList();
		List<Integer> loclst = rct.getLocList();

		int i = 0;

		if (outFileName != null) {
			// for ( Iterator ln = srclst.iterator(); ln.hasNext();) {
			// System.out.println(loclst.get(i)+" "+ ++i +" "+ ln.next());
			// }
			PrintWriter htmlout = new PrintWriter(new File(outFileName
					+ "Inner.html"));
			// htmlout.println(icf.toHTML(srclst.size()));
			htmlout.println(icf.toHTML());
			htmlout.close();

		} else {
			System.out.println(icf.toHTML());

		}
	}

}
