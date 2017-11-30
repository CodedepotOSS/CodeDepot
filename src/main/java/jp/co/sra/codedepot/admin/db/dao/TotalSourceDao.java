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

import jp.co.sra.codedepot.admin.db.entity.TotalSourceEntity;
/***
 * ファイル集計情報のDAOインターフェイス
 *
 */
public interface TotalSourceDao {
	/***
	 * ファイル集計情報を取得する
	 * @param name 識別子名
	 * @return ファイル集計情報
	 * @throws SQLException
	 */
	ArrayList<TotalSourceEntity> getProjectSrcInfo (String name) throws SQLException;
}
