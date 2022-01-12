package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class UpdatePrintSequence extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lastUpdateSequence = SMessageUtil.getBodyItemValue(doc, "LASTPRINTSEQUENCE", true);
		String code = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", true);

		String sql1 = "UPDATE CT_MATERIALLABEL SET LASTPRINTSEQUENCE = :LASTPRINTSEQUENCE WHERE MATERIALCODE = :MATERIALCODE";
		Map<String, Object> bindMap1 = new HashMap<String, Object>();
		bindMap1.put("LASTPRINTSEQUENCE", lastUpdateSequence);
		bindMap1.put("MATERIALCODE", code);

		GenericServiceProxy.getSqlMesTemplate().update(sql1, bindMap1);

		return doc;
	}

}
