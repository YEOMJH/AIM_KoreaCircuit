package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CompleteFirstGlassLot extends SyncHandler {

	private static Log log = LogFactory.getLog(CompleteFirstGlassLot.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String mLotName = SMessageUtil.getBodyItemValue(doc, "MLOTNAME", true);
		String cLotName = SMessageUtil.getBodyItemValue(doc, "CLOTNAME", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventComment("CompleteFirstGlassLot: " + eventInfo.getEventComment());

		// 2020-11-14	dhko	Add Validation
		CommonValidation.checkProcessInfobyString(mLotName);
		CommonValidation.checkProcessInfobyString(cLotName);
		
		Lot mLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(mLotName);
		CommonValidation.checkJobDownFlag(mLotData);

		Lot cLotData = new Lot();
		try
		{
			cLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(cLotName);
		}
		catch (Exception e)
		{
			cLotData = null;
		}

		List<Lot> childLotList = new ArrayList<Lot>();

		if (cLotData != null)
		{
			CommonValidation.checkJobDownFlag(cLotData);
			childLotList = checkChildLot(mLotData, cLotData);
		}
		else
		{
			childLotList = null;
		}

		Object[] bindSet = new Object[] { mLotData.getUdfs().get("JOBNAME") };
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);

		if (childLotList != null)
		{
			if (childLotList.size() > 0)
			{
				MESLotServiceProxy.getLotServiceUtil().deleteSampleFirstGlass(eventInfo, cLotData.getUdfs().get("JOBNAME"), cLotData);
				if (getLotFutureActionList(cLotData, jobData))
				{
					MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionbyReasonCode(eventInfo, cLotData.getKey().getLotName(), cLotData.getFactoryName(),
							jobData.getReturnProcessFlowName(), jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), "0",
							"FirstGlassHold");
				}

				if (childLotList.size() == 1)
				{
					mergeToParentLot(eventInfo, mLotData, cLotData, jobData);

					// Complete Job
					if (StringUtil.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtil.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG")))
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							SetEventInfo setEventInfo = new SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							udfs.put("FIRSTGLASSALL", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							LotServiceProxy.getLotService().setEvent(mLotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + mLotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
					}
				}
				else
				{
					mergeToParentLot(eventInfo, mLotData, cLotData, jobData);
				}
			}
		}
		else
		{
			if (StringUtils.equals(mLotData.getUdfs().get("FIRSTGLASSALL"), "Y"))
			{
				if (!StringUtils.equals(jobData.getProcessFlowName(), mLotData.getProcessFlowName()) || !StringUtils.equals(jobData.getProcessOperationName(), mLotData.getProcessOperationName()))
				{
					throw new CustomException("LOT-0143", mLotData.getProcessFlowName(), mLotData.getProcessOperationName());
				}

				log.info("LotName : " + mLotData.getKey().getLotName() + ", FirstGlassAll : " + mLotData.getUdfs().get("FIRSTGLASSALL"));

				MESLotServiceProxy.getLotServiceUtil().deleteSampleFirstGlass(eventInfo, mLotData.getUdfs().get("JOBNAME"), mLotData);

				if (getLotFutureActionList(mLotData, jobData))
				{
					MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionbyReasonCode(eventInfo, mLotData.getKey().getLotName(), mLotData.getFactoryName(),
							jobData.getReturnProcessFlowName(), jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), "0",
							"FirstGlassHold");
				}

				// Release Final Completed Lot
				if (StringUtil.equals(mLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(mLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(mLotData, productUSequence, new HashMap<String, String>());
					mLotData = LotServiceProxy.getLotService().makeNotOnHold(mLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(mLotData.getKey().getLotName(), "FirstGlassHold", mLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(mLotData.getKey().getLotName(), "FirstGlassHold", mLotData.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, mLotData);
				}

				// Complete Job
				if (StringUtil.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtil.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG")))
				{
					String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("JOBNAME", jobData.getJobName());
					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

					try
					{
						// Delete Job Name
						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

						Map<String, String> udfs = new HashMap<>();
						udfs.put("JOBNAME", "");
						udfs.put("FIRSTGLASSFLAG", "");
						udfs.put("FIRSTGLASSALL", "");
						setEventInfo.setUdfs(udfs);
						eventInfo.setEventName("CompleteFirstGlassJob");
						LotServiceProxy.getLotService().setEvent(mLotData.getKey(), eventInfo, setEventInfo);
					}
					catch (Exception e)
					{
						log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + mLotData.getKey().getLotName());
					}

					// Update Job State
					eventInfo.setEventName("Complete");
					jobData.setJobState("Completed");
					jobData.setLastEventComment(eventInfo.getEventComment());
					ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
				}
			}
		}

		return doc;
	}

	private List<Lot> checkChildLot(Lot mLotData, Lot cLotData) throws CustomException
	{

		if (!StringUtils.equals(cLotData.getUdfs().get("FIRSTGLASSFLAG"), "N"))
			throw new CustomException("FIRSTGLASS-0006", cLotData.getKey().getLotName(), cLotData.getUdfs().get("FIRSTGLASSFLAG"));

		if (!StringUtils.equals(cLotData.getProcessOperationName(), mLotData.getProcessOperationName()))
			throw new CustomException("FIRSTGLASS-0008", cLotData.getKey().getLotName());

		if (!StringUtils.equals(cLotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
			throw new CustomException("FIRSTGLASS-0007", cLotData.getKey().getLotName(), cLotData.getLotProcessState());

		Object[] bindSet = new Object[] { mLotData.getUdfs().get("JOBNAME") };
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);

		List<Lot> childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG IS NOT NULL AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
				new Object[] { jobData.getJobName() });

		return childLotDataList;
	}

	private void mergeToParentLot(EventInfo eventInfo, Lot mLotData, Lot cLotData, FirstGlassJob jobData) throws greenFrameDBErrorSignal, CustomException
	{
		List<Lot> childLotDataList = new ArrayList<Lot>();

		try
		{
			// Find final Completed child lot
			childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
					new Object[] { jobData.getJobName(), "N" });
		}
		catch (Exception e)
		{
		}

		if (childLotDataList.size() > 0)
		{
			Lot childLotData = childLotDataList.get(0);
			String prevChildLotName = childLotData.getKey().getLotName();

			// Change FirstGlassFlag in ChildLot
			eventInfo.setEventName("ChangeFirstGlassFlag");
			LotServiceProxy.getLotService().update(childLotData);

			// Set Event
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FIRSTGLASSFLAG", "Y");

			LotServiceProxy.getLotService().setEvent(childLotData.getKey(), eventInfo, setEventInfo);

			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(prevChildLotName);

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			for (Product productData : productList)
			{
				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(productData.getPosition());
				productP.setUdfs(productData.getUdfs());
				productPSequence.add(productP);
			}

			// Release Final Completed Child Lot
			if (StringUtil.equals(childLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
			{
				eventInfo.setEventName("ReleaseHold");

				//Map<String, String> udfs = childLotData.getUdfs();
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(childLotData);
				MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(childLotData, productUSequence, new HashMap<String, String>());
				childLotData = LotServiceProxy.getLotService().makeNotOnHold(childLotData.getKey(), eventInfo, makeNotOnHoldInfo);

				// delete in LOTMULTIHOLD table
				MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

				// delete in PRODUCTMULTIHOLD table
				MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

				// setHoldState
				MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, childLotData);
			}

			// Release Lot
			if (StringUtil.equals(mLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
			{
				eventInfo.setEventName("ReleaseHold");

				Map<String, String> udfs = new HashMap<String, String>();
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(mLotData);
				MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(mLotData, productUSequence, udfs);
				mLotData = LotServiceProxy.getLotService().makeNotOnHold(mLotData.getKey(), eventInfo, makeNotOnHoldInfo);

				// delete in LOTMULTIHOLD table
				MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(mLotData.getKey().getLotName(), "FirstGlassHold", mLotData.getProcessOperationName());

				// delete in PRODUCTMULTIHOLD table
				MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(mLotData.getKey().getLotName(), "FirstGlassHold", mLotData.getProcessOperationName());

				// setHoldState
				MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, mLotData);
			}

			// Merge Lot
			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(mLotData.getKey().getLotName(), productList.size(), productPSequence,
					mLotData.getUdfs(), new HashMap<String, String>());
			eventInfo.setEventName("Merge");
			childLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, childLotData, transitionInfo);

			Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
			if (StringUtil.isNotEmpty(childLotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(childLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
				DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(childLotData, sLotDurableData, new ArrayList<ProductU>());
				// Deassign Carrier
				eventInfo.setEventName("DeassignCarrier");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(childLotData, deassignCarrierInfo, eventInfo);
			}

			eventInfo.setEventName("MakeEmptied");
			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, childLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();

			if (StringUtil.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtil.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG")))
			{
				// Check Grade
				List<Product> mLotProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(mLotData.getKey().getLotName());
				boolean isExistGrade = false;
				String lotGrade = "";
				for (Product productData : mLotProductList)
				{
					if (StringUtil.equals(productData.getProductGrade(), "R"))
					{
						isExistGrade = true;
						lotGrade = "R";
						break;
					}
				}

				if (isExistGrade)
				{
					eventInfo.setEventName("ChangeGrade");
					List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

					ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(mLotData, lotGrade, productPGSSequence);

					MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, mLotData, changeGradeInfo);
				}
			}
		}
	}

	private boolean getLotFutureActionList(Lot lotData, FirstGlassJob jobData) throws CustomException
	{
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataList(lotData.getKey().getLotName(), lotData.getFactoryName(),
				jobData.getReturnProcessFlowName(), jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), 0, "FirstGlassHold");
		
		boolean checkFlag = false;

		if (lotFutureActionList != null)
		{
			checkFlag = true;
		}

		return checkFlag;
	}

}
