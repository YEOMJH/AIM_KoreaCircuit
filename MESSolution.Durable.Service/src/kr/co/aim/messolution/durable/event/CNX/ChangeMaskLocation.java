package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class ChangeMaskLocation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub

		Element eleBody = SMessageUtil.getBodyElement(doc);
		String saction = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);
		if (saction.equals("FirstRight")) {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("MountMask", this.getEventUser(), this.getEventComment(),
					"", "");

			String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", sMachineName);
			udfs.put("UNITNAME", sUnitName);

			List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
			if (eleBody != null) {
				for (Element eledur : durableList) {
					String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
					String sReticleSlot = SMessageUtil.getChildText(eledur, "POSITION", true);

					// getMachineData
					CommonUtil.getMachineInfo(sMachineName);

					// getDurableData
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

					// Validation
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_Scrapped)) {
						throw new CustomException("MASK-0012", sDurableName, durableData.getDurableState());
					}
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_InUse)) {
						throw new CustomException("MASK-0026", sDurableName, durableData.getDurableState());
					}
					if (!StringUtils.equals(durableData.getDurableCleanState(),
							GenericServiceProxy.getConstantMap().Dur_Clean)) {
						throw new CustomException("MASK-0025", sDurableName);
					}
					if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y")) {
						throw new CustomException("MASK-0013", sDurableName);
					}
					
					MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETime(durableData, eventInfo);
					// if
					// (!StringUtils.equals(durableData.getUdfs().get("MACHINENAME"),
					// sMachineName))
					// {
					// throw new CustomException("MASK-0016", sDurableName);
					// }
					if (!(StringUtils.equals(durableData.getDurableType(), "PhotoMask")
							|| StringUtils.equals(durableData.getDurableType(), "Mask"))) {
						throw new CustomException("MASK-0027", durableData.getDurableType(), sDurableName);
					}

					if (StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), sMachineName)
							&& StringUtils.equals(durableData.getUdfs().get("UNITNAME"), sUnitName)
							&& StringUtils.equals(durableData.getDurableState(),
									GenericServiceProxy.getConstantMap().Dur_Mounted)) {
						if (StringUtils.equals(durableData.getUdfs().get("RETICLESLOT"), sReticleSlot)) {
							// skip when no changes
							continue;
						}
					}

					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
					udfs.put("RETICLESLOT", sReticleSlot);
					MESDurableServiceProxy.getDurableServiceImpl().mountPhotoMask(durableData,
							GenericServiceProxy.getConstantMap().Dur_Mounted, udfs, eventInfo);
				}
			}
		} else if (saction.equals("FirstLeft")) {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnMountMask", this.getEventUser(),
					this.getEventComment(), "", "");

			eleBody = SMessageUtil.getBodyElement(doc);
			String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", "");
			udfs.put("UNITNAME", "");

			if (eleBody != null) {
				for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false)) {
					String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);

					// getMachineData
					CommonUtil.getMachineInfo(sMachineName);

					// getDurableData
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

					// Validation
					/*
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_Scrapped)) {
						throw new CustomException("MASK-0012", sDurableName, durableData.getDurableState());
					}
					*/
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_InUse)) {
						throw new CustomException("MASK-0026", sDurableName, durableData.getDurableState());
					}
					if (MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETimeOver(durableData))
					{
						durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
					}
					/*
					if (!StringUtils.equals(durableData.getDurableCleanState(),
							GenericServiceProxy.getConstantMap().Dur_Clean)) {
						throw new CustomException("MASK-0025", sDurableName);
					}
					*/
					if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y")) {
						throw new CustomException("MASK-0013", sDurableName);
					}
					// if
					// (!StringUtils.equals(durableData.getUdfs().get("MACHINENAME"),
					// sMachineName))
					// {
					// throw new CustomException("MASK-0016", sDurableName);
					// }
					if (!(StringUtils.equals(durableData.getDurableType(), "PhotoMask")
							|| StringUtils.equals(durableData.getDurableType(), "Mask"))) {
						throw new CustomException("MASK-0027", durableData.getDurableType(), sDurableName);
					}
					
					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
					udfs.put("RETICLESLOT", "");
					MESDurableServiceProxy.getDurableServiceImpl().mountPhotoMask(durableData,
							GenericServiceProxy.getConstantMap().Dur_UnMounted, udfs, eventInfo);
					
				    durableData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
					//this.sendMessageToStocker(durableData,eventInfo,origialSourceSubjectName);
				}
			}

		} else if (saction.equals("SecondRight")) {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitMask", this.getEventUser(), this.getEventComment(),
					"", "");
			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", sMachineName);
			udfs.put("UNITNAME", sUnitName);

			if (eleBody != null) {
				for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false)) {
					String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
					CommonUtil.getMachineInfo(sMachineName);

					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
					
					MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETime(durableData, eventInfo);

					/*
					 * List<Durable> kittedMasks = null; String condition =
					 * " MACHINENAME = ? AND UNITNAME = ? AND DURABLETYPE = ? AND DURABLESTATE = ? AND TRANSPORTSTATE = ? AND RETICLESLOT IS NULL"
					 * ; Object[] bindSet = new Object[] { sMachineName,
					 * sUnitName, "PhotoMask", "InUse", "OutStock" }; try {
					 * kittedMasks =
					 * DurableServiceProxy.getDurableService().select(condition,
					 * bindSet); } catch(Exception de) {}
					 * 
					 * if (kittedMasks != null) { for (Durable mask :
					 * kittedMasks) { if
					 * (!StringUtil.equals(mask.getKey().getDurableName(),
					 * sDurableName)) { throw new CustomException("MASK-0031");
					 * } } }
					 */
					MESDurableServiceProxy.getDurableServiceImpl().KitMask(durableData, constantMap.Dur_InUse,
							"OnEQP", "", udfs, eventInfo);

				}
			}

		} else if (saction.equals("SecondLeft")) {

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnkitMask", this.getEventUser(), this.getEventComment(),
					"", "");

			String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

			for (Element eledur : durableList) {
				String durableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
				String position = SMessageUtil.getChildText(eledur, "POSITION", true);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("MACHINENAME", sMachineName);
				udfs.put("UNITNAME", sUnitName);

				CommonUtil.getMachineInfo(sMachineName);

				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

				/*
				if (StringUtils.equals(durableData.getDurableState(),
						GenericServiceProxy.getConstantMap().Dur_Scrapped)) {
					throw new CustomException("MASK-0012", durableName, durableData.getDurableState());
				}
				*/
				if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y")) {
					throw new CustomException("MASK-0013", durableName);
				}
				
				if (MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETimeOver(durableData))
				{
					durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
				}

				MESDurableServiceProxy.getDurableServiceImpl().KitMask(durableData,
						"Mounted", "OnEQP", position, udfs, eventInfo);
			}

			/*
			 * Map<String, String> udfs = new HashMap<String, String>();
			 * udfs.put("MACHINENAME", ""); udfs.put("UNITNAME", "");
			 * 
			 * 
			 * if (eleBody != null) { for (Element eledur :
			 * SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
			 * { String sDurableName = SMessageUtil.getChildText(eledur,
			 * "DURABLENAME", true);
			 * 
			 * // getMachineData CommonUtil.getMachineInfo(sMachineName);
			 * 
			 * // getDurableData Durable durableData =
			 * MESDurableServiceProxy.getDurableServiceUtil().getDurableData(
			 * sDurableName);
			 * 
			 * // Validation if
			 * (StringUtils.equals(durableData.getDurableState(),
			 * GenericServiceProxy.getConstantMap().Dur_Scrapped)) { throw new
			 * CustomException("MASK-0012", sDurableName,
			 * durableData.getDurableState()); } if
			 * (!StringUtils.equals(durableData.getDurableCleanState(),
			 * GenericServiceProxy.getConstantMap().Dur_Clean)) { throw new
			 * CustomException("MASK-0025", sDurableName); } if
			 * (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE")
			 * , "Y")) { throw new CustomException("MASK-0013", sDurableName); }
			 * if (!(StringUtils.equals(durableData.getDurableType(),
			 * "PhotoMask") || StringUtils.equals(durableData.getDurableType(),
			 * "Mask"))) { throw new CustomException("MASK-0027",
			 * durableData.getDurableType(), sDurableName); }
			 * 
			 * udfs.put("TRANSPORTSTATE", "InStock"); udfs.put("RETICLESLOT",
			 * "");
			 * MESDurableServiceProxy.getDurableServiceImpl().mountPhotoMask(
			 * durableData, GenericServiceProxy.getConstantMap().Dur_UnMounted,
			 * udfs, eventInfo); } }
			 */

			/*
			 * 
			 * ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			 * EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnkitMask",
			 * this.getEventUser(), this.getEventComment(), "", "");
			 * if(eleBody!=null) { String sMachineName =
			 * SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true); String
			 * sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			 * for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,
			 * "DURABLELIST", false)) { String durableName =
			 * SMessageUtil.getChildText(eledur, "DURABLENAME", true); //String
			 * position = SMessageUtil.getChildText(eledur, "POSITION", true);
			 * 
			 * 
			 * Map<String, String> udfs = new HashMap<String, String>();
			 * udfs.put("MACHINENAME", sMachineName); udfs.put("UNITNAME",
			 * sUnitName);
			 * 
			 * CommonUtil.getMachineInfo(sMachineName);
			 * 
			 * Durable durableData = CommonUtil.getDurableInfo(durableName);
			 * 
			 * MESDurableServiceProxy.getDurableServiceImpl().KitMask(
			 * durableData, constantMap.Dur_Mounted, "InStock" , "", udfs,
			 * eventInfo); } }
			 */
		} else if (saction.equals("ChangePosition")) {

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", this.getEventUser(),
					this.getEventComment(), "", "");

			String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", sMachineName);
			udfs.put("UNITNAME", sUnitName);

			List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
			if (eleBody != null) {
				for (Element eledur : durableList) {
					String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
					String sReticleSlot = SMessageUtil.getChildText(eledur, "POSITION", true);

					// getMachineData
					CommonUtil.getMachineInfo(sMachineName);

					// getDurableData
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

					// Validation
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_Scrapped)) {
						throw new CustomException("MASK-0012", sDurableName, durableData.getDurableState());
					}
					if (StringUtils.equals(durableData.getDurableState(),
							GenericServiceProxy.getConstantMap().Dur_InUse)) {
						throw new CustomException("MASK-0026", sDurableName, durableData.getDurableState());
					}
					if (!StringUtils.equals(durableData.getDurableCleanState(),
							GenericServiceProxy.getConstantMap().Dur_Clean)) {
						throw new CustomException("MASK-0025", sDurableName);
					}
					if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y")) {
						throw new CustomException("MASK-0013", sDurableName);
					}
					// if
					// (!StringUtils.equals(durableData.getUdfs().get("MACHINENAME"),
					// sMachineName))
					// {
					// throw new CustomException("MASK-0016", sDurableName);
					// }
					if (!(StringUtils.equals(durableData.getDurableType(), "PhotoMask")
							|| StringUtils.equals(durableData.getDurableType(), "Mask"))) {
						throw new CustomException("MASK-0027", durableData.getDurableType(), sDurableName);
					}

					if (StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), sMachineName)
							&& StringUtils.equals(durableData.getUdfs().get("UNITNAME"), sUnitName)
							&& StringUtils.equals(durableData.getDurableState(),
									GenericServiceProxy.getConstantMap().Dur_Mounted)) {
						if (StringUtils.equals(durableData.getUdfs().get("RETICLESLOT"), sReticleSlot)) {
							// skip when no changes
							continue;
						}
					}

					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
					udfs.put("RETICLESLOT", sReticleSlot);
					MESDurableServiceProxy.getDurableServiceImpl().mountPhotoMask(durableData,
							GenericServiceProxy.getConstantMap().Dur_Mounted, udfs, eventInfo);
				}
			}
		}
		return doc;
	}
	
	private void sendMessageToStocker(Durable durableData, EventInfo eventInfo,String origialSourceSubjectName) throws CustomException
	{
		String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");

		if (lineName == null || lineName.isEmpty())
		{
			LogFactory.getLog(this.getClass()).info(String.format("Mask [%s] Stocker LineName is empty.", durableData.getKey().getDurableName()));
		}
		else
		{
			// Get Stocker machine info(Durable.MASKSTOCKERNAME = PhotoMaskStocker.LineName and is Main machine)
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);

			if (machineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
			{
				String targetSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");

				Durable maskData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
				Document messageDoc = MESDurableServiceProxy.getDurableServiceUtil().generateIMSMaskChangeInfo(origialSourceSubjectName,machineData, maskData, eventInfo);

				try
				{
					GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, messageDoc, "EISSender");
				}
				catch (Exception e)
				{
					//SYSTEM-0001: IMSMessage send fail
					throw new CustomException("SYSTEM-0001");
				}
			}
		}
	}
}
