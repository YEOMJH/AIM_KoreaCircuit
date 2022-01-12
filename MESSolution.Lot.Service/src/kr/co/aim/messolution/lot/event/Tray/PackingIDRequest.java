package kr.co.aim.messolution.lot.event.Tray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
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
import kr.co.aim.greenframe.util.object.ObjectUtil;
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
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class PackingIDRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(PackingIDRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingIDReply");

			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", false);
			String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", false);
			String lotDetailGrade = "";
			String packingName = "";
			int quantity = 0;
			
			Lot lotInfoForLabel = new Lot();
			
			if(StringUtil.isEmpty(panelName) && StringUtil.isNotEmpty(trayName))
			{
				List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByTray(trayName);
				
				quantity = lotList.size();
				
				if(lotList == null || lotList.size() == 0)
				{
					// LOT-0087 : No Panel by Tray[{0}] !
					throw new CustomException("LOT-0087", trayName);
				}
				else
				{
					Lot lotData = lotList.get(0);
					lotInfoForLabel = (Lot)ObjectUtil.copyTo(lotData);
					lotDetailGrade = lotData.getUdfs().get("LOTDETAILGRADE").toString();
					if(StringUtil.in(lotDetailGrade, "C1", "C2", "C3", "C4","C5","C6"))
					{
						lotDetailGrade = "C0";
					}
					
					packingName = CreatePackingName("INNER", lotDetailGrade, lotData.getProductSpecName(), lotData.getProductRequestName(), trayName);
				}
			}
			else if(StringUtil.isEmpty(trayName) && StringUtil.isNotEmpty(panelName))
			{
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(panelName);
				lotInfoForLabel = (Lot)ObjectUtil.copyTo(lotData);
				lotDetailGrade = "S";
				
				packingName = CreatePackingName("SPALLET", "S", lotData.getProductSpecName(), lotData.getProductRequestName(), trayName);
			}
			
			ProductSpec productSpecInfo = ProductServiceProxy.getProductSpecService().selectByKey(new ProductSpecKey(lotInfoForLabel.getFactoryName(), lotInfoForLabel.getProductSpecName(), lotInfoForLabel.getProductSpecVersion()));
			String productSpecName = lotInfoForLabel.getProductSpecName().substring(0, 16);
			String desc = productSpecInfo.getDescription();
			
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotInfoForLabel.getProductRequestName());
			String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			
			boolean superWOFlag = true;
			
			if(StringUtil.isEmpty(superProductRequestName))
			{
				superWOFlag = false;
				superProductRequestName = workOrderData.getKey().getProductRequestName();
			}
			
			String batchNo = superProductRequestName.substring(0, 8) + lotDetailGrade;
			if(batchNo.length() == 9)
				batchNo += "0";
			
			String WODesc = "";
			String subProductionType = ""; 
			
			if(superWOFlag)
			{
				SuperProductRequest superWOData = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[] {superProductRequestName});
				WODesc = superWOData.getDescription();
				subProductionType = superWOData.getSubProductionType();
			}
			else
			{
				WODesc = workOrderData.getUdfs().get("DESCRIPTION").toString();
				subProductionType = workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").toString();
			}
			
			String comment = subProductionType + "/" + WODesc;
			
			this.generateBodyTemplate(doc, SMessageUtil.getBodyElement(doc), packingName, productSpecName, desc, batchNo, String.valueOf(quantity), comment);
			
			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingIDReply");
			this.generateBodyTemplate(doc, SMessageUtil.getBodyElement(doc), "", "", "", "", "", "");

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PackingIDReply");

			this.generateBodyTemplate(doc, SMessageUtil.getBodyElement(doc), "", "", "", "", "", "");

			throw new CustomException(e);
		}
	}
	
	private String CreatePackingName(String packingType, String lotDetailGrade, String productSpecName, String productRequestName, String trayName) throws CustomException
	{
		String productSpecSize = productSpecName.substring(2, 3);
		String productType = "";
		
		try
		{
			Integer.parseInt(productSpecSize);
			productType = "0" + productSpecName.substring(2, 5);
		}
		catch (Exception e)
		{
			productType = "10" + productSpecName.substring(3, 5);
		}
		
		String vendor = "00";
		
		if (!StringUtils.isEmpty(trayName))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			try
			{
				vendor =trayName.substring(7, 9);//caixu 2020/1/20 DurableSpec Modify TrayName
			}
			catch(Exception x)
			{
				throw new CustomException("PROCESSGROUP-0017", durableData.getKey().getDurableName(), durableData.getDurableSpecName());
			}
		}

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PACKINGTYPE", packingType);
		nameRuleAttrMap.put("PRODUCTTYPE", productType);
		nameRuleAttrMap.put("VENDOR", vendor);
		nameRuleAttrMap.put("LOTGRADE", lotDetailGrade + "0");

		List<String> nameList = CommonUtil.generateNameByNamingRule("PackingNaming", nameRuleAttrMap, 1);
		String newPackingName = nameList.get(0);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventName("CreateProcessGroup");

		String ProcessGroupType = "";
		String MaterialType = "";

		if (packingType.equals("INNER"))
		{
			ProcessGroupType = "InnerPacking";
			MaterialType = "Panel";
		}
		else
		{
			ProcessGroupType = "ScrapPacking";
			MaterialType = "Panel";
		}

		InsertProcessGroup(newPackingName, ProcessGroupType, MaterialType, lotDetailGrade, productSpecName, productRequestName, eventInfo);
		InsertProcessGroupHistory(newPackingName, lotDetailGrade, productSpecName, productRequestName, eventInfo);
		
		return newPackingName;
	}

	private void InsertProcessGroup(String ProcessGroupName, String ProcessGroupType, String MaterialType, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUP  ");
			sql.append("(PROCESSGROUPNAME, PROCESSGROUPTYPE, MATERIALTYPE, MATERIALQUANTITY, LASTEVENTNAME, ");
			sql.append(" LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTTIMEKEY, CREATETIME, ");
			sql.append(" CREATEUSER, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) ");
			sql.append("VALUES  ");
			sql.append("(:PROCESSGROUPNAME, :PROCESSGROUPTYPE, :MATERIALTYPE, :MATERIALQUANTITY, :LASTEVENTNAME, ");
			sql.append(" :LASTEVENTTIME, :LASTEVENTUSER, :LASTEVENTCOMMENT, :LASTEVENTTIMEKEY, :CREATETIME, ");
			sql.append(" :CREATEUSER, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", ProcessGroupName);
			bindMap.put("PROCESSGROUPTYPE", ProcessGroupType);
			bindMap.put("MATERIALTYPE", MaterialType);
			bindMap.put("MATERIALQUANTITY", 0);
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("CREATETIME", eventInfo.getEventTime());
			bindMap.put("CREATEUSER", eventInfo.getEventUser());
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);
			bindMap.put("LOTDETAILGRADE", lotGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + ProcessGroupName + " into PROCESSGROUP   Error : " + e.toString());
		}
	}

	private void InsertProcessGroupHistory(String ProcessGroupName, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUPHISTORY  ");
			sql.append("(PROCESSGROUPNAME, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, ");
			sql.append(" EVENTCOMMENT, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) ");
			sql.append("VALUES  ");
			sql.append("(:PROCESSGROUPNAME, :TIMEKEY, :EVENTTIME, :EVENTNAME, :EVENTUSER, ");
			sql.append(" :EVENTCOMMENT, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", ProcessGroupName);
			bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);
			bindMap.put("LOTDETAILGRADE", lotGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + ProcessGroupName + " into PROCESSGROUPHISTORY   Error : " + e.toString());
		}
	}
	
	private void generateBodyTemplate(Document doc, Element bodyElementOri, String packingName, String productSpecName, String productDesc, String batchNo, String quantity, String comment) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(bodyElementOri.getChildText("MACHINENAME"));
		bodyElement.addContent(machineNameElement);

		Element trayNameElement = new Element("TRAYNAME");
		trayNameElement.setText(bodyElementOri.getChildText("TRAYNAME"));
		bodyElement.addContent(trayNameElement);

		Element panelNameElement = new Element("PANELNAME");
		panelNameElement.setText(bodyElementOri.getChildText("PANELNAME"));
		bodyElement.addContent(panelNameElement);

		Element packingNameElement = new Element("PACKINGNAME");
		packingNameElement.setText(packingName);
		bodyElement.addContent(packingNameElement);
		
		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(productSpecName);
		bodyElement.addContent(productSpecNameElement);
		
		Element productSpecDescElement = new Element("PRODUCTSPECDESCRIPTION");
		for(int i = 0 ; i < productDesc.length() ; i++)
		{
			int charInt = productDesc.charAt(i);
			String text = String.valueOf(charInt);

			Element desc = new Element("DESC");
			desc.setText(text);
			productSpecDescElement.addContent(desc);
		}
		bodyElement.addContent(productSpecDescElement);

		Element batchNoElement = new Element("BATCHNO");
		batchNoElement.setText(batchNo);
		bodyElement.addContent(batchNoElement);
		
		Element quantityElement = new Element("QUANTITY");
		quantityElement.setText(quantity);
		bodyElement.addContent(quantityElement);
		
		Element commentElement = new Element("COMMENT");
		for(int i = 0 ; i < comment.length() ; i++)
		{
			int charInt = comment.charAt(i);
			String text = String.valueOf(charInt);

			Element desc = new Element("DESC");
			desc.setText(text);
			commentElement.addContent(desc);
		}
		bodyElement.addContent(commentElement);
		
		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}
}