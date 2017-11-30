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

/**
 * DBに関する定数を定義する（フィールド名、フィールド長さ、スキーマ名、SQL文名）
 *
 * @author sra
 *
 */
public interface DBConst {

	/* スキーマ名とSQL文名 */
	public final static String PROJECT = "project";
	/* スキーマ名とSQL文名 */
	public final static String BATCHLOG = "batchlog";

	/* 検索インデックス */
	public final static String SOURCE = "source";

	public final static String PROJECT_UPDATE_PROJECT_SQL_ID = PROJECT
			+ ".updateTableProject";

	/* フィールド名とフィールド長さ テーブル名_フィールド名 */
	/** scm_user */
	public final static String PROJECT_SCM_USER = "scm_user";
	public final int PROJECT_SCM_USER_LENGTH = 40;

	/* プロジェクト情報テーブルのフィールド定義 */
	/** シーケンス番号 */
	public final static String PROJECT_ID = "id";
	/** プロジェクト識別子名 */
	public final static String PROJECT_NAME = "name";
	/** プロジェクトの題名 */
	public final static String PROJECT_TITLE = "title";
	/** プロジェクトの説明 */
	public final static String PROJECT_DESCRIPTION = "description";
	/** プロジェクトのライセンス */
	public final static String PROJECT_LICENSE = "license";
	/** ホームページのＵＲＬ */
	public final static String PROJECT_SITE_URL = "site_url";
	/** ダウンロードサイトのＵＲＬ */
	public final static String PROJECT_DOWNLOAD_URL = "download_url";
	/** アクセス制御の有無 */
	public final static String PROJECT_RESTRICTED = "restricted";
	/** プロジェクトの指定方法 */
	public final static String PROJECT_SRC_TYPE = "src_type";
	/** プロジェクトのパス／URL */
	public final static String PROJECT_SRC_PATH = "src_path";
	/** SCMのログインユーザ名 */
	public final static String PROJECT_scm_user = "scm_user";
	/** SCMのログインパスワード */
	public final static String PROJECT_SCM_PASS = "scm_pass";
	/** 検索インデックス更新処理の開始時刻 */
	public final static String PROJECT_CRONTAB = "crontab";
	/** 検索対象除外拡張子 */
	public final static String PROJECT_IGNORES = "ignores";
	/** プロジェクト管理者のユーザID */
	public final static String PROJECT_ADMIN = "admin";
	/** 削除フラグ */
	public final static String PROJECT_DEL_FLAG = "del_flag";
	/** 作成者 */
	public final static String PROJECT_CUSERID = "cuserid";
	/** 作成時刻 */
	public final static String PROJECT_CTIME = "ctime";
	/** 更新者 */
	public final static String PROJECT_MUSERID = "muserid";
	/** 更新時刻 */
	public final static String PROJECT_MTIME = "mtime";
	/** ファイルテーブル ファイルのパス	*/
	public final static String FILE_PATH = "path";
	/** ログインID	*/
	public final static String LOGIN_ID = "loginID";
	/** ファイルID */
	public final static String FILE_ID = "fileID";
	/** インデックス更新時刻 */
	public final static String PROJECT_INDEXED_AT = "indexed_at";

	/** プロジェクト名の最大桁数 */
	public static int TITLE_MAX_LEN = 80;

	/** ライセンスの最大桁数 */
	public static int LICENSE_MAX_LEN = 80;

	/** SCMのログインパスワードの最大桁数 */
	public static int PASS_MAX_LEN = 20;

	/** SCMのログインパスワードの最小桁数 */
	public static int PASS_MIN_LEN = 4;

	/** ユーザ名の最大桁数 */
	public static int USER_MAX_LEN = 40;

	/** 概要の最大桁数 */
	public static int DESCR_MAX_LEN = 2048;

	/** 検索インデックス更新処理の開始時刻の最大桁数 */
	public static int CRONTAB_MAX_LEN = 80;

	/** URLの最大桁数 */
	public static int URL_MAX_LEN = 1024;

	/**
	 * バイトチェック長さ:アカウント名
	 */
	public static final int CHECK_LENGTH_USERNAME = 40;

	/**
	 * バイトチェック長さ:パスワード
	 */
	public static final int CHECK_LENGTH_PASSWORD = 20;

	/**
	 * バイトチェック長さ:メールアドレス
	 */
	public static final int CHECK_LENGTH_EMAIL = 1024;

	/**
	 * バイトチェック長さ:管理者コメント
	 */
	public static final int CHECK_LENGTH_NOTE = 2048;


	/* アクセス権限テーブルのフィールド定義 */
	/** アカウントID */
	public final static String PERMIT_MID = "mid";
	/** プロジェクト識別子 */
	public final static String PERMIT_PROJECT = "project";
	/** アカウントテーブル  アカウント名 */
	public final static String MEMBER_USERNAME = "username";

	/* バッチ処理ログ情報テーブルのフィールド定義 */
	/** バッチ開始時刻 */
	public final static String BATCHLOG_STIME = "stime";

	/** バッチ終了時刻 */
	public final static String BATCHLOG_ETIME = "etime";

	/** 処理時間 (秒) */
	public final static String BATCHLOG_PERIOD = "period";

	/** プロジェクト識別子名 */
	public final static String BATCHLOG_NAME = "name";

	/** 終了状態 */
	public final static String BATCHLOG_STATUS = "status";

	/** メッセージ */
	public final static String BATCHLOG_MSG = "msg";
}
