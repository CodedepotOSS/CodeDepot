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
/**
 *  There are utilities for convert html
 */
package jp.co.sra.codedepot.search;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Arrays;

public class HTMLConvert{

    /**
     * span tag
     *
     * @param element markup
     * @param className
     */
    public static final String markup(String element, String className) {
        StringBuffer text = new StringBuffer("<span class=\"");
        text.append(className);
        text.append("\">");
        text.append(element);
        text.append("</span>");
        return text.toString();
    }

    public static final String markupLinkid(String element, String idName) {
        StringBuffer text = new StringBuffer("<span id=\"");
        text.append(idName);
        text.append("\">");
        text.append(element);
        text.append("</span>");
        return text.toString();
    }

    public static final String markupstmt(int element, String fid) {
        StringBuffer text = new StringBuffer();
        text.append("<span class=\"stmt\" id=\"lc");
        text.append(element);
        text.append(fid);
        text.append("\">");
        return text.toString();
    }

    /*
     * Change to HTML character
     * @param text for html format
     */

    public static final String convertIntoHTML(String text) {
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
            case '&':
                newText.append("&amp;");
                break;
            case '>':
                newText.append("&gt;");
                break;
            case '<':
                newText.append("&lt;");
                break;
            case '\"':
                newText.append("&quot;");
                break;
            default:
                newText.append(c);
                break;
            }
        }

        return newText.toString();
    }

    public  static final String convertIntoHTML(char[] text, int start, int len )  {
        StringBuilder newText = new StringBuilder();
        String rtn = " ";
        if (start<0 || len < 0) return rtn;
        if (start+len <= text.length ) {
            newText.append(text, start, len);
            rtn =convertIntoHTML(newText.toString());
        }
        return rtn;
    }

    public static final String  convertKeyword(char[] srcchar, HashMap<Integer,String>keywords) {

        int len = srcchar.length;
        StringBuilder html = new StringBuilder();
        ArrayList columList = new ArrayList(keywords.keySet());
        int numberOfkw =columList.size();
        int[] kwIndex = new int[numberOfkw] ;
        // to int[]
        for (int kwid = 0 ; kwid < numberOfkw; kwid++) {
            kwIndex[kwid] = ((Integer)columList.get(kwid)).intValue();
        }
        Arrays.sort(kwIndex);
        int p = 0;
        for(int it = 0; it <kwIndex.length; it++) {
            //Integer id = it.next();
            int col = kwIndex[it];
            String keywd = keywords.get(new Integer(col));
            //System.out.println("Line-col-key "+"-"+col+"-"+keywd);
            int pstrlen = col-p;
            if (pstrlen > 0){
                html.append(convertIntoHTML(srcchar, p, pstrlen));
                //System.out.print(convertIntoHTML(srcchar,p, pstrlen));

            }
            html.append("<span class=\"keyword\">"+keywd+"</span>");
            p = col + keywd.length();
        }
        html.append(convertIntoHTML(srcchar,p, len-p));
        return html.toString();
    }

}
