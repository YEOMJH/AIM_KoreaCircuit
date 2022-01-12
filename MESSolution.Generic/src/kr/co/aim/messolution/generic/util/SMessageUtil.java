package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.*;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.greenframe.util.msg.MessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

public class SMessageUtil {

	private static 	Log 								log = LogFactory.getLog(MessageUtil.class);
	
	public static final String 							BPELName_Tag = "bpelname";
	public static final String 							Message_Tag = "Message";
	public static final String 							DSPMessage_Tag = "ENVELOP";
	public static final String 							MessageName_Tag = "MESSAGENAME";
	public static final String 							Header_Tag = "Header";
	public static final String                          Listener_Tag = "listener";
	public static final String 							Body_Tag = "Body";
	public static final String 							Return_Tag = "Return";
	public static final String 							Result_Name_Tag				= "Return";	        
	public static final String							Result_ReturnCode			= "RETURNCODE";
	public static final String							Result_ErrorMessage			= "RETURNMESSAGE";
	
	private static final String 						ReplySubjectName_Tag = "ORIGINALSOURCESUBJECTNAME";
	public static final String                          DataFieldName = "xmlData";

	public static String getListenerBpelName(Document doc)                                    
	{                   
		String Listener = null;
		try {           
			Listener = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + Listener_Tag);
			if (Listener != null && Listener.length() > 0) {
				if (!Listener.toLowerCase().endsWith(".bpel"))
					Listener = Listener + ".bpel";
			}
		} catch (Exception e) {                                                                 
		}                                                                                       
		return Listener;                                                                              
	} 

	public static String getBpelName(Document doc)                                    
	{                   
		String BpelName = null;
		try {           
			BpelName = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + MessageName_Tag);
			
			// for SPC Message
			if(BpelName.isEmpty())
			{
				BpelName = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + "Head" + "/" + "messageName");	
			}
			
			if (BpelName != null && BpelName.length() > 0) {
				if (!BpelName.toLowerCase().endsWith(".bpel"))
					BpelName = BpelName + ".bpel";
			}
			
		} catch (Exception e) {                                                                 
		}                                                                                       
		return BpelName;                                                                              
	}      

	public static Document getDocumentFromTibrvMsg(TibrvMsg data, String dataFieldTag)
	{
		
		Document document = null;    
		String strMsg = "";
		String replySubjectName = "";
		
		if (data instanceof TibrvMsg)
		{
			TibrvMsg TibrvMsg = (TibrvMsg)data;
			try
			{
				strMsg = (String)TibrvMsg.get(dataFieldTag);
				replySubjectName = (String)TibrvMsg.getReplySubject();
				if (replySubjectName == null) replySubjectName = "";
			}
			catch (TibrvException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//convert DOM object with SAX
		try
		{
			document = JdomUtils.loadText(strMsg);			
		}
		catch (Exception e) 
		{
			try {
				document = createXmlDocument(strMsg);
			} catch (Exception ex) {
				log.error(ex, ex);
			}
		}
		
		//original source subject management
		String existingSourceSubjectName = getElement("//" + Message_Tag + "/" + Header_Tag + "/", document, ReplySubjectName_Tag);
		log.debug("existing original source subject : " + existingSourceSubjectName);
		
		//store reply subject name for sendrequest style
		if ( replySubjectName.length() > 0 && existingSourceSubjectName.isEmpty())
		{
			setItemValue(document, Header_Tag, ReplySubjectName_Tag, replySubjectName);
		}
		
		return document;
	}

	public static String replyXMLMessage(String ReturnCode)
	{
		Document doc = JdomUtils.createDocument(Message_Tag);
		SMessageUtil.setResultItemValue(doc, Result_ReturnCode, ReturnCode);
		SMessageUtil.setResultItemValue(doc, Result_ErrorMessage, "");
		String replyMSG = JdomUtils.toString(doc);
		return replyMSG;
	}

	public static void setElement(String nodePath, Document doc, String nodeName, String nodeValue)
	{
		if (nodeValue == null) nodeValue = "";
		try {
			Element element = JdomUtils.getNode(doc, nodePath + nodeName);
			if (element != null) {
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				JdomUtils.setNodeText(doc, nodePath, nodeName, nodeValue);
			}
			else {
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				Element parentElement = JdomUtils.getNode(doc, nodePath);
				Element newElement = JdomUtils.addElement(parentElement, nodeName, nodeValue);
			}
		} catch (Exception e) {                                                                 
			e.printStackTrace();                                                                
		}                                                                                                                                                                  
	}
	
	public static String getElement(String nodePath, Document doc, String nodeName)
	{
		String nodeValue = "";
		
		try {
			Element element = JdomUtils.getNode(doc, nodePath + nodeName);
			
			if (element != null)
			{
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				nodeValue = JdomUtils.getNodeText(doc, nodePath + "/" + nodeName);
			}
			
		} catch (Exception e) {                                                                 
			e.printStackTrace();
			nodeValue = "";
		}
		
		return nodeValue;
	}
	

	public static void setResultItemValue(Document doc, String nodeName, String nodeValue)                                    
	{
		if (doc.getRootElement().getChild(Result_Name_Tag) == null)
			doc.getRootElement().addContent(new Element(Result_Name_Tag));
		
		String nodePath = "//" + Message_Tag + "/" + Result_Name_Tag + "/";
		setElement(nodePath, doc, nodeName, nodeValue);

		if (nodeName.equalsIgnoreCase(SMessageUtil.Result_ReturnCode) && !nodeValue.equalsIgnoreCase("0"))
			log.info(nodeName + " : " + nodeValue);
		else if (nodeName.equalsIgnoreCase(SMessageUtil.Result_ErrorMessage) && nodeValue.length() > 0)
			log.info(nodeName + " : " + nodeValue);
		else if  (nodeName.equalsIgnoreCase(SMessageUtil.Result_ErrorMessage) && nodeValue.length() == 0)
			log.info(nodeName + " : ");
	}
	
	public static void setHeaderItemValue(Document doc, String nodeName, String nodeValue)                                    
	{
		if (doc.getRootElement().getChild("Header") == null)
			doc.getRootElement().addContent(new Element("Header"));
		
		String nodePath = "//" + Message_Tag + "/" + "Header" + "/";
		setElement(nodePath, doc, nodeName, nodeValue);
	}

	public static Document createXmlDocument(String receivedData) throws Exception      
	{
		Element message = new Element( Message_Tag );                                                                                                            
		Document document = new Document( message );    

		
		int idx = receivedData.indexOf(" ");
		String commandName = receivedData.substring(0, idx).trim();
		HashMap<String, String> messageMap = null;
		messageMap = parsingStringMessage(receivedData);
		
		Element header = new Element( Header_Tag );       	
		
		Element subElement = new Element( BPELName_Tag );    
		subElement.setText(commandName);
		header.addContent(subElement);
		
		subElement = new Element( MessageName_Tag );    
		subElement.setText(commandName);
		header.addContent(subElement);
		message.addContent(header);
		Element body = new Element( Body_Tag );		                                                                                                               
		Element ele = null;
		while (messageMap.keySet().iterator().hasNext())
		{
			String keyName = messageMap.keySet().iterator().next();
			String keyValue = messageMap.remove(keyName);
			ele = new Element( keyName );                                                                                                               
			ele.setText( keyValue );                                                                                                                            
			body.addContent( ele );       			
		}
		message.addContent(body);
		messageMap.clear();

		return document;
	}
	
    public static void setItemValue(Document doc, String elementName, String nodeName, String nodeValue)                                    
	{                                                                                         
		try {
			String nodePath = "//" + Message_Tag + "/" + elementName + "/" + nodeName;
			Element element = JdomUtils.getNode(doc, nodePath);
			if (element != null)
				JdomUtils.setNodeText(doc, nodePath, nodeValue);     
			else {
				nodePath = "//" + Message_Tag + "/" + elementName + "/";
 				if (nodePath.endsWith("/")) 
					nodePath = nodePath.substring(0, nodePath.length()-1);
				Element headerElement = JdomUtils.getNode(doc, nodePath);
				JdomUtils.addElement(headerElement, nodeName, nodeValue);
			}
		} catch (Exception e) {                                                                 
			log.error(e, e);
		}                                                                                                                                                                  	
	}    

	private static HashMap<String, String> parsingStringMessage(String msg)
	{
		String delimeter = "[{X*X}]";  
		
		String msg2 = msg;
		while(true)
		{
			int idx1 = msg2.indexOf("=[");
			int idx2 = msg2.indexOf("]", idx1);
			if (idx1 > 0 && idx2 > 0)
			{
				String a = msg2.substring(idx1+2, idx2);
				if (a.length() == 0)
				{
					msg2 = msg2.substring(idx2+1, msg2.length()); continue;
				}
				String b = org.springframework.util.StringUtils.replace(a, "=", "($%^)");
				msg = org.springframework.util.StringUtils.replace(msg, a, b);				
				msg2 = msg2.substring(idx2, msg2.length());
			}
			else break;
		}
		
		String [] messageSplit = msg.split("=");
		HashMap<String, String> params = new HashMap<String, String>();
		StringBuffer keyNameBuffer = new StringBuffer();
		keyNameBuffer.append("Command").append(delimeter);
		StringBuffer valueBuffer = new StringBuffer();
		for (int i=0; i<messageSplit.length; i++)
		{
			if (getKeyName(messageSplit[i]).length() > 0) {
				keyNameBuffer.append(getKeyName(messageSplit[i])).append(delimeter);
				valueBuffer.append(getValue(messageSplit[i])).append(delimeter);
			}
			else {
				valueBuffer.append(messageSplit[i]).append(delimeter);
			}
		}

		String[] keyNames = org.springframework.util.StringUtils.delimitedListToStringArray(keyNameBuffer.toString(), delimeter);
		String[] keyvalues = org.springframework.util.StringUtils.delimitedListToStringArray(valueBuffer.toString(), delimeter);
					
		for (int i=1; i<keyNames.length; i++)
		{
			if (keyNames[i] == null || keyNames[i].trim().length() == 0) continue;
			try {
				keyvalues[i] = keyvalues[i].trim();
				keyvalues[i] = org.springframework.util.StringUtils.replace(keyvalues[i], "($%^)", "=");
				if (keyvalues[i].startsWith("[") && keyvalues[i].endsWith("]"))
					keyvalues[i] = keyvalues[i].substring(1, keyvalues[i].length()-1);
				params.put(keyNames[i], keyvalues[i].trim());
			} catch (Exception e) {
				params.put(keyNames[i], "");
				log.debug("Value of last data is empty");
			}
		}
		return params;
	}	

	private static String getKeyName(String partialMsg)
	{
		if (partialMsg.startsWith("[") && partialMsg.endsWith("]"))
			return "";
		String character = "";
		String value = "";
		for (int i=partialMsg.length(); i>0; i--)
		{
			character = partialMsg. substring(i-1, i);
			if (character.equalsIgnoreCase(" "))
			{
				return StringUtils.reverse(value);
			}
			else {
				value += character;
			}
		}
		return "";
	}

	private static String getValue(String partialMsg)
	{
		partialMsg = StringUtils.removeEnd(partialMsg, getKeyName(partialMsg));

		return partialMsg;
	}
	
	public static Document generateQueryMessage(String messageName, HashMap<String, Object> paramMap, HashMap<String, Object> bindMap,
												String sOriginalSourceSubjectName, String sEventUser, String sEventComment)
	{
		String sTimeKey = TimeUtils.getCurrentEventTimeKey();
		
		//envelopement
		Element message = new Element(Message_Tag);
		{
			//header
			Element header = new Element(Header_Tag);
			{
				Element messagename = new Element(MessageName_Tag);
				messagename.setText(messageName);
				header.addContent(messagename);
				
				Element transactionId = new Element("TRANSACTIONID");
				transactionId.setText(sTimeKey);
				header.addContent(transactionId);
				
				Element originalSourceSubjectName = new Element("ORIGINALSOURCESUBJECTNAME");
				originalSourceSubjectName.setText(sOriginalSourceSubjectName);
				header.addContent(originalSourceSubjectName);
				
				Element eventUser = new Element("EVENTUSER");
				eventUser.setText(sEventUser);
				header.addContent(eventUser);
				
				Element eventComment = new Element("EVENTCOMMENT");
				eventComment.setText(sEventComment);
				header.addContent(eventComment);
			}
			message.addContent(header);
			
			//body
			Element bodyElement = new Element(Body_Tag);
			{
				for (String keyName : paramMap.keySet())
				{
					Element paramElement = new Element(keyName);
					paramElement.setText(paramMap.get(keyName).toString());
					bodyElement.addContent(paramElement);
				}
				
				for (String keyName : bindMap.keySet())
				{
					Element bindElement = new Element(keyName);
					bindElement.setText(bindMap.get(keyName).toString());
					bodyElement.addContent(bindElement);
				}
				
			}
			message.addContent(bodyElement);
			
			//result
			Element returnElement = new Element(Result_Name_Tag);
			{
				Element returnCode = new Element(Result_ReturnCode);
				returnCode.setText(ESBService.SUCCESS);
				returnElement.addContent(returnCode);
				
				Element returnMessage = new Element(Result_ErrorMessage);
				returnMessage.setText("");
				returnElement.addContent(returnMessage);
			}
			message.addContent(returnElement);
		}

		//converting element to message
		Document replyDoc = new Document(message);
		
		return replyDoc;
	}
	
	public static Document generateQueryMessage1(String messageName, HashMap<String, Object> paramMap, 
												String sOriginalSourceSubjectName, String sEventUser, String sEventComment)
	{
		String sTimeKey = TimeUtils.getCurrentEventTimeKey();
		
		//envelopement
		Element message = new Element(Message_Tag);
		{
			//header
			Element header = new Element(Header_Tag);
			{
				Element messagename = new Element(MessageName_Tag);
				messagename.setText(messageName);
				header.addContent(messagename);
				
				Element transactionId = new Element("TRANSACTIONID");
				transactionId.setText(sTimeKey);
				header.addContent(transactionId);
				
				Element originalSourceSubjectName = new Element("ORIGINALSOURCESUBJECTNAME");
				originalSourceSubjectName.setText(sOriginalSourceSubjectName);
				header.addContent(originalSourceSubjectName);
				
				Element eventUser = new Element("EVENTUSER");
				eventUser.setText(sEventUser);
				header.addContent(eventUser);
				
				Element eventComment = new Element("EVENTCOMMENT");
				eventComment.setText(sEventComment);
				header.addContent(eventComment);
			}
			message.addContent(header);
			
			//body
			Element bodyElement = new Element(Body_Tag);
			{
				for (String keyName : paramMap.keySet())
				{
					Element paramElement = new Element(keyName);
					paramElement.setText(paramMap.get(keyName).toString());
					bodyElement.addContent(paramElement);
				}
								
			}
			message.addContent(bodyElement);
			
			//result
			Element returnElement = new Element(Result_Name_Tag);
			{
				Element returnCode = new Element(Result_ReturnCode);
				returnCode.setText(ESBService.SUCCESS);
				returnElement.addContent(returnCode);
				
				Element returnMessage = new Element(Result_ErrorMessage);
				returnMessage.setText("");
				returnElement.addContent(returnMessage);
			}
			message.addContent(returnElement);
		}

		//converting element to message
		Document replyDoc = new Document(message);
		
		return replyDoc;
	}
	
	public static String getMessageName(Document doc) throws CustomException                            
	{  
		String messageName = getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "MESSAGENAME");
		
		return messageName;
	}
	
	public static Element getBodyElement(Document doc) throws CustomException                            
	{  
		Element bodyElement = null;
		
		try 
		{
			bodyElement = XmlUtil.getNode(doc, new StringBuilder("//").append(SMessageUtil.Message_Tag)
													.append("/").append(SMessageUtil.Body_Tag)
													.toString());
		} catch (Exception e) {
			throw new CustomException("SYS-0001", SMessageUtil.Body_Tag);
		}
		
		return bodyElement;
	}

	public static String getHeaderItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Header_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	public static String getBodyItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}     
		else
		{
			if (StringUtil.isEmpty(itemValue))
			{
				if(StringUtil.equals(itemName, "PROCESSFLOWVERSION"))
					itemValue = "00001";
				
				if(StringUtil.equals(itemName, "PROCESSOPERATIONVERSION"))
					itemValue = "00001";	
				
				if(StringUtil.equals(itemName, "PRODUCTSPECVERSION"))
					itemValue = "00001";
			}			
		}
		
		return itemValue;                                                                              
	}
	
	public static void setBodyItemValue(Document doc, String itemName, String itemValue)
		throws CustomException                            
	{
		try
		{
			Element element = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + itemName);
			
			if (element != null)
			{
				JdomUtils.setNodeText(doc, "//" + Message_Tag + "/" + Body_Tag, itemName, itemValue);
			}
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", itemName);
		}
	}
	
	public static Element getBodySequenceItem(Document doc, String listItemName, boolean required) throws CustomException                                    
	{	
        Element listItem = null;
        
		try 
		{	
			listItem = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + listItemName);			
		} 
		catch (Exception e) 
		{
		}
		
		if(required == true)
		{	
			String itemName = "";
			
			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", listItemName);
			}
			
			itemName = listItemName.substring(0, listItemName.indexOf("LIST"));
			
			if(listItem.getChildren().size() == 0)
				throw new CustomException("SYS-0001", itemName);
			
			for ( Iterator iterator = listItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element item = (Element) iterator.next();
				
				if (!StringUtil.equals(item.getName(), itemName))
					throw new CustomException("SYS-0001", itemName);
			}			
		}

		return listItem;
	}


	public static List<Element> getBodySequenceItemList(Document doc, String listItemName, boolean required) throws CustomException
	{
		Element seqItem = null;
		List<Element> seqItemList = new ArrayList<Element>();

		try
		{
			seqItem = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + listItemName);

			for (Iterator<?> iterator = seqItem.getChildren().iterator(); iterator.hasNext();)
			{
				Element item = (Element) iterator.next();
				seqItemList.add(item);
			}
		}
		catch (Exception e)
		{
		}

		if (required == true)
		{
			String itemName = "";

			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName();
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-0001", listItemName);
			}

			itemName = listItemName.substring(0, listItemName.indexOf("LIST"));

			if (seqItem.getChildren().size() == 0)
				throw new CustomException("SYS-0001", itemName);

			for (Iterator<?> iterator = seqItem.getChildren().iterator(); iterator.hasNext();)
			{
				Element item = (Element) iterator.next();

				if (!StringUtil.equals(item.getName(), itemName))
					throw new CustomException("SYS-0001", itemName);
			}

		}

		return seqItemList;
	}

	public static Element getSubSequenceItem(Element parentE, String subListItemName, boolean required) throws CustomException                                    
	{	
        Element childItemList = null;
        
        try 
		{	
        	childItemList = parentE.getChild(subListItemName);
		} 
		catch (Exception e) 
		{
		}
        
		if(required == true)
		{	
			String childItemName = "";
			
			try
			{
				parentE.getChild(subListItemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("ITEM-0000", subListItemName);
			}
			
			childItemName = subListItemName.substring(0, subListItemName.indexOf("LIST"));
			
			childItemList = parentE.getChild(subListItemName);
			
			if(childItemList.getChildren().size() == 0)
				throw new CustomException("ITEM-0000", childItemName);
			
			for ( Iterator iterator = childItemList.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();
				
				if (!StringUtil.equals(childItem.getName(), childItemName))
					throw new CustomException("ITEM-0000", childItemName);
			}			

		}
		
		return childItemList;                                                                              
	}
	
	public static List<Element> getSubSequenceItemList(Element parentE, String subListItemName, boolean required) throws CustomException                                    
	{	
        Element childSeqItem = null;
        List<Element> childSeqItemList = new ArrayList<Element>();
        
        try 
		{			
			childSeqItem = parentE.getChild(subListItemName);
			
			for ( Iterator iterator = childSeqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();			
				childSeqItemList.add(childItem);
			}
		} 
		catch (Exception e) 
		{
		}
        
		if(required == true)
		{	
			String childItemName = "";
			
			try
			{
				parentE.getChild(subListItemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("ITEM-0000", subListItemName);
			}
			
			childItemName = subListItemName.substring(0, subListItemName.indexOf("LIST"));
			
			childSeqItem = parentE.getChild(subListItemName);
			
			if(childSeqItem.getChildren().size() == 0)
				throw new CustomException("ITEM-0000", childItemName);
			
			for ( Iterator iterator = childSeqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();
				
				if (!StringUtil.equals(childItem.getName(), childItemName))
					throw new CustomException("ITEM-0000", childItemName);
			}			

		}
		
		return childSeqItemList;                                                                              
	}
	
	public static String getReturnItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = (String) JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Return_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Return_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	public static String getChildText(Element element, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		
		itemValue = element.getChildText(itemName);

		if(required == true)
		{
			try
			{
				element.getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                         
		else
		{
			if (StringUtil.isEmpty(itemValue))
			{
				if(StringUtil.equals(itemName, "PROCESSFLOWVERSION"))
					itemValue = "00001";
				
				if(StringUtil.equals(itemName, "PROCESSOPERATIONVERSION"))
					itemValue = "00001";	
				
				if(StringUtil.equals(itemName, "PRODUCTSPECVERSION"))
					itemValue = "00001";
			}			
		}
		
		return itemValue;                                                                              
	}

	public static Document addItemToHeader(Document doc, String itemName, String itemValue) throws CustomException
	{
		Element headerElement = doc.getRootElement().getChild(Header_Tag);

		// Add LotIdElement
		if (StringUtil.isNotEmpty(itemName))
		{
			Element itemE = new Element(itemName);
			itemE.setText(itemValue);
			headerElement.addContent(itemE);
		}
		else
		{
			throw new CustomException("SYS-0003", itemName, itemValue);
		}

		return doc;
	}

	public static Document addItemToBody(Document doc, String itemName, String itemValue) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(Body_Tag);

		// Add Element
		if (StringUtil.isNotEmpty(itemName))
		{
			Element itemE = new Element(itemName);
			itemE.setText(itemValue);
			bodyElement.addContent(itemE);
		}
		else
		{
			throw new CustomException("SYS-0003", itemName, itemValue);
		}

		return doc;
	}

	public static Document addItemToReturn(Document doc, String itemName, String itemValue) throws CustomException
	{
		Element returnElement = doc.getRootElement().getChild(Return_Tag);

		// Add LotIdElement
		if (StringUtil.isNotEmpty(itemName))
		{
			Element itemE = new Element(itemName);
			itemE.setText(itemValue);
			returnElement.addContent(itemE);
		}
		else
		{
			throw new CustomException("SYS-0003", itemName, itemValue);
		}

		return doc;
	}

	public static Document addReturnToMessage(Document doc, String returnCode, String returnMessage) throws CustomException
	{
		// Add LotIdElement
		try
		{
			Element returnElement = doc.getRootElement().getChild("Return");
			
			Element retunCode = returnElement.getChild("RETURNCODE");
			Element retunMessage = returnElement.getChild("RETURNMESSAGE");
		}
		catch(Exception ex)
		{
			Element root = doc.getRootElement();
			
			Element returnE = new Element("Return");
			root.addContent(returnE);
			
			Element retunCodeE = new Element("RETURNCODE");
			retunCodeE.setText(returnCode);
			returnE.addContent(retunCodeE);
			
			Element retunMessageE = new Element("RETURNMESSAGE");
			retunMessageE.setText(returnMessage);
			returnE.addContent(retunMessageE);
		}
		
		return doc;
	}
	
	public static Document createXmlDocument(Element bodyElement, String messageName, String originalSourceSubjectName, String targetSubjectName, String eventUser, String eventComment)
		throws Exception      
	{
		Element message = new Element( Message_Tag );                                                                                                            
		Document document = new Document( message );
		
		Element header = createHeaderElement(messageName, TimeUtils.getCurrentEventTimeKey(), originalSourceSubjectName, originalSourceSubjectName, targetSubjectName, eventUser, eventComment, "ENG");       	
		message.addContent(header);
		
		//body must be orphan
		if (bodyElement != null && bodyElement.getName().equals(Body_Tag))
			message.addContent(bodyElement);
		
		return document;
	}
	
	public static Element createHeaderElement(String messageName, String transactionId,
											String originalSourceSubjectName, String sourceSubjectName, String targetSubjectName,
											String eventUser, String eventComment, String language)
		throws Exception
	{
		Element header = new Element( Header_Tag );       	
		
		Element eleMessageName = new Element( MessageName_Tag );    
		eleMessageName.setText(messageName);
		header.addContent(eleMessageName);
		
		Element eleTrxId = new Element( "TRANSACTIONID" );    
		eleTrxId.setText(transactionId);
		header.addContent(eleTrxId);
		
		Element eleOriginalSRCSubject = new Element( "ORIGINALSOURCESUBJECTNAME" );    
		eleOriginalSRCSubject.setText(originalSourceSubjectName);
		header.addContent(eleOriginalSRCSubject);
		
		Element eleTargetSubjectName = new Element( "TARGETSUBJECTNAME" );    
		eleTargetSubjectName.setText(targetSubjectName);
		header.addContent(eleTargetSubjectName);
		
		Element eleEventUser = new Element( "EVENTUSER" );    
		eleEventUser.setText(eventUser);
		header.addContent(eleEventUser);
		
		Element eleEventComment = new Element( "EVENTCOMMENT" );    
		eleEventComment.setText(eventComment);
		header.addContent(eleEventComment);
		
		Element eleLanguage = new Element( "LANGUAGE" );    
		eleLanguage.setText(language);
		header.addContent(eleLanguage);
		
		return header;
	}
}
