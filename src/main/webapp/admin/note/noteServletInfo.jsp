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
<%@ page import="jp.co.sra.codedepot.admin.util.MessageUtil"%>
<%@ page import="jp.co.sra.codedepot.admin.util.APMsgConst"%>
<%@ page import="jp.co.sra.codedepot.admin.util.APMsgParamConst"%>

   <!-- 選択された{0}を削除しても、よろしいでしょうか？ -->
   var delete_Msg = "<%=MessageUtil.getMessageString(APMsgConst.C_COM_01,APMsgParamConst.MSG_PARAM_NOTEINFO_TITLE)%>";
   <!--  アクセス権限がありません。 -->
   var permit_Msg = "<%=MessageUtil.getMessageString(APMsgConst.W_COM_05)%>";
   <!--{0}画面は三個まで開くことができません。一つの画面を閉じてください。  -->
   var note_Open_Msg = "<%=MessageUtil.getMessageString(APMsgConst.I_MEM_02,APMsgParamConst.MSG_PARAM_NOTE_TITLE)%>";
   var root_path = "<%=request.getContextPath()%>";
