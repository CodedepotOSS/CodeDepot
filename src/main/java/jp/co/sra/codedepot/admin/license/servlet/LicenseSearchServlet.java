package jp.co.sra.codedepot.admin.license.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.sra.codedepot.admin.base.BaseServlet;
import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.exception.BaseException;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.APMsgParamConst;
import jp.co.sra.codedepot.admin.util.MessageUtil;
import jp.co.sra.codedepot.admin.util.StringUtils;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ライセンス一覧サーブレットのイベント処理クラスである。
 *
 * @author xiax
 *
 */
public class LicenseSearchServlet extends BaseServlet {

	/** ログ出力 */
	private Logger logger = LoggerFactory
			.getLogger(LicenseSearchServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doProcess(HttpServletRequest request,
			HttpServletResponse response) throws BaseException {
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_08,
				APConst.MSG_SERVLET));
		JSONArray json = new JSONArray();

		try {
			ProjectDao dao = new ProjectDaoImpl();
			ArrayList<ProjectInfoEntity> licenseList = dao.queryLicenseList();
			// 検索データをJSONObjectに設定する
			for (ProjectInfoEntity bean : licenseList) {
				if (!StringUtils.isEmpty(bean.getLicense())) {
					json.put(bean.getLicense());
				}
			}
			response.getWriter().print(json);
		} catch (Exception sqlEx) {
			// ファイル一覧情報の取得に失敗した場合、(E_COM_05)メッセージを出力し、空文字列をJSON形式に変換してリターンする
			logger.error(MessageUtil.getMessageString(
					APMsgConst.E_COM_05, APMsgParamConst.MSG_LICENSE));
			json.put("");
			try {
				response.getWriter().print(json);
			} catch (IOException e) {
				logger.debug("put \"\" in json failed");
			}
			return;
		}
		logger.info(MessageUtil.getMessageString(APMsgConst.I_COM_09,
				APConst.MSG_SERVLET));

	}

}
