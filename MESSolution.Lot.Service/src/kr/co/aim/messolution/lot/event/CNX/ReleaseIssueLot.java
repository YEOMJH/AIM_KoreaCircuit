package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalSheetDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class ReleaseIssueLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Log log = LogFactory.getLog(ReleaseIssueLot.class);

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String issueType = SMessageUtil.getBodyItemValue(doc, "ISSUETYPE", false);
		String lotIssue = GenericServiceProxy.getConstantMap().Lot_IssueReleased;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseIssueLot", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setReasonCode(lotData.getReasonCode());
		eventInfo.setReasonCodeType(lotData.getReasonCodeType());
		
		log.info(" Start ReleaseIssue Lot " + "LOTNAME: " + lotName);
		log.info("Event User: " + eventInfo.getEventUser() + " Event Time: " + TimeStampUtil.toTimeString(eventInfo.getEventTime()));
		
		Map<String, String> udfs = lotData.getUdfs();
		if (udfs.get("LOTISSUESTATE").equals(GenericServiceProxy.getConstantMap().Lot_Issue))
		{
			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("LOTISSUESTATE", lotIssue);

				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

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
				// If the LotProcessState value is RUN, you cannot released IssueLot. LotName=[{0}]
				throw new CustomException("LOT-3006", lotData.getKey().getLotName());
			}
		}
		else
		{
			// This Lot is not IssueLot. LotName=[{0}], LotIssueState=[{1}]
			throw new CustomException("LOT-3007", lotData.getKey().getLotName(), lotData.getUdfs().get("LOTISSUESTATE"));
		}
		
		if(StringUtils.equals(issueType, "RSIssueLot"))
		{
			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
			for(int i=0;i<productList.size();i++)
			{
				try
				{
					List<AbnormalSheetDetail> abnormalSheetList=ExtendedObjectProxy.getAbnormalSheetDetailService().select(" PRODUCTNAME=? AND ISSUEFLAG='true' ", new Object[]{productList.get(i).getKey().getProductName()});
					for(AbnormalSheetDetail abnormalSheetInfo:abnormalSheetList)
					{
						abnormalSheetInfo.setIssueFlag("");
						ExtendedObjectProxy.getAbnormalSheetDetailService().update(abnormalSheetInfo);
					}
				}
				catch(greenFrameDBErrorSignal n)
				{
					log.info(productList.get(i).getKey().getProductName()+" not exist IssueFlag");
				}
				
			}
		}

		return doc;
	}

}
