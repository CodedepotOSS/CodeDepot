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
<%@page import="jp.co.sra.codedepot.admin.account.action.AccountEditAction"%>
<%@page import="jp.co.sra.codedepot.admin.account.servlet.AccountEditServlet"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.AccountEntity"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.DBConst" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../checkSession.jsp"%>
<%
	AccountEditAction action = new AccountEditAction();
	AccountEntity ae = (AccountEntity) action.doAction(request, response);
	if (null == ae) {
		return;
	}
	if(!(APConst.PAGE_MODE_G_02_03.equals(ae.getFromId()))){
		request.getSession().setAttribute("oldAccountInfo", ae);
	}
	AccountEntity oAe = (AccountEntity) request.getSession().getAttribute("oldAccountInfo");
 %>
<head>
    <title>アカウント追加・変更</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/jsp/session.jsp"></script>
    <script type="text/javascript">loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");</script>
	<script type="text/javascript" language="javascript">
	$(function(){
		$(function(){
			window.parent.$('#edDialog').dialog('option', 'title', "アカウント追加・変更");
		});
		setLangOptions("#deflang");
	});
	doReset = function() {
		// アカウント検索・一覧画面から
		if ($("#mode").val() == '<%= APConst.MODE_ADD%>') {
			$("#username").val("");
			$("#password").val("");
			$("#rePassword").val("");
			$("#email").val("");
			$("#role").val("");
			$("#note").val("");
			$(":radio[name=active]")[0].checked = true;
			$("#role").attr("value", "<%=APCodeBook.ROLE_LEVEL_USER_CODE %>");
			$("#deflang").attr("value", "<%=APCodeBook.LANG_JAVA_CODE %>");
		} else {
			$("#password").val("");
			$("#rePassword").val("");
			document.getElementById("password").disabled = true;
			document.getElementById("rePassword").disabled = true;
			$("#email").val($("#oldEmail").val());
			$("#role").val("");
			$("#note").val($("#oldNote").val());
			$("#pwdChecked").attr("checked", false);
			var active;
			if ($("#oldActive").val()=='true') {
			    active = 0;
			}else{
			    active = 1;
			}
			$(":radio[name=active]")[active].checked = true;
			$("#role").attr("value", $("#oldRole").val());
			$("#deflang").attr("value", $("#oldDeflang").val());
		}
		$("#messageString").text("");
	}

	function doStop() {
	    var form = $("#form_edit");
		var mode = $("#mode").val();
		if ("add" == mode) {
			window.parent.$('#edDialog').dialog('close');
		}
		if ("modify" == mode) {
			form.attr("action", "accountDetailInfo.jsp");
			form.submit();
		}
	}

	doConfim = function() {
		var params = {};
		params["username"] = $("#username").val();
		var pwdChecked = $("#pwdChecked")[0];
		if (pwdChecked) {
			params["pwdChecked"] = pwdChecked.checked;
		}
		params["password"] = $("#password").val();
		params["rePassword"] = $("#rePassword").val();
		params["email"] = $("#email").val();
		params["role"] = $("#role").val();
		params["active"] = $(":radio[name=active]:checked").val() == "1" ? "true" : "false";
		params["deflang"] = $("#deflang").val();
		params["note"] = $("#note").val();
		params["<%=APConst.PARAM_METHOD%>"] = "<%=AccountEditServlet.METHDO_CLICK_CONFIRM %>";
		params["<%=APConst.MODE%>"] = "<%=ae.getMode() %>";

		$.ajax({
			type:"POST",
			dataType:"json",
			url:"aest",
			data:params,
			success:function(data, textStatus) {

				if (data.<%=APConst.PARAM_REDIRECTURL%>) {
					window.parent.$('#edDialog').dialog('close');
					parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
				   	return;
				}

				var status = data.<%=AccountEditServlet.JSON_KEY_STATUS %>;
				// チェックエラー
				if (status == "<%=AccountEditServlet.STATUS_CHECK_ERROR %>") {
					$("#messageString").html(data.warn);
				// チェック成功
				} else {
					var form = $("#form_edit");
					form.attr("action", "<%=AccountEditServlet.URL_PAGE_ACCOUNT_CONFIRM %>");
					form.submit();
				}
			},
			error:handleError
		});
	}

	doCheckBoxClick = function(control) {
		if (control.checked) {
			document.getElementById("password").disabled = false;
			document.getElementById("rePassword").disabled = false;
		} else {
			document.getElementById("password").disabled = true;
			document.getElementById("rePassword").disabled = true;
		}
	}

	doCheck = function() {
		var params = {};
		params["username"] = $("#username").val();
		params["<%=APConst.PARAM_METHOD%>"] = "<%=AccountEditServlet.METHDO_CHECK_DUPLICATE %>";
		params["<%=APConst.MODE%>"] = "<%=ae.getMode() %>";

		$.ajax({
			type:"POST",
			dataType:"json",
			url:"aest",
			data:params,
			success:function(data, textStatus) {

				if (data.<%=APConst.PARAM_REDIRECTURL%>) {
					window.parent.$('#edDialog').dialog('close');
					parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
				   	return;
				}

				var status = data.<%=AccountEditServlet.JSON_KEY_STATUS %>;
				// チェックエラー
				if (status == "<%=AccountEditServlet.STATUS_CHECK_ERROR %>") {
					// $("#messageString").html(data.warn);
					alert(data.warn);
				// チェック成功
				} else {
					// $("#messageString").html(data.info);
					alert(data.info);
				}
			},
			error:handleError
		});
	}

   handleError = function(XMLHttpRequest, textStatus, errorThrown) {
		var form = $("#form_edit");
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
   }
</script>
<base target="_self">
</head>
<body>
	<div id="form_container">
		<form id="form_edit" class="appnitro" method="post" action="#">
			<input type="hidden" id="mode" name="mode" value="<%=ae.getMode() %>"/>
			<label class="description" for="username">アカウント名<font class="astar">(必須)</font><br/>
			<% if(APConst.MODE_ADD.equals(ae.getMode())){ %>
			<input name="username" type="text" class="element text medium" id="username" value="<%=ae.getUsername()%>" maxlength="<%=DBConst.CHECK_LENGTH_USERNAME %>" />
			<% }else{ %>
			<input name="username" type="text" class="element text_readOnly medium" id="username" value="<%=ae.getUsername()%>" maxlength="<%=DBConst.CHECK_LENGTH_USERNAME %>" readonly/>
			<%}%>
			<input type="button" class="CHECK BTN" onclick="doCheck();" <%=APConst.MODE_ADD.equals(ae.getMode()) ? "" : "disabled" %> value="重複チェック" /></label>
			<%if (APConst.MODE_MODIFY.equals(ae.getMode())) {%>
			<fieldset id="modPassword" class="medium">
				<legend><label class="description" for="pwdChecked"><input type="checkbox" id="pwdChecked" onclick="doCheckBoxClick(this);" />パスワード変更<br/></label></legend>
				<label class="description" for="password">パスワード<br/><input type="password" class="element text medium" id="password" value="" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" disabled /></label>
				<label class="description" for="rePassword">パスワード再入力<br/><input type="password" class="element text medium" id="rePassword" value="" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" disabled /></label>
			</fieldset>
			<% } else { %>
			<label class="description" for="password">パスワード<span class="astar">(LDAP認証/外部ウェブ認証の場合は省略可能)</span><br/><input type="password" class="element text medium" id="password" value="" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" /></label>
			<label class="description" for="rePassword">パスワード再入力<br/><input type="password" class="element text medium" id="rePassword" value="" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" /></label>
			<% } %>
			<label class="description" for="email">メールアドレス<br/><input type="text" class="element text medium" id="email" value="<%=ae.getEmail()%>" maxlength="<%=DBConst.CHECK_LENGTH_EMAIL %>" /></label>
			<label class="description" for="role">権限レベル<span class="astar">(必須)</span><br/>
				<select class="element select medium" id="role">
					<% if(APConst.MODE_ADD.equals(ae.getMode()) && APConst.PAGE_MODE_G_02_01.equals(ae.getFromId())) { %>
					<option value="<%=APCodeBook.ROLE_LEVEL_USER_CODE %>" selected>一般ユーザ</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_MANAGER_CODE %>">プロジェクト管理者</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_SYSTEM_CODE %>">システム管理者</option>
					<% } else { %>
					<option value="<%=APCodeBook.ROLE_LEVEL_USER_CODE %>" <%=null != ae.getRole() && APCodeBook.ROLE_LEVEL_USER_CODE == ae.getRole().intValue() ? "selected" : "" %>>一般ユーザ</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_MANAGER_CODE %>" <%=null != ae.getRole() && APCodeBook.ROLE_LEVEL_MANAGER_CODE == ae.getRole().intValue() ? "selected" : "" %>>プロジェクト管理者</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_SYSTEM_CODE %>" <%=null != ae.getRole() && APCodeBook.ROLE_LEVEL_SYSTEM_CODE == ae.getRole().intValue() ? "selected" : "" %>>システム管理者</option>
					<% } %>
				</select>
			</label>
			<label class="description" for="active">ログイン<br/>
				<% if(APConst.MODE_ADD.equals(ae.getMode()) && APConst.PAGE_MODE_G_02_01.equals(ae.getFromId())) { %>
				<input type="radio" name="active" value="1" checked />有効
				<input type="radio" name="active" value="0" />無効
				<% } else { %>
				<input type="radio" name="active" value="1" <%=null != ae.getActive() && ae.getActive() ? "checked" : "" %> />有効
				<input type="radio" name="active" value="0"	<%=null != ae.getActive() && ae.getActive() ? "" : "checked" %> />無効
				<% } %>
			</label>
			<label class="description" for="deflang">デフォルト検索言語<br/>
				<select class="element select medium" id="deflang">
				<% if(APConst.MODE_ADD.equals(ae.getMode()) && APConst.PAGE_MODE_G_02_01.equals(ae.getFromId())) { %>
					<option value="<%=APCodeBook.LANG_JAVA_CODE %>" selected>Java</option>
					<option value="<%=APCodeBook.LANG_C_CODE %>">C/C++</option>
					<option value="<%=APCodeBook.LANG_VBNET_CODE %>">VB.NET</option>
					<option value="<%=APCodeBook.LANG_CSHARP_CODE %>">C#</option>
				<% } else { %>
					<option value="<%=APCodeBook.LANG_JAVA_CODE %>" <%=APCodeBook.LANG_JAVA_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>Java</option>
					<option value="<%=APCodeBook.LANG_C_CODE %>" <%=APCodeBook.LANG_C_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>C/C++</option>
					<option value="<%=APCodeBook.LANG_VBNET_CODE %>" <%=APCodeBook.LANG_VBNET_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>VB.NET</option>
					<option value="<%=APCodeBook.LANG_CSHARP_CODE %>" <%=APCodeBook.LANG_CSHARP_CODE.equals(ae.getDeflang()) ? "selected" : "" %>>C#</option>
				<% } %>
				</select>
			</label>
			<label class="description" for="note">管理者コメント<br/><textarea id="note" cols="35" class="element textarea medium" maxlength="<%=DBConst.CHECK_LENGTH_NOTE %>"><%=ae.getNote()%></textarea></label>
			<label class="wrongfont" for="messageString" id="messageString"><%=ae.getMessageString() %></label>
			<div><input type="button" class="BTN" onclick="doConfim();" value="確認" /><input type="button" class="BTN" onclick="doStop();" value="中止" /><input type="button" class="BTN" onclick="doReset();" value="リセット" /></div>
			<input type="hidden" id="oldEmail" value="<%=oAe.getEmail()%>"/>
			<input type="hidden" id="oldRole" value="<%=oAe.getRole()%>"/>
			<input type="hidden" id="oldActive" value="<%=oAe.getActive()%>"/>
			<input type="hidden" id="oldNote" value="<%=oAe.getNote()%>"/>
			<input type="hidden" id="oldDeflang" value="<%=oAe.getDeflang()%>"/>
			<input type="hidden" id="mode" name="mode" value="<%=oAe.getMode() %>"/>
		</form>
	</div>
</body>
</html>
