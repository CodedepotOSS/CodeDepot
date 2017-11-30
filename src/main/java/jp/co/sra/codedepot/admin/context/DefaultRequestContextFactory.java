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
package jp.co.sra.codedepot.admin.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultRequestContextFactory
 *
 * @author sra
 *
 */
public class DefaultRequestContextFactory implements RequestContextFactory {
	private static Logger log = LoggerFactory
			.getLogger(DefaultRequestContextFactory.class);
	private ThreadLocal<RequestContext> contextThreadLocal = null;

	public DefaultRequestContextFactory() {
		contextThreadLocal = new ThreadLocal<RequestContext>();
	}

	@Override
	public RequestContext getRequestContext() {
		if (contextThreadLocal.get() == null)
			contextThreadLocal.set(create());
		return (RequestContext) contextThreadLocal.get();
	}

	protected void setRequestContext(RequestContext ctx) {
		contextThreadLocal.set(ctx);
	}

	/**
	 * IDを取得
	 *
	 * @return
	 */
	private String createGUID() {
		return String.valueOf(Thread.currentThread().getId());
	}

	@Override
	public RequestContext create() {
		DefaultRequestContext requestContext = new DefaultRequestContext();
		requestContext.setId(createGUID());
		setRequestContext(requestContext);
		log.debug("リクエストコンテキストを新規しました。（id=" + requestContext.getId() + "）");
		return requestContext;
	}

	@Override
	public void destroy() {
		DefaultRequestContext defaultRequestContext = (DefaultRequestContext) contextThreadLocal
				.get();
		if (null != defaultRequestContext) {
			log.debug("カレントスレッドにリクエストコンテキスト（" + defaultRequestContext.getId()
					+ "）を破棄します。");
			contextThreadLocal.set(null);
		} else
			log.debug("カレントスレッドにリクエストコンテキストが存在しません。");
	}
}
