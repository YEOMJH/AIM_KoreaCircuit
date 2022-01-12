package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class CoverTrayGroupProcessAbort extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(CoverTrayGroupProcessAbort.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
		String BCRFlag = SMessageUtil.getBodyItemValue(doc, "BCRFLAG", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		List<Element> trayElementList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(coverTrayName);
		
		//send ok reply to EQP unconditionally
	    this.sendUnconditionallyOKReply(doc,machineName);
		
		int totalLotQty = 0;
		int maxPosition = 1;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", getEventUser(), getEventComment());
		
		if (trayElementList == null || trayElementList.size() <= 0)
		{
			log.info("No TrayList information received from BC.");
		}
		else
		{
			if (trayGroupData.getDurableType().equals("CoverTray") || trayGroupData.getDurableState().equals("InUse") || trayGroupData.getLotQuantity() > 0)
				throw new CustomException("DURABLE-0008", coverTrayName);

			// Assign Tray from TrayGroup
			Map<String, Durable> durableDataMap = this.generateDurableDataMap(trayElementList, true);
			Map<String,List<Element>> trayPanelElementMap = new HashMap<>();
			
			List<Durable> updateDurableList = new ArrayList<>();
			List<DurableHistory> updateDurableHistList = new ArrayList<>();

			for (Element trayElement : trayElementList)
			{
				String trayName = trayElement.getChildText("TRAYNAME");
				List<Element> panelElementList =  SMessageUtil.getSubSequenceItemList(trayElement, "PANELLIST", false); 
				
				if(panelElementList!=null && panelElementList.size()>0) trayPanelElementMap.put(trayName, panelElementList);
				
				int position = Integer.parseInt(trayElement.getChildText("POSITION"));
				int productQty = Integer.parseInt(trayElement.getChildText("PRODUCTQUANTITY"));

				maxPosition = position;
				totalLotQty += productQty;
				
				if(coverTrayName.equals(trayName)) continue;

				Durable trayData = durableDataMap.get(trayName);
				Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

				trayData.setLotQuantity(productQty);
				trayData.setDurableState(productQty == 0 ? GenericServiceProxy.getConstantMap().Dur_Available : GenericServiceProxy.getConstantMap().Dur_InUse);
				
				trayData.getUdfs().put("COVERNAME", coverTrayName);
				trayData.getUdfs().put("POSITION", trayElement.getChildText("POSITION"));
				trayData.getUdfs().put("BCRFLAG", trayElement.getChildText("BCRFLAG"));
				trayData.getUdfs().put("DURABLETYPE1", "Tray");
				trayData.setLastEventName(eventInfo.getEventName());
				trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				trayData.setLastEventTime(eventInfo.getEventTime());
				trayData.setLastEventUser(eventInfo.getEventUser());
				trayData.setLastEventComment(eventInfo.getEventComment());

				if (Integer.parseInt(trayElement.getChildText("POSITION")) > maxPosition)
					maxPosition = Integer.parseInt(trayElement.getChildText("POSITION")) + 1;

				DurableHistory durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayData, trayData, new DurableHistory());

				updateDurableList.add(trayData);
				updateDurableHistList.add(durHistory);
			}

			if (updateDurableList.size() > 0)
			{
				try
				{
					CommonUtil.executeBatch("update", updateDurableList, true);
					CommonUtil.executeBatch("insert", updateDurableHistList, true);

					log.info(String.format("▶Successfully update %s pieces of trays.", updateDurableList.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
			
			if(trayPanelElementMap.size()>0)
			{
				List<Lot> lotDataList = new ArrayList<>();
				List<LotHistory> lotHistList = new ArrayList<>();

				for (String trayName : trayPanelElementMap.keySet())
				{
					List<Element> panelElementList = trayPanelElementMap.get(trayName);
					Map<String, Element> panelElementMap = this.makePanelElementMap(panelElementList);

					List<Lot> panelDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(CommonUtil.makeList(panelElementList, "PANELNAME"), true);

					if (panelDataList == null || panelDataList.size() == 0) continue;

					for (Lot lotData : panelDataList)
					{
						// PANEL-0005: This Panel[{0}] is assigned another Tray[{1}]
						if (!lotData.getCarrierName().isEmpty())
							throw new CustomException("PANEL-0005", lotData.getKey().getLotName(), String.format("BC Reported: %s , MES Registered: %s", trayName, lotData.getCarrierName()));

						CommonValidation.checkLotProcessState(lotData);

						Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

						lotData.setCarrierName(trayName);
						lotData.setLastEventName("AssignTray");
						lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						lotData.setLastEventTime(eventInfo.getEventTime());
						lotData.setLastEventUser(eventInfo.getEventUser());
						lotData.setLastEventComment(eventInfo.getEventComment());

						lotData.getUdfs().put("POSITION", panelElementMap.get(lotData.getKey().getLotName()).getChildText("POSITION"));
						lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
						lotData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
						lotData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
						lotData.setLotGrade(panelElementMap.get(lotData.getKey().getLotName()).getChildText("PRODUCTJUDGE"));
						lotData.getUdfs().put("LOTDETAILGRADE", panelElementMap.get(lotData.getKey().getLotName()).getChildText("PRODUCTGRADE"));

						LotHistory lotHistData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, new LotHistory());

						lotDataList.add(lotData);
						lotHistList.add(lotHistData);
					}
				}

				if (lotDataList.size() > 0)
				{
					try
					{
						CommonUtil.executeBatch("update", lotDataList, true);
						CommonUtil.executeBatch("insert", lotHistList, true);

						log.info(String.format("▶Successfully update %s pieces of Lot.", updateDurableList.size()));
					}
					catch (Exception e)
					{
						log.error(e.getMessage());
						throw new CustomException(e.getCause());
					}
				}
			}
		}
		
		if (trayElementList == null || trayElementList.size() == 0)
			eventInfo.setEventName("UpdateCovertray");
		
		// Set TrayGroup
		trayGroupData.setDurableType("CoverTray");
		trayGroupData.setLotQuantity(totalLotQty);
		trayGroupData.setDurableState(totalLotQty == 0 ? GenericServiceProxy.getConstantMap().Dur_Available : GenericServiceProxy.getConstantMap().Dur_InUse);
		
		DurableServiceProxy.getDurableService().update(trayGroupData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("POSITION",String.valueOf(maxPosition));
		setEventInfo.getUdfs().put("COVERNAME", trayGroupData.getKey().getDurableName());
		setEventInfo.getUdfs().put("BCRFLAG", BCRFlag);
		setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
		
		DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
	}
	
	public Document createReplyDocument(Document doc)
	{
		Document replyDoc = (Document) doc.clone();
		Element bodyElement = replyDoc.getRootElement().getChild(SMessageUtil.Body_Tag);
		
		Element trayListElement = XmlUtil.getChild(bodyElement, "TRAYLIST", false);
		
		if(trayListElement!=null)
		{
			trayListElement.detach();
		}

		SMessageUtil.setHeaderItemValue(replyDoc, SMessageUtil.MessageName_Tag, "CoverTrayGroupProcessAbortReply");
		
		bodyElement.addContent(new Element("RESULT").setText("OK"));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(""));
		
		return replyDoc;
	}
	
	public void sendUnconditionallyOKReply(Document doc, String machineName)
	{
		Document replyDoc = this.createReplyDocument(doc);

		try
		{
			String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, replyDoc, "EISSender");			
			//record message log ---add by cjl 20201203
			GenericServiceProxy.getMessageTraceService().recordMessageLog(replyDoc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}
	}

	public Element removeCoverTrayElement(List<Element> trayElementList,String coverTrayName) throws CustomException
	{
		Element coverTrayElement = null;
		
		for (Element trayElement : new ArrayList<>(trayElementList))
		{
			if (trayElement.getChildText("TRAYNAME").equals(coverTrayName))
			{
				coverTrayElement = (Element) trayElement.clone();
				trayElementList.remove(trayElement);
			}
		}
		
		if(coverTrayElement ==null)
			log.info("Important content: There is no CoverTray information in the TrayList reported by BC .");
		
		return coverTrayElement;
	}
	
	public Map<String, Durable> generateDurableDataMap(List<Element> trayElementList, boolean forUpdate) throws CustomException
	{
		List<Durable> durableList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByTrayNameList(CommonUtil.makeList(trayElementList, "TRAYNAME"), true);
		
		Map<String, Durable> durableDataMap = new HashMap<>(trayElementList.size());

		for (Durable durableData : durableList)
		{
			durableDataMap.put(durableData.getKey().getDurableName(), durableData);
		}

		return durableDataMap;
	}
	
	public Map<String, Element> makePanelElementMap(List<Element> panelElementList)
	{
		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelEleemnt : panelElementList)
			panelElementMap.put(panelEleemnt.getChildText("PANELNAME"), panelEleemnt);

		return panelElementMap;
	}
}
