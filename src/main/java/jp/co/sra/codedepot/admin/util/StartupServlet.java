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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jp.co.sra.codedepot.admin.context.ContextFactoryLoader;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.scm.bo.Scm;

import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupServlet implements Servlet {
	/** ログ出力 */
	private Logger logger = LoggerFactory.getLogger(StartupServlet.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
        try {
        	APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
        	MessageUtil.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_MSG);
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			initJobs(scheduler);
			scheduler.start();
			config.getServletContext().setAttribute(APConst.SESSION_SCHEDULER_INFO, scheduler);
			ContextFactoryLoader.init();
		} catch(Exception e) {
	        throw new ServletException(e);
	    }
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * DBに保存している定時起動ジョブを設定する
	 *
	 * @param scheduler
	 */
	private void initJobs(Scheduler scheduler) {
		try {
			ProjectDao dao = new ProjectDaoImpl();
			List<ProjectInfoEntity> crontabList = dao.getAllCrontab();
			if (null != crontabList && crontabList.size() > 0) {
				JobDetail jobDetail = null;
				CronTrigger trigger = null;
				CronExpression cexp = null;
				for (ProjectInfoEntity pe : crontabList) {
					try {
						// タスクを作成する。
						jobDetail = new JobDetail(pe.getTitle(), APConst.SCHEDULER_GROUP,
								Scm.class);
						// パラメータを渡す
						jobDetail.getJobDataMap().put(APConst.PROJECT_TITLE, pe.getTitle());
						// 検索インデックス更新処理の開始時刻をセットする
						trigger = new CronTrigger(pe.getTitle(),
								APConst.SCHEDULER_GROUP);
						cexp = new CronExpression(pe.getCrontab());
						trigger.setCronExpression(cexp);

						// スケジューラグにタスクを追加する。
						scheduler.scheduleJob(jobDetail, trigger);
						logger.debug("検索インデックス更新ジョブ定時起動設定成功 PJ名 " + pe.getTitle() + " 開始時刻 "
								+ pe.getCrontab());
					} catch (Exception e) {
						logger.debug("検索インデックス更新ジョブ定時起動設定失敗 PJ名 " + pe.getTitle() + " 開始時刻 "
								+ pe.getCrontab());
						logger.error(BaseException.getStackTraceStr(e));
					}
				}
			}
		} catch (SQLException sqle) {
			logger.debug("検索インデックス更新時刻検索失敗しました。");
			logger.error(BaseException.getStackTraceStr(sqle));
		}
	}
}
