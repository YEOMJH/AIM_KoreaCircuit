package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;

import org.jdom.Document;

public class CellCutTrackIn extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotIssueState(lotData);
		CommonValidation.checkMQCPlanState(lotName, "TrackIn");
		CommonValidation.CheckLotQuantity(lotData);
		MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

		// Validation MachineState
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.ChekcMachinState(eqpData);
		CommonValidation.checkMachineHold(eqpData);
		CommonValidation.CheckMachinState(eqpData);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);

		if (!recipeName.equals(machineRecipeName))
			throw new CustomException("MACHINE-0103", recipeName, machineName, "");

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		CommonValidation.CheckDurableHoldState(durableData);
		CommonValidation.CheckDurableCleanState(durableData);

		ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotName, machineName);
		ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(lotName);
		ExtendedObjectProxy.getProductQTimeService().exitQTimeByLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());

		//MESLotServiceProxy.getLotServiceUtil().checkReworkCountLimit(lotData);

		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);

		Map<String, String> lotUdfs = new HashMap<String, String>();
		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portSpecData.getPortType().toString());
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
		lotUdfs.put("MAINMACHINENAME", machineName);

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, lotUdfs);

		eventInfo.setEventName("TrackIn");
		MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

		try
		{
			MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureAction(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "1");
		}
		catch (CustomException e)
		{
			eventLog.info("Can't delete CtLotFutureAction:Abort Info not exist");
		}

		return doc;
	}
}
