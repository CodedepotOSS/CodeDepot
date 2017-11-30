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
import java.util.List;

import jp.co.sra.codedepot.admin.db.entity.BatchLogEntity;

/**
 * バッチ処理ログ情報の取得処理のインターフェースである。
 *
 * @author fenms
 */
public interface BatchlogDao {

	/**
	 * 対象のプロジェクト名の表示項目を取得する。
	 * @param userId ログインユーザID
	 * @param userGroup ログインユーザグループ
	 * @return 対象のプロジェクト名の表示項目
	 * @throws Exception 異常
	 */
	public List<BatchLogEntity> getProjectTitleList(Integer userId, int userGroup) throws SQLException;
}
