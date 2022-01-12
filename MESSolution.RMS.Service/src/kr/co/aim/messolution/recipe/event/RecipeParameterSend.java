package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.messolution.recipe.service.RecipeServiceUtil;
import kr.co.aim.messolution.recipe.service.RecipeServiceUtil.SequenceParameter;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class RecipeParameterSend extends AsyncHandler {
	private Log logger = LogFactory.getLog(RecipeServiceUtil.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

//		MACHINENAME 
//		PORTNAME
//		CARRIERNAME
//		RECIPENAME
//		RECIPETYPE
//		EQPNAME
//		EQPRECIPENAME
//		PARAMETERLIST
	//		PARAMETER
		//		PARAMETERNAME
		//		VALUE


		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String eqpName = SMessageUtil.getBodyItemValue(doc, "EQPNAME", true);
		String eqpRecipeName = SMessageUtil.getBodyItemValue(doc, "EQPRECIPENAME", true);
		
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element parameterList = bodyElement.getChild("PARAMETERLIST");

		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
	
		doc = MESRecipeServiceProxy.getRecipeServiceUtil().validateRecipeParameter(parameterList, machineName, portName, carrierName, recipeName, 
				recipeType, eqpName, eqpRecipeName);
		
		//MES-EAP protocol
		SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
		
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}

}
