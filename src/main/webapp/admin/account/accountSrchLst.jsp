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
<%@page import="jp.co.sra.codedepot.admin.account.servlet.AccountSearchServlet"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.DBConst"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../checkSession.jsp"%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css"></link>
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");</script>
	<script type="text/javascript">
		var showedWindowActionMode;
		var selectedUserName;
		var searchedUserName;
		var searchedPage;

		$(function(){
			$("#edDialog").dialog({
				autoOpen: false,
				modal: true,
				resizable: false,
				width: 600,
				height: 560,
				position:['middle', 20],
				target: "",
				open: function() {
					var iframe = '<iframe id="iframe" frameborder="0" marginheight="0" marginwidth="0"></iframe>';
					var target = $("#edDialog").dialog("option", "target");
					$("#edDialog").append(iframe);
					$("#iframe").attr({
						src: target,
						width: '100%',
						height: '100%'
					});
					return false;
				},
				close: function() {
					$("#iframe").remove();
					formSubmit(searchedPage);
				}
			});
			$("#rmDialog").dialog({
				autoOpen: false,
				modal: true,
				resizable: false,
				width: 600,
				height: 360,
				position:['middle', 20],
				title: "アカウント削除確認",
				target: "",
				open: function() {
					var iframe = '<iframe id="iframe" frameborder="0" marginheight="0" marginwidth="0"></iframe>';
					var target = $("#rmDialog").dialog("option", "target");
					$("#rmDialog").append(iframe);
					$("#iframe").attr({
						src: target,
						width: '100%',
						height: '100%'
					});
					return false;
				},
				close: function() {
					$("#iframe").remove();
					var result = $("#rmDialog").dialog("option", "result");
					if (result == 1) {
						doDelete();
					}
				}
			});
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

		function doSearch()
		{
			searchedUserName = $.trim($("#username").val());
			formSubmit(1);
		}

		function formSubmit(page)
		{
			searchedPage = page;
			$("#username").val(searchedUserName);
			var params = {"method": "getSrchLst", "page": page};
			if (0 != searchedUserName.length)
			{
				$.extend(params, {"username": searchedUserName});
			}
			var paramStr = $.param(params);
			$.ajax({
				type: "GET",
				dataType: "json",
				url: "<%=request.getContextPath()%>/admin/account/search",
				data: paramStr,
				success: dealSrchResults,
				error: dealErrors
				});
		}

		function dealSrchResults(data, textStatus)
		{
			if (data.<%=APConst.PARAM_REDIRECTURL%>) {
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
			if (null != warn)
			{
				try{document.getElementById("btnDel").style.display="none"}catch(e){};
				common_showMessageText(warn);
				return;
			}
			if (0 == eval(data.itemCounts))
			{
				try{document.getElementById("btnDel").style.display="none"}catch(e){};
				common_showMessageText(data.info);
				return;
			}
			try{document.getElementById("btnDel").style.display="block"}catch(e){};
			listTable.append(
		            "<tr>" +
		            "<td colspan='6' class='td-search-result-title-left'>" +
					"表示結果" +
		            "</td>" +
		            "<td class='td-search-result-title-right'>" +
		            "総計" +
		            formatNumber(data.itemCounts) +
		            "件</td>" +
		        	"</tr>" +
		        	"<tr>" +
		            "<th class='cbx'>" +
		            "<input id='chk' class='td-search-result-checkbox' type='checkbox' onclick='chkClick();'/>" +
		            "</th>" +
					"<th>項番</th>" +
		            "<th>アカウント名</th>" +
		            "<th>メールアドレス</th>" +
		            "<th>管理グループ</th>" +
		            "<th>ログイン</th>" +
		            "<th>管理者コメント</th>" +
		        	"</tr>");
			$.each(data.lists, function(i, n) {
				var bean = data.lists[i];
				listTable.append(
					"<tr>" +
					"<td class='cbx'>" +
					"<input id='chk_" + (i + 1) + "' name='chk' value=" + bean.username + " class='td-search-result-checkbox' type='checkbox' />" +
					"</td>" +
					"<td class='No'>" +
					(i + 1) +
					"</td>" +
		        	"<td class='account'>" +
		        		"<a href=\"javascript:showDetailInfo('" + bean.username + "');\">" +
	        				bean.username +
	        			"</a>" +
		        	"</td>" +
		        	"<td class='mailAddress'>" +
		        	bean.email +
		        	"</td>" +
		        	"<td class='role'>" +
		        	bean.role +
		        	"</td>" +
		        	"<td class='active'>" +
		        	bean.active +
		        	"</td>" +
		        	"<td class='comment'><div nowrap='true'>" +
		        	bean.note +
		        	"</div></td>" +
					"</tr>");
			});
			var pages = getPages(eval(data.page), eval(data.pageCounts), "formSubmit");
			$("#pages").append(pages);
		}

		function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
			var form = $("#form_search");
			form.attr("action", '<%=request.getContextPath()%>'+errorPage);
			form.submit();
		}

		function chkClick()
		{
			$(":checkbox").attr("checked", $("#chk").attr("checked"));
		}

		function askDelete()
		{
			if (0 == $(":checkbox[id!='chk'][checked=true]").length)
			{
				common_getMsg("W-MEM-06", "", function(data, textStatus){
					alert(data.msg);});
				return;
			}
                	var uri = '<%=request.getContextPath()%>/admin/account/accountDeleteConfirm.jsp';
                	$("#rmDialog").dialog("option", "target", uri).dialog("open");
		}

		function doDelete()
		{
			var arr = $(":checkbox[id!='chk'][checked='true']");
			var usernames = "";
			$.each(arr, function(i, n) {
				usernames += ",'" + n.value + "'";
			});
			usernames = usernames.substring(1, usernames.length);
			var params = {"method": "dltAccount", "usernames": usernames};
			var paramStr = $.param(params);
			$.ajax({
				type: "POST",
				dataType: "json",
				url: "<%=request.getContextPath()%>/admin/account/search",
				data: paramStr,
				success: dealDltResults,
				error: dealErrors
			});
		}

		function doAdd() {
			showedWindowActionMode = 'add';
			var accountEditUrl = "<%=AccountSearchServlet.URL_PAGE_ACCOUNT_EDIT %>";
                	$("#edDialog").dialog("option", "target", accountEditUrl).dialog("open");
		}

		function showDetailInfo(username) {

			showedWindowActionMode = "modify";
			selectedUserName = username;
			var accountDetailInfoUrl = "<%=AccountSearchServlet.URL_PAGE_ACCOUNT_DETAIL + "&" + APConst.PARAM_USERNAME + "=" %>" + username;
                	$("#edDialog").dialog("option", "target", accountDetailInfoUrl).dialog("open");
		}

		function dealDltResults(data, textStatus)
		{
			if (data.<%=APConst.PARAM_REDIRECTURL%>) {
				var form = $("#form_search");
				form.attr("action", "<%=request.getContextPath()%>");
				form.submit();
				return;
			}

			if (null != data.info)
			{
				$("#messagetext").text(data.info);
				return;
			}
			formSubmit(searchedPage);
		}
	</script>
	<base target="_self"/>
</head>
<body>
		<form id="form_search" class="search" enctype="multipart/form-data" method="post" action="">
			<table width="100%" class="search-conditions">
			    <tr>
			        <td width="60%">
			            <span class="font">&nbsp;&nbsp;アカウント名</span>
			            <input type="text" class="element inputName" id="username" maxlength="<%=DBConst.CHECK_LENGTH_USERNAME %>" />&nbsp;
			            <input type="button" class="BTN" onclick="doSearch();" value="表示" />
			        </td>
			    </tr>
			</table>
			<div class="div-search-result"><table class="table-search-result" id="listTable"></table></div>
			<div class="pageNum" id="pages"></div>
			<div class="wrongfont indent" id="messagetext"></div>
			<table class="btnTable indent">
	            <tr>
	                <td id="btnDel"><input type="button"  class="DEL BTN" onclick="askDelete();" value="削除" /></td>
	                <td><input type="button" class="BTN" onclick="doAdd();" value="追加" /></td>
	            </tr>
	        </table>
		</form>
		<div id="edDialog" style="display: none;"></div>
		<div id="rmDialog" style="display: none;"></div>
</body>
</body>
