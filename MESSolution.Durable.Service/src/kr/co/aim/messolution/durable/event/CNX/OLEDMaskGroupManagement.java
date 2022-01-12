package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;

public class OLEDMaskGroupManagement extends SyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskGroupManagement.class);

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> eleMaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);
		String MaskGroupName = SMessageUtil.getBodyItemValue(doc, "MASKGRUOPNAME", true);
		String MaskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String Quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignGroupMaskLot", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<MaskLot> MaskLotList = new ArrayList<MaskLot>();
		for (Element eledur : eleMaskLotList)
		{
			String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String MaskLotName = SMessageUtil.getChildText(eledur, "MASKLOTNAME", true);
			String MaskType = SMessageUtil.getChildText(eledur, "MASKTYPE", true);
			String Position = SMessageUtil.getChildText(eledur, "POSITION", true);

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { MaskLotName });
			dataInfo.setFactoryName(factoryName);
			dataInfo.setMaskLotName(MaskLotName);
			dataInfo.setMaskType(MaskType);
			dataInfo.setMaskSpecName(MaskSpecName);
			dataInfo.setMaskGroupName(MaskGroupName);
			dataInfo.setReasonCodeType("");
			dataInfo.setReasonCode("");
			dataInfo.setPosition(Position);
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			MaskLotList.add(dataInfo);

		}

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, MaskLotList);

		// GroupInfo Qty Update
		ProcessGroupKey processGroupKey = new ProcessGroupKey();
		processGroupKey.setProcessGroupName(MaskGroupName);

		ProcessGroup processGroupData = new ProcessGroup();
		processGroupData.setKey(processGroupKey);
		processGroupData.getMaterialQuantity();

		AssignMaterialsInfo assignMaterialsInfo = new AssignMaterialsInfo();
		assignMaterialsInfo.setMaterialQuantity(Integer.parseInt(Quantity));

		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().assignMaterials(processGroupData, assignMaterialsInfo, eventInfo);

		return doc;
	}

}
