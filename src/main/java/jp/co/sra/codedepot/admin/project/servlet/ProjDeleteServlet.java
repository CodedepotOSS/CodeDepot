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

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.ProjectCheckUtil;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.scm.bo.UpdSrchIndex;
import jp.co.sra.codedepot.scm.dao.SourceDao;
import jp.co.sra.codedepot.scm.dao.SourceDaoImpl;

import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import org.json.JSONObject;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * プロジェクト情報削除サーブレット
 *
 * @author fenms
 */
public class ProjDeleteServlet extends BaseServlet {

	/** 直列化ID */
	private static final long serialVersionUID = -5779841414516936841L;

	/** ログ出力 */
	private Logger logger = LoggerFactory.getLogger(ProjDeleteServlet.class);

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		JSONObject json = new JSONObject();
		try {
			sqlmap.startTransaction();
			Integer loginUserId = (Integer) request.getSession().getAttribute(
					APConst.SESSION_LOGIN_ID);
			ProjectInfoBean bean = (ProjectInfoBean) ProjectCheckUtil.createFormBean(
					request, ProjectInfoBean.class);

			String name = bean.getName();
			// データベースレコードを更新する。
			ProjectDao dao = new ProjectDaoImpl();
			ProjectInfoEntity info = dao.getProjectInfo(name);
			if (info == null) {
				json.put(APMsgConst.WARN, MessageUtil.getMessageString(
						APMsgConst.E_COM_08));
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
						APConst.MSG_SERVLET));
				response.getWriter().print(json);
				return;
			}

			String title = info.getTitle();
			boolean isSuccess = dao.doProjectDelete(sqlmap, name, loginUserId);

			if (!isSuccess) {
				json.put(APMsgConst.WARN, MessageUtil.getMessageString(
						APMsgConst.E_COM_08));
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
						APConst.MSG_SERVLET));
				response.getWriter().print(json);
				return;
			} else {
				try {
// Added by wubo on 2010/09/19 for V2.1対応 Start
					// プロジェクトのソースとHTMLを削除する。
					SourceDao sourceDao = new SourceDaoImpl();
					sourceDao.deleteSourceByProject(name);
// Added by wubo on 2010/09/19 for V2.1対応 End
					UpdSrchIndex updIndex = new UpdSrchIndex(name);
					updIndex.clearIndexWithCommit();
					// プロジェクトのソースとHTMLを削除する。
					Properties props = CommonUtil.getPropertiesContext(APConst.PROPERTY_INDEXER);
					if (null != props) {
						String htmlDirectory = props.getProperty(APConst.PROP_KEY_HTML_DIRECTORY);
						String codeDirectory = props.getProperty(APConst.PROP_KEY_CODE_DIRECTORY);
						String tempDirectory = props.getProperty(APConst.PROP_KEY_TEMP_DIRECTORY);
						// ソースを削除する。
						deleteAll(new File(codeDirectory + APConst.PATH_LOCAL + name));
						// HTMLを削除する。
						deleteAll(new File(htmlDirectory + APConst.PATH_LOCAL + name));
						// TEMPを削除する。
						deleteAll(new File(tempDirectory + APConst.PATH_LOCAL + name));
					}

					//スケジューラグを削除する。
					Scheduler scheduler = (Scheduler) request.getSession().getServletContext().getAttribute(APConst.SESSION_SCHEDULER_INFO);
					if (null != scheduler) {
						scheduler.unscheduleJob("*" + title, APConst.SIMPLE_GROUP);
						scheduler.deleteJob(title, APConst.SCHEDULER_GROUP);
					}
					isSuccess = true;
				} catch (Exception e) {
					logger.error(MessageUtil.getMessageString(APMsgConst.E_SCM_12, title));
					logger.error(BaseException.getStackTraceStr(e));
					isSuccess = false;
				}
			}
			// 削除結果を判断する。
			if (!isSuccess) {
				json.put(APMsgConst.WARN, MessageUtil.getMessageString(
						APMsgConst.E_COM_01, APConst.PROJECT_INFO,
						APConst.DELETE_CHAR));
				logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
						APConst.MSG_SERVLET));
			} else {
				sqlmap.commitTransaction();
			}
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
	 * ディレクトリー下すべてファイルを削除するメソッドである。
	 * @param path ディレクトリーのパス
	 */
	public void deleteAll(File path) {
		if (!path.exists()) {
			return;
		}
		if (path.isFile()) {
			path.delete();
			return;
		}
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			deleteAll(files[i]);
		}
		path.delete();
	}
}
