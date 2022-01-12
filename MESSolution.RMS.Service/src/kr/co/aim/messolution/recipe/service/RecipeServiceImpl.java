package kr.co.aim.messolution.recipe.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.springframework.context.support.MessageSourceSupport;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.extended.object.management.impl.POSAlterProcessOperationService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;

public class RecipeServiceImpl {

	private Log logger = LogFactory.getLog(RecipeServiceImpl.class);
	
	
	public Document inquiryRecipeList(String machineName, String portName, String carrierName)
		throws CustomException
	{
		try
		{
			MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
			MachineSpec rootMachineData = MESRecipeServiceProxy.getRecipeServiceUtil().getRootMachine(machineName);
			
			String recipeType = "";
			

			if (machineData.getDetailMachineType().equals("MAIN"))
				recipeType = "E";
			else if (machineData.getDetailMachineType().equals("UNIT"))
				recipeType = "U";
			else
				recipeType = "S";
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeInquiry(machineName, recipeType, portName, carrierName);
			//Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeInquiry(rootMachineData.getKey().getMachineName(), portName, carrierName, machineName, recipeType);
			
			//bypass for TIB rv protocol
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");//RMSsvr
			//doc = GenericServiceProxy.getESBServive().sendRequestBySender(targetSubject, doc, "PEMSender");// RMSSender 
			doc = GenericServiceProxy.getESBServive().sendRequestBySenderTimeOut(targetSubject, doc, "PEMSender", 45000);

			return doc;
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
			
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	
	public Document inquiryRecipeList(String machineName, String portName, String carrierName, String subjectName)
			throws CustomException {
		try {
			MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			MachineSpec rootMachineData = MESRecipeServiceProxy.getRecipeServiceUtil().getRootMachine(machineName);

			String recipeType = "";

			if (machineData.getDetailMachineType().equals("MAIN"))
				recipeType = "E";
			else if (machineData.getDetailMachineType().equals("UNIT"))
				recipeType = "U";
			else
				recipeType = "S";

			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeInquiry(machineName, recipeType, portName, carrierName);
			// Document doc =
			// MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeInquiry(rootMachineData.getKey().getMachineName(),
			// portName, carrierName, machineName, recipeType);

			// bypass for TIB rv protocol
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");// RMSsvr
			//String targetSubject = subjectName;
			//doc = GenericServiceProxy.getESBServive().sendRequest(targetSubject, doc);
			//doc = GenericServiceProxy.getESBServive().sendRequestBySender(targetSubject, doc, "PEMSender");// RMSSender
			doc = GenericServiceProxy.getESBServive().sendRequestBySenderTimeOut(targetSubject, doc, "PEMSender", 45000);// RMSSender

			return doc;
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception ex) {
			logger.error(ex);
		
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	
	
	public Document inquiryParameterList(String machineName, String portName, String carrierName, String recipeName) throws CustomException
	{
		try
		{
			MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
			MachineSpec rootMachineData = MESRecipeServiceProxy.getRecipeServiceUtil().getRootMachine(machineName);
			
			String recipeType = "";
			
			if (machineData.getDetailMachineType().equals("MAIN"))
				recipeType = "E";
			else if (machineData.getDetailMachineType().equals("UNIT"))
				recipeType = "U";
			else
				recipeType = "S";
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateParameterInquiryRequest(rootMachineData.getKey().getMachineName(), machineName, recipeName, recipeType, portName, carrierName);
			
			//bypass for TIB rv protocol
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");
			
			//doc = GenericServiceProxy.getESBServive().sendRequestBySender(targetSubject, doc, "PEMSender");
			doc = GenericServiceProxy.getESBServive().sendRequestBySenderTimeOut(targetSubject, doc, "PEMSender", 45000);
			logger.info(doc);
			
			return doc;
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
			
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	
	public void RecipeCheckStartReply(String machineName, String portName, String carrierName, String returnCode, String returnMsg) throws CustomException
	{
		try
		{ 
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			RecipeCheckResult recipeResult = ExtendedObjectProxy.getRecipeCheckResultService().selectByKey(false, new Object[]{machineName, portName});
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineName, portName, portData.getUdfs().get("PORTTYPE"), portData.getUdfs().get("PORTUSETYPE"), carrierName, recipeResult.getOriginalSubjectName());
			String targetSubject = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
			String eventUser = machineName;
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", eventUser);
			SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", portData.getFactoryName());
			SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
			SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
			SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubject);
			// bypass for TIB rv protocol

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "EISSender", returnCode, returnMsg);
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
			
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	
	public void RecipeCheckStartReplyNotCheck(String machineName, String portName, String carrierName, String returnCode, String returnMsg, String originalSubjectName ) throws CustomException
	{
		try
		{
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineName, portName, portData.getUdfs().get("PORTTYPE"), portData.getUdfs().get("PORTUSETYPE"), carrierName, originalSubjectName);
			
			// bypass for TIB rv protocol
			String targetSubject = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
			String eventUser = machineName;
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", eventUser);
			SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", portData.getFactoryName());
			SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
			SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
			SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubject);
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "EISSender", returnCode, returnMsg);
			
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
			
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	/*
	public void RecipeCheckStartReply(String machineName, String portName, String carrierName, String returnCode, String returnMsg, String originalSubjectName) throws CustomException
	{
		try
		{	
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineName, portName, portData.getUdfs().get("PORTTYPE"), portData.getUdfs().get("PORTUSETYPE"), carrierName, originalSubjectName);

			// bypass for TIB rv protocol
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "PEMSender", returnCode, returnMsg);
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
			
		   //RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	*/
	public void sendMainRecipeRequest(String machineName, String portName, String carrierName) throws CustomException
	{
		try
		{
			MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			String recipeType = "";

			if (machineData.getDetailMachineType().equals("MAIN"))
				recipeType = "E";
			else if (machineData.getDetailMachineType().equals("UNIT"))
				recipeType = "U";
			else
				recipeType = "S";

			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateMainRecipeRequest(machineName, recipeType, portName, carrierName);

			// bypass for TIB rv protocol
			String eventUser = machineData.getKey().getMachineName().substring(0, 6);
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", eventUser);
			
			String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
			SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", machineData.getFactoryName());
			SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
			SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
			SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
			//String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");// RMSsvr
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);
		
			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
	
	public void sendRecipeParameterRequest(String machineName, String portName, String carrierName, String recipeName) throws CustomException
	{
		try
		{
			MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
			MachineSpec rootMachineData = MESRecipeServiceProxy.getRecipeServiceUtil().getRootMachine(machineName);

			String recipeType = "";

			if (machineData.getDetailMachineType().equals("MAIN"))
				recipeType = "E";
			else if (machineData.getDetailMachineType().equals("UNIT"))
				recipeType = "U";
			else
				recipeType = "S";
			
			Document doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateParameterRequest(rootMachineData.getKey().getMachineName(), machineName, recipeName, recipeType, portName, carrierName);
			String eventUser = machineData.getKey().getMachineName().substring(0, 6);
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", eventUser);
			
			// bypass for TIB rv protocol
			String targetSubject = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
			SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", machineData.getFactoryName());
			SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true));
			SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
			SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubject);
			
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "EISSender");
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			logger.error(ex);

			//RMS-014: RMSError:Communication failed with Machine[{0}]
			throw new CustomException("RMS-014", machineName);
		}
	}
}
