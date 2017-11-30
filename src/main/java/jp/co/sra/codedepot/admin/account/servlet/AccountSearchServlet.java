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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.ChangePageBean;
import jp.co.sra.codedepot.admin.util.ChangePageUtil;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * アカウント検索・一覧画面
 *
 * @author sra
 *
 */
public class AccountSearchServlet extends BaseServlet {

	/**
	 * ログ出力
	 */
	private static final Logger logger = LoggerFactory.getLogger(AccountSearchServlet.class);

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * メソッド 検索
	 */
	public static final String METHOD_GETSRCHLST = "getSrchLst";

	/**
	 * メソッド 削除
	 */
	public static final String METHOD_DELETEACCOUNT = "dltAccount";

	/**
	 * アカウント詳細情報画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_DETAIL = "../account/accountDetailInfo.jsp?" + APConst.PARAM_FROM_ID + "=" + APConst.PAGE_MODE_G_02_01;

	/**
	 * アカウント追加・変更画面のURL
	 */
	public static final String URL_PAGE_ACCOUNT_EDIT = "../account/accountEdit.jsp?" + APConst.PARAM_FROM_ID + "=" + APConst.PAGE_MODE_G_02_01;


	@SuppressWarnings("unchecked")
	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response)  throws BaseException{

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));

		response.setContentType("text/html; charset=utf-8");

		try {
			// メソッドを取得する
			String method = request.getParameter(APConst.PARAM_METHOD);

			// パラメータ不正
			if (StringUtils.isEmpty(method)) {
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
						APConst.MSG_SERVLET));
				response.getWriter().print("");
				return;
			}

			// 検索
			if (METHOD_GETSRCHLST.equals(method)) {
				Map<String, Object> map = new HashMap<String, Object>();
				String username = request.getParameter(APConst.PARAM_USERNAME);
				if (!StringUtils.isEmpty(username)) {
					if (!CheckUtil.isUserName(username.trim())) {
						JSONObject json = new JSONObject();
						json.put(APMsgConst.WARN,
								MessageUtil.getMessageString(APMsgConst.W_COM_07,
										APMsgParamConst.MSG_PARAM_USERNAME_CHAR,
										APMsgParamConst.MSG_PARAM_USERNAME_CHECK));
						response.getWriter().print(json);
						return;
					}
					map.put(APConst.PARAM_USERNAME,  CommonUtil.escapeStr(username.trim().toLowerCase()));
				}

				// 改ページ対象を作成する
				ChangePageBean pageBean = new ChangePageBean();
				// ページを取得する
				int inPage = Integer.valueOf(request.getParameter(APConst.PARAM_PAGE));
				pageBean.setInPage(inPage);
				pageBean.setInParamMap(map);
				pageBean.setInCountSqlMap("account.selectSrchLstCount");
				pageBean.setInListSqlMap("account.selectSrchLst");

				// 改ページUtilを作成する
				ChangePageUtil pageUtil = new ChangePageUtil(pageBean);
				// 改ページ処理を行う
				pageUtil.getResult();
				// 改ページデータを取得する
				List<AccountEntity> list = (List<AccountEntity>)pageBean.getOutList();
				// JSON列を作成する
				JSONArray jsonBeans = new JSONArray();
				for (AccountEntity bean : list) {
					jsonBeans.put(bean.toJSONObject());
				}
				// JSON対象を作成する
				JSONObject json = new JSONObject();
				// 改ページ数d
				json.put(ChangePageUtil.ITEMCOUNTS, pageBean.getOutItemCounts());
				json.put(ChangePageUtil.ITEMSPERPAGE, pageBean.getOutItemsPerPage());
				json.put(ChangePageUtil.PAGECOUNTS, pageBean.getOutPageCounts());
				json.put(ChangePageUtil.PAGE, pageBean.getOutPage());
				json.put(ChangePageUtil.LISTS, jsonBeans);
				if (0 == pageBean.getOutItemCounts()) {
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.I_COM_03));
				}

				response.getWriter().print(json);
			// 削除
			} else if (METHOD_DELETEACCOUNT.equals(method)) {
				// アカウント名を取得する
				String usernames = request.getParameter("usernames");
				// セッションのログインID
				Integer loginId = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ID);

				AccountDaoImpl dao = new AccountDaoImpl();
				// プロジェクト管理者の情報を削除する
				int count = dao.deleteAccount(usernames, loginId);

				// JSON対象を作成する
				JSONObject json = new JSONObject();
				if (0 == count) {
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.E_COM_08));
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
}
