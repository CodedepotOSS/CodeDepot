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
package jp.co.sra.codedepot.solr;

import jp.co.sra.codedepot.util.RandomString;

import java.net.InetAddress;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Create a unique (hopefully) id for each file
 * @$Id: ProgramUID.java 2342 2017-11-09 05:36:32Z fang $
 * @author yunwen
 *
 */
public class ProgramUID {
	private static Logger logger = Logger.getLogger(ProgramUID.class.getName());

	private static final char[] goodChars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
	private String prefix1, prefix2;
	char[] start, current;
	private int length; //the length of serial UID, serial UID starts with start and goes around

	/**
	 * Create random string generator that guarantees unique for one cycle of execution
	 * of the system, and mostly likely unique even at different execution times. To do so
	 * it uses two prefixes (one is seeded with the current machine time and another is
	 * seeded with inet address).
	 * @param prefix1Length
	 * @param prefix2Length
	 * @param length
	 */
	public ProgramUID(int prefix1Length, int prefix2Length, int length) {
		Random rand = new Random(System.currentTimeMillis());
		prefix1 = new RandomString(prefix1Length,rand, goodChars).next();
		long seed;
		try {
			seed = Long.valueOf(InetAddress.getLocalHost().getHostAddress().replace(".", ""));
			rand = new Random(seed);
		} catch (Exception e) {
			logger.info("Unable to genrate seed for prefix2, use no seed");
			System.out.println(e);
			rand = new Random();
		}
		prefix2 = new RandomString(prefix1Length,rand, goodChars).next();
		rand = new Random();
		this.length = length;
		start = new RandomString(length, rand, goodChars).next().toCharArray();
		current = new char[length];
		System.arraycopy(start, 0, current, 0, this.length);
	}

	public String nextUID() {
		int d = length - 1;
		boolean hasCarry = incrAtIndex(d);
		while (d >= 0 && hasCarry) {
			hasCarry = incrAtIndex(--d);
		}
		if (d < 0) checkOrRestart();
		return prefix1 + prefix2 + new String(current);
	}

	private boolean incrAtIndex(int d) {
		boolean hasCarry = false;
		int i = 0;
		for ( ; i<goodChars.length; i++) {
			if ( current[d] == goodChars[i] )  break;
		}
		hasCarry = (i == goodChars.length - 1);
		current[d] = hasCarry ? goodChars[0] : goodChars[i+1];
		return hasCarry;
	}

	private void checkOrRestart() {
		logger.severe("not implemented yet, indexing real big number now, you should try again after implementing this method.");
	}

	public static void main(String[] args) {
		ProgramUID puid = new ProgramUID(3, 3, 6);
		for (int i=0; i<100; i++) {
			System.out.println(puid.nextUID());
		}
		System.out.println("Second round with a new ProgramUID");
		puid = new ProgramUID(3, 3, 6);
		for (int i=0; i<100; i++) {
			System.out.println(puid.nextUID());
		}
	}
}
