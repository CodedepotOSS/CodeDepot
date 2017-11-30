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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.ChangePageBean;
import jp.co.sra.codedepot.admin.util.ChangePageUtil;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.scm.bo.Scm;

import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;
import com.ibatis.sqlmap.client.SqlMapClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.CronExpression;
import org.quartz.SimpleTrigger;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * プロジェクト検索・一覧画面のイベント処理クラスである。
 *
 * @author sra
 *
 */
public class ProjSearchServlet extends BaseServlet {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(ProjSearchServlet.class);

	private static final long serialVersionUID = 1L;

	public static final String _METHOD_GETPROJLIST = "getProjList";

	public static final String _METHOD_DORUN_ALL = "doRunAll";

	public static final String _METHOD_DORUN = "doRun";

	public static final String _METHOD_DORELOAD = "doReload";

	public static final String _SEARCH_PROJECT_COUNT_SQL = "project.selectProjectCount";

	public static final String _SEARCH_PROJECT_LIST_SQL = "project.selectProjectList";

	/**
	 * Jsonキー:ステータス
	 */
	public static final String JSON_KEY_STATUS = "status";

	/**
	 * ステータス:実行エラー
	 */
	public static final String STATUS_RUN_ERROR = "0";

	/**
	 * ステータス:実行成功
	 */
	public static final String STATUS_RUN_SUCCESS = "1";

	/**
	 * ステータス:実行中
	 */
	public static final String STATUS_RUN_BUSY = "2";
	/**
	 * ステータス:実行待ち
	 */
	public static final String STATUS_RUN_WAIT = "3";

	@SuppressWarnings("unchecked")
	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		HttpSession session = request.getSession();
		String method = request.getParameter(APConst.PARAM_METHOD);
		try {
			if (StringUtils.isEmpty(method)) {
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
						APConst.MSG_SERVLET));
			}

			// 検索ボタンをクリック
			if (_METHOD_GETPROJLIST.equals(method)) {
				Map<String, Object> map = new HashMap<String, Object>();
				String title = request.getParameter(DBConst.PROJECT_TITLE);
				if (!StringUtils.isEmpty(title)) {
					String msg = "";
					String msgWarn = "";
					if (!CheckUtil.isProjName(title)) {
						msg = MessageUtil.getMessageString(APMsgConst.W_COM_07,
								APMsgParamConst.MSG_TITLE,
								APMsgParamConst.MSG_PARAM_PROJECTNAME_CHECK);
						msgWarn += ProjCheckInputServlet.msgHtmlNewLine(msg);
					}
					if (!CheckUtil.checkLength(title, DBConst.TITLE_MAX_LEN)) {
						msg = MessageUtil.getMessageString(APMsgConst.W_MEM_03,
								APMsgParamConst.MSG_TITLE, String
										.valueOf(DBConst.TITLE_MAX_LEN));
						msgWarn += ProjCheckInputServlet.msgHtmlNewLine(msg);
					}
					if (!"".equals(msgWarn)) {
						msgWarn = msgWarn.replaceFirst(APConst.HTML_NEW_LINE, "");
						JSONObject json = new JSONObject();
						json.put(APMsgConst.WARN, msgWarn);
						response.getWriter().print(json);
						return;
					}
					// プロジェクト名が入力されているの場合
					map.put(DBConst.PROJECT_TITLE, CommonUtil.escapeStr(title
							.trim().toLowerCase()));
				}

				// 検索プロジェクト管理テーブル
				map.put(APConst.SESSION_LOGIN_ID, session
						.getAttribute(APConst.SESSION_LOGIN_ID));
				map.put(APConst.SESSION_LOGIN_ROLE, session
						.getAttribute(APConst.SESSION_LOGIN_ROLE));
				int inPage;
				try {
					inPage = Integer.valueOf(request
						.getParameter(APConst.PARAM_PAGE));
				} catch (Exception e) {
					inPage = 1;
				}
				ChangePageBean pageBean = new ChangePageBean();

				pageBean.setInPage(inPage);
				pageBean.setInParamMap(map);
				pageBean.setInCountSqlMap(_SEARCH_PROJECT_COUNT_SQL);
				pageBean.setInListSqlMap(_SEARCH_PROJECT_LIST_SQL);

				ChangePageUtil pageUtil = new ChangePageUtil(pageBean);
				pageUtil.getResult();

				List<ProjectInfoEntity> list = (List<ProjectInfoEntity>) pageBean
						.getOutList();

				JSONArray jsonBeans = new JSONArray();
				// 検索データをJSONObjectに設定する
				for (ProjectInfoEntity bean : list) {
					// 該当プロジェクト実行可否判断する
					if (CommonUtil.checkRunning(bean.getTitle())) {
					        bean.setRunning(1);
					} else if (isBatchWaiting(request, bean.getTitle())) {
						bean.setRunning(2);
					} else {
						bean.setRunning(0);
					}
					bean.setDescription(CommonUtil.cutString(bean.getDescription(), APConst.PROJ_DESCRIPTION_LIMIT));
					jsonBeans.put(bean.toJSONObject());
				}
				JSONObject json = new JSONObject();
				json.put(ChangePageUtil.ITEMCOUNTS, pageBean
								.getOutItemCounts());
				json.put(ChangePageUtil.ITEMSPERPAGE, pageBean
						.getOutItemsPerPage());
				json.put(ChangePageUtil.PAGECOUNTS, pageBean
								.getOutPageCounts());
				json.put(ChangePageUtil.PAGE, pageBean.getOutPage());
				json.put(ChangePageUtil.LISTS, jsonBeans);
				if (0 == pageBean.getOutItemCounts()) {
					json.put(APMsgConst.INFO, MessageUtil
							.getMessageString(APMsgConst.I_COM_03));
				}

				response.getWriter().print(json);
			// 全件実行ボタン押す
			} else if (_METHOD_DORUN_ALL.equals(method)) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(APConst.SESSION_LOGIN_ID, session
						.getAttribute(APConst.SESSION_LOGIN_ID));
				map.put(APConst.SESSION_LOGIN_ROLE, session
						.getAttribute(APConst.SESSION_LOGIN_ROLE));

				String title = request.getParameter(DBConst.PROJECT_TITLE);
				if (!StringUtils.isEmpty(title)) {
					map.put(DBConst.PROJECT_TITLE, CommonUtil.escapeStr(title
							.trim().toLowerCase()));
				}

				SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
				List<ProjectInfoEntity> list = (List<ProjectInfoEntity>)sqlmap.
					queryForList(_SEARCH_PROJECT_LIST_SQL, map);

				JSONArray json = new JSONArray();
				for (ProjectInfoEntity bean : list) {
					// 該当プロジェクト実行可否判断する
					if (CommonUtil.checkRunning(bean.getTitle())) {
					        bean.setRunning(1);
					} else if (isBatchWaiting(request, bean.getTitle())) {
						bean.setRunning(2);
					} else {
						if (runBatchJob(request, bean.getTitle())) {
					        	bean.setRunning(3);
						} else {
							bean.setRunning(0);
						}
					}
					bean.setDescription(CommonUtil.cutString(bean.getDescription(),
						APConst.PROJ_DESCRIPTION_LIMIT));
					json.put(bean.toJSONObject());
				}
				response.getWriter().print(json);

			// リロード
			} else if (_METHOD_DORELOAD.equals(method)) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(APConst.SESSION_LOGIN_ID, session
						.getAttribute(APConst.SESSION_LOGIN_ID));
				map.put(APConst.SESSION_LOGIN_ROLE, session
						.getAttribute(APConst.SESSION_LOGIN_ROLE));
				SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
				List<ProjectInfoEntity> list = (List<ProjectInfoEntity>)sqlmap.
					queryForList(_SEARCH_PROJECT_LIST_SQL, map);

				int count = reloadJob(request, list);
				JSONObject json = new JSONObject();
				json.put("count", count);
				response.getWriter().print(json);

			// 実行ボタン押す
			} else {
				JSONObject json = new JSONObject();

				// プロジェクトタイトルを取得する
				String title = request.getParameter("title");

				// 実行中場合
				if (CommonUtil.checkRunning(title)) {
					json.put(JSON_KEY_STATUS, STATUS_RUN_BUSY);
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.W_SCM_05, title));
				} else if (isBatchWaiting(request, title)) {
					json.put(JSON_KEY_STATUS, STATUS_RUN_WAIT);
					json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.W_SCM_06, title));
				} else {
					// バッチジョブ実行成功場合
					if (runBatchJob(request, title)) {
						json.put(JSON_KEY_STATUS, STATUS_RUN_SUCCESS);
						json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.I_COM_09,
								APMsgParamConst.MSG_PARAM_BATCH_RUN_ERROR));
					// バッチジョブ実行失敗場合
					} else {
						json.put(JSON_KEY_STATUS, STATUS_RUN_ERROR);
						json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.I_COM_10,
								APMsgParamConst.MSG_PARAM_BATCH_RUN_ERROR));
					}
				}

				response.getWriter().print(json);
			}
		} catch (Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/**
	 * バッチジョブを行う
	 *
	 * @param request リクエスト
	 * @param title プロジェクト名
	 * @return true:成功/false:失敗
	 */
	private boolean runBatchJob(HttpServletRequest request, String title) {

		try {
			// スケジューラグを取得する
			Scheduler scheduler = (Scheduler) request.getSession()
					.getServletContext().getAttribute(APConst.SESSION_SCHEDULER_INFO);

			// tiggerを作成する
			Trigger trigger = new SimpleTrigger("*" + title, APConst.SIMPLE_GROUP);

			// ジョブがない場合
			if (null == scheduler.getJobDetail(title, APConst.SIMPLE_GROUP)) {
				// タスクを作成する。
				JobDetail jobDetail = new JobDetail(title, APConst.SIMPLE_GROUP, Scm.class);
				jobDetail.getJobDataMap().put(APConst.PROJECT_TITLE, title);

				// スケジューラにタスクを追加する。
				scheduler.scheduleJob(jobDetail, trigger);
			} else if (null == scheduler.getTrigger("*" + title, APConst.SIMPLE_GROUP)) {
				// スケジューラに Trigger を追加する。
				JobDetail jobDetail = scheduler.getJobDetail(title, APConst.SIMPLE_GROUP);
				scheduler.scheduleJob(jobDetail, trigger);
			} else {
				// スケジューラグにタスクを追加する。
				JobDetail jobDetail = scheduler.getJobDetail(title, APConst.SIMPLE_GROUP);
				trigger.setJobName(jobDetail.getName());
            			trigger.setJobGroup(jobDetail.getGroup());
				scheduler.rescheduleJob(title, APConst.SIMPLE_GROUP, trigger);
			}

			return true;
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			return false;
		}
	}

	private boolean isBatchWaiting(HttpServletRequest request, String title) {
		try {
			// スケジューラグを取得する
			Scheduler scheduler = (Scheduler) request.getSession()
				.getServletContext().getAttribute(APConst.SESSION_SCHEDULER_INFO);

			// triggerを作成する
			if (null != scheduler.getTrigger("*" + title, APConst.SIMPLE_GROUP)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

        private int reloadJob(HttpServletRequest request, List<ProjectInfoEntity> list) {
 		// スケジューラを取得する。
               	Scheduler scheduler = (Scheduler) request.getSession()
                             			.getServletContext().getAttribute(APConst.SESSION_SCHEDULER_INFO);
		if (null == scheduler) {
			return 0;
		}

		int count = 0;
		for (ProjectInfoEntity bean : list) {
			try {
				String title = bean.getTitle();
				if (!StringUtils.isEmpty(bean.getCrontab())) {
					// 検索インデックス更新処理の開始時刻をセットする
					CronTrigger trigger = new CronTrigger(title, APConst.SCHEDULER_GROUP);
					CronExpression cexp = new CronExpression(bean.getCrontab());
					trigger.setCronExpression(cexp);

                                	JobDetail jobDetail = scheduler.getJobDetail(title, APConst.SCHEDULER_GROUP);
					if (null == jobDetail) {
						// タスクを作成する。
						jobDetail = new JobDetail(title, APConst.SCHEDULER_GROUP, Scm.class);
                                		// パラメータを渡す
                                		jobDetail.getJobDataMap().put(APConst.PROJECT_TITLE, title);
                                		// スケジューラグにタスクを追加する。
						logger.debug("スケジューラ追加");
                                		scheduler.scheduleJob(jobDetail, trigger);
                        		} else if (null == scheduler.getTrigger(title, APConst.SCHEDULER_GROUP)) {
                                		// スケジューラグにタスクを追加する。
						logger.debug("スケジューラ追加");
                                		scheduler.scheduleJob(jobDetail, trigger);
                        		} else {
                                		// スケジューラグにタスクを追加する。
						trigger.setJobName(jobDetail.getName());
            					trigger.setJobGroup(jobDetail.getGroup());
						logger.debug("スケジューラ更新");
                                		scheduler.rescheduleJob(title, APConst.SCHEDULER_GROUP, trigger);
                        		}
					count = count + 1;
                                } else {
					logger.debug("スケジューラ削除");
					scheduler.deleteJob(title, APConst.SCHEDULER_GROUP);
                                }
               		} catch (Exception e) {
                		logger.error(BaseException.getStackTraceStr(e));
			}
                }
		return count;
        }
}
