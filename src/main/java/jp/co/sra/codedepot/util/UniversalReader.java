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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.mozilla.universalchardet.UniversalDetector;

public class UniversalReader {

    public static String getContents (java.io.File file) throws IOException {

    	BufferedReader reader = getBufferedReader(file);
    	StringBuilder sb = new StringBuilder((int)file.length());

    	String line;
    	while ((line = reader.readLine()) != null) {
    		//String line2 = line.replace('\f', ' ');
    		//sb.append(line2 + "\n");
    		sb.append(line + "\n");
    	}
    	reader.close();
    	return sb.toString();
    }

    /**
     * Read the contents from fromLine (included) to toLine(excluded) from the file.
     * If fromLine is less than 1, reads from beginning.
     * If fromLine is bigger file length or toLine, return empty string.
     * If toLine is greater than file length, reads to the end.
     * @param file
     * @param fromLine
     * @param toLine
     * @return
     * @throws IOException
     */
    public static String getContentsFromTo (java.io.File file, int fromLine, int toLine) throws IOException {

    	if (fromLine >= toLine) return null;

    	BufferedReader reader = getBufferedReader(file);
    	StringBuilder sb = new StringBuilder((int)file.length());

    	String line;
    	int count = 1;
    	while ((line = reader.readLine()) != null) {
    		if (count >= fromLine && count < toLine) {
        		//String line2 = line.replace('\f', ' ');
    			//sb.append(line2 + "\n");
    			sb.append(line + "\n");
    		}
    		count++;
    	}
    	reader.close();
    	return sb.toString();
    }

    public static List<String> readLines (java.io.File file) throws IOException {
    	BufferedReader reader = getBufferedReader(file);
    	List<String> lines = new java.util.ArrayList<String>();

    	String line;
    	while ((line = reader.readLine()) != null) {
    		//lines.add(line.replace('\f', ' ') + "\n");
    		lines.add(line + "\n");
    	}
    	reader.close();
    	return lines;
    }

    public static BufferedReader getBufferedReader (java.io.File file) throws IOException {
    	return new BufferedReader(getInputStreamReader(file));
    }

    public static InputStreamReader getInputStreamReader (java.io.File file) throws IOException {
        String encoding = detectEncoding(file);
        if (encoding != null) {
		    try {
		        return new InputStreamReader(new FileInputStream(file), encoding);
		    } catch (UnsupportedEncodingException e) {
		    	return new InputStreamReader(new FileInputStream(file));
		    }
        } else {
        	return new InputStreamReader(new FileInputStream(file));
        }
    }

    public static String detectEncoding (java.io.File file) throws IOException {

        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        UniversalDetector detector = new UniversalDetector(null);

        int nread;
        byte[] buf = new byte[4096];

        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        detector.reset();
        fis.close();
        return encoding;
    }
}
