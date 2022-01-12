package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

public class TrackInAbortLot extends SyncHandler {

	private static Log log = LogFactory.getLog(TrackInAbortLot.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			
			String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

			List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
			List<String> productNameList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");

			boolean isFirstGlass = false;
			boolean isInReworkFlow = false;

			// for common
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AbortLot", getEventUser(), getEventComment(), null, null);
			
			MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			// Validation - Check duplicate reported glass ID and position
			CommonValidation.checkDuplicatedProductNameByProductList(productNameList);
			CommonValidation.checkDuplicatePosition(productElementList);

			// Check isInReworkFlow (False or True)
			if (productNameList.size() > 0)
				isInReworkFlow = MESLotServiceProxy.getLotServiceUtil().isInReworkFlow(productNameList);

			// Check FisrtGlass (False or True)
			isFirstGlass = MESLotServiceProxy.getLotServiceUtil().judgeFirstGlassLot(lotData, isFirstGlass);

			// Abort only use by PB Port 
			if (!isFirstGlass)
			{
				if (lotData.getProductQuantity() != productNameList.size())
					throw new CustomException("LOT-0071", productNameList.size(), lotData.getProductQuantity(), "PB");
			}

			// Check Validation (if LotProcessState is WAIT then error)
			if (isFirstGlass && productNameList.size() > 0)
			{
				MESLotServiceProxy.getLotServiceUtil().checkLotValidation(lotData); // Check lotState, lotHoldState, LotProcessState
			}
			else
			{
				MESLotServiceProxy.getLotServiceUtil().checkLotValidation(lotData); // Check lotState, lotHoldState, LotProcessState
				MESLotServiceProxy.getLotServiceUtil().checkLotStatus(productNameList); // Check lotState, lotHoldState, LotProcessState
			}
			
			List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

			eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventComment(eventInfo.getEventComment() + " :Exist 'B' as ProcessingInfo");

			// Set productPGSRCSequence
			if (isFirstGlass)
				productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForFirstGlass(bodyElement,lotData.getKey().getLotName(), isInReworkFlow, eventInfo);
			else
				productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(SMessageUtil.getBodyElement(doc), isInReworkFlow, eventInfo);
			
			//Entered product Qtime for ProcessingInfo != B
			this.moveInQtime(productPGSRCSequence, lotData.getUdfs().get("RETURNFLOWNAME"));
			
			// Cancel TrackIn.
			lotData = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, lotData, productPGSRCSequence, new HashMap<String, String>(), new HashMap<String, String>(),
																													 new ArrayList<ConsumedMaterial>(), carrierName);

			if (!StringUtils.equals(lotData.getLotHoldState(), "Y") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedAlarmFailProduct(productElementList))
			{
				// Set ReasonCode
				eventInfo.setReasonCodeType("HOLD");
				eventInfo.setReasonCode("SYSTEM");

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
			}

			// Update Reserve Lot Info (State)
			MESLotServiceProxy.getLotServiceUtil().updateReservedLotStateByCancelTrackIn(lotData.getKey().getLotName(), machineName, lotData, eventInfo);

			setResultItemValue(doc,"OK","");
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

	private Document setResultItemValue(Document doc, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
	
	private void moveInQtime(List<ProductPGSRC> productPGSRCSequence,String returnFlowName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LotAbort", this.getEventUser(), this.getEventComment());
		
		for(ProductPGSRC productPGSRC: productPGSRCSequence)
		{
			if(!"B".equals(productPGSRC.getUdfs().get("PROCESSINGINFO")) )
			{
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productPGSRC.getProductName());

				if (productData.getProcessFlowName().equals(productPGSRC.getUdfs().get("LASTMAINFLOWNAME"))&& productData.getProcessOperationName().equals(productPGSRC.getUdfs().get("LASTMAINOPERNAME")))
				{
					ExtendedObjectProxy.getProductQTimeService().moveInQTimeByProduct(eventInfo, productData, productData.getFactoryName(), productData.getProcessFlowName(),productData.getProcessFlowVersion(), 
																											  productData.getProcessOperationName(), productData.getProcessOperationVersion(), returnFlowName);
				}
			}
		}
	}
}
