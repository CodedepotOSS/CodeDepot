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
package jp.co.sra.codedepot.parser.c;

import java.io.* ;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Logger;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;

import jp.co.sra.codedepot.search.HTMLConvert;
import jp.co.sra.codedepot.util.LineRead ;

import jp.co.sra.codedepot.solr.Indexer;
import jp.co.sra.codedepot.parser.c.IndexedCodeFile ;
import jp.co.sra.codedepot.parser.c.CPPTokenParser;

import jp.co.sra.codedepot.util.c.CDTUtils ;
import jp.co.sra.codedepot.util.c.MiscUtils ;
import jp.co.sra.codedepot.util.c.CommentInfo ;
import jp.co.sra.codedepot.util.c.DeclarationKinds ;
import jp.co.sra.codedepot.util.c.INameKinds ;
import jp.co.sra.codedepot.util.c.HTMLTagInfo ;


public class CCPP2HTML {

    private static Logger logger = Logger.getLogger(CCPP2HTML.class.getName());

    private IndexedCodeFile _icf ; /** IndexedCodeFile */
    private IASTName[] _names ; /* indetifiers */

    /**
     * コンストラクタ
     *
     */
    public CCPP2HTML(IndexedCodeFile icf){
        this._icf = icf ;
        this._names = icf.getIdentifiers();
    }

    public CCPP2HTML(IndexedCodeFile icf, IASTName[] names){
        this._icf = icf ;
        this._names = names;
    }

    /**
     * HTMLファイルを取得する
     *
     */
    public PrintWriter getHtmlFile(String htmlDir, String fileName) throws IOException {
		String htmlFileName = fileName + ".html";
        File htmlFile = new File(htmlDir, htmlFileName);
        try {
            htmlFile.getParentFile().mkdirs();
            htmlFile.createNewFile();
        } catch (IOException e) {
            logger.info(e.toString());
            throw e;
        }
        return new PrintWriter(new FileWriter(htmlFile));
    }

    /**
     *InnterHTML出力用文字列作成
     *
     */
    public String toHTML(String uuid) {

        StringBuilder html = new StringBuilder();

		char []programText = _icf.getProgramText();
        LineRead rct = new LineRead(programText) ;
        List<String> srclst = rct.getSrcList();

        // 1.行番号ペインを作成
        //
		html.append("<div id=\"ln");
		html.append(uuid);
		html.append("\" class=\"lnPane\">\n");
        html.append("<pre>\n");
        int lines = srclst.size();
        for (int i = 1; i <= lines; i++) {
            html.append("<span id=\"ln");
            html.append(i);
            html.append(uuid);
            html.append("\">");
            html.append(i);
            html.append("</span>\n");
        }
        html.append("</pre>\n");
        html.append("</div>\n");

        // 2.コードペインを作成
        //

		html.append("<div id=\"lc");
		html.append(uuid);
		html.append("\" class=\"codePane\">\n");
        html.append("<pre>\n");

        // identifier の HashMapを作成 <key=LineNo,value=ArrayList(IASTName)>
        HashMap<Integer, ArrayList> identMap = repackIdentifierMap() ;

        // clsdef,fdefに対応する閉じタグのHashMap <key=LineNo,value=Integer (個数)>
        HashMap<Integer, Integer> closeMap = new HashMap<Integer,Integer>() ;

        int total_offset = 0 ;

        // コメントのHashMapを作成 <key=LineNo,value=ArrayList(開始カラム,終了カラム)>
        HashMap<Integer, ArrayList> cmtMap = repackCommentMap() ;

        // ハイライト対象のkeyword 配列を作成(tokenリスト取得)

        IToken[] allTokens = _icf.getTokens() ;
        IToken[] tokens ;
        int tcount = 0 ;

        if ( allTokens == null ){
            // FIXME  CDTのTokenParser.tokenParse() で exception発生
            tokens = new IToken[0] ;
        }
        else{
            // ハイライト対象のkeyword 配列を作成(イライト対象のkeywordのみに絞る)
            tokens = getKeywordToken( allTokens ) ;
        }

        /* identifer,コメント,keywordのHTMLTagInfoのmapを作成する
         * <key=LineNo,value=HTML>
         */
        HashMap<Integer,HTMLTagInfo[]> tagMap = new HashMap<Integer,HTMLTagInfo[]>() ;

        for (int i = 1; i <= lines ; i++){
            // コードを取得
            String srcLine = srclst.get(i-1).toString() ;
            Integer lineNo = new Integer(i);

            HTMLTagInfo tInfo ;
            ArrayList<HTMLTagInfo> lineTag = new ArrayList() ;

            // コメント情報のHTMLTagInfoを作成
            if( cmtMap.containsKey(lineNo) ){
                ArrayList<int[]> cmt_offsets = cmtMap.get(lineNo) ;
                for ( int j = 0 ; j < cmt_offsets.size() ; j++ ){
                    int[] of = cmt_offsets.get(j) ;
                    int offset = of[0] ;
                    int colstart ;
                    int colend ;
                    if( of[0] != 0 ){
                        colstart = of[0] - total_offset ;
                    }
                    else{ // 複数行のブロックコメントでの2行目以下の行
                        colstart = 0 ;
                    }

                    // 行末までにコメントが終了しているか
                    if( total_offset + srcLine.length() > of[1] ){
                        colend = of[1] - total_offset ;
                    }
                    else{
                        colend = srcLine.length() ;
                    }
                    //System.out.printf("===[%3d] CMT<%3d,%3d> ", i, colstart, colend);

                    tInfo = new HTMLTagInfo( HTMLTagInfo.tComment, colstart, colend, "<span class=\"cmt\">" );
                    lineTag.add( tInfo );
                    //System.out.println( tInfo );
                }
            }

            // clsdef, sfdef, clsuse, fcall のHTMLTagInfoを作成
            if( identMap.containsKey(lineNo) ){
                ArrayList<IASTName> nodes = identMap.get(lineNo) ;

                for( int j = 0 ; j < nodes.size() ; j++) {

                    IASTName n = nodes.get(j) ;
                    INameKinds s = CDTUtils.getSemanticKind(n) ;
                    DeclarationKinds k = CDTUtils.getKind(n);
                    int colstart, colend ;

                    String tag = new String() ;
                    int type = HTMLTagInfo.tInit ;

                    if( s == INameKinds.KIND_REFERENCE ){ // clsuse,fcall
                        if( k == DeclarationKinds.KIND_CLASS ||
                            k == DeclarationKinds.KIND_STRUCT ||
                            k == DeclarationKinds.KIND_UNION ||
                            k == DeclarationKinds.KIND_ENUM ){
                            type = HTMLTagInfo.tClsUse ;
                            tag = HTMLTagInfo.createClassUseTag(getSearchTagString(n));
                        }
                        else if( k == DeclarationKinds.KIND_FUNCTION ){
                            type = HTMLTagInfo.tFCal ;
                            tag = HTMLTagInfo.createFunctionCallTag(getSearchTagString(n));
                        }
                    }
                    else { // clsdef,sfdef
                        // FIXME   CDTのバグ？
                        //     declaration, definitonがともにdeclarationになっている。
                        if( k == DeclarationKinds.KIND_CLASS ||
                            k == DeclarationKinds.KIND_STRUCT ||
                            k == DeclarationKinds.KIND_UNION ||
                            k == DeclarationKinds.KIND_ENUM ){
                            type = HTMLTagInfo.tClsDef ;
                            tag = HTMLTagInfo.createClassDefTag(getSearchTagString(n));
                        }
                        else if( k == DeclarationKinds.KIND_FUNCTION ){
                            type = HTMLTagInfo.tFDef ;
                            tag = HTMLTagInfo.createFunctionDefTag(getSearchTagString(n));
                        }
                        // 宣言終了の行番号を取得して、保存する。(</span> 閉じタグ出力用)
                        int endLine = CDTUtils.getEndLine(n) ;
                        if ( closeMap.containsKey(endLine) ){
                            int count = closeMap.get(endLine) + 1 ;
                            closeMap.put( new Integer(endLine), new Integer(count) ) ;
                        }
                        else{
                            closeMap.put( new Integer(endLine), new Integer(1) ) ;
                        }
                    }
                    colstart = n.getFileLocation().getNodeOffset() - total_offset ;
                    colend = colstart + n.getFileLocation().getNodeLength();
                    if( srcLine.length() < colend ){
                        // FIXME   CDTのバグ？
                        // 関数call中に改行が入るとNodeLengthがパラメータを含めたものとなる
                        // ことがある。
                        colend = srcLine.length();
                    }

                    //System.out.printf("===[%3d] DEF<%3d,%3d> ", i, colstart, colend);

                    tInfo = new HTMLTagInfo( type, colstart, colend, tag );
                    lineTag.add( tInfo );
                    //System.out.println( tInfo );
                }
            }

            // keyword のHTMLTagInfoを作成
            if( tokens.length != 0){
                // 現在行に所属しているか？
                while( tokens[tcount].getOffset() >= total_offset &&
                       tokens[tcount].getEndOffset() <= total_offset + srcLine.length() ){
                    int colstart = tokens[tcount].getOffset() - total_offset ;
                    int colend = colstart + tokens[tcount].getLength() ;

                    tInfo = new HTMLTagInfo( HTMLTagInfo.tKeyword, colstart, colend, "<span class=\"keyword\">" );
                    lineTag.add( tInfo );
                    //System.out.printf("===[%3d] KWD<%3d,%3d> ", i, colstart, colend);
                    //System.out.println( tInfo );

                    if( tcount == tokens.length -1 ){
                        break ;
                    }
                    tcount++ ;
                }
            }

            total_offset += srcLine.length() + 1 ; // 改行コード分を+1する

            // HTMLTagInfoをソートして、tagMapに追加する
            if( ! lineTag.isEmpty() ){
                HTMLTagInfo[] tagArray = lineTag.toArray(new HTMLTagInfo[lineTag.size()]) ;
                HTMLTagInfoCompare comp = new HTMLTagInfoCompare() ;
                Arrays.sort( tagArray, comp ) ;
                tagMap.put( lineNo, tagArray );

                //System.out.printf("===[%3d]\n", i);
                //for( int x = 0 ; x < tagArray.length ; x++){
                //System.out.println( tagArray[x] ) ;
                //}
            }

            lineTag.clear() ;
        }

        // HTMLタグを追加しながらコードを出力する。
        total_offset = 0 ;
        for( int i = 1 ; i <= lines ; i ++){
            String srcLine = srclst.get(i-1).toString() ;
            Integer lineNo = new Integer(i);

            // 行頭にリンク用のタグを挿入。クラス、構造体、関数、メソッドの宣言
            if( identMap.containsKey(lineNo) ){
                ArrayList<IASTName> nodes = identMap.get(lineNo) ;
                for( int j = 0 ; j < nodes.size() ; j++) {
                    IASTName n = nodes.get(j) ;
                    INameKinds s = CDTUtils.getSemanticKind(n) ;

                    if( s == INameKinds.KIND_DECLARATION ){ // 宣言
                        DeclarationKinds k = CDTUtils.getKind(n);
                        if( k == DeclarationKinds.KIND_CLASS ||
                            k == DeclarationKinds.KIND_STRUCT ||
                            k == DeclarationKinds.KIND_UNION ||
                            k == DeclarationKinds.KIND_ENUM ){
                            html.append(HTMLTagInfo.createClassDefinitionTag(uuid, n.toString()));
                        }
                        else if( k == DeclarationKinds.KIND_FUNCTION ){
                            String fdef = getFdefString( n ) ;
			    if (fdef!=null)
				html.append(HTMLTagInfo.createMethodDefinitionTag(uuid, fdef));
                        }
                    }
                }
            }

            //  コードにタグを追加しつつ出力
            html.append(HTMLConvert.markupstmt(i, uuid));
            if( tagMap.containsKey(lineNo) ){
                HTMLTagInfo[] tags = tagMap.get(lineNo) ;

                int colm = 0 ;
                for( int j = 0 ; j < tags.length ; j++ ){
                    int scol = tags[j].getStartColumn();
                    int ecol = tags[j].getEndColumn();

                    if( colm > scol || colm > ecol ){
			// 既に出力済み。
			continue;
		    }
                    if( colm < scol ){
                        //タグまでを出力
                        html.append(HTMLConvert.convertIntoHTML(srcLine.substring(colm,scol)));
                    }
                    // タグあり出力
                    html.append( tags[j].getTag() );
                    try{
                        html.append(HTMLConvert.convertIntoHTML(srcLine.substring(scol,ecol)));
                    }
                    catch( Exception e ){
                        ;
                    }
                    html.append("</span>");
                    colm = ecol ;
                }
                if( colm < srcLine.length() ){ // 行末迄出力
                    html.append(HTMLConvert.convertIntoHTML(srcLine.substring(colm,srcLine.length())));
                }
            }
            else{ // タグなし、そのまま出力
                html.append(HTMLConvert.convertIntoHTML(srcLine)) ;
            }
            html.append("</span>");  // close for markupstmt

            // cls,fdef...タグ分の</span>を出力
            if( closeMap.containsKey(lineNo) ){
                int count = closeMap.get(lineNo) ;
                for( int j = 0 ; j < count ; j++){
                    html.append("</span>");
                }
            }
            html.append("\n");

            total_offset += srcLine.length() + 1 ; // 改行分を+1して、1行分カウントアップ
        }

        html.append("</pre>\n");
        html.append("</div>\n");

        //System.out.printf( html.toString() ) ;
        return html.toString() ;
    }


    /**
     *IndexedCodeFileで保持しているIdentifierのリストを、
     * HashMap<key=行番号,Value=ArrayList(IASTName)> に詰め直す。
     */
    private HashMap repackIdentifierMap(){

        HashMap<Integer, ArrayList> identMap ;  // key:行番号,value:IASTName
        identMap = new HashMap<Integer,ArrayList>() ;

        // HashMap<key=行番号,Value=開始カラム> に詰め直す。
        if(_names != null) {
            for(int j = 0; j < _names.length; j++) {
                DeclarationKinds k ;
                try {
                    k = CDTUtils.getKind(_names[j]);
                }
                catch( Exception e){
                    logger.info(e.toString());
                    continue;
                }
                catch (StackOverflowError se) {
                    logger.info(se.toString());
                    continue;
	        }
                if( k == DeclarationKinds.KIND_STRUCT ||
                    k == DeclarationKinds.KIND_UNION ||
                    k == DeclarationKinds.KIND_CLASS ||
                    k == DeclarationKinds.KIND_ENUM ||
                    k == DeclarationKinds.KIND_FUNCTION ){

                    if( k == DeclarationKinds.KIND_FUNCTION &&
                        _names[j].toString().contains("::")){
                        // AllIdentifierにCLASS::funcとfuncが二重に含まれている。
                        // CLASS::func は登録しない。
                        ;
                    }
                    else{
                        // hashmapに登録
                        int lno = CDTUtils.getStartLine(_names[j]) ;
                        ArrayList val = new ArrayList();
                        if( identMap.containsKey(new Integer(lno)) ){
                            val = identMap.get(new Integer(lno)) ;
                        }
                        val.add( _names[j] );
                        identMap.put(new Integer(lno), val);
                    }
                }
            }
        }
        return identMap ;
    }

    /**
     *IndexedCodeFileで保持しているCommentInfのリストからを、
     * HashMap<key=行番号,Value=ArrayList(開始カラム,終了カラム)> に詰め直す。
     */
    private HashMap repackCommentMap() {

        HashMap<Integer, ArrayList> cmtMap ;
        cmtMap = new HashMap<Integer,ArrayList>() ; // key:行番号,value:ArrayList(開始カラム,終了カラム)
        ArrayList<CommentInfo> cmtList = _icf.getCommentList() ;

        if( cmtList == null ){  // コメントなし
            return cmtMap ;
        }

        for(int i = 0 ; i < cmtList.size() ; i++){
            CommentInfo cmtInfo = cmtList.get(i) ;
            int sline = cmtInfo.getStartingLineNumber() ; // コメント開始行
            int eline = cmtInfo.getEndingLineNumber() ; // コメント終了行

            ArrayList<int[]> val = new ArrayList();
            if( cmtMap.containsKey(sline) ){
                val = cmtMap.get(sline) ;
            }
            // 開始行を追加  <行番号、ArrayList(開始カラム,終了カラム)>
            int[] of = new int[2] ;
            of[0] = cmtInfo.getOffset() ;
            of[1] = cmtInfo.getOffset() + cmtInfo.getLength() ;
            val.add(of);

            cmtMap.put(new Integer(sline), val);

            // 終了行まで追加する。
            while(eline - sline > 0){
                sline++ ;
                if( cmtMap.containsKey(sline) ){
                    val = cmtMap.get(sline);
                }
                else {
                    val = new ArrayList() ;
                }
                of = new int[2] ;
                of[0] = 0 ;
                of[1] = cmtInfo.getOffset() + cmtInfo.getLength() ;
                val.add( of ) ;
                cmtMap.put(new Integer(sline), val);
            }
        }
        return cmtMap ;
    }

    /**
     * tokenからハイライト対象のkeywordのみ絞る
     */
    IToken[] getKeywordToken( IToken[] tokens ){

        ArrayList<IToken> at = new ArrayList<IToken>() ;
        int type ;

        for( int i = 0 ; i < tokens.length ; i++){
            type = tokens[i].getType() ;
            //System.out.printf("=============== type = %d  ", type);
            //System.out.print(tokens[i]);
            //System.out.printf("(%3d,%3d)\n", tokens[i].getOffset(), tokens[i].getEndOffset());

            switch(type)
            {
            case IToken.t_asm :
            case IToken.t_auto :
            case IToken.t_bool :
            case IToken.t_break :
            case IToken.t_case :
            case IToken.t_catch :
            case IToken.t_char :
            case IToken.t_class :
            case IToken.t_const :
            case IToken.t_const_cast :
            case IToken.t_continue :
            case IToken.t_default :
            case IToken.t_delete :
            case IToken.t_do :
            case IToken.t_double :
            case IToken.t_dynamic_cast :
            case IToken.t_else :
            case IToken.t_enum :
            case IToken.t_explicit :
            case IToken.t_export :
            case IToken.t_extern :
            case IToken.t_false :
            case IToken.t_float :
            case IToken.t_for :
            case IToken.t_friend :
            case IToken.t_goto :
            case IToken.t_if :
            case IToken.t_inline :
            case IToken.t_int :
            case IToken.t_long :
            case IToken.t_mutable :
            case IToken.t_namespace :
            case IToken.t_new :
            case IToken.t_private :
            case IToken.t_protected :
            case IToken.t_public :
            case IToken.t_register :
            case IToken.t_reinterpret_cast :
            case IToken.t_return :
            case IToken.t_short :
            case IToken.t_sizeof :
            case IToken.t_static :
            case IToken.t_static_cast :
            case IToken.t_signed :
            case IToken.t_struct :
            case IToken.t_switch :
            case IToken.t_template :
            case IToken.t_this :
            case IToken.t_throw :
            case IToken.t_true :
            case IToken.t_try :
            case IToken.t_typedef :
            case IToken.t_typename :
            case IToken.t_union :
            case IToken.t_unsigned :
            case IToken.t_using :
            case IToken.t_virtual :
            case IToken.t_void :
            case IToken.t_volatile :
            case IToken.t_wchar_t :
            case IToken.t_while :

            case IToken.tXORASSIGN :
            case IToken.tXOR :
            case IToken.tAMPERASSIGN :
            case IToken.tAND :
            case IToken.tAMPER :
            case IToken.tBITORASSIGN :
            case IToken.tOR :
            case IToken.tBITOR :
            case IToken.tBITCOMPLEMENT :
            case IToken.tNOTEQUAL :
            case IToken.tNOT :

                at.add(tokens[i]);
                break ;
            default :
                // 何もしない
                break ;
            }
        }
        return at.toArray(new IToken[at.size()]) ;
    }


    // scdef, scuse, sfdef, sfcallタグ用のクラス名(メソッド名)に整形する。
    private String getSearchTagString(IASTName n){

        String name = null;

	try {
	    name = CDTUtils.getAbsoluteScopeName(n, true) ;
	} catch (Exception e) {
            logger.info(e.toString());
	    return null;
        } catch (StackOverflowError se) {
            logger.info(se.toString());
	    return null;
	}

        if( name == null ){
            return null ;
        }

        DeclarationKinds sk = CDTUtils.getScopeKind(n); // 現在のscopeを取得
        DeclarationKinds psk = CDTUtils.getParentScopeKind(n); // 親のscopeを取得

        if( sk == DeclarationKinds.KIND_CLASS &&
            psk == DeclarationKinds.KIND_NAMESPACE ){
            // name は ネームスペース名::クラス名(::メソッド名)になっている。
            // なにもしない
        }
        else {
            DeclarationKinds k = CDTUtils.getKind(n);
            if( k == DeclarationKinds.KIND_FUNCTION ){
                if( sk == DeclarationKinds.KIND_NAMESPACE ){
                    // name は ネームスペース名::メソッド名。_UKNを間に挿入。
                    int index = name.indexOf("::") ;
                    name = name.substring(0, index) + "::" +
                            HTMLTagInfo.UNKNOWN_CLASS + name.substring(index) ;
                }
                else if( sk == DeclarationKinds.KIND_CLASS ){
                    if( CDTUtils.getParentScopeKind(n) !=  DeclarationKinds.KIND_NAMESPACE){
                        // name は クラス名::メソッド名。_STDを先頭に追加。
                        name = HTMLTagInfo.UNKNOWN_NAMESPACE + "::" + name ;
                    }
                }
                else {
                    // name は メソッド名のみ _STD._UKN を先頭に追加。
                    name = HTMLTagInfo.UNKNOWN_NAMESPACE + "::" +
                           HTMLTagInfo.UNKNOWN_CLASS + "::" + name  ;
                }
            }
            else{
                if( sk != DeclarationKinds.KIND_NAMESPACE ){
                    // name は クラス名のみ。_STDを先頭に追加。
                    name = HTMLTagInfo.UNKNOWN_NAMESPACE + "::" + name ;
                }
            }
        }

        // "namespace::クラス(::メソッド)" を "namespace.クラス(#メソッド)"に変更
        int index = name.indexOf("::") ;
        String str1 = name.substring(0, index);
        String str2 = name.substring(index + 2) ;
        name = str1 + "." + str2 ;

        if( (index = name.lastIndexOf("::")) != -1 ){
            str1 = name.substring(0, index);
            str2 = name.substring(index + 2) ;
            name = str1 + "#" + str2 ;
        }
        return name ;
    }


    // fdefタグ用の関数signatureに整形する。
    private String getFdefString( IASTName n ){

        String fdef = null;
        try {
        	fdef = CDTUtils.getFunctionSignature(n) ;
        } catch (Exception e) {
        	logger.info(e.toString());
        	return null;
        } catch (StackOverflowError se) {
        	logger.info(se.toString());
        	return null;
        }

        if( fdef == null ){
            return null ;
        }

        DeclarationKinds sk = CDTUtils.getScopeKind(n); // 現在のscopeを取得
        if( sk == DeclarationKinds.KIND_NAMESPACE ){  // identifierの先頭にnamespaceが存在する
            // identifier から "namespace::" を削除
            fdef = fdef.substring( CDTUtils.getScopeName(n).length() + 2 ) ;
        }

        // クラス::メソッド を クラス#メソッドに変更
        int index = fdef.lastIndexOf("::") ;
        if(index > 0){
            String classString = fdef.substring(0, index);
            String methodString = fdef.substring(index + 2) ;
            fdef = classString + "#" + methodString ;
        }
        else { // クラスのないメソッドは
            fdef = Indexer.getDefaultNamespace() + "#" + fdef ;
        }

        return fdef ;
    }


    /*
     * HTMLTagInfoのComparetor
     *
     */
    private class HTMLTagInfoCompare implements Comparator<HTMLTagInfo> {

        public int compare(HTMLTagInfo n0, HTMLTagInfo n1) {
            int start0 = n0.getStartColumn();
            int start1 = n1.getStartColumn();

            if (start0 < start1) {
                return -1;
            } else if (start0 == start1) {
                return 0;
            } else {
                return 1;
            }
        }

        public boolean equals(HTMLTagInfo n0, HTMLTagInfo n1) {
            String toStr0 = n0.toString() ;
            String toStr1 = n1.toString() ;

            return n0.equals(n1) ;
        }

    }


}

