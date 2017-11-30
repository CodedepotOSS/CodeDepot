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
package jp.co.sra.codedepot.util;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/***
 * Parse a Java source code tokens to extract  keyword position and comment position.
 * @author x.Fang
 */

public class TokenParser {


    ArrayList<int[]> keywordListPos;
    ArrayList<int[]> commentListPos;

    public ArrayList<int[]> getCommentPosition() {
        return commentListPos;
    }

    public ArrayList<int[]> getKeywordPosition() {
        return keywordListPos;
    }

    //    public ArrayList<int[]> getKeywordPosition(CompilationUnit cu, char[] source) {

    public  TokenParser(CompilationUnit cu, char[] source) {

        //        int[] position = new int[2];
        keywordListPos = new ArrayList<int[]>();
        commentListPos = new ArrayList<int[]>();

        if (cu instanceof CompilationUnit) {
            IScanner scanner = ToolFactory.createScanner(true, false, true, false);
            scanner.setSource(source);
            int start = cu.getStartPosition();
            int end = start + cu.getLength();
            scanner.resetTo(start, end);
            int token;
            try {
                while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF)
                    {
                        switch(token) {
                         case ITerminalSymbols.TokenNameCOMMENT_BLOCK:
                            commentListPos.add(new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1, ITerminalSymbols.TokenNameCOMMENT_BLOCK});

                            break;
                        case ITerminalSymbols.TokenNameCOMMENT_JAVADOC:
                            commentListPos.add(new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1, ITerminalSymbols.TokenNameCOMMENT_JAVADOC});
                            break;
                        case ITerminalSymbols.TokenNameCOMMENT_LINE:
                            commentListPos.add(new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1, ITerminalSymbols.TokenNameCOMMENT_LINE});
                            break;
                        case ITerminalSymbols.TokenNameabstract:
                            keywordListPos.add(new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;
                        case ITerminalSymbols.TokenNameassert:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameboolean:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamebreak:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                        case ITerminalSymbols.TokenNamebyte:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamecase:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamecatch:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamechar:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameclass:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameconst:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamecontinue:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamedefault:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamedo:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamedouble:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameelse:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameenum:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameextends:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamefinal:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamefinally:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamefloat:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});

                        break;
                        case ITerminalSymbols.TokenNamefor:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameif:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameinstanceof:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameimplements:
                            keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameimport:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNameint:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                        case ITerminalSymbols.TokenNameinterface:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamelong:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                        case ITerminalSymbols.TokenNamenative:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;
                        case ITerminalSymbols.TokenNamenew:
                            keywordListPos.add( new int[] {
                                scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                            break;

                    case ITerminalSymbols.TokenNamepackage:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNameprivate:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNameprotected:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamepublic:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;
                    case ITerminalSymbols.TokenNamereturn:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNameshort:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamestatic:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamesuper:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamestrictfp:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNameswitch:
                        keywordListPos.add( new int[] {
                            scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamesynchronized:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamethis:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamethrow:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamethrows:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNametransient:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNametry:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamevoid:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamevolatile:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    case ITerminalSymbols.TokenNamewhile:
                        keywordListPos.add( new int[] {scanner.getCurrentTokenStartPosition()+start, scanner.getCurrentTokenEndPosition()+start+1});
                        break;

                    default:
                        break;
                    }

                }
        } catch (InvalidInputException e) {
        }
    }

}

}
