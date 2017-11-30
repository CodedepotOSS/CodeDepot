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

import jp.co.sra.codedepot.admin.util.APConst;

/**
 * SCM連携管理ファクトリークラス
 * @author sra
 *
 */
public class SCMFactory {
	/**
	 * バージョン管理のインスタンスを生成する
	 * @param type バージョン管理のタイプ
	 * @return バージョン管理のインスタンス
	 */
	public static VersionManage getInstance(String type){
		if(APConst.TYPE_SVN.equals(type)){
			return new SVNManage();
		}else if(APConst.TYPE_CVS.equals(type)){
			return new CVSManage();
		}else if(APConst.TYPE_GIT.equals(type)){
			return new GITManage();
			// Added by wubo on 2010/07/27 for V2.1対応 Start
		}else if(APConst.TYPE_JAZZ.equals(type)){
			return new JAZZManage();
		}
		// Added by wubo on 2010/07/27 for V2.1対応 End
		return null;
	}
}
