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
package jp.co.sra.codedepot.admin.scm.action;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.dao.BatchlogDao;
import jp.co.sra.codedepot.admin.db.dao.BatchlogDaoImpl;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.scm.BatchLogBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;

/**
 * バッチ処理ログ表示画面のロード処理クラスである。
 * @author fenms
 */
public class LogSearchAction extends BaseAction {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(LogSearchAction.class);

	@Override
	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));
		BatchLogBean bean = new BatchLogBean();
		try {
			// ログインID
			Integer userId = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ID);
			// ログイン権限
			Integer userGroup = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ROLE);
			BatchlogDao dao = new BatchlogDaoImpl();
			// バッチ処理ログ情報リストを設定する。
			bean.setBatchLogList(dao.getProjectTitleList(userId, userGroup));
		} catch (SQLException lnee) {
			// ランセンスを取得失敗
			bean.setError(true);
			bean.setErrMsg(MessageUtil.getMessageString(APMsgConst.E_COM_05));
			return bean;
		} catch (Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));
		return bean;
	}

}
