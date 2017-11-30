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

import java.util.Date;
import java.sql.SQLException;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;

/**
 * プロジェクト情報の取得処理のインターフェースである。
 *
 * @author fenms
 */
public interface ProjectDao {


	/***
	 * プロジェクト情報を取得する。
	 *
	 * @param name
	 *            識別子名
	 * @return プロジェクト情報
	 * @throws SQLException
	 *             データベース異常
	 */
	public ProjectInfoEntity getAllProjectInfo(String title) throws SQLException;

	/***
	 * プロジェクト情報を取得する。
	 *
	 * @param name
	 *            識別子名
	 * @param mtime
	 *            更新時間
	 * @return 更新件数
	 * @throws SQLException
	 *             データベース異常
	 */
	public int updateIndexedTime(String title, Date utime) throws SQLException;
}
