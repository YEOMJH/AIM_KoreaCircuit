package kr.co.aim.messolution.lot.event.Tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class CreateTrayRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(CreateTrayRequest.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CreateTrayReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

			// check tray exist : NG , Unregistered : OK
			this.checkIsUnregisteredTray(trayName);

			// check spec by namingrule
			String subStrSpecFromId = trayName.substring(0,7);
			DurableSpec specData = this.checkIsTraySpec(machineData.getFactoryName(), subStrSpecFromId);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment());
			CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(trayName, specData.getKey().getDurableSpecName(), "", 
																						    Long.toString(specData.getDefaultCapacity()),machineData.getFactoryName());
			createInfo.getUdfs().put("DURABLETYPE1", "Tray");

			// create tray
			MESDurableServiceProxy.getDurableServiceImpl().create(trayName, createInfo, eventInfo);

			setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception ex)
		{
			setResultItemValue(doc, "NG", ex.getMessage());
			throw new CustomException(ex.getCause());
		}

		return doc;
	}
	
	private void checkIsUnregisteredTray(String trayName) throws  CustomException
	{
		try
		{
			DurableServiceProxy.getDurableService().selectByKey(new DurableKey(trayName));
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format(" No tray [%s] information registered on durable table.", trayName));
			return;
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		//TRAY-0020: Already registered durable [{0}] information.
		throw new CustomException("TRAY-0020",trayName);
	}
	
	private DurableSpec checkIsTraySpec(String factoryName,String specName) throws CustomException
	{
		String specVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
		
		DurableSpecKey keyInfo = new DurableSpecKey();
		keyInfo.setFactoryName(factoryName);
		keyInfo.setDurableSpecName(specName);
		keyInfo.setDurableSpecVersion(specVersion);
		
		DurableSpec dataInfo = null;
		try
		{
			dataInfo = 	DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);
		}
		catch (NotFoundSignal notFoundEx)
		{
			throw new CustomException("COMM-1000", "DurableSpec",String.format("FactoryName = %s , SpecName = %s" ,  factoryName,specName));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if(!"Tray".equals(dataInfo.getDurableType()))
		{
			//TRAY-0021: Spec [{0}] type [{1}] is not Tray.
			throw new CustomException("TRAY-0021",specName,dataInfo.getDurableType());
		}
		
		return dataInfo;
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
}
