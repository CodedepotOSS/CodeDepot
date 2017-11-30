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
<%@ page language="java" contentType="text/html; charest=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="jp.co.sra.codedepot.admin.account.action.AccountConfirmAction"%>
<%@page import="jp.co.sra.codedepot.admin.account.servlet.AccountConfirmServlet"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.AccountEntity"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%
	AccountConfirmAction action = new AccountConfirmAction();
	AccountEntity ae = (AccountEntity) action.doAction(request, response);
	if (null == ae) {
		return;
	}
 %>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../checkSession.jsp"%>
<head>
    <title>アカウント追加・変更確認</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");</script>
	<script type="text/javascript" language="javascript">
	doReturn = function() {
		var form = $("#form_confirm");
		form.attr("action", "<%=AccountConfirmServlet.URL_PAGE_ACCOUNT_EDIT%>");
		form.submit();
	}
	doLogin = function() {
		var params = {};
		params["<%=APConst.MODE%>"] = "<%=ae.getMode() %>";
		$.ajax({
			type:"POST",
			dataType:"json",
			url:"acst",
			data:params,
			success:function(data, textStatus) {
				if (data.<%=APConst.PARAM_REDIRECTURL%>) {
					window.parent.$('#edDialog').dialog('close');
					parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
				   	return;
				}
				var status = data.<%=AccountConfirmServlet.JSON_KEY_STATUS %>;
				// チェックエラー
				if (status == "<%=AccountConfirmServlet.STATUS_CHECK_ERROR %>") {
					doReturn();
				// 更新エラー
				} else if (status == "<%=AccountConfirmServlet.STATUS_UPDATE_ERROR %>") {
					$("#messageString").html(data.warn);
				// 更新成功
				} else {
					// アカウント情報表示画面へ遷移する。
					var form = $("#form_confirm");
					form.attr("action", "<%=AccountConfirmServlet.URL_PAGE_ACCOUNT_DETAIL + "?" + APConst.PARAM_FROM_ID + "=" + APConst.PAGE_MODE_G_02_03 + "&" + APConst.PARAM_USERNAME + "=" %>" + data.username);
					form.submit();
				}
			},
			error:handleError
		});
	}

   function handleError(XMLHttpRequest, textStatus, errorThrown) {
		var form = $("#form_confirm");
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
   }
</script>
<base target="_self" />
</head>
<body>
	<div id="form_container">
		<form id="form_confirm" class="appnitro" method="post" action="#">
			<label class="description" for="username">アカウント名<br/><input id="username" type="text" class="element text_readOnly medium" value="<%=ae.getUsername()%>" size="15" readonly /></label>
			<label class="description" for="email">メールアドレス<br/><input id="email" type="text" class="element text_readOnly medium" value="<%=ae.getEmail() %>" size="15" readonly /></label>
			<label class="description" for="role">権限レベル<br/>
				<select id="role" class="element select_readOnly medium" disabled>
					<option <%=APCodeBook.ROLE_LEVEL_USER_CODE == ae.getRole() ? "selected" : "" %>>一般ユーザ</option>
					<option <%=APCodeBook.ROLE_LEVEL_MANAGER_CODE == ae.getRole() ? "selected" : "" %>>プロジェクト管理者</option>
					<option <%=APCodeBook.ROLE_LEVEL_SYSTEM_CODE == ae.getRole() ? "selected" : "" %>>システム管理者</option>
				</select>
			</label>
			<label class="description" for="active">ログイン<input id="active" type="text" class="element text_readOnly medium" value="<%=ae.getActive() ? "有効" : "無効" %>" size="15" readonly /></label>
			<label class="description" for="deflang">デフォルト検索言語<br/>
				<select id="deflang" class="element select_readOnly medium" disabled>
					<option <%=APCodeBook.LANG_JAVA_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>Java</option>
					<option <%=APCodeBook.LANG_C_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>C/C++</option>
					<option <%=APCodeBook.LANG_VBNET_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>VB.NET</option>
					<option <%=APCodeBook.LANG_CSHARP_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>C#</option>
				</select>
			</label>
			<label class="description" for="note">管理者コメント<br/><textarea id="note" cols="35" class="element textarea_readOnly medium" readonly><%=ae.getNote()%></textarea></label>
			<label class="medium" for="label">上記のアカウント情報で登録、よろしいですか？&nbsp;</label>
			<label class="wrongfont" for="messageString" id="messageString"></label>
			<div><input type="button" class="BTN" onclick="doLogin();" value="登録" /><input type="button" class="BTN" onclick="doReturn();" value="戻る" /></div>
		</form>
	</div>
</body>
</html>
