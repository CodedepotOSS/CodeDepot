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
package jp.co.sra.codedepot.admin.project;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.entity.TotalSourceEntity;
import jp.co.sra.codedepot.admin.util.APConst;

/**
 * プロジェクト情報の保存用フォームBeanである。
 * @author fenms
 */
public class ProjectInfoBean extends BaseBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** アクセス権限指定ユーザ名の分離文字 */
	private static final String USER_SEPARATOR = " ";

	/** ﾓｰﾄﾞ */
	private String mode = "";
	/** プロジェクト識別子名 */
	private String name = "";
	/** プロジェクトの題名 */
	private String title = "";
	/** プロジェクトの題名(データベース取得) */
	private String titleOld = "";
	/** プロジェクトの説明 */
	private String description = "";
	/** プロジェクトのライセンス */
	private String license = "";
	/** プロジェクトポータルのＵＲＬ */
	private String site_url = "";
	/** ダウンロードサイトのＵＲＬ */
	private String download_url = "";
	/** アクセス制御の有無 */
	private boolean restricted = false;
	/** プロジェクトの指定方法 */
	private String src_type = "";
	/** プロジェクトのパス／URL */
	private String src_path = "";
	/** SCMのログインユーザ名 */
	private String scm_user = "";
	/** SCMのログインパスワード */
	private String scm_pass = "";
	/** SCMのログインパスワード再入力 */
	private String scm_passConfirm = "";
	/** 検索インデックス更新処理の開始時刻 */
	private String crontab = "";
	/** プロジェクト管理者のユーザID */
	private String adminId = "";
	/** プロジェクト管理者のユーザ名 */
	private String adminName = "";
	/** アクセス権限指定ユーザID */
	private List<String> accessUserIdList = new ArrayList<String>();
	/** アクセス権限指定ユーザ名 */
	private List<String> accessUserNameList = new ArrayList<String>();
	/** ファイル集計情報 */
	private List<TotalSourceEntity> projectSrcInfo = new ArrayList<TotalSourceEntity>();
	/** 検索インデックス更新時刻 */
	private String utime = "";
	/** 検索対象から除外するファイルの拡張子 */
	private String ignores = "";

	/**
	 * プロジェクト識別子名を取得する。
	 *
	 * @return プロジェクト識別子名
	 */
	public String getName() {
		return name == null ? null : name.trim();
	}

	/**
	 * プロジェクト識別子名を設定する。
	 *
	 * @param name
	 *            プロジェクト識別子名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * プロジェクトの題名 を取得する。
	 *
	 * @return プロジェクトの題名
	 */
	public String getTitle() {
		return title == null ? null : title.trim();
	}

	/**
	 * プロジェクトの題名を設定する。
	 *
	 * @param title
	 *            プロジェクトの題名
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * プロジェクトの説明 を取得する。
	 *
	 * @return プロジェクトの説明
	 */
	public String getDescription() {
		return description == null ? null : description.trim();
	}

	/**
	 * プロジェクトの説明を設定する。
	 *
	 * @param description
	 *            プロジェクトの説明
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * プロジェクトのライセンスを取得する。
	 *
	 * @return プロジェクトのライセンス
	 */
	public String getLicense() {
		return license == null ? null : license.trim();
	}

	/**
	 * プロジェクトのライセンスを設定する。
	 *
	 * @param license
	 *            プロジェクトのライセンス
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * プロジェクトポータルのＵＲＬを取得する。
	 *
	 * @return プロジェクトポータルのＵＲＬ
	 */
	public String getSite_url() {
		return site_url == null ? null : site_url.trim();
	}

	/**
	 * プロジェクトポータルのＵＲＬを設定する。
	 *
	 * @param siteUrl
	 *            プロジェクトポータルのＵＲＬ
	 */
	public void setSite_url(String siteUrl) {
		site_url = siteUrl;
	}

	/**
	 * ダウンロードサイトのＵＲＬを取得する。
	 *
	 * @return ダウンロードサイトのＵＲＬ
	 */
	public String getDownload_url() {
		return download_url == null ? null : download_url.trim();
	}

	/**
	 * ダウンロードサイトのＵＲＬを設定する。
	 *
	 * @param downloadUrl
	 *            ダウンロードサイトのＵＲＬ
	 */
	public void setDownload_url(String downloadUrl) {
		download_url = downloadUrl;
	}

	/**
	 * アクセス制御の有無 文字を取得する。
	 *
	 * @return 「あり」:有/「なし」:無
	 */
	public String getRestricted() {
		return restricted ? APConst.RESTRICTED_TRUE_STR
				: APConst.RESTRICTED_FALSE_STR;
	}

	/**
	 * アクセス制御の有無 を調べる。
	 *
	 * @return true:有/false:無
	 */
	public boolean isRestricted() {
		return restricted;
	}

	/**
	 * アクセス制御の有無文字から、アクセス制御の有無 を設定する。
	 *
	 * @return アクセス制御の有無文字
	 */
	public void setRestricted(String restricted) {
	    restricted = StringEscapeUtils.unescapeHtml(restricted);
		if (APConst.RESTRICTED_TRUE_STR.equals(restricted)) {
			this.restricted = true;
		} else {
			this.restricted = false;
		}
	}

	/**
	 * アクセス制御の有無 を設定する。
	 *
	 * @param restricted
	 *            アクセス制御の有無
	 */
	public void setRestrictedValue(boolean restricted) {
		this.restricted = restricted;
	}

	/**
	 * プロジェクトの指定方法を取得する。
	 *
	 * @return プロジェクトの指定方法
	 */
	public String getSrc_type() {
		return src_type == null ? null : src_type.trim();
	}

	/**
	 * プロジェクトの指定方法を設定する。
	 *
	 * @param srcType
	 *            プロジェクトの指定方法
	 */
	public void setSrc_type(String srcType) {
		src_type = srcType;
	}

	/**
	 * プロジェクトのパス／URLを取得する。
	 *
	 * @return プロジェクトのパス／URL
	 */
	public String getSrc_path() {
		return src_path == null ? null : src_path.trim();
	}

	/**
	 * プロジェクトのパス／URLを設定する。
	 *
	 * @param srcPath
	 *            プロジェクトのパス／URL
	 */
	public void setSrc_path(String srcPath) {
		src_path = srcPath;
	}

	/**
	 * SCMのログインユーザ名 を取得する。
	 *
	 * @return SCMのログインユーザ名
	 */
	public String getScm_user() {
		return scm_user == null ? null : scm_user.trim();
	}

	/**
	 * SCMのログインユーザ名 を設定する。
	 *
	 * @param scmUser
	 *            SCMのログインユーザ名
	 */
	public void setScm_user(String scmUser) {
		scm_user = scmUser;
	}

	/**
	 * SCMのログインパスワード を取得する。
	 *
	 * @return SCMのログインパスワード
	 */
	public String getScm_pass() {
		return scm_pass;
	}

	/**
	 * SCMのログインパスワードを設定する。
	 *
	 * @param scmPass
	 *            SCMのログインパスワード
	 */
	public void setScm_pass(String scmPass) {
		scm_pass = scmPass;
	}

	/**
	 * 検索インデックス更新処理の開始時刻 を取得する。
	 *
	 * @return 検索インデックス更新処理の開始時刻
	 */
	public String getCrontab() {
		return crontab == null ? null : crontab.trim();
	}

	/**
	 * 検索インデックス更新処理の開始時刻 を設定する。
	 *
	 * @param crontab
	 *            検索インデックス更新処理の開始時刻
	 */
	public void setCrontab(String crontab) {
		this.crontab = crontab;
	}

	/**
	 * プロジェクト管理者のユーザIDを取得する。
	 *
	 * @return プロジェクト管理者のユーザID
	 */
	public String getAdminId() {
		return adminId;
	}

	/**
	 * プロジェクト管理者のユーザID を設定する。
	 *
	 * @param adminId
	 *            プロジェクト管理者のユーザID
	 */
	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	/**
	 * プロジェクト管理者のユーザ名を取得する。
	 *
	 * @return プロジェクト管理者のユーザ名
	 */
	public String getAdminName() {
		return adminName == null ? null : adminName.trim();
	}

	/**
	 * プロジェクト管理者のユーザ名 を設定する。
	 *
	 * @param adminName
	 *            プロジェクト管理者のユーザ名
	 */
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	/**
	 * ユーザIDリストを取得する。
	 *
	 * @return ユーザIDリスト
	 */
	public List<String> getAccessUserIdList() {
		return accessUserIdList;
	}

	/**
	 * 分離文字繫ぐユーザID文字列 を取得する。
	 *
	 * @return 分離文字繫ぐユーザID文字列
	 */
	public String getPermitUserIdStr() {
		return getListAsStr(accessUserIdList);
	}

	/**
	 * ユーザIDリストから、 ユーザIDを設定する。
	 *
	 * @param accessUserIdList
	 *            ユーザIDリスト
	 */
	public void setAccessUserIdList(List<String> accessUserIdList) {
		this.accessUserIdList = accessUserIdList;
	}

	/**
	 * 分離文字繫ぐユーザID文字列 を取得する。
	 *
	 * @return 分離文字繫ぐユーザID文字列
	 */
	public void setPermitUserIdStr(String str) {
		accessUserIdList = getStrAsList(str);
	}

	/**
	 * ユーザ名リストを取得する。
	 *
	 * @return ユーザ名リスト
	 */
	public List<String> getAccessUserNameList() {
		return accessUserNameList;
	}

	/**
	 * ユーザ名リストから、 ユーザ名を設定する。
	 *
	 * @param accessUserNameList
	 *            ユーザ名リスト
	 */
	public void setAccessUserNameList(List<String> accessUserNameList) {
		this.accessUserNameList = accessUserNameList;
	}

	/**
	 * 分離文字繫ぐユーザ名文字列 を取得する。
	 *
	 * @return 分離文字繫ぐユーザ名文字列
	 */
	public String getPermitUserStr() {
		return getListAsStr(accessUserNameList);
	}

	/**
	 * 分離文字繫ぐユーザ情報文字列から、アクセス権限指定ユーザ名 を設定する。
	 *
	 * @param permitUserStr
	 *            分離文字繫ぐユーザ名文字列
	 */
	@SuppressWarnings("unchecked")
	public void setPermitUserStr(String permitUserStr) {
		List<String> result = new ArrayList(getStrAsList(permitUserStr));
		// DISTINCT
		List<String> userNameListLow =   new   ArrayList();
		for(int i = 0; i < result.size(); i++) {
	        if (!userNameListLow.contains(result.get(i).toLowerCase())) {
	        	userNameListLow.add(result.get(i).toLowerCase());
	        }
		}
		this.accessUserNameList = userNameListLow;
	}

	/**
	 * ユーザ情報リストから、離文字繫ぐユーザ情報文字列を取得する。
	 *
	 * @param list
	 *            ユーザ情報リスト
	 * @return 分離文字繫ぐユーザ情報文字列
	 */
	private String getListAsStr(List<?> list) {
		StringBuilder result = new StringBuilder();
		if (list != null && list.size() > 0) {
			result.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				result.append(USER_SEPARATOR);
				result.append(list.get(i));
			}
		}
		return result.toString();
	}

	/**
	 * 分離文字繫ぐユーザ情報文字列から、ユーザ情報リストを取得する。
	 *
	 * @param str
	 *            分離文字繫ぐユーザ情報文字列
	 * @return ユーザ情報リスト
	 */
	private List<String> getStrAsList(String str) {
		if (str == null || "".equals(str.trim())) {
			return new ArrayList<String>();
		}
		String userStr = str.replaceAll("\r", USER_SEPARATOR).replaceAll("\n",
				USER_SEPARATOR);
		while (userStr.indexOf(USER_SEPARATOR + USER_SEPARATOR) >= 0) {
			userStr = userStr.replaceAll(USER_SEPARATOR + USER_SEPARATOR,
					USER_SEPARATOR);
		}
		return Arrays.asList(userStr.trim().split(USER_SEPARATOR));
	}

	/**
	 * エラーがありがどうかフラグを設定する。
	 *
	 * @param isError
	 *            エラーがありがどうかフラグ
	 */
	public void setIsError(String isError) {
		if ("true".equalsIgnoreCase(isError)) {
			this.isError = true;
		} else {
			this.isError = false;
		}
	}

	/**
	 * SCMのログインパスワード再入力を取得する。
	 *
	 * @return SCMのログインパスワード再入力
	 */
	public String getScm_passConfirm() {
		return scm_passConfirm;
	}

	/**
	 * SCMのログインパスワード再入力を設定する。
	 *
	 * @param scm_passConfirm
	 *            SCMのログインパスワード再入力
	 */
	public void setScm_passConfirm(String scm_passConfirm) {
		this.scm_passConfirm = scm_passConfirm;
	}

	/**
	 * プロジェクトの題名(データベース取得)を取得する。
	 *
	 * @return プロジェクトの題名(データベース取得)
	 */
	public String getTitleOld() {
		return titleOld;
	}

	/**
	 * プロジェクトの題名(データベース取得)を設定する。
	 *
	 * @param プロジェクトの題名
	 *            (データベース取得)
	 */
	public void setTitleOld(String titleOld) {
		this.titleOld = titleOld;
	}

	/**
	 * インデクス更新時刻を取得する。
	 *
	 * @return インデクス更新時刻(データベース取得)
	 */
	public String getUtime() {
		return utime;
	}

	/**
	 * プロジェクトの題名(データベース取得)を設定する。
	 *
	 * @param プロジェクトの題名
	 *            (データベース取得)
	 */
	public void setUtime(String utime) {
		this.utime = utime;
	}

	/**
	 * 検索対象除外拡張子を取得する。
	 *
	 * @return 拡張子のリスト
	 */
	public String getIgnores() {
		return ignores == null ? null : ignores.trim();
	}

	/**
	 * 検索対象除外拡張子を設定する。
	 *
	 * @param 拡張子のリスト
	 */
	public void setIgnores(String ignores) {
		this.ignores = ignores;
	}

	/**
	 * 更新・登録のモードを取得する。
	 *
	 * @return モード
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * 更新・登録のモードを設定する。
	 *
	 * @param mode
	 *            モード
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * ファイル集計情報を設定する。
	 *
	 * @param projectSrcInfo
	 *            ファイル集計情報
	 */
	public void setProjectSrcInfo(List<TotalSourceEntity> projectSrcInfo) {
		this.projectSrcInfo = projectSrcInfo;
	}

	/**
	 * ファイル集計情報の表示文字を取得する。
	 *
	 * @return ファイル集計情報
	 */
	public List<List<String>> getSrcStrList() {
		List<List<String>> result = new ArrayList<List<String>>();
		NumberFormat  format = new DecimalFormat(APConst.INTEGER_FORMAT);
		int totalFileNum = 0;
		int totalLineNum = 0;
		// ファイルレコードの表示行
		for (TotalSourceEntity entity : projectSrcInfo) {
			List<String> row = new ArrayList<String>();
			int fileNum = entity.getNum();
			int lineNum = entity.getLines();
			row.add(entity.getLang());
			row.add(format.format(fileNum));
			row.add(format.format(lineNum));
			totalFileNum += fileNum;
			totalLineNum += lineNum;
			result.add(row);
		}
		//total行の表示内容
		List<String> total = new ArrayList<String>();
		total.add("");
		total.add(format.format(totalFileNum));
		total.add(format.format(totalLineNum));
		result.add(total);
		return result;
	}
}
