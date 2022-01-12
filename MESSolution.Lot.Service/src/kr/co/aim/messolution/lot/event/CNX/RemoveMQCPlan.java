package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class RemoveMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> mqcList = SMessageUtil.getBodySequenceItemList(doc, "MQCLIST", true);

		if (mqcList != null)
		{
			for (Element mqcE : mqcList)
			{
				String jobName = SMessageUtil.getChildText(mqcE, "JOBNAME", true);
				
				MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanData(jobName);

				if (!planData.getMQCState().equalsIgnoreCase("Suspending") && !planData.getMQCState().equalsIgnoreCase("Created"))
				{
					//MQC-0011: MQC plan must be on hold
					throw new CustomException("MQC-0011");
				}

				String lotName = planData.getLotName();
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

				CommonValidation.checkJobDownFlag(lotData);

				List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);
				// check ProcessInfo
				List<String> productNameList = new ArrayList<>();
				for (Product productA : producList)
				{
					String productName = productA.getKey().getProductName();
					productNameList.add(productName);
				}

				CommonValidation.checkProductProcessInfobyString(productNameList);
				CommonValidation.checkLotState(lotData);
				CommonValidation.checkLotHoldState(lotData);
				CommonValidation.checkLotReworkState(lotData);

				if (!lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_WaitingToLogin))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotProcessState());

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveMQC", getEventUser(), getEventComment(), null, null);

				List<Map<String, Object>> MQCProductList = getMQCProduct(lotData.getKey().getLotName());

				ChangeProcessingInfo(lotName, MQCProductList);

				eventLog.info(" Start delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");
				ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByJobName(jobName);

				eventLog.info(" End delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");

				ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetailByJobName(eventInfo, jobName);

				ExtendedObjectProxy.getMQCPlanService().deleteMQCPlanData(eventInfo, planData);
			}
		}

		return doc;
	}

	private List<Map<String, Object>> getMQCProduct(String lotName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME");
		sql.append("  FROM CT_MQCPLAN M, ");
		sql.append("       CT_MQCPLANDETAIL D, ");
		sql.append("       CT_MQCPLANDETAIL_EXTENDED E, ");
		sql.append("       PRODUCT P ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME ");
		sql.append("   AND M.JOBNAME = E.JOBNAME ");
		sql.append("   AND M.MQCSTATE IN ('Suspending', 'Created')  ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
		sql.append("   AND M.LOTNAME = :LOTNAME ");
		sql.append("   AND M.LOTNAME = P.LOTNAME ");
		sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
		sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("ORDER BY P.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private void ChangeProcessingInfo(String lotName, List<Map<String, Object>> MQCProductList) throws CustomException
	{
		if (MQCProductList.size() > 0)
		{
			for(Map<String, Object> MQCProduct : MQCProductList)
			{
				String productName = ConvertUtil.getMapValueByName(MQCProduct, "PRODUCTNAME");

				StringBuffer sql = new StringBuffer();
				sql.append("UPDATE PRODUCT ");
				sql.append("   SET PROCESSINGINFO = :PROCESSINGINFO ");
				sql.append(" WHERE LOTNAME = :LOTNAME ");
				sql.append("   AND PRODUCTNAME = :PRODUCTNAME ");
				sql.append("   AND PROCESSINGINFO = 'B' ");
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("PRODUCTNAME", productName);
				bindMap.put("LOTNAME", lotName);
				bindMap.put("PROCESSINGINFO", "S");
				
				try
				{
					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
