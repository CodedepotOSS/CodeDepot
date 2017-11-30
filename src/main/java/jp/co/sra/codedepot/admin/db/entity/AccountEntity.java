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

import java.sql.Timestamp;

import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountEntity extends BaseBean {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 遷移元ID
	 */
	private String fromId = "";

	/**
	 * シーケンス番号
	 */
	private Integer id;

	/**
	 * ログイン名
	 */
	private String username = "";

	/**
	 * メールアドレス
	 */
	private String email = "";

	/**
	 * ハッシュ化パスワード
	 */
	private String password = "";

	/**
	 * ハッシュ化パスワード再入力
	 */
	private String rePassword = "";

	/**
	 * ハッシュ化パスワード再入力
	 */
	private String encodePassword = "";

	/**
	 * パスワード更新時刻
	 */
	private Timestamp pwdmtime;

	/**
	 * 管理グループ番号
	 */
	private Integer role;

	/**
	 * デフォント検索言語
	 */
	private String deflang = "";

	/**
	 * 管理者コメント
	 */
	private String note = "";

	/**
	 * ログイン有効フラグ
	 */
	private boolean active = false;

	/**
	 * 削除フラグ
	 */
	private boolean delflag = false;

	/**
	 * 作成者
	 */
	private Integer cuserid;

	/**
	 * 作成時刻
	 */
	private Timestamp ctime;

	/**
	 * 更新者
	 */
	private Integer muserid;

	/**
	 * 更新時刻
	 */
	private Timestamp mtime;

	/**
	 * 画面モード
	 */
	private String mode = "";

	/**
	 * パスワード変更はCheck onかどうか
	 */
	private boolean pwdChecked;

	/**
	 * メッセージ文字列
	 */
	private String messageString = "";

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the pwdmtime
	 */
	public Timestamp getPwdmtime() {
		return pwdmtime;
	}

	/**
	 * @param pwdmtime
	 *            the pwdmtime to set
	 */
	public void setPwdmtime(Timestamp pwdmtime) {
		this.pwdmtime = pwdmtime;
	}

	/**
	 * @return the role
	 */
	public Integer getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(Integer role) {
		this.role = role;
	}

	/**
	 * @return the deflang
	 */
	public String getDeflang() {
		return deflang;
	}

	/**
	 * @param deflang
	 *            the deflang to set
	 */
	public void setDeflang(String deflang) {
		this.deflang = deflang;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @return the delflag
	 */
	public Boolean getDelflag() {
		return delflag;
	}

	/**
	 * @param delflag
	 *            the delflag to set
	 */
	public void setDelflag(Boolean delflag) {
		this.delflag = delflag;
	}

	/**
	 * @return the cuserid
	 */
	public Integer getCuserid() {
		return cuserid;
	}

	/**
	 * @param cuserid
	 *            the cuserid to set
	 */
	public void setCuserid(Integer cuserid) {
		this.cuserid = cuserid;
	}

	/**
	 * @return the ctime
	 */
	public Timestamp getCtime() {
		return ctime;
	}

	/**
	 * @param ctime
	 *            the ctime to set
	 */
	public void setCtime(Timestamp ctime) {
		this.ctime = ctime;
	}

	/**
	 * @return the muserid
	 */
	public Integer getMuserid() {
		return muserid;
	}

	/**
	 * @param muserid
	 *            the muserid to set
	 */
	public void setMuserid(Integer muserid) {
		this.muserid = muserid;
	}

	/**
	 * @return the mtime
	 */
	public Timestamp getMtime() {
		return mtime;
	}

	/**
	 * @param mtime
	 *            the mtime to set
	 */
	public void setMtime(Timestamp mtime) {
		this.mtime = mtime;
	}

	/**
	 *
	 * @return
	 */
	public String getMode() {
		return mode;
	}

	/**
	 *
	 * @param mode
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 *
	 * @return
	 */
	public boolean isPwdChecked() {
		return pwdChecked;
	}

	/**
	 *
	 * @param pwdChecked
	 */
	public void setPwdChecked(boolean pwdChecked) {
		this.pwdChecked = pwdChecked;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", getId());
		json.put("username", StringUtils.nvl(getUsername()));
		json.put("email", StringUtils.nvl(getEmail()));
		json.put("password", StringUtils.nvl(getPassword()));
		json.put("pwdmtime", getPwdmtime());
		switch (getRole().intValue()) {
		case APCodeBook.ROLE_LEVEL_USER_CODE:
			json.put("role", APConst.ROLE_LEVEL_USER);
			break;
		case APCodeBook.ROLE_LEVEL_MANAGER_CODE:
			json.put("role", APConst.ROLE_LEVEL_MANAGER);
			break;
		case APCodeBook.ROLE_LEVEL_SYSTEM_CODE:
			json.put("role", APConst.ROLE_LEVEL_SYSTEM);
			break;
		default:
			json.put("role", "");
			break;
		}

		json.put("deflang", StringUtils.nvl(getDeflang()));
		json.put("note", StringUtils.nvl(getNote()));
		if (getActive()) {
			json.put("active", APConst.USER_ACTIVE);
		} else {
			json.put("active", APConst.USER_INACTIVE);
		}
		json.put("delflag", getDelflag());
		json.put("cuserid", getCuserid());
		json.put("ctime", getCtime());
		json.put("muserid", getMuserid());
		json.put("mtime", getMtime());
		json.put("mode", StringUtils.nvl(getMode()));
		json.put("pwdChecked", isPwdChecked());
		return json;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getRePassword() {
		return rePassword;
	}

	public void setRePassword(String rePassword) {
		this.rePassword = rePassword;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setDelflag(boolean delflag) {
		this.delflag = delflag;
	}

	public String getEncodePassword() {
		return encodePassword;
	}

	public void setEncodePassword(String encodePassword) {
		this.encodePassword = encodePassword;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}
}
