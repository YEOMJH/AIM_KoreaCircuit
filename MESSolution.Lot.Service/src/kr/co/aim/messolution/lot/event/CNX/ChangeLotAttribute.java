package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeLotAttribute extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sDueDate = SMessageUtil.getBodyItemValue(doc, "DUEDATE", false);
		String sPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String sSubProductSpec = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTSPECNAME", false);
		String sProductionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", false);

		long lPriority = 0;
		String lProductionType = null;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

		CommonValidation.checkJobDownFlag(lotData);

		//DueDate
		Timestamp dueDate = null;
		if (StringUtils.isEmpty(sDueDate))
			dueDate = lotData.getDueDate();
		else
			dueDate = TimeUtils.getTimestamp(sDueDate);

		//Priority
		if (StringUtils.isEmpty((sPriority)))
			lPriority = lotData.getPriority();
		else
			lPriority = Long.parseLong(sPriority);

		//ProductionType
		if (StringUtils.isEmpty((sProductionType)))
			lProductionType = lotData.getProductionType();
		else
			lProductionType = sProductionType;

		//SubProductSpec
		if (StringUtils.isEmpty((sSubProductSpec)))
			sSubProductSpec = lotData.getProductSpec2Name();

		if (StringUtil.equals(lotData.getLotState(), "Created"))
		{
			EventInfo eventInfo = new EventInfo();
			if (StringUtils.isEmpty((sPriority)))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangeDueDate", getEventUser(), getEventComment(), "", "");
				lotData.setDueDate(dueDate);
			}
			
			if (StringUtils.isEmpty(sDueDate))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangePriority", getEventUser(), getEventComment(), "", "");
				lotData.setPriority(lPriority);
			}
			
			if (StringUtils.isEmpty(sProductionType))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangeProductionType", getEventUser(), getEventComment(), "", "");
				lotData.setProductionType(lProductionType);
			}

			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(sLotName);

			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
					lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), "", lotData.getPriority(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), lotData.getProductRequestName(), "", "", lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			return doc;
		}

		String processOperationName = lotData.getProcessOperationName();
		String nodeStack = lotData.getNodeStack();

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(sLotName);

		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productRequestName = lotData.getProductRequestName();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, areaName, dueDate, factoryName, lotHoldState, lotProcessState, lotState, nodeStack, lPriority,
				processFlowName, processFlowVersion, processOperationName, processOperationVersion, lProductionType, productRequestName, productSpec2Name, productSpec2Version, productSpecName,
				productSpecVersion, productUdfs, subProductUnitQuantity1, subProductUnitQuantity2);

		if (StringUtils.isNotEmpty((sPriority)))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePriority", getEventUser(), getEventComment(), "", "");
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}
		
		if (StringUtils.isNotEmpty(sDueDate))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDueDate", getEventUser(), getEventComment(), "", "");
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}
		
		if (StringUtils.isNotEmpty(sProductionType))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductionType", getEventUser(), getEventComment(), "", "");
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}

		if (StringUtils.isNotEmpty(sSubProductSpec))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskProductSpecName", getEventUser(), getEventComment(), "", "");

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MASKPRODUCTSPECNAME", sSubProductSpec);

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

			for (ProductU pt : productUdfs)
			{
				String productName = pt.getProductName();
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();

				setProductEventInfo.getUdfs().put("MASKPRODUCTSPECNAME", sSubProductSpec);
				ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setProductEventInfo);
			}
		}

		return doc;
	}

}
