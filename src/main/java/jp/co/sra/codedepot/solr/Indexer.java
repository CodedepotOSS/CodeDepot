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
package jp.co.sra.codedepot.solr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jp.co.sra.codedepot.parser.IndexedCode;
import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.c.CPPParser;
import jp.co.sra.codedepot.parser.java.JavaParser;
import jp.co.sra.codedepot.parser.text.TextParser;
import jp.co.sra.codedepot.parser.sh.ShellParser;
import jp.co.sra.codedepot.parser.pygments.PygmentsVbnetParser;
import jp.co.sra.codedepot.parser.pygments.PygmentsCSharpParser;
import jp.co.sra.codedepot.parser.pygments.PygmentsSqlParser;
import jp.co.sra.codedepot.parser.jsp.JspParser;
import jp.co.sra.codedepot.parser.doc.DocParser;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

/**
 * Create indexes for solr server.
 *
 * @author yunwen, kawabe
 * @version $Id: Indexer.java 2342 2017-11-09 05:36:32Z fang $
 */
public class Indexer {

    /*
     * Constant Variables
     */

    // Field names used for indexing and searching.
    // These fields must be consistent with schema.xml in the conf directory.
	// They are also referenced in the cssSearch.js and cssAdvSearch.js

	/* Unique ID */
    public static final String ID = "id";
	/* Project ID */
    public static final String PID = "pid";
	/* Project Name */
    public static final String PRJ = "prj";
	/* Project Permission */
    public static final String PERMIT = "permission";
	/* License Name */
    public static final String LICENSE = "license";
	/* relative path name */
    public static final String LOCATION = "location";
    public static final String LOCATIONTXT = "locationtxt";
	/* Program Language */
    public static final String LANG = "lang";
	/* Search Unit */
    public static final String UNIT = "unit";
	/* Package Name */
    public static final String PKG = "pkg";
	/* Class Name */
    public static final String CLS = "cls";
    public static final String CLSTXT = "clstxt";
	/* Method Name */
    public static final String FDEF = "fdef";
    public static final String FDEFTXT = "fdeftxt";
	/* All of Source */
    public static final String SRC = "src";
	/* Code of Source */
    public static final String CODE = "code";
	/* Comment of Source */
    public static final String COMMENT = "comment";
	/* list of called methods */
    public static final String FCALL = "fcall";
	/* argument type for method */
    public static final String IN_TYPES = "inTypes";
	/* return type for method */
    public static final String OUT_TYPE = "outType";
	/* clone token */
    public static final String CLONETKN = "clonetkn";
	/* line number in source file */
    public static final String BEGIN = "begin";

    // Default field value.


	/* Default License Name */
    public static final String DEF_LICENSE = "unknown";
	/* Default Project Permission */
    public static final String DEF_PERMISSION = "public";
	/* Default Namespace */
    protected static final String DEF_NAMESPACE = ".GLOBALSCOPE";

    /* fields that are base64 encoded */
    public static final java.util.List<String> ENCODED_FIELDS = Arrays.asList(SRC);
    /*
     * Class Variables
     */

    public static boolean debug = false;
    private static final Logger logger;

    static {
        logger = Logger.getLogger(Indexer.class.getName());
	    // logger.addHandler(new ConsoleHandler());
    }

    /*
     * Instance Variables
     */

    private String projectId = "";			 /* Unique Id of Project */
    private String projectName = "";		 /* Name of Project */
    private String projectLicense = null;	 /* License of project */
    private String projectPermission = null; /* permission of project */
    private String projectSource = null;	 /* Source Directory of project */
	private String projectLanguage = null;	 /* parser language of project */

    private String htmlDirectory = null;	 /* html Directory */
    private String codeDirectory = null;	 /* code Directory */
    private String dataDirectory = null;	 /* data Directory */
    private String tempDirectory = null;	 /* temp Directory */
    private String serverUri = null;		 /* Uri for solr server */
    private String serverUsername = null;	 /* Username for solr server */
    private String serverPassword = null;	 /* Password for solr server */
	private String parserLanguages = null;	 /* default parser language */
    private String ignoreList = null;	 	 /* list of filename pattern to be ignored */
    private String textFiles = null;	 	 /* list of filename pattern for shell file */
    private String shellFiles = null;	 	 /* list of filename pattern for text file */
    private String docFiles = null;	 	     /* list of filename pattern for doc file */
    private String sqlFiles = null;		     /* list of filename pattern for sql file */
    private String csharpFiles = null;		 /* list of filename pattern for c# file */
    private String vbnetFiles = null;		 /* list of filename pattern for vb.net file */
    private Boolean dontSaveCopy = false;    /* flag if dont save copy */
    private int maxFileSize = 10485760; 	 /* maxium file size to parse */

    private SolrServer solrServer = null;
    private CoreContainer solrCore = null;
	private ProgramUID puid = null;
	private List<Parser> langParsers = null;

	private IndexerQueue indexerQueue = null;

    /*
     * Class Methods
     */
	public static boolean isBase64Encoded(String field) {
		return ENCODED_FIELDS.contains(field);
	}

    public static String getDefaultLicense() {
        return DEF_LICENSE;
    }

    public static String getDefaultPermission() {
        return DEF_PERMISSION;
    }

    public static String getDefaultNamespace() {
        return DEF_NAMESPACE;
    }

    public static void addLogHandler(String filename) throws Exception {
		try {
			FileHandler fh = new FileHandler(filename);
			logger.addHandler(fh);
		} catch (IOException e) {
			logger.warning("Cannot append file: " + filename);
			throw e;
		} catch (RuntimeException e) {
			logger.warning("Cannot append file: " + filename);
			throw e;
		}
    }

    public boolean checkIgnore(File file, List<String> list) {

		/* get pathname in project directory */

		File top = new File (this.projectSource);
		int len = top.getAbsolutePath().length();
		String path = file.getAbsolutePath();
		if (path.length() > len) {
			path = path.substring(len);
			path = path.replace(File.separator, "/");
		} else {
			path = "/";
		}

		/* check project root directory */

		if (path.equals("/")) {
			return false;
		}

		/*
		 * get base filename
		 */

		String basename = file.getPath();
		int pos = basename.lastIndexOf( File.separator );
		if (pos != -1 ) {
			basename = basename.substring(pos + 1);
		}

		/*
		 * ignore SCM management directory
		 */
		if (basename.equals("CVS") || basename.equals("RCS") ||
			basename.equals("SCCS") || basename.equals(".svn") ||
			basename.equals(".git") || basename.equals(".hg") ||
			basename.equals(".jazz5") || basename.equals(".metadata")) {
			return true;
		}
// Modified by wubo on 2010/09/09 for V2.1対応 End

		/*
		 * check file size
		 */

        if (file.isFile() && file.length() > this.maxFileSize) {
            return true;
        }

		/*
		 * check ingore list
		 */

		for (String s : list) {
			if (FilenameUtils.wildcardMatch(path, s)) {
				return true;
			}
			if (FilenameUtils.wildcardMatch(basename, s)) {
				return true;
			}
		}
		return false;
    }

    public static boolean checkSymlink(File file) throws IOException {
      	File canon = file;
      	if (file.getParent() != null) {
        	File parent = file.getParentFile().getCanonicalFile();
        	canon = new File(parent, file.getName());
      	}
     	return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }


	/*
	 * Constructor Method
	 */

	public Indexer() {
		this.loadProperties();
	}

	public Indexer(String projectId, String projectName, String license,
				   String permission, String sourceDirectory) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectLicense = license;
		this.projectPermission = permission;
		this.projectSource  = sourceDirectory;

		this.loadProperties();
	}

    /*
     * setter/getter
     */
    public void setProjectId (String projectId) {
		this.projectId = projectId;
    }

    public String getProjectId () {
		return this.projectId;
    }

    public void setProjectName (String projectName) {
		this.projectName = projectName;
    }

    public String getProjectName () {
		return this.projectName;
    }

    public void setProjectLicense (String licenseName) {
		this.projectLicense = licenseName;
    }

    public String getProjectLicense () {
		if (this.projectLicense != null) {
			return this.projectLicense;
		} else {
			return getDefaultLicense();
		}
    }

    public void setProjectPermission (String permission) {
		this.projectPermission = permission;
    }

    public String getProjectPermission () {
		if (this.projectPermission != null) {
			return this.projectPermission;
		} else {
			return getDefaultPermission();
		}
    }

    public void setProjectSource (String path) {
		this.projectSource = path;
    }

    public String getProjectSource () {
		return this.projectSource;
    }

    public void setProjectLanguage (String lang) {
		this.projectLanguage = lang;
    }

    public String getProjectLanguage () {
		return this.projectLanguage;
    }

    public void setCodeDirectory(String path) {
		this.codeDirectory = path;
    }

    public String getCodeDirectory() {
		return this.codeDirectory;
    }

    public void setHtmlDirectory(String path) {
		this.htmlDirectory = path;
    }

    public String getHtmlDirectory() {
		return this.htmlDirectory;
    }

    public void setDataDirectory(String path) {
		this.dataDirectory = path;
    }

    public String getDataDirectory() {
		return this.dataDirectory;
    }

    public void setTempDirectory(String path) {
		this.tempDirectory = path;
    }

    public String getTempDirectory() {
		return this.tempDirectory;
    }

    public void setServerUri(String uri) {
		this.serverUri = uri;
    }

    public String getServerUri() {
		return this.serverUri;
    }

    public void setServerUsername(String username) {
		this.serverUsername = username;
    }

    public void setServerPassword(String password) {
		this.serverPassword = password;
    }

    public void setIgnoreList(String list) {
		this.ignoreList = list;
    }

    public List<String> getIgnoreList() {
		List<String> list = new ArrayList<String>();
		if (!StringUtils.isEmpty(this.ignoreList)) {
			StringTokenizer st = new StringTokenizer(this.ignoreList);
			while(st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
		}
		return list;
    }

    public void setDebug(boolean debug) {
		this.debug = debug;
    }

    public boolean getDebug() {
		return this.debug;
    }

    /*
     * Instance Methods
     */

	public void execute(boolean doIndex, boolean doCommit,
						boolean doCopy, boolean doHtml) throws Exception {

		/* create code/html directories */
		logger.fine("create code/html directories");
		this.makeDirectories();

		/* create parser */
		logger.fine("create parser");
		List<Parser>parsers = this.getParsers();
		if (parsers == null || parsers.size() == 0) {
			throw new RuntimeException("No Lauguage Parser");
		}

		/* walk source directries */
		logger.fine("walk source directries");
		Queue<File> files = this.walkDirectory(parsers);
		if (files == null || files.size() == 0) {
			logger.info("no update file for " + getProjectName() + ".");
			return;
		}

		/*
		 * get location of files
		 */
		logger.fine("get location of files");
		List<String> locations = getLocations(files);

		/*
		 * check updated file
		 */
		logger.fine("check updated file");
		List<String> updates = this.checkUpdates(locations);
		if (updates == null || updates.size() == 0) {
			logger.info("no update file for " + getProjectName() + ".");
			return;
		}

		/*
		 * get solr server
		 */
		logger.fine("get solr server");
		SolrServer server = null;
		if (doIndex) {
			server = getSolrServer();
		}

		/*
		 * remove old index
	 	 */
		logger.fine("remove old index");
		removeIndex(server, updates);

		/*
		 * parse source files
		 */
		logger.fine("parse source files");
		List<String>updated = this.parseFiles(server, parsers, updates, doIndex, doCopy, doHtml);
		if (updated == null || updated.size() == 0) {
			logger.info("no update file for " + getProjectName() + ".");
			return;
		}


		/* commit update */
		logger.fine("commit update");
		if (doCommit) {
			commitUpdates(updated);
			this.commitSolrServer();
		} else {
			this.rollbackSolrServer();
		}

		/* close parser */
		if (updated != null && updated.size() > 0) {
			closeParsers();
		}

		/* close server */
		this.closeSolrServer();
	}

    /*
     * Private Methods
     */

    private void loadProperties() {
		String resource = "indexer.properties";
		try {
			Properties props = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream stream = loader.getResourceAsStream(resource);
			props.load(stream);
			stream.close();

			this.serverUri = props.getProperty("serverUri");
			this.htmlDirectory = props.getProperty("htmlDirectory");
			this.codeDirectory = props.getProperty("codeDirectory");
			this.dataDirectory = props.getProperty("dataDirectory");
			this.tempDirectory = props.getProperty("tempDirectory");
			this.parserLanguages = props.getProperty("parserLanguages");
			this.serverUsername = props.getProperty("serverUsename");
			this.serverPassword = props.getProperty("serverPassword");
			this.ignoreList = props.getProperty("ignoreList");
			this.shellFiles = props.getProperty("shellFiles");
			this.textFiles = props.getProperty("textFiles");
			this.docFiles = props.getProperty("docFiles");
			this.sqlFiles = props.getProperty("sqlFiles");
			this.csharpFiles = props.getProperty("csharpFiles");
			this.vbnetFiles = props.getProperty("vbnetFiles");

            String dontCopy = props.getProperty("dontSaveCopy");
		    if (!StringUtils.isEmpty(dontCopy) &&
                ("true".compareToIgnoreCase(dontCopy) == 0 ||
                 "yes".compareToIgnoreCase(dontCopy) == 0)) {
                this.dontSaveCopy = true;
            } else {
                this.dontSaveCopy = false;
            }

            String maxFileSize = props.getProperty("maxFileSize");
		    if (!StringUtils.isEmpty(maxFileSize)) {
                try {
                    this.maxFileSize = Integer.parseInt(maxFileSize);
		        } catch (NumberFormatException e) {
                    ;
                }
            }

		} catch (Exception e) {
			logger.warning("cannot load resouce: " + resource);
		}
	}

	/*
	 * ディレクトリの作成
 	 */
    protected synchronized void makeDirectories() throws Exception {
		if (this.htmlDirectory != null && this.projectId != null) {
			try {
				File dir = new File(this.htmlDirectory, this.projectId);
				if (!dir.isDirectory()) {
					dir.mkdirs();
				}
			} catch (RuntimeException e) {
				logger.warning("cannot make directory: " + this.projectId
							   + " in " + this.htmlDirectory);
				throw e;
			}
		}

		if (this.codeDirectory != null && this.projectId != null) {
			try {
				File dir = new File(this.codeDirectory, this.projectId);
				if (!dir.isDirectory()) {
					dir.mkdirs();
				}
			} catch (RuntimeException e) {
				logger.warning("cannot make directory: " + this.projectId
							   + " in " + codeDirectory);
				throw e;
			}
		}
		if (this.dataDirectory != null) {
			try {
				File dir = new File(this.dataDirectory);
				if (!dir.isDirectory()) {
					dir.mkdirs();
				}
			} catch (RuntimeException e) {
				logger.warning("cannot make directory: " + dataDirectory);
				throw e;
			}
		}
		if (this.tempDirectory != null) {
			try {
				File dir = new File(this.tempDirectory);
				if (!dir.isDirectory()) {
					dir.mkdirs();
				}
			} catch (RuntimeException e) {
				logger.warning("cannot make directory: " + tempDirectory);
				throw e;
			}
		}
    }

	/*
	 * Solr クライアントの操作
 	 */
    protected synchronized void updateSolrServer(SolrInputDocument doc) throws Exception {
        if (this.solrServer != null) {
			this.solrServer.add(doc);
		}
	}

    protected synchronized void commitSolrServer() throws Exception {
        if (this.solrServer != null) {
			this.solrServer.commit();
			this.solrServer.optimize();
		}
    }

    protected synchronized void optimizeSolrServer() throws Exception {
        if (this.solrServer != null) {
			this.solrServer.optimize();
		}
    }

    protected synchronized void rollbackSolrServer() throws Exception {
        if (this.solrServer != null) {
			this.solrServer.rollback();
		}
    }

	/*
	 * Solr クライアントの終了
 	 */
    protected synchronized void closeSolrServer() throws Exception {
        if (this.solrServer != null) {
			if (this.solrCore != null) {
				solrCore.shutdown();
				this.solrCore = null;
			}
			this.solrServer = null;
		}
    }

	/*
	 * Solr クライアントの設定
 	 */
    protected void setSolrServer(SolrServer server) {
		this.solrServer = server;
    }

	/*
	 * Solr クライアントの取得
 	 */
    protected synchronized SolrServer getSolrServer() throws Exception {
		if (this.solrServer != null) {
			return this.solrServer;
		}

		if (this.serverUri != null) {
			if (this.serverUri.startsWith("file://")) {
			    URI uri = new URI(this.serverUri);
			    File directory = new File(uri);
			    this.solrServer = getEmbeddedSolrServer(directory.getCanonicalPath());
			} else {
				this.solrServer = getHttpSolrServer(this.serverUri, this.serverUsername, this.serverPassword);
			}
		}
		return this.solrServer;
    }

	/*
	 * 組み込みの Solr クライアント
 	 */
    protected SolrServer getEmbeddedSolrServer(String solrHome) throws Exception {
		System.setProperty("solr.solr.home", solrHome);
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer = initializer.initialize();
		EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
		this.solrCore = coreContainer;
		return server;
	}

	/*
	 * リモートの Solr クライアント
 	 */
    protected SolrServer getHttpSolrServer(String url, String username, String password) throws Exception {
			CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);
			if (server != null && username != null && password != null) {
				java.net.URI uri = new java.net.URI(url);
				AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
				Credentials cred = new UsernamePasswordCredentials(username, password);
				HttpClient client = server.getHttpClient();
				client.getState().setCredentials(scope, cred);
			}
			return server;
	}

	/*
	 * ファイル識別子を作成
 	 */
	private synchronized String getUniqueId() {
		if (puid == null) {
			puid = new ProgramUID(3, 3, 6);
		}
		return "_" + puid.nextUID();
	}

	/*
	 * 言語パーサを作成
 	 */
    protected synchronized List<Parser> getParsers() {

		if (this.langParsers != null) {
			return this.langParsers;
		}

		String languages = this.getProjectLanguage();
        if (languages == null) {
            languages = this.parserLanguages;
        }

		ArrayList<String> langs = new ArrayList<String>();
		if (!StringUtils.isEmpty(languages)) {
			StringTokenizer st = new StringTokenizer(languages);
			while(st.hasMoreTokens()) {
				langs.add(st.nextToken());
			}
		}

		List <Parser> parsers = new ArrayList<Parser>();


		// add Java Parser
		if (!(langs.size() > 0 && langs.indexOf("java") < 0)) {
			parsers.add(getJavaParser());
			parsers.add(getJspParser());
		}

		// add C/C++ Parser
		if (!(langs.size() > 0 && langs.indexOf("C") < 0)) {
			parsers.add(getCppParser());
		}

		// add C# Parser
		if (!(langs.size() > 0 && langs.indexOf("csharp") < 0)) {
			parsers.add(getPygmentsCSharpParser());
		}

		// add VB.net Parser
		if (!(langs.size() > 0 && langs.indexOf("vb.net") < 0)) {
			parsers.add(getPygmentsVbnetParser());
		}

		// add SQL Parser
		if (langs.indexOf("!sql") < 0) {
			parsers.add(getPygmentsSqlParser());
		}

		// add Shell Parser
		if (langs.indexOf("!sh") < 0) {
			parsers.add(getShellParser());
		}

		// add Doc Parser
		if (langs.indexOf("!doc") < 0) {
			parsers.add(getDocParser());
		}

		// add Text Parser
		if (langs.indexOf("!text") < 0) {
			parsers.add(getTextParser());
        }

		this.langParsers = parsers;
		return this.langParsers;
	}

	/*
	 * ファイルから適切なパーサを選択
 	 */
	protected Parser findParser(List<Parser> parsers, File file) {
		for (Parser p : parsers) {
			if (p.accept(file)) {
				return p;
			}
		}
		return null;
	}

    protected synchronized void closeParsers() {
		if (this.langParsers != null) {
			for (Parser p : this.langParsers) {
				p.close();
			}
			this.langParsers = null;
		}

    }

	/*
	 * java 言語のパーサを生成
 	 */
    private Parser getJavaParser() {
		File dir = new File  (this.getDataDirectory(), "java");
		if (!dir.isDirectory()) {
			try {
				dir.mkdirs();
			} catch (Exception e) {
				logger.warning("Cannot create directory: " + dir.getPath());
			}
		}
		File globalTab = new File (dir, "globalClass.data");
		File localTab = new File (dir, "localClass-" + getProjectId() + ".data");
		return new JavaParser(globalTab, localTab);
	}

	/*
	 * C/C++ 言語のパーサを生成
 	 */
    private Parser getCppParser() {
		File c_options = new File (this.projectSource, ".c_options");
		File cpp_options = new File (this.projectSource, ".cpp_options");
		return new CPPParser(c_options.getAbsolutePath(), cpp_options.getAbsolutePath(), null);
	}

	/*
	 * Text パーサを生成
 	 */
    private Parser getTextParser() {
		return new TextParser(getTokenArray(this.textFiles));
	}

	/*
	 * Doc パーサを生成
 	 */
    private Parser getDocParser() {
		return new DocParser(getTokenArray(this.docFiles));
	}

	/*
	 * Shell パーサを生成
 	 */
    private Parser getShellParser() {
		return new ShellParser(getTokenArray(this.shellFiles));
	}

	/*
	 * C# パーサを生成
 	 */
    private Parser getPygmentsCSharpParser() {
		return new PygmentsCSharpParser(getTokenArray(this.csharpFiles));
	}

	/*
	 * VB.Net パーサを生成
 	 */
    private Parser getPygmentsVbnetParser() {
		return new PygmentsVbnetParser(getTokenArray(this.vbnetFiles));
	}

	/*
	 * SQL パーサを生成
 	 */
    private Parser getPygmentsSqlParser() {
		return new PygmentsSqlParser(getTokenArray(this.sqlFiles));
	}

	/*
	 * Jsp パーサを生成
 	 */
    private Parser getJspParser() {
		return new JspParser();
	}

    /*
     * 文字列から拡張子リストを取得する
     */
    public String[] getTokenArray(String suffix) {
        String[] result = null;
        if (!StringUtils.isEmpty(suffix)) {
            List<String> list = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(suffix);
            while(st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
            result = list.toArray(new String[list.size()]);
        }
        return result;
    }


	/*
	 * ファイル一覧を取得
 	 */
	protected Queue<File> walkDirectory (List<Parser>parsers) throws Exception {

		Queue<File> dirs = new LinkedList<File>();
		Queue<File> files = new LinkedList<File>();
		List<String> ignores = getIgnoreList();

		File d = new File(this.projectSource);
		if (!dirs.offer(d)) {
			logger.warning("Unable to add queue: " + d.getAbsolutePath());
			return null;
		}

		while ((d = dirs.poll()) != null) {

			/*
			 * ignore Hidden files
			 */
			if (d.isHidden()) {
				continue;
			}

			/*
			 * ignore symbolic link
			 */
			if (checkSymlink(d)) {
				continue;
			}

			/*
			 * check ingore
			 */
			if (checkIgnore(d, ignores)) {
				logger.info("skip ignore file: " + d.getAbsolutePath());
				continue;
			}

			/*
			 * add directies or files.
			 */
			if (d.isDirectory()) {
				/* add directory */
				File[] list = d.listFiles();
				if (list != null) {
					for (File f : list) {
						if (!dirs.offer(f)) {
							logger.warning("Unable to add queue: " + f.getAbsolutePath());
							return null;
						}
					}
				}
			} else {
				/* add file */
				if (findParser(parsers, d) != null) {
					if (!files.offer(d)) {
						logger.warning("Unable to add queue: " + d.getAbsolutePath());
						return null;
					}
				} else {
					logger.fine("no parsers for: " + d.getAbsolutePath());
				}
			}
		}
		return files;
    }

	/*
	 * 相対パスを取得
 	 */
	protected List<String> getLocations(Queue<File>files) {
		if (files.size() > 0) {
			File top = new File (this.projectSource);
			int len = top.getAbsolutePath().length() + 1;
			List<String> locations = new ArrayList<String>();

			Iterator<File>i = files.iterator();
			while (i.hasNext()) {
				File f = i.next();
// Added by wubo on 2010/09/07 for V2.1対応 Start
				// locations.add(f.getAbsolutePath().substring(len));
				String path = f.getAbsolutePath().substring(len);
				path = path.replace(File.separator, "/");
				locations.add(path);
// Added by wubo on 2010/09/07 for V2.1対応 End
			}
			return locations;
		} else {
			return null;
		}
	}

	/*
	 * ファイル群を解析/登録
 	 */
    protected List<String> parseFiles(SolrServer server, List<Parser> parsers, List<String> files, boolean doIndex, boolean doCopy, boolean doHtml) throws Exception {
        if (indexerQueue != null) {
            return indexerQueue.parseFiles(server, parsers, files, doIndex, doCopy, doHtml);
        }

        // unchanged since olden times.
        File baseDir = new File(this.getProjectSource());
        List<String> plist = new LinkedList<String>();
        int dcount = 0;

        for (String location : files) {
            File file = new File(baseDir, location);
            Parser parser = findParser(parsers, file);
            if (parser == null) {
                logger.info("no paresr for: " + file.getAbsolutePath());
                continue;
            }

            int count = this.parseFile(location, server, parsers, doIndex, doCopy, doHtml);
            if (count <= 0) {
                continue;
            }

            dcount += count;
            plist.add(location);
        }

        logger.info("update " + dcount + " fragments in " + plist.size() + " files for " + getProjectName() + ".");
        return plist;
    }

	/*
	 * 特定のファイルを解析/登録。
 	 */
    protected int parseFile(String location, SolrServer server, List<Parser> parsers, boolean doIndex, boolean doCopy, boolean doHtml) throws Exception {
        int dcount = 0;
        File f = new File(this.getProjectSource(), location);

        try {
            /*
             * find parser
             */
            Parser parser = findParser(parsers, f);
            if (parser == null) {
                logger.info("no parser for: " + f.getAbsolutePath());
                return 0;
            }

            /*
             * generate unique id
             */
            String fid = getProjectId() + "#" + location;
            String uuid = this.getUniqueId();

            /*
             * parse source file
             */
            IndexedCode ic = parser.parse(fid, uuid, f);
            if (ic == null) {
                logger.info("Cannot parse file: " + f.getAbsolutePath());
                return 0;
            }

            /*
             * register index to solr
             */
            // fcount += 1;
            dcount += registIndex(server, location, ic);

            /*
             * generate html
             */
            if (doHtml) {
                String html = parser.toHtml(ic);
                writeHtml(location, html);
            }

            /*
             * copy source file
             */
            if (doCopy && !dontSaveCopy) {
                makeCopy(location, f);
            }

            // plist.add(location);

        } catch (IOException e) {
            logger.info("cannot update file: " + f.getAbsolutePath());
            e.printStackTrace();
        } catch (RuntimeException e) {
            logger.info("cannot update file: " + f.getAbsolutePath());
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            logger.info("cannot update file: " + f.getAbsolutePath());
            e.printStackTrace();
        } catch (StackOverflowError e) {
            logger.info("cannot update file: " + f.getAbsolutePath());
            e.printStackTrace();
        }

        return dcount;
    }

	/*
	 * 検索インデックスを登録
 	 */
    private int registIndex(SolrServer server, String location, IndexedCode ic) throws Exception {

		int count = 0;

		SolrInputDocument doc = new SolrInputDocument();

		/*
		 * regist this node.
		 */
		Map<String, String>map = ic.getFields();

		if (map != null && map.size() > 0) {

			/* copy properties from IndexedCode */
			for(Map.Entry<String, String> e : map.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if (k != null && v != null) {
					if (isBase64Encoded(k)) {
						byte[] bs = v.getBytes();
						/* add byte[] directly for performance gain.
						//We can convert the byte array to string as follows, but solr will loop
						//over the string once again when it calls ClientUtils.writeXML
						String encodedString = org.apache.solr.common.util.Base64.byteArrayToBase64(bs, 0, bs.length);
						doc.addField(k, encodedString);
						*/
						doc.addField(k, bs);
					} else {
						doc.addField(k, v);
					}
				}
			}

			doc.setField(LOCATION, location);
			doc.setField(PID, this.getProjectId());
			doc.setField(PRJ, this.getProjectName());
			doc.setField(PERMIT, this.getProjectPermission());
			doc.setField(LICENSE, this.getProjectLicense());

			if (debug) {
				/* debuging */
				String text = doc.toString();
				System.out.println(text);
			}

			/* regist document to solr server */

			if (server != null) {
				updateSolrServer(doc);
			}
			count++;
		}

		/*
		 * regist child node.
		 */
		List <IndexedCode> children = ic.getChildren();
		if (children != null) {
			for (IndexedCode c : children) {
				count += registIndex(server, location, c);	// recursive call
			}
		}

		/*
		 * return number of registered document
		 */
		return count;
    }

	/*
	 * 表示用HTML ファイルを作成
 	 */
    private void writeHtml(String filename, String html) throws Exception {
		if (this.htmlDirectory != null && projectId != null) {
			File parent = new File(this.htmlDirectory, this.projectId);
            File htmlFile = new File(parent, filename + ".html");
            try {
                htmlFile.getParentFile().mkdirs();
                htmlFile.createNewFile();
            } catch (Exception e) {
				logger.warning("Unable to create html: " + htmlFile.getAbsolutePath());
				throw e;
			}

			try {
				FileOutputStream stream = new FileOutputStream(htmlFile);
				OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
				writer.write(html);
				writer.close();
			} catch (SecurityException e) {
				logger.warning("Unable to write html: " + htmlFile.getAbsolutePath());
				throw e;
			} catch (IOException e) {
				logger.warning("Unable to write html: " + htmlFile.getAbsolutePath());
				throw e;
			}
        }
    }

	/*
	 * コードのコピーを作成
 	 */
	private void makeCopy(String filename, java.io.File file) throws Exception {
		if (this.codeDirectory != null && projectId != null) {
			File parent = new File(this.codeDirectory, this.projectId);
            File copyFile = new File(parent, filename);
            try {
                copyFile.getParentFile().mkdirs();
                copyFile.createNewFile();
				FileUtils.copyFile(file, copyFile);
            } catch (Exception e) {
				logger.warning("Unable to copy file: " + copyFile.getAbsolutePath());
				throw e;
			}
        }
    }

	/*
	 * コードのコピーを削除
 	 */
	private void removeCopy(String[] locations) {
		if (this.codeDirectory != null && projectId != null && locations != null) {
			File parent = new File(this.codeDirectory, this.projectId);
			for (String f : locations) {
           		File copyFile = new File(parent, f);
				if (copyFile.exists()) {
					copyFile.delete();
				}
			}
		}
	}

	/*
	 * プロジェクトの全ての検索インデックスを削除
 	 */
	protected void clearIndex(SolrServer server) throws Exception {
		if (projectId != null) {
			String query = "pid:" + ClientUtils.escapeQueryChars(projectId);

			if (debug) {
				/* debuging */
				System.out.println("delete: " + query);
			}

			/* remove all document from solr server */

			if (server != null) {
				server.deleteByQuery(query);
			}
		}
	}

	/*
	 * プロジェクト内の特定のファイルの検索インデックスを削除
 	 */
	protected void removeIndex(SolrServer server, String location) throws Exception {
		List<String> locations = new ArrayList<String>();
		locations.add(location);
		removeIndex(server, locations);
	}

	/*
	 * プロジェクト内の特定のファイル群の検索インデックスを削除
 	 */
	protected void removeIndex(SolrServer server, List<String> locations) throws Exception {

		if (projectId != null && locations != null) {

			/* TODO: This value should be less then maxBooleanClauses in solrconfig.xml */
			int maxClause = 100;

			for (int i = 0; i < locations.size(); i = i + maxClause) {
				int end = ((i + maxClause) < locations.size()) ? (i + maxClause) : locations.size();
				List<String> sub = locations.subList(i, end);

				StringBuilder sb = new StringBuilder();
				for (String f : sub) {
					if (sb.length() > 0) {
			    		sb.append(" OR ");
					}
			    	sb.append(ClientUtils.escapeQueryChars(f));
				}

				String query = "+pid:" + ClientUtils.escapeQueryChars(projectId) +
					" +location:(" + sb.toString() + ")";

				if (debug) {
					/* debuging */
					System.out.println("delete: " + query);
				}

				/* remove document from solr server */

				if (server != null) {
					server.deleteByQuery(query);
				}
			}
		}
	}

	/*
	 * 更新ファイルの取得
 	 */
	private List<String> checkUpdates (List<String> files) throws Exception {
		return files;
	}

	/*
	 * 更新ファイルの登録
 	 */
	private void commitUpdates (List<String> files) throws Exception {
	}

	/**
	 * Create an indexer queue.
	 *
	 * @param numberOfWorkers int
	 */
	protected void createQueue(int numberOfWorkers) {
	    assert numberOfWorkers >= 2;
	    indexerQueue = new IndexerQueue(this, numberOfWorkers);
	}

    /*
     * main method
     */
    public static void main(String[] args) {

		/*
		 * get option setting.
		 */
		Options options = new Options();
		options.addOption("s", "server", true, "specify CodeDepot_server_url");
		options.addOption("l", "log", true, "specify logfilename");
		options.addOption("P", "projectId", true, "specify project id, must be unique all the time");
		options.addOption("N", "projectName", true, "specify project name");
		options.addOption("L", "license", true, "specify license name");
		options.addOption("S", "src", true, "specify source directory name");
		options.addOption("D", "data", true, "specify data directory name");
		options.addOption("T", "temp", true, "specify temp directory name");
		options.addOption("C", "code", true, "specify code directory name");
		options.addOption("H", "html", true, "specify html directory name");
		options.addOption("n", "noindex", false, "don't generate index; generate html files only");
		options.addOption("c", "commit", false, "commit to index at end");
		options.addOption("t", "lang", true, "programming language.");
        options.addOption("d", "debug", false, "toggle debug flag");
        options.addOption("w", "workers", true, "specify number of workers");

		/*
		 * parse command option.
		 */
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter help = new HelpFormatter();
			help.printHelp("Indexer", options, true);
			System.exit(1);
		}

		/* create application instance */

		Indexer indexer = new Indexer ();

		/*
		 * extract command option.
		 */
		Boolean doIndex = true;			/* update solr index */
    	Boolean doCommit = false;		/* commit solr index */
    	Boolean doCopy = false;			/* copy source code */
    	Boolean doHtml = false;			/* generate html */

		if (cmd.hasOption("s")) {
			String v = cmd.getOptionValue("s");
			indexer.setServerUri(v);
		}
		if (cmd.hasOption("P")) {
			String v = cmd.getOptionValue("P");
			indexer.setProjectId(v);
		}
		if (cmd.hasOption("N")) {
			String v = cmd.getOptionValue("N");
			indexer.setProjectName(v);
		}
		if (cmd.hasOption("L")) {
			String v = cmd.getOptionValue("L");
			indexer.setProjectLicense(v);
		}
		if (cmd.hasOption("S")) {
			String v = cmd.getOptionValue("S");
			indexer.setProjectSource(v);
		}
		if (cmd.hasOption("D")) {
			String v = cmd.getOptionValue("D");
			indexer.setDataDirectory(v);
		}
		if (cmd.hasOption("T")) {
			String v = cmd.getOptionValue("T");
			indexer.setTempDirectory(v);
		}
		if (cmd.hasOption("C")) {
			String v = cmd.getOptionValue("C");
			indexer.setCodeDirectory(v);
			doCopy = true;
		}
		if (cmd.hasOption("H")) {
			String v = cmd.getOptionValue("H");
			indexer.setHtmlDirectory(v);
			doHtml = true;
		}
		if (cmd.hasOption("t")) {
			String v = cmd.getOptionValue("T");
			indexer.setProjectLanguage(v);
		}

		if (cmd.hasOption("n")) {
			doIndex = !doIndex;
		}
		if (cmd.hasOption("c")) {
			doCommit = !doCommit;
		}
		if (cmd.hasOption("d")) {
			debug = !debug;
		}

		if (cmd.hasOption("l")) {
			String v = cmd.getOptionValue("l");
			try {
				addLogHandler(v);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		}

		if (cmd.hasOption('w')) {
		    String value = cmd.getOptionValue('w');
		    try {
		        int numberOfWorkers = Integer.parseInt(value);
		        if (numberOfWorkers > 1) {
		            indexer.createQueue(numberOfWorkers);
		        } else if (numberOfWorkers == 1) {
		            // do indexing without workers.
		        } else if (numberOfWorkers <= 0) {
                    logger.warning("The number of workers should be greater than zero.");
		        }
		    } catch (NumberFormatException e) {
		        logger.warning("The number of workers is specified, but failed to parse - " + value);
		    }
		}

		/* execute indexer */

		try {
			indexer.execute(doIndex, doCommit, doCopy, doHtml);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
    }
}

/* vim:set tabstop=4: */
