package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.R2RFeedbackDEPOHist;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class R2RFeedbackDEPTimeUpdateReport extends AsyncHandler 
{
    Log log = LogFactory.getLog(this.getClass());
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String mode = SMessageUtil.getBodyItemValue(doc, "MODE", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", false);
		List<Element> recipeParaElementList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARALIST", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("R2RFeedbackDEPTimeUpdateReport", this.getEventUser(), this.getEventComment());

		List<R2RFeedbackDEPOHist> dataList = new ArrayList<>();

		for (Element paraElement : recipeParaElementList)
		{
			String paraName = paraElement.getChildText("PARANAME");
			String paraValue = paraElement.getChildText("PARAVALUE");

			//RMS-050: Element [{0}] is not reported or the value is empty.
			if (CommonValidation.isNullOrEmpty(paraName))
				throw new CustomException("RMS-050","PARANAME");

			R2RFeedbackDEPOHist dataInfo = new R2RFeedbackDEPOHist();
			dataInfo.setTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setMachineRecipeName(machineRecipeName);
			dataInfo.setUnitName(unitName);
			dataInfo.setRecipeName(recipeName);
			dataInfo.setParaName(paraName);
			dataInfo.setParaValue(paraValue);
			dataInfo.setSubUnitName(subUnitName);
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setMode(mode);
			dataInfo.setResult(result);
			
			dataInfo.setEventName(eventInfo.getEventName());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventComment(eventInfo.getEventComment());
			
			dataList.add(dataInfo);
		}
		
		if (dataList != null && dataList.size() > 0)
		{
			ExtendedObjectProxy.getR2RFeedbackDEPOHistService().insert(dataList);
			log.info(String.format("▶Successfully update %s pieces of data.", dataList.size()));
		}
	}
}
