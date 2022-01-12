package kr.co.aim.messolution.lot.event;

import java.util.List;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.extended.object.management.data.SorterSignReserve;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SorterJobEnded extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("End", getEventUser(), getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		
		//-------------------Delete SorterSignReserve Start------------------//
		List<SortJob> sortJobData = ExtendedObjectProxy.getSortJobService().getSortJobList(jobName);
		
		if(sortJobData.get(0).getJobType().equals("Split"))
		{
			List<SortJobProduct> sortProductList = ExtendedObjectProxy.getSortJobProductService().getSortJobProductByJobName(jobName);
			
			if(sortProductList!=null && sortProductList.size()>0)
			{
				for (SortJobProduct sortJobProduct : sortProductList) 
				{
					List<SorterSignReserve> checkList = ExtendedObjectProxy.getSorterSignReserveService().getDataInfoListByProductName(sortJobProduct.getProductName());
					
					if (checkList!=null && checkList.size()>0) 
					{
						ExtendedObjectProxy.getSorterSignReserveService().remove(eventInfo,checkList);
					}
				}
			}
		}
		//-------------------Delete SorterSignReserve End------------------//
		
		MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED);
	}
}
