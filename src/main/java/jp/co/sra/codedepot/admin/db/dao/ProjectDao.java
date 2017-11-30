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

import com.ibatis.sqlmap.client.SqlMapClient;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;

/**
 * プロジェクト情報の取得処理、更新処理、登録処理、削除処理のインターフェースである。
 *
 * @author fenms
 */
public interface ProjectDao {

	/**
	 * プロジェクト識別子名を取得する
	 *
	 * @param title
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ProjectInfoEntity> queryProjectList(String title, String name)
			throws Exception;

	/**
	 * 入力したアクセス権限ありユーザを登録する。
	 *
	 * @param sqlmap
	 *            トランザクション
	 * @param bean
	 *            入力したプロジェクト情報
	 * @throws SQLException
	 *             データベース更新異常
	 */
	boolean insertPermintUsers(SqlMapClient sqlmap, ProjectInfoBean bean)
			throws SQLException;

	/**
	 * 入力したプロジェクト情報を更新する。
	 *
	 * @param sqlmap
	 *            トランザクション
	 * @param projectInfo
	 *            入力したプロジェクト情報
	 * @param userId
	 *            ログインユーザ
	 * @throws Exception
	 *             異常
	 */
	boolean doProjectUpdate(SqlMapClient sqlmap, ProjectInfoBean projectInfo,
			Integer userId) throws Exception;

	/**
	 * 入力したプロジェクト情報を追加する。
	 *
	 * @param projectInfo
	 *            入力したプロジェクト情報
	 * @param userId
	 *            ログインユーザ
	 * @param sqlmap
	 *            トランザクション
	 * @throws Exception
	 *             異常
	 */
	boolean doProjectInsert(SqlMapClient sqlmap, ProjectInfoBean projectInfo,
			Integer userId) throws Exception;

	/***
	 * プロジェクト情報を取得する
	 *
	 * @param name
	 *            識別子名
	 * @return プロジェクト情報
	 * @throws SQLException
	 */
	public ProjectInfoEntity getProjectInfo(String name) throws SQLException;

	/**
	 * プロジェクト識別子名を取得する。
	 *
	 * @param name
	 *            テスト識別子名
	 * @return 識別子名
	 * @throws SQLException
	 *             データベース更新異常
	 */
	public int getProjectNameNum(String name) throws SQLException;

	/***
	 * プロジェクト情報を取得する。
	 *
	 * @param name
	 *            識別子名
	 * @return プロジェクト情報
	 * @throws SQLException
	 *             データベース異常
	 */
	public ProjectInfoEntity getAllProjectInfo(String name) throws SQLException;

	/**
	 * アクセス権限ユーザを取得する。
	 *
	 * @param name
	 *            識別子名
	 * @return プロジェクトアクセス権限ユーザ情報
	 * @throws SQLException
	 *             データベース異常
	 */
	public List<ProjectInfoEntity> getAccessUserInfo(String name)
			throws SQLException;

	/**
	 * プロジェクト情報を削除する。
	 *
	 * @param sqlmap
	 *            トランザクション
	 * @param name
	 *            識別子名
	 * @param userId
	 *            ログインユーザ
	 * @throws Exception
	 *             異常
	 */
	boolean doProjectDelete(SqlMapClient sqlmap, String name, Integer userId)
			throws Exception;

	/**
	 * ライセンス情報を取得する
	 *
	 * @param title
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ProjectInfoEntity> queryLicenseList()
			throws Exception;

	/**
	 * プロジェクト識別子名を取得する。(プロジェクト情報存在チェック)
	 *
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public String checkActive(String name) throws SQLException;

	/**
	 * 検索インデックス更新処理の開始時刻の検索
	 *
	 * @return
	 * @throws SQLException
	 */
	public List<ProjectInfoEntity> getAllCrontab() throws SQLException;

	/**
	 * 検索可能なプロジェクト名の一覧を取得する。
	 *
	 * @param mid
	 * @return
	 * @throws SQLException
	 */
	public List<ProjectInfoEntity> getPermitProjectList(Integer mid) throws SQLException;
}
