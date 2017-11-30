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

public class Token {
    public String kind;
    public String value;
    public int offset;
    public int length;

    public Token(String kind, String value, int offset) {
       this.kind = kind;
       this.value = value;
       this.offset = offset;
       this.length = value.length();
    }

    public Token(String kind, String value, int offset, int length) {
       this.kind = kind;
       this.value = value;
       this.offset = offset;
       this.length = length;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return this.kind;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return this.length;
    }

    public String toString() {
        String ov = String.valueOf(this.offset);
        String lv = String.valueOf(this.length);
        return this.kind + ":" + this.value + ":" + ov + ":" + lv;
    }
}
