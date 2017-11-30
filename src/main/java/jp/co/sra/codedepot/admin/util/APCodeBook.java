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

public interface APCodeBook {

	/** 一般ユーザコード */
	public static final int ROLE_LEVEL_USER_CODE = 0;

	/** プロジェクト管理者コード */
	public static final int ROLE_LEVEL_MANAGER_CODE = 1;

	/** システム管理者コード */
	public static final int ROLE_LEVEL_SYSTEM_CODE = 2;

    /** 検索言語コード */
    public static final String AVAIL_LANGUAGE_CODE = "java C csharp vb.net";

    /** 検索言語名前 */
    public static final String AVAIL_LANGUAGE_NAME = "Java C/C++ C# VB.NET";


	/** 検索言語 Javaコード */
	public static final short  LANG_JAVA_BITS = 0x01;
	public static final String LANG_JAVA_NAME = "Java";
	public static final String LANG_JAVA_CODE = "java";

	/** 検索言語 C/C++コード */
	public static final short  LANG_C_BITS = 0x02;
	public static final String LANG_C_NAME = "C/C++";
	public static final String LANG_C_CODE = "C";

        /** 検索言語 C#コード */
	public static final short  LANG_CSHARP_BITS = 0x04;
    public static final String LANG_CSHARP_NAME = "C#";
    public static final String LANG_CSHARP_CODE = "csharp";

        /** 検索言語 VB.NETコード */
	public static final short  LANG_VBNET_BITS = 0x08;
    public static final String LANG_VBNET_NAME = "VB.NET";
    public static final String LANG_VBNET_CODE = "vb.net";

	/** バッチ処理ログ情報．終了状態:TRUE */
	public static final String STATUS_TRUE_STR = "正常";

	/** バッチ処理ログ情報．終了状態:FALSE */
	public static final String STATUS_FALSE_STR = "異常";
}
