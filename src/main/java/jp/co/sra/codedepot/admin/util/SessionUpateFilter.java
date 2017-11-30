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
package jp.co.sra.codedepot.admin.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.net.HttpURLConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.context.ContextFactoryLoader;
import jp.co.sra.codedepot.admin.context.RequestContext;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionUpateFilter implements Filter {

	// ログ
	private static final Logger logger = LoggerFactory.getLogger(SessionUpateFilter.class);
	/**
	 * VersionUID
	 */
	private static final long serialVersionUID = 1L;
	String successURL = StringUtils.EMPTY_STR;
	String loginURL = StringUtils.EMPTY_STR;
	String loginServlet = StringUtils.EMPTY_STR;
	String checkSessionJsp = StringUtils.EMPTY_STR;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession(true);
		// セッションから、ログインユーザIDを取得する
		Integer loginID = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);
		// init(filterConfig);

		// ログイン画面、セッション期限切れではない場合
		if (null != loginID) {
			if (isLoginURI(req)) {
				res.sendRedirect(successURL);
				return;
			}
			// ログイン名とクライアントIPを保存
			RequestContext rc = ContextFactoryLoader.getFactory().getRequestContext();
			if (null != rc) {
				rc.setAttribute(APConst.SESSION_LOGIN_NAME, (String) session
						.getAttribute(APConst.SESSION_LOGIN_NAME));
				rc.setAttribute(APConst.SESSION_CLIENT_IP, request.getRemoteHost());
			}

			// セッションから、前回DBからアクセス権限を取得した時刻を取得する
			Long permissionGetTimeL = (Long) session
					.getAttribute(APConst.SESSION_LOGIN_PERMITPRJTIME);
			long now = System.currentTimeMillis();
			List<String> pidList = null;
			// 間隔
			int duringTime = 5;
			try {
				duringTime = Integer.parseInt(APProperties.getProperty(APConst.ACCESS_DURING_TIME));
			} catch (Exception e) {
				duringTime = 5;
			}
			// 時間差>5分間の場合
			if ((null == permissionGetTimeL)
					|| (duringTime * 1000 * 60 < (now - permissionGetTimeL.longValue()))) {

				AccountDao dao = new AccountDaoImpl();
				try {
					pidList = dao.getPermitPrjInfo(loginID.intValue());
					session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST, pidList);
					session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJTIME, now);
					logger.debug(" SESSION.SESSION_LOGIN_PERMITPRJLIST updated num is: "
							+ pidList.size());
				} catch (SQLException sqle) {
					logger.error(MessageUtil.getMessageString(APMsgConst.E_COM_05));
				}
			}
		} else {
			if (!isLoginURI(req) && !isExcludeURL(req)) {
				if (isAjaxURL(req)) {
                        		res.sendError(HttpURLConnection.HTTP_UNAUTHORIZED, "Session Timeout");
				} else {
					res.sendRedirect(checkSessionJsp);
				}
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (null == filterConfig)
			return;
		try {
			successURL = filterConfig.getServletContext().getContextPath()
					+ StringUtils.nvl(filterConfig.getInitParameter(APConst.SUCCESS_URL));
		} catch (NullPointerException npe) {

		}
		try {
			loginURL = filterConfig.getServletContext().getContextPath()
					+ StringUtils.nvl(filterConfig.getServletContext().getInitParameter(APConst.LOGIN_URL));
		} catch (NullPointerException npe) {

		}
		try {
			loginServlet = filterConfig.getServletContext().getContextPath()
					+ StringUtils.nvl(filterConfig.getInitParameter(APConst.LOGIN_SERVLET));
		} catch (NullPointerException npe) {

		}

		try {
			checkSessionJsp = filterConfig.getServletContext().getContextPath()
					+ StringUtils.nvl(filterConfig.getInitParameter(APConst.CHECK_SESSION_JSP));
		} catch (NullPointerException npe) {

		}
	}

	private boolean isLoginURI(HttpServletRequest req) {
		if (null == req)
			return false;
		String url = req.getRequestURI();
		if ((req.getContextPath() + "/").equals(url) || loginURL.equals(url)
				|| loginServlet.equals(url)) {
			return true;
		}
		return false;
	}

	private boolean isAjaxURL(HttpServletRequest req) {
		if (null == req)
			return false;
		String url = req.getRequestURI();
		if (url.endsWith("/") || url.endsWith(".jsp") || url.endsWith(".html")) {
			return false;
		}
		return true;
	}

	private boolean isExcludeURL(HttpServletRequest req) {
		if (null == req)
			return true;
		String url = req.getRequestURI();
		if (StringUtils.isEmpty(url))
			return true;
		if (url.matches(".*((/solr/select){1}).*")) {
			return false;
		}
		if (url.matches(".*(\\.js)$") || url.matches(".*(\\.css)$")
				|| url.matches(".*(((/img/){1})|((/images/){1})).*")
				|| url.matches(".*(checkSession\\.jsp)$")
				|| url.matches(".*((/solr/){1}).*")) {
			return true;
		}
		return false;
	}

	@Override
	public void destroy() {
	}

}
