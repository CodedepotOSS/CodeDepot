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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.exception.LicenseNotExistException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.CheckUtil;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.scm.dao.ProjectDao;
import jp.co.sra.codedepot.scm.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.scm.entity.BatchLog;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.Key;

import sun.misc.BASE64Decoder;

public class Scm implements Job {
	private static final Logger logger;
	static {
        logger = Logger.getLogger(Scm.class.getName());
    }
	private static Map<Key, Integer> running = new ConcurrentHashMap<Key, Integer>();
	/**
	 * バッチ開始時刻
	 */
	private Date stime = null;
	/**
	 * バッチ終了時刻
	 */
	private Date etime = null;
	/**
	 * テンポラリーディレクトリ
	 */
	private String tmpDir = null;
	/**
	 * プロジェクト情報
	 */
	private ProjectInfoEntity project = null;

	static {
		try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
			MessageUtil.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_MSG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isRunning(String title, String group) {
		return running.containsKey(new Key(title, group));
	}

	public void execute(JobExecutionContext context) {
		JobDetail jd = context.getJobDetail();

		synchronized (running) {
			if (null != running.get(jd.getKey())) {
				logger.info("同名のジョブが実行中です。");
				return ;
			}
			running.put(jd.getKey(), new Integer(1));
		}

		String projectTitle = jd.getJobDataMap().getString(APConst.PROJECT_TITLE);

		// バッチ開始時刻を記録する
		stime = new Date();
		UpdSrchIndex indexer = null;

		logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_01,
				projectTitle, "開始"));
		try{
			action(projectTitle);

			// 検索インデックスの差分更新処理
			indexer = new UpdSrchIndex(
					project,
					tmpDir,
					stime);

			try{
				logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02, projectTitle, "開始"));
				indexer.execute(true, true, true, true);
				logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02, projectTitle, "正常終了"));
			}catch(Exception e){
				logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_02,
						projectTitle, "異常終了"));
				throw e;
			}
			logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_01,
					projectTitle, "正常終了"));
		}catch(ScmException e){
			if(ScmException.WARN == e.getLevel()){
				logger.warning(e.getMessage());
			}else if(ScmException.INFO == e.getLevel()){
				logger.info(e.getMessage());
			}else if(ScmException.ERROR == e.getLevel()){
				logger.fine(ScmException.getStackTraceStr(e));
				logger.severe(BaseException.getStackTraceStr(e));
			}
			logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_01,
					projectTitle, "異常終了"));
			// バッチ終了時刻を記録する
			etime = new Date();
			int period = (int)(etime.getTime() - stime.getTime()) / 1000;
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
		}catch(Exception ex){
			logger.severe(ScmException.getStackTraceStr(ex));
			logger.severe(ex.getMessage());
			logger.info(MessageUtil.getMessageString(APMsgConst.I_SCM_01,
					projectTitle , "異常終了"));
			// バッチ終了時刻を記録する
			etime = new Date();
			int period = (int)(etime.getTime() - stime.getTime()) / 1000;
			BatchLog batchlog = new BatchLog();
			batchlog.setEtime(etime);
			batchlog.setMsg(ex.getMessage());
			batchlog.setPeriod(period);
			if(null != project){
				batchlog.setProject(project.getName());
			}
			batchlog.setStatus(false);
			batchlog.setStime(stime);
			ScmUtil.insertBatchLog(batchlog);
		} finally {
			running.remove(jd.getKey());

			if (null != jd.getJobDataMap().get("shutdown") && (Boolean)jd.getJobDataMap().get("shutdown")) {
				try {
					context.getScheduler().shutdown();
				} catch (SchedulerException e) {
					logger.fine(BaseException.getStackTraceStr(e));
				}
			}
		}
	}

	/**
	 * SCM連携処理を行う
	 *
	 * @param projectName プロジェクトの題名
	 */
	public void action(String projectName) throws ScmException,UnsupportedEncodingException{
		// プロジェクト管理情報の取得
		logger.fine("プロジェクト管理情報の取得が開始しました");
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

		// プロジェクト情報解析/チェック
		logger.fine("プロジェクト管理情報の解析/チェックが開始しました");
		projectParseCheck();

		if(APConst.TYPE_LOCAL.equals(project.getSrc_type())) {
			tmpDir = project.getSrc_path();
		}
		// src_typeが"cvs"或は"svn"或は"jazz"の場合
		else {
			// テンポラリーディレクトリの生成
			logger.fine("テンポラリーディレクトリの生成が開始しました");
			tempDirMake();

			VersionManage versionManage = SCMFactory.getInstance(project.getSrc_type());
			// チェックアウト/アップデート判断
			if (versionManage.isCheckout(project, tmpDir)) {
				logger.fine("チェックアウトの場合");
				// テンポラリディレクトリからファイルを削除する
				logger.fine("テンポラリーディレクトリのクリアが開始しました");
				tempDirClear();
				// バージョン管理リポジトリへの接続
				logger.fine("バージョン管理リポジトリへの接続を開始しました");
				versionManage.connect(project);
				// チェックアウト
				logger.info("チェックアウト処理を開始しました");
				versionManage.checkout(project, tmpDir);
			} else {
				logger.fine("アップデートの場合");
				// バージョン管理リポジトリへの接続
				logger.fine("バージョン管理リポジトリへの接続が開始しました");
				versionManage.connect(project);
				// アップデート
				logger.info("アップデート処理が開始しました");
				versionManage.update(project, tmpDir);
			}
		}
	}

	/**
	 * プロジェクト管理情報を解析/チェックする
	 *
	 * @throws ScmException 処理異常
	 */
	public void projectParseCheck()throws ScmException,UnsupportedEncodingException {
		logger.fine("src_type:" + project.getSrc_type());
		logger.fine("src_path:" + project.getSrc_path());
		// src_typeのチェック
// Modified by wubo on 2010/08/30 for V2.1対応 Start
//		if (!APConst.TYPE_LOCAL.equals(project.getSrc_type())
//				&& !APConst.TYPE_CVS.equals(project.getSrc_type())
//				&& !APConst.TYPE_SVN.equals(project.getSrc_type())) {
//			throw new ScmException(new Message(APMsgConst.W_SCM_01, project.getTitle()), ScmException.WARN);
//		}
		// src_typeのチェック
		if (!APConst.TYPE_LOCAL.equals(project.getSrc_type())
				&& !APConst.TYPE_CVS.equals(project.getSrc_type())
				&& !APConst.TYPE_SVN.equals(project.getSrc_type())
				&& !APConst.TYPE_GIT.equals(project.getSrc_type())
				&& !APConst.TYPE_JAZZ.equals(project.getSrc_type())) {
			throw new ScmException(new Message(APMsgConst.W_SCM_01, project.getTitle()), ScmException.WARN);
		}
// Modified by wubo on 2010/08/30 for V2.1対応 End
		// src_pathの解析
		if (APConst.TYPE_LOCAL.equals(project.getSrc_type())) {
			if (!CheckUtil.isLocalPath(project.getSrc_path())) {
				throw new ScmException(new Message(APMsgConst.W_SCM_02, project.getTitle()), ScmException.WARN);
			}
		} else if (APConst.TYPE_CVS.equals(project.getSrc_type())) {
			if (!CheckUtil.isCVSPath(project.getSrc_path())) {
				throw new ScmException(new Message(APMsgConst.W_SCM_03, project.getTitle(), project.getSrc_path()), ScmException.WARN);
			}
// Modified by wubo on 2010/08/30 for V2.1対応 Start
//		} else {
//			if (!CheckUtil.isSVNPath(project.getSrc_path())) {
//				throw new ScmException(new Message(APMsgConst.W_SCM_03, project.getTitle(), project.getSrc_path()), ScmException.WARN);
//			}
//		}
		} else if (APConst.TYPE_SVN.equals(project.getSrc_type())) {
			if (!CheckUtil.isSVNPath(project.getSrc_path())) {
				throw new ScmException(new Message(APMsgConst.W_SCM_03, project.getTitle(), project.getSrc_path()), ScmException.WARN);
			}
		} else if (APConst.TYPE_GIT.equals(project.getSrc_type())) {
			if (!CheckUtil.isGITPath(project.getSrc_path())) {
				throw new ScmException(new Message(APMsgConst.W_SCM_03, project.getTitle(), project.getSrc_path()), ScmException.WARN);
			}
		} else if (APConst.TYPE_JAZZ.equals(project.getSrc_type())) {
			if (!CheckUtil.isJAZZPath(project.getSrc_path())) {
				throw new ScmException(new Message(APMsgConst.W_SCM_03, project.getTitle(), project.getSrc_path()), ScmException.WARN);
			}
		}
// Modified by wubo on 2010/08/30 for V2.1対応 End
		try{
			// scm_passのデコ窶買h
			String pass = project.getScm_pass();
			if (null != pass && !"".equals(pass)) {
				BASE64Decoder decoder = new BASE64Decoder();
				project.setScm_pass(new String(decoder.decodeBuffer(pass)));
			}

		}catch(UnsupportedEncodingException e) {
			throw e;
		}catch(IOException ioe){
			throw new ScmException(new Message(APMsgConst.E_SCM_01), ioe, ScmException.ERROR);
		}

	}

	/**
	 * テンポラリーディレクトリの生成
	 *
	 * @throws ScmException 処理異常
	 */
	public void tempDirMake() throws ScmException{
		tmpDir = ScmUtil.getSourceDirectory(project.getName());
		System.err.println("tmpDir:" + this.tmpDir);
		File dir = new File(tmpDir);
		if(dir.exists()){
			return;
		}
		if(!dir.mkdirs()){
			throw new ScmException(new Message(APMsgConst.E_SCM_02, tmpDir),
					new Throwable(MessageUtil.getMessageString(APMsgConst.E_SCM_02, tmpDir)), ScmException.ERROR);
		}
	}
	/**
	 * テンポラリーディレクトリをクリアする
	 *
	 * @throws ScmException 処理異常
	 */
	public void tempDirClear()throws ScmException{
		try{
			File f = new File(tmpDir);
			Stack<File> s = new Stack<File>();
			File[] delFiles = f.listFiles();
			for(int i = 0; i < delFiles.length; i++){
				s.push(delFiles[i]);
			}
			while(!s.isEmpty()){
				File file = s.pop();
				if(file.isFile() ||
					(file.isDirectory() && file.listFiles().length == 0)){
					if(!file.delete()){
						throw new Exception("");
					}
				}else{
					File[] delFile = file.listFiles();
					if (delFile != null) {
						s.push(file);
						for(int i = 0; i < delFile.length; i++){
							s.push(delFile[i]);
						}
					}
				}

			}
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), e, ScmException.ERROR);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 1){
			return;
		}
		String projectTitle = args[0];
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();
		JobDetail job = new JobDetail(projectTitle, APConst.SIMPLE_GROUP, Scm.class);
		job.getJobDataMap().put(APConst.PROJECT_TITLE, projectTitle);
		job.getJobDataMap().put("shutdown", true);
		SimpleTrigger trigger = new SimpleTrigger("trigger1", "group1");
		sched.scheduleJob(job, trigger);
		sched.start();
	}
}

/* vim: set tabstop=4: */
