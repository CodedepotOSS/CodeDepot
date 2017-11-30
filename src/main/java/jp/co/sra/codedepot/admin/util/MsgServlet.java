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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseServlet;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

	public static final String METHOD_GETMSG = "getMsg";

	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/html; charset=utf-8");
		String method = request.getParameter("method");
		if (!StringUtils.isEmpty(method)) {
			if (METHOD_GETMSG.equals(method)) {
				String msgid = request.getParameter("msgid");
				String[] params = request.getParameterValues("msgparams");
				try {
					JSONObject json = new JSONObject();
					json.put("msg", MessageUtil.getMessageString(msgid, params));
					response.getWriter().print(json);
				} catch (Exception e) {
					logger.error("error");
					//TODO
				}
				return;
			}
		}
	}
}
