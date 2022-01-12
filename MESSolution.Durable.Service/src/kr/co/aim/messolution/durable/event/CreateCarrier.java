package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CreateCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableSpec = SMessageUtil.getBodyItemValue(doc, "DURABLESPECNAME", true);
		String sCapacity = SMessageUtil.getBodyItemValue(doc, "CAPACITY", true);

		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		List<Element> eleDurableList = new ArrayList<Element>();

		for (Element Durable : durableList)
		{
			String sDurableName = SMessageUtil.getChildText(Durable, "DURABLENAME", true);

			Durable durableData = new Durable();

			try
			{
				durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
			}
			catch (Exception e)
			{
				durableData = null;
			}

			if (durableData != null)
				throw new CustomException("MASK-0001", sDurableName);

			CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(sDurableName, sDurableSpec, "", sCapacity, sFactoryName);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
			if(createInfo.getDurableType().equals("Tray"))
			{
				Map<String, String> udfs = createInfo.getUdfs();
				if (udfs.isEmpty())
				{
					udfs = new HashMap<String, String>();
				}
				String lastCleanTime = TimeStampUtil.getCurrentTimestamp().toString();
				udfs.put("LASTCLEANTIME",lastCleanTime);
				createInfo.setUdfs(udfs);
			}
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(sDurableName, createInfo, eventInfo);

			eleDurableList.add(setCreatedDurableList(newDurable));
		}

		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "DURABLELIST", eleDurableList);

		return doc;
	}

	private Element setCreatedDurableList(Durable durableData)
	{
		Element eleDurable = new Element("DURABLE");

		try
		{
			XmlUtil.addElement(eleDurable, "FACTORYNAME", durableData.getFactoryName());
			XmlUtil.addElement(eleDurable, "DURABLENAME", durableData.getKey().getDurableName());
			XmlUtil.addElement(eleDurable, "DURABLESPECNAME", durableData.getDurableSpecName());
			XmlUtil.addElement(eleDurable, "CAPACITY", String.valueOf((long) durableData.getCapacity()));
			XmlUtil.addElement(eleDurable, "TIMEUSEDLIMIT", String.valueOf((double) durableData.getTimeUsedLimit()));
			XmlUtil.addElement(eleDurable, "DURABLETYPE", durableData.getDurableType());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", durableData.getKey().getDurableName()));
		}

		return eleDurable;
	}

}
