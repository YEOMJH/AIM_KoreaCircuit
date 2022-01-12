package kr.co.aim.messolution.lot.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import sun.net.www.content.text.Generic;
import sun.reflect.generics.factory.GenericsFactory;
import kr.co.aim.greentrack.port.management.data.Port;

public class LotProcessStarted extends AsyncHandler {

	private static Log log = LogFactory.getLog(LotProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		boolean isSorter = false;

		String lotName     = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String cstName     = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName  = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		// Sorter EQP - MachineGroupName : SORTER
		if (StringUtils.equalsIgnoreCase(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			isSorter = true;
		
		if(machineData.getUdfs().get("OPERATIONMODE").equals(constMap.SORT_OPERATIONMODE))
			isSorter = true;

		// Get Lot Data
		Lot lotData = this.getLotData(isSorter, lotName, machineName, portName, cstName);
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			
			if (lotData != null)
			{
				if (!StringUtils.equals(lotName, lotData.getKey().getLotName()))
				{
					// Different LotID from EQP[{0}] and CST[{1}:{2}]
					throw new CustomException("LOT-0008", lotName, lotData.getKey().getLotName(), cstName);
				}
				
				//Set download slot map
				eventInfo.setEventComment(String.format("LotProcessStarted WillRunSlot :%s", lotData.getUdfs().get("SLOTSEL")));
			}
		    
			String	machineRecipeName = "";
			
			// Check Available EQP
			//Pass sorter recipe 2020-08-21 Modify by ghhan 
			if(!isSorter)
			{
				if(machineData.getFactoryName().equals("TP") && CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
				{
					List<Product> productList = MESProductServiceProxy.getProductServiceUtil().allUnScrappedProductsByLot(lotData.getKey().getLotName());
					
					String productOffset = productList.get(0).getUdfs().get("OFFSET").toString();
					ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
					
					if (((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isNotEmpty(productOffset))
					{
						TPOffsetAlignInfo offsetInfo = ExtendedObjectProxy.getTPOffsetAlignInfoService().selectByKey(false, new Object[] { productOffset, operationData.getKey().getProcessOperationName(),
								operationData.getKey().getProcessOperationVersion(), machineData.getKey().getMachineName() });

						machineRecipeName = offsetInfo.getRecipeName();
					}
				}
				else
				{
					machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
				}
				
			
				// Check Recipe
				if (!StringUtils.equals(machineRecipeName, recipeName))
				{	
					// How to handle? Error or Hold after Track In
					log.info("Wrong Recipe. EQP:" + recipeName + ",MES:" + machineRecipeName);
				}
			}
			
			if(lotData == null)
			{
				throw new CustomException("CARRIER-9002", cstName);
			}
			
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
			
			// Check PVD Idel Time (use only Online Remote)
			if (processFlowData.getProcessFlowType().equals(constMap.ProcessFlowType_Main) && StringUtil.in(lotData.getProductionType(), "P", "E")
					&& StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_CSTCleaner))
			{
				MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
				String glassProcessTime = machineSpecData.getUdfs().get("GLASSPROCESSTIME");
				String transferTime = machineSpecData.getUdfs().get("TRANSFERTIME");

				Node nextNode = PolicyUtil.getNextOperation(lotData);
				ProcessOperationSpec poSpecData = CommonUtil.getProcessOperationSpec(nextNode.getFactoryName(), nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

				if (poSpecData.getDetailProcessOperationType().equals("PVD"))
					checkPVDIdelTimeOver(lotData.getProductQuantity(), glassProcessTime, transferTime);
				else
					log.info("▶Next Operation is not PVD.");
			}
			
			//if (!StringUtil.equals(processFlowData.getProcessFlowType(), "Sort"))
			//	ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotData.getKey().getLotName(), machineName);
			
			if (StringUtil.equals(processFlowData.getProcessFlowType(), "BackUp"))
			{
				MESLotServiceProxy.getLotServiceUtil().isBackUpTrackIn(lotData, eventInfo);
			}
			else if (!StringUtil.equals(processFlowData.getProcessFlowType(), "Sort"))
			{
				// Reserve Hold QueueTime Over Lot
				ExtendedObjectProxy.getProductQTimeService().reserveHoldWhenQTimeOver(lotData, eventInfo, "RH-ExceedQtime", "ReserveHoldLot", "SystemReserveHold LotProcessStart after ExceedQtime");
				ExtendedObjectProxy.getProductQTimeService().exitQTimeByLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			}
			
			if ((processFlowData.getProcessFlowType().equals("Inspection") || processFlowData.getProcessFlowType().equals("Sample")) && !processFlowData.getProcessFlowType().equals("MQC"))
			{
				ExtendedObjectProxy.getSampleProductService().setSampleProductTrackInEvent(eventInfo,lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
			}
			
			Map<String, String> udfs = new HashMap<>();
			udfs.put("PORTNAME", portData.getKey().getPortName());
			udfs.put("PORTTYPE", CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"));
			udfs.put("PORTUSETYPE", CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"));
			
			if (lotData.getFactoryName().equals("POSTCELL"))
				udfs.put("MAINMACHINENAME", machineName);

			// Skip Recipe Check for Sorter
			if (!isSorter)
			{
				lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
				recipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);

				StringBuffer sqlBuffer = new StringBuffer("");
				sqlBuffer.append(" SELECT R.LASTAPPROVETIME,R.lastchangetime");
				sqlBuffer.append("   FROM CT_RECIPE R ");
				sqlBuffer.append("  WHERE ROWNUM=1 ");
				sqlBuffer.append("	 AND R.machineName= ? ");
				sqlBuffer.append("    AND R.recipename = ? ");
				sqlBuffer.append("    AND R.recipetype = 'MAIN' ");
				sqlBuffer.append("    AND R.recipestate = 'Approved'");
				sqlBuffer.append("  order by R.LASTAPPROVETIME desc ");

				String sqlStmt = sqlBuffer.toString();
				Object[] bindSet = new String[] { machineName, recipeName };
				List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);

				if (sqlResult.size() > 0)
				{
					ListOrderedMap temp = sqlResult.get(0);
					Date LASTAPPROVEDTIME1 = (Date) CommonUtil.getDateValue(temp, "LASTAPPROVETIME");

					if (LASTAPPROVEDTIME1 != null)
					{
						String LASTAPPROVEDTIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(LASTAPPROVEDTIME1);
						udfs.put("LASTAPPROVEDTIME", LASTAPPROVEDTIME);
					}
					else
					{
						udfs.put("LASTAPPROVEDTIME", "");
					}
				}
			}

			if (StringUtils.equals(lotData.getUdfs().get("JOBDOWNFLAG"), "Y"))
				udfs.put("JOBDOWNFLAG", "");

			// TrackIn
			List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
			MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, udfs);

			eventInfo.setEventName("TrackIn");
			eventInfo.setEventComment("LotProcessStarted WillRunSlot: " + lotData.getUdfs().get("SLOTSEL"));
			
			Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

			// change portInfo
			MESPortServiceProxy.getPortServiceUtil().portProcessing(eventInfo, machineName, portName);

			if (!cstName.isEmpty())
			{
				Durable cstData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cstName);
				if (cstData.getUdfs().get("CANCELINFOFLAG").equals("Y"))
				{
					kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
					Map<String, String> durableUdfs = new HashMap<>();
					durableUdfs.put("CANCELINFOFLAG", "");
					setEventInfoDur.setUdfs(durableUdfs);
					DurableServiceProxy.getDurableService().setEvent(cstData.getKey(), eventInfo, setEventInfoDur);
				}
			}

			// Delete CT_LotFutureAction
			try
			{
				MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureAction(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "1");
			}
			catch (CustomException e)
			{
				eventLog.info("Can't delete CtLotFutureAction:Abort Info not exist");
			}

			updateReservedLotState(lotName, machineName, lotData, eventInfo);
		}
		catch (CustomException ce)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			if (lotData != null && !(StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run) || (StringUtils.indexOf(ce.errorDef.getEng_errorMessage(),
					"Lot.MakeLoggedIn.makeLoggedIn") > 0 && StringUtils.indexOf(ce.errorDef.getEng_errorMessage(), "(Released,RUN,") > 0)))
			{
				try
				{
					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("HD100");
					
					eventInfo.setEventComment("Lot is really track in by EQP,but failtrackin on OIC,please confirm don't doublerun!!");

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				}
				catch (Exception ee)
				{
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				}
			}

			throw new CustomException("SYS-0010", ce.errorDef.getEng_errorMessage());
		}
		catch (Exception e)
		{

			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			if (lotData != null && !StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
			{
				try
				{
					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("HD100");

					eventInfo.setEventComment("Lot is really track in by EQP,but failtrackin on OIC,please confirm don't doublerun!!");
					
					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				}
				catch (Exception ee)
				{
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				}
			}

			throw new CustomException("SYS-0010", e.getMessage());
		}
	}
	
	private void checkPVDIdelTimeOver(double glassQty,String glassProcessTime,String transferTime) throws CustomException
	{
		if (!this.checkIsNumeric(glassProcessTime, transferTime))
		{
			log.info(String.format("checkPVDIdelTimeOver: glassProcessTime[%s] or transferTime[%s] is Invalid.",glassProcessTime,transferTime));
			return;
		}
			
		String sql = " SELECT M.MACHINENAME , MS.IDLETIMELIMIT - ROUND((SYSDATE - M.LASTIDLETIME) * 24,1) AS REMAINTIME"
				  + " FROM MACHINE M , MACHINESPEC MS "
				  + " WHERE MS.FACTORYNAME = 'ARRAY' "
				  + " AND MS.MACHINEGROUPNAME = 'PVD'"
				  + " AND MS.DETAILMACHINETYPE = 'MAIN'"
				  + " AND MS.MACHINENAME = M.MACHINENAME"
				  + " AND M.MACHINESTATENAME = 'IDLE'"
				  + " AND M.DSPFLAG ='Y' ";
		
	   List<Map<String,Object>> resultList = null;
	   
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		}
		catch (Exception ex)
		{
           throw new CustomException(ex.getCause());
		}
		
		if(resultList ==null || resultList.size()==0)
		{
			log.info("No machine found for PVD idel time check");
			return;
		}
		
		double totalGlassProcessTime = Double.parseDouble(glassProcessTime) * glassQty + Double.parseDouble(transferTime);
		
		for(Map<String,Object> mapInfo : resultList)
		{
			String machineName = ConvertUtil.getMapValueByName(mapInfo, "MACHINENAME");
			String remainTime = ConvertUtil.getMapValueByName(mapInfo, "REMAINTIME");
			
			if(!this.checkIsNumeric(remainTime))
			{
				log.info(String.format("Invalid RemainTime of %s machine.",machineName));
				continue;
			}
			
			if(totalGlassProcessTime < Double.parseDouble(remainTime))
				log.info(String.format("PVD machine %s idel time check OK.",machineName));
			else 
				this.sendPVDCheckMail(machineName,remainTime,String.valueOf(totalGlassProcessTime));
		}
	}
	
	private void sendPVDCheckMail(String machineName,String remainTime,String totalGlassProcessTime) throws CustomException
	{
		String message =  "<pre>===============PVDIdleTimeOver================</pre>";
	           message += "<pre>==============================================</pre>";
	           message += "<pre>- MachineName  : " + machineName + "</pre>";
	           message += "<pre>- RemainTime  : " + remainTime + "</pre>";
	           message += "<pre>- TotalGlassProcessTime  : " + totalGlassProcessTime + "</pre>";
	      
		CommonUtil.SendMail("PVDIdleTimeOver", "PVD Machine Idle Time Over", message);
	}
	
	private boolean checkIsNumeric(String... args)
	{
		boolean resultFlag = true;

		if (args == null || args.length == 0)
		{
			log.info("The incoming argument value is Empty or Null!!.");
			return false;
		}

		int sequence = 0;
		for (String var : args)
		{
			sequence++;
			if (var == null || var.isEmpty())
			{
				log.info("No." + sequence + " argument value is Null!!.");
				return false;
			}

			if (var.contains("+") || var.contains("-"))
			{
				if ((var.contains("+") && var.lastIndexOf("+") != 0) || (var.contains("-") && var.lastIndexOf("-") != 0))
				{
					log.info("No." + sequence + " argument contains more than one positive and negative sign or in the wrong position.");
					resultFlag = false;
				}

				var = var.substring(1);
			}

			if (var.contains("."))
			{
				if (var.indexOf(".") == 0)
				{
					log.info("No." + sequence + " The decimal point is in the wrong position.");
					resultFlag = false;
				}

				String[] decimalSplit = var.split("\\.");

				if (decimalSplit.length > 2)
				{
					log.info("No." + sequence + " argument contains more than one decimal point.");
					resultFlag = false;
				}

				for (String numric : decimalSplit)
				{
					if (!StringUtil.isNumeric(numric))
					{
						log.info("No." + sequence + " argument value is not a number.");
						resultFlag = false;
					}
				}
			}
		}

		return resultFlag;
	}

	private Lot getLotDataForSorter(String machineName, String portName, String carrierName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = null;
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT C.LOTNAME, J.JOBNAME, C.MACHINENAME, C.PORTNAME ");
			sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, LOT L ");
			sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
			sql.append("   AND C.LOTNAME = L.LOTNAME ");
			sql.append("   AND L.LOTSTATE = 'Released' ");
			sql.append("   AND C.CARRIERNAME = L.CARRIERNAME ");
			sql.append("   AND C.CARRIERNAME = :CARRIERNAME ");
			//sql.append("   AND C.TRANSFERDIRECTION = 'SOURCE' ");
			sql.append("   AND (J.JOBSTATE = 'CONFIRMED' OR J.JOBSTATE = 'STARTED') ");
			sql.append("ORDER BY J.CREATETIME ");

			Map<String, String> args = new HashMap<String, String>();
			args.put("CARRIERNAME", carrierName);
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				String lotName = ConvertUtil.getMapValueByName(result.get(0), "LOTNAME");
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			}
			else
			{
				eventLog.info("No Assigned Source Lot on BufferCST: " + carrierName);
			}
		}
		else
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
		}

		return lotData;
	}

	private String getLotInfoBydurableNameForFisrtGlass(String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotList;
		String lotName = "";

		String sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME " + "AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG = 'N' AND JOBNAME IS NOT NULL";

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (lotList.size() > 0 && lotList != null)
			lotName = lotList.get(0).get("LOTNAME").toString();
		else
		{
			sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME " + "AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG IS NULL AND JOBNAME IS NOT NULL";

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (lotList.size() > 0 && lotList != null)
				lotName = lotList.get(0).get("LOTNAME").toString();
			else
			{
				sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE ";
				lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (lotList.size() > 0 && lotList != null)
					lotName = lotList.get(0).get("LOTNAME").toString();
			}
		}

		return lotName;
	}

	private void updateReservedLotState(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Reserved" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_START);
			reserveLot.get(0).setInputTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			/*
			condition = "productRequestName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and  machineName = ? and planDate = ? ";
			bindSet = new Object[] { lotData.getProductRequestName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName,
					reserveLot.get(0).getPlanDate() };
			List<DSPProductRequestPlan> productRequestPlan = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

			productRequestPlan.get(0).setPlanState("Processing");
			ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0));*/
		}
		catch (Exception e)
		{
			eventLog.info("Fail ReservedLot Updating");
		}
	}

	private String alarmSmsText(String errorEvent, String machineName, String carrierName, String lotName, String errorDetail, String factoryName) throws CustomException
	{
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append("AlarmEvent: ");
		smsMessage.append(errorEvent);
		smsMessage.append("\n");
		smsMessage.append("Factory: ");
		smsMessage.append(factoryName);
		smsMessage.append("\n");
		smsMessage.append("Machine: ");
		smsMessage.append(machineName);
		smsMessage.append("\n");
		smsMessage.append("CST ID: ");
		smsMessage.append(carrierName);
		smsMessage.append("\n");
		smsMessage.append("Lot ID: ");
		smsMessage.append(lotName);
		smsMessage.append("\n");
		smsMessage.append("AlarmText: ");
		smsMessage.append(errorDetail);

		return smsMessage.toString();
	}
	
	private Lot getLotData (boolean isSorter, String lotName, String machineName, String portName, String cstName) throws CustomException
	{
		Lot lotData = null;
		
		if (isSorter)
		{
			try
			{
				lotData = getLotDataForSorter(machineName, portName, cstName);

				if (lotData == null)
					return lotData;
			}
			catch (Exception e)
			{
				// No Assigned Lot
				log.info("No Assigned Lot on CST: " + cstName);
				return lotData;
			}
		}
		else
		{
			lotName = this.getLotInfoBydurableNameForFisrtGlass(cstName);
			try{lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);}catch (Exception e){}
		}
		
		return lotData;
	}
	
}
