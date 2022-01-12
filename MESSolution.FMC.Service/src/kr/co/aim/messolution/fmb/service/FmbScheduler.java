package kr.co.aim.messolution.fmb.service;

import java.util.List;

import kr.co.aim.messolution.fmb.service.impl.Schedule;

public interface FmbScheduler {

	void setNamespaceURI(String namespaceURI);

	void setScheduleList(List<Schedule> scheduleList);

}
