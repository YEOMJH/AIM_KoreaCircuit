package kr.co.aim.messolution.extended.webinterface.service;

import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.util.xml.JdomUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExtendedWebInterfaceServiceUtil implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(ExtendedWebInterfaceServiceUtil.class);
	
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public String generateXml(String bizTransactionId, String consumer, List<Map<String, String>> dataInfoMapList)
	{
		// Make HEAD
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(String.valueOf(dataInfoMapList.size())));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		// Make LIST
		String dataInfoString = "";
		for (Map<String, String> dataInfoMap : dataInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		
		if (StringUtils.isNotEmpty(dataInfoString))
		{
			dataInfoString = "[{" + dataInfoString.substring(0, dataInfoString.length() - 1) + "}]";
		}
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		Element dataElement = new Element("DATA");
		dataElement.addContent(headElement);
		dataElement.addContent(listElement);
		
		return JdomUtils.toString(dataElement);
	}
	
	public String generateXmlWithHeader(String bizTransactionId, String consumer, List<Map<String, String>> headInfoMapList, List<Map<String, String>> bodyInfoMapList)
	{
		// Make HEAD
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(String.valueOf(headInfoMapList.size())));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		// Make LIST
		String dataInfoString = "";
		for (Map<String, String> dataInfoMap : headInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		
		if (StringUtils.isNotEmpty(dataInfoString))
		{
			dataInfoString = "{" + dataInfoString.substring(0, dataInfoString.length() - 1) + ",\"T_ITEM\":[{";
		}
		for (Map<String, String> dataInfoMap : bodyInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		dataInfoString=dataInfoString.substring(0, dataInfoString.length() - 1)+"}]}";
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		Element dataElement = new Element("DATA");
		dataElement.addContent(headElement);
		dataElement.addContent(listElement);
		
		return JdomUtils.toString(dataElement);
	}
	
	public String generateXmlBatch(String bizTransactionId, String consumer, List<List<Map<String, String>>> dataInfoMapList)
	{
		// Make HEAD
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(String.valueOf(dataInfoMapList.size())));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		// Make LIST
		String dataInfoString = "[";
		for (List<Map<String, String>> dataInfoMap : dataInfoMapList) 
		{
			dataInfoString=dataInfoString+"{";
			for(Map<String, String> dataInfo : dataInfoMap)
			{
				String dataString = "";
				for (String itemName : dataInfo.keySet()) 
				{
					String itemValue = dataInfo.get(itemName);
					
					dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
				}
				dataInfoString += dataString;
				
			}
			dataInfoString= dataInfoString.substring(0, dataInfoString.length() - 1)+"},";
		}
		dataInfoString=dataInfoString.substring(0, dataInfoString.length() - 1)+"]";
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		Element dataElement = new Element("DATA");
		dataElement.addContent(headElement);
		dataElement.addContent(listElement);
		
		return JdomUtils.toString(dataElement);
	}
	
	public String generateXmlWithResponse(String bizTransactionId, String consumer, List<Map<String, String>> dataInfoMapList)
	{
		// Make HEAD
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(String.valueOf(dataInfoMapList.size())));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		// Make LIST
		String dataInfoString = "";
		for (Map<String, String> dataInfoMap : dataInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		
		if (StringUtils.isNotEmpty(dataInfoString))
		{
			dataInfoString = "[{" + dataInfoString.substring(0, dataInfoString.length() - 1) + "}]";
		}
		dataInfoString="{\"response\": {\"returnData\":"+dataInfoString+",\"returnCode\": \"S\",\"returnDesc\": \"Seccess\"}}";
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		Element dataElement = new Element("DATA");
		dataElement.addContent(headElement);
		dataElement.addContent(listElement);
		
		return JdomUtils.toString(dataElement);
	}
	
	public Element generateHeaderElement(String bizTransactionId, String consumer, String count)
	{
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(count));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		return headElement;
	}
	
	public Element generateBodyElement(List<Map<String, String>> dataInfoMapList)
	{
		String dataInfoString = "";
		for (Map<String, String> dataInfoMap : dataInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		
		if (StringUtils.isNotEmpty(dataInfoString))
		{
			dataInfoString = "[{" + dataInfoString.substring(0, dataInfoString.length() - 1) + "}]";
		}
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		return listElement;
	}
	
	public String getWSUrl(String mode)
	{
		String url = "";		
		
		log.info("mode = "  + mode);
		
		if ( mode.equals("DEV") )
		{
			url = "http://10.56.200.65:8080/ekp";		
		}
		else if( mode.equals("TEST") )
		{
			url = "http://10.56.200.65:8080/ekp";			
		}
		else if( mode.equals("PROD") )
		{
			url = "http://oa.cecchot.com";
			
		}
		else
		{
			url = "http://10.56.200.65:8080/ekp";
		}
		
		log.info("url = "  + url);
		
		return url;
	}
	
	public String generateXmlForOA(String bizTransactionId, String consumer, List<Map<String, String>> dataInfoMapList)
	{
		// Make HEAD
		Element headElement = new Element("HEAD");
		headElement.addContent(new Element("BIZTRANSACTIONID").setText(bizTransactionId));
		headElement.addContent(new Element("COUNT").setText(String.valueOf(dataInfoMapList.size())));
		headElement.addContent(new Element("CONSUMER").setText(consumer));
		headElement.addContent(new Element("SRVLEVEL").setText("1"));
		headElement.addContent(new Element("USE").setText(""));
		headElement.addContent(new Element("COMMENTS").setText(""));
		
		// Make LIST
		String dataInfoString = "";
		for (Map<String, String> dataInfoMap : dataInfoMapList) 
		{
			String dataString = "";
			for (String itemName : dataInfoMap.keySet()) 
			{
				String itemValue = dataInfoMap.get(itemName);
				
				dataString += "\"" + itemName + "\":\"" + itemValue + "\",";
			}
			dataInfoString += dataString;
		}
		
		if (StringUtils.isNotEmpty(dataInfoString))
		{
			dataInfoString = "{" + dataInfoString.substring(0, dataInfoString.length() - 1) + "}";
		}
		
		Element itemElement = new Element("ITEM");
		itemElement.addContent(new Element("DATAINFO").setText(dataInfoString));
		
		Element listElement = new Element("LIST");
		listElement.addContent(itemElement);
		
		Element dataElement = new Element("DATA");
		dataElement.addContent(headElement);
		dataElement.addContent(listElement);
		
		return JdomUtils.toString(dataElement);
	}
}
