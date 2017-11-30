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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tika.detect.Detector;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.metadata.Metadata;

public class FileTypeDetect {

	static class LastValue {
		public String absolutePath;
		public long lastModified;
		public String mimeType;
	};

	private static final ThreadLocal <LastValue> threadValue =
         	new ThreadLocal <LastValue> () {};

	public static String detect(java.io.File file) throws IOException {

		LastValue last = threadValue.get();

		if (last != null &&
		    file.getAbsolutePath().equals(last.absolutePath) &&
		    file.lastModified() == last.lastModified) {
			return last.mimeType;
		}

                InputStream stream = null;
                MediaType mtype = null;

		try {
                	stream = new FileInputStream(file);
                	InputStream input = new BufferedInputStream(stream);
                	Detector detector = new DefaultDetector();
                	Metadata metadata = new Metadata();

                	mtype = detector.detect(input, metadata);
		} finally {
                        if (stream != null) {
				stream.close();
                        }
                }

		if (mtype == null) {
			return null;
		}

		last = new LastValue();
		last.absolutePath = file.getAbsolutePath();
		last.lastModified = file.lastModified();
		last.mimeType = mtype.getType() + "/" + mtype.getSubtype();
		threadValue.set(last);

		return last.mimeType;
	}
}
