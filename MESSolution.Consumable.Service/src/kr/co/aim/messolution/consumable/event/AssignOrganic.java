package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SplitInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class AssignOrganic extends SyncHandler {

	private static Log log = LogFactory.getLog(AssignOrganic.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String crucibleName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLENAME", true);
		String crucibleLotName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLELOTNAME", true);
		String weight = SMessageUtil.getBodyItemValue(doc, "WEIGHT", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String consumableSpecName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESPECNAME", true);
		String consumableSpecVersion = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESPECVERSION", false);
		String crucibleWeight = SMessageUtil.getBodyItemValue(doc, "CRUCIBLEWEIGHT", false);
		
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<Element> organicList = SMessageUtil.getBodySequenceItemList(doc, "CONSUMABLELIST", true);
		List<String> organicNameList = CommonUtil.makeList(bodyElement, "CONSUMABLELIST", "CONSUMABLENAME");

		if (organicNameList.size() > 0)
		{
			Durable crucibleData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(crucibleName);
			CrucibleLot crucibleLot = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { crucibleLotName });
			ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, consumableSpecName, consumableSpecVersion);
			String crucibleLotState = crucibleLot.getCrucibleLotState();

			if (StringUtils.equals(crucibleLotState, GenericServiceProxy.getConstantMap().CrucibleLotState_Scrapped))
				throw new CustomException("MATERIAL-9009", crucibleLotState, crucibleLotName);
			
//补料需求：加完料后更改加料计划仍可加料
//			if (!StringUtils.equals(crucibleLotState, GenericServiceProxy.getConstantMap().CrucibleLotState_Created))
//				throw new CustomException("MATERIAL-9010", crucibleLotState, GenericServiceProxy.getConstantMap().CrucibleLotState_Created, crucibleLotName);
//
//			int organicQty = getAssignedConsumableQuantityByCrucibleLot(crucibleLotName);
//
//			if (organicQty > 0)
//				throw new CustomException("MATERIAL-9011", organicQty, crucibleLotName);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignOrganic", getEventUser(), getEventComment(), null, null);

			for (Element organicE : organicList)
			{
				String organicName = organicE.getChildText("CONSUMABLENAME");
				String inputQty = organicE.getChildText("INPUTQUANTITY");
				String consumedFlag = organicE.getChildText("CONSUMEDFLAG");
				
				Consumable oriOrganicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName.substring(0, organicName.lastIndexOf("-")));
				//eventInfo = EventInfoUtil.makeEventInfo("AssignOrganic", getEventUser(), getEventComment(), null, null);
				
				if (StringUtils.isEmpty(oriOrganicData.getUdfs().get("COVEROPENTIME")))
				{
					eventInfo = EventInfoUtil.makeEventInfo("OpenCover", getEventUser(), getEventComment(), null, null);
					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfoForActionOrganic = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
					setEventInfoForActionOrganic.getUdfs().put("COVEROPENTIME", TimeStampUtil.getCurrentTime());

					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(oriOrganicData.getKey().getConsumableName(), setEventInfoForActionOrganic, eventInfo);
					oriOrganicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(oriOrganicData.getKey().getConsumableName());
				}

				Map<String, String> conUdfs = new HashMap<String, String>();
				conUdfs.put("ASSIGNEDQTY", String.valueOf(inputQty));
				conUdfs.put("CARRIERNAME", crucibleName);
				conUdfs.put("CRUCIBLELOTNAME", crucibleLotName);
				conUdfs.put("COVEROPENTIME", oriOrganicData.getUdfs().get("COVEROPENTIME"));
				conUdfs.put("LIFETIMEOPEN", consumableSpec.getUdfs().get("LIFETIMEOPEN"));
				conUdfs.put("LIFETIMESTORE", consumableSpec.getUdfs().get("LIFETIMESTORE"));
				conUdfs.put("EXPIRATIONDATE", oriOrganicData.getUdfs().get("EXPIRATIONDATE"));
				conUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Crucible);
				//houxk 20201207
				conUdfs.put("BATCHNO", oriOrganicData.getUdfs().get("BATCHNO"));
				conUdfs.put("TIMEUSEDLIMIT", oriOrganicData.getUdfs().get("TIMEUSEDLIMIT"));
				conUdfs.put("DURATIONUSEDLIMIT", oriOrganicData.getUdfs().get("DURATIONUSEDLIMIT"));
				conUdfs.put("PRODUCTDATE", oriOrganicData.getUdfs().get("PRODUCTDATE"));
				conUdfs.put("CONSUMABLEHOLDSTATE", oriOrganicData.getUdfs().get("CONSUMABLEHOLDSTATE"));
				conUdfs.put("WMSFACTORYNAME", oriOrganicData.getUdfs().get("WMSFACTORYNAME"));
				conUdfs.put("BOXID", oriOrganicData.getUdfs().get("BOXID"));
				conUdfs.put("DEPARTEXPENSEFLAG", oriOrganicData.getUdfs().get("DEPARTEXPENSEFLAG"));
				conUdfs.put("WMSFACTORYPOSITION", oriOrganicData.getUdfs().get("WMSFACTORYPOSITION"));

				SplitInfo splitInfo = new SplitInfo();
				splitInfo.setChildConsumableName(organicName);
				splitInfo.setChildConsumableUdfs(conUdfs);
				splitInfo.setQuantity(Double.parseDouble(inputQty));
				
				if(oriOrganicData.getConsumableType().equals("Organic"))
				{
					EventInfo splitEventInfo = EventInfoUtil.makeEventInfoForOrganic("AssignOrganic", getEventUser(), getEventComment());
					MESConsumableServiceProxy.getConsumableServiceImpl().split(oriOrganicData, splitInfo, splitEventInfo);
				}
				else if(oriOrganicData.getConsumableType().equals("InOrganic"))
				{
					EventInfo splitEventInfo = EventInfoUtil.makeEventInfoForOrganic("AssignInOrganic", getEventUser(), getEventComment());
					MESConsumableServiceProxy.getConsumableServiceImpl().split(oriOrganicData, splitInfo, splitEventInfo);
				}
								
				Consumable actionOrganic = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName);
				oriOrganicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName.substring(0, organicName.lastIndexOf("-")));

				if (StringUtil.equals(consumedFlag, "Y"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("UnConsumedAll", getEventUser(), getEventComment(), null, null);
					
					Map<String, String> udfs = new HashMap<String, String>();
					udfs.put("CONSUMEDFLAG", consumedFlag);
					//ConsumableServiceProxy.getConsumableService().update(oriOrganicData);
					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfoForOriOrganic = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
					setEventInfoForOriOrganic.setUdfs(udfs);
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(oriOrganicData.getKey().getConsumableName(), setEventInfoForOriOrganic, eventInfo);
				}
				if (StringUtil.equals(consumedFlag, "N"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ConsumedAll", getEventUser(), getEventComment(), null, null);
					
					Map<String, String> udfs = new HashMap<String, String>();
					udfs.put("CONSUMEDFLAG", consumedFlag);
					//ConsumableServiceProxy.getConsumableService().update(oriOrganicData);
					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfoForOriOrganic = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
					setEventInfoForOriOrganic.setUdfs(udfs);
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(oriOrganicData.getKey().getConsumableName(), setEventInfoForOriOrganic, eventInfo);
				}
				
				if (StringUtil.equals(actionOrganic.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
					actionOrganic.setConsumableState("InUse");

					ConsumableServiceProxy.getConsumableService().update(actionOrganic);
					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfoForActionOrganic = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(actionOrganic.getKey().getConsumableName(), setEventInfoForActionOrganic, eventInfo);
				}

				if (StringUtil.equals(consumedFlag,"N") && StringUtil.equals(oriOrganicData.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);

					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(oriOrganicData, makeNotAvailableInfo, eventInfo);
				}

				//sendMail(eventInfo, crucibleData, oriOrganicData, actionOrganic, inputQty);
			}
			
			if(consumableSpec.getConsumableType().equals("Organic"))
			{
				eventInfo = EventInfoUtil.makeEventInfo("AssignOrganic", getEventUser(), getEventComment(), null, null);
			}
			else if(consumableSpec.getConsumableType().equals("InOrganic"))
			{
				eventInfo = EventInfoUtil.makeEventInfo("AssignInOrganic", getEventUser(), getEventComment(), null, null);
			}
			
			crucibleData.setLotQuantity(1);
			crucibleData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			DurableServiceProxy.getDurableService().update(crucibleData);

			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(crucibleData, setEventInfo, eventInfo);

			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			crucibleLot.setCrucibleLotState(GenericServiceProxy.getConstantMap().CrucibleLotState_Released);
			crucibleLot.setWeight(Double.valueOf(weight));
			crucibleLot.setDurableName(crucibleName);
			crucibleLot.setOldDurableName(crucibleName);
			crucibleLot.setLastEventComment(eventInfo.getEventComment());
			crucibleLot.setLastEventName(eventInfo.getEventName());
			crucibleLot.setLastEventTime(eventInfo.getEventTime());
			crucibleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
			crucibleLot.setLastEventUser(eventInfo.getEventUser());
			
			if(StringUtils.equals(crucibleLotState, GenericServiceProxy.getConstantMap().CrucibleLotState_Created))
			{
				crucibleLot.setCrucibleWeight(crucibleWeight.isEmpty()?0.0d:Double.valueOf(crucibleWeight));
				crucibleLot.setAssignTime(eventInfo.getEventTime());
			}			
			
			ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLot);
		}
		else
		{
			throw new CustomException("MATERIAL-0023");
		}

		return doc;
	}

	public int getAssignedConsumableQuantityByCrucibleLot(String crucibleLotName)
	{
		int qty = 0;
		String sql = "SELECT COUNT (CONSUMABLENAME) AS QTY FROM CONSUMABLE WHERE CRUCIBLELOTNAME = :CRUCIBLELOTNAME ";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CRUCIBLELOTNAME", crucibleLotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");

			qty = Integer.parseInt(quantity);
		}

		return qty;
	}

	public void sendMail(EventInfo eventInfo, Durable crucibleData, Consumable oriConsumable, Consumable actionConsumable, String qty) throws CustomException
	{
		String[] mailList = CommonUtil.getEmailListByAlarmGroup("OrganicScaleReport");

		if (mailList == null || mailList.length == 0)
			return;

		String message = "<pre>=======================AlarmInformation=======================</pre>";
		message += "<pre>==============================================================</pre>";
		message += "<pre>- CrucibleName			: " + crucibleData.getKey().getDurableName() + "</pre>";
		message += "<pre>- Origianl OrganicName	: " + oriConsumable.getKey().getConsumableName() + "</pre>";
		message += "<pre>- Assign OrganicName	: " + actionConsumable.getKey().getConsumableName() + "</pre>";
		message += "<pre>- Quantity				: " + qty + "</pre>";
		message += "<pre>- EventComment			: " + eventInfo.getEventComment() + "</pre>";
		message += "<pre>==============================================================</pre>";

		try
		{
			GenericServiceProxy.getMailSerivce().postMail(mailList, this.getClass().getSimpleName(), message);
		}
		catch (Exception ex)
		{
			if (ex instanceof CustomException)
			{
				log.info(((CustomException) ex).errorDef.getEng_errorMessage());
				// CommonUtil.sendSMSWhenPostMailFail(message);
			}
			else
			{
				throw new CustomException(ex.getCause());
			}
		}
	}
}
