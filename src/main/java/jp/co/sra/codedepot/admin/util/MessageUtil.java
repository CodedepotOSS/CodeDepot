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
package jp.co.sra.codedepot.admin.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class MessageUtil {
private static PropertyTree ptree = null;

    private MessageUtil() {

    }

    /**
     * <p> プロパティ初期化関数 </p>
     *
     * @param filename      プロパティファイル名
     * @throws IOException
     */
    public synchronized static void init(String filename)
    		throws IOException {
    	Properties props = new Properties();
    	InputStream is = MessageUtil.class.getClassLoader().getResourceAsStream(filename);
        props.load(is);
        is.close();
        // プロパティツリー作成
        ptree = PropertyTree.makeTree(props);
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param message メッセージ
     * @return String メッセージ内容
     */
    public static String getMessageString(Message message) {
    	if ((null == ptree) || (null == message)) {
    		return "";
    	}
        String msgContent = ptree.getProperty(message.getMsgId());
        if (msgContent == null || "".equals(msgContent)) {
            msgContent = "";
        } else {
            msgContent = MessageFormat.format(msgContent, (Object[])message.getParams());
        }
        return msgContent;
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param params メッセージパラメータ
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String[] param) {
    	if ((null == ptree) || (null == msgId)) {
    		return "";
    	}
        String msgContent = ptree.getProperty(msgId);
        if (msgContent == null || "".equals(msgContent)) {
            msgContent = "";
        } else {
            msgContent = MessageFormat.format(msgContent, (Object[])param);
        }
        return msgContent;
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId) {
    	if ((null == ptree) || (null == msgId)) {
    		return "";
    	}
        String msgContent = ptree.getProperty(msgId);
        if (msgContent == null || "".equals(msgContent)) {
            msgContent = "";
        } else {
            msgContent = MessageFormat.format(msgContent, (Object[])null);
        }
        return msgContent;
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ１
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String param1) {
        String[] params = {param1};
        return getMessageString(msgId, params);
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ１
     * @param param2 メッセージパラメータ２
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String param1, String param2) {
        String[] params = {param1, param2};
        return getMessageString(msgId, params);
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ１
     * @param param2 メッセージパラメータ２
     * @param param3 メッセージパラメータ３
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String param1, String param2, String param3) {
        String[] params = {param1, param2, param3};
        return getMessageString(msgId, params);
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ１
     * @param param2 メッセージパラメータ２
     * @param param3 メッセージパラメータ３
     * @param param4 メッセージパラメータ4
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String param1, String param2, String param3
    		, String param4) {
        String[] params = {param1, param2, param3, param4};
        return getMessageString(msgId, params);
    }

    /**
     * <p>[名称] メッセージ内容の取得</p>
     * <p>[機能] メッセージ内容を取得する</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ１
     * @param param2 メッセージパラメータ２
     * @param param3 メッセージパラメータ３
     * @param param4 メッセージパラメータ4
     * @param param5 メッセージパラメータ5
     * @return String メッセージ内容
     */
    public static String getMessageString(String msgId, String param1, String param2, String param3
    		, String param4, String param5) {
        String[] params = {param1, param2, param3, param4, param5};
        return getMessageString(msgId, params);
    }
}
