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
import java.lang.StringBuilder;
import java.io.FileReader;
import java.io.BufferedReader;

import jp.co.sra.codedepot.util.c.*;

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

public class CPPTokenParser {
    /*
     * Private members.
     */
    private String mFileName = null;

    private ParserLanguage mLang = null;
    private ParserMode mMode = null;

    private HashMap<String, String> mDefineMacros = null;
    private String[] mIncludePath = null;

    private String mOrgSrc = null;

    /*
     * Public methods.
     */
    public String getFileName() {
        return mFileName;
    }

    public ParserLanguage getLanguage() {
        return mLang;
    }

    public ParserMode getMode() {
        return mMode;
    }

    public HashMap<String, String> getDefineMacros() {
        return mDefineMacros;
    }

    public String[] getIncludePath() {
        return mIncludePath;
    }

    public String getOriginalContents() {
        return mOrgSrc;
    }

    public IToken[] tokenParse() throws Exception {
        /*
         * A main workhorse.
         */

        /*
         * Read the file.
         */
        FileReader fr = null;
        try {
            fr = new FileReader(mFileName);
        } catch (Exception e) {
            throw e;
        }

        try {
            StringBuilder aFile = new StringBuilder();
            BufferedReader br = new BufferedReader(fr);
            String aLine;

            while ((aLine = br.readLine()) != null) {
                aFile.append(aLine + "\n");
            }
            aLine = null;

            /*
             * Make a contents.
             */
            mOrgSrc = aFile.toString();

            aFile = null;
            br.close();
            br = null;
        } catch (Exception e) {
            throw e;
        }
        fr.close();
        fr = null;

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
                    new CodeReader(mOrgSrc.toCharArray()),
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
    public CPPTokenParser(String fileName,
                     ParserLanguage lang,
                     ParserMode mode,
                     String[] includePath,
                     HashMap<String, String> defineMacros) {
        mFileName = fileName;
        mLang = lang;
        mMode = mode;
        mIncludePath = includePath;
        mDefineMacros = defineMacros;

        if (mLang == null) {
            mLang = ParserLanguage.C;	// the default is C.
        }
        if (mMode == null) {
            mMode = ParserMode.COMPLETE_PARSE;
        }
    }
}
