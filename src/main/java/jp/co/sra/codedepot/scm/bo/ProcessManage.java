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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.util.CvsScramble;

public class ProcessManage {
	private static final Logger logger;
	static {
        logger = Logger.getLogger(Scm.class.getName());
    }

	public static Integer waitFor(Process p, long timeout) {
		long limitTime = timeout + System.currentTimeMillis();
		Integer status = null;
		do {
			try {
				status = p.exitValue();
				break;
			} catch (IllegalThreadStateException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException we) {
					return null;
				}
			}
		} while (System.currentTimeMillis() < limitTime);

		if (status == null) {
			p.destroy();
		}

		return status;
	}

	public static void messagePrint(Process p)throws IOException{
		InputStream stream = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line = null;
		while ((line = br.readLine()) != null) {
			//logger.fine(line);
			logger.info(line);
		}
		br.close();
	}


	/*
	 * CVS コマンドの実行
	 */

	public static void cvsExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType)throws ScmException{
		cvsExec(project, localPath, cmdType, 0);
	}

	public static void cvsExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType, int timeout) throws ScmException {
		File file = null;
		try{
// Modified by wubo on 2010/10/22 for V2.1対応 Start
//			String[] arr = project.getSrc_path().split(" ");
//			String cvsroot = arr[0];
//			String module = arr[1];
			String srcPath = project.getSrc_path().trim();
			int indexOfSpace = srcPath.indexOf(" ");
			String cvsroot = srcPath.substring(0, indexOfSpace);
			String module = srcPath.substring(indexOfSpace + 1).trim();
// Modified by wubo on 2010/10/22 for V2.1対応 End

			String username = project.getScm_user();
			String password = project.getScm_pass();
			String tempPath = ScmUtil.getTempDirectory();

			file = new File(tempPath, ".cvspass");
			cvsroot = CvsScramble.normalizeRoot(cvsroot, username);
			CvsScramble.generatePassFile(file, cvsroot, password);

			List<String> cmd = new ArrayList<String>();
// Modified by wubo on 2010/09/13 for V2.1対応 Start
			// cmd.add("cvs");
			if (CommonUtil.isWinOS()) {
				String cvsPath = ScmUtil.getSCMPath(APConst.TYPE_CVS);
				if ("".equals(cvsPath)) {
					cmd.add("cvs");
				} else {
					cmd.add(cvsPath + "cvs.exe");
				}
			} else {
				cmd.add("cvs");
			}
// Modified by wubo on 2010/09/13 for V2.1対応 End
			cmd.add("-d");
			cmd.add(cvsroot);
			if(cmdType == SCMCmdType.CONNECT){
				cmd.add("version");
			}else if(cmdType == SCMCmdType.CHECKOUT){
				cmd.add("checkout");
				cmd.add(module);
			}else if(cmdType == SCMCmdType.UPDATE){
				cmd.add("update");
// Added by wubo on 2010/09/19 for V2.1対応 Start
				cmd.add("-dP");
// Added by wubo on 2010/09/19 for V2.1対応 End
				cmd.add(module);
			}
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Map<String, String> env = pb.environment();
			env.put("CVS_PASSFILE", file.getAbsolutePath());
			if(cmdType == SCMCmdType.CHECKOUT || cmdType == SCMCmdType.UPDATE){
				pb.directory(new File(localPath));
			}
			Process p = pb.start();
			messagePrint(p);

			int exitValue = 0;
			if (timeout > 0) {
				Integer status = ProcessManage.waitFor(p, timeout);
				if (status != null) {
					exitValue = status.intValue();
				} else {
					exitValue = -1;
				}
			} else {
				exitValue = p.waitFor();
			}

			if(exitValue != 0){
				if(cmdType == SCMCmdType.CONNECT){
					throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), null, ScmException.ERROR);
				}else if(cmdType == SCMCmdType.CHECKOUT){
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), null, ScmException.ERROR);
				}else{
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), null, ScmException.ERROR);
				}
			}
		}catch(Exception e){
			if(cmdType == SCMCmdType.CONNECT){
				throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
			}else if(cmdType == SCMCmdType.CHECKOUT){
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), e, ScmException.ERROR);
			}else{
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), e, ScmException.ERROR);
			}
		}finally{
			if(null != file){
				file.delete();
			}
		}
	}

	/*
	 * SVN コマンドの実行
	 */

	public static void svnExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType)throws ScmException{
		svnExec(project, localPath, cmdType, 0);
	}

	public static void svnExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType, int timeout)throws ScmException{
		try{
			List<String> cmd = new ArrayList<String>();

// Modified by wubo on 2010/09/13 for V2.1対応 Start
			// cmd.add("svn");
			if (CommonUtil.isWinOS()) {
				String svnPath = ScmUtil.getSCMPath(APConst.TYPE_SVN);
				if ("".equals(svnPath)) {
					cmd.add("svn");
				} else {
					cmd.add(svnPath + "svn.exe");
				}
			} else {
				cmd.add("svn");
			}
// Modified by wubo on 2010/09/13 for V2.1対応 End

			if(cmdType == SCMCmdType.CONNECT){
				cmd.add("info");
			}else if(cmdType == SCMCmdType.CHECKOUT){
				cmd.add("checkout");
			}else if(cmdType == SCMCmdType.UPDATE){
				cmd.add("update");
			}
			cmd.add("--no-auth-cache");
			cmd.add("--non-interactive");
			if(SVNManage.isSvn1_6()){
				cmd.add("--trust-server-cert");
			}
			if(!StringUtils.isEmpty(project.getScm_user())){
				cmd.add("--username");
				cmd.add(project.getScm_user());
				if(!StringUtils.isEmpty(project.getScm_pass())){
					cmd.add("--password");
					cmd.add(project.getScm_pass());
				}
			}
			if(cmdType == SCMCmdType.CHECKOUT || cmdType == SCMCmdType.UPDATE){
				cmd.add("-q");
			}
			if(cmdType != SCMCmdType.UPDATE){
				cmd.add(project.getSrc_path());
			}
			if(cmdType == SCMCmdType.CHECKOUT || cmdType == SCMCmdType.UPDATE){
				cmd.add(localPath);
			}
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			messagePrint(p);

			int exitValue = 0;
			if (timeout > 0) {
				Integer status = ProcessManage.waitFor(p, timeout);
				if (status != null) {
					exitValue = status.intValue();
				} else {
					exitValue = -1;
				}
			} else {
				exitValue = p.waitFor();
			}

			if(exitValue != 0){
				if(cmdType == SCMCmdType.CONNECT){
					throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), null, ScmException.ERROR);
				}else if(cmdType == SCMCmdType.CHECKOUT){
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), null, ScmException.ERROR);
				}else{
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), null, ScmException.ERROR);
				}
			}
		}catch(Exception e){
			if(cmdType == SCMCmdType.CONNECT){
				throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
			}else if(cmdType == SCMCmdType.CHECKOUT){
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), e, ScmException.ERROR);
			}else{
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), e, ScmException.ERROR);
			}
		}
	}

	/*
	 * GIT コマンドの実行
	 */

	public static void gitExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType)throws ScmException{
		gitExec(project, localPath, cmdType, 0);
	}

	public static void gitExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType, int timeout)throws ScmException {
		try{
			List<String> cmd = new ArrayList<String>();

			String srcPath = project.getSrc_path().trim();
			String repository = srcPath;
			String branch = null;

			int indexOfSpace = srcPath.indexOf(" ");
			if (indexOfSpace > 0) {
				repository = srcPath.substring(0, indexOfSpace);
				branch = srcPath.substring(indexOfSpace + 1).trim();
                        }

			if (CommonUtil.isWinOS()) {
				String gitPath = ScmUtil.getSCMPath(APConst.TYPE_GIT);
				if ("".equals(gitPath)) {
					cmd.add("git");
				} else {
					cmd.add(gitPath + "git.exe");
				}
			} else {
				cmd.add("git");
			}

			if(cmdType == SCMCmdType.CONNECT){
				cmd.add("ls-remote");
			}else if(cmdType == SCMCmdType.CHECKOUT){
				cmd.add("clone");
			}else if(cmdType == SCMCmdType.UPDATE){
				cmd.add("pull");
			}

			if(cmdType == SCMCmdType.CONNECT){
				cmd.add("-h");
			}
			if(cmdType == SCMCmdType.CHECKOUT || cmdType == SCMCmdType.UPDATE){
				cmd.add("-q");
			}
			if(cmdType == SCMCmdType.CHECKOUT && branch != null) {
				cmd.add("-b");
				cmd.add(branch);
			}

			cmd.add(repository);

			if(cmdType == SCMCmdType.CHECKOUT) {
				cmd.add(localPath);
			}

			ProcessBuilder pb = new ProcessBuilder(cmd);
			if(cmdType == SCMCmdType.UPDATE) {
				pb.directory(new File(localPath));
			}
			pb.redirectErrorStream(true);
			Process p = pb.start();
			messagePrint(p);

			int exitValue = 0;
			if (timeout > 0) {
				Integer status = ProcessManage.waitFor(p, timeout);
				if (status != null) {
					exitValue = status.intValue();
				} else {
					exitValue = -1;
				}
			} else {
				exitValue = p.waitFor();
			}

			if(exitValue != 0){
				if(cmdType == SCMCmdType.CONNECT){
					throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), null, ScmException.ERROR);
				}else if(cmdType == SCMCmdType.CHECKOUT){
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), null, ScmException.ERROR);
				}else{
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), null, ScmException.ERROR);
				}
			}
		}catch(Exception e) {
			if(cmdType == SCMCmdType.CONNECT){
				throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
			}else if(cmdType == SCMCmdType.CHECKOUT){
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), e, ScmException.ERROR);
			}else{
				throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), e, ScmException.ERROR);
			}
		}
	}

	/*
	 * SCM コマンドの実行
	 */

	// Added by wubo on 2010/07/27 for V2.1対応 Start

	public static void jazzExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType)throws ScmException {
		jazzExec(project, localPath, cmdType, 0);
	}

	public static void jazzExec(ProjectInfoEntity project, String localPath, SCMCmdType cmdType, int timeout)throws ScmException{
		boolean isJazzExecEnable = true;
		try{

			String srcPath = project.getSrc_path().trim();
			int indexOfSpace = srcPath.indexOf(" ");
			String repositoryUri = srcPath.substring(0, indexOfSpace);
// Modified by wubo on 2010/10/18 for V2.1対応 検収バグNo.4 Start
//			String ws_module_Name = srcPath.substring(indexOfSpace + 1);
			String ws_module_Name = srcPath.substring(indexOfSpace + 1).trim();
// Modified by wubo on 2010/10/18 for V2.1対応 検収バグNo.4 End

			String[] ws_Module_Name = JAZZManage.splitWsModule(ws_module_Name);
			String wsName = ws_Module_Name[0];
			String moduleNames = ws_Module_Name[1];
			List<String> module_NameList = JAZZManage.splitModule(moduleNames);

			List<String> cmd = new ArrayList<String>();
			String jazzPath = ScmUtil.getSCMPath(APConst.TYPE_JAZZ);
			isJazzExecEnable = JAZZManage.isJazzExecEnable(jazzPath);
			if (!isJazzExecEnable) {
				isJazzExecEnable = false;
				throw new ScmException(new Message(APMsgConst.E_SCM_13, "Jazz"), null, ScmException.ERROR);
			}
			if ("".equals(jazzPath)) {
				cmd.add("scm");
			} else {
				if (CommonUtil.isWinOS()) {
					cmd.add(jazzPath + "scm.exe");
				} else {
					cmd.add(jazzPath + "scm.sh");
				}
			}

			if(cmdType == SCMCmdType.CONNECT){
				cmd.add("list");
				cmd.add("components");
			} else if(cmdType == SCMCmdType.CHECKOUT){
				cmd.add("load");
			} else if(cmdType == SCMCmdType.UPDATE){
				cmd.add("accept");
			}
			cmd.add("-r");
			cmd.add(repositoryUri);

			if(!StringUtils.isEmpty(project.getScm_user())){
				cmd.add("-u");
				cmd.add(project.getScm_user());
				if(!StringUtils.isEmpty(project.getScm_pass())){
					cmd.add("-P");
					cmd.add(project.getScm_pass());
				}
			}

			if(cmdType == SCMCmdType.CHECKOUT || cmdType == SCMCmdType.UPDATE){
				cmd.add("-d");
				cmd.add(localPath);
			}

			if(cmdType != SCMCmdType.UPDATE){
				cmd.add(wsName);
				if(cmdType == SCMCmdType.CHECKOUT){
					if (module_NameList != null && module_NameList.size() > 0) {
						for (int i = 0; i < module_NameList.size(); i++) {
							cmd.add(module_NameList.get(i));
						}
					}
				}
			}

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			messagePrint(p);

			int exitValue = 0;
			if (timeout > 0) {
				Integer status = ProcessManage.waitFor(p, timeout);
				if (status != null) {
					exitValue = status.intValue();
				} else {
					exitValue = -1;
				}
			} else {
				exitValue = p.waitFor();
			}


			if(exitValue != 0){
				if(cmdType == SCMCmdType.CONNECT){
					throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), null, ScmException.ERROR);
				}else if(cmdType == SCMCmdType.CHECKOUT){
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), null, ScmException.ERROR);
				}else{
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), null, ScmException.ERROR);
				}
			}
		}catch(Exception e){
			if (!isJazzExecEnable) {
				throw new ScmException(new Message(APMsgConst.E_SCM_13, "Jazz"), null, ScmException.ERROR);
			} else {
				if(cmdType == SCMCmdType.CONNECT){
					throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
				}else if(cmdType == SCMCmdType.CHECKOUT){
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "チェックアウト"), e, ScmException.ERROR);
				}else{
					throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getTitle(), "アップデート"), e, ScmException.ERROR);
				}
			}
		}
	}
	// Added by wubo on 2010/07/27 for V2.1対応 End
}
