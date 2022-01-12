package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalSheetDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.OrderedMap;
import org.jdom.Document;

public class AbnormalSheetAttachFile extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String abnormalSheetName = SMessageUtil.getBodyItemValue(doc, "ABNORMALSHEETNAME", true);
		String fileName = SMessageUtil.getBodyItemValue(doc, "FILENAME", true);
		String fileType = SMessageUtil.getBodyItemValue(doc, "FILETYPE", false);
		String fileSize = SMessageUtil.getBodyItemValue(doc, "FILESIZE", false);
		String filePath = SMessageUtil.getBodyItemValue(doc, "FILEPATH", false);
		String attachComment = SMessageUtil.getBodyItemValue(doc, "ATTACHCOMMENT", false);
		String aType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		if (aType.equals("Insert"))
		{
			eventInfo.setEventName("Insert Attach File.");
			eventInfo.setEventComment("Insert Attach File.");

			InsertAttachFile(abnormalSheetName, fileName, fileType, fileSize, filePath, attachComment, eventInfo);
		}
		else if (aType.equals("Delete"))
		{
			eventInfo.setEventName("Delete Attach File.");
			eventInfo.setEventComment("Delete Attach File.");

			DeleteAttachFile(abnormalSheetName, fileName);
		}

		return doc;
	}

	@SuppressWarnings("unchecked")
	private void InsertAttachFile(String abnormalSheetName, String fileName, String fileType, String fileSize, String filePath, String attachComment, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String sql = "SELECT DISTINCT PRODUCTNAME "
					   + "FROM CT_ABNORMALSHEETDETAIL "
					   + "WHERE ABNORMALSHEETNAME = ? ";
			
			List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { abnormalSheetName });
			if (resultDataList != null && resultDataList.size() > 0)
			{
				List<Object[]> bindSetList = new ArrayList<>();
				
				for (OrderedMap resultData : resultDataList) 
				{
					Object[] bindSet = new Object[] { abnormalSheetName, resultData.get("PRODUCTNAME").toString(), fileName, fileType, fileSize, filePath, 
							attachComment, eventInfo.getEventUser(), eventInfo.getEventTime() };
					
					bindSetList.add(bindSet);
				}
				
				sql = "INSERT INTO CT_ABNORMALSHEETATTACHFILE "
						+ "(ABNORMALSHEETNAME, PRODUCTNAME, FILENAME, FILETYPE, FILESIZE, FILEPATH, ATTACHCOMMENT, LASTEVENTUSER, LASTEVENTTIME) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
				
				GenericServiceProxy.getSqlMesTemplate().updateBatch(sql, bindSetList);				
			}
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + fileName + " into CT_ABNORMALSHEETATTACHFILE   Error : " + e.toString());
		}
	}

	private void DeleteAttachFile(String abnormalSheetName, String fileName) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM CT_ABNORMALSHEETATTACHFILE ");
			sql.append(" WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME ");
			sql.append("   AND FILENAME = :FILENAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", abnormalSheetName);
			bindMap.put("FILENAME", fileName);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for delete " + fileName + " from the CT_ABNORMALSHEETATTACHFILE   Error : " + e.toString());
		}
	}
}
