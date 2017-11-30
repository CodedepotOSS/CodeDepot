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

import java.sql.SQLException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.ProjectCheckUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.scm.bo.Scm;

import org.json.JSONObject;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * プロジェクト情報更新・追加サーブレット
 * @author fenms
 */
public class ProjUpdateServlet extends BaseServlet {

	/**	直列化ID */
	private static final long serialVersionUID = 4557156603591695563L;

	/** ログ出力 */
	private Logger logger = LoggerFactory.getLogger(ProjUpdateServlet.class);

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		try {
			sqlmap.startTransaction();
			JSONObject json = new JSONObject();
			ProjectInfoBean bean = (ProjectInfoBean) ProjectCheckUtil
					.createFormBean(request, ProjectInfoBean.class);
			if (APConst.TYPE_LOCAL.equals(bean.getSrc_type())) {
				bean.setSrc_path(CommonUtil.replaceDupSlash(bean.getSrc_path()));
			}
			ProjCheckInputServlet.doConfirmCheck(bean, json);

		        HttpSession session = request.getSession();
			Integer role = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ROLE);

			if (!bean.isError()) {
				// プロジェクトの説明
				String description = bean.getDescription();
				bean.setDescription(StringUtils.trimSpace(description));
				boolean isSuccess;
				ProjectDao dao = new ProjectDaoImpl();
				Integer loginUserId = (Integer) request.getSession()
						.getAttribute(APConst.SESSION_LOGIN_ID);
				if (APConst.MODE_ADD.equals(bean.getMode())) {
					String projectName = getProjectName();
					if (StringUtils.isEmpty(projectName)) {
						json.put(APMsgConst.WARN, MessageUtil.getMessageString(
								APMsgConst.E_COM_04,
								APMsgParamConst.MSG_PARAM_PROJECT_NAME_CHAR));
						response.getWriter().print(json);
						return;
					}
					// 追加モデル
					bean.setName(projectName);
					isSuccess = dao.doProjectInsert(sqlmap, bean, loginUserId);
				} else {
					// 変更モデル
					String projectName = dao.checkActive(bean.getName());
					if (StringUtils.isEmpty(projectName)) {
						json.put(APMsgConst.WARN, MessageUtil.getMessageString(
								APMsgConst.E_COM_01, APConst.PROJECT_INFO,
								APConst.UPDATE_CHAR));
						response.getWriter().print(json);
						return;
					}
					if (role != APCodeBook.ROLE_LEVEL_SYSTEM_CODE) {
						ProjectInfoEntity info = dao.getAllProjectInfo(projectName);
						bean.setAdminName(info.getAdmin());
						bean.setSrc_type(info.getSrc_type());
						bean.setSrc_path(URLDecoder.decode(info.getSrc_path(),APConst.ENCODE_UTF_8));
					}
					isSuccess = dao.doProjectUpdate(sqlmap, bean, loginUserId);
				}
				// プロジェクトのスケジューラを更新する。
				if (isSuccess && !StringUtils.isEmpty(bean.getCrontab())) {
					logger.debug("スケジューラ更新");
					isSuccess = runScheduler(request, bean.getTitle(), bean
							.getCrontab());
				} else {
					logger.debug("スケジューラ削除");
					deleteScheduler(request, bean.getTitle());
				}

				// 結果判定
				if (!isSuccess) {
					json.put(APMsgConst.WARN, MessageUtil.getMessageString(
							APMsgConst.E_COM_01, APConst.PROJECT_INFO,
							APConst.UPDATE_CHAR));
					logger.info(MessageUtil.getMessageString(
							APMsgConst.I_COM_10, APConst.MSG_SERVLET));
				} else {
					sqlmap.commitTransaction();
				}
			} else {
				// チェックエラー
				json.put(APMsgConst.WARN, bean.getErrMsg());
			}
			// チェックエラー
			json.put(DBConst.PROJECT_NAME, bean.getName());
			response.getWriter().print(json);
		} catch (Exception e) {
			throw new BaseException(e);
		} finally {
			try {
				sqlmap.endTransaction();
			} catch (SQLException e) {
				throw new BaseException(e);
			}
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/**
	 * プロジェクト識別子名を取得する。
	 *
	 * @return 識別子名
	 * @throws SQLException
	 *             データベース更新異常
	 */
	private String getProjectName() throws SQLException {
		String uid = CommonUtil.getUniqueId();
		ProjectDao dao = new ProjectDaoImpl();
		for (int i = 0; i < APConst.PROJ_NAME_RETRY_TIMES; i++) {
			if (dao.getProjectNameNum(uid) <= 0) {
				break;
			}
			uid = CommonUtil.getUniqueId();
		}
		return uid;
	}

	/**
	 * プロジェクトのスケジューラを更新する。
	 *
	 * @param request
	 *            リクエスト
	 * @param title
	 *            プロジェクト名
	 * @param crond
	 *            検索インデックス更新処理の開始時刻
	 * @return true:成功/false:失敗
	 */
	private boolean runScheduler(HttpServletRequest request, String title,
			String crond) {
		try {
			// スケジューラを取得する。
			Scheduler scheduler = (Scheduler) request.getSession()
					.getServletContext().getAttribute(
							APConst.SESSION_SCHEDULER_INFO);
			// スケジューラが起動されていない
			if (null == scheduler) {
				return true;
			}

			// 検索インデックス更新処理の開始時刻をセットする
			CronTrigger trigger = new CronTrigger(title, APConst.SCHEDULER_GROUP);
			CronExpression cexp = new CronExpression(crond);
			trigger.setCronExpression(cexp);

			if (null == scheduler.getJobDetail(title, APConst.SCHEDULER_GROUP)) {
				// タスクを作成する。
				JobDetail jobDetail = new JobDetail(title,
						APConst.SCHEDULER_GROUP, Scm.class);
				// パラメータを渡す
				jobDetail.getJobDataMap().put(APConst.PROJECT_TITLE, title);

				// スケジューラにタスクを追加する。
				scheduler.scheduleJob(jobDetail, trigger);
			} else if (null == scheduler.getTrigger(title, APConst.SCHEDULER_GROUP)) {
				JobDetail jobDetail = scheduler.getJobDetail(title, APConst.SCHEDULER_GROUP);
				scheduler.scheduleJob(jobDetail, trigger);
			} else {
				// ジョブ情報を設定する。
				JobDetail jobDetail = scheduler.getJobDetail(title, APConst.SCHEDULER_GROUP);
				trigger.setJobName(jobDetail.getName());
				trigger.setJobGroup(jobDetail.getGroup());

				// スケジューラにタスクを追加する。
				scheduler
						.rescheduleJob(title, APConst.SCHEDULER_GROUP, trigger);
			}

			return true;
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			return false;
		}
	}

	/**
	 * プロジェクトのスケジューラを削除する。
	 *
	 * @param request
	 *            リクエスト
	 * @param title
	 *            プロジェクト名
	 * @return true:成功/false:失敗
	 */
	private boolean deleteScheduler(HttpServletRequest request, String title) {

		try {
			// スケジューラを取得する。
			Scheduler scheduler = (Scheduler) request.getSession()
					.getServletContext().getAttribute(
							APConst.SESSION_SCHEDULER_INFO);
			// スケジューラが起動されている
			if (null != scheduler) {
				scheduler.deleteJob(title, APConst.SCHEDULER_GROUP);
			}
			return true;
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			return false;
		}
	}
}
