package kr.co.aim.messolution.query.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.query.MESQueryServiceProxy;
import kr.co.aim.messolution.query.service.QueryServiceUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greenframe.orm.SqlCursorItemReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PagingQuery extends SyncHandler {
	public static Log logger = LogFactory.getLog(PagingQuery.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		eventLog.info(String.format("[%s] started", getClass().getSimpleName()));

		String messageName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "MESSAGENAME");
		String sourceSubjectName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "ORIGINALSOURCESUBJECTNAME");
		String transactionId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TRANSACTIONID");
		String queryId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "QUERYID");
		String version = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "VERSION");
		String targetSubject = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TARGETSUBJECTNAME");
		String pageNum = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "PAGENUMBER");
		String countValue = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "PAGEROWCOUNT");
		
		Element bindElement = null;
		
		int minRow = ( ( Integer.parseInt( pageNum ) - 1 ) * ( Integer.parseInt( countValue ) ) + 1 );
		int maxRow = Integer.parseInt( pageNum ) * Integer.parseInt( countValue );
		
		try
		{
			bindElement = XmlUtil.getNode(doc, "//Message/Body/BINDV");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Element conditionElement = null;
		try
		{
			conditionElement = XmlUtil.getNode(doc, "//Message/Body/BINDP");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String result = "";
		
		String usrSql = "SELECT QUERYSTRING, QUERYSTRINGEXTEND FROM CT_CUSTOMQUERY WHERE queryId = ? and version = ? ";

		String[] args = new String[] { queryId, version };

		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList( usrSql, args );
		
		if (resultList.size() == 0)
		{
			throw new CustomException("SYS-0012", queryId, version);
		}

		ListOrderedMap queryMap = (ListOrderedMap) resultList.get(0);
		String dbSql = ConvertUtil.getMapValueByName(queryMap, "queryString");
		String dbSqlExtended = ConvertUtil.getMapValueByName(queryMap, "queryStringExtend");

		if (StringUtils.isNotEmpty(dbSqlExtended))
		{
			dbSql = dbSql + " " + dbSqlExtended;
		}

		if ( queryMap.get( "bindheader" ) != null && queryMap.get( "bindheader" ).toString().length() > 0 )
		{
			String bindHeader = queryMap.get( "bindheader" ).toString();
			String bindBody = queryMap.get( "bindbody" ).toString();

			Map<String, String> reChange = new HashMap<String, String>();
			String[] paraN = bindHeader.replace( "{", "" ).replace( "}", "" ).split( "[$]" );
			String[] paraV = bindBody.split( "[$]" );
			String[] paraKey = bindHeader.split( "[$]" );

			List<Element> bindElementList = bindElement.getChildren();
			String[] getName = new String[bindElementList.size()];

			for ( int i = 0; i < getName.length; i++ )
			{
				getName[i] = bindElementList.get( i ).getName();
			}

			for ( int i = 0; i < paraN.length; i++ )
			{
				reChange.put( paraKey[i], "" );
				for ( int j = 0; j < getName.length; j++ )
				{
					if ( paraN[i].equals( getName[j] ) )
					{
						reChange.remove( paraKey[i] );
						reChange.put( paraKey[i], paraV[i] );
					}
				}

				dbSql = dbSql.replace( paraKey[i], reChange.get( paraKey[i] ) );
			}
		}

		resultList.clear();

		// condition statement replacement
		// code is '{CODE}'
		if (conditionElement != null)
		{
			Map phraseMap = getBindMapByElement(conditionElement);

			if (phraseMap != null)
			{
				for (Object keyValue : phraseMap.keySet())
				{
					String phraseString = (String) phraseMap.get(keyValue.toString());

					if (phraseString != null)
						dbSql = StringUtil.replace(dbSql, new StringBuilder().append("{").append(keyValue.toString()).append("}").toString(), phraseString);
				}

				eventLog.info("Dynamic SQL conversion complete");
			}
		}

		if ( StringUtils.indexOf( dbSql, "?" ) != -1 )
		{
			Object[] bind = getBindObjectByElement( bindElement );

			if ( bind != null )
			{
				eventLog.debug( ">> SQL = [" + dbSql + "] : [" + bind.toString() + "]" );
				eventLog.info( ">> BIND INFO : [" + bind.toString() + "]" );
			}
			else eventLog.debug( ">> SQL = [" + dbSql + "] : []" );

			SqlCursorItemReader reader = GenericServiceProxy.getSqlMesTemplate().queryByCursor( dbSql, bind );
			try
			{
				result = QueryServiceUtil.createXmlByList( reader, messageName, sourceSubjectName, targetSubject, transactionId, queryId, version );
			}
			catch ( Exception e )
			{
				eventLog.error(e);
			}
		}
		else
		{
			try
			{
				Map bindMap = getBindMapByElement( bindElement );

				if ( bindMap != null )
				{
					eventLog.debug( ">> SQL = [" + dbSql + "] : [" + bindMap.toString() + "]" );
					eventLog.info( ">> BIND INFO : [" + bindMap.toString() + "]" );
				}
				else eventLog.debug( ">> SQL = [" + dbSql + "] : []" );

				String CountdbSql = "SELECT COUNT(*) COUNT FROM (" + dbSql + ")";
				List<Map<String, Object>> CountsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList( CountdbSql, bindMap );
				String count = CountsqlResult.get( 0 ).get( "COUNT" ).toString();

				dbSql = "SELECT * FROM (SELECT ROWNUM X ,A.* FROM (" + dbSql + ")A) WHERE X BETWEEN " + Integer.toString( minRow ) + " AND " + Integer.toString( maxRow );

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList( dbSql, bindMap );

				result = QueryServiceUtil.createXmlByListNameByPaging( sqlResult, messageName, sourceSubjectName, targetSubject, transactionId, queryId, version, count ); // new
																																										// QueryServiceProxy().createXml(resultList,
																																										// queryID);
				
				eventLog.info( "QueryID: " + queryId + ", Version: " + version + ", QueryResult: " + sqlResult.size() );

			}
			catch ( Exception e )
			{
				eventLog.error(e);
			}
		}
		
		if(StringUtil.equals(pageNum, "1"))
		{
			setUseFlagAndIncreaseUseCount(queryId, version);
		}

		return result;
	}

	private void setUseFlag(String queryId, String version)
	{
		if (StringUtils.isNotEmpty(queryId) && StringUtils.isNotEmpty(version))
		{
			try
			{
				String sql = "UPDATE CT_CUSTOMQUERY SET USEFLAG = 'Y', USETIMEKEY = :USETIMEKEY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";
				Map<String, String> args = new HashMap<String, String>();
				args.put("QUERYID", queryId);
				args.put("VERSION", version);
				args.put("USETIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private void setUseFlagAndIncreaseUseCount(String queryId, String version)
	{
		if (StringUtils.isNotEmpty(queryId) && StringUtils.isNotEmpty(version))
		{
			try
			{
				Object[] keySet = new Object[] { queryId, version };
				String lockSql = "SELECT QUERYID, VERSION FROM CT_CUSTOMQUERY WHERE QUERYID = :QUERYID AND VERSION = :VERSION FOR UPDATE";
				List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(lockSql, keySet);
				logger.info("CustomQuery [" + queryId + ", " + version + "] was queried and its use count will be increased.");
				
				String sql = "UPDATE CT_CUSTOMQUERY SET USEFLAG = 'Y', USECOUNT = USECOUNT+1, USETIMEKEY = :USETIMEKEY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";
				Map<String, String> args = new HashMap<String, String>();
				args.put("QUERYID", queryId);
				args.put("VERSION", version);
				args.put("USETIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
				logger.info("CustomQuery [" + queryId + ", " + version + "]'s use count has been increased.");
			}
			catch (Exception e)
			{
			}
		}
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
		Map<String, String> returnBindMap = new HashMap<String, String>();
		if(bindElement == null)
			return null;
		List<Element> bindElementList = bindElement.getChildren();
		for(int i = 0;i < bindElementList.size(); i++){
			returnBindMap.put(bindElementList.get(i).getName(), bindElementList.get(i).getText());
		}
		return returnBindMap;
	}
}
