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
package jp.co.sra.codedepot.index;

import jp.co.sra.codedepot.index.c.CppProgramAnalyzer;
import jp.co.sra.codedepot.index.java.JavaProgramAnalyzer;
import jp.co.sra.codedepot.index.pygments.PygmentsVbnetProgramAnalyzer;
import jp.co.sra.codedepot.index.pygments.PygmentsCSharpProgramAnalyzer;
import jp.co.sra.codedepot.index.pygments.PygmentsSqlProgramAnalyzer;
import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.StringReader;
import java.io.IOException;

abstract public class ProgramAnalyzer extends Analyzer {

    public static final String LANG_JAVA = new String("java");
    public static final String LANG_C = new String("C");
    public static final String LANG_VBNET = new String("vb.net");
    public static final String LANG_CSHARP = new String("csharp");
    public static final String LANG_SQL = new String("sql");

    public static ProgramAnalyzer getAnalyzerWithLang(String lang) {
        if (lang == null) {
            return new JavaProgramAnalyzer();
        }
        if (lang.equals(LANG_JAVA)) {
            return new JavaProgramAnalyzer();
        }
        if (lang.equals(LANG_C)) {
            return new CppProgramAnalyzer();
        }
        if (lang.equals(LANG_VBNET)) {
            return new PygmentsVbnetProgramAnalyzer();
        }
        if (lang.equals(LANG_CSHARP)) {
            return new PygmentsCSharpProgramAnalyzer();
        }
        if (lang.equals(LANG_SQL)) {
            return new PygmentsSqlProgramAnalyzer();
        }
        return null;
    }

    public static String getTokenText(String lang, String src) throws IOException {
        Analyzer analyzer = getAnalyzerWithLang(lang);
        if (analyzer != null) {
            StringReader reader = new StringReader(src);
            TokenStream tokens = analyzer.tokenStream(Indexer.CLONETKN, reader);
            String text = CloneTokenEncoder.toString(tokens);
            return text;
        } else {
            return "";
        }
    }
}
