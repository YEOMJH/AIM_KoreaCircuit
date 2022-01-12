package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import kr.co.aim.messolution.datacollection.service.DataCollectionServiceImpl;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import org.jdom.Element;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

public class NikonEventReport extends AsyncHandler {
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("NikonEventReport", getEventUser(), getEventComment(), null, null);
 
		String machineName 			= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName 			= SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String eventID              = SMessageUtil.getBodyItemValue(doc, "EVENTID", false);
		Element itemList	 		= SMessageUtil.getBodySequenceItem(doc, "ITEMLIST", true);

		
		String strTemp = "";
		String strDCSpec = "";
		String strDCSpec1 = "";
		String productName = "";
		
		if(itemList != null && eventID.equals("1101"))
		{
			for(Iterator iteratoritemList = itemList.getChildren().iterator();iteratoritemList.hasNext();)
			{
				Element itemE = (Element)iteratoritemList.next();
				
				if(itemE != null)
				{
					String itemName = itemE.getChildText("ITEMNAME");
					Element sitelistElement = itemE.getChild("SITELIST");
					
					String siteName = "";
					String siteValue = "";
					
					if(sitelistElement != null)
					{						
						for(Iterator siteIterator = sitelistElement.getChildren().iterator();siteIterator.hasNext();)
						{
							Element siteElement = (Element)siteIterator.next();
							siteName = siteElement.getChild("SITENAME").getText();
							siteValue = siteElement.getChild("SITEVALUE").getText();
						}
					}
					
					if(itemName.equals("PlateProcessStartGlassID"))
					{
						productName = siteValue;
						break;
					}
				}
			}
		}
		
		//Check processopeation type
		if(eventID.equals("1101"))
		{
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			List<Map<String, Object>> processOperationSpecData = DataCollectionServiceUtil.getProcessOperationSpecData(productData, productData.getProcessOperationName(), unitName);
			
			if(processOperationSpecData.size() > 0)
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				
			    Map<String, String> udfs = productData.getUdfs();
				int index=unitName.indexOf("EXP");
				
				if(index != -1)
					setEventInfo.getUdfs().put("BEFOREPROCESSOPERATION", productData.getProcessOperationName());
				else
					setEventInfo.getUdfs().put("BEFOREPROCESSOPERATION", productData.getProcessOperationName());

				setEventInfo.getUdfs().put("BEFOREPROCESSMACHINE", machineName);

				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo,eventInfo);
			}
			
			
			//add check factoryname and productgrade
			if((StringUtil.equals("CELL",productData.getFactoryName()) && !StringUtil.equals("N",productData.getProductGrade())) ||
					!StringUtil.equals("CELL",productData.getFactoryName()))
			{
				//Get TPOM Policy
				try 
				{
					//Get DCSpecInfo
					DCSpec dcSpecData = this.getDCSpecTPOM(productData);
					strTemp = dcSpecData.getKey().getDCSpecName();
					
					//Get SPC TPOMPolicyInfo
					List<Map<String, Object>> TPOMPolicyInfo = DataCollectionServiceImpl.getSPCTPOMPolicyInfo(strTemp, productData);
					
					if(TPOMPolicyInfo.size() > 0 )
					{
						// Insert DCDATA
						DataCollectionServiceImpl.insertDCData(doc, eventInfo, productData, dcSpecData);
						
						// Insert DCDATARESULT
						DataCollectionServiceImpl.insertDCDataResult(doc, eventInfo, TPOMPolicyInfo);
				
						StringBuilder spcXmlMsg = new StringBuilder(50000);
				        
				        spcXmlMsg.append("<Message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    <Head>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <messageName>").append("/SPCEngine/execute").append("</messageName>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <id />").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <messageType>").append("Application").append("</messageType>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <replyChannel>").append("GVO.A1.MES.PRD.GEN.EDCsvr").append("</replyChannel>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    </Head>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    <SpcData>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <DcDataId>").append(DataCollectionServiceUtil.getCurrDcDataId()).append("</DcDataId>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    </SpcData>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <code />").append(SystemPropHelper.CR);
				        spcXmlMsg.append("      <message />").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
				        spcXmlMsg.append("    </Message>").append(SystemPropHelper.CR);
			
						try
						{
							GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), spcXmlMsg.toString(), "SPCSender");
						}
						catch (Exception ex)
						{
							eventLog.error(ex);
						}
					}
					else
					{
						eventLog.error("SPC Modeling not have config this DCSpec");
					}
				}
				catch (Exception e) 
				{
					eventLog.error(e);
				}
				//End TPOM Policy
				
				//Get TFOM Policy
				try 
				{
					DCSpec dcSpecData = this.getDCSpecTFOM(productData);
					strDCSpec = dcSpecData.getKey().getDCSpecName();
					
					//check DCSpec whether duplicate
					if(!StringUtil.equals(strTemp, strDCSpec))
					{	
						//Get SPC TPOMPolicyInfo
						List<Map<String, Object>> TFOMPolicyInfo= DataCollectionServiceImpl.getSPCTFOMPolicyInfo(strDCSpec, productData);
						
						if(TFOMPolicyInfo.size() > 0)
						{
							// Insert DCDATA
							DataCollectionServiceImpl.insertDCData(doc, eventInfo, productData, dcSpecData);
							
							// Insert DCDATARESULT
							DataCollectionServiceImpl.insertDCDataResult(doc, eventInfo, TFOMPolicyInfo);
				
							StringBuilder spcXmlMsg = new StringBuilder(50000);
					        
					        spcXmlMsg.append("<Message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <Head>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <messageName>").append("/SPCEngine/execute").append("</messageName>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <id />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <messageType>").append("Application").append("</messageType>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <replyChannel>").append("GVO.A1.MES.PRD.GEN.EDCsvr").append("</replyChannel>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Head>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <SpcData>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <DcDataId>").append(DataCollectionServiceUtil.getCurrDcDataId()).append("</DcDataId>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </SpcData>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <code />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <message />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Message>").append(SystemPropHelper.CR);
				
							try
							{
								GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), spcXmlMsg.toString(), "SPCSender");
							}
							catch (Exception ex)
							{
								eventLog.error(ex);
							}
						}
						else
						{
							eventLog.error("SPC Modeling not have config this DCSpec");
						}
					}
				}
				catch (Exception e)
				{
					eventLog.error(e);
				}
				//End TFOM Policy
				
				//Get TFPOM Policy
				try
				{
					DCSpec dcSpecData = this.getDCSpecTFPOM(productData);
					strDCSpec1 = dcSpecData.getKey().getDCSpecName();
					
					//check DCSpec whether duplicate
					if(!StringUtil.equals(strTemp, strDCSpec1) && !StringUtil.equals(strDCSpec, strDCSpec1))
					{
						//Get SPC TPOMPolicyInfo
						List<Map<String, Object>> TFPOMPolicyInfo= DataCollectionServiceImpl.getSPCTFPOMPolicyInfo(strDCSpec1, productData);
						
						if(TFPOMPolicyInfo.size() > 0)
						{
							// Insert DCDATA
							DataCollectionServiceImpl.insertDCData(doc, eventInfo, productData, dcSpecData);
		
							// Insert DCDATARESULT
							DataCollectionServiceImpl.insertDCDataResult(doc, eventInfo, TFPOMPolicyInfo);
					
							StringBuilder spcXmlMsg = new StringBuilder(50000);
					        
					        spcXmlMsg.append("<Message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <Head>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <messageName>").append("/SPCEngine/execute").append("</messageName>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <id />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <messageType>").append("Application").append("</messageType>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <replyChannel>").append("GVO.A1.MES.PRD.GEN.EDCsvr").append("</replyChannel>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Head>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <SpcData>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <DcDataId>").append(DataCollectionServiceUtil.getCurrDcDataId()).append("</DcDataId>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </SpcData>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <code />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("      <message />").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
					        spcXmlMsg.append("    </Message>").append(SystemPropHelper.CR);
				
							try
							{
								GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), spcXmlMsg.toString(), "SPCSender");
							}
							catch (Exception ex)
							{
								eventLog.error(ex);
							}
						}
						else
						{
							eventLog.error("SPC Modeling not have config this DCSpec");
						}
					}
				}
				catch (Exception e) 
				{
					eventLog.error(e);
				}
				//End TFPOM Policy
				
				// Insert CT_PRODUCTPROCESSDATA, CT_PRODUCTPROCESSDATAITEM
				DataCollectionServiceImpl.insertCTProductProcessData(doc, eventInfo);
			}
			else
			{
				eventLog.error("ProductJudge is N,So not collection ProductProcessData");
			}
		}
	}

	private DCSpec getDCSpecTPOM(Product productData) throws CustomException
	{
		StringBuilder sqlBuilderTPOM = new StringBuilder();
		sqlBuilderTPOM.append("SELECT C.DCSpecName, C.DCSpecVersion" + "\n")
			.append("    FROM TPOMPolicy C" + "\n")
			.append(" WHERE C.factoryName = ?" + "\n")  
			.append("    AND C.productSpecName = ?" + "\n")
			.append("    AND C.productSpecVersion = ?" + "\n")  //PRODUCTSPECNAME/PRODUCTSPECVERSION
			.append("    AND C.processOperationName = ?" + "\n")
			.append("    AND C.processOperationVersion = ?" + "\n")
			.append("    AND C.machineName = ?" + "\n");
		
		Object[] bindArrayTPOM = new Object[] {productData.getFactoryName(),productData.getProductSpecName(),productData.getProductSpecVersion(),
				productData.getProcessOperationName(), productData.getProcessOperationVersion(), productData.getMachineName()};
		
		List<ListOrderedMap> resultTPOM;
		try
		{
			resultTPOM = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilderTPOM.toString(), bindArrayTPOM);
		}
		catch (FrameworkErrorSignal fe)
		{
			resultTPOM = new ArrayList<ListOrderedMap>();
		}
		
		if (resultTPOM.size() < 1)
		{
			//CUSTOM-0021: not defined DC spec in {0} policy
			throw new CustomException("CUSTOM-0021", "TPOM");
		}
		
		String dcSpecNameTPOM = CommonUtil.getValue(resultTPOM.get(0), "DCSPECNAME");
		String dcSpecVersionTPOM = CommonUtil.getValue(resultTPOM.get(0), "DCSPECVERSION");
		
		DCSpecKey keyInfoTPOM = new DCSpecKey();
		keyInfoTPOM.setDCSpecName(dcSpecNameTPOM);
		keyInfoTPOM.setDCSpecVersion(dcSpecVersionTPOM);
		
		try
		{
			DCSpec dcSpecData = DataCollectionServiceProxy.getDCSpecService().selectByKey(keyInfoTPOM);
			
			return dcSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "DCSpec");
		}
	}
	
	private DCSpec getDCSpecTFOM(Product productData) throws CustomException
	{	
		StringBuilder sqlBuilderTFOM = new StringBuilder();
		sqlBuilderTFOM.append("SELECT C.DCSpecName, C.DCSpecVersion" + "\n")
			.append("    FROM TFOMPolicy C" + "\n")
			.append(" WHERE C.factoryName = ?" + "\n")
			.append("    AND C.processFlowName = ?" + "\n")
			.append("    AND C.processFlowVersion = ?" + "\n")
			.append("    AND C.processOperationName = ?" + "\n")
			.append("    AND C.processOperationVersion = ?" + "\n")
			.append("    AND C.machineName = ?" + "\n");
		
		Object[] bindArrayTFOM = new Object[] {productData.getFactoryName(), productData.getProcessFlowName(), productData.getProcessFlowVersion(),
											productData.getProcessOperationName(), productData.getProcessOperationVersion(),
											productData.getMachineName()};
		
		List<ListOrderedMap> resultTFOM;
		try
		{
			resultTFOM = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilderTFOM.toString(), bindArrayTFOM);
		}
		catch (FrameworkErrorSignal fe)
		{
			resultTFOM = new ArrayList<ListOrderedMap>();
		}
		
		if (resultTFOM.size() < 1)
		{
			//CUSTOM-0021: not defined DC spec in {0} policy
			throw new CustomException("CUSTOM-0021", "TFOM");
		}
		
		String dcSpecNameTFOM = CommonUtil.getValue(resultTFOM.get(0), "DCSPECNAME");
		String dcSpecVersionTFOM = CommonUtil.getValue(resultTFOM.get(0), "DCSPECVERSION");
		
		DCSpecKey keyInfoTFOM = new DCSpecKey();
		keyInfoTFOM.setDCSpecName(dcSpecNameTFOM);
		keyInfoTFOM.setDCSpecVersion(dcSpecVersionTFOM);
		
		try
		{
			DCSpec dcSpecData = DataCollectionServiceProxy.getDCSpecService().selectByKey(keyInfoTFOM);
			
			return dcSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "DCSpec");
		}
		
	}

	private DCSpec getDCSpecTFPOM(Product productData) throws CustomException
	{
		StringBuilder sqlBuilderTFPOM = new StringBuilder();
		sqlBuilderTFPOM.append("SELECT C.DCSpecName, C.DCSpecVersion" + "\n")
			.append("    FROM TFPOMPolicy C" + "\n")
			.append(" WHERE C.factoryName = ?" + "\n")  
			.append("    AND C.productSpecName = ?" + "\n")
			.append("    AND C.productSpecVersion = ?" + "\n")  //PRODUCTSPECNAME/PRODUCTSPECVERSION
			.append("    AND C.processFlowName = ?" + "\n")
			.append("    AND C.processFlowVersion = ?" + "\n")
			.append("    AND C.processOperationName = ?" + "\n")
			.append("    AND C.processOperationVersion = ?" + "\n")
			.append("    AND C.machineName = ?" + "\n");
		
		Object[] bindArrayTFPOM = new Object[] {productData.getFactoryName(),productData.getProductSpecName(),productData.getProductSpecVersion(), productData.getProcessFlowName(), productData.getProcessFlowVersion(),
				productData.getProcessOperationName(), productData.getProcessOperationVersion(), productData.getMachineName()};
		
		List<ListOrderedMap> resultTFPOM;
		try
		{
			resultTFPOM = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilderTFPOM.toString(), bindArrayTFPOM);
		}
		catch (FrameworkErrorSignal fe)
		{
			resultTFPOM = new ArrayList<ListOrderedMap>();
		}
		
		if (resultTFPOM.size() < 1)
		{
			// CUSTOM-0021: not defined DC spec in {0} policy
			throw new CustomException("CUSTOM-0021", "TFPOM");
		}
		
		String dcSpecNameTFPOM = CommonUtil.getValue(resultTFPOM.get(0), "DCSPECNAME");
		String dcSpecVersionTFPOM = CommonUtil.getValue(resultTFPOM.get(0), "DCSPECVERSION");
		
		DCSpecKey keyInfoTFPOM = new DCSpecKey();
		keyInfoTFPOM.setDCSpecName(dcSpecNameTFPOM);
		keyInfoTFPOM.setDCSpecVersion(dcSpecVersionTFPOM);
		
		try
		{
			DCSpec dcSpecData = DataCollectionServiceProxy.getDCSpecService().selectByKey(keyInfoTFPOM);
			
			return dcSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "DCSpec");
		}
	}
}
