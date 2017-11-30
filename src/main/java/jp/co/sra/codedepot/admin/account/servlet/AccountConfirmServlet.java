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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アカウント追加・変更確認画面
 *
 * @author sra
 *
 */
public class AccountConfirmServlet extends AccountEditServlet {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ステータス:更新エラー
	 */
	public static final String STATUS_UPDATE_ERROR = "2";

	/**
	 * ステータス:更新成功
	 */
	public static final String STATUS_UPDATE_SUCCESS = "3";

	/**
	 * アカウント追加・変更画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_EDIT = "../account/accountEdit.jsp?" + APConst.PARAM_FROM_ID + "=" + APConst.PAGE_MODE_G_02_03;

	/**
	 * ログ出力
	 */
	private static final Logger logger = LoggerFactory.getLogger(AccountConfirmServlet.class);

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));

		response.setContentType("text/html; charset=utf-8");

		// Json対象を作成する
		JSONObject json = new JSONObject();

		try {
			// メッセージ
			StringBuffer messageString = new StringBuffer();

			// 新しいセッションの対象を取得する
			AccountEntity nAe = (AccountEntity) request.getSession().getAttribute(
					APConst.SESSION_NEW_ACCOUNT_INFO);

			// 画面項目チェック処理を行う
			if (checkPage(nAe, messageString)) {
				if (nAe.isPwdChecked() || APConst.MODE_ADD.equals(nAe.getMode())) {
					// 変換のパスワード
					String encodePwd = "";
					// 変換必要
					if (!StringUtils.isEmpty(nAe.getPassword())) {
						// パスワードをMessageDigestで暗号化して、base64に変換する
						encodePwd = CommonUtil.getPwdDigest(nAe.getPassword());
					}
					nAe.setEncodePassword(encodePwd);
				}

				AccountDao dao = new AccountDaoImpl();
				// 更新件数
				int count = 0;
				// 追加
				if (APConst.MODE_ADD.equals(nAe.getMode())) {
					// アカウント情報がデータベースのアカウント情報テーブルに登録される
					count = dao.insertMember(nAe);
				// 更新
				} else {
					// 画面のアカウント名テキストボックスにより、アカウント情報が変更される。
					count = dao.updateMember(nAe);
				}

				// 登録失敗の場合(0件更新)、画面にエラーメッセージを表示して、処理を終了する。
				if (0 == count) {
					// 追加
					if (APConst.MODE_ADD.equals(nAe.getMode())) {
						messageString.append(MessageUtil.getMessageString(APMsgConst.E_COM_01,
								APMsgParamConst.MSG_PARAM_USERINFO_CHAR, APMsgParamConst.MSG_PARAM_INSERT_CHAR));
					// 更新
					} else {
						messageString.append(MessageUtil.getMessageString(APMsgConst.E_COM_01,
								APMsgParamConst.MSG_PARAM_USERINFO_CHAR, APMsgParamConst.MSG_PARAM_UPDATE_CHAR));
					}
					json.put(JSON_KEY_STATUS, STATUS_UPDATE_ERROR);
					json.put(APMsgConst.WARN, messageString);
				} else {
					json.put(JSON_KEY_STATUS, STATUS_UPDATE_SUCCESS);
					// アカウント詳細情報画面へパラメータを渡る
					json.put(APConst.PARAM_USERNAME, nAe.getUsername());
					// セッションの対象をクリアする
					request.getSession().removeAttribute(APConst.SESSION_ACCOUNT_INFO);
					//request.getSession().removeAttribute(APConst.SESSION_NEW_ACCOUNT_INFO);
				}
			} else {
				// チェック失敗の場合、エラーメッセージを表示して、処理を終了する。
				json.put(JSON_KEY_STATUS, STATUS_CHECK_ERROR);
				nAe.setMessageString(messageString.toString());
			}

			response.getWriter().print(json);
		} catch(Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_SERVLET));
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}
}
