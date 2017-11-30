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
package jp.co.sra.codedepot.admin.account.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.context.ContextFactoryLoader;
import jp.co.sra.codedepot.admin.context.RequestContext;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.admin.util.CodeProcess;

import org.json.JSONObject;
import org.json.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ログイン画面（LDAP認証機能付け）
 *
 * @author sra
 *
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(LoginServlet.class);

	/** 成功場合の遷移先 */
	protected String successURL = "";
	/** 失敗場合の遷移先 */
	protected String failureURL = "";
	/** ログイン画面の遷移先 */
	protected String loginURL = "";
	// パラメータ.アカウント名
	private static final String param_userName = "userName";
	// パラメータ.パスワード名
	private static final String param_userPwd = "userPwd";
	// パラメータ.バッチ
	private static final String param_json = "json";
	// エラーメッセージのリスト名
	private static final String param_errorMsgList = "errorMsgList";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			successURL =  getServletContext().getContextPath() + getInitParameter(APConst.SUCCESS_URL);
		} catch (NullPointerException npe) {

		}
		try {
			failureURL = getInitParameter(APConst.FAILURE_URL);
		} catch (NullPointerException npe) {

		}
		logger.debug("successURL = " + successURL);
		logger.debug("failureURL = " + failureURL);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			try {
				request.setCharacterEncoding(APConst.ENCODE_UTF_8);
				response.setContentType("text/html; charset=" + APConst.ENCODE_UTF_8);
			} catch (UnsupportedEncodingException e) {
				logger.error(BaseException.getStackTraceStr(e));
			}
			logger.debug("successURL = " + successURL);
			logger.debug("failureURL = " + failureURL);
			logger.debug("loginURL = " + loginURL);
			doProcess(request, response);
		} catch (BaseException be) {
			logger.error(BaseException.getStackTraceStr(be));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_SERVLET));
			response.reset();
			try {
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (IOException e) {
			}
		} catch (Exception e) {
			try {
				response.getWriter().print("");
			} catch (IOException ioe) {
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}

	/***
	 * ログイン画面（LDAP認証機能付け）のProcess
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response)throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));

		// ログイン名
		String loginName = "";
		// パスワード
		String loginPwd = "";
		// エラーメッセージのリスト
		List<String> errorMsgList = new ArrayList<String>();
		// 画面遷移フラグ
		boolean doFoward = true;

		try {
			/* 画面遷移フラグのチェック */
			String param = request.getParameter(param_json);
			if (!StringUtils.isEmpty(param) && "true".equalsIgnoreCase(param)) {
				doFoward = false;
			}

			/* BASIC 認証の取得 */
			List <String> auth = getAuthorization(request);
			if (auth == null) {
				// ログイン名を取得する
				loginName = (String) request.getParameter(param_userName);
				// ユーザ名は大文字／小文字を区別しない
				if (!StringUtils.isEmpty(loginName))
					loginName = loginName.trim().toLowerCase();
				// パスワードを取得する
				loginPwd = (String) request.getParameter(param_userPwd);
			} else {
				// ログイン名を取得する
				loginName = auth.get(0);
				// パスワードを取得する
				loginPwd = auth.get(1);
			}

			// ログイン名とパスワードのチェックを行う。
			boolean isCheckedOk = dologinInfoCheck(loginName, loginPwd, errorMsgList);

			// 入力したの値をチェック失敗の場合
			String forwardUrl = successURL;
			if (!isCheckedOk) {
				forwardUrl = failureURL;
				request.setAttribute(param_errorMsgList, errorMsgList);
			} else {
				// ログイン認証を行う
				isCheckedOk = loginLogicHandle(loginName, loginPwd, errorMsgList);
				if (isCheckedOk) {
					saveSessionInfo(request, loginName);
				} else {
					forwardUrl = failureURL;
					request.setAttribute(param_errorMsgList, errorMsgList);
				}
			}

			// チェック成功の場合
			if(isCheckedOk){
				// forwardUrlにより、画面を遷移する
				if (doFoward) {
					response.sendRedirect(forwardUrl);
				} else {
					response.setStatus(HttpServletResponse.SC_OK);
					JSONObject json = new JSONObject();
					json.put("status", "ok");
					json.put("user", loginName);
					HttpSession session = request.getSession();
					json.put("sessionId", session.getId());
					response.getWriter().print(json);
				}
			}else{
				if (doFoward) {
					// forwardUrlにより、画面を遷移する
					RequestDispatcher dispatcher = request.getRequestDispatcher(forwardUrl);
					// ユーザ名
					String loginNameParam = (String) request.getParameter(param_userName);
					request.setAttribute(APConst.PARAM_USERNAME, loginNameParam);

					dispatcher.forward(request, response);
				} else {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					JSONObject json = new JSONObject();
					json.put("status", "error");
					if (errorMsgList.size() > 0) {
						StringBuilder sb = new StringBuilder();
						for (String err : errorMsgList) {
							if (sb.length() > 0) {
  								sb.append("\n");
							}
  							sb.append(err);
						}
						json.put("reason", sb.toString());
					}
					response.getWriter().print(json);
				}
			}

		} catch (Exception e) {
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/***
	 * ログイン情報の保存
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param loginName
	 */
	private void saveSessionInfo(HttpServletRequest request, String loginName) throws Exception {
		AccountDao dao = new AccountDaoImpl();
		AccountEntity bean = dao.getLoginInfo(loginName);

		// アクセス権限の情報を取得する
		List<String> permitPrjList = getPermitPrjInfo(bean);
		// 取得した情報をセッションに保存する
		HttpSession session = request.getSession();
		// ログインID
		session.setAttribute(APConst.SESSION_LOGIN_ID, bean.getId());
		// ログイン名
		session.setAttribute(APConst.SESSION_LOGIN_NAME, bean.getUsername());
		// ログイン権限
		session.setAttribute(APConst.SESSION_LOGIN_ROLE, bean.getRole());
		// ユーザのデフォルト検索言語
		session.setAttribute(APConst.SESSION_LOGIN_LANG, bean.getDeflang());
		// アクセス権限情報のリスト
		session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST,permitPrjList);
		// アクセス権限の情報取得した日時
		session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJTIME,System.currentTimeMillis());

        // SESSION_AVAIL_LANG OPEN FOR ALL
        session.setAttribute(APConst.SESSION_AVAIL_LANG, APCodeBook.AVAIL_LANGUAGE_CODE);

		RequestContext rc = ContextFactoryLoader.getFactory().getRequestContext();
		if (null != rc) {
			rc.setAttribute(APConst.SESSION_LOGIN_NAME, bean.getUsername());
			rc.setAttribute(APConst.SESSION_CLIENT_IP,request.getRemoteHost());
		}
	}

	/***
	 * ログイン画面のロジック
	 *
	 * @param loginName
	 * @return チェック成功: true チェック失敗: false
	 */
	private boolean loginLogicHandle(String loginName, String loginPwd, List<String>errorMsgList) throws Exception {
		AccountEntity bean = null;
		AccountDao dao = null;
		bean = null;

		boolean isCheckedOk = true;
		try {
			// LDAP 認証
			String ldapFileName = APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_LDAPAUTH;
			Properties ldapProps = CommonUtil.getPropertiesContext(ldapFileName);
			boolean isLdapAuth = null != ldapProps && ldapProps.size() != 0
				&& ldapProps.get("LDAP_AUTH") != null
                                && APConst.LDAP_AUTH_ON.equals(ldapProps.get("LDAP_AUTH").toString().toLowerCase());
			boolean isLdapRegist = null != ldapProps && ldapProps.size() != 0
				&& ldapProps.get("LDAP_REGIST") != null
				&& APConst.LDAP_AUTH_ON.equals(ldapProps.get("LDAP_REGIST").toString().toLowerCase());

			// HTTP 認証
			String httpFileName = APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_HTTPAUTH;
			Properties httpProps = CommonUtil.getPropertiesContext(httpFileName);
			boolean isHttpAuth = null != httpProps && httpProps.size() != 0
				&& httpProps.get("HTTP_AUTH") != null
                                && APConst.HTTP_AUTH_ON.equals(httpProps.get("HTTP_AUTH").toString().toLowerCase());
			boolean isHttpRegist = null != httpProps && httpProps.size() != 0
				&& httpProps.get("HTTP_REGIST") != null
				&& APConst.HTTP_AUTH_ON.equals(httpProps.get("HTTP_REGIST").toString().toLowerCase());

			dao = new AccountDaoImpl();
			bean = dao.getLoginInfo(loginName);

			if (null == bean && !(isLdapAuth && isLdapRegist) && !(isHttpAuth && isHttpRegist)) {
				errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
				isCheckedOk = false;
			} else if (null != bean && bean.getActive() != true) {
				errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
				isCheckedOk = false;
			} else {

				// LDAP認証の場合
				if (isLdapAuth) {
					boolean ldapValidationFlag = IsLdapValidationOk(ldapProps, loginName, loginPwd);
					// LDAP認証成功の場合
					if (ldapValidationFlag) {
						isCheckedOk = true;
						if (bean == null) {
							dao.registMember(loginName, "", "LDAP authentication");
							bean = dao.getLoginInfo(loginName);
						}
					} else if (bean == null) {
						errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
						isCheckedOk = false;
					} else {
						// パスワードチェックを行う
						isCheckedOk = isPasswordEqual(bean, loginPwd, errorMsgList);
					}
				// HTTP認証の場合
				} else if (isHttpAuth) {
					boolean httpValidationFlag = IsHttpValidationOk(httpProps, loginName, loginPwd);
					// HTTP認証成功の場合
					if (httpValidationFlag) {
						isCheckedOk = true;
						if (bean == null) {
							dao.registMember(loginName, "", "HTTP authentication");
							bean = dao.getLoginInfo(loginName);
						}
					} else if (bean == null) {
						errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
						isCheckedOk = false;
					} else {
						// パスワードチェックを行う
						isCheckedOk = isPasswordEqual(bean, loginPwd, errorMsgList);
					}
				} else {
					// パスワードチェックを行う
					isCheckedOk = isPasswordEqual(bean, loginPwd, errorMsgList);
				}
			}
		} catch (SQLException e) {
			throw e;
		}
		return isCheckedOk;
	}

	/***
	 * LDAP認証を行う
	 *
	 * @param props
	 *            LDAPのproperty file
	 * @return チェック成功: true チェック失敗: false
	 */
	private boolean IsLdapValidationOk(Properties props, String loginName, String loginPwd) {
		// LDAP認証を行う
		return CommonUtil.ldapCertification(props, loginName, loginPwd);
	}

	/***
	 * HTTP認証を行う
	 *
	 * @param props
	 *            HTTPのproperty file
	 * @return チェック成功: true チェック失敗: false
	 */
	private boolean IsHttpValidationOk(Properties props, String loginName, String loginPwd) {
		// HTTP認証を行う
		return CommonUtil.httpCertification(props, loginName, loginPwd);
	}

	/***
	 * ログイン名とパスワードのチェックを行う。
	 *
	 * @param loginName
	 *            ログイン名
	 * @param loginPwd
	 *            パスワード
	 * @return チェック成功: true チェック失敗: false
	 */
	private boolean dologinInfoCheck(String loginName, String loginPwd, List<String> errorMsgList) {
		boolean isCheckedOk = true;

		// 入力したログイン名が空かどうかをチェックする
		if (StringUtils.isEmpty(loginName)) {
			errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
					APMsgParamConst.MSG_PARAM_LOGINNAME_CHAR));
			isCheckedOk = false;
		} else { // 半角英数字或は半角符号
			if (!CheckUtil.isUserName(loginName)) {
				errorMsgList.add(MessageUtil.getMessageString(
						APMsgConst.W_COM_07,
						APMsgParamConst.MSG_PARAM_LOGINNAME_CHAR,
						APMsgParamConst.MSG_PARAM_USERNAME_CHECK));
				isCheckedOk = false;
			}
		}

		// 入力したパスワードが空かどうかをチェックする
		if (StringUtils.isEmpty(loginPwd)) {
			errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
							 APMsgParamConst.MSG_PARAM_PWD_CHAR));
			isCheckedOk = false;
		} else { // 半角英数字或は半角符号
			if (!CheckUtil.isEngNumMark(loginPwd)) {
				errorMsgList.add(MessageUtil.getMessageString(
						APMsgConst.W_COM_07,
						APMsgParamConst.MSG_PARAM_PWD_CHAR,
						APMsgParamConst.MSG_PARAM_PWD_CHECK));
				isCheckedOk = false;
			}
		}
		return isCheckedOk;
	}

	/***
	 * パスワードチェックを行う
	 *
	 * @return true:チェックOK false:チェックNG
	 */
	private boolean isPasswordEqual(AccountEntity bean, String loginPwd, List<String> errorMsgList) {
		// ハッシュ化パスワードがNULLの場合
		if (StringUtils.isEmpty(bean.getPassword())) {
			errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
			return false;
		} else {
			// パスワードDigest
			String loginDigestPwd = CommonUtil.getPwdDigest(loginPwd);
			// パスワード一致を判断する
			if (!loginDigestPwd.equals(bean.getPassword())) {
				// ログイン名またはパスワードが間違っています。
				errorMsgList.add(MessageUtil.getMessageString(APMsgConst.W_MEM_08));
				return false;
			}
		}
		return true;
	}

	/***
	 * アクセス権限の情報を取得する
	 *
	 * @return アクセス権限の情報
	 */
	private List<String> getPermitPrjInfo(AccountEntity bean) {
		AccountDao dao = null;
		try {
			dao = new AccountDaoImpl();
			// アクセス権限の情報を取得する
			return dao.getPermitPrjInfo(bean.getId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private List<String> getAuthorization(HttpServletRequest req) {
		String header = req.getHeader("Authorization");
		if (header == null) {
			return null;
		}

		StringTokenizer st = new StringTokenizer(header);
		try {
            		String scheme = st.nextToken();
			if (!scheme.equalsIgnoreCase("Basic")) {
      				return null;
			}

            		String cred = st.nextToken();
			String auth = new String(CodeProcess.base64Decode(cred));
			int idx = auth.indexOf(":");
                	if (idx < 0) {
				return null;
			}

			String user = auth.substring(0, idx).trim().toLowerCase();
                	String pass = auth.substring(idx + 1).trim();

			if (user.trim().equals("") || pass.trim().equals("")) {
				return null;
			}

			List<String> list = new ArrayList<String>(2);
			list.add(user);
			list.add(pass);
			return list;
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
