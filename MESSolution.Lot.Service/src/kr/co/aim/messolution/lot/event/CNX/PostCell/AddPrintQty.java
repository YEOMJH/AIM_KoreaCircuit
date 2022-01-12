package kr.co.aim.messolution.lot.event.CNX.PostCell;

import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AddPrintQty extends SyncHandler 
{
	private static Log log = LogFactory.getLog(AddPrintQty.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String packingName = SMessageUtil.getBodyItemValue(doc, "PACKINGNAME", true);
		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(packingName);
		String qty = processGroup.getUdfs().get("PRINTQTY").toString();
		if(qty.isEmpty())
		{
			qty = "0";
		}
		int printQty = Integer.parseInt(qty);
		printQty=printQty+1;
		qty = Integer.toString(printQty);
		
		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("PRINTQTY", qty);
		processGroup.setUdfs(packUdfs);
		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);	
		return doc;
	}
}
