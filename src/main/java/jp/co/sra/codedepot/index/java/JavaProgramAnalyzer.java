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
package jp.co.sra.codedepot.index.java;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jp.co.sra.codedepot.index.IdentifierReplacementFilter;
import jp.co.sra.codedepot.index.MergeTokensFilter;
import jp.co.sra.codedepot.index.PrettyPrint;
import jp.co.sra.codedepot.index.ProgramAnalyzer;
import jp.co.sra.codedepot.index.ProgramTokenizer;

import org.apache.lucene.analysis.TokenStream;

/**
 * Separate Java program into token streams. Whitespace characters and ending characters
 * are used to split the tokens. Whitespace characters are deleted; while ending characters
 * are included as an individual token. Each token retains its original position in the
 * source program. This is done through three steps:
 * 	(1) ProgramTokenizer splits, using both END_TOKENS and whitespace
 *  (2) IdentifierReplacementFilter drops DROPPED_KEYWORDS, and replace identifiers
 *  	other than RETAINED_TOKENS into ID_TOKEN (default to "p");
 *  (3) MergeTokensFilter merge tokens into one, using TERM_SEPARATORS as the end of the
 *      merged token. By doing so, we treat each statement that ends with ";" as one token,
 *      and each one that ends with "{" or "}" as one token too. Those TERM_SEPARATORS
 *      are kept in the token too.
 *
 *      TODO
 *      in for statement splits tokens, needs further investigation if remedies need
 *      to be taken.
 * @author ye
 * @updader matubara
 * @$Id: JavaProgramAnalyzer.java 2342 2017-11-09 05:36:32Z fang $
 */
public class JavaProgramAnalyzer extends ProgramAnalyzer {
    public static boolean debug = false;
    // Characters that indicate the end of a term(token) for clone detection
    // Each statement line is treated as one token (through the MergeTokensFilter)
    public static final Set<String> TERM_SEPARATORS = new HashSet<String>(Arrays.asList(new String[] { ";", "{", "}", ":" }));

    // Characters that are used to indicate the end of an identifier or number,
    // and remain in the tokens
    public static final String END_TOKENS = "(){}[]+-*/=!<>;,&|^~?:";

    // Keywords that are retained as tokens
    // TODO Think about the possibility of replace them with the same token
    public static final Set<String> RETAINED_KEYWORDS = new HashSet<String>(Arrays.asList("assert", "break", "case", "catch", "class", "continue", "default", "do", "else", "enum", "extends", "finally", "for", "goto", "if", "implements", "import",
            "instanceof", "interface", "native", "new", "package", "return", "strictfp", "super", "switch",
            // "this",
            // this.xxx will cause problem
            "throw", "throws", "transient", "try", "void", "while"));

    // These are the keywords that get dropped, extension of
    //  Kamiya RJ5: remove accessibility keywords
    public static final Set<String> DROPPED_KEYWORDS = new HashSet<String>(Arrays.asList("abstract", "const", "final", "native", "private", "protected", "public", "static", "synchronized", "volatile"));

    public static final Set<String> RETAINED_TOKENS;
    static {
        RETAINED_TOKENS = new HashSet<String>(RETAINED_KEYWORDS);
        for (char c : END_TOKENS.toCharArray()) {
            RETAINED_TOKENS.add("" + c);
        }
        // System.out.println(RETAINED_TOKENS);
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new MergeTokensFilter(new IdentifierReplacementFilter(new ProgramTokenizer(reader, END_TOKENS), DROPPED_KEYWORDS, RETAINED_TOKENS), TERM_SEPARATORS);
    }

    public static void main(String[] args) throws IOException {
        JavaProgramAnalyzer analyzer = new JavaProgramAnalyzer();
        TokenStream tokens;
        tokens = analyzer.tokenStream("clone", new FileReader("./src/java/jp/co/sra/codedepot/index/java/JavaProgramAnalyzer.java"));
        // tokens = analyzer.tokenStream("clone", new FileReader("./testdata/testprj/Test.java"));
        PrettyPrint.pprint(tokens);
    }

    /*
     * Full list of Java keywords as specified by
     * http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#229308
     "abstract", "assert", "boolean", "break", "byte", "case", "catch",
        "char", "class", "const", "continue", "default", "do", "double",
        "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
        "if", "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "null", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "true", "try",
        "void", "volatile", "while"
    */
}
