package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class ComponentInUnit extends AsyncHandler {
	private static Log log = LogFactory.getLog(ComponentInUnit.class);

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String fromSlotPosition = SMessageUtil.getBodyItemValue(doc, "FROMSLOTPOSITION", false);
		String toSlotPosition = SMessageUtil.getBodyItemValue(doc, "TOSLOTPOSITION", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		
		Product productData = null;
		Lot lotData = null;
		String factoryName = "";
		String productSpecName = "";
		String productSpecVersion = "";
		String processFlowName = "";
		String processFlowVersion = "";
		String processOperationName = "";
		String processOperationVersion = "";
		String productionType = "";
		String productRequestName = "";
		
		// 1. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		
		// 2. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if(productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			factoryName = lotData.getFactoryName();
			productSpecName = lotData.getProductSpecName();
			productSpecVersion = lotData.getProductSpecVersion();
			processFlowName = lotData.getProcessFlowName();
			processFlowVersion = lotData.getProcessFlowVersion();
			processOperationName = lotData.getProcessOperationName();
			processOperationVersion = lotData.getProcessOperationVersion();
			productionType = lotData.getProductionType();
			productRequestName = lotData.getProductRequestName();
		}
		else
		{
			productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			factoryName = productData.getFactoryName();
			productSpecName = productData.getProductSpecName();
			productSpecVersion = productData.getProductSpecVersion();
			processFlowName = productData.getProcessFlowName();
			processFlowVersion = productData.getProcessFlowVersion();
			processOperationName = productData.getProcessOperationName();
			processOperationVersion = productData.getProcessOperationVersion();
			productionType = productData.getProductionType();
			productRequestName = productData.getProductRequestName();
			productType = productData.getProductType();
		}

		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentInUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
		dataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId)== "" ? 0 : Integer.valueOf(fromSlotId));
		dataInfo.setToSlotPosition(toSlotPosition);
		dataInfo.setFromSlotPosition(fromSlotPosition);
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductionType(productionType);
		dataInfo.setProductType(productType);
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName(unitName);
		dataInfo.setProductGrade(productGrade);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductRequestName(productRequestName);

		ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		if(StringUtils.equals(machineData.getMachineGroupName(),"FLC"))
		{
			if(checkPostCellLoad()){
				
				updatePostCellLoadInfo(unitName,"FLCPL",productSpecName,processOperationName,productRequestName);
			}
			
		}
		
		//4. Insert to Component Monitor
		
		// Clear ComponentInUnit IN Component Monitor
		try
		{
			try
			{
				List<ComponentMonitor> dataMonitorRemove = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ? AND EVENTNAME = 'ComponentInUnit'", new Object[] { productName });
				if (dataMonitorRemove.size()>0)
				{
					for (ComponentMonitor dataMonitorRemoves : dataMonitorRemove)
					{
						eventLog.info(" Remove Product: " + productName + " IN CT_ComponentMonitor ");
						ExtendedObjectProxy.getComponentMonitorService().remove(eventInfo, dataMonitorRemoves);
					}
				}
			}
			catch(Exception ex)
			{
				eventLog.info(ex.getCause());
			}
			
			// Check Machine Component Monitor Flag
			String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND MONITORTYPE IN ('ComponentMonitor', 'ComponentMonitorLower') AND SUBMACHINENAME = ? AND (PRODUCTSPECNAME = 'ALL' OR PRODUCTSPECNAME = ?) AND PROCESSOPERATIONNAME = ? ";
			
			List<EQPProcessTimeConf> sqlResult = ExtendedObjectProxy.getEQPProcessTimeConfService().select(condition, new Object[]{factoryName, machineName, unitName, productSpecName, processOperationName});
			
			if(!sqlResult.isEmpty() && sqlResult.size() > 0)
			{
				eventLog.info(" ProcessOperationName: "+ processOperationName +" ComponentMonitorFlag: ON ");
				
				for (EQPProcessTimeConf eQPProcessTimeConf : sqlResult)
				{
					if (eQPProcessTimeConf.getMonitorType().equals("ComponentMonitor"))
					{
						try
						{
							List<ComponentMonitor> dataMonitorCheck = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ? AND EVENTNAME = 'ComponentInSubUnit'", new Object[] { productName });
							if (!dataMonitorCheck.isEmpty() && dataMonitorCheck.size()>0)
							{
								for (ComponentMonitor dataMonitorRemoves : dataMonitorCheck)
								{
									ExtendedObjectProxy.getComponentMonitorService().remove(eventInfo, dataMonitorRemoves);
								}
							}
						}
						catch(Exception ex)
						{
							eventLog.info(ex.getCause());
						}
						
						String emailType = eQPProcessTimeConf.getEmailType();
						String ruleTime = eQPProcessTimeConf.getRuleTime();
					
						ComponentMonitor dataMonitor = new ComponentMonitor();

						dataMonitor.setTimeKey(TimeStampUtil.getCurrentEventTimeKey());
						dataMonitor.setProductName(productName);
						dataMonitor.setLotName(lotName);
						dataMonitor.setEventName(eventInfo.getEventName());
						dataMonitor.setEventTime(eventInfo.getEventTime());
						dataMonitor.setEventUser(eventInfo.getEventUser());
						dataMonitor.setFactoryName(factoryName);
						dataMonitor.setProductSpecName(productSpecName);
						dataMonitor.setProductSpecVersion(productSpecVersion);
						dataMonitor.setProcessOperationName(processOperationName);
						dataMonitor.setProcessOperationVersion(processOperationVersion);
						dataMonitor.setMachineName(machineName);
						dataMonitor.setMaterialLocationName(unitName);
						dataMonitor.setEmailType(emailType);
						dataMonitor.setRuleTime(ruleTime);
						dataMonitor.setSendFlag("N");

						eventLog.info(" Insert Product: " + productName + " into CT_ComponentMonitor ");
						ExtendedObjectProxy.getComponentMonitorService().create(eventInfo, dataMonitor);
					}
					else if (eQPProcessTimeConf.getMonitorType().equals("ComponentMonitorLower"))
					{
						try
						{
							List<ComponentMonitor> dataMonitorCheck = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ? AND EVENTNAME = 'ComponentInSubUnit'", new Object[] { productName });
						}
						catch(Exception ex)
						{
							eventLog.info(ex.getCause());
							String emailType = eQPProcessTimeConf.getEmailType();
							String ruleTime = eQPProcessTimeConf.getRuleTime();
						
							ComponentMonitor dataMonitor = new ComponentMonitor();
	
							dataMonitor.setTimeKey(TimeStampUtil.getCurrentEventTimeKey());
							dataMonitor.setProductName(productName);
							dataMonitor.setLotName(lotName);
							dataMonitor.setEventName(eventInfo.getEventName());
							dataMonitor.setEventTime(eventInfo.getEventTime());
							dataMonitor.setEventUser(eventInfo.getEventUser());
							dataMonitor.setFactoryName(factoryName);
							dataMonitor.setProductSpecName(productSpecName);
							dataMonitor.setProductSpecVersion(productSpecVersion);
							dataMonitor.setProcessOperationName(processOperationName);
							dataMonitor.setProcessOperationVersion(processOperationVersion);
							dataMonitor.setMachineName(machineName);
							dataMonitor.setMaterialLocationName(unitName);
							dataMonitor.setEmailType(emailType);
							dataMonitor.setRuleTime(ruleTime);
							dataMonitor.setSendFlag("N");
	
							eventLog.info(" Insert Product: " + productName + " into CT_ComponentMonitor ");
							ExtendedObjectProxy.getComponentMonitorService().create(eventInfo, dataMonitor);
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
	}
	  private boolean checkPostCellLoad()
	  {
			String sql="SELECT*FROM ENUMDEFVALUE WHERE ENUMNAME='PostCellLoadInfo' and ENUMVALUE='PostCellLoadInfo' ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			List<Map<String,Object>> resultList =null;
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
			if(resultList!=null&& resultList.size()>0)
			{
				return true;
				
			}
			return false;
	    }
	    private void updatePostCellLoadInfo(String machineName,String portName,String productSpec,String processOperation,String productRequest) throws CustomException
		{
			String sql="SELECT PRODUCTSPEC,PROCESSOPERATION, PRODUCTREQUEST FROM CT_POSTCELLLOADINFO WHERE  MACHINENAME=:MACHINENAME AND PORTNAME=:PORTNAME ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("PORTNAME", portName);
			List<Map<String,Object>> resultList =null;
			try
			{
			   resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
			}
			catch (Exception ex)
			{
			  log.info("updatePostCellLoadInfo File");
			}
			if(resultList!=null&& resultList.size()>0)
			{
				String loadProductSpecName=resultList.get(0).get("PRODUCTSPEC").toString();
				String loadProcessOperationName=resultList.get(0).get("PROCESSOPERATION").toString();
				String loadProductRequestName=resultList.get(0).get("PRODUCTREQUEST").toString();
				if(!loadProductSpecName.equals(productSpec)||!loadProcessOperationName.equals(processOperation)||!loadProductRequestName.equals(productRequest)){
					
					StringBuffer updateSql = new StringBuffer();
					updateSql.append("UPDATE CT_POSTCELLLOADINFO SET PRODUCTSPEC=?, ");
					updateSql.append(" PROCESSOPERATION=?, PRODUCTREQUEST=?  ");
					updateSql.append(" WHERE  MACHINENAME=? AND PORTNAME=?  ");
					List<Object[]> updatePostcellLoadInfo = new ArrayList<Object[]>();
					List<Object> loadInfo = new ArrayList<Object>();
					loadInfo.add(productSpec);
					loadInfo.add(processOperation);
					loadInfo.add(productRequest);
					loadInfo.add(machineName);
					loadInfo.add(portName);
					updatePostcellLoadInfo.add(loadInfo.toArray());
					try
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(updateSql.toString(), updatePostcellLoadInfo);
					}
					catch (Exception ex)
					{
					  log.info("updatePostCellLoadInfo File");
					}
					
				}
				
			}else{
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("INSERT INTO CT_POSTCELLLOADINFO  ");
				insertSql.append("(PRODUCTSPEC,PROCESSOPERATION,PRODUCTREQUEST,MACHINENAME,PORTNAME) ");
				insertSql.append(" VALUES  ");
				insertSql.append("(?,?,?,?,?) ");
				List<Object[]> insertPostcellLoadInfo = new ArrayList<Object[]>();
				List<Object> insertloadInfo = new ArrayList<Object>();
				insertloadInfo.add(productSpec);
				insertloadInfo.add(processOperation);
				insertloadInfo.add(productRequest);
				insertloadInfo.add(machineName);
				insertloadInfo.add(portName);
				insertPostcellLoadInfo.add(insertloadInfo.toArray());
				try
				{
					MESLotServiceProxy.getLotServiceUtil().updateBatch(insertSql.toString(), insertPostcellLoadInfo);
				}
				catch (Exception ex)
				{
				  log.info("insertPostCellLoadInfo File");
				}
			}
	 }
}
