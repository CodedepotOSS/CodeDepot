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
package jp.co.sra.codedepot.admin.scm.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.BatchlogDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.BatchLogEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.ChangePageBean;
import jp.co.sra.codedepot.admin.util.ChangePageUtil;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

/**
 * バッチ処理ログ表示画面の検索サーブレット
 * @author fenms
 */
public class LogSearchServlet extends BaseServlet {

	/**	直列化ID */
	private static final long serialVersionUID = -6119799600827804769L;

	/** ログ出力 */
	private Logger logger = LoggerFactory.getLogger(LogSearchServlet.class);

	@SuppressWarnings("unchecked")
	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			// 終了状態パラメータを取得する。
			String statusStr = request.getParameter(DBConst.BATCHLOG_STATUS);
			if (String.valueOf(true).equalsIgnoreCase(statusStr)) {
				map.put(DBConst.BATCHLOG_STATUS, true);
			} else if (String.valueOf(false).equalsIgnoreCase(statusStr)) {
				map.put(DBConst.BATCHLOG_STATUS, false);
			}
			// プロジェクト識別子名パラメータを取得する。
			String name = request.getParameter(DBConst.BATCHLOG_NAME);
			if(!StringUtils.isEmpty(name)){
				map.put(DBConst.BATCHLOG_NAME, name);
			}
			// プロジェクト識別子名パラメータを取得する。
			String title = request.getParameter(DBConst.PROJECT_TITLE);
			if(!StringUtils.isEmpty(title)){
				map.put(DBConst.PROJECT_TITLE, title.trim().toLowerCase());
			}
			// ログインID
			Integer userId = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ID);
			// ログイン権限
			Integer userGroup = (Integer) request.getSession().getAttribute(APConst.SESSION_LOGIN_ROLE);
			if(APCodeBook.ROLE_LEVEL_MANAGER_CODE == userGroup){
				// プロジェクト管理者
				map.put(DBConst.PROJECT_ADMIN, userId);
			}else{
				// システム管理者の場合、パラメータがない。
			}
			// ページ番
			int inPage = Integer.valueOf(request.getParameter("page"));
			// 検索用パラメータを作成する。
			ChangePageBean pageBean = new ChangePageBean();
			pageBean.setInPage(inPage);
			pageBean.setInParamMap(map);
			pageBean.setInCountSqlMap(BatchlogDaoImpl.SELECT_TOTAL_COUNT_SQL_ID);
			pageBean.setInListSqlMap(BatchlogDaoImpl.SELECT_LOG_INFO_SQL_ID);

			ChangePageUtil pageUtil = new ChangePageUtil(pageBean);
			pageUtil.getResult();
			// 検索結果を取得する。
			List<BatchLogEntity> list = (List<BatchLogEntity>)pageBean.getOutList();
			JSONObject json = new JSONObject();
			JSONArray jsonBeans = new JSONArray();
			for (BatchLogEntity bean : list) {
				jsonBeans.put(bean.toJSONObject());
			}
			json.put(ChangePageUtil.ITEMCOUNTS, pageBean.getOutItemCounts());
			json.put(ChangePageUtil.ITEMSPERPAGE, pageBean.getOutItemsPerPage());
			json.put(ChangePageUtil.PAGECOUNTS, pageBean.getOutPageCounts());
			json.put(ChangePageUtil.PAGE, pageBean.getOutPage());
			json.put(ChangePageUtil.LISTS, jsonBeans);
			if (0 == pageBean.getOutItemCounts()) {
				json.put(APMsgConst.INFO, MessageUtil.getMessageString(APMsgConst.I_COM_03));
			}
			// 検索結果を返却する。
			response.getWriter().print(json);
		} catch (Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}
}
