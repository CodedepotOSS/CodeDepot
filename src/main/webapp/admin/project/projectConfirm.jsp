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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.Date"%>
<%@page import="jp.co.sra.codedepot.admin.project.ProjectInfoBean"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.project.action.ProjLoadConfirmAction"%><html>
<head>
	<%@include file="../checkSession.jsp"%>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>プロジェクト追加・変更確認 - CodeDepot</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "project.css");</script>
	<script type="text/javascript" language="javascript">
		var submitFlg = false;
        $(function(){
			window.parent.$('#dialog').dialog('option', 'title', "プロジェクト追加・変更確認");
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
			var params = {};
			$("#form_confirm input,textarea").each(function(){
				var name = this.name;
				var value = this.value;
				if (name && 0 != name.length) {
					params[name] = value;
				}
			});
			var paramStr = $.param(params);
			$.ajax({
				type: "POST",
				dataType: "json",
				url: "projUpdate",
				data: paramStr,
				success: updateCallback,
				error: dealErrors
			});
		}
		function updateCallback(data, textStatus) {
			if (data.<%=APConst.PARAM_REDIRECTURL%>) {
				window.parent.$('#dialog').dialog('close');
				parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
				return;
			}
			var isSuccess = true;
			var ex = data.exception;
			if (null != ex) {
				dealErrors();
				return;
			}
			var warn = data.warn;
			if (warn) {
				$("#errMsg").val(warn);
				$("#isError").val("true");
				isSuccess = false;
			}
			var name = data.name;
			if (name) {
				$("#name").val(name);
			}
			var form = $("#form_confirm");
			if(!isSuccess){
				form.attr("action", "projectEdit.jsp");
			} else {
				form.attr("action", "projectDetail.jsp");
			}
			form.submit();
		}
		function doReturn() {
			if(submitFlg) {
				return;
			}
			submitFlg = true;
			var form = $("#form_confirm");
			form.attr("action", "projectEdit.jsp");
			form.submit();
		}
		function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
			var form = $("#form_confirm");
			form.attr("action", '<%=request.getContextPath()%>'+errorPage);
			form.submit();
		}
	</script>
	<base target="_self">
</head>
<%
	ProjLoadConfirmAction action = new ProjLoadConfirmAction();
	ProjectInfoBean infoBean = (ProjectInfoBean) action.doAction(request, response);
	if (null == infoBean) {
		return;
	}
%>
<body>
	<div  id="form_container">
		<form id="form_confirm" class="appnitro" method="post" action="">
			<input type="hidden" id="inputFlg" name="inputFlg" value="true"/>
			<input type="hidden" id="mode" name="mode" value="<%=infoBean.getMode() %>"/>
			<input type="hidden" id="name" name="name" value="<%=infoBean.getName() %>"/>
			<input type="hidden" id="scm_pass" name="scm_pass" value="<%=infoBean.getScm_pass() %>"/>
			<input type="hidden" id="scm_passConfirm" name="scm_passConfirm" value="<%=infoBean.getScm_passConfirm() %>"/>
			<input type="hidden" id="adminId" name="adminId" value="<%=infoBean.getAdminId() %>"/>
			<input type="hidden" id="permitUserIdStr" name="permitUserIdStr" value="<%=infoBean.getPermitUserIdStr() %>"/>
			<input type="hidden" id="errMsg" name="errMsg" value=""/>
			<input type="hidden" id="isError" name="isError" value=""/>
			<input type="hidden" id="titleOld" name="titleOld" value="<%=infoBean.getTitleOld() %>"/>
			<label class="description" for="title">プロジェクト名<br/><input name="title" type="text" class="text_readOnly medium" id="title" value="<%=infoBean.getTitle()%>" readonly="readonly"/></label>
			<label class="description" for="description">説明<br/><textarea id="description" name="description" class="textarea_readOnly medium" readonly="readonly"><%=infoBean.getDescription()%></textarea></label>
			<label class="description" for="license">ライセンス名<br/><input name="license" type="text" class="text_readOnly medium" id="license" value="<%=infoBean.getLicense()%>"  readonly="readonly"/></label>
			<label class="description" for="site_url">ホームページのURL<br/><input name="site_url" type="text" class="text_readOnly medium" id="site_url" value="<%=infoBean.getSite_url()%>" readonly="readonly"/></label>
			<label class="description" for="download_url">ダウンロードページのURL<br/><input name="download_url" type="text" class="text_readOnly medium" id="download_url" value="<%=infoBean.getDownload_url()%>" readonly="readonly"/></label>
			<label class="description" for="src_type">プロジェクトの参照方法<br/>
				<select class="select_readOnly medium" id="src_type" name="src_type" disabled="disabled">
					<option value="svn" <%="SVN".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Subversion</option>
					<option value="cvs" <%="CVS".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>CVS</option>
					<option value="git" <%="GIT".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Git</option>
					<option value="jazz" <%="JAZZ".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Jazz</option>
					<option value="local" <%="local".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>local</option>
				</select>
				<input type="hidden" id="src_type" name="src_type" value="<%=infoBean.getSrc_type() %>"/>
			</label>
			<label class="description" for="src_path">プロジェクトのパス名／URL<br/><input name="src_path" type="text" class="text_readOnly medium" id="src_path" value="<%=infoBean.getSrc_path()%>" readonly="readonly"/></label>
			<% if (! "local".equalsIgnoreCase(infoBean.getSrc_type())) { %>
			<label class="description" for="scm_user"> Subversion/CVS/Jazz のユーザ名<br/><input name="scm_user" type="text" class="text_readOnly medium" id="scm_user" value="<%=infoBean.getScm_user()%>" readonly="readonly"/></label>
			<% } %>
			<label class="description" for="crontab">検索インデックス自動更新時刻（秒 分 時 日 月 週を指定）<br/> <input name="crontab" type="text" class="text_readOnly medium" id="crontab" value="<%=infoBean.getCrontab()%>" readonly="readonly"/></label>
			<label class="description" for="ignores">検索対象から除外するファイル<br/><input name="ignores" type="text" class="text_readOnly medium" id="ignores" value="<%=infoBean.getIgnores()%>" readonly="readonly"/></label>
			<label class="description" for="adminName">プロジェクト管理者<br/><input name="adminName" type="text" class="text_readOnly medium" id="adminName" value="<%=infoBean.getAdminName()%>" size="15" readonly="readonly"/></label>
			<label class="description" for="restricted">アクセス制御<br/><input name="restricted" type="text" class="text_readOnly medium" id="restricted" value="<%=infoBean.getRestricted()%>" readonly="readonly"/></label>
			<label class="description" for="permitUserStr">アクセス許可ユーザ<br><textarea id="permitUserStr" name="permitUserStr" class="textarea_readOnly medium" readonly="readonly"><%=infoBean.getPermitUserStr() %></textarea></label>
			<label class="medium" for="label">上記のプロジェクト情報で登録しますか？<br><input type="button" class="BTN" onclick="doUpdate()" value="登録" /><input type="button" class="BTN" onclick="doReturn()" value="戻る" />
			</label>
			<p class="medium">アクセス許可ユーザアカウントの変更は即時有効になります。<br>その他の変更は検索インデックス更新処理の実行後に有効になります。<br>すぐに検索インデックス更新処理を行うには、プロジェクト管理で「即時実行」を行います。</p>
		</form>
	</div>
</body>
</html>
