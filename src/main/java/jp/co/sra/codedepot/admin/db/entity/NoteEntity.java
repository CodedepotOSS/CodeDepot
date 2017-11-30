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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.io.IOException;

import jp.co.sra.codedepot.admin.util.APConst;

/***
 * ノート一覧画面のbean
 * @author sra
 *
 */
public class NoteEntity implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * シーケンス番号
	 */
	private int id;

    /**
	 *開始行数
	 */
	private int linefrom;

    /**
	 *終了行数
	 */
	private int lineto;

    /**
	 * 本文
	 */
	private String contents;
	/**
	 * 作成者
	 */
	private int cuserid;

	/**
	 * 更新時刻
	 */
    private String mtime;

    /**
	 * ログイン名
	 */
	private String username;

    /**
	 * Email
	 */
	private String email;

	/**
	 * 公開フラグ
	 */
	private boolean publicFlag;

	/**
	 * ファイル識別番号
	 */
	private int fid;

	/**
	 * ファイルパス
	 */
	private String path;

	/**
	 * プロジェクト名
	 */
	private String project;

	/**
	 * プロジェクトid
	 */
	private String pid;

	/**
	 * 更新者
	 */
	private int muserid;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the linefrom
	 */
	public int getLinefrom() {
		return linefrom;
	}

	/**
	 * @param linefrom the linefrom to set
	 */
	public void setLinefrom(int linefrom) {
		this.linefrom = linefrom;
	}

	/**
	 * @return the lineto
	 */
	public int getLineto() {
		return lineto;
	}

	/**
	 * @param lineto the lineto to set
	 */
	public void setLineto(int lineto) {
		this.lineto = lineto;
	}

	/**
	 * @return the contents
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * @param contents the contents to set
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

    /**
	 * @return the cuserid
	 */
	public int getCuserid() {
		return cuserid;
	}

	/**
	 * @param cuserid the cuserid to set
	 */
	public void setCuserid(int cuserid) {
		this.cuserid = cuserid;
	}

	/**
	 * @return the mtime
	 */
	public String getMtime() {
		return mtime;
	}

	/**
	 * @param mtime the mtime to set
	 */
	public void setMtime(String mtime) {
		this.mtime = mtime;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the user email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the users email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the publicFlag
	 */
	public boolean isPublicFlag() {
		return publicFlag;
	}

	/**
	 * @param publicFlag the publicFlag to set
	 */
	public void setPublicFlag(boolean publicFlag) {
		this.publicFlag = publicFlag;
	}

	/**
	 * @return the fid
	 */
	public int getFid() {
		return fid;
	}

	/**
	 * @param fid the fid to set
	 */
	public void setFid(int fid) {
		this.fid = fid;
	}

	/**
	 * @return the muserid
	 */
	public int getMuserid() {
		return muserid;
	}

	/**
	 * @param muserid the muserid to set
	 */
	public void setMuserid(int muserid) {
		this.muserid = muserid;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the project id
	 */
	public String getPid() {
		return this.pid;
	}

	/**
	 * @param path the project id to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return this.project;
	}

	/**
	 * @param path the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/***
	 *
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toNoteJsonLists() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", getId());
		json.put("fid", getFid());
		json.put("linefrom", getLinefrom());
		json.put("lineto", getLineto());
		json.put("contents", getContents());
		json.put("cuserid", getCuserid());
		json.put("mtime", getMtime());
		json.put("username", getUsername());
		json.put("email", getEmail());
		if (getPid() != null) {
                        json.put("pid", getPid());
		}
		if (getProject() != null) {
                        json.put("project", getProject());
		}
		if (getPath() != null) {
                    	json.put("path", getPath());
                }
		return json;
	}

	/***
	 *
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toNoteListJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", getId());
		json.put("linefrom", getLinefrom());
		json.put("lineto", getLineto());
		json.put("contents", getContents());
		json.put("publicFlag", isPublicFlag());
		return json;
	}
}
