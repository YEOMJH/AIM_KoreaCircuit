package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

public class PP003Import extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		List<Element> scrapList = SMessageUtil.getBodySequenceItemList(doc, "SCRAPLIST", false);
		List<Element> shipList = SMessageUtil.getBodySequenceItemList(doc, "SHIPLIST", false);
		
		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK  "
				+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE,ATTRIBUTIONDATE) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

		//ProductSummary
        List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (Element Product : productList)
		{

	        try 
	        {
				Thread.sleep(10);
			} 
	        catch (InterruptedException e) 
	        {
				e.printStackTrace();
			}
	        String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = SMessageUtil.getChildText(Product, "PRODUCTREQUESTNAME", true);
			String processOperationName = SMessageUtil.getChildText(Product, "PROCESSOPERATIONNAME", true);
			String productType = SMessageUtil.getChildText(Product, "PRODUCTTYPE", true);
			String attributionDate = SMessageUtil.getChildText(Product, "T_TIME", true);
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int productQuantity = Integer.valueOf(SMessageUtil.getChildText(Product, "QUANTITY", true)); 
			int scrapQuantity = 0;
			int reworkQuantity = 0;
			if(productQuantity==0) continue;

			if(StringUtils.equals(processOperationName, "32001")) processOperationName="32000";
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage,attributionDate };
			bindObjList.add(bindObj);
		}
		if(bindObjList.size()>0)
		{
			GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		}
		
		// ScrapSummary
		List<Object[]> bindObjList2 = new ArrayList<Object[]>();
		for (Element scrapInfo : scrapList) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = SMessageUtil.getChildText(scrapInfo, "PRODUCTREQUESTNAME", true);
			String processOperationName = SMessageUtil.getChildText(scrapInfo, "PROCESSOPERATIONNAME", true);
			String productType = SMessageUtil.getChildText(scrapInfo, "PRODUCTTYPE", true);
			String factoryName = SMessageUtil.getChildText(scrapInfo, "FACTORYNAME", true);
			String productSpecType = SMessageUtil.getChildText(scrapInfo, "PRODUCTSPECTYPE", false);
			String attributionDate = SMessageUtil.getChildText(scrapInfo, "T_TIME", true);
			String eventTime = TimeStampUtil.getCurrentTime();
			int subProductUnitQuantity = Integer.parseInt(SMessageUtil.getChildText(scrapInfo, "SUBPRODUCTUNITQUANTITY1", true));

			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if (hour >= 19) {
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime = receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";

			int scrapQty = Integer.valueOf(SMessageUtil.getChildText(scrapInfo, "SCRAPQUANTITY", true));
			int unScrapQty = Integer.valueOf(SMessageUtil.getChildText(scrapInfo, "CUTSCRAPQUANTITY", true));
			if (scrapQty == 0 && unScrapQty == 0)
				continue;

			int productQuantity = 0;
			int scrapQuantity = 0;
			int reworkQuantity = 0;

			if (StringUtils.equals(processOperationName, "3100S")) {
				productQuantity = 0;
				scrapQuantity = unScrapQty;
				processOperationName = "31000";
			} else {
				productQuantity = -scrapQty;
				scrapQuantity = scrapQty;
			}

			if (StringUtils.equals(factoryName, "OLED") && StringUtils.startsWith(processOperationName, "1")) {
				processOperationName = "21100";
				productQuantity = 0;
				scrapQuantity = scrapQuantity * 2;
			} else if (StringUtils.equals(factoryName, "OLED") && StringUtils.startsWith(processOperationName, "4")) {
				if (StringUtils.equals(productSpecType, "F")) {
					processOperationName = "212S0";
				} else {
					processOperationName = "221S0";
				}
				productQuantity = 0;

			} else if (StringUtils.equals(factoryName, "TP") && StringUtils.startsWith(processOperationName, "2")) {
				processOperationName = "4AAAA";
				productQuantity = 0;
			} else if (StringUtils.equals(factoryName, "POSTCELL")
					&& StringUtils.startsWith(processOperationName, "2")) {
				processOperationName = "31000";
				productQuantity = 0;
				scrapQuantity = scrapQuantity * (subProductUnitQuantity / 2);
			}

			if (StringUtils.equals(processOperationName, "32001"))
				processOperationName = "32000";

			if (productQuantity == 0 && scrapQuantity == 0 && reworkQuantity == 0)
				continue;

			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity,
					scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, result, resultMessage,attributionDate };
			bindObjList2.add(bindObj);
		}
		if (bindObjList2.size() > 0) {
			GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList2);
		}

		//Ship
		List<Object[]> bindObjList3 = new ArrayList<Object[]>();
		for (Element shipInfo : shipList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = SMessageUtil.getChildText(shipInfo, "PRODUCTREQUESTNAME", true);
			String processOperationName = SMessageUtil.getChildText(shipInfo, "PROCESSOPERATIONNAME", true);
			String productType = SMessageUtil.getChildText(shipInfo, "PRODUCTTYPE", true);
			String attributionDate = SMessageUtil.getChildText(shipInfo, "T_TIME", true);
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int outQty = Integer.valueOf(SMessageUtil.getChildText(shipInfo, "OUT_QTY", true));
			
			int productQuantity = outQty ;
			int scrapQuantity = 0;
			int reworkQuantity = 0;
		
			if(productQuantity==0&&scrapQuantity==0&&reworkQuantity==0)continue;
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage,attributionDate };
			bindObjList3.add(bindObj);
		}
		if (bindObjList3.size() > 0) {
			GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList3);
		}
	
		
		return doc;
	}

}
