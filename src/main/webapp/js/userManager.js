/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
ERR_MSG_HEAD = "__error__";
var jpMsg=langList.jp;

var usm = {
	htmls: {
		loggedIn:
        '<div id="loggedIn">' +
            '<ui>' +
                '<li id="username" class="vlink" onclick="showPrefsWindow()">_USERNAME_</li>' +
                '<li><a class="vlink" href="jsp/logout.jsp">ログアウト</a></li><br>' +
                '<li id="project_list"><a href="#" class="vlink" onclick="showProjectList()">プロジェクト一覧</a></li>' +
                '<li id="admin_menu"><a href="#" class="vlink" onclick="showAdminWindow()">システム管理</a></li>' +
                '<li id="help_menu"><a href="#" class="vlink" onclick="showHelpWindow()">マニュアル</a></li>' +
            '</ui>' +
        '</div>'
    },
    status: null,
    username: ""
};

function updateUsername(uname) {
	var username = $.trim(uname);
	if (username != "") {
		$("#usmWrapper").html(usm.htmls.loggedIn.replace(/_USERNAME_/, username));
        usm.username = username;
        usm.status = "loggedIn";
    } else {
        $("#usmWrapper").html(usm.htmls.loggedOut);
        usm.status = "loggedOut";
        usm.username = "";
    }
}

function isAdminUser() {
    role = loginRole - 0;
    return (role > 0) ? true : false;
}

function returnUsername() {
	return usm.username;
}

/* set the element's content specified by the id to empty */
function setEmpty(selector) {
	$(selector).html("");
}

/* link to menu page */
function linkTo(parts) {
	location.replace(parts.options[parts.selectedIndex].value);
}

function showAdminWindow() {
	var newwin = window.open(contextPath + "/admin/menu/menu.jsp?pageid=top");
	if (window.focus) {newwin.focus();}
}

function setPrefsWindow() {
    $("#dialog").dialog({
        autoOpen: false,
        modal: true,
        resizable: false,
        width: 600,
        height: 600,
	position:['middle', 20],
        title: "個人設定",
        open: function() {
            var iframe = '<iframe id="iframe" frameborder="0" marginheight="0" marginwidth="0"></iframe>';
            $("#dialog").append(iframe);
            $("#iframe").attr({
                src: contextPath + '/admin/account/personInfoUpdate.jsp',
                width: '100%',
                height: '100%'
            });
            return false;
        },
        close: function() {
            $("#iframe").remove();
        }
    });
}

function showPrefsWindow() {
	$("#dialog").dialog("open");
}
