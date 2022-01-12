package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RunTimeMachineInfo;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
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
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PackingTrayGroupInfoDownloadRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(PackingTrayGroupInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingTrayGroupInfoDownloadSend");

			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverTrayName);
			
			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(coverTrayData);
			CommonValidation.CheckDurableHoldState(coverTrayData);
			CommonValidation.ChekcMachinState(machineData);
			
			// PORT-1001:Port[{0}] Type NotMismatch !
			if (!portData.getUdfs().get("PORTTYPE").equals(portType))
				throw new CustomException("PORT-1001", portName);

			if (!StringUtils.equals(coverTrayData.getDurableType(), "CoverTray"))
			{
				// DURABLE-0009:This TrayGroup [{0}] is not CoverTray
				throw new CustomException("DURABLE-0009", coverTrayName);
			}

			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTrayAllowNull(coverTrayName, false);

			if(trayList == null || trayList.size() == 0)
			{
				coverTrayData.getUdfs().put("POSITION", "1");
				trayList = new ArrayList<Durable>();
				trayList.add(coverTrayData);
			}
			
			if (StringUtils.equals(portData.getUdfs().get("PORTTYPE"), constantMap.PORT_TYPE_PL))
			{
				if (!StringUtil.equals(coverTrayData.getDurableState(), constantMap.Dur_InUse))
				{
					// DURABLE-0012:DurableState is not InUse
					throw new CustomException("DURABLE-0012", coverTrayData.getDurableState(), coverTrayName);
				}

				List<Lot> lotDataList = null;
				Map<String, List<Lot>> trayLotListMap = new HashMap<>();
				
				//Get All Panel
				lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList, false);
				
				// Check for empty trays in the tray list
				trayLotListMap = this.generateTrayLotListMap(trayList, lotDataList);

				// DURABLE-9004:No panel assigned to TrayGroup[{0}]
				if (lotDataList == null || lotDataList.size() == 0)
					throw new CustomException("DURABLE-9004", coverTrayName);
				
				ProductRequest groupPR = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotDataList.get(0).getProductRequestName());
				String groupSubProductionType = groupPR.getUdfs().get("SUBPRODUCTIONTYPE");
				
				//LOT-0020 Panel SameSpec Check 
				for(Lot lotData : lotDataList)
				{
					checkLotSameSpec(lotData, lotDataList.get(0).getLotGrade(), lotDataList.get(0).getUdfs().get("LOTDETAILGRADE").toString(), 
							lotDataList.get(0).getProcessOperationName(), lotDataList.get(0).getProductSpecName(), lotDataList.get(0).getProductionType(), 
							lotDataList.get(0).getKey().getLotName(), lotDataList.get(0).getProductRequestName(), lotDataList.get(0).getProcessFlowName(),
							groupSubProductionType);
					
					//Mantis -0000461 Validation already packing panel Request by caixu 
					checkPackingPanel(lotData,lotDataList.get(0).getKey().getLotName());
					
					
					if(StringUtil.equals(lotData.getLotGrade(), "G"))
					{
						CommonValidation.checkLotState(lotData);
					}
					else
					{
						CommonValidation.checkLotStateScrapped(lotData);
						CommonValidation.checkLotProcessStateWait(lotData);
					}
					CommonValidation.checkLotHoldState(lotData);
				}
				
				// Check DetailOperationType SORT-0010
				if(StringUtil.equals(lotDataList.get(0).getLotGrade(), "G"))
				{
					CommonValidation.checkDetailOperationType(lotDataList.get(0), "InnerPack");
				}
				else
				{
					CommonValidation.checkDetailOperationType(lotDataList.get(0), "ScrapPack");
				}
				
				String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeNameByLotList(lotDataList, machineName);

				// check TPPolicy + POSContainer
				this.checkTPContainer(coverTrayData, lotDataList.get(0).getFactoryName(), lotDataList.get(0).getProductSpecName(), lotDataList.get(0).getProductSpecVersion());

				// Set bodyElement
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineRecipeName, lotDataList.get(0), trayList.size());

				for (Durable durableData : trayList)
				{
					this.createTrayElement(doc, durableData, trayLotListMap);
				}
			}

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingTrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingTrayGroupCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}

	private Element generateBodyTemplate(Element bodyElement, String machineRecipeName, Lot lotData, int quantity) throws CustomException
	{
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

	private void createTrayElement(Document doc, Durable durableData, Map<String, List<Lot>> trayLotListMap) throws CustomException
	{
		CommonValidation.CheckDurableState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);
		//CommonValidation.CheckDurableCleanState(durableData);

		if (durableData.getUdfs().get("POSITION") == null)
			throw new CustomException("CARRIER-9003", durableData.getKey().getDurableName());

		List<Lot> sortLotList = trayLotListMap.get(durableData.getKey().getDurableName());

		Element trayListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "TRAYLIST", true);

		Element trayElement = new Element("TRAY");
		trayElement.addContent(new Element("TRAYNAME").setText(durableData.getKey().getDurableName()));
		trayElement.addContent(new Element("POSITION").setText(durableData.getUdfs().get("POSITION")));
		trayElement.addContent(new Element("PRODUCTQUANTITY").setText(String.valueOf(sortLotList.size())));

		Element panelListElement = new Element("PANELLIST");

		for (Lot lotData : sortLotList)
		{
			Element panelElement = new Element("PANEL");
			panelElement.addContent(new Element("PANELNAME").setText(lotData.getKey().getLotName()));
			panelElement.addContent(new Element("POSITION").setText(lotData.getUdfs().get("POSITION")));
			panelElement.addContent(new Element("PRODUCTJUDGE").setText(lotData.getLotGrade()));
			panelElement.addContent(new Element("PRODUCTGRADE").setText(lotData.getUdfs().get("LOTDETAILGRADE")));
			panelListElement.addContent(panelElement);
		}

		trayElement.addContent(panelListElement);
		trayListElement.addContent(trayElement);
	}
	public static void checkLotSameSpec(Lot lotData, String groupGrade, String groupDetailGrade, String groupOperation, String groupSpec, String groupProduction, String groupLotName, String groupWO, String groupFlowName, String groupSubProductionType)
			throws CustomException
	{
		if (!lotData.getLotGrade().equals(groupGrade)  || !lotData.getProcessOperationName().equals(groupOperation)
				|| !lotData.getProductSpecName().equals(groupSpec) || !lotData.getProductionType().equals(groupProduction) /*|| !lotData.getProductRequestName().equals(groupWO)*/
				|| !lotData.getProcessFlowName().equals(groupFlowName))
		{
			throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
		}
		if(groupDetailGrade.equals("C1")||groupDetailGrade.equals("C2")||groupDetailGrade.equals("C3")||groupDetailGrade.equals("C4")||groupDetailGrade.equals("C5")||groupDetailGrade.equals("C6"))
		{
			if(!lotData.getUdfs().get("LOTDETAILGRADE").equals("C1")&&!lotData.getUdfs().get("LOTDETAILGRADE").equals("C2")&&!lotData.getUdfs().get("LOTDETAILGRADE").equals("C3")&&!lotData.getUdfs().get("LOTDETAILGRADE").equals("C4")
					&&!lotData.getUdfs().get("LOTDETAILGRADE").equals("C5")&&!lotData.getUdfs().get("LOTDETAILGRADE").equals("C6"))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
			}
	    }
		else
	    {
			if(!lotData.getUdfs().get("LOTDETAILGRADE").equals(groupDetailGrade))
			{
				
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotName);
				
			}
	    }
		
		if(groupGrade.equals("S"))
		{
			ProductRequest productRe = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
			String subProductionType = productRe.getUdfs().get("SUBPRODUCTIONTYPE");
			
			if(!subProductionType.equals(groupSubProductionType))
			{
				throw new CustomException("SYS-0010", "S Grade Panel Has Diffrent SubProductionType In ProductRequest LotID: " + lotData.getKey().getLotName());
			}
		}
		else
		{
			if(!lotData.getProductRequestName().equals(groupWO))
			{
				throw new CustomException("SYS-0010", "Diffrent ProductRequestName LotID: " + lotData.getKey().getLotName());
			}
		}
	}
	
	public static void checkPackingPanel(Lot lotData, String lotName)
			throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT A.LOTNAME, ");
		sql.append("  A.PRODUCTSPECNAME, ");
		sql.append("  A.PROCESSFLOWNAME, ");
		sql.append("  A.PROCESSOPERATIONNAME, ");
		sql.append("  A.MACHINENAME, ");
		sql.append("  A.LOTDETAILGRADE, ");
		sql.append("  A.PROCESSGROUPNAME, ");
		sql.append("  B.PROCESSGROUPTYPE ");		
		sql.append("  FROM LOT A, PROCESSGROUP B ");
		sql.append(" WHERE     A.PROCESSGROUPNAME = B.PROCESSGROUPNAME ");
		sql.append("   AND A.LOTNAME = :LOTNAME ");


		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAME", lotName);


		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		// POSTCELL-0002:This Panel already Packing.. check Panel Data   PanelName:({0}), ProcessGroupType({1}).
		if (resultList !=null && resultList.size()>0){
			throw new CustomException("POSTCELL-0002", lotName, resultList.get(0).get("PROCESSGROUPTYPE").toString());
		}
		
	}
}