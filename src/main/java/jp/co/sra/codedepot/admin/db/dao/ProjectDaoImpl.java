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
package jp.co.sra.codedepot.admin.db.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;

import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.admin.project.ProjectInfoBean;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.CodeProcess;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

/**
 * プロジェクト情報の取得処理、更新処理、登録処理、削除処理を実現する。
 * @author fenms
 */
public class ProjectDaoImpl implements ProjectDao {

	/** プロジェクト情報テーブルの更新SQL文ID */
	private static final String UPDATE_PROJECT_SQL_ID = DBConst.PROJECT
			+ ".updateTableProject";
	/** アクセス権限テーブルの削除SQL文ID */
	private static final String DELETE_PERMIT_SQL_ID = DBConst.PROJECT
			+ ".deleteTablePermit";
	/** アクセス権限テーブルの挿入SQL文ID */
	private static final String INSERT_PERMIT_SQL_ID = DBConst.PROJECT
			+ ".inertTablePermit";
	/** プロジェクト情報テーブルの挿入SQL文ID */
	private static final String INSERT_PROJECT_SQL_ID = DBConst.PROJECT
			+ ".inertTableProject";
	/** プロジェクト情報テーブル.識別子名の検索SQL文ID */
	private static final String SELECT_PROJECT_NAME_SQL_ID = DBConst.PROJECT
			+ ".selectProjectName";
	/** プロジェクト情報テーブル.プロジェクト名の検索SQL文ID */
	private static final String SELECT_PROJECT_LIST = DBConst.PROJECT
			+ ".selectProject";
	/** プロジェクト情報の検索SQL文ID */
	private static final String SELECT_PROJECT_INFO_SQL_ID = DBConst.PROJECT
			+ ".selectAllProjectInfo";
	/** プロジェクトアクセス権限ユーザの検索SQL文ID */
	private static final String SELECT_PERMIT_INFO_SQL_ID = DBConst.PROJECT
			+ ".selectPermitUser";
	/** プロジェクト情報の削除SQL文ID */
	private static final String DELETE_PROJECT_SQL_ID = DBConst.PROJECT
			+ ".deleteTableProject";
	/** プロジェクト情報のライセンス情報SQL文ID */
	private static final String SELECT_LICENSE_SQL_ID = DBConst.PROJECT
			+ ".selectLicense";
	/** プロジェクト情報テーブル.識別子名の検索SQL文ID(プロジェクト情報存在チェック) */
	private static final String SELECT_NAME_SQL_ID = DBConst.PROJECT
			+ ".checkActive";
	/** プロジェクト情報テーブル.検索インデックス更新処理の開始時刻の検索SQL文ID */
	private static final String SELECT_ALL_CRONTAB = DBConst.PROJECT
			+ ".selectAllCrontab";
	/** 検索可能なプロジェクト情報テーブル検索 SQL文ID */
	private static final String SELECT_PERMIT_PROJECT = DBConst.PROJECT
			+ ".selectPermitPrjInfo";

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<ProjectInfoEntity> queryProjectList(String title,
			String name) throws Exception {
		// プロジェクト識別子名を取得する
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		if (!"".equals(name)) {
			projParamMap.put(DBConst.PROJECT_NAME, name);
		}
		projParamMap.put(DBConst.PROJECT_TITLE, title.toLowerCase());
		return (ArrayList<ProjectInfoEntity>) sqlmap.queryForList(
				SELECT_PROJECT_LIST, projParamMap);
	}

	@Override
	public String checkActive(String name) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		ProjectInfoEntity info = (ProjectInfoEntity) sqlmap.queryForObject(
				SELECT_NAME_SQL_ID, name);
		if (null != info) {
			return info.getName();
		} else {
			return "";
		}
	}

	@Override
	public boolean doProjectInsert(SqlMapClient sqlmap,
			ProjectInfoBean projectInfo, Integer userId) throws Exception {
		// プロジェクト管理テーブルを更新する。
		Map<String, Object> projParamMap = getProjMapFromBean(projectInfo);
		projParamMap.put(DBConst.PROJECT_CUSERID, userId);
		projParamMap.put(DBConst.PROJECT_MUSERID, userId);
		sqlmap.insert(INSERT_PROJECT_SQL_ID, projParamMap);
		// アクセス権限ありユーザを挿入する。
		return insertPermintUsers(sqlmap, projectInfo);
	}

	@Override
	public boolean doProjectUpdate(SqlMapClient sqlmap,
			ProjectInfoBean projectInfo, Integer userId) throws Exception {
		// プロジェクト管理テーブルを更新する。
		Map<String, Object> projParamMap = getProjMapFromBean(projectInfo);
		projParamMap.put(DBConst.PROJECT_MUSERID, userId);
		int updateCount = sqlmap.update(UPDATE_PROJECT_SQL_ID, projParamMap);
		// 更新0件の場合
		if (updateCount <= 0) {
			return false;
		}
		// 既存アクセス権限ありユーザを削除する。
		sqlmap.delete(DELETE_PERMIT_SQL_ID, projParamMap);
		// アクセス権限ありユーザを挿入する。
		return insertPermintUsers(sqlmap, projectInfo);
	}

	@Override
	public boolean doProjectDelete(SqlMapClient sqlmap, String name,
			Integer userId) throws Exception {
		// プロジェクト管理テーブルを更新する。
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		projParamMap.put(DBConst.PROJECT_MUSERID, userId);
		projParamMap.put(DBConst.PROJECT_NAME, name);
		int updateCount = sqlmap.update(DELETE_PROJECT_SQL_ID, projParamMap);
		// 削除0件の場合
		if (updateCount <= 0) {
			return false;
		}
		// アクセス権限ユーザを削除する。
		sqlmap.delete(DELETE_PERMIT_SQL_ID, projParamMap);
		return true;
	}

	/**
	 * 入力したプロジェクト情報から、更新SQL文用パラメータマップを取得する。
	 *
	 * @param bean
	 *            入力したプロジェクト情報
	 * @return 更新用マップ
	 * @throws UnsupportedEncodingException
	 *             異常
	 */
	private Map<String, Object> getProjMapFromBean(ProjectInfoBean bean)
			throws UnsupportedEncodingException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(DBConst.PROJECT_NAME, bean.getName());
		resultMap.put(DBConst.PROJECT_TITLE, bean.getTitle());
		resultMap.put(DBConst.PROJECT_DESCRIPTION, bean.getDescription());
		resultMap.put(DBConst.PROJECT_LICENSE, bean.getLicense());
		resultMap.put(DBConst.PROJECT_SITE_URL, URLEncoder.encode(bean
				.getSite_url(), APConst.ENCODE_UTF_8));
		resultMap.put(DBConst.PROJECT_DOWNLOAD_URL, URLEncoder.encode(bean
				.getDownload_url(), APConst.ENCODE_UTF_8));
		resultMap.put(DBConst.PROJECT_RESTRICTED, bean.isRestricted());
		resultMap.put(DBConst.PROJECT_SRC_TYPE, bean.getSrc_type());
		resultMap.put(DBConst.PROJECT_SRC_PATH, URLEncoder.encode(bean
				.getSrc_path(), APConst.ENCODE_UTF_8));
		resultMap.put(DBConst.PROJECT_SCM_USER, bean.getScm_user());
		resultMap.put(DBConst.PROJECT_SCM_PASS, CodeProcess.base64Encode(bean
				.getScm_pass().getBytes()));
		resultMap.put(DBConst.PROJECT_CRONTAB, bean.getCrontab());
		resultMap.put(DBConst.PROJECT_IGNORES, bean.getIgnores());
		Integer adminId = StringUtils.isEmpty(bean.getAdminId()) ? null
				: Integer.parseInt(bean.getAdminId());
		resultMap.put(DBConst.PROJECT_ADMIN, adminId);
		return resultMap;
	}

	@Override
	public boolean insertPermintUsers(SqlMapClient sqlmap, ProjectInfoBean bean)
			throws SQLException {
		if (bean.isRestricted()) {
			List<String> userIdList = bean.getAccessUserIdList();
			for (String userIdStr : userIdList) {
				Map<String, Object> projParamMap = new HashMap<String, Object>();
				projParamMap.put(DBConst.PERMIT_MID, Integer
						.parseInt(userIdStr));
				projParamMap.put(DBConst.PERMIT_PROJECT, bean.getName());
				sqlmap.insert(INSERT_PERMIT_SQL_ID, projParamMap);
			}
		}
		return true;
	}

	@Override
	public ProjectInfoEntity getProjectInfo(String name) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (ProjectInfoEntity) sqlmap.queryForObject(
				"project.selectProjectInfo", name);
	}

	@Override
	public ProjectInfoEntity getAllProjectInfo(String name) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		projParamMap.put(DBConst.PROJECT_NAME, name);
		return (ProjectInfoEntity) sqlmap.queryForObject(
				SELECT_PROJECT_INFO_SQL_ID, projParamMap);
	}

	@Override
	public int getProjectNameNum(String name) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		projParamMap.put(DBConst.PROJECT_NAME, name);
		return (Integer) sqlmap.queryForObject(SELECT_PROJECT_NAME_SQL_ID,
				projParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectInfoEntity> getAccessUserInfo(String name)
			throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> projParamMap = new HashMap<String, Object>();
		projParamMap.put(DBConst.PERMIT_PROJECT, name);
		return (List<ProjectInfoEntity>) sqlmap.queryForList(
				SELECT_PERMIT_INFO_SQL_ID, projParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<ProjectInfoEntity> queryLicenseList() throws Exception {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (ArrayList<ProjectInfoEntity>) sqlmap
				.queryForList(SELECT_LICENSE_SQL_ID);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectInfoEntity> getAllCrontab() throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (List<ProjectInfoEntity>) sqlmap.queryForList(SELECT_ALL_CRONTAB);
	}

	@SuppressWarnings("unchecked")
	@Override
        public List<ProjectInfoEntity> getPermitProjectList(Integer mid) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (List<ProjectInfoEntity>) sqlmap.queryForList(SELECT_PERMIT_PROJECT, mid);
	}

}
