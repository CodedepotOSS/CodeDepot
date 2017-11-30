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
package jp.co.sra.codedepot.util.c;

public enum DeclarationKinds {
    KIND_UNKNOWN("%unknown%"),

    KIND_STRUCT("struct"),
    KIND_UNION("union"),
    KIND_ENUM("enum"),

    KIND_CLASS("class"),
    KIND_NAMESPACE("namespace"),
    KIND_USING("using"),

    KIND_TEMPLATETYPE("%templatetype%"),
    KIND_LABEL("%label%"),
    KIND_NAMED("%named%"),
    KIND_BASICTYPE("%basictype%"),
    KIND_VARIABLE("%variable%"),
    KIND_PARAMETER("%parameter%"),
    KIND_MEMBER("%member%"),
    KIND_FUNCTION("%function%")
    ;

    private final String mKind;
    private DeclarationKinds(String kind) {
        mKind = kind;
    }

    public String toString() {
        return mKind;
    }
}

