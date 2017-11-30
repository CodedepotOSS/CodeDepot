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

import java.sql.SQLException;
import java.util.List;

import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

import com.ibatis.sqlmap.client.SqlMapClient;

public class ChangePageUtil {

	public static final String ITEMCOUNTS = "itemCounts";

	public static final String ITEMSPERPAGE = "itemsPerPage";

	public static final String PAGECOUNTS = "pageCounts";

	public static final String PAGE = "page";

	public static final String LISTS = "lists";

	private ChangePageBean _pageBean;

	public ChangePageUtil(ChangePageBean pageBean) {
		this._pageBean = pageBean;
	}

	public void getResult() throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int itemCounts = (Integer)sqlmap.queryForObject(_pageBean.getInCountSqlMap(), _pageBean.getInParamMap());

		int itemsPerPage = Integer.parseInt(APProperties.getProperty(APConst.ITEMS_PER_PAGE));
		int pageCounts =
			0 == itemCounts % itemsPerPage ?
				itemCounts / itemsPerPage :
				itemCounts / itemsPerPage + 1;
		if (_pageBean.getInPage() > pageCounts) {
			_pageBean.setOutPage(pageCounts);
		} else {
			_pageBean.setOutPage(_pageBean.getInPage());
		}

		_pageBean.getInParamMap().put("limit", itemsPerPage);
		int pages = (_pageBean.getOutPage() - 1) <= 0 ?0:(_pageBean.getOutPage() - 1);
		_pageBean.getInParamMap().put("offset", (pages) * itemsPerPage);

		List<?> list = (List<?>)sqlmap.queryForList(_pageBean.getInListSqlMap(), _pageBean.getInParamMap());

		_pageBean.setOutItemCounts(itemCounts);
		_pageBean.setOutItemsPerPage(itemsPerPage);
		_pageBean.setOutPageCounts(pageCounts);
		_pageBean.setOutList(list);
	}
}
