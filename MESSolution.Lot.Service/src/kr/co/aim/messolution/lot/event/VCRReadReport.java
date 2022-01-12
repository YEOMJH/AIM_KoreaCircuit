package kr.co.aim.messolution.lot.event;


import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.VcrAbnormalPanel;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class VCRReadReport extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String vcrProductName = SMessageUtil.getBodyItemValue(doc, "VCRPRODUCTNAME", false);
		String productType = "Lot";

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		if (!machineData.getMachineGroupName().isEmpty() && machineData.getMachineGroupName().equalsIgnoreCase("Tension"))
		{
			if (!productName.equals(vcrProductName))
			{
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(productName);
				maskLotData.setVcrMaskName(vcrProductName);
				ExtendedObjectProxy.getMaskLotService().update(maskLotData);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("VCRMaskReadReport", getEventUser(), getEventComment());
				ExtendedObjectProxy.getMaskLotService().addHistory(eventInfo, "MASKLOTHISTORY", maskLotData, eventLog);
			}
			else
			{
				eventLog.info(String.format("[VCR ReadID:%s]Consistent information。", vcrProductName));
			}
		}
		else
		{

			Lot lotData = new Lot();
			Product productData = new Product();
			String factoryName = "";
			try
			{
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productName);
				factoryName = lotData.getFactoryName();
			}
			catch (Exception e)
			{
				productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				productType = "Product";
				factoryName = productData.getFactoryName();
				eventLog.info("Not exist Panel Info on Lot Table");// Modify by wangys -20200413
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("VCRReadReport", getEventUser(), getEventComment(), "", "");

			if (StringUtils.equals(productType, "Lot"))
			{
				if (StringUtils.isEmpty(vcrProductName) || !StringUtils.equals(productName, vcrProductName))
				{
					String eventComment = "Mismatched VCRProductName by EQP " + vcrProductName + ", Panel " + productName;
					eventLog.info(eventComment);
					eventInfo.setEventComment(eventComment);
					sendEmail(machineName, unitName, subUnitName, productName, vcrProductName, factoryName);
				}

				SetEventInfo setLotEventInfo = new SetEventInfo();
				setLotEventInfo.getUdfs().put("VCRPRODUCTNAME", vcrProductName);

				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
				MESLotServiceProxy.getLotServiceImpl().updateLotData("sourcelotname", lotData.getSourceLotName(), lotData.getKey().getLotName());
				updateLotHistory(lotData, eventInfo.getEventName(), eventInfo.getEventTimeKey());
				
				if (StringUtils.isEmpty(vcrProductName) || !StringUtils.equals(productName, vcrProductName))
				{	
					VcrAbnormalPanel panelData = new VcrAbnormalPanel();
					panelData.setTaskID(TimeStampUtil.getCurrentEventTimeKey());
					panelData.setPanelID(productName);
					if(StringUtils.isEmpty(vcrProductName))
					{
						
						StringUtils.isEmpty("");
					}else
					{
						
					    panelData.setVcrPanelID(vcrProductName);
					}
					panelData.setLastEventUser(machineName);
					panelData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
					panelData.setLastEventTime(eventInfo.getEventTime());
					panelData.setLastEventName("VCRReadReport");
					panelData.setLastEventComment("VcrAbnormalPanel");
					panelData.setTaskState("Created");
					ExtendedObjectProxy.getVcrAbnormalPanelDataService().create(eventInfo, panelData);
				}
				
			}
			else
				
			{
				if (StringUtils.isEmpty(vcrProductName) || !StringUtils.equals(productName, vcrProductName))
				{
					String eventComment = "Mismatched VCRProductName by EQP " + vcrProductName + ", Product " + productName;
					eventLog.info(eventComment);
					eventInfo.setEventComment(eventComment);
					sendEmail(machineName, unitName, subUnitName, productName, vcrProductName, factoryName);
				}

				kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setProductEventInfo.getUdfs().put("VCRPRODUCTNAME", vcrProductName);

				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setProductEventInfo, eventInfo);
			}
		}
	}

	private void sendEmail(String machineName, String unitName, String subUnitName, String productName, String vcrProductName, String factoryName) throws CustomException
	{
		String message = "";
		message += "<pre>===============AlarmInformation===============</pre>";
		message += "<pre>==============================================</pre>";
		message += "<pre>- MachineName	: " + machineName + "</pre>";
		message += "<pre>- UnitName	: " + unitName + "</pre>";
		message += "<pre>- SubUnitName	: " + subUnitName + "</pre>";
		message += "<pre>- ProductName	: " + productName + "</pre>";
		message += "<pre>- VCRProductName	: " + vcrProductName + "</pre>";
		message += "<pre>==============================================</pre>";

		CommonUtil.SendMail("VCR", "VCRAlarmReport", message);
	}

	private void updateLotHistory(Lot lotData, String eventName, String eventTimeKey) throws CustomException
	{

		String condition = "UPDATE LOTHISTORY SET SOURCELOTNAME=?  WHERE LOTNAME=? AND TIMEKEY =? AND EVENTNAME=? ";
		Object[] bindSet = new Object[] {};
		bindSet = new Object[] { lotData.getSourceLotName(), lotData.getKey().getLotName(), eventTimeKey, eventName };
		kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);

	}
}
