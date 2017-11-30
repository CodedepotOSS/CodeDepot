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

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アカウント追加・変更画面
 *
 * @author sra
 *
 */
public class AccountEditServlet extends BaseServlet {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ログ出力
	 */
	private static final Logger logger = LoggerFactory.getLogger(AccountEditServlet.class);

	/**
	 * メソッド 重複チェック
	 */
	public static final String METHDO_CHECK_DUPLICATE = "checkDuplicate";

	/**
	 * メソッド 確認クリック
	 */
	public static final String METHDO_CLICK_CONFIRM = "clickConfirm";

	/**
	 * Jsonキー:ステータス
	 */
	public static final String JSON_KEY_STATUS = "status";

	/**
	 * ステータス:チェックエラー
	 */
	public static final String STATUS_CHECK_ERROR = "0";

	/**
	 * ステータス:チェック成功
	 */
	public static final String STATUS_CHECK_SUCCESS = "1";

	/**
	 * アカウント検索・一覧画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_LIST = "../account/accountList.jsp";

	/**
	 * アカウント詳細情報画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_DETAIL = "../account/accountDetailInfo.jsp";

	/**
	 * アカウント追加・変更確認画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_CONFIRM = "../account/accountConfirm.jsp";

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

			// リクエストメソッドを取得する
			String method = request.getParameter("method");
			// パラメータからアカウント対象を作成する
			AccountEntity ae = createAccountEntity(request);
			// 重複チェック
			if (METHDO_CHECK_DUPLICATE.equals(method)) {
				// 重複チェックを行う
				if (checkDuplicateAccount(ae, messageString)) {
					json.put(JSON_KEY_STATUS, STATUS_CHECK_SUCCESS);
					json.put(APMsgConst.INFO, messageString.toString());
				} else {
					json.put(JSON_KEY_STATUS, STATUS_CHECK_ERROR);
					json.put(APMsgConst.WARN, messageString.toString().replaceAll(APConst.HTML_NEW_LINE,
							APConst.NEW_LINE));
				}

				response.getWriter().print(json);
			// 確認クリック
			} else if (METHDO_CLICK_CONFIRM.equals(method)) {
				// 画面項目チェック処理を行う
				if (checkPage(ae, messageString)) {
					// セッションの対象をコピーする
					copySessionAccountEntity(request, ae);
					json.put(JSON_KEY_STATUS, STATUS_CHECK_SUCCESS);
					json.put(APConst.SUCCESS_URL, URL_PAGE_ACCOUNT_CONFIRM);
				} else {
					// チェック失敗の場合、エラーメッセージを表示して、処理を終了する。
					json.put(JSON_KEY_STATUS, STATUS_CHECK_ERROR);
					json.put(APMsgConst.WARN, messageString.toString());
				}

				response.getWriter().print(json);
			}
		} catch(Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_SERVLET));
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/**
	 * 画面項目チェック処理を行う
	 *
	 * @param username アカウント名
	 * @param ae アカウント対象
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 */
	protected boolean checkPage(AccountEntity ae, StringBuffer messageString) throws SQLException {

		// チェック成功フラグ
		boolean isCheckedOk = true;
		// アカウント名
		if (!checkAccount(ae, messageString)) {
			isCheckedOk = false;
		}else{
			StringBuffer msg = new StringBuffer();
			if (!checkDuplicateAccount(ae, msg)) {
				messageString.append(msg);
				isCheckedOk = false;
			}
		}

		if (ae.isPwdChecked() || APConst.MODE_ADD.equals(ae.getMode())) {
			// パスワード
			String password = ae.getPassword();
			// パスワード再入力
			String rePassword = ae.getRePassword();
			if (!checkPassword(password, rePassword, messageString)) {
				isCheckedOk = false;
			}
		}
		// メールアドレス
		String email = ae.getEmail();
		if (!checkEmail(email, messageString)) {
			isCheckedOk = false;
		}
		// 管理者コメント
		String note = ae.getNote();
		if (!checkNote(note, messageString)) {
			isCheckedOk = false;
		}

		return isCheckedOk;
	}

	/**
	 * アカウント名チェック処理を行う
	 *
	 * @param ae アカウント対象
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 * @throws SQLException SQL例外
	 */
	private boolean checkAccount(AccountEntity ae, StringBuffer messageString) throws SQLException {

		// チェック成功フラグ
		boolean isCheckedOk = true;
		// アカウント名が指定されていない場合、エラーメッセージを表示して、処理を終了する。
		if (StringUtils.isEmpty(ae.getUsername())) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR));
			messageString.append(APConst.HTML_NEW_LINE);
			return false;
		}

		if (!CheckUtil.isUserName(ae.getUsername())) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR, APMsgParamConst.MSG_PARAM_USERNAME_CHECK));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		// 40字符数以下チェック
		if (!CheckUtil.checkLength(ae.getUsername(), DBConst.CHECK_LENGTH_USERNAME)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR, String.valueOf(DBConst.CHECK_LENGTH_USERNAME)));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		return isCheckedOk;
	}

	/**
	 * 重複チェック処理を行う
	 *
	 * @param ae アカウント対象
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 */
	private boolean checkDuplicateAccount(AccountEntity ae, StringBuffer messageString) throws SQLException {

		boolean isCheckedOk = true;
		// アカウント名が指定されていない場合、エラーメッセージを表示して、処理を終了する。
		if (StringUtils.isEmpty(ae.getUsername())) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR));
			return false;
		}

		// アカウント名が入力不正の場合、画面にエラーメッセージを表示して、処理を終了する。
		if (!CheckUtil.isUserName(ae.getUsername())) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR, APMsgParamConst.MSG_PARAM_USERNAME_CHECK));
			isCheckedOk = false;
		}

		// 40字符数以下チェック
		if (!CheckUtil.checkLength(ae.getUsername(), DBConst.CHECK_LENGTH_USERNAME)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
					APMsgParamConst.MSG_PARAM_USERNAME_CHAR, String.valueOf(DBConst.CHECK_LENGTH_USERNAME)));
			isCheckedOk = false;
		}

		// 追加
		if (APConst.MODE_ADD.equals(ae.getMode()) && isCheckedOk) {
			AccountDao dao = new AccountDaoImpl();
			// データベースのユーザ管理テーブルから指定されたアカウント名に対応するデータを抽出する
			int count = dao.getMemberByUsername(ae.getUsername().toLowerCase());
			// 取得したレコード数  = 0 の場合、メッセージを表示して、処理を終了する。
			if (count == 0) {
				messageString.append(MessageUtil.getMessageString(APMsgConst.I_COM_01, ae.getUsername()));
				return true;
			// 取得したレコード数  !=  0 の場合、画面にエラーメッセージを表示して、処理を終了する。
			} else {
				messageString.append(MessageUtil.getMessageString(APMsgConst.W_COM_01,
						APMsgParamConst.MSG_PARAM_USERNAME_CHAR));
				messageString.append(APConst.HTML_NEW_LINE);
				return false;
			}
		}

		return isCheckedOk;
	}

	/**
	 * パスワードチェック処理を行う
	 *
	 * @param password パスワード
	 * @param rePassword パスワード再入力
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 * @throws SQLException SQL例外
	 */
	private boolean checkPassword(String password, String rePassword, StringBuffer messageString) throws SQLException {

		// チェック成功フラグ
		boolean isCheckedOk = true;
		if (!CheckUtil.isEngNumMark(password)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
					APMsgParamConst.MSG_PARAM_PWD_CHAR, APMsgParamConst.MSG_PARAM_PWD_CHECK));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		// 20字符数以下チェック
		if (!CheckUtil.checkLength(password, DBConst.CHECK_LENGTH_PASSWORD)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
					APMsgParamConst.MSG_PARAM_PWD_CHAR, String.valueOf(DBConst.CHECK_LENGTH_PASSWORD)));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		if (!CheckUtil.isEngNumMark(rePassword)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
					APMsgParamConst.MSG_PARAM_PWDAGAIIN_CHAR, APMsgParamConst.MSG_PARAM_PWD_CHECK));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		// 20字符数以下チェック
		if (!CheckUtil.checkLength(rePassword, DBConst.CHECK_LENGTH_PASSWORD)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
					APMsgParamConst.MSG_PARAM_PWDAGAIIN_CHAR, String.valueOf(DBConst.CHECK_LENGTH_PASSWORD)));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		// パスワードとパスワード再入力と同じチェック
		if (!password.equals(rePassword)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_05));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		return isCheckedOk;
	}

	/**
	 * メールアドレスチェック処理を行う
	 *
	 * @param email メールアドレス
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 * @throws SQLException SQL例外
	 */
	private boolean checkEmail(String email, StringBuffer messageString) throws SQLException {

		if (StringUtils.isEmpty(email)) {
			return true;
		}
		// チェック成功フラグ
		boolean isCheckedOk = true;
		// 1024字符数以下チェック
		if (!CheckUtil.checkLength(email, DBConst.CHECK_LENGTH_EMAIL)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03, APMsgParamConst.MSG_PARAM_EMAIL_CHAR, String.valueOf(DBConst.CHECK_LENGTH_EMAIL)));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		// メールアドレスチェック
		if (!CheckUtil.isEmail(email)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_12, APMsgParamConst.MSG_PARAM_EMAIL_CHAR, APMsgParamConst.MSG_PARAM_EMAIL_CHAR));
			messageString.append(APConst.HTML_NEW_LINE);
			isCheckedOk = false;
		}

		return isCheckedOk;
	}

	/**
	 * 管理者コメントチェック処理を行う
	 *
	 * @param note 管理者コメント
	 * @param messageString メッセージ
	 *
	 * @return true チェック成功 false:チェック失敗
	 * @throws SQLException SQL例外
	 */
	private boolean checkNote(String note, StringBuffer messageString) throws SQLException {

		// 256字符数以下チェック
		if (!CheckUtil.checkLength(note, DBConst.CHECK_LENGTH_NOTE)) {
			messageString.append(MessageUtil.getMessageString(APMsgConst.W_MEM_03,
					APMsgParamConst.MSG_PARAM_NOTE_CHAR, String.valueOf(DBConst.CHECK_LENGTH_NOTE)));
			messageString.append(APConst.HTML_NEW_LINE);
			return false;
		}

		return true;
	}

	/**
	 * パラメータからアカウント対象を作成する
	 *
	 * @param request リクエスト
	 *
	 * @return true チェック成功 false:チェック失敗
	 * @throws SQLException SQL例外
	 */
	private AccountEntity createAccountEntity(HttpServletRequest request) {

		// 新しいセッションの対象する
		AccountEntity nAe = new AccountEntity();

		// アカウント名
		String username = request.getParameter("username");
		if (!StringUtils.isEmpty(username)) {
			nAe.setUsername(username.trim());
		}
		// パスワード更新
		String pwdChecked = request.getParameter("pwdChecked");
		if (!StringUtils.isEmpty(pwdChecked)) {
			nAe.setPwdChecked(Boolean.parseBoolean(pwdChecked));
		}
		// メールアドレス
		String email = request.getParameter("email");
		if (!StringUtils.isEmpty(email)) {
			nAe.setEmail(email.trim());
		}
		// 管理者コメント
		String note = request.getParameter("note");
		if (!StringUtils.isEmpty(note)) {
			nAe.setNote(note.trim());
		}
		// 有効フラグ
		String active = request.getParameter("active");
		if (!StringUtils.isEmpty(active)) {
			nAe.setActive(Boolean.parseBoolean(active));
		}
		// パスワード
		String password = request.getParameter("password");
		nAe.setPassword(password);
		// パスワード再入力
		String rePassword = request.getParameter("rePassword");
		nAe.setRePassword(rePassword);
		// 管理グループ番号
		String role = request.getParameter("role");
		if (!StringUtils.isEmpty(role)) {
			nAe.setRole(Integer.parseInt(role));
		}
		// デフォント検索言語
		String deflang = request.getParameter("deflang");
		if (!StringUtils.isEmpty(deflang)) {
			nAe.setDeflang(deflang);
		}
		// 処理モード
		String mode = request.getParameter("mode");
		nAe.setMode(mode);

		return nAe;
	}

	/**
	 * セッションの対象をコピーする
	 *
	 * @param request リクエスト
	 * @param nAe アカウント対象
	 *
	 * @throws SQLException SQL例外
	 */
	private void copySessionAccountEntity(HttpServletRequest request, AccountEntity nAe) {

		// 古いセッションの対象を取得する
		AccountEntity ae = (AccountEntity) request.getSession().getAttribute(
				APConst.SESSION_ACCOUNT_INFO);

		// シーケンス番号
		nAe.setId(ae.getId());
		// 遷移元ID
		nAe.setFromId(ae.getFromId());
		// セッションのログインID
		Integer loginId = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ID);
		// 作成者
		nAe.setCuserid(loginId);
		// 更新者
		nAe.setMuserid(loginId);
		// アカウント名を小文字に変換する
		nAe.setUsername(nAe.getUsername().toLowerCase());

		request.getSession().setAttribute(APConst.SESSION_NEW_ACCOUNT_INFO, nAe);
	}
}
