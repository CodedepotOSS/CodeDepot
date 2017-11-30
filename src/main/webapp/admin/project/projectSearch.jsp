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
<%@page import="jp.co.sra.codedepot.admin.util.DBConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="jp.co.sra.codedepot.admin.project.servlet.ProjSearchServlet"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../checkSession.jsp"%>
<%
  String pageNo = request.getParameter("page");
  if (pageNo == null || pageNo.isEmpty()) {
    pageNo = "1";
  }
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "project.css");</script>
<script type="text/javascript">
	var showedWindowActionMode;
	var selectedProjName;
	var searchedPage = <%= pageNo %>;
	var submitFlg = false;
	var interval = 5000;

	$(function(){
		$("#dialog").dialog({
			autoOpen: false,
			modal: true,
                        resizable: false,
                        width: 600,
                        height: 800,
                        position:['middle', 20],
			target: "",
                        open: function() {
                                var iframe = '<iframe id="iframe" frameborder="0" marginheight="0" marginwidth="0"></iframe>';
				var target = $("#dialog").dialog("option", "target");
                                $("#dialog").append(iframe);
                                $("#iframe").attr({
                                        src: target,
                                        width: '100%',
                                        height: '100%'
                                });
                                return false;
                        },
                        close: function() {
                                $("#iframe").remove();
				dealDltResults();
				submitFlg = false;
                        }
		});
		dealDltResults();
	});

	function doSearch() {
		if(submitFlg) return;
		search();
		submitFlg = true;
	}

	function search() {
		selectedProjName = $.trim($("#titlesrch").val());
		formSubmit(1);
	}

	function formSubmit(page) {
		searchedPage = page;
		$("#titlesrch").val(selectedProjName);
		var params = {"method": "getProjList", "page": page};
		if (0 != selectedProjName.length) {
			$.extend(params, {"title": selectedProjName});
		}
		var paramStr = $.param(params);
		$.ajax({
			type: "GET",
			dataType: "json",
			url: "<%=request.getContextPath()%>/admin/project/projlist",
			data: paramStr,
			success: dealProjResults,
			error: dealErrors
			});
	}

	function dealProjResults(data, textStatus) {
		submitFlg = false;
		if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
			var form = $("#form_search");
			form.attr("action", "<%=request.getContextPath()%>");
			form.submit();
			return;
		}
		common_showMessageText("");
		var listTable = $("#listTable");
		listTable.empty();
		$("#pages").empty();
		var warn = data.warn;
		if (null != warn) {
			common_showMessageText(warn);
			return;
		}
		if (0 == eval(data.itemCounts)) {
			common_showMessageText(data.info);
			return;
		}
		listTable.append (
	            "<tr><td colspan='6' class='td-search-result-title-left' >表示結果</td>" +
	            "<td class='td-search-result-title-right' >総計" + formatNumber(data.itemCounts) + "件</td></tr>" +
	        	"<tr><th><div nowrap='true'>項番</div></th>" +
	            "<th><div nowrap='true'>プロジェクト名</div></th>" +
	            "<th><div nowrap='true'>管理者</div></th>" +
	            "<th><div nowrap='true'>アクセス制御</div></th>" +
	            "<th><div nowrap='true'>説明</div></th>" +
	            "<th><div nowrap='true'>検索インデックス更新処理</div></th>" +
	            "<th><div nowrap='true'>検索インデックス更新時刻</div></th></tr>");

		var isRunnning = false;
		$.each(data.lists, function(i, n) {
			var bean = data.lists[i];

			var runRow = "";
			if (bean.isRunning > 1) {
				isRunnning = true;
				runRow = "<td id='" + bean.title + "' class='td-center pro-result-run'><span class='job_waiting'>実行待ち</span></td>";
			} else if (bean.isRunning > 0) {
				isRunnning = true;
				runRow = "<td id='" + bean.title + "' class='td-center pro-result-run'><span class='job_running'>実行中</span></td>";
			} else {
				runRow = "<td id='" + bean.title + "' class='td-center pro-result-run'><input type='button' class='BTN' onclick='doRun(\"" + bean.title + "\");' value='即時実行' /></td>";
			}
			listTable.append(
				"<tr><td class='td-right pro-result-tdno'>" + (i + 1) + "</td>" +
	        	"<td class='td-left pro-result-tdname'>" +
	        	"<a href=\"javascript:showDetailInfo('" + bean.name + "')\"; >" + bean.title + "</a></td>" +
	        	"<td class='td-left pro-result-tdheader'>" + bean.username + "</td>" +
	        	"<td class='td-center pro-result-tdrestrict'>" + bean.restricted + "</td>" +
	        	"<td class='td-left pro-result-tddescription'>" + bean.description + "</td>" +
	        	runRow +
	        	"<td class='td-center pro-result-tdtime'>" + bean.utime + "</div></td></tr>");
		});
		var pages = getPages(eval(data.page), eval(data.pageCounts), "formSubmit");
		$("#pages").append(pages);
		if (isRunnning) {
			setTimeout("dealDltResults()", interval);
		}
	}

	function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
		submitFlg = false;
		var form = $("#form_search");
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
	}

	/**
	 * プロジェクト名をクリック
	 */
	function showDetailInfo(name) {
		if(submitFlg) return;
		submitFlg = true;
		var uri = '<%=request.getContextPath()%>/admin/project/projectDetail.jsp?name=' + name;
		$("#dialog").dialog("option", "target", uri).dialog("open");
	}

	/**
	 * 追加ボタンをクリック
	 */
	function doAdd() {
		if(submitFlg) return;
		submitFlg = true;
		var uri = '<%=request.getContextPath()%>/admin/project/projectEdit.jsp?mode=add';
		$("#dialog").dialog("option", "target", uri).dialog("open");
	}

	/**
	 * 一覧画面に検索条件より、再検索して、検索結果を表示する。
	 */
	function dealDltResults() {
			selectedProjName = $.trim($("#titlesrch").val());
			formSubmit(searchedPage);
		}

	$(document).keydown(function(event) {
		  if (event.keyCode == 13) {
		    $('form').each(function() {
		      event.preventDefault();
		      doSearch();
		    });
		  }
	});

	function doRunAll() {
		if(submitFlg) return;
		submitFlg = true;

		if (!confirm('全ての検索インデックスを更新します。')){
                        submitFlg = false;
                        return;
		}


		var params = {};
		selectedProjName = $.trim($("#titlesrch").val());
		params["<%= APConst.PARAM_METHOD %>"] = "<%= ProjSearchServlet._METHOD_DORUN_ALL %>";
		$.ajax({
			type: "GET",
			dataType: "json",
			url: "<%=request.getContextPath()%>/admin/project/projlist",
			data: params,
			success:
				function(data, textStatus) {
					dealDltResults();
					submitFlg = false;
				},
			error:
				function(XMLHttpRequest, textStatus, errorThrown) {
					dealDltResults();
					submitFlg = false;
				}
		});
	}

	function doRun(title) {

		var params = {};
		params["<%= APConst.PARAM_METHOD %>"] = "<%= ProjSearchServlet._METHOD_DORUN %>";
		params["title"] = title;

		title = title.replace(/\./g, "\\.");
		$("#" + title)[0].innerHTML = "起動中";

		$.ajax({
			type: "GET",
			dataType: "json",
			url: "<%=request.getContextPath()%>/admin/project/projlist",
			data: params,
			success:
				function(data, textStatus) {
					if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
						var form = $("#form_search");
						form.attr("action", "<%=request.getContextPath()%>");
						form.submit();
						return;
					}

					if (data.status == '<%= ProjSearchServlet.STATUS_RUN_ERROR %>') {
						$("#" + title)[0].innerHTML = '<span class="job_abort">実行失敗</span>';
						alert(data.info);
					} else if (data.status == '<%= ProjSearchServlet.STATUS_RUN_WAIT %>') {
						$("#" + title)[0].innerHTML = '<span class="job_waiting">実行待ち</span>';
						setTimeout("dealDltResults()", interval / 2);
					} else if (data.status == '<%= ProjSearchServlet.STATUS_RUN_SUCCESS %>') {
						$("#" + title)[0].innerHTML = '<span class="job_running">実行中</span>';
						setTimeout("dealDltResults()", interval / 2);
					} else if (data.status == '<%= ProjSearchServlet.STATUS_RUN_BUSY %>') {
						$("#" + title)[0].innerHTML = '<span class="job_running">実行中</span>';
						setTimeout("dealDltResults()", interval / 2);
					}
				},
			error:dealErrors
		});
	}
</script>
<base target="_self"/>
</head>
<body>
<form id="form_search" class="search" enctype="multipart/form-data" method="post" action="">
	<table class="pro-srch-table">
	    <tr>
	        <td class="pro-srch-title"><span class="font">プロジェクト名</span><input type="text" class="element inputTitle" id="titlesrch" maxlength="<%=DBConst.TITLE_MAX_LEN %>"/><input type="button" class="BTN" onclick="doSearch();" value="表示" /></td>
	        <td class="pro-srch-title" align="right"><input type="button" class="BTN" onclick="doRunAll();" value="全ての検索インデックスの更新" /></td>
	    </tr>
	</table>
	<input type="hidden" id="method" name="method" value=""/>
	<div class="div-search-result" style="overflow-y:hidden;"><table class="table-search-result" id="listTable"></table></div>
	<div class="pageNum" id="pages"></div>
	<div id="messagetext" class="wrongfont indent"></div>
	<table class="btnTable indent">
        <tr>
            <td><%Integer role = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ROLE);	if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %><input type="button" class="BTN" onclick="doAdd();" value="追加" /><% } %></td>
        </tr>
    </table>
</form>
<div id="dialog"></div>
</body>
