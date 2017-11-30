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
package jp.co.sra.codedepot.index.c;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jp.co.sra.codedepot.index.ProgramAnalyzer;

import org.apache.lucene.analysis.TokenStream;

/**
 * Separate C++ program into token streams. Whitespace characters and ending characters
 * are used to split the tokens. Whitespace characters are deleted; while ending characters
 * are included as an individual token. Each token retains its original position in the
 * source program. This is done through three steps:
 *  (1) ProgramTokenizer splits, using both END_TOKENS and whitespace
 *  (2) IdentifierReplacementFilter drops DROPPED_KEYWORDS, and replace identifiers
 *      other than RETAINED_TOKENS into ID_TOKEN (default to "p");
 *  (3) MergeTokensFilter merge tokens into one, using TERM_SEPARATORS as the end of the
 *      merged token. By doing so, we treat each statement that ends with ";" as one token,
 *      and each one that ends with "{" or "}" as one token too. Those TERM_SEPARATORS
 *      are kept in the token too.
 *
 *      TODO
 *      in for statement splits tokens, needs further investigation if remedies need
 *      to be taken.
 * @author matubara
 */
public class CppProgramAnalyzer extends ProgramAnalyzer {
    public static boolean debug = false;
    // Characters that indicate the end of a term(token) for clone detection
    // Each statement line is treated as one token (through the MergeTokensFilter)
    public static final Set<String> TERM_SEPARATORS = new HashSet<String>(Arrays.asList(new String[] { ";", "{", "}", ":" }));

    // Characters that are used to indicate the end of an identifier or number,
    // and remain in the tokens
    public static final String END_TOKENS = "(){}[]+-*/=!<>;,&|^~?:";

    // Keywords that are retained as tokens
    // TODO Think about the possibility of replace them with the same token
    public static final Set<String> RETAINED_KEYWORDS = new HashSet<String>(Arrays.asList("class", "struct", "union", "enum", "operator", "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue", "goto", "return", "try",
            "catch", "new", "delete", "dynamic_cast", "static_cast", "const_cast", "reinterpret_cast", "sizeof", "typeid", "throw", "template", "typename", "export", "namespace", "using"));

    public static final Set<String> DROPPED_KEYWORDS = new HashSet<String>(Arrays.asList("const", "volatile", "auto", "extern", "register", "static", "mutable", "friend", "typedef", "explicit", "inline", "virtual", "public", "protected", "private"));

    public static final Set<String> RETAINED_TOKENS;
    static {
        RETAINED_TOKENS = new HashSet<String>(RETAINED_KEYWORDS);
        for (char c : END_TOKENS.toCharArray()) {
            RETAINED_TOKENS.add("" + c);
        }
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new CppMergeTokensFilter(new CppIdentifierReplacementFilter(new CppProgramTokenizer(reader, END_TOKENS), DROPPED_KEYWORDS, RETAINED_TOKENS), TERM_SEPARATORS);
    }

    /*
     * Full list of C++ keywords as specified by
     * http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2008/n2798.pdf
     * basic types
     *  "int", "long", "short", "signed", "unsigned"
     *  "float", "double"
     *  "bool", "true", "false"
     *  "char", "wchar_t"
     *  "void"
     * complex types
     *  "class", "struct", "union"
     *  "enum"
     * modifiers
     *  "const"
     *  "volatile"
     * storage-class specifiers
     *  "auto", "extern", "register", "static", "mutable"
     * declaration specifiers
     *  "friend", "typedef"
     * function specifiers
     *  "explicit", "inline", "virtual"
     * access control specifiers in class definition
     *  "public", "protected", "private"
     * operator overload specifier in class definition
     *  "operator"
     * pointer of self in class definition
     *  "this"
     * control flows
     *  "if", "else"
     *  "for"
     *  "while", "do"
     *  "switch", "case", "default"
     *  "break"
     *  "continue"
     *  "goto"
     *  "return"
     *  "try", "catch"
     * dynamic bindings
     *  "new", "delete"
     *  "dynamic_cast", "static_cast", "const_cast", "reinterpret_cast"
     *  "sizeof", "typeid"
     *  "throw"
     * templates
     *  "template", "typename", "export"
     * namespaces
     *  "namespace", "using"
     * macros
     *  "and", "and_eq"
     *  "bitand", "bitor"
     *  "compl"
     *  "not", "not_eq"
     *  "or", "or_eq"
     *  "xor", "xor_eq"
     * inline assembler
     *  "asm"
    */
}
