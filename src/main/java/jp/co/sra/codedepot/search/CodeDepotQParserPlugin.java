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

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.LuceneQParserPlugin;

public class CodeDepotQParserPlugin extends LuceneQParserPlugin {
  private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CodeDepotQParserPlugin.class);

  public static final String NAME = "codedepot";
  @Override
  public QParser createParser(String qstr, SolrParams localParams,
                              SolrParams params, SolrQueryRequest req) {
    logger.debug("createParser for {}", qstr);
    return new CodeDepotQParser(qstr, localParams, params, req);
  }

  /**
   * システム初期化
   */
  @Override
  public void init(NamedList args) {
    // TODO Auto-generated method stub

  }

}
