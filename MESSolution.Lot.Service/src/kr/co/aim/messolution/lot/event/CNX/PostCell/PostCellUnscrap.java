package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class PostCellUnscrap extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String messageName = SMessageUtil.getMessageName(doc);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody!=null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sFactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
				String sLotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", true);
				double productQuantity = 1.0;
				
				//call function to change lotState
				this.changeLotState(sLotName);
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				
				ProductServiceProxy.getProductService().update(productData);
				eventInfo.setReasonCode("");
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				LotKey lotKey = lotData.getKey();
				
				List<ProductU> productUSequence = new ArrayList<ProductU>();
				ProductU productU = new ProductU();
				productU.setProductName(sProductName);
				productUSequence.add(productU);
				
				MakeUnScrappedInfo makeUnScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeUnScrappedInfo(lotData, lotData.getLotProcessState(), productQuantity, productUSequence);
					
			    //LotServiceProxy.getLotService().makeUnScrapped(lotKey, eventInfo, makeUnScrappedInfo);
			    MESLotServiceProxy.getLotServiceImpl().makeUnScrapped(eventInfo, lotData, makeUnScrappedInfo);
			    
			    //restore ProductGrade
			    Product productInfo = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

				if(StringUtil.isNotEmpty(productInfo.getUdfs().get("FINALPANELJUDGE")))
				{
					productInfo.setProductGrade(productInfo.getUdfs().get("FINALPANELJUDGE"));
					ProductServiceProxy.getProductService().update(productInfo);
					
					Map<String,Object> bindMap = new HashMap<String,Object>();
					bindMap.put("productGrade",productInfo.getUdfs().get("FINALPANELJUDGE"));
					bindMap.put("productName", sProductName);
							
					String sql = " UPDATE PRODUCTHISTORY SET PRODUCTGRADE = :productGrade " +
						" WHERE 1=1 " +
						" AND TIMEKEY = (SELECT LASTEVENTTIMEKEY FROM PRODUCT WHERE PRODUCTNAME = :productName )" +
						" AND PRODUCTNAME = :productName";
					try
					{
						GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);	
					}
					catch(Exception e)
					{
						throw new CustomException("SYS-8001", e.getMessage());
					}
				}
			
			}
		}
		
		return doc;
	}
	
	private void changeLotState(String lotName) throws CustomException
	{
	
		//Update LotState 'Emptied' to 'Scrapped'
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		if(lotData.getLotState().equals("Emptied"))
		{
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			
			List<Product> productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());
			
			 for ( Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext(); )
			 {
				Product product = iteratorProduct.next();
				
				if(product.getProductState().equals("Scrapped"))
				{
					lotData.setLotState("Scrapped");
					LotServiceProxy.getLotService().update(lotData);
					
					LotHistoryKey lotHistKey = new LotHistoryKey();
					lotHistKey.setLotName(lotData.getKey().getLotName());
					
					LotHistory lotHistoryData = LotServiceProxy.getLotHistoryService().selectLastOne(lotHistKey) ;
					lotHistoryData.setLotState("Scrapped");
					LotServiceProxy.getLotHistoryService().update(lotHistoryData);
					break;
				}
			 }
		}
	
	}

}
