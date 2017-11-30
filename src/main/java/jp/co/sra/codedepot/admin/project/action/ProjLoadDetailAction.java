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
package jp.co.sra.codedepot.admin.project.action;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.dao.TotalSourceDao;
import jp.co.sra.codedepot.admin.db.dao.TotalSourceDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.CodeProcess;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.ProjectCheckUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.apache.commons.lang.StringEscapeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * プロジェクト詳細情報画面のロード処理クラスである。
 * @author fenms
 */
public class ProjLoadDetailAction extends BaseAction {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(ProjLoadDetailAction.class);

	/** プロジェクト識別子名 */
	private String name;

	@Override
	public BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		ProjectInfoBean oldBean=null;
		ProjectInfoBean bean = null;
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));
		try {
			oldBean = (ProjectInfoBean) ProjectCheckUtil.createFormBean(request,
					ProjectInfoBean.class);
			name = oldBean.getName();
			ProjectDao dao = new ProjectDaoImpl();
			// DAOからプロジェクト情報を取得する
			ProjectInfoEntity entrity = dao.getAllProjectInfo(name);
			bean = new ProjectInfoBean();
			if (entrity == null) {
				bean.setError(true);
				bean.setErrMsg(MessageUtil.getMessageString(APMsgConst.E_COM_08));
				return bean;
			}
			bean.setError(false);
			bean.setName(name);
			bean.setMode(APConst.MODE_MODIFY);
			bean.setName(entrity.getName());
			bean.setTitle(entrity.getTitle());
			if (!StringUtils.isEmpty(entrity.getDescription())) {
				bean.setDescription(StringEscapeUtils.escapeHtml(entrity.getDescription()));
			}
			if (!StringUtils.isEmpty(entrity.getLicense())) {
				bean.setLicense(StringEscapeUtils.escapeHtml(entrity.getLicense()));
			}
			if (!StringUtils.isEmpty(entrity.getSite_url())) {
			    bean.setSite_url(StringEscapeUtils.escapeHtml(URLDecoder.decode(entrity.getSite_url(),APConst.ENCODE_UTF_8)));
			}
			if (!StringUtils.isEmpty(entrity.getDownload_url())) {
			    bean.setDownload_url(StringEscapeUtils.escapeHtml(URLDecoder.decode(entrity.getDownload_url(),APConst.ENCODE_UTF_8)));
			}
			if (!StringUtils.isEmpty(entrity.getRestricted())) {
			    bean.setRestricted(entrity.getRestricted());
			}
			if (!StringUtils.isEmpty(entrity.getSrc_type())) {
			    bean.setSrc_type(entrity.getSrc_type());
			}
			if (!StringUtils.isEmpty(entrity.getSrc_path())) {
			    bean.setSrc_path(StringEscapeUtils.escapeHtml(URLDecoder.decode(entrity.getSrc_path(),APConst.ENCODE_UTF_8)));
			}
			if (!StringUtils.isEmpty(entrity.getScm_user())) {
			    bean.setScm_user(StringEscapeUtils.escapeHtml(entrity.getScm_user()));
			}
			if (!StringUtils.isEmpty(entrity.getScm_pass())) {
				byte[] passByte = CodeProcess.base64Decode(entrity.getScm_pass());
				bean.setScm_pass(StringEscapeUtils.escapeHtml(new String(passByte)));
			} else {
				bean.setScm_pass("");
			}
			bean.setScm_passConfirm(bean.getScm_pass());
			if (!StringUtils.isEmpty(entrity.getCrontab())) {
			    bean.setCrontab(StringEscapeUtils.escapeHtml(entrity.getCrontab()));
			}
			if (!StringUtils.isEmpty(entrity.getIgnores())) {
			    bean.setIgnores(StringEscapeUtils.escapeHtml(entrity.getIgnores()));
			}
			if (!StringUtils.isEmpty(entrity.getAdmin())) {
			    bean.setAdminId(entrity.getAdmin());
			}
			if (!StringUtils.isEmpty(entrity.getUsername())) {
			    bean.setAdminName(entrity.getUsername());
			}
			if (entrity.getIndexed_at() != null) {
			    SimpleDateFormat format = new SimpleDateFormat(APConst.DATE_FORMAT_YYMMDDHHMMSS);
			    bean.setUtime(format.format(entrity.getIndexed_at()));
			}
			//アクセス権限ユーザを取得する。
			if(bean.isRestricted()){
				List<ProjectInfoEntity> userList = dao.getAccessUserInfo(name);
				List<String> idList = new ArrayList<String>();
				List<String> nameList = new ArrayList<String>();
				for (ProjectInfoEntity entityItem : userList) {
					idList.add(entityItem.getId());
					nameList.add(entityItem.getUsername());
				}
				bean.setAccessUserIdList(idList);
				bean.setAccessUserNameList(nameList);
			}
			// ファイル集計情報を取得する。
			TotalSourceDao totalSrcDao = new TotalSourceDaoImpl();
			bean.setProjectSrcInfo(totalSrcDao.getProjectSrcInfo(name));
			// セッションに設定する。
			request.getSession().setAttribute(APConst.SESSION_EDITING_PROJECT_INFO, bean);
		} catch (Exception e) {
			logger.error(BaseException.getStackTraceStr(e));
			logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_10,
					APConst.MSG_ACTION));
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));
		return bean;
	}
}
