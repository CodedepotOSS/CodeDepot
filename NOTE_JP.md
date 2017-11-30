# 主なディレクトリとファイル

* data/index/conf/solrconfig.xml  
 検索エンジンのベースとなるSolrをカスタマイズするコンフィグファイル  
 修正があると、再デプロイする必要があります。

* schema.xml  
 コードをインデクスするためにfieldとfield typeを定義するファイル  
 修正があると、インデクスを作り直す必要があります。

* stopwords.txt synonyms.txt  
 インデクスと検索用のファイル、内容がなくても、ファイルが必要。

* COPYING  
 利用しているライブラリのラインセスがあります。

* db/SchemaV2.sql  
 データベース作成用SQLファイル

* lib  
 ライブラリファイルがあります。

* src/main/resource  
 リソースファイルの場所
 - application.properties  
  プロジェクト管理機能設定ファイル
 - auth.properties  
  外部HTTP認証設定ファイル
 - indexer.properties  
  検索インデックス設定ファイル
 - jdbc.properties  
  PostgreSQLデータベース設定ファイル
 - logging.properties  
  ログ出力設定ファイル

* src/main/java/jp/co/sra/codedepot/index  
 検索の索引作成パッケージ
 - CharSeparatorTokenizer.java  
  コードファイルをTokenに分割する（クローン検索用）
 - ProgramTokenizer.java  
  CharSeparatorTokenizer.javaを継承、Tokenの終わりのStringを定義。
 - IdentifierReplacementFilter.java  
  一部のIDを＄に変える(クローン検索用）
 - JavaProgramAnalyzer.java  
  Javaファイルをクローントークンに変更,検索用（クローン検索用）
 - JavaProgramAnalyzerWithOffset.java  
  Javaファイルをクローントークンに変更、オフセットをPayloadに格納。（クローン検索用）
 - MergeTokensFilter.java  
  各行のTokenを一つのTokenに統合。(クローン検索用）
 - PrettyPrint.java  
  TokenStreamを印刷する

* src/main/java/jp/co/sra/codedepot/parser  
 各種パーザーがある

* src/main/java/jp/co/sra/codedepot/search  
 クローン検索パッケージ

* src/main/java/jp/co/sra/codedepot/solr  
 インデックスをsolrインデクスデータベースに登録するパッケージ

* src/main/java/jp/co/sra/codedepot/util  
 補助的なプログラム

* src/main/java/jp/co/sra/codedepot/scm  
 プロジェクト、アカウントを管理するパッケージ

* src/main/java/jp/co/sra/codedepot/web/servlet  
 ダウンロードや検索以外のServletプログラム

* src/main/webapp//search.html  
 ウェブ検索用ユーザインターフェース。

* src/main/WEB-INF/web.xml  
 packageするときにこれを元に、build.propertiesの値を代入したweb.xmlがwarファイルに書き込まれる。


* src/main/webapp/css/images
* src/main/webapp/js/lib/themes/redmond/images
* src/main/webapp/admin/images  
 UIに使用している画像ファイルの場所

* src/main/webapp/css
* src/main/webapp/admin/css
* src/main/webapp/help/css  
 スタイルシートがある場所

* code.css  
 Javaプログラム表示用スタイルシート
* jquery-ui-1.7.1.redmond.css  
 jQueryのUIのデフォルトスタイル
* note.css  
 ノートのスタイル
* prog.css  
 検索ページとタブのスタイル
* search.css  

* src/main/webapp/error  
 - 500.html  
  500エラー発生時に表示するページ  
 - dberr.html  
  データベースエラー発生時に表示するページ

* src/main/webapp/img
* src/main/webapp/help/img  
  jQuery以外の画像ファイルの場所

* src/main/webapp/js  
 検索入出力処理用のJavaScriptファイルの場所

* src//main/java/jp/co/sra/codedepot/admin/scm
* src//main/java/jp/co/sra/codedepot/scm  
 プロジェクト、アカウント管理プログラムの場所
