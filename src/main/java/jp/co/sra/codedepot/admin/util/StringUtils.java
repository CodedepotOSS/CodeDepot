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

public class StringUtils {
	/** 空文字列 */
	public final static String EMPTY_STR = "";

	/**
	 *
	 * null or 空文字列チェック
	 *
	 * @param str
	 * @return true: null or "", false: null or !""
	 */
	public static boolean isEmpty(String str) {
		boolean ret = false;
		if (null == str || "".equals(str)) {
			ret = true;
		}
		return ret;
	}

	/**
	 *
	 * nullを空文字列へ変換
	 *
	 * @param str
	 * @return 変換された文字列
	 */
	public static String nvl(String str) {
		if (null == str)
			return EMPTY_STR;
		else
			return str;
	}

	/**
	 * 数字をフォーマットする。 9999999 -> 9,999,999
	 *
	 * @param input0  数字ストリング
	 * @return 変換されたストリング。
	 */
	public static String formatNum(String input0) {
		if (input0 == null) {
			return "";
		}
		if (input0.trim().length() == 0) {
			return "";
		}
		String input = input0;
		boolean neg = false;
		if (input.startsWith("-")) {
			neg = true;
			input = input0.substring(1);
		}
		int point = (input.indexOf(".") > 0) ? input.indexOf(".") : input
				.length();
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < point - 1; i++) {
			if ((point - i - 1) % 3 == 0) {
				result.append(input.charAt(i)).append(",");
			} else {
				result.append(input.charAt(i));
			}
		}
		result.append(input.substring(point - 1));
		return (neg ? "-" + result.toString() : result.toString());
	}

	/**
	 *
	 * 先頭や最後に半角や全角スペースが入力された場合はTrimされる
	 *
	 * @param str
	 * @return Trimされる文字列
	 */
	public static String trimSpace(String str) {

		if (isEmpty(str)) {
			return str;
		}

		String newstr = str.replaceAll("　","  ");
		newstr = newstr.trim();
		newstr = newstr.replaceAll("  ","　");
		return newstr;
	}
}
