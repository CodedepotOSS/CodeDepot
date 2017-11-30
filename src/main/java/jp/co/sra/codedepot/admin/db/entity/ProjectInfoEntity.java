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
package jp.co.sra.codedepot.admin.db.entity;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;

import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ProjectInfoEntity implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * シーケンス番号
	 */
	private String id;

	/**
	 * プロジェクト識別子名
	 */
	private String name;

	/**
	 * プロジェクトの題名
	 */
	private String title;

	/**
	 * プロジェクトの説明
	 */
	private String description;

	/**
	 * ライセンス名
	 */
	private String license;

	/**
	 * ポータルのＵＲＬ
	 */
	private String site_url;

	/**
	 * ダウンロードサイトのＵＲＬ
	 */
	private String download_url;

	/**
	 * プロジェクトの参照方法
	 */
	private String src_type;

	/**
	 * プロジェクトのパス／URL
	 */
	private String src_path;

	/**
	 * SCMのログインユーザ名
	 */
	private String scm_user;

	/**
	 * SCMのログインパスワード
	 */
	private String scm_pass;

	/**
	 * 検索インデックス更新処理の開始時刻
	 */
	private String crontab;

	/**
	 * 検索対象除外拡張子
	 */
	private String ignores;

	/**
	 * アクセス制御の有無
	 */
	private boolean restricted;

	/**
	 * プロジェクト管理者のユーザID
	 */
	private String admin;

	/**
	 * プロジェクト管理者のユーザ名
	 */
	private String username;

	/**
	 * 更新時刻
	 */
	private Date mtime;

	/**
	 * インデックス更新時刻
	 */
	private Date utime;

	/**
	 * 実行中フラグ(1:実行中, 2:実行待ち, 3:非実行中)
	 */
	private int isRunning;

	public int isRunning() {
		return isRunning;
	}

	public void setRunning(int isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * プロジェクト識別子名を取得する。
	 *
	 * @return プロジェクト識別子名
	 */
	public String getName() {
		return name;
	}

	/**
	 * プロジェクト識別子名を設定する。
	 *
	 * @param name プロジェクト識別子名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * プロジェクトの題名を取得する。
	 *
	 * @return プロジェクトの題名
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * プロジェクトの題名を設定する。
	 *
	 * @param title プロジェクトの題名
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * プロジェクトの説明を取得する。
	 *
	 * @return プロジェクトの説明
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * プロジェクトの説明を設定する。
	 *
	 * @param description プロジェクトの説明
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * ライセンス名を取得する。
	 *
	 * @return ライセンス名
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * ライセンス名を設定する。
	 *
	 * @param license ライセンス名
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * ポータルのＵＲＬを取得する。
	 *
	 * @return ポータルのＵＲＬ
	 */
	public String getSite_url() {
		return site_url;
	}

	/**
	 * ポータルのＵＲＬを設定する。
	 *
	 * @param site_url ポータルのＵＲＬ
	 */
	public void setSite_url(String site_url) {
		this.site_url = site_url;
	}

	/**
	 * ダウンロードサイトのＵＲＬを取得する。
	 *
	 * @return ダウンロードサイトのＵＲＬ
	 */
	public String getDownload_url() {
		return download_url;
	}

	/**
	 * ダウンロードサイトのＵＲＬを設定する。
	 *
	 * @param download_url ダウンロードサイトのＵＲＬ
	 */
	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	/**
	 * プロジェクトの参照方法を取得する。
	 *
	 * @return プロジェクトの参照方法
	 */
	public String getSrc_type() {
		return src_type;
	}

	/**
	 * プロジェクトの参照方法を設定する。
	 *
	 * @param src_type プロジェクトの参照方法
	 */
	public void setSrc_type(String src_type) {
		this.src_type = src_type;
	}

	/**
	 * プロジェクトのパス／URLを取得する。
	 *
	 * @return プロジェクトのパス／URL
	 */
	public String getSrc_path() {
		return src_path;
	}

	/**
	 * プロジェクトのパス／URLを設定する。
	 *
	 * @param src_path プロジェクトのパス／URL
	 */
	public void setSrc_path(String src_path) {
		this.src_path = src_path;
	}

	/**
	 * SCMのログインユーザ名を取得する。
	 *
	 * @return SCMのログインユーザ名
	 */
	public String getScm_user() {
		return scm_user;
	}

	/**
	 * SCMのログインユーザ名を設定する。
	 *
	 * @param scm_user SCMのログインユーザ名
	 */
	public void setScm_user(String scm_user) {
		this.scm_user = scm_user;
	}

	/**
	 * SCMのログインパスワードを取得する。
	 *
	 * @return SCMのログインパスワード
	 */
	public String getScm_pass() {
		return scm_pass;
	}

	/**
	 * SCMのログインパスワードを設定する。
	 *
	 * @param scm_pass SCMのログインパスワード
	 */
	public void setScm_pass(String scm_pass) {
		this.scm_pass = scm_pass;
	}

	/**
	 * 検索インデックス更新処理の開始時刻を取得する。
	 *
	 * @return 検索インデックス更新処理の開始時刻
	 */
	public String getCrontab() {
		return crontab;
	}

	/**
	 * 検索インデックス更新処理の開始時刻を設定する。
	 *
	 * @param crontab 検索インデックス更新処理の開始時刻
	 */
	public void setCrontab(String crontab) {
		this.crontab = crontab;
	}

	/**
	 * 検索対象除外拡張子を取得する。
	 *
	 * @return 検索対象除外拡張子
	 */
	public String getIgnores() {
		return ignores;
	}

	/**
	 * 検索対象除外拡張子を設定する。
	 *
	 * @param ignores 検索除外拡張子
	 */
	public void setIgnores(String ignores) {
		this.ignores = ignores;
	}

	/**
	 * プロジェクト管理者のユーザ名を取得する。
	 *
	 * @return プロジェクト管理者のユーザ名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * プロジェクト管理者のユーザ名を設定する。
	 *
	 * @param username プロジェクト管理者のユーザ名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * アクセス制御の有無を取得する。
	 *
	 * @return アクセス制御の有無
	 */
	public String getRestricted() {
		return restricted ? APConst.RESTRICTED_TRUE_STR : APConst.RESTRICTED_FALSE_STR;
	}
	public String getPermission() {
		return restricted ? APConst.PERMISSION_RESTRICTED : APConst.PERMISSION_PUBLIC;
	}

	/***
	 * アクセス制御の有無を取得する。
	 * @return アクセス制御の有無
	 */
	public boolean getRestrictedBooleanValue() {
		return restricted;
	}
	/**
	 * アクセス制御の有無を設定する。
	 *
	 * @param restricted アクセス制御の有無
	 */
	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	/**
	 * JSON形式に変換する
	 *
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", getName());
		json.put("title", getTitle());
		if (!StringUtils.isEmpty(getDescription())) {
			json.put("description", getDescription());
		} else {
			json.put("description", "");
		}

		if (!StringUtils.isEmpty(getUsername())) {
			json.put("username", getUsername());
		} else {
			json.put("username", "");
		}
		json.put("restricted", getRestricted());
		if (!StringUtils.isEmpty(getCrontab())) {
			json.put("crontab", getCrontab());
		} else {
			json.put("crontab", "");
		}
		if (!StringUtils.isEmpty(getSite_url())) {
			json.put("site_url", getSite_url());
		} else {
			json.put("site_url", "");
		}
		if (!StringUtils.isEmpty(getDownload_url())) {
			json.put("download_url", getDownload_url());
		} else {
			json.put("download_url", "");
		}
		if (!StringUtils.isEmpty(getIgnores())) {
			json.put("ignores", getIgnores());
		} else {
			json.put("ignores", "");
		}

		json.put("license", getLicense());
		json.put("isRunning", isRunning());

		if (getIndexed_at() != null) {
			SimpleDateFormat format = new SimpleDateFormat(APConst.DATE_FORMAT_YYMMDDHHMMSS);
			json.put("utime", format.format(getIndexed_at()));
		} else {
			json.put("utime", "");
		}
		return json;
	}

	/**
	 * シーケンス番号を取得する。
	 *
	 * @return シーケンス番号
	 */
	public String getId() {
		return id;
	}

	/**
	 * シーケンス番号を設定する。
	 *
	 * @param id シーケンス番号
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * プロジェクト管理者のユーザIDを取得する。
	 *
	 * @return プロジェクト管理者のユーザID
	 */
	public String getAdmin() {
		return admin;
	}


	/**
	 * プロジェクト管理者のユーザIDを設定する。
	 *
	 * @param admin プロジェクト管理者のユーザID
	 */
	public void setAdmin(String admin) {
		this.admin = admin;
	}


	/**
	 * 更新時刻を取得する。
	 *
	 * @return 更新時刻
	 */
	public Date getMtime() {
		return mtime;
	}


	/**
	 * 更新時刻を設定する。
	 *
	 * @param mtime 更新時刻
	 */
	public void setMtime(Date mtime) {
		this.mtime = mtime;
	}

	/**
	 * インデックス更新時刻を取得する。
	 *
	 * @return 更新時刻
	 */
	public Date getIndexed_at() {
		return utime;
	}

	/**
	 * インデックス更新時刻を設定する。
	 *
	 * @param mtime 更新時刻
	 */
	public void setIndexed_at(Date utime) {
		this.utime = utime;
	}
}
