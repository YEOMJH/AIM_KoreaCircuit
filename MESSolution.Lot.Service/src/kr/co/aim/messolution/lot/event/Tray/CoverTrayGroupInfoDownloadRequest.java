package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RiskLot;
import kr.co.aim.messolution.extended.object.management.data.RunTimeMachineInfo;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CoverTrayGroupInfoDownloadRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(CoverTrayGroupInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CoverTrayGroupInfoDownloadSend");

			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);
			String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
			String machineRecipeName ="";

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverTrayName);

			String BCRFlag = CommonValidation.isNullOrEmpty(coverTrayData.getUdfs().get("BCRFLAG")) ? "N" : coverTrayData.getUdfs().get("BCRFLAG");
			String trayOperMode = coverTrayData.getUdfs().get("OPERATIONMODE");

			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(coverTrayData);
			CommonValidation.CheckDurableHoldState(coverTrayData);
			
			if(!operationMode.equals(trayOperMode))
			{
				//TRAY-0012: Tray cannot be processed.[OperationMode: Machine = {0} , Tray = {1}]
				throw new CustomException("TRAY-0012" , coverTrayName,operationMode,trayOperMode);
			}

			// PORT-1001:Port[{0}] Type NotMismatch !
			if (!portData.getUdfs().get("PORTTYPE").equals(portType))
				throw new CustomException("PORT-1001", portName);

			// validate sort operation mode
			if (machineData.getUdfs().get("OPERATIONMODE").equals(operationMode))
				MESMachineServiceProxy.getMachineInfoUtil().validateSorterOperation(operationMode);
			else
				throw new CustomException("MACHINE-0021", machineName, operationMode);

			// check port by operation mode and cleanmode
			MESPortServiceProxy.getPortServiceUtil().validateSorterPort(machineData, portData);

			// check Tray BCRFLAG (Y is mix Tray)
			// SORTER-0005:Tray BCRFlag ({0}) not defined in {1} operation mode port {2}.
			if (BCRFlag == null || !BCRFlag.equals(EnumInfoUtil.SorterOperationCondition.getBCRFlag(operationMode, portName)))
				throw new CustomException("SORTER-0005", coverTrayName + ":" + BCRFlag, operationMode, portName);

			if (!StringUtils.equals(coverTrayData.getDurableType(), "CoverTray"))
			{
				// DURABLE-0009:This TrayGroup [{0}] is not CoverTray
				throw new CustomException("DURABLE-0009", coverTrayName);
			}

			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(coverTrayName, false);

			if (StringUtils.equals(portData.getUdfs().get("PORTTYPE"), constantMap.PORT_TYPE_PL))
			{
				if (!StringUtil.equals(coverTrayData.getDurableState(), constantMap.Dur_InUse))
				{
					// DURABLE-0012:DurableState is not InUse
					throw new CustomException("DURABLE-0012", coverTrayData.getDurableState(), coverTrayName);
				}

				List<Lot> lotDataList = null;
				Map<String, List<Lot>> trayLotListMap = new HashMap<>();

				if ("Y".equals(BCRFlag))
				{
					// SORTER-0006:Invalid tray group loaded.[OperationMode={0},Port={1},BCRFlag={2},CoverTray={3},SubTraySize={4}]
					if (trayList != null && trayList.size() > 0)
						throw new CustomException("SORTER-0006", operationMode, portName, BCRFlag, coverTrayName, trayList.size());
					checkProcessOperation(coverTrayName);
					lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayName(coverTrayName, false);
					trayLotListMap.put(coverTrayName, lotDataList);
					trayList.add(coverTrayData);
				}
				else
				{
					if (trayList == null || trayList.size() == 0)
					{
						// TRAY-0009: No tray data was found for the {0} tray group.
						throw new CustomException("TRAY-0009", coverTrayName);
					}
					checkProcessOperation(trayList,coverTrayName);
					lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList, false);

					// Check for empty trays in the tray list
					trayLotListMap = this.generateTrayLotListMap(trayList, lotDataList);
				}

				// DURABLE-9004:No panel assigned to TrayGroup[{0}]
				if (lotDataList == null || lotDataList.size() == 0)
					throw new CustomException("DURABLE-9004", coverTrayName);
				 Lot lotData = lotDataList.get(0);
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

				// check TPPolicy + POSContainer
				this.checkTPContainer(coverTrayData, lotDataList.get(0).getFactoryName(), lotDataList.get(0).getProductSpecName(), lotDataList.get(0).getProductSpecVersion());

				// check RunTimeInfo(wo,flow,Oper).
				//EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment());
				//ExtendedObjectProxy.getRunTimeMachineInfoService().checkRunTimeMachineInfo(eventInfo, machineName, portName, lotDataList.get(0));

				Map<String, String> sorterPickLotMap = new HashMap<>();

				// OperationMode = T-PIPR(Pick Print mode) down load OperationFlag => PickPrint change from T-PIPR to T-SHIP
				if (EnumInfoUtil.SorterOperationCondition.TSHIP.getOperationMode().equals(operationMode))
				{
					List<RiskLot> sorterPickLotList = ExtendedObjectProxy.getRiskLotService().getDataInfoListByLotNameList(CommonUtil.makeToStringList(lotDataList));

					if (sorterPickLotList!= null && sorterPickLotList.size()>0)
						sorterPickLotMap = this.generateSorterPickLotMap(sorterPickLotList);	
					/* 屏蔽原挑屏逻辑,使用CT_RISKLOT表作为挑屏标准
					List<SorterPickPrintInfo> sorterPickLotList = ExtendedObjectProxy.getSorterPickPrintInfoService().getDataInfoListByLotNameList(CommonUtil.makeToStringList(lotDataList));

					// SORTER-0009:Could not find the lot to pick or print.[OperationMode = {0}]
					if (sorterPickLotList!= null && sorterPickLotList.size()>0)
					{
					  sorterPickLotMap = this.generateSorterPickLotMap(sorterPickLotList);	
					}*/
				}
				// Set bodyElement
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineRecipeName, BCRFlag, lotDataList.get(0), trayList.size());

				for (Durable durableData : trayList)
				{
					this.createTrayElement(doc, durableData, trayLotListMap, BCRFlag, sorterPickLotMap);
				}
			}
			else
			{
				// BL Port( olny empty port )
				if ("Y".equals(BCRFlag) || EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
				{
					// SORTER-0006:Invalid tray group loaded.[OperationMode={0},Port={1},BCRFlag={2},CoverTray={3},SubTraySize={4}]
					if (trayList != null && trayList.size() > 0)
						throw new CustomException("SORTER-0006", operationMode, portName, BCRFlag, coverTrayName, trayList.size());
					
					if (!CommonValidation.checkTrayIsEmpty(coverTrayName))
					{
						// TRAY-0013:Panels exist in the tray on the {0} port.
						throw new CustomException("TRAY-0013", portType);
					}
					
					if(EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
					{
						if(!StringUtil.equals(portName, "P04"))
						{
							throw new CustomException("PORT-9999", String.format("Use P04 Port"));
						}
					}
				}
				else
				{
					if (trayList == null || trayList.size() == 0)
					{
						// TRAY-0009: No tray data was found for the {0} tray group.
						throw new CustomException("TRAY-0009", coverTrayName);
					}

					if (!CommonValidation.checkTrayGroupIsEmpty(coverTrayData))
					{
						// TRAY-0014: Panels exist in the tray group on the {0} port.
						throw new CustomException("TRAY-0014", portType);
					}
				}

				//RunTimeMachineInfo runTimeInfo = ExtendedObjectProxy.getRunTimeMachineInfoService().getDataInfoByKey(machineName, true); 

				// check TPPolicy + POSContainer
				//this.checkTPContainer(coverTrayData, machineData.getFactoryName(), runTimeInfo.getProductSpecName(), runTimeInfo.getProductSpecVersion());

				// Set bodyElement
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), "", BCRFlag, null, trayList == null ? 0 : trayList.size());

				if (!"Y".equals(BCRFlag))
				{
					for (Durable durableData : trayList)
					{
						this.createTrayElementNoPanel(doc, durableData, BCRFlag);
					}
				}
//				else if (EnumInfoUtil.SorterOperationCondition.TPIPR.getOperationMode().equals(operationMode))
//				{
//					this.createTrayElementInCoverTray(doc, coverTrayData);
//				}
			}

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CoverTrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CoverTrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}

	private Element generateBodyTemplate(Element bodyElement, String machineRecipeName, String BCRFlag, Lot lotData, int quantity) throws CustomException
	{
		XmlUtil.addElement(bodyElement, "BCRFLAG", BCRFlag);
		XmlUtil.addElement(bodyElement, "PRODUCTSPECNAME", (lotData == null || lotData.getProductSpecName() == null) ? "" : lotData.getProductSpecName());
		XmlUtil.addElement(bodyElement, "PRODUCTSPECVERSION", (lotData == null || lotData.getProductSpecVersion() == null) ? "" : lotData.getProductSpecVersion());
		XmlUtil.addElement(bodyElement, "PROCESSFLOWNAME", (lotData == null || lotData.getProcessFlowName() == null) ? "" : lotData.getProcessFlowName());
		XmlUtil.addElement(bodyElement, "PROCESSFLOWVERSION", (lotData == null || lotData.getProcessFlowVersion() == null) ? "" : lotData.getProcessFlowVersion());
		XmlUtil.addElement(bodyElement, "PROCESSOPERATIONNAME", (lotData == null || lotData.getProcessOperationName() == null) ? "" : lotData.getProcessOperationName());
		XmlUtil.addElement(bodyElement, "PROCESSOPERATIONVERSION", (lotData == null || lotData.getProcessOperationVersion() == null) ? "" : lotData.getProcessOperationVersion());
		XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", machineRecipeName);
		XmlUtil.addElement(bodyElement, "WORKORDER", (lotData == null || lotData.getProductRequestName() == null) ? "" : lotData.getProductRequestName());
		XmlUtil.addElement(bodyElement, "QUANTITY", Integer.toString(quantity));
		XmlUtil.addElement(bodyElement, "TRAYLIST", "");

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

		Element coverTrayNameElement = new Element("COVERTRAYNAME");
		coverTrayNameElement.setText(bodyElementOri.getChildText("COVERTRAYNAME"));
		bodyElement.addContent(coverTrayNameElement);

		Element operationElement = new Element("OPERATIONMODE");
		operationElement.setText(bodyElementOri.getChildText("OPERATIONMODE"));
		bodyElement.addContent(operationElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}

	private Map<String, List<Lot>> generateTrayLotListMap(List<Durable> trayList, List<Lot> lotDataList) throws CustomException
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

	private List<Lot> getSortLotListByCarrier(String carrierName, List<Lot> lotDataList)
	{
		List<Lot> returnLotList = new ArrayList<>();

		for (Lot lotData : lotDataList)
		{
			if (lotData.getCarrierName().equals(carrierName))
				returnLotList.add(lotData);
		}

		return returnLotList;
	}

	/*
	private Map<String, String> generateSorterPickLotMap(List<SorterPickPrintInfo> sorterPickLotList)
	{
		Map<String, String> sorterPickLotMap = new HashMap<>();

		if (sorterPickLotList == null || sorterPickLotList.size() == 0)
			return sorterPickLotMap;

		for (SorterPickPrintInfo dataInfo : sorterPickLotList)
			sorterPickLotMap.put(dataInfo.getLotName(), dataInfo.getPickPrintMode());

		return sorterPickLotMap;
	}*/

	private Map<String, String> generateSorterPickLotMap(List<RiskLot> sorterPickLotList) 
	{
		Map<String, String> sorterPickLotMap = new HashMap<>();

		if (sorterPickLotList == null || sorterPickLotList.size() == 0)
			return sorterPickLotMap;

		for (RiskLot dataInfo : sorterPickLotList)
			sorterPickLotMap.put(dataInfo.getLotName(), "YN");

		return sorterPickLotMap;
	}
	
	// TODO(PLY):Exception
	@SuppressWarnings("unchecked")
	private void checkTPContainer(Durable trayData, String factoryName, String productSpecName, String productSpecVersion) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTSPECNAME, DURABLESPECNAME ");
		sql.append("  FROM TPPOLICY T, POSCONTAINER P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND P.DURABLETYPE = 'Tray' ");
		sql.append("   AND P.DURABLESPECNAME = :DURABLESPECNAME ");
		sql.append("   AND P.DURABLESPECVERSION = :DURABLESPECVERSION ");

		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
		bindMap.put("DURABLESPECNAME", trayData.getDurableSpecName());
		bindMap.put("DURABLESPECVERSION", trayData.getDurableSpecVersion());

		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		// SORTER-0007:Sorter Policy Check Fail!! [TP + POSContainer]: ProductSpecName({0}) , DurableSpecName({1}).
		if (resultList == null || resultList.size() == 0)
			throw new CustomException("SORTER-0007", productSpecName, trayData.getDurableSpecName());

		log.info(String.format("Sorter Policy Check[TP + POSContainer]: ProductSpecName(%s) , DurableSpecName(%s).", productSpecName, trayData.getDurableSpecName()));
	}

	private void createTrayElement(Document doc, Durable durableData, Map<String, List<Lot>> trayLotListMap, String BCRFlag, Map<String, String> sorterPickLotMap) throws CustomException
	{
		CommonValidation.CheckDurableState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);
		//CommonValidation.CheckDurableCleanState(durableData);

		if (!"Y".equals(BCRFlag) && durableData.getUdfs().get("POSITION") == null)
			throw new CustomException("CARRIER-9003", durableData.getKey().getDurableName());

		List<Lot> sortLotList = trayLotListMap.get(durableData.getKey().getDurableName());

		Element trayListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "TRAYLIST", true);

		Element trayElement = new Element("TRAY");
		trayElement.addContent(new Element("TRAYNAME").setText(durableData.getKey().getDurableName()));
		trayElement.addContent(new Element("POSITION").setText(durableData.getUdfs().get("POSITION")));
		trayElement.addContent(new Element("PRODUCTQUANTITY").setText(String.valueOf(sortLotList.size())));
		trayElement.addContent(new Element("BCRFLAG").setText(durableData.getUdfs().get("BCRFLAG")));

		Element panelListElement = new Element("PANELLIST");

		for (Lot lotData : sortLotList)
		{
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);

			Element panelElement = new Element("PANEL");
			panelElement.addContent(new Element("PANELNAME").setText(lotData.getKey().getLotName()));
			panelElement.addContent(new Element("POSITION").setText(lotData.getUdfs().get("POSITION")));
			panelElement.addContent(new Element("PRODUCTJUDGE").setText(lotData.getLotGrade()));
			panelElement.addContent(new Element("PRODUCTGRADE").setText(lotData.getUdfs().get("LOTDETAILGRADE")));
			if(StringUtil.isEmpty(sorterPickLotMap.get(lotData.getKey().getLotName())))
			{
				panelElement.addContent(new Element("OPERATIONFLAG").setText("NN"));
			}
			else
			{
				panelElement.addContent(new Element("OPERATIONFLAG").setText(sorterPickLotMap.get(lotData.getKey().getLotName())));
			}

			panelListElement.addContent(panelElement);
		}

		trayElement.addContent(panelListElement);
		trayListElement.addContent(trayElement);
	}

	private void createTrayElementNoPanel(Document doc, Durable durableData, String BCRFlag) throws CustomException
	{
		CommonValidation.CheckDurableState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);
		//CommonValidation.CheckDurableCleanState(durableData);

		if (!"Y".equals(BCRFlag) && durableData.getUdfs().get("POSITION") == null)
			throw new CustomException("CARRIER-9003", durableData.getKey().getDurableName());

		Element trayListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "TRAYLIST", true);

		Element trayElement = new Element("TRAY");
		trayElement.addContent(new Element("TRAYNAME").setText(durableData.getKey().getDurableName()));
		trayElement.addContent(new Element("POSITION").setText(durableData.getUdfs().get("POSITION")));
		trayElement.addContent(new Element("PRODUCTQUANTITY").setText("0"));
		trayElement.addContent(new Element("BCRFLAG").setText(durableData.getUdfs().get("BCRFLAG")));
		Element panelListElement = new Element("PANELLIST");
		trayElement.addContent(panelListElement);
		trayListElement.addContent(trayElement);
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
			//TRAY-0015: The TrayGroup has different Info Panel [TrayGroupName = {0}]
			throw new CustomException("TRAY-0015", trayGroupName);
		}
		
	}
	private void checkProcessOperation(String trayGroupName )throws CustomException
	{
		String sql =" SELECT DISTINCT PRODUCTREQUESTNAME , PRODUCTSPECNAME , PRODUCTSPECVERSION ,"
				  + " PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION "
				  + " FROM LOT WHERE CARRIERNAME IN (:TRAYLIST)  ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYLIST",trayGroupName);
		
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
			//TRAY-0015: The TrayGroup has different Info Panel [TrayGroupName = {0}]
			throw new CustomException("TRAY-0015", trayGroupName);
		}
		
	}

	private void createTrayElementInCoverTray(Document doc, Durable coverTrayData) throws CustomException
	{
		CommonValidation.CheckDurableState(coverTrayData);
		CommonValidation.CheckDurableHoldState(coverTrayData);
		//CommonValidation.CheckDurableCleanState(coverTrayData);

		Element trayListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "TRAYLIST", true);

		Element trayElement = new Element("TRAY");
		trayElement.addContent(new Element("TRAYNAME").setText(coverTrayData.getKey().getDurableName()));
		trayElement.addContent(new Element("POSITION").setText(""));
		trayElement.addContent(new Element("PRODUCTQUANTITY").setText(""));
		trayElement.addContent(new Element("BCRFLAG").setText(""));

		Element panelListElement = new Element("PANELLIST");

		trayElement.addContent(panelListElement);
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
}