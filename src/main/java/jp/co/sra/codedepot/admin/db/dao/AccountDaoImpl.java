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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

import com.ibatis.sqlmap.client.SqlMapClient;

public class AccountDaoImpl implements AccountDao {

	/** プロジェクト管理者検索SQL文ID */
	private static final String SELECT_ACCOUNT_MANAGER_SQL_ID = "account.selectManager";

	/** 権限ユーザ検索SQL文ID */
	private static final String SELECT_ACCOUNT_USER_SQL_ID = "account.selectUser";

	/** ログインの情報検索SQL文ID */
	private static final String SELECT_LOGIN_SQL_ID = "account.getLoginInfo";

	/** アクセス権限の情報検索SQL文ID */
	private static final String SELECT_PERMIT_INFO_SQL_ID = "account.getPermitPrjInfo";

	/** アカウントのパスワード情報変更SQL文ID */
	private static final String UPDATE_ACCOUNT_PWD_SQL_ID = "account.updatePersonPwd";

	/** アカウント情報変更SQL文ID */
	private static final String UPDATE_ACCOUNT_INFO_SQL_ID = "account.updatePersonInfo";

	/** アカウント詳細情報検索SQL文ID */
	private static final String SELECT_ACCOUNT_DETAIL_SQL_ID = "account.getAccountDetailInfo";
	/** パスワード情報検索SQL文ID */
	private static final String SELECT_PWD_INFO_SQL_ID = "account.getPwdInfo";


	/**
	 * プロジェクト管理者の情報を削除する
	 *
	 * @param usernames アカウント名
	 * @param loginId 更新者
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	@SuppressWarnings("unchecked")
	public int deleteAccount(String usernames, Integer loginId) throws SQLException {

		// パラメータを作成する
		Map parameterMap = new HashMap();
		// 更新者
		parameterMap.put("loginId", loginId);
		// アカウント名
		parameterMap.put("usernames", usernames);

		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int count = (Integer)sqlmap.delete("account.deleteAccount", parameterMap);
		return count;
	}

	/**
	 * プロジェクト管理者の情報を取得する
	 *
	 * @param manager
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<AccountEntity> queryManagerList(String manager) throws Exception {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (ArrayList) sqlmap.queryForList(SELECT_ACCOUNT_MANAGER_SQL_ID, manager);
	}

	/**
	 * 権限ユーザの情報を取得する
	 *
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<AccountEntity> queryUserList(String user) throws Exception {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (ArrayList) sqlmap.queryForList(SELECT_ACCOUNT_USER_SQL_ID, user);
	}

	/***
	 * ログインの情報を取得する
	 * @param username ログイン名
	 * @return ログインの情報
	 * @throws SQLException
	 */
	public AccountEntity getLoginInfo(String username)throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		List<?> list = sqlmap.queryForList(SELECT_LOGIN_SQL_ID,username);
		AccountEntity accountBean = null;
		if( null != list && list.size()> 0){
			accountBean =(AccountEntity)list.get(0);
		}
		return accountBean;
	}

	/***
	 * アクセス権限の情報を取得する
	 * @param userid ログインID
	 * @return アクセス権限の情報
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public List<String> getPermitPrjInfo(int userid) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return sqlmap.queryForList(SELECT_PERMIT_INFO_SQL_ID,userid);
	}

	/***
	 * アカウントのパスワード情報を変更する
	 * @param bean アカウントのbean
	 * @return
	 * @throws SQLException
	 */
	public int updatePersonPwd(AccountEntity bean) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int count = (Integer)sqlmap.update(UPDATE_ACCOUNT_PWD_SQL_ID, bean);
		return count;
	}

	/***
	 * アカウント情報を変更する
	 * @param bean アカウントのbean
	 * @return
	 * @throws SQLException
	 */
	public int updatePersonInfo(AccountEntity bean) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int count = (Integer)sqlmap.update(UPDATE_ACCOUNT_INFO_SQL_ID, bean);
		return count;
	}

	/**
	 * データベースのユーザ管理テーブルから指定されたアカウント名に対応するデータを抽出する
	 *
	 * @param username アカウント名
	 * @return 指定されたアカウント名の数量
	 * @throws SQLException SQL例外
	 */
	@SuppressWarnings("unchecked")
	public int getMemberByUsername(String username)throws SQLException {

		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();

		// パラメータを作成する
		Map parameterMap = new HashMap();
		// アカウント名
		parameterMap.put("username", username);
		// データベースのユーザ管理テーブルから指定されたアカウント名に対応するデータを抽出する
		List dataList = (List) sqlmap.queryForList("account.getMemberByUsername", parameterMap);
		int count = (Integer) dataList.get(0);

		return count;
	}

	/**
	 * アカウント情報がデータベースのアカウント情報テーブルに登録される
	 *
	 * @param ae アカウント対象
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	@SuppressWarnings("unchecked")
	public int insertMember(AccountEntity ae)throws SQLException {

		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();

		// パラメータを作成する
		Map parameterMap = new HashMap();
		// アカウント名
		parameterMap.put("username", ae.getUsername());
		// パスワード
		parameterMap.put("password", ae.getEncodePassword());
		// メールアドレス
		parameterMap.put("email", ae.getEmail());
		// 管理グループ番号
		parameterMap.put("role", ae.getRole());
		// デフォント検索言語
		parameterMap.put("def_lang", ae.getDeflang());
		// 管理者コメント
		parameterMap.put("note", ae.getNote());
		// ログイン有効フラグ
		parameterMap.put("active", ae.getActive());
		// 作成者
		parameterMap.put("cuserid", ae.getCuserid());
		// データベースのユーザ管理テーブルから指定されたアカウント名に対応するデータを抽出する
		sqlmap.insert("account.insertMember", parameterMap);

		return 1;
	}

	/**
	 * アカウント情報がデータベースのアカウント情報テーブルに登録される
	 *
	 * @param username アカウント名
	 * @param note ノート
	 * @param ae アカウント対象
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	@SuppressWarnings("unchecked")
	public int registMember(String username, String email, String note)throws SQLException {

		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();

		// パラメータを作成する
		AccountEntity entity = new AccountEntity();
		// アカウント名
		entity.setUsername(username);
		// Emailアドレス
		entity.setEmail(email);
		// 管理者コメント
		entity.setNote(note);

		sqlmap.insert("account.registMember", entity);
		return 1;
	}

	/**
	 * 画面のアカウント名テキストボックスにより、アカウント情報が変更される
	 *
	 * @param ae アカウント対象
	 * @return 更新件数
	 * @throws SQLException SQL例外
	 */
	@SuppressWarnings("unchecked")
	public int updateMember(AccountEntity ae)throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();

		// パラメータを作成する
		Map parameterMap = new HashMap();
		// アカウント名
		parameterMap.put("username", ae.getUsername());
		if (ae.isPwdChecked() || APConst.MODE_ADD.equals(ae.getMode())) {
			// パスワード
			parameterMap.put("password", ae.getEncodePassword());
		}
		// メールアドレス
		parameterMap.put("email", ae.getEmail());
		// 管理グループ番号
		parameterMap.put("role", ae.getRole());
		// デフォント検索言語
		parameterMap.put("def_lang", ae.getDeflang());
		// 管理者コメント
		parameterMap.put("note", ae.getNote());
		// ログイン有効フラグ
		parameterMap.put("active", ae.getActive());
		// 更新者
		parameterMap.put("muserid", ae.getCuserid());
		// 画面のアカウント名テキストボックスにより、アカウント情報が変更される
		int count = sqlmap.update("account.updateMember", parameterMap);

		return count;
	}

	/***
	 * アカウント詳細情報を取得する
	 * @param name アカウント名
	 * @return アカウント詳細情報
	 * @throws SQLException
	 */
	public AccountEntity getAccountDetailInfo(String name)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		List<?> list = sqlmap.queryForList(SELECT_ACCOUNT_DETAIL_SQL_ID,name);
		AccountEntity accountBean = null;
		if( null != list && list.size()> 0){
			accountBean =(AccountEntity)list.get(0);
		}
		return accountBean;
	}

	/***
	 * パスワード情報を取得する
	 * @param loginID ログインID
	 * @return パスワード情報
	 * @throws SQLException
	 */
	public String getPwdInfo(int loginID)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient(); //
		String pwdInfo = (String)sqlmap.queryForObject(SELECT_PWD_INFO_SQL_ID,loginID);
		return pwdInfo;
	}
}
