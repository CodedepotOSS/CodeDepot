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

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 個人情報変更画面
 * @author sra
 *
 */
public class PersonInfoUpdateServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(PersonInfoUpdateServlet.class);
	// エラーメッセージのリスト
	private JSONObject errorJson = null;
	// アカウントのbean
	private AccountEntity bean = null;
	// 変更パスワードのフラグ
	private boolean isUpdatePwdFlag = true;
	// 新パスワード
	private String newPwd = "";
	// メールアドレス
	private String email = "";
	// デフォルト検索言語
	private String def_lang = "";
	// パスワード
	private final static String PARAM_UPDATE_PWD_FLAG = "updatePwdFlag";
	// 旧パスワード
	private final static String PARAM_OLD_PWD = "oldPwd";
	// 新パスワード
	private final static String PARAM_NEW_PWD = "newPwd";
	// 新パスワード再入力
	private final static String PARAM_NEW_PWD_AGAIN = "newPwdAgin";
	// メールアドレス
	private final static String PARAM_EMAIL = "email";
	// デフォルト検索言語
	private final static String PARAM_DEF_LANG = "def_lang";

	protected synchronized void doProcess(HttpServletRequest request,
			HttpServletResponse response)throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		try {
			  // 個人情報変更のロジック
			  personUpdateLogicHandle(request);

			  response.getWriter().print(errorJson);
		} catch (Exception e) {
				throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/***
	 * 個人情報変更のロジック
	 * @param request HttpServletRequest
	 */
	private void personUpdateLogicHandle(HttpServletRequest request) throws SQLException,JSONException {

		//  チェックフラグ
		boolean isCheckedOk = true;
		try{
			errorJson = new JSONObject();
			// 個人情報変更画面のチェック
			isCheckedOk = doPersonInfoUpdateCheck(request);
			// チェック成功の場合
			if(isCheckedOk){
				// 個人情報をセットする
				setBeanValue(request);
				// 変更件数
				int updateResult = 0;
				// パスワード変更或いは個人情報変更の文字
				String msgTitle = "";
				if(isUpdatePwdFlag){// パスワード変更
					updateResult = updatePersonPwd(bean);
					msgTitle = APMsgParamConst.MSG_PARAM_TITLE_PWD_CHAR;
				}else{// 個人情報変更
					updateResult = updatePersonInfo(bean);
					msgTitle = APMsgParamConst.MSG_PARAM_PERSONINFO_PWD_CHAR;

					// セッション情報の更新
					HttpSession session = request.getSession();
					session.setAttribute(APConst.SESSION_LOGIN_LANG, bean.getDeflang());
				}

				// 変更失敗の場合、エラーメッセージを表示する
				if(updateResult == 0){
					errorJson.put( APMsgConst.WARN
								 , MessageUtil.getMessageString(APMsgConst.E_COM_01
										 					   ,msgTitle
										 					   ,APConst.UPDATE_CHAR)
						         + APConst.HTML_NEW_LINE);
				}

			}
		}catch (SQLException e) {
			throw e;
		} catch (JSONException ex) {
			throw ex;
		}
	}

	/***
	 * 個人情報変更画面のチェック
	 * @param request HttpServletRequest
	 * @return
	 */
	private boolean doPersonInfoUpdateCheck(HttpServletRequest request) throws JSONException, SQLException{

		// チェック成功フラグ
		boolean isCheckedOk = true;
		// 変更パスワードのフラグを取得する
		isUpdatePwdFlag = Boolean.parseBoolean(request
				.getParameter(PARAM_UPDATE_PWD_FLAG));
		// エラーメッセージ
		StringBuffer errorMsgBuf = new StringBuffer();
		try {
			// パスワード変更
			if (isUpdatePwdFlag) {
				// 旧パスワード
				String oldPwd = (String) request.getParameter(PARAM_OLD_PWD);
				// 新パスワード
				newPwd = (String) request.getParameter(PARAM_NEW_PWD);
				// 新パスワード再入力
				String newPwdAgin = (String) request.getParameter(PARAM_NEW_PWD_AGAIN);

				// 入力した旧パスワードが空かどうかをチェックする
				if (StringUtils.isEmpty(oldPwd)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
							APMsgParamConst.MSG_PARAM_OLDPWD_CHAR));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 入力した新パスワードが空かどうかをチェックする
				if (StringUtils.isEmpty(newPwd)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
							APMsgParamConst.MSG_PARAM_NEWPWD_CHAR));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 入力した新パスワード再入力が空かどうかをチェックする
				if (StringUtils.isEmpty(newPwdAgin)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
							APMsgParamConst.MSG_PARAM_NEWPWDAGAIN_CHAR));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 旧パスワード半角英数字或は半角符号
				if(!CheckUtil.isEngNumMark(oldPwd)){
					errorMsgBuf.append( MessageUtil.getMessageString(APMsgConst.W_COM_07,
								APMsgParamConst.MSG_PARAM_OLDPWD_CHAR,
							 	APMsgParamConst.MSG_PARAM_PWD_CHECK
							 ));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 旧パスワードの文字は{1}バイト以下で入力してください。
				if(!CheckUtil.checkLength(oldPwd, DBConst.CHECK_LENGTH_PASSWORD)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
								APMsgParamConst.MSG_PARAM_OLDPWD_CHAR,
						 		String.valueOf(DBConst.CHECK_LENGTH_PASSWORD)
						 	));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}

				// 新パスワード半角英数字或は半角符号
				if(!CheckUtil.isEngNumMark(newPwd)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
								APMsgParamConst.MSG_PARAM_NEWPWD_CHAR,
								APMsgParamConst.MSG_PARAM_PWD_CHECK
							 ));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 新パスワードの文字は{1}バイト以下で入力してください。
				if(!CheckUtil.checkLength(newPwd, DBConst.CHECK_LENGTH_PASSWORD)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
								APMsgParamConst.MSG_PARAM_NEWPWD_CHAR,
						 		String.valueOf(DBConst.CHECK_LENGTH_PASSWORD)
						 	));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 新パスワードは {2} 文字以上のパスワードを入力して下さい。
				if(!CheckUtil.checkLengthAbove(newPwd, DBConst.PASS_MIN_LEN)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_10,
								APMsgParamConst.MSG_PARAM_NEWPWD_CHAR,
					 			String.valueOf(DBConst.PASS_MIN_LEN)
					 		));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 新パスワード再入力半角英数字或は半角符号
				if(!CheckUtil.isEngNumMark(newPwdAgin)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
								APMsgParamConst.MSG_PARAM_NEWPWDAGAIN_CHAR,
								APMsgParamConst.MSG_PARAM_PWD_CHECK
							 ));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}

				// 入力したパスワードが一致しません。
				if(!newPwd.equals(newPwdAgin)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_05) + APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// 新パスワードには、旧パスワード  と同じパスワードは設定できません
				if(!StringUtils.isEmpty(oldPwd) && !StringUtils.isEmpty(newPwd) && oldPwd.equals(newPwd)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_11,
									 APMsgParamConst.MSG_PARAM_NEWPWD_CHAR
								    ,APMsgParamConst.MSG_PARAM_OLDPWD_CHAR
								));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}

				HttpSession session = request.getSession();
				// ログインIDを取得する
				int loginId = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ID);
				String dbOldPwd = getPwdInfo(loginId);
				// DBにパスワード復号化
				String oldPwdDigest = CommonUtil.getPwdDigest(oldPwd);
				// 入力した旧パスワードとセッションから取得したパスワードが不一致する場合
				if( !StringUtils.isEmpty(oldPwd) && !oldPwdDigest.equals(dbOldPwd)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_COM_02,
									   APMsgParamConst.MSG_PARAM_INPUT_PWD_CHAR));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}

			} else {// 個人情報変更
				// メールアドレス
				email = (String) request.getParameter(PARAM_EMAIL);
				email = email.trim();
				// デフォルト検索言語
				def_lang = (String) request.getParameter(PARAM_DEF_LANG);

				// 入力したメールアドレスが空かどうかをチェックする
				/*
				if (StringUtils.isEmpty(email)) {
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
							APMsgParamConst.MSG_PARAM_EMAIL_CHAR));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				*/
				// {0}の文字は{1}バイト以下で入力してください。
				if(!StringUtils.isEmpty(email) &&
				   !CheckUtil.checkLength(email, DBConst.CHECK_LENGTH_EMAIL)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
								APMsgParamConst.MSG_PARAM_EMAIL_CHAR,
					 			String.valueOf(DBConst.CHECK_LENGTH_EMAIL)
					 		 ));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
				// メールアドレス標準書式
				if (!StringUtils.isEmpty(email) &&
				    !CheckUtil.isEmail(email)){
					errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_12,
							   APMsgParamConst.MSG_PARAM_EMAIL_CHAR,
							   APMsgParamConst.MSG_PARAM_EMAIL_CHAR
							 ));
					errorMsgBuf.append(APConst.HTML_NEW_LINE);
					isCheckedOk = false;
				}
			}
			// エラーメッセージをセットする
			errorJson.put( APMsgConst.WARN, errorMsgBuf.toString());
		} catch (JSONException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		}
		return isCheckedOk;
	}

	/***
	 * 個人情報変更画面のBeanをセットする
	 * @param request HttpServletRequest
	 */
	private void setBeanValue(HttpServletRequest request){
		bean = new AccountEntity();
		HttpSession session = request.getSession();
		int loginId = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ID);
		// パスワード変更
		if (isUpdatePwdFlag) {
			// パスワードDigest形式
			String updateDBPwd = CommonUtil.getPwdDigest(newPwd);
			bean.setPassword(updateDBPwd);
		} else {// 個人情報変更
			bean.setEmail(email);
			bean.setDeflang(def_lang);
		}
		bean.setId(loginId);
		bean.setMuserid(loginId);
	}

	/***
	 * パスワード変更
	 * @param bean AccountEntity
	 * @return 変更件数
	 * @throws SQLException
	 */
	private int updatePersonPwd(AccountEntity bean)throws SQLException{
		AccountDao dao = null;
		try {
			dao = new AccountDaoImpl();
			// パスワード変更の件数
			return dao.updatePersonPwd(bean);
		}catch (SQLException e) {
			throw e;
		}
	}

	/***
	 * 個人情報設定変更
	 * @param bean AccountEntity
	 * @return 変更件数
	 * @throws SQLException
	 */
	private int updatePersonInfo(AccountEntity bean) throws SQLException{
		AccountDao dao = null;
		try {
			dao = new AccountDaoImpl();
			// 個人情報設定変更の件数
			return dao.updatePersonInfo(bean);
		}catch (SQLException e) {
			throw e;
		}
	}

	/***
	 * パスワード情報を取得する
	 * @param loginID ログインID
	 * @return パスワード情報
	 * @throws SQLException
	 */
	private String getPwdInfo(int loginID) throws SQLException{
		AccountDao dao = null;
		try {
			dao = new AccountDaoImpl();
			// パスワード情報を取得する
			return dao.getPwdInfo(loginID);
		}catch (SQLException e) {
			throw e;
		}
	}

}
