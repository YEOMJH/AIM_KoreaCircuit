package kr.co.aim.messolution.query.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.SqlCursorItemReader;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class QueryServiceImpl {
	private static Log log = LogFactory.getLog(QueryServiceImpl.class);

	public String getQueryResult(String messageName,
			                     String sourceSubject,
			                     String targetSubject,
			                     String transactionId,
			                     String queryId, String version, Element bindElement) 
		throws Exception
	{
		if(log.isInfoEnabled())
			log.info(String.format("GetQueryResult [%s %s] prepairing.....", queryId, version));

		String usrSql = "SELECT queryString FROM CT_CustomQuery WHERE queryId = ? and version = ? ";

		String[] args = new String[] { queryId, version };

		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(usrSql, args);

		if (resultList.size() == 0)
			throw new CustomException("CanNotFoundQueryID", queryId, version);

		ListOrderedMap queryMap = (ListOrderedMap) resultList.get(0);
		String dbSql = queryMap.get("queryString").toString();
	
		resultList.clear();
	
		String result = "";
		if(StringUtils.indexOf(dbSql, "?") != -1){
			Object[] bind = getBindObjectByElement(bindElement);
			
			
			
			SqlCursorItemReader reader = GenericServiceProxy.getSqlMesTemplate().queryByCursor(dbSql, bind);						
			try {
				result = QueryServiceUtil.createXmlByList(reader, messageName, sourceSubject,
																  targetSubject, transactionId, queryId, version);
			
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			try {
				Map bindMap = getBindMapByElement(bindElement);
				
				
								
				List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(dbSql, bindMap);
				//result = new QueryServiceProxy().createXml(resultList, queryID);
				result = QueryServiceUtil.createXmlByList(sqlResult, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
				//log.info("QueryID: " + queryId + ", Version: " + version + ", QueryResult: " + sqlResult.size());
				log.info(String.format("QueryID: %s, Version: %s, QueryResult: %d", queryId, version, sqlResult.size()));
				
				
			} catch (Exception e) {
				log.error(e);
			}
		}
		return result;
	}
	
	
	public String getQueryResult(String messageName,
					            String sourceSubject,
					            String targetSubject,
					            String transactionId,
					            String queryId, String version,
					            Element condElement,
					            Element bindElement) 
					throws Exception
	{
		if (log.isInfoEnabled())
		{
			log.info(String.format("GetQueryResult [%s %s] prepairing.....", queryId, version));
		}

		String usrSql = "SELECT QUERYSTRING, QUERYSTRINGEXTEND FROM CT_CUSTOMQUERY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("QUERYID", queryId);
		args.put("VERSION", version);

		List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(usrSql, args);

		if (resultList.size() == 0)
		{
			throw new CustomException("SYS-0012", queryId, version);
		}

		String dbSql = ConvertUtil.getMapValueByName(resultList.get(0), "QUERYSTRING");
		String dbSqlExtended = ConvertUtil.getMapValueByName(resultList.get(0), "QUERYSTRINGEXTEND");
		
		if (StringUtils.isNotEmpty(dbSqlExtended))
		{
			dbSql = dbSql + " " + dbSqlExtended;
		}

		resultList.clear();

		String result = "";

		// condition statement replacement
		// code is '{CODE}'
		if (condElement != null)
		{
			Map phraseMap = getBindMapByElement(condElement);

			if (phraseMap != null)
			{
				for (Object keyValue : phraseMap.keySet())
				{
					String phraseString = (String) phraseMap.get(keyValue.toString());

					if (phraseString != null)
						dbSql = StringUtil.replace(dbSql, new StringBuilder().append("{").append(keyValue.toString()).append("}").toString(), phraseString);
				}

				log.info("Dynamic SQL conversion complete");
			}
		}

		if (StringUtils.indexOf(dbSql, "?") != -1)
		{
			Object[] bind = getBindObjectByElement(bindElement);

			SqlCursorItemReader reader = GenericServiceProxy.getSqlMesTemplate().queryByCursor(dbSql, bind);

			try
			{
				result = QueryServiceUtil.createXmlByList(reader, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
			}
			catch (Exception e)
			{
				log.error(e);
			}
		}
		else
		{
			try
			{
				Map bindMap = getBindMapByElement(bindElement);

				if (log.isInfoEnabled() && bindMap != null)
				{
					log.info(">> BIND INFO : [" + bindMap.toString() + "]");
				}

				if (log.isDebugEnabled())
				{
					log.debug(String.format(">> SQL : [%s]", dbSql));
				}

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(dbSql, bindMap);

				result = QueryServiceUtil.createXmlByList(sqlResult, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
				log.info(String.format("QueryID: %s, Version: %s, QueryResult: %d", queryId, version, sqlResult.size()));

			}
			catch (Exception e)
			{
				log.error(e);
			}
		}

		return result;
	}


	public Document getName(String ruleName, long quantity, Element nameRuleAttrElement, Document doc)
		throws CustomException
	{
		if (nameRuleAttrElement != null)
		{
			//name parameter is mandatory
			Map bindMap = getBindMapByElement(nameRuleAttrElement);
			
			List<String> lstName = CommonUtil.generateNameByNamingRule(ruleName, bindMap, quantity);
			
			Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
			//bodyElement.removeChild("BINDV");
			
			Element resultListElement = new Element("DATALIST");
			
			for (String name : lstName)
			{
				Element resultElement = new Element("DATA");
				{
					Element valueElement = new Element("NAMEVALUE");
					valueElement.setText(name);
					resultElement.addContent(valueElement);
				}
				
				resultListElement.addContent(resultElement);
			}
			bodyElement.addContent(resultListElement);
		}
		
		return doc;
	}

	
	private Object[] getBindObjectByElement(Element bindElement){
		List<Object> returnBindObject = new ArrayList<Object>();
		if(bindElement == null)
			return null;
		List<Element> bindElementList = bindElement.getChildren();
		for(int i = 0;i < bindElementList.size(); i++){
			returnBindObject.add(bindElementList.get(i).getText());
		}		
		return returnBindObject.toArray();
	}

	
	private Map getBindMapByElement(Element bindElement){
		Map<String, Object> returnBindMap = new HashMap<String, Object>();
		if(bindElement == null)
			return null;
		List<Element> bindElementList = bindElement.getChildren();
		for(int i = 0;i < bindElementList.size(); i++){
			
			if(bindElementList.get(i).getText().contains("LISTON:"))
			{
				if (bindElementList.get(i).getText().contains(","))
				{
					String[] strArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(bindElementList.get(i).getText().replace("LISTON:", ""));
					returnBindMap.put(bindElementList.get(i).getName(), Arrays.asList(strArray));
				}
				else
				{
					returnBindMap.put(bindElementList.get(i).getName(), bindElementList.get(i).getText().replace("LISTON:", ""));
				}
				continue;
			}
			returnBindMap.put(bindElementList.get(i).getName(), bindElementList.get(i).getText());
		}
		return returnBindMap;
	}
}