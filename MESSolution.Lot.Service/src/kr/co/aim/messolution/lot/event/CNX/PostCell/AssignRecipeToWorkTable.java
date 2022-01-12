package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class AssignRecipeToWorkTable extends SyncHandler {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(AssignRecipeToWorkTable.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignRecipeToWorkTable", eventUser.get(), eventComment.get(), "", "");
		List<Element> workTableListE = SMessageUtil.getBodySequenceItemList(doc, "WORKTALBELIST", true);

		for (Element workTableE : workTableListE) {
			String workTableId = SMessageUtil.getChildText(workTableE, "WORKTABLEID", true);
			String machineName = SMessageUtil.getChildText(workTableE, "MACHINENAME", false);
			String recipeName = SMessageUtil.getChildText(workTableE, "MACHINERECIPENAME", false);
			
			Durable workTableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(workTableId);
			if(!workTableData.getDurableState().equals("Available"))
			{
				throw new CustomException("MATERIAL-9018", workTableId, workTableData.getDurableState());
			}
			
			SetEventInfo setEventInfo = new SetEventInfo();
			
			setEventInfo.getUdfs().put("MACHINENAME", machineName);
			setEventInfo.getUdfs().put("MACHINERECIPENAME", recipeName);
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(workTableData, setEventInfo, eventInfo);
		}
		
		return doc;
	}

}
