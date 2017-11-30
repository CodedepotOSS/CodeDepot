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

public class APMsgParamConst {

	/** ラインFrom文字列 ノート情報用 */
	public  final static  String MSG_PARAM_LINEFROM_NAME = "開始行";
	/** ラインTo文字列 ノート情報用 */
	public  final static  String MSG_PARAM_LINETO_NAME = "終了行";
	/** 正整数 文字列 */
	public  final static  String MSG_PARAM_INTEGER_NAME = "自然数";
	/** 本文 文字列 ノート情報用 */
	public  final static  String MSG_PARAM_CONTENTS_NAME = "本文";
	/** ノート情報のtitle ノート追加・変更用 */
	public  final static  String MSG_PARAM_NOTE_TITLE = "ノート編集";
	/** ノート情報のtitle ノート情報用 */
	public  final static  String MSG_PARAM_NOTEINFO_TITLE = "ノート";
	/** アカウントチェック用 */
	public final static String MSG_PARAM_USERNAME_CHECK = "半角英数字と半角符号 \".\" \"-\" \"_\" \"@\"";
	public final static String MSG_PARAM_PROJECTNAME_CHECK = "半角英数字と半角符号 \".\" \"-\" \"_\"";
	/** ライセンス名用 */
	public final static String MSG_PARAM_LICENSE_CHECK = "半角英数字と半角符号 \".\" \"-\" \"+\" \"_\" \"/\"";
	/** アカウント名 */
	public final static String MSG_PARAM_USERNAME_CHAR = "アカウント名";
	/** ログイン名 */
	public final static String MSG_PARAM_LOGINNAME_CHAR = "ログイン名";
	/** アカウント情報 */
	public final static String MSG_PARAM_USERINFO_CHAR = "アカウント情報";
	/** 挿入 */
	public final static String MSG_PARAM_INSERT_CHAR = "挿入";
	/** 更新 */
	public final static String MSG_PARAM_UPDATE_CHAR = "更新";
	/** 削除 */
	public final static String MSG_PARAM_DELETE_CHAR = "削除";
	/** 管理者コメント */
	public final static String MSG_PARAM_NOTE_CHAR = "管理者コメント";
	/** パスワードチェック用 */
	public final static String MSG_PARAM_PWD_CHECK = "半角英数字或は半角符号";
	/** パスワード文字 */
	public final static String MSG_PARAM_PWD_CHAR = "パスワード";
	/** パスワード再入力文字 */
	public final static String MSG_PARAM_PWDAGAIIN_CHAR = "パスワード再入力";
	/** 旧パスワード文字 */
	public final static String MSG_PARAM_OLDPWD_CHAR = "旧パスワード";
	/** 新パスワード文字 */
	public final static String MSG_PARAM_NEWPWD_CHAR = "新パスワード";
	/** 新パスワード再入力文字 */
	public final static String MSG_PARAM_NEWPWDAGAIN_CHAR = "新パスワード再入力";
	/** メールアドレス */
	public final static String MSG_PARAM_EMAIL_CHAR = "メールアドレス";
	/**入力した旧パスワード */
	public final static String MSG_PARAM_INPUT_PWD_CHAR = "入力した旧パスワード";
	/** パスワード変更タイトル */
	public final static String MSG_PARAM_TITLE_PWD_CHAR = "パスワード変更";
	/** 個人情報変更タイトル */
	public final static String MSG_PARAM_PERSONINFO_PWD_CHAR = "個人情報変更";
	/** プロジェクト識別子名 */
	public final static String MSG_PARAM_PROJECT_NAME_CHAR = "プロジェクト識別子名";
	/** ラインFrom */
	public final static String MSG_PARAM_LINE_FROM_CHAR = "開始行";
	/** ラインTo */
	public final static String MSG_PARAM_LINE_TO_CHAR = "終了行";
	/** バッチジョブの即時起動 */
	public final static String MSG_PARAM_BATCH_RUN_ERROR = "検索インデックス更新処理の起動";

	/** 登録(メッセージ) */
	public static String MSG_INSERT = "登録";

	/** プロジェクト名(メッセージ) */
	public static String MSG_TITLE = "プロジェクト名";

	/** SCM(メッセージ) */
// Modified by wubo on 2010/08/30 for V2.1対応 Start
//	public static String MSG_SCM = "Subversion/CVS";
// Modified by kawabe on 2011/09/20 for Git 対応
//	public static String MSG_SCM = "Subversion/CVS/Jazz";
	public static String MSG_SCM = "リポジトリ";
// Modified by wubo on 2010/08/30 for V2.1対応 End

	/** プロジェクト管理者(メッセージ) */
	public static String MSG_MANAGER = "プロジェクト管理者";

	/** 権限ユーザ(メッセージ) */
	public static String MSG_LIMIT_USER = "権限ユーザ";

	/** ライセンス(メッセージ) */
	public static String MSG_LICENSE = "ライセンス";

	/** ライセンスキー(メッセージ) */
	public static String MSG_LICENSE_KEY = "ライセンスキー";

	/** ライセンス名(メッセージ) */
	public static String MSG_LICENSE_NAME = "ライセンス名";

	/** 構成管理システムのパスワード(メッセージ) */
	public static String MSG_PASS = "Subversion/CVS/Jazz のパスワード";

	/** 構成管理システムのパスワード再入力(メッセージ) */
	public static String MSG_PASS_CONFIRM = "Subversion/CVS/Jazz のパスワード再入力";

	/** 構成管理システムのユーザ名(メッセージ) */
	public static String MSG_SCM_USER = "Subversion/CVS/Jazz のユーザ名";

	/** 概要(メッセージ) */
	public static String MSG_DESCR = "概要";

	/** アクセスユーザ(メッセージ) */
	public static String MSG_ACCESS_USER = "アクセス許可ユーザ";

	/** 検索インデックス更新処理の開始時刻 */
	public static String MSG_CRONTAB = "検索インデックス自動更新時刻";

	/** URL(メッセージ) */
	public static String MSG_SRC_PATH = "プロジェクトのパス／URL";

	/** SVNのURL(メッセージ) */
	public static String MSG_SVN_URL = "SubversionのURL";

	/** CVSのURL(メッセージ) */
	public static String MSG_CVS_URL = "CVSのURL";

	/** Git のURL(メッセージ) */
	public static String MSG_GIT_URL = "GitのURL";

// Added by wubo on 2010/08/30 for V2.1対応 Start
	/** JAZZのURL(メッセージ) */
	public static String MSG_JAZZ_URL = "JazzのURL";
// Added by wubo on 2010/08/30 for V2.1対応 End

	/** ローカル(メッセージ) */
	public static String MSG_LOCAL_URL = "パス";

	/** ホームページのＵＲＬ(メッセージ) */
	public static String MSG_SITE_URL = "ホームページのＵＲＬ";

	/** ダウンロードページのＵＲＬ(メッセージ) */
	public static String MSG_DOWNLOAD_URL = "ダウンロードページのＵＲＬ";

	/** ＵＲＬ(メッセージ) */
	public static String MSG_URL = "ＵＲＬ";

	/** CRON標準書式 */
	public static String MSG_CRON = "起動時刻";

	/** LDAPのバースDNとLDAPのアカウントDN必須です。*/
	public static  String LDAP_DN_NAME_CHAR= "LDAP_BASE_DN とLDAP_USER_DNは必須のパラメタです。";
}
