package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class CheckOLEDMaskRequest extends SyncHandler {

	private static Log log = LogFactory.getLog(CheckOLEDMaskRequest.class);

	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, SMessageUtil.MessageName_Tag, "CheckOLEDMaskReply");
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);

			if (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker)
					|| !machineData.getUdfs().get("OPERATIONMODE").equals("UNPACKING"))
			
			{
				//MASK-0103: The message can be reported in UNPACKING mode on the MaskUnpacker machine.
				throw new CustomException("MASK-0103");
			}

			MaskLot maskLot = null;
			try
			{
				maskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			}
			catch (greenFrameDBErrorSignal dbError)
			{
				if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				{
					setResultItemValue(doc, "OK", "Mask is not registered with MES.");
					return doc;
				}
				else
				{
					throw new CustomException(dbError.getCause());
				}
			}
			catch (Exception ex)
			{
				throw new CustomException(ex.getCause());
			}

			// check maskSpec
			this.checkSpecExists(maskLotName);

			if (!maskLot.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
			{
				//MASK-0104: The mask state[{0}] to be marked is not Created
				throw new CustomException("MASK-0104",maskLot.getMaskLotState());
			}

			if (!maskLot.getFrameName().equals(frameName))
				throw new CustomException("FRAME-0001",String.format("Frame=%s,Mask=%s", frameName,maskLot.getMaskLotName()));
			
			setResultItemValue(doc, "OK","");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}
		return doc;
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
	
	private void checkSpecExists(String maskLotName) throws CustomException
	{
		String maskSpecName = maskLotName.substring(1, 3) + maskLotName.substring(9, 10);

		MaskSpec maskSpec = null;
		try
		{
			maskSpec = ExtendedObjectProxy.getMaskSpecService().selectByKey(false, new Object[] { "OLED", maskSpecName });
		}
		catch (Exception ex)
		{
			// COMM-1000:{0} Data Information is not registered.condition by [{1}].
			if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "CT_MASKSPEC", "MaskSpecName = " + maskSpecName);
			else
				throw new CustomException(ex.getCause());
		}
	} 
}
