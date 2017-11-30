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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.sra.codedepot.admin.db.entity.FileEntity;
import jp.co.sra.codedepot.admin.db.entity.NoteEntity;
import jp.co.sra.codedepot.admin.util.DBConst;
import jp.co.sra.codedepot.db.sqlmap.SqlMapConfig;

import com.ibatis.sqlmap.client.SqlMapClient;

public class NoteDaoImpl implements NoteDao{
	/** ノート一覧画面情報リストを取得 SQL文ID */
	private static final String SELECT_NOTE_INFOLIST_SQL_ID = "note.getNoteInfoList";
	/** ファイルレIDを取得 SQL文ID */
	private static final String SELECT_FILEID_SQL_ID = "note.getFileID";
	/** ノート情報レコード数を取得 SQL文ID */
	private static final String SELECT_NOTECOUNT_SQL_ID = "note.getNoteInfoCount";
	/**  ノート情報を削除 SQL文ID */
	private static final String DELETE_NOTEINFO_SQL_ID = "note.deleteNoteInfo";
	/**  ノート情報を取得 SQL文ID */
	private static final String SELECT_NOTE_INFO_SQL_ID = "note.getNoteInfo";
	/**  ファイルの行数を取得 SQL文ID */
	private static final String SELECT_FILE_LINES_SQL_ID = "note.getSourceLines";
	/**  ノート情報を新規 SQL文ID */
	private static final String INSERT_NOTE_INFO_SQL_ID = "note.insertNoteInfo";
	/**  ノート情報を変更 SQL文ID */
	private static final String UPDATE_NOTE_INFO_SQL_ID = "note.updateNoteInfo";
	/**  ファイル情報を新規 SQL文ID */
	private static final String INSERT_FILE_INFO_SQL_ID = "note.insertFileInfo";
	/**  個人ノート数カウント SQL文ID */
	private static final String COUNT_PERSONAL_NOTE_SQL_ID = "note.countPersonal";
	/**  公開ノート数カウント SQL文ID */
	private static final String COUNT_PUBLIC_NOTE_SQL_ID = "note.countPublic";
	/**  個人ノートの一覧所得 SQL文ID */
	private static final String SELECT_PERSONAL_NOTE_SQL_ID = "note.selectPersonal";
	/**  公開ノートの一覧所得 SQL文ID */
	private static final String SELECT_PUBLIC_NOTE_SQL_ID = "note.selectPublic";
	/**  ノート検索の一覧所得 SQL文ID */
	private static final String SEARCH_NOTE_SQL_ID = "note.searchNote";

	/***
	 * ノート一覧画面情報を取得する
	 * @param fileID ファイルID
	 * @param loginID ログインID
	 * @return ノート一覧画面情報リスト
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public List<NoteEntity> getNoteInfoList(int fileID,int loginID)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> noteParamMap = new HashMap<String, Object>();
		noteParamMap.put(DBConst.FILE_ID, fileID);
		noteParamMap.put(DBConst.LOGIN_ID, loginID);
		return sqlmap.queryForList(SELECT_NOTE_INFOLIST_SQL_ID,noteParamMap);
	}

	/***
	 * ファイルレIDを取得する
	 * @param project プロジェクト識別子
	 * @param path ファイルのパス
	 * @return ファイルレID
	 * @throws SQLException
	 */
	public int getFileID (String project, String path)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> noteParamMap = new HashMap<String, Object>();
		noteParamMap.put(DBConst.PERMIT_PROJECT, project);
		noteParamMap.put(DBConst.FILE_PATH, path);
		List<?> list = sqlmap.queryForList(SELECT_FILEID_SQL_ID,noteParamMap);
		int fileID = -1;
		if( null != list && list.size()> 0){
			fileID = (Integer)list.get(0);
		}
		return fileID;
	}

	/***
	 * ノート情報レコード数を取得する
	 * @param fileID ファイルID
	 * @param loginID ログインID
	 * @return ノート情報レコード数
	 * @throws SQLException
	 */
	public int getNoteCount(int fileID,int loginID)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> noteParamMap = new HashMap<String, Object>();
		noteParamMap.put(DBConst.FILE_ID, fileID);
		noteParamMap.put(DBConst.LOGIN_ID, loginID);
		List<?> list = sqlmap.queryForList(SELECT_NOTECOUNT_SQL_ID,noteParamMap);
		int noteInfoCount = 0;
		if( null != list && list.size()> 0){
			noteInfoCount = (Integer)list.get(0);
		}
		return noteInfoCount;
	}

	/***
	 * ノート情報を削除する
	 * @param noteID ノートID
	 * @return ノート情報削除件数
	 * @throws SQLException
	 */
	public int deleteNoteInfo(int noteID) throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int resultcount = (Integer)sqlmap.delete(DELETE_NOTEINFO_SQL_ID, noteID);
		return resultcount;
	}

	/***
	 * ノート情報を取得する
	 * @param noteID ノートID
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public NoteEntity getNoteInfo(int noteID) throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		List<NoteEntity> list = sqlmap.queryForList(SELECT_NOTE_INFO_SQL_ID,noteID);
		NoteEntity bean = null;
		if( null != list && list.size()> 0){
			bean =(NoteEntity)list.get(0);
		}
		return bean;
	}

	/***
	 * ファイルの行数を取得する
	 * @param project プロジェクト識別子
	 * @param path ファイルのパス
	 * @return ファイルの行数
	 * @throws SQLException
	 */
	public int getSourceLines(String project, String path)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> noteParamMap = new HashMap<String, Object>();
		noteParamMap.put(DBConst.PERMIT_PROJECT, project);
		noteParamMap.put(DBConst.FILE_PATH, path);
		List<?> list = sqlmap.queryForList(SELECT_FILE_LINES_SQL_ID,noteParamMap);
		int sourcelines = 0;
		if( null != list && list.size()> 0){
			sourcelines = (Integer)list.get(0);
		}
		return sourcelines;
	}

	/***
	 * ノート情報を新規する
	 * @param noteEntity NoteEntity
	 * @return ノート情報新規件数
	 * @throws SQLException
	 */
	public int insertNoteInfo(NoteEntity noteEntity)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Object inserobj = sqlmap.insert(INSERT_NOTE_INFO_SQL_ID, noteEntity);
		int count = 0;
		if( null != inserobj ){
			count = (Integer)inserobj;
		}
		return count;
	}

	/***
	 * ノート情報を変更する
	 * @param noteEntity NoteEntity
	 * @return ノート情報変更件数
	 * @throws SQLException
	 */
	public int updateNoteInfo(NoteEntity noteEntity)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		int count = (Integer)sqlmap.update(UPDATE_NOTE_INFO_SQL_ID, noteEntity);
		return count;
	}

	/***
	 * ファイル情報を新規する
	 * @param fileEntity FileEntity
	 * @return ファイルID
	 * @throws SQLException
	 */
	public int insertFileInfo(FileEntity fileEntity)throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Object inserobj = sqlmap.insert(INSERT_FILE_INFO_SQL_ID, fileEntity);
		int count = 0;
		if( null != inserobj ){
			count = (Integer)inserobj;
		}
		return count;
	}

	/***
	 * プロジェクトの個人ノート数をカウントする
	 * @param String project
	 * @return Interger
	 * @throws SQLException
	 */
        public int countPersonalNotes(Integer uid, String pid) throws SQLException{
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("uid", uid);
		if (pid != null) {
			map.put("pid", pid);
		}
		List<Object> list = sqlmap.queryForList(COUNT_PERSONAL_NOTE_SQL_ID,map);

		int count = 0;
		if( null != list && list.size()> 0){
			count = (Integer)list.get(0);
		}
		return count;
	}

	/***
	 * プロジェクトの公開ノート数をカウントする
	 * @param String project
	 * @return Interger
	 * @throws SQLException
	 */
	public int countPublicNotes(String pid) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();

		Map<String, Object> map = new HashMap<String, Object>();
		if (pid != null) {
			map.put("pid", pid);
		}
		List<Object> list = sqlmap.queryForList(COUNT_PUBLIC_NOTE_SQL_ID,map);

		int count = 0;
		if( null != list && list.size()> 0){
			count = (Integer)list.get(0);
		}
		return count;
	}
	/***
	 * プロジェクトの個人ノートの一覧を取得する
	 * @param Integer uid
	 * @param String project
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> selectPersonalNotes(Integer uid, String pid) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("uid", uid);
		if (pid != null) {
			map.put("pid", pid);
		}
		return (List<NoteEntity>)sqlmap.queryForList(SELECT_PERSONAL_NOTE_SQL_ID,map);
	}

	/***
	 * プロジェクトの公開ノートの一覧を取得する
	 * @param Integer uid
	 * @param String project
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> selectPublicNotes(String pid) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		Map<String, Object> map = new HashMap<String, Object>();
		if (pid != null) {
			map.put("pid", pid);
		}
		return (List<NoteEntity>)sqlmap.queryForList(SELECT_PUBLIC_NOTE_SQL_ID,map);
	}

	/***
	 * 検索ノートの一覧を取得する
	 * @param HashMap query
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> searchNotes(Map<String, Object> query) throws SQLException {
		SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
		return (List<NoteEntity>)sqlmap.queryForList(SEARCH_NOTE_SQL_ID,query);
	}
}
