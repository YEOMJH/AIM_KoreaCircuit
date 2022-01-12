package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class MaskMaterialService extends CTORMService<MaskMaterial>{

	public static Log logger = LogFactory.getLog(MaskMaterial.class);
	
	private final String historyEntity = "MaskMaterialHistory";
	
	public List<MaskMaterial> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
		{
			List<MaskMaterial> result = super.select(condition, bindSet, MaskMaterial.class);
			
			return result;
		}
		
		public MaskMaterial selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
		{
			return super.selectByKey(MaskMaterial.class, isLock, keySet);
		}
		
		public MaskMaterial create(EventInfo eventInfo, MaskMaterial dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void create(EventInfo eventInfo, List<MaskMaterial> dataInfoList)
				throws greenFrameDBErrorSignal
		{		
			dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
			
			super.insert(dataInfoList);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
		
		public void remove(EventInfo eventInfo, MaskMaterial dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public void remove(EventInfo eventInfo, List<MaskMaterial> dataInfoList) throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	
			super.delete(dataInfoList);
		}
		
		public MaskMaterial modify(EventInfo eventInfo, MaskMaterial dataInfo)
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		private List<MaskMaterial> setDataFromEventInfo(EventInfo eventInfo, List<MaskMaterial> dataInfoList)
		{
			for(MaskMaterial dataInfo : dataInfoList)
			{
				dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
			}
			
			return dataInfoList;
		}
		
		private MaskMaterial setDataFromEventInfo(EventInfo eventInfo, MaskMaterial dataInfo)
		{
			String eventTimekey = null;
			if(StringUtil.isNotEmpty(eventInfo.getLastEventTimekey()))
			{
				eventTimekey = eventInfo.getLastEventTimekey();
			}
			else if(StringUtil.isNotEmpty(eventInfo.getEventTimeKey()))
			{
				eventTimekey = eventInfo.getEventTimeKey();
			}
			else
			{
				eventTimekey = TimeStampUtil.getCurrentEventTimeKey();
			}
			
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTimeKey(eventTimekey);
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			
			return dataInfo;
		}
		
		public MaskMaterial getMaskMaterialData(String maskLotName,String materialType,String materialName) throws CustomException
		{
			if (StringUtil.in(StringUtil.EMPTY, maskLotName,materialType,materialName))
			{
				//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
				throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
			}

			MaskMaterial dataInfo = null;
			try
			{
				dataInfo = this.selectByKey(false, new Object[] { maskLotName,materialType,materialName });
			}
			catch (greenFrameDBErrorSignal dbEx)
			{
				if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
					throw new CustomException("COMM-1000", "CT_MASKMATERIAL", String.format("MaskLotName = %s, MaterialType = %s , MaterialName = %s", maskLotName,materialType,materialName));
				else 
					throw new CustomException(dbEx.getCause());
			}
			catch(Exception ex)
			{
				throw new CustomException(ex.getCause());
			}
			
			return dataInfo;
		}
}
