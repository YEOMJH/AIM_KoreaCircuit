package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.impl.MaskLotService;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskRecipeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class ActionMaskRecipe extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveMaskRecipe", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String flag = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		List<ReserveMaskRecipe> maskRecipeList = new ArrayList<ReserveMaskRecipe>();
		List<Object[]> updateArgList = new ArrayList<Object[]>();
		ArrayList<String> existMaskLotList = new ArrayList<String>();
		for (Element eleMask : maskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(eleMask, "MASKLOTNAME", true);
			String maskSpecName = SMessageUtil.getChildText(eleMask, "MASKSPECNAME", true);
			String toProcessFlowName = SMessageUtil.getChildText(eleMask, "TOPROCESSFLOWNAME", true);
			String toProcessFlowVersion = SMessageUtil.getChildText(eleMask, "TOPROCESSFLOWVERSION", true);
			String toProcessOperationName = SMessageUtil.getChildText(eleMask, "TOPROCESSOPERATIONNAME", true);
			String toProcessOperationVersion = SMessageUtil.getChildText(eleMask, "TOPROCESSOPERATIONVERSION", true);
			String toMachineName = SMessageUtil.getChildText(eleMask, "TOMACHINENAME", true);
			String toRecipeName = SMessageUtil.getChildText(eleMask, "TORECIPENAME", true);
			String rmsFlag = SMessageUtil.getChildText(eleMask, "RMSFLAG", true);
			String newToProcessFlowName = SMessageUtil.getChildText(eleMask, "NEWTOPROCESSFLOWNAME", false);
			String newToProcessFlowVersion = SMessageUtil.getChildText(eleMask, "NEWTOPROCESSFLOWVERSION", false);
			String newToProcessOperationName = SMessageUtil.getChildText(eleMask, "NEWTOPROCESSOPERATIONNAME", false);
			String newToProcessOperationVersion = SMessageUtil.getChildText(eleMask, "NEWTOPROCESSOPERATIONVERSION", false);
			String newToMachineName = SMessageUtil.getChildText(eleMask, "NEWTOMACHINENAME", false);

			MaskLot maskLotData = null;
			try
			{
				maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			}
			catch (greenFrameDBErrorSignal nfds)
			{
				throw new CustomException("DURABLE-5050", maskLotName);
			}

			// MaskLotHoldState Check
			if (StringUtil.equals(maskLotData.getMaskLotHoldState(), constantMap.MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotData.getMaskLotName());

			// MaskLotState Check
			if (!StringUtil.equals(maskLotData.getMaskLotState(), constantMap.MaskLotState_Released))
				throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());

			if (!StringUtil.equalsIgnoreCase(flag, "Delete"))
			{
				// Check Operation between Tension and last inspection.
				ProcessOperationSpec toOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), toProcessOperationName, toProcessOperationVersion);

				ProcessFlowKey currentProcessFlowKey = new ProcessFlowKey();
				currentProcessFlowKey.setFactoryName(maskLotData.getFactoryName());
				currentProcessFlowKey.setProcessFlowName(maskLotData.getMaskProcessFlowName());
				currentProcessFlowKey.setProcessFlowVersion(maskLotData.getMaskProcessFlowVersion());

				if (!StringUtil.equalsIgnoreCase(toOperationData.getProcessOperationType(), "Inspection") && !StringUtil.equalsIgnoreCase(toOperationData.getDetailProcessOperationType(), "MASKPPA"))
					throw new CustomException("MASK-0046", toProcessOperationName);

			}

			Object[] keySet = new Object[] { maskLotName, maskSpecName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, toMachineName };
			ReserveMaskRecipe maskRecipeData = null;

			if (StringUtil.equalsIgnoreCase(flag, "Create"))
			{
				try
				{
					maskRecipeData = ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(false, keySet);
				}
				catch (greenFrameDBErrorSignal nfds)
				{
				}

				if (maskRecipeData != null)
				{
					eventLog.info("This Mask Lot is already reserved.");
					continue;
				}

				maskRecipeData = new ReserveMaskRecipe(maskLotName, maskSpecName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, toMachineName);
				maskRecipeData.setMachineName(toMachineName);
				maskRecipeData.setRecipeName(toRecipeName);
				maskRecipeData.setRMSFlag(rmsFlag);

				maskRecipeList.add(maskRecipeData);
				if(existMaskLotList.contains(maskLotName))
				{
					eventLog.info("The Same Mask Lot Exist Multiple ActionMaskRecipe at the Same Time. ");
				}
				else
				{
					MaskLotService MaskLotService = ExtendedObjectProxy.getMaskLotService();	
					MaskLotService.addHistory(eventInfo, MaskLotService.getMaskLotHistoryEntity(), maskLotData, MaskLotService.getMaskLotLogger());
					existMaskLotList.add(maskLotName);
				}
						
			}
			else if (StringUtil.equalsIgnoreCase(flag, "Modify"))
			{
				List<Object> bindList = new ArrayList<Object>();
				try
				{
					maskRecipeData = ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(false, keySet);
				}
				catch (greenFrameDBErrorSignal nfds)
				{
					throw new CustomException("MASK-0048", maskLotName, toProcessFlowName, toProcessOperationName);
				}

				eventInfo.setEventName("ModifyReserveMaskRecipe");
				bindList.add(0, newToProcessFlowName);
				bindList.add(1, newToProcessFlowVersion);
				bindList.add(2, newToProcessOperationName);
				bindList.add(3, newToProcessOperationVersion);
				bindList.add(4, newToMachineName);
				bindList.add(5, toRecipeName);
				bindList.add(6, rmsFlag);
				bindList.add(7, eventInfo.getEventName());
				bindList.add(8, eventInfo.getEventTime());
				bindList.add(9, eventInfo.getEventUser());
				bindList.add(10, eventInfo.getEventTimeKey());
				bindList.add(11, eventInfo.getEventComment());
				bindList.add(12, maskLotName);
				bindList.add(13, maskSpecName);
				bindList.add(14, toProcessFlowName);
				bindList.add(15, toProcessFlowVersion);
				bindList.add(16, toProcessOperationName);
				bindList.add(17, toProcessOperationVersion);
				bindList.add(18, toMachineName);
				updateArgList.add(bindList.toArray());

				maskRecipeData.setProcessOperationName(newToProcessOperationName);
				maskRecipeData.setProcessOperationVersion(newToProcessOperationVersion);
				maskRecipeData.setMachineName(newToMachineName);
				maskRecipeData.setRecipeName(toRecipeName);
				maskRecipeData.setRMSFlag(rmsFlag);

				maskRecipeList.add(maskRecipeData);
				if(existMaskLotList.contains(maskLotName))
				{
					eventLog.info("The Same Mask Lot Exist Multiple ActionMaskRecipe at the Same Time. ");
				}
				else
				{
					MaskLotService MaskLotService = ExtendedObjectProxy.getMaskLotService();	
					MaskLotService.addHistory(eventInfo, MaskLotService.getMaskLotHistoryEntity(), maskLotData, MaskLotService.getMaskLotLogger());
					existMaskLotList.add(maskLotName);
				}
			}
			else if (StringUtil.equalsIgnoreCase(flag, "Delete"))
			{
				try
				{
					maskRecipeData = ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(false, keySet);
				}
				catch (greenFrameDBErrorSignal nfds)
				{
					throw new CustomException("MASK-0048", maskLotName, toProcessFlowName, toProcessOperationName);
				}

				maskRecipeList.add(maskRecipeData);
				eventInfo.setEventName("DeleteReserveMaskRecipe");
				if(existMaskLotList.contains(maskLotName))
				{
					eventLog.info("The Same Mask Lot Exist Multiple ActionMaskRecipe at the Same Time. ");
				}
				else
				{
					MaskLotService MaskLotService = ExtendedObjectProxy.getMaskLotService();	
					MaskLotService.addHistory(eventInfo, MaskLotService.getMaskLotHistoryEntity(), maskLotData, MaskLotService.getMaskLotLogger());
					existMaskLotList.add(maskLotName);
				}
			}
			else
			{
				throw new CustomException("MASK-0049", flag);
			}
		}

		// 2021-03-10	dhko	Add null check
		if (StringUtil.equalsIgnoreCase(flag, "Create") && maskRecipeList != null && maskRecipeList.size() > 0)
		{
			eventInfo.setEventName("CreateReserveMaskRecipe");
			ExtendedObjectProxy.getReserveMaskRecipeService().create(eventInfo, maskRecipeList);
		}
		else if (StringUtil.equalsIgnoreCase(flag, "Modify"))
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_RESERVEMASKRECIPE ");
			sql.append("   SET PROCESSFLOWNAME = ?, ");
			sql.append("       PROCESSFLOWVERSION = ?, ");
			sql.append("       PROCESSOPERATIONNAME = ?, ");
			sql.append("       PROCESSOPERATIONVERSION = ?, ");
			sql.append("       MACHINENAME = ?, ");
			sql.append("       RECIPENAME = ?, ");
			sql.append("       RMSFLAG = ?, ");
			sql.append("       LASTEVENTNAME = ?, ");
			sql.append("       LASTEVENTTIME = ?, ");
			sql.append("       LASTEVENTUSER = ?, ");
			sql.append("       LASTEVENTTIMEKEY = ?, ");
			sql.append("       LASTEVENTCOMMENT = ? ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND MASKLOTNAME = ? ");
			sql.append("   AND MASKSPECNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND PROCESSOPERATIONNAME = ? ");
			sql.append("   AND PROCESSOPERATIONVERSION = ? ");
			sql.append("   AND MACHINENAME = ? ");

			updateBatch(sql.toString(), updateArgList);
			ReserveMaskRecipeService service = ExtendedObjectProxy.getReserveMaskRecipeService();
			service.addHistory(eventInfo, service.getHistoryEntity(), maskRecipeList, ReserveMaskRecipeService.getLogger());
		}
		else if (StringUtil.equalsIgnoreCase(flag, "Delete"))
		{
			eventInfo.setEventName("DeleteReserveMaskRecipe");
			ExtendedObjectProxy.getReserveMaskRecipeService().remove(eventInfo, maskRecipeList);
		}

		return doc;
	}

	public void updateBatch(String queryString, List<Object[]> updateArgList) throws CustomException
	{
		// Update Batch
		if (updateArgList.size() > 0)
		{
			try
			{
				if (updateArgList.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryString, updateArgList.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryString, updateArgList);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}
}
