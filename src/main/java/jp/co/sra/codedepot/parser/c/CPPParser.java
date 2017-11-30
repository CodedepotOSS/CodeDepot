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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.lang.StringBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import jp.co.sra.codedepot.util.*;
import jp.co.sra.codedepot.util.c.*;
import jp.co.sra.codedepot.parser.*;

import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.core.parser.tests.scanner.*;

import org.eclipse.cdt.core.dom.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.parser.*;
import org.eclipse.cdt.core.dom.parser.c.*;
import org.eclipse.cdt.core.dom.parser.cpp.*;

import org.eclipse.cdt.internal.core.dom.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.c.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import org.eclipse.cdt.internal.core.parser.scanner.*;

public class CPPParser extends Parser {

    private static Logger logger = Logger.getLogger(CPPParser.class.getName());

    private static String languageName = new String("C");

    /*
     * Private members.
     */
    private ParserMode mode = null;

	/* compiler options for C language */
    private HashMap<String, String> cDefineMacros = null;
    private List<String>cIncludeList = null;

	/* compiler options for C++ language */
    private HashMap<String, String> cppDefineMacros = null;
    private List<String> cppIncludeList = null;

    /*
     * Public methods.
     */

    public ParserMode getMode() {
        return this.mode;
    }

    public HashMap<String, String> getDefineMacros(ParserLanguage lang) {
		if (lang.isCPP()) {
			return this.cppDefineMacros;
		} else {
			return this.cDefineMacros;
		}
    }

    public List<String> getIncludeList(ParserLanguage lang) {
		if (lang.isCPP()) {
			return this.cppIncludeList;
		} else {
			return this.cIncludeList;
		}
    }

	public void addDefine(ParserLanguage lang, String value) {
		HashMap<String,String> defs = getDefineMacros(lang);
		if (value.startsWith("-D")) {
			String def = value.substring("-D".length());
			int ind = def.indexOf("=");
			if (-1 == ind) {
				defs.put(def, "1");
			} else {
				String key = def.substring(0, ind);
				String val = def.substring(ind + 1, def.length());
				defs.put(key, val);
			}
		} else if (value.startsWith("-U")) {
			String def = value.substring("-U".length());
			defs.put(def, "0");
		}
	}

	public void addInclude(ParserLanguage lang, String value) {
		List<String> includes = getIncludeList(lang);
		if (value.startsWith("-I")) {
			String inc = value.substring("-I".length());
			includes.add(inc);
		}
	}

    public String[] getIncludePath(ParserLanguage lang, String parent) {

		List<String>list = getIncludeList(lang);
		int size = list.size();

		String[] array = new String[size + 1];

		for (int i = 0; i < size; i++) {
			String path = list.get(i);
			File file = new File(path);
			if (!file.isAbsolute()) {
				File f = new File(parent, path);
				try {
					array[i] = f.getCanonicalPath();
				} catch (IOException e) {
					array[i] = path;
				}
			} else {
				array[i] = path;
			}
		}
		array[size] = parent;
		return array;
	}

    private void loadOptions(ParserLanguage lang, String filename) throws IOException {

		FileReader in = new FileReader(filename);
		BufferedReader br = new BufferedReader(in);

		String line;
		while ((line = br.readLine()) != null) {
			String value = line.trim();

			if (value.startsWith("-D") || value.startsWith("-U")) {
				addDefine(lang, value);
			}
			if (value.startsWith("-I")) {
				addInclude(lang, value);
			}
		}
		br.close();
		in.close();
	}

    public IASTTranslationUnit astParse(char[] mSrc, ParserLanguage mLang,
										HashMap<String, String> mDefineMacros, String[] mIncludePath) throws Exception {
        /*
         * Create a ScannerInfo.
         */
        ScannerInfo si = null;
        if (mIncludePath != null || mDefineMacros != null) {
            ExtendedScannerInfo esI =
                new ExtendedScannerInfo(mDefineMacros,
                                        mIncludePath);
            si = (ScannerInfo)esI;
        } else {
            si = new ScannerInfo();
        }

        IScannerExtensionConfiguration sec = null;
        if (mLang.isCPP() == true) {
            GPPScannerExtensionConfiguration cppSec =
                new GPPScannerExtensionConfiguration();
            sec = (IScannerExtensionConfiguration)cppSec;
        } else {
            GCCScannerExtensionConfiguration cSec =
                new GCCScannerExtensionConfiguration();
            sec = (GCCScannerExtensionConfiguration)cSec;
        }

        /*
         * Create a scanner.
         */
        IScanner s = null;
        try {
            s =
                new CPreprocessor(
                    new CodeReader(mSrc),
                    si,
                    mLang,
                    new NullLogService(),
                    sec,
                    FileCodeReaderFactory.getInstance());
        } catch (Exception e) {
            throw e;
        }

        /*
         * Create a parser.
         */
        IASTTranslationUnit ret = null;
        if (mLang.isCPP() == true) {
            GNUCPPSourceParser p = new GNUCPPSourceParser(
                s,
                mode,
                new NullLogService(),
                //new ANSICPPParserExtensionConfiguration(),
                new GPPParserExtensionConfiguration(),
                null);

            /*
             * Finally, parse.
             */
            ret = p.parse();
            p = null;
        } else {
            GNUCSourceParser p = new GNUCSourceParser(
                s,
                mode,
                new NullLogService(),
                //new ANSICParserExtensionConfiguration(),
                new GCCParserExtensionConfiguration(),
                null);

            /*
             * Finally, parse.
             */
            ret = p.parse();
            p = null;
        }

        s = null;
        si = null;

        return ret;
    }

    public IToken[] tokenParse(char[] mSrc, ParserLanguage mLang,
								HashMap<String, String> mDefineMacros, String[] mIncludePath) throws Exception {
        /*
         * Create a ScannerInfo.
         */
        ScannerInfo si = null;
        if (mIncludePath != null || mDefineMacros != null) {
            ExtendedScannerInfo esI =
                new ExtendedScannerInfo(mDefineMacros,
                                        mIncludePath);
            si = (ScannerInfo)esI;
        } else {
            si = new ScannerInfo();
        }

        IScannerExtensionConfiguration sec = null;
        if (mLang.isCPP() == true) {
            GPPScannerExtensionConfiguration cppSec =
                new GPPScannerExtensionConfiguration();
            sec = (IScannerExtensionConfiguration)cppSec;
        } else {
            GCCScannerExtensionConfiguration cSec =
                new GCCScannerExtensionConfiguration();
            sec = (GCCScannerExtensionConfiguration)cSec;
        }

        /*
         * Create a scanner.
         */
        IScanner s = null;
        try {
            s =
                new CPreprocessor(
                    new CodeReader(mSrc),
                    si,
                    mLang,
                    new NullLogService(),
                    sec,
                    FileCodeReaderFactory.getInstance());
        } catch (Exception e) {
            throw e;
        }

        /*
         * token分割し、ITokenのリストを作成する。
         */

        ArrayList<IToken> tokens = new ArrayList<IToken>() ;
        while( true ) {
            IToken t ;
            try {
                t = s.nextToken() ;
            } catch( EndOfFileException e){ // ファイル終了
                // TODO ファイル終了をException以外で判断できないか？
                break ;
            }
            tokens.add( t ) ;
        }
        return tokens.toArray(new IToken[tokens.size()]) ;
    }


    /*
     * Constructor.
     */

    public CPPParser(String c_option, String cpp_option, ParserMode mode) {
        if (mode == null) {
            this.mode = ParserMode.COMPLETE_PARSE;
        }

		cDefineMacros = new HashMap<String, String>();
		cIncludeList = new ArrayList<String>();

		cppDefineMacros = new HashMap<String, String>();
		cppIncludeList = new ArrayList<String>();

		try {
			if (c_option != null) {
				loadOptions(ParserLanguage.C, c_option);
			}
		} catch (IOException e) {
			;
		}

		try {
			if (cpp_option != null) {
				loadOptions(ParserLanguage.CPP, cpp_option);
			}
		} catch (IOException e) {
			;
		}
    }

	@Override
    public IndexedCode parse(String fid, String uuid, java.io.File file) throws Exception {
        /*
         * Guess Language by filename suffix.
         */

		String filename = file.getAbsolutePath();
		ParserLanguage lang = MiscUtils.guessParserLanguage(filename);
		if (lang != ParserLanguage.C && lang != ParserLanguage.CPP) {
			return null;
		}

		/*
		 * get Pre-Processor Macro and Includes path.
		 */

		HashMap<String, String> macros = getDefineMacros(lang);

		String parent = file.getParent();
		String[] includes = getIncludePath(lang, parent);

        /*
         * Read the file.
         */

		String src = UniversalReader.getContents(file);
		char[] programText = src.toCharArray();

		/*
		 * Parse source.
		 */

		boolean parseResult = true;
		IASTTranslationUnit t = null;

		try {
			t = astParse(programText, lang, macros, includes);
		} catch (Exception e) {
			logger.info("cannot parse file: " + filename + " by " + e.toString());
			parseResult = false;
		} catch (StackOverflowError se) {
			logger.info("cannot parse file: " + filename + " by " + se.toString());
			parseResult = false;
		}

		/*
		 * create IndexedCodeFile
		 */

		IndexedCodeFile icf = new IndexedCodeFile();
		icf.setId(fid);
		icf.setUuid(uuid);
		icf.setProgramText(programText);

		/*
		 * call visitor
		 */
		HashMap<IASTFunctionCallExpression, FunctionCallInfo> mCallTbl = null;
		HashMap<String, IASTName[]> identifiers = null;

		if (parseResult) {
			try {
				FunctionCallVisitor fcVisitor = new FunctionCallVisitor(filename);
				t.accept(fcVisitor);
				mCallTbl = fcVisitor.getFunctionCallTable();

				AllIdentifierVisitor aiVisitor = new AllIdentifierVisitor(filename);
				t.accept(aiVisitor);
				identifiers = aiVisitor.getIdentifiers();
				icf.setIdentifiers(identifiers.get(filename));

				t.accept(new IndexCFileVisitor(icf, filename, mCallTbl));
			} catch (Exception e) {
				logger.info("cannot parse file: " + filename + " by " + e.toString());
				parseResult = false;
			} catch (StackOverflowError se) {
				logger.info("cannot parse file: " + filename + " by " + se.toString());
				parseResult = false;
			}
		}

		/*
		 * call Token Parser
		 */
		boolean tokenResult = true;
		IToken[] tokens = null;
		try {
			tokens = tokenParse(programText, lang, macros, includes);
		} catch (Exception e) {
			logger.info("cannot parse file: " + filename + " by " + e.toString());
			tokenResult = false;
		} catch (StackOverflowError se) {
			logger.info("cannot parse file: " + filename + " by " + se.toString());
			tokenResult = false;
		}

		if (tokenResult) {
			icf.setTokens(tokens);
		}

		/*
		 * return result
		 */
		return icf;
	}

	@Override
	public String getLanguageName() {
		return languageName;
	}

	@Override
	public boolean accept(java.io.File file) {
		String filename = file.getAbsolutePath();
		ParserLanguage lang = MiscUtils.guessParserLanguage(filename);
		return lang != null;
	}

	@Override
	public String toHtml(IndexedCode ic) {
		IndexedCodeFile icf = (IndexedCodeFile)ic;
		CCPP2HTML c2h = new CCPP2HTML(icf);
		return c2h.toHTML(icf.getUuid());
	}

	@Override
	public void close() {
	}
}
