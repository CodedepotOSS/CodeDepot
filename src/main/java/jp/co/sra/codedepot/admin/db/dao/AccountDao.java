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
package jp.co.sra.codedepot.admin.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.co.sra.codedepot.admin.db.entity.AccountEntity;

public interface AccountDao {

	/**
	 * プロジェクト管理者の情報を削除する
	 *
	 * @param usernames アカウント名
	 * @param muserid 更新者
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	public int deleteAccount(String usernames, Integer muserid) throws SQLException;

	/**
	 * プロジェクト管理者の情報を取得する
	 *
	 * @param manager
	 * @return
	 * @throws Exception
	 */
	public ArrayList<AccountEntity> queryManagerList(String manager) throws Exception;

	/**
	 * 権限ユーザの情報を取得する
	 *
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ArrayList<AccountEntity> queryUserList(String user) throws Exception;

	/***
	 * ログインの情報を取得する
	 * @param username ログイン名
	 * @return ログインの情報
	 * @throws SQLException
	 */
	public  AccountEntity getLoginInfo(String username)throws SQLException;

	/***
	 * アクセス権限の情報を取得する
	 * @param userid ログインID
	 * @return アクセス権限の情報
	 * @throws SQLException
	 */
	public  List<String> getPermitPrjInfo(int userid)throws SQLException;

	/***
	 * アカウントのパスワード情報を変更する
	 * @param bean アカウントのbean
	 * @return
	 * @throws SQLException
	 */
	public int updatePersonPwd(AccountEntity bean) throws SQLException;

	/***
	 * アカウント情報を変更する
	 * @param bean アカウントのbean
	 * @return
	 * @throws SQLException
	 */
	public int updatePersonInfo(AccountEntity bean) throws SQLException;

	/**
	 * データベースのユーザ管理テーブルから指定されたアカウント名に対応するデータを抽出する
	 *
	 * @param username アカウント名
	 * @return 指定されたアカウント名の数量
	 * @throws SQLException SQL例外
	 */
	public int getMemberByUsername(String username)throws SQLException;

	/**
	 * アカウント情報がデータベースのアカウント情報テーブルに登録される
	 *
	 * @param ae アカウント対象
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	public int insertMember(AccountEntity ae)throws SQLException;

	/**
	 * アカウント情報がデータベースのアカウント情報テーブルに登録される
	 *
	 * @param username アカウント名
	 * @param note コメント
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	public int registMember(String username, String email, String note)throws SQLException;

	/**
	 * 画面のアカウント名テキストボックスにより、アカウント情報が変更される
	 *
	 * @param ae アカウント対象
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	public int updateMember(AccountEntity ae)throws SQLException;

	/***
	 * アカウント詳細情報を取得する
	 * @param name アカウント名
	 * @return アカウント詳細情報
	 * @throws SQLException
	 */
	public AccountEntity getAccountDetailInfo(String name)throws SQLException;

	/***
	 * パスワード情報を取得する
	 * @param loginID ログインID
	 * @return パスワード情報
	 * @throws SQLException
	 */
	public String getPwdInfo(int loginID)throws SQLException;
}
