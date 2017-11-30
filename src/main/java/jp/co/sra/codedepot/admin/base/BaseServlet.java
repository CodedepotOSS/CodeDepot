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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
	/** 成功場合の遷移先 */
	protected String successURL = StringUtils.EMPTY_STR;
	/** 失敗場合の遷移先 */
	protected String failureURL = StringUtils.EMPTY_STR;
	/** ログイン画面の遷移先 */
	protected String loginURL = StringUtils.EMPTY_STR;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			successURL = getServletContext().getContextPath()
					+ StringUtils.nvl(getInitParameter(APConst.SUCCESS_URL));
		} catch (NullPointerException npe) {

		}
		try {
			failureURL = getServletContext().getContextPath()
					+ StringUtils.nvl(getInitParameter(APConst.FAILURE_URL));
		} catch (NullPointerException npe) {

		}
		try {
			loginURL = getServletContext().getContextPath()
					+ StringUtils.nvl(getServletContext().getInitParameter(APConst.LOGIN_URL));
		} catch (NullPointerException npe) {

		}
	}

	protected abstract void doProcess(HttpServletRequest request, HttpServletResponse response)
			throws BaseException, Exception;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		// ログイン状態チェック
		boolean isLogin = false;
		try {
			if (null != request.getSession().getAttribute(APConst.SESSION_LOGIN_ID))
				isLogin = true;
		} catch (Exception e) {
		}
		// 未ログインの場合
		if (!isLogin) {
			try {
				response.reset();
				if (!StringUtils.EMPTY_STR.equals(loginURL)) {
					JSONObject json = new JSONObject();
					json.put(APConst.PARAM_REDIRECTURL, loginURL);
					response.getWriter().print(json);
				} else {
					response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			} catch (Exception e) {
			}
			return;
		}

		try {
			try {
				response.setHeader("Pragma", "No-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				request.setCharacterEncoding(APConst.ENCODE_UTF_8);
				response.setContentType("text/html; charset=" + APConst.ENCODE_UTF_8);
			} catch (UnsupportedEncodingException e) {
				logger.error(BaseException.getStackTraceStr(e));
			}
			logger.debug("successURL = " + successURL);
			logger.debug("failureURL = " + failureURL);
			logger.debug("loginURL = " + loginURL);
			doProcess(request, response);
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10, APConst.MSG_SERVLET));
			try {
				response.getWriter().print("");
			} catch (IOException ioe) {
			}
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
}
