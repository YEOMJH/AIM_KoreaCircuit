package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SplitLotForTP extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		int destinationCount = Integer.parseInt(SMessageUtil.getBodyItemValue(doc, "DESTINATIONCOUNT", true));
		boolean trackOutFlag = Boolean.parseBoolean(SMessageUtil.getBodyItemValue(doc, "TRACKOUTFLAG", true));

		List<String> srcLotNameList = new ArrayList<String>();
		List<String> destLotNameList = new ArrayList<String>();

		List<Element> eleDestLotList = new ArrayList<Element>();

		srcLotNameList.add(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		//2021-03-24 wangys  Modify CST Split For OLED TN->CN
		//CommonValidation.checkDetailOperationType(lotData, GenericServiceProxy.getConstantMap().SORT_TPtoOLED);
		ProcessOperationSpec operData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		if (!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_TPtoOLED)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().TSPSHIP)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().MaterialLocation_Bank)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT)&&
				!StringUtils.equals(operData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().SORT_OLEDtoTP))
		{
			throw new CustomException("SORT-0010", operData.getDetailProcessOperationType());
		}    
		
		CommonValidation.checkOriginalProduct(lotData);
		CommonValidation.checkSortJobState(lotData);
		
		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, lotData.getProductSpecName(), lotData.getProductSpecVersion());

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

		// Create GarbageLotName List
		List<String> lotNameList = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, destinationCount);

		Element eleBody = SMessageUtil.getBodyElement(doc);

		Map<String, String> lotUdfs = lotData.getUdfs();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(lotData.getMachineName(), lotUdfs.get("PORTNAME"));

		List<Element> remainProductList = null;
		try
		{
			remainProductList = SMessageUtil.getSubSequenceItemList(eleBody, "REMAINPRODUCTLIST", false);
		}
		catch (Exception e)
		{
		}

		if (remainProductList != null)
		{
			String srcLotGrade = CommonUtil.judgeLotGradeByProductList(remainProductList, "PRODUCTGRADE");
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", srcLotGrade, lotName);
		}

		// For GarbageLotName Change
		int count = 0;

		// Get DurableName and ProductList
		for (Element eleDurable : SMessageUtil.getSubSequenceItemList(eleBody, "DURABLELIST", true))
		{
			List<Element> eleProductList = null;
			try
			{
				eleProductList = SMessageUtil.getSubSequenceItemList(eleDurable, "PRODUCTLIST", true);
			}
			catch (Exception e)
			{
			}

			if (eleProductList != null)
			{
				String desDurableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);

				Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(desDurableName);

				CommonValidation.CheckDurableHoldState(desDurableData);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				String productProductionType = null;

				for (Element eleProduct : eleProductList)
				{
					String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
					int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(position);
					productP.getUdfs().put("SLOTPOSITION", "");

					productPSequence.add(productP);

					productProductionType = productData.getProductionType();
				}

				// Create GarbageLot
				String newlotName = lotNameList.get(count);

				destLotNameList.add(newlotName);
				
				eventInfo.setEventName("Create");
				Lot garbageLot = this.createWithParentLotAndProductProductionType(eventInfo, newlotName, lotData, productProductionType, "", false, new HashMap<String, String>(), lotData.getUdfs());

				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(garbageLot.getKey().getLotName(), eleProductList.size(), productPSequence,
						lotData.getUdfs(), new HashMap<String, String>());

				// Assign Carrier
				List<ProductP> productPEmptySequence = new ArrayList<ProductP>();

				AssignCarrierInfo AssigncreateInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(garbageLot, desDurableName, productPEmptySequence);

				//Validation
				CommonValidation.checkEmptyCst(desDurableName);
				CommonValidation.checkMultiLot(desDurableName);
				
				eventInfo.setEventName("AssignCarrier");
				MESLotServiceProxy.getLotServiceImpl().assignCarrier(garbageLot, AssigncreateInfo, eventInfo);

				// Split Lot
				String desLotGrade = CommonUtil.judgeLotGradeByProductList(eleProductList, "PRODUCTGRADE");
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", desLotGrade, newlotName);
				eventInfo.setEventName("SplitForShip");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

				garbageLot = MESLotServiceProxy.getLotInfoUtil().getLotData(garbageLot.getKey().getLotName());

				// For Return DestLotList
				eleDestLotList.add(setDestLotList(garbageLot));

				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();

				// Make Emptied
				if (lotData.getProductQuantity() == 0)
				{
					if (StringUtil.isNotEmpty(lotData.getCarrierName()))
					{
						Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
						DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());
						// Deassign Carrier
						eventInfo.setEventName("DeassignCarrier");
						MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
					}

					eventInfo.setEventName("MakeEmptied");
					MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
					deassignCarrierUdfs.clear();
				}

				List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(eleProductList);

				if (trackOutFlag)
				{
					// Sampling
					ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(garbageLot);
					if (!StringUtil.equals(processFlowData.getProcessFlowType(), "MQC"))
						MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(eventInfo, garbageLot, new ArrayList<Element>(), true);
					MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, garbageLot);

					// Track Out GarbageLot
					MESLotServiceProxy.getLotServiceUtil().
						trackOutLotWithSampling(eventInfo, garbageLot, portData, desDurableName, garbageLot.getLotGrade(), lotData.getMachineName(), "", productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
				}
				count += 1;
			}
		}

		//Mantis 0000356
		MESLotServiceProxy.getLotServiceUtil().deleteSortJob(eventInfo, srcLotNameList, destLotNameList, "Split (TP to OLED)");
		
		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotNameList, destLotNameList);

		// 2020-11-15	dhko	LotHold if ProcessingInfo is 'B' in Product
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MESLotServiceProxy.getLotServiceImpl().makeOnHoldByAbortProductList(eventInfo, srcLotNameList);
		MESLotServiceProxy.getLotServiceImpl().makeOnHoldByAbortProductList(eventInfo, destLotNameList);
		
		// Return GarbageLotList
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "SPLITLOTLIST", eleDestLotList);

		Element durableList = XmlUtil.getChild(eleBody, "DURABLELIST", false);

		if (durableList != null)
			eleBody.removeChild("DURABLELIST");

		Element remainProductListE = XmlUtil.getChild(eleBody, "REMAINPRODUCTLIST", false);

		if (remainProductListE != null)
			eleBody.removeChild("REMAINPRODUCTLIST");

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

	private Element setDestLotList(Lot lotData)
	{
		Element eleLot = new Element("SPLITLOT");

		try
		{
			XmlUtil.addElement(eleLot, "SPLITLOTNAME", lotData.getKey().getLotName());
			XmlUtil.addElement(eleLot, "CARRIERNAME", lotData.getCarrierName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECNAME", lotData.getProductSpecName());
			XmlUtil.addElement(eleLot, "PROCESSFLOWNAME", lotData.getProcessFlowName());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", lotData.getKey().getLotName()));
		}

		return eleLot;
	}
}
