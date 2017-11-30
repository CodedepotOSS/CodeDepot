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
package jp.co.sra.codedepot.admin.account.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.sra.codedepot.admin.account.servlet.AccountEditServlet;
import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;

/**
 * アカウント追加・変更画面アクションクラス
 *
 * @author sra
 *
 */
public class AccountEditAction extends BaseAction {

	/**
	 * ログ出力
	 */
	private static final Logger logger =
		LoggerFactory.getLogger(AccountEditServlet.class);

	/* (non-Javadoc)
	 * @see jp.co.sra.codedepot.admin.base.BaseAction#doProcess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));

		// 対象を取得する
		AccountEntity ae = null;

		// 遷移元モードを取得する
		String fromId = request.getParameter(APConst.PARAM_FROM_ID);

		// アカウント検索・一覧画面
		if (APConst.PAGE_MODE_G_02_01.equals(fromId)) {
			ae = new AccountEntity();
			// 遷移元モードをセットする
			ae.setFromId(fromId);
			// 処理モードをセットする
			ae.setMode(APConst.MODE_ADD);
			// アカウント対象をセッションに追加する
			request.getSession().setAttribute(APConst.SESSION_ACCOUNT_INFO, ae);
		// アカウント追加・変更確認画面
		} else if (APConst.PAGE_MODE_G_02_03.equals(fromId)) {
			ae = (AccountEntity) request.getSession().getAttribute(
					APConst.SESSION_NEW_ACCOUNT_INFO);
			if (null == ae) {
				return ae;
			}
			ae.setFromId(fromId);
		// アカウント詳細情報画面殻
		} else if (APConst.PAGE_MODE_G_02_06.equals(fromId)) {
			ae = (AccountEntity) request.getSession().getAttribute(
					APConst.SESSION_ACCOUNT_INFO);
			if (null == ae) {
				return ae;
			}
			ae.setMode(APConst.MODE_MODIFY);
			ae.setFromId(fromId);
			// 本画面にパスワード更新チェック・ボックス（checked = FALSE）を表示する。
			ae.setPwdChecked(false);
		} else {
			throw new BaseException();
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));

		return ae;
	}
}
