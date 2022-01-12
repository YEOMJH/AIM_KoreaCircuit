package kr.co.aim.messolution.datacollection.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import kr.co.aim.greentrack.datacollection.management.info.CollectDataInfo;
import kr.co.aim.greentrack.datacollection.management.info.ext.ResultData;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;

public class DataCollectionInfoUtil {

	public  CollectDataInfo collectDataInfo(String newDataFlag, String DCSpecName, String DCSpecVersion, String materialType, 
												  String materialName, String factoryName, String productSpecName, String productSpecVersion,
												  String processFlowName, String processFlowVersion, String processOperationName, 
												  String processOperationVersion, String machineName, String machineRecipeName, 
												  Timestamp createTime, String createUser, String createComment, List<SampleData> sds,
												  Map<String, String> udfs)
	{
		CollectDataInfo collectDataInfo = new CollectDataInfo();
		collectDataInfo.setNewDataFlag(newDataFlag);
		collectDataInfo.setDCSpecName(DCSpecName);
		collectDataInfo.setDCSpecVersion(DCSpecVersion);
		collectDataInfo.setMaterialType(materialType);
		collectDataInfo.setMaterialName(materialName);
		collectDataInfo.setFactoryName(factoryName);
		collectDataInfo.setProductSpecName(productSpecName);
		collectDataInfo.setProductSpecVersion(productSpecVersion);
		collectDataInfo.setProcessFlowName(processFlowName);
		collectDataInfo.setProcessFlowVersion(processFlowVersion);
		collectDataInfo.setProcessOperationName(processOperationName);
		collectDataInfo.setProcessOperationVersion(processOperationVersion);
		collectDataInfo.setMachineName(machineName);
		collectDataInfo.setMachineRecipeName(machineRecipeName);
		collectDataInfo.setCreateTime(createTime);
		collectDataInfo.setCreateUser(createUser);
		collectDataInfo.setCreateComment(createComment);
		collectDataInfo.setSds(sds);
		collectDataInfo.setUdfs(udfs);
		
		return collectDataInfo;
	}
	
	public  List<SampleData> sampleData(String sampleMaterialName, String sampleMaterialType, long sampleNo, List<ResultData> resultDataList)
	{
		List<SampleData> sds = new ArrayList<SampleData>();
		
		SampleData sampleData = new SampleData();
		sampleData.setSampleMaterialName(sampleMaterialName);
		sampleData.setSampleMaterialType(sampleMaterialType);
		sampleData.setSampleNo(sampleNo);
		sampleData.setRds(resultDataList);
		
		sds.add(sampleData);
		
		return sds;
	}
	
	public List<SampleData> getSampleData(Element element) throws Exception
	{
				
		List<SampleData> sds = new ArrayList<SampleData>();
		
		SampleData sampleData =	new SampleData();			
		sampleData.setSampleMaterialName( element.getChildText("PRODUCTNAME"));
		sampleData.setSampleMaterialType( "Product" );
		sampleData.setSampleNo( Integer.parseInt(element.getChildText("POSITION")));
		
		Element itemlist = (Element) element.getChild("ITEMLIST");
		
		List items = (List) itemlist.getChildren("ITEM");
					
		List<ResultData> resultDataList = new ArrayList<ResultData>();
		
		for ( int j = 0; j < items.size(); j++ )
		{
			Element itemE = (Element) items.get(j);
			if ( itemE != null )
			{					
				
				Element sitelist = (Element) itemE.getChild("SITELIST");
				List sites = (List) sitelist.getChildren("SITE");
				
				for ( int k = 0; k < sites.size(); k++ )
				{
					Element siteE = (Element) sites.get(k);
					
					if(siteE != null){
					
						ResultData resultData =  new ResultData();					
						resultData.setItemName( itemE.getChildText("ITEMNAME"));
						resultData.setSiteName( siteE.getChildText("SITENAME"));
						resultData.setResult( siteE.getChildText("SITEVALUE"));
						
						resultDataList.add( resultData );
					}
				}
			}
		}
		
		
		sampleData.setRds( resultDataList );
		sds.add( sampleData );
				
		return sds;
	}
}
