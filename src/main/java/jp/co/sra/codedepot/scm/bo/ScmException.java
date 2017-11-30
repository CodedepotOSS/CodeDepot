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

import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.Message;

/**
 * SCM連携機能異常処理クラス
 * @author sra
 *
 */
public class ScmException extends BaseException {
	private static final long serialVersionUID = 1L;
	/**
	 * 異常レベル
	 */
	private int level;
	public static int DEBUG = 0;
	public static int WARN = 1;
	public static int INFO = 2;
	public static int ERROR = 3;
	public ScmException(Message message, int level){
		super(message);
		this.level = level;
	}
	public ScmException(Message message, Throwable cause, int level){
		super(message, cause);
		this.level = level;
	}
	/**
	 * 異常レベルを取得する
	 * @return 異常レベル
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * 異常レベルをセットする
	 * @param level 異常レベル
	 */
	public void setLevel(int level) {
		this.level = level;
	}
}
