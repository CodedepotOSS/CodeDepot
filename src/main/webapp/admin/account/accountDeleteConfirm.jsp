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
<%@ page import="jp.co.sra.codedepot.admin.util.MessageUtil"%>
<%@ page import="jp.co.sra.codedepot.admin.util.APMsgConst"%>
<%@ page import="jp.co.sra.codedepot.admin.util.APConst"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>アカウント削除確認</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.7.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery-ui-1.7.1.redmond.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/admin/js/common.js"></script>
    <script type="text/javascript">
    loadCSS("<%=request.getContextPath()%>/admin/css/", "admin_common.css");</script>
<script type="text/javascript">
	$(document).ready(function(){
		var arr = window.parent.$(":checkbox[id!='chk'][checked='true']");
		$.each(arr, function(i, n) {
			var elem = $("<span></span>").text(n.value);
			$("#users").append('<li>' + elem.html() + '</li>');
		});
	});
	$(document).keydown(function(event) {
		  if (event.keyCode == 116) {
		    $('form').each(function() {
		      event.preventDefault();
		    });
		  }
	});
	function doDelete() {
		var dialog = window.parent.$('#rmDialog');
		dialog.dialog('option', 'result', 1);
		dialog.dialog('close');
		return false;
	}
	function doCancel() {
		var dialog = window.parent.$('#rmDialog');
		dialog.dialog('option', 'result', 0);
		dialog.dialog('close');
		return false;
	}
</script>
</head>
<body>
	<div id="form_container">
		<form id="form_137683" class="appnitro" enctype="multipart/form-data" method="post" action="">
			<label class="medium"><%=MessageUtil.getMessageString(APMsgConst.C_COM_01, APConst.ACCOUNT_LABLE)%></label>
			<label class="medium"><ul id="users"></ul></label>
			<div class="medium"><button class="DEL button-mouseout" onmouseover="this.className='button-mouseover'" onmouseout="this.className='button-mouseout'" onmousedown="this.className='button-mousedown'" onmouseup="this.className='button-mouseup'" onclick="doDelete();">削除</button><button class="button-mouseout" onmouseover="this.className='button-mouseover'" onmouseout="this.className='button-mouseout'" onmousedown="this.className='button-mousedown'" onmouseup="this.className='button-mouseup'" onclick="doCancel();">中止</button></div>
        </form>
    </div>
</body>
</html>
