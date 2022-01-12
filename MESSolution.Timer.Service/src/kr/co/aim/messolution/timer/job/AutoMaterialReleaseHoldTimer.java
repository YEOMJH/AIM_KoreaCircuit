package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class AutoMaterialReleaseHoldTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(AutoMaterialReleaseHoldTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		String sql = "SELECT 'Durable' AS MATERIALKIND, WH.MATERIALSPECNAME, WH.BATCHNO, WH.RELEASESYSTEM, WH.RELEASEUSER, WH.RELEASETIME "
				   + "FROM MES_WMSIF_HOLD WH, DURABLESPEC DS "
				   + "WHERE 1 = 1 "
				   + "  AND NVL(WH.HOLDSTATE, 'N') = 'N' "
				   + "  AND WH.RELEASESYSTEM = 'WMS' "
				   + "  AND NVL(WH.INTERFACEFLAG, 'N') = 'N' "
				   + "  AND DS.DURABLESPECNAME = WH.MATERIALSPECNAME "
				   + "  AND DS.DURABLESPECVERSION = '00001' "
				   + "UNION ALL "
				   + "SELECT 'Consumable' AS MATERIALKIND, WH.MATERIALSPECNAME, WH.BATCHNO, WH.RELEASESYSTEM, WH.RELEASEUSER, WH.RELEASETIME "
				   + "FROM MES_WMSIF_HOLD WH, CONSUMABLESPEC CS "
				   + "WHERE 1 = 1 "
				   + "  AND NVL(WH.HOLDSTATE, 'N') = 'N' "
				   + "  AND WH.RELEASESYSTEM = 'WMS' "
				   + "  AND NVL(WH.INTERFACEFLAG, 'N') = 'N' "
				   + "  AND CS.CONSUMABLESPECNAME = WH.MATERIALSPECNAME "
				   + "  AND CS.CONSUMABLESPECVERSION = '00001' ";
		
		List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("MES_WMSIF_HOLD is no data.");
			return ;
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldMaterial", "WMS", "AutoMaterialReleaseHoldTimer", null, null);
		
		for (Map<String, Object> resultData : resultDataList) 
		{
			String materialKind = resultData.get("MATERIALKIND").toString();
			String materialSpecName = resultData.get("MATERIALSPECNAME").toString();
			String batchNo = resultData.get("BATCHNO").toString();
			
			sql = "UPDATE MES_WMSIF_HOLD "
				+ "SET INTERFACEFLAG = 'Y' "
				+ "WHERE 1 = 1 "
				+ "  AND MATERIALSPECNAME = :MATERIALSPECNAME "
				+ "  AND BATCHNO = :BATCHNO ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MATERIALSPECNAME", materialSpecName);
			bindMap.put("BATCHNO", batchNo);

			greenFrameServiceProxy.getSqlTemplate().update(sql, bindMap);
			
			if (StringUtils.equals("Consumable", materialKind))
			{
				this.holdConsumable(eventInfo, materialSpecName, batchNo);
			}
			else if (StringUtils.equals("Durable", materialKind))
			{
				this.holdDurable(eventInfo, materialSpecName, batchNo);
			}
		}
	}
	
	private void holdConsumable(EventInfo eventInfo, String consumableSpecName, String batchNo)
	{
		String condition = " BATCHNO = ? AND CONSUMABLESPECNAME = ? AND CONSUMABLEHOLDSTATE = 'Y' ";
		Object[] bindSet = new Object[] { batchNo, consumableSpecName };

		try
		{
			List<Consumable> consumableList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);

			for (Consumable consumableData : consumableList)
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("CONSUMABLEHOLDSTATE", "N");

				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
			}
		}
		catch (Exception e)
		{
			log.info("ConsumableData is Not Exist");
		}
	}

	private void holdDurable(EventInfo eventInfo, String durableSpecName, String batchNo)
	{
		String condition = " BATCHNO = ? AND DURABLESPECNAME = ? AND DURABLEHOLDSTATE = 'Y' ";
		Object[] bindSet = new Object[] { batchNo, durableSpecName };

		try
		{
			List<Durable> durableList = DurableServiceProxy.getDurableService().select(condition, bindSet);

			for (Durable durableData : durableList)
			{
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "N");

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
		catch (Exception e)
		{
			log.info("DurableData is Not Exist");
		}
	}
}
