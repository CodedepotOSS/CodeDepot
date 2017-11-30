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
package jp.co.sra.codedepot.admin.menu;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * メニュー
 *
 * @author sra
 *
 */
public class MenuAction extends BaseAction {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(MenuAction.class);

	private static final long serialVersionUID = 1L;

	public MenuAction(ServletConfig config) {
		super(config);
	}

	@Override
	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		BaseBean baseBean = new BaseBean();
		try {
			HttpSession session = request.getSession();
			session.getAttribute(APConst.SESSION_LOGIN_ROLE);
			session.getAttribute(APConst.SESSION_LOGIN_ID);
			request.setAttribute(APConst.SESSION_LOGIN_NAME,
					session.getAttribute(APConst.SESSION_LOGIN_NAME));
		} catch(Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(
				APMsgConst.I_COM_09, APConst.MSG_SERVLET));
		return baseBean;
	}

}
