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

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CvsScramble {
     private static final char[] shifts = {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
       16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
      114,120, 53, 79, 96,109, 72,108, 70, 64, 76, 67,116, 74, 68, 87,
      111, 52, 75,119, 49, 34, 82, 81, 95, 65,112, 86,118,110,122,105,
       41, 57, 83, 43, 46,102, 40, 89, 38,103, 45, 50, 42,123, 91, 35,
      125, 55, 54, 66,124,126, 59, 47, 92, 71,115, 78, 88,107,106, 56,
       36,121,117,104,101,100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48,
       58,113, 32, 90, 44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85,223,
      225,216,187,166,229,189,222,188,141,249,148,200,184,136,248,190,
      199,170,181,204,138,232,218,183,255,234,220,247,213,203,226,193,
      174,172,228,252,217,201,131,230,197,211,145,238,161,179,160,212,
      207,221,254,173,202,146,224,151,140,196,205,130,135,133,143,246,
      192,159,244,239,185,168,215,144,139,165,180,157,147,186,214,176,
      227,231,219,169,175,156,206,198,129,164,150,210,154,177,134,127,
      182,128,158,208,162,132,167,209,149,241,153,251,237,236,171,195,
      243,233,253,240,194,250,191,155,142,137,245,235,163,242,178,152
    };

    /*
     * Encoded password in CVS_PASSFILE
     */

    public static String scramble(String str) {
        StringBuffer sb = new StringBuffer(str.length() + 1);
        sb.append('A');
        for (int i = 0; i < str.length(); i++) {
            sb.append(shifts[str.charAt(i)]);
        }
        return sb.toString();
    }

    /*
     * Decode encoded password in CVS_PASSFILE
     */

    public static String descramble(String str) {
	if (str.length() == 0 || str.charAt(0) != 'A') {
	    return null;
	}
        StringBuffer sb = new StringBuffer(str.length() - 1);
	for (int i = 1; i < str.length(); i++) {
            sb.append(shifts[str.charAt(i)]);
        }
        return sb.toString();
    }

    /*
     * password entry for CVS_PASSFILE
     */

    public static String getPassFileEntry(String cvsroot, String password) {
	return getPassFileEntry(cvsroot, null, password);
    }

    public static String getPassFileEntry(String cvsroot, String user, String password) {
	StringBuffer sb = new StringBuffer();
	sb.append("/1 ");
	sb.append(normalizeRoot(cvsroot, user));
	sb.append(" ");
	sb.append(scramble(password));
	sb.append("\n");
        return sb.toString();
    }

    public static String normalizeRoot (String cvsroot) {
	return normalizeRoot(cvsroot, null);
    }

    public static String normalizeRoot (String cvsroot, String user) {

	/* :pserver:[[user][:password]@]host[:[port]]/path */

	if (user != null && user.length() > 0) {
	    String re = "^(:.server:)([^@/]+)/";
	    Pattern p = Pattern.compile(re);
	    Matcher m = p.matcher(cvsroot);
	    if (m.find()) {
	        String g = m.group(1);
	        cvsroot = g + user + "@" + cvsroot.substring(g.length());
	    }
	}

	String re = "^(:.server:(([^:@/]+)(:[^:@/]+)?@)?([^:@/]+))/";
	Pattern p = Pattern.compile(re);
	Matcher m = p.matcher(cvsroot);
	if (m.find()) {
	    String g = m.group(1);
	    cvsroot = g + ":2401" + cvsroot.substring(g.length());
	}
	return cvsroot;
    }

    public static void generatePassFile (File file, String cvsroot, String pass)
        throws IOException {
	file.createNewFile();
	FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
	String entry = getPassFileEntry(cvsroot, pass);
        writer.write(entry);
        writer.close();
        stream.close();
    }

    public static void main(String[] args) {
	for (int i = 0; i + 2 < args.length; i+=3) {
	    System.out.print(getPassFileEntry(args[i], args[i+1], args[i+2]));
	}
    }

}


