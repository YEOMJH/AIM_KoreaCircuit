package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DeptComment;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class CreateDeptComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String newComment = SMessageUtil.getBodyItemValue(doc, "NEWCOMMENT", true);
		long seq = getMaxSeq(factoryName, department);

		DeptComment dataInfo = new DeptComment();
		try
		{
			dataInfo = ExtendedObjectProxy.getDeptCommentService().selectByKey(false, new Object[] { factoryName, department, seq });
			dataInfo = null;
		}
		catch (Exception e)
		{
			dataInfo.setFactoryName(factoryName);
			dataInfo.setDepartment(department);
			dataInfo.setSeq(seq);
			dataInfo.setDeptComment(newComment);

			ExtendedObjectProxy.getDeptCommentService().insert(dataInfo);
		}

		if (dataInfo == null)
		{
			throw new CustomException("COMMENT-0001", seq);
		}

		return doc;
	}

	private long getMaxSeq(String factoryName, String department)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MAX (SEQ) AS SEQ ");
		sql.append("  FROM CT_DEPTCOMMENT ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND DEPARTMENT = :DEPARTMENT ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("DEPARTMENT", department);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		long seq = 0;

		if (sqlResult.size() > 0)
		{
			String maxSql = ConvertUtil.getMapValueByName(sqlResult.get(0), "SEQ");

			if (StringUtils.isEmpty(maxSql))
				maxSql = "0";

			seq = Long.parseLong(maxSql);
		}

		return seq + 1;
	}

}
