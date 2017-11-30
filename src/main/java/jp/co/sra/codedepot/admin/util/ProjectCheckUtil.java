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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;

import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.StringUtils;

public class ProjectCheckUtil {
	/**
	 * 利用ユーザの管理グループ番号クリック
	 *
	 * @param loginSession
	 * @return
	 */
	public static boolean isProjectAdmin(AccountEntity loginSession) {
		Integer _role_1 = 1;
		if (loginSession.getRole().equals(_role_1)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 「重複チェック」ボタンをクリック
	 *
	 * @param request
	 * @param response
	 */
	public static String isDuplicateProjName(String title, String proName)
			throws Exception {

		ProjectDao dao = new ProjectDaoImpl();
		ArrayList<ProjectInfoEntity> projectInfoEdit = dao
				.queryProjectList(title, proName);
		if (projectInfoEdit == null || projectInfoEdit.size() == 0) {
			return "";
		} else {
			return projectInfoEdit.get(0).getName();
		}
	}

	/**
	 * 権限ユーザの「存在チェック」ボタンをクリック
	 *
	 * @param loginSession
	 * @return
	 */
	public static String isUserExist(boolean shouldAdminGroup,
			String userName) throws Exception {
		String id = "";
		AccountDao dao = new AccountDaoImpl();
		// プロジェクト管理者の「存在チェック」ボタンをクリック
		if(shouldAdminGroup) {
			ArrayList<AccountEntity> memberInfo = dao.queryManagerList(userName);
			if (memberInfo == null || memberInfo.size() == 0) {
				return id;
			}
			return String.valueOf(memberInfo.get(0).getId());
		// 権限ユーザの「存在チェック」ボタンをクリック
		} else {
			ArrayList<AccountEntity> userInfo = dao.queryUserList(userName);
			if (userInfo == null || userInfo.size() == 0) {
				return id;
			}
			return String.valueOf(userInfo.get(0).getId());
		}
	}

	/**
	 * HTTPリクエストのパラメータより、フォームBeanを作成して、プロジェクト情報を返却する処理である。
	 * @param req HTTPのリクエスト
	 * @param cls フォームBeanのクラスオブジェクト
	 * @throws Exception フォームBean設定の異常
	 */
	public static BaseBean createFormBean(HttpServletRequest req,
			Class<? extends BaseBean> cls) throws Exception{
		Map<?, ?> reqMap= req.getParameterMap();
		BaseBean bean = cls.newInstance();
		Method[] beanMethods = cls.getMethods();
		for (Object key : reqMap.keySet()) {
			for (Method method : beanMethods) {
				String setMethodName = "set" + key;
				if (method.getName().equalsIgnoreCase(setMethodName)) {
					method.invoke(bean, req.getParameter((String) key));
					break;
				}
			}
		}
		return bean;
	}

	/**
	 * Bean 中の文字列の HTML エスケープ処理を行う。
	 * @param bean
	 * @throws Exception フォームBeanの異常
	 */
	public static BaseBean escapeHtml(BaseBean bean) {
		Class cls = bean.getClass();
		Method[] methods = cls.getMethods();
		for (int i=0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getName().startsWith("set")) {
				try {
					Method getter = cls.getMethod("get" + m.getName().substring(3));
					Object obj = getter.invoke(bean);
					if (obj instanceof String && !StringUtils.isEmpty((String) obj)) {
						String e = StringEscapeUtils.escapeHtml((String) obj);
						m.invoke(bean, e);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return bean;
	}


	/**
	 * プロジェクト名をチェック
	 *
	 * @param title
	 * @return
	 */
	public static String checkTitle(String title) {
		String msg = "";
		// 入力されていない場合
		if (StringUtils.isEmpty(title)) {
			msg = MessageUtil.getMessageString(APMsgConst.W_MEM_01, APMsgParamConst.MSG_TITLE);
			return msg;
		}
		// 入力許可文字以外が入力された場合
		if (!CheckUtil.isProjName(title)) {
			msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
					APMsgConst.W_COM_07, APMsgParamConst.MSG_TITLE,
					APMsgParamConst.MSG_PARAM_PROJECTNAME_CHECK);
		}
		// 文字数が最大桁数を超える
		if (!CheckUtil.checkLength(title, DBConst.TITLE_MAX_LEN)) {
			msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
					APMsgConst.W_MEM_03, APMsgParamConst.MSG_TITLE,
					String.valueOf(DBConst.TITLE_MAX_LEN));
		}
		if (msg.startsWith(APConst.HTML_NEW_LINE)) {
			msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
		}
		return msg;
	}

	/**
	 * ライセンス名をチェック
	 *
	 * @param license
	 * @return
	 */
	public static String checkLicense(String license) {
		String msg = "";
		if (!StringUtils.isEmpty(license)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isLicenseName(license)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_LICENSE_NAME,
						APMsgParamConst.MSG_PARAM_LICENSE_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(license, DBConst.LICENSE_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE +MessageUtil.getMessageString(APMsgConst.W_MEM_03,
						APMsgParamConst.MSG_LICENSE_NAME,
						String.valueOf(DBConst.LICENSE_MAX_LEN));
			}
			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * SCMのログインパスワードをチェック
	 *
	 * @param pass
	 * @param passConfirm
	 * @return
	 */
	public static String checkPass(String pass, String passConfirm) {
		String msg = "";
		if (!StringUtils.isEmpty(pass)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isEngNumMark(pass)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_PASS,
						APMsgParamConst.MSG_PARAM_PWD_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(pass, DBConst.PASS_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_PASS,
						String.valueOf(DBConst.PASS_MAX_LEN));
			}
			// 文字数が最小桁数未満
			if (!CheckUtil.checkLengthAbove(pass, DBConst.PASS_MIN_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_10, APMsgParamConst.MSG_PASS,
						String.valueOf(DBConst.PASS_MIN_LEN));
			}
		}
		if (!StringUtils.isEmpty(passConfirm)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isEngNumMark(passConfirm)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_PASS_CONFIRM,
						APMsgParamConst.MSG_PARAM_PWD_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(passConfirm, DBConst.PASS_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_PASS_CONFIRM,
						String.valueOf(DBConst.PASS_MAX_LEN));
			}
			// 文字数が最小桁数未満
			if (!CheckUtil.checkLengthAbove(passConfirm, DBConst.PASS_MIN_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_10, APMsgParamConst.MSG_PASS_CONFIRM,
						String.valueOf(DBConst.PASS_MIN_LEN));
			}
		}
		// 同じではない場合
		if (!pass.equals(passConfirm)) {
			msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(APMsgConst.W_MEM_05);
		}
		if (msg.startsWith(APConst.HTML_NEW_LINE)) {
			msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
		}
		return msg;
	}

	/**
	 * SCMのログインユーザ名をチェック
	 *
	 * @param scmUser
	 * @return
	 */
	public static String checkScmUser(String scmUser) {
		String msg = "";
		if (!StringUtils.isEmpty(scmUser)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isUserName(scmUser)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_SCM_USER,
						APMsgParamConst.MSG_PARAM_USERNAME_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(scmUser, DBConst.USER_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE+MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_SCM_USER,
						String.valueOf(DBConst.USER_MAX_LEN));
			}
			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * プロジェクト管理者をチェック
	 *
	 * @param license
	 * @return
	 */
	public static String checkAdminName(String adminName) {
		String msg = "";
		if (!StringUtils.isEmpty(adminName)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isUserName(adminName)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_MANAGER,
						APMsgParamConst.MSG_PARAM_USERNAME_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(adminName, DBConst.USER_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_MANAGER,
						String.valueOf(DBConst.USER_MAX_LEN));
			}
			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * アクセス権限指定をチェック
	 *
	 * @param license
	 * @return
	 */
	public static String checkUser(String user) {
		String msg = "";
		if (!StringUtils.isEmpty(user)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isUserName(user)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_ACCESS_USER,
						APMsgParamConst.MSG_PARAM_USERNAME_CHECK);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(user, DBConst.USER_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_ACCESS_USER,
						String.valueOf(DBConst.USER_MAX_LEN));
			}

			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * アクセス権限指定をチェック、重複なメッセージは追加しまん。
	 *
	 * @param user チェック対象
	 * @param msgBuf 既存メッセージ
	 */
	public static void checkUserWithUniqueMsg(String user, StringBuffer msgBuf) {
		String _msg = null;
		if (!StringUtils.isEmpty(user)) {
			// 入力許可文字以外が入力された場合
			if (!CheckUtil.isUserName(user)) {
				_msg = APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_COM_07, APMsgParamConst.MSG_ACCESS_USER,
						APMsgParamConst.MSG_PARAM_USERNAME_CHECK);
			}
			CommonUtil.appendMsg2BufUnique(_msg, msgBuf);
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(user, DBConst.USER_MAX_LEN)) {
				_msg = APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_ACCESS_USER,
						String.valueOf(DBConst.USER_MAX_LEN));
			}
			CommonUtil.appendMsg2BufUnique(_msg, msgBuf);
		}
	}

	/**
	 * 概要をチェック
	 *
	 * @param description
	 * @return
	 */
	public static String checkDescription(String description) {
		String msg = "";
		if (!StringUtils.isEmpty(description)) {
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(description, DBConst.DESCR_MAX_LEN)) {
				msg += MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_DESCR,
						String.valueOf(DBConst.DESCR_MAX_LEN));
			}
		}
		return msg;
	}

	/**
	 * 検索インデックス更新処理の開始時刻をチェック
	 *
	 * @param crontab
	 * @return
	 */
	public static String checkCrontab(String crontab) {
		String msg = "";
		if (!StringUtils.isEmpty(crontab)) {
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(crontab, DBConst.CRONTAB_MAX_LEN)) {
				msg += MessageUtil.getMessageString(APMsgConst.W_MEM_03,
						APMsgParamConst.MSG_CRONTAB,
						String.valueOf(DBConst.CRONTAB_MAX_LEN));
			}
			// CRON標準書式
			if (!CheckUtil.isCrontab(crontab)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(APMsgConst.W_MEM_15,
						APMsgParamConst.MSG_CRONTAB,
						APMsgParamConst.MSG_CRON);
			}
			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * プロジェクトURLをチェック
	 *
	 * @param type
	 * @param srcPath
	 * @return
	 */
	public static String checkTypeURL(String type, String srcPath) {
		String msg = "";
		// パスが入力されていない場合
		if (StringUtils.isEmpty(srcPath)) {
			msg = MessageUtil.getMessageString(APMsgConst.W_MEM_01, APMsgParamConst.MSG_SRC_PATH);
			return msg;
		}
		if ((!StringUtils.isEmpty(type)) && (!StringUtils.isEmpty(srcPath))) {
			// 参照方法がSVNのとき、URLがhttpsまたはhttpで始まらなかった場合
			if (APConst.TYPE_SVN.equals(type)) {
				if (!CheckUtil.isSVNPath(srcPath)) {
					msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
							APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
							APMsgParamConst.MSG_SVN_URL);
				}
			}
			// 参照方法がCVSの時、URLがpserverで始まらなかった場合
			if (APConst.TYPE_CVS.equals(type)) {
				if (!CheckUtil.isCVSPath(srcPath)) {
					msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
							APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
							APMsgParamConst.MSG_CVS_URL);
				}
			}
			// 参照方法がGITの時、URLがhttpまたはhttpsで始まらなかった場合
			if (APConst.TYPE_GIT.equals(type)) {
				if (!CheckUtil.isGITPath(srcPath)) {
					msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
							APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
							APMsgParamConst.MSG_GIT_URL);
				}
			}
// Added by wubo on 2010/08/30 for V2.1対応 Start
			// 参照方法がJAZZの時、URLがhttpsで始まらなかった場合
			if (APConst.TYPE_JAZZ.equals(type)) {
				if (!CheckUtil.isJAZZPath(srcPath)) {
					msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
							APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
							APMsgParamConst.MSG_JAZZ_URL);
				}
			}
// Added by wubo on 2010/08/30 for V2.1対応 End
			// 参照方法がlocalの時、URLが/で始まらなかった場合
			if (APConst.TYPE_LOCAL.equals(type)) {
// Added by wubo on 2010/08/30 for V2.1対応 Start
				if (CommonUtil.isWinOS()) {
					if (!CheckUtil.isWindowsLoaclPath(srcPath)) {
						msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
								APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
								APMsgParamConst.MSG_LOCAL_URL);
					}
					if (CommonUtil.getNotAllowedWindowsLocalPath().contains(srcPath.toLowerCase())) {
						msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
								APMsgConst.W_PRJ_01, StringEscapeUtils.escapeHtml(srcPath));
					}
// Added by wubo on 2010/08/30 for V2.1対応 End
				} else {
					if (!srcPath.startsWith(APConst.PATH_LOCAL)) {
						msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
								APMsgConst.W_MEM_15, APMsgParamConst.MSG_SRC_PATH,
								APMsgParamConst.MSG_LOCAL_URL);
					}
					if (CommonUtil.getNotAllowedLocalPath().contains(srcPath)) {
						msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
								APMsgConst.W_PRJ_01, StringEscapeUtils.escapeHtml(srcPath));
					}
				}
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(srcPath, DBConst.URL_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE +MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_SRC_PATH,
						String.valueOf(DBConst.URL_MAX_LEN));
			}
			if (msg.startsWith(APConst.HTML_NEW_LINE)) {
				msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
			}
		}
		return msg;
	}

	/**
	 * ダウンロードサイトのＵＲＬをチェック
	 *
	 * @param crontab
	 * @return
	 */
	public static String checkDownLoad(String download) {
		String msg = "";
		if (!StringUtils.isEmpty(download)) {
			// URLの型ではない場合
			if (!CheckUtil.isUrl(download)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_12, APMsgParamConst.MSG_SITE_URL, APMsgParamConst.MSG_URL);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(download, DBConst.URL_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE +MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_SITE_URL,
						String.valueOf(DBConst.URL_MAX_LEN));
			}
		}
		if (msg.startsWith(APConst.HTML_NEW_LINE)) {
			msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
		}
		return msg;
	}

	/**
	 * ポータルＵＲＬをチェック
	 *
	 * @param crontab
	 * @return
	 */
	public static String checkSite(String site) {
		String msg = "";
		if (!StringUtils.isEmpty(site)) {
			// URLの型ではない場合
			if (!CheckUtil.isUrl(site)) {
				msg += APConst.HTML_NEW_LINE + MessageUtil.getMessageString(
						APMsgConst.W_MEM_12, APMsgParamConst.MSG_DOWNLOAD_URL,
						APMsgParamConst.MSG_URL);
			}
			// 文字数が最大桁数を超える
			if (!CheckUtil.checkLength(site, DBConst.URL_MAX_LEN)) {
				msg += APConst.HTML_NEW_LINE +MessageUtil.getMessageString(
						APMsgConst.W_MEM_03, APMsgParamConst.MSG_DOWNLOAD_URL,
						String.valueOf(DBConst.URL_MAX_LEN));
			}
		}
		if (msg.startsWith(APConst.HTML_NEW_LINE)) {
			msg = msg.replaceFirst(APConst.HTML_NEW_LINE, "");
		}
		return msg;
	}
}
