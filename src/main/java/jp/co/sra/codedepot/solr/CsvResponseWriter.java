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

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import java.io.Writer;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.request.QueryResponseWriter;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.SolrIndexSearcher;

import jp.co.sra.codedepot.util.CsvPrinter;

public class CsvResponseWriter implements QueryResponseWriter {
    final static String CSV_CONTENT_TYPE = "text/plain; charset=UTF-8";

    @Override
    public void init(NamedList list) {
	/* NOOP */
    }

    @Override
    public void write(Writer writer,
	SolrQueryRequest request, SolrQueryResponse response) throws IOException {

	CsvPrinter csv = new CsvPrinter(writer);

	NamedList values = response.getValues();
	NamedList header = (NamedList)(values.get("responseHeader"));
	DocList result = (DocList)(values.get("response"));
	NamedList highlight = (NamedList)values.get("highlighting");

	// dump query

	StringBuffer sb = new StringBuffer();

	sb.append("[");
	String query = request.getParams().get("q");
	if (query != null) {
		sb.append(query);
	}
	//sb.append("\"");

	String[] fparams = {"fq", "dq"};
	for (String k : fparams) {
		String q = request.getParams().get(k);
		if (q != null) {
			sb.append(" ");
			sb.append(q);
		}
	}

	String[] cparams = {"qt", "qslop", "dslop"};
	for (String k : cparams) {
		String q = request.getParams().get(k);
		if (q != null) {
			sb.append(" ");
			sb.append(k);
			sb.append(":");
			sb.append(q);
		}
	}
	sb.append("]");
	// dump query

	csv.print("#query");
	csv.print(sb.toString());
	csv.println();

	// dump result

	csv.print("#found");
	if (result != null) {
    		Integer found = new Integer(result.matches());
    		csv.print(found.toString());
	} else {
    		csv.print("0");
	}
	csv.println();

	// dump list of result

	if (result != null) {
		SolrIndexSearcher searcher = request.getSearcher();
		Set<String>fields = response.getReturnFields();
		String[] columns = {"location", "prj", "license"};

		csv.print("#no");
		csv.print("#path");
		csv.print("#project");
		csv.print("#license");
		if (highlight != null) {
			csv.print("#code");
		}
		csv.println();

		int line = 1;
    		DocIterator iterator = result.iterator();
		while (iterator.hasNext()) {
      			int id = iterator.nextDoc();
      		    	Document doc = searcher.doc(id, fields);

			writeVal(csv, String.valueOf(line++));
		    	for (String name : columns) {
				Fieldable field = doc.getFieldable(name);
				if (field != null) {
		    			writeVal(csv, field.stringValue());
				} else {
		    			writeVal(csv, null);
				}
			}

			// dump highlight
			if (highlight != null) {
				Fieldable field = doc.getFieldable("id");
				Object hl = highlight.get(field.stringValue());
				if (hl == null) {
		    			writeVal(csv, "");
				} else if (hl instanceof NamedList) {
					NamedList nl = (NamedList)hl;
					Object src = nl.get("src");
		    			writeVal(csv, src);
				} else if (hl instanceof NamedList[]) {
					NamedList[] nlist = (NamedList[])hl;
					StringBuffer tmp = new StringBuffer();
					for (NamedList nl : nlist) {
						Object src = nl.get("src");
						Object start = nl.get("start");
					        String[] lines = src.toString().split("\n");
						int lineno = Integer.parseInt(start.toString());
						for (int r = 0; r < lines.length; r++) {
		    				    tmp.append(String.valueOf(lineno + r) + ":");
		    				    tmp.append(lines[r] + "\n");
						}
					}
		    			writeVal(csv, tmp.toString().trim());
				} else {
		    			writeVal(csv, hl);
				}
			}
			csv.println();
		}
	}
    }

    @Override
    public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
	return CSV_CONTENT_TYPE;
    }

    public void writeVal(CsvPrinter csv, Object val) throws IOException {
	if (val==null) {
	    csv.print("");
	} else if (val instanceof String) {
            String v = (String)val;
	    csv.print(v.replace("\r", "").replace("\n", "\r\n"));
	} else if (val instanceof Object[]) {
	    Object[] array = (Object[])val;
	    for (Object o : array) {
		writeVal(csv, o);
	    }
	} else if (val instanceof List) {
	    List list = (List)val;
	    for (Object o : list) {
		writeVal(csv, o);
	    }
	} else {
	    csv.print(val.toString());
	}
    }
}
