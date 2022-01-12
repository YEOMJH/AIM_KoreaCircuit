package kr.co.aim.messolution.pms.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

public class PMSAlarmReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PMSAlarm", getEventUser(), getEventComment(), "", "");

		String alarmComment = SMessageUtil.getBodyItemValue(doc, "ALARMCOMMENT", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		
		if ("EH".equals(alarmCode))
		{
			// EQP Hold
			MachineKey machineKey = new MachineKey();
			machineKey.setMachineName(machineName);
			Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
			MESMachineServiceProxy.getMachineServiceImpl().changeMachineLockFlag(eventInfo, machineData, "Y");
			
			// Lot FutureHold
			this.futureHoldByWipLot(eventInfo, machineName, alarmCode, alarmComment);
		}
		else if ("OL".equals(alarmCode))
		{
			// Send OPCallSend
			this.sendOPCallMessage(doc, machineName, alarmComment);
		}
		else
		{
			eventLog.info("<ALARMCODE> is Empty.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void futureHoldByWipLot(EventInfo eventInfo, String machineName, String alarmCode, String alarmComment) throws greenFrameDBErrorSignal, CustomException
	{
		String sql = "SELECT LOTNAME, FACTORYNAME, "
				   + "       PROCESSFLOWNAME, PROCESSFLOWVERSION, "
				   + "       PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION "
				   + "  FROM LOT "
				   + " WHERE 1 = 1 "
				   + "   AND LOTPROCESSSTATE = 'RUN' "
				   + "   AND MACHINENAME = ? ";
		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { machineName });
		if (resultDataList == null || resultDataList.size() == 0)
		{
			return ;
		}
		
		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			LotFutureAction dataInfo = new LotFutureAction();
			dataInfo.setLotName(resultData.get("LOTNAME").toString());
			dataInfo.setFactoryName(resultData.get("FACTORYNAME").toString());
			dataInfo.setProcessFlowName(resultData.get("PROCESSFLOWNAME").toString());
			dataInfo.setProcessFlowVersion(resultData.get("PROCESSFLOWVERSION").toString());
			dataInfo.setProcessOperationName(resultData.get("PROCESSOPERATIONNAME").toString());
			dataInfo.setProcessOperationVersion(resultData.get("PROCESSOPERATIONVERSION").toString());
			dataInfo.setPosition(0);
			dataInfo.setReasonCode(alarmCode);
			dataInfo.setReasonCodeType("HOLD");
			dataInfo.setActionName("hold");
			dataInfo.setActionType("PMS");
			dataInfo.setAttribute1("");
			dataInfo.setAttribute2("");
			dataInfo.setAttribute3("");
			dataInfo.setBeforeAction("False");
			dataInfo.setAfterAction("True");
			dataInfo.setBeforeActionComment("");
			dataInfo.setAfterActionComment(alarmComment);
			dataInfo.setBeforeActionUser("");
			dataInfo.setAfterActionUser("");
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setBeforeMailFlag("N");
			dataInfo.setAfterMailFlag("N");
			dataInfo.setAlarmCode(alarmCode);
			dataInfo.setReleaseType("All");
			dataInfo.setRequestDepartment("");
			
			dataInfoList.add(dataInfo);
		}
		
		ExtendedObjectProxy.getLotFutureActionService().insert(dataInfoList);
		
	}

	private void sendOPCallMessage(Document doc, String machineName, String alarmComment)
	{
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		Element originalBodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = (Element)originalHeaderElement.clone();
		headerElement.addContent(new Element("MESSAGENAME").setText("OpCallSend"));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("MACHINENAME").setText(originalBodyElement.getChildText("MACHINENAME")));
		bodyElement.addContent(new Element("OPCALLDESCRIPTION").setText("PMSAlarm : " + alarmComment));
		
		rootElement.addContent(bodyElement);
		
		String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, new Document(rootElement), "EISSender");
	}
}
