package kr.co.aim.messolution.lot.event.Tray;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AVIPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.PanelProcessCount;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class TrayInfoDownloadRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayInfoDownloadSend");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

			if (durableData.getDurableType().equals("CoverTray"))
			{
				throw new CustomException("DURABLE-9008", trayName);
			}
			if (!StringUtil.isEmpty(durableData.getUdfs().get("COVERNAME")))
				throw new CustomException("CARRIER-9008", trayName, durableData.getUdfs().get("COVERNAME"));

			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);
			//CommonValidation.CheckDurableCleanState(durableData);

			// message from unloader port
			if (StringUtil.in(portType, "PU", "BL"))
			{
				CommonValidation.checkTrayIsEmpty(durableData);

				if (!StringUtil.isEmpty(durableData.getUdfs().get("COVERNAME")))
					throw new CustomException("CARRIER-9008", trayName, durableData.getUdfs().get("COVERNAME"));
				else
					this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), null, 0, "");

				return doc;
			}

			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayName(trayName, false);

			if (lotList == null || lotList.size() == 0)
				throw new CustomException("CARRIER-9003", trayName);

			checkAssignWO(machineData, lotList);

			// Recipe
			//caixu 2021/1/4 Postcell RroductRequest Assign Recipe
			String machineRecipeName="";
			Lot lotdata = lotList.get(0);
			if(lotdata != null)
			{
				String productSpecName =lotdata.getProductSpecName();
				String productSpecVersion =lotdata.getProductSpecVersion();
				String processFlowName =lotdata.getProcessFlowName();
				String processFlowVersion =lotdata.getProcessFlowVersion() ;
				String processOperationName =lotdata.getProcessOperationName(); 
				String processOperationVersion =lotdata.getProcessOperationVersion();
				String productRequestName =lotdata.getProductRequestName();
				WOPatternFilmInfo dataInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);
			    if(dataInfo!=null)
			   {
				
				machineRecipeName=dataInfo.getRecipeName();
			   } 

			 }//}
			 if( StringUtil.isEmpty(machineRecipeName))
			 {
					 
			   machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeNameByLotList(lotList, machineName);
					 
		      }
			

			// Set bodyElement
			this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotList.get(0), lotList.size(), machineRecipeName);

			for (Lot lotData : lotList)
			{
				CommonValidation.checkLotState(lotData);
				CommonValidation.checkLotProcessState(lotData);
				CommonValidation.checkLotHoldState(lotData);

				this.createPanelElement(doc, lotData);
			}

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
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

		Element trayGroupNameElement = new Element("TRAYNAME");
		trayGroupNameElement.setText(bodyElementOri.getChildText("TRAYNAME"));
		bodyElement.addContent(trayGroupNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}

	private Element generateBodyTemplate(Element bodyElement, Lot lotData, int productQuantity, String machineRecipeName) throws CustomException
	{
		if (lotData != null)
		{
			XmlUtil.addElement(bodyElement, "PROCESSFLOWNAME", lotData.getProcessFlowName());
			XmlUtil.addElement(bodyElement, "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			XmlUtil.addElement(bodyElement, "PROCESSOPERATIONNAME", lotData.getProcessOperationName());
			XmlUtil.addElement(bodyElement, "PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
			XmlUtil.addElement(bodyElement, "PRODUCTSPECNAME", lotData.getProductSpecName());
			XmlUtil.addElement(bodyElement, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			XmlUtil.addElement(bodyElement, "PRODUCTIONTYPE", lotData.getProductionType());
			XmlUtil.addElement(bodyElement, "PRODUCTQUANTITY", Integer.toString(productQuantity));
			XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", machineRecipeName);
			XmlUtil.addElement(bodyElement, "PRODUCTREQUESTNAME", lotData.getProductRequestName());
			XmlUtil.addElement(bodyElement, "PANELLIST", StringUtil.EMPTY);
		}
		else
		{
			XmlUtil.addElement(bodyElement, "PROCESSFLOWNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PROCESSFLOWVERSION", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PROCESSOPERATIONNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PROCESSOPERATIONVERSION", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTSPECNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTSPECVERSION", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTIONTYPE", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTQUANTITY", Integer.toString(productQuantity));
			XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTREQUESTNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PANELLIST", StringUtil.EMPTY);
		}

		return bodyElement;
	}

	private void createPanelElement(Document doc, Lot lotData) throws CustomException
	{
		Element panelListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PANELLIST", true);

		Element panelElement = new Element("PANEL");

		Element elePanelName = new Element("PANELNAME");
		elePanelName.setText(lotData.getKey().getLotName());
		panelElement.addContent(elePanelName);

		Element elePosition = new Element("POSITION");
		elePosition.setText(lotData.getUdfs().get("POSITION"));
		panelElement.addContent(elePosition);

		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(lotData.getProductSpecName());
		panelElement.addContent(productSpecNameElement);

		Element productSpecVersionElement = new Element("PRODUCTSPECVERSION");
		productSpecVersionElement.setText(lotData.getProductSpecVersion());
		panelElement.addContent(productSpecVersionElement);

		Element processFlowNameElement = new Element("PROCESSFLOWNAME");
		processFlowNameElement.setText(lotData.getProcessFlowName());
		panelElement.addContent(processFlowNameElement);

		Element processFlowVersionElement = new Element("PROCESSFLOWVERSION");
		processFlowVersionElement.setText(lotData.getProcessFlowVersion());
		panelElement.addContent(processFlowVersionElement);

		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText(lotData.getProcessOperationName());
		panelElement.addContent(processOperationNameElement);

		Element processOperationVersionElement = new Element("PROCESSOPERATIONVERSION");
		processOperationVersionElement.setText(lotData.getProcessOperationVersion());
		panelElement.addContent(processOperationVersionElement);

		Element productionTypeElement = new Element("PRODUCTIONTYPE");
		productionTypeElement.setText(lotData.getProductionType());
		panelElement.addContent(productionTypeElement);

		Element productJudgeElement = new Element("PRODUCTJUDGE");
		productJudgeElement.setText(lotData.getLotGrade());
		panelElement.addContent(productJudgeElement);

		Element productGradeElement = new Element("PRODUCTGRADE");
		productGradeElement.setText(lotData.getUdfs().get("LOTDETAILGRADE"));
		panelElement.addContent(productGradeElement);

		String tpJudge = "";
		if (ExtendedObjectProxy.getAVIPanelJudgeService().isExist(lotData.getKey().getLotName()))
		{
			AVIPanelJudge aviPanelJudge = ExtendedObjectProxy.getAVIPanelJudgeService().selectByKey(false, new Object[] { lotData.getKey().getLotName() });
			
			tpJudge = aviPanelJudge.getTpJudge();
		}

		ProcessOperationSpec poSpec = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(lotData);

		if ("Lami".equals(poSpec.getDetailProcessOperationType()))
		{
			Element productTPJudgeElement = new Element("PRODUCTTPJUDGE");
			productTPJudgeElement.setText(tpJudge);
			panelElement.addContent(productTPJudgeElement);
		}

		Element inspectionListElement = new Element("INSPECTIONLIST");

		if ("AVI".equals(poSpec.getDetailProcessOperationType()))
		{
			List<PanelProcessCount> inspectionDataList = ExtendedObjectProxy.getPanelProcessCountService().getDataListByPanelName(lotData.getKey().getLotName(), false);

			if (inspectionDataList != null && inspectionDataList.size() > 0)
			{
				for (PanelProcessCount dataInfo : inspectionDataList)
				{
					Element inspectionElement = new Element("INSPECTION");
					inspectionElement.addContent(new Element("INSPECTIONTYPE").setText(dataInfo.getDetailProcessOperationType()));
					inspectionElement.addContent(new Element("PROCESSCOUNT").setText(dataInfo.getProcessCount().toString()));
					inspectionElement.addContent(new Element("PROCESSLIMIT").setText(dataInfo.getProcessLimit().toString()));

					inspectionListElement.addContent(inspectionElement);
				}
			}
		}

		panelElement.addContent(inspectionListElement);
		panelListElement.addContent(panelElement);
	}

	private void checkAssignWO(Machine machineData, List<Lot> lotList) throws CustomException
	{
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_PFL))
		{
			Consumable patternFilmData = getMountedPatternFilm(machineData.getKey().getMachineName());

			for (Lot lotData : lotList)
			{
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
		if (consumableDataList != null)
		{
			int filmCount = 0;
			String filmSpec="";
			for (Consumable consumableData : consumableDataList)
			{
				
			  if(!filmSpec.equals(consumableData.getConsumableSpecName()))
			   {  
				filmSpec=consumableData.getConsumableSpecName();
				filmCount=filmCount+1;
			   }
				if (filmCount> 1)
				throw new CustomException("MATERIAL-0036", consumableDataList.size());
				
			}
		}
		return consumableDataList.get(0);
	}
}