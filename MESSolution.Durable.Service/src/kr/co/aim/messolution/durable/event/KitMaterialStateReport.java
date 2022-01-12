package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableHistory;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class KitMaterialStateReport extends AsyncHandler {
	Log log = LogFactory.getLog(this.getClass());

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);

		List<Consumable> consumableDataList = new ArrayList<>();
		List<ConsumableHistory> consumableHistList = new ArrayList<>();

		List<Durable> durableDataList = new ArrayList<>();
		List<DurableHistory> durableHistList = new ArrayList<>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitMaterialStateReport", this.getEventUser(), this.getEventComment());

		for (Element materialElement : materialElementList)
		{
			String unitName = materialElement.getChildText("UNITNAME");
			String subUnitName = materialElement.getChildText("SUBUNITNAME");
			String materialName = materialElement.getChildText("MATERIALNAME");
			String materialType = materialElement.getChildText("MATERIALTYPE");
			String materialState = materialElement.getChildText("MATERIALSTATE");
			String quantity = materialElement.getChildText("QUANTITY");
			String timeUsed = materialElement.getChildText("TIMEUSED");
			String durationUsed = materialElement.getChildText("DURATIONUSED");
			String materialPosition = materialElement.getChildText("MATERIALPOSITION");

			int position = 0;

			try
			{
				int mPosition = Integer.parseInt(materialPosition);
				if (mPosition > position)
					position = mPosition;
			}
			catch (Exception e)
			{
				position = 0;
			}

			checkIsNumeric(quantity, timeUsed, durationUsed);

			String materialLocation = "";

			if (StringUtil.isNotEmpty(subUnitName))
			{
				materialLocation = subUnitName;
			}
			else if (StringUtil.isNotEmpty(unitName))
			{
				materialLocation = unitName;
			}
			else
			{
				materialLocation = machineName;
			}

			if (materialType.equals(constantMap.MaterialType_PatternFilm))
			{
				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialName);

				Consumable oldData = (Consumable) ObjectUtil.copyTo(consumableData);

				// Decrement or Increment consumable (cannot be used policy)
				consumableData.setMaterialLocationName(materialLocation);

				consumableData.getUdfs().put("MACHINENAME", machineName);
				consumableData.getUdfs().put("UNITNAME", unitName);

				consumableData.setQuantity(quantity.isEmpty() ? 0 : Double.parseDouble(quantity));
				consumableData.setConsumableState(constantMap.Cons_InUse);
				consumableData.setLastEventName(eventInfo.getEventName());
				consumableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				consumableData.setLastEventTime(eventInfo.getEventTime());
				consumableData.setLastEventUser(eventInfo.getEventUser());
				consumableData.setLastEventComment(eventInfo.getEventComment());

				ConsumableHistory dataHistory = ConsumableServiceProxy.getConsumableHistoryDataAdaptor().setHV(oldData, consumableData, new ConsumableHistory());

				consumableDataList.add(consumableData);
				consumableHistList.add(dataHistory);
			}
			else
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);

				Durable oldData = (Durable) ObjectUtil.copyTo(durableData);

				durableData.setMaterialLocationName(materialLocation);

				durableData.getUdfs().put("MACHINENAME", machineName);
				durableData.getUdfs().put("UNITNAME", unitName);

				if (position != 0)
					durableData.getUdfs().put("POSITION", Integer.toString(position));
				else
					durableData.getUdfs().put("POSITION", "");

				durableData.setDurableState(constantMap.Dur_InUse);
				durableData.setTimeUsed(timeUsed.isEmpty() ? 0 : Double.parseDouble(timeUsed));
				durableData.setDurationUsed(durationUsed.isEmpty() ? 0 : Double.parseDouble(durationUsed));
				durableData.setLastEventName(eventInfo.getEventName());
				durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableData.setLastEventTime(eventInfo.getEventTime());
				durableData.setLastEventUser(eventInfo.getEventUser());
				durableData.setLastEventComment(eventInfo.getEventComment());

				DurableHistory dataHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, durableData, new DurableHistory());

				durableDataList.add(durableData);
				durableHistList.add(dataHistory);
			}

		}

		try
		{

			if (consumableDataList.size() > 0)
			{
				CommonUtil.executeBatch("update", consumableDataList, true);
				CommonUtil.executeBatch("insert", consumableHistList, true);
				log.info(String.format("▶Successfully update %s pieces of Consumables.", consumableDataList.size()));
			}

			if (durableDataList.size() > 0)
			{
				CommonUtil.executeBatch("update", durableDataList, true);
				CommonUtil.executeBatch("insert", durableHistList, true);
				log.info(String.format("▶Successfully update %s pieces of Durables.", durableDataList.size()));
			}

		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

	}

	private boolean checkIsNumeric(String... args) throws CustomException
	{
		boolean resultFlag = true;

		if (args == null || args.length == 0)
		{
			log.info("The incoming argument value is Empty or Null!!.");
			return false;
		}

		int sequence = 0;
		for (String var : args)
		{
			sequence++;
			if (var == null || var.isEmpty())
			{
				log.info("No." + sequence + " argument value is Null!!.");
				return false;
			}

			if (var.contains("+") || var.contains("-"))
			{
				if ((var.contains("+") && var.lastIndexOf("+") != 0) || (var.contains("-") && var.lastIndexOf("-") != 0))
				{
					log.info("No." + sequence + " argument contains more than one positive and negative sign or in the wrong position.");
					resultFlag = false;
				}

				var = var.substring(1);
			}

			if (var.contains("."))
			{
				if (var.indexOf(".") == 0)
				{
					log.info("No." + sequence + " The decimal point is in the wrong position.");
					resultFlag = false;
				}

				String[] decimalSplit = var.split("\\.");

				if (decimalSplit.length > 2)
				{
					log.info("No." + sequence + " argument contains more than one decimal point.");
					resultFlag = false;
				}

				for (String numric : decimalSplit)
				{
					if (!StringUtil.isNumeric(numric))
					{
						log.info("No." + sequence + " argument value is not a number.");
						resultFlag = false;
					}
				}
			}
		}

		return resultFlag;
	}
}
