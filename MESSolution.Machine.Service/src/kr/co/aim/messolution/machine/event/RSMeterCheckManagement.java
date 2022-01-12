package kr.co.aim.messolution.machine.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.ExtendedSSLSession;
import javax.print.DocFlavor.STRING;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineHistory;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;


public class RSMeterCheckManagement extends SyncHandler{
	@Override
	public Document doWorks(Document doc) throws CustomException
	{	
		String type = SMessageUtil.getBodyItemValue(doc, "TYPE", true);
		String checkType = SMessageUtil.getBodyItemValue(doc, "CHECKTYPE", true);
		String returnProcessOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSOPERATIONNAME", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TurnCheckManagement", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		for (Element Product : productList)
		{
			String productName = SMessageUtil.getChildText(Product, "PRODUCTNAME", true);
			String turnCount = SMessageUtil.getChildText(Product, "TURNCOUNT", false);
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			String sql="";
			if(StringUtils.equals(type, "Add"))
			{
				sql="INSERT INTO CT_RSCHECKLOTINFO(PRODUCTNAME,LOTNAME,CARRIERNAME,PRODUCTSPECNAME,PROCESSFLOWNAME,PROCESSOPERATIONNAME,"
				+" POSITION,TURNCOUNT,LASTEVENTTIME,LASTEVENTTIMEKEY,LASTEVENTUSER,LASTEVENTCOMMENT,CHECKTYPE,RETURNPROCESSOPERATIONNAME) VALUES(:PRODUCTNAME,:LOTNAME,:CARRIERNAME"
				+ ",:PRODUCTSPECNAME,:PROCESSFLOWNAME,:PROCESSOPERATIONNAME,:POSITION,:TURNCOUNT,:LASTEVENTTIME,:LASTEVENTTIMEKEY,:LASTEVENTUSER,:LASTEVENTCOMMENT,:CHECKTYPE,:RETURNPROCESSOPERATIONNAME ) ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("PRODUCTNAME", productName);
				bindMap.put("LOTNAME",productData.getLotName() );
				bindMap.put("CARRIERNAME", productData.getCarrierName());
				bindMap.put("PRODUCTSPECNAME", productData.getProductSpecName());
				bindMap.put("PROCESSFLOWNAME",productData.getProcessFlowName() );
				bindMap.put("PROCESSOPERATIONNAME", productData.getProcessOperationName());
				bindMap.put("POSITION", productData.getPosition());
				bindMap.put("TURNCOUNT", turnCount);
				bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
				bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
				bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
				bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
				bindMap.put("CHECKTYPE",checkType);
				bindMap.put("RETURNPROCESSOPERATIONNAME",returnProcessOperationName );
				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}
			else if(StringUtils.equals(type, "Delete"))
			{
				sql=" DELETE CT_RSCHECKLOTINFO WHERE PRODUCTNAME=:PRODUCTNAME AND CHECKTYPE=:CHECKTYPE ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("PRODUCTNAME", productName);
				bindMap.put("CHECKTYPE", checkType);
				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}

		}
		return doc;
	}
}