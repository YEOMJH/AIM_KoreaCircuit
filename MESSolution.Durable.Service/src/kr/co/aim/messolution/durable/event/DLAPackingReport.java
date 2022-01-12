package kr.co.aim.messolution.durable.event;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DLAPacking;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DLAPackingReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(DLAPackingReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// PRODUCTNAME
		// DELAMIGLASSNAME
		// BOXNAME
		// PRODUCTSPECNAME
		// PRODUCTSPECVERSION
		// PROCESSFLOWNAME
		// PROCESSFLOWVERSION
		// PROCESSOPERATIONNAME
		// PROCESSOPERATIONVERSION

		String messageName = SMessageUtil.getMessageName(doc);

		String machineName             = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String productName             = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String deLamiGlassName         = SMessageUtil.getBodyItemValue(doc, "DELAMIGLASSNAME", false);
		String boxName                 = SMessageUtil.getBodyItemValue(doc, "BOXNAME", false);
		String productSpecName         = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME ", false);
		String productSpecVersion      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION ", false);
		String processFlowName 		   = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion      = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION ", false);
		String processOperationName    = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME ", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION ", false);
		String slotNo                  = SMessageUtil.getBodyItemValue(doc, "SLOTNO ", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);

		if (StringUtils.isNotEmpty(deLamiGlassName))
		{
			// Create MaterialProduct
			DLAPacking dataInfo = new DLAPacking();
			dataInfo.setDeLamiGlassName(deLamiGlassName);
			dataInfo.setProductName(productName);
			dataInfo.setBoxName(boxName);
			dataInfo.setMachineName(machineName);
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setProductSpecVersion(productSpecVersion);
			dataInfo.setProcessOperationName(processOperationName);
			dataInfo.setProcessOperationVersion(processOperationVersion);
			dataInfo.setSlotNo(slotNo);
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setEventTime(eventInfo.getEventTime());

			ExtendedObjectProxy.getDLAPackingService().create(eventInfo, dataInfo);

		}
		else
			log.info("DeLamiGlassName [" + deLamiGlassName + "], ProductName [" + productName + "] is empty!!");
	}

}
