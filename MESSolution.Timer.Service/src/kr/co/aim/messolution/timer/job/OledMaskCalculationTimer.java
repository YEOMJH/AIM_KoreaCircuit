package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class OledMaskCalculationTimer implements Job, InitializingBean
{
	private static Log log = LogFactory.getLog(OledMaskCalculationTimer.class);
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
			//Monitor OLED Masks's clean time
			monitorOledMaskCleanTime();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	private void monitorOledMaskCleanTime() throws CustomException
	{
		try
		{
			//isolation
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			{
				String condition = "CLEANTIME IS NOT NULL "
					+ "AND CLEANSTATE NOT IN ('Dirty', 'Repairing') "
					+ "AND MASKLOTSTATE <> 'Scrapped' "
					+ "AND SYSDATE > (CLEANTIME + DURATIONUSEDLIMIT / 24) "
					+ "AND MASKLOTPROCESSSTATE != 'RUN' "
					+ "AND MASKKIND != 'TFE' ";
				
				List<MaskLot> maskLotList = null;
				try
				{
					maskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, null);
				}
				catch(greenFrameDBErrorSignal nfdes)
				{
					throw new CustomException("MASK-0067");
				}
				
				List<Durable> carrierList = new ArrayList<Durable>(); 
				Timestamp currentTimestamp = TimeStampUtil.getCurrentTimestamp();
				for(MaskLot maskLotData : maskLotList)
				{					
					maskLotData.setCleanFlag(constantMap.Flag_N);
					maskLotData.setCleanState(constantMap.Dur_Dirty);
					maskLotData.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold); 
					
					String carrierName = maskLotData.getCarrierName();
					if(StringUtil.isNotEmpty(carrierName))
					{
						boolean notInList = true;
						for(Durable carrierDataInList : carrierList)
						{
							String carrierNameInList = carrierDataInList.getKey().getDurableName();
							if(StringUtil.equals(carrierNameInList, carrierName))
							{
								notInList = false;
							}
						}
						
						if(notInList)
						{
							try
							{
								Durable carrierDataSearched = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
								if(StringUtil.equals(carrierDataSearched.getDurableCleanState(), constantMap.Dur_Clean))
								{
									carrierList.add(carrierDataSearched);
								}
							}
							catch(CustomException ce) {}
						}
					}

					//MutilHold
					EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "OLED Mask Clean Monitoring service", "HoldByOledMaskCalculationTimer", "CleanTimeOverHold");
					holdEventInfo.setEventTime(currentTimestamp);;
					ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotData);				
				}
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "OLED Mask Clean Monitoring service", "HoldByOledMaskCalculationTimer", "CleanTimeOverHold");
				eventInfo.setEventTime(currentTimestamp);
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotList);
				
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
