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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import jp.co.sra.codedepot.scm.dao.SourceDao;
import jp.co.sra.codedepot.scm.dao.SourceDaoImpl;
import jp.co.sra.codedepot.scm.entity.Source;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * プロジェクト一覧サーブレットのイベント処理クラス。
 *
 */
public class ProjectSearchServlet extends BaseServlet {

	/** ログ出力 */
	private Logger logger = LoggerFactory.getLogger(ProjectSearchServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));

		JSONArray json = new JSONArray();
		try {
			HttpSession session = request.getSession();
			Integer loginUserId = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);

			String pid = request.getParameter("pid");
                        if (StringUtils.isEmpty(pid)) {
				ProjectDao dao = new ProjectDaoImpl();
				List <ProjectInfoEntity> list = dao.getPermitProjectList(loginUserId);

				if (list != null && list.size() > 0) {
					for (ProjectInfoEntity e : list) {
						JSONObject obj = e.toJSONObject();
						json.put(obj);
					}
				}
			} else {
                        	List<String> permitList = (List) session
                                        .getAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST);
                        	if (CommonUtil.havePermitAccessCheck(pid, permitList)) {
					SourceDao dao = new SourceDaoImpl();
					String lang = request.getParameter("lang");
					ArrayList<Source> list = dao.getSources(pid, lang);

					if (list != null && list.size() > 0) {
						for (Source s : list) {
							JSONObject obj = s.toJSONObject();
							json.put(obj);
						}
					}
				}
			}
			response.getWriter().print(json);
		} catch (Exception sqlEx) {
			logger.error(MessageUtil.getMessageString(APMsgConst.E_COM_05));
			try {
				response.getWriter().print(json);
			} catch (java.io.IOException e) {
				throw new BaseException(e);
			}
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}
}
