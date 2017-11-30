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
<%@page import="jp.co.sra.codedepot.admin.project.action.ProjectEditAction"%>
<%@page import="jp.co.sra.codedepot.admin.project.ProjectInfoBean"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.AccountEntity"%>
<%@page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="java.util.Date"%><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@include file="../checkSession.jsp"%>
    <meta http-equiv="X-UA-Compatible" content="IE=7" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>プロジェクト追加・変更 - CodeDepot</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "project.css");</script>
    <script type="text/javascript">
	var submitFlg = false;
	$(function(){
		window.parent.$('#dialog').dialog('option', 'title', "プロジェクト追加・変更");
	});
	function doCheckInput(method) {
		if(submitFlg) {
			return;
		}
		submitFlg = true;
		var paramStr = {"method": method};
		$("#method").val(method);
		var mode = $("#mode").val();
		var title = $("#title").val();
		var name = $("#name").val();
		var titleOld = $("#titleOld").val();
		var adminName = $("#adminName").val();
		var description = $("#description").val();
		var license = $("#license").val();
		var site_url = $("#site_url").val();
		var download_url = $("#download_url").val();
		var src_type = $("#src_type").val();
		var src_path = $("#src_path").val();
		var scm_user = $("#scm_user").val();
		var scm_pass = $("#scm_pass").val();
		var scm_passConfirm = $("#scm_passConfirm").val();
		var crontab = $("#crontab").val();
		var restricted = $(':radio[name=restricted][checked]').val();
		var permitUserStr = $("#permitUserStr").val();
		if ("checkProject" == method) {
			$.extend(paramStr, {"mode": mode});
			$.extend(paramStr, {"title": title});
			$.extend(paramStr, {"titleOld": titleOld});
			$.extend(paramStr, {"name": name});
		}

		if ("checkManager" == method) {
			$.extend(paramStr, {"adminName": adminName});
		}

		if ("checkUser" == method) {
			$.extend(paramStr, {"permitUserStr": permitUserStr});
		}

		if ("scm_connect" == method) {
			$.extend(paramStr, {"src_type": src_type});
			$.extend(paramStr, {"src_path": src_path});
			$.extend(paramStr, {"scm_user": scm_user});
			$.extend(paramStr, {"scm_pass": scm_pass});
			$.extend(paramStr, {"scm_passConfirm": scm_passConfirm});
		}

		if ("doConfirm" == method) {
			$.extend(paramStr, {"mode": mode});
			$.extend(paramStr, {"title": title});
			$.extend(paramStr, {"titleOld": titleOld});
			$.extend(paramStr, {"name": name});
			$.extend(paramStr, {"adminName": adminName});
			$.extend(paramStr, {"permitUserStr": permitUserStr});
			$.extend(paramStr, {"description": description});
			$.extend(paramStr, {"restricted": restricted});
			$.extend(paramStr, {"license": license});
			$.extend(paramStr, {"site_url": site_url});
			$.extend(paramStr, {"download_url": download_url});
			$.extend(paramStr, {"src_type": src_type});
			$.extend(paramStr, {"src_path": src_path});
			$.extend(paramStr, {"scm_user": scm_user});
			$.extend(paramStr, {"scm_pass": scm_pass});
			$.extend(paramStr, {"scm_passConfirm": scm_passConfirm});
			$.extend(paramStr, {"crontab": crontab});
			common_showMessageText("");
		}
		var params = $.param(paramStr);
		$.ajax({
			type: "POST",
			dataType: "json",
			url: "projCheck",
			data: params,
			success: alertMsg,
			error: dealErrors
		});
	}

	function alertMsg(data, textStatus) {
		if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
			window.parent.$('#dialog').dialog('close');
			parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
			return;
		}
		submitFlg = false;
		var isSuccess = true;
		var method = $("#method").val();
		var warn = data.warn;
		var info = data.info;
		var error = data.error;
		var msg = "";
		if ("checkProject" == method) {
			if (null != warn) {
				alert(warn);
				return;
			}
			if (null != info) {
				alert(info);
				return;
			}
		}
		if ("checkManager" == method) {
			if (null != error) {
				alert(error);
				return;
			}
			if (null != info) {
				alert(info);
				return;
			}
			if (null != warn) {
				alert(warn);
				return;
			}
		}

		if ("checkUser" == method) {
			if (null != error) {
				alert(error);
				return;
			}
			if (null != info) {
				alert(info);
				return;
			}
			if (null != warn) {
				alert(warn);
				return;
			}
		}

		if ("scm_connect" == method) {
			if (null != error) {
				alert(error);
				return;
			}
			if (null != info) {
				alert(info);
				return;
			}
			if (null != warn) {
				alert(warn);
				return;
			}
			if (null != data.doConnect) {
				return;
			}
		}
		if ("doConfirm" == method) {
			if (null != warn) {
				msg = warn;
				if (null != error) {
					msg +="<br>";
				}
			}
			if (null != error) {
				msg += error;
			}
		}
		if ("" != msg) {
			common_showMessageText(msg);
			return;
		}
		if(isSuccess){
			var form = $("#form_edit");
			form.attr("action", "projectConfirm.jsp");
			var adminid = data.adminid;
			var permituserid = data.permituserid;
			var proName = data.proName;
			$("#adminId").val(adminid);
			$("#permitUserIdStr").val(permituserid);
			$("#name").val(proName);
			$("#src_path").val(data.srcpath);
			form.submit();
		}
	}

	function dealErrors(XMLHttpRequest, textStatus, errorThrown) {
		submitFlg = false;
		var form = $("#form_edit");
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
	}

	function doAccess(status) {
		if ("true" == status) {
			document.getElementById('permitUserStr').disabled = false;
			document.getElementById('accseeBtn').disabled = false;
		}
		if ("false" == status) {
			document.getElementById('permitUserStr').disabled = true;
			document.getElementById('accseeBtn').disabled = true;
		}
	}

	function doCancel() {
		if(submitFlg) {
			return;
		}
		submitFlg = true;
		var form = $("#form_edit");
		var mode = $("#mode").val();
		if ("add" == mode) {
			window.parent.$('#dialog').dialog('close');
		}
		if ("modify" == mode) {
			form.attr("action", "projectDetail.jsp");
			form.submit();
		}
	}

	function doReset() {
		if(submitFlg) {
			return;
		}
		submitFlg = true;
		var form = $("#form_edit");
		form.attr("action", "projectEdit.jsp");
		form.submit();
	}

	function selectSrcType(type) {
		type = $.trim(type).toLowerCase();
		if (type == "local") {
		    $(".scm_field").hide();
		    $(".path_info_local").show();
		    $(".path_info_svn").hide();
		    $(".path_info_cvs").hide();
		    $(".path_info_git").hide();
		    $(".path_info_jazz").hide();
		} else if (type == "svn") {
		    $(".scm_field").show();
		    $(".path_info_local").hide();
		    $(".path_info_svn").show();
		    $(".path_info_cvs").hide();
		    $(".path_info_git").hide();
		    $(".path_info_jazz").hide();
		} else if (type == "cvs") {
		    $(".scm_field").show();
		    $(".path_info_local").hide();
		    $(".path_info_svn").hide();
		    $(".path_info_cvs").show();
		    $(".path_info_git").hide();
		    $(".path_info_jazz").hide();
		} else if (type == "git") {
		    $(".scm_field").hide();
		    $(".path_info_local").hide();
		    $(".path_info_svn").hide();
		    $(".path_info_cvs").hide();
		    $(".path_info_git").show();
		    $(".path_info_jazz").hide();
		} else if (type == "jazz") {
		    $(".scm_field").show();
		    $(".path_info_local").hide();
		    $(".path_info_svn").hide();
		    $(".path_info_cvs").hide();
		    $(".path_info_git").hide();
		    $(".path_info_jazz").show();
		}
	}
	</script>
	<base target="_self">
</head>
<body id="dialog_body" style="background-color: #ffffff;">
<% ProjectEditAction action = new ProjectEditAction();
   ProjectInfoBean infoBean = (ProjectInfoBean) action.doAction(request, response);
   if (infoBean == null) return;
   Integer role = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ROLE);
%>
	<div id="form_container">
		<form id="form_edit" class="appnitro" method="post" action="">
			<input type="hidden" id="dialogType" value="modal"/>
			<input type="hidden" id="method" name="method" value=""/>
			<input type="hidden" id="mode" name="mode" value="<%=infoBean.getMode() %>"/>
			<input type="hidden" id="name" name="name" value="<%=infoBean.getName() %>"/>
			<input type="hidden" id="adminId" name="adminId" value="<%=infoBean.getAdminId() %>"/>
			<input type="hidden" id="permitUserIdStr" name="permitUserIdStr" value="<%=infoBean.getPermitUserIdStr() %>"/>
			<label for="title">プロジェクト名<font class="astar">(必須)</font><br/>
				<input type="text" name="title" class="element text medium" id="title" value="<%=infoBean.getTitle() %>" maxlength="80"/><input type="hidden" id="titleOld" name="titleOld" value="<%=infoBean.getTitle() %>"/><input type="button" class="CHECK BTN" onclick="doCheckInput('checkProject');" value="重複チェック" />
			</label>
			<fieldset class="medium">
				<legend>プロジェクトの概要</legend>
				<label for="description">説明<br/>
					<textarea id="description" name="description" class="element textarea medium" ><%=infoBean.getDescription()%></textarea>
				</label>
				<label for="license">ライセンス名<br/>
					<input id="license" name="license" type="text" class="element text medium" maxlength="80" value="<%=infoBean.getLicense()%>"/>
                </label>
                <label for="site_url">ホームページのURL<br/>
                    <input name="site_url" type="text" id="site_url" class="element text medium" maxlength="1024" value="<%=infoBean.getSite_url()%>" />
                </label>
                <label for="download_url">ダウンロードページのURL<br/>
                    <input name="download_url" type="text" class="element text medium" maxlength="1024" id="download_url" value="<%=infoBean.getDownload_url()%>" />
                </label>
			</fieldset>
			<fieldset class="medium">
				<legend>プロジェクトの参照先</legend>
				<label for="src_type">プロジェクトのアクセス方法<font class="astar">(必須)</font><br/>
					<% if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %>
				<select class="element select medium" id="src_type" name="src_type" onChange="selectSrcType(this.value);">
					<option value="local" <%="local".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>local</option>
					<option value="svn" <%="SVN".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Subversion</option>
					<option value="cvs" <%="CVS".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>CVS</option>
					<option value="git" <%="GIT".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Git</option>
					<option value="jazz" <%="JAZZ".equalsIgnoreCase(infoBean.getSrc_type()) ? "selected" : ""%>>Jazz</option>
				</select>
					<% } else { %>
				<input name="src_type" type="text" id="src_type" class="text_readOnly element text medium" maxlength="1024" value="<%=infoBean.getSrc_type()%>" readonly="readonly" />
					<% } %>
				</label>
				<label for="src_path">プロジェクトのパス／URL<font class="astar">(必須)</font><br/>
					<% if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %>
					<input name="src_path" type="text" id="src_path" class="element text medium" maxlength="1024" value="<%=infoBean.getSrc_path()%>" />
					<% } else { %>
					<input name="src_path" type="text" id="src_path" class="text_readOnly element text medium" maxlength="1024" value="<%=infoBean.getSrc_path()%>" readonly="readonly" />
					<% } %>
					<input type="button" class="CHECK BTN" onclick="doCheckInput('scm_connect');" value="参照チェック" />
					<div id="path_info">
						<div class="path_info_local">local では、ディレクトリの絶対パスを指定してます。</div>
						<div class="path_info_svn">Subversion では、 WebDAV プロトコルの URL を指定します。</div>
						<div class="path_info_cvs">CVS では、CVS リポジトリとモジュール名を空白で区切って指定します。</div>
						<div class="path_info_cvs">CVS リポジトリの指定には、pserver プロトコルが指定できます。</div>
						<div class="path_info_git">Git では、Git リポジトリとタグ名を空白で区切って指定できます。</div>
						<div class="path_info_git">Git リポジトリの指定には、http と git プロトコルを指定できます。</div>
						<div class="path_info_jazz">Jazz では、Jazz リポジトリとワークスペース名を空白で区切って指定します。</div>
						<div class="path_info_jazz">Jazz リポジトリの指定には、https プロトコルを指定できます。</div>
					</div>
				</label>
				<label for="scm_user">Subversion/CVS/Jazz のユーザ名<br/>
					<input name="scm_user" type="text" id="scm_user" class="element text medium" maxlength="40" value="<%=infoBean.getScm_user()%>" />
				</label>
				<label for="scm_pass">Subversion/CVS/Jazz のパスワード<br/>
					<input name="scm_pass" type="password" id="scm_pass" class="element text medium" maxlength="80" value="<%=infoBean.getScm_pass()%>" />
				</label>
				<label for="scm_passConfirm">Subversion/CVS/Jazz のパスワード再入力<br/>
					<input name="scm_passConfirm" type="password" id="scm_passConfirm" class="element text medium" maxlength="80" value="<%=infoBean.getScm_passConfirm()%>" />
				</label>
		  </fieldset>
			<label for="crontab">検索インデックスの自動更新時刻（秒 分 時 日 月 週を指定）<br/>
				<input name="crontab" type="text" class="element text medium" id="crontab" maxlength="80" value="<%=infoBean.getCrontab()%>"/>
				<div id="crontab_sample">
					<table>
						<tr><th rowspan="2">設定例</th><td><tt>00 30 05 * * ?</tt></td><td>毎朝5時30分に起動</td></tr>
						<tr><td><tt>00 00 18 ? * SUN</tt></td><td>毎週日曜日の18時に起動</td></tr>
					</table>
				</div>
			</label>
			<label for="ignores">検索対象から除外するファイル<br/>
				<input name="ignores" type="text" class="element text medium" id="ignores" maxlength="80" value="<%=infoBean.getIgnores()%>"/>
				<div id="ignores_sample">設定例 <tt>*.txt *.dat /test/*</tt></div>
			</label>
			<label for="adminName">プロジェクト管理者<br/>
				<% if (new Integer(APCodeBook.ROLE_LEVEL_SYSTEM_CODE).equals(role)) { %>
					<input name="adminName" type="text" id="adminName" class="element text medium" maxlength="80" value="<%=infoBean.getAdminName() %>" /><input type="button" class="CHECK BTN" onclick="doCheckInput('checkManager');" value="権限チェック" />
				<% } else if (new Integer(APCodeBook.ROLE_LEVEL_MANAGER_CODE).equals(role)) { %>
					<input name="adminName" type="text" id="adminName" class="text_readOnly medium" value="<%=infoBean.getAdminName() %>" readonly="readonly" />
					<input type="hidden" id="adminId" name="adminId" value="<%=infoBean.getAdminId() %>"/>
				<% } %>
			</label>
			<label id="restrictedLbl" for="restricted">アクセス制御<input type="radio" name="restricted" id="restrictedTrue" value="あり" onclick="doAccess('true');" <%=infoBean.isRestricted() ? "checked" : ""%>>あり</input><input type="radio" name="restricted" id="restrictedFalse" value="なし" onclick="doAccess('false');"<%=infoBean.isRestricted() ? "" : "checked"%>>なし</input></label>
			<label for="permitUserStr">アクセス許可ユーザ<br>
				<textarea id="permitUserStr" name="permitUserStr" class="element textarea medium" <%=infoBean.isRestricted()?"":"disabled" %>><%=infoBean.getPermitUserStr()%></textarea><input type="button" class="CHECK BTN" id="accseeBtn" <%=infoBean.isRestricted()?"":"disabled" %> onclick="doCheckInput('checkUser');" value="存在チェック" />
				<div id="permit_info">ユーザのアカウント名を、半角スペースまたは改行で区切って入力します。</div>
			</label>
            <div id="messagetext" class="wrongfont"><%=infoBean.isError()?infoBean.getErrMsg():"" %></div>
			<div><input type="button" class="BTN" onclick="doCheckInput('doConfirm');" value="確認"/><input type="button" class="BTN" onclick="doCancel();" value="中止" /><input type="button" class="BTN" onclick="doReset();" value="リセット" /></div>
		</form>
    </div>
    <script type="text/javascript">selectSrcType("<%= infoBean.getSrc_type() %>");</script>
</body>
</html>
