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
import org.json.JSONException;
import org.json.JSONObject;

public class Source {
	/**
	 * プロジェクト識別子
	 */
	private String project = "";
	/**
	 * URLエンコードしたファイルのパス
	 */
	private String path = "";
	/**
	 * コードの言語種別
	 */
	private String lang = "";
	/**
	 * ファイルサイズ
	 */
	private long size;
	/**
	 * ファイルの行数
	 */
	private long lines;
	/**
	 * ファイルのハッシュ値
	 */
	private String digest = "";
	/**
	 * データベースの作成時刻
	 */
	private Date ctime;
	/**
	 * ファイルの更新時刻
	 */
	private Date mtime;
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getLines() {
		return lines;
	}
	public void setLines(long lines) {
		this.lines = lines;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public Date getCtime() {
		return ctime;
	}
	public void setCtime(Date ctime) {
		this.ctime = ctime;
	}
	public Date getMtime() {
		return mtime;
	}
	public void setMtime(Date mtime) {
		this.mtime = mtime;
	}

	public JSONObject toJSONObject() throws JSONException {
                JSONObject json = new JSONObject();
                json.put("path", getPath());
                json.put("lang", getLang());
                json.put("size", getSize());
                json.put("lines", getLines());
                json.put("mtime", getMtime().getTime());
                return json;
	}
}
