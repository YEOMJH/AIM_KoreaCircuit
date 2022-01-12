package kr.co.aim.messolution.durable.event.IMS;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhotoMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class IMSMaskMapDownloadRequest extends SyncHandler 
{
	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "IMSMaskMapDownloadReply");
			String lineName = SMessageUtil.getBodyItemValue(doc, "LINENAME", true);

			doc.getRootElement().getChild(SMessageUtil.Body_Tag).addContent(new Element("MASKLIST"));

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);
			CommonValidation.checkMachineHold(machineData);

//			List<PhotoMaskStocker> maskStockerDataList = ExtendedObjectProxy.getPhotoMaskStockerService().getDataInfoByLineName(lineName);
//
//			if (maskStockerDataList != null && maskStockerDataList.size() > 0)
//			{
//				for (PhotoMaskStocker maskStockerData : maskStockerDataList)
//				{
//					if (!maskStockerData.getMaskName().isEmpty())
//					{
//						this.setMaskElement(doc, maskStockerData.getMaskName());
//					}
//					else
//					{
//						log.info(String.format("Empty Slot Info: StockerName [%s], SlotId [%s] , SlotState [%s] .",
//																 maskStockerData.getMaskStockerName(), maskStockerData.getSlotId(),maskStockerData.getSlotStatus()));
//					}
//				}
//			}
			
			List<Map<String, Object>> maskMapList = this.getMaskListByLineName(lineName);

			for (Map<String, Object> maskMap : maskMapList)
			{
				Element maskElement = new Element("MASK");

				maskElement.addContent(new Element("MASKID").setText(ConvertUtil.getMapValueByName(maskMap, "DURABLENAME")));
				maskElement.addContent(new Element("VENDOR").setText(ConvertUtil.getMapValueByName(maskMap, "VENDOR")));
				maskElement.addContent(new Element("MASKTSTATE").setText(ConvertUtil.getMapValueByName(maskMap, "DURABLESTATE")));
				maskElement.addContent(new Element("CLEANSTATE").setText(ConvertUtil.getMapValueByName(maskMap, "DURABLECLEANSTATE")));
				maskElement.addContent(new Element("HOLDSTATE").setText(ConvertUtil.getMapValueByName(maskMap, "DURABLEHOLDSTATE")));
				maskElement.addContent(new Element("TRANSFERSTATE").setText(ConvertUtil.getMapValueByName(maskMap, "TRANSPORTSTATE")));

				doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("MASKLIST").addContent(maskElement);
			}

			//this.setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			//setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception ex)
		{
			//setResultItemValue(doc, "NG", ex.getMessage());
			throw new CustomException(ex.getCause());
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
	
	private void setMaskElement(Document doc, String maskName) throws CustomException
	{
		Map<String, Object> maskInfo = this.getMaskInfo(maskName);

		Element maskElement = new Element("MASK");

		maskElement.addContent(new Element("MASKID").setText(ConvertUtil.getMapValueByName(maskInfo, "DURABLENAME")));
		maskElement.addContent(new Element("VENDOR").setText(ConvertUtil.getMapValueByName(maskInfo, "VENDOR")));
		maskElement.addContent(new Element("MASKTSTATE").setText(ConvertUtil.getMapValueByName(maskInfo, "DURABLESTATE")));
		maskElement.addContent(new Element("CLEANSTATE").setText(ConvertUtil.getMapValueByName(maskInfo, "DURABLECLEANSTATE")));
		maskElement.addContent(new Element("HOLDSTATE").setText(ConvertUtil.getMapValueByName(maskInfo, "DURABLEHOLDSTATE")));
		maskElement.addContent(new Element("TRANSFERSTATE").setText(ConvertUtil.getMapValueByName(maskInfo, "TRANSPORTSTATE")));

		doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("MASKLIST").addContent(maskElement);
	}
	
	private Map<String,Object> getMaskInfo(String maskName) throws CustomException
	{
		String sql = " SELECT D.DURABLENAME , S.VENDOR , D.VENDORNUMBER , D.DURABLESTATE ,"
				   + " D.DURABLECLEANSTATE ,D.DURABLEHOLDSTATE , D.TRANSPORTSTATE "
				   + " FROM DURABLE D , DURABLESPEC S"
				   + " WHERE 1=1 "
				   + " AND D.FACTORYNAME = S.FACTORYNAME"
				   + " AND D.DURABLESPECNAME = S.DURABLESPECNAME "
				   + " AND D.DURABLETYPE ='PhotoMask' "
				   + " AND D.DURABLENAME = ? ";
		
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { maskName });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			throw new CustomException("COMM-0010", "Durable", "DurableName = " + maskName);

		return resultList.get(0);
	}
	
	private List<Map<String,Object>> getMaskListByLineName(String lineName) throws CustomException
	{
		String sql = " SELECT D.DURABLENAME , S.VENDOR , D.VENDORNUMBER , D.DURABLESTATE ,"
				   + " D.DURABLECLEANSTATE ,D.DURABLEHOLDSTATE , D.TRANSPORTSTATE "
				   + " FROM DURABLE D , DURABLESPEC S"
				   + " WHERE 1=1 "
				   + " AND D.FACTORYNAME = S.FACTORYNAME"
				   + " AND D.DURABLESPECNAME = S.DURABLESPECNAME "
				   + " AND D.DURABLETYPE ='PhotoMask' "
				   + " AND D.MASKSTOCKERNAME = ? ";
		
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { lineName });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			throw new CustomException("COMM-0010", "Durable", "MaskStockerName = " + lineName);

		return resultList;
	}
}
