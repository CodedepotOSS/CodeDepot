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
package jp.co.sra.codedepot.admin.util;

import java.util.HashMap;
import java.util.Map;

import jp.co.sra.codedepot.admin.exception.BaseException;

public class ObjectFactory {
	private static ObjectFactory FACTORY = new ObjectFactory();
	private Map<String, Class<?>> classes;
	private Map<String, Object> objects;

	protected ObjectFactory() {
		classes = new HashMap<String, Class<?>>();
		objects = new HashMap<String, Object>();
	}

	public static ObjectFactory getObjectFactory() {
		return FACTORY;
	}

	public static void setObjectFactory(ObjectFactory factory) {
		if (factory != null)
			FACTORY = factory;
	}

	public Class<?> getClassInstance(String className) throws BaseException {
		if (StringUtils.isEmpty(className))
			return null;
		Class<?> clazz = classes.get(className);
		try {
			if (clazz == null) {
				clazz = Class.forName(className);
				classes.put(className, clazz);
			}
		} catch (ClassNotFoundException cnfe) {
			throw new BaseException(new Message("クラス（" + className
					+ "）がロードできません。"), cnfe);
		}
		return clazz;
	}

	public Object buildBean(Class<?> clazz) throws BaseException {
		return buildBean(clazz, false);
	}

	public Object buildBean(Class<?> clazz, boolean useCache) throws BaseException {
		if (clazz == null)
			return null;
		Object instance = null;
		if (useCache)
			instance = objects.get(clazz.getName());
		if (instance == null) {
			try {
				instance = clazz.newInstance();
			} catch (InstantiationException ie) {
				throw new BaseException(
						new Message(
								"\u30AF\u30E9\u30B9\uFF08"
										+ clazz
										+ "\uFF09\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u306E\u751F\u6210\u304C\u3067\u304D\u307E\u305B\u3093\u3002"),
						ie);
			} catch (IllegalAccessException iae) {
				throw new BaseException(
						new Message(
								"\u30AF\u30E9\u30B9\uFF08"
										+ clazz
										+ "\uFF09\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u306E\u751F\u6210\u304C\u3067\u304D\u307E\u305B\u3093\u3002"),
						iae);
			}
			if (useCache)
				objects.put(clazz.getName(), instance);
		}
		return instance;
	}

	public Object buildBean(String className) throws BaseException {
		return buildBean(className, false);
	}

	public Object buildBean(String className, boolean useCache)
			throws BaseException {
		if (StringUtils.isEmpty(className))
			return null;
		Object instance = null;
		if (useCache)
			instance = objects.get(className);
		if (instance == null) {
			try {
				Class<?> clazz = getClassInstance(className);
				instance = clazz.newInstance();
			} catch (InstantiationException iae) {
				throw new BaseException(
						new Message(
								"\u30AF\u30E9\u30B9\uFF08"
										+ className
										+ "\uFF09\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u306E\u751F\u6210\u304C\u3067\u304D\u307E\u305B\u3093\u3002"),
						iae);
			} catch (IllegalAccessException iae) {
				throw new BaseException(
						new Message(
								"\u30AF\u30E9\u30B9\uFF08"
										+ className
										+ "\uFF09\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u306E\u751F\u6210\u304C\u3067\u304D\u307E\u305B\u3093\u3002"),
						iae);
			}
			if (useCache)
				objects.put(className, instance);
		}
		return instance;
	}
}
