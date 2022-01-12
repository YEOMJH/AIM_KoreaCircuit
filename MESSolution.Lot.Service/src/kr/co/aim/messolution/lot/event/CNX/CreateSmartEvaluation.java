package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;


public class CreateSmartEvaluation extends SyncHandler{

	private static Log log = LogFactory.getLog(CreateSmartEvaluation.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		List<Element> reviewTestNameList = SMessageUtil.getBodySequenceItemList(doc, "REVIEWTESTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Import", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		StringBuffer insetSQL  = new StringBuffer();
		insetSQL.append("  INSERT INTO CT_REVIEWSMARTEVALUATION VALUES ");
		insetSQL.append(" (:SEQ,:USERID,:FACTORYNAME,:MACHINETYPE,:TESTTYPE,:PROCESSOPERATIONNAME,:DEFECTCODE,:PERCENTAGE,:OPERATIONFLAG,:PLANQTY,:FINISHQTY,:ABORTQTY,:UNIT, ");
		insetSQL.append(" :TESTSTATE,:LASTEVENTTIMEKEY,:LASTEVENTUSER,:LASTEVENTNAME,:LASTEVENTCOMMENT ) ");
		for (Element reviewTest : reviewTestNameList)
		{
			String userID = reviewTest.getChild("USERID").getText();
			String factoryName = reviewTest.getChild("FACTORYNAME").getText();
			String machineType = reviewTest.getChild("MACHINETYPE").getText();
			String testType = reviewTest.getChild("TESTTYPE").getText();
			String processOpera = reviewTest.getChild("PROCESSOPERATIONNAME").getText();
			String defectCode = reviewTest.getChild("DEFECTCODE").getText();
			String percentage = reviewTest.getChild("PERCENTAGE").getText();
			String operationFlag = reviewTest.getChild("OPERATIONFLAG").getText();
			String planQTY = reviewTest.getChild("PLANQTY").getText();
			String unit = reviewTest.getChild("UNIT").getText();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SEQ", TimeUtils.getCurrentEventTimeKey());
			args.put("USERID", userID);
			args.put("FACTORYNAME", factoryName);
	
			args.put("MACHINETYPE", machineType);
			args.put("TESTTYPE", testType);
			args.put("PROCESSOPERATIONNAME", processOpera);
			args.put("DEFECTCODE", defectCode);
			args.put("PERCENTAGE", percentage);
			args.put("OPERATIONFLAG",operationFlag);
			args.put("PLANQTY", Integer.parseInt(planQTY));
			args.put("FINISHQTY", 0);
			args.put("ABORTQTY", 0);
			args.put("UNIT", Integer.parseInt(unit));
			args.put("TESTSTATE", "Released");
			args.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			args.put("LASTEVENTUSER", eventInfo.getEventUser());
			args.put("LASTEVENTNAME", eventInfo.getEventName());
			args.put("LASTEVENTCOMMENT", eventInfo.getEventComment());

			try
			{
			    GenericServiceProxy.getSqlMesTemplate().update(insetSQL.toString(), args);
			    log.info(factoryName +" info Insert");
			}
			catch (Exception e)
			{
				throw new CustomException(e.getCause());
			}			
			
		}

		return doc;

		
	}
}
