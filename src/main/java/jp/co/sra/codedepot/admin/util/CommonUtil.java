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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.net.URI;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;

import jp.co.sra.codedepot.admin.db.dao.ProjectDao;
import jp.co.sra.codedepot.admin.db.dao.ProjectDaoImpl;
import jp.co.sra.codedepot.admin.db.entity.ProjectInfoEntity;
import jp.co.sra.codedepot.scm.bo.Scm;
import jp.co.sra.codedepot.solr.ProgramUID;

import org.quartz.utils.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	// ログ
	private static final Logger logger = LoggerFactory
			.getLogger(CommonUtil.class);

	/***
	 *  パスワード  Digest
	 * @param info パスワード
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getPwdDigest(String info){
		byte[] byteInfo = null;
		byteInfo = CodeProcess.SHADigest(info);
		return CodeProcess.base64Encode(byteInfo);
	}

	/**
	 * ユニークIDを取得する。
	 * @return ユニークID
	 */
	public static  synchronized String getUniqueId() {
		ProgramUID puid = new ProgramUID(3, 3, 6);
		return "_" + puid.nextUID();
	}

	/***
	 * LDAP認証
	 * @param prop
	 * @param username
	 * @param pwd
	 * @return
	 */
	public  static boolean  ldapCertification(Properties prop,String username, String pwd) {
		boolean isLdapValidationOk = true;
		Hashtable<String,String> env = new Hashtable<String,String>();
		// LDAPのクラス
		String factory = (String)prop.get("LDAP_CONTEXT_FACTORY");
		if (StringUtils.isEmpty(factory)) {
			factory = "com.sun.jndi.ldap.LdapCtxFactory";
		}
		env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
		// LDAPサーバのURL
		StringBuffer urlBuf = new StringBuffer();
		// LDAPのバースDN
		String ldapBaseDn = (String)prop.get("LDAP_BASE_DN");
		// LDAPのアカウントDN
		String ldapUserDn = (String)prop.get("LDAP_USER_DN");
		// LDAPのバースDNとLDAPのアカウントDNのチェック
		if(StringUtils.isEmpty(ldapBaseDn) || StringUtils.isEmpty(ldapUserDn)){
			logger.error(MessageUtil.getMessageString(APMsgConst.W_MEM_01,APMsgParamConst.LDAP_DN_NAME_CHAR));
			return false;
		}

		// LDAP URL
		String ldapUrl = (String)prop.get("LDAP_URL");
		if (StringUtils.isEmpty(ldapUrl)) {
			ldapUrl = "ldap://127.0.0.1:389/";
		}
		urlBuf.append(ldapUrl);
		if (!ldapUrl.endsWith("/")) {
		    urlBuf.append("/");
		}
		urlBuf.append(ldapBaseDn);
		env.put(Context.PROVIDER_URL,urlBuf.toString());

		// LDAPシンプル認証
		String authType = (String)prop.get("LDAP_SECURITY_AUTHENTICATION");
		if (StringUtils.isEmpty(authType)) {
			authType = "simple";
		}
		env.put(Context.SECURITY_AUTHENTICATION, authType);

		// LDAPの Timeout設定
		String timeoutStr =  (String)prop.get("LDAP_TIMEOUT");
		if(!StringUtils.isEmpty(timeoutStr) && CheckUtil.isInteger(timeoutStr)){
			int timeoutInt = Integer.valueOf(timeoutStr);
			env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(timeoutInt));
		} else {
			int timeoutInt = 10000;
			env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(timeoutInt));
		}

		// LDAPのユーザ名
		ldapUserDn = String.format(ldapUserDn, username);
		env.put(Context.SECURITY_PRINCIPAL,ldapUserDn );
		// LDAPのパスワード
		env.put(Context.SECURITY_CREDENTIALS, pwd);
		DirContext ctx = null;

		try {
		      ctx = new InitialDirContext(env);
		      isLdapValidationOk = true;
		    }
		    catch (javax.naming.AuthenticationException e) {
		    	isLdapValidationOk = false;
		    }
		    catch (Exception e) {
			logger.warn(e.getMessage());
		    	isLdapValidationOk = false;
		    }finally{

			    if (ctx != null) {
			      try {
			        ctx.close();
			      }
			      catch (NamingException e) {
			    	  isLdapValidationOk = false;
			      }
			    }
		    }
		    return isLdapValidationOk;
	}

	/***
	 * HTTP 認証
	 * @param prop
	 * @param username
	 * @param pwd
	 * @return
	 */
	public  static boolean httpCertification(Properties prop, String username, String password) {
		boolean isValidationOk = false;

		String prop_url = (String)prop.get("HTTP_URL");
		String prop_user = (String)prop.get("HTTP_USER");
		String prop_realm = (String)prop.get("HTTP_REALM");
		String prop_timeout = (String)prop.get("HTTP_TIMEOUT");

		if (StringUtils.isEmpty(prop_url)) {
                        return false;
                }

		String auth_user = username;
		if (!StringUtils.isEmpty(prop_user)) {
			auth_user = String.format(prop_user, username);
		}

		try {
			HttpClient client = new HttpClient();

			// timeout
			Integer timeout = new Integer(10000);	// msec
			if (!StringUtils.isEmpty(prop_timeout)) {
				timeout = Integer.valueOf(prop_timeout);
			}
			client.getParams().setParameter("http.socket.timeout", timeout);

			String realm = AuthScope.ANY_REALM;
			if (!StringUtils.isEmpty(prop_realm)) {
				realm = prop_realm;
			}

			// authentication
                        java.net.URI uri = new java.net.URI(prop_url);
                        AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), realm);
                        Credentials cred = new UsernamePasswordCredentials(auth_user, password);
                        client.getState().setCredentials(scope, cred);

			// head method
			HeadMethod head = new HeadMethod(prop_url);
			head.setDoAuthentication( true );
			head.setFollowRedirects(false);

			// execute method
			int status = client.executeMethod( head );
			if (status == HttpStatus.SC_OK) {
				isValidationOk = true;
			}
			head.releaseConnection();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			isValidationOk = false;
		}
		return isValidationOk;
	}
	/***
	 * Propertyを取得する
	 * @param filename ファイル名
	 * @return Property
	 * @throws IOException
	 */
	public static Properties getPropertiesContext(String filename) throws IOException {
		String path = Thread.currentThread().getContextClassLoader()
			.getResource("").getPath();
		path = path + filename;
		return getPropertiesByFullPath(path);
	}

	/***
	 * Propertyを取得する
	 * @param fullPath ファイルフルパス
	 * @return Property
	 * @throws IOException
	 */
	public static Properties getPropertiesByFullPath(String fullPath) throws IOException {
		Properties props = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(URLDecoder.decode(fullPath, APConst.ENCODE_UTF_8));
			props = new Properties();
			props.load(in);
		} catch (IOException e) {
			throw e;
		}finally{
			try {
				if (null != in) {
					in.close();
					in = null;
				}
			} catch (IOException ex) {
			}
		}

		return props;
	}


	/***
	 * アクセス制御のチェックを行う
	 * @param pid プロジェクト識別子名
	 * @param permitList アクセス権限情報のリスト
	 * @return
	 */
	public static boolean  havePermitAccessCheck(String pid, List<String> permitList) throws SQLException{
		ProjectDao dao = null;
		dao = new ProjectDaoImpl();
		boolean havePermit = true;
		ProjectInfoEntity bean = dao.getProjectInfo(pid);
		// pidにより、レコードを取得できない場合
		if(null == bean){
			return false;
		}

		// アクセス制御のチェックを行う
		havePermit = bean.getRestrictedBooleanValue();
		// 権限制御がない場合 havePermit = false
		if(!havePermit){
			return true;
		}

		if(null == permitList || permitList.size() <= 0){
			// セッションに、アクセス権限情報のリストを取得されてない場合
			return false;
		}else if(permitList.contains(pid)){
			// アクセス権限情報のリストに、プロジェクト識別子名が存在する場合
			return true;
		}

		return false;
	}

	/**
	 * 文字列をエスケープする
	 *
	 * @param sqlParam SQL文のパラメータ
	 * @return エスケープ後のSQLパラメータ
	 */
	public static String escapeStr(String sqlParam) {
		if (StringUtils.isEmpty(sqlParam))
			return sqlParam;
		String[] escapeChars = {"\\\\", "_", "%"};
		for (String escapChar: escapeChars) {
			sqlParam = sqlParam.replaceAll(escapChar, "\\\\"+escapChar);
		}

		return sqlParam;
	}

	/**
	 * 文字列をエスケープする
	 *
	 * @param sqlParam SQL文のパラメータ
	 * @return エスケープ後のSQLパラメータ
	 */
	 @SuppressWarnings("unchecked")
	public static <K, V extends Object> Map<K, V> escapeStr(Map<K, V> map) {
		if (null == map || map.size() == 0)
			return map;
		Set<K> keys = map.keySet();
		Iterator<K> i = keys.iterator();
		K key  = null;
		V value  = null;
		while (i.hasNext()) {
			key = i.next();
			value  = map.get(key);
			if (value instanceof String) {
				map.put(key, (V)escapeStr((String)value));
			}
		}

		return map;
	}

	/**
	 * 該当プロジェクト実行ステータスを判断する
	 *
	 * @param title プロジェクトタイトル
	 * @return true:実行中;false:実行中以外
	 */
	public static boolean checkRunning(String title) {

		return Scm.isRunning(title, APConst.SIMPLE_GROUP)
			|| Scm.isRunning(title, APConst.SCHEDULER_GROUP);
	}

	public static void appendMsg2BufUnique(String msg, StringBuffer msgBuf) {
		if (null == msg || null == msgBuf) return;
		if (-1 == msgBuf.indexOf(msg))
			msgBuf.append(msg);
	}

	/**
	 * ApPropertiesから許可されていないパスを取得する
	 *
	 * @return
	 */
	public static List<String> getNotAllowedLocalPath() {
		String path = APProperties.getProperty(APConst.NOT_ALLOWED_LOCAL_PATH);
		if (StringUtils.isEmpty(path)) {
			path = APConst.PATH_LOCAL;
		}

		String[] pathArr = path.split(APConst.PATH_SPLITER);
		List<String> list = new ArrayList<String>();
		for (String dir : pathArr) {
			File f = new File(dir);
			while (f != null) {
				list.add(f.getPath());
				f = f.getParentFile();
			}
		}
		return list;
	}

// Added by wubo on 2010/08/30 for V2.1対応 Start
	/**
	 * ApPropertiesから許可されていないパスを取得する
	 *
	 * @return
	 */
	public static List<String> getNotAllowedWindowsLocalPath() {
		List<String> list = new ArrayList<String>();
		String path = APProperties.getProperty(APConst.NOT_ALLOWED_LOCAL_PATH);
		if (!StringUtils.isEmpty(path)) {
			String[] pathArr = path.split(APConst.PATH_SPLITER_WINDOWS);
			for (String dir : pathArr) {
				File f = new File(dir);
				while (f != null) {
					list.add(f.getPath().toLowerCase());
					f = f.getParentFile();
				}
			}
		}

		return list;
	}
// Added by wubo on 2010/08/30 for V2.1対応 End

	/**
	 * パスの重複的な「/」を一つに変換
	 *
	 * @param path
	 * @return
	 */
	public static String replaceDupSlash(String path) {
		if (StringUtils.isEmpty(path))
			return path;
		path = path.replaceAll("["+APConst.PATH_LOCAL+"]+", APConst.PATH_LOCAL);
		if (path.length() > 1 && path.endsWith(APConst.PATH_LOCAL))
			path = path.substring(0, path.length() - 1);
		return path;
	}

	/**
	 * 指定された桁数で、文字列をカット
	 *
	 * @param src
	 * @param limit
	 * @return
	 */
	public static String cutString(String src, int limit) {
		if (StringUtils.isEmpty(src) || limit < 0)
			return src;
		for (int i = 0, w = 0; i < src.length(); i++) {
			char c = src.charAt(i);
			w += (c > 255) ? 2 : 1;
			if (w > limit) {
				return src.substring(0, i) + APConst.LIMIT_STR;
			}
		}
		return src;
	}

	// Added by wubo on 2010/08/30 for V2.1対応 Start
	/**
	 * Windows OSを判断
	 *
	 * @return
	 */
	public static boolean isWinOS() {
		String osName =  System.getProperties().getProperty("os.name");
		if (osName.toLowerCase().contains("windows")) {
			return true;
		} else {
			return false;
		}
	}
	// Added by wubo on 2010/08/30 for V2.1対応 End
}
