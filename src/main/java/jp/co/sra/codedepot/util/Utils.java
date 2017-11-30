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

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

public class Utils {

	public static void printTopDocs(TopDocs topdocs, Searcher searcher, String field) throws CorruptIndexException, IOException {
		System.out.println(topDocsToString(topdocs, searcher, field));
	}

	public static String topDocsToString(TopDocs topdocs, Searcher searcher, String field)
		throws CorruptIndexException, IOException {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<topdocs.totalHits; i++) {
			int d = topdocs.scoreDocs[i].doc;
			float score = topdocs.scoreDocs[i].score;
			sb.append(score + "\t" + d + "\t" + searcher.doc(d).get(field));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static int[] extractDocNums(TopDocs topdocs) {
		int[] result = new int[topdocs.totalHits];
		for (int i=0; i<topdocs.totalHits; i++) {
			int d = topdocs.scoreDocs[i].doc;
			result[i] = d;
		}
		return result;
	}
}
