package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import org.jdom.Document;

public class ChangeOperationShieldLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String shieldLotName = SMessageUtil.getBodyItemValue(doc, "SHIELDLOTNAME", true);
		String newOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String nodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", true);
		String doAlsoOthers = SMessageUtil.getBodyItemValue(doc, "DO_ALSO_OTHERS", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationShield", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		ShieldLot shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldLotName });
		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		if(StringUtil.equals(doAlsoOthers, "NO") && StringUtil.isNotEmpty(shieldLot.getCarrierName()))
		{
			throw new CustomException("MASKNO-0001");
		}
		else if (StringUtil.equals(doAlsoOthers, "YES") && StringUtil.isNotEmpty(shieldLot.getCarrierName()))
		{
			String condition = " WHERE CARRIERNAME = ? ";
			Object[] bindSet = new Object[] { shieldLot.getCarrierName() };
			shieldLotList = ExtendedObjectProxy.getShieldLotService().select(condition, bindSet);
		}
		else
		{
			shieldLotList.add(shieldLot);
		}

		for (ShieldLot shieldLotData : shieldLotList)
		{
			ExtendedObjectProxy.getShieldLotService().changeOperationShield(eventInfo, shieldLotData, sFactoryName, newOperationName, nodeStack);
		}

		return doc;
	}
}
