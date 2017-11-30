<%--
 Copyright (c) 2009 SRA (Software Research Associates, Inc.)

 This file is part of CodeDepot.
 CodeDepot is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 3.0
 as published by the Free Software Foundation and appearing in
 the file GPL.txt included in the packaging of this file.

 CodeDepot is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>

<%@page import="java.net.HttpURLConnection"%>
<%@page import="jp.co.sra.codedepot.admin.scm.action.LogSearchAction"%>
<%@page import="jp.co.sra.codedepot.admin.scm.BatchLogBean"%>
<%@page import="java.util.List"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.BatchLogEntity"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "loglist.css");</script>
    <script type="text/javascript" src="../../js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="../../js/lib/jquery.bgiframe.js"></script>
    <script type="text/javascript" src="../../js/lib/jquery.autocomplete.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../../css/jquery-ui-1.7.1.redmond.css">
    <link rel="stylesheet" type="text/css" href="../../css/jquery.autocomplete.css" />
    <%@include file="../checkSession.jsp"%>
<script type="text/javascript">
var searchedStatus;
var searchedProjName;
var searchedPage;

$(function(){
    doSearch();
});

$(document).keydown(function(event) {
    if (event.keyCode == 13) {
        $('form').each(function() {
            event.preventDefault();
            doSearch();
        });
    }
});

function doSearch() {
	searchedStatus = $.trim($("#statusSelect").val());
	searchedTitle = $.trim($("#titleInput").val());
	formSubmit(1);
}

function formSubmit(page) {
	searchedPage = page;
	$("#statusSelect").val(searchedStatus);
	$("#titleInput").val(searchedTitle);
	var params = {"status": searchedStatus, "title": searchedTitle, "page": page};
	var paramStr = $.param(params);
	$.ajax({
		type: "GET",
		dataType: "json",
		url: "<%=request.getContextPath()%>/admin/scm/logSearch",
		data: paramStr,
		success: dealSrchResults,
		error: dealErrors
		});
}

function dealSrchResults(data, textStatus) {
	if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
			var form = $("#form_list");
			form.attr("action", "<%=request.getContextPath()%>");
			form.submit();
			return;
	 }
	common_showMessageText("");
	var listTable = $("#listTable");
	listTable.empty();
	$("#pages").empty();
	var warn = data.warn;
	if (warn) {
		common_showMessageText(warn);
		return;
	}
	if (0 == eval(data.itemCounts)) {
		common_showMessageText(data.info);
		return;
	}
	listTable.append(
            "<tr>" +
	            "<td colspan='6' class='td-search-result-title-left'>表示結果</td>" +
	            "<td class='td-search-result-title-right'>総計 " + formatNumber(data.itemCounts) + "件</td>" +
        	"</tr>" +
        	"<tr>" +
	            "<th width='3%'>項番</th>" +
	            "<th width='13%'>開始時刻</th>" +
	            "<th width='13%'>終了時刻</th>" +
	            "<th width='8%'>処理時間</th>" +
	            "<th width='6%'>終了状態</th>" +
	            "<th width='10%'>プロジェクト名</th>" +
	            "<th>メッセージ</th>" +
        	"</tr>");
	$.each(data.lists, function(i, n) {
		var bean = data.lists[i];
		listTable.append(
			"<tr>" +
				"<td class='td-right'>" + (i + 1) + "</td>" +
				"<td class='td-left'>"  + bean.stime + "</td>" +
				"<td class='td-left'>"  + bean.etime + "</td>" +
				"<td class='td-right'>" + formatNumber(bean.period) + " 秒 </td>" +
				"<td class='td-left'>"  + bean.status + "</td>" +
				"<td class='td-left'>"  + bean.title + "</td>" +
				"<td class='td-left'>"  + bean.msg + "</td>" +
			"</tr>");
	});
	var pages = getPages(eval(data.page), eval(data.pageCounts), "formSubmit");
	$("#pages").append(pages);
}

function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
	window.top.location.href = '<%=request.getContextPath()%>'+errorPage;
}
</script>
<base target="_self"/>
</head>
<body>
<%
    LogSearchAction action = new LogSearchAction();
    BatchLogBean bean = (BatchLogBean)action.doAction(request,response);
    if (null == bean) {
   		return;
    }
%>
<form id="form_list" class="search" method="post" action="">
	<table class="search-conditions">
	    <tr><td>
			<span class="font">終了状態</span>
			<select id="statusSelect" name="statusSelect">
				<option value=""></option>
				<option value="true">正常</option>
				<option value="false">異常</option>
			</select>
			<span class="font">プロジェクト名</span><input type="text" maxlength="80" id="titleInput" name="titleInput" class="element inputTitle"/></span><input type="button" class="BTN" onclick="doSearch();" value="表示" />
		</td></tr>
	</table>
	<div class="div-search-result"><table class="table-search-result" cellspacing="0" id="listTable"></table></div>
	<div class="pageNum" id="pages"></div>
	<div id="messagetext" class="wrongfont indent"><%=bean.isError()?bean.getErrMsg():"" %></div>
</form>
<SCRIPT type="text/javascript">
    var projectList = new Array(0);
    <% List<BatchLogEntity> projectList = bean.getBatchLogList(); %>
    <% for(BatchLogEntity entity : projectList){ %>
    projectList.push('<%=entity.getTitle() %>');
    <%} %>
    $("#titleInput").autocomplete(projectList);
</SCRIPT>
</body>
</html>
