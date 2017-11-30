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
package jp.co.sra.codedepot.solr.java;

import java.io.IOException;
import java.util.ListIterator;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import jp.co.sra.codedepot.parser.java.IndexedClass;
import jp.co.sra.codedepot.parser.java.IndexedCodeFile;
import jp.co.sra.codedepot.parser.java.IndexedMethod;

/**
 * Create indexes for Java files for a solr server. $Id $
 *
 * @author yunwen
 *
 */
public class Indexer {

	// Field names used for indexing and searching
	// must be consistent with schema.xml in the conf directory
	// TODO: read the schema.xml from the conf directory and
	// verify that the following fields exist there
	public static final String ID = "id";
	public static final String SRC = "src";
	public static final String LOCATION = "location";
	public static final String LICENSE = "license";
	public static final String LANG = "lang";
	public static final String UNIT = "unit";
	public static final String FCALL = "fcall";
	public static final String IN_TYPES = "inTypes";
	public static final String OUT_TYPE = "outType";
	public static final String CODE = "code";
	public static final String COMMENT = "comment";
	public static final String FDEF = "fdef";
	public static final String CLS = "cls";
	public static final String PKG = "pkg";
	public static final String PRJ = "prj";
	public static final String CLONETKN = "clonetkn";
	public static final String BEGIN = "begin";

	private static SolrServer _server; // TODO: to be replace with solrServer
	// later

	private static long _methodCount = 0, _fileCount = 0;

	private static String _projectLicense = "unknown";

	/**
	 * Index a parsed Java file. Names of field must be synced with
	 *
	 * @link parser.IndexedCodeFile and
	 * @link parser.IndexedMethod and schema.xml. TODO: need a better way to
	 *       handle these tedious sync work. (move the block of statements that
	 *       create SolrInputDocument to IndexedCodeFile and IndexedMethod is
	 *       another option so that we will have a single point of change. But
	 *       this makes IndexedCodeFile depend on SolrInputDocument.
	 * @param icf
	 * @throws SolrServerException
	 * @throws IOException
	 */
	private static void index(IndexedCodeFile icf) throws SolrServerException,
			IOException {
		String pkg = icf.getPackageName(), license = icf.getLicense(), location = icf
				.getLocation();
		String projectName = icf.getProject();

		// String id;
		SolrInputDocument d;

		if (license == "")
			license = _projectLicense;
		/*
		 * multi for (ListIterator
		 * iter=icls.getDeclaredMethods().listIterator(); iter.hasNext(); ) {
		 * IndexedMethod cmtd = (IndexedMethod) iter.next(); d = new
		 * SolrInputDocument(); d.addField(PRJ, projectName); d.addField(PKG,
		 * pkg); d.addField(CLS, cls); d.addField(FDEF, cmtd.getMethodName());
		 * d.addField(COMMENT, cmtd.getComment()); d.addField(CODE,
		 * cmtd.getCodeText()); d.addField(OUT_TYPE, cmtd.getReturnType());
		 * d.addField(IN_TYPES, cmtd.getInputTypes()); d.addField(FCALL,
		 * cmtd.getMethodInvokeSeq()); d.addField(UNIT, "method");
		 * d.addField(LANG, "java"); d.addField(LICENSE, license);
		 * d.addField(LOCATION, location); d.addField(SRC, cmtd.getSrc()); // id =
		 * pkg + "." + cls + "#" + cmtd.getMethodName() + "(" +
		 * cmtd.getInputTypes() + ")"; //d.addField(ID, cmtd.getId());
		 * d.addField(ID, id); _server.add(d); _methodCount += 1; }
		 */

		for (ListIterator citer = icf.getDeclaredClasses().listIterator(); citer
				.hasNext();) {
			IndexedClass icls = (IndexedClass) citer.next();
			String cls = icls.getClassName();

			for (ListIterator iter = icls.getDeclaredMethods().listIterator(); iter
					.hasNext();) {
				IndexedMethod cmtd = (IndexedMethod) iter.next();
				d = new SolrInputDocument();
				d.addField(PRJ, projectName);
				d.addField(PKG, pkg);
				d.addField(CLS, cls);
				d.addField(BEGIN, cmtd.getStart());
				d.addField(FDEF, cmtd.getMethodName());
				d.addField(COMMENT, cmtd.getComment());
				d.addField(CODE, cmtd.getCodeText());
				d.addField(OUT_TYPE, cmtd.getReturnType());
				d.addField(IN_TYPES, cmtd.getInputTypes());
				d.addField(FCALL, cmtd.getMethodInvokeSeq());
				d.addField(UNIT, "method");
				d.addField(LANG, "java");
				d.addField(LICENSE, license);
				d.addField(LOCATION, location);
				d.addField(SRC, cmtd.getSrc());
				// d.addField(CLONETKN, cmtd.getSrc());
				// id = pkg + "." + cls + "#" + cmtd.getMethodName() + "(" +
				// cmtd.getInputTypes() + ")";
				// d.addField(ID, cmtd.getId());
				d.addField(ID, cmtd.getId());
				_server.add(d);
				_methodCount += 1;
			}
			d = new SolrInputDocument();
			d.addField(PRJ, projectName);
			d.addField(PKG, pkg);
			d.addField(CLS, cls);
			d.addField(BEGIN, icls.getStart());
			d.addField(FDEF, icls.getDeclaredMethodIds());
			d.addField(COMMENT, icls.getComment());
			d.addField(CODE, icls.getCodeText());
			d.addField(LOCATION, location);
			d.addField(UNIT, "class");
			d.addField(LANG, "java");
			d.addField(LICENSE, license);
			d.addField(FCALL, icls.getMethodInvokeSeq());
			d.addField(SRC, icls.getSrc());
			// id = pkg + "." + cls;
			// use location as id for performance check
			d.addField(ID, icls.getId());
			// d.addField(ID, location); // used to identify the uniqueness of
			// the code, if we are going
			// to store several version of the code, we need to add version
			// number here.
			_server.add(d);
			_fileCount += 1;
		}
	}
}
