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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.dao.TotalSourceDao;
import jp.co.sra.codedepot.admin.db.dao.TotalSourceDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.db.entity.TotalSourceEntity;
import jp.co.sra.codedepot.admin.db.dao.NoteDao;
import jp.co.sra.codedepot.admin.db.dao.NoteDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.NoteEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

	/***
	 * プロジェクト詳細情報画面(一般ユーザ用)のServletクラス
	 * @author sra
	 *
	 */
	public class ProjectDetailForNormalServlet extends BaseServlet {

		private static final long serialVersionUID = 1L;
		// ログ
		private static final Logger logger =
			LoggerFactory.getLogger(ProjectDetailForNormalServlet.class);
		// ファイル集計情報
		public static final String SRC_LIST = "srcList";
		// ノート集計情報
		public static final String NOTES = "notes";
		public static final String PERSONAL_NOTES = "personalNotes";
		public static final String PUBLIC_NOTES = "publicNotes";
		// プロジェクト情報
		public static final String PROJECTINFO = "projecInfo";
		// パラメータの識別子名
		public static final String PRAM_PRJ_NAME = "pid";
		public static final String PARAM_PERMIT = "permit";

	/***
	 * プロジェクト情報を取得する
	 * @param name 識別子名
	 * @return プロジェクト情報
	 */
	private ProjectInfoEntity getProjectInfo(String name) throws Exception {

		ProjectInfoEntity bean = new ProjectInfoEntity();
		ProjectDao dao = null;
			dao = new ProjectDaoImpl();
			// DAOからプロジェクト情報を取得する
			bean = dao.getProjectInfo(name);
		return bean;
	}
	/***
	 * ファイル集計情報を取得する
	 * @param name 識別子名
	 * @return ファイル集計情報
	 */
	private  ArrayList<TotalSourceEntity> getProjectSrcInfo (String name) throws Exception{
		TotalSourceDao dao = null;
		ArrayList<TotalSourceEntity> srcList = null;
			dao = new TotalSourceDaoImpl();
			// DAOからファイル集計情報を取得する
			srcList = dao.getProjectSrcInfo(name);
		return srcList;

	}

	/***
	 * パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
	 *
	 * @param project プロジェクト識別子名
	 * @param loginID ログインID
	 * @return  アクセス制御のチェック成功 : true
	 * 			 アクセス制御のチェック失敗 : false
	 * @throws SQLException
	 */
	private boolean havePermitAccess(String project, List<String> permitList)
			throws SQLException {
		boolean havePermit = true;
		// アクセス制御のチェック
		if (!CommonUtil.havePermitAccessCheck(project, permitList)) {
			havePermit = false;
		}
		return havePermit;
	}

	/***
	 * プロジェクト詳細情報画面(一般ユーザ用)のProcess
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	@SuppressWarnings("unchecked")
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		response.setContentType("text/html; charset=utf-8");
		// 識別子名を取得
		String name = request.getParameter(PRAM_PRJ_NAME);
		HttpSession session = null;
		try {
			session = request.getSession();
			// 識別子名が存在しない場合、処理を終了する。
			if (StringUtils.isEmpty(name)) {
				logger.error(APMsgConst.ERROR, "parameter is not correct");
				response.getWriter().print("");
				return;
			}
			JSONObject json = new JSONObject();
			json.put(PRAM_PRJ_NAME, name);
			// セッション.アクセス権限情報のリスト
			List<String> permitList = (List)session.getAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST);
			// パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
			boolean havePermitAccess = true;
			havePermitAccess = havePermitAccess(name, permitList);
			// アクセス制御のチェック成功の場合
			if (!havePermitAccess) {
				json.put(PARAM_PERMIT, false);
			} else{
				json.put(PARAM_PERMIT, true);
			}
			ProjectInfoEntity pjInfo = null;
			// プロジェクト情報を取得する
			pjInfo = getProjectInfo(name);
			try {
				if (!StringUtils.isEmpty(pjInfo.getSite_url())) {
				pjInfo.setSite_url(URLDecoder.decode(pjInfo.getSite_url(), APConst.ENCODE_UTF_8));
				}
			} catch(UnsupportedEncodingException  uee) {
				logger.debug(uee.getMessage());
			}
			try {
				if (!StringUtils.isEmpty(pjInfo.getDownload_url())) {
				pjInfo.setDownload_url((URLDecoder.decode(pjInfo.getDownload_url(), APConst.ENCODE_UTF_8)));
				}
			} catch(UnsupportedEncodingException  uee) {
				logger.debug(uee.getMessage());
			}

			if (StringUtils.isEmpty(pjInfo.getIgnores())) {
        			String resource = "indexer.properties";
        			try {
            				Properties props = new Properties();
            				ClassLoader loader = Thread.currentThread().getContextClassLoader();
            				InputStream stream = loader.getResourceAsStream(resource);
            				props.load(stream);
					pjInfo.setIgnores(props.getProperty("ignoreList"));
            				stream.close();
				} catch (Exception e) {
					;
        			}
			}

			JSONObject projectJson = new JSONObject();
			if(null != pjInfo){
				projectJson = pjInfo.toJSONObject();
			}

			json.put(PROJECTINFO, projectJson);
			JSONArray jsonBeans = new JSONArray();
			ArrayList<TotalSourceEntity> srcList = null;
			// ファイル集計情報を取得する
			srcList = getProjectSrcInfo(name);
			for (TotalSourceEntity bean : srcList) {
				jsonBeans.put(bean.toJSONObject());
			}
			json.put(SRC_LIST, jsonBeans);

			// ノート集計情報を取得する (V21)
			Integer id = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);
			int personalNotes = countPersonalNotes(id, name);
			int publicNotes = countPublicNotes(name);
			JSONObject notes = new JSONObject();
			notes.put(PERSONAL_NOTES, personalNotes);
			notes.put(PUBLIC_NOTES, publicNotes);
			json.put(NOTES, notes);

			// 結果を送信
			response.getWriter().print(json);
		}catch(SQLException se) {
			logger.error(BaseException.getStackTraceStr(se));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_SERVLET));
			try {
				JSONObject errorjson = new JSONObject();
				//ファイル集計情報とプロジェクト情報の取得に失敗した場合、メッセージを表示して、処理を終了する。
				errorjson.put(APMsgConst.WARN,
						MessageUtil.getMessageString(APMsgConst.E_COM_04, "プロジェクト情報"));
				response.getWriter().print(errorjson);
			} catch (Exception e1) {
			}

		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_SERVLET));
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}
	/***
	 * ノート集計情報を取得する
	 * @param name 識別子名
	 * @return ノート数
	 */
	private Integer countPersonalNotes(Integer uid, String project) {
		try {
			NoteDao dao = new NoteDaoImpl();
			return dao.countPersonalNotes(uid, project);
		} catch (SQLException e) {
			return 0;
		}
	}

	private Integer countPublicNotes(String project) {
		try {
			NoteDao dao = new NoteDaoImpl();
			return dao.countPublicNotes(project);
		} catch (SQLException e) {
			return 0;
		}
	}
}
