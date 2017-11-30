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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.CommonUtil;
import jp.co.sra.codedepot.admin.util.Message;

/**
 * SVNバージョン管理クラス
 * @author sra
 *
 */
public class SVNManage implements VersionManage {
	private static final Logger logger;
	static {
        logger = Logger.getLogger(SVNManage.class.getName());
        try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	@Override
	public void checkout(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.svnExec(project, localPath, SCMCmdType.CHECKOUT);
	}
	public void connect(ProjectInfoEntity project)throws ScmException {
		try{
			int timeout = Integer.parseInt(APProperties.getProperty(APConst.SCM_TIMEOUT));
			ProcessManage.svnExec(project, null, SCMCmdType.CONNECT, timeout);
		}catch(ScmException se){
			throw se;
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
		}
	}

	@Override
	public void update(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.svnExec(project, localPath, SCMCmdType.UPDATE);
	}
	@Override
	public boolean isCheckout(ProjectInfoEntity project, String localPath){
		// テンポラリディレクトリに SVN/Entries が存在する
		File subDir = new File(localPath + "/.svn");
		if(subDir.exists()){
			logger.fine(localPath + " is Versioned Directory");
			Date last = new Date(subDir.lastModified());
			logger.fine("SVN/Entriesの更新日付:" + last);
			logger.fine("プロジェクト管理テーブルの更新日時:" + project.getMtime());
			// SVN/Entriesの更新日付がプロジェクト管理テーブルの更新日時より新しい場合
			if(last.after(project.getMtime())){
				return false;
			}
		}
		return true;
	}
	public static boolean isSvn1_6()throws Exception{
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
			cmd.add("--version");
			cmd.add("--quiet");
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = br.readLine();
			br.close();
			if(0 != p.waitFor()){
				throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), null, ScmException.ERROR);
			}
			if(null != line && line.startsWith("1.6")){
				return true;
			}
			return false;
	}
}
