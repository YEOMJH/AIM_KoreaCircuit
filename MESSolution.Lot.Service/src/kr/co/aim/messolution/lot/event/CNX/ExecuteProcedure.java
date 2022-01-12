package kr.co.aim.messolution.lot.event.CNX;

import org.jdom.Document;

import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class ExecuteProcedure extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{
		String sProcedureName = SMessageUtil.getBodyItemValue(doc, "PROCEDURENAME", true);

		GenericServiceProxy.getSqlMesTemplate().executeProcedure(sProcedureName);

		return doc;
	}
}
