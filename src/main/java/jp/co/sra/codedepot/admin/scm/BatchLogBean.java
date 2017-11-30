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
package jp.co.sra.codedepot.admin.scm;

import java.util.List;

import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.entity.BatchLogEntity;

/**
 * バッチ処理ログの保存用フォームBeanである。
 * @author fenms
 */
public class BatchLogBean extends BaseBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/** バッチ処理ログ情報リスト */
	private List<BatchLogEntity> batchLogList;

	/**
	 * 検索したバッチ処理ログ情報リストを設定する。
	 * @param batchLogList 検索したバッチ処理ログ情報リスト
	 */
	public void setBatchLogList(List<BatchLogEntity> batchLogList) {
		this.batchLogList = batchLogList;
	}

	/**
	 * バッチ処理ログ情報リストを取得する。
	 * @return バッチ処理ログ情報リスト
	 */
	public List<BatchLogEntity> getBatchLogList() {
		return batchLogList;
	}

}
