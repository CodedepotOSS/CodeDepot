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

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * アカウント詳細情報画面
 *
 * @author sra
 *
 */
public class AccountDetailAction extends BaseAction {
	/**
	 * ログ出力
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(AccountDetailAction.class);

	// アカウントBean
	private AccountEntity accountBean = null;

	@Override
	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));

		// アカウント名を取得する
		String accountName = request.getParameter(APConst.PARAM_USERNAME);
		try {
			// アカウント詳細情報bean
			accountBean = getAccountDetailInfo(accountName);
			// 画面ID アカウント詳細情報画面
			accountBean.setFromId(APConst.PAGE_MODE_G_02_06);
			HttpSession session = request.getSession();
			// セッションに、 アカウント情報をセットする。
			session.setAttribute(APConst.SESSION_ACCOUNT_INFO, accountBean);
		} catch (SQLException e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_ACTION));
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));
		return accountBean;
	}

	/***
	 * アカウント詳細情報を取得する
	 * @param name アカウント名
	 * @return アカウント詳細情報
	 * @throws SQLException
	 */
	private AccountEntity getAccountDetailInfo(String accountName)throws SQLException {
		AccountDao dao = null;
		AccountEntity bean = new AccountEntity();
		try {
			dao = new AccountDaoImpl();
			bean = dao.getAccountDetailInfo(accountName.trim().toLowerCase());
		} catch (SQLException e) {
			throw e;
		}
		return bean ;
	}
}
