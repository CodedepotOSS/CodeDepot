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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CodeProcess {

	/**
	 * DES暗号化
	 *
	 * @param keybyte
	 * @param src
	 * @return
	 */
	public static byte[] desEncode(byte[] keybyte, byte[] src) {
		try {
			IvParameterSpec iv = new IvParameterSpec(APConst.IV_KEY.getBytes());
			DESKeySpec dks = new DESKeySpec(keybyte);
			// 暗号化
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(APConst.DES_CODE);
			SecretKey key = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(APConst.DES_METHOD);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);

			return cipher.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	/**
	 * DES復号化
	 *
	 * @param keybyte
	 * @param src
	 * @return
	 */
	public static byte[] desDecode(byte[] keybyte, byte[] src) {
		try {
			IvParameterSpec iv = new IvParameterSpec(APConst.IV_KEY.getBytes());
			DESKeySpec dks = new DESKeySpec(keybyte);
			// 復号化
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(APConst.DES_CODE);
			SecretKey key = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(APConst.DES_METHOD);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			return cipher.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	/**
	 * byte配列をHEX文字列に変換
	 *
	 * @param bytes
	 * @param hyph
	 * @return
	 */
	public static String outputHex(byte[] bytes, int hyph) {
		String hs = "";
		for (int i = 0;i < bytes.length;i++)
			hs += String.format("%02x", bytes[i]);
		if (hyph > 0)
			hs = hs.replaceAll("(.{" +hyph +"})", APConst.HYPH+"$1").substring(1);
		return hs;
	}

	/**
	 * base64エンコード
	 *
	 * @param src
	 * @return
	 */
	public static String base64Encode(byte[] src) {
		String requestValue = "";
		try {
			BASE64Encoder base64en = new BASE64Encoder();
			requestValue = base64en.encode(src);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return requestValue;
	}

	/**
	 * base64デコード
	 *
	 * @param str
	 * @return
	 */
	public static byte[] base64Decode(String str) {
		if (str == null || str.equals("")) {
			return "".getBytes();
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			return decoder.decodeBuffer(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "".getBytes();
	}

	/***
	 * SHADigest
	 * @param info
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] SHADigest(String info){

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(APConst.SHA_ALGORITHM);
			digest.update(info.getBytes());
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
