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
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.Message;

/**
 * CVSバージョン管理クラス
 * @author sra
 *
 */
public class CVSManage implements VersionManage {

	@Override
	public void checkout(ProjectInfoEntity project, String localPath) throws ScmException {

		if(!ScmUtil.directoryCanRead(localPath)
				|| !ScmUtil.directoryCanWrite(localPath)){
			throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getName(), "アップデート"), null, ScmException.ERROR);
		}
		ProcessManage.cvsExec(project, localPath, SCMCmdType.CHECKOUT);
	}

	@Override
	public void connect(ProjectInfoEntity project)throws ScmException{
		ProcessManage.cvsExec(project, null, SCMCmdType.CONNECT);
	}
	@Override
	public void update(ProjectInfoEntity project, String localPath) throws ScmException {

		if(!ScmUtil.directoryCanRead(localPath)
				|| !ScmUtil.directoryCanWrite(localPath)){
			throw new ScmException(new Message(APMsgConst.E_SCM_04, project.getName(), "アップデート"), null, ScmException.ERROR);
		}
		ProcessManage.cvsExec(project, localPath, SCMCmdType.UPDATE);
	}
	@Override
	public boolean isCheckout(ProjectInfoEntity project, String localPath){
		String module = "";
		Pattern pattern = Pattern.compile(":pserver:.*@.+/.+\\s(.+)");
		Matcher matcher = pattern.matcher(project.getSrc_path());
		if(matcher.matches()){
			module = matcher.group(1);
		}
		File subDir = new File(localPath + "/" + module + "/CVS");
		if(subDir.exists()){
			Date last = new Date(subDir.lastModified());
			if(last.after(project.getMtime())){
				return false;
			}
		}
		return true;
	}
}
