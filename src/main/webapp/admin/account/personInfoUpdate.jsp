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
<%@ page import="jp.co.sra.codedepot.admin.account.action.PersonInfoUpdateAction"%>
<%@ page import="jp.co.sra.codedepot.admin.util.APConst"%>
<%@page import="jp.co.sra.codedepot.admin.db.entity.AccountEntity"%>
<%@page import="jp.co.sra.codedepot.admin.util.APCodeBook"%>
<%@page import="jp.co.sra.codedepot.admin.util.DBConst" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<%@include file="../checkSession.jsp"%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>個人設定</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/jsp/session.jsp"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css", "account.css");</script>
    <base target="_self" />
    <%
		PersonInfoUpdateAction action = new PersonInfoUpdateAction();
    	AccountEntity bean = (AccountEntity)action.doAction(request,response);
		boolean updatePersonPwdFlag = action.getUpdatePersonPwdFlag();
		if (null == bean) return;
	%>
    <script type="text/javascript">

    var formID = "";
    $(function(){
		setLangOptions("#def_lang");
	  	if("<%=updatePersonPwdFlag%>" == "true"){
	  		$("#pwdUpdateUL").css("display","");
		}else{
			$("#pwdUpdateUL").css("display","none");
		}
	    $("#pwd_update_msg").css("display","none");
		$("#person_update_msg").css("display","none");

	});

 	function pwdBtnClick(){
 		formID = "PwdUpdateform";
 		$("#pwd_update_msg").css("display","none");
		var updatePwdFlag = true;
		$("#pwd_msg").text("");
		setPwdCompnentEnableTrue();
		var paramJsonData = {"oldPwd" : $("#oldPwd").val()
						,"newPwd": $("#newPwd").val()
						,"newPwdAgin" : $("#newPwdAgin").val()
						,"updatePwdFlag" : updatePwdFlag
				};

		$.ajax({
			 type:"POST"
			,dataType:"json"
			,url:"personInfoUpdate"
			,data:paramJsonData
			,success:pwdChangedCallBack
			,error:dealErrors
		});
	}

 	function pwdChangedCallBack(data, textStatus){
 	   if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
                        window.parent.$('#dialog').dialog('close');
                        parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
                        return;
 		}
 		setPwdCompnentEnableFalse();
 		var warn = data.warn;
 		if(null != warn && "" != warn ){
 			$("#pwd_msg").html(warn);
 		}else{
 			$("#pwd_update_msg").css("display","");
 		}

 		setPwdTextEmpty();
    }

   function personBtnClick(){
	    formID = "PersonUpdateform";
	    $("#person_update_msg").css("display","none");
	   	var updatePwdFlag = false;
	   	$("#person_msg").text("");
		setPersonCompnentEnableTrue();

		var paramJsonData = {"email":$("#email").val()
						,"def_lang":$("#def_lang").val()
						,"updatePwdFlag":updatePwdFlag
		};

		$.ajax({
			 type:"POST"
			,dataType:"json"
			,url:"personInfoUpdate"
			,data:paramJsonData
			,success:personChangedCallBack
			,error:dealErrors
		});
	}

   function personChangedCallBack(data, textStatus){
		if (null !=data.<%=APConst.PARAM_REDIRECTURL%>) {
                        window.parent.$('#dialog').dialog('close');
                        parent.location = data.<%=APConst.PARAM_REDIRECTURL%>;
                        return;
		}
	   setPersonCompnentEnableFalse();
	   var warn = data.warn;
	   if(null != warn  && "" != warn){
			$("#person_msg").html(warn);
	   }else{
		   $("#person_update_msg").css("display","");
	   }
	}

   function dealErrors(XMLHttpRequest, textStatus, errorThrown){
		var form = $("#" + formID);
		form.attr("action", '<%=request.getContextPath()%>'+errorPage);
		form.submit();
   }

	function setPwdCompnentEnableTrue(){
		$("#oldPwd").attr("disabled",true);
		$("#newPwd").attr("disabled",true);
		$("#newPwdAgin").attr("disabled",true);
		$("#pwdBtn").attr("disabled",true);
	}

	function setPwdTextEmpty(){
		$("#oldPwd").val("");
		$("#newPwd").val("");
		$("#newPwdAgin").val("");
	}

	function setPwdCompnentEnableFalse(){
		$("#oldPwd").attr("disabled",false);
		$("#newPwd").attr("disabled",false);
		$("#newPwdAgin").attr("disabled",false);
		$("#pwdBtn").attr("disabled",false);
	}

	function setPersonCompnentEnableTrue(){
		$("#email").attr("disabled",true);
		$("#def_lang").attr("disabled",true);
		$("#personBtn").attr("disabled",true);
	}

	function setPersonCompnentEnableFalse(){
		$("#email").attr("disabled",false);
		$("#def_lang").attr("disabled",false);
		$("#personBtn").attr("disabled",false);
	}

	function window_close(){
		window.parent.$('#dialog').dialog('close');
	}

	$(document).keydown(function(event) {
		if (event.keyCode == 13) {
			$('form').each(function() {
				event.preventDefault();
			});
		}
	});
 </script>
</head>
<body>
    <div id="form_container">
        <form id="PwdUpdateform" class="appnitro" method="post">
        	<div id="pwdUpdateUL">
				<div class="group">
					<label class="description" for="oldPwd">旧パスワード<font class="astar">(必須)</font><br/><input name="oldPwd" type="password" id="oldPwd" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" class="element text medium"/><br /></label>
					<label class="description" for="newPwd">新パスワード<font class="astar">(必須)</font><br/><input name="newPwd" type="password" id="newPwd" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" class="element text medium" /></label>
					<label class="description" for="newPwdAgin">新パスワード再入力<span class="astar">(必須)</span><br/><input name="newPwdAgin" type="password" id="newPwdAgin" maxlength="<%=DBConst.CHECK_LENGTH_PASSWORD %>" class="element text medium" /></label>
					<label class="wrongfont" id="pwd_msg" ></label>
					<div id="pwd_update_msg"><img class="imgwidthHeight" src="../images/ok.png" alt="ok" />パスワードを変更しました。</div>
					<input type="button" id="pwdBtn" class="BTN" onclick="pwdBtnClick();" value="パスワード変更" />
				</div>
			</div>
		</form>
		<form id="PersonUpdateform" class="appnitro" method="post">
			<div class="group">
				<label class="description" for="email">メールアドレス(<span class="email-notice">ノートのユーザ名の表示に使用します。</span>)<br/><input name="email" type="text" id="email" class="element text medium" value="<%=bean.getEmail()%>" maxlength="<%=DBConst.CHECK_LENGTH_EMAIL %>"/></label>
                <label class="description" for="def_lang">デフォルト検索言語<br/>
                <select id="def_lang" name="def_lang"  class="element select medium ">
					<option value="<%=APCodeBook.LANG_JAVA_CODE %>" <%=APCodeBook.LANG_JAVA_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>Java</option>
					<option value="<%=APCodeBook.LANG_C_CODE %>" <%=APCodeBook.LANG_C_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>C/C++</option>
					<option value="<%=APCodeBook.LANG_VBNET_CODE %>" <%=APCodeBook.LANG_VBNET_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>VB.NET</option>
					<option value="<%=APCodeBook.LANG_CSHARP_CODE %>" <%=APCodeBook.LANG_CSHARP_CODE.toUpperCase().equals(bean.getDeflang().toUpperCase()) ? "selected" : "" %>>C#</option>
                </select>
				</label>
				<div id="msgDiv"><label class="wrongfont" id="person_msg" ></label></div>
                <div id="person_update_msg"><img  class="imgwidthHeight" src="../images/ok.png" alt="ok" />個人設定を変更しました。</div>
				<input type="button" class="BTN" id="personBtn" onclick="personBtnClick();" value="  設定変更    " />
            </div>
		</form>
		<div class="PersonInfoUpdateSpace"><input type="button" class="BTN " value="閉じる" onclick="window_close();" /></div>
	</div>
</body>
</html>
