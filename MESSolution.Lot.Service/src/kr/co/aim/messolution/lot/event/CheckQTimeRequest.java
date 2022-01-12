package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CheckQTimeRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		try
		{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String slotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", true);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CheckQTimeRequest", getEventUser(), getEventComment(), null, null);

			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CheckQTimeReply");

			if(!StringUtils.equals(lotData.getProductionType(), "M")&&!StringUtils.equals(lotData.getProductionType(), "D"))
			{
				CommonValidation.checkMachineIdleTime(machineData);
			}

			if (!CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
			{
				// Q-time
				ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotData.getKey().getLotName(), machineName);
				ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(lotData.getKey().getLotName());
			}

			// SYS-0010:{0}  remove the check condition according to CIM requirements.
//			if (!lotData.getUdfs().get("SLOTSEL").equals(slotSel))
//				throw new CustomException("SYS-0010", "The slot information downloaded by MES is inconsistent with that reported by BC.");
			
			setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		return doc;
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}

}
