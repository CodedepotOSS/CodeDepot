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

import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import jp.co.sra.codedepot.admin.util.StringUtils;

public class CsvServletFilter implements Filter {

	final static String CSV_CONTENT_TYPE = "application/octet-stream";
	final static String CSV_CHARSET = "Windows-31J";

	public void init (FilterConfig config) throws ServletException {
	}

	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		String param = request.getParameter("download");
		if (StringUtils.isEmpty(param)) {
			chain.doFilter(request, response);
			return;
		}

		/* ラッパの作成 */
		HttpServletResponse resp = (HttpServletResponse)response;
		CsvServletResponse wrapper = new CsvServletResponse(resp);

		/* フィルタの実行 */
		chain.doFilter(request, wrapper);

		/* 処理結果の書換え */
		if (wrapper.getStatus() == HttpServletResponse.SC_OK ) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			String date = format.format(new Date());
			String name = "result-" + date + ".csv";
			resp.setCharacterEncoding(CSV_CHARSET);
			resp.setContentType(CSV_CONTENT_TYPE);
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
			resp.setHeader("Content-Description", name);
		}
		PrintWriter out = response.getWriter();
		String result = wrapper.getContent();
		out.write(result);
		wrapper.reset();
	  	out.flush();
	  	out.close();
	}

	public void destroy () {
	}
}
