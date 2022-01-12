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

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayGroupInfoDownloadRequestForClave extends SyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupInfoDownloadRequestForClave.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupInfoDownloadSendForClave");
			
			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);

			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(coverTrayData);
			CommonValidation.CheckDurableHoldState(coverTrayData);

			if (!StringUtils.equals(coverTrayData.getDurableType(), "CoverTray"))
			{
				//DURABLE-0009:This TrayGroup [{0}] is not CoverTray
				throw new CustomException("DURABLE-0009", trayGroupName);
			}

			if (StringUtils.equals(portData.getUdfs().get("PORTTYPE"), constantMap.PORT_TYPE_PL))
			{
				if (!StringUtil.equals(coverTrayData.getDurableState(), constantMap.Dur_InUse))
				{
					//DURABLE-9006:Invalid TrayGroupState[{0}] by TrayGroup[{1}]
					throw new CustomException("DURABLE-9006", coverTrayData.getDurableState(), trayGroupName);
				}
			}

			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,false);
			
			if (trayList == null || trayList.size()==0 )
			{
				// TRAY-0009:No tray data was found for the {0} tray group.
				new CustomException("TRAY-0009", trayGroupName);
			}
				checkProcessOperation(trayList,trayGroupName);
			List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList,false);

			// DURABLE-9004:No panel assigned to TrayGroup[{0}]
			if(lotDataList ==null || lotDataList.size()==0)
				throw new CustomException("DURABLE-9004", trayGroupName);
			
			// Check for empty trays in the tray list
			Map<String,List<Lot>> trayLotListMap =  this.generateTrayLotListMap(trayList,lotDataList);
			
			// get machine recipe
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeNameByLotList(lotDataList, machineName);

			// Set bodyElement
			this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineRecipeName, lotDataList.get(0), trayList.size());

			for (Durable durableData : trayList)
			{
				CommonValidation.CheckDurableState(durableData);
				CommonValidation.CheckDurableHoldState(durableData);
			
				this.createTrayElement(doc, durableData,trayLotListMap);
			}

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupCancelCommandSendForClave");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupCancelCommandSendForClave");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}

	private Element generateBodyTemplate(Element bodyElement, String machineRecipeName, Lot lotData, int quantity) throws CustomException
	{
		XmlUtil.addElement(bodyElement, "PRODUCTSPECNAME", lotData.getProductSpecName());
		XmlUtil.addElement(bodyElement, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		XmlUtil.addElement(bodyElement, "PROCESSFLOWNAME", lotData.getProcessFlowName());
		XmlUtil.addElement(bodyElement, "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		XmlUtil.addElement(bodyElement, "PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		XmlUtil.addElement(bodyElement, "PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", machineRecipeName);
		XmlUtil.addElement(bodyElement, "WORKORDER", lotData.getProductRequestName());
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

		Element trayGroupNameElement = new Element("TRAYGROUPNAME");
		trayGroupNameElement.setText(bodyElementOri.getChildText("TRAYGROUPNAME"));
		bodyElement.addContent(trayGroupNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
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

	private void createTrayElement(Document doc, Durable durableData,Map<String,List<Lot>> trayLotListMap) throws CustomException
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
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);
			
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
}