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
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@page import="jp.co.sra.codedepot.admin.util.DBConst" %>
<%@page import="jp.co.sra.codedepot.admin.util.APConst" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>ログイン - CodeDepot</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");
    $(function(){
		$("input").keypress(function(e){
			if (e.which == 13){
				var inputs = $("#loginForm").find("input");
				var idx = inputs.index(this);
				if (idx == inputs.length - 2){
					$("#loginForm").submit();
				}else{
					inputs[idx + 1].focus();
				}
				return false;
			}
		});
	});
    </script>
</head>
<%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);

String loginName = (String)request.getAttribute(APConst.PARAM_USERNAME);
if(null == loginName || "".equals(loginName)) {
	loginName = "";
}
%>

<body>
    <div id="form_container">
		<div class="loginContainer">
			<img class="logo" src="<%=request.getContextPath()%>/img/title-logo.gif" alt="logo" />
			<form id="loginForm" action="<%=request.getContextPath()%>/admin/account/login" method="post">
				<table>
					<tr>
						<td><label class="description" for="userName">ユーザー名</label></td>
						<td><input name="userName" type="text" value ="<%= loginName%>" class="element text medium" id="userName" maxlength="<%=DBConst.CHECK_LENGTH_USERNAME %>" autofocus /></td>
					</tr>
					<tr>
						<td class="loginTdPadding"><label class="description" for="userPwd" >パスワード</label></td>
						<td class="loginTdPadding"><input name="userPwd" type="password" class="element text medium" id="userPwd" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" /></td>
					</tr>
					<tr>
						<td></td><td><input type="button" value="ログイン" class="BTN" onclick="javascript:submit();"/></td>
					</tr>
				</table>
			</form>
			<div class="wrongfont">
				<%
					List<?> list = (List<?>)request.getAttribute("errorMsgList");
					if( null != list && list.size() > 0){
						for(Object msgItem :list){
							out.println(msgItem.toString() + "<br/>");
						}
					}
				%>
			</div>
		</div>
	</div>
</body>
</html>
