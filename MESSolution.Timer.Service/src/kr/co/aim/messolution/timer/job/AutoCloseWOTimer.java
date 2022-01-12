package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;

public class AutoCloseWOTimer implements Job, InitializingBean{
	
	private static Log log = LogFactory.getLog(AutoCloseWOTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			closeWO();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	public void closeWO() throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteWorkOrder", "System", "Auto Completed", "", "");
		Object[] bindArray = new Object[0];
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append(" SELECT P.PRODUCTREQUESTNAME,P.FACTORYNAME,P.PRODUCTSPECNAME,P.PLANFINISHEDTIME, ");
		sqlSelect.append(" P.PLANQUANTITY,P.RELEASEDQUANTITY,P.SCRAPPEDQUANTITY,P.FINISHEDQUANTITY FROM PRODUCTREQUEST P ");
		sqlSelect.append(" WHERE P.FACTORYNAME IN (SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME='AutoCloseWOFactory') ");
		sqlSelect.append(" AND P.PRODUCTREQUESTSTATE ='Released' ");
		sqlSelect.append(" AND P.RELEASEDQUANTITY=P.SCRAPPEDQUANTITY+P.FINISHEDQUANTITY ");
		sqlSelect.append(" AND ((CASE WHEN P.FACTORYNAME!='ARRAY' THEN P.RELEASETIME+10 ELSE P.PLANFINISHEDTIME END)<SYSDATE ");
		sqlSelect.append(" OR P.PLANFINISHEDTIME<SYSDATE) ");
		sqlSelect.append(" AND P.PRODUCTREQUESTTYPE IN ('P','E','T') ");

		
		List<ListOrderedMap> selResult;
		try
		{
			selResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSelect.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			selResult = null;
		}
		if (selResult != null && selResult.size() > 0)
		{
			for (ListOrderedMap resultRow : selResult)
			{
				String productRequestName = CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME");
				String factoryName = CommonUtil.getValue(resultRow, "FACTORYNAME");
				ProductRequest productRequest=new ProductRequest();
				try
				{
					productRequest=ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(productRequestName));
				}
				catch(NotFoundSignal n)
				{
					log.info("WO Not Found: "+productRequestName);	
					continue;
				}
				catch (FrameworkErrorSignal f) 
				{
					log.info("WO Not Found: "+productRequestName);	
					continue;
				}
				if(!StringUtils.equals(productRequest.getProductRequestState(), "Released")
						||productRequest.getReleasedQuantity()!=productRequest.getScrappedQuantity()+productRequest.getFinishedQuantity())
				{
					log.info(" WO is not Relesed or Qty is NG: "+productRequestName);	
					continue;
				}
				if(!checkCreateLot(productRequest))
				{
					log.info(" WO has Created lot: "+productRequestName);	
					continue;
				}
				if(StringUtils.equals(factoryName, "POSTCELL")&&!checkShipLot(productRequest))
				{
					log.info(" WO has not Shipped lot: "+productRequestName);	
					continue;
				}

                try
                {
                	MakeCompletedInfo makeCompletedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeCompletedInfo(productRequest);

    				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(productRequest, makeCompletedInfo, eventInfo);
                }
                catch(Exception e)
                {
                	log.info(" WO Close error: "+productRequestName);
                }
				
			}
		}
		
	}
	
	private boolean checkCreateLot(ProductRequest productRequestData) 
	{
		String condition = " PRODUCTREQUESTNAME = ? AND LOTSTATE = 'Created'";
		Object[] bindSet = new Object[] { productRequestData.getKey().getProductRequestName() };

		List<Lot> lotList = new ArrayList<Lot>();

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
			return false;
		}
		catch (Exception e)
		{
			lotList = null;
			return true;
		}
	}
	
	private boolean checkShipLot(ProductRequest productRequestData) 
	{
		String condition = " PRODUCTREQUESTNAME = ? AND LOTSTATE != 'Shipped' AND PRODUCTTYPE='Panel' ";
		Object[] bindSet = new Object[] { productRequestData.getKey().getProductRequestName() };

		List<Lot> lotList = new ArrayList<Lot>();

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
			return false;
		}
		catch (Exception e)
		{
			lotList = null;
			return true;
		}
	}

}