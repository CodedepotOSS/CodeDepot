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
package jp.co.sra.codedepot.parser;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;


public abstract class IndexedCode {
    /*
     * Abstract Methods
     */

    public abstract Map<String, String> getFields();
    public abstract List<IndexedCode> getChildren();
    public abstract String getSourceText();

    /*
     * Static Methods
     */

    public static String toXml(IndexedCode ic) {

		StringBuilder xml = new StringBuilder();

		/* for this node */

		Map<String, String> map = ic.getFields();
		if (map != null && map.size() > 0) {
			xml.append("<doc>\n");
			for(Map.Entry<String, String> e : map.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if (k != null && v != null) {
					xml.append("  <field name=" + k + ">" + v + "</field>\n");
				}
			}
			xml.append("</doc>\n");
		}

		/* for child node */

		List <IndexedCode> children = ic.getChildren();
		if (children != null) {
			for (IndexedCode c : children) {
				xml.append(toXml(c));
			}
		}

		/* return result */
        return xml.toString();
    }
}
