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
package jp.co.sra.codedepot.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.db.dao.AccountDao;
import jp.co.sra.codedepot.admin.db.dao.AccountDaoImpl;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.solr.Indexer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * アクセス制限クラス
 * @author sra
 *
 */
public class AccessControlComponent extends SearchComponent {

	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(AccessControlComponent.class);

	@Override
	public String getDescription() {
		// do nothing
		return null;
	}

	@Override
	public String getSource() {
		// do nothing
		return null;
	}

	@Override
	public String getSourceId() {
		// do nothing
		return null;
	}

	@Override
	public String getVersion() {
		// do nothing
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepare(ResponseBuilder rb) throws IOException {

		// HttpServletRequestを取得する
		HttpServletRequest req = (HttpServletRequest)rb.req.getContext().get("HttpServletRequest");
		if (req == null) {
			return;
		}

		// HttpSessionを取得する
		HttpSession session = req.getSession();

		// セッションから、ログインユーザIDを取得する
		Integer id = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ID);

		// セッションから、前回DBからアクセス権限を取得した時刻を取得する
		Long permissionGetTimeL = (Long)session.getAttribute(APConst.SESSION_LOGIN_PERMITPRJTIME);

		if (null == id || null == permissionGetTimeL) {
			logger.error(MessageUtil.getMessageString(APMsgConst.E_COM_06));
			throw new IOException();
		}

		long now = System.currentTimeMillis();
		List<String> pidList = null;
		//間隔
		int duringTime = 5;
		try {
			duringTime = Integer.parseInt(APProperties
					.getProperty(APConst.ACCESS_DURING_TIME));
		} catch (Exception e) {
			duringTime = 5;
		}
		if ((null == permissionGetTimeL)
				|| (duringTime * 1000 * 60 < (now - permissionGetTimeL
						.longValue()))) {
			// 時間差>5分間の場合
			AccountDao dao = new AccountDaoImpl();
			try {
				pidList = dao.getPermitPrjInfo(id.intValue());
				session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST, pidList);
				session.setAttribute(APConst.SESSION_LOGIN_PERMITPRJTIME, now);
			} catch (SQLException sqle) {
				logger.error(MessageUtil.getMessageString(APMsgConst.E_COM_05));
				throw new IOException(sqle);
			}
		} else {
			// 時間差が5分間以内の場合
			pidList = (ArrayList<String>)session.getAttribute(APConst.SESSION_LOGIN_PERMITPRJLIST);
		}

		// クエリフィルターを作成する
		BooleanQuery filter = new BooleanQuery();

		// アクセス制限ないクエリ条件を追加する
		Query pub = new TermQuery(new Term(Indexer.PERMIT, Indexer.DEF_PERMISSION));
		filter.add(pub, BooleanClause.Occur.SHOULD);

		// アクセス制限あるクエリ条件を追加する
		if (null != pidList && 0 < pidList.size()) {
			BooleanQuery prv = new BooleanQuery();
			for (String s : pidList) {
				Term term = new Term(Indexer.PID, s);
				prv.add(new TermQuery(term), BooleanClause.Occur.SHOULD);
			}
			filter.add(prv, BooleanClause.Occur.SHOULD);
		}

		// 作成したクエリフィルターをResponseBuilderに設定する
		List<Query> filters = rb.getFilters();
        if (null == filters) {
        	filters = new ArrayList<Query>();
        	rb.setFilters(filters);
        }
		filters.add(filter);

	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		// 検索条件をログに出力する
		logger.info(getQueryString(rb));
	}

	/***
	 * 検索文字列を取得する
	 * @param ResponseBuilder
	 * @return 検索文字列
	 */
	public String getQueryString(ResponseBuilder rb) {
		// クエリから検索文字列を取得する
		Query query = rb.getQuery();
		StringBuilder sb = new StringBuilder();

		sb.append("\"");
		if (query == null) {
			//clone search has no query, only string
			String cloneqstr = rb.getQueryString();
			sb.append("Clone Search String: ");
			sb.append(cloneqstr);
		} else {
			sb.append(query.toString());
		}
		sb.append("\"");

		// クエリフィルターから検索文字列を取得する
		List<Query> filters = rb.getFilters();
		if (filters != null) {
			for (Query q : filters) {
				sb.append(",\"");
				sb.append(q.toString());
				sb.append("\"");
			}
		}

		return sb.toString();
	}
}
