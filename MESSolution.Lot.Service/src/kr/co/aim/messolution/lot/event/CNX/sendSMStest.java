package kr.co.aim.messolution.lot.event.CNX;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom.Document;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;



public class sendSMStest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String LotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append(LotName);
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append("Factory: ");
		smsMessage.append("\n");
		smsMessage.append("CurrentMachineName: ");
		smsMessage.append("\n");
		smsMessage.append("CurrentAreName: ");
		smsMessage.append("\n");
		smsMessage.append("CurrentTime: ");
		smsMessage.append("\n");
		smsMessage.append("ReasonCode: ");		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String CurrentTime = df.format(new Date());
		GenericServiceProxy.getSMSInterface().indexSmsSend("FA",  smsMessage.toString());	
		return doc;
	}
}
