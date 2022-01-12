package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class PanelJudgeService extends CTORMService<PanelJudge> {
	
public static Log logger = LogFactory.getLog(PanelJudge.class);
	
	private final String historyEntity = "PanelJudgeHistory";
	
	public List<PanelJudge> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<PanelJudge> result = super.select(condition, bindSet, PanelJudge.class);
			
			return result;
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public PanelJudge selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(PanelJudge.class, isLock, keySet);
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public PanelJudge create(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PanelJudge modify(EventInfo eventInfo, PanelJudge dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public boolean checkExistProductName(String panelName) throws CustomException{
		
		
		boolean existProduct = false;
		
		String condition = "WHERE PANELNAME = ?";
		
		Object[] bindSet = new Object[] { panelName};
	 try
	 {
		 List<PanelJudge> sqlResult =  ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
			 
		 return existProduct = true;
	 }
	 catch (NotFoundSignal ex)
	 {
		 return existProduct = false;
	 }
	}
	
	public List<Map<String, Object>> checkPanelJudge(String judge) throws CustomException{
		
		String sql = "SELECT ENUMNAME, ENUMVALUE, DESCRIPTION, DEFAULTFLAG FROM ENUMDEFVALUE WHERE ENUMNAME = 'YieldJudge' AND ENUMVALUE = :ENUMVALUE";
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("ENUMVALUE", judge);
		
		List<Map<String, Object>> sqlJudge = 
				kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlJudge;		
	}

	public List<PanelJudge> getPanelList(String sheetName,String glassName) throws CustomException{
		
		try 
		{
			String condition = "Where sheetName = ? and glassName = ? ";
	
			Object bindSet[] = new Object[] { sheetName, glassName};
	
			List<PanelJudge> panelList = ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
	
			return panelList;
		} 
		catch (Exception ex) 
		{
			logger.info("glassName: " + glassName + "is not exist panelList");
			return null;
		}			
	}
}
