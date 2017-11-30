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
package jp.co.sra.codedepot.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalAddressFilter implements Filter {
	/* instance variables */

	private String[] localAddress = { "127.0.0.1", "::1" };

	/* public methods */

	public void init(FilterConfig config) throws ServletException {
		String param = config.getInitParameter("localAddress");
		if (param != null) {
			localAddress = param.split(",");
			for (int i = 0; i < localAddress.length; i++) {
				localAddress[i] = localAddress[i].trim();
			}
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		final String remote = httpRequest.getRemoteAddr();
		if (checkAddress(remote)) {
			chain.doFilter(request, response);
		} else {
			httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	public void destroy() {
		localAddress = null;
	}

	/* private methods */

	private boolean checkAddress (String remote) {
		if (localAddress != null) {
			for (int i = 0; i < localAddress.length; i++) {
				String local = localAddress[i];
				if (remote.equals(local)) {
					return true;
				}
			}
		}
		return false;
	}
}

