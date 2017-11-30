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

package jp.co.sra.codedepot.scm.bo;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;

/**
 * バージョン管理インターフェース
 * @author sra
 *
 */
public interface VersionManage {
	/**
	 * バージョン管理リポジトリへの接続
	 * @param project　プロジェクト情報
	 * @throws ScmException　処理異常
	 */
	public void connect(ProjectInfoEntity project)throws ScmException;
	/**
	 * ファイルのチェックアウト処理
	 * @param project　プロジェクト情報
	 * @param localPath　テンポラリディレクトリ
	 * @throws ScmException　処理異常
	 */
	public void checkout(ProjectInfoEntity project, String localPath) throws ScmException;
	/**
	 * ファイルのアップデート処理
	 * @param project プロジェクト情報
	 * @param localPath テンポラリディレクトリ
	 * @throws ScmException 処理異常
	 */
	public void update(ProjectInfoEntity project, String localPath) throws ScmException;
	/**
	 * チェックアウト/アップデート判断
	 * @param project プロジェクト情報
	 * @param localPath テンポラリディレクトリ
	 * @return チェックアウトフラッグ
	 */
	public boolean isCheckout(ProjectInfoEntity project, String localPath);
}
