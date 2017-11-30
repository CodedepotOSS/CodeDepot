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
package jp.co.sra.codedepot.parser.pygments;

import java.util.List;
import java.util.Iterator;

public class PygmentsCSharpParser extends PygmentsParser {

    private static final String[] default_pattern = { "*.cs" };

    private static final String[] clone_ctrl_keyword = new String[] {
      "if", "while", "for", "foreach", "switch",
    };

    private static final String[] clone_drop_keyword = new String[] {
      "abstract", "const", "final", "readonly", "delegate",
      "private", "protected", "public", "sealed", "static",
      "async", "await", "volatile", "internal", "extern",
      "virtual", "override", "explicit", "implicit", "partial",
    };

    private static final String[] clone_drop_punctuation = new String[] {
      "{", "}",
    };

    /*
     * Constructor
     */

    public PygmentsCSharpParser() {
        super("csharp", default_pattern);
    }

    public PygmentsCSharpParser(String[] pattern) {
        super("csharp", default_pattern);
        this.setPattern(pattern);
    }

    @Override
    public boolean isCloneEndToken(Token t, List<Token>stmt) {
        if (t.getKind().startsWith("Punctuation") || t.getKind().startsWith("Operator")) {
            if (t.getValue().equals(";")) {
                return true;
            }
            if (t.getValue().equals(":")) {
                return true;
            }
            if (t.getValue().equals("{")) {
                return true;
            }
            if (t.getValue().equals(")")) {
                if (matchControlKeyword(stmt) && matchParentheses(stmt)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isCloneDropToken(Token t) {
        if (super.isCloneDropToken(t)) {
            return true;
        }

        String kind = t.getKind();
        if (kind.startsWith("Keyword")) {
            for (int i = 0; i < clone_drop_keyword.length; i++) {
                if (t.getValue().equalsIgnoreCase(clone_drop_keyword[i])) {
                    return true;
                }
            }
        }

        if (kind.startsWith("Punctuation") || kind.startsWith("Operator")) {
            for (int i = 0; i < clone_drop_punctuation.length; i++) {
                if (t.getValue().equalsIgnoreCase(clone_drop_punctuation[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchControlKeyword(List<Token> stmt) {
        Iterator<Token> it = stmt.iterator();
        while (it.hasNext()) {
            Token t = it.next();
            if (super.isCloneDropToken(t)) {
                continue;
            }

            if (t.getKind().startsWith("Keyword")) {
                for (int i = 0; i < clone_ctrl_keyword.length; i++) {
                    if (t.getValue().equalsIgnoreCase(clone_ctrl_keyword[i])) {
                        return true;
                    }
                }
            }
            break;
        }
        return false;
    }

    private boolean matchParentheses(List<Token> stmt) {
        int match = 0;
        Iterator<Token> it = stmt.iterator();
        while (it.hasNext()) {
            Token t = it.next();
            if (t.getKind().startsWith("Punctuation") || t.getKind().startsWith("Operator")) {
                if (t.getValue().equals("(")) {
                    match++;
                }
                if (t.getValue().equals(")")) {
                    match--;
                }
            }
        }
        return match == 0;
    }

}
