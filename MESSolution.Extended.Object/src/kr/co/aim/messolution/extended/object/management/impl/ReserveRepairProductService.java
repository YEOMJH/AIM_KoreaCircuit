package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.management.data.Product;

public class ReserveRepairProductService extends CTORMService<ReserveRepairProduct> {
	
	public static Log logger = LogFactory.getLog(ReserveRepairProductService.class);
	
	private final String historyEntity = "ReserveRepairProductHist";
	
	public List<ReserveRepairProduct> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ReserveRepairProduct> result = super.select(condition, bindSet, ReserveRepairProduct.class);
		
		return result;
	}
	
	public ReserveRepairProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ReserveRepairProduct.class, isLock, keySet);
	}
	
	public ReserveRepairProduct create(EventInfo eventInfo, ReserveRepairProduct dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveRepairProduct dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveRepairProduct modify(EventInfo eventInfo, ReserveRepairProduct dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
//	public void removeReserveRepairProductData(EventInfo eventInfo, Lot lotData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
//	{
//		String lotName = lotData.getKey().getLotName();
//		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
//		
//		for (Product productData : productDataList) 
//		{
//			ReserveRepairProduct dataInfo = this.selectByKey(false, new Object[] { productData.getKey().getProductName() });
//			
//			this.remove(eventInfo, dataInfo);
//		}
//	}
	
//	public List<ReserveRepairProduct> getReserveRepairProductData(Lot lotData) throws CustomException
//	{
//		try
//		{
//			String condition = "WHERE 1 = 1 "
//							 + "  AND PROCESSFLOWNAME = ? "
//							 + "  AND PROCESSFLOWVERSION = ? "
//							 + "  AND PROCESSOPERATIONNAME = ? "
//							 + "  AND PROCESSOPERATIONVERSION = ? "
//							 + "  AND PRODUCTNAME IN(SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = ?)";
//			
//			return this.select(condition, new Object[] { lotData.getUdfs().get("BEFOREFLOWNAME"), "00001",
//														 lotData.getUdfs().get("BEFOREOPERATIONNAME"), "00001",
//														 lotData.getKey().getLotName() });
//		}
//		catch(Exception ex)
//		{
//			logger.info("ReserveRepairProduct data is not exists.");
//		}
//		
//		return new ArrayList<ReserveRepairProduct>();
//	}
	
	/**
	 * 
	 * CO-INS-0017-01
	 * Reserve Repair
	 * 
	 * @author aim_dhko
	 * @return 
	 */
//	@SuppressWarnings("unchecked")
//	public Node getReserveRepairNodeData(Lot lotData)
//	{
//		// Get ReserveRepair Data
//		StringBuilder sql = new StringBuilder();
//		sql.append("SELECT RP.TOPROCESSFLOWNAME, RP.TOPROCESSFLOWVERSION, RP.TOPROCESSOPERATIONNAME, RP.TOPROCESSOPERATIONVERSION, PA.RETURNPROCESSFLOWNAME, PA.RETURNPROCESSFLOWVERSION, PA.RETURNOPERATIONNAME, PA.RETURNOPERATIONVERSION ");
//		sql.append("FROM TFOPOLICY TP, POSALTERPROCESSOPERATION PA, ");
//		sql.append("( ");
//		sql.append("    SELECT DISTINCT RP.LOTNAME, RP.TOMAINPROCESSFLOWNAME AS PROCESSFLOWNAME, RP.TOMAINPROCESSFLOWVERSION AS PROCESSFLOWVERSION, ");
//		sql.append("        RP.TOMAINPROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, RP.TOMAINPROCESSOPERATIONVERSION AS PROCESSOPERATIONVERSION, ");
//		sql.append("        RP.TOPROCESSFLOWNAME, RP.TOPROCESSFLOWVERSION, RP.TOPROCESSOPERATIONNAME, RP.TOPROCESSOPERATIONVERSION ");
//		sql.append("    FROM CT_RESERVEREPAIRPRODUCT RP, LOT L ");
//		sql.append("    WHERE 1 = 1 ");
//		sql.append("      AND L.LOTNAME = :LOTNAME ");
//		sql.append("      AND RP.LOTNAME = L.LOTNAME ");
//		sql.append("      AND RP.TOMAINPROCESSFLOWNAME = L.PROCESSFLOWNAME ");
//		sql.append("      AND RP.TOMAINPROCESSFLOWVERSION = L.PROCESSFLOWVERSION ");
//		sql.append("      AND RP.TOMAINPROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME ");
//		sql.append("      AND RP.TOMAINPROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION ");
//		sql.append(") RP ");
//		sql.append("WHERE 1 = 1 ");
//		sql.append("  AND TP.FACTORYNAME = :FACTORYNAME ");
//		sql.append("  AND TP.PROCESSFLOWNAME = RP.PROCESSFLOWNAME ");
//		sql.append("  AND TP.PROCESSFLOWVERSION = RP.PROCESSFLOWVERSION ");
//		sql.append("  AND TP.PROCESSOPERATIONNAME = RP.PROCESSOPERATIONNAME ");
//		sql.append("  AND TP.PROCESSOPERATIONVERSION = RP.PROCESSOPERATIONVERSION ");
//		sql.append("  AND PA.CONDITIONNAME = 'Repair' ");
//		sql.append("  AND PA.TOPROCESSFLOWNAME = RP.TOPROCESSFLOWNAME ");
//		sql.append("  AND PA.TOPROCESSFLOWVERSION = RP.TOPROCESSFLOWVERSION ");
//		sql.append("  AND PA.TOPROCESSOPERATIONNAME = RP.TOPROCESSOPERATIONNAME ");
//		sql.append("  AND PA.TOPROCESSOPERATIONVERSION = RP.TOPROCESSOPERATIONVERSION ");
//		sql.append("  AND PA.CONDITIONID = TP.CONDITIONID ");
//		
//		Map<String, String> bindMap = new HashMap<String, String>();
//		bindMap.put("LOTNAME", lotData.getKey().getLotName());
//		bindMap.put("FACTORYNAME", lotData.getFactoryName());
//		
//		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
//		if(sqlResult == null || sqlResult.size() == 0)
//		{
//			return null;
//		}
//		
//		String processFlowName = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWNAME");
//		String processFlowVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWVERSION");
//		String processOperationName = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSOPERATIONNAME");
//		String processOperationVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSOPERATIONVERSION");
//		
//		Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), processFlowName, processFlowVersion, "ProcessOperation", processOperationName, processOperationVersion);
//		
//		Map<String, String> udfs = new HashMap<String, String>();
//		udfs.put("RETURNPROCESSFLOWNAME", ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWNAME"));
//		udfs.put("RETURNPROCESSFLOWVERSION", ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWVERSION"));
//		udfs.put("RETURNOPERATIONNAME", ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONNAME"));
//		udfs.put("RETURNOPERATIONVERSION", ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONVERSION"));
//		
//		nextNode.setUdfs(udfs);
//		
//		return nextNode;
//	}
	
//	public void removeReserveRepairData(EventInfo eventInfo, Lot lotData) 
//		throws CustomException
//	{
//		String condition = "LOTNAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? ";
//		List<ReserveRepairProduct> dataInfoList = new ArrayList<ReserveRepairProduct>();
//		
//		try
//		{
//			dataInfoList = this.select(condition, new Object[] { lotData.getKey().getLotName(), 
//																 lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
//																 lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });
//			
//			for (ReserveRepairProduct dataInfo : dataInfoList) 
//			{
//				this.remove(eventInfo, dataInfo);
//			}
//		}
//		catch (Exception e)
//		{
//			dataInfoList = null;
//		}
//	}
}
