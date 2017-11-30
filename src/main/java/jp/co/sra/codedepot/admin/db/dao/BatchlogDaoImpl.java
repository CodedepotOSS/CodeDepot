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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;

import jp.co.sra.codedepot.admin.db.entity.BatchLogEntity;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

/**
 * バッチ処理ログ情報の取得処理を実現するクラス。
 * @author fenms
 */
public class BatchlogDaoImpl implements BatchlogDao {

	/** バッチ処理ログ情報の検索SQL文ID */
	public static final String SELECT_LOG_INFO_SQL_ID = DBConst.BATCHLOG + ".selectLogList";
	/** バッチ処理ログ件数の検索SQL文ID */
	public static final String SELECT_TOTAL_COUNT_SQL_ID = DBConst.BATCHLOG + ".selectLogCount";

	/** プロジェクト情報名情報の検索SQL文ID */
	private static final String SELECT_TITLE_SQL_ID = DBConst.BATCHLOG + ".selectProjectTitle";

	@SuppressWarnings("unchecked")
	@Override
	public List<BatchLogEntity> getProjectTitleList(Integer userId, int userGroup) throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		if(APCodeBook.ROLE_LEVEL_MANAGER_CODE == userGroup){
			// プロジェクト管理者
			projParamMap.put(DBConst.PROJECT_ADMIN, userId);
		}else{
			// システム管理者の場合、パラメータがない。
		}
		return sqlmap.queryForList(SELECT_TITLE_SQL_ID, projParamMap);
	}
}
