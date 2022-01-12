package kr.co.aim.messolution.transportjob.event;

import java.util.Arrays;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class GetMaskDataRequest extends AsyncHandler {

	private static Log log = LogFactory.getLog(GetMaskDataRequest.class);
	
	/**
	 * MessageSpec [TEX -> MCS]
	 * 
	 * <Body>
	 *    <MASKNAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);

		try
		{
			this.checkCondition(doc);
			
			// Set EventComment as MessageName
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);

			// Send Message to MCS
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendBySender(replySubject, doc, "HIFSender");
		}
		catch (Exception e)
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();

			if (StringUtils.isNotEmpty(originalSourceSubjectName))
			{
				// Reply NG to OIC
				GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), e, "OICSender");
			}

			throw new CustomException(e);
		}
	}
	
	/*
	 * Mantis : 0000411
	 * 若被同步Mask MachineName为 MaskCleaner、Tension、MaskUnpacker、EVA，Port不为空，且ProcessState为Run则禁止信息同步，
	 * 提示出账后再同步位置信息，防止Mask从卡匣流入机台作业，向MCS请求位置信息后，
	 * MCS上报CurrentSlotPosition为空，导致Mask所在CST Position更新为空，解绑Mask等均无法操作
	 */
	private void checkCondition(Document doc) throws CustomException
	{
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
		
		String machineName = maskLotData.getMachineName();
		String portName = maskLotData.getPortName();
		String lotProcessState = maskLotData.getMaskLotProcessState();
		
		String machineGroupName = StringUtils.EMPTY;
		//Mask Assigned CST Can't do Sync
		if(StringUtils.isNotEmpty(maskLotData.getCarrierName()))
		{
			throw new CustomException("MASK-3002");
		}
		
		try
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			machineGroupName = machineData.getMachineGroupName();			
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}
		
		String[] machineGroupNames = { GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner,
									   GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner,
									   GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker,
									   GenericServiceProxy.getConstantMap().MachineGroup_MaskTension,
									   GenericServiceProxy.getConstantMap().MachineGroup_EVA };
		
		if (StringUtils.isNotEmpty(machineGroupName) && Arrays.asList(machineGroupNames).contains(machineGroupName)||!StringUtils.isNotEmpty(machineName))
		{
			// Proceed to Synchronize again after TrackOut operation.
			throw new CustomException("MASK-3001");
		}
	}
}
