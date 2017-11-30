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

public class PygmentsVbnetParser extends PygmentsParser {

    private static final String[] default_pattern = { "*.vb", "*.vba" };

    private static final String[] clone_drop_keyword = new String[] {
      "abstract", "const", "final", "native",
      "partial", "private", "protected", "public", "static",
      "synchronized", "volatile",
    };

    /*
     * Constructor
     */

    public PygmentsVbnetParser() {
        super("vb.net", default_pattern);
    }

    public PygmentsVbnetParser(String[] pattern) {
        super("vb.net", default_pattern);
        this.setPattern(pattern);
    }

    @Override
    public boolean isCloneEndToken(Token t, List<Token> stmt) {
        if (super.isCloneEndToken(t, stmt)) {
            return true;
        }
        String kind = t.getKind();
        if (kind.startsWith("Punctuation") || kind.startsWith("Operator")) {
            if (t.getValue().equals(":")) {
                t.setValue("");
                return true;
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

        return false;
    }
}
