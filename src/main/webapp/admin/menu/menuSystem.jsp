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
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<td class="nav-text-page-name"><nobr><span style="font-size: 80%; padding-left: 1em;"></nobr></td>
<% if  ((APConst.ID_TOP.equals(request.getParameter("pageid"))) || (APConst.ID_LOGIN.equals(request.getParameter("pageid")))) { %>
<td class="nav-backgroud nav-text-menu-cur"><nobr>TOPページ</nobr></td>
<%} else { %>
<td class="nav-backgroud nav-text-menu"><nobr><a href="../menu/menu.jsp?pageid=top">TOPページ</a></nobr></td>
<%}%>
<% if (APConst.ID_ACCOUNT.equals(request.getParameter("pageid"))) { %>
<td class="nav-backgroud nav-text-menu-cur"><nobr>アカウント管理</nobr></td>
<%} else {%>
<td class="nav-backgroud nav-text-menu"><nobr><a href="../menu/menu.jsp?pageid=account">アカウント管理</a></nobr></td>
<%}%>
<% if (APConst.ID_PROJECT.equals(request.getParameter("pageid"))) { %>
<td class="nav-backgroud nav-text-menu-cur"><nobr>プロジェクト管理</nobr></td>
<%} else {%>
<td class="nav-backgroud nav-text-menu"><nobr><a href="../menu/menu.jsp?pageid=project">プロジェクト管理</a></nobr></td>
<%}%>
<% if (APConst.ID_BATCH.equals(request.getParameter("pageid"))) { %>
<td class="nav-backgroud nav-text-menu-cur"><nobr>ログ表示</nobr></td>
<%} else {%>
<td class="nav-backgroud nav-text-menu"><nobr><a href="../menu/menu.jsp?pageid=batch">ログ表示</a></nobr></td>
<%}%>

