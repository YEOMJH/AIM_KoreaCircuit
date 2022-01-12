package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMask extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		ArrayList<Machine> IMSMachineList = new ArrayList<Machine>();
		ArrayList<Durable> IMSMaskList = new ArrayList<Durable>();

		for (Element eledur : durableList)
		{
			String sfactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String sPosition = SMessageUtil.getChildText(eledur, "POSITION", true);
			String sDurableType = SMessageUtil.getChildText(eledur, "DURABLETYPE", true);
			String sDurableSpec = SMessageUtil.getChildText(eledur, "DURABLESPECNAME", true);
			String sTimeUsedLimit = SMessageUtil.getChildText(eledur, "TIMEUSEDLIMIT", false);
			String sDurationUsedLimit = SMessageUtil.getChildText(eledur, "DURATIONUSEDLIMIT", false);
			String sPhotoMaskStock = SMessageUtil.getChildText(eledur, "MASKSTOCK", true);
			
			if(IMSMachineList.size() == 0)
			{
				MachineKey machineKey = new MachineKey();
				machineKey.setMachineName(sPhotoMaskStock);
				Machine stockInfo = MachineServiceProxy.getMachineService().selectByKey(machineKey);
				
				if(stockInfo.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
				{
					IMSMachineList.add(stockInfo);
				}
			}
			else
			{
				boolean findCheck = false;
				for(Machine stockInfo: IMSMachineList)
				{
					if(sPhotoMaskStock.equals(stockInfo.getKey().getMachineName()))
					{
						findCheck = true;
						break;
					}
				}
				
				if(!findCheck)
				{
					MachineKey machineKey = new MachineKey();
					machineKey.setMachineName(sPhotoMaskStock);
					Machine stockInfo = MachineServiceProxy.getMachineService().selectByKey(machineKey);
					
					if(stockInfo.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
					{
						IMSMachineList.add(stockInfo);
					}
				}
			}
			
			MESDurableServiceProxy.getDurableServiceImpl().checkExistDurable(sDurableName);
			DurableSpecKey specKey = new DurableSpecKey();
			specKey.setDurableSpecName(sDurableSpec);
			specKey.setDurableSpecVersion("00001");
			specKey.setFactoryName(sfactoryName);
			DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(specKey);

			Durable maskData = new Durable();
			DurableKey maskKey = new DurableKey(sDurableName);
			maskData.setKey(maskKey);
			maskData.setFactoryName(sfactoryName);
			maskData.setDurableType(sDurableType);
			maskData.setDurableSpecName(sDurableSpec);
			maskData.setDurableSpecVersion("00001");
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			maskData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Clean);
			maskData.setTimeUsedLimit(Long.parseLong(sTimeUsedLimit));
			maskData.setDurationUsedLimit(Long.parseLong(sDurationUsedLimit));
			maskData.setCreateTime(eventInfo.getEventTime());
			maskData.setCreateUser(eventInfo.getEventUser());
			Map<String, String> maskUdfs = maskData.getUdfs();
			
			SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(maskUdfs);
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
			setEventInfo.getUdfs().put("POSITION", sPosition);
			setEventInfo.getUdfs().put("LASTCLEANTIME", eventInfo.getEventTime().toString());
			setEventInfo.getUdfs().put("CLEANUSEDLIMIT", durableSpec.getUdfs().get("CLEANUSEDLIMIT").toString());
			setEventInfo.getUdfs().put("MASKSTOCKERNAME", sPhotoMaskStock);
			setEventInfo.getUdfs().put("CLEANUSED", "0");
			setEventInfo.getUdfs().put("VENDORNUMBER", durableSpec.getUdfs().get("VENDOR"));
			setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "N");
			//setEventInfo.getUdfs().put("DEPARTMENT", "");
			DurableServiceProxy.getDurableService().insert(maskData);
			Durable afterInfo = DurableServiceProxy.getDurableService().setEvent(maskKey, eventInfo, setEventInfo);
			
			IMSMaskList.add(afterInfo);
		}
		
		for(Machine stockInfo : IMSMachineList)
		{
			Document IMSDoc = generateIMSCreateMaskSend(stockInfo, IMSMaskList);
			
			SMessageUtil.setHeaderItemValue(IMSDoc, "EVENTUSER", eventInfo.getEventUser());
			String targetSubjectName = stockInfo.getUdfs().get("MCSUBJECTNAME").toString();
			SMessageUtil.setItemValue(IMSDoc, "Header", "SHOPNAME", stockInfo.getFactoryName());
			SMessageUtil.setItemValue(IMSDoc, "Header", "MACHINENAME", stockInfo.getKey().getMachineName());
			SMessageUtil.setItemValue(IMSDoc, "Header", "ORIGINALSOURCESUBJECTNAME",SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true) );
			SMessageUtil.setItemValue(IMSDoc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("CNXsvr"));
			SMessageUtil.setItemValue(IMSDoc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
			
			try 
			{
				GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(targetSubjectName, IMSDoc, "EISSender");
				GenericServiceProxy.getMessageTraceService().recordMessageLog(IMSDoc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
			} 
			catch (Exception e) 
			{
				//SYSTEM-0001: IMSMessage send fail
				throw new CustomException("SYSTEM-0001");
			}
			
		}
		
		return doc;
	}
	
	public Document generateIMSCreateMaskSend(Machine stockInfo, ArrayList<Durable> maskList) throws CustomException                
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("LINENAME");
				attMachineName.setText(stockInfo.getKey().getMachineName());
				bodyElement.addContent(attMachineName);
				
				Element attMaskList = new Element("MASKLIST");
				
				for(Durable maskInfo : maskList)
				{
					if(stockInfo.getKey().getMachineName().equals(maskInfo.getUdfs().get("MASKSTOCKERNAME")))
					{
						Element attMask = new Element("MASK");
						
						Element attMaskID = new Element("MASKID");
						attMaskID.setText(maskInfo.getKey().getDurableName());
						attMask.addContent(attMaskID);
						
						Element attVendor = new Element("VENDOR");
						attVendor.setText(maskInfo.getUdfs().get("VENDORNUMBER"));
						attMask.addContent(attVendor);
						
						Element attMaskState = new Element("MASKTSTATE");
						attMaskState.setText(maskInfo.getDurableState());
						attMask.addContent(attMaskState);
						
						Element cleanState = new Element("CLEANSTATE");
						cleanState.setText(maskInfo.getDurableCleanState());
						attMask.addContent(cleanState);
						
						Element attHoldState = new Element("HOLDSTATE");
						attHoldState.setText(maskInfo.getUdfs().get("DURABLEHOLDSTATE").toString());
						attMask.addContent(attHoldState);
						
						Element attTransferState = new Element("TRANSFERSTATE");
						attTransferState.setText(maskInfo.getUdfs().get("TRANSPORTSTATE"));
						attMask.addContent(attTransferState);
						
						attMaskList.addContent(attMask);
					}
				}
				
				bodyElement.addContent(attMaskList);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "IMSCreateMaskSend", "", "", "MES", "");
			
			return doc;
		}
		catch (Exception ex)
		{
			//SYSTEM-0002: IMSMessage generate fail
			throw new CustomException("SYSTEM-0002");
		}
	}
}