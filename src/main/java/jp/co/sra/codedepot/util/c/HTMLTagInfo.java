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
package jp.co.sra.codedepot.util.c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import jp.co.sra.codedepot.search.HTMLConvert;


public class HTMLTagInfo {

    /** */
    public static final String UNKNOWN_NAMESPACE = "_STD" ;

    /** */
    public static final String UNKNOWN_CLASS = "_UKN" ;

    /** 行頭のクラス宣言タグ */
    private static final String CLS_TAG = "<span class=\"cls\" id=\"" ;
    /** 行頭のメソッド宣言タグ */
    private static final String FDEF_TAG = "<span class=\"fdef\" id=\"" ;

    /** コード中の クラス定義タグ文字列 */
    private static final String CD_TAG = "<span class=\"cd\" onclick=\"scdef('" ;
    /** コード中の クラス利用タグ文字列 */
    private static final String CU_TAG = "<span class=\"cu\" onclick=\"scuse('" ;
    /** コード中の メソッド定義タグ文字列 */
    private static final String FD_TAG = "<span class=\"fd\" onclick=\"sfdef('" ;
    /** コード中の メソッド利用タグ文字列 */
    private static final String FC_TAG = "<span class=\"fc\" onclick=\"sfcall('" ;


    public static final int tInit = 0;    /** HTMLTagタイプ 未設定 */
    public static final int tComment = 1; /** HTMLTagタイプ コメント*/
    public static final int tClsDef = 2;  /** HTMLTagタイプ クラス定義 */
    public static final int tClsUse = 3;  /** HTMLTagタイプ クラス使用 */
    public static final int tFDef = 4 ;   /** HTMLTagタイプ メソッド定義 */
    public static final int tFCal = 5;    /** HTMLTagタイプ メソッド利用 */
    public static final int tKeyword = 6; /** HTMLTagタイプ 言語予約語 */

    private int _start ;
    private int _end ;

    private int _type ;
    private String _tag ;

    public HTMLTagInfo(){
        _type = HTMLTagInfo.tInit ;
        _tag = new String();
    }

    public HTMLTagInfo( int type, int start, int end, String tag){
        this._type = type ;
        this._start = start ;
        this._end = end ;
        this._tag = new String(tag);
    }

    public int getType(){
        return _type ;
    }

    public int getStartColumn(){
        return _start ;
    }

    public int getEndColumn(){
        return _end ;
    }

    public String getTag(){
        return _tag ;
    }

    public String toString(){

        String out ;

        switch (_type)
        {
        case tInit :
            out = "UNINIT" ;
            break ;
        case tComment :
            out = "COMMENT" ;
            break ;
        case tClsDef :
            out = "CLSDEF" ;
            break ;
        case tClsUse :
            out = "CLSUSE" ;
            break ;
        case tFDef :
            out = "FDEF" ;
            break ;
        case tFCal :
            out = "FCAL" ;
            break ;
        default :
            out = "" ;
            break ;
        }
        out = out + "<" + _start + "," + _end + ">" + _tag ;

        return out ;
    }

    /**
     * 行頭のclsタグ文字列を作成する。
     *
     */
    public static String createClassDefinitionTag(String fid, String clsName) {

        StringBuffer clsTag = new StringBuffer();

        clsTag.append(CLS_TAG);
        clsTag.append(HTMLConvert.convertIntoHTML(clsName));
        clsTag.append(fid) ;
        clsTag.append("\">");

        return clsTag.toString() ;
    }

    /**
     * 行頭のfdefタグ文字列を作成する。
     *
     */
    public static String createMethodDefinitionTag(String fid, String methodName) {
        StringBuffer fdefTag = new StringBuffer();

        fdefTag.append(FDEF_TAG);
        fdefTag.append(HTMLConvert.convertIntoHTML(methodName));
        fdefTag.append(fid) ;
        fdefTag.append("\">");
        return fdefTag.toString() ;
    }

    /**
     *  class宣言のタグを作成する
     *
     */
    public static String createClassDefTag( String className ){
        StringBuffer fcalTag = new StringBuffer();

        fcalTag.append(CD_TAG);
        fcalTag.append(HTMLConvert.convertIntoHTML(className));
        fcalTag.append("')\">");
        return fcalTag.toString();
    }

    /**
     *  関数宣言のタグを作成する
     *
     */
    public static String createFunctionDefTag( String functionName ){
        StringBuffer fcalTag = new StringBuffer();

        fcalTag.append(FD_TAG);
        fcalTag.append(HTMLConvert.convertIntoHTML(functionName));
        fcalTag.append("')\">");
        return fcalTag.toString();
    }

    /**
     *  class利用のタグを作成する
     *
     */
    public static String createClassUseTag( String className ){
        StringBuffer fcalTag = new StringBuffer();

        fcalTag.append(CU_TAG);
        fcalTag.append(HTMLConvert.convertIntoHTML(className));
        fcalTag.append("')\">");
        return fcalTag.toString();
    }

    /**
     *  関数callのタグを作成する
     *
     */
    public static String createFunctionCallTag( String functionName ){
        StringBuffer fcalTag = new StringBuffer();

        fcalTag.append(FC_TAG);
        fcalTag.append(HTMLConvert.convertIntoHTML(functionName));
        fcalTag.append("')\">");
        return fcalTag.toString();
    }

}

