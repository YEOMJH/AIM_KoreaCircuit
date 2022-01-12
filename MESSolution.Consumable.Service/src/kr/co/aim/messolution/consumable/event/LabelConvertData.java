package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.jdom.Document;
import org.jdom.Element;

public class LabelConvertData extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> eleCrateList = SMessageUtil.getBodySequenceItemList(doc, "CRATELIST", true);

		for (Element eleCrate : eleCrateList)
		{
			String materialNumber = SMessageUtil.getChildText(eleCrate, "MATERIALNUMBER", true);
			String materialDescription = SMessageUtil.getChildText(eleCrate, "MATERIALDESCRIPTION", true);
			String factoryName = SMessageUtil.getChildText(eleCrate, "FACTORYNAME", true);
			String createUser = SMessageUtil.getChildText(eleCrate, "CREATEUSER", true);

			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO CT_MATERIALLABELDATA  ");
			sql.append("  (MATERIALNUMBER, MATERIALDESCRIPTION, FACTORYNAME, CREATEUSER, CREATETIME) ");
			sql.append("VALUES  ");
			sql.append("  (:MATERIALNUMBER, :MATERIALDESCRIPTION, :FACTORYNAME, :CREATEUSER, :CREATETIME) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("MATERIALNUMBER", materialNumber);
			bindMap.put("MATERIALDESCRIPTION", materialDescription);
			bindMap.put("CREATEUSER", createUser);
			bindMap.put("CREATETIME", TimeStampUtil.getCurrentEventTimeKey());

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}

		return doc;
	}
}
