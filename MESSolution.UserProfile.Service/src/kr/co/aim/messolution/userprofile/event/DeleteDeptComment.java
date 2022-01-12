package kr.co.aim.messolution.userprofile.event;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DeptComment;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;

public class DeleteDeptComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> commentList = SMessageUtil.getBodySequenceItemList(doc, "COMMENTLIST", true);

		long newSeq = 0;

		for (Element comment : commentList)
		{
			String factoryName = SMessageUtil.getChildText(comment, "FACTORYNAME", true);
			String department = SMessageUtil.getChildText(comment, "DEPARTMENT", true);
			String seq = SMessageUtil.getChildText(comment, "SEQ", true);
			String deleteRow = SMessageUtil.getChildText(comment, "DELETEROW", true);

			DeptComment dataInfo = new DeptComment();
			try
			{
				dataInfo = ExtendedObjectProxy.getDeptCommentService().selectByKey(false, new Object[] { factoryName, department, seq });
			}
			catch (Exception e)
			{
				dataInfo = null;
			}

			if (dataInfo != null)
			{
				if (StringUtils.equals(deleteRow, "Y"))
				{
					ExtendedObjectProxy.getDeptCommentService().delete(dataInfo);
				}
				else
				{
					newSeq += 1;

					if (Long.parseLong(seq) != newSeq)
					{
						DeptComment newDataInfo = (DeptComment) ObjectUtil.copyTo(dataInfo);
						newDataInfo.setSeq(newSeq);
						ExtendedObjectProxy.getDeptCommentService().updateToNew(dataInfo, newDataInfo);
					}
				}
			}
		}

		return doc;
	}

}
