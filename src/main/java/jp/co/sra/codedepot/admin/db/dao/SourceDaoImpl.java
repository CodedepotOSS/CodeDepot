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

import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * プロジェクト情報の取得処理を実現する。
 *
 * @author sra
 *
 */
public class SourceDaoImpl implements SourceDao {

	/** プロジェクト情報テーブル.識別子名の検索SQL文ID */
	private static final String SELECT_SOURCE_FILE_ID = DBConst.SOURCE + ".selectFileCount";
	private static final String SELECT_SOURCE_LINE_ID = DBConst.SOURCE + ".selectLineCount";

	@Override
	public int fileCount() throws Exception {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (Integer) sqlmap.queryForObject(SELECT_SOURCE_FILE_ID);
	}

	@Override
	public int lineCount() throws Exception {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Object obj = sqlmap.queryForObject(SELECT_SOURCE_LINE_ID);
		if (obj != null) {
			return (Integer) sqlmap.queryForObject(SELECT_SOURCE_LINE_ID);
		} else {
			return 0;
		}
	}
}
