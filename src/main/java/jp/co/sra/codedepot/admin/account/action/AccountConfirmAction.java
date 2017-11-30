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

import jp.co.sra.codedepot.admin.account.servlet.AccountEditServlet;
import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アカウント追加・変更確認画面アクションクラス
 *
 * @author sra
 *
 */
public class AccountConfirmAction extends BaseAction {

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
			HttpServletResponse response) {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));

		// 対象を取得する
		AccountEntity ae = (AccountEntity) request.getSession().getAttribute(
				APConst.SESSION_NEW_ACCOUNT_INFO);

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));

		return ae;
	}
}
