package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;

public class DeleteMaintenanceOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String maintOrderName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEORDERNAME", true);
		
		if(!maintOrderName.isEmpty())
		{
			String sqlOrder = "DELETE MAINTENANCEORDER MO " +
					 	      " WHERE 1 = 1" +
					 	      "   AND MO.MAINTENANCEORDERNAME = :MAINTENANCEORDERNAME ";
	
			Map<String,Object> bindMapOrder = new HashMap<String,Object>();
			bindMapOrder.put("MAINTENANCEORDERNAME", maintOrderName);			
			
			GenericServiceProxy.getSqlMesTemplate().update(sqlOrder, bindMapOrder);

			String sqlItem = "DELETE MAINTENANCEORDERITEM MO " +
					 	 	 " WHERE 1 = 1" +
					 	 	 "   AND MO.MAINTENANCEORDERNAME = :MAINTENANCEORDERNAME ";
	
			Map<String,Object> bindMapItem = new HashMap<String,Object>();
			bindMapItem.put("MAINTENANCEORDERNAME", maintOrderName);

			GenericServiceProxy.getSqlMesTemplate().update(sqlItem, bindMapItem);
		}
		else
		{
			throw new CustomException(String.format("[%s]th maintenance Order Delte is failed", maintOrderName));
		}
		return doc;
	}

}
