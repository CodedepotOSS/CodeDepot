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

import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.parser.ParserLanguage;


import java.io.File;


public class MiscUtils {
    static private final String [] cppExt = {"cpp", "cc", "cxx", "c++"};
    static private final String [] hppExt = {"hpp", "hh", "hxx", "h++"};

    static private final String cExt = new String("c");
    static private final String hExt = new String("h");

    static private final int pathDelmChar = File.separatorChar;

    public static String getFilename(String file) {
        int l = file.length();
        if (file == null || l == 0) {
            return file;
        }

        int i = file.lastIndexOf(pathDelmChar);
        if (i < 0) {
            return file;
        }
        if (i < l) {
            i++;
        }
        return file.substring(i, l);
    }


    public static String getFilenameExtention(String file) {
        String s = getFilename(file);
        int l = s.length();
        if (s == null || l == 0) {
            return file;
        }

        int i = s.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        if (i < l) {
            i++;
        }
        return s.substring(i, l);
    }


    public static String getAbsoluteFilename(String file) {
        if (file == null || file.length() <= 0) {
            return null;
        }
        File f = new File(file);
        String ret = null;
        try {
            ret = f.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ret;
    }


    public static ParserLanguage guessParserLanguage(String file) {
        String fExt = getFilenameExtention(file);

        if (fExt == null || fExt.length() == 0) {
            return null;
        }

    	if (fExt.equals("C")) {
    		return ParserLanguage.CPP;
    	}

        if (fExt.compareToIgnoreCase(cExt) == 0) {
            return ParserLanguage.C;
        }

        if (fExt.compareToIgnoreCase(hExt) == 0) {
        	return ParserLanguage.CPP;
        }

        for(int i=0;i< cppExt.length;i++) {
	        if (fExt.compareToIgnoreCase(cppExt[i]) == 0) {
		        return ParserLanguage.CPP;
                }
        }
        for(int i=0;i< hppExt.length;i++){
	        if (fExt.compareToIgnoreCase(hppExt[i]) == 0) {
		        return ParserLanguage.CPP;
  	        }
        }

        return null;
    }


    public static String eliminateCTypeQualifiers(String str) {
        if (str == null || str.length() <= 0) {
            return str;
        }

        return str.replaceAll("(const|volatile|restrict)", "");
    }


    public static String eliminateCStorageClasses(String str) {
        if (str == null || str.length() <= 0) {
            return str;
        }

        return str.replaceAll("(auto|extern|register|static)", "");
    }


    public static String eliminateCTypeModifiers(String str) {
        if (str == null || str.length() <= 0) {
            return str;
        }

        String noSCStr = eliminateCStorageClasses(str);
        return eliminateCTypeQualifiers(noSCStr);
    }
}
