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


public class APConst {
	/** エンコード UTF-8 */
	public static final String ENCODE_UTF_8 = "UTF-8";

	public static final String PROPERTY_BASEPATH = "";

	public static final String PROPERTY_FILENAME_AP = "application.properties";

	public static final String PROPERTY_FILENAME_LICENSE = "license.properties";

	public static final String PROPERTY_FILENAME_MSG = "messages.properties";

	public static final String PROPERTY_FILENAME_LDAPAUTH = "ldap.properties";

	public static final String PROPERTY_FILENAME_HTTPAUTH = "auth.properties";

	public static final String PROPERTY_INDEXER = "indexer.properties";

	/** ページ毎に表示件数 */
	public static final String ITEMS_PER_PAGE = "ITEMS_PER_PAGE";

	/** ファイルダウンロードベースパス */
	public static final String FILE_DOWN_PATH = "FILE_DOWN_PATH";

	/** 異常のログ出力キー */
	public static final String EXCEPTION_KEY = "exception";

	/** 更新成功かどうかフラグキー */
	public static final String IS_SUCCESS_KEY = "isSuccess";

	/** 編集 モデルキー*/
	public static final String MODE = "mode";
	/** パラメータ ページ */
	public static final String PARAM_PAGE = "page";
	/** パラメータ 遷移元名 */
	public static final String PARAM_FROM_ID = "fromId";
	/** パラメータ 処理モード */
	public static final String PARAM_METHOD = "method";
	/** パラメータ アカウント名 */
	public static final String PARAM_USERNAME = "username";
	/** Redirect */
	public static final String PARAM_REDIRECTURL = "redirectURL";

	/** モデル:追加 */
	public static final String MODE_ADD = "add";
	/** モデル:変更 */
	public static final String MODE_MODIFY = "modify";
	/** モデル:初期化 */
	public static final String MODE_INIT = "init";
	/** モデル:削除 */
	public static final String MODE_DEL ="del";
	/** モデル:画面一覧を取得の場合 */
	public static final String MODE_LIST ="list";

	/** 分類キー*/
	public static final String KIND = "kind";
	/** 分類:公開 */
	public static final String KIND_PUBLIC = "public";
	/** 分類:個人 */
	public static final String KIND_PRIVATE = "private";

	/** セッション.ログインID */
	public static final String  SESSION_LOGIN_ID = "loginId";
	/** セッション.ログイン名 */
	public static final String  SESSION_LOGIN_NAME = "loginName";
	/** セッション.ログイン権限 */
	public static final String  SESSION_LOGIN_ROLE = "loginRole";
	/** セッション.ユーザのデフォルト検索言語 */
	public static final String  SESSION_LOGIN_LANG = "loginLang";
	/** セッション.アクセス権限情報のリスト */
	public static final String  SESSION_LOGIN_PERMITPRJLIST = "permission";
	/** セッション.アクセス権限の情報取得した日時 */
	public static final String  SESSION_LOGIN_PERMITPRJTIME = "permissionGetTime";

	/** 利用可能な検索言語 */
	public static final String  SESSION_AVAIL_LANG = "availLang";

	/** セッション．アカウント情報 */
	public static final String SESSION_ACCOUNT_INFO = "accountInfo";
	/** セッション．編集中のプロジェクト情報 */
	public static final String SESSION_EDITING_PROJECT_INFO = "projectInfo";
	/** セッション．アカウント情報 */
	public static final String SESSION_NEW_ACCOUNT_INFO = "newAccountInfo";
	/** セッション．クライアントIP */
	public static final String SESSION_CLIENT_IP = "clientIP";
	/** セッション．スケジューラ情報 */
	public static String SESSION_SCHEDULER_INFO = "schedulerInfo";
	/** 削除の文字 */
	public static final String DELETE_CHAR = "削除";
	/** 変更の文字 */
	public static final String UPDATE_CHAR = "変更";
	/** 新規の文字 */
	public static final String INSERT_CHAR = "新規";

	/** Sevlet */
	public static final String MSG_SERVLET = "Servlet";
	/** Action */
	public static final String MSG_ACTION = "Action";

	/** 参照方法がSVN */
	public static final String TYPE_SVN = "svn";
	/** 参照方法CVS */
	public static final String TYPE_CVS = "cvs";
	/** 参照方法git */
	public static final String TYPE_GIT = "git";
	/** 参照方法がlocal */
	public static final String TYPE_LOCAL = "local";
	// Added by wubo on 2010/08/27 for V2.1対応 Start
	/** 参照方法がJAZZ */
	public static final String TYPE_JAZZ = "jazz";
	// Added by wubo on 2010/08/27 for V2.1対応 End

	/** httpで始まる */
	public static final String PATH_HTTP = "http://";

	/** httpsで始まる */
	public static final String PATH_HTTPS = "https://";

	/** fileで始まる */
	public static final String PATH_FILE = "file:///";

	/** svn+sshで始まる */
	public static final String PATH_SVN_SSH = "svn+ssh://";

	/** pserverで始まる */
	public static final String PATH_PSERVER = "pserver";

	/** gitで始まる */
	public static final String PATH_GIT = "git://";

	/** sshで始まる */
	public static final String PATH_SSH = "ssh://";

	/** /で始まる */
	public static final String PATH_LOCAL = "/";

	/** 改行符 */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	/** 成功場合の遷移先 */
	public static final String SUCCESS_URL = "SUCCESS_URL";
	/** 失敗場合の遷移先 */
	public static final String FAILURE_URL = "FAILURE_URL";
	/** ログイン画面の遷移先 */
	public static final String LOGIN_URL = "LOGIN_URL";
	/** ログインServlet */
	public static final String LOGIN_SERVLET = "LOGIN_SERVLET";
	/** CHECK_SESSION_JSP */
	public static final String CHECK_SESSION_JSP = "CHECK_SESSION_JSP";

	/** LDAP認証フラグ */
	public static final String LDAP_AUTH_ON = "on";
	/** HTTP認証フラグ */
	public static final String HTTP_AUTH_ON = "on";
	/** パスワード SHA*/
	public static final String SHA_ALGORITHM = "SHA-1";

	/** 機能ID プロジェクト */
	public static final String ID_PROJECT = "project";

	/** 機能ID アカウント */
	public static final String ID_ACCOUNT = "account";

	/** 機能ID ライセンス */
	public static final String ID_LICENSE = "license";

	/** 機能ID バッチログ */
	public static final String ID_BATCH = "batch";

	/** 機能ID TOPページ */
	public static final String ID_TOP = "top";

	/** 機能ID ログイン */
	public static final String ID_LOGIN = "login";

	/** 機能名 プロジェクト */
	public static final String NAME_PROJECT = "プ ロ ジ ェ ク ト 検 索 ・ 一 覧";

	/** 機能名 アカウント */
	public static final String NAME_ACCOUNT = "ア カ ウ ン ト 検 索 ・ 一 覧";

	/** 機能名 ライセンス */
	public static final String NAME_LICENSE = "ラ イ セ ン ス 情 報";

	/** 機能ID バッチログ */
	public static final String NAME_BATCH = "バッチ処理ログ表示";

	/** 機能名 TOPページ */
	public static final String NAME_TOP = "";

	/** 機能名 ログイン */
	public static final String NAME_LOGIN = "ログイン";

	/** プロジェクト情報 */
	public static final String PROJECT_INFO = "プロジェクト情報";

	/**
	 * 画面ID アカウント検索・一覧画面
	 */
	public static final String PAGE_MODE_G_02_01 = "G_02_01";

	/**
	 * 画面ID アカウント追加・変更画面
	 */
	public static final String PAGE_MODE_G_02_02 = "G_02_02";

	/**
	 * 画面ID アカウント追加・変更確認画面
	 */
	public static final String PAGE_MODE_G_02_03 = "G_02_03";

	/**
	 * 画面ID アカウント詳細情報画面
	 */
	public static final String PAGE_MODE_G_02_06 = "G_02_06";

	/**
	 * 日時のフォーマット
	 */
	public static final String DATE_FORMAT_YYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";
	/** 整数の表示フォーマット */
	public static final String INTEGER_FORMAT = "#,##0";


	/** プロジェクトのアクセス制御の有無文字:あり */
	public static final String RESTRICTED_TRUE_STR = "あり";
	/** プロジェクトのアクセス制御の有無文字:なし */
	public static final String RESTRICTED_FALSE_STR = "なし";
	/** アクセス制限のないプロジェクト:public */
	public static final String PERMISSION_PUBLIC = "public";
	/** アクセス制限のあるプロジェクト:restricted */
	public static final String PERMISSION_RESTRICTED = "restricted";

	/** 改行 */
	public static final String NEW_LINE = "\n";

	/** HTML改行 */
	public static final String HTML_NEW_LINE = "<br/>";
	/** ON String */
	public static final String ON_CHAR = "on";

	/** プロジェクト識別子名nの取得回数 */
	public static int PROJ_NAME_RETRY_TIMES = 100;

	/** ロパティキー プロジェクトのパス */
	public static final String PROP_KEY_CODE_DIRECTORY = "codeDirectory";
	/** ロパティキー プロジェクトのHTMLパス */
	public static final String PROP_KEY_HTML_DIRECTORY = "htmlDirectory";
	/** ロパティキー プロジェクトのTEMPパス */
	public static final String PROP_KEY_TEMP_DIRECTORY = "tempDirectory";

	/** classesパス */
	public static String FILE_BUILDPATH = "WEB-INF/classes/";

	/** ライセンス */
	public static String PROPERTY_LICENSE = "LICENSE=";

	/** DESキー */
	public static String DES_KEY = "abcdefgh";

	/** IVキー */
	public static String IV_KEY = "12345679";

	/** DES暗号 */
	public static String DES_CODE = "DES";

	/** DES方法 */
	public static String DES_METHOD = "DES/CFB8/NoPadding";

	/** ライセンスキー */
	public static String LICENSE_KEY = "LICENSE";

	/** プロジェクト識別子 **/
	public static String PROJECT_TITLE = "projectTitle";

	/** スケジューラグループの文字 **/
	public static String SCHEDULER_GROUP = "schedulerGroup";

	/** 簡単なグループの文字 **/
	public static String SIMPLE_GROUP = "simpleGroup";

	/** ライセンス取得標識 **/
	public static String LICENSE_GET_FLG = "flg";

	/** ライセンス取得成功の場合 **/
	public static String LICENSE_GET_ERROR = "error";

	/** 一般ユーザ */
	public static final String ROLE_LEVEL_USER = "一般ユーザ";

	/** プロジェクト管理者 */
	public static final String ROLE_LEVEL_MANAGER = "プロジェクト管理者";

	/** システム管理者 */
	public static final String ROLE_LEVEL_SYSTEM = "システム管理者";

	/** アカウントアクティブ 有効 */
	public static final String USER_ACTIVE = "有効";

	/** アカウントアクティブ 無効 */
	public static final String USER_INACTIVE = "無効";

	/** ハイフン */
	public static final String HYPH = "-";

	/** アクセス権限時間期間 */
	public static final String ACCESS_DURING_TIME = "ACCESS_DURING_TIME";

	/** SCMのテスト */
	public static final String SCM_CONNECT = "doConnect";

	/** SCMのテスト失敗 */
	public static final String SCM_CONNECT_FALSE = "false";

	/** 無制限年 */
	public static final String UNLIMIT_YEAR = "9999";

	/** 無制限月 */
	public static final String UNLIMIT_MONTH = "99";

	/** 解析した無制限年月 */
	public static final String UNLIMIT_DATE_CODE = "9999";

	/** 解析した年月フォーマット */
	public static final String DATE_FORMAT_YYMM = "yyMM";

	/** ライセンス情報表示画面、有効期限フォーマット */
	public static final String DATE_FORMAT_YYYYMMDD = "yyyy/MM/dd";

	/** 有効期限無制限 */
	public static final String UNLIMIT_DATE = "無制限";

	/** ファイルの最大数 */
	public static final String UNLIMIT_FILE = "無制限";

	/** "0"を入力した場合、ファイル数無制限とみなす */
	public static final String UNLIMIT_FILE_LOAD = "0";

	/** タイプhtml */
	public static final String TYPE_HTML = "html";
	/** タイプsrc */
	public static final String TYPE_SRC = "src";
	/** ファイルの最大数ライセンスオプション */
	public static final int FILEMAXNUM_OPTION_RATIO = 1000;
	public static final int BATCHLOG_MAXNUM = 20000;
	public static final String TEMP_PATH_KEY = "tempDirectory";
	public static final String SCM_TIMEOUT = "SCM_TIMEOUT";

	/** HTML ファイルのSuffix */
	public static final String SUFFIX_HTML = ".html";

	/** プロジェクト管理者のユーザID(json) */
	public static final String JSON_SRC_PATH = "srcpath";
	/** プロジェクト管理者のユーザID(json) */
	public static final String JSON_ADMINID = "adminid";

	/** アクセス権限指定ユーザID(json) */
	public static final String JSON_PERMITID = "permituserid";

	/** プロジェクト識別子名(json) */
	public static final String JSON_PRONAME = "proName";

	/** プロジェクト追加・変更成功のフラグ(json) */
	public static final String JSON_INPUTFLG = "inputFlg";

	/** アカウント */
	public static final String ACCOUNT_LABLE = "アカウント";

	/** LICENSE_FILE */
	public static final String LICENSE_FILE = "LICENSE_FILE";

	/** NOT_ALLOWED_LOCAL_PATH */
	public static final String NOT_ALLOWED_LOCAL_PATH = "NOT_ALLOWED_LOCAL_PATH";
	/** NOT_ALLOWED_LOCAL_PATH Spliter*/
	public static final String PATH_SPLITER = ":";
// Added by wubo on 2010/08/27 for V2.1対応 Start
	/** NOT_ALLOWED_LOCAL_PATH Spliter*/
	public static final String PATH_SPLITER_WINDOWS = "\\|";
	/** SVN_PATH */
	public static final String SVN_PATH = "SVN_PATH";
	/** CVS_PATH */
	public static final String CVS_PATH = "CVS_PATH";
	/** GIT_PATH */
	public static final String GIT_PATH = "GIT_PATH";
	/** JAZZ_PATH */
	public static final String JAZZ_PATH = "JAZZ_PATH";
// Added by wubo on 2010/08/27 for V2.1対応 End
	/** suffix */
	public static final String LIMIT_STR = "...";
	/** プロジェクト検索画面概要コラムの文字列制限 */
	public static final int PROJ_DESCRIPTION_LIMIT = 40;
}
