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
package jp.co.sra.codedepot.admin.log;

import java.io.UnsupportedEncodingException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import jp.co.sra.codedepot.admin.context.ContextFactoryLoader;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.StringUtils;

public class APLogFormatter extends Formatter {

	@Override
	public String format(LogRecord rec) {
		String res = String.format(
				"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS [%2$s] [%3$s] [%4$s]: %5$s"
						+ APConst.LINE_SEPARATOR, rec.getMillis(),
						rec.getLevel(), getClientIP(),
				getUserName(), rec.getMessage());
		try {
			res = new String(res.getBytes(APConst.ENCODE_UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return res;

	}

	private String getClientIP() {
		String ip = StringUtils.EMPTY_STR;
		try {
			ip = (String) ContextFactoryLoader.getFactory().getRequestContext()
					.getAttribute(APConst.SESSION_CLIENT_IP);
			ip = StringUtils.nvl(ip);
		} catch (NullPointerException ne) {
		}
		return ip;
	}

	private String getUserName() {
		String userName = StringUtils.EMPTY_STR;
		try {
			userName = (String) ContextFactoryLoader.getFactory()
					.getRequestContext().getAttribute(
							APConst.SESSION_LOGIN_NAME);
			userName = StringUtils.nvl(userName);
		} catch (NullPointerException ne) {
		}
		return userName;
	}

}
