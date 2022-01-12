package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;

public class CreateShieldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		//String shieldSet = SMessageUtil.getBodyItemValue(doc, "SHIELDSET", false);
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShieldCarrier", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		List<Element> eleDurableList = new ArrayList<Element>();

		for (Element durable : durableList)
		{
			String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);
			String durableSpecName = SMessageUtil.getChildText(durable, "DURABLESPECNAME", true);
			String durableType = SMessageUtil.getChildText(durable, "DURABLETYPE", true);
			String chamberType = SMessageUtil.getChildText(durable, "CHAMBERTYPE", true);

			DurableSpec specData = GenericServiceProxy.getSpecUtil().getDurableSpec(factoryName, durableSpecName, "00001");

			String ruleName = "";
			String newDurableName = "";
			
			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("CHAMBERTYPE", chamberType);

			if (StringUtil.equals(durableType, "Car"))
			{
				//ruleName = "ShieldCarNaming";
				//nameRuleAttrMap.put("SHIELDSET", shieldSet);
				newDurableName = durableName;
			}
			else if (StringUtil.equals(durableType, "Basket"))
			{
				ruleName = "ShieldBasketNaming";
				List<String> durableNameList = CommonUtil.generateNameByNamingRule(ruleName, nameRuleAttrMap, 1);
				newDurableName = durableNameList.get(0);
			}

			//List<String> durableNameList = CommonUtil.generateNameByNamingRule(ruleName, nameRuleAttrMap, 1);
			//String newDurableName = durableNameList.get(0);
			
			CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(newDurableName, durableSpecName, "", Long.toString(specData.getDefaultCapacity()), factoryName);
			Map<String, String> udfs = createInfo.getUdfs();
			udfs.put("CHAMBERTYPE", chamberType);
			
			Durable newDurable = MESDurableServiceProxy.getDurableServiceImpl().create(newDurableName, createInfo, eventInfo);

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
			XmlUtil.addElement(eleDurable, "DURABLENAME", durableData.getKey().getDurableName());
			XmlUtil.addElement(eleDurable, "DURABLESPECNAME", durableData.getDurableSpecName());
			XmlUtil.addElement(eleDurable, "DURABLETYPE", durableData.getDurableType());
			XmlUtil.addElement(eleDurable, "CHAMBERTYPE", durableData.getUdfs().get("CHAMBERTYPE"));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", durableData.getKey().getDurableName()));
		}

		return eleDurable;
	}

}
