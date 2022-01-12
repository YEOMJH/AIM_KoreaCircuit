package kr.co.aim.messolution.lot.event.Tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class PickEndReport extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PickEndReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String timeKey = SMessageUtil.getBodyItemValue(doc, "TIMEKEY", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", false);
		String code = SMessageUtil.getBodyItemValue(doc, "CODE", false);
		String qty = SMessageUtil.getBodyItemValue(doc, "QUANTITY", false);
		String endQty = SMessageUtil.getBodyItemValue(doc, "ENDQUANTITY", true);

		//MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		SVIPickInfo pickData = ExtendedObjectProxy.getSVIPickInfoService().getDataInfoByKey(timeKey, true);
		
		CommonValidation.isNumeric(endQty);
        
		if (!pickData.getDownLoadFlag().equals("Y"))
		{
			// SVI-0002:The DownFlag of the SVI pick information is not Y.
			throw new CustomException("SVI-0002");
		}

		if (!pickData.getMachineName().equals(machineName))
		{
			//SVI-0003:Reported machine [{0}] is not registered in the SVI pick information.[PickKey={1},MachineName ={2}]
			throw new CustomException("SVI-0003", machineName, timeKey, pickData.getMachineName());
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), this.getEventUser(), this.getEventComment());

		pickData.setEndQuantity(Long.parseLong(endQty));
		ExtendedObjectProxy.getSVIPickInfoService().modify(eventInfo, pickData);

		// delete current data row
		ExtendedObjectProxy.getSVIPickInfoService().delete(pickData);
	}
}