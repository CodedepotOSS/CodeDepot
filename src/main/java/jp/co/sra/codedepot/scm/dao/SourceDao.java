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
package jp.co.sra.codedepot.scm.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import jp.co.sra.codedepot.scm.bo.ScmException;
import jp.co.sra.codedepot.scm.entity.Source;

import com.ibatis.sqlmap.client.SqlMapClient;

public interface SourceDao {
	/**
	 * ソースの削除
	 * @param sqlmap
	 * @param source　ソース
	 * @throws SQLException　処理異常
	 */
	public void deleteSource(SqlMapClient sqlmap, Source source)throws ScmException;
	/**
	 * ソースの削除
	 * @param sqlmap
	 * @param source　ソース
	 * @throws SQLException　処理異常
	 */
	public void deleteSourceByProject(String projectId)throws ScmException;
	/**
	 * ソースの新規
	 * @param sqlmap
	 * @param source
	 * @throws SQLException
	 */
	public void insertSource(SqlMapClient sqlmap, Source source)throws ScmException;
	/**
	 * ソースの更新
	 * @param sqlmap
	 * @param source　ソース
	 * @throws SQLException　処理異常
	 */
	public void updateSource(SqlMapClient sqlmap, Source source)throws ScmException;
	/**
	 * ソース一覧の取得
	 * @param projectId　プロジェクト識別
	 * @return　取得したソース一覧
	 * @throws SQLException　処理異常
	 */
	public ArrayList<Source> getSources(String projectId)throws ScmException;
	/**
	 * ソース数の取得
	 * @param projectId　プロジェクト識別子
	 * @return　取得したソース一覧
	 * @throws SQLException　処理異常
	 */
	public int getSourceCount()throws ScmException;

	/**
	 * ソース一覧の取得
	 * @param projectId　プロジェクト識別子
	 * @param lang　プログラム言語
	 * @return　取得したソース一覧
	 * @throws SQLException　処理異常
	 */
	public ArrayList<Source> getSources(String projectId, String lang)throws ScmException;

	/**
	 * ソース数の取得
	 * @param projectId　プロジェクト識別子
	 * @param lang　プログラム言語
	 * @return　取得したソース一覧
	 * @throws SQLException　処理異常
	 */
}
