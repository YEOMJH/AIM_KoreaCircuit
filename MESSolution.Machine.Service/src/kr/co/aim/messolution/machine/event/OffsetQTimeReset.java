package kr.co.aim.messolution.machine.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhotoOffsetResult;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineHistory;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class OffsetQTimeReset extends SyncHandler{
	
	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		String photoOffsetLimit = SMessageUtil.getBodyItemValue(doc, "PHOTOOFFSETLIMIT", true);
		String oldPhotoOffsetLimit = SMessageUtil.getBodyItemValue(doc, "OLDPHOTOOFFSETLIMIT", true);
		List<Element> offsetList = SMessageUtil.getBodySequenceItemList(doc, "OFFSETLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ResetLastUseTime", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		//update ct_photooffsetresult
		for (Element offsetInfo : offsetList) 
		{	
			String machineName = SMessageUtil.getChildText(offsetInfo, "MACHINENAME", true);
		    String offset = SMessageUtil.getChildText(offsetInfo, "OFFSET", true);
		    String lastUseTime = SMessageUtil.getChildText(offsetInfo, "LASTUSETIME", true);
		    Timestamp resetUsetTime =TimeUtils.getTimestamp(lastUseTime);
		    PhotoOffsetResult photoOffsetResult = null;
		    try
		    {
		    	photoOffsetResult = ExtendedObjectProxy.getPhotoOffsetResultService().selectByKey(false, new Object[]{machineName, offset});
		    }
		    catch (Exception e)
		    {
		    	eventLog.info("");
		    }
            if (photoOffsetResult != null && !photoOffsetLimit.isEmpty())
            {
        		photoOffsetResult.setLastUseTime(resetUsetTime);
        		photoOffsetResult.setLastEventUser(eventInfo.getEventUser());
        		photoOffsetResult.setLastEventComment(eventInfo.getEventComment());
        		ExtendedObjectProxy.getPhotoOffsetResultService().update(photoOffsetResult);
            }
            else 
            {
            	PhotoOffsetResult newPhotoOffsetResult = new PhotoOffsetResult();
            	newPhotoOffsetResult.setMachineName(machineName);
            	newPhotoOffsetResult.setOffset(offset);
            	newPhotoOffsetResult.setLastUseTime(resetUsetTime);
            	newPhotoOffsetResult.setLastEventUser(eventInfo.getEventUser());
            	newPhotoOffsetResult.setLastEventComment(eventInfo.getEventComment());
            	ExtendedObjectProxy.getPhotoOffsetResultService().create(eventInfo, newPhotoOffsetResult);
			}
		}
		
		//update offsetLimit
		if(!StringUtils.equals(oldPhotoOffsetLimit, photoOffsetLimit))
		{
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE ENUMDEFVALUE SET ENUMVALUE=:ENUMVALUE,DESCRIPTION=:DESCRIPTION WHERE ENUMNAME='PhotoOffsetLimit' ");
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ENUMVALUE", photoOffsetLimit);
			bindMap.put("DESCRIPTION", photoOffsetLimit);
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		
		return doc;
	}

}
