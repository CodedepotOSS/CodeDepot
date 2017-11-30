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
package jp.co.sra.codedepot.admin.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APProperties {

	private static PropertyTree ptree = null;

    /**
     * <p> プロパティ初期化関数 </p>
     *
     * @param filename      プロパティファイル名
     * @throws IOException
     */
    public synchronized static void init(String filename)
    		throws IOException {
    	Properties props = new Properties();
        InputStream is = APProperties.class.getClassLoader().getResourceAsStream(filename);
        props.load(is);
        is.close();
        // プロパティツリー作成
        ptree = PropertyTree.makeTree(props);
    }

    public static String getProperty(String key) {
    	if ((null == key) || ("".equals(key))) {
    		return "";
    	}
    	if (null == ptree) {
    		return "";
    	}
    	return ptree.getProperty(key);
    }
}
