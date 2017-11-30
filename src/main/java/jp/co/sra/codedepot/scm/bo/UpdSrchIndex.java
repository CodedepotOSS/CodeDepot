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
package jp.co.sra.codedepot.scm.bo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APCodeBook;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.scm.dao.ProjectDao;
import jp.co.sra.codedepot.scm.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.scm.dao.SourceDao;
import jp.co.sra.codedepot.scm.dao.SourceDaoImpl;
import jp.co.sra.codedepot.scm.dao.VersionDao;
import jp.co.sra.codedepot.scm.dao.VersionDaoImpl;
import jp.co.sra.codedepot.scm.entity.BatchLog;
import jp.co.sra.codedepot.scm.entity.Source;
import jp.co.sra.codedepot.scm.entity.Version;
import jp.co.sra.codedepot.scm.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.solr.Indexer;

import org.apache.solr.client.solrj.SolrServer;

import com.ibatis.sqlmap.client.SqlMapClient;

public class UpdSrchIndex extends Indexer {

	private static final Logger logger;

    static {
        logger = Logger.getLogger(UpdSrchIndex.class.getName());
    }
	static {
		try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
			MessageUtil.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_MSG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public UpdSrchIndex(String projectId) {
		super();
		this.setProjectId(projectId);
	}

	public UpdSrchIndex(ProjectInfoEntity project, String tempDir, Date stime) {
		super(project.getName(), project.getTitle(), project.getLicense(), project.getPermission(), tempDir);
		if (!StringUtils.isEmpty(project.getIgnores())){
			setIgnoreList(project.getIgnores());
		}
		this.projectMtime = project.getMtime();
		this.stime = stime;
		if(StringUtils.isEmpty(tempDir)){
			String sourcePath = "";
			if(APConst.TYPE_LOCAL.equals(project.getSrc_type())){
				sourcePath = project.getSrc_path();
			}else{
				sourcePath = new File(this.getTempDirectory(), project.getName()).getAbsolutePath();
			}
			this.setProjectSource(sourcePath);
		}else{
			this.setProjectSource(tempDir);
		}
		try {
	    		VersionDao dao = new VersionDaoImpl();
			this.version = dao.getVersion("indexer");
		} catch (SQLException e) {
			this.version = null;
		}
	}
    /*
     * Instance Methods
     */

	public void execute(boolean doIndex, boolean doCommit,
						boolean doCopy, boolean doHtml) throws Exception {
		if(null == stime){
			stime = new Date();
		}

		/* create cody/html directories */
		logger.fine("create cody/html directories");
		this.makeDirectories();

        this.setProjectLanguage(APCodeBook.AVAIL_LANGUAGE_CODE);

		/* create parser */
		logger.fine("create parser");
		List<Parser>parsers = this.getParsers();
		if (parsers == null || parsers.size() == 0) {
			throw new RuntimeException("No Lauguage Parser");
		}

		/* walk source directries */
		logger.fine("walk source directries");
		Queue<File> files = this.walkDirectory(parsers);
		List<String> locations = null;
		if (null != files && 0 != files.size()) {
			// get location of files
			locations = getLocations(files);
		}
		if (null == locations) {
			locations = new ArrayList<String>();
			logger.fine("locations size == 0");
		}

		/*
		 * check updated file
		 */
		logger.fine("check updated file");
		List<String> updates = this.checkUpdates(locations);
		List<String> updated = new ArrayList<String>();
		if (null != updates && 0 != updates.size()) {
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
			updated = this.parseFiles(server, parsers, updates, doIndex, doCopy, doHtml);
		} else {
			logger.info("no update file for " + getProjectName() + ".");
		}

		/* commit update */
		logger.fine("commit update");
		if (doCommit) {
			commitUpdates(updated);

			Date etime = new Date();
			String msg = "";
			msg += "追加ファイル数:" + insedFileList.size();
			msg += " 更新ファイル数:" + updedFileList.size();
			msg += " 削除ファイル数:" + deledFileList.size();
			int period = (int)(etime.getTime() - stime.getTime()) / 1000;
			BatchLog batchlog = new BatchLog();
			batchlog.setEtime(etime);
			batchlog.setMsg(msg);
			batchlog.setPeriod(period);
			batchlog.setProject(this.getProjectId());
			batchlog.setStatus(true);
			batchlog.setStime(stime);
			ScmUtil.insertBatchLog(batchlog);

	    		ProjectDao dao = new ProjectDaoImpl();
			String prj = this.getProjectId();
			dao.updateIndexedTime(prj, etime);

			this.commitSolrServer();
		} else {
			this.rollbackSolrServer();
		}

		/* close parser */
		if (updated != null && updated.size() > 0) {
			this.closeParsers();
		}

		/* close server */
		this.closeSolrServer();
	}
	private List<String> checkUpdates (List<String> files) throws Exception {
		List<String> updates = new ArrayList<String>();

		// ファイル一覧の取得
		SourceDao sourceDao = new SourceDaoImpl();
		List<Source> oldFileList = sourceDao.getSources(this.getProjectId());
		Iterator<Source> it = oldFileList.iterator();
		while(it.hasNext()){
			Source s = it.next();
			oldFileMap.put(s.getPath(), s);
		}
		// 差分比較
		this.differ(files);
		Iterator<Source> iIt = insFileList.iterator();
		Iterator<Source> uIt = updFileList.iterator();
		while(iIt.hasNext()){
			Source s = iIt.next();
			updates.add(s.getPath());
		}
		while(uIt.hasNext()){
			Source s = uIt.next();
			updates.add(s.getPath());
		}
		return updates;
	}

	private void commitUpdates (List<String> files) throws Exception {
		// 変更対象特定
		this.getRegisterItems(files);
		// DB登録
		logger.fine("DB登録が開始しました");
		this.dbRegister();
	}

	/**
	 * バッチ開始時刻
	 */
	private Date stime = null;
//	/**
//	 * バッチ終了時刻
//	 */
//	private Date etime = null;

	/**
	 * 旧ファイルリスト
	 */
	private Map<String, Source> oldFileMap = new HashMap<String, Source>();
	/**
	 * 新規リスト
	 */
	private ArrayList<Source> insFileList = new ArrayList<Source>();
	/**
	 * 更新リスト
	 */
	private ArrayList<Source> updFileList = new ArrayList<Source>();
	/**
	 * 削除リスト
	 */
	private ArrayList<Source> delFileList = new ArrayList<Source>();
	/**
	 * 新規済リスト
	 */
	private ArrayList<Source> insedFileList = new ArrayList<Source>();
	/**
	 * 更新済リスト
	 */
	private ArrayList<Source> updedFileList = new ArrayList<Source>();
	/**
	 * 削除済リスト
	 */
	private ArrayList<Source> deledFileList = new ArrayList<Source>();
	private Date projectMtime = null;
	/**
	 * Digestのキャッシュ
	 */
	private HashMap<String, String> digestMap = new HashMap<String, String>();
	/**
	 * Indexer のバージョン情報
	 */
	private Version version;

	/**
	 * ファイルを解析する
	 * @param file　ファイル
	 */
	private Source fileParse(String relativePath, File file)throws Exception{
		Source source = new Source();
		source.setDigest(getFileDigest(file));
		source.setMtime(new Date(file.lastModified()));
		source.setPath(relativePath);
		source.setProject(this.getProjectId());
		source.setSize(file.length());
		source.setLines(0);

		Parser parser = findParser(getParsers(), file);
		if(null != parser){
			source.setLang(parser.getLanguageName());
			if ("doc".compareTo(parser.getLanguageName()) != 0) {
				source.setLines(getFileLines(file));
			}
		}
		return source;
	}

	/**
	 * ファイルのハッシュ値を計算する
	 * @param file　ファイル
	 * @return　ファイルのハッシュ値
	 * @throws Exception 処理異常
	 */
	private String getFileDigest(File file) throws Exception {
		String filename = file.getPath();
		if (digestMap.containsKey(filename))  {
			return digestMap.get(filename);
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		digest = MessageDigest.getInstance("SHA");
		in = new FileInputStream(file);
		while (-1 != (len = in.read(buffer, 0, 1024))) {
			digest.update(buffer, 0, len);
		}
		in.close();
		BigInteger bigInt = new BigInteger(1, digest.digest());
		String result = bigInt.toString(16);

		digestMap.put(filename, result);
		return result;
	}
	/**
	 * ファイルの行数を計算する
	 * @param file　ァイル
	 * @return ファイルの行数
	 * @throws Exception　処理異常
	 */
	private long getFileLines(File file)throws Exception{
		long lines = 0;
		BufferedReader in = new BufferedReader(new FileReader(file));
		while(null != in.readLine()) lines++;
		in.close();
		return lines;
	}
	private boolean checkUpdateFile(File newFile, Source oldFile){
		/* バージョン時刻のチェック */
		if (this.version != null && oldFile.getCtime().before(version.getMtime())) {
			// add debug log
			logger.fine("checkUpdateFile: before version.mtime");
			return true;
		}

		/* 更新時刻のチェック */
		if(oldFile.getCtime().before(this.projectMtime)){
			// add debug log
			logger.fine("checkUpdateFile: before projectMtime");
			return true;
		}
		/* 更新時刻のチェック */
		if(oldFile.getMtime().before(new Date(newFile.lastModified()))){
			// add debug log
			logger.fine("checkUpdateFile: before lastModified");
			return true;
		}
		/* サイズのチェック */
		if(newFile.length() != oldFile.getSize()){
			// add debug log
			logger.fine("checkUpdateFile: length not equal");
			return true;
		}
		try{
			/* ハッシュ値のチェック */
			if(!getFileDigest(newFile).equals(oldFile.getDigest())){
				logger.fine("checkUpdateFile: digest not equal");
				return true;
			}
		}catch(Exception e){
			return false;
		}
		return false;
	}
	/**
	 * 新ファイルリストと旧ファイルリストを比較して、新規リスト、更新リストと削除リストを得る
	 */
	public void differ(List<String> files) {
		Iterator<String> oIt = oldFileMap.keySet().iterator();
		while(oIt.hasNext()){
			String k = oIt.next();
			File file = new File(this.getProjectSource(), k);
			Source s = null;
			if(files.contains(k)){
				if(this.checkUpdateFile(file, oldFileMap.get(k))){
					try{
						s = fileParse(k, file);
						updFileList.add(s);
					}catch(Exception e){
						logger.warning(file.getPath() + "の解析に失敗しました");
					}
				}
			}else{
				delFileList.add(oldFileMap.get(k));
			}
		}
		Iterator<String> nIt = files.iterator();
		while(nIt.hasNext()){
			String k = nIt.next();
			File file = new File(this.getProjectSource(), k);
			Source s = null;
			if(!oldFileMap.containsKey(k)){
				try{
					s = fileParse(k, file);
					insFileList.add(s);
				}catch(Exception e){
					logger.warning(file.getPath() + "の解析に失敗しました");
				}
			}
		}
	}

	/**
	 * 変更対象特定処理を行う
	 */
	public void getRegisterItems(List<String> updated) {
		// add debug log start
		Iterator<String> updatedIt = updated.iterator();
		while(updatedIt.hasNext()){
			logger.fine("updated(index):" + updatedIt.next());
		}
		// add debug log end
		// 新規済リストを新規リストとする
		Iterator<Source> nIt = insFileList.iterator();
		while(nIt.hasNext()){
			Source s = nIt.next();
			// add debug log start
			logger.fine("insert:" + s.getPath());
			// add debug log end
			if(updated.contains(s.getPath())){
				insedFileList.add(s);
			}
		}
		// 更新リストにある、更新済リストにないアイテム、更新リストから削除し、削除リストに追加する
		deledFileList.addAll(delFileList);
		Iterator<Source> uIt = updFileList.iterator();
		while(uIt.hasNext()){
			Source s = uIt.next();
			// add debug log start
			logger.fine("update:" + s.getPath());
			// add debug log end
			if(updated.contains(s.getPath())){
				updedFileList.add(s);
			}else{
				deledFileList.add(s);
			}
		}
		// add debug log start
		Iterator<Source> delIt = delFileList.iterator();
		while(delIt.hasNext()){
			Source s = delIt.next();
			logger.fine("delete:" + s.getPath());
		}
		Iterator<Source> insedIt = insedFileList.iterator();
		while(insedIt.hasNext()){
			Source s = insedIt.next();
			logger.fine("inserted:" + s.getPath());
		}
		Iterator<Source> updedIt = updedFileList.iterator();
		while(updedIt.hasNext()){
			Source s = updedIt.next();
			logger.fine("updated:" + s.getPath());
		}
		Iterator<Source> deledIt = deledFileList.iterator();
		while(deledIt.hasNext()){
			Source s = deledIt.next();
			logger.fine("deleted:" + s.getPath());
		}
		// add debug log end
	}
	/**
	 * DB登録処理を行う
	 * @throws SQLException DB異常
	 */
	public void dbRegister() throws ScmException, Exception {
		SqlMapClient sqlmap = null;
		try{
			SourceDao dao = new SourceDaoImpl();
			sqlmap = SqlMapConfig.getSqlMapClient();
			sqlmap.startTransaction();
			// 削除対象の処理
			for(Source source : deledFileList){
				// 「インデックスの削除」のAPIを呼び出す
				this.removeIndex(this.getSolrServer(), source.getPath());
				// ファイル情報を削除する
				dao.deleteSource(sqlmap, source);
			}
			// 新規リストの処理
			for(Source source : insedFileList){
				// ファイル情報を登録する
				dao.insertSource(sqlmap, source);
			}
			// 更新対象の処理
			for(Source source : updedFileList){
				// ファイル情報を更新する
				dao.updateSource(sqlmap, source);
			}
			sqlmap.commitTransaction();
		}catch(SQLException e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}finally{
			try{
				sqlmap.endTransaction();
			}catch(SQLException e){
				throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
			}
		}
	}

    /*
     * main method
     */

    public Date getStime() {
		return stime;
	}

	public void setStime(Date stime) {
		this.stime = stime;
	}

	public static void main(String[] args) {
		String projectName = args[0];
		UpdSrchIndex indexer = null;
		ProjectInfoEntity project = null;
		try{
	    	// プロジェクト管理情報の取得
	    	ProjectDao dao = new ProjectDaoImpl();
			try{
				project = dao.getAllProjectInfo(projectName);
			}catch(SQLException e){
				throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
			}
			if(null == project){
				throw new ScmException(new Message(APMsgConst.E_COM_03, projectName), ScmException.INFO);
			}
			if(!StringUtils.isEmpty(project.getSrc_path())){
				project.setSrc_path(URLDecoder.decode(project.getSrc_path(), APConst.ENCODE_UTF_8));
			}
			indexer = new UpdSrchIndex(project, null, null);

	    	logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02, projectName, "開始"));
	    	indexer.execute(true, true, true, true);
	    	logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02, projectName, "正常終了"));
		}catch(Exception e){
			logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02,
					projectName, "異常終了"));
			logger.severe(e.getMessage());

			Date stime = null;
			if(null == indexer){
				stime = new Date();
			}else{
				stime = indexer.getStime();
			}
			Date etime = new Date();
			int period = (int)(etime.getTime() - stime.getTime()) / APConst.FILEMAXNUM_OPTION_RATIO;
			BatchLog batchlog = new BatchLog();
			batchlog.setEtime(etime);
			batchlog.setMsg(e.getMessage());
			batchlog.setPeriod(period);
			if(null != project){
				batchlog.setProject(project.getName());
			}
			batchlog.setStatus(false);
			batchlog.setStime(stime);
			ScmUtil.insertBatchLog(batchlog);
		}
    }

	/**
	 * プロジェクトのIndexerをすべてクリアして、コミットする。
	 *
	 */
	public void clearIndexWithCommit() throws Exception {
		try {
			logger.fine("clearInder start");
			logger.fine("clearInder SolrServer" + this.getSolrServer());
			this.clearIndex(this.getSolrServer());
			this.commitSolrServer();
		} catch (Exception e) {
			this.rollbackSolrServer();
			throw e;
		} finally {
			this.closeSolrServer();
		}
	}
}
