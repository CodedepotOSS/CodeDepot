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
package jp.co.sra.codedepot.web.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDownload extends HttpServlet {

	// 2009.12.18 add start
	private static final long serialVersionUID = 1L;
	// 2009.12.18 add end

	// 2009.12.18 modify start
	// private static Logger logger =
	// Logger.getLogger(FileDownload.class.getName());
	private static final Logger logger = LoggerFactory.getLogger(FileDownload.class.getName());
	// 2009.12.18 modify end

	// 2009.12.18 add start
	/** パラメータ アカウント名 */
	public final String PARAM_PID = "pid";
	/** Redirect */
	public final String PARAM_FPATH = "fpath";
	/** パラメータ アカウント名 */
	public final String PARAM_TYPE = "type";
	static String htmlDirectory = "";
	static String codeDerectory = "";
	static String tempDerectory = "";
	static {
		java.util.Properties props = null;
		try {
			props = CommonUtil.getPropertiesContext(APConst.PROPERTY_INDEXER);
			if (null != props) {
				htmlDirectory = props.getProperty(APConst.PROP_KEY_HTML_DIRECTORY);
				codeDerectory = props.getProperty(APConst.PROP_KEY_CODE_DIRECTORY);
				tempDerectory = props.getProperty(APConst.PROP_KEY_TEMP_DIRECTORY);
			}
		} catch (IOException e) {
			// do nothing;
		}
	}

	// 2009.12.18 add end

	public void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException,
			IOException {
		doPost(request, res);
	}

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse res)
			throws ServletException, IOException {

		// 2009.12.18 add start
		String pid = request.getParameter(PARAM_PID);
		String fpath = request.getParameter(PARAM_FPATH);
		String type = request.getParameter(PARAM_TYPE);
		if (isNull(pid) || isNull(fpath) || isNull(type) || fpath.startsWith("../") || -1 != fpath.indexOf("/../")
				|| (!APConst.TYPE_HTML.equals(type) && !APConst.TYPE_SRC.equals(type))) {
			logger.debug("HTTPステータス 400 - BAD REQUEST: " + request.getQueryString());
			res.reset();
			res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "BAD REQUEST");
			return;
		}


		ProjectInfoEntity info = null;

		try {
			// 2009.12.18 add start
			// セッションからログインユーザのログインIDを取得する
			HttpSession session = request.getSession();
			Integer id = (Integer) session.getAttribute(APConst.SESSION_LOGIN_ID);
			// 取得できない場合、ファイルが存在しないとして、異常処理へ。
			if (null == id) {
				logger.debug("ログインしていない");
				throw new FileNotFoundException();
			}

			// セッション.アクセス権限情報のリスト
			List<String> permitList = (List) session
					.getAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST);
			// 取得したレコード数 = 0 の場合、ファイルが存在しないとして、異常処理へ。
			if (!CommonUtil.havePermitAccessCheck(pid, permitList)) {
				logger.debug("権限がありません。(pid = " + pid + "; userid = " + id);
				throw new FileNotFoundException();
			}

			ProjectDao dao = new ProjectDaoImpl();
			info = dao.getAllProjectInfo(pid);
			if (info == null) {
				logger.debug("プロジェクトがありません。(pid = " + pid + "; userid = " + id);
				throw new FileNotFoundException();
			}

		} catch (Exception e) {
			res.reset();
			res.sendError(HttpURLConnection.HTTP_NOT_FOUND, e.toString());
		}


		// 2009.12.18 add end
		// 2009.12.18 modify start
		// String fileName = request.getParameter("fname");
		String fileName = "";
		String fileNameForLog = "";
		File file = null;

		if (APConst.TYPE_HTML.equals(type)) {
			fileNameForLog = APConst.TYPE_HTML + APConst.PATH_LOCAL + pid + APConst.PATH_LOCAL
					+ fpath;
			fileName = htmlDirectory + APConst.PATH_LOCAL + pid + APConst.PATH_LOCAL + fpath
					+ APConst.SUFFIX_HTML;
		        file = new File(fileName);
		} else {
			fileNameForLog = APConst.TYPE_SRC + APConst.PATH_LOCAL + pid + APConst.PATH_LOCAL
					+ fpath;
			fileName = codeDerectory + APConst.PATH_LOCAL + pid + APConst.PATH_LOCAL + fpath;
			file = new File(fileName);
			if (!file.exists()) {
                String src_type = info.getSrc_type();
				if (!src_type.equals("local")) {
					fileName = tempDerectory + APConst.PATH_LOCAL + pid + APConst.PATH_LOCAL + fpath;
					file = new File(fileName);
                } else {
					String sourceDirectory = URLDecoder.decode(info.getSrc_path(), APConst.ENCODE_UTF_8);
					fileName = sourceDirectory + APConst.PATH_LOCAL + fpath;
					file = new File(fileName);
				}
			}
		}
		// 2009.12.18 modify end

		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		// 2009.12.29 delete start
		// String fname = file.getName();
		// boolean notDL = true;
		//
		// // notDL = !(fname.endsWith(".java") || fname.endsWith(".c") ||
		// // fname.endsWith(".cc"));
		//
		// if (notDL) {
		// res.reset();
		// res.sendError(HttpURLConnection.HTTP_NOT_AUTHORITATIVE,
		// "The download file  is illegal!!!");
		// }
		// 2009.12.29 delete end

		try {

			in = new BufferedInputStream(new FileInputStream(file));
			out = new BufferedOutputStream(res.getOutputStream());
			// 2009.12.18 add end

			// HTTP head-info
			res.setContentType("application/octet-stream");

			String agent = request.getHeader("User-Agent");
			String filename = URLEncoder.encode(file.getName(), "UTF-8");

			if (agent.indexOf("MSIE") != -1) {
				res.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");
			} else {
				res.setHeader("Content-disposition", "attachment; filename*=UTF-8''" + filename);
			}
			res.setContentLength((int) file.length());

			/*
			 * cache res.setHeader("Expires", "0");
			 * res.setHeader("Cache-Control",
			 * "private,must-revalidate, post-check=0,pre-check=0");
			 * res.setHeader("Pragma", "private");
			 */
			// 2009.12.18 delete start
			// in = new BufferedInputStream(new FileInputStream(file));
			// out = new BufferedOutputStream(res.getOutputStream());
			// 2009.12.18 delete end

			byte buf[] = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}

			// 2009.12.18 add start
			logger
					.info(fileNameForLog + " "
							+ StringUtils.formatNum(String.valueOf(file.length())));
			// 2009.12.18 add end
		} catch (Exception e) {
			// 2009.12.18 add start
			logger.info(fileNameForLog + " -1");
			// 2009.12.18 add end
			res.reset();
			res.sendError(HttpURLConnection.HTTP_NOT_FOUND, e.toString());
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		return;
	}

	// 2009.12.18 add start
	/***
	 * 文字列がnull或いは""かどうかを判定する
	 *
	 * @param 文字列
	 * @return 判定結果
	 */
	private boolean isNull(String str) {
		return (null == str || "".equals(str));
	}
	// 2009.12.18 add end
}

/* vim: set tabstop=4: */
