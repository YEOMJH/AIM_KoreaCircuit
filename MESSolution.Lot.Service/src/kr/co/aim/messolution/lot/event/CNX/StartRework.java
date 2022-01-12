package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.TryRunControl;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.data.LotMultiHoldKey;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class StartRework extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "REWORKFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "REWORKOPERATIONNAME", true);
		String processOperationVer = SMessageUtil.getBodyItemValue(doc, "REWORKOPERATIONVER", false);
		String returnProcessFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnProcessOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONNAME", true);
		String returnPorcessOpertaionVer = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", false);
		String reworkType = SMessageUtil.getBodyItemValue(doc, "REWORKTYPE", false);
		String reworkCountLimit = SMessageUtil.getBodyItemValue(doc, "REWORKCOUNTLIMIT", false);
		String oriProcessFlowname = SMessageUtil.getBodyItemValue(doc, "ORIPROCESSFLOWNAME", true);
		String oriProcessOperationName = SMessageUtil.getBodyItemValue(doc, "ORIPROCESSOPERATIONNAME", true);

		if (!StringUtil.isNotEmpty(processOperationVer))
			processOperationVer = "00001";

		if (!StringUtil.isNotEmpty(returnPorcessOpertaionVer))
			returnPorcessOpertaionVer = "00001";

		List<Element> reworkOperList = SMessageUtil.getBodySequenceItemList(doc, "REWORKOPERLIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
		Element element = doc.getDocument().getRootElement();
		String factoryName = lotData.getFactoryName();

		long numReworkCountLimit = 0;
		boolean isContainSameOperforFuturehold = false;

		// validation.
		CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkProcessInfobyString(lotName);
		CommonValidation.checkDummyProductReserve(lotData);

		List<Element> reworkProdList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		List<String> productNameList = new ArrayList<>();
		for (Element ProductE : reworkProdList)
		{
			String ProductName = ProductE.getChildText("PRODUCTNAME");
			if (!productNameList.contains(ProductName))
				productNameList.add(ProductName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);
		List<String> reworkFlagProdList = new ArrayList<String>();
		
		if (reworkProdList.size() == 0)
			throw new CustomException("PRODUCT-9004");

		if (StringUtil.isNotEmpty(reworkCountLimit) && StringUtil.isNotEmpty(reworkType))
		{
			numReworkCountLimit = Long.valueOf(reworkCountLimit);
			for (Element reworkProd : reworkProdList)
			{
				for (Element reworkOper : reworkOperList)
				{
					try
					{
						ReworkProduct reworkProduct = ExtendedObjectProxy.getReworkProductService().selectByKey(false, new Object[]{reworkProd.getChildText("PRODUCTNAME"),reworkOper.getChildText("REWORKOPERNAME")});

						if (reworkProduct.getReworkCount() > numReworkCountLimit)
							throw new CustomException("PRODUCT-0203", reworkProd.getChildText("PRODUCTNAME"),reworkProduct.getReworkCount(),numReworkCountLimit);
					}
					catch (greenFrameDBErrorSignal ex)
					{

					}
				}

			}
		}

		if (StringUtil.isEmpty(reworkType))
			reworkType = "Normal";

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 2019.12.23 Reqeust by V3.
		lotData = MESLotServiceProxy.getLotServiceUtil().ReleaseHoldLot(eventInfo, lotData);
		
		//Mantis - 0000061
		if (StringUtils.equals(lotData.getLotGrade(), "P") )
		{
			throw new CustomException("LOT-0223"); 
		}
		
		
		List<ProductPGS> productPGSList = this.getPGSList(lotName,productNameList);
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, "R", productPGSList);

		eventInfo.setEventName("ChangeGradeForRework");
		lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		
		eventInfo.setEventName("Rework");

		if (factoryName.equals("ARRAY") || factoryName.equals("TP"))
		{
			String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
			String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODENAME", true);
			eventInfo.setReasonCode(reasonCode);
			eventInfo.setReasonCodeType(SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + reasonCodeType);
		}

		if (reworkOperList.size() > 0)
		{
			// reserve FutureSkip Not Selected Rework Operation
			List<String> reworkFlowOperList = CommonUtil.getOperList(lotData.getFactoryName(), processFlowName, "00001");

			// for ReworkFlag.
			long firstOperNum = 0;
			for (Element reworkOper : reworkOperList)
			{
				if (firstOperNum == 0)
					firstOperNum = Long.parseLong(reworkOper.getChildText("REWORKOPERNUM"));
				
				long operNum = Long.parseLong(reworkOper.getChildText("REWORKOPERNUM"));
				
				if(firstOperNum > operNum)
					firstOperNum = operNum;
			}

			for (Element reworkOper : reworkOperList)
			{
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), reworkOper.getChildText("REWORKOPERNAME"), "00001");

				if (StringUtils.equals(operationData.getProcessOperationType(), "Inspection"))
				{
					isContainSameOperforFuturehold = true;
					break;
				}
			}

			for (int i = 0; i < reworkFlowOperList.size(); i++)
			{
				String reworkFlowOper = reworkFlowOperList.get(i);
				// Check Selected Rework Operation
				boolean isContainSameOper = false;
				boolean firstOperReworkFlag = false;
				
				for (Element reworkOper : reworkOperList)
				{
					if (StringUtil.equals(reworkFlowOper, reworkOper.getChildText("REWORKOPERNAME")))
					{
						isContainSameOper = true;
						long currentNum = Long.parseLong(reworkOper.getChildText("REWORKOPERNUM"));
						
						if (firstOperNum == currentNum)
							firstOperReworkFlag = true;
					}
				}

				if (isContainSameOper == true)
				{
					List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
							lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
							lotData.getProcessOperationVersion(), processFlowName, "00001", reworkFlowOper, processOperationVer);

					if (sampleLot == null)
					{
						List<String> actualSamplePositionList = new ArrayList<String>();

						List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

						long RjudgeCountOfProductList = 0;

						for (Product productE : productList)
						{
							String productName = productE.getKey().getProductName();
							Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

							if (StringUtil.equals(productData.getProductGrade(), "R"))
								RjudgeCountOfProductList++;
						}

						if (!factoryName.equals("POSTCELL"))
						{
							for (Product productE : productList)
							{
								String productName = productE.getKey().getProductName();
								Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

								for (Element reworkProd : reworkProdList)
								{
									if (StringUtil.equals(reworkFlowOper, reworkProd.getChildText("OPERATIONNAME")) && StringUtil.equals(productName, reworkProd.getChildText("PRODUCTNAME")))
									{
										ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, reworkProd.getChildText("PRODUCTNAME"), lotName, productData.getFactoryName(),
												productData.getProductSpecName(), productData.getProductSpecVersion(), productData.getProcessFlowName(), productData.getProcessFlowVersion(),
												productData.getProcessOperationName(), productData.getProcessOperationVersion(), "NA", processFlowName, "00001", reworkFlowOper, "00001", "Y",
												String.valueOf(RjudgeCountOfProductList), reworkProd.getChildText("POSITION"), reworkProd.getChildText("POSITION"), "", "Y");

										actualSamplePositionList.add(String.valueOf(productData.getPosition()));
										
										// for ReworkFlag
										if (firstOperReworkFlag)
											reworkFlagProdList.add(productName);

									}
								}
							}

							ExtendedObjectProxy.getSampleLotService()
									.insertSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
											lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "NA", processFlowName, "00001", reworkFlowOper,
											processOperationVer, "Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
											CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", 0, returnProcessFlowName, "00001", returnProcessOperationName,
											returnPorcessOpertaionVer, "");
						}
					}
					else
					{
						throw new CustomException("LOT-0018", sampleLot.get(0).getLotName(), sampleLot.get(0).getToProcessOperationName());
					}
				}
			}

			List<LotFutureAction> reserveData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutReasonCode(lotName, factoryName, returnProcessFlowName, "00001",
					returnProcessOperationName, processOperationVer, 0);

			EventInfoExtended CompleteReworkHoldEventInfo = new EventInfoExtended(eventInfo);
			CompleteReworkHoldEventInfo.setEventComment("CompleteReworkHold + " + getEventComment());

			if (reserveData != null)
			{
				String actionName = reserveData.get(0).getActionName();
				String beforeAction = reserveData.get(0).getBeforeAction();
				String eventName = reserveData.get(0).getLastEventName();
				String reasonCode = reserveData.get(0).getReasonCode();

				if (!(StringUtil.equals(actionName, "hold") && StringUtils.equals(beforeAction, "True") && StringUtils.equals(eventName, "Rework") && StringUtils.equals(reasonCode,
						"CheckReworkSamplingDate")))
				{
					if (isContainSameOperforFuturehold == true && (factoryName.equals("ARRAY") || factoryName.equals("TP")))
					{
						MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(CompleteReworkHoldEventInfo, lotName, factoryName, returnProcessFlowName, "00001",
								returnProcessOperationName, processOperationVer, "0", "hold", "System", "ReserveHoldLot", "CheckReworkSamplingDate", "", "", "", "True", "False",
								CompleteReworkHoldEventInfo.getEventComment(), "", getEventUser(), "", "Insert", "", "");
					}
				}
			}
			else if (isContainSameOperforFuturehold == true && (factoryName.equals("ARRAY") || factoryName.equals("TP")))
			{
				MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(CompleteReworkHoldEventInfo, lotName, factoryName, returnProcessFlowName, "00001",
						returnProcessOperationName, processOperationVer, "0", "hold", "System", "ReserveHoldLot", "CheckReworkSamplingDate", "", "", "", "True", "False",
						CompleteReworkHoldEventInfo.getEventComment(), "", getEventUser(), "", "Insert", "", "");
			}

			// makeInRework
			//Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);
			Map<String, String> udfs = new HashMap<String, String>();
			
			List<ProductRU> productRUdfs = MESLotServiceProxy.getLotServiceUtil().setProductRUSequence(reworkFlagProdList);

			MakeInReworkInfo makeInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(lotData, eventInfo, lotName, processFlowName, processOperationName, processOperationVer,
					returnProcessFlowName, returnProcessOperationName, returnPorcessOpertaionVer, udfs, productRUdfs);
			
			makeInReworkInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			makeInReworkInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());

			Lot validationLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			if (StringUtil.equals(oriProcessOperationName, validationLotData.getProcessOperationName()) && StringUtil.equals(oriProcessFlowname, validationLotData.getProcessFlowName()))
				lotData = MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, lotData, makeInReworkInfo);
			else
				throw new CustomException("LOT-0210", processOperationName, validationLotData.getProcessOperationName());

			// Hold Lot
			makeOnHoldForRework(eventInfo, lotData, oldLotData);
		}
		else
		{
			throw new CustomException("LOT-0101");
		}

		try
		{
			if (StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("Switch_DSP_AOILotJudge"), "Y") && StringUtils.equals(lotData.getFactoryName(), "ARRAY"))
				CommonValidation.checkReworkAOILotJudge(lotData);
		}
		catch (Exception e)
		{
			eventLog.info("Error Occurred - ChangeAOILotJudgeByReworkLot");
		}
		
		if(StringUtils.equals(processFlowName, "LRGP21"))
		{
			try
			{
				List<EnumDefValue> enumDataList=ExtendedObjectProxy.getEnumDefValueService().select(" ENUMNAME=? ",new Object[]{"D17EtchSampling"});
				if(enumDataList!=null&&enumDataList.size()>0)
				{
					for(EnumDefValue enumData:enumDataList)
					{
						int seq=0;
						if(StringUtils.isNotEmpty(enumData.getSeq()))
						{
							seq=Integer.parseInt(enumData.getSeq());
						}
						ExtendedObjectProxy.getEnumDefValueService().modifyEnumSeq("D17EtchSampling", enumData.getEnumValue(),Integer.toString(seq+1) );
					}

				}
			}

			catch (FrameworkErrorSignal e) 
			{
				
			}
		}

		return doc;
	}

	private List<ProductPGS> getPGSList(String lotName, List<String> productNameList) 
	{
		List<ProductPGS> productPGSList = new ArrayList<ProductPGS>();
		List<Product> productDatas = new ArrayList<Product>();
		
		productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for (Product product : productDatas) 
		{
			boolean isRProduct = false;
			ProductPGS productPGS = new ProductPGS();
			productPGS.setProductName(product.getKey().getProductName());
			productPGS.setPosition(product.getPosition());
			
			for (String rProduct : productNameList) 
			{
				if (rProduct.equals(product.getKey().getProductName()))
				{
					isRProduct = true;
					break;
				}
			}
			
			if (isRProduct) 
			{
				productPGS.setProductGrade("R");
			}
			else 
			{
				productPGS.setProductGrade(product.getProductGrade());
			}
			
			productPGS.setSubProductGrades1(product.getSubProductGrades1());
			productPGS.setSubProductGrades2(product.getSubProductGrades2());
			productPGS.setSubProductQuantity1(product.getSubProductQuantity());

			productPGS.setUdfs(product.getUdfs());

			productPGSList.add(productPGS);
		}
		
		return productPGSList;
	}

	private void makeOnHoldForRework(EventInfo eventInfo, Lot lotData, Lot oldLotData) throws CustomException
	{
		if (StringUtil.equals(oldLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("Hold");
			eventInfo.setReasonCode("SYSTEM");
			eventInfo.setEventComment("Start Rework [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());

			Map<String, String> udfs = new HashMap<String, String>();

			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
			{
				lotMultiHold(eventInfo, lotData, udfs);
			}
			else
			{
				throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
			}
		}
	}

	private void lotMultiHold(EventInfo eventInfo, Lot lotData, Map<String, String> udfs) throws CustomException
	{
		// Set EventInfo
		eventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

		// Update HoldState for execution MultiHold
		if (StringUtil.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			// Update LotHoldState - N
			String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

			// Update ProductHoldState - N
			sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			bindMap.clear();
			bindMap.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}

		// Get ProductUSequence
		List<ProductU> productUSequence = getAllProductUSequence(lotData);

		// Set MakeOnHoldInfo
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, new HashMap<String, String>());
		LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

		// Set Udfs - PROCESSOPERATIONNAME to Insert OperationName MultiHold
		udfs.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

		// Insert into LOTMULTIHOLD table
		LotMultiHoldKey holdKey = new LotMultiHoldKey();
		holdKey.setLotName(lotData.getKey().getLotName());
		holdKey.setReasonCode(eventInfo.getReasonCode());

		LotMultiHold holdData = new LotMultiHold();
		holdData.setKey(holdKey);
		holdData.setEventName(eventInfo.getEventName());
		holdData.setEventTime(eventInfo.getEventTime());
		holdData.setEventComment(eventInfo.getEventComment());
		holdData.setEventUser(eventInfo.getEventUser());
		holdData.setUdfs(udfs);

		try
		{
			LotServiceProxy.getLotMultiHoldService().insert(holdData);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-0002", holdKey.getLotName(), holdKey.getReasonCode());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// insert in PRODUCTMULTIHOLD table
		MESProductServiceProxy.getProductServiceImpl().setProductMultiHold(eventInfo, lotData.getKey().getLotName(), udfs);
	}

	private List<ProductU> getAllProductUSequence(Lot lotData) throws CustomException
	{
		// 1. Set Variable
		List<ProductU> ProductUSequence = new ArrayList<ProductU>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		try
		{
			productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			if (product.getProductGrade().equals("R"))
				product.getUdfs().put("REWORKFLAG", "Y");

			ProductU productU = new ProductU();

			productU.setProductName(product.getKey().getProductName());
			productU.setUdfs(product.getUdfs());

			// Add productPSequence By Product
			ProductUSequence.add(productU);
		}

		return ProductUSequence;
	}
}
