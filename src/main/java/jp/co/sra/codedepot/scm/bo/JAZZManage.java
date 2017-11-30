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
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APProperties;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.admin.util.CommonUtil;

/**
 * JAZZバージョン管理クラス
 * @author sra
 *
 */
public class JAZZManage implements VersionManage {
	private static final Logger logger;
	static {
        logger = Logger.getLogger(JAZZManage.class.getName());
        try {
			APProperties.init(APConst.PROPERTY_BASEPATH + APConst.PROPERTY_FILENAME_AP);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	@Override
	public void checkout(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.jazzExec(project, localPath, SCMCmdType.CHECKOUT);
	}
	public void connect(ProjectInfoEntity project)throws ScmException {
		try{
			int timeout = Integer.parseInt(APProperties.getProperty(APConst.SCM_TIMEOUT));
			ProcessManage.jazzExec(project, null, SCMCmdType.CONNECT, timeout);
		}catch(ScmException se){
			throw se;
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_02, "リポジトリ"), e, ScmException.ERROR);
		}
	}

	@Override
	public void update(ProjectInfoEntity project, String localPath) throws ScmException {
		ProcessManage.jazzExec(project, localPath, SCMCmdType.UPDATE);
	}
	@Override
	public boolean isCheckout(ProjectInfoEntity project, String localPath){
		// テンポラリディレクトリに JAZZ が存在する
		File subDir = new File(localPath + "/.jazz5");
		if(subDir.exists()){
			Date last = new Date(subDir.lastModified());
			// JAZZ/.jazz5の更新日付がプロジェクト管理テーブルの更新日時より新しい場合
			if(last.after(project.getMtime())){
				return false;
			}
		}
		return true;
	}

// Modified by wubo on 2010/09/13 for V2.1対応 Start
	public static String[] splitWsModule(String ws_module_Name) {
		String[] ws_module_Names = new String[2];
		int index;
		String ws_Name = "";
		String module_Names = "";
		if (ws_module_Name.startsWith("\"")) {
			index = ws_module_Name.indexOf("\"", 1);
			if (index != -1) {
				ws_Name = ws_module_Name.substring(1, index);
				if (ws_module_Name.length() > index + 1) {
					module_Names = ws_module_Name.substring(index + 2);
				}
			} else {
				ws_Name = ws_module_Name.substring(1);
			}
		} else {
			index = ws_module_Name.indexOf(" ");
			if (index != -1) {
				ws_Name = ws_module_Name.substring(0, index);
				module_Names = ws_module_Name.substring(index + 1);
			} else {
				ws_Name = ws_module_Name;
			}
		}
		ws_module_Names[0] = ws_Name;
		ws_module_Names[1] = module_Names;
		return ws_module_Names;
	}

	public static List<String> splitModule(String module_Names) {
		module_Names = module_Names.trim();
		List<String> moduleNameList = new ArrayList<String>();
		int index;
		String module_Name = "";
		if (!StringUtils.isEmpty(module_Names)) {
			if (module_Names.startsWith("\"")) {
				index = module_Names.indexOf("\"", 1);
				if (index != -1) {
					module_Name = module_Names.substring(1, index);
					moduleNameList.add(module_Name);
					if (module_Names.length() > index + 1) {
						module_Names = module_Names.substring(index + 2);
						moduleNameList.addAll(splitModule(module_Names));
					}
				} else {
					module_Name = module_Names.substring(1);
					moduleNameList.add(module_Name);
				}
			} else {
				index = module_Names.indexOf(" ");
				if (index != -1) {
					module_Name = module_Names.substring(0, index);
					moduleNameList.add(module_Name);
					module_Names = module_Names.substring(index + 1);
					moduleNameList.addAll(splitModule(module_Names));
				} else {
					module_Name = module_Names;
					moduleNameList.add(module_Name);
				}
			}
		}
		return moduleNameList;
	}
// Modified by wubo on 2010/09/13 for V2.1対応 End

	public static boolean isJazzExecEnable(String jazzPath) {

		List<String> cmd = new ArrayList<String>();;
		try{
			if ("".equals(jazzPath)) {
				cmd.add("scm");
			} else {
				if (CommonUtil.isWinOS()) {
					cmd.add(jazzPath + "scm.exe");
				} else {
					cmd.add(jazzPath + "scm.sh");
				}
			}

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
		}catch(Exception e){
			return false;
		}
		return true;
	}
}
