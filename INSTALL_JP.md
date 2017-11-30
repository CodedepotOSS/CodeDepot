# インストール


## 推奨動作環境

* CPU: i686/x86_64 アーキテクチャ
* メモリ容量: 2GB 以上
* ディスク容量: 100GB 以上の空き領域


## 動作確認済み環境

### CentOS

OS Name: CentOS
OS Version: release 5.11 (Final)
PostgreSQL: 8.1.23  
Python: 2.7.9
Java: jdk1.7.0_76
Apache Tomcat: 7.0.82 

### ubuntu

OS Name: Ubuntu  
OS Version: 17.10 (Artful Aardvark)  
PostgreSQL: 9.6  
Python: 2.7.14  
Java: OpenJDK 1.8.0_151  
Apache Tomcat: 8  
OpenSSH: 7.5p1  
git: 2.14.1  


### Mac

OS Name: Mac OS X  
OS Version: 10.13.1  
PostgreSQL: 10.1  
Python: 2.7.10  

* JDK8 + Tomcat8  
 * Java: 1.8.0_144-b01  
 * Apache Tomcat: 8.5.23  
* JDK7 + Tomcat7  
 * Java: 1.7.0_80-b15  
 * Apache Tomcat: 7.0.82  


### ビルドツール

Apache Maven: 3.5.0


## 連携できるバージョン管理システム

Subversion 1.3 以上  
CVS  
Git  


## ダウンロード

github.com から git コマンドでクローンをダウンロードします。  

% git clone https://github.com/CodedepotOSS/CodeDepot.git  

ダウンロードすると CodeDepot ディレクトリが作成されます。


## ビルド

CodeDepotのビルドには、Apache Mavenを使用します。

% cd CodeDepot

### 設定

設定はbuild.propertiesに書きます。

build.properties.default をコピーして編集してくだい。

#### build.properties に設定する項目

DATA_ROOT ... codedepot のデータを記録するディレクトリパス  
PGPORT ... PostgreSQL が使用するポート番号  
DBNAME ... PostgreSQL で使用する codedepot 用のデータベース名  
DBUSER ... PostgreSQL で使用する codedepot 用のユーザ名  
DBPASS ... PostgreSQL で使用する codedepot 用のパスワード

DATA_ROOT に書いたディレクトリを作成してください。

% mkdir DATA_ROOT

### ビルドの実行

% mvn package

#### 生成される主なファイル

target/codedepot.war Java Web アプリケーションパッケージ  
bin/setup.sh データファイル作成スクリプト  
bin/initdb.sh データベース作成スクリプト  


## インストール

### PostgreSQL を起動

CodeDepot は PostgreSQL を使用します。

あらかじめ PostgreSQL を起動しておいてください。

PostgreSQL の設定ファイル hba.conf を編集して、build.properties で設定したDBNAMEとDBUSERに対してパスワード認証(MD5)を行うよう設定してください。
具体的には、DBNAMEがcodedepot_db、DBUSERがcodedepot_userとすると、下記を設定の冒頭に挿入してください。

```
local	codedepot_db	codedepot_user				md5
host	codedepot_db	codedepot_user	127.0.0.1/32		md5
host	codedepot_db	codedepot_user	::1/128			md5
```

### データディレクトリの作成

以下のコマンドを、tomcatユーザで実行してください。  

% sudo -u tomcat_user ./bin/setup.sh

### PostgreSQL にデータベースを作成

initdb.sh は PostgreSQL のスーパーユーザで実行してください。

% ./bin/initdb.sh

### tomcat に codedepot.war を配置

target/codedepot.war を tomcat の webapps フォルダにコピーします。

% cp target/codedepot.war /path/to/tomcat/webapps


## 利用方法

### tomcat を起動

tomcat を起動します。

### ログイン

ブラウザで次のURLを開きます。

hostname および port は tomcat の設定に応じて指定してください。

http://hostname:port/codedepot

### 管理者アカウント名とパスワード

アカウント名: admin  
パスワード: admin

ログイン後に、画面右上の admin をクリックしてパスワードを変更してください。

### プロジェクト追加

CodeDepot は、コードやドキュメントなど検索対象とするファイル群をプロジェクトごとに登録します。

プロジェクトの追加は admin アカウントで行います。

画面右上の「システム管理」をクリックします。

「プロジェクト管理」タブをクリックします。

「追加」ボタンを押します。

プロジェクト名を入力し、プロジェクトの参照方法を選んでください。

* local  
 CodeDepotがインストールされているコンピュータ上にあるプロジェクトのディレクトリパスを指定して登録します。  
 例えば、対象のコード群があるディレクトリが /home/projects/project-a に在る場合、次のように書きます。  
 
 /home/projects/project-a
 
 
* Subversion  
 Subversionリポジトリからプロジェクトを登録します。


* CVS  
 CVSリポジトリからプロジェクトを登録します。


* Git  
 Gitリポジトリからプロジェクトを登録します。  
 例えば、CodeDepot のリポジトリから登録する場合次のように書きます。

 https://github.com/CodedepotOSS/CodeDepot

「確認」ボタンを押すと、プロジェクトが登録されます。


### 検索インデックス作成・更新

登録したプロジェクトの検索インデックスを作成します。

プロジェクトの一覧の「即時実行」ボタンを押すと検索インデックスの作成が始まります。

検索インデックスの作成が成功すると、検索インデックス更新時刻をします。

プロジェクトのコードを更新した場合、「即時実行」をクリックすると、変更されたコードを検索インデックスに反映します。

### パスワード変更

画面右上のユーザ名をクリックするとパスワードを変更できます。

### アカウント登録

アカウントを登録すると複数の利用者とコード検索ができます。

詳細はマニュアルを参照してください。
