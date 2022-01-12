package kr.co.aim.messolution.durable.event;

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
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ExtractBufferCST extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotNameSourceA = SMessageUtil.getBodyItemValue(doc, "SOURCELOTNAME", true);
		String durableNameSource = SMessageUtil.getBodyItemValue(doc, "SOURCEDURABLENAME", true);
		String durableNameDestination = SMessageUtil.getBodyItemValue(doc, "DESTDURABLENAME", true);
		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleDestinationProductList = SMessageUtil.getSubSequenceItemList(eleBody, "DESTINATIONPRODUCTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Lot lotDataSourceA = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameSourceA);

		Durable srcDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableNameSource);
		Durable destDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableNameDestination);

		// validation
		CommonValidation.CheckDurableHoldState(destDurableData);
		CommonValidation.CheckDurableCleanState(destDurableData);
		CommonValidation.checkLotState(lotDataSourceA);
		CommonValidation.checkLotProcessState(lotDataSourceA);
		CommonValidation.checkLotHoldState(lotDataSourceA);

		if (StringUtils.equals(destDurableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
		{
			// 1. Create New Lot
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
			Lot destLot = createWithParentLotAndProductProductionType(eventInfo, newlotName, lotDataSourceA, lotDataSourceA.getProductionType(), "", false, new HashMap<String, String>(),
					lotDataSourceA.getUdfs());

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			List<ProductP> productPSequenceEmpty = new ArrayList<ProductP>();

			// 2. Transfer Product to Lot
			AssignCarrierInfo AssignCreateInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(destLot, durableNameDestination, productPSequenceEmpty);
			eventInfo.setEventName("AssignCarrier");
			MESLotServiceProxy.getLotServiceImpl().assignCarrier(destLot, AssignCreateInfo, eventInfo);

			for (Element eleProduct : eleDestinationProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
				String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
				int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

				Map<String, String> productUdfs = new HashMap<String, String>();
				productUdfs.put("SLOTPOSITION", slotPosition);

				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(position);
				productP.setUdfs(productUdfs);
				productPSequence.add(productP);
			}

			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(destLot.getKey().getLotName(), eleDestinationProductList.size(), productPSequence,
					destLot.getUdfs(), new HashMap<String, String>());
			eventInfo.setEventName("MergeForReceive");
			lotDataSourceA = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotDataSourceA, transitionInfo);

			UpdateSrcData(lotNameSourceA, durableNameSource, srcDurableData.getLotQuantity(), destDurableData.getLastEventTimeKey(), lotDataSourceA.getLastEventTimeKey());

			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTNAME", destLot.getKey().getLotName());
			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTHOLDSTATE", destLot.getLotHoldState());
			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTSTATE", destLot.getLotState());
			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTGRADE", destLot.getLotGrade());
			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTQTY", String.valueOf((int) destLot.getProductQuantity()));
			XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTSPEC", destLot.getProductSpecName());

		}
		else if (StringUtils.equals(destDurableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
		{
			// 1. Get Lot Data by Dest CST
			Lot DestLotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(durableNameDestination);

			// 2. Check ProductSpec, ProcessFlow, ProcessOperation - SourceProduct (PRODUCTSPECNAME, BEFOREFLOWNAME, BEFOREWOPERATIONNAME)
			if (lotDataSourceA.getProductSpecName().equals(DestLotData.getProductSpecName()) && lotDataSourceA.getUdfs().get("RETURNFLOWNAME").equals(DestLotData.getUdfs().get("RETURNFLOWNAME"))
					&& lotDataSourceA.getUdfs().get("RETURNOPERATIONNAME").equals(DestLotData.getUdfs().get("RETURNOPERATIONNAME")))
			{
				// 3. Transfer Product to Lot
				List<ProductP> productPSequence = new ArrayList<ProductP>();

				for (Element eleProduct : eleDestinationProductList)
				{
					String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
					String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
					int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

					Map<String, String> productUdfs = new HashMap<String, String>();
					productUdfs.put("SLOTPOSITION", slotPosition);

					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(position);
					productP.setUdfs(productUdfs);
					productPSequence.add(productP);
				}

				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(DestLotData.getKey().getLotName(), eleDestinationProductList.size(),
						productPSequence, DestLotData.getUdfs(), new HashMap<String, String>());
				eventInfo.setEventName("MergeForReceive");
				lotDataSourceA = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotDataSourceA, transitionInfo);

				UpdateSrcData(lotNameSourceA, durableNameSource, srcDurableData.getLotQuantity(), DestLotData.getLastEventTimeKey(), lotDataSourceA.getLastEventTimeKey());

				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTNAME", DestLotData.getKey().getLotName());
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTHOLDSTATE", DestLotData.getLotHoldState());
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTSTATE", DestLotData.getLotState());
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTGRADE", DestLotData.getLotGrade());
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTQTY", String.valueOf((int) DestLotData.getProductQuantity()));
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTSPEC", DestLotData.getProductSpecName());

			}
			else
			{
				throw new CustomException("LOT-9012");
			}

		}
		else
		{
			// validation
			CommonValidation.CheckDurableState(destDurableData);
		}

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

		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", parentlotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", parentlotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", parentlotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("ARRAYLOTNAME", parentlotData.getUdfs().get("ARRAYLOTNAME"));		
		
		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	private void UpdateSrcData(String srclotName, String srcDurableName, long srcLotQty, String durabletimekey, String lottimekey) throws CustomException
	{
		try
		{
			long LotQty = srcLotQty - 1;

			if (LotQty > 0)
			{
				// Update LOT Deassign ScrLot by ScrCST
				String sql = "UPDATE LOT SET CARRIERNAME = :CARRIERNAME WHERE LOTNAME = :LOTNAME";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("CARRIERNAME", "");
				bindMap.put("LOTNAME", srclotName);

				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

				// Update DURABLE ScrCarrier LotQty
				String sql3 = "UPDATE DURABLE SET LOTQUANTITY = :LOTQUANTITY WHERE DURABLENAME = :DURABLENAME";
				Map<String, Object> bindMap3 = new HashMap<String, Object>();
				bindMap3.put("LOTQUANTITY", LotQty);
				bindMap3.put("DURABLENAME", srcDurableName);

				GenericServiceProxy.getSqlMesTemplate().update(sql3, bindMap3);

				// Update LOTHISTORY Deassign ScrLot by ScrCST
				String sql2 = "UPDATE LOTHISTORY SET CARRIERNAME = :CARRIERNAME WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("CARRIERNAME", "");
				bindMap2.put("LOTNAME", srclotName);
				bindMap2.put("TIMEKEY", lottimekey);

				GenericServiceProxy.getSqlMesTemplate().update(sql2, bindMap2);

				// Update DURABLEHISTORY ScrCarrier LotQty
				String sql4 = "UPDATE DURABLEHISTORY SET LOTQUANTITY = :LOTQUANTITY WHERE DURABLENAME = :DURABLENAME AND TIMEKEY = :TIMEKEY";
				Map<String, Object> bindMap4 = new HashMap<String, Object>();
				bindMap4.put("LOTQUANTITY", LotQty);
				bindMap4.put("DURABLENAME", srcDurableName);
				bindMap4.put("TIMEKEY", durabletimekey);

				GenericServiceProxy.getSqlMesTemplate().update(sql4, bindMap4);
			}
			else
			// LotQty == 0
			{
				// Update LOT Deassign ScrLot by ScrCST
				String sql = "UPDATE LOT SET CARRIERNAME = :CARRIERNAME WHERE LOTNAME = :LOTNAME";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("CARRIERNAME", "");
				bindMap.put("LOTNAME", srclotName);

				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

				// Update DURABLE ScrCarrier LotQty
				String sql3 = "UPDATE DURABLE SET LOTQUANTITY = :LOTQUANTITY, DURABLESTATE = :DURABLESTATE WHERE DURABLENAME = :DURABLENAME";
				Map<String, Object> bindMap3 = new HashMap<String, Object>();
				bindMap3.put("LOTQUANTITY", LotQty);
				bindMap3.put("DURABLESTATE", GenericServiceProxy.getConstantMap().Dur_Available);
				bindMap3.put("DURABLENAME", srcDurableName);

				GenericServiceProxy.getSqlMesTemplate().update(sql3, bindMap3);

				// Update LOTHISTORY Deassign ScrLot by ScrCST
				String sql2 = "UPDATE LOTHISTORY SET CARRIERNAME = :CARRIERNAME WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("CARRIERNAME", "");
				bindMap2.put("LOTNAME", srclotName);
				bindMap2.put("TIMEKEY", lottimekey);

				GenericServiceProxy.getSqlMesTemplate().update(sql2, bindMap2);

				// Update DURABLEHISTORY ScrCarrier LotQty
				String sql4 = "UPDATE DURABLEHISTORY SET LOTQUANTITY = :LOTQUANTITY, DURABLESTATE = :DURABLESTATE WHERE DURABLENAME = :DURABLENAME AND TIMEKEY = :TIMEKEY";
				Map<String, Object> bindMap4 = new HashMap<String, Object>();
				bindMap4.put("LOTQUANTITY", LotQty);
				bindMap4.put("DURABLESTATE", GenericServiceProxy.getConstantMap().Dur_Available);
				bindMap4.put("DURABLENAME", srcDurableName);
				bindMap4.put("TIMEKEY", durabletimekey);

				GenericServiceProxy.getSqlMesTemplate().update(sql4, bindMap4);
			}
		}
		catch (Exception e)
		{
		}
	}
}