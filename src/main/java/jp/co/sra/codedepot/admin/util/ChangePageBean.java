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

import java.util.List;
import java.util.Map;

public class ChangePageBean {

	private int inPage;

	private String inCountSqlMap;

	private String inListSqlMap;

	private Map<String, Object> inParamMap;

	private int outItemsPerPage;

	private int outItemCounts;

	private int outPageCounts;

	private int outPage;

	private List<?> outList;

	/**
	 * @return the inPage
	 */
	public int getInPage() {
		return inPage;
	}

	/**
	 * @param inPage the inPage to set
	 */
	public void setInPage(int inPage) {
		this.inPage = inPage;
	}

	/**
	 * @return the inCountSqlMap
	 */
	public String getInCountSqlMap() {
		return inCountSqlMap;
	}

	/**
	 * @param inCountSqlMap the inCountSqlMap to set
	 */
	public void setInCountSqlMap(String inCountSqlMap) {
		this.inCountSqlMap = inCountSqlMap;
	}

	/**
	 * @return the inListSqlMap
	 */
	public String getInListSqlMap() {
		return inListSqlMap;
	}

	/**
	 * @param inListSqlMap the inListSqlMap to set
	 */
	public void setInListSqlMap(String inListSqlMap) {
		this.inListSqlMap = inListSqlMap;
	}

	/**
	 * @return the inParamMap
	 */
	public Map<String, Object> getInParamMap() {
		return inParamMap;
	}

	/**
	 * @param inParamMap the inParamMap to set
	 */
	public void setInParamMap(Map<String, Object> inParamMap) {
		this.inParamMap = inParamMap;
	}

	/**
	 * @return the outItemsPerPage
	 */
	public int getOutItemsPerPage() {
		return outItemsPerPage;
	}

	/**
	 * @param outItemsPerPage the outItemsPerPage to set
	 */
	public void setOutItemsPerPage(int outItemsPerPage) {
		this.outItemsPerPage = outItemsPerPage;
	}

	/**
	 * @return the outItemCounts
	 */
	public int getOutItemCounts() {
		return outItemCounts;
	}

	/**
	 * @param outItemCounts the outItemCounts to set
	 */
	public void setOutItemCounts(int outItemCounts) {
		this.outItemCounts = outItemCounts;
	}

	/**
	 * @return the outPageCounts
	 */
	public int getOutPageCounts() {
		return outPageCounts;
	}

	/**
	 * @param outPageCounts the outPageCounts to set
	 */
	public void setOutPageCounts(int outPageCounts) {
		this.outPageCounts = outPageCounts;
	}

	/**
	 * @return the outPage
	 */
	public int getOutPage() {
		return outPage;
	}

	/**
	 * @param outPage the outPage to set
	 */
	public void setOutPage(int outPage) {
		this.outPage = outPage;
	}

	/**
	 * @return the outList
	 */
	public List<?> getOutList() {
		return outList;
	}

	/**
	 * @param outList the outList to set
	 */
	public void setOutList(List<?> outList) {
		this.outList = outList;
	}

}
