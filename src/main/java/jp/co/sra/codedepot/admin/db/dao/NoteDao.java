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
import java.util.List;
import java.util.Map;

import jp.co.sra.codedepot.admin.db.entity.FileEntity;
import jp.co.sra.codedepot.admin.db.entity.NoteEntity;

public interface NoteDao {

	/***
	 * ノート一覧画面情報を取得する
	 * @param fileID ファイルID
	 * @param loginID ログインID
	 * @return
	 * @throws SQLException
	 */
	public List<NoteEntity> getNoteInfoList(int fileID,int loginID)throws SQLException;

	/***
	 * ファイルレIDを取得する
	 * @param project プロジェクト識別子
	 * @param path ファイルのパス
	 * @return
	 * @throws SQLException
	 */
	public int getFileID(String project, String path)throws SQLException;

	/***
	 * ノート情報レコード数を取得する
	 * @param fileID ファイルID
	 * @param loginID ログインID
	 * @return
	 * @throws SQLException
	 */
	public int getNoteCount(int fileID, int loginID)throws SQLException;

	/***
	 * ノート情報を削除する
	 * @param noteID ノートID
	 * @return
	 * @throws SQLException
	 */
	public int deleteNoteInfo(int noteID)throws SQLException;

	/***
	 * ノート情報を取得する
	 * @param noteID ノートID
	 * @return
	 * @throws SQLException
	 */
	public NoteEntity getNoteInfo(int noteID) throws SQLException;

	/***
	 * ファイルの行数を取得する
	 * @param project プロジェクト識別子
	 * @param path ファイルのパス
	 * @return
	 * @throws SQLException
	 */
	public int getSourceLines(String project, String path)throws SQLException;

	/***
	 * ノート情報を新規する
	 * @param noteEntity NoteEntity
	 * @return
	 * @throws SQLException
	 */
	public int insertNoteInfo(NoteEntity noteEntity)throws SQLException;

	/***
	 * ノート情報を変更する
	 * @param noteEntity NoteEntity
	 * @return
	 * @throws SQLException
	 */
	public int updateNoteInfo(NoteEntity noteEntity)throws SQLException;

	/***
	 * ファイル情報を新規する
	 * @param fileEntity FileEntity
	 * @return ファイルID
	 * @throws SQLException
	 */
	public int insertFileInfo(FileEntity fileEntity)throws SQLException;

	/***
	 * プロジェクトの個人ノート数をカウントする
	 * @param Integer uid
	 * @param String project
	 * @return Interger
	 * @throws SQLException
	 */
	public int countPersonalNotes(Integer uid, String pid) throws SQLException;

	/***
	 * プロジェクトの公開ノート数をカウントする
	 * @param String project
	 * @return Interger
	 * @throws SQLException
	 */
	public int countPublicNotes(String pid) throws SQLException;

	/***
	 * プロジェクトの個人ノートの一覧を取得する
	 * @param Integer uid
	 * @param String project
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> selectPersonalNotes(Integer uid, String pid) throws SQLException;

	/***
	 * プロジェクトの公開ノートの一覧を取得する
	 * @param Integer uid
	 * @param String project
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> selectPublicNotes(String pid) throws SQLException;

	/***
	 * 検索ノートの一覧を取得する
	 * @param HashMap query
	 * @return List of NoteEntity
	 * @throws SQLException
	 */
	public List<NoteEntity> searchNotes(Map<String, Object> query) throws SQLException;
}
