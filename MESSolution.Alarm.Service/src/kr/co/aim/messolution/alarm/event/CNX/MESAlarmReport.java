package kr.co.aim.messolution.alarm.event.CNX;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class MESAlarmReport extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {
		Log log = LogFactory.getLog(MESAlarmReport.class);
		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String errorEvent = SMessageUtil.getBodyItemValue(doc, "ERRORMESSAGE", true);
		String errorDetail = SMessageUtil.getBodyItemValue(doc, "ERRORDETAIL", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);		

		String message = "<pre>===============AlarmInformation===============</pre>";
			message += "<pre>- AlarmEvent	: " + errorEvent + "</pre>";
			message += "<pre>- AlarmText	: " + errorDetail + "</pre>";
			message += "<pre>- FactoryName	: " + factoryName + "</pre>";
			message += "<pre>- MachineName	: " + machineName + "</pre>";
			message += "<pre>- PortName	: " + portName + "</pre>";
			message += "<pre>- CST ID	: " + carrierName + "</pre>";
			message += "<pre>- Lot ID	: " + lotName + "</pre>";
			message += "<pre>==============================================</pre>";
			
		CommonUtil.sendAlarmEmail(errorEvent, "CANCEL",  factoryName ,machineName ," " ,message);

		// Set SMS Text
		String smsMessage = alarmSmsText(errorEvent, machineName, carrierName, lotName, errorDetail, factoryName);

		// Send SMS
		//GenericServiceProxy.getSMSInterface().AlarmSmsSend(errorEvent, "CANCEL",  smsMessage);
	}
	private String alarmSmsText(String errorEvent, String machineName, String carrierName, String lotName, String errorDetail, String factoryName)
	{
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append("AlarmEvent: ");
		smsMessage.append(errorEvent);
		smsMessage.append("\n");
		smsMessage.append("Factory: ");
		smsMessage.append(factoryName);
		smsMessage.append("\n");
		smsMessage.append("Machine: ");
		smsMessage.append(machineName);
		smsMessage.append("\n");
		smsMessage.append("CST ID: ");
		smsMessage.append(carrierName);
		smsMessage.append("\n");
		smsMessage.append("Lot ID: ");
		smsMessage.append(lotName);
		smsMessage.append("\n");
		smsMessage.append("AlarmText: ");
		smsMessage.append(errorDetail);

		return smsMessage.toString();
	}
	
}
