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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Logger;
import com.ibatis.sqlmap.client.SqlMapClient;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.scm.dao.BatchLogDao;
import jp.co.sra.codedepot.scm.dao.BatchLogDaoImpl;
import jp.co.sra.codedepot.scm.entity.BatchLog;
import jp.co.sra.codedepot.scm.sqlmap.SqlMapConfig;

public class ScmUtil {
	/**
	 * テンポラリーディレクトリの設定ファイル
	 */
	private static final String propertiesFilePath = APConst.PROPERTY_INDEXER;
	private static final Logger logger;
	static {
		logger = Logger.getLogger(ScmUtil.class.getName());
		try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
			MessageUtil.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_MSG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getSourceDirectory(String projectName)throws ScmException{
		try{
			Properties props = CommonUtil.getPropertiesContext(propertiesFilePath);
			String localPath = props.getProperty(APConst.TEMP_PATH_KEY);
			if (StringUtils.isEmpty(localPath)) {
				throw new ScmException(new Message(APMsgConst.E_SCM_02, projectName), ScmException.INFO);
			}
			localPath = localPath.trim();
			return new File(localPath, projectName).getAbsolutePath();
		}catch(IOException e){
			throw new ScmException(new Message(APMsgConst.E_SCM_11), e, ScmException.ERROR);
		}
	}
	public static String getTempDirectory()throws ScmException{
		try{
			Properties props = CommonUtil.getPropertiesContext(propertiesFilePath);
			String localPath = props.getProperty(APConst.TEMP_PATH_KEY);
			if (StringUtils.isEmpty(localPath)) {
				throw new ScmException(new Message(APMsgConst.E_SCM_11), null, ScmException.ERROR);
			}
			return localPath.trim();
		}catch(IOException e){
			throw new ScmException(new Message(APMsgConst.E_SCM_11), e, ScmException.ERROR);
		}
	}
	/**
	 * バッチ処理ログの情報をデータベースに保存する
	 * @param status　終了状態
	 * @param msg　メッセージ
	 */
	public static void insertBatchLog(BatchLog batchLog) {
		try{
			SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
			BatchLogDao dao = new BatchLogDaoImpl();
			int count = dao.getBatchLogCount();
			if(APConst.BATCHLOG_MAXNUM < count){
				dao.deleteBatchLog(sqlmap);
			}
			dao.insertBatchLog(sqlmap, batchLog);
		}catch(Exception e){
			logger.severe(MessageUtil.getMessageString(APMsgConst.E_COM_05));
		}
	}
	public static boolean directoryCanRead(String path){
		File file = new File(path);
		Queue<File> dirs = new LinkedList<File>();
		if(!dirs.offer(file)){
			return false;
		}
		while((file = dirs.poll()) != null){
			if(!file.canRead()){
				return false;
			}
			if(file.isDirectory()){
				File[] list = file.listFiles();
				if (list != null) {
					for (File f : list) {
						if (!dirs.offer(f)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	public static boolean directoryCanWrite(String path){
		File file = new File(path);
		Queue<File> dirs = new LinkedList<File>();
		if(!dirs.offer(file)){
			return false;
		}
		while((file = dirs.poll()) != null){
			if(!file.canWrite()){
				return false;
			}
			if(file.isDirectory()){
				File[] list = file.listFiles();
				if (list != null) {
					for (File f : list) {
						if (!dirs.offer(f)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

// Modified by wubo on 2010/09/13 for V2.1対応 Start
	public static String getSCMPath(String type) {

		String scmPath = "";
		try{
			if(APConst.TYPE_SVN.equals(type)){
				scmPath = APProperties.getProperty(APConst.SVN_PATH);
			}else if(APConst.TYPE_CVS.equals(type)){
				scmPath = APProperties.getProperty(APConst.CVS_PATH);
			}else if(APConst.TYPE_GIT.equals(type)){
				scmPath = APProperties.getProperty(APConst.GIT_PATH);
			}else if(APConst.TYPE_JAZZ.equals(type)){
				scmPath = APProperties.getProperty(APConst.JAZZ_PATH);
			}
		}catch(Exception   e)   {
			scmPath =  "";
		}
		if (!"".equals(scmPath) && (!scmPath.endsWith("\\") || !scmPath.endsWith("/"))) {
			scmPath =  scmPath + "/";
		}
		return scmPath;
	}
// Modified by wubo on 2010/09/13 for V2.1対応 End
}
