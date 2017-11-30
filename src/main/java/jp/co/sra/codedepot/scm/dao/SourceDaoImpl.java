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
package jp.co.sra.codedepot.scm.dao;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.sra.codedepot.admin.util.APConst;
import jp.co.sra.codedepot.admin.util.APMsgConst;
import jp.co.sra.codedepot.admin.util.Message;
import jp.co.sra.codedepot.admin.util.StringUtils;
import jp.co.sra.codedepot.scm.sqlmap.SqlMapConfig;
import jp.co.sra.codedepot.scm.bo.ScmException;
import jp.co.sra.codedepot.scm.entity.Source;
import com.ibatis.sqlmap.client.SqlMapClient;

public class SourceDaoImpl implements SourceDao {

	@Override
	public void deleteSource(SqlMapClient sqlmap, Source source) throws ScmException{
		try{
			source.setPath(URLEncoder.encode(source.getPath(), APConst.ENCODE_UTF_8));
			sqlmap.delete("source.deleteSource", source);
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}

	@Override
	public ArrayList<Source> getSources(String projectId) throws ScmException{
		try{
			ArrayList<Source> list = new ArrayList<Source>();
			SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("project", projectId);
			List<?> tmp = sqlmap.queryForList("source.getSources", map);
			for(Object o : tmp){
				Source s = (Source)o;
				if(!StringUtils.isEmpty(s.getPath())){
					s.setPath(URLDecoder.decode(s.getPath(), APConst.ENCODE_UTF_8));
				}
				list.add((Source)o);
			}
			return list;
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}

	@Override
	public ArrayList<Source> getSources(String projectId, String lang) throws ScmException{
		try{
			ArrayList<Source> list = new ArrayList<Source>();
			SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("project", projectId);
			map.put("lang", lang);
			List<?> tmp = sqlmap.queryForList("source.getSources", map);
			for(Object o : tmp){
				Source s = (Source)o;
				if(!StringUtils.isEmpty(s.getPath())){
					s.setPath(URLDecoder.decode(s.getPath(), APConst.ENCODE_UTF_8));
				}
				list.add((Source)o);
			}
			return list;
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}

	@Override
	public void insertSource(SqlMapClient sqlmap, Source source) throws ScmException{
		try{
			if(!StringUtils.isEmpty(source.getPath())){
				source.setPath(URLEncoder.encode(source.getPath(), APConst.ENCODE_UTF_8));
			}
			sqlmap.insert("source.insertSource", source);
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}

	@Override
	public void updateSource(SqlMapClient sqlmap, Source source) throws ScmException{
		try{
			if(!StringUtils.isEmpty(source.getPath())){
				source.setPath(URLEncoder.encode(source.getPath(), APConst.ENCODE_UTF_8));
			}
			sqlmap.update("source.updateSource", source);
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}

	@Override
	public void deleteSourceByProject(String projectId) throws ScmException {
		try{
			// add debug log start
			// System.out.println("projectId:" + projectId);
			// add debug log end
			SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
			int ret = sqlmap.delete("source.deleteSourceByProject", projectId);
			// add debug log start
			// System.out.println("source delete result:" + ret);
			// add debug log end
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}

	}

	@Override
	public int getSourceCount() throws ScmException {
		try{
			SqlMapClient sqlmap = SqlMapConfig.getSqlMapClient();
			return (Integer) sqlmap.queryForObject("source.selectCount");
		}catch(Exception e){
			throw new ScmException(new Message(APMsgConst.E_COM_05), e, ScmException.ERROR);
		}
	}
}
