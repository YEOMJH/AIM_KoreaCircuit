package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

public class FirstGlassPhotoQueueTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(FirstGlassPhotoQueueTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			monitorQueueTime();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	
	public void monitorQueueTime() throws CustomException
	{
		//Validation FirstGlass
		StringBuilder firstGlasssql = new StringBuilder();
		firstGlasssql.append("SELECT F.LOTNAME, ");
		firstGlasssql.append("     F.JOBNAME, ");
		firstGlasssql.append("     F.FACTORYNAME, ");
		firstGlasssql.append("     F.PRODUCTSPECNAME, ");
		firstGlasssql.append("     F.PRODUCTSPECVERSION, ");
		firstGlasssql.append("     F.PROCESSFLOWNAME, ");
		firstGlasssql.append("     F.PROCESSFLOWVERSION, ");
		firstGlasssql.append("     F.PROCESSOPERATIONNAME, ");
		firstGlasssql.append("     F.PROCESSOPERATIONVERSION, ");
		firstGlasssql.append("     F.MACHINENAME, ");
		firstGlasssql.append("     M.MACHINEGROUPNAME ");
		firstGlasssql.append("     FROM CT_FIRSTGLASSJOB F, MACHINESPEC M ");
		firstGlasssql.append("     WHERE F.MACHINENAME = M.MACHINENAME ");
		firstGlasssql.append("     AND M.MACHINEGROUPNAME = 'Photo' ");
		firstGlasssql.append("     AND F.JOBSTATE ='Reserved' ");		
		List<Map<String, Object>> firstGlassresult = new ArrayList<Map<String, Object>>();		
//		try
//		{
//			firstGlassresult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(firstGlasssql.toString(), new HashMap<String, Object>());
//		}
//		catch (FrameworkErrorSignal fe)
//		{
//			firstGlassresult = null;
//			throw new CustomException("SYS-9999", fe.getMessage());
//			// return;
//		}
		
		firstGlassresult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(firstGlasssql.toString(), new HashMap<String, Object>());
    	if(firstGlassresult ==null || firstGlassresult.size()==0)
        {
        	log.info("FirstGlassPhotoQueueTimer: FirstGlassJob  Data Information is Empty!!");
        	return ;
        }
		
		if(firstGlassresult.size()>0 || firstGlassresult !=null){
			
			// Check Q-Time
			StringBuilder sql = new StringBuilder();
			sql.append("WITH QUEUETIMELIST ");
			sql.append("     AS (SELECT Q.LOTNAME, ");
			sql.append("                Q.PRODUCTNAME, ");
			sql.append("                Q.FACTORYNAME, ");
			sql.append("                Q.PROCESSFLOWNAME, ");
			sql.append("                Q.PROCESSOPERATIONNAME, ");
			sql.append("                Q.TOFACTORYNAME, ");
			sql.append("                Q.TOPROCESSFLOWNAME, ");
			sql.append("                Q.TOPROCESSOPERATIONNAME, ");
			sql.append("                Q.QUEUETIMESTATE, ");
			sql.append("                Q.ENTERTIME, ");
			sql.append("                Q.WARNINGDURATIONLIMIT, ");
			sql.append("                (Q.ENTERTIME + Q.WARNINGDURATIONLIMIT / 24) WARNINGTIME, ");
			sql.append("                Q.INTERLOCKDURATIONLIMIT, ");
			sql.append("                (Q.ENTERTIME + Q.INTERLOCKDURATIONLIMIT / 24) INTERLOCKTIME, ");
			sql.append("                Q.RESOLVETIME, ");
			sql.append("                SYSDATE CURRENTTIME ");
			sql.append("           FROM CT_PRODUCTQUEUETIME Q, CT_FIRSTGLASSJOB F, MACHINESPEC M ");
			sql.append("          WHERE 1 = 1 ");
			sql.append("            AND Q.ENTERTIME IS NOT NULL ");
			sql.append("            AND Q.QUEUETIMESTATE NOT IN ('Resolved', 'Exit', 'Interlocked') ");
			sql.append("             AND Q.LOTNAME = F.LOTNAME ");
			sql.append("             AND Q.TOPROCESSOPERATIONNAME = F.PROCESSOPERATIONNAME ");
			sql.append("            AND F.MACHINENAME = M.MACHINENAME ");
			sql.append("            AND Q.PROCESSFLOWNAME = F.PROCESSFLOWNAME ");
			sql.append("            AND M.MACHINEGROUPNAME = 'Photo' ");
			sql.append("            AND  F.JOBSTATE ='Reserved') ");			
			sql.append("SELECT DISTINCT ");
			sql.append("       R.PRODUCTNAME, ");
			sql.append("       P.LOTNAME, ");
			sql.append("       R.FACTORYNAME, ");
			sql.append("       R.PROCESSFLOWNAME, ");
			sql.append("       R.PROCESSOPERATIONNAME, ");
			sql.append("       R.TOFACTORYNAME, ");
			sql.append("       R.TOPROCESSFLOWNAME, ");
			sql.append("       R.TOPROCESSOPERATIONNAME, ");
			sql.append("       R.TOQUEUETIMESTATE, ");
			sql.append("       R.QUEUETIMESTATE ");
			sql.append("  FROM (SELECT DISTINCT ");
			sql.append("               L.PRODUCTNAME, ");
			sql.append("               L.LOTNAME, ");
			sql.append("               L.FACTORYNAME, ");
			sql.append("               L.PROCESSFLOWNAME, ");
			sql.append("               L.PROCESSOPERATIONNAME, ");
			sql.append("               L.TOFACTORYNAME, ");
			sql.append("               L.TOPROCESSFLOWNAME, ");
			sql.append("               L.TOPROCESSOPERATIONNAME, ");
			sql.append("               CASE ");
			sql.append("                  WHEN L.CURRENTTIME > L.INTERLOCKTIME THEN 'Interlocked' ");
			sql.append("                  WHEN L.CURRENTTIME > L.WARNINGTIME THEN 'Warning' ");
			sql.append("                  ELSE NULL ");
			sql.append("               END AS TOQUEUETIMESTATE, ");
			sql.append("               L.QUEUETIMESTATE ");
			sql.append("          FROM QUEUETIMELIST L ");
			sql.append("         WHERE 1 = 1) R, ");
			sql.append("       LOT A, ");
			sql.append("       PRODUCT P ");
			sql.append(" WHERE R.TOQUEUETIMESTATE IS NOT NULL ");
			sql.append("   AND A.LOTNAME = P.LOTNAME ");
			sql.append("   AND P.PRODUCTNAME = R.PRODUCTNAME ");
			sql.append("   AND A.LOTSTATE = 'Released' ");
			sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
			sql.append("ORDER BY P.LOTNAME, R.PRODUCTNAME ");

			List<String> lotNameList = new ArrayList<String>();
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

			try
			{
				result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), new HashMap<String, Object>());
			}
			catch (FrameworkErrorSignal fe)
			{
				result = null;
				throw new CustomException("SYS-9999", fe.getMessage());
				// return;
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MES", "FirstGlass Q-time monitoring service", null, null);
			
			for (Map<String, Object> row : result)
			{
				String productName = CommonUtil.getValue(row, "PRODUCTNAME");
				String lotName = CommonUtil.getValue(row, "LOTNAME");
				String factoryName = CommonUtil.getValue(row, "FACTORYNAME");
				String processFlowName = CommonUtil.getValue(row, "PROCESSFLOWNAME");
				String processOperationName = CommonUtil.getValue(row, "PROCESSOPERATIONNAME");
				String toFactoryName = CommonUtil.getValue(row, "TOFACTORYNAME");
				String toProcessFlowName = CommonUtil.getValue(row, "TOPROCESSFLOWNAME");
				String toProcessOperationName = CommonUtil.getValue(row, "TOPROCESSOPERATIONNAME");
				String toQueueTimeState = CommonUtil.getValue(row, "TOQUEUETIMESTATE");
				String queueTimeState = CommonUtil.getValue(row, "QUEUETIMESTATE");


				updateQueueTime(eventInfo, productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName, toQueueTimeState);

				if (!lotNameList.contains(lotName))
				{
					lotNameList.add(lotName);

					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

					// SetEvent
					if (!StringUtils.equals(queueTimeState, toQueueTimeState) &&
						(StringUtils.equals(toQueueTimeState, GenericServiceProxy.getConstantMap().QTIME_STATE_OVER) ||
						 StringUtils.equals(toQueueTimeState, GenericServiceProxy.getConstantMap().QTIME_STATE_WARN)))
					{
						if (StringUtils.equals(toQueueTimeState, GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
						{
							eventInfo.setEventName("Interlock");
						}
						else if (StringUtils.equals(toQueueTimeState, GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
						{
							eventInfo.setEventName("Warn");
						}

						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						MESLotServiceProxy.getLotServiceImpl().setEventForce(eventInfo, lotData);
					}

					// Execute QueueTime Action
					doActQueueTime(lotName, productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
				}
			}
		}
	}


	private void updateQueueTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName,
			String processOperationName, String toFactoryName, String toProcessFlowName, String toProcessOperationName, String toQueueTimeState) throws CustomException
	{
		try
		{
			// isolation
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			{
				// lock target Q-time
				ProductQueueTime QTimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
						new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

				if (toQueueTimeState.equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER) && !QTimeData.getQueueTimeState().equals(toQueueTimeState))
				{
					ExtendedObjectProxy.getProductQTimeService().lockQTime(eventInfo, productName, factoryName, processFlowName,
							processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
				}
				else if (toQueueTimeState.equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) && !QTimeData.getQueueTimeState().equals(toQueueTimeState))
				{
					ExtendedObjectProxy.getProductQTimeService().warnQTime(eventInfo, productName, factoryName, processFlowName,
							processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
				}
				else
				{
					// ignore
				}
			}
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(fe);
		}
		catch (Exception ex)
		{
			// safety gear
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(ex);
		}
		// unlock Q-time
	}


	private void doActQueueTime(String lotName, String productName, String factoryName, String processFlowName, String processOperationName,
			String toFactoryName, String toProcessFlowName, String toProcessOperationName) throws CustomException
	{
		try
		{
			// isolation
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			{
				// lock target Lot
				Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
				ProductQueueTime QTimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(false,
						new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

				if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
				{
					// rework
					if (lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_WaitingToLogin)
							&& lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_NotInRework))
					{
						makeInRework(lotData);
					}
				}
				else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
				{
					// hold
					if (lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_WaitingToLogin))
					{
						// makeOnHold(lotData);
						log.warn(String.format("Q-Time[%s %s %s %s %s %s %s] would be expired soon", QTimeData.getLotName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(),
								QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(), QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName()));
					}
					else
					{
						// reserve future action on running
					}
				}
			}
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(fe);
		}
		catch (Exception ex)
		{
			// safety gear
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(ex);
		}

		// unlock Lot
	}


	private void makeInRework(Lot lotData)
		throws CustomException
	{
		//generate hold request message
		Element eleBody = new Element(SMessageUtil.Body_Tag);
		{
			Element eleLotName = new Element("LOTNAME");
			eleLotName.setText(lotData.getKey().getLotName());
			eleBody.addContent(eleLotName);
			
			Element eleReworkFlowName = new Element("REWORKFLOWNAME");
			//eleReworkFlowName.setText("");
			eleBody.addContent(eleReworkFlowName);
			
			Element eleReworkOperationNameN = new Element("REWORKOPERATIONNAME");
			//eleReworkOperationNameN.setText("");
			eleBody.addContent(eleReworkOperationNameN);
			
			Element eleReturnFlowName = new Element("RETURNFLOWNAME");
			//eleReturnFlowName.setText("");
			eleBody.addContent(eleReturnFlowName);
			
			Element eleReturnOperationName = new Element("RETURNOPERATIONNAME");
			//eleReturnOperationName.setText("");
			eleBody.addContent(eleReturnOperationName);
			
			//move in accordance with post location
			List<ListOrderedMap> alterPathList = 
					PolicyUtil.getAlterProcessOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
			String reworkFactoryName = "";
			String reworkFlowName = "";
			String reworkOperationName = "";
			String returnFlowName = "";
			String returnOperationName = "";
			{
				for (ListOrderedMap alterPath : alterPathList)
				{
					
					if (CommonUtil.getValue(alterPath, "CONDITIONNAME").equals("Rework")
							&& CommonUtil.getValue(alterPath, "CONDITIONVALUE").equals("Auto"))
					{
						reworkFactoryName =  CommonUtil.getValue(alterPath, "FACTORYNAME");
						reworkFlowName = CommonUtil.getValue(alterPath, "TOPROCESSFLOWNAME");
						reworkOperationName = CommonUtil.getValue(alterPath, "TOPROCESSOPERATIONNAME");
						returnFlowName = CommonUtil.getValue(alterPath, "TORETURNPROCESSFLOWNAME");
						returnOperationName = CommonUtil.getValue(alterPath, "TORETURNOPERATIONNAME");
						
						eleReworkFlowName.setText(reworkFlowName);
						eleReworkOperationNameN.setText(reworkOperationName);
						eleReturnFlowName.setText(returnFlowName.isEmpty()?lotData.getProcessFlowName():"");
						eleReturnOperationName.setText(returnOperationName.isEmpty()?lotData.getProcessOperationName():"");
						
						break;
					}
				}
			}
			
			Element eleReworkOperationList = new Element("REWORKOPERLIST");
			eleBody.addContent(eleReworkOperationList);
			{
				List<Node> nodeList = ProcessFlowServiceProxy.getNodeService().getNodeTagList(GenericServiceProxy.getConstantMap().Node_ProcessOperation,
																								reworkFactoryName, reworkFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				
				for (Node node : nodeList)
				{
					Element eleReworkOperation = new Element("REWORKOPER");
					eleReworkOperationList.addContent(eleReworkOperation);
					
					Element eleReworkOperationName = new Element("REWORKOPERNAME");
					eleReworkOperationName.setText(node.getNodeAttribute1());
					eleReworkOperation.addContent(eleReworkOperationName);
				}
			}
		}
		
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
			
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "StartRework",
					"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
					targetSubject,
					"MES",
					"Queue Time auto rework");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "CNXSender");
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}
	
/*	
	private void makeOnHold(Lot lotData)
		throws CustomException
	{
		//generate hold request message
		Element eleBody = new Element(SMessageUtil.Body_Tag);
		{
			Element eleLotList = new Element("LOTLIST");
			eleBody.addContent(eleLotList);
			
			Element eleLot = new Element("LOT");
			eleLotList.addContent(eleLot);
			
			Element eleLotName = new Element("LOTNAME");
			eleLotName.setText(lotData.getKey().getLotName());
			eleLot.addContent(eleLotName);
			
			Element eleReasonCodeType = new Element("REASONCODETYPE");
			eleReasonCodeType.setText("HOLD");
			eleLot.addContent(eleReasonCodeType);
			
			Element eleReasonCode = new Element("REASONCODE");
			eleReasonCode.setText("Hold01");
			eleLot.addContent(eleReasonCode);
		}
		
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
			
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "HoldLot",
					"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
					targetSubject,
					"MES",
					"Queue Time hold request");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "TibSender");
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}*/
}
