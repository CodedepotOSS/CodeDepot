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
 * GITバージョン管理クラス
 *
 */
public class GITManage implements VersionManage {
	private static final Logger logger;
	static {
        logger = Logger.getLogger(GITManage.class.getName());
        try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void checkout(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.gitExec(project, localPath, SCMCmdType.CHECKOUT);
	}

	@Override
	public void connect(ProjectInfoEntity project)throws ScmException {
		try{
			int timeout = Integer.parseInt(APProperties.getProperty(APConst.SCM_TIMEOUT));
			ProcessManage.gitExec(project, null, SCMCmdType.CONNECT, timeout);
		}catch(ScmException se){
			throw se;
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
		}
	}

	@Override
	public void update(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.gitExec(project, localPath, SCMCmdType.UPDATE);
	}

	@Override
	public boolean isCheckout(ProjectInfoEntity project, String localPath){
		// テンポラリディレクトリに .git が存在する
		File subDir = new File(localPath + "/.git");
		if(subDir.exists()){
			logger.fine(localPath + " is Versioned Directory");
			Date last = new Date(subDir.lastModified());
			if(last.after(project.getMtime())){
				return false;
			}
		}
	    return true;
    }
}
