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
import java.util.Map;
import java.util.HashMap;

import java.sql.SQLException;
import com.ibatis.sqlmap.client.SqlMapClient;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.scm.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.admin.util.DBConst;

/**
 * プロジェクト情報の取得処理を実現する。
 * @author fenms
 */
public class ProjectDaoImpl implements ProjectDao {
	@Override
	public ProjectInfoEntity getAllProjectInfo(String title) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (ProjectInfoEntity) sqlmap.queryForObject("project.selectAllProjectInfo", title);
	}

        @Override
        public int updateIndexedTime(String title, Date utime) throws SQLException {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(DBConst.PROJECT_NAME, title);
                map.put(DBConst.PROJECT_INDEXED_AT, utime);

                SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
                int updateCount = sqlmap.update("project.updateIndexTime", map);

                return updateCount;
        }

}
