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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quartz.CronExpression;

public class CheckUtil {

	/**
	 * 半角英数字と半角符号"."、"-"、"_"、"@"のみをチェックする
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英数字と半角符号"."、"-"、"_"、"@"のみ
	 *         false:入力した文字は半角英数字と半角符号"."、"-"、"_"、"@"のみではない
	 */
	public static boolean isUserName(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[0-9A-Za-z\\.\\-_@]+$");
		}
	}

	/**
	 * 半角英数字と半角符号"."、"-"、"_"のみをチェックする
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英数字と半角符号"."、"-"、"_"のみ
	 *         false:入力した文字は半角英数字と半角符号"."、"-"、"_"のみではない
	 */
	public static boolean isProjName(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[0-9A-Za-z\\.\\-_]+$");
		}
	}

	/**
	 * 半角英数字と半角符号"."、"-"、"_"のみをチェックする
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英数字と半角符号".","-","_","/","+" のみ
	 *         false:入力した文字は半角英数字と半角符号".","-","_","/","+" のみではない
	 */
	public static boolean isLicenseName(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[0-9A-Za-z\\.\\-\\+_/]+$");
		}
	}

	/**
	 * 半角英数字或は半角符号
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英数字或は半角符号 false:入力した文字は半角英数字或は半角符号ではない
	 */
	public static boolean isEngNumMark(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("[\\p{Graph} ]*");
		}
	}

	/**
	 * 半角英数字
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英数字 false:入力した文字は半角英数字ではない
	 */
	public static boolean isEngNum(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[0-9A-Za-z]+$");
		}
	}

	/**
	 * 半角英文
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角英文 false:入力した文字は半角英文ではない
	 */
	public static boolean isEng(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[A-Za-z]+$");
		}
	}

	/**
	 * 半角数字
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は半角数字 false:入力した文字は半角数字ではない
	 */
	public static boolean isNum(String str) {
		if (null == str) {
			return false;
		} else {
			return str.matches("^[0-9]*$");
		}
	}

	/***
	 * 正整数
	 *
	 * @param str
	 *            入力した文字
	 * @return boolean true:入力した文字は正整数 false:入力した文字は正整数ではない
	 */
	public static boolean isInteger(String str) {
		boolean isIntegerOk = true;
		try {
			if (null == str) {
				return false;
			} else {
				isIntegerOk = str.matches("^[0-9]*$");
				if (isIntegerOk) {
					int itemValue = Integer.valueOf(str);
					if (itemValue == 0) {
						isIntegerOk = false;
					}

				}
			}
		} catch (NumberFormatException ex) {
			return false;
		}
		return isIntegerOk;
	}

	/***
	 * Integer
	 * @param str 入力した文字
	 * @return boolean true:入力した文字は正整数 false:入力した文字は正整数ではない
	 */
	public static boolean isIntegerCheck(String str){
		try {
			if(StringUtils.isEmpty(str)) {
				return false;
			}else {
					Integer.valueOf(str);
					return true;
			}
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	/**
	 * 日付
	 *
	 * @param date
	 * @return true:チェックOK false:チェックNG
	 */
	public static boolean checkDate(String date) {
		if (!isNum(date) || date.length() != 8) {
			return false;
		}
		int nYear = Integer.parseInt(date.substring(0, 4));
		int nMonth = Integer.parseInt(date.substring(4, 6));
		int nDay = Integer.parseInt(date.substring(6, 8));
		int[] daysOfMonth = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if ((nMonth < 1) || (nMonth > 12)) {
			return false;
		}
		if ((nDay < 1) || (nDay > daysOfMonth[nMonth - 1])) {
			return false;
		}
		if (!isLeapYear(nYear) && (nMonth == 2) && (nDay == 29)) {
			return false;
		}
		return true;
	}

	/**
	 * 閏年
	 *
	 * @param year
	 *            入力した年
	 * @return boolean true:入力したは閏年 false:入力したは閏年ではない
	 */
	private static boolean isLeapYear(int year) {
		if (year % 100 != 0 && year % 4 == 0) {
			return true;
		} else if (year % 400 == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 「YYYYMM」の形式をチェックする
	 *
	 * @param date
	 * @return boolean true: 入力した文字は「YYYYMM」の形式, false: 入力した文字は「YYYYMM」の形式ではない
	 */
	public static boolean checkFormatDate(String date) {
		if (!isNum(date) || date.length() != 6) {
			return false;
		}
		int nMonth = Integer.parseInt(date.substring(4, 6));
		if ((nMonth < 1) || (nMonth > 12)) {
			return false;
		}
		return true;
	}

	/**
	 * 文字数チェック
	 *
	 * @param str
	 *            入力した文字
	 * @param len
	 *            桁数
	 *
	 * @return true:チェックOK false:チェックNG
	 */
	public static boolean checkLength(String str, int len) {

		if (StringUtils.isEmpty(str)) {
			return true;
		}

		byte[] itembyte = null;
		int strLength = 0;
		try {
			itembyte = str.getBytes(APConst.ENCODE_UTF_8);
			strLength = itembyte.length;
			if (strLength > len) {
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		return true;
	}

	/***************************************************************************
	 * メールアドレスチェック
	 *
	 * @param str
	 *            メールアドレス
	 * @return true:チェックOK false:チェックNG
	 */
	public static boolean isEmail(String str) {
		if (null == str) {
			return false;
		} else {
			// return str.matches("\\w+(\\.\\w+)*@\\w+(\\.\\w+)+");
			return str.matches("[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+");
		}
	}

	/***************************************************************************
	 * len文字以上の字符をチェック (Byte)
	 *
	 * @param str
	 *            字符
	 * @param len
	 *            文字数
	 * @return true:チェックOK false:チェックNG
	 */
	public static boolean checkLengthAbove(String str, int len) {

		if (StringUtils.isEmpty(str)) {
			return false;
		}
		byte[] itembyte = null;
		int strLength = 0;
		try {
			itembyte = str.getBytes(APConst.ENCODE_UTF_8);
			strLength = itembyte.length;
			if (strLength < len) {
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		return true;
	}

	/**
	 * URLチェック<br>
	 * <br>
	 *
	 * @param pInput
	 *            入力したURL<br>
	 * @return true:チェックOK false:チェックNG<br>
	 */
	public static boolean isUrl(String url) {
		if (url == null) {
			return false;
		}
		try {
			new URL(url);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * src_typeが"local"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isLocalPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return false;
		}
		File path = new File(srcPath);
		return path.exists();
	}

// Added by wubo on 2010/09/09 for V2.1対応 Start
	/**
	 * src_typeが"local"の場合、src_pathのアクセス権限をチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isPathAccess(String srcPath) {
		File path = new File(srcPath);
		if (path.listFiles() == null) {
			return false;
		}
		return true;
	}
// Added by wubo on 2010/09/09 for V2.1対応 End

	/**
	 * src_typeが"svn"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isSVNPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return false;
		}
		return srcPath.startsWith(APConst.PATH_HTTP)
				|| srcPath.startsWith(APConst.PATH_HTTPS)
				|| srcPath.startsWith(APConst.PATH_FILE)
				|| srcPath.startsWith(APConst.PATH_SVN_SSH);
	}

	/**
	 * src_typeが"cvs"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isCVSPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return false;
		}
		Pattern pattern = Pattern.compile(":pserver:(([^:@/]+)(:[^:@/]+)?@)?[^:@/]+(:[^:@/]+)?/.*\\s\\S+");
		Matcher matcher = pattern.matcher(srcPath);
		return matcher.matches();
	}

	/**
	 * src_typeが"git"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isGITPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return false;
		}
		return srcPath.startsWith(APConst.PATH_HTTP)
				|| srcPath.startsWith(APConst.PATH_HTTPS)
				|| srcPath.startsWith(APConst.PATH_FILE)
				|| srcPath.startsWith(APConst.PATH_GIT)
				|| srcPath.startsWith(APConst.PATH_SSH);
	}

// Added by wubo on 2010/08/30 for V2.1対応 Start
	/**
	 * src_typeが"jazz"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isJAZZPath(String srcPath) {
		if (StringUtils.isEmpty(srcPath)) {
			return false;
		}

		// https://<server>:<port>/jazz wsName moduleName
// Modified by wubo on 2010/10/09 for V2.1対応 検収バグNo.4 Start
//		Pattern pattern = Pattern.compile("https://[^:@/]+:[0-9]+/jazz\\s((\"[^\\s\"][^\"]*[^\\s\"]\")|([^\\s\"]+))(\\s((\"[^\\s\"][^\"]*[^\\s\"]\")|([^\\s\"]+)))*")

		Pattern pattern = Pattern.compile("https://[^:@/]+:[0-9]+/\\S*\\s+((\"[\\s]*[^\\s\"][^\"]*[^\\s\"][\\s]*\"[\\s]*)*|([^\\s\"]+)[\\s]*)(\\s+((\"[\\s]*[^\\s\"][^\"]*[^\\s\"][\\s]*\"[\\s]*)|([^\\s\"]+)[\\s]*))*");
// Modified by wubo on 2010/10/09 for V2.1対応 検収バグNo.4 End
		Matcher matcher = pattern.matcher(srcPath);
		return matcher.matches();
	}

	/**
	 * Windows OS及び src_typeが"local"の場合、src_pathをチェックする
	 *
	 * @param srcPath
	 * @return
	 */
	public static boolean isWindowsLoaclPath(String srcPath) {
// Modified by wubo on 2010/10/28 for V2.1対応  Start
		//String matchStr = "^([A-Za-z]:)(\\\\[^/:;,*?<>|\\\\]+)*$";
		String matchStr = "^([A-Za-z]:)([\\\\|\\/][^/:;,*?<>|\\\\]+)*$";
// Modified by wubo on 2010/10/28 for V2.1対応  End
		return srcPath.matches(matchStr);
	}
// Added by wubo on 2010/08/30 for V2.1対応 End

	/**
	 * 検索インデックス更新処理の開始時刻をチェック
	 *
	 * @param crontab
	 * @return
	 */
	public static boolean isCrontab(String crontab) {
		try {
			new CronExpression(crontab);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
