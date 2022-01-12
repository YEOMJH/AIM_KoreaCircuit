package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.master.EnumDef;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class TrayGroupInfoDownloadRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupInfoDownloadSend");

			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
			String	machineRecipeName="";
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);

			CommonValidation.CheckDurableState(trayGroupData);
			CommonValidation.CheckDurableHoldState(trayGroupData);
			CommonValidation.checkMachineHold(machineData);

			if (!StringUtils.equals(trayGroupData.getDurableType(), "CoverTray"))
			{
				// DURABLE-0009:This TrayGroup [{0}] is not CoverTray
				throw new CustomException("DURABLE-0009", trayGroupName);
			}

			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,false);
			String lastTrayName=trayList.get(0).getKey().getDurableName();
			
			if (trayList == null || trayList.size()==0 )
			{
				// TRAY-0009:No tray data was found for the {0} tray group.
				new CustomException("TRAY-0009", trayGroupName);
			}
			
			// CIM feedback firstRunQty is managed by the machine itself。
			int firstRunQty = StringUtil.isEmpty(trayGroupData.getUdfs().get("FIRSTRUNQTY")) ? 0 : Integer.valueOf(trayGroupData.getUdfs().get("FIRSTRUNQTY"));

			if (StringUtils.equals(portType, constantMap.PORT_TYPE_PL))
			{
				if (!StringUtil.equals(trayGroupData.getDurableState(), constantMap.Dur_InUse))
				{
					// DURABLE-9006:Invalid TrayGroupState[{0}] by TrayGroup[{1}]
					throw new CustomException("DURABLE-9006", trayGroupData.getDurableState(), trayGroupName);
				}

				List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList, false);
				List<Lot> firstTrayLotList=MESLotServiceProxy.getLotServiceUtil().getLotDataListByTray(lastTrayName);

				//Check ERPBOM 2020-10-15
				Lot lotData = lotDataList.get(0);
				
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
				
				/*if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
				{
					MESConsumableServiceProxy.getConsumableServiceUtil().compareERPBOM(lotData.getFactoryName(), productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, lotData.getProductSpecName());
				}*///2021/1/28 屏蔽ERPBOM
				
				// DURABLE-9004:No panel assigned to TrayGroup[{0}]
				if (lotDataList == null || lotDataList.size() == 0)
					throw new CustomException("DURABLE-9004", trayGroupName);
				
				// Check for empty trays in the tray list
				Map<String,List<Lot>> trayLotListMap =  this.generateTrayLotListMap(trayList,lotDataList);
				
				// check panel process count (DetailProcessType in 'ACA, SCA, CRP')
				this.checkPanelProcessCount(lotDataList);
				
				// check Bom 
				this.checkBom(lotDataList.get(0), machineName);
				//caixu 2021/1/4 Postcell RroductRequest Assign Recipe
				checkAssignWO(machineData, lotData);
				checkProcessOperation(trayList,trayGroupName);
				//caixu 20210323 CheckPFLFlag
				checkPFLFlag(machineData,trayGroupName);
				 if(lotData != null)
				{
					String productSpecName =lotData.getProductSpecName();
					String productSpecVersion =lotData.getProductSpecVersion();
					String processFlowName =lotData.getProcessFlowName();
					String processFlowVersion =lotData.getProcessFlowVersion() ;
					String processOperationName =lotData.getProcessOperationName(); 
					String processOperationVersion =lotData.getProcessOperationVersion();
					String productRequestName =lotData.getProductRequestName();
					WOPatternFilmInfo dataInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);
					if(dataInfo!=null)
					{
				
					 machineRecipeName=dataInfo.getRecipeName();
					} 
					if(checkPostCellLoad()){
						
						updatePostCellLoadInfo(machineName,portName,productSpecName,processOperationName,productRequestName);
					}

				}//}
				 if( StringUtil.isEmpty(machineRecipeName))
				 {
					 
					 machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeNameByLotList(lotDataList, machineName);
					 
				 }
				 
				 //PFL Update JobDownloadFlag and machine
				 if(StringUtils.equals(machineData.getMachineGroupName(), "PFL"))
				 {
					 updateLotJobDownFlag(firstTrayLotList,machineName);
				 }
				 				 				 
				// Set bodyElement
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineRecipeName, lotDataList.get(0).getProductRequestName(), trayList.size(),firstRunQty, false);
			}
			else
			{
				if("PU".equals(portType))
				{
					// TRAY-0025: {0} message was reported on PU port.[PortName = {1}]
					throw new CustomException("TRAY-0025", "TrayGroupInfoDownloadRequest", portName);
				}
					
				// BL Port
				if (!CommonValidation.checkTrayGroupIsEmpty(trayGroupName))
				{
					// TRAY-0014:Panels exist in the tray group on the {0} port.
					throw new CustomException("TRAY-0014", portType);
				}
				if(checkPostCellLoad()&&StringUtils.equals(machineData.getMachineGroupName(), "FLC")&&"BL".equals(portType)){
					
					updatePostCellBLLoadInfo(machineName,portName,trayGroupData.getDurableSpecName());
				}

				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), "", "", trayList.size(),firstRunQty, true);
			}

			for (Durable durableData : trayList)
			{
				CommonValidation.CheckDurableState(durableData);
				CommonValidation.CheckDurableHoldState(durableData);
				//CommonValidation.CheckDurableCleanState(durableData); 2021/8/6 zhizao wumachao need

				this.createTrayElement(doc, durableData);
			}
			
			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}
	
	// check panel process count (DetailProcessType in 'ACA, SCA, CRP')
	private void checkPanelProcessCount(List<Lot> lotDataList) throws CustomException
	{
		if (lotDataList == null || lotDataList.size() == 0) return;

		Map<String,String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
		String detailProcessOperationType = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(lotDataList.get(0)).getDetailProcessOperationType();
		
		for (Lot lotData : lotDataList)
		{
			if (processLimitEnum != null && StringUtil.in(detailProcessOperationType, processLimitEnum.keySet().toArray(new String[]{})))
			{
				ExtendedObjectProxy.getPanelProcessCountService().checkPanelProcessCount(lotData.getKey().getLotName(), detailProcessOperationType);
			}
		}
	}
	private void checkPFLFlag(Machine machineData,String trayGroupName) throws CustomException{
		//已经做过PFL设备的Panel 不能再上PFL设备
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_PFL)){
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT L.* ");
			sql.append("  FROM DURABLE D, LOT L ");
			sql.append(" WHERE D.COVERNAME = :TRAYGROUPNAME ");
			sql.append("   AND D.DURABLENAME = L.CARRIERNAME ");
			sql.append("   AND L.JOBDOWNFLAG = 'PFL' ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TRAYGROUPNAME", trayGroupName);

			@SuppressWarnings("unchecked")
			//查看 Panel 是否带PFL Flag
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if(sqlResult.size()>0){
				//PFL Flag 不是空，不能再上PFL设备
				throw new CustomException("DURABLE-9015");
			}
			
		}
		
		
		
	}
	private void checkAssignWO(Machine machineData, Lot lotData) throws CustomException
	{
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_PFL))
		{
			Consumable patternFilmData = getMountedPatternFilm(machineData.getKey().getMachineName());

		
				List<WOPatternFilmInfo> woPatternFilmInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(lotData,machineData.getKey().getMachineName());

				if (woPatternFilmInfo != null)
				{ 
					WOPatternFilmInfo patternFilm=woPatternFilmInfo.get(0);
					String patternFilmSpec=patternFilm.getMaterialSpecName();
					if(!patternFilmSpec.equals(patternFilmData.getConsumableSpecName()))
					{
					 throw new CustomException("MATERIAL-0038", patternFilmData.getKey().getConsumableName(), lotData.getKey().getLotName());
					}
					
				}
		  }
	}
	private Consumable getMountedPatternFilm(String machineName) throws CustomException
	{
		List<Consumable> consumableDataList = null;
		try
		{
			String condition = " WHERE MACHINENAME = ? AND CONSUMABLESTATE = 'InUse' AND TRANSPORTSTATE = 'OnEQP' AND CONSUMABLETYPE = 'PatternFilm'";
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName });
		}
		catch (Exception ex)
		{
			//Add Validation, NotFounSignal Error Validation 
			if (!(ex instanceof NotFoundSignal)){
				throw new CustomException("SYS-0010", ex.getCause());
			}else{
				throw new CustomException("CONSUMABLE-0004");
			}
				
		}
		return consumableDataList.get(0);
	}
	private Map<String,List<Lot>> generateTrayLotListMap(List<Durable> trayList,List<Lot> lotDataList) throws CustomException
	{
		Map<String, List<Lot>> trayLotListMap = new HashMap<>();
		List<Lot> tempLotDataList = new ArrayList<>(lotDataList);

		for (Durable trayData : trayList)
		{
			List<Lot> sortedLotList = this.getSortLotListByCarrier(trayData.getKey().getDurableName(), tempLotDataList);

			if (sortedLotList == null || sortedLotList.size() == 0)
				throw new CustomException("DURABLE-9003", trayData.getKey().getDurableName());
			else
				tempLotDataList.removeAll(sortedLotList);

			trayLotListMap.put(trayData.getKey().getDurableName(), sortedLotList);
		}

		return trayLotListMap;
	}
	
	private List<Lot> getSortLotListByCarrier(String carrierName,List<Lot> lotDataList)
	{
		List<Lot> returnLotList = new ArrayList<>();

		for (Lot lotData : lotDataList)
		{
			if (lotData.getCarrierName().equals(carrierName))
				returnLotList.add(lotData);
		}

		return returnLotList;
	}

	private void checkBom(Lot lotData , String machineName) throws CustomException
	{
		//check consumable material 
		List<Consumable> consumableDataList = this.getMountedConsumableMaterial(machineName);

		if (consumableDataList != null)
		{
			int filmCount = 0;
			String filmSpec="";
			for (Consumable consumableData : consumableDataList)
			{
				if (consumableData.getConsumableType().equals(GenericServiceProxy.getConstantMap().MaterialType_PatternFilm))
				{
					if(!filmSpec.equals(consumableData.getConsumableSpecName()))
					{  
						filmSpec=consumableData.getConsumableSpecName();
						filmCount=filmCount+1;
					}
					if (filmCount> 1)
						throw new CustomException("MATERIAL-0036", consumableDataList.size());
				}
				checkConsumableMaterial(lotData, consumableData.getConsumableSpecName());
			}
		}

		//check durable material
		List<Durable> durableDataList = this.getMountedDurableMaterial(lotData.getFactoryName(), machineName);

		if (durableDataList != null)
		{
			for (Durable durableData : durableDataList)
			{
				checkDurableMaterial(machineName, durableData.getKey().getDurableName(),lotData.getProductSpecName());
			}
		}
	}
	
	private List<Consumable> getMountedConsumableMaterial(String machineName) throws CustomException
	{
		List<Consumable> consumableDataList = null;
		try
		{
			String condition = " WHERE MACHINENAME = ? AND CONSUMABLESTATE = 'InUse' AND TRANSPORTSTATE = 'OnEQP'";
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName });
		}
		catch (Exception ex)
		{
			if (!(ex instanceof NotFoundSignal))
				throw new CustomException("SYS-0010", ex.getCause());
		}

		return consumableDataList;
	}
	
	private List<Durable> getMountedDurableMaterial(String factoryName,String machineName) throws CustomException
	{
		List<Durable> durableDataList = null;
		try
		{
			String condition = " WHERE FACTORYNAME = ? AND MACHINENAME = ? AND (DURABLETYPE = 'PalletJig' OR DURABLETYPE = 'FPC') AND UPPER(TRANSPORTSTATE) = 'ONEQP' AND DURABLESTATE = 'InUse'";
			durableDataList = DurableServiceProxy.getDurableService().select(condition, new Object[] { factoryName, machineName });
		}
		catch (Exception ex)
		{
			if (!(ex instanceof NotFoundSignal))
				throw new CustomException("SYS-0010", ex.getCause());
		}

		return durableDataList;
	}
	
	private void checkConsumableMaterial(Lot lotData, String materialSpecName) throws CustomException
	{
		String sql = " SELECT E.MATERIALSPECNAME FROM CT_ERPBOM E, PRODUCTREQUEST P " +
			    	" WHERE E.PRODUCTREQUESTNAME=P.SUPERPRODUCTREQUESTNAME " +
			    	" AND E.MATERIALSPECNAME=:MATERIALSPECNAME " +
			    	" AND P.PRODUCTREQUESTNAME=:PRODUCTREQUESTNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MATERIALSPECNAME", materialSpecName);
		bindMap.put("PRODUCTREQUESTNAME", lotData.getProductRequestName());

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
			throw new CustomException("MATERIAL-0024", lotData.getKey().getLotName(), lotData.getProductSpecName(), materialSpecName);
	}
	
	private void checkDurableMaterial(String machineName, String durableName, String lotProductSpecNmae) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT *  ");
		sql.append("  FROM TMPOLICY T, POSDURABLE P, DURABLE D, MACHINE M ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND D.DURABLENAME = :DURABLENAME ");
		sql.append("   AND P.DURABLESPECNAME = D.DURABLESPECNAME ");
		sql.append("   AND P.DURABLESPECVERSION = D.DURABLESPECVERSION ");
		sql.append("   AND T.FACTORYNAME = D.FACTORYNAME ");
		sql.append("   AND M.MACHINENAME = :MACHINENAME ");
		sql.append("   AND T.MACHINENAME = M.MACHINENAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DURABLENAME", durableName);
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> sqlResult = null;

		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		if (sqlResult == null || sqlResult.size() < 1)
		{
			throw new CustomException("MATERIAL-0013");
		}

		// 20200731 DURABLE PRODUCTSPEC = PANEL PRODUCTSPEC Mapping Validation Request by caxiu
		String durableSpecName = ConvertUtil.getMapValueByName(sqlResult.get(0), "DURABLESPECNAME");
		String factoryName = ConvertUtil.getMapValueByName(sqlResult.get(0), "FACTORYNAME");

		StringBuffer mappingsql = new StringBuffer();
		mappingsql.append("SELECT PRODUCTSPECNAME ");
		mappingsql.append(" FROM DURABLESPEC  ");
		mappingsql.append(" WHERE  ");
		mappingsql.append(" FACTORYNAME = :FACTORYNAME ");
		mappingsql.append(" AND DURABLESPECNAME = :DURABLESPECNAME ");

		Map<String, Object> mappingbindMap = new HashMap<String, Object>();
		mappingbindMap.put("FACTORYNAME", factoryName);
		mappingbindMap.put("DURABLESPECNAME", durableSpecName);

		List<Map<String, Object>> mappingsqlResult = null;

		/*try
		{
			mappingsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(mappingsql.toString(), mappingbindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		String productSpecName = ConvertUtil.getMapValueByName(mappingsqlResult.get(0), "PRODUCTSPECNAME");

		if (StringUtils.isEmpty(productSpecName))
		{
			throw new CustomException("TRAYGROUP-0001", durableSpecName);
		}

		if (!mappingsqlResult.get(0).get("PRODUCTSPECNAME").toString().equals(lotProductSpecNmae))
		{
			throw new CustomException("TRAYGROUP-0002");
		}*/
	}
	
	private Element generateBodyTemplate(Element bodyElement, String machineRecipeName, String workOrder, int quantity,int firstRunQty, boolean IsUnLoader) throws CustomException
	{
		if (IsUnLoader)
		{
			XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "WORKORDER", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "QUANTITY", String.valueOf(quantity));
			XmlUtil.addElement(bodyElement, "FIRSTRUNQTY",String.valueOf(firstRunQty));
			XmlUtil.addElement(bodyElement, "TRAYLIST", StringUtil.EMPTY);
		}
		else
		{
			XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", machineRecipeName);
			XmlUtil.addElement(bodyElement, "WORKORDER", workOrder);
			XmlUtil.addElement(bodyElement, "QUANTITY", String.valueOf(quantity));
			XmlUtil.addElement(bodyElement, "FIRSTRUNQTY",String.valueOf(firstRunQty));
			XmlUtil.addElement(bodyElement, "TRAYLIST", StringUtil.EMPTY);
		}

		return bodyElement;
	}

	private void generateNGBodyTemplate(Document doc, Element bodyElementOri) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(bodyElementOri.getChildText("MACHINENAME"));
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(bodyElementOri.getChildText("PORTNAME"));
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(bodyElementOri.getChildText("PORTTYPE"));
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(bodyElementOri.getChildText("PORTUSETYPE"));
		bodyElement.addContent(portUseTypeElement);

		Element trayGroupNameElement = new Element("TRAYGROUPNAME");
		trayGroupNameElement.setText(bodyElementOri.getChildText("TRAYGROUPNAME"));
		bodyElement.addContent(trayGroupNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}
	private void checkProcessOperation(List<Durable>  trayList,String trayGroupName )throws CustomException
	{
		String sql =" SELECT DISTINCT PRODUCTREQUESTNAME , PRODUCTSPECNAME , PRODUCTSPECVERSION ,"
				  + " PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION "
				  + " FROM LOT WHERE CARRIERNAME IN (:TRAYLIST)  ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYLIST", CommonUtil.makeToStringList(trayList));
		
		List<Map<String,Object>> resultList =null;
		try
		{
		   resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if(resultList!=null&& resultList.size()>1)
		{
			//TRAY-0015:The TrayGroup has different Info Panel [TrayGroupName = {0}]
			throw new CustomException("TRAY-0015", trayGroupName);
		}
		
	}

	private void createTrayElement(Document doc,Durable durableData) throws CustomException
	{
		if (durableData.getUdfs().get("POSITION") == null)
			throw new CustomException("CARRIER-9003", durableData.getKey().getDurableName());

		Element trayListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "TRAYLIST", true);

		Element trayElement = new Element("TRAY");

		Element eleTrayName = new Element("TRAYNAME");
		eleTrayName.setText(durableData.getKey().getDurableName());
		trayElement.addContent(eleTrayName);

		Element elePosition = new Element("POSITION");
		elePosition.setText(durableData.getUdfs().get("POSITION"));
		trayElement.addContent(elePosition);

		trayListElement.addContent(trayElement);
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
	private void updatePostCellBLLoadInfo(String machineName,String portName,String durableSpec) throws CustomException
	{
		String sql="SELECT DURABLESPEC FROM CT_POSTCELLLOADINFO WHERE  MACHINENAME=:MACHINENAME AND PORTNAME=:PORTNAME ";
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
			String loadDurableSpecName=resultList.get(0).get("DURABLESPEC").toString();
			if(!loadDurableSpecName.equals(durableSpec)){
				
				StringBuffer updateSql = new StringBuffer();
				updateSql.append("UPDATE CT_POSTCELLLOADINFO SET DURABLESPEC=?, ");
				updateSql.append(" WHERE  MACHINENAME=? AND PORTNAME=?  ");
				List<Object[]> updatePostcellLoadInfo = new ArrayList<Object[]>();
				List<Object> loadInfo = new ArrayList<Object>();
				loadInfo.add(durableSpec);
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
			insertSql.append("(DURABLESPEC,MACHINENAME,PORTNAME) ");
			insertSql.append(" VALUES  ");
			insertSql.append("(?,?,?) ");
			List<Object[]> insertPostcellLoadInfo = new ArrayList<Object[]>();
			List<Object> insertloadInfo = new ArrayList<Object>();
			insertloadInfo.add(durableSpec);
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
	
	private void updateLotJobDownFlag(List<Lot> lotList,String machineName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayGroupInfoDownLoadSend", this.getEventUser(), this.getEventComment());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
        for(Lot lotData:lotList)
        {
			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(machineName);
			lotBindList.add(lotData.getKey().getLotName());
			updateLotArgList.add(lotBindList.toArray());
        }
        
        StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT SET ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       JOBDOWNFLAG = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		try 
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		} catch (Exception ex) 
		{
			log.info("JobDownFlag update file");
		}

	}
}