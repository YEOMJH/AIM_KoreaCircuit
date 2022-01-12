package kr.co.aim.messolution.datacollection.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.datacollection.management.info.CollectDataInfo;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataCollectionServiceImpl implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DataCollectionServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	public String collectData(String dcSpecName, String lotName,
			String productName, String machineName, String machineRecipeName,
			List<SampleData> sds, Map<String, String> udfs, String createUser,
			String createTime, String createComment)
			throws FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal {
		if (log.isInfoEnabled()) {
			log.info("dcSpecName = " + dcSpecName);
			log.info("lotName = " + lotName);
			log.info("productName = " + productName);
			log.info("machineName = " + machineName);
			log.info("machineRecipeName = " + machineRecipeName);
			log.info("createUser = " + createUser);
			log.info("createTime = " + createTime);
			log.info("createComment = " + createComment);
		}

		CollectDataInfo collectDataInfo = new CollectDataInfo();

		DCSpecKey dcSpecKey = new DCSpecKey();
		dcSpecKey.setDCSpecName(dcSpecName);
		dcSpecKey.setDCSpecVersion("00001");

		DCSpec dcSpecdata = null;
		dcSpecdata = DataCollectionServiceProxy.getDCSpecService().selectByKey(
				dcSpecKey);

		collectDataInfo.setNewDataFlag("Y");
		collectDataInfo.setDCSpecName(dcSpecName);
		collectDataInfo.setDCSpecVersion("00001");
		collectDataInfo.setMaterialType(dcSpecdata.getMaterialType());

		Lot lotData = null;
		if (dcSpecdata.getMaterialType().equals("Lot")) {
			LotKey lotKey = new LotKey();

			lotKey.setLotName(lotName);
			lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

			collectDataInfo.setMaterialName(lotName);
			collectDataInfo.setFactoryName(lotData.getFactoryName());
			collectDataInfo.setProductSpecName(lotData.getProductSpecName());
			collectDataInfo.setProductSpecVersion("00001");
			collectDataInfo.setProcessFlowName(lotData.getProcessFlowName());
			collectDataInfo.setProcessFlowVersion("00001");
			collectDataInfo.setProcessOperationName(lotData
					.getProcessOperationName());
			collectDataInfo.setProcessOperationVersion("00001");
		} else if (dcSpecdata.getMaterialType().equals("Product")) {
			ProductKey productKey = new ProductKey();

			productKey.setProductName(productName);
			Product productData = ProductServiceProxy.getProductService()
					.selectByKey(productKey);

			collectDataInfo.setMaterialName(productName);
			collectDataInfo.setFactoryName(productData.getFactoryName());
			collectDataInfo
					.setProductSpecName(productData.getProductSpecName());
			collectDataInfo.setProductSpecVersion("00001");
			collectDataInfo
					.setProcessFlowName(productData.getProcessFlowName());
			collectDataInfo.setProcessFlowVersion("00001");
			collectDataInfo.setProcessOperationName(productData
					.getProcessOperationName());
			collectDataInfo.setProcessOperationVersion("00001");
		}

		collectDataInfo.setMachineName(machineName);
		collectDataInfo.setMachineRecipeName(machineRecipeName);
		collectDataInfo.setSds(sds);
		collectDataInfo.setUdfs(udfs);
		collectDataInfo.setCreateUser(createUser);
		if (createTime == "") {
			createTime = ConvertUtil.getCurrTime();
		}
		collectDataInfo.setCreateTime(ConvertUtil
				.convertToTimeStamp(createTime));
		collectDataInfo.setCreateComment(createComment);

		long result = DataCollectionServiceProxy.getDCDataService()
				.collectData(collectDataInfo);
		log.info("ProcessData Collection");

		String sresult = String.valueOf(result);

		return sresult;
	}
	
	public static void insertDCData(Document doc, EventInfo eventInfo, Product productData, DCSpec dcSpecData)
		throws CustomException
	{
		String materialName = "";
		String materialType = "";
		String sql = "";
		
		if(SMessageUtil.getMessageName(doc).equals("LotProcessData"))
		{
			materialName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
			materialType = "Lot";
		}
		else if(SMessageUtil.getMessageName(doc).equals("ProductProcessData"))
		{
			materialName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
			materialType = "Product";
		}
		else if(SMessageUtil.getMessageName(doc).equals("NikonEventReport"))
		{
			materialName = productData.getKey().getProductName();
			materialType = "Product";
		}
		//MES_DCDATA Add Lotname,BEFOREPROCESSOPERATION,BEFOREPROCESSMACHINE
		sql = "Insert into MES_DCDATA                                                                 		 	"
			+ " (DCDATAID, DCSPECNAME, DCSPECVERSION, MATERIALTYPE, MATERIALNAME,                            	"
			+ " FACTORYNAME, PRODUCTSPECNAME, PRODUCTSPECVERSION, PROCESSFLOWNAME, PROCESSFLOWVERSION,       	"
			+ " PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, MACHINENAME, MACHINERECIPENAME,   				"
			+ " CREATEUSER, CREATECOMMENT, TIMEKEY ,LOTNAME, BEFOREPROCESSOPERATION, BEFOREPROCESSMACHINE)                                           			      	"
			+ " Values                                                                                       	"
			+ " (:dcDataId, :dcSpecName, :dcSpecVersion, :materialType, :materialName,                       	"
			+ " :factoryName, :productSpecName, :productSpecVersion, :processFlowName, :processFlowVersion,     "
			+ " :processOperationName, :processOperationVersion, :machineName, :machineRecipeName,				"
			+ " :createUser, :createComment, :timeKey ,:lotName, :beforeProcessOperation, :beforeProcessMachine)                                               			";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("dcDataId", DataCollectionServiceUtil.getNextDCDataId());
		bindMap.put("dcSpecName", dcSpecData.getKey().getDCSpecName());
		bindMap.put("dcSpecVersion", dcSpecData.getKey().getDCSpecVersion());
		bindMap.put("materialType", materialType);
		bindMap.put("materialName", materialName);
		bindMap.put("factoryName", productData.getFactoryName());
		bindMap.put("productSpecName", productData.getProductSpecName());
		bindMap.put("productSpecVersion", productData.getProductSpecVersion());
		bindMap.put("processFlowName", productData.getProcessFlowName());
		bindMap.put("processFlowVersion", productData.getProcessFlowVersion());
		bindMap.put("processOperationVersion", productData.getProcessOperationVersion());
		
		bindMap.put("lotName", productData.getLotName());
		bindMap.put("beforeProcessOperation", productData.getUdfs().get("BEFOREPROCESSOPERATION"));
		bindMap.put("beforeProcessMachine", productData.getUdfs().get("BEFOREPROCESSMACHINE"));
		
		bindMap.put("machineName", SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true));
		if(SMessageUtil.getMessageName(doc).equals("NikonEventReport"))
		{
			bindMap.put("machineRecipeName", "");
		}
		else
		{
			bindMap.put("machineRecipeName", SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true));
		}
		
		bindMap.put("createUser", eventInfo.getEventUser());
		bindMap.put("createComment", eventInfo.getEventComment());
		bindMap.put("timeKey",StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()):eventInfo.getEventTimeKey());

		String eventuser = eventInfo.getEventUser();
		String exp = "EXP";
		int index=eventuser.indexOf(exp);
		
		if(index != -1)
		{
			bindMap.put("processOperationName", productData.getProcessOperationName());
		}
		else
		{
			bindMap.put("processOperationName", SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true));
		}
		
		GenericServiceProxy.getDcolQueryTemplate().update(sql, bindMap);
	}

	public static void insertDCDataResult(Document doc, EventInfo eventInfo) throws CustomException
	{
		Element root = doc.getDocument().getRootElement();
		Element body = root.getChild("Body");
		Element itemListElement = body.getChild("ITEMLIST");
		Element siteListElement = null;

		String itemName = "";
		String siteName = "";
		String siteValue = "";
		String timeKey = "";
		String sql = "";
		String dcDataId = DataCollectionServiceUtil.getCurrDcDataId();
		String sampleMaterialName = "";
		
		if(SMessageUtil.getMessageName(doc).equals("LotProcessData"))
		{
			sampleMaterialName = body.getChild("LOTNAME").getText();
		}
		else if(SMessageUtil.getMessageName(doc).equals("ProductProcessData"))
		{
			sampleMaterialName = body.getChild("PRODUCTNAME").getText();
		}

		Object[] bind = null;
		List<Object[]> batchArgs = new ArrayList<Object[]>();

		timeKey = StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()):eventInfo.getEventTimeKey();
		                                                                                   
		sql = "Insert into MES_DCDATARESULT                                                		  "
			+ " (DCDATAID, SAMPLEMATERIALNAME, ITEMNAME, SITENAME, DERIVEDITEMNAME,               "
			+ " DATATYPE, RESULT, TIMEKEY)                                                        "
			+ " Values                                                                            "
			+ " (?, ?, ?, ?, ?, ?, ?, ?)         												  ";
		
		for (Iterator<?> itemIterator = itemListElement.getChildren().iterator(); itemIterator.hasNext();) {
			Element itemElement = (Element) itemIterator.next();
			
			if(itemElement != null)
			{
				itemName = itemElement.getChildText("ITEMNAME");
				siteListElement = itemElement.getChild("SITELIST");
				
				if(siteListElement != null)
				{
					for (Iterator<?> siteIterator = siteListElement.getChildren().iterator(); siteIterator.hasNext();) {
						Element siteElement = (Element) siteIterator.next();
						
						if(siteElement != null)
						{
							siteName = StringUtil.isEmpty(siteElement.getChildText("SITENAME")) ? "-":siteElement.getChild("SITENAME").getText();
							siteValue = siteElement.getChildText("SITEVALUE");

							bind = new Object[] { dcDataId, sampleMaterialName, itemName, siteName, "-", "", siteValue, timeKey };
							batchArgs.add(bind);
						}
					}
				}
			}
		}
		if (batchArgs.size() > 0) {
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
			log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_DCDATARESULT");
		}
	}

	public static void insertDCDataResult(Document doc, EventInfo eventInfo, List<Map<String, Object>> itemList) throws CustomException
	{
		Element root = doc.getDocument().getRootElement();
		Element body = root.getChild("Body");
		Element itemListElement = body.getChild("ITEMLIST");
		Element siteListElement = null;

		String itemName = "";
		String siteName = "";
		String siteValue = "";
		String timeKey = "";
		String sql = "";
		String dcDataId = DataCollectionServiceUtil.getCurrDcDataId();
		String sampleMaterialName = "";
		
		if(SMessageUtil.getMessageName(doc).equals("LotProcessData"))
		{
			sampleMaterialName = body.getChild("LOTNAME").getText();
		}
		else if(SMessageUtil.getMessageName(doc).equals("ProductProcessData"))
		{
			sampleMaterialName = body.getChild("PRODUCTNAME").getText();
		}
		else if(SMessageUtil.getMessageName(doc).equals("NikonEventReport"))
		{
			String productname = "";
			for(Iterator iteratoritemList = itemListElement.getChildren().iterator();iteratoritemList.hasNext();)
			{
				Element itemE = (Element)iteratoritemList.next();
				
				if(itemE != null)
				{
					String itemname = itemE.getChildText("ITEMNAME");
					Element sitelistElement = itemE.getChild("SITELIST");
					
					String sitename = "";
					String sitevalue = "";
					
					if(sitelistElement != null)
					{						
						for(Iterator siteIterator = sitelistElement.getChildren().iterator();siteIterator.hasNext();)
						{
							Element siteElement = (Element)siteIterator.next();
							sitename = siteElement.getChild("SITENAME").getText();
							sitevalue = siteElement.getChild("SITEVALUE").getText();
						}
					}
					
					if(itemname.equals("PlateProcessStartGlassID"))
					{
						productname = sitevalue;
						break;
					}
				}
			}
			sampleMaterialName = productname;
		}

		Object[] bind = null;
		List<Object[]> batchArgs = new ArrayList<Object[]>();

		timeKey = StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()):eventInfo.getEventTimeKey();
		                                                                                   
		sql = "Insert into MES_DCDATARESULT                                                		  "
			+ " (DCDATAID, SAMPLEMATERIALNAME, ITEMNAME, SITENAME, DERIVEDITEMNAME,               "
			+ " DATATYPE, RESULT, TIMEKEY)                                                        "
			+ " Values                                                                            "
			+ " (?, ?, ?, ?, ?, ?, ?, ?)         												  ";
		
		for (Iterator<?> itemIterator = itemListElement.getChildren().iterator(); itemIterator.hasNext();) {
			Element itemElement = (Element) itemIterator.next();
			
			if(itemElement != null)
			{
				itemName = itemElement.getChildText("ITEMNAME");
				siteListElement = itemElement.getChild("SITELIST");
				
				for(Map<String, Object> item : itemList)
				{
					if(item.get("ITEMNAME").equals(itemName))
					{
						if(siteListElement != null)
						{
							for (Iterator<?> siteIterator = siteListElement.getChildren().iterator(); siteIterator.hasNext();) {
								Element siteElement = (Element) siteIterator.next();
								
								if(siteElement != null)
								{
									siteName = StringUtil.isEmpty(siteElement.getChildText("SITENAME")) ? "-":siteElement.getChild("SITENAME").getText();
									siteValue = siteElement.getChildText("SITEVALUE");
		
									bind = new Object[] { dcDataId, sampleMaterialName, itemName, siteName, "-", "", siteValue, timeKey };
									batchArgs.add(bind);
								}
							}
						}
					}
				}
			}
		}
		if (batchArgs.size() > 0) {
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
			log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_DCDATARESULT");
		}
	}
	
	public static void insertCTLotProcessData(Document doc, EventInfo eventInfo)
	{
		Element root = doc.getDocument().getRootElement();
		Element body = root.getChild("Body");
		Element itemListElement = body.getChild("ITEMLIST");
		Element siteListElement = null;

		String sql = "";
		String itemName = "";
		String siteName = "";
		String siteValue = "";
		String timeKey = "";
		String machineName = body.getChild("MACHINENAME").getText();
		String unitName = body.getChild("UNITNAME").getText();
		String subUnitName = body.getChild("SUBUNITNAME").getText();
		String lotName = body.getChild("LOTNAME").getText();
		String carrierName = body.getChild("CARRIERNAME").getText();
		String machineRecipeName = body.getChild("MACHINERECIPENAME").getText();
		String processOperationName = body.getChild("PROCESSOPERATIONNAME").getText();
		String productSpecName = body.getChild("PRODUCTSPECNAME").getText();

		Object[] bind = null;
		List<Object[]> batchArgs = new ArrayList<Object[]>();

		String factoryName = DataCollectionServiceUtil.getMachineFactory(machineName);

		// Insert Data
		timeKey = CT_LOTPROCESSDATA(machineName, unitName,
				subUnitName, lotName, carrierName, machineRecipeName,
				processOperationName, productSpecName, eventInfo, factoryName);
		
		sql = "INSERT INTO CT_LOTPROCESSDATAITEM(TIMEKEY, ITEMNAME, SITENAME, SITEVALUE) VALUES(?, ?, ?, ?) ";
		for (Iterator itemIterator = itemListElement.getChildren().iterator(); itemIterator.hasNext();) {
			Element itemElement = (Element) itemIterator.next();
			itemName = itemElement.getChild("ITEMNAME").getText();
			siteListElement = itemElement.getChild("SITELIST");

			for (Iterator siteIterator = siteListElement.getChildren().iterator(); siteIterator.hasNext();) {
				Element siteElement = (Element) siteIterator.next();
				siteName = siteElement.getChild("SITENAME").getText();
				siteValue = siteElement.getChild("SITEVALUE").getText();

				bind = new Object[] { timeKey, itemName, siteName, siteValue };
				batchArgs.add(bind);
			}
		}
		if (batchArgs.size() > 0) {
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
			log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into CT_LOTPROCESSDATAITEM");
		}
	}
	
	public static void insertCTProductProcessData(Document doc, EventInfo eventInfo)
	throws CustomException
	{
		Element root = doc.getDocument().getRootElement();
		Element body = root.getChild("Body");
		Element itemListElement = body.getChild("ITEMLIST");
		Element siteListElement = null;

		String itemName = "";
		String siteName = "";
		String siteValue = "";
		String timeKey = "";
		String sql = "";
		String machineName = body.getChild("MACHINENAME").getText();
		String unitName = body.getChild("UNITNAME").getText();
		String subUnitName = "";
		String lotName = "";
		String carrierName = "";
		String productName = "";
		String machineRecipeName = "";
		String processOperationName = "";
		String productSpecName = "";

		if(unitName.indexOf("EXP")>-1)
		{
			if(itemListElement != null)
			{
				for(Iterator iteratoritemList = itemListElement.getChildren().iterator();iteratoritemList.hasNext();)
				{
					Element itemE = (Element)iteratoritemList.next();
					
					if(itemE != null)
					{
						String itemname = itemE.getChildText("ITEMNAME");
						Element sitelistElement = itemE.getChild("SITELIST");
						
						String sitename = "";
						String sitevalue = "";
						
						if(sitelistElement != null)
						{						
							for(Iterator siteIterator = sitelistElement.getChildren().iterator();siteIterator.hasNext();)
							{
								Element siteElement = (Element)siteIterator.next();
								sitename = siteElement.getChild("SITENAME").getText();
								sitevalue = siteElement.getChild("SITEVALUE").getText();
							}
						}
						
						if(itemname.equals("PlateProcessStartGlassID"))
						{
							productName = sitevalue;
						}
						else if(itemname.equals("PlatePrealignmentLotID"))
						{
							lotName = sitevalue;
						}
					}
				}
			}
			
			Lot lotData = CommonUtil.getLotInfoByLotName(lotName);
			
			subUnitName = "";
			carrierName = lotData.getCarrierName();
			machineRecipeName = lotData.getMachineRecipeName();
			processOperationName = lotData.getProcessOperationName();
			productSpecName = lotData.getProductSpecName();
		}
		else
		{
			subUnitName = body.getChild("SUBUNITNAME").getText();
			lotName = body.getChild("LOTNAME").getText();
			carrierName = body.getChild("CARRIERNAME").getText();
			productName = body.getChild("PRODUCTNAME").getText();
			machineRecipeName = body.getChild("MACHINERECIPENAME").getText();
			processOperationName = body.getChild("PROCESSOPERATIONNAME").getText();
			productSpecName = body.getChild("PRODUCTSPECNAME").getText();
		}

		Object[] bind = null;
		List<Object[]> batchArgs = new ArrayList<Object[]>();

		String factoryName = DataCollectionServiceUtil.getMachineFactory(machineName);

		timeKey = CT_PRODUCTPROCESSDATA(machineName,
				unitName, subUnitName, lotName, carrierName, productName,
				machineRecipeName, processOperationName, productSpecName,
				eventInfo, factoryName);
		
		sql = "INSERT INTO CT_PRODUCTPROCESSDATAITEM(TIMEKEY, ITEMNAME, SITENAME, SITEVALUE) VALUES(?, ?, ?, ?) ";
		
		for (Iterator<?> itemIterator = itemListElement.getChildren().iterator(); itemIterator.hasNext();) {
			Element itemElement = (Element) itemIterator.next();
			
			if(itemElement != null)
			{
				itemName = itemElement.getChildText("ITEMNAME");
				siteListElement = itemElement.getChild("SITELIST");
				
				if(siteListElement != null)
				{
					for (Iterator<?> siteIterator = siteListElement.getChildren().iterator(); siteIterator.hasNext();) {
						Element siteElement = (Element) siteIterator.next();
						siteName = siteElement.getChild("SITENAME").getText();
						siteValue = siteElement.getChild("SITEVALUE").getText();

						bind = new Object[] { timeKey, itemName, siteName, siteValue };
						batchArgs.add(bind);
					}
				}
			}
		}
		if (batchArgs.size() > 0) {
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
			log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into CT_PRODUCTPROCESSDATAITEM");
		}
	}
	
	public static String CT_LOTPROCESSDATA(String machineName, 
						 String unitName              ,
						 String subUnitName           ,
						 String lotName               ,
						 String carrierName           ,
						 String machineRecipeName     ,
						 String processOperationName  ,
						 String productSpecName       , 
						 EventInfo eventInfo			 ,
						 String factoryName) {
		
		String timeKey = ConvertUtil.getCurrTimeKey();
		
		try {
			// 01. Make a query.
			String sql = "INSERT INTO CT_LOTPROCESSDATA( " +
							"TIMEKEY,               " +
							"MACHINENAME,           " +
							"UNITNAME,              " +
							"SUBUNITNAME,           " +
							"LOTNAME,               " +
							"CARRIERNAME,           " +
							"MACHINERECIPENAME,     " +
							"PROCESSOPERATIONNAME,  " +
							"PRODUCTSPECNAME,       " +
							"CREATETIME,            " +
							"EVENTNAME,             " +
							"EVENTTIMEKEY,          " +
							"EVENTUSER,             " +
							"FACTORYNAME            " +
						" ) " +
						" VALUES(" +
							":timeKey,              " +
							":machineName,           " +
							":unitName,              " +
							":subUnitName,           " +
							":lotName,               " +
							":carrierName,           " +
							":machineRecipeName,     " +
							":processOperationName,  " +
							":productSpecName,       " +
							"sysdate,                " +
							":eventName,             " +
							":eventTimeKey,          " +
							":eventUser,             " +
							":factoryName            " +
						")";					

			// 02. Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("timeKey"                   ,       timeKey                     );   
			bindMap.put("machineName"               ,       machineName                 );		
			bindMap.put("unitName"                  ,       unitName                    );   
			bindMap.put("subUnitName"               ,       subUnitName                 );		
			bindMap.put("lotName"                   ,       lotName                     );   
			bindMap.put("carrierName"               ,       carrierName                 );		
			bindMap.put("machineRecipeName"         ,       machineRecipeName           );   
			bindMap.put("processOperationName"      ,       processOperationName        );		
			bindMap.put("productSpecName"           ,       productSpecName             );   
			bindMap.put("eventName"                 ,       eventInfo.getEventName()    );   
			bindMap.put("eventTimeKey"              ,       timeKey                     );		
			bindMap.put("eventUser"                 ,       eventInfo.getEventUser()    );		
			bindMap.put("factoryName"               ,       factoryName					);			
			
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
			
			return timeKey;
		} catch (Exception e) {
			return "";
		}
	}			
	
	public static String CT_PRODUCTPROCESSDATA(String machineName, 
						 String unitName              ,
						 String subUnitName           ,
						 String lotName               ,
						 String carrierName           ,
						 String productName           ,
						 String machineRecipeName     ,
						 String processOperationName  ,
						 String productSpecName		  ,
						 EventInfo eventInfo		  ,
						 String factoryName) {

		String timeKey = ConvertUtil.getCurrTimeKey();
		
		try {

			// 01. Make a query.
			String sql = "INSERT INTO CT_PRODUCTPROCESSDATA( " +
							  "TIMEKEY,               " +
			                  "MACHINENAME,           " +
			                  "UNITNAME,              " +
			                  "SUBUNITNAME,           " +
			                  "LOTNAME,               " +
			                  "CARRIERNAME,           " +
			                  "PRODUCTNAME,           " +
			                  "MACHINERECIPENAME,     " +
			                  "PROCESSOPERATIONNAME,  " +
			                  "PRODUCTSPECNAME,       " +
			                  "CREATETIME,            " +
			                  "EVENTNAME,             " +
			                  "EVENTTIMEKEY,          " +
			                  "EVENTUSER,             " +
			                  "FACTORYNAME            " +
						" ) " +
						" VALUES(" +
						    ":timeKey,               " +
							":machineName,           " +
							":unitName,              " +
							":subUnitName,           " +
							":lotName,               " +
							":carrierName,           " +
							":productName,           " +
							":machineRecipeName,     " +
							":processOperationName,  " +
							":productSpecName,       " +
							"sysdate,                " +
							":eventName,             " +
							":eventTimeKey,          " +
							":eventUser,             " +
							":factoryName            " +
						")";					

			// 02. Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("timeKey"                   ,       timeKey                     );   
			bindMap.put("machineName"               ,       machineName                 );		
			bindMap.put("unitName"                  ,       unitName                    );   
			bindMap.put("subUnitName"               ,       subUnitName                 );		
			bindMap.put("lotName"                   ,       lotName                     );   
			bindMap.put("carrierName"               ,       carrierName                 );		
			bindMap.put("productName"               ,       productName                 );		
			bindMap.put("machineRecipeName"         ,       machineRecipeName           );   
			bindMap.put("processOperationName"      ,       processOperationName        );		
			bindMap.put("productSpecName"           ,       productSpecName             );   
			bindMap.put("eventName"                 ,       eventInfo.getEventName()    );   
			bindMap.put("eventTimeKey"              ,       timeKey                     );		
			bindMap.put("eventUser"                 ,       eventInfo.getEventUser()    );		
			bindMap.put("factoryName"               ,       factoryName					);			
			
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
			
			return timeKey;
			
		} catch (Exception e) {

			return "";
		}
	}	
	
	public static void insertSPCControlSpec(String spcControlSpecName, 
						 String newSpcControlSpecName, 
						 String spcDcSpecName              			,
						 String spcControlGroupType           		,
						 String spcTotalControlCount               	,
						 String spcFactoryName           			,
						 String spcProductSpecName          	 	,
						 String spcProductSpecVersion     			,
						 String spcProcessFlowName  				,
						 String spcProcessFlowVersion		  		,
						 String spcProcessOperationName		  		,
						 String spcProcessOperationVersion          ,
						 String spcMachineName     					,
						 String spcMachineRecipeName  				,
						 String spcReferenceponame		  			,
						 String spcReferencepoversion		  		,
						 String spcReferenceMachineFlag           	,
						 String spcReferenceMachineRecipeFlag       ,
						 String eventUser) {
		
		String timeKey = ConvertUtil.getCurrTimeKey();
		
		try {

			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPEC( 	" +
							  "SPCCONTROLSPECNAME,        	" +
			                  "DCSPECNAME,           		" +
			                  "DCSPECVERSION,              	" +
			                  "CONTROLGROUPTYPE,           	" +
			                  "TOTALCONTROLCOUNT,           " +
			                  "FACTORYNAME,           		" +
			                  "PRODUCTSPECNAME,           	" +
			                  "PRODUCTSPECVERSION,     		" +
			                  "PROCESSFLOWNAME,  			" +
			                  "PROCESSFLOWVERSION,       	" +
			                  "PROCESSOPERATIONNAME,        " +
			                  "PROCESSOPERATIONVERSION,     " +
			                  "MACHINENAME,          		" +
			                  "MACHINERECIPENAME,           " +
			                  "REFERENCEPONAME,            	" +
			                  "REFERENCEPOVERSION,          " +
			                  "REFERENCEMACHINEFLAG,       	" +
			                  "REFERENCEMACHINERECIPEFLAG,  " +
			                  "UPDATETIME,            		" +
			                  "EVENTUSER            		" +
						" ) " +
						" VALUES(" +
						    ":SPCCONTROLSPECNAME,           " +
							":DCSPECNAME,           		" +
							":DCSPECVERSION,              	" +
							":CONTROLGROUPTYPE,          	" +
							":TOTALCONTROLCOUNT,            " +
							":FACTORYNAME,           		" +
							":PRODUCTSPECNAME,           	" +
							":PRODUCTSPECVERSION,     		" +
							":PROCESSFLOWNAME,  			" +
							":PROCESSFLOWVERSION,       	" +
							":PROCESSOPERATIONNAME,         " +
							":PROCESSOPERATIONVERSION,      " +
							":MACHINENAME,             		" +
							":MACHINERECIPENAME,            " +
							":REFERENCEPONAME,             	" +
							":REFERENCEPOVERSION,          	" +
							":REFERENCEMACHINEFLAG,         " +
							":REFERENCEMACHINERECIPEFLAG,   " +
							"sysdate,                		" +
							":EVENTUSER                		" +
						")";					

			// 02. Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("SPCCONTROLSPECNAME"      	,		newSpcControlSpecName			);   
			bindMap.put("DCSPECNAME"              	,   	spcDcSpecName            		);		
			bindMap.put("DCSPECVERSION"           	,   	"00001"                    		);   
			bindMap.put("CONTROLGROUPTYPE"        	,       spcControlGroupType             );		
			bindMap.put("TOTALCONTROLCOUNT"       	,       spcTotalControlCount            );   
			bindMap.put("FACTORYNAME"            	,       spcFactoryName                 	);		
			bindMap.put("PRODUCTSPECNAME"         	,       spcProductSpecName              );		
			bindMap.put("PRODUCTSPECVERSION"      	,       spcProductSpecVersion           );   
			bindMap.put("PROCESSFLOWNAME"			,       spcProcessFlowName        		);		
			bindMap.put("PROCESSFLOWVERSION"        ,       spcProcessFlowVersion           );   
			bindMap.put("PROCESSOPERATIONNAME"      ,       spcProcessOperationName   		);   
			bindMap.put("PROCESSOPERATIONVERSION"   ,       spcProcessOperationVersion      );		
			bindMap.put("MACHINENAME"               ,       spcMachineName    				);		
			bindMap.put("MACHINERECIPENAME"         ,       spcMachineRecipeName			);			
			bindMap.put("REFERENCEPONAME"           ,       spcReferenceponame    			);		
			bindMap.put("REFERENCEPOVERSION"        ,       spcReferencepoversion			);		
			bindMap.put("REFERENCEMACHINEFLAG"      ,       spcReferenceMachineFlag    		);		
			bindMap.put("REFERENCEMACHINERECIPEFLAG",       spcReferenceMachineRecipeFlag	);		
			bindMap.put("EVENTUSER"                 ,       eventUser	                    );
			
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
			log.info("SPCPolicyCopy Insert MES_SPCControlSpec Success");
			
		} catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpec Fail");
		}
		
		try {

			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECHISTORY( 	" +
							  "SPCCONTROLSPECNAME,        			" +
							  "TIMEKEY,        						" +							  
							  "EVENTTIME,        					" +							  
							  "EVENTNAME,        					" +							  
			                  "DCSPECNAME,           				" +
			                  "DCSPECVERSION,              			" +
			                  "CONTROLGROUPTYPE,           			" +
			                  "TOTALCONTROLCOUNT,           		" +
			                  "FACTORYNAME,           				" +
			                  "PRODUCTSPECNAME,           			" +
			                  "PRODUCTSPECVERSION,     				" +
			                  "PROCESSFLOWNAME,  					" +
			                  "PROCESSFLOWVERSION,       			" +
			                  "PROCESSOPERATIONNAME,        		" +
			                  "PROCESSOPERATIONVERSION,     		" +
			                  "MACHINENAME,          				" +
			                  "MACHINERECIPENAME,          		 	" +
			                  "REFERENCEPONAME,            			" +
			                  "REFERENCEPOVERSION,          		" +
			                  "REFERENCEMACHINEFLAG,       			" +
			                  "REFERENCEMACHINERECIPEFLAG,  		" +
			                  "EVENTUSER  		                	" +
						" ) " +
						" VALUES(" +
						    ":SPCCONTROLSPECNAME,           		" +
						    ":TIMEKEY,           					" +						    
						    "sysdate,           					" +						    
						    ":EVENTNAME,           					" +						    
							":DCSPECNAME,           				" +
							":DCSPECVERSION,              			" +
							":CONTROLGROUPTYPE,          			" +
							":TOTALCONTROLCOUNT,            		" +
							":FACTORYNAME,           				" +
							":PRODUCTSPECNAME,           			" +
							":PRODUCTSPECVERSION,     				" +
							":PROCESSFLOWNAME,  					" +
							":PROCESSFLOWVERSION,       			" +
							":PROCESSOPERATIONNAME,         		" +
							":PROCESSOPERATIONVERSION,      		" +
							":MACHINENAME,             				" +
							":MACHINERECIPENAME,            		" +
							":REFERENCEPONAME,             			" +
							":REFERENCEPOVERSION,          			" +
							":REFERENCEMACHINEFLAG,         		" +
							":REFERENCEMACHINERECIPEFLAG,   		" +
							":EVENTUSER   			                " +
						")";					

			// 02. Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();

			bindMap.put("SPCCONTROLSPECNAME"      	,		newSpcControlSpecName			);
			bindMap.put("TIMEKEY"              		,   	timeKey  						);	
			bindMap.put("EVENTNAME"              	,   	"Copy"            				);	
			bindMap.put("DCSPECNAME"              	,   	spcDcSpecName            		);		
			bindMap.put("DCSPECVERSION"           	,   	"00001"                    		);   
			bindMap.put("CONTROLGROUPTYPE"        	,       spcControlGroupType             );		
			bindMap.put("TOTALCONTROLCOUNT"       	,       spcTotalControlCount            );   
			bindMap.put("FACTORYNAME"            	,       spcFactoryName                 	);		
			bindMap.put("PRODUCTSPECNAME"         	,       spcProductSpecName              );		
			bindMap.put("PRODUCTSPECVERSION"      	,       spcProductSpecVersion           );   
			bindMap.put("PROCESSFLOWNAME"			,       spcProcessFlowName        		);		
			bindMap.put("PROCESSFLOWVERSION"        ,       spcProcessFlowVersion           );   
			bindMap.put("PROCESSOPERATIONNAME"      ,       spcProcessOperationName   		);   
			bindMap.put("PROCESSOPERATIONVERSION"   ,       spcProcessOperationVersion      );		
			bindMap.put("MACHINENAME"               ,       spcMachineName    				);		
			bindMap.put("MACHINERECIPENAME"         ,       spcMachineRecipeName			);			
			bindMap.put("REFERENCEPONAME"           ,       spcReferenceponame    			);		
			bindMap.put("REFERENCEPOVERSION"        ,       spcReferencepoversion			);		
			bindMap.put("REFERENCEMACHINEFLAG"      ,       spcReferenceMachineFlag    		);		
			bindMap.put("REFERENCEMACHINERECIPEFLAG",       spcReferenceMachineRecipeFlag	);		
			bindMap.put("EVENTUSER"                 ,       eventUser	                    );
			
			GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecHistory Success");
			
		} catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecHistory Fail");
		}
	}
	
	public static void insertSPCControlSpecItem(List<Map<String, Object>> spcControlSpecItemList, String newSpcControlSpecName,String eventUser) 
	{
		String timeKey = ConvertUtil.getCurrTimeKey();
		String eventName= "Copy";
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECITEM(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CONTROLCHARTTYPE,					" +
							"DATATYPE,							" +
							"DERIVEDTYPE,						" +
							"DERIVEDEXPRESSION,					" +
							"CLCREATIONCYCLE,					" +
							"CLAUTOUPDATEFLAG,					" +
							"LASTCLCREATEDTIME,					" +
							"SPECLIMITTYPE,						" +
							"TARGET,							" +
							"UPPERSPECLIMIT,					" +
							"LOWERSPECLIMIT,					" +
							"UPPERSCREENLIMIT,					" +
							"LOWERSCREENLIMIT,					" +
							"SCREENLIMITREMOVEOPTION,			" +
							"OOCREMOVEOPTION,                   " +
							"EVENTUSER) 					    " +
							"VALUES( ?, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, ? ,?)";
			
			for(Map<String, Object> row : spcControlSpecItemList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String controlChartType = CommonUtil.getValue(row, "CONTROLCHARTTYPE");
				String dataType = CommonUtil.getValue(row, "DATATYPE");
				String derivedType = CommonUtil.getValue(row, "DERIVEDTYPE");
				String derivedExpression = CommonUtil.getValue(row, "DERIVEDEXPRESSION");
				String clcreaTionCycle = CommonUtil.getValue(row, "CLCREATIONCYCLE");
				String clautoupDateFlag = CommonUtil.getValue(row, "CLAUTOUPDATEFLAG");
				String specLimitType = CommonUtil.getValue(row, "SPECLIMITTYPE");
				String starget = CommonUtil.getValue(row, "TARGET");
				String supperSpecLimit = CommonUtil.getValue(row, "UPPERSPECLIMIT");
				String slowerSpecLimit = CommonUtil.getValue(row, "LOWERSPECLIMIT");
				String supperScreenLimit = CommonUtil.getValue(row, "UPPERSCREENLIMIT");
				String slowerScreenLimit = CommonUtil.getValue(row, "LOWERSCREENLIMIT");
				String screenLimitRemoveOption = CommonUtil.getValue(row, "SCREENLIMITREMOVEOPTION");
				String oocRemoveOption = CommonUtil.getValue(row, "OOCREMOVEOPTION");
				
				Double target = Double.parseDouble(starget);
				Double upperSpecLimit = Double.parseDouble(supperSpecLimit);
				Double lowerSpecLimit = Double.parseDouble(slowerSpecLimit);
				Double upperScreenLimit = Double.parseDouble(supperScreenLimit);
				Double lowerScreenLimit = Double.parseDouble(slowerScreenLimit);
				
				bind = new Object[] { newSpcControlSpecName		,
										itemName				,
										controlChartType		,
										dataType				,
										derivedType				,
										derivedExpression		,
										clcreaTionCycle			,
										clautoupDateFlag		,
										specLimitType			,
										target					,
										upperSpecLimit			,
										lowerSpecLimit			,
										upperScreenLimit		,
										lowerScreenLimit		,
										screenLimitRemoveOption	,
										oocRemoveOption			,
										eventUser               };
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECITEM");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecItem Fail");
		}
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECITEMHISTORY(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"TIMEKEY,							" +
							"EVENTTIME,							" +
							"EVENTNAME,							" +
							"CONTROLCHARTTYPE,					" +
							"DATATYPE,							" +
							"DERIVEDTYPE,						" +
							"DERIVEDEXPRESSION,					" +
							"CLCREATIONCYCLE,					" +
							"CLAUTOUPDATEFLAG,					" +
							"LASTCLCREATEDTIME,					" +
							"SPECLIMITTYPE,						" +
							"TARGET,							" +
							"UPPERSPECLIMIT,					" +
							"LOWERSPECLIMIT,					" +
							"UPPERSCREENLIMIT,					" +
							"LOWERSCREENLIMIT,					" +
							"SCREENLIMITREMOVEOPTION,			" +
							"OOCREMOVEOPTION,                   " +
							"EVENTUSER) 					    " +
							"VALUES( ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, ?,? )";
			
			for(Map<String, Object> row : spcControlSpecItemList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String controlChartType = CommonUtil.getValue(row, "CONTROLCHARTTYPE");
				String dataType = CommonUtil.getValue(row, "DATATYPE");
				String derivedType = CommonUtil.getValue(row, "DERIVEDTYPE");
				String derivedExpression = CommonUtil.getValue(row, "DERIVEDEXPRESSION");
				String clcreaTionCycle = CommonUtil.getValue(row, "CLCREATIONCYCLE");
				String clautoupDateFlag = CommonUtil.getValue(row, "CLAUTOUPDATEFLAG");
				String specLimitType = CommonUtil.getValue(row, "SPECLIMITTYPE");
				String starget = CommonUtil.getValue(row, "TARGET");
				String supperSpecLimit = CommonUtil.getValue(row, "UPPERSPECLIMIT");
				String slowerSpecLimit = CommonUtil.getValue(row, "LOWERSPECLIMIT");
				String supperScreenLimit = CommonUtil.getValue(row, "UPPERSCREENLIMIT");
				String slowerScreenLimit = CommonUtil.getValue(row, "LOWERSCREENLIMIT");
				String screenLimitRemoveOption = CommonUtil.getValue(row, "SCREENLIMITREMOVEOPTION");
				String oocRemoveOption = CommonUtil.getValue(row, "OOCREMOVEOPTION");
								
				Double target = Double.parseDouble(starget);
				Double upperSpecLimit = Double.parseDouble(supperSpecLimit);
				Double lowerSpecLimit = Double.parseDouble(slowerSpecLimit);
				Double upperScreenLimit = Double.parseDouble(supperScreenLimit);
				Double lowerScreenLimit = Double.parseDouble(slowerScreenLimit);
						
				bind = new Object[] { newSpcControlSpecName		,
										itemName				,
										timeKey					,	
										eventName				,
										controlChartType		,
										dataType				,
										derivedType				,
										derivedExpression		,
										clcreaTionCycle			,
										clautoupDateFlag		,
										specLimitType			,
										target					,
										upperSpecLimit			,
										lowerSpecLimit			,
										upperScreenLimit		,
										lowerScreenLimit		,
										screenLimitRemoveOption	,
										oocRemoveOption			,
										eventUser};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECITEMHISTORY");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecItemHistory Fail");
		}
	}
	
	public static void insertSPCControlSpecChart(List<Map<String, Object>> spcControlSpecChartList, String newSpcControlSpecName,String eventUser) 
	{
		String timeKey = ConvertUtil.getCurrTimeKey();
		String eventName= "Copy";
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECCHART(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"DESCRIPTION,						" +
							"CENTERLINE,						" +
							"UPPERCONTROLLIMIT,					" +
							"LOWERCONTROLLIMIT,					" +
							"UPPERQUALITYLIMIT,					" +
							"LOWERQUALITYLIMIT,					" +
							"UPDATETIME,                        " +
							"EVENTUSER) 						" +
							"VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate ,?)";
			
			for(Map<String, Object> row : spcControlSpecChartList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String description = CommonUtil.getValue(row, "DESCRIPTION");
				String sCenterline = CommonUtil.getValue(row, "CENTERLINE");
				String supperControlLimit = CommonUtil.getValue(row, "UPPERCONTROLLIMIT");
				String slowerControlLimit = CommonUtil.getValue(row, "LOWERCONTROLLIMIT");
				String supperQualityLimit = CommonUtil.getValue(row, "UPPERQUALITYLIMIT");
				String slowerQualityLimit = CommonUtil.getValue(row, "LOWERQUALITYLIMIT");
				
				Double centerline = Double.parseDouble(sCenterline);
				Double upperControlLimit = Double.parseDouble(supperControlLimit);
				Double lowerControlLimit = Double.parseDouble(slowerControlLimit);
				Double upperQualityLimit = Double.parseDouble(supperQualityLimit);
				Double lowerQualityLimit = Double.parseDouble(slowerQualityLimit);
				
				bind = new Object[] { newSpcControlSpecName		,
										itemName				,
										chartName				,
										description				,
										centerline				,
										upperControlLimit		,
										lowerControlLimit		,
										upperQualityLimit		,
										lowerQualityLimit		,
										eventUser               };
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECCHART");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecChart Fail");
		}
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECCHARTHISTORY(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"TIMEKEY,							" +							
							"EVENTTIME,							" +							
							"EVENTNAME,							" +							
							"DESCRIPTION,						" +
							"CENTERLINE,						" +
							"UPPERCONTROLLIMIT,					" +
							"LOWERCONTROLLIMIT,					" +
							"UPPERQUALITYLIMIT,					" +
							"LOWERQUALITYLIMIT,                 " +
							"EVENTUSER) 				        " +
							"VALUES( ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?,?)";
			
			for(Map<String, Object> row : spcControlSpecChartList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String description = CommonUtil.getValue(row, "DESCRIPTION");
				String sCenterline = CommonUtil.getValue(row, "CENTERLINE");
				String supperControlLimit = CommonUtil.getValue(row, "UPPERCONTROLLIMIT");
				String slowerControlLimit = CommonUtil.getValue(row, "LOWERCONTROLLIMIT");
				String supperQualityLimit = CommonUtil.getValue(row, "UPPERQUALITYLIMIT");
				String slowerQualityLimit = CommonUtil.getValue(row, "LOWERQUALITYLIMIT");
				
				Double centerline = Double.parseDouble(sCenterline);
				Double upperControlLimit = Double.parseDouble(supperControlLimit);
				Double lowerControlLimit = Double.parseDouble(slowerControlLimit);
				Double upperQualityLimit = Double.parseDouble(supperQualityLimit);
				Double lowerQualityLimit = Double.parseDouble(slowerQualityLimit);
				
				bind = new Object[] { newSpcControlSpecName		,
										itemName				,
										chartName				,
										timeKey					,
										eventName				,
										description				,
										centerline				,
										upperControlLimit		,
										lowerControlLimit		,
										upperQualityLimit		,
										lowerQualityLimit		,
										eventUser};
				batchArgs.add(bind);		
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECCHARTHISTORY");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecChartHistory Fail");
		}
	}
	
	public static void insertSPCControlSpecRule(List<Map<String, Object>> spcControlSpecRuleList, String newSpcControlSpecName,String eventUser) 
	{
		String timeKey = ConvertUtil.getCurrTimeKey();
		String eventName= "Copy";
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECRULE(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"SPCCONTROLRULENAME,				" +
							"UPDATETIME,                        " +
							"EVENTUSER) 						" +
							"VALUES( ?, ?, ?, ?, sysdate,?)";
			
			for(Map<String, Object> row : spcControlSpecRuleList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String spcControlRuleName = CommonUtil.getValue(row, "SPCCONTROLRULENAME");
				
				bind = new Object[] { newSpcControlSpecName, itemName, chartName, spcControlRuleName,eventUser};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECRULE");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecRule Fail");
		}
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECRULEHISTORY(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"SPCCONTROLRULENAME,				" +
							"TIMEKEY,				            " +							
							"EVENTTIME,				            " +							
							"EVENTNAME,                         " +
							"EVENTUSER) 						" +
							"VALUES( ?, ?, ?, ?, ?, sysdate, ? ,?)";
			
			for(Map<String, Object> row : spcControlSpecRuleList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String spcControlRuleName = CommonUtil.getValue(row, "SPCCONTROLRULENAME");
				
				bind = new Object[] { newSpcControlSpecName, itemName, chartName, spcControlRuleName, timeKey, eventName,eventUser};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECRULEHISTORY");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecRuleHistory Fail");
		}
	}
	
	public static void insertSPCControlSpecRuleGroup(List<Map<String, Object>> spcControlSpecRuleGroupList, String newSpcControlSpecName,String eventUser) 
	{
		String timeKey = ConvertUtil.getCurrTimeKey();
		String eventName= "Copy";
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECRULEGROUP(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"SPCCONTROLRULEGROUPNAME,			" +
							"UPDATETIME,                        " +
							"EVENTUSER) 						" +
							"VALUES( ?, ?, ?, ?, sysdate,?)";
			
			for(Map<String, Object> row : spcControlSpecRuleGroupList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String spcControlRuleGroupName = CommonUtil.getValue(row, "SPCCONTROLRULEGROUPNAME");
				
				bind = new Object[] { newSpcControlSpecName, itemName, chartName, spcControlRuleGroupName,eventUser};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECRULE");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecRule Fail");
		}
		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCTRSPECRULEGROUPHIST(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CHARTNAME,							" +
							"SPCCONTROLRULEGROUPNAME,				" +
							"TIMEKEY,				" +							
							"EVENTTIME,				" +							
							"EVENTNAME,             " +
							"EVENTUSER) 						" +
							"VALUES( ?, ?, ?, ?, ?, sysdate, ?,?)";
			
			for(Map<String, Object> row : spcControlSpecRuleGroupList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String chartName = CommonUtil.getValue(row, "CHARTNAME");
				String spcControlRuleGroupName = CommonUtil.getValue(row, "SPCCONTROLRULEGROUPNAME");
				
				bind = new Object[] { newSpcControlSpecName, itemName, chartName, spcControlRuleGroupName, timeKey, eventName,eventUser};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECRULEHISTORY");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecRuleHistory Fail");
		}
	}
	
	public static void insertSPCControlSpecCapbility(List<Map<String, Object>> spcControlSpecCapbilityList, String newSpcControlSpecName) 
	{		
		try 
		{
			Object[] bind = null;
			List<Object[]> batchArgs = new ArrayList<Object[]>();
						
			// 01. Make a query.
			String sql = "INSERT INTO MES_SPCCONTROLSPECCAPABILITY(	" +
							"SPCCONTROLSPECNAME,				" + 
							"ITEMNAME,							" +
							"CREATIONCYCLE,							" +
							"LASTCREATEDTIME,				" +
							"UPDATETIME) 						" +
							"VALUES( ?, ?, ?, sysdate, sysdate)";
			
			for(Map<String, Object> row : spcControlSpecCapbilityList)
			{
				String itemName = CommonUtil.getValue(row, "ITEMNAME");
				String creationCycle = CommonUtil.getValue(row, "CREATIONCYCLE");
				
				bind = new Object[] { newSpcControlSpecName, itemName, creationCycle};
				batchArgs.add(bind);				
			}
			if (batchArgs.size() > 0) 
			{
				GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().batchUpdate(sql, batchArgs);
				log.info(String.valueOf(batchArgs.size()).toString() + " Rows Insert into MES_SPCCONTROLSPECCAPABILITY");
			}
		}
		catch (Exception e) 
		{
			log.info("SPCPolicyCopy Insert MES_SPCControlSpecCapability Fail");
		}
	}
	
	public static List<Map<String, Object>> getSPCTPOMPolicyInfo(String dcSpecName, Product productData)
	{		
		String sql=" SELECT DISTINCT(I.ITEMNAME) FROM MES_SPCCONTROLSPEC S,MES_SPCCONTROLSPECITEM I " +
		"WHERE S.PRODUCTSPECNAME=:PRODUCTSPECNAME AND S.SPCCONTROLSPECNAME=I.SPCCONTROLSPECNAME AND DCSPECNAME=:DCSPECNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DCSPECNAME", dcSpecName);
		bindMap.put("PRODUCTSPECNAME", productData.getProductSpecName());
		
		List<Map<String, Object>> result= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql,bindMap);
		
		if(result.size() == 0)
		{
			log.info("SPC TPOMPolicy Item not have config");
		}
		return result;
	}
	
	public static List<Map<String, Object>> getSPCTFOMPolicyInfo(String dcSpecName, Product productData)
	{		
		String sql=" SELECT DISTINCT(I.ITEMNAME) FROM MES_SPCCONTROLSPEC S,MES_SPCCONTROLSPECITEM I " +
		"WHERE S.PROCESSFLOWNAME=:PROCESSFLOWNAME AND S.SPCCONTROLSPECNAME=I.SPCCONTROLSPECNAME AND DCSPECNAME=:DCSPECNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DCSPECNAME", dcSpecName);
		bindMap.put("PROCESSFLOWNAME", productData.getProcessFlowName());
		
		List<Map<String, Object>> result= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql,bindMap);
		
		if(result.size() == 0)
		{
			log.info("SPC TFOMPolicy Item not have config");
		}
		
		return result;
	}
	
	public static List<Map<String, Object>> getSPCTFPOMPolicyInfo(String dcSpecName, Product productData)
	{		
		String sql=" SELECT DISTINCT(I.ITEMNAME) FROM MES_SPCCONTROLSPEC S,MES_SPCCONTROLSPECITEM I " +
		"WHERE S.PRODUCTSPECNAME=:PRODUCTSPECNAME AND S.PROCESSFLOWNAME=:PROCESSFLOWNAME " +
		"AND S.SPCCONTROLSPECNAME=I.SPCCONTROLSPECNAME AND DCSPECNAME=:DCSPECNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DCSPECNAME", dcSpecName);
		bindMap.put("PRODUCTSPECNAME", productData.getProductSpecName());
		bindMap.put("PROCESSFLOWNAME", productData.getProcessFlowName());
		
		List<Map<String, Object>> result= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql,bindMap);
		
		if(result.size() == 0)
		{
			log.info("SPC TFPOMPolicy Item not have config");
		}
		
		return result;
	}
}
