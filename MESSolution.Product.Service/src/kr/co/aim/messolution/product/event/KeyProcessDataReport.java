package kr.co.aim.messolution.product.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.NEW;

import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentInspectHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.PhotoOffsetResult;
import kr.co.aim.messolution.extended.object.management.data.ReviewComponentHistory;
import kr.co.aim.messolution.extended.object.management.impl.ReviewComponentHistoryService;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.generic.util.XmlUtil;

public class KeyProcessDataReport extends AsyncHandler {
	
	private static Log log = LogFactory.getLog(KeyProcessDataReport.class);
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KeyProcessDataReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		//Check machineType : SE-SR
	    MachineSpec machineData=CommonUtil.getMachineSpecByMachineName(machineName);
	    if(!(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_SE)
	    		&&StringUtils.equals(machineData.getFactoryName(),"ARRAY")))
	    {
	    	return;
	    }
	    
	    List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_KEYPROCESSDATA(TIMEKEY,PRODUCTNAME,PARAMETERNAME,SITENAME,SITEVALUE,LOTNAME,CARRIERNAME,"
				+ "MACHINENAME,UNITNAME,SUBUNITNAME,PRODUCTSPECNAME,PROCESSFLOWNAME,PROCESSOPERATIONNAME,EVENTTIME) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		    
	    for(Element productInfo:productList)
	    {
	    	String productName=SMessageUtil.getChildText(productInfo, "PRODUCTNAME", true);
	    	Product productData=MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
	    	
	    	List<Element> parameterList = SMessageUtil.getSubSequenceItemList(productInfo, "PARAMETERLIST", true);
	    	for(Element parameterInfo:parameterList)
	    	{
	    		String parameterName=SMessageUtil.getChildText(parameterInfo, "PARAMETERNAME", true);
	    		List<Element> siteList = SMessageUtil.getSubSequenceItemList(parameterInfo, "SITELIST", true);
	    		for(Element siteInfo:siteList)
	    		{
	    			String siteName=SMessageUtil.getChildText(siteInfo, "SITENAME", true);
	    			String siteValue=SMessageUtil.getChildText(siteInfo, "SITEVALUE", true);
	    			
	                List<Object> lotBindList = new ArrayList<Object>();
	    			
	    			lotBindList.add(eventInfo.getEventTimeKey());
	    			lotBindList.add(productName);
	    			lotBindList.add(parameterName);
	    			lotBindList.add(siteName);
	    			lotBindList.add(siteValue);
	    			lotBindList.add(productData.getLotName());
	    			lotBindList.add(productData.getCarrierName());
	    			lotBindList.add(machineName);
	    			lotBindList.add(unitName);
	    			lotBindList.add(subUnitName);
	    			lotBindList.add(productData.getProductSpecName());
	    			lotBindList.add(productData.getProcessFlowName());
	    			lotBindList.add(productData.getProcessOperationName());
	    			lotBindList.add(eventInfo.getEventTime());
	    				    			
	    			updateLotArgList.add(lotBindList.toArray());
	    		}
	    	}
	    	
	    }

	    try
	    {
			if(updateLotArgList!=null&&updateLotArgList.size()>0)
			{
				MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
			}
	    }
	    catch(greenFrameDBErrorSignal n)
	    {
	    	log.error("Insert CT_KeyProcessData failed");
	    }
	
	    
	}

}
