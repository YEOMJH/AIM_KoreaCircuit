/**********************************************************************************
\ * title : JudgeFirstGlassJob
 * write : uclee
 * date  : 2018.05.24 
 * contents : JudgeFirstGlassJob
 * 1.judge - Pass
 * 	- check job
 * 	- check final child lot, firstglassflag : Y
 *  - check other child lot, firstglassflag : N
 *  - merge lot
 *  - update lot firstglassflag : Y
 *  - update job judge
 *  - update job state : Completed
 *  - update parent lot release
 * 2.judge - Retry
 *	- check job
 *	- check final child lot, firstglassflag : Y
 *  - check other child lot, firstglassflag : N
 *	- merge lot
 *	- update lot firstglassflag : Y
 *	- update job judge
 *	- update job state - no update
 *	- update parent lot release - no release
 * 3.judge - Rework
 * 	- check job
 * 	- check final child lot, firstglassflag : Y
 *  - check other child lot, firstglassflag : N
 * 	- merge lot	
 * 	- update lot firstglassflag : Y
 *  - rework start
 * 	- update job judge
 * 	- update job state : Completed
 * 	- parent lot release
 **********************************************************************************/
package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
//import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
//import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
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
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class JudgeFirstGlassJob extends SyncHandler {

	private static Log log = LogFactory.getLog(JudgeFirstGlassJob.class);
	private String Pass = "Pass";
	private String Retry = "Retry";
	private String Rework = "Rework";
	private String Strip = "Strip";

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("JudgeFirstGlassJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventComment("JudgeFirstGlassJob: " + eventInfo.getEventComment());

		boolean checkAllSplitChildLot = false;
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String parentLotName = SMessageUtil.getBodyItemValue(doc, "PARENTLOTNAME", true);
		String childLotName = SMessageUtil.getBodyItemValue(doc, "CHILDLOTNAME", false);

		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String reworkFlowName = SMessageUtil.getBodyItemValue(doc, "REWORKFLOWNAME", false);
		String reworkFlowVersion = SMessageUtil.getBodyItemValue(doc, "REWORKFLOWVERSION", false);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", false);

		Object[] bindSet = new Object[] { jobName };
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);

		Lot parentLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(parentLotName);
		Lot childLotData = new Lot();

		String firstGlassAll = parentLotData.getUdfs().get("FIRSTGLASSALL");

		if (StringUtils.isNotEmpty(childLotName))
		{
			childLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(childLotName);

			// Lot's Operation[{0}] is unable to Split. Job's Operation is [{0}]
			if (!childLotData.getProcessOperationName().equals(jobData.getReturnProcessOperationName()))
				throw new CustomException("FIRSTGLASS-0005", childLotData.getProcessOperationName(), jobData.getReturnProcessOperationName());
		}

		StringBuilder sqlM = new StringBuilder();
		sqlM.append("SELECT LOTNAME ");
		sqlM.append("  FROM LOT ");
		sqlM.append(" WHERE JOBNAME = :JOBNAME ");
		sqlM.append("   AND LOTSTATE = 'Released' ");
		sqlM.append("   AND PRODUCTQUANTITY > 0 ");
		sqlM.append("   AND FIRSTGLASSFLAG IS NULL ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("JOBNAME", jobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultMotherLot = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlM.toString(), args);

		if (resultMotherLot.size() == 0)
			checkAllSplitChildLot = true;

		if (StringUtils.isNotEmpty(childLotName))
			childLotData = MESLotServiceProxy.getLotServiceUtil().mergeFirstGlassChildLot(eventInfo, jobData, childLotData, checkAllSplitChildLot);

		if (StringUtils.equals(Pass, judge))
		{
			// - update childLot firstglassflag 'Y'
			// - update job judge
			// - update job state
			// - update parent lot release

			// mLot is emptied.
			if (StringUtil.equals(parentLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied))
			{
				if (StringUtils.isNotEmpty(childLotName))
				{
					// Release Hold Child Lot
					if (childLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
					{
						eventInfo.setEventName("ReleaseHold");
						Map<String, String> udfs = new HashMap<String, String>();
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(childLotData);
						MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(childLotData, productUSequence, udfs);
						LotServiceProxy.getLotService().makeNotOnHold(childLotData.getKey(), eventInfo, makeNotOnHoldInfo);

						// delete in LOTMULTIHOLD table
						MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(childLotData.getKey().getLotName(), "FirstGlassHold", childLotData.getProcessOperationName());

						// delete in PRODUCTMULTIHOLD table
						MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(childLotData.getKey().getLotName(), "FirstGlassHold", childLotData.getProcessOperationName());

						// setHoldState
						MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, childLotData);
					}

					this.changeJobInfo(eventInfo, jobData, childLotData);
				}
			}
			else if (StringUtil.equals(parentLotData.getUdfs().get("FIRSTGLASSALL"), GenericServiceProxy.getConstantMap().FLAG_Y))
			{
				log.info("FirstGlassAll Case JobName : " + jobName);
				if (!StringUtils.equals(jobData.getReturnProcessFlowName(), parentLotData.getProcessFlowName())
						|| !StringUtils.equals(jobData.getReturnProcessOperationName(), parentLotData.getProcessOperationName()))
				{
					throw new CustomException("FIRSTGLASS-0017", parentLotData.getProcessOperationName());
				}

				// Release Hold Lot
				if (parentLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(parentLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(parentLotData, productUSequence, udfs);
					LotServiceProxy.getLotService().makeNotOnHold(parentLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, parentLotData);
				}

				// Update Job State
				jobData.setJudge(judge);
				ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

				this.changeJobInfo(eventInfo, jobData, parentLotData);
			}
			else
			// Normal Case
			{
				if (StringUtils.isNotEmpty(childLotName))
				{
					if (checkAllSplitChildLot && StringUtils.isEmpty(childLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isEmpty(childLotData.getUdfs().get("JOBNAME")))
					{
						log.info("Do not update FirstGlass Flag when last child lot has been completed first glass job");
					}
					else
					{
						this.updateFirstGlassFlag(eventInfo, childLotData);
					}

					// Release Hold Mother Lot
					if (parentLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
					{
						eventInfo.setEventName("ReleaseHold");

						Map<String, String> udfs = new HashMap<String, String>();
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(parentLotData);
						MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(parentLotData, productUSequence, udfs);
						LotServiceProxy.getLotService().makeNotOnHold(parentLotData.getKey(), eventInfo, makeNotOnHoldInfo);

						// delete in LOTMULTIHOLD table
						MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(parentLotName, "FirstGlassHold", parentLotData.getProcessOperationName());

						// delete in PRODUCTMULTIHOLD table
						MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(parentLotName, "FirstGlassHold", parentLotData.getProcessOperationName());

						// setHoldState
						MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, parentLotData);
					}

					if (checkAllSplitChildLot && StringUtils.isEmpty(childLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isEmpty(childLotData.getUdfs().get("JOBNAME")))
					{
						log.info("Do not Hold Lot when last child lot has been completed first glass job");
					}
					else
					{
						// Again Hold ChildLot
						if (childLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
						{
							// Hold ChildLot that was merged
							eventInfo.setReasonCode("FirstGlassHold");
							eventInfo.setReasonCodeType("FirstGlass");
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, childLotData, new HashMap<String, String>());
						}
					}

					// Update Job State
					jobData.setJudge(judge);
					ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
				}
			}
		}

		if (StringUtils.equals(Retry, judge))
		{
			// - update child lot firstglassflag 'Y'
			// - update job judge
			// - update job state - no update
			// - update parent lot release - no release
			// - update child lot hold

			if (StringUtils.equals(parentLotData.getUdfs().get("FIRSTGLASSALL"), GenericServiceProxy.getConstantMap().FLAG_Y))
			{
				// Release Hold Lot
				if (parentLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(parentLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(parentLotData, productUSequence, udfs);
					LotServiceProxy.getLotService().makeNotOnHold(parentLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, parentLotData);
				}

				eventInfo.setEventName("JudgeFirstGlassJob");
				String beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(parentLotData.getProcessFlowName(), parentLotData.getProcessOperationName());
				String newNodeStack = CommonUtil.getNodeStack(parentLotData.getFactoryName(), parentLotData.getProcessFlowName(), parentLotData.getProcessFlowVersion(), beforeOperationName, "00001");
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(parentLotData.getKey().getLotName());

				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(parentLotData, parentLotData.getAreaName(), parentLotData.getDueDate(),
						parentLotData.getFactoryName(), parentLotData.getLotHoldState(), parentLotData.getLotProcessState(), parentLotData.getLotState(), newNodeStack, parentLotData.getPriority(),
						parentLotData.getProcessFlowName(), parentLotData.getProcessFlowVersion(), beforeOperationName, "00001", parentLotData.getProcessOperationName(),
						parentLotData.getProductionType(), parentLotData.getProductRequestName(), parentLotData.getProductSpec2Name(), parentLotData.getProductSpec2Version(),
						parentLotData.getProductSpecName(), parentLotData.getProductSpecVersion(), productUdfs, parentLotData.getSubProductUnitQuantity1(), parentLotData.getSubProductUnitQuantity2());

				Lot newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, parentLotData, changeSpecInfo);

				eventInfo.setReasonCode("FirstGlassHold");
				eventInfo.setReasonCodeType("FirstGlass");

				lotMultiHold(eventInfo, newLotData, new HashMap<String, String>());
			}
			else
			{
				if (checkAllSplitChildLot && StringUtils.isEmpty(childLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isEmpty(childLotData.getUdfs().get("JOBNAME")))
				{
					log.info("Do not update FirstGlass Flag when last child lot has been completed first glass job");
				}
				else
				{
					this.updateFirstGlassFlag(eventInfo, childLotData);

					// Again Hold ChildLot
					if (childLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
					{
						// Hold ChildLot that was merged
						eventInfo.setReasonCode("FirstGlassHold");
						eventInfo.setReasonCodeType("FirstGlass");
						MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, childLotData, new HashMap<String, String>());
					}
				}
			}

			// Update Job State
			jobData.setJudge(judge);
			ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
		}

		if (StringUtils.equals(Rework, judge))
		{
			// - update lot firstglassflag 'Y'
			// - update job judge
			// - update job state
			// - parent lot release hold - no release
			// - lot release hold
			// - rework start

			Lot lotData = new Lot();

			if (StringUtils.equals(firstGlassAll, "Y"))
				lotData = parentLotData;
			else
				lotData = childLotData;

			// Release Hold Lot
			if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
			{
				eventInfo.setEventName("ReleaseHold");

				Map<String, String> udfs = new HashMap<String, String>();
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
				LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

				// delete in LOTMULTIHOLD table
				MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

				// delete in PRODUCTMULTIHOLD table
				MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

				// setHoldState
				MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
			}

			// Update Job State
			jobData.setJudge(judge);
			ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

			// Reserve Future Hold
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("InsertFutureHold", getEventUser(), getEventComment(), "", "");
			holdEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			holdEventInfo.setEventComment("ReserveOper:" + lotData.getProcessOperationName() + "." + eventInfo.getEventComment());
			try
			{
				String beforeActionComment = eventInfo.getEventComment();
				String beforeActionUser = eventInfo.getEventUser();

				MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(holdEventInfo, lotData.getKey().getLotName(), factoryName, jobData.getReturnProcessFlowName(),
						jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), "0", "hold", "System", "FirstGlass",
						"FirstGlassHold", "", "", "", "True", "False", beforeActionComment, "", beforeActionUser, "", "Insert", "", "");
			}
			catch (Exception e)
			{
				log.info("Error Occurred - insertCtLotFutureMultiHoldAction");
			}

			// Rework
			String returnProcessFlowName = jobData.getReturnProcessFlowName();
			String returnProcessFlowVersion = jobData.getReturnProcessFlowVersion();
			String returnProcessOperationName = jobData.getReturnProcessOperationName();
			String returnProcessOperationVersion = jobData.getReturnProcessOperationVersion();

			List<ProductU> productUSequence = getAllProductUSequence(lotData);

			eventInfo.setEventName("Rework");
			// start rework
			this.startRework(eventInfo, lotData, productUSequence, factoryName, reworkFlowName, reworkFlowVersion, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
					returnProcessOperationVersion, judge, operationList);
		}

		if (StringUtils.equals(Strip, judge))
		{
			// - split strip Lot
			// - update job state - no update
			// - update parent lot release - no release
			// - Strip Lot rework start

			// QTY validation
			if (productList.size() < 1)
				throw new CustomException("FIRSTGLASS-0001");

			// FirstGlassAll case : Do not split the R grade
			if (StringUtils.equals(firstGlassAll, "Y"))
			{
				List<Product> parentProdList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(parentLotData.getKey().getLotName());
				
				boolean isExistGrade = true;
				String lotGrade = "";
				for (Product productData : parentProdList)
				{
					if (StringUtil.equals(productData.getProductGrade(), "R"))
					{
						isExistGrade = false;
						lotGrade = "R";
						break;
					}
					else if (StringUtil.equals(productData.getProductGrade(), "G"))
					{
						isExistGrade = true;
						lotGrade = "G";
					}
				}
				
				if (isExistGrade)
				{
					eventInfo.setEventName("ChangeGrade");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

					ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(parentLotData, lotGrade, productPGSSequence);

					parentLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, parentLotData, changeGradeInfo);
				}
				
				// Update Job State
				jobData.setJudge(judge);
				ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
				
				// Release Hold Lot
				if (parentLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(parentLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(parentLotData, productUSequence, udfs);
					LotServiceProxy.getLotService().makeNotOnHold(parentLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(parentLotData.getKey().getLotName(), "FirstGlassHold", parentLotData.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, parentLotData);
				}
				
				String beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(parentLotData.getProcessFlowName(), parentLotData.getProcessOperationName());
				
				String returnProcessFlowName = parentLotData.getProcessFlowName();
				String returnProcessFlowVersion = parentLotData.getProcessFlowVersion();
				String returnProcessOperationName = beforeOperationName;
				String returnProcessOperationVersion = "00001";

				List<ProductU> productUSequence = getAllProductUSequence(parentLotData);

				eventInfo.setEventName("Rework");
				// start rework
				this.startRework(eventInfo, parentLotData, productUSequence, factoryName, reworkFlowName, reworkFlowVersion, returnProcessFlowName, returnProcessFlowVersion,
						returnProcessOperationName, returnProcessOperationVersion, judge, operationList);
			}
			else
			{
				List<Product> cLotProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(childLotData.getKey().getLotName());

				Lot stripLotData = new Lot();
				boolean finalFlag = false;

				// ProductList(OIC) is all 'R'
				if (cLotProductList.size() == productList.size())
				{
					stripLotData = childLotData;
				}
				else
				{
					stripLotData = this.splitStripLot(eventInfo, productList, childLotData, jobData);
					finalFlag = true;
				}

				// Check Grade.
				List<Product> mLotProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(childLotData.getKey().getLotName());
				boolean isExistGrade = true;
				String lotGrade = "";
				for (Product productData : mLotProductList)
				{
					if (StringUtil.equals(productData.getProductGrade(), "R"))
					{
						isExistGrade = false;
						lotGrade = "R";
						break;
					}
					else if (StringUtil.equals(productData.getProductGrade(), "G"))
					{
						isExistGrade = true;
						lotGrade = "G";
					}
				}

				if (isExistGrade)
				{
					eventInfo.setEventName("ChangeGrade");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

					ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(childLotData, lotGrade, productPGSSequence);

					childLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, childLotData, changeGradeInfo);
				}

				if (checkAllSplitChildLot && StringUtils.isEmpty(childLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isEmpty(childLotData.getUdfs().get("JOBNAME")))
				{
					log.info("Do not update FirstGlass Flag when last child lot has been completed first glass job");
				}
				else
				{
					if (finalFlag)
						this.updateFirstGlassFlag(eventInfo, childLotData);
				}

				// Update Job State
				jobData.setJudge(judge);
				ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

				// Release Hold Child Lot
				if (stripLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(stripLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(stripLotData, productUSequence, udfs);
					LotServiceProxy.getLotService().makeNotOnHold(stripLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(stripLotData.getKey().getLotName(), "FirstGlassHold", childLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(stripLotData.getKey().getLotName(), "FirstGlassHold", stripLotData.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, stripLotData);
				}

				// Rework
				String returnProcessFlowName = parentLotData.getProcessFlowName();
				String returnProcessFlowVersion = parentLotData.getProcessFlowVersion();
				String returnProcessOperationName = parentLotData.getProcessOperationName();
				String returnProcessOperationVersion = parentLotData.getProcessOperationVersion();

				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(stripLotData);

				eventInfo.setEventName("Rework");
				// start rework
				this.startRework(eventInfo, stripLotData, productUSequence, factoryName, reworkFlowName, reworkFlowVersion, returnProcessFlowName, returnProcessFlowVersion,
						returnProcessOperationName, returnProcessOperationVersion, judge, operationList);
			}
		}

		return doc;
	}

	private void changeJobInfo(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData) throws CustomException
	{
		log.info("ChangeJobInfo : CompleteFirstGlassJob");
		String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBNAME", jobData.getJobName());
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		try
		{
			// Delete Job Name
			SetEventInfo setEventInfo = new SetEventInfo();

			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			Map<String, String> udfs = new HashMap<>();
			udfs.put("JOBNAME", "");
			udfs.put("FIRSTGLASSFLAG", "");
			udfs.put("FIRSTGLASSALL", "");
			setEventInfo.setUdfs(udfs);
			eventInfo.setEventName("CompleteFirstGlassJob");
			lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		catch (Exception e)
		{
			log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + lotData.getKey().getLotName());
		}

		// Update Job State
		eventInfo.setEventName("Complete");
		jobData.setJobState("Completed");
		jobData.setLastEventComment(eventInfo.getEventComment());
		ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
	}

	private Lot splitStripLot(EventInfo eventInfo, List<Element> productList, Lot lotData, FirstGlassJob jobData) throws CustomException
	{
		log.debug(String.format("[ELAP]Lot composition start at [%s]", System.currentTimeMillis()));
		log.info(String.format("Lot[%s] is doing split with ProductQuantity[%d]", lotData.getKey().getLotName(), productList.size()));

		// For Split lot, Release Hold Lot
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("ReleaseHold");

			Map<String, String> udfs = new HashMap<String, String>();
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			// delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

			// delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

			// setHoldState
			MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
		}

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotData.getKey().getLotName());

		String newLotName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1).get(0);

		eventInfo.setEventName("Create");
		Map<String, String> udf = new HashMap<String, String>();
		udf.put("FIRSTGLASSFLAG", "N");
		udf.put("JOBNAME", jobData.getJobName());

		Lot childLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLotAndProductProductionType(eventInfo, newLotName, lotData, lotData.getProductionType(), lotData.getCarrierName(), false,
				new HashMap<String, String>(), udf);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList);

		TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(childLot.getKey().getLotName(), productList.size(), productPSequence, udf,
				new HashMap<String, String>());

		// Split Lot
		eventInfo.setEventName("Split");
		lotData.getUdfs().put("JOBNAME", jobData.getJobName());
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

		childLot = MESLotServiceProxy.getLotInfoUtil().getLotData(childLot.getKey().getLotName());

		log.info(String.format("Lot[%s] of ProductQuantity[%f] in Carrier[%s]", childLot.getKey().getLotName(), childLot.getProductQuantity(), childLot.getCarrierName()));
		log.debug(String.format("[ELAP]Lot composition end at [%s]", System.currentTimeMillis()));

		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

			if (lotData.getProductQuantity() > 0)
			{
				// Mother Lot Hold
				eventInfo.setReasonCode("FirstGlassHold");
				eventInfo.setReasonCodeType("FirstGlass");

				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
			}
			else
			{
				// Make Emptied when All Child Lot Split
				if (StringUtil.isNotEmpty(lotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());

					// De-assign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), new HashMap<String, String>());
			}
		}

		return childLot;
	}

	private void startRework(EventInfo eventInfo, Lot lotData, List<ProductU> productUSequence, String factoryName, String reworkFlowName, String reworkFlowVersion, String returnFlowName,
			String returnFlowVersion, String returnOperationName, String returnOperationVersion, String judge, List<Element> operationList) throws CustomException
	{
		// Get Rework First Operation
		ProcessFlowKey reworkProcessFlowKey = new ProcessFlowKey(factoryName, reworkFlowName, reworkFlowVersion);

		String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(reworkProcessFlowKey).getKey().getNodeId();
		String nodeId = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNodeStack, "Normal", "").getKey().getNodeId();

		Node reworkNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);

		String reworkProcessOperationName = reworkNode.getNodeAttribute1();
		String reworkProcessOperationVer = reworkNode.getNodeAttribute2();

		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

		List<ListOrderedMap> alterPathList = null;

		if (StringUtils.equals(judge, "Strip")) //2021-03-05 conditionName (Rework → Strip ) by jhyeom
		{
			alterPathList = PolicyUtil.getAlterProcessOperationForReworkType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), reworkFlowName, 
						reworkFlowVersion, reworkProcessOperationName, reworkProcessOperationVer, "Strip");
		}
		else if (StringUtils.equals(judge, "Rework"))
		{
			alterPathList = PolicyUtil.getAlterProcessOperationForReworkType(lotData.getFactoryName(), returnFlowName, returnFlowVersion, returnOperationName, returnOperationVersion, reworkFlowName,
					reworkFlowVersion, reworkProcessOperationName, reworkProcessOperationVer, "Rework");
		}

		// Check ReworkCount
		if (alterPathList.size() < 1)
		{
			//LOT-0051: Can't find ReworkType. So Can't Count ReworkCount
			throw new CustomException("LOT-0051");
		}
		
		for (Product productE : productList)
		{
			String productName = productE.getKey().getProductName();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

			if (StringUtil.equals(productData.getProductGrade(), "R"))
			{
				for (Element operation : operationList)
				{
					String reworkFlowOper = SMessageUtil.getChildText(operation, "PROCESSOPERATIONNAME", true);
					try
					{
						ReworkProduct reworkProduct = ExtendedObjectProxy.getReworkProductService().selectByKey(false, new Object[]{productName,reworkFlowOper});

						long reworkCountLimit = 0;
						if(StringUtils.isEmpty(reworkProduct.getReworkCountLimit()))
						{
							reworkCountLimit=100;
						}
						else
						{
							reworkCountLimit=Long.parseLong(reworkProduct.getReworkCountLimit());
						}
						if (reworkProduct.getReworkCount() > reworkCountLimit)
							throw new CustomException("PRODUCT-0203", productName,reworkProduct.getReworkCount(),reworkCountLimit);
					}
					catch (greenFrameDBErrorSignal ex)
					{

					}
				}
			}
		}


		List<String> ct_reworkProdList = new ArrayList<String>();

		for (Element operation : operationList)
		{
			String reworkFlowOper = SMessageUtil.getChildText(operation, "PROCESSOPERATIONNAME", true);
			String reworkFlowOperVersion = SMessageUtil.getChildText(operation, "PROCESSOPERATIONVERSION", true);

			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
					lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
					lotData.getProcessOperationVersion(), reworkFlowName, reworkFlowVersion, reworkFlowOper, reworkFlowOperVersion);

			if (sampleLot == null)
			{
				List<String> actualSamplePositionList = new ArrayList<String>();

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

						if (StringUtil.equals(productData.getProductGrade(), "R"))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotData.getKey().getLotName(), productData.getFactoryName(),
									productData.getProductSpecName(), productData.getProductSpecVersion(), productData.getProcessFlowName(), productData.getProcessFlowVersion(),
									productData.getProcessOperationName(), productData.getProcessOperationVersion(), "NA", reworkFlowName, reworkFlowVersion, reworkFlowOper, reworkFlowOperVersion,
									"Y", String.valueOf(RjudgeCountOfProductList), String.valueOf(productData.getPosition()), String.valueOf(productData.getPosition()), "", "Y");

							actualSamplePositionList.add(String.valueOf(productData.getPosition()));

							if (!ct_reworkProdList.contains(productName))
								ct_reworkProdList.add(productName);
						}
					}

					// set SampleLotData(CT_SAMPLELOT)
					ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
							"NA", reworkFlowName, reworkFlowVersion, reworkFlowOper, reworkFlowOperVersion, "Y", "1", "1", "1", "1", CommonUtil.toStringWithoutBrackets(actualSamplePositionList),
							String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", 0, returnFlowName, returnFlowVersion,
							returnOperationName, returnOperationVersion, "");
				}
			}
			else
			{
				throw new CustomException("LOT-0018", sampleLot.get(0).getLotName(), sampleLot.get(0).getToProcessOperationName());
			}
		}

		// makeInRework
		//List<ProductRU> productRUdfs = new ArrayList<ProductRU>();

		/*for (Product product : productList)
		{
			String productName = product.getKey().getProductName();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			if (StringUtil.equals(productData.getProductGrade(), "R"))
		   {
			ProductRU productRU = new ProductRU();
			productRU.setProductName(product.getKey().getProductName());
			productRU.setUdfs(product.getUdfs());
			productRU.setReworkFlag("Y");
			productRUdfs.add(productRU);
			}
		}*/
		List<ProductRU> productRUdfs = MESLotServiceProxy.getLotServiceUtil().setProductRUSequence(ct_reworkProdList);

		Map<String, String> udfs = new HashMap<String, String>();

		MakeInReworkInfo makeInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeInReworkInfo(lotData, eventInfo, lotData.getKey().getLotName(), reworkFlowName, reworkProcessOperationName,
				reworkProcessOperationVer, returnFlowName, returnOperationName, returnOperationVersion, udfs, productRUdfs);

		MESLotServiceProxy.getLotServiceImpl().startRework(eventInfo, lotData, makeInReworkInfo);
	}

	private void updateFirstGlassFlag(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("ChangeFirstGlassFlag");

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("FIRSTGLASSFLAG", "Y");

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	}

	public void lotMultiHold(EventInfo eventInfo, Lot lotData, Map<String, String> udfs) throws CustomException
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
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

		// Set MakeOnHoldInfo
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
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
			String condition = "WHERE lotName = ? AND productState != ? AND productState != ? AND productGrade = ? ORDER BY position ";
			Object[] bindSet = new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, 
							GenericServiceProxy.getConstantMap().Prod_Consumed, GenericServiceProxy.getConstantMap().ProductGrade_R };

			productDatas = ProductServiceProxy.getProductService().select(condition, bindSet);
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

			
			
			ProductU productU = new ProductU();
			productU.setProductName(product.getKey().getProductName());
			
			// Add productPSequence By Product
			ProductUSequence.add(productU);
		}

		return ProductUSequence;
	}
}
