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
package jp.co.sra.codedepot.scm.sqlmap;

import java.io.InputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import jp.co.sra.codedepot.scm.bo.Scm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class SqlMapConfig {

   private static final Logger logger = LoggerFactory.getLogger(SqlMapConfig.class);
   private static final String resource = "jp/co/sra/codedepot/scm/sqlmap/scmsqlmap.xml";

   private static SqlMapClient sqlMap;

   static {
        try {
        InputStream stream = SqlMapConfig.class.getClassLoader().getResourceAsStream(resource);

  	    sqlMap = SqlMapClientBuilder.buildSqlMapClient(stream);
	} catch (Exception e) {
	    logger.error("Cannot Initialize Database.", e);
	}
   }

   public static SqlMapClient getSqlMapClient () {
	return sqlMap;
   }

   public static DataSource getDataSource() throws java.sql.SQLException {
	return sqlMap.getDataSource();
   }

   public static Connection getConnection () throws java.sql.SQLException {
	return sqlMap.getDataSource().getConnection();
   }
}
