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

public class Version {
	/**
	 * 区分
	 */
	private String kind;
	/**
	 * バージョン番号
	 */
	private int vernum;
	/**
	 * 更新時刻
	 */
	private Date mtime;

	/*
	 * getter for kind
	 */
	public String getKind() {
		return this.kind;
	}
	/*
	 * setter for kind
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

        /*
	 * getter for vernum
	 */
	public int getVernum() {
		return this.vernum;
	}
	/*
	 * setter for vernum
	 */
	public void setVernum(int vernum) {
		this.vernum = vernum;
	}

        /*
	 * getter for mtime
         */
	public Date getMtime() {
		return mtime;
	}
        /*
	 * setter for mtime
         */
	public void setMtime(Date mtime) {
		this.mtime = mtime;
	}
}
