package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TFEMaskCalculationTimer implements Job, InitializingBean
{
	private static Log log = LogFactory.getLog(TFEMaskCalculationTimer.class);
	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
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
			//Monitor TFE Masks's clean time
			monitorTFEMaskCleanTime();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void monitorTFEMaskCleanTime() throws CustomException
	{
		try
		{
			//isolation
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			{
				String sql = "SELECT L.MASKLOTNAME "
						   + "FROM CT_MASKLOT L, PROCESSFLOW PF "
						   + "WHERE 1 = 1 "
						   + "  AND L.CLEANTIME IS NOT NULL "
						   + "  AND L.CLEANSTATE = 'Clean' "
						   + "  AND L.MASKLOTSTATE != 'Scrap' "
						   + "  AND L.MASKLOTPROCESSSTATE = 'WAIT' "
						   + "  AND L.MASKKIND = 'TFE' "
						   + "  AND ROUND((SYSDATE - L.CLEANTIME) * 24) > NVL(L.DURATIONUSEDLIMIT, 0)"
						   + "  AND PF.DETAILPROCESSFLOWTYPE = 'TFENEW' "
						   + "  AND PF.FACTORYNAME = L.FACTORYNAME "
						   + "  AND PF.PROCESSFLOWNAME = L.MASKPROCESSFLOWNAME "
						   + "  AND PF.PROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ";
				
				List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] {});
				if (resultDataList != null && resultDataList.size() > 0)
				{
					List<MaskLot> maskLotDataList = new ArrayList<MaskLot>();
					List<Durable> carrierList = new ArrayList<Durable>();
					
					for (OrderedMap resultData : resultDataList) 
					{
						String maskLotName = resultData.get("MASKLOTNAME").toString();
						
						MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
						
						maskLotData.setCleanFlag(constantMap.Flag_N);
						maskLotData.setCleanState(constantMap.Dur_Dirty);
						maskLotData.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold); 
						
						maskLotDataList.add(maskLotData);
						
						String carrierName = maskLotData.getCarrierName();
						if(StringUtils.isNotEmpty(carrierName))
						{
							boolean notInList = true;
							for(Durable carrierDataInList : carrierList)
							{
								String carrierNameInList = carrierDataInList.getKey().getDurableName();
								if(StringUtils.equals(carrierNameInList, carrierName))
								{
									notInList = false;
								}
							}
							
							if(notInList)
							{
								try
								{
									Durable carrierDataSearched = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
									if(StringUtils.equals(carrierDataSearched.getDurableCleanState(), constantMap.Dur_Clean))
									{
										carrierList.add(carrierDataSearched);
									}
								}
								catch(CustomException ce) {}
							}
						}	
					}
					
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "OLED Mask Clean Monitoring service", null, null);
					ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotDataList);
					
					if(carrierList.size() > 0)
					{
						for(Durable carrierData : carrierList)
						{
							DirtyInfo dirtyData = MESDurableServiceProxy.getDurableInfoUtil().dirtyInfo(carrierData, carrierData.getUdfs().get("MACHINENAME"));
							MESDurableServiceProxy.getDurableServiceImpl().dirty(carrierData, dirtyData, eventInfo);
							DurableServiceProxy.getDurableService().setEvent(carrierData.getKey(), eventInfo, new SetEventInfo());
						}
					}
				}
			}
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(fe);
		}
		catch (Exception ex)
		{
			//safety gear
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(ex);
		}
	}
}
