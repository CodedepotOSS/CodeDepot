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

import jp.co.sra.codedepot.scm.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.scm.entity.BatchLog;

import com.ibatis.sqlmap.client.SqlMapClient;

public class BatchLogDaoImpl implements BatchLogDao {

	@Override
	public void deleteBatchLog(SqlMapClient sqlmap) throws SQLException {
		sqlmap.delete("batchlog.deleteBatchLog");
	}

	@Override
	public void insertBatchLog(SqlMapClient sqlmap, BatchLog batchLog)throws SQLException {
		sqlmap.insert("batchlog.insertBatchLog", batchLog);
	}

	@Override
	public int getBatchLogCount() throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (Integer)sqlmap.queryForObject("batchlog.getBatchLogCount");
	}
}
