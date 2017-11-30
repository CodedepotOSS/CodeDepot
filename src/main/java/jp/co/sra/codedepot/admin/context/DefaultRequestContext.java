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
import java.util.HashMap;
import java.util.Iterator;

import jp.co.sra.codedepot.admin.util.APConst;

/**
 *
 * @author sra
 *
 */
public class DefaultRequestContext implements RequestContext {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	String id = null;
	private HashMap<Serializable, Serializable> attributes;

	public DefaultRequestContext() {
		attributes = new HashMap<Serializable, Serializable>();
	}

	public String toString() {
		StringBuffer stringRep = new StringBuffer();
		if (attributes != null) {
			stringRep.append(" attributes={" + APConst.LINE_SEPARATOR);
			Object key = null;
			for (Iterator<Serializable> keys = attributes.keySet().iterator(); keys
					.hasNext(); stringRep.append(key + "="
					+ attributes.get(key) + APConst.LINE_SEPARATOR))
				key = keys.next();

			stringRep.append("}" + APConst.LINE_SEPARATOR);
		}
		stringRep.append("]");
		return stringRep.toString();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Serializable getAttribute(Serializable key) {
		return (Serializable) attributes.get(key);
	}

	@Override
	public void setAttribute(Serializable key, Serializable value) {
		attributes.put(key, value);
	}

	@Override
	public Serializable removeAttribute(Serializable key) {
		return (Serializable) attributes.remove(key);
	}

	@Override
	public boolean isEmptyRequestContext() {
		boolean isEmpty = true;
		if (attributes != null && attributes.size() > 0)
			isEmpty = false;
		return isEmpty;
	}

}
