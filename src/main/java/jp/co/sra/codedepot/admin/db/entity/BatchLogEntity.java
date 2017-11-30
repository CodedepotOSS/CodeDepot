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

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.DBConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * バッチ処理ログ情報の検索結果を保存するエンティティーである。
 * @author fenms
 */
public class BatchLogEntity extends BaseBean{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** バッチ開始時刻 */
	private Date stime = null;

	/** バッチ終了時刻 */
	private Date etime = null;

	/** 処理時間 (秒) */
	private int period = 0;

	/** プロジェクト識別子名 */
	private String name = "";

	/** 終了状態 */
	private Boolean status = null;

	/** メッセージ */
	private String msg = "";

	/** プロジェクト名 */
	private String title = "";

	/** ログインユーザID */
	private Integer userid = null;

	/**
	 * バッチ開始時刻を取得する。
	 * @return バッチ開始時刻
	 */
	public Date getStime() {
		return stime;
	}

	/**
	 * バッチ開始時刻を設定する。
	 * @param stime バッチ開始時刻
	 */
	public void setStime(Date stime) {
		this.stime = stime;
	}

	/**
	 * バッチ終了時刻を取得する。
	 * @return バッチ終了時刻
	 */
	public Date getEtime() {
		return etime;
	}

	/**
	 * バッチ終了時刻を設定する。
	 * @param etime バッチ終了時刻
	 */
	public void setEtime(Date etime) {
		this.etime = etime;
	}

	/**
	 * 処理時間を取得する。
	 * @return 処理時間
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * 処理時間を設定する。
	 * @param period 処理時間
	 */
	public void setPeriod(int period) {
		this.period = period;
	}

	/**
	 * プロジェクト識別子名を取得する。
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * プロジェクト識別子名を設定する。
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 終了状態を取得する。
	 * @return 終了状態
	 */
	public Boolean getStatus() {
		return status;
	}

	/**
	 * 終了状態を設定する。
	 * @param status 終了状態
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	/**
	 * メッセージを取得する。
	 * @return メッセージ
	 */
	public String getMsg() {
		return msg == null ? null : msg.trim();
	}

	/**
	 * メッセージを設定する。
	 * @param msg メッセージ
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * プロジェクト名を取得する。
	 * @return プロジェクト名
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * プロジェクト名を設定する。
	 * @param title プロジェクト名
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * ログインユーザIDを取得する。
	 * @return ログインユーザID
	 */
	public Integer getUserid() {
		return userid;
	}

	/**
	 * ログインユーザIDを設定する。
	 * @param userid ログインユーザID
	 */
	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject json = new JSONObject();
		SimpleDateFormat format = new SimpleDateFormat(APConst.DATE_FORMAT_YYMMDDHHMMSS);
		// バッチ開始時刻
		json.put(DBConst.BATCHLOG_STIME, format.format(stime));
		// バッチ終了時刻
		json.put(DBConst.BATCHLOG_ETIME, format.format(etime));
		// 処理時間 (秒)
		json.put(DBConst.BATCHLOG_PERIOD, period);
		// プロジェクト識別子名
		json.put(DBConst.BATCHLOG_NAME, name);
		// 終了状態
		if(status == null){
			json.put(DBConst.BATCHLOG_STATUS, "");
		} else {
			json.put(DBConst.BATCHLOG_STATUS, status ? APCodeBook.STATUS_TRUE_STR : APCodeBook.STATUS_FALSE_STR);
		}
		// メッセージ
		json.put(DBConst.BATCHLOG_MSG, msg == null ? "" : msg);
		// プロジェクト名
		if (title == null) {
			json.put(DBConst.PROJECT_TITLE, "");
		} else {
			json.put(DBConst.PROJECT_TITLE, title);
		}
		return json;
	}
}
