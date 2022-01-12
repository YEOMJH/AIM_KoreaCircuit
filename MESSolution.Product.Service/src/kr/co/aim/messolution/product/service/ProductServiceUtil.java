/**
 * Histories:
 * 

 */     


package kr.co.aim.messolution.product.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class ProductServiceUtil implements ApplicationContextAware 
{
	private static Log log = LogFactory.getLog(ProductServiceUtil.class);

	

	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub

	}
	
	public List<Product> getRunbanProductList(List<String> productNameList) throws CustomException
	{
		if (productNameList == null || productNameList.size() == 0)
		{
			log.info("The incoming variable value is null or empty!!");
			return new ArrayList<Product>();
		}
		
		Boolean abortFlag=false;		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		String sql="";

		try
		{
			String checkAbort =" SELECT PRODUCTNAME FROM PRODUCT WHERE PRODUCTNAME IN "+
		                        " (:PRODUCTNAMELIST) AND PROCESSINGINFO='B' AND PRODUCTSTATE=:PRODUCTSTATE ";
			Map<String,Object> bindMap1 = new HashMap<>();
			bindMap1.put("PRODUCTNAMELIST", productNameList);
			bindMap1.put("PRODUCTSTATE",  GenericServiceProxy.getConstantMap().Prod_InProduction);
			List<Map<String, Object>> resultList=greenFrameServiceProxy.getSqlTemplate().queryForList(checkAbort, bindMap1);
			if(resultList!=null && resultList.size()>0)
			{
				abortFlag = true;
			}
			else
			{
				abortFlag = false;
			}

		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		if(abortFlag)
		{
			sql = " SELECT * FROM PRODUCT "
					   + " WHERE 1=1 AND PRODUCTNAME IN (:PRODUCTLIST)  "
					   + " AND PRODUCTSTATE != :SCRAPSTATE  AND PRODUCTSTATE != :CONSUMSTATE "
					   + " AND PROCESSINGINFO = 'B' "
					   + " ORDER BY POSITION";
		}
		else
		{
			sql = " SELECT * FROM PRODUCT "
					   + " WHERE 1=1 AND PRODUCTNAME IN (:PRODUCTLIST)  "
					   + " AND PRODUCTSTATE != :SCRAPSTATE  AND PRODUCTSTATE != :CONSUMSTATE "
					   + " ORDER BY POSITION";
		}

		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTLIST", productNameList);
		bindMap.put("SCRAPSTATE", constMap.Prod_Scrapped);
		bindMap.put("CONSUMSTATE", constMap.Prod_Consumed);

		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)return new ArrayList<Product>();

		return ProductServiceProxy.getProductService().transform(resultList);
	}
	
	public List<Product> getRunbanProductList(String lotName) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info("lotName = " + lotName);
		
		Boolean abortFlag=false;
		String condition="";
		Object bindSet[] = new Object[3];
		
		try
		{
				ProductServiceProxy.getProductService().select(
						"lotName = ? AND processingInfo = ? AND productState = ?", new Object[] { lotName, "B", GenericServiceProxy.getConstantMap().Prod_InProduction });

			abortFlag = true;
		}
		catch (NotFoundSignal ne)
		{
			log.info("retry after TK In Canceled");

			abortFlag = false;
		}
		if(abortFlag)
		{
			condition = " WHERE LOTNAME = ?  AND PRODUCTSTATE != ?  AND PRODUCTSTATE != ?  AND PROCESSINGINFO = 'B' ORDER BY POSITION";

		}
		else
		{
			condition = " WHERE LOTNAME = ?  AND PRODUCTSTATE != ?  AND PRODUCTSTATE != ? ORDER BY POSITION";
		}

		bindSet[0] = lotName;
		bindSet[1] = GenericServiceProxy.getConstantMap().Prod_Scrapped;
		bindSet[2] = GenericServiceProxy.getConstantMap().Prod_Consumed;

		List<Product> productDataList = null;

		try
		{
			productDataList = ProductServiceProxy.getProductService().select(condition, bindSet);
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info("Not found Runban product list.search by lotname = " + lotName);
			return new ArrayList<Product>();
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		return productDataList;
	}
	
	public List<Product> getProductDataListByNameList(List<String> productList) throws CustomException
	{
		String sql = " SELECT * FROM PRODUCT WHERE 1=1 AND PRODUCTNAME IN ( :PRODUCTLIST ) ";
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTLIST", productList);
		
		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList  = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			log.info(ex.toString());
			throw new CustomException(ex);
		}

		if (resultList == null || resultList.size() == 0) return null;
		List<Product> productDataList = ProductServiceProxy.getProductService().transform(resultList);

		return productDataList;
	}
	
	public Product updateFlagStack(EventInfo eventInfo,Product productData,String machineName,String unitName,String subUnitName,String processFlowType) throws CustomException
	{
		Object [] bindSet = new Object[7];
		
		bindSet[0] = productData.getFactoryName();
		bindSet[1] = processFlowType;
		bindSet[2] = productData.getProcessOperationName();
		bindSet[3] = productData.getProcessOperationVersion();
		bindSet[4] = machineName;
		bindSet[5] = unitName;
		bindSet[6] = subUnitName;

		RunBanRule dataInfo = null;
		try
		{
			dataInfo = ExtendedObjectProxy.getRunBanRuleService().selectByKey(false, bindSet);
		}
		catch (greenFrameDBErrorSignal errorSignal)
		{
			if (!errorSignal.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				log.info("Operation:"+productData.getProcessOperationName()+"not set up RunBanRule");
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if (dataInfo == null)return productData;
		String[] addFlagList=StringUtil.split(dataInfo.getAddFlag(), ",");
		String[] deleteFlagList=StringUtil.split(dataInfo.getDeleteFlag(), ",");
		
		List<String> flagStackList = new ArrayList<String>();
		CollectionUtils.addAll(flagStackList, org.springframework.util.StringUtils.commaDelimitedListToStringArray(productData.getUdfs().get("FLAGSTACK")));

		SetEventInfo setEventInfo = new SetEventInfo();
   
		for(int i=0;i<deleteFlagList.length;i++)
		{
			if (flagStackList.contains(deleteFlagList[i]))
			{
				flagStackList.remove(deleteFlagList[i]);
				setEventInfo.getUdfs().put("FLAGSTACK", org.springframework.util.StringUtils.collectionToCommaDelimitedString(flagStackList));
			}
		}
         
		for(int i=0;i<addFlagList.length;i++)
		{
			if (!flagStackList.contains(addFlagList[i]))
			{
				flagStackList.add(addFlagList[i]);
				setEventInfo.getUdfs().put("FLAGSTACK", org.springframework.util.StringUtils.collectionToCommaDelimitedString(flagStackList));
			}

		}
		
		productData = ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
		
		return productData;
	}
	
	public List<Product> getAllAvailableProductList(List<String> productNameList) throws CustomException
	{
		if (productNameList == null || productNameList.size() == 0)
		{
			log.info("The incoming variable value is null or empty!!");
			return new ArrayList<Product>();
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String sql = " SELECT * FROM PRODUCT "
				   + " WHERE 1=1 AND PRODUCTNAME IN (:PRODUCTLIST)  "
				   + " AND PRODUCTSTATE != :SCRAPSTATE  AND PRODUCTSTATE != :CONSUMSTATE "
				   + " AND (PROCESSINGINFO IS NULL OR ( PROCESSINGINFO != 'B' AND PROCESSINGINFO != 'S' )) "
				   + " ORDER BY POSITION";

		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTLIST", productNameList);
		bindMap.put("SCRAPSTATE", constMap.Prod_Scrapped);
		bindMap.put("CONSUMSTATE", constMap.Prod_Consumed);

		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)return new ArrayList<Product>();

		return ProductServiceProxy.getProductService().transform(resultList);
	}
	
	public List<Product> getAllAvailableProductList(String lotName) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info("lotName = " + lotName);
		
		String condition = " WHERE LOTNAME = ?  AND PRODUCTSTATE != ?  AND PRODUCTSTATE != ? AND (PROCESSINGINFO IS NULL OR  (PROCESSINGINFO != 'B' AND PROCESSINGINFO != 'S')) ORDER BY POSITION";

		Object bindSet[] = new Object[3];
		bindSet[0] = lotName;
		bindSet[1] = GenericServiceProxy.getConstantMap().Prod_Scrapped;
		bindSet[2] = GenericServiceProxy.getConstantMap().Prod_Consumed;

		List<Product> productDataList = null;

		try
		{
			productDataList = ProductServiceProxy.getProductService().select(condition, bindSet);
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info("Not found norlmal product list.search by lotname = " + lotName);
			return new ArrayList<Product>();
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		return productDataList;
	}

	public List<Product> getProductListByLotName(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(log.isInfoEnabled()){
			log.info("lotName = " + lotName);
		}
		
		List<Product> productList = ProductServiceProxy.getProductService().allProductsByLot(lotName);
		
		return productList;
	}

	public  Product getProductData(String productName) throws CustomException
	{
		try
		{
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productName);
			
			Product productData = ProductServiceProxy.getProductService().selectByKey(productKey);
	
			return productData;
		}
	    catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productName);	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	}
	
	public List<Product> allUnScrappedProductsByLot(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(log.isInfoEnabled()){
			log.info("lotName = " + lotName);
		}
		
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		
		return productList;
	}
	
	public ProductSpec getProductSpecByProductName ( Product productData ) throws CustomException {
	
		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(productData.getFactoryName());
		productSpecKey.setProductSpecName(productData.getProductSpecName());
		productSpecKey.setProductSpecVersion(productData.getProductSpecVersion());
		
		ProductSpec productSpecData = null;
		productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
		
		return productSpecData;
	}
	
	public List<ProductU> setProductUSequence(String productName)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		ProductU productU = new ProductU();
		List<ProductU> productUList = new ArrayList<ProductU>();
		productU.setProductName(productName);
		productUList.add(productU);
		 
		return productUList;
	} 
	 
	
	public List<ProductU> setProductUSequence(org.jdom.Document doc)
			throws FrameworkErrorSignal, NotFoundSignal {
		if (doc == null) {
			log.error("xml is null");
		}

		List<ProductU> productUList = new ArrayList<ProductU>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil
				.getBundleServiceClass(ProductServiceUtil.class);

		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");
		if (lotName == null || lotName == "") {
			String carrierName = root.getChild("Body").getChildText(
					"CARRIERNAME");
			if (carrierName != null && carrierName != "") {

				String condition = "WHERE carrierName = ?"
						+ "AND productState != ?" + "AND productState != ?"
						+ "ORDER BY position ";

				Object[] bindSet = new Object[] { carrierName,
						GenericServiceProxy.getConstantMap().Prod_Scrapped,
						GenericServiceProxy.getConstantMap().Prod_Consumed };
				try {
					productDatas = ProductServiceProxy.getProductService()
							.select(condition, bindSet);
				} catch (Exception e) {

				}
			}
		} else {
			try {
				productDatas = ProductServiceProxy.getProductService()
						.allUnScrappedProductsByLot(lotName);
			} catch (Exception e) {
				log.error(e);
				productDatas = ProductServiceProxy.getProductService()
						.allProductsByLot(lotName);
			}

		}

		Element element = root.getChild("Body").getChild("PRODUCTLIST");

		if (element != null) {
			for (Iterator iterator = element.getChildren().iterator(); iterator
					.hasNext();) {
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();

				ProductU productU = new ProductU();

				productU.setProductName(productName);
				productU.setUdfs(productServiceUtil.setNamedValueSequence(
						productName, productE));

				productUList.add(productU);
			}
		} else {

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct
					.hasNext();) {
				Product product = iteratorProduct.next();

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(product.getUdfs());

				productUList.add(productU);
			}
		}

		return productUList;
	} 
	
	
	public  Map<String, String> setNamedValueSequence(String productName, Element element) throws FrameworkErrorSignal, NotFoundSignal
	{ 
		if(log.isInfoEnabled()){
			log.info("productName = " + productName);
		}
		
		Map<String, String> namedValueMap = new HashMap<String, String>();
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		
		try{
			Product product = ProductServiceProxy.getProductService().selectByKey(productKey);
			namedValueMap = product.getUdfs();
		}catch(NotFoundSignal ne){}
		
		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Product", "ExtendedC");

		if ( objectAttributeDefs != null )
		{
			for ( int i = 0; i < objectAttributeDefs.size(); i++ )
			{				
				String name = "";
				String value = "";
				
				if ( element != null )
				{
					for ( int j = 0; j < element.getContentSize(); j++ )
					{
						if ( element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null )
						{
							name  = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

							if (StringUtil.isNotEmpty(name) && StringUtil.isNotEmpty(value))
								namedValueMap.put(name, value);
							
							break;
						}
						else
						{
							break;
						}
					}
				}
			}
		}

		return namedValueMap;
	}
	
	public List<String> generateGlassName(String productName, int subProductUnitQty)
	{
		List<String> panelNames = new ArrayList<String>(subProductUnitQty);

		for (int i = 0; i < subProductUnitQty; i++)
		{
			panelNames.add(new StringBuilder(productName).append(String.valueOf(i + 1)).toString());
		}

		return panelNames;
	}
	
	public List<Product> allScrappedProductsByLot(String lotName) throws CustomException
	{
		List<Product> listProduct  = null;
		String condition = "WHERE LOTNAME = ? AND PRODUCTSTATE = 'Scrapped' ORDER BY POSITION";
		
		Object[] bindSet = new Object[] {lotName};
	
		try
		{
			listProduct = ProductServiceProxy.getProductService().select(condition, bindSet);
			 
			return listProduct;
		}
		catch (greenFrameDBErrorSignal de )
		{
			if (de.getErrorCode().equals("NotFoundSignal")){
				throw new NotFoundSignal(de.getDataKey(), de.getSql());}
			else{
				throw new CustomException("SYS-8001", de.getSql());}
		}	 
	}
	
	public List<Map<String, Object>>  getEnumList(String EnumName,String ENUMVALUE) throws CustomException
	{
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE = :ENUMVALUE ";
		
		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("ENUMNAME", EnumName);
		bindMap.put("ENUMVALUE", ENUMVALUE);
		
		List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		return sqlResult;
	}
	
	public void updateProductData(String fieldName, String fieldValue, String productName) throws CustomException
	{

		String sql = "UPDATE PRODUCT SET " 
					+ fieldName + " = :fieldValue WHERE PRODUCTNAME = :productName ";
			
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("fieldValue"	  , fieldValue);
		bindMap.put("productName"	  , productName);			
		try
		{
			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}		
	}

	public String getSlotMap(Durable durableData)
			throws CustomException
	{
		StringBuffer normalSlotInfoBuffer = new StringBuffer();
			
		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity(); 
		
		// Get Product's Slot , These are not Scrapped Product.
		List<Product> productList = new ArrayList<Product>();
		
		try
		{
			productList = ProductServiceProxy.getProductService().select("carrierName = ? AND productState = ?",
							new Object[] {durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_InProduction});
		}
		catch (Exception ex)
		{
			log.debug("CST Product List is Empty : " + durableData.getKey().getDurableName());
		}
		
		// Make Durable Normal SlotMapInfo
		for(int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		
		log.debug("Default Slot Map : " + normalSlotInfoBuffer);
	
		for(int i = 0; i < productList.size(); i++)
		{
			try
			{
				int index = (int)productList.get(i).getPosition() - 1;
				
				normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
			catch (Exception ex)
			{
				log.error("Position conversion failed");
				normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
		}
		
		log.info("Current Slot Map : " + normalSlotInfoBuffer);
		
		return normalSlotInfoBuffer.toString();
	}

	public void increateDummyUsedCount(EventInfo eventInfo, List<Product> productDataList, String position)
	{
		for (Product productData : productDataList)
		{
			if (productData.getPosition() == Long.parseLong(position))
			{
				String dummyUsedCount = productData.getUdfs().get("DUMMYUSEDCOUNT");
				
				if(StringUtils.isEmpty(dummyUsedCount))
					dummyUsedCount = "0";
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("DUMMYUSEDCOUNT", String.valueOf(Long.parseLong(dummyUsedCount) + 1));
				
				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
				
				break;
			}
		}
	}
	
	public void checkReworkCountLimitForLotEnd(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = null;
		
		try
		{
			productList = ProductServiceProxy.getProductService().select("  LOTNAME = ? AND PRODUCTGRADE = ? " , new Object[] { lotData.getKey().getLotName(), "R"});
		}
		catch (Exception ex)
		{
			log.info("Product table has no data..");
		}
		
		for (Product productData : productList)
		{
			if( StringUtil.equals(productData.getProductGrade(), "R"))
			{
				try
				{
					List<ReworkProduct> reworkProductList = ExtendedObjectProxy.getReworkProductService().select(
							"  PRODUCTNAME = ? " + "AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? ",
							new Object[] { productData.getKey().getProductName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), });
	
					if (reworkProductList.size() > 0)
					{
						// check reworCountLimit
						if (reworkProductList.get(0).getReworkCount() >= Integer.parseInt(reworkProductList.get(0).getReworkCountLimit()))
						{
							//judge
							eventInfo.setEventName("ChangeGrade");
							ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), "N" , productData.getProductProcessState(),
																productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());
							
							productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);

						}
					}
					else
					{
						throw new CustomException("PRODUCT-9100", productData.getKey().getProductName());
					}
	
				}
				catch (greenFrameDBErrorSignal ex)
				{
				}
			}
		}
	}
	
	public void changeProductGrade(EventInfo eventInfo, String productName,String productGrade,
			String subProductGrades1,String subProductGrade2,String detailGrade) throws CustomException
	{
		String sql = "UPDATE PRODUCT SET PRODUCTGRADE=:PRODUCTGRADE,SUBPRODUCTGRADES1=:SUBPRODUCTGRADES1, "
				+ " SUBPRODUCTGRADES2=:SUBPRODUCTGRADES2,DETAILGRADE=:DETAILGRADE WHERE PRODUCTNAME = :PRODUCTNAME ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTNAME", productName);
		bindMap.put("PRODUCTGRADE", productGrade);
		bindMap.put("SUBPRODUCTGRADES1", subProductGrades1);
		bindMap.put("SUBPRODUCTGRADES2", subProductGrade2);
		bindMap.put("DETAILGRADE", detailGrade);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		Product newProductData=MESProductServiceProxy.getProductServiceUtil().getProductData(productName);	
		SetEventInfo setEventInfo = new SetEventInfo();
		MESProductServiceProxy.getProductServiceImpl().setEvent(newProductData, setEventInfo, eventInfo);				
	}
	
}
