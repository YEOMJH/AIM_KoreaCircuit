/*
 ****************************************************************************
 *
 *  (c) Copyright 2009 AIM Systems, Inc. All rights reserved.
 *
 *  This software is proprietary to and embodies the confidential
 *  technology of AIM Systems, Inc. Possession, use, or copying of this
 *  software and media is authorized only pursuant to a valid written
 *  license from AIM Systems, Inc.
 *
 ****************************************************************************
 */

package kr.co.aim.messolution.query.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.orm.SqlCursorItemReader;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;


public class QueryServiceUtil
{
	private static Log log = LogFactory.getLog(QueryServiceUtil.class);

	
	public static String createXmlByList(SqlCursorItemReader reader, String messageName,
            							 String sourceSubject,
            							 String targetSubject,
            							 String transactionId,
            							 String queryID, String version) throws Exception {
		
		if(log.isInfoEnabled()){
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
			log.debug("queryId = " + queryID);
			log.debug("version = " + version);
		}
		
        StringBuilder sXmlMsg = new StringBuilder(50000);
        
        sXmlMsg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ").append(SystemPropHelper.CR);
        sXmlMsg.append("  <Message>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Header>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <MESSAGENAME>").append(messageName).append("</MESSAGENAME>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <SOURCESUBJECT>").append(sourceSubject).append("</SOURCESUBJECT>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <TARGETSUBJECT>").append(targetSubject).append("</TARGETSUBJECT>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <TRANSACTIONID>").append(transactionId).append("</TRANSACTIONID>").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Header>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Body>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <QUERYID>").append(queryID).append("</QUERYID>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <VERSION>").append(version).append("</VERSION>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <DATALIST>").append(SystemPropHelper.CR);
        reader.open();
        int dataSize = 0;
		reader.setListMapper();
		reader.setAppendSpace("          ");
		while(true) 
		{
	        try {
	        	ListOrderedMap orderMap = (ListOrderedMap)reader.read();
	        	if (orderMap == null) break;
				MapIterator map = orderMap.mapIterator();
				dataSize++;
		        sXmlMsg.append("        <DATA>").append(SystemPropHelper.CR);
				while (map.hasNext()) {
					String Key = (String) map.next();
					String value = null;
					if (map.getValue() == null) {
						value = "";
					} else if (map.getValue() instanceof String){
						value = (String) map.getValue();
					}else{
						value = map.getValue().toString();
					}
					
					
					value = replaceIllegalCharacter(value);
					
			        sXmlMsg.append("          <").append(Key).append(">").append(value).append("</").append(Key).append(">").append(SystemPropHelper.CR);
				}
		        sXmlMsg.append("        </DATA>").append(SystemPropHelper.CR);
		        orderMap.clear();
	        } catch (Exception e) {
	        	log.error(e);
	        	break;
	        }
		}
        reader.close();
        sXmlMsg.append("      </DATALIST>").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Body>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <").append(SMessageUtil.Result_ReturnCode).append(">").append("0").append("</").append(SMessageUtil.Result_ReturnCode).append(">").append(SystemPropHelper.CR);
        sXmlMsg.append("      <").append(SMessageUtil.Result_ErrorMessage).append(">").append("</").append(SMessageUtil.Result_ErrorMessage).append(">").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
        sXmlMsg.append("  </Message>").append(SystemPropHelper.CR);
		log.info(">> Create Message : " + dataSize);	
		log.info("After - createXml");
		return sXmlMsg.toString();
	}	

	public static String replaceIllegalCharacter(String value) {
		String str = value.replace("&", "&amp;");
		str = str.replace("<", "&lt;");
		str = str.replace(">", "&gt;");
		return str;
	}

	
	public static String createStringByList(SqlCursorItemReader reader, String messageName,
			                                String sourceSubject,
			                                String targetSubject,
			                                String transactionId,
			                                String queryID, String version) throws Exception {

		if(log.isInfoEnabled()){
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
			log.debug("queryId = " + queryID);
			log.debug("version = " + version);
		}
		
        StringBuilder sStrMsg = new StringBuilder(50000);
        
        sStrMsg.append(messageName).append(".REP ");
        sStrMsg.append("HDR(").append(sourceSubject).append(",").append(targetSubject);
        sStrMsg.append(",").append(transactionId).append(") ");
        sStrMsg.append("QUERYID=").append(queryID).append(" ");
        sStrMsg.append("VERSION=").append(version).append(" ");
        sStrMsg.append("DATA=\"");
        
        reader.open();
        int dataSize = 0;
		reader.setListMapper();
		reader.setAppendSpace("          ");
		while(true) 
		{
	        try {
	        	ListOrderedMap orderMap = (ListOrderedMap)reader.read();
	        	if (orderMap == null) break;
				MapIterator map = orderMap.mapIterator();
				dataSize++;
				while (map.hasNext()) {
					String Key = (String) map.next();
					String value = null;
					if (map.getValue() == null) {
						value = "";
					} else if (map.getValue() instanceof String)
						value = (String) map.getValue();
					else
						value = map.getValue().toString();
					sStrMsg.append(value);
					sStrMsg.append(" ");
				}
				sStrMsg.append(SystemPropHelper.CR);
		        orderMap.clear();
	        } catch (Exception e) {
	        	log.error(e);
	        	break;
	        }
		}
		sStrMsg.append("\"");
        reader.close();
		return sStrMsg.toString();
	}	
	
	
	public static String createXmlByList(List<Map<String, Object>> result,
											String messageName,
											String sourceSubject,
											String targetSubject,
											String transactionId,
											String queryID, String version) throws Exception
	{
	
		if(log.isInfoEnabled())
		{
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
		//	log.debug("queryId = " + queryID);
		//	log.debug("version = " + version);
		}
		
		StringBuilder sXmlMsg = new StringBuilder(50000);
		
		sXmlMsg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ").append(SystemPropHelper.CR);
		sXmlMsg.append("  <Message>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Header>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <MESSAGENAME>").append(messageName).append("</MESSAGENAME>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <SOURCESUBJECT>").append(sourceSubject).append("</SOURCESUBJECT>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <TARGETSUBJECT>").append(targetSubject).append("</TARGETSUBJECT>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <TRANSACTIONID>").append(transactionId).append("</TRANSACTIONID>").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Header>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Body>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <QUERYID>").append(queryID).append("</QUERYID>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <VERSION>").append(version).append("</VERSION>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <DATALIST>").append(SystemPropHelper.CR);

		int dataSize = 0;
		
		for (Map row : result) 
		{
			try
			{
				ListOrderedMap orderMap = (ListOrderedMap)row;
				if (orderMap == null) break;
				
				MapIterator map = orderMap.mapIterator();
				dataSize++;
				
				sXmlMsg.append("        <DATA>").append(SystemPropHelper.CR);
				
				while (map.hasNext())
				{
					String Key = (String) map.next();
					String value = null;
					
					if (map.getValue() == null)
						value = "";
					else if (map.getValue() instanceof String)
						value = (String) map.getValue();
					else
						value = map.getValue().toString();
					
					
					value = replaceIllegalCharacter(value);
					
					sXmlMsg.append("          <").append(Key).append(">").append(value).append("</").append(Key).append(">").append(SystemPropHelper.CR);
				}
				
				sXmlMsg.append("        </DATA>").append(SystemPropHelper.CR);
				orderMap.clear();
			}
			catch (Exception e)
			{
				log.error(e);
				break;
			}
		}
		
		
		sXmlMsg.append("      </DATALIST>").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Body>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <").append(SMessageUtil.Result_ReturnCode).append(">").append("0").append("</").append(SMessageUtil.Result_ReturnCode).append(">").append(SystemPropHelper.CR);
		sXmlMsg.append("      <").append(SMessageUtil.Result_ErrorMessage).append(">").append("</").append(SMessageUtil.Result_ErrorMessage).append(">").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
		sXmlMsg.append("  </Message>").append(SystemPropHelper.CR);
		log.info(">> Create Message : " + dataSize);	
		log.info("After - createXml");
		return sXmlMsg.toString();
	}
	
	public static String createXmlByListNameByPaging( List<Map<String, Object>> sqlResult, String messageName, String sourceSubject, String targetSubject, String transactionId, String queryID,
			String version, String Count ) throws Exception
	{

		if ( log.isInfoEnabled() )
		{
			log.debug( "messageName = " + messageName );
			log.debug( "sourceSubject = " + sourceSubject );
			log.debug( "targetSubject = " + targetSubject );
			log.debug( "transactionId = " + transactionId );
			log.debug( "queryId = " + queryID );
			log.debug( "version = " + version );
		}

		StringBuilder sXmlMsg = new StringBuilder( 50000 );

		sXmlMsg.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " ).append( SystemPropHelper.CR );
		sXmlMsg.append( "  <Message>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    <Header>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <MESSAGENAME>" ).append( messageName ).append( "</MESSAGENAME>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <SOURCESUBJECT>" ).append( sourceSubject ).append( "</SOURCESUBJECT>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <TARGETSUBJECT>" ).append( targetSubject ).append( "</TARGETSUBJECT>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <TRANSACTIONID>" ).append( transactionId ).append( "</TRANSACTIONID>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    </Header>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    <Body>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <QUERYID>" ).append( queryID ).append( "</QUERYID>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <VERSION>" ).append( version ).append( "</VERSION>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "       <DATACOUNT>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "          <" ).append( "COUNT" ).append( ">" ).append( Count ).append( "</" ).append( "COUNT" ).append( ">" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      </DATACOUNT>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <DATALIST>" ).append( SystemPropHelper.CR );

		int dataSize = 0;

		for ( int i = 0; i < sqlResult.size(); i++ )
		{
			ListOrderedMap list = (ListOrderedMap) sqlResult.get( i );

			MapIterator mapIterator = list.mapIterator();

			try
			{
				dataSize++;
				sXmlMsg.append( "        <DATA>" ).append( SystemPropHelper.CR );
				while ( mapIterator.hasNext() )
				{
					String Key = (String) mapIterator.next();
					String value = null;

					if ( mapIterator.getValue() == null )
					{
						value = "";
					}
					else if ( mapIterator.getValue() instanceof String )
					{
						value = (String) mapIterator.getValue();
					}
					else
					{
						value = String.valueOf( mapIterator.getValue() );
					}

					if ( value.indexOf( "<?xml version" ) == -1 )
					{
						// if the value is xml string, do not process replaceIllegalCharacter
						value = replaceIllegalCharacter( value );
					}
					sXmlMsg.append( "          <" ).append( Key ).append( ">" ).append( value ).append( "</" ).append( Key ).append( ">" ).append( SystemPropHelper.CR );
				}
				sXmlMsg.append( "        </DATA>" ).append( SystemPropHelper.CR );
			}
			catch ( Exception e )
			{
				log.error( e );
				break;
			}
		}

		sXmlMsg.append( "      </DATALIST>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    </Body>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    <Return>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "      <" ).append( SMessageUtil.Result_ReturnCode ).append( ">" ).append( "0" ).append( "</" ).append( SMessageUtil.Result_ReturnCode ).append( ">" )
				.append( SystemPropHelper.CR );
		sXmlMsg.append( "      <" ).append( SMessageUtil.Result_ErrorMessage ).append( ">" ).append( "</" ).append( SMessageUtil.Result_ErrorMessage ).append( ">" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "    </Return>" ).append( SystemPropHelper.CR );
		sXmlMsg.append( "  </Message>" ).append( SystemPropHelper.CR );
		log.debug( ">> Create Message : " + dataSize );
		return sXmlMsg.toString();
	}
}
