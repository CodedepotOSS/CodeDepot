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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.sra.codedepot.admin.base.BaseAction;
import jp.co.sra.codedepot.admin.base.BaseBean;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.ProjectCheckUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * プロジェクト検索・一覧画面の初期化処理クラスである。
 *
 * @author sra
 *
 */
public class ProjectEditAction extends BaseAction {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(ProjectEditAction.class);

	/**
	 * 画面ID
	 */
	public static final String _PAGE_ID = "projectEdit";

	@Override
	protected BaseBean doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {

		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_ACTION));
		ProjectInfoBean bean = new ProjectInfoBean();
		try {
			String inputFlg = request.getParameter(APConst.JSON_INPUTFLG);
			String mode = request.getParameter(APConst.MODE);
			if (StringUtils.isEmpty(inputFlg)) {
				// 追加模式初期化
				if (APConst.MODE_ADD.equals(mode)) {
					bean.setMode(APConst.MODE_ADD);
					bean.setRestricted(APConst.RESTRICTED_FALSE_STR);
					bean.setSrc_type(APConst.TYPE_LOCAL);
				// 変更模式初期化
				} else {
					HttpSession session = request.getSession();
					bean = (ProjectInfoBean) session.getAttribute(
							APConst.SESSION_EDITING_PROJECT_INFO);
				}
			// プロジェクト追加・変更確認画面から
			} else {
				bean = (ProjectInfoBean) ProjectCheckUtil
						.createFormBean(request, ProjectInfoBean.class);
				bean = (ProjectInfoBean) ProjectCheckUtil.escapeHtml(bean);
				bean.setScm_passConfirm(bean.getScm_pass());
			}
		} catch (Exception e) {
			throw new BaseException(e);
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_ACTION));
		return bean;
	}
}
