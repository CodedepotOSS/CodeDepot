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
package jp.co.sra.codedepot.admin.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.MessageUtil;

public class BaseException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected Message msg;

	public BaseException() {
		msg = null;
	}

	public BaseException(Message message) {
		this(message, null);
	}

	public BaseException(Throwable cause) {
		this(((Message) (new Message("error.0001", getStackTraceStr(cause)))),
				cause);
	}

	public BaseException(Message message, Throwable cause) {
		super(MessageUtil.getMessageString(message), cause);
		msg = null;
		msg = message;
	}

	public Message getInternalMessage() {
		return msg;
	}

	public static String getStackTraceStr(Throwable e) {
		ByteArrayOutputStream baos;
		PrintWriter writer;
		if (e == null)
			return "";
		baos = new ByteArrayOutputStream();
		writer = null;
		try {
			writer = new PrintWriter(baos);
			e.printStackTrace(writer);
			writer.flush();
			return baos.toString();
		} catch (Throwable ex) {
			return "";
		} finally {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}


}
