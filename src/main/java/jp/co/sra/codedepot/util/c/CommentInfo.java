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
package jp.co.sra.codedepot.util.c;

/**
 * Commentクラス。
 *
 */
public class CommentInfo {

	/** 開始行 */
	private int _start;

	/** 終了行 */
	private int _end;

	/** 初期オフセット */
	private int _offset;

	/** 長さ */
	private int _length;

	/**
     * コンストラクタ。
     *
     * @param start 開始行
     * @param end 終了行
     * @param offset 初期オフセット
     * @param length 長さ
     */
	public CommentInfo(int start, int end, int offset, int length) {
		this._start = start;
		this._end = end;
		this._offset = offset;
		this._length = length;
	}

	/**
     * 開始行を取得する。
     *
     * @return 開始行
     */
	public int getStartingLineNumber() {
		return _start;
	}

	/**
     * 終了行を取得する。
     *
     * @return 終了行
     */
	public int getEndingLineNumber() {
		return _end;
	}

	/**
     * 初期オフセットを取得する。
     *
     * @return 初期オフセット
     */
	public int getOffset() {
		return _offset;
	}

	/**
     * 長さを取得する。
     *
     * @return 長さ
     */
	public int getLength() {
		return _length;
	}

	/**
     * このコメントが引数で指定された中にあるか判定する。
     *
     * @param offset オフセット
     * @param length 長さ
     * @return true:指定された中にある、false:指定された中にない
     */
	public boolean isCommentIn(int offset, int length) {

		boolean result = false;

		if (offset < _offset && offset + length > _offset + _length) {
			result = true;
		}
		return result;
	}
}
