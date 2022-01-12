package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CreateDefectCode extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{

			String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
			String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
			String superDefectCode = SMessageUtil.getBodyItemValue(doc, "SUPERDEFECTCODE", true);
			String levelNo = SMessageUtil.getBodyItemValue(doc, "LEVELNO", true);
			
			

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDefectCode", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			if (!checkExist(defectCode, superDefectCode))
				throw new CustomException("DEFECTCODE-0003", defectCode, superDefectCode);
			
			if (levelNo.equals("1")&&!superDefectCode.equals(defectCode))
				throw new CustomException("DEFECTCODE-0007", levelNo,defectCode,superDefectCode);
			
			if (Integer.parseInt(levelNo)>1&&!checkSuperDefectExist(superDefectCode,levelNo))
				throw new CustomException("DEFECTCODE-0009");
			

			DefectCode dataInfo =new DefectCode();
			dataInfo.setDefectCode(defectCode);
			if(StringUtil.isNotEmpty(description))dataInfo.setDescription(description);				
			dataInfo.setSuperDefectCode(superDefectCode);
			dataInfo.setLevelNo(Integer.parseInt(levelNo));
			

			ExtendedObjectProxy.getDefectCodeService().create(eventInfo, dataInfo);
		return doc;
	}
	
	private boolean checkExist(String defecCode, String superDefectCode) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getDefectCodeService().selectByKey(false, new Object[] { defecCode,superDefectCode});
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
	
	private boolean checkSuperDefectExist(String defectCode,String levelNo) throws greenFrameDBErrorSignal, CustomException
	{
			StringBuffer sql=new StringBuffer();
			sql.append(" SELECT DEFECTCODE  ");
			sql.append(" FROM CT_DEFECTCODE ");
			sql.append(" WHERE DEFECTCODE=:DEFECTCODE ");
			sql.append(" AND LEVELNO =:LEVELNO ");
						
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("LEVELNO", Integer.toString((Integer.parseInt(levelNo)-1)));
			
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if(result==null || result.size()==0){
				return false;
			}
			return true;
	}
}
