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
package jp.co.sra.codedepot.admin.account.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.entity.AccountEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;

public class PersonInfoUpdateAction extends BaseAction{
	// ログ
	private static final Logger logger = LoggerFactory.getLogger(PersonInfoUpdateAction.class);
	// 変更パスワードのフラグ
	private  boolean updatePersonPwdFlag = false;
	private AccountEntity bean = null;
	// パスワード検索SQL文ID
	private static final String SELECT_PERSON_SQL_ID = "account.getPersonInfo";

	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response)throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));

		SqlMapClient sqlmap = null;
		try {
			sqlmap = SqlMapConfig.getSqlMapClient();
			HttpSession session = request.getSession();
			// ログインIDを取得する
			int loginId = (Integer)session.getAttribute(APConst.SESSION_LOGIN_ID);
			// ログイン情報を取得する
			List<?> resultlist = sqlmap.queryForList(SELECT_PERSON_SQL_ID,loginId);

			if(null != resultlist && resultlist.size() > 0){
				// DBから、個人情報を取得する
				 bean = (AccountEntity)resultlist.get(0);

				if(StringUtils.isEmpty(bean.getPassword())){
					updatePersonPwdFlag = false;
				}else{
					updatePersonPwdFlag = true;
				}
			}else{
				// アカウント情報を取得できないの場合
				throw new BaseException();
			}
		} catch (Exception e) {
			throw new BaseException(e);
		}

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));

		return bean;
	}

	/***
	 * 変更パスワードのフラグを取得する
	 * @return 変更パスワードのフラグ true/false
	 */
	public  boolean getUpdatePersonPwdFlag(){
		return updatePersonPwdFlag;
	}
}
