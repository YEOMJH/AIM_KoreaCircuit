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
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ZPP_MES_ORDEROFF_ESB implements Job, InitializingBean {
	
	private static Log log = LogFactory.getLog(ZPP_MES_ORDEROFF_ESB.class);
	
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
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteWorkOrder", "System", "Auto Completed", "", "");
		Object[] bindArray = new Object[0];
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append(" SELECT S3.PRODUCTREQUESTNAME, ");
		sqlSelect.append(" MAX(Z.SEQ), ");
		sqlSelect.append(" CASE WHEN MAX(Z.SEQ) IS NULL THEN 0 WHEN  SYSDATE-(TO_DATE(MAX(SUBSTR(Z.SEQ,1,10)),'YYYYMMDDHH24'))>=2 THEN 1 ELSE -1 END AS ERRORTIME, ");
		sqlSelect.append(" MAX(P3.COMPLETETIME) , ");
		sqlSelect.append(" CASE WHEN SYSDATE-MAX(P3.COMPLETETIME)>= 0.1 THEN 1 ELSE  -1 END AS COMPLETETIME ");
		sqlSelect.append(" FROM ZPP_MES_ORDEROFF_ESB Z,PRODUCTREQUEST P3,( ");
		sqlSelect.append("    SELECT DISTINCT S2.PRODUCTREQUESTNAME FROM PRODUCTREQUEST P2, CT_SUPERPRODUCTREQUEST S2   ");
		sqlSelect.append("    WHERE P2.SUPERPRODUCTREQUESTNAME=S2.PRODUCTREQUESTNAME   ");
		sqlSelect.append(" AND S2.PRODUCTREQUESTSTATE='Released'  ");
		sqlSelect.append(" AND S2.PRODUCTREQUESTNAME NOT IN (  "); 
		sqlSelect.append(" SELECT S.PRODUCTREQUESTNAME FROM PRODUCTREQUEST P,CT_SUPERPRODUCTREQUEST S  "); 
		sqlSelect.append(" WHERE P.SUPERPRODUCTREQUESTNAME=S.PRODUCTREQUESTNAME "); 
		sqlSelect.append(" AND S.PRODUCTREQUESTSTATE='Released' "); 
		sqlSelect.append(" AND P.PRODUCTREQUESTSTATE!='Completed'))S3  "); 
		sqlSelect.append(" WHERE S3.PRODUCTREQUESTNAME = Z.PRODUCTREQUESTNAME(+) "); 
		sqlSelect.append(" AND P3.SUPERPRODUCTREQUESTNAME=S3.PRODUCTREQUESTNAME "); 
		sqlSelect.append(" GROUP BY S3.PRODUCTREQUESTNAME "); 
		sqlSelect.append(" ORDER BY PRODUCTREQUESTNAME DESC "); 
		
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
				String errorTime= CommonUtil.getValue(resultRow, "ERRORTIME");
				String completeTime= CommonUtil.getValue(resultRow, "COMPLETETIME");
				List<ProductRequest> notCompleteProductRequestList = null;
				SuperProductRequest superWO=new SuperProductRequest();
				if(!(StringUtils.equals(errorTime, "1")||
						(StringUtils.equals(completeTime, "1")&&StringUtils.equals(errorTime, "0"))))
				{
					log.info("Time is NG "+productRequestName);	
					continue;
				}
				
				try
				{					
				    superWO=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new String[]{productRequestName});
					if(!StringUtils.equals(superWO.getProductRequestState(), "Released")
							||superWO.getReleasedQuantity()!=superWO.getScrappedQuantity()+superWO.getFinishedQuantity())
					{
						log.info("Super WO is not Relesed or Qty is NG: "+productRequestName);	
						continue;
					}										
				}				
				catch (greenFrameDBErrorSignal n ) 
				{
					log.info("not found SuperWO: "+productRequestName);		
					continue;
				}
				
                try 
                {
                  notCompleteProductRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestState != ? ", new Object[] { productRequestName, GenericServiceProxy.getConstantMap().Prq_Completed});
                  log.info(productRequestName+" exise released littleWO");
                  continue;
				} 
                catch (NotFoundSignal n1) 
				{
					log.info(productRequestName +" SuperWO Validate OK");
				}
				catch (FrameworkErrorSignal n2) 
				{
					log.info(productRequestName+" SuperWO Validate OK");
				}
                try
				{
					if(StringUtils.isNotEmpty(sapFlag)&&StringUtils.equals(sapFlag, "Y"))
					{
						eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
						List<Map<String, String>> dataInfoMapList = new ArrayList<Map<String,String>>();
						
						Map<String, String> dataInfoMap = new HashMap<String, String>();
						dataInfoMap.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
						
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
							dataInfoMap.put("EVENTTIME",receiveTime.toString().replace("-","").substring(0,8));
						}
						else
						{
							dataInfoMap.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
						}
						dataInfoMap.put("ZCANCEL", "");
						
						dataInfoMapList.add(dataInfoMap);
						
						String resultCode=ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().workOrderClose(eventInfo, dataInfoMapList, 1);
						if(!resultCode.equals("S"))
						{
							log.info(productRequestName+" Close SAPWO Failed");
						}
						else
						{
							superWO.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
							superWO.setLastEventName(eventInfo.getEventName());
							superWO.setLastEventComment(eventInfo.getEventComment());
							superWO.setLastEventFlag("N");
							superWO.setLastEventTime(eventInfo.getEventTime());
							superWO.setLastEventTimeKey(eventInfo.getEventTimeKey());
							superWO.setLastEventUser(eventInfo.getEventUser());
							superWO.setCompleteTime(eventInfo.getEventTime());
							superWO.setCompleteUser(eventInfo.getEventUser());
							GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
							ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superWO);
							GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						}
					}					
				}
				catch(CustomException cs)
				{
					log.info(productRequestName+" Close SAPWO Failed");
				}
				catch(Exception ex)
				{
					log.info(productRequestName+" Send To SAP Failed");
				}
				
			}
		}
		
	}

}
