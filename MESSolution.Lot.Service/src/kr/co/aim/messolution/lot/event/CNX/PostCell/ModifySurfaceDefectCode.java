package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SurfaceDefectCode;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;


public class ModifySurfaceDefectCode  extends SyncHandler {
	
	private static Log log = LogFactory.getLog(ModifySurfaceDefectCode.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String oprationName = SMessageUtil.getBodyItemValue(doc, "OPERATIONNAME", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String standard = SMessageUtil.getBodyItemValue(doc, "STANDARD", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(actionName+"SurfaceDefectCode", getEventUser(), getEventComment(), "", "");
		
		if(StringUtil.equals(actionName, "Add"))
		{
			SurfaceDefectCode newSurfeceDefectCode = new SurfaceDefectCode();
			newSurfeceDefectCode.setProductSpecName(productSpecName);
			newSurfeceDefectCode.setOperationName(oprationName);
			newSurfeceDefectCode.setJudge(judge);
			newSurfeceDefectCode.setDefectCode(defectCode);
			newSurfeceDefectCode.setStandard(standard);
			newSurfeceDefectCode.setDescription(description);
			
			ExtendedObjectProxy.getSurfaceDefectCodeService().create(eventInfo, newSurfeceDefectCode);
			log.info("Excute Insert Notice :Inserted into CT_SurfaceDefectCode! ");
			
		}
		else if(StringUtil.equals(actionName, "Modify"))
		{
			SurfaceDefectCode surfaceDefectCode = ExtendedObjectProxy.getSurfaceDefectCodeService().selectByKey(true, new Object[] { productSpecName, oprationName, judge, defectCode });
			if (surfaceDefectCode == null)
			{
				throw new CustomException("DEFECTCODE-0002", defectCode);
			}
			surfaceDefectCode.setStandard(standard);
			surfaceDefectCode.setDescription(description);
			ExtendedObjectProxy.getSurfaceDefectCodeService().modify(eventInfo, surfaceDefectCode);
			log.info("Excute Insert Notice :Modify into CT_SurfaceDefectCode! ");
		}
		else if(StringUtil.equals(actionName, "Delete"))
		{
			SurfaceDefectCode surfaceDefectCode = ExtendedObjectProxy.getSurfaceDefectCodeService().selectByKey(true, new Object[] { productSpecName, oprationName, judge, defectCode });
			if (surfaceDefectCode == null)
			{
				throw new CustomException("DEFECTCODE-0002", defectCode);
			}
			ExtendedObjectProxy.getSurfaceDefectCodeService().remove(eventInfo, surfaceDefectCode);
			log.info("Excute Insert Notice :Delete into CT_MviDefectCode! ");
		}
		
		return doc;
	}
	
}
