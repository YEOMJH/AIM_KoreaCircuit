package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class IssueLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Log log = LogFactory.getLog(IssueLot.class);

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String lotIssue = GenericServiceProxy.getConstantMap().Lot_Issue;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("IssueLot", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setReasonCode(lotData.getReasonCode());
		eventInfo.setReasonCodeType(lotData.getReasonCodeType());

		log.info(" Start Issue Lot " + "LOTNAME: " + lotName);
		log.info("Event User: " + eventInfo.getEventUser() + " Event Time: " + TimeStampUtil.toTimeString(eventInfo.getEventTime()));
		
		
		CommonValidation.checkJobDownFlag(lotData);
		
		Map<String, String> udfs = lotData.getUdfs();
		if (udfs.get("LOTISSUESTATE").equals(GenericServiceProxy.getConstantMap().Lot_IssueReleased) || udfs.get("LOTISSUESTATE").isEmpty())
		{
			// Check Lot State
			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
			{
				// Set Lot Issue State
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("LOTISSUESTATE", lotIssue);
				
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				
				// Set Product Issue State
				kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);

				for (Product product : productList)
				{
					Map<String, String> udfspro = new HashMap<String, String>();
					udfspro.put("ISSUESTATE", lotIssue);
					udfspro.put("ISSUETIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					udfspro.put("ISSUEUSER", eventInfo.getEventUser());

					setProductEventInfo.setUdfs(udfspro);
					ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setProductEventInfo);
				}
			}
			else
			{
				// If the LotProcessState value is RUN, you cannot specify IssueLot. LotName=[{0}]
				throw new CustomException("LOT-3004", lotData.getKey().getLotName());
			}
		}
		else
		{
			// This Lot is already IssueLot. LotName=[{0}], LotIssueState=[{1}]
			throw new CustomException("LOT-3005", lotData.getKey().getLotName(), lotData.getUdfs().get("LOTISSUESTATE"));
		}

		return doc;
	}

}
