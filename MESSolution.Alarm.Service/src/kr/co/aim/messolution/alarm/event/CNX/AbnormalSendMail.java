package kr.co.aim.messolution.alarm.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class AbnormalSendMail extends AsyncHandler {
	Log log = LogFactory.getLog(AbnormalSendMail.class);
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		
		String abnormalSheetName = SMessageUtil.getBodyItemValue(doc, "ABNORMALSHEETNAME", false);	
		
		String abnormalCode = SMessageUtil.getBodyItemValue(doc, "EXCEPTIONCODE", false);
		List<Element> mailList = SMessageUtil.getBodySequenceItemList(doc, "MAILLIST", true);		
		String lotName = SMessageUtil.getChildText(mailList.get(0), "LOTNAME", true);
		String machineName = SMessageUtil.getChildText(mailList.get(0), "MACHINENAME", true);
		String createUser = SMessageUtil.getChildText(mailList.get(0), "CREATEUSER", true);

		
		sendMail(abnormalSheetName,lotName, machineName,createUser);


	}
	
	public void sendMail(String abnormalSheetName, String lotName, String machineName, String createUser)
	{
		List<Map<String,Object>> resultList = null;
	    String sql ="SELECT EMAIL FROM USERPROFILE WHERE DEPARTMENT ='TA' ";
	    
		Map<String,Object> bindMap = new HashMap<>();
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		}catch(Exception ex)
		{
			
		    log.info(ex.getCause());
		}
		
		if(resultList !=null && resultList.size()>0)
		{
			String message = "<pre>===============RecipeChanged===============</pre>";
	  	  	   message += "<pre> AbnormalSheet : " + abnormalSheetName + "</pre>";
	  	  	   message += "<pre> Lot :  " + lotName + "</pre>";
	  	  	   message += "<pre> Machine : " + machineName + "</pre>";
	  	  	   message += "<pre> CreateUser : " + createUser + "</pre>";
	  	  	   message += "<pre>Comment : Over Time Limit! </pre>";
	  	
		
			List<String> userList = CommonUtil.makeListBySqlResult(resultList, "EMAIL");
			try {
				GenericServiceProxy.getMailSerivce().postMail(userList.toArray(new String[] {}), this.getClass().getSimpleName(),message);
			} 
			catch (Exception e) 
			{
				log.error("Failed to send mail.");
				e.printStackTrace();
			}
		}	
	}
	
}
