package kr.co.aim.messolution.fmb.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.fmb.service.Db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;

public class FmbDbImpl  implements Db {
	private static Log log = LogFactory.getLog(FmbDbImpl.class);
	private LobHandler lobHandler;
	public JdbcTemplate jdbcTemplate;
	
	public FmbDbImpl() {
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	public String getShopBay(String machineName){
		String sql = "SELECT FACTORYNAME,AREANAME FROM MACHINESPEC where MACHINENAME=?";
		try {
			String shopBay = (String) jdbcTemplate.query(sql, new Object[]{machineName}, new ResultSetExtractor(){

				@Override
				public Object extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					while(rs.next()){
						String factoryName= rs.getString("FACTORYNAME");
						String areaName = rs.getString("AREANAME");
						return factoryName+"."+areaName;
					}
					return null;
				}});
			return shopBay;
		} catch (DataAccessException e) {
			return null;
		}
		
	}
	public Map<String, String> getShopBayMap(){
		String sql = "SELECT MACHINENAME,FACTORYNAME,AREANAME FROM MACHINESPEC";

		try {
			Map<String, String> map = (Map<String, String>) jdbcTemplate.query(sql, new ResultSetExtractor(){

				@Override
				public Object extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					Map<String, String> map = new HashMap<String, String>();
					while(rs.next()){
						String machineName = rs.getString("MACHINENAME");
						String factoryName= rs.getString("FACTORYNAME");
						String areaName = rs.getString("AREANAME");
						map.put(machineName, factoryName+"."+areaName);
					}
					return map;
				}});

			return map;
		} catch (DataAccessException e) {
			return new HashMap<String, String>();
		}
	}
	public Document updateForBindv(Document doc, String queryID, String version, Object binds) {
		List<String> bindList = createBindList(binds);
		String query = getQuery(queryID, version);
		if(query == null) {
			String returnCode = "1";
			String returnMessage = String.format("Not find Query[id=%s, ver=%s]", queryID, version);
			
			DocMessageUtil.setReturnElement(doc, returnCode, returnMessage);
			return doc;
		}
		try {
			
			int update = jdbcTemplate.update(query,bindList.toArray());
			String msg = String.format("Success[update=%d]", update);
			DocMessageUtil.setReturnElement(doc, "0", msg);

		} catch (Throwable e) {
			e.printStackTrace();
			DocMessageUtil.setReturnElement(doc, "1", e.getMessage());
		}
		return doc;
	}

	public Document queryForBindv(Document doc, String queryID, String version, Object binds) {
		List<String> bindList = createBindList(binds);
		String query = getQuery(queryID, version);
		query = query.replaceAll(";\\s*$", "");
		if(query == null) {
			String returnCode = "1";
			String returnMessage = String.format("Not find Query[id=%s, ver=%s]", queryID, version);
			
			DocMessageUtil.setReturnElement(doc, returnCode, returnMessage);
			return doc;
		}
		Element results = null;
		try {
			long time = System.currentTimeMillis();
			results = (Element) jdbcTemplate.query(query,bindList.toArray(),new ReplayResultSetExtractor());
			if(results != null) {
				doc.getRootElement().getChild("body").addContent(results);
				DocMessageUtil.setReturnElement(doc, "0", "success");
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
			DocMessageUtil.setReturnElement(doc, "1", e.getMessage());
		}
		return doc;
	}

	
	
	public String getQuery(String queryID, String version) {
		String query = null;
		try {
			
			String sql = "SELECT QUERYSTRING FROM CT_FMB_CUSTOMQUERY WHERE QUERYID=? AND VERSION=?";
			query = (String)jdbcTemplate.query(sql, new Object[] {queryID, version}, new ResultSetExtractor() {

				@Override
				public Object extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					String sentence = null;
					while (rs.next()) {
						sentence = lobHandler.getClobAsString(rs, 1);
						break;
					}
					return sentence;
				}
			});
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return query;
	}

	public List<String> createBindList(Object binds) {
		List<String> bindList = null;
		if(binds instanceof List) {
			bindList = (List<String>) binds;
		}
		else if(binds instanceof String){
			bindList = new ArrayList<String>();
			bindList.add((String) binds);
		}
		return bindList;
	}
	public class ReplayResultSetExtractor implements ResultSetExtractor{


		public ReplayResultSetExtractor() {
		}

		@Override
		public Object extractData(ResultSet rs) throws SQLException,
				DataAccessException {
			
			int columnCount = rs.getMetaData().getColumnCount();

			Element results = new Element("Results");
			Element metaData = new Element("MetaData");
			results.addContent(metaData);
			for (int i = 1;i<=columnCount;i++) {
				String columnLabel = rs.getMetaData().getColumnLabel(i);
				String columnTypeName = rs.getMetaData().getColumnTypeName(i);
				Element column = new Element("Column");
				column.setAttribute("name",columnLabel);
				column.setAttribute("type",columnTypeName);
				metaData.addContent(column);
			}
			while(rs.next()) {
				Element data = new Element("Data");
				results.addContent(data);
				for (int i = 1;i<=columnCount;i++) {
					data.addContent(new Element("D").setText(""+rs.getObject(i)));
				}
			}
			return results;
		}
		
	}

	@Override
	public String getUserPassword(String userId) {
		String sql = "SELECT PASSWORD FROM USERPROFILE WHERE USERID = ?";
		String password;
		try {
			password = (String) jdbcTemplate.queryForObject(sql, new Object[] {userId}, String.class);
		} catch (Throwable e) {
			return null;
		}
		
		return password;
	}
	
	public Document getUserDocument(Document doc, String queryID, String version) {
		Document result = queryForBindv(doc, queryID, version, new ArrayList<String>());
		return result;
	}

}
