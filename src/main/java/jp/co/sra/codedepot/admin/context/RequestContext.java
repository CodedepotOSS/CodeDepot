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
package jp.co.sra.codedepot.admin.context;

import java.io.Serializable;

/**
 *
 * @author sra
 *
 */
public interface RequestContext
    extends Serializable
{
	/**
	 * ID取得
	 *
	 * @return
	 */
    public abstract String getId();

    /**
     * 属性取得
     *
     * @param key キー
     * @return 属性
     */
    public abstract Serializable getAttribute(Serializable key);

    /**
     * 属性設定
     *
     * @param key キー
     * @param value 値
     */
    public abstract void setAttribute(Serializable key, Serializable value);

    /**
     * 属性を削除
     *
     * @param key キー
     * @return
     */
    public abstract Serializable removeAttribute(Serializable key);

    /**
     * RequestContextが空かどうかのをチェック
     *
     * @return true 空；false 非空
     */
    public abstract boolean isEmptyRequestContext();
}
