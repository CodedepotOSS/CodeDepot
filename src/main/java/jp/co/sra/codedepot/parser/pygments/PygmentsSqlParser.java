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

public class PygmentsSqlParser extends PygmentsParser {

    private static final String[] default_pattern = { "*.sql" };

    /*
     * Constructor
     */

    public PygmentsSqlParser() {
        super("sql", default_pattern);
    }

    public PygmentsSqlParser(String[] pattern) {
        super("sql", default_pattern);
        this.setPattern(pattern);
    }

    @Override
    public boolean isCloneEndToken(Token t, List<Token>stmt) {
        if (t.getKind().startsWith("Punctuation")) {
            return true;
        }
        if (t.getKind().startsWith("Operator")) {
            return true;
        }
        if (t.getKind().startsWith("Text")) {
            return true;
        }
        return false;
    }

    @Override
    public List<Token> correctToken(List<Token> token) {
        return token;
    }

    @Override
    public boolean isCloneDropToken(Token t) {
        if (super.isCloneDropToken(t)) {
            return true;
        }
        if (t.getKind().startsWith("Punctuation") || t.getKind().startsWith("Operator")) {
            if (t.getValue().equals(";")) {
                return true;
            }
        }
        return false;
    }
}
