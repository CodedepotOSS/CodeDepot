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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class PropertyTree {

	private int number = 0;

	private Hashtable<String, String> hash;

	/**
	 * コンストラクタ
	 */
	public PropertyTree() {
		hash = new Hashtable<String, String>();
	}

	/**
	 * 要素数の取り出し
	 *
	 * @return 要素数
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * 子プロパティのキーの取り出し
	 *
	 * a.b,a.cの場合、aのPropertyTreeに対して行うとb,cを含むEnumerationを返す
	 *
	 * @return 子プロパティのキー
	 */
	public Enumeration<String> getPropertyTreeNames() {
		return hash.keys();
	}

	/**
	 * 値の取得
	 *
	 * @param namev キーのVector形式たとえばa.bは１番目がa、2番目がb
	 * @return 対応する値
	 */
	public String getProperty(String name) {
		if ((null == name) || ("".equals(name))) {
			return "";
		}
		Object o = hash.get(name);
		if (null == o) {
			return "";
		}
		return (String) o;
	}

	/**
	 * PropertyTreeの作成
	 *
	 * @param pa 元になるインスタンスに対応したPropAdapterの実装
	 * @return 作成されたPropertyTree
     * @throws IOException
	 */
	public static PropertyTree makeTree(Properties props)
			throws IOException {
		PropertyTree ptree = new PropertyTree();
		Enumeration<?> names = props.propertyNames();
		String name, value;
		while (names.hasMoreElements()) {
			name = (String) names.nextElement();
			value = (String) props.getProperty(name);
			if ((null != name) && (null != value)) {
				ptree.hash.put(name, value);
				ptree.number++;
			}
		}
		return ptree;
	}
}
