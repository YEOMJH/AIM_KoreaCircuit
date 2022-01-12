package kr.co.aim.messolution.processgroup.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.processgroup.management.info.AssignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.AssignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.DeassignSuperProcessGroupInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.processgroup.management.info.MakeWaitingToLoginInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetAreaInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.management.info.UndoInfo;

public class ProcessGroupServiceImpl implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("ProcessGroupServiceImpl");

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void assignMaterials(ProcessGroup processGroupData, AssignMaterialsInfo assignMaterialsInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().assignMaterials(processGroupData.getKey(), eventInfo, assignMaterialsInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void assignSuperProcessGroup(ProcessGroup processGroupData, AssignSuperProcessGroupInfo assignSuperProcessGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().assignSuperProcessGroup(processGroupData.getKey(), eventInfo, assignSuperProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void changeSpec(ProcessGroup processGroupData, ChangeSpecInfo changeSpecInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().changeSpec(processGroupData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void create(ProcessGroup processGroupData, CreateInfo createInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().create(processGroupData.getKey(), eventInfo, createInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void deassignMaterials(ProcessGroup processGroupData, DeassignMaterialsInfo deassignMaterialsInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials(processGroupData.getKey(), eventInfo, deassignMaterialsInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void deassignSuperProcessGroup(ProcessGroup processGroupData, DeassignSuperProcessGroupInfo deassignSuperProcessGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().deassignSuperProcessGroup(processGroupData.getKey(), eventInfo, deassignSuperProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeCompleted(ProcessGroup processGroupData, MakeCompletedInfo makeCompletedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeCompleted(processGroupData.getKey(), eventInfo, makeCompletedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeIdle(ProcessGroup processGroupData, MakeIdleInfo makeIdleInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeIdle(processGroupData.getKey(), eventInfo, makeIdleInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeInRework(ProcessGroup processGroupData, MakeInReworkInfo makeInReworkInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeInRework(processGroupData.getKey(), eventInfo, makeInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeLoggedIn(ProcessGroup processGroupData, MakeLoggedInInfo makeLoggedInInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeLoggedIn(processGroupData.getKey(), eventInfo, makeLoggedInInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeLoggedOut(ProcessGroup processGroupData, MakeLoggedOutInfo makeLoggedOutInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeLoggedOut(processGroupData.getKey(), eventInfo, makeLoggedOutInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeNotOnHold(ProcessGroup processGroupData, MakeNotOnHoldInfo makeNotOnHoldInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeNotOnHold(processGroupData.getKey(), eventInfo, makeNotOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeOnHold(ProcessGroup processGroupData, MakeOnHoldInfo makeOnHoldInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeOnHold(processGroupData.getKey(), eventInfo, makeOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeProcessing(ProcessGroup processGroupData, MakeProcessingInfo makeProcessingInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeProcessing(processGroupData.getKey(), eventInfo, makeProcessingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeReceived(ProcessGroup processGroupData, MakeReceivedInfo makeReceivedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeReceived(processGroupData.getKey(), eventInfo, makeReceivedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeShipped(ProcessGroup processGroupData, MakeShippedInfo makeShippedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeShipped(processGroupData.getKey(), eventInfo, makeShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeTraveling(ProcessGroup processGroupData, MakeTravelingInfo makeTravelingInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeTraveling(processGroupData.getKey(), eventInfo, makeTravelingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeWaitingToLogin(ProcessGroup processGroupData, MakeWaitingToLoginInfo makeWaitingToLoginInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().makeWaitingToLogin(processGroupData.getKey(), eventInfo, makeWaitingToLoginInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setArea(ProcessGroup processGroupData, SetAreaInfo setAreaInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().setArea(processGroupData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setEvent(ProcessGroup processGroupData, SetEventInfo setEventInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().setEvent(processGroupData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void undo(ProcessGroup processGroupData, UndoInfo undoIfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProcessGroupServiceProxy.getProcessGroupService().undo(processGroupData.getKey(), eventInfo, undoIfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public int update(ProcessGroup processGroupData)
	{
		int groupResult = ProcessGroupServiceProxy.getProcessGroupService().update(processGroupData);
		return groupResult;
	}

	public void insertHistory(ProcessGroup oldProcessGroupData, ProcessGroup processGroupData)
	{
		ProcessGroupHistory HistoryData = new ProcessGroupHistory();
		ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroupData, processGroupData, HistoryData);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(HistoryData);
	}
}
