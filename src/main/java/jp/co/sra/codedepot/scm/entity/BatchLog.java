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
package jp.co.sra.codedepot.scm.entity;

import java.util.Date;

public class BatchLog {
	/**
	 * バッチ開始時刻
	 */
	private Date stime;
	/**
	 * バッチ終了時刻
	 */
	private Date etime;
	/**
	 * 処理時間(秒)
	 */
	private int period;
	/**
	 * プロジェクト名
	 */
	private String project;
	/**
	 * 終了状態
	 */
	private boolean status;
	/**
	 * メッセージ
	 */
	private String msg;
	/**
	 * @return the stime
	 */
	public Date getStime() {
		return stime;
	}
	/**
	 * @param stime the stime to set
	 */
	public void setStime(Date stime) {
		this.stime = stime;
	}
	/**
	 * @return the etime
	 */
	public Date getEtime() {
		return etime;
	}
	/**
	 * @param etime the etime to set
	 */
	public void setEtime(Date etime) {
		this.etime = etime;
	}
	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}

	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

}
