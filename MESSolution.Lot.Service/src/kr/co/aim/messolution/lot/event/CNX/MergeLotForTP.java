package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class MergeLotForTP extends SyncHandler {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(MergeLotForTP.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotNameSourceA = SMessageUtil.getBodyItemValue(doc, "SOURCELOTNAME", true);
		String durableNameDestination = SMessageUtil.getBodyItemValue(doc, "DESTDURABLENAME", true);
		Boolean trackoutflag = Boolean.parseBoolean(SMessageUtil.getBodyItemValue(doc, "TRACKOUTFLAG", true));

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		Lot lotDataSourceA = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameSourceA);

		//2021-03-24 wangys  Modify CST Merge For OLED CN->TN 
		//CommonValidation.checkDetailOperationType(lotDataSourceA, GenericServiceProxy.getConstantMap().SORT_OLEDtoTP);
		ProcessOperationSpec operData = CommonUtil.getProcessOperationSpec(lotDataSourceA.getFactoryName(), lotDataSourceA.getProcessOperationName(), lotDataSourceA.getProcessOperationVersion());
		if (!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_TPtoOLED)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().TSPSHIP)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().MaterialLocation_Bank)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_OLEDtoTP))
		{
			throw new CustomException("SORT-0010", operData.getDetailProcessOperationType());
		}    
		CommonValidation.checkOriginalProduct(lotDataSourceA);
		CommonValidation.checkSortJobState(lotDataSourceA);
		
		// 2020-11-14	dhko	Add Validation
		CommonValidation.checkProcessInfobyString(lotNameSourceA);
		
		List<String> srcLotList = new ArrayList<String>();
		List<String> destLotList = new ArrayList<String>();

		List<Element> eleDestLotList = new ArrayList<Element>();

		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, lotDataSourceA.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		String namingRule = "";

		if (StringUtils.equals(baseData.getProductionType(), "P"))
			namingRule = "ProductionLotNaming";
		else
			namingRule = "LotNaming";

		if (StringUtils.isEmpty(baseData.getUdfs().get("PRODUCTCODE")))
			throw new CustomException("NAMING-0002", baseData.getKey().getProductSpecName());
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTCODE", baseData.getUdfs().get("PRODUCTCODE"));
		nameRuleAttrMap.put("PRODUCTIONTYPE", baseData.getProductionType());
		nameRuleAttrMap.put("PRODUCTSPECTYPE", baseData.getUdfs().get("PRODUCTSPECTYPE"));
		
		// Create DestLot
		String newlotName = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, 1).get(0);

		eventInfo.setEventName("Create");
		Lot destLot = createWithParentLotAndProductProductionType(eventInfo, newlotName, lotDataSourceA, lotDataSourceA.getProductionType(), "", false, new HashMap<String, String>(), lotDataSourceA.getUdfs());

		destLotList.add(destLot.getKey().getLotName());
		
		Map<String, String> lotUdfs = destLot.getUdfs();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(destLot.getMachineName(), lotUdfs.get("PORTNAME"));

		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleSourceDurableList = SMessageUtil.getSubSequenceItemList(eleBody, "SOURCE_DURABLELIST", true);

		List<Element> eleDestiProductList = null;
		try
		{
			eleDestiProductList = SMessageUtil.getSubSequenceItemList(eleBody, "DESTINATIONPRODUCTLIST", true);

		}
		catch (Exception e)
		{
		}

		List<ProductP> productPSequenceEmpty = new ArrayList<ProductP>();

		AssignCarrierInfo AssignCreateInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(destLot, durableNameDestination, productPSequenceEmpty);
		
		//Validation
		CommonValidation.checkEmptyCst(durableNameDestination);
		CommonValidation.checkMultiLot(durableNameDestination);
		
		eventInfo.setEventName("AssignCarrier");
		MESLotServiceProxy.getLotServiceImpl().assignCarrier(destLot, AssignCreateInfo, eventInfo);

		String productOffset = "";
		
		for (Element eleSourceDurable : eleSourceDurableList)
		{
			String durableName = SMessageUtil.getChildText(eleSourceDurable, "DURABLENAME", true);

			Durable sourceDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			CommonValidation.CheckDurableHoldState(sourceDurableData);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleSourceDurable, "PRODUCTLIST", true);

			List<Element> eleSrcProductList = null;
			try
			{
				eleSrcProductList = SMessageUtil.getSubSequenceItemList(eleBody, durableName, false);
			}
			catch (Exception e)
			{
			}

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			
			for (Element eleProduct : eleProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
				String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", true);
				int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

				if(StringUtil.isEmpty(productOffset))
				{
					if(productData.getUdfs().get("OFFSET") != null)
					{
						productOffset = productData.getUdfs().get("OFFSET").toString();
					}
				}
				else
				{
					if(!productOffset.equals(productData.getUdfs().get("OFFSET")))
					{
						throw new CustomException("OFFSET-0001", productData.getKey().getProductName());
					}
				}
				
				Map<String, String> productUdfs = new HashMap<String, String>();
				productUdfs.put("SLOTPOSITION", slotPosition);

				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(position);
				productP.setUdfs(productUdfs);
				productPSequence.add(productP);
			}

			Lot sourceLotData = null;
			try
			{
				sourceLotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(durableName);
			}
			catch (CustomException ce)
			{
			}

			if (sourceLotData != null)
			{
				//2021-03-24 wangys  Modify CST Merge For OLED CN->TN 
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(sourceLotData.getFactoryName(), sourceLotData.getProcessOperationName(), sourceLotData.getProcessOperationVersion());
				if (!StringUtils.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_TPtoOLED)&&
						!StringUtils.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().TSPSHIP)&&
						!StringUtils.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().MaterialLocation_Bank)&&
						!StringUtils.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT)&&
						!StringUtils.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_OLEDtoTP))
				{
					throw new CustomException("SORT-0010", operData.getDetailProcessOperationType());
				}  
				//CommonValidation.checkDetailOperationType(sourceLotData, GenericServiceProxy.getConstantMap().SORT_OLEDtoTP);
				CommonValidation.checkOriginalProduct(sourceLotData);
				CommonValidation.checkSortJobState(sourceLotData);
			}

			if (eleSrcProductList != null)
			{
				String sourLotGrade = CommonUtil.judgeLotGradeByProductList(eleSrcProductList, "PRODUCTJUDGE");

				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, sourceLotData.getKey().getLotName());
			}

			if (eleDestiProductList != null)
			{
				String desLotGrade = CommonUtil.judgeLotGradeByProductList(eleDestiProductList, "PRODUCTJUDGE");

				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", desLotGrade, newlotName);
			}

			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(destLot.getKey().getLotName(), eleProductList.size(), productPSequence,
					destLot.getUdfs(), new HashMap<String, String>());
			eventInfo.setEventName("MergeForReceive");
			sourceLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, sourceLotData, transitionInfo);

			srcLotList.add(sourceLotData.getKey().getLotName());
			
			if (sourceLotData.getProductQuantity() == 0)
			{
				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				
				if (StringUtil.isNotEmpty(sourceLotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceLotData.getCarrierName());
					deassignCarrierUdfs = sLotDurableData.getUdfs();
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(sourceLotData, sLotDurableData, new ArrayList<ProductU>());
					// Deassign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(sourceLotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, sourceLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
				deassignCarrierUdfs.clear();
			}
		}
		// Mantis 0000355
		MESLotServiceProxy.getLotServiceUtil().deleteSortJob(eventInfo, srcLotList, destLotList, "Merge (OLED to TP)");

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);
		
		if (trackoutflag)
		{
			destLot = MESLotServiceProxy.getLotInfoUtil().getLotData(destLot.getKey().getLotName());

			List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(eleDestLotList);
			Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableNameDestination);

			// Sampling
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(destLot);
			
			if (!StringUtil.equals(processFlowData.getProcessFlowType(), "MQC"))
				MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(eventInfo, destLot, new ArrayList<Element>(), true);
			
			MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, destLot);

			// Track Out GarbageLot
			MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, destLot, portData, durableNameDestination, destLot.getLotGrade(), destLot.getMachineName(), "",
					productPGSRCSequence, assignCarrierUdfs, desDurableData.getUdfs(), new HashMap<String, String>());
		}

		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTNAME", destLot.getKey().getLotName());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTHOLDSTATE", destLot.getLotHoldState());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTSTATE", destLot.getLotState());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTGRADE", destLot.getLotGrade());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTQTY", String.valueOf((int) destLot.getProductQuantity()));
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTSPEC", destLot.getProductSpecName());

		return doc;
	}

	private Lot createWithParentLotAndProductProductionType(EventInfo eventInfo, String newLotName, Lot parentlotData, String productProductionType, String newCarrierName, boolean deassignFlag,
			Map<String, String> assignCarrierUdfs, Map<String, String> udfs) throws CustomException
	{
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(parentlotData.getAreaName(), deassignFlag ? "N" : "Y", assignCarrierUdfs,
				deassignFlag ? "" : newCarrierName, parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
				parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(), parentlotData.getLotHoldState(), newLotName,
				parentlotData.getLotProcessState(), parentlotData.getLotState(), parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
				parentlotData.getOriginalLotName(), parentlotData.getPriority(), parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
				parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), productProductionType, new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
				parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(), parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
				parentlotData.getReworkCount(), "", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(), parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
				parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(), udfs, parentlotData);
		
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", parentlotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", parentlotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", parentlotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("ARRAYLOTNAME", parentlotData.getUdfs().get("ARRAYLOTNAME"));

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}
}