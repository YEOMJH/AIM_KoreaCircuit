package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class UpdateOICVersion extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String newVersion = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
		String newDescription = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);

		// Delete old data
		String sql = " DELETE ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("ENUMNAME", "LASTOICVERSION");

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		// Insert new data
		sql = " INSERT INTO ENUMDEFVALUE VALUES ( :ENUMNAME, :NEWVERSION , :NEWDESCRIPTION, 'Y', NULL, NULL) ";

		bindMap.put("NEWVERSION", newVersion);
		bindMap.put("NEWDESCRIPTION", newDescription);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		return doc;
	}

}
