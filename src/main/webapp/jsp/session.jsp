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

<%@ page contentType="text/javascript;charset=UTF-8" %>

var contextPath = "<%= request.getContextPath() %>";
var loginName = "<%= (session.getAttribute("loginName") != null ? session.getAttribute("loginName") : "") %>";
var loginId = "<%= (session.getAttribute("loginId") != null ? session.getAttribute("loginId") : "-1") %>";
var loginRole = "<%= (session.getAttribute("loginRole") != null ? session.getAttribute("loginRole") : "") %>";
var loginLang = "<%= (session.getAttribute("loginLang") != null ? session.getAttribute("loginLang") : "") %>";
var availLang = "<%= (session.getAttribute("availLang") != null ? session.getAttribute("availLang") : "") %>";

function setLangOptions(div) {
	var selection = $(div);
	var langs = availLang.split(" ");
	if ($.inArray("java", langs) < 0) {
		selection.children("option[value='java']").remove();
	}
	if ($.inArray("C", langs) < 0) {
		selection.children("option[value='C']").remove();
	}
	if ($.inArray("csharp", langs) < 0) {
		selection.children("option[value='csharp']").remove();
	}
	if ($.inArray("vb.net", langs) < 0) {
		selection.children("option[value='vb.net']").remove();
	}
}
