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
package jp.co.sra.codedepot.solr.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jp.co.sra.codedepot.parser.c.AllIdentifierVisitor;
import jp.co.sra.codedepot.parser.c.CPPParser;
import jp.co.sra.codedepot.parser.c.FunctionCallVisitor;
import jp.co.sra.codedepot.parser.c.IndexCFileVisitor;
import jp.co.sra.codedepot.parser.c.IndexedFunction;
import jp.co.sra.codedepot.parser.c.IndexedMethod;
import jp.co.sra.codedepot.parser.c.IndexedAbstructType;
import jp.co.sra.codedepot.parser.c.IndexedClass;
import jp.co.sra.codedepot.parser.c.IndexedCodeFile;
import jp.co.sra.codedepot.parser.c.CCPP2HTML;
import jp.co.sra.codedepot.solr.ProgramUID;
import jp.co.sra.codedepot.util.c.FunctionCallInfo;
import jp.co.sra.codedepot.util.c.MiscUtils;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.IToken;

/**
 * Indexer クラス。
 */
public class Indexer {

	/** フィールド名 ID */
	public static final String ID = "id";

	/** フィールド名 SRC */
	public static final String SRC = "src";

	/** フィールド名 LOCATION */
	public static final String LOCATION = "location";

	/** フィールド名 LICENSE */
	public static final String LICENSE = "license";

	/** フィールド名 LANG */
	public static final String LANG = "lang";

	/** フィールド名 UNIT */
	public static final String UNIT = "unit";

	/** フィールド名 FCALL */
	public static final String FCALL = "fcall";

	/** フィールド名 IN_TYPES */
	public static final String IN_TYPES = "inTypes";

	/** フィールド名 OUT_TYPE */
	public static final String OUT_TYPE = "outType";

	/** フィールド名 CODE */
	public static final String CODE = "code";

	/** フィールド名 COMMENT */
	public static final String COMMENT = "comment";

	/** フィールド名 FDEF */
	public static final String FDEF = "fdef";

	/** フィールド名 CLS */
	public static final String CLS = "cls";

	/** フィールド名 PKG */
	public static final String PKG = "pkg";

	/** フィールド名 PRJ */
	public static final String PRJ = "prj";

	/** フィールド名 BEGIN */
	public static final String BEGIN = "begin";

	/** フィールド名 CLONETKN */
	public static final String CLONETKN = "clonetkn";

	public static final String UNIT_FILE = "file";

	private static final String UNIT_CLASS = "tClass";

	private static final String UNIT_STRUCT = "tStruct";

	private static final String UNIT_UNION = "tUnion";

	private static final String UNIT_ENUM = "tEnum";

	private static final String UNIT_METHOD_DECL = "mmDecl";

	private static final String UNIT_METHOD_DEF = "mmDef";

	private static final String UNIT_FUNC_DECL = "mfDecl";

	private static final String UNIT_FUNC_DEF = "mfDef";

	private static final String LANG_C = "C";

	private static SolrServer _server;

	private static long _functionCount = 0;

	private static long _methodCount = 0;

	private static long _fileCount = 0;

	private static long _typeCount = 0;

	private static String _projectLicense = "unknown";

	private static final String CPPOPTION_CPP = "bin/cppoption_cpp.txt";

	private static final String CPPOPTION_C = "bin/cppoption_c.txt";

	private static void index(IndexedCodeFile icf, String projectName, String license, String location) throws SolrServerException,
			IOException {
		SolrInputDocument d;

		for (ListIterator citer = icf.getDeclaredTypes().listIterator(); citer
				.hasNext();) {

			IndexedAbstructType type = (IndexedAbstructType) citer.next();

			String cls = type.getName();

			if (type instanceof IndexedClass) {
				IndexedClass icls = (IndexedClass) type;

				for (ListIterator iter = icls.getDeclaredMethods()
						.listIterator(); iter.hasNext();) {
					IndexedMethod cmtd = (IndexedMethod) iter.next();
					d = new SolrInputDocument();
					d.addField(PRJ, projectName);
					d.addField(PKG, cmtd.getNamespace());
					d.addField(CLS, cls);
					d.addField(BEGIN, cmtd.getStart());
					d.addField(FDEF, cmtd.getMethodName());
					d.addField(COMMENT, cmtd.getComment());
					d.addField(CODE, cmtd.getCodeText());
					d.addField(OUT_TYPE, cmtd.getReturnType());
					d.addField(IN_TYPES, cmtd.getInputTypes());
					d.addField(FCALL, cmtd.getMethodInvokeSeq());
					d.addField(UNIT, getUnitName(cmtd));
					d.addField(LANG, getLangName());
					d.addField(LICENSE, getLicenseName(license));
					d.addField(LOCATION, location);
					d.addField(SRC, cmtd.getSrc());
					d.addField(ID, cmtd.getId());
					_server.add(d);
					_methodCount += 1;
				}
				d = new SolrInputDocument();
				d.addField(FDEF, icls.getDeclaredMethodIds());
				d.addField(FCALL, icls.getMethodInvokeSeq());
			} else {
				d = new SolrInputDocument();
			}

			d.addField(PRJ, projectName);
			d.addField(PKG, type.getNamespace());
			d.addField(CLS, cls);
			d.addField(BEGIN, type.getStart());
			d.addField(COMMENT, type.getComment());
			d.addField(CODE, type.getCodeText());
			d.addField(LOCATION, location);
			d.addField(UNIT, getUnitName(type));
			d.addField(LANG, getLangName());
			d.addField(LICENSE, getLicenseName(license));
			d.addField(SRC, type.getSrc());
			d.addField(ID, type.getId());
			_server.add(d);
			_typeCount += 1;

		}

		for (ListIterator citer = icf.getDeclaredFunctions().listIterator(); citer
				.hasNext();) {
			IndexedFunction func = (IndexedFunction) citer.next();

			d = new SolrInputDocument();
			d.addField(PRJ, projectName);
			d.addField(PKG, func.getNamespace());
			d.addField(BEGIN, func.getStart());
			d.addField(FDEF, func.getFunctionName());
			d.addField(COMMENT, func.getComment());
			d.addField(CODE, func.getCodeText());
			d.addField(OUT_TYPE, func.getReturnType());
			d.addField(IN_TYPES, func.getInputTypes());
			d.addField(FCALL, func.getMethodInvokeSeq());
			d.addField(UNIT, getUnitName(func));
			d.addField(LANG, getLangName());
			d.addField(LICENSE, getLicenseName(license));
			d.addField(LOCATION, location);
			d.addField(SRC, func.getSrc());
			d.addField(ID, func.getId());
			_server.add(d);
			_functionCount += 1;
		}

		for (ListIterator iter = icf.getDeclaredMethods().listIterator(); iter
				.hasNext();) {
			IndexedMethod cmtd = (IndexedMethod) iter.next();
			d = new SolrInputDocument();
			d.addField(PRJ, projectName);
			d.addField(PKG, cmtd.getNamespace());
			d.addField(CLS, cmtd.getClassName());
			d.addField(BEGIN, cmtd.getStart());
			d.addField(FDEF, cmtd.getMethodName());
			d.addField(COMMENT, cmtd.getComment());
			d.addField(CODE, cmtd.getCodeText());
			d.addField(OUT_TYPE, cmtd.getReturnType());
			d.addField(IN_TYPES, cmtd.getInputTypes());
			d.addField(FCALL, cmtd.getMethodInvokeSeq());
			d.addField(UNIT, getUnitName(cmtd));
			d.addField(LANG, getLangName());
			d.addField(LICENSE, getLicenseName(license));
			d.addField(LOCATION, location);
			d.addField(SRC, cmtd.getSrc());
			d.addField(ID, cmtd.getId());
			_server.add(d);
			_methodCount += 1;
		}

		d = new SolrInputDocument();
		d.addField(PRJ, projectName);
		d.addField(BEGIN, icf.getStart());
		d.addField(COMMENT, icf.getComments());
		d.addField(CODE, icf.getCodeText());
		d.addField(UNIT, UNIT_FILE);
		d.addField(LANG, getLangName());
		d.addField(LICENSE, getLicenseName(license));
		d.addField(LOCATION, location);
		d.addField(SRC, new String(icf.getProgramText()));
		d.addField(ID, icf.getId());
		d.addField(PKG, icf.getNamespace());
		_server.add(d);
		_fileCount += 1;
	}

	/**
     * UNIT名を取得する。
     *
     * @param type IndexedAbstructTypeオブジェクト
     * @return UNIT名
     */
	public static String getUnitName(IndexedAbstructType type) {

		String unitName = null;

		IndexedAbstructType.Type t = type.getType();
		switch (t) {
		case CLASS:
			unitName = UNIT_CLASS;
			break;
		case STRUCT:
			unitName = UNIT_STRUCT;
			break;
		case ENUM:
			unitName = UNIT_ENUM;
			break;
		case UNION:
			unitName = UNIT_UNION;
			break;
		default:
			// ありえない
			unitName = "unknown";
			break;
		}

		return unitName;
	}

	/**
     * UNIT名を取得する。
     *
     * @param func IndexedFunctionオブジェクト
     * @return UNIT名
     */
	public static String getUnitName(IndexedFunction func) {
		String unitName = null;
		if (func.isDefinition()) {
			unitName = UNIT_FUNC_DEF;
		} else {
			unitName = UNIT_FUNC_DECL;
		}
		return unitName;
	}

	/**
     * UNIT名を取得する。
     *
     * @param cmtd IndexedMethodオブジェクト
     * @return UNIT名
     */
	public static String getUnitName(IndexedMethod cmtd) {
		String unitName = null;
		if (cmtd.isDefinition()) {
			unitName = UNIT_METHOD_DEF;
		} else {
			unitName = UNIT_METHOD_DECL;
		}
		return unitName;
	}

	/**
     * LANG名を取得する。
     *
     * @return LANG名
     */
	public static String getLangName() {
		// とりあえず現状は「C」固定
		return LANG_C;
	}

	/**
     * LICENSE名を取得する。
     *
     * @param license 設定されているLICENSE名
     * @return LICENSE名
     */
	public static String getLicenseName(String license) {

		// 未設定の場合にはデフォルト値を返す
		if (null == license || "" == license) {
			license = _projectLicense;
		}

		return license;
	}

	public static void main(String[] args) {
		long thestart = new Date().getTime();

		ProgramUID puid = new ProgramUID(3, 3, 6);

		String usage = "Indexer" + "[(-server | -s) solr_server_port]" + // default
				// to
				// http://localhost:8080/solr
				"[-log logfile]" + // default to logs/indexLog
				"[-prj project]" + // default to empty
				"[-commit]" + // commit after all files are indexed
				"[-noindex]" + // no index will be produced, only for
				// linksource
				"[(-linksource | -linksrc) dir]" + // directory where html
				// files of java programs
				// are stored and linked.
				// convention: webapps/src/html/project/package/Class.java.html"
				"[-srcdir srcdir] " + // top directory to be indexed
				"[-srcfile file]" + // a java file to be indexed
				"[-D...] " + // define
				"[-I...] " + // include
				"[-U...] " + // undef
				"[-out outputfile] "; // icf xml output

		// file path will be kept in -linksrc dir

		String serverName = "localhost/solr";
		String logFileName = "logs/indexLog";
		String outFileName = null;
		Boolean linkSource = false;
		String projectName = "";
		String htmlSourceDirectoryName = "";
		String srcdir = "";
		Boolean commit = false;
		Boolean index = true; // if set to false, don't perform indexing
		Queue<File> filesToIndex = new LinkedList<File>();

		ArrayList<String> incList = new ArrayList<String>();
		HashMap<String, String> defs = new HashMap<String, String>();

		Logger logger = Logger.getLogger(Indexer.class.getName());
		// Logger loggerSolr = Logger.getLogger("org.apache.solr");
		// loggerSolr.addHandler(new ConsoleHandler());
		logger.addHandler(new ConsoleHandler());

		long byteCount = 0; // number of bytes that are indexed

		int i = 0;
		File f = null;
		boolean isFirstWriteXML = true;

		while (i < args.length) {
			if (args[i].equals("-server") || args[i].equals("-s")) {
				serverName = args[++i];
			} else if (args[i].equals("-log")) {
				logFileName = args[++i];
			} else if (args[i].equals("-prj")) {
				projectName = args[++i];
			} else if (args[i].equals("-commit")) {
				commit = true;
			} else if (args[i].equals("-linksrc")
					|| args[i].equals("-linksource")) {
				htmlSourceDirectoryName = args[++i];
				if (!htmlSourceDirectoryName.equals(""))
					linkSource = true;
			} else if (args[i].equals("-noindex")) {
				index = false;
			} else if (args[i].equals("-license")) {
				_projectLicense = args[++i];
			} else if (args[i].equals("-srcdir")) {
				srcdir = args[++i];
				if (srcdir.endsWith(File.separator)) {
					srcdir = srcdir.substring(0, srcdir.length() - 1);
				}
				f = new File(srcdir);
			} else if (args[i].equals("-srcfile")) {
				f = new File(args[++i]);
			} else if (args[i].startsWith("-D")) {
				defs = setDefs(defs, args[i]);
			} else if (args[i].startsWith("-I")) {
				incList = setIncList(incList, args[i]);
			} else if (args[i].startsWith("-U")) {
				defs = setDefs(defs, args[i]);
			} else if (args[i].equals("-out")) {
				outFileName = args[++i] + ".icf";
			} else {
				System.err.println("Wrong format of input\n" + usage);
				System.exit(-1);
			}
			i++;
		}

		if (!filesToIndex.offer(f)) {
			System.err.println("Unable to read " + f.getAbsolutePath() + "\n"
					+ usage);
			System.exit(-1);
		}

		FileHandler fh;
		try {
			fh = new FileHandler(logFileName);
			logger.addHandler(fh);

			// loggerSolr.addHandler(fh); // this is not working
		} catch (SecurityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Get the link source directory, if not create
		File htmlSourceDirectory = null;
		if (linkSource) {
			try {
				htmlSourceDirectory = new File(htmlSourceDirectoryName);
				if (!htmlSourceDirectory.isDirectory()) {
					// create new directory
					htmlSourceDirectory.mkdirs();
				}
			} catch (SecurityException e) {
				logger
						.severe("Unable to create directory that holds html files of program in "
								+ htmlSourceDirectoryName
								+ " due to security reason. Abort");
				System.err
						.println("Unable to create directory that holds html files of program in "
								+ htmlSourceDirectoryName
								+ " due to security reason. Abort");
				System.exit(-1);
			}
		}

		if (index) {
			try {
				_server = new CommonsHttpSolrServer(serverName);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// get a global package cls table
		// GlobalClassTab gTab = new GlobalClassTab("data/globalClass.data");
		// start indexing
		String fid;

		while ((f = filesToIndex.poll()) != null) {
			if (f.isDirectory()) {
				for (File tmpf : f.listFiles()) {
					if (!filesToIndex.offer(tmpf)) {
						logger
								.warning("Unable to add file "
										+ tmpf.getAbsolutePath()
										+ " to queue. Ignored");
					}
				}
				continue;
			}
			if (null == MiscUtils.guessParserLanguage(f.getAbsolutePath())) {
				// not a C/C++ file, doing nothing
				continue;
			}
			long size = f.length();
			logger.info("Start indexing file " + f.getAbsolutePath()
					+ " of size " + size);
			long starttime = new Date().getTime();

			ParserLanguage l = MiscUtils.guessParserLanguage(f
					.getAbsolutePath());

			// コマンドライン引数で設定がない場合は、設定ファイルから読み込む
			boolean setDefs = !defs.isEmpty();
			boolean setIncs = !incList.isEmpty();

			try {
				FileReader in = null;
				BufferedReader br = null;
				String line;

				switch (l) {
				case C:
					in = new FileReader(CPPOPTION_C);
					br = new BufferedReader(in);
					break;
				case CPP:
					in = new FileReader(CPPOPTION_CPP);
					br = new BufferedReader(in);
					break;
				default:
					// ありえない
					break;
				}

				while ((line = br.readLine()) != null) {
					String value = line.trim();

					if (!setDefs
							&& (value.startsWith("-D") || value
									.startsWith("-U"))) {
						defs = setDefs(defs, value);
					}
					if (!setIncs && value.startsWith("-I")) {
						incList = setIncList(incList, value);
					}
				}

				if (null != in) {
					in.close();
				}
				if (null != br) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			int len = (int) f.length();
			char[] programText = new char[len];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				reader.read(programText, 0, len);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			CPPParser parser = new CPPParser(null, null, null);
			String[] incs = (String[]) incList.toArray(new String[] {});

			IASTTranslationUnit t = null;
			boolean parseResult = true;
			try {
			    t = parser.astParse(programText, l, defs, incs);
			} catch (IOException e) {
				logger.info("skip file: " + f.getAbsolutePath());
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				parseResult = false;
			}

			String fileName = f.getAbsolutePath();
			HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl = null;
			HashMap<String, IASTName[]> identifiers = null;
			if (parseResult) {
				FunctionCallVisitor fcVisitor = new FunctionCallVisitor(
						fileName);
				t.accept(fcVisitor);
				mCallTbl = fcVisitor.getFunctionCallTable();

				AllIdentifierVisitor aiVisitor = new AllIdentifierVisitor(
						fileName);
				t.accept(aiVisitor);

				identifiers = aiVisitor.getIdentifiers();
			}

			IndexedCodeFile icf = new IndexedCodeFile();
			//icf.setProgramText(t.getRawSignature().toCharArray());
			// icf.setLocation(fileName);
			icf.setProgramText(programText);

			System.out.println("index-location : " + fileName);
			if (parseResult) {
				t.accept(new IndexCFileVisitor(icf, fileName, mCallTbl));
			}

			boolean tokenResult = true;
			IToken[] tokens = null;
			try {
				tokens = parser.tokenParse(programText, l, defs, incs);
			} catch (Exception e) {
				e.printStackTrace();
				tokenResult = false;
			}

			if (tokenResult) {
				icf.setTokens(tokens);
			}

			// icf.setProject(projectName);
			fid = "_" + puid.nextUID();
			icf.setId(fileName);


			if (index) {
				try {
				    index(icf, projectName, _projectLicense, fileName);
					byteCount += size;
					logger.fine(" Finished in "
							+ ((new Date()).getTime() - starttime));
				} catch (SolrServerException e) {
					e.printStackTrace();
					logger.severe("SolrServerException when indexing "
							+ args[i]);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SolrException e) {
					// Internal Server Error 対策のためエラーをキャッチする
					e.printStackTrace();
				}
			}

			// convert and write to html file
			if (linkSource) {

				System.out.println(fileName);
				System.out
						.println("========> make innerHTML(" + fileName + ")");
				// fid = "_" + puid.nextUID();

				try {
					File target_f = new File(fileName);
					String htmlProgramPath;
					// if(
                    // target_f.getAbsolutePath().equals(f.getAbsolutePath()) ){
					if (target_f.getCanonicalPath()
							.equals(f.getCanonicalPath())) {
						htmlProgramPath = f.toString().substring(
								srcdir.length());
					} else {
						htmlProgramPath = fileName;
					}

					CCPP2HTML c2h;
					if (parseResult) {
					    c2h = new CCPP2HTML(icf, identifiers.get(fileName));
					} else {
					    c2h = new CCPP2HTML(icf, null);
					}

					PrintWriter htmlout = c2h.getHtmlFile(htmlSourceDirectoryName, htmlProgramPath);
					htmlout.println(c2h.toHTML(fid));
					htmlout.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (commit) {
				// because committing is really time consuming, we will let
				// the
				// system handle the
				// auto-commit, or do it by the user unless they clearly ask
				// for
				// committing
				System.out.println("Committing");
				try {
					// if we don't commit here the change will not be
					// visible
					// to the searcher
					_server.commit();
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (outFileName != null) {
				PrintWriter out;
				try {
					if (isFirstWriteXML) {
						out = new PrintWriter(new File(outFileName));
					} else {
						// 2回目以降は追加書き込みとする
						out = new PrintWriter(new FileWriter(outFileName, true));
					}
					out.println(icf.toXML());
					isFirstWriteXML = false;
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(icf.toXML());
			}

			System.out.println("_SUMMARY_ " + _fileCount + " files, "
					+ _typeCount + " types, " + _methodCount + " methods, "
					+ _functionCount + " functions, " + byteCount + " bytes, "
					+ (new Date().getTime() - thestart) + " milliseconds");
			logger.info("_SUMMARY_ " + _fileCount + " files, " + _typeCount
					+ " types, " + _methodCount + " methods, " + _functionCount
					+ " functions, " + byteCount + " bytes, "
					+ (new Date().getTime() - thestart) + " milliseconds");

		}
	}

	private static HashMap<String, String> setDefs(
			HashMap<String, String> defs, String value) {

		HashMap<String, String> retDefs = defs;

		if (value.startsWith("-D")) {
			try {
				String def = value.substring("-D".length());
				if (!def.isEmpty()) {
					int ind = def.indexOf("=");
					if (-1 == ind) {
						retDefs.put(def, "1");
					} else {
						String key = def.substring(0, ind);
						String val = def.substring(ind + 1, def.length());
						retDefs.put(key, val);
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} else if (value.startsWith("-U")) {
			String def = value.substring("-U".length());
			if (!def.isEmpty()) {
				retDefs.put(def, "0");
			}
		}

		return retDefs;
	}

	private static ArrayList<String> setIncList(ArrayList<String> incList,
			String value) {

		ArrayList<String> retIncList = incList;

		String inc = value.substring("-I".length());
		if (!inc.isEmpty()) {
			retIncList.add(inc);
		}
		return retIncList;
	}

}
