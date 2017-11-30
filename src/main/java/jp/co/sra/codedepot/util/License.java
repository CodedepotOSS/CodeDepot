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

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.File;

/**
 * Determine the license of code based on comment strings.
 * @author yunwen
 *
 */
public class License {
	static public Properties licenseTellTales; //mapping tell-tale strings to license
	static public String	propertyFile = "jp/co/sra/codedepot/util/Licenses.xml";
	static private Logger logger = Logger.getLogger(License.class.getName());

	static public String getLicense(String comment) {

		if (licenseTellTales == null) {
			licenseTellTales = new Properties();
			try {
                        	ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        	InputStream stream = loader.getResourceAsStream(propertyFile);
				licenseTellTales.loadFromXML(stream);
                        	stream.close();
			} catch (IOException e) {
				logger.warning("Unable to read licenses.properties.");
				return("");
			}
		}
		if (licenseTellTales != null) {
			for (Enumeration e = licenseTellTales.propertyNames(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				// System.out.println("comment="+comment);
				// System.out.println("key="+key+",val="+licenseTellTales.getProperty(key));
				if (comment.indexOf(key) > 0) {
					String val = licenseTellTales.getProperty(key);
					return(val);
				}
			}
		}
		return("");
	}
}
