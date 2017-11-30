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
<%@page import="jp.co.sra.codedepot.admin.account.action.AccountDetailAction"%>
<%@page import="jp.co.sra.codedepot.admin.base.BaseAction"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.AccountEntity"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="java.text.SimpleDateFormat"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<%@include file="../checkSession.jsp"%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>アカウント詳細情報</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");</script>
    <base target="_self" />
    <script type="text/javascript">
        $(function(){
			window.parent.$('#edDialog').dialog('option', 'title', "アカウント詳細情報");
        });
    	function form_sumbit(){
    		var form = $("#form_detail");
			form.attr("action", "../account/accountEdit.jsp?" + "<%=APConst.PARAM_FROM_ID%>" + "=" + "<%=APConst.PAGE_MODE_G_02_06%>");
			form.submit();
        }
	function doClose() {
		window.parent.$('#edDialog').dialog('close');
		return false;
	}
    </script>
</head>

<%
	String fromId = (String)request.getParameter(APConst.PARAM_FROM_ID);
	AccountEntity bean = null;
	// 遷移元はアカウント検索・一覧画面或いはアカウント追加・変更確認画面の場合
	if(APConst.PAGE_MODE_G_02_01.equals(fromId) || APConst.PAGE_MODE_G_02_03.equals(fromId)){
		BaseAction action = new AccountDetailAction();
		bean = (AccountEntity) action.doAction(request, response);
	}else{ // アカウント追加・変更画面
		bean = (AccountEntity)session.getAttribute(APConst.SESSION_ACCOUNT_INFO);
	}

	if (null == bean) return;

	String pwdtime = "";
	if( null != bean.getPwdmtime() && !"".equals(bean.getPwdmtime())){
		SimpleDateFormat  format = new  SimpleDateFormat(APConst.DATE_FORMAT_YYMMDDHHMMSS);
		pwdtime = format.format(bean.getPwdmtime());
	}
	String noteStr = "";
	if(null != bean.getNote()){
		noteStr = bean.getNote();
	}
%>
<body>
    <div id="form_container">
		<form id="form_detail" class="appnitro" enctype="multipart/form-data" method="post" action="">
			<label class="description" for="username">アカウント名<br/><input name="username" type="text" class="element text_readOnly medium" id="username" value="<%=bean.getUsername()%>" class="accountEdit_input" readonly="readonly"/><br/></label>
			<label class="description" for="email">メールアドレス<br/><input name="email" type="text" class="element text_readOnly medium" id="email" value="<%=bean.getEmail()%>" class="accountEdit_input"readonly="readonly"/><br/></label>
			<label class="description" for="role">権限レベル<br/>
				<select class="element select_readOnly medium" id="role" name="role" disabled="disabled" class="accountEdit_input">
					<option value=""></option>
					<option value="<%=APCodeBook.ROLE_LEVEL_USER_CODE %>"
					<%=null != bean.getRole() && APCodeBook.ROLE_LEVEL_USER_CODE == bean.getRole().intValue() ? "selected" : "" %>>一般ユーザ</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_MANAGER_CODE %>"
					<%=null != bean.getRole() && APCodeBook.ROLE_LEVEL_MANAGER_CODE == bean.getRole().intValue() ? "selected" : "" %>>プロジェクト管理者</option>
					<option value="<%=APCodeBook.ROLE_LEVEL_SYSTEM_CODE %>"
					<%=null != bean.getRole() && APCodeBook.ROLE_LEVEL_SYSTEM_CODE == bean.getRole().intValue() ? "selected" : "" %>>システム管理者</option>
				</select>
			</label>
			<label class="description" for="active">ログイン<br/>
				<% if(bean.getActive()){ %>
				<input name="active" type="text" id="active" value="有効" class="element text_readOnly medium" readonly="readonly"/><br/>
				<% }else { %>
				<input name="active" type="text" id="active" value="無効" class="element text_readOnly medium" readonly="readonly"/><br/>
				<% } %>
			</label>
			<label class="description" for="deflang">デフォルト検索言語<br/>
				<select class="element select_readOnly medium" id="deflang" name="deflang" disabled="disabled" class="accountEdit_input">
					<option value=""></option>
					<option value="<%=APCodeBook.LANG_JAVA_CODE %>" <%=APCodeBook.LANG_JAVA_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>Java</option>
					<option value="<%=APCodeBook.LANG_C_CODE %>" <%=APCodeBook.LANG_C_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>C/C++</option>
					<option value="<%=APCodeBook.LANG_C_CODE %>" <%=APCodeBook.LANG_C_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>C/C++</option>
					<option value="<%=APCodeBook.LANG_VBNET_CODE %>" <%=APCodeBook.LANG_VBNET_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>VB.NET</option>
					<option value="<%=APCodeBook.LANG_CSHARP_CODE %>" <%=APCodeBook.LANG_CSHARP_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>C#</option>
				</select>
			</label>
			<label class="description" for="pwd_mtime">パスワード変更時間<br/><input name="pwd_mtime" type="text" value="<%=pwdtime%>" class="element text_readOnly medium" id="pwd_mtime" class="accountEdit_input" readonly="readonly"/><br/></label>
			<label class="description" for="note">管理者コメント<br/><textarea  class="element textarea_readOnly medium" id="note" name="note" readonly="readonly" class="accountEdit_input"><%=noteStr%></textarea></label>
			<div><input type="button" class="BTN" value="編集" onclick="form_sumbit();" /></div>
		</form>
	</div>
</body>
</html>
