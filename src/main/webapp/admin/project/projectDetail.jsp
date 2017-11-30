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

<%@page import="jp.co.sra.codedepot.admin.project.ProjectInfoBean"%>
<%@page import="jp.co.sra.codedepot.admin.project.action.ProjLoadDetailAction"%>
<%@page import="java.util.List"%>
<%@page import="jp.co.sra.codedepot.admin.util.MessageUtil"%>
<%@page import="jp.co.sra.codedepot.admin.util.APMsgConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@include file="../checkSession.jsp"%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>プロジェクト詳細 - CodeDepot</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "project.css");</script>
    <script type="text/javascript">
	var submitFlg = false;

	$(function(){
		window.parent.$('#dialog').dialog('option', 'title', "プロジェクト詳細");
	});

	$(document).keydown(function(event) {
		  if (event.keyCode == 13) {
		    $('form').each(function() {
		      event.preventDefault();
		    });
		  }
	});

	function doUpdate() {
		if(submitFlg) {
			return;
		}
		submitFlg = true;
		var form = $("#form_Info");
		form.attr("action", "projectEdit.jsp");
		form.submit();

		submitFlg = false;
	}

	function doDelete() {
		if(submitFlg) {
			return;
		}
		submitFlg = true;
		if (!confirm('<%= MessageUtil.getMessageString(APMsgConst.C_COM_01,APConst.PROJECT_INFO)%>')){
			submitFlg = false;
			return;
		} else {
			var param = {"name":$("#name").val()};
			$("#delBtn").val("削除中");
			$("#delBtn").css("color", "#a9a9a9");
			$.ajax({
				type: "POST",
				dataType: "json",
				url: "projDelete",
				data: param,
				success: delCallback,
				error: dealErrors
				});
		}
	}
	function delCallback(data, textStatus) {

		if (data.<%=APConst.PARAM_REDIRECTURL%>) {
			window.parent.$('#dialog').dialog('close');
			parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
		   	return;
		}
		var warn = data.warn;
		if (warn) {
			alert(warn);
			submitFlg = false;
			return;
		}
		doReturn();
	}
	function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
		var form = $("#form_Info");
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
	}

	function doReturn() {
		window.parent.$('#dialog').dialog('close');
	}
	</script>

    <base target="_self"/>
</head>
<%
	ProjLoadDetailAction action = new ProjLoadDetailAction();
	ProjectInfoBean infoBean = (ProjectInfoBean) action.doAction(request, response);
	if (!infoBean.isError()) {
	List<List<String>> srcInfoList = infoBean.getSrcStrList();

	String src_type = "local";
	if ("SVN".equalsIgnoreCase(infoBean.getSrc_type())) src_type = "Subversion";
	if ("CVS".equalsIgnoreCase(infoBean.getSrc_type())) src_type = "CVS";
	if ("GIT".equalsIgnoreCase(infoBean.getSrc_type())) src_type = "Git";
	if ("JAZZ".equalsIgnoreCase(infoBean.getSrc_type())) src_type = "Jazz";
%>
<body style="background-color: #ffffff;">
	<div id="form_container">
		<form id="form_Info" class="appnitro" method="post" action="">
			<input type="hidden" id="mode" name="mode" value="<%=infoBean.getMode()%>"/>
			<input type="hidden" id="name" name="name" value="<%=infoBean.getName() %>"/>
			<label for="title">プロジェクト名<br/><input name="title" type="text" class="text_readOnly medium" id="title" value="<%=infoBean.getTitle()%>" readonly="readonly"/></label>
			<label for="description">説明<br/><textarea id="description" name="description" class="textarea_readOnly medium" readonly="readonly"><%=infoBean.getDescription()%></textarea></label>
			<label for="license">ライセンス名<br /><input name="license" type="text" class="text_readOnly medium" id="license" value="<%=infoBean.getLicense()%>"  readonly="readonly"/></label>
			<label for="site_url">ホームページのURL<br /><input name="site_url" type="text" class="text_readOnly medium" id="site_url" value="<%=infoBean.getSite_url()%>" readonly="readonly"/></label>
			<label for="download_url">ダウンロードページのURL<br /><input name="download_url" type="text" class="text_readOnly medium" id="download_url" value="<%=infoBean.getDownload_url()%>" readonly="readonly"/></label>
			<label for="src_type">プロジェクトの参照先<br/><input name="src_type" type="text" class="text_readOnly medium" id="src_type" value="<%= src_type %>" readonly="readonly"/></label>
			<label for="src_path">プロジェクトのパス／URL<br /><input name="src_path" type="text" class="text_readOnly medium" id="src_path" value="<%=infoBean.getSrc_path()%>" readonly="readonly"/></label>
			<% if (! "local".equalsIgnoreCase(infoBean.getSrc_type())) { %>
			<label for="scm_user">Subversion/CVS/Jazz のユーザ名<br /><input name="scm_user" type="text" class="text_readOnly medium" id="scm_user" value="<%=infoBean.getScm_user()%>" readonly="readonly"/></label>
			<% } %>
			<label for="crontab">検索インデックス自動更新時刻（秒 分 時 日 月 週を指定）<br /><input name="crontab" type="text" class="text_readOnly medium" id="crontab" value="<%=infoBean.getCrontab()%>" readonly="readonly"/></label>
			<label for="ignores">検索対象から除外するファイル<br /><input name="ignores" type="text" class="text_readOnly medium" id="ignores" value="<%=infoBean.getIgnores()%>" readonly="readonly"/></label>
			<label for="adminName">プロジェクト管理者<br /><input name="adminName" type="text" class="text_readOnly medium" id="adminName" value="<%=infoBean.getAdminName()%>" size="15" readonly="readonly"/></label>
			<label for="restricted">アクセス制御<br/><input name="restricted" type="text" class="text_readOnly medium" id="restricted" value="<%=infoBean.getRestricted()%>" readonly="readonly"/></label>
			<label for="permitUserStr">アクセス許可ユーザ<br><textarea id="permitUserStr" name="permitUserStr" class="textarea_readOnly medium" readonly="readonly"><%=infoBean.getPermitUserStr()%></textarea></label>
			<label for="indexTime">検索インデックス最終更新時刻<br /><input name="indexed_at" type="text" class="text_readOnly medium" id="indexed_at" value="<%= infoBean.getUtime()%>" readonly="readonly"/></label>
			<%
			   int count =	srcInfoList.size();
			   if(count > 1) {
			%>
			<label for="srcInfo">検索対象
				<table class="table-search-result" style="margin:0px 0px 0px 0px;" id="srcInfo">
					<tr>
						<th width="13%" class="td-head" style="height: 16px;">言語</th>
						<th width="43%" class="td-head" style="height: 16px;">ファイル数</th>
						<th width="44%" class="td-head" style="height: 16px;">行数</th>
					</tr>
					<%
					   for (int i = 0; i < count - 1; i++) {
						List<String> record = srcInfoList.get(i);
					%>
					<tr>
						<td class="td-normal" style="text-align: center;"><%=record.get(0)%></td>
						<td class="td-normal"><div class="number"><%=record.get(1)%></div></td>
						<td class="td-normal"><div class="number"><%=record.get(2)%></div></td>
					</tr>
					<% } %>
					<tr>
						<td class="td-total" style="text-align: center;">Total</td>
						<td class="td-total"><div class="number"><%=srcInfoList.get(count - 1).get(1)%></div></td>
						<td class="td-total"><div class="number"><%=srcInfoList.get(count - 1).get(2)%></div></td>
					</tr>
				</table>
			</label>
			<% } %>
			<div>
				<input class="BTN" type="button" onclick="doUpdate()" value="編集" />
				<% try {Integer role = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ROLE); if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %>
				<input class="DEL BTN" type="button" id="delBtn" onclick="doDelete()" value="削除" />
				<% }} catch (Exception e) {}%>
				<input class="BTN" type="button" onclick="doReturn()" value="閉じる" />
			</div>
		</form>
	</div>
</body>
<% } else { %>
<script type="text/javascript">
alert('<%=infoBean.getErrMsg()%>');
window.parent.$('#dialog').dialog('close');
</script>
<% } %>
</html>
