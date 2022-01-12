package kr.co.aim.messolution.generic.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MainReserveSkip;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.OriginalProductInfo;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.extended.object.management.data.SorterSignReserve;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

//import com.sun.xml.internal.ws.wsdl.writer.document.Port;

public class CommonValidation implements ApplicationContextAware
{
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(CommonValidation.class);



	@Override
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public static boolean IsSorterAvailableFlow(Lot lotData) throws CustomException
	{
		ProcessFlow processFlow = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

		if (StringUtil.in(processFlow.getProcessFlowType(), "Sort", "BackUp"))
		{
			//SORT-0012:The Lot of {0} flow cannot be sort operation.
			throw new CustomException("SORT-0012", processFlow.getProcessFlowType());
		}
		else if ("Inspection".equals(processFlow.getProcessFlowType()))
		{
			String condition = " LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? "
							 + " AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? "
							 + " AND RETURNPROCESSFLOWNAME = ? AND RETURNPROCESSFLOWVERSION = ? AND RETURNOPERATIONNAME = ? AND RETURNOPERATIONVERSION = ? AND FORCESAMPLINGFLAG = 'Y'";
			
			Object[] bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), 
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),  lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),  
					lotData.getUdfs().get("RETURNFLOWNAME").toString(),"00001", lotData.getUdfs().get("RETURNOPERATIONNAME").toString(), "00001"};
			
			List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

			try
			{
				sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
			}
			catch (greenFrameDBErrorSignal dbError)
			{
				if (ErrorSignal.NotFoundSignal.equals(dbError.getErrorCode()))
					log.info("This inspection flow is not forcesample.");
				else
					throw new CustomException(dbError);
			
			}catch(Exception ex)
			{
				throw new CustomException(ex);
			}

			if(sampleLotList != null && sampleLotList.size() > 0)
			{
				throw new CustomException("SORT-0012","ForceSample");
			}
		}
		else
		{
			log.info(String.format("Sorter Lot [%s] Flow is %s", lotData.getKey().getLotName(), processFlow.getProcessFlowType()));
		}
		
		return true;
	}
	
	/**
	 * Flow/Oper of NodeStack compare to Flow/Oper of Lot
	 * 
	 * 2021-02-04	dhko	Add function
	 */
	public static boolean checkNodeStackCompareToOperation(Lot lotData) throws CustomException
	{
		if (lotData == null)
		{
			return true;
		}
		
		String nodeStack = lotData.getNodeStack();
		if (StringUtils.isNotEmpty(nodeStack))
		{
			String nodeId = nodeStack;
			if (nodeId.indexOf(".") > 0)
			{
				String[] nodeIdList = StringUtils.split(nodeStack, ".");
				nodeId = nodeIdList[nodeIdList.length - 1];
			}
			
			Node nodeInfo = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);
			if (!StringUtils.equals(lotData.getProcessFlowName(), nodeInfo.getProcessFlowName()))
			{
				// Different ProcessFlow. Lot = [{0}], Node = [{1}]
				throw new CustomException("LOT-3010", lotData.getProcessFlowName(), nodeInfo.getProcessFlowName());
			}
			else if (!StringUtils.equals(lotData.getProcessOperationName(), nodeInfo.getNodeAttribute1()))
			{
				// Different ProcessOperation. Lot = [{0}], Node = [{1}]
				throw new CustomException("LOT-3011", lotData.getProcessOperationName(), nodeInfo.getNodeAttribute1());
			}
		}
		
		return true;
	}
	
	public static boolean checkProcessingLotOnEQP(String factoryName, String machineName) throws CustomException
	{
		boolean hasProcessingLot = true;
		
		String sql = " SELECT 1 FROM DUAL "
					+ " WHERE 1=1 AND EXISTS (  SELECT LOTNAME FROM LOT "
											+ " WHERE FACTORYNAME = :FACTORYNAME AND LOTSTATE = :LOTSTATE "
											+ " AND LOTPROCESSSTATE = :LOTPROCESSSTATE AND MACHINENAME = :MACHINENAME ) ";

		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
		bindMap.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_LoggedIn);
		bindMap.put("MACHINENAME", machineName);
		
		List<Map<String,Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException(e.getCause());
		}

		if (resultList == null || resultList.size() == 0) hasProcessingLot = false;

		return hasProcessingLot;
	}
	
	public static boolean isNullOrEmpty(String... args)
	{
		if (args == null || args.length == 0)
			return true;

		for (String compValue : args)
		{
			if (compValue == null || compValue.isEmpty())
				return true;
		}
		return false;
	}
	
	public static boolean isNumeric(String... args)
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
					if (!StringUtils.isNumeric(numric))
					{
						log.info("No." + sequence + " argument value is not a number.");
						resultFlag = false;
					}
				}
			}
		}

		return resultFlag;
	}
	
	public static void checkRunbanPreventRule(List<Product> productDataList, String machineName,String processFlowType) throws CustomException
	{
		String sql = " SELECT R.VALUE ,REGEXP_REPLACE(LISTAGG( R.FLAG,',') WITHIN GROUP(ORDER BY R.FLAG ASC ) ,'([^,]+)(,\\1)*(,|$)','\\1\\3') AS FLAGLIST  "
				   + " FROM CT_RUNBANPREVENTRULE R , PRODUCT P "
				   + " WHERE P.PRODUCTNAME =  :PRODUCTNAME "
				   + " AND P.FACTORYNAME = R.FACTORYNAME"
				   + " AND P.PROCESSOPERATIONNAME = R.PROCESSOPERATIONNAME"
				   + " AND P.PROCESSOPERATIONVERSION = R.PROCESSOPERATIONVERSION "
				   + " AND R.MACHINENAME = :MACHINENAME "
				   + " AND R.PROCESSFLOWTYPE = :PROCESSFLOWTYPE "
				   + " GROUP BY R.VALUE "
				   + " ORDER BY R.VALUE DESC ";
		
		List<Map<String,Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { productDataList.get(0).getKey().getProductName(), machineName ,processFlowType});
		}
		catch (Exception ex)
		{
           if(ex instanceof FrameworkErrorSignal) 
           log.info(String.format(" Could not find Runban prevent rule registered as lot condition.[FactoryName =%s ,OperationName =%s , MachineName =%s]",
        		   																					productDataList.get(0).getFactoryName(),productDataList.get(0).getProcessOperationName(),machineName));
		}
		
		if(resultList == null || resultList.size()==0) return ;
		
		Map<String, List<String>> prohibitFlagMap = new HashMap<>();
		Map<String, Set<String>> mandatoryFlagMap = new HashMap<>();
		Map<String,List<String>> includeAnyFlagMap = new HashMap<>();
		
		for(Map<String,Object> resultMap: resultList)
		{
			if (ConvertUtil.getMapValueByName(resultMap, "VALUE").equals("1"))
			{
				List<String> flagList = Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ConvertUtil.getMapValueByName(resultMap, "FLAGLIST")));
				prohibitFlagMap.put("1", flagList);
			}
			else if (ConvertUtil.getMapValueByName(resultMap, "VALUE").equals("0"))
			{
				Set<String> flagSet = org.springframework.util.StringUtils.commaDelimitedListToSet(ConvertUtil.getMapValueByName(resultMap, "FLAGLIST"));
				mandatoryFlagMap.put("0", flagSet);
			}
			else if (ConvertUtil.getMapValueByName(resultMap, "VALUE").equals("2"))
			{
				List<String> flagList = Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(ConvertUtil.getMapValueByName(resultMap, "FLAGLIST")));
				includeAnyFlagMap.put("2", flagList);
			}
		}
		
		for (Product productData : productDataList)
		{
			String flagStack = productData.getUdfs().get("FLAGSTACK");

			// RUNBAN-0003:Product [{0}:FlagStack = {1}] has a prohibited unit.
			if (prohibitFlagMap.size() > 0 && CollectionUtils.containsAny(Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(flagStack)), prohibitFlagMap.get("1")))
				throw new CustomException("RUNBAN-0003", productData.getKey().getProductName(), flagStack);

			// RUNBAN-0004:Product [{0}:FlagStack = {1}] does not have all mandatory unit.
			if (mandatoryFlagMap.size() > 0 && !org.springframework.util.StringUtils.commaDelimitedListToSet(flagStack).containsAll(mandatoryFlagMap.get("0")))
				throw new CustomException("RUNBAN-0004", productData.getKey().getProductName(), flagStack);

			// RUNBAN-0005:Product [{0}:FlagStack = {1}] does not include any one unit of Optional.
			if (includeAnyFlagMap.size() > 0)
			{
				if (CollectionUtils.containsAny(Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(flagStack)), includeAnyFlagMap.get("2")))
					continue;
				else
					throw new CustomException("RUNBAN-0005", productData.getKey().getProductName(), flagStack);
			}
		}
	}
	
	public static void checkBaseLine(Lot lotData, Machine machineData,String eventUser) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
	    ProductRequest woData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
		   
		if (!constMap.Mac_OnLineRemote.equals(machineData.getCommunicationState()))
		{
			checkBaseLineOnLineLocal(constMap, woData, lotData, machineData,eventUser);
		}
	}
	
	public static void checkBaseLineOnLineLocal(ConstantMap constMap,ProductRequest woData,Lot lotData, Machine machineData,String eventUser) throws CustomException
	{
		boolean checkFail = true ;
		
		// Runban Rule BaseLine Check OnLineLocal 2/2
		if(StringUtil.in(lotData.getProductionType(),constMap.Pord_Production,constMap.Pord_Engineering,constMap.Pord_Test))
		{
			if(StringUtils.equals(woData.getUdfs().get("SUBPRODUCTIONTYPE"), "P")
		    		||StringUtils.equals(woData.getUdfs().get("SUBPRODUCTIONTYPE"), "ESLC"))
			{
				checkFail = false;
			}
		}
		
		if(!checkFail)
		{
			ProcessOperationSpec poSpecData =  MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(lotData);
			if (!constMap.Mac_InspectUnit.equals(poSpecData.getProcessOperationType()))
			{

				if(checkExistAbortProcessingInfo(lotData))
				{
					if(constMap.Mac_OffLine.equals(machineData.getCommunicationState()))
					{
		                String sql = " SELECT DECODE(COUNT(USERID),0,'False','True') AS ACCESSFLAG "
		                		   + " FROM USERPROFILE U , ENUMDEFVALUE E "
		                		   + " WHERE U.DEPARTMENT = E.ENUMVALUE "
		                		   + " AND U.USERID = ? AND E.ENUMNAME = 'BaseLineDepartment' ";
		                
		                List<Map<String,Object>> resultList = null;
		                
		                try
		                {
							resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { eventUser });
		                }
		                catch(Exception ex)
		                {
		                	throw new CustomException(ex.getCause());
		                }
		                
		                //USER-0012:This user [{0}] is not registered in the Baselinedpartment. 
						if ("False".equals(ConvertUtil.getMapValueByName(resultList.get(0), "ACCESSFLAG")))
							throw new CustomException("USER-0012");       	
					}
					else
					{
						throw new CustomException("SYS-9000",String.format("MachineName:%s,CommunicationState:%s,ProductionType:%s", 
																				machineData.getKey().getMachineName(),machineData.getCommunicationState(),lotData.getProductionType()));
					}					
				}
			}		
		}
	}
	
	public static Boolean checkExistAbortProcessingInfo(Lot lotData) throws CustomException
	{
		List<Product> productList = MESLotServiceProxy.getLotServiceUtil().getProductDataListByLotName(lotData.getKey().getLotName(), false);
		
		boolean notExist = true;
		for(Product productData: productList)
		{
			if("B".equals(productData.getUdfs().get("PROCESSINGINFO")))
			{
				notExist = false;
				break;
			}
		}
		return notExist;

	}
	
	public static void checkFilmCSTStateIsInuse(Durable durableData) throws CustomException
	{
		if (!StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
		{
			throw new CustomException("CST-0016", durableData.getKey().getDurableName(), durableData.getDurableState());
		}
	}
	
	public static void checkFilmCasstte(Durable durableData) throws CustomException
	{
		if (!durableData.getDurableType().equals(GenericServiceProxy.getConstantMap().CST_TYPE_OLEDFilm))
			throw new CustomException("DURABLE-0014", durableData.getDurableType());
	}
	
	public static void checkFilmBox(Durable durableData) throws CustomException
	{
		if (!durableData.getDurableType().equals(GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox))
			throw new CustomException("DURABLE-0014", durableData.getDurableType());
	}
	
	public static void checkFilmAvailableState(Consumable filmData) throws CustomException
	{
		if (!StringUtils.equals(filmData.getConsumableState(), GenericServiceProxy.getConstantMap().Cons_Available))
			throw new CustomException("CONSUMABLE-0001", filmData.getKey().getConsumableName(),filmData.getConsumableState());
	}
	
	public static void checkFilmInUseState(Consumable filmData) throws CustomException
	{
		if (!StringUtils.equals(filmData.getConsumableState(), GenericServiceProxy.getConstantMap().Cons_InUse))
			throw new CustomException("CONSUMABLE-0001", filmData.getKey().getConsumableName(),filmData.getConsumableState());
	}
	
	public static void checkFilmQtyIsNotEmpty(Consumable filmData) throws CustomException
	{
		if (filmData.getQuantity() <= 0)
			throw new CustomException("MATERIAL-0017", filmData.getKey().getConsumableName());
	}
	
	public static boolean checkTrayGroupIsEmpty(Durable trayData) throws CustomException
	{
		if(trayData == null)
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		if( !checkTrayIsEmpty(trayData.getKey().getDurableName())||!checkTrayGroupIsEmpty(trayData.getKey().getDurableName()))
		{
			//TRAY-0006: The tray group is not empty.[TrayName:{0},State:{1},LotQty:{2}]
			throw new CustomException("TRAY-0006", trayData.getKey().getDurableName(),trayData.getDurableState(),trayData.getLotQuantity());
		}
		
		return true;
	}
	
	public static void checkTrayIsEmpty(Durable trayData) throws CustomException
	{
		if(trayData == null)
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		if(!trayData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available)|| !checkTrayIsEmpty(trayData.getKey().getDurableName())
		   || trayData.getLotQuantity() > 0 )
		{
			// TRAY-0007: The tray is not empty.[TrayName:{0},State:{1},LotQty:{2}]
			throw new CustomException("TRAY-0007", trayData.getKey().getDurableName(), trayData.getDurableState(), trayData.getLotQuantity());
		}
	}
	
	public static void checkTrayIsNotEmpty(Durable trayData) throws CustomException
	{
		if(trayData == null)
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		if(!trayData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_InUse) || checkTrayIsEmpty(trayData.getKey().getDurableName())
		   || trayData.getLotQuantity() <= 0 )
			throw new CustomException("DURABLE-9003",String.format("TrayName:%s,State:%s,LotQty:%s", trayData.getKey().getDurableName(),trayData.getDurableState(),trayData.getLotQuantity()));
	}
	
	public static boolean checkTrayIsEmpty(String trayName) throws CustomException
	{
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(" SELECT 1 FROM LOT WHERE 1=1 AND CARRIERNAME = ? ", new Object[] { trayName });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)return true;

		return false;
	}
	
	public static boolean checkTrayGroupIsEmpty(String trayGroupName) throws CustomException
	{
		List<Map<String, Object>> resultList = null;

		try
		{
			String sql = " SELECT 1 FROM DUAL WHERE EXISTS (SELECT L.LOTNAME FROM LOT L ,DURABLE D WHERE L.CARRIERNAME = D.DURABLENAME AND D.DURABLETYPE = 'Tray' AND D.COVERNAME = ? ) ";
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { trayGroupName });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)return true;

		return false;
	}
	
	public static <T> int differentCheck(List<T> list){
		
		T oldValue = list.get(0);
		int differentPoint = 0;
		
		for(T value : list){
			
			if(!oldValue.equals(null) && !oldValue.equals(value)){
				return differentPoint;
			}
			oldValue = value;
			differentPoint++;
		}
		
		return 0;
	}
	
	public static <T> boolean differentCheck(T oldvalue, T newvalue){
		
		if(!oldvalue.equals(null) && !oldvalue.equals(newvalue)){
			return false;
		}else{
			return true;
		}
		
	}
	public static void CheckLotQuantity(Lot lotData) throws CustomException
	{
		if(lotData.getProductQuantity()<=0)
		{
			log.info("Product Quantity is less than '0' - CheckLotQuantity");
			throw new CustomException("LOT-0311");
		}	
	}
	public static <T> int AccordCheck(List<T> list, T arcordValue){
		
		int differentPoint = 0;
		
		for(T value : list){
			
			if(!arcordValue.equals(value)){
				return differentPoint;
			}
			differentPoint++;
		}
		
		return -1;
	}
	
	public static void checkNotNull( String aItem, String aValue ) throws CustomException{
		if( StringUtils.isEmpty(aValue)){
			throw new CustomException("COM-9000", aItem, aValue);
		}
	}

	@SuppressWarnings("unchecked")
	public static void checkStateModelEvent(String type, String machineName, String portName, String oldState, String newState) throws CustomException
	{
		boolean isDifferent = false;

		String stateModel = "";
		String sql = "";
		String eventName = "";

		Map<String, String> bindMap = new HashMap<String, String>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

		if (StringUtils.equals(type, "MACHINE"))
		{
			MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);
			stateModel = machineSpec.getMachineStateModelName();

			if (oldState.equals(newState))
			{
				isDifferent = true;
			}
		}
		else if (StringUtils.equals(type, "PORT"))
		{
			PortSpec portSpec = CommonUtil.getPortSpecInfo(machineName, portName);
			stateModel = portSpec.getPortStateModelName();
		}

		if (!isDifferent)
		{
			sql = "SELECT EVENTNAME FROM STATEMODELEVENT WHERE STATEMODELNAME = :stateModel AND EVENTNAME = :eventName";
			eventName = oldState + "-" + newState;

			bindMap.put("stateModel", stateModel);
			bindMap.put("eventName", eventName);

			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() == 0)
			{
				throw new CustomException("MACHINE-0006", newState, oldState, newState);
			}
		}
	}

	public static void checkNamingRuleArgumentsCount(List<Map<String, Object>> sqlResult, String ruleName) throws CustomException
	{
		int enumValueCount = 0;
		for (int i = 0; i < sqlResult.size(); i++) {
			if(!StringUtils.isEmpty((String)sqlResult.get(i).get("ENUMVALUE"))) enumValueCount = enumValueCount +1;
		}
		if(sqlResult.size() != enumValueCount)
			throw new CustomException("NAMING-9000", enumValueCount, ruleName, sqlResult.size());
	}
	
	public static void checkLotState( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released)) )
		{
			throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	public static void checkLotStateScrapped( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Scrapped)) )
		{
			throw new CustomException("LOT-0043", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	public static void checkLotStateUnScrapped( Lot lotData ) throws CustomException{
		if ( (lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Scrapped)) )
		{
			throw new CustomException("LOT-0050", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	public static void checkLotCarriernameNull( Lot lotData ) throws CustomException{
		if (!StringUtils.isEmpty(lotData.getCarrierName()))
		{
			throw new CustomException("LOT-0124", lotData.getKey().getLotName(), lotData.getCarrierName()); 
		}
	}
	
	public static void checkLotCarriernameNotNull( Lot lotData ) throws CustomException{
		if (StringUtils.isEmpty(lotData.getCarrierName()))
		{
			throw new CustomException("LOT-1013"); 
		}
	}

	public static void checkLotGradeN(Lot lotData) throws CustomException
	{
		if (lotData.getLotGrade().equals("N"))
		{
			throw new CustomException("LOT-0321", lotData.getKey().getLotName(), lotData.getLotGrade());
		}
	}

	public static void checkLotGradeNotG(Lot lotData) throws CustomException
	{
		if (!lotData.getLotGrade().equals("G"))
		{
			throw new CustomException("LOT-0321", lotData.getKey().getLotName(), lotData.getLotGrade());
		}
	}

	public static void checkLotDuedate( Lot lotData ) throws CustomException{
		int value = TimeStampUtil.getTimestamp(TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DAY)).compareTo(lotData.getDueDate());
		
		if(value > 0)
		{
			throw new CustomException("LOT-0037", lotData.getKey().getLotName());
		}
	}
	
	public static void checkLotIssueState( Lot lotData ) throws CustomException{
		if (lotData.getUdfs().get("LOTISSUESTATE").equals("Y"))
		{
			ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
			if(flowData.getProcessFlowType().equals("Main"))
			{
				throw new CustomException("LOT-0036", lotData.getKey().getLotName(), lotData.getUdfs().get("LOTISSUESTATE"));
			}
		}
	}
	
	public static void checkLotIssuestate( Lot lotData ) throws CustomException{
		if (lotData.getUdfs().get("LOTISSUESTATE").equals("Y"))
		{
				throw new CustomException("LOT-0036", lotData.getKey().getLotName(), lotData.getUdfs().get("LOTISSUESTATE"));
		}
	}
	
	public static void checkSlotSel( String slotSel,  String slotMap ) throws CustomException
	{
		//slotSel Validation
		if(StringUtils.isNotEmpty(slotSel))
		{
			for(  int i = 0; i< slotSel.length(); i++ ) 
			{
				String checkSlotSel = Character.toString(slotSel.charAt(i));
				int position = i;
				
				if ( checkSlotSel.equals("O"))
				{
					if(StringUtils.isNotEmpty(slotMap)) 
					{
						String slotMapOne = slotMap.substring(position, position+1);
						 
						if(!slotMapOne.equals("O")) 
						{ 
							throw new CustomException("LOT-0017", slotMap , slotSel);
						}
					}		
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void checkStateName( String stateModelName, String stateName ) throws CustomException{
		
		String sql = "SELECT STATENAME FROM STATEMODELSTATE WHERE STATEMODELNAME = :stateModelName AND STATENAME = :stateName";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("stateModelName", stateModelName);
		bindMap.put("stateName", stateName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() == 0){
			throw new CustomException("STATE-0001", stateName);
		}
	}

	public static void checkAvailableCst(Durable durableData) throws CustomException
	{
		if (!StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
		{
			throw new CustomException("CST-0016", durableData.getKey().getDurableName(), durableData.getDurableState());
		}
	}

	public static void checkEmptyCst( String carrierName ) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			  
		if(durableData.getDurableState().equals("InUse"))
			throw new CustomException("CST-0006", carrierName);
		
	}
	
	public static void checkMultiLot( String carrierName ) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			  
		if(StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_ARRAY) ||
				StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_OLED) ||
					StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_TPNORMAL) )
		{
			if(StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
			{
				long durableLotQty = durableData.getLotQuantity(); 
				
				if(durableLotQty != 0)
				{
					throw new CustomException("CST-0023", carrierName);
				}
			}
			else
			{
				throw new CustomException("CST-0006", carrierName);
			}
		}
	}
	
	public static void checkLotProcessState( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedOut)) )
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState()); 
		}
	}
	
	public static void checkJobDownFlag( Lot lotData ) throws CustomException{
		if(StringUtils.equals(lotData.getUdfs().get("JOBDOWNFLAG"),"Y"))
		{
			throw new CustomException ("LOT-0312");
		}
	}
	
	public static void checkJobDownFlag( MaskLot lotData ) throws CustomException{
		if(StringUtils.equals(lotData.getUdfs().get("JOBDOWNFLAG"),"Y"))
		{
			throw new CustomException ("LOT-0312");
		}
	}
	
	public static void checkLotProcessStateRun( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Run)) )
		{
			throw new CustomException("LOT-0044", lotData.getKey().getLotName()); 
		}
	}

	public static void checkLotProcessStateWait( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Wait)) )
		{
			throw new CustomException("LOT-0091", lotData.getKey().getLotName()); 
		}
	}
	
	public static void checkCancelAvailEvent(String lastEventName, String availEventName)
		throws CustomException
	{
		if (!lastEventName.equals(availEventName))
		{
			throw new CustomException("SYS-0000", "Invalid condition to do cancel");				
		}
	}

	public static void CheckDurableState(Durable durableData) throws CustomException
	{
		if (StringUtils.equals(durableData.getDurableState(), "Scrapped"))
		{
			throw new CustomException("CST-0007", durableData.getKey().getDurableName());
		}
	}
	
	public static void CheckScrapedDurableState(Durable durableData) throws CustomException
	{
		if (!StringUtils.equals(durableData.getDurableState(), "Scrapped"))
		{
			throw new CustomException("CST-0022", durableData.getKey().getDurableName());
		}
	}
	
	public static void CheckEmptyCoverName(Durable durableData) throws CustomException
	{
		if (StringUtils.isNotEmpty(durableData.getUdfs().get("COVERNAME")))
		{
			throw new CustomException("CST-0021", durableData.getKey().getDurableName());
		}
	}
		
	@SuppressWarnings("unchecked")
	public static void checkTrayLotQty(String durableInfo) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT LOTNAME ");
		sql.append("  FROM LOT ");
		sql.append(" WHERE FACTORYNAME = 'POSTCELL' ");
		sql.append("   AND CARRIERNAME = :DURABLENAME");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DURABLENAME", durableInfo);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			throw new CustomException("LOT-0126", durableInfo,sqlResult.size());
		}
		
	}
	
	public static void CheckDurableHoldState(Durable durableData) throws CustomException
	{
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
		{
			throw new CustomException("CST-0005", durableData.getKey().getDurableName().toString());
		}
	}
	
	public static void CheckShieldHoldState(ShieldLot shieldLot) throws CustomException
	{
		String holdState = shieldLot.getLotHoldState();
		if (holdState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
		{
			throw new CustomException("SHIELD-0011", shieldLot.getShieldLotName());
		}
	}
	
	public static void CheckShieldProcessState_WAIT(ShieldLot shieldLot) throws CustomException
	{
		String processState = shieldLot.getLotProcessState();
		if (processState.equals(GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("SHIELD-0010", shieldLot.getShieldLotName());
		}
	}
	
	public static void CheckShieldProcessState_RUN(ShieldLot shieldLot) throws CustomException
	{
		String processState = shieldLot.getLotProcessState();
		if (processState.equals(GenericServiceProxy.getConstantMap().Lot_Wait))
		{
			throw new CustomException("SHIELD-0017", shieldLot.getShieldLotName());
		}
	}
	
	public static void CheckShieldState_Released(ShieldLot shieldLot) throws CustomException
	{
		String state = shieldLot.getLotState();
		if (!state.equals(GenericServiceProxy.getConstantMap().Lot_Released))
		{
			throw new CustomException("SHIELD-0012", shieldLot.getShieldLotName(), state);
		}
	}
	
	public static void CheckShieldItemLineSetChamber(ShieldLot shieldLot, ShieldLot checkShield) throws CustomException
	{
		if(shieldLot.getLine() != checkShield.getLine())
		{
			throw new CustomException("SHIELD-0006");
		}
		
		if(!shieldLot.getChamberNo().equals(checkShield.getChamberNo()))
		{
			throw new CustomException("SHIELD-0008");
		}
		
		if(!shieldLot.getSetValue().equals(checkShield.getSetValue()))
		{
			throw new CustomException("SHIELD-0007");
		}
	}
	
	public static void CheckShieldItemCommon(ShieldLot shieldLot, ShieldLot checkShield) throws CustomException
	{
		if(!shieldLot.getProcessOperationName().equals(checkShield.getProcessOperationName()))
		{
			throw new CustomException("SHIELD-0016");
		}
		
		if(!shieldLot.getShieldSpecName().equals(checkShield.getShieldSpecName()))
		{
			throw new CustomException("SHIELD-0003");
		}
	}
	
	public static void CheckDurableNotHoldState(Durable durableData) throws CustomException
	{
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_N))
		{
			throw new CustomException("CST-0015", durableData.getKey().getDurableName().toString());
		}
	}

	public static void CheckDurableCleanState(Durable durableData) throws CustomException
	{
		if (StringUtils.equals(durableData.getDurableCleanState(), "Dirty"))
		{
			log.info("CST Load Fail,Please Clean It");
			throw new CustomException("CST-0008", durableData.getKey().getDurableName());
		}
	}

	public static void checkEmptyCST(String carrierName,
									 String portType) throws CustomException
	{
		// Get DurableData & PortData
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);

		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
		// Get valuable
		String durableState = durableData.getDurableState();
		
		//Check DurableState
		if(StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PL) || 
		   StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PB)) 
		{
			if(StringUtils.equals(durableState, GenericServiceProxy.getConstantMap().Dur_Available))
			{
				throw new CustomException("DURABLE-9002", carrierName);
			}
		}
	}
	 
	public static void checkAvailableCST(String carrierName,
									 String portType, String machineDesc) throws CustomException
	{		
		// Exception handling when machinegroup value is UPK
		if (StringUtils.equals(machineDesc, GenericServiceProxy.getConstantMap().MachineGroup_Unpacker)) return;
					
		// Check DurableValue
		if(carrierName.isEmpty())
			throw new CustomException("CARRIER-9000", carrierName);
		
		// Get DurableData
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(carrierName);
		
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
								
		// Get valuable
		String durableState = durableData.getDurableState();
		
		// Check DurableState
		if(StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PL) || 
		   StringUtils.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PB)) 
		{
			if(StringUtils.equals(durableState, GenericServiceProxy.getConstantMap().Dur_NotAvailable))
			{
				throw new CustomException("DURABLE-9002", carrierName);
			}
		}
	}
	
	public static void checkAlreadyLotProcessStateTrackIn(Lot lotData) throws CustomException{
		if(lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedIn))
		{
			throw new CustomException("LOT-9003",lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState());
		}	
	}
	 
	public static Durable checkExistCarrier( String carrierName ) throws CustomException{

		try{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(carrierName);
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
			return durableData;
		} catch ( Exception e ){
			throw new CustomException("CARRIER-9000", carrierName);
		}
	}
	
	public static void checkExistMachine( String machineName ) throws CustomException{

		try{
			MachineKey machineKey = new MachineKey();
			machineKey.setMachineName(machineName);
			MachineServiceProxy.getMachineService().selectByKey(machineKey);
		} catch ( Exception e ){
			throw new CustomException("MACHINE-9000", machineName);
		}
	}
	
	public static void checkConsumedProduct( Product productData, String productName ) throws CustomException{

		if(productData.getProductState().equals(GenericServiceProxy.getConstantMap().Prod_Consumed)) {
			throw new CustomException("PRODUCT-9015", productName, productData.getProductState());
		}
	}

	public static void checkExistPort(String machineName, String portName) throws CustomException
	{
		try
		{
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);
			PortServiceProxy.getPortService().selectByKey(portKey);
		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	public static void checkLotShippedState( Lot lotData ) throws CustomException{
		if ( lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped) )
		{
			throw new CustomException("LOT-9020", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	public static void checkLotUnShippedState( Lot lotData ) throws CustomException{
		if ( !(lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped)) )
		{
			throw new CustomException("LOT-9020", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	
	
	public static void checkTFTLotCFLot( String TFTLot, String CFLot ) throws CustomException{

		try{ 
			if(TFTLot.equals(CFLot))
			{
				throw new CustomException("Lot-0054", TFTLot,CFLot);
			}
		} catch ( Exception e ){
			throw new CustomException("Exception-0001", e);
		}
	}

	public static void checkLotHoldState( Lot lotData ) throws CustomException{
		if ( lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().FLAG_Y) )
		{
			throw new CustomException("LOT-9015", lotData.getKey().getLotName(), lotData.getLotHoldState()); 
		}
	}
	
	public static void checkLotReworkState(Lot lotData) throws CustomException{
		if (!lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_NotInRework))
		{
			throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getReworkState());
		}	
	}
	
	
	public static void checkLotGrade( Lot lotData ) throws CustomException{
		if(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_R))
		{
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
			
			if ( !StringUtils.equals(processFlowData.getProcessFlowType(), "Rework"))
			{
				if ( !StringUtils.equals(processFlowData.getProcessFlowType(), "Strip"))
					throw new CustomException("LOT-9025", lotData.getKey().getLotName(), lotData.getLotGrade() ,lotData.getProcessFlowName()); 
			}
		}
	}
	
	public static void checkLotSameSpec(Lot lotData, String groupGrade, String groupDetailGrade, String groupOperation, String groupSpec, String groupProduction, String groupLotName, String groupWO)
			throws CustomException
	{
		if (!lotData.getLotGrade().equals(groupGrade) || !lotData.getUdfs().get("LOTDETAILGRADE").equals(groupDetailGrade) || !lotData.getProcessOperationName().equals(groupOperation)
				|| !lotData.getProductSpecName().equals(groupSpec) || !lotData.getProductionType().equals(groupProduction) || !lotData.getProductRequestName().equals(groupWO))
		{
			throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
		}
	}
	public static void checkLotSameSpec(Lot lotData, String groupGrade, String groupOperation, String groupSpec, String groupProduction, String groupLotName, String groupWO)
			throws CustomException
	{
		if (!lotData.getLotGrade().equals(groupGrade)|| !lotData.getProcessOperationName().equals(groupOperation)
				|| !lotData.getProductSpecName().equals(groupSpec) || !lotData.getProductionType().equals(groupProduction) || !lotData.getProductRequestName().equals(groupWO))
		{
			throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
		}
	}
	
	public static void checkLotSameSpecForMVI(Lot lotData, String groupOperation, String groupSpec, String groupProduction, String groupLotName, String groupWO)
			throws CustomException
	{
		if (!lotData.getProcessOperationName().equals(groupOperation) || !lotData.getProductSpecName().equals(groupSpec) || !lotData.getProductionType().equals(groupProduction) || !lotData.getProductRequestName().equals(groupWO))
		{
			throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
		}
	}
	
	public static void checkLotNotHoldState( Lot lotData ) throws CustomException{
		if ( lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_N) )
		{
			throw new CustomException("LOT-9015", lotData.getKey().getLotName(), lotData.getLotHoldState()); 
		}
	}
	
	public static void checkExistPanelName(String productName) throws CustomException{
		
		ProductKey keyInfo = new ProductKey();
		keyInfo.setProductName(productName);
		
		Product product = new Product();
		try{
			product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
		}catch(Exception e){
			throw new CustomException("PANEL-9001", product.getKey().getProductName());
		}
	}
	 
	public static Product checkExistProductName(String productName) throws CustomException
	{
		try {
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productName);
			
			Product productData = null;
			productData = ProductServiceProxy.getProductService().selectByKey(productKey);
			
			return productData;
		} catch (Exception e) {		
			throw new CustomException("PRODUCT-9000", productName);	
		}
	}
	
	public static void checkExistSheetName(String productName) throws CustomException{
		
		ProductKey keyInfo = new ProductKey();
		keyInfo.setProductName(productName);
		
		Product product = new Product();
		try{
			product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
		}catch(Exception e){
			throw new CustomException("PANEL-9001", product.getKey().getProductName());
		}
	}
	 
	@SuppressWarnings("unchecked")
	public static void checkProductListAndCount(String productList, int listCount, int productQuantity) throws CustomException
	{
		if(listCount == 0) {
			throw new CustomException("PRODUCT-9002", "");
		}else if(listCount !=  productQuantity) {
			throw new CustomException("PRODUCT-9002", "Reported: " + Integer.toString(listCount) + ", ExistOnMES: " + Integer.toString(productQuantity) + ". List: (" + productList + ")");
		}else {
			String sql = "SELECT PRODUCTNAME FROM PRODUCT WHERE PRODUCTNAME IN ( " + productList + " ) ";
			Map<String, String> bindMap = new HashMap<String, String>();
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			if(sqlResult.size() > 0) {
				throw new CustomException("PRODUCT-9012", productList);
			}
		}
	}
	
	public static void checkTrayGroupHoldState(String coverName) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(coverName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		//Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
		{
			throw new CustomException("Tray-0005", durableData.getKey().getDurableName().toString());
		}
		
		
	}

	public static void checkTrayHoldState(String trayName) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(trayName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		//Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
		if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
		{
			throw new CustomException("Tray-4444", durableData.getKey().getDurableName().toString());
		}
	}
	
	public static void checkMaskSpecAndProductSpec(Durable maskData, ProductSpec productSpecData) throws CustomException
	{
		if(maskData.getDurableType().equals("EVAMask"))
		{
			if(!maskData.getDurableSpecName().equals(productSpecData.getUdfs().get("EVAMASKSPECNAME")))
			{
				throw new CustomException("SYS-9999", "Mask["+maskData.getKey().getDurableName()+"] MaskSpec["+maskData.getDurableSpecName()+"]/ProductSpec["+productSpecData.getKey().getProductSpecName()+"] MaskSpec["+productSpecData.getUdfs().get("EVAMASKSPECNAME")+"]");
			}
			return;
		}
		else if(maskData.getDurableType().equals("TFEMask"))
		{
			if(!maskData.getDurableSpecName().equals(productSpecData.getUdfs().get("TFEMASKSPECNAME")))
			{
				throw new CustomException("SYS-9999", "Mask["+maskData.getKey().getDurableName()+"] MaskSpec["+maskData.getDurableSpecName()+"]/ProductSpec["+productSpecData.getKey().getProductSpecName()+"] MaskSpec["+productSpecData.getUdfs().get("TFEMASKSPECNAME")+"]");
			}
			return;
		}
		else if(maskData.getDurableType().equals("FritMask"))
		{
			if(!maskData.getDurableSpecName().equals(productSpecData.getUdfs().get("FRITMASKSPECNAME")))
			{
				throw new CustomException("SYS-9999", "Mask["+maskData.getKey().getDurableName()+"] MaskSpec["+maskData.getDurableSpecName()+"]/ProductSpec["+productSpecData.getKey().getProductSpecName()+"] MaskSpec["+productSpecData.getUdfs().get("FRITMASKSPECNAME")+"]");
			}
			return;
		}

	}

	public static void checkMaskTurnCount(Durable mask) throws CustomException
	{
		String turnCount = mask.getUdfs().get("TURNCOUNT").toString();
		int iTurnCount = Integer.parseInt(turnCount);
		
		//Mask Turn count must be 0, 2, 4... / Not 1, 3, 5...
		if(iTurnCount%2 != 0)
		{
			//MASK-0111:Mask[{0}] Turn Count is [{1}]. Turn Count Error. 
			throw new CustomException("MASK-0111", mask.getKey().getDurableName(),turnCount); 
		}   
	}
	
	public static boolean checkPairProduct(MachineSpec machineSpecData, Port portData, ProductSpec productSpecData) throws CustomException
	{
		boolean isCheckPairProduct = false;
		
		//Get Info
		boolean isEVASpec = checkEVASpec(productSpecData);

		if(isEVASpec)
		{
			return true;
		}
		
		return isCheckPairProduct;
	}
	
	public static boolean checkEVASpec(ProductSpec productSpecData) throws CustomException
	{
		boolean isEVASpec = false;
		
		if(StringUtils.equals(productSpecData.getUdfs().get("PATTERN"), "F"))
		{
			isEVASpec = true;
		}
		
		return isEVASpec;
	}
	
	public static boolean checkTFESpec(ProductSpec productSpecData) throws CustomException
	{
		boolean isEVASpec = false;
		
		if(StringUtils.equals(productSpecData.getUdfs().get("PATTERN"), "T"))
		{
			isEVASpec = true;
		}
		
		return isEVASpec;
	}
	
	public static void ChekcMachinState(Machine machineData) throws CustomException
	{
		if(StringUtils.equals(machineData.getCommunicationState(),"OnLineLocal") ||
				StringUtils.equals(machineData.getCommunicationState(),"OnLineRemote"))
		{
			if(StringUtils.equals(machineData.getMachineStateName(), "DOWN") ||
					StringUtils.equals(machineData.getMachineStateName(), "MANUAL") ||
					StringUtils.equals(machineData.getMachineStateName(), "ENGINEER"))
			{
				throw new CustomException("MACHINE-0024", machineData.getCommunicationState(), machineData.getMachineStateName());
			}
		}
	}
	
	public static void CheckMachinState(Machine machineData) throws CustomException
	{
		if(StringUtils.equals(machineData.getMachineStateName(), "DOWN") ||
				StringUtils.equals(machineData.getMachineStateName(), "MANUAL") ||
				StringUtils.equals(machineData.getMachineStateName(), "ENGINEER"))
		{
			throw new CustomException("MACHINE-0024", machineData.getCommunicationState(), machineData.getMachineStateName());
		}
	}
	
	public static void CheckMachineState(Machine machineData, Lot lotData) throws CustomException
	{
			if (lotData != null && ( (lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().Pord_Production))||(lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().Pord_Engineering)) ) )
			{
				if(StringUtils.equals(machineData.getMachineStateName(), "DOWN") ||
					StringUtils.equals(machineData.getMachineStateName(), "MANUAL") 
					//||StringUtils.equals(machineData.getMachineStateName(), "ENGINEER")
					)
			       {
				      throw new CustomException("MACHINE-0024", machineData.getCommunicationState(), machineData.getMachineStateName());
			       }
		    }	
	}
	
	public static void CheckMachineStateExceptedEVALine(Machine machineData, Lot lotData) throws CustomException
	{
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME ORDER BY SEQ";
		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("ENUMNAME", "MachineRun");
		@SuppressWarnings("unchecked")
		List<Map<String,String>> groupResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			log.info(" Cancel validation EVA MachineGroup machineState = DOWN or PM  20190103 " );
			return;
		}
		if (lotData != null && ( (lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().Pord_Production))||(lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().Pord_Engineering)) ) )
		{	
			if((StringUtils.equals(machineData.getMachineStateName(), "DOWN")&&!StringUtils.equals(machineData.getUdfs().get("MACHINESUBSTATE"), groupResult.get(0).get("ENUMVALUE"))) ||
				(StringUtils.equals(machineData.getMachineStateName(), "MANUAL") && !machineData.getReasonCode().equals(groupResult.get(1).get("ENUMVALUE")))
				)
		       {
				  throw new CustomException("MACHINE-0029", groupResult.get(0).get("ENUMVALUE"), groupResult.get(1).get("ENUMVALUE"));
		       }
	    }	
	}
	public static void checkFilmSpecAndProductSpec(Consumable filmData, ProductSpec productSpecData) throws CustomException
	{
		if(filmData.getConsumableType().equals("TFEFilm"))
		{
			if(!filmData.getConsumableSpecName().equals(productSpecData.getUdfs().get("FILMSPEC")))
			{
				throw new CustomException("SYS-9999", "Film["+filmData.getKey().getConsumableName()+"] FilmSpec["+filmData.getConsumableSpecName()+"]/ProductSpec["+productSpecData.getKey().getProductSpecName()+"] FilmSpec["+productSpecData.getUdfs().get("FILMSPECNAME")+"]");
			}
			return;
		}
	}

	public static void checkMachineLockFlag(Machine machineData, Lot lotData) throws CustomException
	{
		String machineLockFlag = machineData.getUdfs().get("MACHINELOCKFLAG");
		log.info("MachineLockFlag is " + machineLockFlag);

		if (lotData != null && StringUtils.equals(machineLockFlag, "Y"))
		{
			ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
			log.info("ProcessFlow: " + flowData.getKey().getProcessFlowName() + ". ProcessFlowType: " + flowData.getProcessFlowType());

			if (StringUtils.equals(flowData.getProcessFlowType(), "MQC"))
			{
				log.info("Only allowed to process MQC Lot when MachineLockFlag is Y");
			}
			else
			{
				throw new CustomException("MACHINE-0030");
			}
		}
	}

	public static void checkMachineHold(Machine machineData) throws CustomException {
		if (machineData.getUdfs().get("MACHINEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().Mac_OnHold)) 
		{     
			//MACHINE-0053: Machine[{0}] has not  hold !
			throw new CustomException("MACHINE-0053", machineData.getKey().getMachineName());
		}
	}
	
	public static void checkSameOperation(String operationName, String compareOperationName, String operVersion, String compareOperVersion) throws CustomException {
		if ((!operationName.equals(compareOperationName)) || (!operVersion.equals(compareOperVersion))) {
			throw new CustomException("LOT-1000");
		}
	}
	
	public static void checkSameLotGrade(String lotGrade, String compareLotGrade) throws CustomException {
		if (!lotGrade.equals(compareLotGrade)) {
			throw new CustomException("LOT-1001");
		}
	}

	@SuppressWarnings("unchecked")
	public static void checkSameLotState(List<String> lotList, String lotState) throws CustomException
	{
		if (lotList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT LOTNAME, LOTSTATE ");
			sql.append("  FROM LOT ");
			sql.append(" WHERE LOTNAME IN ( :LOTLIST) ");
			sql.append("   AND (LOTSTATE IS NULL OR LOTSTATE != :LOTSTATE) ");
			sql.append("   AND ROWNUM = 1 ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", lotList);
			bindMap.put("LOTSTATE", lotState);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult.size() > 0)
			{
				String lotName = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTNAME");
				String slotState = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTSTATE");
				throw new CustomException("LOT-9019", slotState, lotName);
			}
		}
	}

	public static void checkSameLotProcessState(List<String> lotList, String processState) throws CustomException
	{
		if (lotList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT LOTNAME, LOTPROCESSSTATE ");
			sql.append("  FROM LOT ");
			sql.append(" WHERE LOTNAME IN ( :LOTLIST) ");
			sql.append("   AND (LOTPROCESSSTATE IS NULL OR LOTPROCESSSTATE != :LOTPROCESSSTATE) ");
			sql.append("   AND ROWNUM = 1 ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", lotList);
			bindMap.put("LOTPROCESSSTATE", processState);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult.size() > 0)
			{
				String lotName = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTNAME");
				String lotProcessState = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTPROCESSSTATE");
				throw new CustomException("LOT-9018", lotProcessState, lotName);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void checkProductGradeP(Lot lotData) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		List<String> productNameList = new ArrayList<>();

		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if ((flowData.getProcessFlowType().equals("Main")) // only check flow
				&& !operationData.getDetailProcessOperationType().equals("RP") // RP Oper Skip
				&& operationData.getProcessOperationType().equals("Production") // only check Production Oper
				&& (!(operationData.getDetailProcessOperationType().equals("SHIP") // special Oper Skip
						|| operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().SORT_OLEDtoTP)
						|| operationData.getDetailProcessOperationType().equals("SORT")
						|| operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().SORT_TPtoOLED) 
						|| operationData.getDetailProcessOperationType().equals("TSPSHIP")
						||operationData.getKey().getProcessOperationName().equals("21400"))))
		{
			List<Product> producList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			for (Product productA : producList)
			{
				String productName = productA.getKey().getProductName();
				productNameList.add(productName);
			}

			if (productNameList == null || productNameList.size() <= 0)
			{
				log.info("LotInfoDownSend Product is Not Found");
			}
			else
			{
				StringBuilder sql = new StringBuilder();
				sql.append("  SELECT PRODUCTGRADE, POSITION ");
				sql.append("  FROM PRODUCT ");
				sql.append("  WHERE PRODUCTNAME IN ( :PRODUCTNAMELIST ) ");
				sql.append("  AND PRODUCTSTATE = 'InProduction' ");
				sql.append("  AND PRODUCTGRADE = 'P' ");

				bindMap.put("PRODUCTNAMELIST", productNameList);

				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				if (sqlResult.size() > 0)
				{
					log.info("--checkProductGradeP--");
					productNameList.clear();
					productNameList = CommonUtil.makeListBySqlResult(sqlResult, "POSITION");

					throw new CustomException("LOT-9068", "P", productNameList);// LOT-9068: This Lot exists product that productGrade is {0}, Position is {1}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void checkLotHoldState(List<String> lotList) throws CustomException
	{
		if (lotList.size() > 0)
		{
			String sql = " SELECT LOTNAME, LOTHOLDSTATE FROM LOT WHERE LOTNAME IN ( :LOTLIST) AND NVL (LOTHOLDSTATE, :LOTHOLDSTATE) = :LOTHOLDSTATE AND ROWNUM = 1 ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", lotList);
			bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() > 0)
			{
				String lotName = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTNAME");
				String lotHoldState = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTHOLDSTATE");

				throw new CustomException("LOT-9015", lotName, lotHoldState);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void checkMQCPlanState(String lotName, String eventName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PROCESSFLOWTYPE ");
		sql.append("  FROM PROCESSFLOW P, LOT L ");
		sql.append(" WHERE P.FACTORYNAME = L.FACTORYNAME ");
		sql.append("   AND P.PROCESSFLOWNAME = L.PROCESSFLOWNAME ");
		sql.append("   AND P.PROCESSFLOWVERSION = L.PROCESSFLOWVERSION ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");
		sql.append("   AND P.PROCESSFLOWTYPE = 'MQC' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			// Get MQC Job by Lot ID. If MQCState In ('Released','Recycling') then OK else NG
			log.info("In MQC Flow");

			List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiStateNotIn(lotName);
			
			if (mqcPlanData == null)
			{
				//MQC-0004: MQC State is not Released or Recycling !
				throw new CustomException("MQC-0004");	
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void checkDistinctProcessOperation(List<String> lotList) throws CustomException
	{
		if (lotList.size() > 0)
		{

			String sql = "SELECT DISTINCT PROCESSOPERATIONNAME FROM LOT WHERE LOTNAME IN (:lotList) ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("lotList", lotList);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() > 1)
			{
				throw new CustomException("LOT-9016");
			}
		}
	}
	public static void checkMQCStateReleasedCount(String lotName, String eventName) throws CustomException
	{
		log.info("In MQC Flow");
		
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getReleasedMQCPlanDataByLotName(lotName);
		
		if (mqcPlanData != null)
		{
			throw new CustomException("LOT-0104", eventName);
		}
	}
	
	public static void checkRecipeV3(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName, String recipeName ,boolean isPhotoRecipe) throws CustomException
	{
		String RMSFlag = PolicyUtil.getPOSMachineRMSFlag(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, 
				processOperationName, processOperationVersion, machineName);;
		
		if (!StringUtils.equals(RMSFlag, "N"))
		{
			MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnCancelTrackInTime(machineName, recipeName);
		}
		else
		{
			log.info("Skip check Recipe : CheckLevel = N ");
		}
	}
	
	public static void checkRecipe(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName, String recipeName) throws CustomException
	{
		String checkLevel = PolicyUtil.getRecipeCheckLevel(factoryName, productSpecName, productSpecVersion,
				processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName);

		if (!StringUtils.equals(checkLevel, "N"))
		{
			MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnTrackInTime(machineName, recipeName);
		}
		else
		{
			log.info("Skip check Recipe : CheckLevel = N ");
		}
	}

	public static void checkRecipeForOLEDMask(String factoryName, String maskSpecName, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName, String recipeName) throws CustomException
	{
		String checkLevel = PolicyUtil.getRecipeCheckLevelForOLEDMask(factoryName, maskSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName);
		
		if(!checkLevel.equals("N"))
			MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnTrackInTime(machineName,recipeName);
		else
			log.info("Skip check Recipe : CheckLevel = N ");
	}

	public static void checkSlotMapInfo(Lot lotData, Durable durableData, String slotMap) throws CustomException
	{
		ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
		
		if(lotData.getFactoryName().equals("TP") && processOperationSpecData.getProcessOperationType().equals("Production"))
		{
			if(durableData.getCapacity() == StringUtils.length(slotMap))
			{
				int startPos = 0;
				int endPos = 2;
				for (int i = 0; i < 26; i++)
				{
					String slotPos = StringUtils.substring(slotMap, startPos, endPos);
					if(slotPos.equals("OX") || slotPos.equals("XO"))
					{
						throw new CustomException("CARRIER-9002",durableData.getKey().getDurableName());
					}
					else
					{
						startPos = startPos + 2;
						endPos = endPos + 2;
					}
				}
			}
			else
			{
				throw new CustomException("CARRIER-9002",durableData.getKey().getDurableName());
			}
		}
		else
			log.info("Skip check slot position info : factory [ " + lotData.getFactoryName() + " ], OperationType [ " + processOperationSpecData.getProcessOperationType() + " ] ");
	}

	public static void checkDuplicatedProductNameByProductList(List<String> productNameList) throws CustomException
	{
		if (productNameList == null || productNameList.size() <= 0)
		{
			log.info("Product is Not Found - checkDuplicatedProductNameByProductList");
			return;
		}

		List<String> checkList = new ArrayList<String>();
		List<String> checkDuplicatedList = new ArrayList<String>();

		for (String productName : productNameList)
		{
			if (checkList.contains(productName))
			{
				if (!checkDuplicatedList.contains(productName))
				{
					checkDuplicatedList.add(productName);
				}
			}
			else
			{
				checkList.add(productName);
			}
		}

		if (checkDuplicatedList.size() > 0)
		{
			throw new CustomException("LOT-9062", checkDuplicatedList);
		}
	}

	public static void checkDuplicatePosition(List<Element> productList) throws CustomException
	{
		if (productList == null || productList.size() <= 0)
		{
			log.info("Product is Not Found - checkDuplicatePosition");
			return;
		}

		List<String> checkList = new ArrayList<String>();
		List<String> checkDuplicatedList = new ArrayList<String>();

		for (Element productE : productList)
		{
			String position = productE.getChildText("POSITION");
			String slotPosition = productE.getChildText("SLOTPOSITION");
			String checkPosition = position + slotPosition;

			if (checkList.contains(checkPosition))
			{
				if (!checkDuplicatedList.contains(checkPosition))
				{
					checkDuplicatedList.add(checkPosition);
				}
			}
			else
			{
				checkList.add(checkPosition);
			}
		}

		if (checkDuplicatedList.size() > 0)
		{
			throw new CustomException("LOT-9063", checkDuplicatedList);
		}
	}
	
	public static void checkDifferentSpec(List<Element> productList) throws CustomException
	{
		if (productList == null || productList.size() <= 0)
		{
			log.info("Product is Not Found - checkDifferentSpec");
			return;
		}
		
		String firstSpec = "";
		String firstWO = "";
		
		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			if(StringUtils.isEmpty(firstSpec))
			{
				firstSpec = productData.getProductSpecName();
				firstWO = productData.getProductRequestName();
				continue;
			}
			
			if(!(productData.getProductSpecName().equals(firstSpec) && productData.getProductRequestName().equals(firstWO)))
				throw new CustomException("LOT-0155", productName);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void checkFirstGlassLot(Lot lotData, String machineName) throws CustomException
	{
		log.info(" Check FisrtGlass Lot Start ");
		boolean checkFlag = false;
		boolean checkEQFlag = false;
		String jobName = "";
		
		if( !StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")))
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			{
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT P.MACHINENAME, T.FACTORYNAME, T.PRODUCTSPECNAME, T.PROCESSFLOWNAME, T.PROCESSOPERATIONNAME");
				sql.append(" FROM TPFOPOLICY T, POSMACHINE P");
				sql.append(" WHERE 1=1");
				sql.append(" AND T.CONDITIONID = P.CONDITIONID");
				sql.append(" AND T.FACTORYNAME= :FACTORYNAME");
				sql.append(" AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME");
				sql.append(" AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION");
				sql.append(" AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME");
				sql.append(" AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION");
				sql.append(" AND T.PROCESSOPERATIONNAME= :PROCESSOPERATIONNAME");
				sql.append(" AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION");
		
				Map<String, String> args = new HashMap<String, String>();
				args.put("FACTORYNAME", lotData.getFactoryName());
				args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
				args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
				args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
				args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		
				List<Map<String, Object>> policyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				
				if (policyResult.size() > 0)
				{
					StringBuilder jobsql = new StringBuilder();
					jobsql.append(" SELECT JOBNAME FROM CT_FIRSTGLASSJOB B");
					jobsql.append(" WHERE B.FACTORYNAME = :FACTORYNAME");
					jobsql.append(" AND B.PRODUCTSPECNAME = :PRODUCTSPECNAME");
					jobsql.append(" AND B.PROCESSFLOWNAME = :PROCESSFLOWNAME");
					jobsql.append(" AND B.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME");
					jobsql.append(" AND B.MACHINENAME = :MACHINENAME");
					jobsql.append(" AND JOBSTATE <> 'Completed'");
					jobsql.append(" AND JOBSTATE <> 'Canceled'");
					jobsql.append(" AND (JUDGE <> 'Pass' or JUDGE IS NULL )");
					jobsql.append(" AND (JUDGE <> 'Strip' or JUDGE IS NULL )");
		
					args.clear();
					args.put("FACTORYNAME", policyResult.get(0).get("FACTORYNAME").toString());
					args.put("PRODUCTSPECNAME", policyResult.get(0).get("PRODUCTSPECNAME").toString());
					args.put("PROCESSFLOWNAME", policyResult.get(0).get("PROCESSFLOWNAME").toString());
					args.put("PROCESSOPERATIONNAME", policyResult.get(0).get("PROCESSOPERATIONNAME").toString());
					args.put("MACHINENAME", machineName);
					
					List<Map<String, Object>> jobResult = GenericServiceProxy.getSqlMesTemplate().queryForList(jobsql.toString(), args);
					
					if(jobResult.size() > 0)
					{
						jobName = jobResult.get(0).get("JOBNAME").toString();
						checkFlag = true;
					}
				}
				if(checkFlag)
				{
					//throw new CustomException("LOT-0086", jobName);
					log.info("FistGlass [" + jobName + "] not finished yet.");
				}
			}
			if (StringUtils.equals(machineData.getMachineGroupName(), "PVD")&&StringUtils.equals(lotData.getFactoryName(), "ARRAY")
					&&!StringUtils.equals(lotData.getProductionType(), "M")&&!StringUtils.equals(lotData.getProductionType(), "D"))
			{		
				Map<String, String> args = new HashMap<String, String>();
				StringBuilder jobsql = new StringBuilder();
				jobsql.append(" SELECT JOBNAME FROM CT_FIRSTGLASSJOB B");
				jobsql.append(" WHERE 1=1");
				jobsql.append(" AND B.MACHINENAME = :MACHINENAME");
				jobsql.append(" AND JOBSTATE <> 'Completed'");
				jobsql.append(" AND JOBSTATE <> 'Canceled'");
				jobsql.append(" AND (JUDGE <> 'Pass' or JUDGE IS NULL )");
				jobsql.append(" AND (JUDGE <> 'Strip' or JUDGE IS NULL )");
	
				args.clear();
				args.put("MACHINENAME", machineName);
				
				List<Map<String, Object>> jobResult = GenericServiceProxy.getSqlMesTemplate().queryForList(jobsql.toString(), args);
				
				if(jobResult.size() > 0)
				{
					jobName = jobResult.get(0).get("JOBNAME").toString();
					checkFlag = true;
				}
				
				if(checkFlag)throw new CustomException("LOT-0086", jobName);
			}
		}
		else
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			{
				log.info(" Check FisrtGlass Machine Start ");
				StringBuilder sqlEQ = new StringBuilder();
				sqlEQ.append(" SELECT B.MACHINENAME");
				sqlEQ.append(" FROM CT_FIRSTGLASSJOB B");
				sqlEQ.append(" WHERE 1=1");
				sqlEQ.append(" AND B.JOBNAME = :JOBNAME");
				sqlEQ.append(" AND B.JOBSTATE <> 'Completed'");
				sqlEQ.append(" AND B.JOBSTATE <> 'Canceled'");
		
				Map<String, String> argsEQ = new HashMap<String, String>();
				argsEQ.put("JOBNAME", lotData.getUdfs().get("JOBNAME"));
		
				List<Map<String, Object>> EQResult1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlEQ.toString(), argsEQ);
				
				if(EQResult1.size() > 0)
				{
					String machineNameNew = EQResult1.get(0).get("MACHINENAME").toString();
					if(machineNameNew.equals(machineName))
					{
						checkEQFlag = true;
					}
					if(!checkEQFlag)throw new CustomException("LOT-0222", EQResult1.get(0));
				}
			}
	    }
	}	

	@SuppressWarnings("unchecked")
	public static void checkProductProcessInfobyString(List<String> productNameList) throws CustomException
	{
		//List<String> productNameList = new ArrayList<>();
		Map<String, Object> bindMap = new HashMap<String, Object>();

		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		if (productNameList == null || productNameList.size() <= 0)
		{
			log.info("Product is Nor Found");
		}
		else
		{

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT PROCESSINGINFO, PRODUCTNAME ");
			sql.append("  FROM PRODUCT ");
			sql.append(" WHERE PRODUCTNAME IN ( :PRODUCTNAMELIST ) ");
			sql.append("   AND PRODUCTSTATE = 'InProduction' ");
			sql.append("   AND PROCESSINGINFO = 'B' ");

			bindMap.put("PRODUCTNAMELIST", productNameList);

			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult.size() > 0)
			{
				// Clear ProductNameList
				productNameList.clear();

				productNameList = CommonUtil.makeListBySqlResult(sqlResult, "PRODUCTNAME");

				throw new CustomException("LOT-9064", productNameList);
			}
		}
	
	}
	
	@SuppressWarnings("unchecked")
	public static void checkProcessInfobyString(String lotName) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		if (lotName == null)
		{
			log.info("Lot is Not Found");
		}
		else
		{

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT PROCESSINGINFO, PRODUCTNAME ");
			sql.append("  FROM PRODUCT ");
			sql.append(" WHERE LOTNAME = :LOTNAME  ");
			sql.append("   AND PRODUCTSTATE = 'InProduction' ");
			sql.append("   AND PROCESSINGINFO = 'B' ");

			bindMap.put("LOTNAME", lotName);

			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult.size() > 0)
			{
				throw new CustomException("LOT-9066", lotName);
			}
		}
	}
	
	/*
	 * 2020-11-14	dhko	Add Function
	 */
	@SuppressWarnings("unchecked")
	public static void checkProcessInfobyString(String lotName, String machineName,String portName ) throws CustomException
	{
		if (StringUtils.isEmpty(lotName))
		{
			log.info("LotName is empty.");
			return ;
		}
		
		// Determine if ProcessingInfo has 'B' in Product
		List<Product> productDataList = new ArrayList<Product>();
		try
		{
			String condition = "WHERE 1 = 1 "
							 + "  AND LOTNAME = ? "
							 + "  AND PRODUCTSTATE = 'InProduction' "
							 + "  AND PROCESSINGINFO = 'B' ";
			
			productDataList = ProductServiceProxy.getProductService().select(condition, new Object[] { lotName });
		}
		catch (NotFoundSignal nfs)
		{
			log.info("Product data is not exists. PRODUCTSTATE = 'InProduction' AND PROCESSINGINFO = 'B' ");
		}
		catch (Exception ex)
		{
			log.info("Product data is not exists. " + ex.getCause());
		}
		
		if (productDataList != null && productDataList.size() > 0)
		{
			// Determine if the EQP is registered in EnumDef
			boolean isExist = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("CheckProcessingInfoEQP", machineName);
			if (!isExist)
			{
				return ;
			}
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			MachineSpec machineSpecInfo=MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			PortSpec portSpecInfo=MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);
			
			
			// Determine if the EQP is an OnLineReomte
			if (StringUtils.equals(machineSpecInfo.getMachineType(), "ProductionMachine")&&StringUtils.equals(portSpecInfo.getPortType(), "PB")
					&&GenericServiceProxy.getConstantMap().Mac_OnLineRemote.equals(machineData.getCommunicationState()))
			{
				String abortProductNames = StringUtils.EMPTY;
				for (Product productData : productDataList) 
				{
					abortProductNames += productData.getKey().getProductName() + ",";
				}
				abortProductNames = abortProductNames.substring(0, abortProductNames.length() - 1);
				
				// This Lot is aborted. Aborted Product{0}
				throw new CustomException("LOT-9064", abortProductNames);
			}
		}
		else
		{
			/*
			boolean isExist = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("CheckLastMainOperation", machineName);
			if (isExist)
			{
				return ;
			}
			String sql = "SELECT DISTINCT "
					   + "		 NVL(LASTMAINFLOWNAME, 'NONE') AS LASTMAINFLOWNAME, "
					   + "		 NVL(LASTMAINOPERNAME, 'NONE') AS LASTMAINOPERNAME "
					   + "FROM PRODUCT "
					   + "WHERE 1 = 1 "
					   + "  AND LOTNAME = ? "
					   + "  AND PRODUCTIONTYPE IN ('P','E','T') "
					   + "  AND PRODUCTSTATE = 'InProduction' ";
					  // + "  AND PROCESSINGINFO = 'B' ";

			List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { lotName });
			if (resultDataList != null && resultDataList.size() > 1)
			{
				String lastMainFlowNames = StringUtils.EMPTY;
				String lastMainOperNames = StringUtils.EMPTY;
				
				for (OrderedMap resultData : resultDataList) 
				{
					lastMainFlowNames += resultData.get("LASTMAINFLOWNAME").toString() + ",";
					lastMainOperNames += resultData.get("LASTMAINOPERNAME").toString() + ",";
				}
				
				// Thie Lot is mixed. LastMainFlowName[{0}], LastMainOperName[{1}]
				throw new CustomException("PRODUCT-3002", lastMainFlowNames, lastMainOperNames);
			}*/
		}
	}
	
	//Check ProductGrade 20190228
	@SuppressWarnings("unchecked")
	public static void checkProductGradebyString(List<String> productNameList) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		
		if (productNameList == null || productNameList.size() <= 0) {
			log.info("Product is Not Found");
		} else {
			StringBuilder sql = new StringBuilder();
			sql.append("  SELECT PRODUCTGRADE, POSITION ");
			sql.append("  FROM PRODUCT ");
			sql.append("  WHERE PRODUCTNAME IN ( :PRODUCTNAMELIST ) ");
			sql.append("  AND PRODUCTSTATE = 'InProduction' ");
			sql.append("  AND PRODUCTGRADE = 'W' ");

			bindMap.put("PRODUCTNAMELIST", productNameList);

			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult.size() > 0) {
				productNameList.clear();

				productNameList = CommonUtil.makeListBySqlResult(sqlResult,"POSITION");

				throw new CustomException("LOT-9065", productNameList);//LOT-9065: This Lot exists product that productGrade is 'W', Position is {0}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void checkReworkAOILotJudge(Lot lotData) throws CustomException
	{
		log.info(" Rework lot change lot judge ");
		//boolean checkAOIFlag = false;
		String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
		String timeKey = TimeStampUtil.getCurrentEventTimeKey();
		String lotName = lotData.getKey().getLotName();
		
		if( !StringUtils.isNotEmpty(lotData.getUdfs().get("LOTNAME")))
		{
			StringBuilder sql = new StringBuilder();
			sql.append(" WITH AOI ");
			sql.append("  AS (  SELECT LOTNAME, MAX (TIMEKEY) AS TIMEKEY ");
			sql.append("  FROM CT_AOILOT ");
			sql.append("  WHERE LOTNAME = :LOTNAME ");
			sql.append(" GROUP BY LOTNAME ) ");
			sql.append(" SELECT A.LOTNAME,A.LOTJUDGE,A.MACHINENAME,A.EVENTUSER,A.EVENTTIME,A.TIMEKEY ");
			sql.append("   FROM CT_AOILOT A, AOI ");
			sql.append("  WHERE A.LOTNAME = AOI.LOTNAME AND A.TIMEKEY = AOI.TIMEKEY ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			if (sqlResult.size() > 0)
			{
				StringBuilder updateSql = new StringBuilder();
				updateSql.append(" UPDATE CT_AOILOT ");
				updateSql.append("     SET LOTJUDGE = :AOILOTJUDGE, ");
				updateSql.append("   EVENTUSER = :EVENTUSER, ");
				updateSql.append("  EVENTNAME = :EVENTNAME, ");
				updateSql.append("  EVENTTIME = :EVENTTIME, ");
				updateSql.append(" TIMEKEY = :TIMEKEY ");
				updateSql.append("  WHERE TIMEKEY = (SELECT MAX (L.TIMEKEY) ");
				updateSql.append("  FROM CT_AOILOT L ");
				updateSql.append("  WHERE L.LOTNAME = :LOTNAME) ");
				
				Map<String, Object> updateMap = new HashMap<String, Object>();
				updateMap.put("LOTNAME", lotName);
				updateMap.put("AOILOTJUDGE", "Y");
				updateMap.put("EVENTUSER", "MES");
				updateMap.put("EVENTNAME", "ChangeLotJudgeByReworkLot");
				updateMap.put("EVENTTIME", CurrentTime);
				updateMap.put("TIMEKEY", timeKey);

				GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), updateMap);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotJudge-Y", "MES", "ChangeLotJudgeByReworkLot", "", "");
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			}
		} 
	}
	
	
	@SuppressWarnings("unchecked")
	public static boolean checkProductSpecByComsumable ( Consumable crateData) throws CustomException
	{
		boolean checkFlag = false;
		List<Map<String, Object>> sqlResult = null;
		
		try
		{
			String sql = "SELECT TP.PRODUCTSPECNAME, TP.PRODUCTSPECVERSION, TP.FACTORYNAME"
					+ "	FROM TPPOLICY TP, POSBOM PB"
					+ "	WHERE TP.CONDITIONID = PB.CONDITIONID"
					+ "	AND PB.MATERIALSPECNAME = :CONSUMABLESPECNAME"
					+ "	AND PB.MATERIALSPECVERSION = :CONSUMABLESPECVERSION ";
	
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
			bindMap.put("CONSUMABLESPECVERSION", crateData.getConsumableSpecVersion());
	
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch(Exception e)
		{
			sqlResult = null;
		}
		
		if (sqlResult.size() == 0)
			checkFlag = true;
		
		return checkFlag;
	}

	public static void checkReworkLotGrade(Lot lotData, String lotGrade) throws CustomException
	{
		List<String> srcProductList = MESLotServiceProxy.getLotServiceUtil().getProductList(lotData.getKey().getLotName());

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT F.PROCESSFLOWTYPE ");
		sql.append("  FROM PRODUCT P, PROCESSFLOW F ");
		sql.append(" WHERE P.PRODUCTNAME IN ( :PRODUCTNAMELIST) ");
		sql.append("   AND P.PRODUCTSTATE = :PRODUCTSTATE ");
		sql.append("   AND P.FACTORYNAME = F.FACTORYNAME ");
		sql.append("   AND P.PROCESSFLOWNAME = F.PROCESSFLOWNAME ");
		sql.append("   AND P.PROCESSFLOWVERSION = F.PROCESSFLOWVERSION ");
		sql.append("   AND F.PROCESSFLOWTYPE = :PROCESSFLOWTYPE ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAMELIST", srcProductList);
		args.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_InProduction);
		args.put("PROCESSFLOWTYPE", "Rework");
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);
		
		if (result.size() > 0)
		{
			if (lotGrade.equals("R"))
				throw new CustomException("LOT-0322", lotData.getKey().getLotName());
		}

	}
	
	@SuppressWarnings("unchecked")
	public static void checkReturnInfo(Lot trackOutLot) throws CustomException
	{
		log.info("Check Return Info");
		String returnFlowName = trackOutLot.getUdfs().get("RETURNFLOWNAME");
		String returnOperationName = trackOutLot.getUdfs().get("RETURNOPERATIONNAME");
		
		String nodeStack = trackOutLot.getNodeStack();
		
		String originalNode = nodeStack.substring(0, nodeStack.lastIndexOf("."));

		if (originalNode.lastIndexOf(".") > -1)
		{
			String[] nodeStackArray = StringUtils.split(originalNode, ".");
			originalNode = nodeStackArray[nodeStackArray.length - 1];
		}
		
		String sql = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME FROM NODE WHERE NODEID = :NODEID AND NODETYPE = 'ProcessOperation' ";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", originalNode);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		String nextFlowName = "";
		String nextOperationName = "";
		
		if (result.size() > 0)
		{
			nextFlowName = ConvertUtil.getMapValueByName(result.get(0), "PROCESSFLOWNAME");
			nextOperationName = ConvertUtil.getMapValueByName(result.get(0), "NODEATTRIBUTE1");
		}
		
		if (!StringUtils.equals(returnFlowName, nextFlowName) || !StringUtils.equals(returnOperationName, nextOperationName))
		{
			throw new CustomException("MACHINE-0031", nextFlowName, nextOperationName, returnFlowName, returnOperationName);
		}
	}
	
	public static void checkIsOrNotTransferJob(String destinationMachineName,String destinationPositionName ) throws CustomException
	{
		log.info("checkIsOrNotTransferJob");
				
		StringBuilder sql = new StringBuilder();			
		sql.append("SELECT A.TRANSPORTJOBNAME ");			
		sql.append("  FROM CT_TRANSPORTJOBCOMMAND A, DURABLE D ");	
		sql.append(" WHERE     A.JOBSTATE IN ('Started', 'Accepted', 'Requested') ");
		sql.append("       AND (A.CANCELSTATE IS NULL OR A.CANCELSTATE NOT IN ('Completed')) ");
		sql.append("       AND A.CARRIERNAME = D.DURABLENAME ");
		sql.append("       AND D.TRANSPORTLOCKFLAG = 'Y' ");
		sql.append("       AND A.LASTEVENTTIME BETWEEN SYSDATE - 1 AND SYSDATE ");
		sql.append("       AND A.DESTINATIONMACHINENAME = :MACHINENAME ");
		sql.append("       AND A.DESTINATIONPOSITIONNAME = :PORTNAME ");

		Map<String, Object> args = new HashMap<>();
		args.put("MACHINENAME", destinationMachineName);
		args.put("PORTNAME", destinationPositionName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

			
		if (result.size() > 0) 		
		{				
			throw new CustomException("MACHINE-0032",destinationMachineName, destinationPositionName, ConvertUtil.getMapValueByName(result.get(0), "TRANSPORTJOBNAME"));
		}
			
	}
	
	public static void checkMachineIdleTime(Machine machineData) throws CustomException
	{
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineData.getKey().getMachineName());

		if (StringUtils.isNotEmpty(machineSpecData.getUdfs().get("MAXIDLETIME")))
		{
			double maxIdleTime = Double.parseDouble(machineSpecData.getUdfs().get("MAXIDLETIME"));

			String sCurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
			String sLastIdleTime = machineData.getUdfs().get("LASTIDLETIME");
			
			if(isNullOrEmpty(sLastIdleTime))
				return;
			
            String idleTimeCheck= machineSpecData.getUdfs().get("CHECKIDLETIME");
			SimpleDateFormat sf = new SimpleDateFormat(TimeStampUtil.FORMAT_DEFAULT);

			long lCurrentTime = 0;
			long lLastIdleTime = 0;

			try
			{
				lCurrentTime = sf.parse(sCurrentTime).getTime();
				lLastIdleTime = sf.parse(sLastIdleTime).getTime();
			}
			catch (Exception e)
			{

			}

			// Hour unit
			if ((idleTimeCheck!=null&&idleTimeCheck.equals("Y"))&&((lCurrentTime - lLastIdleTime) > maxIdleTime * 3600000))
			{
				throw new CustomException("MACHINE-0038");
			}
		}
	}

	public static void checkMQCFlow(ProcessFlow processFlowData) throws CustomException
	{
		if (!CommonUtil.equalsIn(processFlowData.getProcessFlowType(), "MQCPrepare", "MQC", "MQCRecycle"))
			throw new CustomException("LOT-0144", processFlowData.getProcessFlowType());
	}

	public static void checkProductRequestState(ProductRequest productRequestData) throws CustomException
	{
		if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Released))
		{
			throw new CustomException("WORKORDER-0003", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestState());
		}
	}
	
	public static void checkProductRequestStateCreated(ProductRequest productRequestData) throws CustomException
	{
		if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
		{
			throw new CustomException("WORKORDER-0003", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestState());
		}
	}

	public static void checkProductRequestHoldState(ProductRequest productRequestData, String holdState) throws CustomException
	{
		if (StringUtils.equals(productRequestData.getProductRequestHoldState(), holdState))
		{
			throw new CustomException("WORKORDER-0004", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());
		}
	}
	
	/**
	 * 
	 * AR-AMF-0030-01 Check the existence of MainReserveSkip data
	 * 
	 * @author aim_dhko
	 * @return
	 */
	public static void checkMainReserveSkipData(Lot lotData, String mainOperationName, String mainOperationVersion) throws CustomException
	{
		MainReserveSkip dataInfo = ExtendedObjectProxy.getMainReserveSkipService().getMainResrveSkipData(lotData.getKey().getLotName(), mainOperationName, mainOperationVersion);

		if (dataInfo != null)
		{
			// Same skip action already exist. MainReserveSkip LotName=[{0}]
			// throw new CustomException("LOT-3002", lotData.getKey().getLotName());
			throw new CustomException("LOT-3003", mainOperationName);
		}
	}
	
	public static void check2ndLamination(Port portData, Machine machineData, Durable durableData ) throws CustomException
	{
		PortSpec portSpecData = CommonUtil.getPortSpecInfo(machineData.getKey().getMachineName(), portData.getKey().getPortName());
		
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
		{
			if (StringUtils.equals(portData.getKey().getPortName(), "PL"))
			{
				if (portSpecData.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_OLED))
				{
					if(!StringUtils.equals(durableData.getDurableType(), "OLEDGlassCST"))
						throw new CustomException("DURABLE-0021", durableData.getDurableType());
				}
				else
					throw new CustomException("PORT-1002", portData.getKey().getPortName(), portSpecData.getUdfs().get("UseDurableType"));
			}
			
			if (StringUtils.equals(portData.getKey().getPortName(), "PU"))
			{
				if (portSpecData.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_OLEDFilm))
				{
					if(!StringUtils.equals(durableData.getDurableType(), "FilmCST"))
						throw new CustomException("DURABLE-0021", durableData.getDurableType());
				}
				else
					throw new CustomException("PORT-1002", portData.getKey().getPortName(), portSpecData.getUdfs().get("UseDurableType"));
			}
		}
	}

	public static void checkFilmCST(Port portData, Machine machineData) throws CustomException
	{
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination, GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
		{
			List<Consumable> onEQFilmList = MESConsumableServiceProxy.getConsumableServiceUtil().getOnEQFilmList(machineData, portData.getKey().getPortName());

			if (onEQFilmList!=null&&onEQFilmList.size() < 1)
			{
				throw new CustomException("FILM-0005", machineData.getKey().getMachineName(), portData.getKey().getPortName());
			}
		}
	}
	
	public static void checkLaminationSpec(Lot lotData, Port portData, Machine machineData) throws CustomException
	{
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination)
				||StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
		{
			if(lotData ==null)
			{
				return;
			}
			
			List<Consumable> onEQFilmList = MESConsumableServiceProxy.getConsumableServiceUtil().getOnEQFilmList(machineData, portData.getKey().getPortName());
	
			if(onEQFilmList==null)
			{
				return;
			}
			if (onEQFilmList != null && onEQFilmList.size() > 0)
			{
				Consumable onEQFilm = onEQFilmList.get(0);
				
				//****************************check在AllowedLamiFilmSpec Enum表是否按Lot指定FilmSpec*****************************//
				String enumSql = "SELECT A.ENUMNAME,A.ENUMVALUE,A.DEFAULTFLAG,A.DESCRIPTION,A.DISPLAYCOLOR,A.SEQ " 
					       + " FROM ENUMDEFVALUE A " 
					       + " WHERE A.ENUMNAME = 'AllowedLamiFilmSpec' " 
					       + " AND A.ENUMVALUE = :LOTNAME " ;
				
				Map<String,Object>objectMap = new HashMap<String,Object>();
				objectMap.put("LOTNAME", lotData.getKey().getLotName());
				
				if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination))
				{
					enumSql += " AND A.DEFAULTFLAG = :TOPLAMINATION ";
					objectMap.put("TOPLAMINATION", "TopLamination");
				}
				
				if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
				{
					enumSql += " AND A.SEQ = :BOTTOMLAMITION ";
					objectMap.put("BOTTOMLAMITION", "BottomLamination");
				}
				
				List<Map<String, Object>> enumResult = null;
				try
				{
					enumResult = GenericServiceProxy.getSqlMesTemplate().queryForList(enumSql, objectMap);
				}
				catch (Exception ex)
				{
					log.info("Not Exist Enum:AllowedLamiFilmSpec Data!");
				}
				
				if(enumResult!=null&&enumResult.size()>0)
				{
					if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination))
					{
						if(enumResult.get(0).get("DISPLAYCOLOR").equals(onEQFilm.getConsumableSpecName()))
						{
							return;
						}
						else
						{
							//EnumName:AllowedLamiFilmSpec Master Data not Matched, Lot: {0}, MaterialSpec: {1}, MatchedMaterialSpec: {2}
							throw new CustomException("MATERIAL-0041", lotData.getKey().getLotName(), onEQFilm.getConsumableSpecName(),enumResult.get(0).get("DISPLAYCOLOR"));
						}
					}
					
					if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
					{
						if(enumResult.get(0).get("DESCRIPTION").equals(onEQFilm.getConsumableSpecName()))
						{
							return;
						}
						else
						{
							//EnumName:AllowedLamiFilmSpec Master Data not Matched, Lot: {0}, MaterialSpec: {1}, MatchedMaterialSpec: {2}
							throw new CustomException("MATERIAL-0041", lotData.getKey().getLotName(), onEQFilm.getConsumableSpecName(),enumResult.get(0).get("DESCRIPTION"));
						}
					}
					
				}
				
				//***********************************Check TP+BOM中是否维护ProductSpec与FilmSpec匹配关系*****************************//
				String sql = "SELECT DISTINCT 1 " 
					       + " FROM TPPOLICY TP, POSBOM PB " 
					       + " WHERE TP.CONDITIONID = PB.CONDITIONID " 
					       + " AND TP.FACTORYNAME = :FACTORYNAME " 
					       + " AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME"
					       + " AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION"
					       + " AND PB.MATERIALFACTORYNAME = :MATERIALFACTORYNAME " 
					       + " AND PB.MATERIALSPECNAME = :MATERIALSPECNAME "
					       + " AND PB.MATERIALSPECVERSION = :MATERIALSPECVERSION ";
		
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("FACTORYNAME", lotData.getFactoryName());
				bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
				bindMap.put("MATERIALFACTORYNAME", onEQFilm.getFactoryName());
				bindMap.put("MATERIALSPECNAME", onEQFilm.getConsumableSpecName());
				bindMap.put("MATERIALSPECVERSION", onEQFilm.getConsumableSpecVersion());
		
				List<Map<String, Object>> sqlResult = null;
		
				try
				{
					sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				}
				catch (Exception ex)
				{
					throw new CustomException("SYS-0010", ex.getCause());
				}
		
				if (sqlResult == null || sqlResult.size() < 1)
					throw new CustomException("MATERIAL-0024", lotData.getKey().getLotName(), lotData.getProductSpecName(), onEQFilm.getConsumableSpecName());
			}
		}
	}

	public static void checkShieldLotProcessStateWait(ShieldLot shieldLotData) throws CustomException
	{
		if (!StringUtils.equals(shieldLotData.getLotProcessState(), "WAIT"))
		{
			throw new CustomException("SHIELD-0010", shieldLotData.getShieldLotName());
		}
	}
	
	public static void checkShieldLotHoldStateN(ShieldLot shieldLotData) throws CustomException
	{
		if (!StringUtils.equals(shieldLotData.getLotHoldState(), "N"))
		{
			throw new CustomException("SHIELD-0011", shieldLotData.getShieldLotName());
		}
	}
	
	public static void checkShieldLotHoldStateY(ShieldLot shieldLotData) throws CustomException
	{
		if (!StringUtils.equals(shieldLotData.getLotHoldState(), "Y"))
		{
			throw new CustomException("SHIELD-0019", shieldLotData.getShieldLotName());
		}
	}

	public static void checkShieldCarrierState(Durable durableData) throws CustomException
	{
		if (StringUtils.equals(durableData.getDurableState(), "NotAvailable"))
		{
			throw new CustomException("SHIELD-0013", durableData.getKey().getDurableName(), durableData.getDurableState());
		}
	}
	
	public static void checkShieldCarrierStateAvailable(Durable durableData) throws CustomException
	{
		if (!StringUtils.equals(durableData.getDurableState(), "NotAvailable"))
		{
			throw new CustomException("SHIELD-0013", durableData.getKey().getDurableName(), durableData.getDurableState());
		}
	}

	public static void checkConsumableState(Consumable consumableData) throws CustomException
	{
		if (!StringUtils.equals(consumableData.getConsumableState(), "Available"))
		{
			throw new CustomException("CONSUMABLE-0001", consumableData.getKey().getConsumableName(), consumableData.getConsumableState());
		}
	}
	
	public static void checkMaskLotHoldState(MaskLot maskLotData) throws CustomException
	{
		if(StringUtils.equals(maskLotData.getMaskLotHoldState(), GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
		{
			throw new CustomException("MASK-0013",maskLotData.getMaskLotName() );
		}
	}
	
	public static void checkMaskLotState(MaskLot maskLotData) throws CustomException
	{
		if(!StringUtils.equals(maskLotData.getMaskLotState(), GenericServiceProxy.getConstantMap().MaskLotState_Released))
		{
			throw new CustomException("MASK-0081");
		}
	}
	
	public static void checkMaskLotReworkState(MaskLot maskLotData) throws CustomException
	{
		if(!StringUtils.equals(maskLotData.getMaskLotState(), GenericServiceProxy.getConstantMap().MaskLotReworkState_InRework))
		{
			throw new CustomException("MASK-0086");
		}
	}
	
	public static void checkMaskLotProcessStateRun(MaskLot masklotData) throws CustomException
	{
		if(StringUtils.equals(masklotData.getMaskLotProcessState(), GenericServiceProxy.getConstantMap().MaskLotProcessState_Run))
		{
			throw new CustomException("MASK-0079");
		}
	}
	
	public static void checkMaskLotProcessStateNotRun(MaskLot masklotData) throws CustomException
	{
		if(!StringUtils.equals(masklotData.getMaskLotProcessState(), GenericServiceProxy.getConstantMap().MaskLotProcessState_Run))
		{
			throw new CustomException("MASK-0080");
		}
	}
	
	public static void checkProductQueueTime(Lot lotData) throws CustomException
	{
		List<ProductQueueTime> prodQtimeList = new ArrayList<ProductQueueTime>();

		String condition = " LOTNAME = ? AND QUEUETIMESTATE = ?";
		Object[] bindSet = new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().QTIME_STATE_OVER };

		try
		{
			prodQtimeList = ExtendedObjectProxy.getProductQTimeService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			prodQtimeList = null;
		}

		if (prodQtimeList != null)
		{
			throw new CustomException("QTIME-0001");
		}
	}
	
	public static void checkOriginalProduct(Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT CO.PRODUCTNAME ");
		sql.append("  FROM LOT L, PRODUCT P, CT_ORIGINALPRODUCTINFO CO ");
		sql.append(" WHERE L.LOTNAME = P.LOTNAME ");
		sql.append("   AND P.CHANGESHOPLOTNAME = CO.LOTNAME ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");
		sql.append("GROUP BY CO.PRODUCTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotData.getKey().getLotName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		List<String> prodNameList = new ArrayList<String>();

		for (Map<String, Object> prod : sqlResult)
		{
			String productName = ConvertUtil.getMapValueByName(prod, "PRODUCTNAME");
			prodNameList.add(productName);
		}

		if (sqlResult.size() > 0)
		{
			throw new CustomException("LOT-0306", prodNameList);
		}
	}

	public static void checkMQCDummyUsedCount(Lot lotData) throws CustomException
	{
		if (lotData != null)
		{
			if (StringUtils.equals(lotData.getProductionType(), "M") || StringUtils.equals(lotData.getProductionType(), "D"))
			{
				StringBuffer sql = new StringBuffer();
				Map<String, Object> args = new HashMap<String, Object>();
				sql.setLength(0);
				sql.append("SELECT DISTINCT A.PRODUCTNAME,B.DUMMYUSEDCOUNT,B.MQCPRODUCTUSEDLIMIT ");
				sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED A , PRODUCT B,CT_MQCPLAN C WHERE 1=1 ");
				sql.append(" AND A.PRODUCTNAME = B.PRODUCTNAME AND B.PRODUCTSTATE = 'InProduction' ");
				sql.append("   AND A.JOBNAME = C.JOBNAME AND C.MQCSTATE IN ('Released','Recycling') ");
				sql.append("   AND A.LOTNAME = :LOTNAME ");				

				args.clear();
				args.put("LOTNAME", lotData.getKey().getLotName());
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> prodList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				
				if(prodList.size()>0)
				{
					for(int i=0;i<prodList.size();i++)
					{
						int dummyUsedCount = 0;
						int mqcProductUsedLimit = 0;
						try
						{
							dummyUsedCount = Integer.parseInt(prodList.get(i).get("DUMMYUSEDCOUNT").toString());
						}
						catch(Exception ex)
						{
							log.info("Product:"+prodList.get(i).get("PRODUCTNAME")+" ,DummyUseCount is null");
						}
						
						try
						{
							mqcProductUsedLimit = Integer.parseInt(prodList.get(i).get("MQCPRODUCTUSEDLIMIT").toString());
						}
						catch(Exception ex)
						{
							log.info("Product:"+prodList.get(i).get("PRODUCTNAME")+" ,mqcProductUsedLimit is null");
						}
			
						if (dummyUsedCount >= mqcProductUsedLimit)
						{
							throw new CustomException("LOT-0307", prodList.get(i).get("PRODUCTNAME").toString(), dummyUsedCount, mqcProductUsedLimit);
						}												
					}
				}							
			}
		}
	}
	
	public static void checkIFIProcessing(String productName) throws CustomException
	{
		OriginalProductInfo oriProdInfo = ExtendedObjectProxy.getOriginalProductInfoService().getOriginalProductInfoList(productName);

		if (oriProdInfo != null)
			throw new CustomException("LOT-0309", productName);
	}

	public static void checkSampleData(Lot lotData) throws CustomException
	{
		List<SampleLot> sampleList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName());

		if (sampleList != null)
			throw new CustomException("SAMPLE-0003", lotData.getKey().getLotName());
	}
	
	public static void checkFutureActionData(Lot lotData) throws CustomException
	{
		List<LotFutureAction> futureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionList(lotData.getKey().getLotName());

		if (futureActionList != null)
			throw new CustomException("LOTFUTUREACTION-0001", lotData.getKey().getLotName());
	}
	
	public static void checkDummyProductReserve(Lot lotData) throws CustomException
	{
		if (lotData == null)
			return;

		List<DummyProductReserve> reserveList = ExtendedObjectProxy.getDummyProductReserveService().getDummyProductReserveDataList(lotData.getKey().getLotName());

		if (reserveList != null)
			throw new CustomException("DUMMYPRODUCT-0002", lotData.getKey().getLotName());
	}

	public static void checkDummyProductReserveOper(Lot lotData) throws CustomException
	{
		if(lotData == null)
			return;
		
		boolean flag = ExtendedObjectProxy.getDummyProductReserveService().checkDummyProductReserveData(lotData.getKey().getLotName());

		if (flag)
		{
			ProcessFlow flowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

			if (StringUtils.equals(flowData.getProcessFlowType(), "Sort"))
			{
				if (!StringUtils.equals(lotData.getUdfs().get("MERGEABLEFLAG"), "Y"))
					throw new CustomException("DUMMYPRODUCT-0004", lotData.getKey().getLotName());
			}
//			else
//			{
//				List<Map<String, Object>> operaionList = CommonUtil.getOperationLevelList(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
//				DummyProductReserve dataInfo = ExtendedObjectProxy.getDummyProductReserveService().getMaxOperationName(lotData.getKey().getLotName());
//
//				int currentLevel = 0;
//				int reserveLevel = 0;
//
//				for (Map<String, Object> operation : operaionList)
//				{
//					int level = Integer.parseInt(ConvertUtil.getMapValueByName(operation, "LV"));
//					String operationName = ConvertUtil.getMapValueByName(operation, "PROCESSOPERATIONNAME");
//
//					if (StringUtils.equals(operationName, lotData.getProcessOperationName()))
//						currentLevel = level;
//
//					if (StringUtils.equals(operationName, dataInfo.getProcessOperationName()))
//						reserveLevel = level;
//
//					if (currentLevel != 0 && reserveLevel != 0)
//						break;
//				}
//
//				if (currentLevel > reserveLevel)
//					throw new CustomException("LOT-0324", lotData.getKey().getLotName());
//			}
		}
	}

	public static void checkDummyGlassFlag(Lot lotData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		
		for (Product productData : productList)
		{
			if (StringUtils.equals(productData.getUdfs().get("DUMMYGLASSFLAG"), "Y"))
				throw new CustomException("LOT-0310", productData.getKey().getProductName());
		}
	}

	public static void checkSortJob(Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT CC.JOBNAME, CC.CARRIERNAME, CC.LOTNAME, CS.JOBSTATE ");
		sql.append("  FROM CT_SORTJOB CS, CT_SORTJOBCARRIER CC, LOT L ");
		sql.append(" WHERE CS.JOBNAME = CC.JOBNAME ");
		sql.append("   AND CC.LOTNAME = L.LOTNAME ");
		sql.append("   AND CS.JOBSTATE NOT IN ('ENDED', 'CANCELED', 'ABORT') ");
		sql.append("   AND CC.LOTNAME = :LOTNAME ");
		sql.append("UNION ");
		sql.append("SELECT CC.JOBNAME, CC.CARRIERNAME, CC.LOTNAME, CS.JOBSTATE ");
		sql.append("  FROM CT_SORTJOB CS, CT_SORTJOBCARRIER CC, LOT L ");
		sql.append(" WHERE CS.JOBNAME = CC.JOBNAME ");
		sql.append("   AND CC.CARRIERNAME = L.CARRIERNAME ");
		sql.append("   AND CS.JOBSTATE NOT IN ('ENDED', 'CANCELED', 'ABORT') ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");
		sql.append("GROUP BY CC.JOBNAME, CC.CARRIERNAME, CC.LOTNAME, CS.JOBSTATE ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			String jobName = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBNAME");
			String lotName = ConvertUtil.getMapValueByName(sqlResult.get(0), "LOTNAME");
			String carrierName = ConvertUtil.getMapValueByName(sqlResult.get(0), "CARRIERNAME");
			
			throw new CustomException("SORT-0007", jobName, lotName, carrierName);
		}
	}
	
	public static void checkSortJobState(Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SJ.JOBNAME, SJ.JOBSTATE ");
		sql.append("  FROM CT_SORTJOB SJ, CT_SORTJOBPRODUCT SP ");
		sql.append(" WHERE SJ.JOBNAME = SP.JOBNAME ");
		sql.append("   AND SJ.JOBSTATE IN ('STARTED', 'CONFIRMED') ");
		sql.append("   AND (SP.FROMLOTNAME = :FROMLOTNAME ");
		sql.append("     OR SP.TOLOTNAME = :TOLOTNAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FROMLOTNAME", lotData.getKey().getLotName());
		args.put("TOLOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			String jobName = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBNAME");
			String jobState = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBSTATE");
			
			throw new CustomException("SORT-0008", jobName, jobState);
		}
	}
	
	public static void checkDetailOperationType(Lot lotData, String detailProcessOperationType) throws CustomException
	{
		ProcessOperationSpec operData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (!StringUtils.equals(operData.getDetailProcessOperationType(), detailProcessOperationType))
		{
			throw new CustomException("SORT-0010", lotData.getKey().getLotName(), detailProcessOperationType);
		}
	}
	
	public static void checkVirtualMachine(MachineSpec machineSpecData) throws CustomException
	{
		if (!StringUtils.equals(machineSpecData.getMachineType(), "VirtualMachine"))
		{
			throw new CustomException("MACHINE-0110", machineSpecData.getKey().getMachineName());
		}
	}
	
	public static void checkOLEDSortCarrier(Machine machineData, Durable durableData, Lot lotData) throws CustomException
	{
		if (StringUtils.equals(machineData.getFactoryName(), "OLED") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
		{
			String operationMode = machineData.getUdfs().get("OPERATIONMODE");
			String durableType = durableData.getDurableType();

			if ((StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT) && StringUtils.equals(durableType, "OLEDGlassCST") && lotData != null)
					|| (StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE) && StringUtils.equals(durableType, "TPGlassCST") && lotData != null))
			{
				throw new CustomException("MACHINE-0111", operationMode, durableData.getKey().getDurableName(), durableType);
			}
		}
	}
	
	public static void checkProductGrade(Lot lotData, List<Product> productList) throws CustomException
	{
		for (Product productData : productList)
		{
			String productGrade = productData.getProductGrade();

			if(StringUtils.equals(productGrade, GenericServiceProxy.getConstantMap().ProductGrade_S))
			{
				throw new CustomException("PRODUCT-9017", lotData.getKey().getLotName(), productData.getKey().getProductName());
			}
		}		
	}
	
	public static void checkOvenSortMode(Machine machineData,Port portData,Lot lotData) throws CustomException
	{
		String operationMode = machineData.getUdfs().get("OPERATIONMODE");
		String machineGroupName = machineData.getMachineGroupName();
		if(lotData != null)
		{
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
			if(machineGroupName.equals(GenericServiceProxy.getConstantMap().MachineGroup_Oven)
					&&(operationMode.equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE)||
							CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
					&& !processFlowData.getProcessFlowType().equals("Sort"))
			{
				throw new CustomException("MACHINE-0047", machineData.getKey().getMachineName(), portData.getUdfs().get("PORTTYPE"),operationMode,processFlowData.getProcessFlowType());
			}
		}
		else
		{
			return;
		}
		
	}
	
	public static void checkBaseLineLot(Lot lotData) throws CustomException
	{
	    ProductRequest woData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
	    if(StringUtils.equals(woData.getUdfs().get("SUBPRODUCTIONTYPE"), "P")
	    		||StringUtils.equals(woData.getUdfs().get("SUBPRODUCTIONTYPE"), "ESLC"))
		{
			throw new CustomException("PRODUCT-9018",woData.getUdfs().get("SUBPRODUCTIONTYPE") );
		}  
	}
	public static void checkProductionTypebyPort(Lot lotData,Machine machineData,Port portData) throws CustomException
	{
		String machineGroupName = machineData.getMachineGroupName();
		if (machineGroupName.equals(GenericServiceProxy.getConstantMap().MachineGroup_EVA)){
			if (StringUtils.equals(portData.getUdfs().get("PORTTYPE"), "PB")&&!StringUtils.equals(lotData.getProductionType(), "M"))
			{
				throw new CustomException("PORT-1004", portData.getUdfs().get("PORTTYPE"),lotData.getProductionType());
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static void checkRSMeterLot(String lotName, Machine machineData ) throws CustomException
	{
		if (StringUtils.isEmpty(lotName))
		{
			log.info("LotName is empty.");
			return ;
		}
		if(!StringUtils.equals(machineData.getMachineGroupName(), "RS"))
		{
			String sql = "SELECT R.PRODUCTNAME,P.TURNCOUNT FROM PRODUCT P,CT_RSCHECKLOTINFO R "
					   + "		 WHERE P.PRODUCTNAME = R.PRODUCTNAME "
					   + "		 AND P.LOTNAME=:LOTNAME "
					   + "		 AND R.CHECKTYPE='RSMeter' "
					   + "       AND MOD(P.TURNCOUNT,2)=1 ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotName);
			List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if (resultDataList != null && resultDataList.size() > 0)
			{
				throw new CustomException("LOT-1016");
			}
		}
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		StringBuffer sqlForDelete = new StringBuffer();
		sqlForDelete.append("DELETE CT_RSCHECKLOTINFO WHERE PRODUCTNAME=? AND CHECKTYPE='Normal'");
		
		String sql = "SELECT R.PRODUCTNAME,MOD(NVL(P.TURNCOUNT,0)-NVL(R.TURNCOUNT,0),2) AS TURNFLAG FROM PRODUCT P,CT_RSCHECKLOTINFO R "
				   + "		 WHERE P.PRODUCTNAME = R.PRODUCTNAME "
				   + "		 AND P.LOTNAME=:LOTNAME "
				   + "		 AND R.CHECKTYPE='Normal' "
				   + "		 AND P.PROCESSOPERATIONNAME=R.RETURNPROCESSOPERATIONNAME ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		if (resultDataList != null && resultDataList.size() > 0)
		{
			for(int i=0;i<resultDataList.size();i++)
			{
				if(StringUtils.equals(resultDataList.get(i).get("TURNFLAG").toString(), "1"))
				{
					throw new CustomException("LOT-1016");
				}
				else
				{
					List<Object> lotBindList = new ArrayList<Object>();
					
					lotBindList.add(resultDataList.get(i).get("PRODUCTNAME").toString());
					
					updateLotArgList.add(lotBindList.toArray());
				}
			}
			if(updateLotArgList!=null&&updateLotArgList.size()>0)
			{
				MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlForDelete.toString(), updateLotArgList);
			}
		}
			
		

	}

	public static void checkFirstGlassLotOffset(Lot lotData, Machine machineData, String offsetResult) throws CustomException
	{
		log.info(" Check FisrtGlass Lot Start ");
		boolean checkFlag = false;
		String jobName = "";
		
		if(StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
		{
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			{
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT P.MACHINENAME, T.FACTORYNAME, T.PRODUCTSPECNAME, T.PROCESSFLOWNAME, T.PROCESSOPERATIONNAME");
				sql.append(" FROM TPFOPOLICY T, POSMACHINE P");
				sql.append(" WHERE 1=1");
				sql.append(" AND T.CONDITIONID = P.CONDITIONID");
				sql.append(" AND T.FACTORYNAME= :FACTORYNAME");
				sql.append(" AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME");
				sql.append(" AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION");
				sql.append(" AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME");
				sql.append(" AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION");
				sql.append(" AND T.PROCESSOPERATIONNAME= :PROCESSOPERATIONNAME");
				sql.append(" AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION");
		
				Map<String, String> args = new HashMap<String, String>();
				args.put("FACTORYNAME", lotData.getFactoryName());
				args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
				args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
				args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
				args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		
				List<Map<String, Object>> policyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				
				if (policyResult.size() > 0)
				{
					StringBuilder jobsql = new StringBuilder();
					jobsql.append(" SELECT JOBNAME FROM CT_FIRSTGLASSJOB B");
					jobsql.append(" WHERE B.FACTORYNAME = :FACTORYNAME");
					jobsql.append(" AND B.PRODUCTSPECNAME = :PRODUCTSPECNAME");
					jobsql.append(" AND B.PROCESSFLOWNAME = :PROCESSFLOWNAME");
					jobsql.append(" AND B.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME");
					jobsql.append(" AND B.MACHINENAME = :MACHINENAME");
					jobsql.append(" AND JOBSTATE <> 'Completed'");
					jobsql.append(" AND JOBSTATE <> 'Canceled'");
					jobsql.append(" AND (JUDGE <> 'Pass' or JUDGE IS NULL )");
					jobsql.append(" AND (JUDGE <> 'Strip' or JUDGE IS NULL )");
					jobsql.append(" AND OFFSET = :OFFSET ");
		
					args.clear();
					args.put("FACTORYNAME", policyResult.get(0).get("FACTORYNAME").toString());
					args.put("PRODUCTSPECNAME", policyResult.get(0).get("PRODUCTSPECNAME").toString());
					args.put("PROCESSFLOWNAME", policyResult.get(0).get("PROCESSFLOWNAME").toString());
					args.put("PROCESSOPERATIONNAME", policyResult.get(0).get("PROCESSOPERATIONNAME").toString());
					args.put("MACHINENAME", machineData.getKey().getMachineName());
					args.put("OFFSET", offsetResult);
					
					List<Map<String, Object>> jobResult = GenericServiceProxy.getSqlMesTemplate().queryForList(jobsql.toString(), args);
					
					if(jobResult.size() > 0)
					{
						jobName = jobResult.get(0).get("JOBNAME").toString();
						checkFlag = true;
					}
				}
				if(checkFlag)
				{
					throw new CustomException("LOT-0156", jobName, offsetResult);
				}
			}
		}
	}
	
	public static void checkSorterSignReserve(Lot lotData) throws CustomException
	{
		log.info(" Check CT_SorterSignReserve Start ");
		boolean checkFlag = false;
		String nodeStack = lotData.getNodeStack();
		String lotName = lotData.getKey().getLotName();
		
		if (nodeStack.indexOf(".") < 0)
		{
			Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStack);
			
			ProcessFlow flowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(new ProcessFlowKey(currentNode.getFactoryName(), currentNode.getProcessFlowName(), currentNode.getProcessFlowVersion()));
			List<Map<String, String>> errorInfo = new ArrayList<Map<String, String>>();
			if (flowData.getProcessFlowType().equals("Main"))
			{
				List<SorterSignReserve> checkList = ExtendedObjectProxy.getSorterSignReserveService().getDataInfoListByLotName(lotName);
				
				if (checkList != null && checkList.size() > 0)
				{
					for (SorterSignReserve sorterSignReserve : checkList) 
					{
						try 
						{
							Node reserveNode = ProcessFlowServiceProxy.getNodeService().getNode(currentNode.getFactoryName(), 
									sorterSignReserve.getProcessFlowName(), currentNode.getProcessFlowVersion(), "ProcessOperation", 
									sorterSignReserve.getProcessOperationName(), "00001");
							
							if (reserveNode.getYCoordinate() <= currentNode.getYCoordinate())
							{
								Map<String, String> error = new HashMap<String, String>();
								error.put("PRODUCTNAME", sorterSignReserve.getProductName());
								error.put("CURRENTOPRATIONNAME", currentNode.getNodeAttribute1());
								error.put("RESERVEOPRATIONNAME", sorterSignReserve.getProcessOperationName());
								error.put("RESERVEUSER", sorterSignReserve.getReserveUser());
								
								errorInfo.add(error);
							}
						} 
						catch (Exception e) 
						{
							log.info("Can't Find Node ,Skip Check CT_SorterSignReserve");
						}
					}
					
					if (errorInfo != null && errorInfo.size()>0)
					{
						log.info(errorInfo.toString());
						throw new CustomException("WC-0001", errorInfo.get(0).toString());
					}
				}
				
			}
		}
		log.info(" Check CT_SorterSignReserve End ");
	}
	
	public static void check2ndLaminationVendor(Lot lotData,Machine machineData) throws CustomException 
	{
		if (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
		{
			return;
		}
		
		if(lotData == null)
		{
			return;
		}
		//******************************上下膜厂商一致Check开关*********************************************//
		List<Map<String,Object>>EnumDefDataList  = EnumDefValueList("FunctionSwitch","check2ndLaminationVendor");
		if(EnumDefDataList!=null&&EnumDefDataList.size()>0)
		{
			if(EnumDefDataList.get(0).get("DEFAULTFLAG").equals("N"))
			{
				return;
			}
		}
		//******************************获得机台上在制程FilmData信息******************************************//
		List<Consumable> consumableDataList = null;
		try
		{
			String condition = " WHERE CARRIERNAME IN (SELECT DURABLENAME FROM DURABLE WHERE 1=1 AND MACHINENAME = ? AND DURABLETYPE = 'FilmBox'  AND UPPER(TRANSPORTSTATE) = 'ONEQP')";
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineData.getKey().getMachineName()});
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format("No film loaded on macine.[MachineName = %s] ", machineData.getKey().getMachineName()));
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}
		
		if(consumableDataList==null||consumableDataList.size()<1)
		{
			return;
		}
		//**********************************上贴膜与下贴膜厂商必须一致Check************************************//
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT DISTINCT C.VENDOR ");
		sql.append(" FROM CT_MATERIALPRODUCT A ,PRODUCT B,CONSUMABLESPEC C,CONSUMABLE D WHERE 1=1 ");
		sql.append(" AND D.CONSUMABLESPECNAME = C.CONSUMABLESPECNAME ");
		sql.append(" AND C.CONSUMABLETYPE IN ('TopLamination','BottomLamination') ");
		sql.append(" AND A.MATERIALNAME = D.CONSUMABLENAME ");
		sql.append(" AND A.TIMEKEY > TO_CHAR(SYSDATE-30,'yyyyMMddHH24miss') ");
		sql.append(" AND A.PRODUCTNAME = B.PRODUCTNAME ");
		sql.append(" AND B.LOTNAME = :LOTNAME ");
		sql.append(" AND C.FACTORYNAME = 'OLED' ");
		
		Map<String,Object>bindMap = new HashMap<String,Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		List<Map<String,Object>>sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		if(sqlResult.size()>1)
		{
			//FILM-0012  CST：[{0}]，contains more than 2 vendor's firstLamination，Please Cancel the Glass CST and Spilt lot by vendor
			throw new CustomException("FILM-0012",lotData.getCarrierName());
		}
		else if(sqlResult.size()>0)
		{
			String topLaminationVendor = sqlResult.get(0).get("VENDOR").toString();
			List<String>consumableSpecList = new ArrayList<>();
			for(int i = 0;i<consumableDataList.size();i++)
			{
				String consumableSpec = consumableDataList.get(i).getConsumableSpecName();
				if(!consumableSpecList.contains(consumableSpec))
				{
					consumableSpecList.add(consumableSpec);
				}
			}
			
			StringBuilder consumableSpecSQL = new StringBuilder();
			consumableSpecSQL.append(" SELECT DISTINCT A.VENDOR FROM CONSUMABLESPEC A WHERE 1=1 ");
			consumableSpecSQL.append(" AND A.CONSUMABLESPECNAME IN ( :CONSUMABLESPECNAME) ");
			consumableSpecSQL.append(" AND A.CONSUMABLETYPE IN ('TopLamination','BottomLamination') ");
			consumableSpecSQL.append(" AND A.FACTORYNAME = 'OLED' ");
			
			Map<String,Object>consumableMap = new HashMap<String,Object>();
			consumableMap.put("CONSUMABLESPECNAME", consumableSpecList);
			List<Map<String,Object>>consumableResult = GenericServiceProxy.getSqlMesTemplate().queryForList(consumableSpecSQL.toString(), consumableMap);
			if(consumableResult.size()>1)
			{
				//FILM-0013 FilmBox：[(0)]，contains more than 2 vendor's film，can not process on EQP
				throw new CustomException("FILM-0013",consumableDataList.get(0).getUdfs().get("CARRIERNAME"));
			}
			else if(consumableResult.size()>0)
			{
				String bottomLaminationVendor = consumableResult.get(0).get("VENDOR")==null?null:consumableResult.get(0).get("VENDOR").toString();
				if(!StringUtils.equals(topLaminationVendor, bottomLaminationVendor))
				{
					//FILM-0014  The film vendor of  TopLamination and bottomLamination is different,The TopLamination vendor of  GlassCST：[{0}]  is：[{1}]，The BottomLamination vendor of  FilmCST：[{2}]  is：[{3}],
					throw new CustomException("FILM-0014",lotData.getCarrierName(),topLaminationVendor,consumableDataList.get(0).getUdfs().get("CARRIERNAME"),bottomLaminationVendor);
				}
			}
		}
	}
	
	public static List<Map<String,Object>>EnumDefValueList (String EnumName,String EnumValue)throws CustomException 
	{
		List<Map<String,Object>>sqlResult = null;
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT * FROM ENUMDEFVALUE A WHERE 1=1 ");
		sql.append(" AND A.ENUMNAME = :ENUMNAME ");
		sql.append(" AND (A.ENUMVALUE = :ENUMVALUE OR :ENUMVALUE IS NULL) ");
		
		Map<String,Object>bindMap = new HashMap<String,Object>();
		bindMap.put("ENUMNAME", EnumName);
		bindMap.put("ENUMVALUE", EnumValue);
		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch(Exception ex)
		{
			log.info("EnumName: "+EnumName+",EnumValue: "+EnumValue+",not found!");
		}
		
		return sqlResult;
	}
	
	public static void checkReworkLimit(Lot lotData) throws CustomException
	{
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		if (StringUtils.equals(processFlowData.getProcessFlowType(),"Rework")
				||StringUtils.equals(processFlowData.getProcessFlowType(),"Strip"))
		{
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
			if(!StringUtils.equals(operationData.getProcessOperationType(), "Production"))
			{
				return;
			}
			boolean abortFlag=false;
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT P.PRODUCTNAME,NVL(P.PROCESSINGINFO,'N') AS PROCESSINGINFO,NVL(R.PRODUCTNAME,'NULL') AS REWORKPRODUCT  ");
			sql.append(" FROM PRODUCT P LEFT JOIN CT_REWORKPRODUCT R   ");
			sql.append("  ON P.PRODUCTNAME = R.PRODUCTNAME ");
			sql.append(" AND R.REWORKTYPE=:PROCESSOPERATIONNAME ");
			sql.append(" AND R.REWORKCOUNT>TO_NUMBER(R.REWORKCOUNTLIMIT) ");
			sql.append(" WHERE P.LOTNAME=:LOTNAME ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
			args.put("LOTNAME", lotData.getKey().getLotName());

			List<Map<String, Object>> productList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (productList!=null&&productList.size() > 0)
			{
				for(int i=0;i<productList.size();i++)
				{
					if(StringUtils.equals(productList.get(i).get("PROCESSINGINFO").toString(), "B")
							&&!StringUtils.equals(productList.get(i).get("REWORKPRODUCT").toString(), "NULL"))
					{
						throw new CustomException("LOT-0328",productList.get(i).get("PRODUCTNAME").toString());
					}
					if(StringUtils.equals(productList.get(i).get("PROCESSINGINFO").toString(), "B"))
					{
						abortFlag=true;
					}
				}
				if(!abortFlag)
				{
					for(int i=0;i<productList.size();i++)
					{
						if(!StringUtils.equals(productList.get(i).get("REWORKPRODUCT").toString(), "NULL"))
						{
							throw new CustomException("LOT-0328",productList.get(i).get("PRODUCTNAME").toString());
						}
					}
				}
				
			}
		}

		return ;
	}
	
	public static void checkFirstGlassLot(Lot lotData) throws CustomException
	{			
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME FROM LOT L  ");
		sql.append("   WHERE 1=1   ");
		sql.append("    AND L.LOTNAME = :LOTNAME ");
		sql.append("    AND L.JOBNAME IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotData.getKey().getLotName());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result!=null&&result.size() > 0)
		{
			throw new CustomException("LOT-0047",lotData.getKey().getLotName());				
		}

		return ;
	}
	
	public static void checkAutoSorterSpec_Flow_Operation_ProductRequest(Machine machineData,Durable durableData,Lot lotData) throws CustomException
	{		
		String operationMode = machineData.getUdfs().get("OPERATIONMODE");
		String durableType = "";
		if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT))
		{
			durableType = "TPGlassCST";
		}
		else if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE))
		{
			durableType = "OLEDGlassCST";
		}
		else
		{
			return;
		}
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT L.PRODUCTSPECNAME,L.PROCESSFLOWNAME,L.PROCESSOPERATIONNAME,L.PRODUCTREQUESTNAME ");
		sql.append("FROM LOT L,DURABLE D,TPFOPOLICY TPFO,POSMACHINE POS,MACHINE M ");
		sql.append("WHERE 1=1 ");
		sql.append("AND L.FACTORYNAME = 'OLED' ");
		sql.append("AND ((D.MACHINENAME = M.MACHINENAME ");
		sql.append("AND UPPER(D.TRANSPORTSTATE) = 'ONEQP' ");
		sql.append("AND D.DURABLETYPE =:DURABLETYPE ");
		sql.append("AND D.DURABLESTATE = 'InUse') OR L.LOTPROCESSSTATE = 'RUN') ");
		sql.append("AND L.CARRIERNAME = D.DURABLENAME(+) ");
		sql.append("AND L.FACTORYNAME = TPFO.FACTORYNAME ");
		sql.append("AND L.PRODUCTSPECNAME = TPFO.PRODUCTSPECNAME ");
		sql.append("AND L.PRODUCTSPECVERSION = TPFO.PRODUCTSPECVERSION ");
		sql.append("AND L.PROCESSFLOWNAME = TPFO.PROCESSFLOWNAME ");
		sql.append("AND L.PROCESSFLOWVERSION = TPFO.PROCESSFLOWVERSION ");
		sql.append("AND L.PROCESSOPERATIONNAME = TPFO.PROCESSOPERATIONNAME ");
		sql.append("AND L.PROCESSOPERATIONVERSION = TPFO.PROCESSOPERATIONVERSION ");
		sql.append("AND TPFO.CONDITIONID = POS.CONDITIONID ");
		sql.append("AND POS.MACHINENAME = M.MACHINENAME ");
		sql.append("AND M.MACHINENAME = :MACHINENAME ");		

		Map<String, Object> args = new HashMap<String, Object>();
		
		args.put("DURABLETYPE",durableType);
		args.put("MACHINENAME", machineData.getKey().getMachineName());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result!=null&&result.size() > 1)
		{
			throw new CustomException("LOT-0157",machineData.getKey().getMachineName());				
		}
		else if(result!=null&&result.size() == 1)
		{
			Object productSpecName = result.get(0).get("PRODUCTSPECNAME");
			Object processFlowName = result.get(0).get("PROCESSFLOWNAME");
			Object processOperationName = result.get(0).get("PROCESSOPERATIONNAME");
			Object productRequestName = result.get(0).get("PRODUCTREQUESTNAME");
			if(!(lotData.getProductSpecName().equals(productSpecName)&&lotData.getProcessFlowName().equals(processFlowName)
					&&lotData.getProcessOperationName().equals(processOperationName)&&lotData.getProductRequestName().equals(productRequestName)))
			{
				throw new CustomException("LOT-0158",productSpecName,processFlowName,processOperationName,productRequestName,
						lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName(),lotData.getProductRequestName());	
			}
		}

		return ;
	}
}