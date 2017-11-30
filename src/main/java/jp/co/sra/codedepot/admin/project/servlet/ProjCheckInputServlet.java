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
package jp.co.sra.codedepot.admin.project.servlet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.ProjectCheckUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.scm.bo.SCMFactory;
import jp.co.sra.codedepot.scm.bo.ScmException;
import jp.co.sra.codedepot.scm.bo.VersionManage;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * プロジェクト追加・変更確認画面のイベント処理クラスである。
 *
 * @author sra
 *
 */
public class ProjCheckInputServlet extends BaseServlet {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(ProjCheckInputServlet.class);

	private static final long serialVersionUID = 1L;

	public static final String _CHECK_PROJECT = "checkProject";

	public static final String _CHECK_MANAGER = "checkManager";

	public static final String _CHECK_USER = "checkUser";

	public static final String _SCM_CONNECT = "scm_connect";

	public static final String _MODE_CONFIRM = "doConfirm";

	/**
	 * 画面処理<BR>
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		String method = request.getParameter(APConst.PARAM_METHOD);
		try {
			if (StringUtils.isEmpty(method)) {
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
						APConst.MSG_SERVLET));
			}

			ProjectInfoBean projectInfo = (ProjectInfoBean) ProjectCheckUtil
					.createFormBean(request, ProjectInfoBean.class);
			JSONObject json = new JSONObject();
			StringBuffer msgWarnBuf = new StringBuffer();
			String _msg = "";

			if (APConst.TYPE_LOCAL.equals(projectInfo.getSrc_type())) {
				projectInfo.setSrc_path(CommonUtil.replaceDupSlash(projectInfo.getSrc_path()));
			}

			// 「重複チェック」ボタンをクリック
			if (_CHECK_PROJECT.equals(method)) {
				// プロジェクト名項目チェック
				msgWarnBuf.append(ProjectCheckUtil.checkTitle(projectInfo
						.getTitle()));
				_msg = msgWarnBuf.toString();
				_msg = _msg.replaceAll(APConst.HTML_NEW_LINE,
						APConst.NEW_LINE);
				if (!"".equals(_msg)) {
					json.put(APMsgConst.WARN, _msg);
					response.getWriter().print(json);
					return;
				}
				if (APConst.MODE_MODIFY.equals(projectInfo.getMode())) {
					if (projectInfo.getTitle()
							.equals(projectInfo.getTitleOld())) {
						json.put(APMsgConst.INFO, MessageUtil
								.getMessageString(APMsgConst.I_PRJ_01));
						response.getWriter().print(json);
						return;
					}
				}
				String _name = ProjectCheckUtil.isDuplicateProjName(projectInfo
						.getTitle().toLowerCase(), projectInfo.getName());
				// 取得したレコード数が0件の場合、メッセージを表示する。
				if ("".equals(_name)) {
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(
							APMsgConst.I_COM_01, projectInfo.getTitle()));
				} else {
					// プロジェクト識別子名をプロジェクト情報に保存
					json.put(APConst.JSON_PRONAME, _name);
					json.put(APMsgConst.WARN, MessageUtil.getMessageString(
							APMsgConst.W_COM_01, APMsgParamConst.MSG_TITLE));
				}
			}

			// 「テスト」ボタンをクリック
			if (_SCM_CONNECT.equals(method)) {
				// プロジェクトURLは入力されていない場合、処理を終了する
				if (StringUtils.isEmpty(projectInfo.getSrc_path())) {
					json.put(APMsgConst.WARN, MessageUtil.getMessageString(
							APMsgConst.W_COM_04, APMsgParamConst.MSG_SRC_PATH));
					response.getWriter().print(json);
					return;
				}
				// プロジェクトURL
				_msg = ProjectCheckUtil.checkTypeURL(projectInfo.getSrc_type(),
						projectInfo.getSrc_path());
				msgWarnBuf.append(msgHtmlNewLine(_msg));
				if (! APConst.TYPE_LOCAL.equals(projectInfo.getSrc_type())) {
					// SCMのログインユーザ名をチェック
					_msg = ProjectCheckUtil.checkScmUser(projectInfo.getScm_user());
					msgWarnBuf.append(msgHtmlNewLine(_msg));
					// SCMのログインパスワードをチェック
					_msg = ProjectCheckUtil.checkPass(projectInfo.getScm_pass(),
							projectInfo.getScm_passConfirm());
					msgWarnBuf.append(msgHtmlNewLine(_msg));
				}

				_msg = msgWarnBuf.toString();
				_msg = _msg.replaceAll(APConst.HTML_NEW_LINE,
						APConst.NEW_LINE);
				_msg = _msg.replaceFirst(APConst.NEW_LINE, "");
				if (!"".equals(_msg)) {
					json.put(APMsgConst.WARN, _msg);
					response.getWriter().print(json);
					return;
				}
				if (APConst.TYPE_LOCAL.equals(projectInfo.getSrc_type())) {
					if (CheckUtil.isLocalPath(projectInfo.getSrc_path())) {
// Added by wubo on 2010/09/09 for V2.1対応  Start
						if (!CheckUtil.isPathAccess(projectInfo.getSrc_path())) {
							json.put(APMsgConst.ERROR, MessageUtil
									.getMessageString(APMsgConst.W_COM_05));
						} else {
// Added by wubo on 2010/09/09 for V2.1対応  End
							json.put(APMsgConst.INFO, MessageUtil
									.getMessageString(APMsgConst.I_COM_11));
						}
					} else {
						json.put(APMsgConst.ERROR, MessageUtil
								.getMessageString(APMsgConst.E_COM_07));
					}
					response.getWriter().print(json);
					return;
				}
// Modified by wubo on 2010/10/14 for V2.1対応 Start
//				if (doTest(projectInfo)) {
//					json.put(APMsgConst.INFO, MessageUtil.getMessageString(
//							APMsgConst.I_COM_02, APMsgParamConst.MSG_SCM));
//				} else {
//					json.put(APMsgConst.ERROR, MessageUtil.getMessageString(
//							APMsgConst.E_COM_02, APMsgParamConst.MSG_SCM));
//				}
				try {
					doTest(projectInfo);
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(
							APMsgConst.I_COM_02, APMsgParamConst.MSG_SCM));
				} catch (ScmException e) {
					json.put(APMsgConst.ERROR, e.getMessage());
				}
// Modified by wubo on 2010/10/14 for V2.1対応 End
			}
			// プロジェクト管理者の「存在チェック」ボタンをクリック
			if (_CHECK_MANAGER.equals(method)) {
				// プロジェクト管理者テキストに入力されていない場合、処理を終了する。
				if (StringUtils.isEmpty(projectInfo.getAdminName())) {
					json.put(APMsgConst.WARN, MessageUtil.getMessageString(
							APMsgConst.W_COM_04, APMsgParamConst.MSG_MANAGER));
					response.getWriter().print(json);
					return;
				}
				// プロジェクト管理者項目チェック
				msgWarnBuf.append(ProjectCheckUtil.checkAdminName(projectInfo
						.getAdminName()));
				_msg = msgWarnBuf.toString();
				_msg = _msg.replaceAll(APConst.HTML_NEW_LINE, APConst.NEW_LINE);
				if (!"".equals(_msg)) {
					json.put(APMsgConst.WARN, _msg);
					response.getWriter().print(json);
					return;
				}
				String _managerId = ProjectCheckUtil.isUserExist(true,
						projectInfo.getAdminName().toLowerCase());
				// 取得したレコード数が0件の場合、メッセージを表示する
				if ("".equals(_managerId)) {
					json.put(APMsgConst.ERROR, MessageUtil.getMessageString(
							APMsgConst.E_COM_03, APMsgParamConst.MSG_MANAGER));
					// 以外の場合、メッセージを表示する
				} else {
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(
							APMsgConst.I_COM_01, APMsgParamConst.MSG_MANAGER));
				}
			}

			// 権限ユーザの「存在チェック」ボタンをクリック
			if (_CHECK_USER.equals(method)) {

				// 権限ユーザテキストに入力されていない場合、処理を終了する
				if (StringUtils.isEmpty(projectInfo.getPermitUserStr())) {
					json.put(APMsgConst.WARN, MessageUtil
							.getMessageString(APMsgConst.W_COM_04,
									APMsgParamConst.MSG_LIMIT_USER));
					response.getWriter().print(json);
					return;
				}
				// スペースで、権限ユーザテキストの文字を分割して、ユーザ名をループする
				List<String> userName = (List<String>) projectInfo
						.getAccessUserNameList();
				String _userId = "";
				for (int i = 0; i < userName.size(); i++) {
					// 権限ユーザ項目チェック
					_msg = ProjectCheckUtil.checkUser(userName.get(i));
					_msg = _msg.replaceAll(APConst.HTML_NEW_LINE,
							APConst.NEW_LINE);
					if (!"".equals(_msg)) {
						json.put(APMsgConst.WARN, _msg);
						response.getWriter().print(json);
						return;
					}
					_userId = ProjectCheckUtil.isUserExist(false, userName.get(
							i).toLowerCase());
					// 取得したレコード数が0件の場合、エラーメッセージを表示して、処理を終了する
					if ("".equals(_userId)) {
						json.put(APMsgConst.ERROR, MessageUtil
								.getMessageString(APMsgConst.E_COM_03, userName
										.get(i)));
						response.getWriter().print(json);
						return;
					}
				}
				// ユーザ毎に、レコード数が0以外の場合、メッセージを表示する
				json.put(APMsgConst.INFO, MessageUtil.getMessageString(
						APMsgConst.I_COM_01, APMsgParamConst.MSG_LIMIT_USER));
			}

			// 「確認」ボタンをクリック
			if (_MODE_CONFIRM.equals(method)) {
				// 「画面項目チェック」シートのチェックと上記の重複チェック、存在チェックを行う
				doConfirmCheck(projectInfo, json);
			}
			response.getWriter().print(json);
		} catch (Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(
				APMsgConst.I_COM_09, APConst.MSG_SERVLET));
	}

	/**
	 * 「テスト」ボタンをクリック
	 *
	 * @param projectInfo
	 * @param json
	 * @throws ScmException
	 */
	public void doTest(ProjectInfoBean projectInfo) throws ScmException {

// Modified by wubo on 2010/10/14 for V2.1対応 Start
//		try {
//			// バージョン管理インターフェース
//			VersionManage versionManage = SCMFactory.getInstance(projectInfo
//					.getSrc_type());
//			ProjectInfoEntity project = new ProjectInfoEntity();
//			project.setScm_user(projectInfo.getScm_user());
//			project.setScm_pass(projectInfo.getScm_pass());
//			project.setSrc_path(projectInfo.getSrc_path());
//			versionManage.connect(project);
//        } catch (Exception e) {
//        	return false;
//        }
//		return true;

		// バージョン管理インターフェース
		VersionManage versionManage = SCMFactory.getInstance(projectInfo
				.getSrc_type());
		ProjectInfoEntity project = new ProjectInfoEntity();
		project.setScm_user(projectInfo.getScm_user());
		project.setScm_pass(projectInfo.getScm_pass());
		project.setSrc_path(projectInfo.getSrc_path());
		versionManage.connect(project);

// Modified by wubo on 2010/10/14 for V2.1対応 End

	}

	/**
	 * メッセージの左の改行を削除する。
	 * @param msgWarn
	 * @return
	 */
	public static String msgHtmlNewLine(String msg) {
		if (!"".equals(msg)) {
			return APConst.HTML_NEW_LINE + msg;
		}
		return msg;
	}
	/**
	 * 「確認」ボタンをクリック
	 *
	 * @param projectInfo
	 * @param json
	 * @throws Exception
	 */
	public static void doConfirmCheck(ProjectInfoBean projectInfo,
			JSONObject json) throws Exception{
		String msgError = "";
		StringBuffer msgWarnBuf = new StringBuffer();
		String msg = "";

		// プロジェクト名をチェック
		String _title = projectInfo.getTitle().toLowerCase();
		String _msgTitle = ProjectCheckUtil.checkTitle(_title);
		String _titleOld = projectInfo.getTitleOld().toLowerCase();
		// 変更モデルかつプロジェクト名が変更されているの場合或は追加モデル、重複チェックを行う。
		if ((APConst.MODE_ADD.equals(projectInfo.getMode()))
				|| ((APConst.MODE_MODIFY.equals(projectInfo.getMode())) && (!_title
						.equals(_titleOld)))) {
			if (!"".equals(_msgTitle)) {
				msg = _msgTitle;
				msgWarnBuf.append(msgHtmlNewLine(msg));
			} else {
				String _proName = ProjectCheckUtil.isDuplicateProjName(_title,
						projectInfo.getName());
				if (!"".equals(_proName)) {
					msg = MessageUtil.getMessageString(APMsgConst.W_COM_01,
							APMsgParamConst.MSG_TITLE);
					msgWarnBuf.append(msgHtmlNewLine(msg));
				}
			}
		}

		// ライセンス名をチェック
		msg = ProjectCheckUtil.checkLicense(projectInfo.getLicense());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// 概要をチェック
		msg = ProjectCheckUtil.checkDescription(projectInfo.getDescription());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// ポータルのＵＲＬをチェック
		msg = ProjectCheckUtil.checkSite(projectInfo.getSite_url());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// ダウンロードサイトのＵＲＬをチェック
		msg = ProjectCheckUtil.checkDownLoad(projectInfo.getDownload_url());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// プロジェクトURL
		msg = ProjectCheckUtil.checkTypeURL(projectInfo.getSrc_type(),
				projectInfo.getSrc_path());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// check only if src is not local
		if (!APConst.TYPE_LOCAL.equals(projectInfo.getSrc_type())) {
			// SCMのログインユーザ名をチェック
			msg = ProjectCheckUtil.checkScmUser(projectInfo.getScm_user());
			msgWarnBuf.append(msgHtmlNewLine(msg));

			// SCMのログインパスワードをチェック
			msg = ProjectCheckUtil.checkPass(projectInfo.getScm_pass(),
					projectInfo.getScm_passConfirm());
			msgWarnBuf.append(msgHtmlNewLine(msg));
		}

		// 検索インデックス更新処理の開始時刻をチェック
		msg = ProjectCheckUtil.checkCrontab(projectInfo.getCrontab());
		msgWarnBuf.append(msgHtmlNewLine(msg));

		// プロジェクト管理者をチェック
		if (!StringUtils.isEmpty(projectInfo.getAdminName())) {
			msg = ProjectCheckUtil.checkAdminName(projectInfo.getAdminName());
			msgWarnBuf.append(msgHtmlNewLine(msg));
			String _managerId = ProjectCheckUtil.isUserExist(
					true, projectInfo.getAdminName().toLowerCase());
			if ("".equals(_managerId)) {
				msg = MessageUtil.getMessageString(
						APMsgConst.E_COM_03, APMsgParamConst.MSG_MANAGER);
				msgError += msgHtmlNewLine(msg);
			} else {
				projectInfo.setAdminId(_managerId);
			}
		}
		// アクセス制御が"あり"の場合、権限ユーザの存在チェックボタンを行う
		if (APConst.RESTRICTED_TRUE_STR.equals(projectInfo.getRestricted())) {
			// アクセス権限指定をチェック
			if (!StringUtils.isEmpty(projectInfo.getPermitUserStr())) {
				List<String> userName = (List<String>) projectInfo
						.getAccessUserNameList();
				String _userId = "";
				List<String> userList = new ArrayList<String>();
				for (int i = 0; i < userName.size(); i++) {
					ProjectCheckUtil.checkUserWithUniqueMsg(userName.get(i),
							msgWarnBuf);
					_userId = ProjectCheckUtil.isUserExist(false, userName.get(
							i).toLowerCase());
					if ("".equals(_userId)) {
						msg = MessageUtil.getMessageString(
								APMsgConst.E_COM_03, userName.get(i));
						msgError += msgHtmlNewLine(msg);
					} else {
						userList.add(_userId);
					}
				}
				projectInfo.setAccessUserIdList(userList);
			}
		}
		String msgWarn = msgWarnBuf.toString();
		msgWarn = msgWarn.replaceFirst(APConst.HTML_NEW_LINE, "");
		msgError = msgError.replaceFirst(APConst.HTML_NEW_LINE, "");

		if (!"".equals(msgWarnBuf)) {
			json.put(APMsgConst.WARN, msgWarnBuf);
		}
		if (!"".equals(msgError)) {
			json.put(APMsgConst.ERROR, msgError);
		}

		//SRC PATH
		json.put(APConst.JSON_SRC_PATH, projectInfo.getSrc_path());
		// プロジェクト管理者のユーザID
		json.put(APConst.JSON_ADMINID, projectInfo.getAdminId());
		// アクセス権限指定ユーザID
		json.put(APConst.JSON_PERMITID, projectInfo.getPermitUserIdStr());
		String errorMsg = "";

		if (!StringUtils.isEmpty(msgError)) {
			errorMsg = msgError;
			if (!StringUtils.isEmpty(msgWarn)) {
				errorMsg += APConst.HTML_NEW_LINE;
			}
		}
		if (!StringUtils.isEmpty(msgWarn)) {
			errorMsg += msgWarnBuf;
		}
		if(!StringUtils.isEmpty(errorMsg)){
			projectInfo.setErrMsg(errorMsg);
			projectInfo.setError(true);
		}else{
			projectInfo.setError(false);
		}
	}
}
