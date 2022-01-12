package kr.co.aim.messolution.port.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.jdom.Document;

public class WorkOrderStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "WORKORDER", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("WorkOrderStarted", getEventUser(), getEventComment(), "", "");

		// Get Product Request Data
		ProductRequestKey prdKey = new ProductRequestKey();
		prdKey.setProductRequestName(productRequestName);

		ProductRequest prdReq = ProductRequestServiceProxy.getProductRequestService().selectByKey(prdKey);

		// Change Product Request State
		if (prdReq.getProductRequestState().toString().equals("Created"))
		{
			MakeReleasedInfo makeReleasedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeReleasedInfo(prdReq);
			eventInfo.setEventName("Release");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(prdReq, makeReleasedInfo, eventInfo);
		}

		ProductRequestPlan PlanData = this.getFirstWorkOrderPlan(productRequestName, true);

		ExtendedObjectProxy.getProductRequestPlanService().makeStartedProductRequestPlan(eventInfo, PlanData);
	}

	private ProductRequestPlan getFirstWorkOrderPlan(String productRequestName, boolean flag) throws CustomException
	{
		try
		{
			ProductRequestPlan pPlan = new ProductRequestPlan();

			if (flag)
			{
				List<ProductRequestPlan> pPlanList = ExtendedObjectProxy.getProductRequestPlanService().select(
						"productRequestName = ? and planState IN (?, ?) and position = (select min(position) from CT_PRODUCTREQUESTPLAN Where productRequestName = ? and planState IN (?, ?))",
						new Object[] { productRequestName, "Released", "Started", productRequestName, "Released", "Started" });

				if (pPlanList.size() > 1)
				{
					throw new CustomException("PRODUCTREQUEST-0022", productRequestName);
				}

				pPlan = pPlanList.get(0);
			}

			return pPlan;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PRODUCTREQUEST-0021", productRequestName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCTREQUEST-0021", productRequestName);
		}
	}
}