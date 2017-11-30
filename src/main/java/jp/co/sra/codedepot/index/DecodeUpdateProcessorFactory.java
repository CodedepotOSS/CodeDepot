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
/**
 *
 */
package jp.co.sra.codedepot.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import jp.co.sra.codedepot.solr.Indexer;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import org.postgresql.util.Base64;
import org.slf4j.Logger;

/**
 * When {@link jp.co.sra.codedepot.solr.Indexer} calls
 * {@link org.apache.solr.client.solrj.SolrServer#add(SolrInputDocument)} to index a
 * document, Solr calls {@link org.apache.solr.client.solrj.util.ClientUtils#toXML(SolrInputDocument)}
 * to and escapes XML character entities for all fields that are {@java.lang.String}.
 * <p>
 * This rewriting makes the character position of clone tokens incorrect because single
 * characters like &lt; ^L becomes &amp;lt; &amp;#12 in the stored
 * {@link jp.co.sra.codedepot.solr.Indexer#SRC} field.
 * <p>
 * To avoid XML escape, we need to set the fields to byte array.
 * {@link org.apache.solr.client.solrj.util.ClientUtils#toXML(SolrInputDocument)} uses
 * {@link org.apache.solr.common.util.Base64#byteArrayToBase64(byte[], int, int)} to
 * encode byte arrays into String.
 * <p>
 * This class decodes the encoded fields. Encoded fields are specified in
 * {@link jp.co.sra.codedepot.solr.Indexer#ENCODED_FIELDS} and can be checked using
 * {@link jp.co.sra.codedepot.solr.Indexer#isBase64Encoded(String)}.
 * <p>
 * A better design is to read encoded fields from the solrconfig.xml where this
 * factory class was configured. The problem is we will not be able to run batch
 * indexer.sh program or other index updating programs independent of the
 * CodeDepot engine, because the batch program does not have access to solrconfig.xml.
 *
 * @author ye
 *
 */
public class DecodeUpdateProcessorFactory extends UpdateRequestProcessorFactory
		implements NamedListInitializedPlugin {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DecodeUpdateProcessorFactory.class);

	/**ALTERNATIVE DESIGN
	 * the string to be used in solrconfig.xml to specify which field is encoded
	 *
	public final static String FIELD_ATTR = "fields";

	// the field to be encoded and decoded
	private static String _field_name = null;

	public static boolean isDecoded(String s) {
		return _field_name !=null && _field_name.equals(s);
	}
	public void init(NamedList args) {
		// _field_name = (String)args.get(FIELD_ATTR);
		// expansion: if more than one fields are to be processed the same way,
		// split them along comma(,) to get a list of strings
	}
	*/

	/* (non-Javadoc)
	 * @see org.apache.solr.update.processor.UpdateRequestProcessorFactory#getInstance(org.apache.solr.request.SolrQueryRequest, org.apache.solr.request.SolrQueryResponse, org.apache.solr.update.processor.UpdateRequestProcessor)
	 */
	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req,
			SolrQueryResponse rsp, UpdateRequestProcessor next) {
		return new DecodeUpdateProcessor(next);
	}
}

class DecodeUpdateProcessor extends UpdateRequestProcessor {
	public DecodeUpdateProcessor(UpdateRequestProcessor next) {
		super(next);
	}

	@Override
	public void processAdd(org.apache.solr.update.AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		//System.out.println(doc);
		try {
			for (String field : Indexer.ENCODED_FIELDS) {
				Collection vals = doc.getFieldValues(field);
				if (vals != null) {
					StringBuilder sb = new StringBuilder();
					for (Iterator it = vals.iterator(); it.hasNext(); ) {
						Object v = it.next();
						if (v instanceof String) {
							String encodedString = (String)v;
							byte[] bs = org.apache.solr.common.util.Base64.base64ToByteArray(encodedString);
							sb.append(new String(bs));
						}
					}
					doc.setField(field, sb.toString());
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("While indexing {}\n\t{}", doc.getFieldValue(Indexer.ID), e.getMessage());
		}
		//System.out.println("After decoded\n"+doc);
		super.processAdd(cmd); //calls next UpdateProcessor in the chain
	}
}
