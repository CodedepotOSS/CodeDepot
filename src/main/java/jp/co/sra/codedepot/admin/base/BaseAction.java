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
package jp.co.sra.codedepot.admin.base;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(BaseAction.class);
	protected ServletConfig config = null;
	/** 成功場合の遷移先 */
	protected String successURL = StringUtils.EMPTY_STR;
	/** 失敗場合の遷移先 */
	protected String failureURL = StringUtils.EMPTY_STR;
	/** ログイン画面の遷移先 */
	protected String loginURL = StringUtils.EMPTY_STR;

	/**
	 * コントラクタ
	 */
	public BaseAction() {
		super();
	}

	/**
	 * コントラクタ
	 *
	 * @param config
	 * ServletConfig
	 */
	public BaseAction(ServletConfig config) {
		super();
		this.config = config;
		try {
			successURL = config.getServletContext().getContextPath()
					+ StringUtils.nvl(config.getInitParameter(APConst.SUCCESS_URL));
		} catch (NullPointerException npe) {

		}
		try {
			failureURL = config.getServletContext().getContextPath()
					+ StringUtils.nvl(config.getInitParameter(APConst.FAILURE_URL));
		} catch (NullPointerException npe) {

		}
		try {
			loginURL = config.getServletContext().getContextPath()
					+ StringUtils.nvl(config.getServletContext()
							.getInitParameter(APConst.LOGIN_URL));
		} catch (NullPointerException npe) {

		}
	}

	/**
	 *
	 * アクション処理の主体
	 *
	 * @param request
	 * HttpServletRequest
	 * @param response
	 * HttpServletResponse
	 * @return 処理されたエントリ
	 */
	protected abstract BaseBean doProcess(HttpServletRequest request, HttpServletResponse response)
			throws BaseException;

	/**
	 *
	 * アクション処理のエントランス
	 *
	 * @param request
	 * HttpServletRequest
	 * @param response
	 * HttpServletResponse
	 * @return 処理されたエントリ
	 */
	public final BaseBean doAction(HttpServletRequest request, HttpServletResponse response) {

		BaseBean baseBean = null;
		try {
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			request.setCharacterEncoding(APConst.ENCODE_UTF_8);
			response.setContentType("text/html; charset=" + APConst.ENCODE_UTF_8);
			baseBean = doProcess(request, response);
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10, APConst.MSG_ACTION));
			try {
				response.getWriter().write(
						"<script type=\"text/javascript\">" + "window.location.href=\""
								+ request.getContextPath() + "/error/500.html\";" + "</script>");
			} catch (Exception ex) {
			}
		}
		return baseBean;
	}

}
