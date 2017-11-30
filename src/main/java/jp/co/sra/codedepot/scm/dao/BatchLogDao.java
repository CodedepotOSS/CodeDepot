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

import jp.co.sra.codedepot.scm.entity.BatchLog;

import com.ibatis.sqlmap.client.SqlMapClient;

public interface BatchLogDao {
	/**
	 * バッチログを削除する
	 * @param sqlmap
	 * @throws SQLException
	 */
	public void deleteBatchLog(SqlMapClient sqlmap)throws SQLException;
	/**
	 * バッチログを登録する
	 * @param sqlmap
	 * @param batchLog
	 * @throws SQLException
	 */
	public void insertBatchLog(SqlMapClient sqlmap, BatchLog batchLog)throws SQLException;
	/**
	 * バッチログ数を取得する
	 * @return バッチログ数
	 * @throws SQLException
	 */
	public int getBatchLogCount()throws SQLException;
}
