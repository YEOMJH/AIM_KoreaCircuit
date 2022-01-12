package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> DEFECTLIST = SMessageUtil.getBodySequenceItemList(doc, "DELETELIST", true);

		for (Element DEFECT : DEFECTLIST)
		{
			String defectCode = SMessageUtil.getChildText(DEFECT, "DEFECTCODE", true);
			String superDefectCode = SMessageUtil.getChildText(DEFECT, "SUPERDEFECTCODE", true);
			String levelNo = SMessageUtil.getChildText(DEFECT, "LEVELNO", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDefectCode", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			if(Integer.parseInt(levelNo)<3 &&checkSubDefect(defectCode,superDefectCode,levelNo)){				
				//throw new CustomException(;//,defectCode,superDefectCode
				throw new CustomException("DEFECTCODE-0008",defectCode,superDefectCode,levelNo);
			}

			DefectCode dataInfo = ExtendedObjectProxy.getDefectCodeService().selectByKey(false, new Object[]{defectCode,superDefectCode});
			ExtendedObjectProxy.getDefectCodeService().remove(eventInfo, dataInfo);			
		}
		return doc;
	}
	
	private boolean checkSubDefect(String defecCode,String superDefectCode,String levelNo) throws greenFrameDBErrorSignal, CustomException
	{

			
			//ExtendedObjectProxy.getDefectCodeService().selectByKey(false, new Object[] { superDefectCode, Integer.toString(Integer.parseInt(levelNo)+1)});
			
			StringBuffer sql=new StringBuffer();
			sql.append(" SELECT DEFECTCODE  ");
			sql.append(" FROM CT_DEFECTCODE ");
			sql.append(" WHERE SUPERDEFECTCODE=:SUPERDEFECTCODE ");
			sql.append(" AND LEVELNO =:LEVELNO ");
						
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("SUPERDEFECTCODE", defecCode);
			bindMap.put("LEVELNO", Integer.toString((Integer.parseInt(levelNo)+1)));
			
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if(result==null || result.size()==0){
				return false;
			}
			return true;
		}
	}
