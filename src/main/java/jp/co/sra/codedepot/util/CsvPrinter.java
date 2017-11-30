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

import java.io.Writer;
import java.io.IOException;
import org.apache.commons.lang.StringEscapeUtils;

public class CsvPrinter {
	private Writer writer;
	private int column;

        public CsvPrinter(Writer writer) {
		this.writer = writer;
		this.column = 0;
        }

        public void print (String str) throws IOException {
                if (this.column > 0) {
                        writer.write(",");
                }
                StringEscapeUtils.escapeCsv(this.writer, str);
		this.column++;
        }

        public void println () throws IOException {
                writer.write("\r\n");
		this.column = 0;
	}
}
