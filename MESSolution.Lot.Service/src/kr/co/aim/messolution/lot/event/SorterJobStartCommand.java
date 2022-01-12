package kr.co.aim.messolution.lot.event;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class SorterJobStartCommand extends AsyncHandler {
	private Log log = LogFactory.getLog(SorterJobStartCommand.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		Element bodyElement  = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		SortJob jobData = null;
		try
		{
			jobData = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] { jobName });
		}
		catch (Exception ex)
		{
			CustomException customEx = null;

			if (ex instanceof NotFoundSignal)
			{
				customEx = new CustomException("COMM-1000", "CT_SORTJOB", "JOBName =" + jobName);
			}
			else
			{
				customEx = new CustomException("SYS-0010", ex.getMessage());
			}

			this.interruptDowork(customEx, doc);
		}
		
		if(!constMap.SORT_JOBSTATE_RESERVED.equals(jobData.getJobState()))
		{
			//SORT-0004:Can not do process ({0}) at Sort job state.[Current:{1} ->ChangeTo: {2} ]
			CustomException customEx = new CustomException("SORT-0004","changeSortJobState",jobData.getJobState(),"CONFIRM");
			this.interruptDowork(customEx, doc);
		}
		
		String transferDirection = jobData.getJobType().equals("SourceOnly")?constMap.SORT_TRANSFERDIRECTION_TARGET:constMap.SORT_TRANSFERDIRECTION_SOURCE;
		
		List<SortJobCarrier>  carrierDataList = null;
		try
		{
			carrierDataList = ExtendedObjectProxy.getSortJobCarrierService().select(" WHERE 1=1 AND JOBNAME = ? AND TRANSFERDIRECTION = ? ", new Object[]{jobName,transferDirection}); 
		}
		catch(Exception ex)
		{
			CustomException customEx = null;

			if (ex instanceof NotFoundSignal)
			{
				customEx = new CustomException("COMM-1000", "CT_SORTJOBCARRIER", "JOBName =" + jobName);
			}
			else
			{
				customEx = new CustomException("SYS-0010", ex.getMessage());
			}

			this.interruptDowork(customEx, doc);
		}
		
        Element formLotListE = new Element("FROMLOTLIST");
		for(SortJobCarrier carrierData : carrierDataList)
		{
			Element fromLotE = new Element("FROMLOT");
			fromLotE.addContent(new Element("LOTNAME").setText(carrierData.getLotName()));
			fromLotE.addContent(new Element("PORTNAME").setText(carrierData.getPortName()));
			fromLotE.addContent(new Element("CARRIERNAME").setText(carrierData.getCarrierName()));

			Element productListE = new Element("SORTERPRODUCTLIST");
			List<SortJobProduct> productList = ExtendedObjectProxy.getSortJobProductService().select(" WHERE 1=1 AND JOBNAME = ? AND FROMCARRIERNAME = ?  ", new Object[]{jobName,carrierData.getCarrierName()}); 
		    
			for(SortJobProduct productData : productList)
			{
				String pairProductName ="";
				Element productE = new Element("SORTERPRODUCT");
				productE.addContent(new Element("PRODUCTNAME").setText(productData.getProductName()));
				
				if (jobData.getJobType().equals("Merge (OLED to TP)"))
					pairProductName = this.getPairProduct(jobName, productData.getToCarrierName(), productData.getToPortName(), productData.getToPosition(), productData.getProductName());
				
				productE.addContent(new Element("PAIRPRODUCTNAME").setText(pairProductName));
				productE.addContent(new Element("FROMPOSITION").setText(productData.getFromPosition()));
				productE.addContent(new Element("FROMSLOTPOSITION").setText(productData.getFromSlotPosition()));
				productE.addContent(new Element("TOPORTNAME").setText(productData.getToPortName()));
				productE.addContent(new Element("TOCARRIERNAME").setText(productData.getToCarrierName()));
				productE.addContent(new Element("TOPOSITION").setText(productData.getToPosition()));
				productE.addContent(new Element("TOSLOTPOSITION").setText(productData.getToSlotPosition()));
				productE.addContent(new Element("TURNFLAG").setText(productData.getTurnFlag()));
				productE.addContent(new Element("TURNDEGREE").setText(productData.getTurnDegree()));
				productE.addContent(new Element("SCRAPFLAG").setText(productData.getScrapFlag()));
				productE.addContent(new Element("CUTFLAG").setText(productData.getCutFlag()));
				
				productListE.addContent(productE);
			}
			
			fromLotE.addContent(productListE);
			formLotListE.addContent(fromLotE);
		}
		
		bodyElement.addContent(formLotListE);
		
		// send message to EQP
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
		
		if (machineData.getCommunicationState().equals(constMap.Mac_OffLine))
		{
			this.interruptDowork(new CustomException("MACHINE-0003", machineData.getKey().getMachineName()), doc);
		}

		if (machineData.getMachineGroupName() != null && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
		{
			if (!machineData.getUdfs().get("OPERATIONMODE").equals(constMap.SORT_OPERATIONMODE_NORMAL)) 
			{
				this.interruptDowork(new CustomException("MACHINE-0100", machineData.getKey().getMachineName(),machineData.getUdfs().get("OPERATIONMODE")), doc);
			}
		}
		else 
		{
			if(!StringUtils.equals(machineData.getUdfs().get("OPERATIONMODE"), GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE))
			{
				this.interruptDowork(new CustomException("MACHINE-0100", machineData.getKey().getMachineName(),machineData.getUdfs().get("OPERATIONMODE")), doc);
			}
		}
		
		//send messsage to EQP
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		
		//record message log 
		GenericServiceProxy.getMessageTraceService().recordMessageLog(doc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
	}
	
	private String getPairProduct(String jobName,String toCarrierName,String toPortName,String toPosition,String productName ) throws CustomException 
	{
		List<SortJobProduct> productList =null;
		
		try
		{
			productList = ExtendedObjectProxy.getSortJobProductService().select(" WHERE 1=1 AND JOBNAME = ? AND TOCARRIERNAME = ? AND TOPORTNAME =? AND TOPOSITION = ? AND PRODUCTNAME <> ? ",
					                                                              new Object[] { jobName, toCarrierName, toPortName, toPosition, productName });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (ErrorSignal.NotFoundSignal.equals(dbError.getErrorCode()))
				log.info(String.format("▶ Couldn't find the product that pairs with %s product.",productName));
			else
				throw new CustomException(dbError);

		}
		catch (Exception ex)
		{
			throw new CustomException(ex);
		}
		
		if(productList ==null || productList.size()==0)return "";
		
		return productList.get(0).getProductName();
	}
	
	private void interruptDowork(CustomException customEx, Document doc) throws CustomException
	{
		String language = "English";

		try
		{
			language = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/" + "LANGUAGE");
		}
		catch (Exception ex)
		{
		}

		String errorCode = customEx.errorDef.getErrorCode();
		String errorMessage = customEx.errorDef.getEng_errorMessage();

		if ("Chinese".equals(language))
		{
			errorMessage = customEx.errorDef.getCha_errorMessage();
		}
		else if ("Korean".equals(language))
		{
			errorMessage = customEx.errorDef.getKor_errorMessage();
		}

		Element returnElement = doc.getRootElement().getChild(SMessageUtil.Return_Tag);

		if (returnElement == null)
		{
			returnElement = new Element(SMessageUtil.Return_Tag);
			returnElement.addContent(new Element(SMessageUtil.Result_ReturnCode));
			returnElement.addContent(new Element(SMessageUtil.Result_ErrorMessage));
			doc.getRootElement().addContent(returnElement);
		}

		returnElement.getChild(SMessageUtil.Result_ReturnCode).setText(errorCode);
		returnElement.getChild(SMessageUtil.Result_ErrorMessage).setText(errorMessage);

		// send to OIC
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");
		throw customEx;
	}

}
