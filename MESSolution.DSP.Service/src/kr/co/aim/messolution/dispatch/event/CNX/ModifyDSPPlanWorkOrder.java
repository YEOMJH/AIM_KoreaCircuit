package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ModifyDSPPlanWorkOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String action = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		List<Element> planEList = SMessageUtil.getBodySequenceItemList(doc, "PLANLIST", true);

		if (action.equalsIgnoreCase("DELETEPLAN"))
		{
			eventInfo.setEventName("Delete");

			for (Element planE : planEList)
			{
				String planName = SMessageUtil.getChildText(planE, "PLANNAME", true);

				String condition = " planName = ? ";
				Object[] bindSet = new Object[] { planName };

				List<DSPProductRequestPlan> planWOList = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

				for (DSPProductRequestPlan planWO : planWOList)
				{
					ExtendedObjectProxy.getDSPProductRequestPlanService().remove(eventInfo, planWO);

					condition = " productSpecName = ? and processOperationName = ? and factoryName = ? and productRequestName = ? and planDate = ? ";

					bindSet = new Object[] { planWO.getProductSpecName(), planWO.getProcessOperationName(), planWO.getFactoryName(), planWO.getProductRequestName(), planWO.getPlanDate() };

					try
					{
						List<ReserveLot> lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
	
						for (ReserveLot lot : lotList)
						{
							ExtendedObjectProxy.getReserveLotService().remove(eventInfo, lot);
						}
					}
					catch(Exception e)
					{
						eventLog.info(e.getCause());
					}
				}
			}
		}
		else if (action.equalsIgnoreCase("DELETEWOPLAN"))
		{
			eventInfo.setEventName("Delete");

			for (Element planE : planEList)
			{
				String planName = SMessageUtil.getChildText(planE, "PLANNAME", true);
				String productRequestName = SMessageUtil.getChildText(planE, "PRODUCTREQUESTNAME", true);
				String productSpecName = SMessageUtil.getChildText(planE, "PRODUCTSPECNAME", true);
				String ProcessFlowName = SMessageUtil.getChildText(planE, "PROCESSFLOWNAME", true);

				String condition = " productSpecName = ? and processFlowName = ? and planName = ? and productRequestName = ? ";
				Object[] bindSet = new Object[] { productSpecName, ProcessFlowName, planName, productRequestName };

				List<DSPProductRequestPlan> planWOList = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

				for (DSPProductRequestPlan planWO : planWOList)
				{
					ExtendedObjectProxy.getDSPProductRequestPlanService().remove(eventInfo, planWO);

					condition = " productSpecName = ? and processOperationName = ? and factoryName = ? and productRequestName = ? and planDate = ? ";

					bindSet = new Object[] { planWO.getProductSpecName(), planWO.getProcessOperationName(), planWO.getFactoryName(), planWO.getProductRequestName(), planWO.getPlanDate() };

					try
					{
						List<ReserveLot> lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
	
						for (ReserveLot lot : lotList)
						{
							ExtendedObjectProxy.getReserveLotService().remove(eventInfo, lot);
						}
					}
					catch(Exception e)
					{
						eventLog.info(e.getCause());
					}
				}
			}
		}
		else if (action.equalsIgnoreCase("MODIFYPLAN"))
		{
			eventInfo.setEventName("Modify");

			for (Element planE : planEList)
			{

				String planName = SMessageUtil.getChildText(planE, "PLANNAME", true);
				String priority = SMessageUtil.getChildText(planE, "PRIORITY", true);

				String condition = " planName = ? ";
				Object[] bindSet = new Object[] { planName };

				List<DSPProductRequestPlan> planWOList = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

				for (DSPProductRequestPlan planWO : planWOList)
				{
					planWO.setPriority(Long.parseLong(priority));

					ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, planWO);
				}
			}
		}
		else if (action.equalsIgnoreCase("MODIFYWOPLAN"))
		{
			eventInfo.setEventName("Modify");

			for (Element planE : planEList)
			{
				String planName = SMessageUtil.getChildText(planE, "PLANNAME", true);
				String productRequestName = SMessageUtil.getChildText(planE, "PRODUCTREQUESTNAME", true);
				String productSpecName = SMessageUtil.getChildText(planE, "PRODUCTSPECNAME", true);
				String ProcessFlowName = SMessageUtil.getChildText(planE, "PROCESSFLOWNAME", true);
				String position = SMessageUtil.getChildText(planE, "POSITION", true);

				String condition = " productSpecName = ? and processFlowName = ? and planName = ? and productRequestName = ? ";
				Object[] bindSet = new Object[] { productSpecName, ProcessFlowName, planName, productRequestName };

				List<DSPProductRequestPlan> planWOList = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

				for (DSPProductRequestPlan planWO : planWOList)
				{
					planWO.setPosition(Long.parseLong(position));

					ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, planWO);

				}
			}
		}
		else if (action.equalsIgnoreCase("MODIFYPOPLAN"))
		{
			eventInfo.setEventName("Modify");
			Map<String, String> planData = new HashMap<String, String>();

			for (Element planE : planEList)
			{
				String planDate = SMessageUtil.getChildText(planE, "PLANDATE", true);
				String planName = SMessageUtil.getChildText(planE, "PLANNAME", true);
				String productRequestName = SMessageUtil.getChildText(planE, "PRODUCTREQUESTNAME", true);
				String productSpecName = SMessageUtil.getChildText(planE, "PRODUCTSPECNAME", true);
				String ProcessFlowName = SMessageUtil.getChildText(planE, "PROCESSFLOWNAME", true);
				String ProcessOperationName = SMessageUtil.getChildText(planE, "PROCESSOPERATIONNAME", true);
				String machineName = SMessageUtil.getChildText(planE, "MACHINENAME", true);
				String newMachineName = SMessageUtil.getChildText(planE, "NEWMACHINENAME", true);
				String factoryName = SMessageUtil.getChildText(planE, "FACTORYNAME", true);

				StringBuilder sKey = new StringBuilder();
				sKey.append(planName).append("_").append(productRequestName).append("_").append(factoryName).append("_");
				sKey.append(ProcessFlowName).append("_").append(ProcessOperationName).append("_").append(machineName);

				if (StringUtils.isEmpty(planData.get(sKey.toString())))
				{
					// DSPProductRequestPlan
					String condition = " planName = ? and productRequestName = ? and factoryName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and machineName = ?  ";
					Object[] bindSet = new Object[] { planName, productRequestName, factoryName, productSpecName, ProcessFlowName, ProcessOperationName, machineName };

					List<DSPProductRequestPlan> planWOList = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

					for (DSPProductRequestPlan planWO : planWOList)
					{
						planWO.setMachineName(newMachineName);

						ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, planWO);
					}

					// ReserveLot
					condition = " productSpecName = ? and processOperationName = ? and factoryName = ? and productRequestName = ? and planDate = ? and machineName = ? ";
					bindSet = new Object[] { productSpecName, ProcessOperationName, factoryName, productRequestName, planDate, machineName };

					List<ReserveLot> lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

					for (ReserveLot lot : lotList)
					{
						ReserveLot newlot = (ReserveLot) ObjectUtil.copyTo(lot);
						newlot.setMachineName(newMachineName);

						ExtendedObjectProxy.getReserveLotService().updateToNew(lot, newlot);
					}

					planData.put(sKey.toString(), "KEYVALUE");
				}
			}
		}

		return doc;
	}
}
