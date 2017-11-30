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
package jp.co.sra.codedepot.util;

import java.util.Random;

/** create random string
 *
 * @author yunwen
 *
 */
public class RandomString {
	public static final char[] numbers = 	{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	public static final char[] lowercase = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
											 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
											 'u', 'v', 'w', 'x', 'y', 'z'};
	public static final char[] uppercase = new String(lowercase).toUpperCase().toCharArray();

	private char[] goodChars; //chars to be used for the random string
	private Random rand; // random generator
	private int length; // the length of string
	private char[] randomString;

	/**
	 * Generate random strings of the given length, with the given random generator, by using
	 * the characters from the given array of characters.
	 * @param length
	 * @param rand
	 * @param goodChars
	 */
	public RandomString(int length, Random rand, char[] goodChars) {
		this.goodChars = goodChars;
		this.rand = rand;
		this.length = length;
		randomString = new char[this.length];
	}

	public String next() {
		for (int i=0; i<randomString.length; i++) {
			randomString[i]=goodChars[rand.nextInt(goodChars.length)];
		}
		return new String(randomString);
	}
}
