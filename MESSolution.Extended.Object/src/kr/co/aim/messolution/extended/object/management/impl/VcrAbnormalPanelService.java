package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.data.VcrAbnormalPanel;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class VcrAbnormalPanelService  extends CTORMService<VcrAbnormalPanel> {
	
	public static Log logger = LogFactory.getLog(VcrAbnormalPanelService.class);
	private final String historyEntity = "CT_VCRABNORMALPANELHISTORY";
	
	public VcrAbnormalPanel selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{

		return super.selectByKey(VcrAbnormalPanel.class, isLock, keySet);
	}

	public List<VcrAbnormalPanel> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{

		List<VcrAbnormalPanel> result = super.select(condition, bindSet, VcrAbnormalPanel.class);

		return result;
	}

	public void modify(EventInfo eventInfo, VcrAbnormalPanel dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.update(dataInfo);

	}

	public void create(EventInfo eventInfo, VcrAbnormalPanel dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.insert(dataInfo);

	}

	public void remove(EventInfo eventInfo, VcrAbnormalPanel dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public VcrAbnormalPanel getVcrAbnormalPanelData(String taskID, String panelID,String vcrPanelID)
	{
		VcrAbnormalPanel dataInfo = new VcrAbnormalPanel();

		try
		{
			dataInfo = ExtendedObjectProxy.getVcrAbnormalPanelDataService().selectByKey(false, new Object[] { taskID, panelID,vcrPanelID });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

}
