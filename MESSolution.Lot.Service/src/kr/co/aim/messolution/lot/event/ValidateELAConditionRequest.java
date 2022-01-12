package kr.co.aim.messolution.lot.event;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ELACondition;
import kr.co.aim.messolution.extended.object.management.impl.ELAConditionService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

public class ValidateELAConditionRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(ValidateELAConditionRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		try
		{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String uc = SMessageUtil.getBodyItemValue(doc, "UC", true);
			String gasUseAmount = SMessageUtil.getBodyItemValue(doc, "GASUSEAMOUNT", true);
			String laserFrequency = SMessageUtil.getBodyItemValue(doc, "LASERFREQUENCY", true);
			String tackTime = SMessageUtil.getBodyItemValue(doc, "TACTTIME", true);
			String glassQty = SMessageUtil.getBodyItemValue(doc, "GLASSQTY", true);

			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ValidateELAConditionReply");

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
			String elaRunTableSwitch=CommonUtil.getEnumDefValueStringByEnumName("ELARunTableSwitch");
			
			if(StringUtil.equals(elaRunTableSwitch, "True"))
			{
				if (StringUtil.in(lotData.getProductionType(), "P", "E"))
				{
					double dUC = Double.parseDouble(uc);
					double dGasUseAmount = Double.parseDouble(gasUseAmount);
					double dLaserFrequency = Double.parseDouble(laserFrequency);
					double dTackTime = Double.parseDouble(tackTime);
					double dGlassQty = Double.parseDouble(glassQty);
					double dLotProductQuantity = lotData.getProductQuantity();

					// CUSTOM-0014:The laser life is over the limit range, please confirm.
					if (!checkLaserLifeTime(machineName,lotData.getProductSpecName(), dUC, dGasUseAmount, dLaserFrequency, dTackTime, dGlassQty, dLotProductQuantity))
						throw new CustomException("CUSTOM-0014");
				}
			}
			//Insert into Lot values：ELANFC yueke 20210702 by ELA JC
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("InsertELANFC", this.getEventUser(), "Insert ELA NFC:"+gasUseAmount, "", "");
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("ELANFC", gasUseAmount);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo,setEventInfo);
			
			setResultItemValue(doc, "OK", "");
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
	
	private boolean checkLaserLifeTime(String machineName,String productSpecName, double uc , double gasUseAmount,double laserFrequency,double tackTime,double glassQty,double lotGlassQty) throws CustomException
	{
		boolean checkFlag = false ;
		
		double dSpecValue =getUCSpecValue(machineName,productSpecName,uc).doubleValue();
		
		//CUSTOM-0015:UC value is less than NFC value.
		if (dSpecValue < gasUseAmount)
			throw new CustomException("CUSTOM-0015");
		
		//validation by formula (UCUpperValue * 1000000 - GlassAmount * 1000000) /LaserFrequency /TackTime - GlassQty > LotProductQuntity
        if((dSpecValue   -gasUseAmount ) /laserFrequency /tackTime - glassQty >lotGlassQty)
        	return true;
		
		return checkFlag;
	}
	
	private Number getUCSpecValue(String machineName,String productSpecName, double uc) throws CustomException
	{
		ELACondition dataInfo  = null;
		
		try
		{
		    dataInfo = ExtendedObjectProxy.getELAConditionService().selectByKey(false, new Object[] { machineName, productSpecName });
		}
		catch (Exception ex)
		{
           if((ex instanceof greenFrameDBErrorSignal) && ((greenFrameDBErrorSignal)ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
        	   throw new CustomException("COMM-1000","CT_ELACONDITION",String.format("MachineName=%s,ProductSpecName=%s", machineName,productSpecName));
           else 
        	   throw new CustomException(ex.getCause());
		}
		
		Number returnValue = null;
		if (dataInfo.getUc1() != null && uc <= dataInfo.getUc1().doubleValue())
		{
			returnValue = dataInfo.getUc1Value();
			log.info("Gets the value of UC1.");
		}
		else if (!hasNull(dataInfo.getUc1(), dataInfo.getUc2()) && dataInfo.getUc1().doubleValue() < uc && uc <= dataInfo.getUc2().doubleValue())
		{
			returnValue = dataInfo.getUc2Value();
			log.info("Gets the value of UC2.");
		}
		else if (!hasNull(dataInfo.getUc2(), dataInfo.getUc3()) && dataInfo.getUc2().doubleValue() < uc && uc <= dataInfo.getUc3().doubleValue())
		{
			returnValue = dataInfo.getUc3Value();
			log.info("Gets the value of UC3.");
		}
		else if (!hasNull(dataInfo.getUc3(), dataInfo.getUc4()) && dataInfo.getUc3().doubleValue() < uc && uc <= dataInfo.getUc4().doubleValue())
		{
			returnValue = dataInfo.getUc4Value();
			log.info("Gets the value of UC4.");
		}
		else if (!hasNull(dataInfo.getUc4(), dataInfo.getUc5()) && dataInfo.getUc4().doubleValue() < uc && uc <= dataInfo.getUc5().doubleValue())
		{
			returnValue = dataInfo.getUc5Value();
			log.info("Gets the value of UC5.");
		}
		else if (!hasNull(dataInfo.getUc5(), dataInfo.getUc6()) && dataInfo.getUc5().doubleValue() < uc && uc <= dataInfo.getUc6().doubleValue())
		{
			returnValue = dataInfo.getUc6LowerValue();
			log.info("Gets the value of UC6 Lower.");
		}
		else if (dataInfo.getUc6() != null && uc > dataInfo.getUc6().doubleValue())
		{
			returnValue = dataInfo.getUc6UpperValue();
			log.info("Gets the value of UC6 Upper.");
		}
		else
		{
			log.info("No UC data matching the comparison criteria was found.");
		}
		
		//CUSTOM-0016:{0} table information registration error.
		if(returnValue == null ) throw new CustomException("CUSTOM-0016", "CT_EALCONDITION");
		
		return returnValue;
	}
	
	private boolean hasNull(Object... args)
	{
		for (Object compValue : args)
		{
			if (compValue == null)
				return true;
		}
		return false;
	}

	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
}