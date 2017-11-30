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
package jp.co.sra.codedepot.solr;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CsvServletResponse extends HttpServletResponseWrapper {
	private ByteArrayOutputStream stream;
	private PrintWriter writer;
	private int status;

	CsvServletResponse (HttpServletResponse response) {
		super(response);
		this.stream = new ByteArrayOutputStream();
		this.writer = new PrintWriter(this.stream);
		this.status = 200;
	}

	public String getContent() {
		this.writer.flush();
		return this.stream.toString();
	}

	public PrintWriter getWriter() {
		return this.writer;
	}

	public void setStatus(int status) {
		super.setStatus(status);
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}

	public void reset() {
		this.stream.reset();
	}
}
