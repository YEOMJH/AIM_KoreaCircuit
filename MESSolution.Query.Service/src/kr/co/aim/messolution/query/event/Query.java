package kr.co.aim.messolution.query.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.query.MESQueryServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class Query extends SyncHandler {
	public static Log logger = LogFactory.getLog(Query.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		eventLog.info(String.format("[%s] started", getClass().getSimpleName()));

		String messageName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "MESSAGENAME");
		String sourceSubjectName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "ORIGINALSOURCESUBJECTNAME");
		String transactionId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TRANSACTIONID");
		String queryId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "QUERYID");
		String version = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "VERSION");

		Element bindElement = null;
		try
		{
			bindElement = XmlUtil.getNode(doc, "//Message/Body/BINDV");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Element conditionElement = null;
		try
		{
			conditionElement = XmlUtil.getNode(doc, "//Message/Body/BINDP");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String result = "";
		try
		{
			result = MESQueryServiceProxy.getQueryServiceImpl().getQueryResult(messageName, sourceSubjectName, sourceSubjectName, transactionId, queryId, version, conditionElement, bindElement);
		}
		catch (Exception e)
		{
			if (e instanceof CustomException)
			{
				throw (CustomException) e;
			}
			else
			{
				throw new CustomException("SYS-0000", e.getCause().getMessage());
			}
		}

		eventLog.info(String.format("[%s] finished", getClass().getSimpleName()));

		setUseFlagAndIncreaseUseCount(queryId, version);

		return result;
	}

	private void setUseFlag(String queryId, String version)
	{
		if (StringUtils.isNotEmpty(queryId) && StringUtils.isNotEmpty(version))
		{
			try
			{
				String sql = "UPDATE CT_CUSTOMQUERY SET USEFLAG = 'Y', USETIMEKEY = :USETIMEKEY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";
				Map<String, String> args = new HashMap<String, String>();
				args.put("QUERYID", queryId);
				args.put("VERSION", version);
				args.put("USETIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
			catch (Exception e)
			{
			}
		}
	}

	private void setUseFlagAndIncreaseUseCount(String queryId, String version)
	{
		if (StringUtils.isNotEmpty(queryId) && StringUtils.isNotEmpty(version))
		{
			try
			{
				Object[] keySet = new Object[] { queryId, version };
				//String lockSql = "SELECT QUERYID, VERSION FROM CT_CUSTOMQUERY WHERE QUERYID = :QUERYID AND VERSION = :VERSION FOR UPDATE";
				String lockSql = "SELECT QUERYID, VERSION FROM CT_CUSTOMQUERY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";
				List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(lockSql, keySet);
				/*
				logger.info("CustomQuery [" + queryId + ", " + version + "] was queried and its use count will be increased.");

				String sql = "UPDATE CT_CUSTOMQUERY SET USEFLAG = 'Y', USECOUNT = USECOUNT+1, USETIMEKEY = :USETIMEKEY WHERE QUERYID = :QUERYID AND VERSION = :VERSION ";
				Map<String, String> args = new HashMap<String, String>();
				args.put("QUERYID", queryId);
				args.put("VERSION", version);
				args.put("USETIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
				logger.info("CustomQuery [" + queryId + ", " + version + "]'s use count has been increased.");
				*/
			}
			catch (Exception e)
			{
			}
		}
	}
}
