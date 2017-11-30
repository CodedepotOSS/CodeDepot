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
package jp.co.sra.codedepot.admin.note.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.NoteDao;
import jp.co.sra.codedepot.admin.db.dao.NoteDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.NoteEntity;
import jp.co.sra.codedepot.admin.db.entity.FileEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import java.net.URLDecoder;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * G_07_01_ノート一覧画面
 * @author sra
 *
 */
public class NoteSearchServlet extends BaseServlet {
	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(NoteSearchServlet.class);
	private static final long serialVersionUID = 1L;
	/** ノート情報メッセージ リスト*/
	private JSONObject noteInfoJson = null;
	/** ノート情報リスト */
	private static final String NOTE_INFO_LIST = "noteInfoList";
	/** 成功場合の遷移先 */
	private static final String PARAM_PERMIT = "permit";
	/** プロジェクト識別子名 */
	private static final String PARAM_PROJECT_NAME = "prjName";
	/** ファイルのパス */
	private static final String PARAM_PATH = "path";
	/** ファイルID */
	private static final String PARAM_FILE_ID= "fileID";
	/** ノートID */
	private static final String PARAM_NOTE_ID= "noteID";
	/** ノート情報の件数 */
	private static final String PARAM_NOTE_COUNT= "noteCount";

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response)throws BaseException{

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		try {
			// ノート一覧画面のロジック
			noteLogicHandle(request);
			response.getWriter().println(noteInfoJson);
		} catch (Exception e) {
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/***
	 * ノート一覧画面のロジック
	 *
	 * @param request HttpServletRequest
	 *
	 */
	private void noteLogicHandle(HttpServletRequest request)throws SQLException,
			JSONException,UnsupportedEncodingException {
		String mode = request.getParameter(APConst.MODE);
		noteInfoJson = new JSONObject();
		try {
			// 初期化の場合
			if (APConst.MODE_INIT.equals(mode)) {
				// 初期化の情報を取得する
				getNoteInit(request);
			} else if (APConst.MODE_MODIFY.equals(mode)) { // 変更の場合
				// ノート変更情報を取得する
				getNoteInfo(request);
			} else if (APConst.MODE_DEL.equals(mode)) { // 削除の場合
				// 選択されたノート情報を削除する
				deleteNote(request);
			} else if (APConst.MODE_LIST.equals(mode)) { // 画面一覧を取得する場合
				String fileIDStr = request.getParameter(PARAM_FILE_ID);
				String projName = (String) request.getParameter(PARAM_PROJECT_NAME);
				if (!StringUtils.isEmpty(fileIDStr)) {
					getNoteList(request);
				} else if (!StringUtils.isEmpty(projName)) {
					getProjectNoteList(request);
				} else {
					searchNoteList(request);
				}
			} else if (APConst.MODE_ADD.equals(mode)) { // 追加の場合
				getAddPermit(request);
			}
		} catch (SQLException e) {
			throw e;
		} catch (JSONException ex) {
			throw ex;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
	}

	/***
	 * ノート一覧概要画面情報を取得する
	 *
	 * @param request
	 *            HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void getNoteInit(HttpServletRequest request) throws SQLException,
			JSONException, UnsupportedEncodingException{

		NoteDao dao = null;
		// ファイルID
		int fileID = -1;
		int noteCount = 0;
		// プロジェクト識別子
		String project = (String) request.getParameter(PARAM_PROJECT_NAME);
		// ファイルのパス
		String path = (String) request.getParameter(PARAM_PATH);
		path = path.trim();
		HttpSession session = null;
		try {
			path = URLEncoder.encode(path, APConst.ENCODE_UTF_8);
			dao = new NoteDaoImpl();
			session = request.getSession();
			int loginID = (Integer) session
					.getAttribute(APConst.SESSION_LOGIN_ID);
			// パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
			boolean havePermitAccess = true;
			havePermitAccess = havePermitAccess(project, request);
			// アクセス制御のチェック成功の場合
			if (havePermitAccess) {
				fileID = dao.getFileID(project, path);
				if (fileID != -1) { // ファイルID取得成功の場合
					noteCount = dao.getNoteCount(fileID, loginID);
				}
				noteInfoJson.put(PARAM_NOTE_COUNT, noteCount);
				noteInfoJson.put(PARAM_FILE_ID, fileID);
				noteInfoJson.put(PARAM_PERMIT, true);
			} else {
				noteInfoJson.put(PARAM_PERMIT, false);
			}
		} catch (SQLException e) {
			throw e;
		} catch (JSONException ex) {
			throw ex;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
	}

	/***
	 * パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
	 *
	 * @param project プロジェクト識別子名
	 * @param request HttpServletRequest
	 * @return  アクセス制御のチェック成功 : true
	 * 			 アクセス制御のチェック失敗 : false
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	private boolean havePermitAccess(String project, HttpServletRequest request)
			throws SQLException {
		HttpSession session = request.getSession();
		List<String> permitList = (List)session.getAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST);
		return CommonUtil.havePermitAccessCheck(project, permitList);
	}


	/***
	 * ノート一覧詳細画面情報を取得する
	 *
	 * @param request HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void getNoteList(HttpServletRequest request) throws SQLException,
			JSONException {
		NoteDao dao = null;
		// ファイルID
		String fileIDStr = (String) request.getParameter(PARAM_FILE_ID);
		int fileID = 0;
		if (!StringUtils.isEmpty(fileIDStr)) {
			fileID = Integer.valueOf(fileIDStr);
		}
		List<NoteEntity> noteList = null;
		HttpSession session = null;
		try {
			dao = new NoteDaoImpl();
			session = request.getSession();
			// ログインID
			int loginID = (Integer) session
					.getAttribute(APConst.SESSION_LOGIN_ID);
			// ノート情報リストを取得する
			noteList = dao.getNoteInfoList(fileID, loginID);
			JSONArray jsonBeans = new JSONArray();
			for (NoteEntity bean : noteList) {
				jsonBeans.put(bean.toNoteJsonLists());
			}
			noteInfoJson.put(APConst.MODE_LIST, jsonBeans);
			noteInfoJson.put(APConst.SESSION_LOGIN_ID, loginID);

		} catch (SQLException e) {
			throw e;
		} catch (JSONException ex) {
			throw ex;
		}
	}

	/***
	 * ノート情報を削除する
	 *
	 * @param request HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void deleteNote(HttpServletRequest request) throws SQLException,
			JSONException {
		NoteDao dao = null;
		// ノートID
		String noteIDStr = (String) request.getParameter(PARAM_NOTE_ID);
		// プロジェクト識別子名
		String project = (String) request.getParameter(PARAM_PROJECT_NAME);
		// パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
		boolean havePermitAccess = true;
		havePermitAccess = havePermitAccess(project, request);
		// アクセス制御のチェック成功の場合
		if (havePermitAccess) {
			noteInfoJson.put(PARAM_PERMIT, true);
			// ノートID
			int noteID = 0;
			if (!StringUtils.isEmpty(noteIDStr)) {
				noteID = Integer.valueOf(noteIDStr);
			}
			int delresult = 0;
			try {
				dao = new NoteDaoImpl();
				// ノート情報削除する
				delresult = dao.deleteNoteInfo(noteID);
				// 削除失敗の場合
				if (delresult == 0) {
					noteInfoJson.put(APMsgConst.WARN, MessageUtil
							.getMessageString(APMsgConst.E_COM_01, APMsgParamConst.MSG_PARAM_NOTE_TITLE,
									APConst.DELETE_CHAR));
				} else {
					// 画面再表示の情報を取得する
					getNoteList(request);
				}
			} catch (SQLException e) {
				throw e;
			} catch (JSONException ex) {
				throw ex;
			}
		} else {
			// アクセス制御のチェック失敗
			noteInfoJson.put(PARAM_PERMIT, false);
		}
	}

	/***
	 * ノート追加・変更画面用情報を取得する
	 *
	 * @param request HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void getNoteInfo(HttpServletRequest request)throws SQLException,JSONException{
		NoteDao dao = null;
		// ノートID
		String noteIDStr = (String)request.getParameter(PARAM_NOTE_ID);
		// プロジェクト識別子名
		String project = (String) request.getParameter(PARAM_PROJECT_NAME);
		// パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
		boolean havePermitAccess = true;
		// アクセス制御のチェック
		havePermitAccess = havePermitAccess(project,request);
		// アクセス制御のチェック成功の場合
		if(havePermitAccess){
			// アクセス制御のチェック成功
			noteInfoJson.put(PARAM_PERMIT,true);
			int noteID = 0;
			if(!StringUtils.isEmpty(noteIDStr)){
				noteID = Integer.valueOf(noteIDStr);
			}
			NoteEntity bean = null;
			JSONObject noteInfoList = null;
			try {
				dao = new NoteDaoImpl();
				// ノート情報を取得する。
				bean = dao.getNoteInfo(noteID);
				noteInfoList = new JSONObject();
				if(null != bean){
					noteInfoList = bean.toNoteListJson();
				}
				// 取得したノート情報をセットする
				noteInfoJson.put(NOTE_INFO_LIST,noteInfoList);
			} catch (SQLException e) {
				throw e;
			} catch (JSONException ex) {
				throw ex;
			}
		}else{
			// アクセス制御のチェック失敗
			noteInfoJson.put(PARAM_PERMIT,false);
		}
	}

	/***
	 * アクセス制御のチェックを行う
	 *
	 * @param request
	 *            HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void getAddPermit(HttpServletRequest request) throws SQLException,
			JSONException {
		String project = (String) request.getParameter(PARAM_PROJECT_NAME);
		boolean havePermitAccess = true;
		// パラメータ.プロジェクト識別子名により、アクセス制御のチェックを行う。
		havePermitAccess = havePermitAccess(project, request);
		// アクセス制御のチェック成功の場合
		if (havePermitAccess) {
			noteInfoJson.put(PARAM_PERMIT, true);
		} else {
			// アクセス制御のチェック失敗
			noteInfoJson.put(PARAM_PERMIT, false);
		}
	}

	/***
	 * プロジェクトのノート情報を取得する
	 *
	 * @param request HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void getProjectNoteList(HttpServletRequest request) throws SQLException, JSONException {
		try {
			HttpSession session = request.getSession();
			NoteDao dao = new NoteDaoImpl();
			String pid = (String) request.getParameter(PARAM_PROJECT_NAME);
			String kind = (String) request.getParameter(APConst.KIND);
			Integer uid = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);

			List<NoteEntity> list = null;
			if (APConst.KIND_PUBLIC.equals(kind)) {
				list = dao.selectPublicNotes(pid);
			} else {
				list = dao.selectPersonalNotes(uid, pid);
			}
			JSONArray jsonBeans = new JSONArray();
			for (NoteEntity bean : list) {
				JSONObject obj = bean.toNoteJsonLists();
				try {
					String path = URLDecoder.decode(bean.getPath(), APConst.ENCODE_UTF_8);
					obj.put("path", path);
				} catch (IOException e) {
					;
				}
				jsonBeans.put(obj);

			}
			noteInfoJson.put(APConst.MODE_LIST, jsonBeans);
			noteInfoJson.put(APConst.SESSION_LOGIN_ID, uid);
		} catch (SQLException e) {
			throw e;
		}
	}

	private void searchNoteList(HttpServletRequest request) throws SQLException, JSONException {
		try {
			HttpSession session = request.getSession();
			NoteDao dao = new NoteDaoImpl();

			Integer loginId = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);
			String kind = (String) request.getParameter(APConst.KIND);
			String project = (String) request.getParameter("project");
			String content = (String) request.getParameter("qtext");
			String file = (String) request.getParameter("file");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("loginID", loginId);
			map.put("project", project);
			map.put("content", content);
			map.put("file", file);

			if (APConst.KIND_PUBLIC.equals(kind)) {
				map.put("public", true);
			} else {
				map.put("uid", loginId);
			}

			List<NoteEntity> list = null;
			list = dao.searchNotes(map);

			JSONArray jsonBeans = new JSONArray();
			for (NoteEntity bean : list) {
				JSONObject obj = bean.toNoteJsonLists();
				try {
					String path = URLDecoder.decode(bean.getPath(), APConst.ENCODE_UTF_8);
					obj.put("path", path);
				} catch (IOException e) {
					;
				}
				jsonBeans.put(obj);
			}
			noteInfoJson.put(APConst.MODE_LIST, jsonBeans);
			noteInfoJson.put(APConst.SESSION_LOGIN_ID, loginId);
		} catch (SQLException e) {
			throw e;
		}
	}

}
