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
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "menu.css");</script>
</head>
<body>
<table class="top-table">
<%
	Integer role = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ROLE);
	if (!(new Integer(APCodeBook.ROLE_LEVEL_USER_CODE)).equals(role)) { %>
  <div id="top-help">
    <h1>機能一覧</h1>
    <dl>
<%	if ((new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE)).equals(role)) { %>
      <dt><a href="../menu/menu.jsp?pageid=account">アカウント管理</a></dt>
<%	} else { %>
      <dt>アカウント管理</dt>
<%	} %>
      <dd>アカウントの追加、登録済みアカウントの設定、アカウントの削除が行えます。</dd>
      <dt><a href="../menu/menu.jsp?pageid=project">プロジェクト管理</a></dt>
      <dd>プロジェクトの追加、登録済みプロジェクトの設定、プロジェクトの削除が行えます。</dd>
      <dt><a href="../menu/menu.jsp?pageid=batch">ログ表示</a></dt>
      <dd>検索インデックスの更新処理の実行履歴を参照できます。</dd>
    </dl>
  </div>
<%	} else { %>
<p class="top-color">管理権限がありません。</p>
<%	} %>
</table>
</body>
</html>
