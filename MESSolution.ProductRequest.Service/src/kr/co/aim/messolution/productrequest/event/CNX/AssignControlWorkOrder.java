package kr.co.aim.messolution.productrequest.event.CNX;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ControlProductRequestAssign;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class AssignControlWorkOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> workOrderList = SMessageUtil.getBodySequenceItemList(doc, "WORKORDERLIST", true);

		for (Element workOrder : workOrderList)
		{
			String factoryName = SMessageUtil.getChildText(workOrder, "FACTORYNAME", true);
			String productRequestName = SMessageUtil.getChildText(workOrder, "PRODUCTREQUESTNAME", true);
			String toFactoryName = SMessageUtil.getChildText(workOrder, "TOFACTORYNAME", true);
			String toProductRequestName = SMessageUtil.getChildText(workOrder, "TOPRODUCTREQUESTNAME", true);

			ProductRequest shipProductRequestInfo = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
			ProductRequest receiveProductRequestInfo = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(toProductRequestName);

			//CommonValidation.checkProductRequestState(shipProductRequestInfo);
			CommonValidation.checkProductRequestState(receiveProductRequestInfo);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignWorkOrder", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());

			ControlProductRequestAssign assignData = ExtendedObjectProxy.getControlProductRequestAssignService().getControlProductRequestAssignData(productRequestName, toProductRequestName);

			if (assignData == null)
			{
				ControlProductRequestAssign dataInfo = new ControlProductRequestAssign();
				dataInfo.setFactoryName(factoryName);
				dataInfo.setProductRequestName(productRequestName);
				dataInfo.setProductSpecName(shipProductRequestInfo.getProductSpecName());
				dataInfo.setProductSpecVersion(shipProductRequestInfo.getProductSpecVersion());
				dataInfo.setProcessFlowName(CommonUtil.getValue(shipProductRequestInfo.getUdfs(), "PROCESSFLOWNAME"));
				dataInfo.setProcessFlowVersion(CommonUtil.getValue(shipProductRequestInfo.getUdfs(), "PROCESSFLOWVERSION"));
				dataInfo.setToFactoryName(toFactoryName);
				dataInfo.setToProductRequestName(toProductRequestName);
				dataInfo.setToProductSpecName(receiveProductRequestInfo.getProductSpecName());
				dataInfo.setToProductSpecVersion(receiveProductRequestInfo.getProductSpecVersion());
				dataInfo.setToProcessFlowName(CommonUtil.getValue(receiveProductRequestInfo.getUdfs(), "PROCESSFLOWNAME"));
				dataInfo.setToProcessFlowVersion(CommonUtil.getValue(receiveProductRequestInfo.getUdfs(), "PROCESSFLOWVERSION"));
				dataInfo.setDescription(CommonUtil.getValue(shipProductRequestInfo.getUdfs(), "DESCRIPTION"));
				dataInfo.setToDescription(CommonUtil.getValue(receiveProductRequestInfo.getUdfs(), "DESCRIPTION"));
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

				ExtendedObjectProxy.getControlProductRequestAssignService().create(eventInfo, dataInfo);
			}
		}

		return doc;
	}

}
