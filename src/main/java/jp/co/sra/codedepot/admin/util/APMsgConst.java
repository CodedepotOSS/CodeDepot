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

public class APMsgConst {
	public static final String WARN = "warn";

	public static final String INFO = "info";

	public static final String ERROR = "error";

	/* 共通 */
	/** 選択された{0}を削除しても、よろしいでしょうか？ */
	public static final String C_COM_01 = "C-COM-01";
	/** {0}はご利用できます。 */
	public static final String I_COM_01 = "I-COM-01";
	/** {0}への接続が成功しました。 */
	public static final String I_COM_02 = "I-COM-02";
	/** 検索結果が存在しません。 */
	public static final String I_COM_03 = "I-COM-03";
	/** 検索文字列 {0} */
	public static final String I_COM_05 = "I-COM-05";
	/** ダウンロード プロジェクト名 {0} ファイル名 {1} */
	public static final String I_COM_06 = "I-COM-06";
	/** 参照プロジェクト名 {0} ファイル名 {1} */
	public static final String I_COM_07 = "I-COM-07";
	/** {0}開始します。 */
	public static final String I_COM_08 = "I-COM-08";
	/** {0}正常終了しました。 */
	public static final String I_COM_09 = "I-COM-09";
	/** {0}異常終了しました。 */
	public static final String I_COM_10 = "I-COM-10";
	/** src_pathで指定されたディレクトリは利用できます。 */
	public static final String I_COM_11 = "I-COM-11";
	/** 入力した{0}は既に存在しています。 */
	public static final String W_COM_01 = "W-COM-01";
	/** {0}が無効です。 */
	public static final String W_COM_02 = "W-COM-02";
	/** ライセンスは不正です。 */
	public static final String W_COM_03 = "W-COM-03";
	/** {0}を入力してください。 */
	public static final String W_COM_04 = "W-COM-04";
	/** アクセス権限がありません。 */
	public static final String W_COM_05 = "W-COM-05";
	/** {0}には{1}しか入力できません。 */
	public static final String W_COM_07 = "W-COM-07";
	/** ライセンスの有効期限を過ぎました。*/
	public static final String W_COM_08 = "W-COM-08";
	/** {0}の{1}操作が失敗しました。 */
	public static final String E_COM_01 = "E-COM-01";
	/** {0}への接続が失敗しました。 */
	public static final String E_COM_02 = "E-COM-02";
	/** プロジェクト{0}の情報が存在しません。 */
	public static final String E_COM_03 = "E-COM-03";
	/** {0}の取得処理が失敗しました。 */
	public static final String E_COM_04 = "E-COM-04";
	/** データベースエラーが発生しました。 */
	public static final String E_COM_05 = "E-COM-05";
	/** セッションエラーが発生しました。 */
	public static final String E_COM_06 = "E-COM-06";
	/** src_pathで指定されたディレクトリは存在していません。 */
	public static final String E_COM_07 = "E-COM-07";
	/** 操作対象となるデータが存在しません。他ユーザに削除される可能性があります。 */
	public static final String E_COM_08 = "E-COM-08";
	/* ユーザ管理機能 */
	/** {0}画面は三個まで開くことができません。一つの画面を閉じてください。 */
	public static final String I_MEM_02 = "I-MEM-02";
	/** {0}は必須入力項目です。 */
	public static final String W_MEM_01 = "W-MEM-01";
	/** {0}の文字は{1}バイト以下で入力してください。 */
	public static final String W_MEM_03 = "W-MEM-03";
	/** 入力したパスワードが一致しません。 */
	public static final String W_MEM_05 = "W-MEM-05";
	/** 少なくとも一件の処理対象を選択してください。 */
	public static final String W_MEM_06 = "W-MEM-06";
	/** 一件の処理対象を選択してください。 */
	public static final String W_MEM_07 = "W-MEM-07";
	/** ログイン名またはパスワードが間違っています。 */
	public static final String W_MEM_08 = "W-MEM-08";
	/** 入力した{0}は入力した{1}より大きくしなければなりません。 */
	public static final String W_MEM_09 = "W-MEM-09";
	/** {0} は {1} 文字以上のパスワードを入力して下さい。 */
	public static final String W_MEM_10 = "W-MEM-10";
	/** {0} には、{1} と同じパスワードは設定できません。 */
	public static final String W_MEM_11 = "W-MEM-11";
	/** {0} は標準書式に従った {1} を入力して下さい。 */
	public static final String W_MEM_12 = "W-MEM-12";
	/** 入力したラインTo は最大ファイル行数{0}以下で入力してください。 */
	public static final String W_MEM_13 = "W-MEM-13";
	/** {0} はディレクトリの絶対パス名を入力して下さい。*/
	public static final String W_MEM_14 = "W-MEM-14";
	/** {0} は指定した書式に従った {1} を入力して下さい。*/
	public static final String W_MEM_15 = "W-MEM-15";

	/* SCM連携機能 */
	/** {0}からファイルを取得する処理が{1}しました。 */
	public static final String I_SCM_01 = "I-SCM-01";
	/** プロジェクト{0}の検索インデックスの差分更新処理が{1}しました。 */
	public static final String I_SCM_02 = "I-SCM-02";
	/** ライセンスの検証に失敗しました。 */
	public static final String I_SCM_04 = "I-SCM-04";
	/** プロジェクト{0}情報のsrc_typeは"local"、"cvs"、"svn"のいずれでもありません。 */
	public static final String W_SCM_01 = "W-SCM-01";
	/** プロジェクト{0}情報のsrc_pathで指定されたディレクトリは存在していません。 */
	public static final String W_SCM_02 = "W-SCM-02";
	/** プロジェクト{0}情報のsrc_pathで指定された{1}は書式が正しくありません。 */
	public static final String W_SCM_03 = "W-SCM-03";
	/** プロジェクト{0}情報のscm_userはSCMのログインユーザ名を指定しなければなりません。 */
	public static final String W_SCM_04 = "W-SCM-04";
	/** バッチジョブ{0}は実行しています。 */
	public static final String W_SCM_05 = "W-SCM-05";
	/** バッチジョブ{0}は実行待ちです。 */
	public static final String W_SCM_06 = "W-SCM-06";
	/** プロジェクト管理情報テーブルから取得したパスワードの復元が失敗しました。 */
	public static final String E_SCM_01 = "E-SCM-01";
	/** ディレクトリ{0}の生成に失敗しました。 */
	public static final String E_SCM_02 = "E-SCM-02";
	/** プロジェクト{0}のファイルの{1}処理が失敗しました。 */
	public static final String E_SCM_04 = "E-SCM-04";
	/** 検索インデックスの差分更新に失敗したしました。 */
	public static final String E_SCM_08 = "E-SCM-08";
	/** プロパティファイルからライセンスを取得する処理が失敗しました。 */
	public static final String E_SCM_10 = "E-SCM-10";
	/** 設定ファイルからプロジェクトのパスを取得する処理が失敗しました。 */
	public static final String E_SCM_11 = "E-SCM-11";
	/** プロジェクト{0}の検索インデックスのクリアが失敗しました。 */
	public static final String E_SCM_12 = "E-SCM-12";
// Added by wubo on 2010/10/13 for V2.1対応 Start
	/** {0} コマンドが見つかりません。 */
	public static final String E_SCM_13 = "E-SCM-13";
// Added by wubo on 2010/10/13 for V2.1対応 End

	/* ライセンス管理機能 */
	/** ライセンスを発行する処理が{0}しました。 */
	public static final String I_LIC_01 = "I-LIC-01";
	/** 顧客コードは2バイトを超えてはいけません。 */
	public static final String W_LIC_01 = "W-LIC-01";
	/** ライセンスの形式が正しくありません。 */
	public static final String W_LIC_02 = "W-LIC-02";
	/** 有効期限の形式は正しくありません。 */
	public static final String W_LIC_03 = "W-LIC-03";
	/** ライセンスオプションは有効的なshort型の数値ではありません。 */
	public static final String W_LIC_04 = "W-LIC-04";
	/** ライセンスはまだ登録されていません。 */
	public static final String W_LIC_05 = "W-LIC-05";
	/** ライセンスの{0}が失敗しました。 */
	public static final String E_LIC_03 = "E-LIC-03";

	/* プロジェクト管理機能 */
	/** プロジェクト名が変更されていません。 */
	public static final String I_PRJ_01 = "I-PRJ-01";
	/** {0}はプロジェクトのパスとして指定できません。 */
	public static final String W_PRJ_01 = "W-PRJ-01";

}
