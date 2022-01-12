package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

public class AllUnitReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <UNITLIST>
	 *       <UNIT>
	 *          <UNITNAME />
	 *          <UNITSTATE />
	 *       </UNIT>
	 *    </UNITLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", true);

		for (Element unitElement : unitList)
		{
			String machineName = SMessageUtil.getChildText(unitElement, "UNITNAME", true);
			String machineStateName = SMessageUtil.getChildText(unitElement, "UNITSTATE", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			try
			{
				// if MACHINESTATAE is same, Pass
				if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
				{
					continue;
				}

				MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
				MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
			}
			catch (CustomException ex)
			{
				eventLog.warn(ex.getLocalizedMessage());
			}
		}
	}
}
