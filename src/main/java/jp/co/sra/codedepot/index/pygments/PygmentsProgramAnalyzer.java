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
package jp.co.sra.codedepot.index.pygments;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Iterator;

import jp.co.sra.codedepot.index.ProgramAnalyzer;
import jp.co.sra.codedepot.index.CloneTokenFilter;
import jp.co.sra.codedepot.parser.pygments.PygmentsParser;
import jp.co.sra.codedepot.parser.pygments.PygmentsVbnetParser;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

public class PygmentsProgramAnalyzer extends ProgramAnalyzer {

    private PygmentsParser parser = null;

    // Constructor

    public PygmentsProgramAnalyzer(PygmentsParser parser) {
        this.parser = parser;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {

        String code;
        try {
            code = getReaderString(reader);
        }
        catch (IOException e) {
            code = "";
        }

        String clone = getCloneToken(code);
        TokenStream ts = new WhitespaceTokenizer(new StringReader(clone));
        return new CloneTokenFilter(ts);
    }

    private String getCloneToken(String code) {
        List<String>ctoken = this.parser.getCloneToken(parser.parseToken(code));

        StringBuffer sb = new StringBuffer();
        Iterator<String> it = ctoken.iterator();
        while (it.hasNext()) {
            String t = it.next();
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(t);
        }
        return sb.toString();
    }

    private String getReaderString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char []buffer = new char[1024];

        while (true) {
            int nr = reader.read(buffer);
            if (nr > 0) {
                sb.append(buffer, 0, nr);
            } else {
                break;
            }
        }
        return sb.toString();
    }

}
