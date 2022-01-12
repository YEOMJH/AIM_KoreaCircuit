package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class AssignOperaDepart extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignOperaDepart", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		Element eleBody = SMessageUtil.getBodyElement(doc);
		String sql = " UPDATE PROCESSOPERATIONSPEC SET DEPARTMENT = ? WHERE FACTORYNAME = ? AND PROCESSOPERATIONNAME = ? ";
		
		StringBuilder sqlHis = new StringBuilder();
		sqlHis.append(" INSERT INTO PROCESSOPERATIONSPECHISTORY (FACTORYNAME, ");
		sqlHis.append("   PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, TIMEKEY, EVENTTIME, PROCESSOPERATIONTYPE, ");
		sqlHis.append("   DETAILPROCESSOPERATIONTYPE, PROCESSOPERATIONGROUP, PROCESSOPERATIONUNIT, ISLOGINREQUIRED, ");
		sqlHis.append("   DEFAULTAREANAME, EVENTUSER, EVENTCOMMENT, LAYERNAME, DEPARTMENT, ISMAINOPERATION, CHANGELOTNAME, MAINLAYERSTEP, EVENTNAME) ");
		sqlHis.append("  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		
		List<Object[]> updateArgsList = new ArrayList<Object[]>();
		List<Object[]> updateArgsHis = new ArrayList<Object[]>();
		
		if (eleBody != null)
		{
			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "OPERLIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);
				String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
				String department = SMessageUtil.getChildText(eleLot, "DEPARTMENT", true);

				ProcessOperationSpec operSpec = CommonUtil.getProcessOperationSpec(factoryName, processOperationName, "00001");
				
				List<Object> updateBindList = new ArrayList<Object>();
				updateBindList.add(department);
				updateBindList.add(factoryName);
				updateBindList.add(processOperationName);
				updateArgsList.add(updateBindList.toArray());
				
				List<Object> updateBindHis = new ArrayList<Object>();
				updateBindHis.add(factoryName);
				updateBindHis.add(processOperationName);
				updateBindHis.add("00001");
				updateBindHis.add(eventInfo.getEventTimeKey());
				updateBindHis.add(eventInfo.getEventTime());
				updateBindHis.add(operSpec.getProcessOperationType());
				updateBindHis.add(operSpec.getDetailProcessOperationType());
				updateBindHis.add(operSpec.getProcessOperationGroup());
				updateBindHis.add(operSpec.getProcessOperationUnit());
				updateBindHis.add(operSpec.getIsLogInRequired());
				updateBindHis.add(operSpec.getDefaultAreaName());
				updateBindHis.add(eventInfo.getEventUser());
				updateBindHis.add(eventInfo.getEventComment());
				updateBindHis.add(operSpec.getUdfs().get("LAYERNAME").toString());
				updateBindHis.add(department);
				updateBindHis.add(operSpec.getUdfs().get("ISMAINOPERATION").toString());
				updateBindHis.add(operSpec.getUdfs().get("CHANGELOTNAME").toString());
				updateBindHis.add(operSpec.getUdfs().get("MAINLAYERSTEP").toString());
				updateBindHis.add(eventInfo.getEventName());
				updateArgsHis.add(updateBindHis.toArray());
				
			}
		}
		
		if(updateArgsList.size()>0){
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql.toString(), updateArgsList);
		}
		
		if(updateArgsHis.size()>0){
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlHis.toString(), updateArgsHis);
		}

		return doc;
	}

}
