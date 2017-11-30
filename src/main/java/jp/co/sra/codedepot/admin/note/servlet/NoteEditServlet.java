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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.NoteDao;
import jp.co.sra.codedepot.admin.db.dao.NoteDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.FileEntity;
import jp.co.sra.codedepot.admin.db.entity.NoteEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteEditServlet extends BaseServlet {
	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(NoteEditServlet.class);
	private static final long serialVersionUID = 1L;
	// エラーメッセージのリスト
	private JSONObject errorJson = null;
	// 画面モード
	private String mode = "";
	// ラインFrom
	private String lineFrom = "";
	// ラインTo
	private String lineTo = "";
	// 本文
	private String contents = "";
	// プロジェクト識別子名
	private String  project = "";
	// URL エンコードしたファイルのパス名
	private String path = "";
	// ファイルID
	private int fileID = 0;
	// チェック成功のフラグ
	private static final String SUCCESS_FALG = "success";
	// ラインFrom
	private static final String PARAM_LINE_FROM = "lineFrom";
	// ラインTo
	private static final String PARAM_LINE_TO = "lineTo";
	// 本文
	private static final String PARAM_CONTENTS = "contents";
	// プロジェクト識別子名
	private static final String PARAM_PROJECT_NAME = "project";
	// ファイルのパス
	private static final String PARAM_PATH = "path";
	// 公開フラグ
	private static final String PARAM_PUBLIC_FLAG = "publicFlag";
	// ファイルID
	private static final String PARAM_FILE_ID= "fileID";
	// ノートID
	private static final String PARAM_NOTE_ID= "noteID";

	@Override
	protected synchronized void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		try {
			// ノート追加・変更画面のロジック
			noteEditLogicHandle(request);
			if(null == errorJson || errorJson.length() <= 0){
				errorJson.put(SUCCESS_FALG, SUCCESS_FALG);
			}
			response.getWriter().print(errorJson);

		} catch (Exception e) {
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));
	}

	/***
	 * ノート追加・変更画面のロジック
	 * @param request HttpServletRequest
	 * @throws SQLException
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	private void noteEditLogicHandle(HttpServletRequest request)
			throws SQLException, JSONException, UnsupportedEncodingException {
		// チェックフラグ
		boolean isCheckedOk = true;
		try {
			errorJson = new JSONObject();
			// ノート追加・変更画面のチェック
			isCheckedOk = doNoteEditInfoUpdateCheck(request);

			// チェック成功の場合
			if (isCheckedOk){
				NoteEntity bean = setBeanValue(request);
				 int updateResult = 0;
				 String msgOpea = "";
				// 新規ノート情報
				if (APConst.MODE_ADD.equals(mode)){
					// 新規ノート情報
					updateResult = insertNote(bean);
					msgOpea = APConst.INSERT_CHAR;
					// ファイルID
					errorJson.put(PARAM_FILE_ID, fileID);
					// ノートID
					errorJson.put(PARAM_NOTE_ID, updateResult);

				}else{
					 // 更新ノート情報
					updateResult = updateNote(bean);
					msgOpea = APConst.UPDATE_CHAR;
				}
				// 変更失敗の場合、エラーメッセージを表示する
				if(updateResult <= 0){
					errorJson.put( APMsgConst.WARN
								 , MessageUtil.getMessageString(APMsgConst.E_COM_01
										 					   ,APMsgParamConst.MSG_PARAM_NOTE_TITLE
										 					   ,msgOpea)
						         + APConst.HTML_NEW_LINE);
				}
			}
		} catch (SQLException e) {
			throw e;
		} catch (JSONException ex) {
			throw ex;
		}
	}

	/***
	 * ノート追加・変更画面のチェック
	 * @param request HttpServletRequest
	 * @return チェック成功 true
	 * 		         チェック失敗 false
	 * @throws JSONException
	 * @throws SQLException
	 * @throws UnsupportedEncodingException
	 */
	private boolean doNoteEditInfoUpdateCheck(HttpServletRequest request) throws JSONException,SQLException, UnsupportedEncodingException {
		// チェック成功フラグ
		boolean isCheckedOk = true;
		// エラーメッセージ
		StringBuffer errorMsgBuf = new StringBuffer();
		try{
			// ラインFrom
			lineFrom = (String)request.getParameter(PARAM_LINE_FROM);
			// ラインTo
			lineTo = (String)request.getParameter(PARAM_LINE_TO);
			// 本文
			contents =(String)request.getParameter(PARAM_CONTENTS);
			contents = contents.trim();
			// プロジェクト識別子名
			project = (String)request.getParameter(PARAM_PROJECT_NAME);
			project = project.trim();
			// URL エンコードしたファイルのパス名
			path = (String)request.getParameter(PARAM_PATH);
			path = path.trim();
			path = URLEncoder.encode(path, APConst.ENCODE_UTF_8);
			// ラインFromには正整数しか入力できません。
			if(!StringUtils.isEmpty(lineFrom) && !CheckUtil.isInteger(lineFrom)){
				errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
									APMsgParamConst.MSG_PARAM_LINEFROM_NAME,
									APMsgParamConst.MSG_PARAM_INTEGER_NAME));
				errorMsgBuf.append(APConst.HTML_NEW_LINE);
				isCheckedOk = false;
			}
			// ラインToには正整数しか入力できません。
			if(!StringUtils.isEmpty(lineTo) && !CheckUtil.isInteger(lineTo)){
				errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_COM_07,
						APMsgParamConst.MSG_PARAM_LINETO_NAME,
						APMsgParamConst.MSG_PARAM_INTEGER_NAME));
				errorMsgBuf.append(APConst.HTML_NEW_LINE);
				isCheckedOk = false;
			}
			// 本文は必須入力項目です。
			if (StringUtils.isEmpty(contents)) {
				errorMsgBuf.append(MessageUtil.getMessageString(APMsgConst.W_MEM_01,
									APMsgParamConst.MSG_PARAM_CONTENTS_NAME));
				errorMsgBuf.append(APConst.HTML_NEW_LINE);
				isCheckedOk = false;
			}

			// 入力した{0}は入力した{1}より大きくしなければなりません。
			if(CheckUtil.isIntegerCheck(lineFrom) && CheckUtil.isIntegerCheck(lineTo)){
				int lineFromInt = Integer.valueOf(lineFrom);
					int lineToInt = Integer.valueOf(lineTo);
					// 入力した{0}は入力した{1}より大きくしなければなりません。
					 if(lineFromInt > lineToInt){
							errorMsgBuf.append(
									MessageUtil.getMessageString(APMsgConst.W_MEM_09,
									APMsgParamConst.MSG_PARAM_LINETO_NAME,
									APMsgParamConst.MSG_PARAM_LINEFROM_NAME));
							errorMsgBuf.append(APConst.HTML_NEW_LINE);
							isCheckedOk = false;
					 }
			}

			// 最大ファイル行数を取得する。
			int sourceLineMax = getFileLines(project,path);

			// ラインFrom
			if(!StringUtils.isEmpty(lineFrom) && CheckUtil.isInteger(lineFrom)){
				int lineFromInteger = Integer.valueOf(lineFrom);

				// 入力したラインFromは最大ファイル行数{0}以下で入力してください。
				if(lineFromInteger > sourceLineMax ){
					 errorMsgBuf.append(
								MessageUtil.getMessageString(APMsgConst.W_MEM_13,
										APMsgParamConst.MSG_PARAM_LINE_FROM_CHAR,
											String.valueOf(sourceLineMax)));
					 errorMsgBuf.append(APConst.HTML_NEW_LINE);
					 isCheckedOk = false;
				 }
			}

			// ラインTo
			if(!StringUtils.isEmpty(lineTo) && CheckUtil.isInteger(lineTo)){
				int lineToInteger = Integer.valueOf(lineTo);
				// 入力したラインTo は最大ファイル行数{0}以下で入力してください。
				if(lineToInteger > sourceLineMax ){
					 errorMsgBuf.append(
								MessageUtil.getMessageString(APMsgConst.W_MEM_13,
											APMsgParamConst.MSG_PARAM_LINE_TO_CHAR,
											String.valueOf(sourceLineMax)));
					 errorMsgBuf.append(APConst.HTML_NEW_LINE);
					 isCheckedOk = false;
				 }
			}

			// エラーメッセージをセットする
			errorJson.put( APMsgConst.WARN, errorMsgBuf.toString());
		}catch (JSONException e) {
			throw e;
		} catch (SQLException ex) {
			throw ex;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
		return isCheckedOk;
	}

	/***
	 * ノート情報Beanの値をセットする。
	 * @param request HttpServletRequest
	 * @return ノート情報のBean
	 * @throws SQLException
	 */
	private NoteEntity setBeanValue(HttpServletRequest request) throws SQLException{
		NoteEntity bean = null;
		HttpSession session = null;
		try{
			session = request.getSession();
			int loginId = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ID);
			bean = new NoteEntity();
			// ラインFromテキストボックスが入力されていない場合、"-1"をラインFromにセットする。
			if(StringUtils.isEmpty(lineFrom)){
				bean.setLinefrom(-1);
			}else{
				bean.setLinefrom(Integer.valueOf(lineFrom));
			}
			// ラインToテキストボックスが入力されていない場合、"-1"をラインToにセットする。
			if(StringUtils.isEmpty(lineTo)){
				bean.setLineto(-1);
			}else{
				bean.setLineto(Integer.valueOf(lineTo));
			}
			// 本文をセットする。
			bean.setContents(contents);
			// 公開フラグ
			String publicFlag = (String)request.getParameter(PARAM_PUBLIC_FLAG);
			if (APConst.ON_CHAR.equals(publicFlag)){
				bean.setPublicFlag(true);
			}else{
				bean.setPublicFlag(false);
			}
			// 更新者
			bean.setMuserid(loginId);
			// モードを取得する
			mode = (String)request.getParameter(APConst.MODE);
			// 新規ノート情報モード
			if (APConst.MODE_ADD.equals(mode)){
				String fileIDStr = (String)request.getParameter(PARAM_FILE_ID);
				// ファイルID
				int fid = 0;
				if(StringUtils.isEmpty(fileIDStr)){
					// 新規ファイル情報
					fid = insertFile();

				}else{
					fid = Integer.valueOf(fileIDStr);
				}
				// ファイルID
				fileID  = fid;
				// ファイル識別番号
				bean.setFid(fid);
				// 作成者
				bean.setCuserid(loginId);
			}else{ // 更新ノート情報モード
				// ノートID
				String noteIDStr = (String)request.getParameter(PARAM_NOTE_ID);
				bean.setId(Integer.valueOf(noteIDStr));
			}
		} catch (SQLException e) {
			throw e;
		}
		return bean;
	}

	/***
	 * 新規ファイル情報
	 * @return ファイルID
	 */
	private int insertFile() throws SQLException{
		 NoteDao dao = null;
		 FileEntity fileEntity = null;
		 try {
			fileEntity = new FileEntity();
			dao = new NoteDaoImpl();
			// ファイルのパス
			fileEntity.setPath(path);
			// プロジェクト識別子
			fileEntity.setProject(project);
			// 新規ファイル情報
			return dao.insertFileInfo(fileEntity);
		 } catch (SQLException e) {
			throw e;
		 }
	}

	/***
	 * 更新ノート情報
	 * @param bean ノート情報のbean
	 * @return 更新ノート情報の件数
	 * @throws SQLException
	 */
	private int updateNote(NoteEntity bean)throws SQLException{
		 NoteDao dao = null;
			try {
				dao = new NoteDaoImpl();
				// 更新ノート情報の件数
				return dao.updateNoteInfo(bean);
			}catch (SQLException e) {
				throw e;
			}
	}

	/***
	 * 新規ノート情報
	 * @param bean
	 * @return	新規ノート情報ID
	 * @throws SQLException
	 */
	private int insertNote(NoteEntity bean)throws SQLException{
		 NoteDao dao = null;
			try {
				dao = new NoteDaoImpl();
				// 新規ノート情報ID
				return dao.insertNoteInfo(bean);
			}catch (SQLException e) {
				throw e;
			}
	}

	/***
	 * ファイルの行数の検索
	 * @param project プロジェクト識別子名
	 * @param path URL エンコードしたファイルのパス名
	 * @return ファイルの行数 int
	 */
	private int getFileLines(String project,String path)throws SQLException{
		 NoteDao dao = null;
			try {
				dao = new NoteDaoImpl();
				// ファイルの行数の検索
				return dao.getSourceLines(project, path);
			}catch (SQLException e) {
				throw e;
			}
	}

}
