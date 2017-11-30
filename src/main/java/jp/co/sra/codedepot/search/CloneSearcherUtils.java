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
package jp.co.sra.codedepot.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import jp.co.sra.codedepot.index.java.JavaProgramAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.spans.Spans;

/**
 * place holder for common functionalities to be factored out later.
 * @author ye
 *
 */
public class CloneSearcherUtils {

	private Searcher _searcher;
	private Analyzer _analyzer;
	private String LOCATION, SRC;
	private int _slop;

	public CloneSearcherUtils(Searcher searcher, Analyzer analyzer) {
		_searcher = searcher;
		_analyzer = analyzer;
	}

	public CloneSearcherUtils(Searcher searcher) {
		this(searcher, new JavaProgramAnalyzer());
	}

	//update to lucene 2.9.1
	//public void pprint(PayloadSpans s) throws IOException {
	public void pprint(Spans s) throws IOException {
		StringBuilder ss = new StringBuilder("----Spans Results----\n");
		System.out.println(s);
		int count = 1;
		while (s.next() ) {
			int docNo = s.doc();
			Document doc = _searcher.doc(docNo);
			String path = doc.get(LOCATION);
			// ss.append(s);
			// ss.append("\nPayload: ").append(s.getPayload().toArray(a));
			ss.append("\n==Result " + count++ + "==\n");
			ss.append("Doc No.:" + docNo + "\n");
			ss.append("Doc Path:" + path + "\n");
			// ss.append("\""+indexReader.document(s.doc()).getField(FieldName).stringValue() + "\" ");
			ss.append("Tokens from " + s.start() +" to " + s.end() + "\n");
			ss.append("Matching Parts: \n");
			// ss.append(jp.co.sra.ye.util.FileUtils.getLines(path, s.start(), s.end()));
			if (s.isPayloadAvailable()) {
				ss.append(getFragments(s, doc));
			}
		}
		ss.append("----END OF RESULTS----\n");
		System.out.print(ss);
	}

	public void setSlop(int slop) {
		_slop = slop;
	}

	/**
	 * return the code fragments indicated by the offset information
	 * stored in payloads.
	 */
	//update to lucene 2.9.1
	//public String getFragments(PayloadSpans s, Document doc) throws IOException {
	public String getFragments(Spans s, Document doc) throws IOException {
		Collection payloads = s.getPayload();
		StringBuffer sb = new StringBuffer(1024);
		String src = doc.get(SRC);
		System.out.println("doc is: " + doc.get(LOCATION));
		// sb.append("\nspans type: ").append(s.getClass()).append("\n");
		// sb.append(payloads.size()).append(" ").append(payloads.getClass()).append("\n");
		// Byte[] bs = (Byte[])payloads.toArray(new Byte[0]);
		for (Iterator<byte[]> iterator = payloads.iterator(); iterator.hasNext(); ) {
			byte[] bs= iterator.next();
			// for (byte b: bs) System.out.format("%h%n", b);
			int start = PayloadHelper.decodeInt(bs, 0);
			int end = PayloadHelper.decodeInt(bs, 4);
			System.out.format("**start=%d, end=%d%n**", start, end);
			sb.append("  Chars ").append(start).append("-").append(end).append(": ");
			System.out.format("%s%n", src.substring(start, end));
			sb.append(src.substring(start, end)).append("\n");
			/*
			for (byte b: bs) {
				sb.append(b).append(" ");
			}
			sb.append("===>");
			sb.append(PayloadHelper.decodeInt(bs, 0)).append(", ");
			sb.append(PayloadHelper.decodeInt(bs, 4));
			sb.append("\n");
			*/
		}
		return sb.toString();
	}
}
