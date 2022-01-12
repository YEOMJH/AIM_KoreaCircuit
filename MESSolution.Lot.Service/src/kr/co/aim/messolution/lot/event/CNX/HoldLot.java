package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class HoldLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false))
			{
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
				String reasonCodeType = SMessageUtil.getChildText(eleLot, "REASONCODETYPE", true);
				String requestDepartment = SMessageUtil.getChildText(eleLot, "REQUESTDEPARTMENT", false);
				String owner = SMessageUtil.getChildText(eleLot, "OWNER", false);
				eventInfo.setReasonCode(reasonCode);
				eventInfo.setReasonCodeType(reasonCodeType + "[" + requestDepartment + "," + owner + "]");

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

				CommonValidation.checkJobDownFlag(lotData);

				if (!StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
					throw new CustomException("LOT-0047", lotName);

				Map<String, String> udfs = new HashMap<String, String>();

				if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
				{
					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
				}
				else
				{
					throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
				}
			}
		}

		return doc;
	}

}
