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
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="jp.co.sra.codedepot.admin.menu.MenuAction"%>
<%@page import="jp.co.sra.codedepot.admin.base.BaseBean"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@include file="../checkSession.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CodeDepot管理</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "menu.css");</script>
</head>
<base target="_self">
<body>
<%
   MenuAction action = new MenuAction(config);
   BaseBean baseBean = (BaseBean)action.doAction(request, response);
%>
<div id="form_container_srch">
<table class="menu_table">
 <tr>
    <td rowspan="2" class="menu-logo"><img src="../../img/logo.gif" class="img-logo" alt="logo"/></td>
    <td class="nav-title">CodeDepot 管理</td>
    <td colspan="3" class="menu-user">ユーザ <%=(String)request.getAttribute(APConst.SESSION_LOGIN_NAME)%>
    </td>
    <td class="menu-close"><a href='javascript:window.close()'>閉じる</a></td>
 </tr>
 <tr>
 	<%
	   Integer role = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ROLE);
	   if (new Integer(APCodeBook.ROLE_LEVEL_USER_CODE).equals(role)) {
 	%>
		<jsp:include   page="../menu/menuNormal.jsp"   flush="true">
			<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
		</jsp:include>
		<jsp:include   page="../menu/top.jsp"   flush="true">
			<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
		</jsp:include>
	<%	} else if (new Integer(APCodeBook.ROLE_LEVEL_MANAGER_CODE).equals(role)) {%>
		<jsp:include   page="../menu/menuManager.jsp"   flush="true">
			<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
		</jsp:include>
		<% if ((APConst.ID_TOP.equals(request.getParameter("pageid"))) || (APConst.ID_LOGIN.equals(request.getParameter("pageid")))) { %>
			<jsp:include   page="../menu/top.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } else if (APConst.ID_PROJECT.equals(request.getParameter("pageid"))) { %>
			<jsp:include   page="../project/projectSearch.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } else if (APConst.ID_BATCH.equals(request.getParameter("pageid"))) { %>
			<jsp:include   page="../batch/logSearch.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } %>
 	<% } else if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %>
		<jsp:include   page="../menu/menuSystem.jsp"   flush="true">
			<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
		</jsp:include>
		<% if ((APConst.ID_TOP.equals(request.getParameter("pageid"))) || (APConst.ID_LOGIN.equals(request.getParameter("pageid")))) { %>
			<jsp:include   page="../menu/top.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } else if (APConst.ID_ACCOUNT.equals(request.getParameter("pageid"))) { %>
			<jsp:include   page="../account/accountSrchLst.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } else if (APConst.ID_PROJECT.equals(request.getParameter("pageid"))) { %>
			<jsp:include   page="../project/projectSearch.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } else if (APConst.ID_BATCH.equals(request.getParameter("pageid"))) { %>
			<jsp:include   page="../batch/logSearch.jsp"   flush="true">
				<jsp:param   name="pageid"   value='<%=request.getParameter("pageid")%>'   />
			</jsp:include>
		<% } %>
 	<% } %>
</tr>
</table>
   </div>
</body>
</html>
