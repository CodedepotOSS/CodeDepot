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

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;
import org.xml.sax.SAXException;
import java.util.Properties;

/** Collection of utility functions to interact with Solr locally
 * @author ye
 * @version $Id: SolrUtils.java 2342 2017-11-09 05:36:32Z fang $
 */
public class SolrUtils {

	/**
	 * Get the SolrCore by the solr_home directory name.
	 * The solr_home directory is also known as instanceDir, it has two
	 * subdirectories, conf and data/index
	 * @param solrHomeDir: the solr_home directory, that has conf/solrconfig.xml and conf/schema.xml
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	static public SolrCore getSolrCore(String solrHomeDir) throws ParserConfigurationException, IOException, SAXException {

		// read and parse solrconfig.xml that is under solrHomeDir/conf
		if (solrHomeDir.charAt(solrHomeDir.length()-1) == '/') {
			solrHomeDir = solrHomeDir.substring(0, solrHomeDir.length());
		}
		SolrConfig config = new SolrConfig(solrHomeDir, "solrconfig.xml", null);
		// read and parse schema.xml that is also under solrHomeDir/conf
		IndexSchema indexSchema = new IndexSchema(config, "schema.xml", null);
		// Now we can get the core
		System.err.flush();
		System.out.println("Loading core");
		// It seems to me that schema and config files were loaded twice.
		// The first parameter is String name; and the last one is CoreDescriptor cd; I guess these two are only used for
		// multi core
		SolrCore core = new SolrCore(null, solrHomeDir+"/data/", config, indexSchema, null);
		return core;
	}

	/**
	 * Return an embedded solr server. Accessing this server does not need http access.
	 * The solrHomeDire is the solr home directory, which has the following files
	 *     conf/solrconfig.xml
	 *     conf/schema.xml
	 *     data/<lucene index files>
	 * @param solrHomeDir is the solr home directory,
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	static public EmbeddedSolrServer getEmbeddedSolrServer(String solrHomeDir) throws ParserConfigurationException, IOException, SAXException {
		System.setProperty("solr.solr.home", solrHomeDir);
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer = initializer.initialize();
		EmbeddedSolrServer server =  new EmbeddedSolrServer(coreContainer, "");
		return server;
	}

	/** return the total number of documents that are indexed
	 * @param solrHomeDir
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws SolrServerException
	 */
	static public Integer getTotalDocNum(String solrHomeDir) throws ParserConfigurationException, IOException, SAXException, SolrServerException {
		SolrServer server = getEmbeddedSolrServer(solrHomeDir);
		LukeRequest req = new LukeRequest();
		LukeResponse res = req.process(server);
		// System.out.println(res);
		return res.getNumDocs();
	}
}
