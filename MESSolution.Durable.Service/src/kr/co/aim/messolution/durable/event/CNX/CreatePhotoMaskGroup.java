package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;

import org.jdom.Document;
import org.jdom.Element;

public class CreatePhotoMaskGroup extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> groupList = SMessageUtil.getBodySequenceItemList(doc, "GROUPLIST", false);
		String type = SMessageUtil.getBodyItemValue(doc, "TYPE", false);
		
		if (type.isEmpty()) 
		{
			for (Element eleGroup : groupList) 
			{
				String factoryName = SMessageUtil.getChildText(eleGroup, "FACTORYNAME", true);
				String durableSpecName = SMessageUtil.getChildText(eleGroup, "DURABLESPECNAME", true);
				String durableType = SMessageUtil.getChildText(eleGroup, "DURABLETYPE", true);
				String description = SMessageUtil.getChildText(eleGroup, "DESCRIPTION", true);
				String checkState = SMessageUtil.getChildText(eleGroup, "CHECKSTATE", true);
				String activeState = SMessageUtil.getChildText(eleGroup, "ACTIVESTATE", true);
				String stockLocation = SMessageUtil.getChildText(eleGroup, "STOCKLOCATION", true);

				String condition = "FACTORYNAME=? AND DURABLESPECNAME=? AND DURABLESPECVERSION='00001'";
				Object bindSet[] = new Object[] { factoryName, durableSpecName };

				List<DurableSpec> durableSpecInfo = new ArrayList<DurableSpec>();
				try {
					durableSpecInfo = DurableServiceProxy.getDurableSpecService().select(condition, bindSet);
				} catch (Exception e) {
					eventLog.info(e.getCause());
				}

				if (!durableSpecInfo.isEmpty()) {
					throw new CustomException("PhotoMaskGroup-0001", durableSpecName);
				}

				kr.co.aim.greentrack.durableSpec.management.info.CreateInfo aCreateInfo = new kr.co.aim.greentrack.durableSpec.management.info.CreateInfo();

				aCreateInfo.setDurableSpecName(durableSpecName);
				aCreateInfo.setFactoryName(factoryName);
				aCreateInfo.setDescription(description);
				aCreateInfo.setDurableType(durableType);
				aCreateInfo.setTimeUsedLimit(999999);
				aCreateInfo.setDurationUsedLimit(999999);
				aCreateInfo.setDurableSpecVersion("00001");

				DurableServiceProxy.getDurableSpecService().create(aCreateInfo);

				DurableSpecKey durableSpecKey = new DurableSpecKey();
				durableSpecKey.setFactoryName(factoryName);
				durableSpecKey.setDurableSpecName(durableSpecName);
				durableSpecKey.setDurableSpecVersion("00001");

				DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
				durableSpec.setActiveState(activeState);
				durableSpec.setCheckState(checkState);
				durableSpec.setCreateUser(getEventUser());

				Map<String, String> udfs = durableSpec.getUdfs();
				udfs.put("STOCKLOCATION", stockLocation);
				udfs.put("CLEANUSEDLIMIT", "999999");

				durableSpec.setUdfs(udfs);

				DurableServiceProxy.getDurableSpecService().update(durableSpec);
			}
		}
		else
		{
			for (Element rule : groupList)
			{
				String factoryName = rule.getChildText("FACTORYNAME");
				String specName = rule.getChildText("DURABLESPECNAME");
				
				DurableSpecKey key = new DurableSpecKey();
				key.setFactoryName(factoryName);
				key.setDurableSpecName(specName);
				key.setDurableSpecVersion("00001");
				
				DurableServiceProxy.getDurableSpecService().delete(key);
			}
		}
		return doc;
	}
}