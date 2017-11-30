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

public class Message {
    /** メッセージＩＤ */
    private String msgId = null;

    /** メッセージパラメータ */
    private String[] params = null;

    /**
     * <p>[名称] デフォルトコンストラクタ</p>
     */
    public Message() {

    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     */
    public Message(String msgId) {
        this.msgId = msgId;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param param メッセージパラメータ
     */
    public Message(String msgId, String param) {
        this.msgId = msgId;
        String[] msP = {param};
        this.params = msP;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ1
     * @param param2 メッセージパラメータ2
     */
    public Message(String msgId, String param1, String param2) {
        this.msgId = msgId;
        String[] msP = {param1, param2};
        this.params = msP;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ1
     * @param param2 メッセージパラメータ2
     * @param param3 メッセージパラメータ3
     */
    public Message(String msgId, String param1, String param2, String param3) {
        this.msgId = msgId;
        String[] msP = {param1, param2, param3};
        this.params = msP;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ1
     * @param param2 メッセージパラメータ2
     * @param param3 メッセージパラメータ3
     * @param param4 メッセージパラメータ4
     */
    public Message(String msgId, String param1, String param2, String param3
    		, String param4) {
        this.msgId = msgId;
        String[] msP = {param1, param2, param3, param4};
        this.params = msP;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param param1 メッセージパラメータ1
     * @param param2 メッセージパラメータ2
     * @param param3 メッセージパラメータ3
     * @param param4 メッセージパラメータ4
     * @param param5 メッセージパラメータ5
     */
    public Message(String msgId, String param1, String param2, String param3
    		, String param4, String param5) {
        this.msgId = msgId;
        String[] msP = {param1, param2, param3, param4, param5};
        this.params = msP;
    }

    /**
     * <p>[名称] コンストラクタ</p>
     * <p>[機能] コンストラクタ</p>
     *
     * @param msgId メッセージＩＤ
     * @param params メッセージパラメータ
     */
    public Message(String msgId, String[] params) {
        this.msgId = msgId;
        this.params = params;
    }

    /**
     * <p>[名称] メッセージＩＤの取得</p>
     * <p>[機能] メッセージＩＤの取得する</p>
     *
     * @return メッセージＩＤ
     */
	public String getMsgId() {
		return msgId;
	}

	/**
     * <p>[名称] メッセージＩＤの設定</p>
     * <p>[機能] メッセージＩＤの設定</p>
     *
     * @param msg メッセージＩＤ
     */
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	/**
     * <p>[名称] メッセージパラメータの取得</p>
     * <p>[機能] メッセージパラメータの取得する</p>
     *
     * @return エラーメッセージ
     */
	public String[] getParams() {
		return params;
	}

	/**
     * <p>[名称] メッセージパラメータの設定</p>
     * <p>[機能] メッセージパラメータの設定</p>
     *
     * @param msg エラーメッセージ
     */
	public void setParams(String[] params) {
		this.params = params;
	}
}
