package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.extended.object.management.data.CustomAlarm;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.user.management.data.UserProfile;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReleaseHoldLot extends SyncHandler {
	
	Log log = LogFactory.getLog(this.getClass());

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		//String SPCFLAG = SMessageUtil.getBodyItemValue(doc, "SPCFLAG", false);
		String issueFlag = SMessageUtil.getBodyItemValue(doc, "ISSUEFLAG", false);
		String dspFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", false);
		String lotIssue = GenericServiceProxy.getConstantMap().Lot_Issue;
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", getEventUser(), getEventComment(), "", "");

		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		Map<String, Lot> lotDataMap = new HashMap<String, Lot>();

		for (Element eleLot : eleLotList)
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String reasonCodeType = SMessageUtil.getChildText(eleLot, "REASONCODETYPE", false);
			String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
			String processOperationName = SMessageUtil.getChildText(eleLot, "HOLDOPERATIONNAME", true);
			String holdEventUser = SMessageUtil.getChildText(eleLot, "HOLDEVENTUSER", true);
			String releaseType = SMessageUtil.getChildText(eleLot, "RELEASETYPE", false);
			String requestDepartment = SMessageUtil.getChildText(eleLot, "REQUESTDEPARTMENT", false);
			String actionType = SMessageUtil.getChildText(eleLot, "ACTIONTYPE", false);
			
			boolean isSPCHold = false;
			boolean isReviewStation = false;
			boolean isFDCHold = false;
			boolean isEQPHold = isEQPHold(holdEventUser);
			boolean isAbnormalSheet = false;
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			CommonValidation.checkJobDownFlag(lotData);
			
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setReasonCodeType(reasonCodeType);
			eventInfo.setReasonCode(reasonCode);

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			if (StringUtils.indexOf(holdEventUser, "ReviewStation") > -1)
			{
				eventLog.info("ReviewStation Hold");
				isReviewStation = true;
			}
			if (StringUtils.indexOf(holdEventUser, "SPC") > -1)
			{
				eventLog.info("SPC Hold");
				isSPCHold = true;
			}

			if (StringUtils.indexOf(holdEventUser, "FDC") > -1)
			{
				eventLog.info("FDC Hold");
				isFDCHold = true;
			}

			if ("Department".equals(releaseType))
			{
				eventLog.info("AbnormalSheet Hold");
				isAbnormalSheet = true;
			}
			
			/*
			//Modify wangys 2020/01/21 add judge into Release SPC Hold
            //Started{ caixu 2020/12/07 ADD Release SPC Hold
			if(SPCFLAG!=null&&SPCFLAG.equals("Y"))
			{
				addReleaseSPCHold(eleLot, actionType);
			}
			*/
			if(isReviewStation)
			{
				this.checkAbnormalSheetState(lotName,lotData.getFactoryName());//xuch
			}
			if (isAbnormalSheet)
			{
				
				// Check AbnormalSheet Department
				this.checkAbnormalSheetDepartment(lotName, requestDepartment);
			}
			
			//Check Validation
			//checkSplitReasonCode(lotData, reasonCode);
			
			//Get ProcessOperationSpec for firstGlass
			ProcessOperationSpec operationSpecData = 
					CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(),lotData.getProcessOperationVersion());
			
			if (!isEQPHold && !isSPCHold && !isFDCHold && !isReviewStation && !isAbnormalSheet)
			{
				checkDepartmentInfo(holdEventUser, this.getEventUser(), lotName);
			}
			
			if (isSPCHold)
			{
				if(CommonUtil.getEnumDefValueStringByEnumName("SPCAbnormalHold").equals("Y"))
					checkAbnormalFlag(lotName,eventInfo);
			}
			
			if (!StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
			{
				FirstGlassJob jobData = null;
				try
				{
					Object[] bindSet = new Object[] { lotData.getUdfs().get("JOBNAME") };
					jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);
				}
				catch (Exception ex)
				{
					log.info("This Lot is not FirstGlass");
				}
				
				if ( StringUtils.equals(operationSpecData.getProcessOperationType(), "Production") 
						&& jobData != null && !StringUtils.isEmpty(jobData.getJudge()))
				{
					this.firstGlassReleaseHold(eventInfo, productUSequence, lotData, processOperationName, reasonCode);
					checkMultiHold(eventInfo, lotData);
					
					continue;
				}
				else
				{
					if (StringUtils.equals(reasonCode, "FirstGlassHold"))
					{
						throw new CustomException("LOT-0047", lotName);
					}
				}	
			}
			
			 /*Started{ caixu 2020/12/07 ADD Release SPC Hold
			if("SPC".equals(actionType))
			{
				String eventReason = eleLot.getChildText("EVENTREASON");
				String eventAction = eleLot.getChildText("EVENTACTION");
				String spcDescription = eleLot.getChildText("SPCDESCRIPTION");
				String prevent = eleLot.getChildText("PREVENT");
				String scope = eleLot.getChildText("SCOPE");
				String result = eleLot.getChildText("RESULT");
				String alarmIndex = eleLot.getChildText("ALARMINDEX");

				Document replyDoc = this.createReleaseHoldMessage(this.getEventUser(), eventReason, eventAction, spcDescription, prevent, scope, result,  alarmIndex);
				
				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("SPC");
				GenericServiceProxy.getESBServive().sendBySender(targetSubject, replyDoc, "SPCSender");
				
				try 
				{
					String alarmCode = eleLot.getChildText("ALARMCODE");
					String alarmTimeKey = eleLot.getChildText("ALARMTIMEKEY");
					
					CustomAlarm alarmData = ExtendedObjectProxy.getCustomAlarmService().selectByKey(false,new Object[] { alarmCode, alarmTimeKey });
					alarmData.setReleaseHoldFlag("Y");
					
					ExtendedObjectProxy.getCustomAlarmService().update(alarmData);
				} 
				catch (Exception ex) 
				{
					eventLog.info(ex.getCause());
				}
			}*/
			
			if (StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
			{
				if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
					updateHoldState(lotData);
			}
			else
			{
				throw new CustomException("LOT-0134", lotData.getLotState(), lotData.getLotProcessState());
			}

//			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
//			LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			//MaskNotOnHold
			this.updateHoldState(lotData, GenericServiceProxy.getConstantMap().Lot_NotOnHold, eventInfo);
			// delete in LOTMULTIHOLD table
			releaseMultiHold(lotName, reasonCode, processOperationName);

			// delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotName, reasonCode, processOperationName);

			// setHoldState
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			setHoldState(eventInfo, lotData);

			this.changeDSPFlagToN(dspFlag,lotData);

			if (issueFlag.equals("Y")) 
			{
				EventInfo eventInfoS = EventInfoUtil.makeEventInfo("IssueLot", this.getEventUser(),
						this.getEventComment(), "", "");
				log.info(" Start Issue Lot " + "LOTNAME: " + lotName);
				log.info("Event User: " + eventInfoS.getEventUser() + " Event Time: "
						+ TimeStampUtil.toTimeString(eventInfoS.getEventTime()));

				CommonValidation.checkJobDownFlag(lotData);

				Map<String, String> udfs = lotData.getUdfs();
				
				if (udfs.get("LOTISSUESTATE").equals(GenericServiceProxy.getConstantMap().Lot_IssueReleased)
						|| udfs.get("LOTISSUESTATE").isEmpty()) 
				{
					// Check Lot State
					if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released)
							&& StringUtil.equals(lotData.getLotProcessState(), "WAIT")) 
					{
						// Set Lot Issue State
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("LOTISSUESTATE", lotIssue);

						lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoS, setEventInfo);

						// Set Product Issue State
						kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
						List<Product> productList = MESProductServiceProxy.getProductServiceUtil()
								.getProductListByLotName(lotName);

						for (Product product : productList) 
						{
							Map<String, String> udfspro = new HashMap<String, String>();
							udfspro.put("ISSUESTATE", lotIssue);
							udfspro.put("ISSUETIME", TimeStampUtil.toTimeString(eventInfoS.getEventTime()));
							udfspro.put("ISSUEUSER", eventInfoS.getEventUser());

							setProductEventInfo.setUdfs(udfspro);
							ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfoS,
									setProductEventInfo);
						}
					} 
					else 
					{
						// If the LotProcessState value is RUN, you cannot
						// specify IssueLot. LotName=[{0}]
						throw new CustomException("LOT-3004", lotData.getKey().getLotName());
					}
				} 
				else 
				{
					// This Lot is already IssueLot.
					log.info(" This Lot is already IssueLot. " + " LOTNAME: " + lotName);
				}
			}
			
			// Create lotdata map for checkFutureSkip
			Lot newLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			lotDataMap.put(newLotData.getKey().getLotName(), newLotData);
		}

		// AR-AMF-0016-01
		// If both Hold/Skip exist during a scheduled job, proceed with the Hold operation first.
		// Skip operation is when the operator performs the ReleaseHold.
		checkFutureSkip(lotDataMap);

		return doc;
	}

	private void changeDSPFlagToN(String dspFlag, Lot lotData) throws CustomException 
	{
		if (dspFlag.equals("N")) 
		{
			EventInfo eventInfoD = EventInfoUtil.makeEventInfo("ChangeLotDSPFlag", this.getEventUser(),
					this.getEventComment(), "", "");
			log.info(" Start ChangeLotDSPFlag " + "LOTNAME: " + lotData.getKey().getLotName());
			log.info("Event User: " + eventInfoD.getEventUser() + " Event Time: "
					+ TimeStampUtil.toTimeString(eventInfoD.getEventTime()));

			CommonValidation.checkJobDownFlag(lotData);

			Map<String, String> udfs = lotData.getUdfs();
			
			if (udfs.get("DSPFLAG").equals("Y")
					|| udfs.get("DSPFLAG").isEmpty()) 
			{
				// Check Lot State
				if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released)
						&& StringUtil.equals(lotData.getLotProcessState(), "WAIT")) 
				{
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("DSPFLAG", dspFlag);

					lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoD, setEventInfo);
				} 
				else 
				{
					// If the LotProcessState value is RUN, you cannot
					// specify IssueLot. LotName=[{0}]
					throw new CustomException("LOT-3004", lotData.getKey().getLotName());
				}
			} 
			else 
			{
				// This Lot is already IssueLot.
				log.info(" This Lot is already DSPFLAG N. " + " LOTNAME: " + lotData.getKey().getLotName());
			}
		}
	}

	private Document createReleaseHoldMessage(String userId, String reason, String actionName, String description, String prevent, String scope, String result, String alarmIndex) throws CustomException
	{
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		Element headerElement = new Element(SMessageUtil.Header_Tag);
		Element bodyElement = new Element(SMessageUtil.Body_Tag);

		headerElement.addContent(new Element("MESSAGENAME").setText("RELEASE_HOLD_COMMENT"));
		headerElement.addContent(new Element("SHOPNAME").setText(""));
		headerElement.addContent(new Element("MACHINENAME").setText(""));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("EVENTUSER").setText("MESSystem"));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(this.getClass().getSimpleName()));

		bodyElement.addContent(new Element("USERID").setText(userId));
		bodyElement.addContent(new Element("REASON").setText(reason));
		bodyElement.addContent(new Element("ACTION").setText(actionName));
		bodyElement.addContent(new Element("RELEASEHOLDCOMMENT"));

		rootElement.addContent(headerElement);
		rootElement.addContent(bodyElement);

		Document roodDocument = new Document(rootElement);

		Element releaseHoldComment = XmlUtil.getChild(SMessageUtil.getBodyElement(roodDocument), "RELEASEHOLDCOMMENT", true);
		releaseHoldComment.addContent(new Element("DESCRIPTION").setText(description));
		releaseHoldComment.addContent(new Element("PREVENT").setText(prevent));
		releaseHoldComment.addContent(new Element("SCOPE").setText(scope));
		releaseHoldComment.addContent(new Element("RESULT").setText(result));
		releaseHoldComment.addContent(new Element("SPCALARMINDEX").setText(alarmIndex));
		
		return roodDocument;
	}

	private boolean isEQPHold(String eventUser)
	{
		boolean isEQPHold = false;
		String sql = "SELECT MACHINENAME FROM MACHINE WHERE MACHINENAME = :MACHINENAME ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", eventUser);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
			isEQPHold = true;

		return isEQPHold;
	}

	private void releaseMultiHold(String lotName, String reasonCode, String processOperationName) throws CustomException
	{
		try
		{
			LotServiceProxy.getLotMultiHoldService().delete(" lotname = ? and reasoncode = ? and processoperationname = ?", new Object[] { lotName, reasonCode, processOperationName });
		}
		catch (NotFoundSignal ne)
		{
			new CustomException("LOT-9999", lotName);
		}
		catch (FrameworkErrorSignal fe)
		{
			new CustomException("LOT-9999", fe.getMessage());
		}
		finally
		{
			if (eventLog.isDebugEnabled())
				eventLog.debug("Multi-hold removed");
		}
	}

	private void setHoldState(EventInfo eventInfo, Lot lotData)
	{
		try
		{
			List<LotMultiHold> multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? ", new Object[] { lotData.getKey().getLotName() });

			if (multiHoldList.size() > 0)
			{
				// Lot - LotHoldState
				lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_OnHold);
				LotServiceProxy.getLotService().update(lotData);

				// LotHistory - LotHoldState
				String sql = "UPDATE LOTHISTORY SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// Product - ProductHoldState
				sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";

				args = new HashMap<String, String>();
				args.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// ProductHistory - ProductHoldState
				sql = "UPDATE PRODUCTHISTORY SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE PRODUCTNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) AND TIMEKEY = :TIMEKEY ";
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
		}
		catch (Exception e)
		{
		}
	}
	
	private void firstGlassReleaseHold(EventInfo eventInfo, List<ProductU> productUSequence, Lot lotData, String processOperationName, String reasonCode) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
				updateHoldState(lotData);
		}
		else
		{
			throw new CustomException("LOT-0134", lotData.getLotState(), lotData.getLotProcessState());
		}
		
			
			
		MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
		lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

		// delete in LOTMULTIHOLD table
		releaseMultiHold(lotData.getKey().getLotName() , reasonCode, processOperationName);

		// delete in PRODUCTMULTIHOLD table
		MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), reasonCode, processOperationName);

			
		try
		{
			// Select lotMultiHold
			List<LotMultiHold> multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasonCode <> 'FirstGlassHold' ", new Object[] { lotData.getKey().getLotName() });
			
			if (multiHoldList.size() > 0)
			{
				// Lot - LotHoldState
				lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_OnHold);
				LotServiceProxy.getLotService().update(lotData);

				// LotHistory - LotHoldState
				String sql = "UPDATE LOTHISTORY SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// Product - ProductHoldState
				sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME AND PRODUCTSTATE <> 'Scrapped'"; //2021-03-31
				args = new HashMap<String, String>();
				args.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// ProductHistory - ProductHoldState
				sql = "UPDATE PRODUCTHISTORY SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE PRODUCTNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) AND TIMEKEY = :TIMEKEY ";
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
		}
		catch(Exception e) // Check FirstGlass 21-03-22 Matis #0000464 Request by wangcan, yueke
		{
			try
			{
				FirstGlassJob jobData = null;
				try
				{
					Object[] bindSet = new Object[] { lotData.getUdfs().get("JOBNAME") };
					jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);
				}
				catch (Exception ex)
				{
					log.info("This Lot is not FirstGlass");
					return ;
				}
					
				// If Mother Lot, Merge with ChildLot
				if (StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG")))
				{
					lotData = mergeFirstGlassChildLot(eventInfo, jobData, lotData, false);
				}

				// Strip ChildLot OR Pass ChildLot , Merge with MotherLot
				if (StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.isNotEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG")))
				{
					lotData = mergeFirstGlassMotherLot(eventInfo, jobData, lotData, false);
				}

				lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
			}
			catch (Exception ex)
			{
				log.warn("excuteFirstGlass is failed");
			}
		}
	}
	
	private void checkMultiHold (EventInfo eventInfo, Lot lotData) throws CustomException
	{
		try
		{
			// Select lotMultiHold
			List<LotMultiHold> multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasonCode = 'FirstGlassHold'", new Object[] { lotData.getKey().getLotName() });
						
			if (multiHoldList.size() > 0)
			{
				// Lot - LotHoldState
				lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_OnHold);
				LotServiceProxy.getLotService().update(lotData);
	
				// LotHistory - LotHoldState
				String sql = "UPDATE LOTHISTORY SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY ";
	
				Map<String, String> args = new HashMap<String, String>();
				args.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());
	
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	
				// Product - ProductHoldState
				sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME AND PRODUCTSTATE <> 'Scrapped'"; //2021-03-31
				args = new HashMap<String, String>();
				args.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());
	
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	
				// ProductHistory - ProductHoldState
				sql = "UPDATE PRODUCTHISTORY SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE PRODUCTNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) AND TIMEKEY = :TIMEKEY ";
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
		}
		catch (Exception e)
		{
		}
	}

	private void updateHoldState(Lot lotData)
	{
		// Update LotHoldState - Y
		String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		// Update ProductHoldState - Y
		sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
		bindMap.clear();
		bindMap.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold);
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	/**
	 * 
	 * AR-AMF-0016-01 If both Hold/Skip exist during a scheduled job, proceed with the Hold operation first. Skip operation is when the operator performs the ReleaseHold.
	 * 
	 * @author aim_dhko
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void checkFutureSkip(Map<String, Lot> lotDataMap)
	{
		if (lotDataMap == null || lotDataMap.size() == 0)
		{
			eventLog.info("checkFutureSkip : lotDataMap is not exists.");
			return;
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Skip", getEventUser(), getEventComment(), "", "");

		for (Lot lotData : lotDataMap.values())
		{
			if (GenericServiceProxy.getConstantMap().Lot_OnHold.equals(lotData.getLotHoldState()))
			{
				eventLog.info("checkFutureSkip : LotHoldState is 'OnHold'. LotName='" + lotData.getKey().getLotName());
				continue;
			}

			try
			{
				// Get ActionNames
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT LISTAGG (ACTIONNAME, ',') WITHIN GROUP (ORDER BY ACTIONNAME) AS ACTIONNAMES ");
				sql.append("  FROM (SELECT DISTINCT CASE ");
				sql.append("                           WHEN ACTIONNAME = 'hold' ");
				sql.append("                            AND BEFOREACTION = 'True' ");
				sql.append("                           THEN ");
				sql.append("                              'FutureHold' ");
				sql.append("                           WHEN ACTIONNAME = 'hold' ");
				sql.append("                            AND AFTERACTION = 'True' ");
				sql.append("                           THEN ");
				sql.append("                              'TrackOutHold' ");
				sql.append("                           WHEN ACTIONNAME = 'skip' ");
				sql.append("                           THEN ");
				sql.append("                              'FutureSkip' ");
				sql.append("                        END ");
				sql.append("                           AS ACTIONNAME ");
				sql.append("          FROM CT_LOTFUTUREACTION ");
				sql.append("         WHERE 1 = 1 ");
				sql.append("           AND LOTNAME = :LOTNAME ");
				sql.append("           AND FACTORYNAME = :FACTORYNAME ");
				sql.append("           AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				sql.append("           AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
				sql.append("           AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				sql.append("           AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("FACTORYNAME", lotData.getFactoryName());
				args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
				args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
				args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
				
				List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if ((resultDataList == null || resultDataList.size() == 0) || resultDataList.get(0) == null)
				{
					continue;
				}

				String actionNames = ConvertUtil.getMapValueByName(resultDataList.get(0), "ACTIONNAMES");
				
				if (actionNames.contains("FutureHold"))
				{
					MESLotServiceProxy.getLotServiceUtil().executeReserveAction(eventInfo, lotData, lotData);
				}
				else if (actionNames.contains("FutureSkip") && !actionNames.contains("TrackOutHold"))
				{
					MESLotServiceProxy.getLotServiceUtil().executeReserveAction(eventInfo, lotData, lotData);
				}
				else if (actionNames.contains("FutureSkip") && actionNames.contains("TrackOutHold"))
				{
					MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, lotData, lotData, false);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("checkFutureSkip : executeReserveAction Error.", ex);
				continue;
			}
		}
	}
	
	/**
	 * 
	 * Check AbnormalSheet Department
	 * 
	 * @author aim_dhko
	 * @return
	 * @throws CustomException 
	 */
	//xuch  add array abnormalSheet close check
	private void checkAbnormalSheetState(String lotName,String Factory) throws CustomException
	{
		if(StringUtils.equals(Factory, "ARRAY")||StringUtils.equals(Factory, "TP"))
		{
			String inquirysql = " SELECT DT.* FROM PRODUCT P ,CT_ABNORMALSHEETDETAIL DT where  1=1 AND  P.PRODUCTNAME = DT.PRODUCTNAME AND P.LOTNAME =:LOTNAME AND DT.PROCESSSTATE <>'Closing' ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotName);

			List<Map<String, Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate()
					.queryForList(inquirysql, bindMap);
			if (!result.isEmpty()) {
				throw new CustomException("LOT-3015", lotName);
			}
		}
		
	}
	
	
	
	private void checkAbnormalSheetDepartment(String lotName, String requestDepartment) throws CustomException
	{
		UserProfile userData = MESUserServiceProxy.getUserProfileServiceUtil().getUser(getEventUser());
		
		if (!StringUtils.isEmpty(requestDepartment) 
			&& !requestDepartment.equals(userData.getUdfs().get("ABNORMALDEPARTMENT")))
		{
			// Lot[{0}] only can be ReleaseHold by AbnormalDepartment[{1}]
			throw new CustomException("LOT-3009", lotName, userData.getUdfs().get("ABNORMALDEPARTMENT"));
		}
	}
	
	
	
	// hankun
	private void checkDepartmentInfo(String holdEventUser, String nowEventUser, String lotName) throws CustomException {
		String holdUserDepartment = "";
		String nowUserDepartment = "";

		String inquirysql = " SELECT DEPARTMENT FROM USERPROFILE WHERE USERID = :USERID ";

		Map<String, String> nowUserbindMap = new HashMap<String, String>();
		nowUserbindMap.put("USERID", nowEventUser);

		List<Map<String, Object>> nowUserResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate()
				.queryForList(inquirysql, nowUserbindMap);
		if (nowUserResult.isEmpty()) {
			log.info("Can't Find UserDepartment, But Only MFGLeader Can Release It");
		} else {
			nowUserDepartment = ConvertUtil.getMapValueByName(nowUserResult.get(0), "DEPARTMENT");
		}

		Map<String, String> userbindMap = new HashMap<String, String>();
		userbindMap.put("USERID", holdEventUser);

		List<Map<String, Object>> userResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate()
				.queryForList(inquirysql, userbindMap);
		if (userResult == null || userResult.size() == 0) {
			String inquirysqlH = " SELECT DEPARTMENT FROM USERPROFILEHISTORY WHERE USERID = :USERID AND EVENTNAME <> 'Remove' ORDER BY TIMEKEY DESC";

			userResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysqlH,
					userbindMap);
			if (!userResult.isEmpty()) {
				holdUserDepartment = ConvertUtil.getMapValueByName(userResult.get(0), "DEPARTMENT");
			} else {
				log.info("Unable to find department information in UserProfile. UserID={" + holdEventUser + "}");
			}
		} else {
			holdUserDepartment = ConvertUtil.getMapValueByName(userResult.get(0), "DEPARTMENT");
		}

		if (!checkReleaseFlag(nowEventUser, nowUserDepartment)) {
			if (!StringUtils.equals(nowUserDepartment, holdUserDepartment)) {
				if (StringUtils.isNotEmpty(holdUserDepartment)) {
					throw new CustomException("LOT-0212", lotName, holdUserDepartment);
				} else {
					throw new CustomException("LOT-0213", lotName);
				}
			}
		}
	}

	// hankun
	private boolean checkReleaseFlag(String eventUserId, String holdUserDepartment) 
	{
		boolean isRelease = false;
		String usergroupsql = " SELECT U.MFGRELEASEFLAG FROM USERGROUP U, USERPROFILE P WHERE P.USERGROUPNAME=U.USERGROUPNAME AND P.USERID= :USERID ";

		Map<String, String> nowUserGroupMap = new HashMap<String, String>();
		nowUserGroupMap.put("USERID", eventUserId);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> nowUserGroupResult = GenericServiceProxy.getSqlMesTemplate()
				.queryForList(usergroupsql, nowUserGroupMap);
		String nowMFGReleaseFlag = ConvertUtil.getMapValueByName(nowUserGroupResult.get(0), "MFGRELEASEFLAG");

		if (StringUtils.equals(nowMFGReleaseFlag, "ALL")) {
			isRelease = true;
		} else if (!nowMFGReleaseFlag.isEmpty()) {
			String[] line = nowMFGReleaseFlag.split(",");
			for (String s : line) {
				if (StringUtils.equals(s, holdUserDepartment)) {
					isRelease = true;
					break;
				} else {
					isRelease = false;
				}
			}
		}

		return isRelease;
	}
	
	private void addReleaseSPCHold (Element eleLot, String actionType) throws CustomException
	{
		if("SPC".equals(actionType))
		{
			String eventReason = eleLot.getChildText("EVENTREASON");
			String eventAction = eleLot.getChildText("EVENTACTION");
			String spcDescription = eleLot.getChildText("SPCDESCRIPTION");
			String prevent = eleLot.getChildText("PREVENT");
			String scope = eleLot.getChildText("SCOPE");
			String result = eleLot.getChildText("RESULT");
			String alarmIndex = eleLot.getChildText("ALARMINDEX");

			Document replyDoc = this.createReleaseHoldMessage(this.getEventUser(), eventReason, eventAction, spcDescription, prevent, scope, result,  alarmIndex);
		
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("SPC");
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, replyDoc, "SPCSender");
		
			try 
			{
				String alarmCode = eleLot.getChildText("ALARMCODE");
				String alarmTimeKey = eleLot.getChildText("ALARMTIMEKEY");
			
				CustomAlarm alarmData = ExtendedObjectProxy.getCustomAlarmService().selectByKey(false,new Object[] { alarmCode, alarmTimeKey });
				alarmData.setReleaseHoldFlag("Y");
			
				ExtendedObjectProxy.getCustomAlarmService().update(alarmData);
			} 
			catch (Exception ex) 
			{
				eventLog.info(ex.getCause());
			}
		}
	}
	
	public Lot mergeFirstGlassChildLot(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData, boolean checkAllSplitChildLot) throws CustomException
	{
		log.info("Start mergeFirstGlassChildLot");
		String jobName = jobData.getJobName();

		List<Lot> childLotDataList = new ArrayList<Lot>();
		try
		{// Find final Completed child lot
			childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'", new Object[] { jobName, "N" });
		}
		catch (Exception e)
		{
		}
		
		List<Lot> completedChildLotDataList = new ArrayList<Lot>();
		try
		{
			// Find final Completed child lot
			completedChildLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND LOTNAME <> ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
					new Object[] { jobName, "Y", lotData.getKey().getLotName() });
		}
		catch (Exception e)
		{
		}

		// if final Completed child lot exist, merge
		if (childLotDataList.size() > 0)
		{
			Lot childLotData = childLotDataList.get(0);
			String prevChildLotName = childLotData.getKey().getLotName();

			if ( StringUtils.equals(lotData.getProcessFlowName(), childLotData.getProcessFlowName()) 
					&& StringUtils.equals(lotData.getProcessFlowVersion(), childLotData.getProcessFlowVersion())
					&& StringUtils.equals(lotData.getProcessOperationName(), childLotData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), childLotData.getProcessOperationVersion())
					&& StringUtils.equals("Strip", jobData.getJudge())
					&& StringUtils.equals(lotData.getProcessFlowName(), jobData.getProcessFlowName())
					&& StringUtils.equals(lotData.getProcessFlowVersion(), jobData.getProcessFlowVersion())
					&& StringUtils.equals(lotData.getProcessOperationName(), jobData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), jobData.getProcessOperationVersion()))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(prevChildLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);

				}

				// Release Final Completed Child Lot
				List<LotMultiHold> multiHoldList = null;
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { childLotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}
				if (StringUtils.equals(childLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(childLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(childLotData, productUSequence, udfs);
					childLotData = LotServiceProxy.getLotService().makeNotOnHold(childLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, childLotData);
				}

				// Release Lot
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { lotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}
				if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, lotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(lotData.getKey().getLotName(), productList.size(), productPSequence,
						lotData.getUdfs(), new HashMap<String, String>());
				eventInfo.setEventName("Merge");
				childLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, childLotData, transitionInfo);

				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				if (StringUtils.isNotEmpty(childLotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(childLotData.getCarrierName());
					deassignCarrierUdfs = sLotDurableData.getUdfs();
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(childLotData, sLotDurableData, new ArrayList<ProductU>());
					// Deassign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(childLotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, childLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
				deassignCarrierUdfs.clear();

				if (completedChildLotDataList.size() == 0)
				{
					// Complete job and remove JobName at lot if Lot is satisfied conditions below
					// 1. This Lot is MotherLot.
					// 2. Not exist Mother & All ChildLot are split.
					if ((StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG"))) || checkAllSplitChildLot)
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + lotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

						// Check Grade.
						List<Product> mLotProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
						boolean isExistGrade = false;
						String lotGrade = "";
						for (Product productData : mLotProductList)
						{
							if (StringUtils.equals(productData.getProductGrade(), "R"))
							{
								isExistGrade = true;
								lotGrade = "R";
								break;
							}
						}

						if (isExistGrade)
						{
							eventInfo.setEventName("ChangeGrade");
							List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

							ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);

							lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
						}
					}
				}

				
				eventInfo.setReasonCodeType("StripHOLD");
				eventInfo.setReasonCode("StripHOLD");
				eventInfo.setEventComment("FirstGlassHold:After Strip,Please call Photo Enginner(8364)");

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
				
			}
			else
			{
				//LOT-0052: Can't Merge because Job's Return Info is not same Lot's Info
				throw new CustomException("LOT-0052");
			}
		}// Pass Case Or Strip Not ALL Case
		else if(completedChildLotDataList.size() > 0)
		{
			Lot completedchildLotData = completedChildLotDataList.get(0);
			String prevMotherLotName = lotData.getKey().getLotName();
			String childLotName = completedchildLotData.getKey().getLotName();
			
			if (StringUtils.equals(jobData.getReturnProcessFlowName(), lotData.getProcessFlowName()) && StringUtils.equals(jobData.getReturnProcessFlowVersion(), lotData.getProcessFlowVersion())
					&& StringUtils.equals(jobData.getReturnProcessOperationName(), lotData.getProcessOperationName())
					&& StringUtils.equals(jobData.getReturnProcessOperationVersion(), lotData.getProcessOperationVersion())
					&& StringUtils.equals(completedchildLotData.getProcessFlowName(), lotData.getProcessFlowName()) && StringUtils.equals(completedchildLotData.getProcessFlowVersion(), lotData.getProcessFlowVersion())
					&& StringUtils.equals(completedchildLotData.getProcessOperationName(), lotData.getProcessOperationName())
					&& StringUtils.equals(completedchildLotData.getProcessOperationVersion(), lotData.getProcessOperationVersion())
					&& (StringUtils.equals("Pass", jobData.getJudge()) || StringUtils.equals("Strip", jobData.getJudge())))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(childLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);
				}
				
				// Release Final Completed Child Lot
				List<LotMultiHold> multiHoldList = null;
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { completedchildLotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}
				if (StringUtils.equals(completedchildLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(completedchildLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(completedchildLotData, productUSequence, udfs);
					completedchildLotData = LotServiceProxy.getLotService().makeNotOnHold(completedchildLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(childLotName, "FirstGlassHold", completedchildLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(childLotName, "FirstGlassHold", completedchildLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, completedchildLotData);
				}

				// Release Mother Lot
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { lotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}
				if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevMotherLotName, "FirstGlassHold", lotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevMotherLotName, "FirstGlassHold", lotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, lotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(prevMotherLotName, productList.size(), productPSequence, lotData.getUdfs(),
						new HashMap<String, String>());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventName("Merge");
				completedchildLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, completedchildLotData, transitionInfo);

				// Completed FirstGlass.
				this.updateStripLotInformation(eventInfo, completedchildLotData);
				
				if ((StringUtils.isNotEmpty(completedchildLotData.getUdfs().get("JOBNAME")) && StringUtils.equals(completedchildLotData.getUdfs().get("FIRSTGLASSFLAG"), "Y")))
				{
					// Complete job and remove JobName at lot if Lot is satisfied conditions below
					// 1. This Lot is MotherLot.
					// 2. Not exist Mother & All ChildLot are split.
					if ((StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG"))))
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + lotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

						// Check Grade.
						if (!StringUtils.equals(lotData.getLotGrade(), "G"))
						{
							eventInfo.setEventName("ChangeGrade");
							List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

							ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, "G", productPGSSequence);

							lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
						}
					}
				}
			}
			else
			{
				//LOT-0052: Can't Merge because Job's Return Info is not same Lot's Info
				throw new CustomException("LOT-0052");
			}
		}

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		return lotData;

	}
	
	public Lot mergeFirstGlassMotherLot(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData, boolean checkAllSplitChildLot) throws CustomException
	{
		String jobName = jobData.getJobName();

		List<Lot> mLotDataList = new ArrayList<Lot>();
		try
		{
			// Find final Completed mother lot
			mLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG is null AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'", new Object[] { jobName });
		}
		catch (Exception e)
		{
			return lotData;
		}

		List<Lot> childLotDataList = new ArrayList<Lot>();
		try
		{
			// Find final Completed child lot
			childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND LOTNAME <> ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
					new Object[] { jobName, "Y", lotData.getKey().getLotName() });
		}
		catch (Exception e)
		{
		}

		// if final Completed child lot exist, merge
		if (mLotDataList.size() > 0)
		{
			Lot mLotData = mLotDataList.get(0);
			String prevMotherLotName = mLotData.getKey().getLotName();
			String stripLotName = lotData.getKey().getLotName();

			
			// Strip Case
			if (StringUtils.equals(jobData.getProcessFlowName(), lotData.getProcessFlowName()) && StringUtils.equals(jobData.getProcessFlowVersion(), lotData.getProcessFlowVersion())
					&& StringUtils.equals(jobData.getProcessOperationName(), lotData.getProcessOperationName())
					&& StringUtils.equals(jobData.getProcessOperationVersion(), lotData.getProcessOperationVersion()) && StringUtils.equals(lotData.getProcessFlowName(), mLotData.getProcessFlowName())
					&& StringUtils.equals(lotData.getProcessFlowVersion(), mLotData.getProcessFlowVersion()) && StringUtils.equals(lotData.getProcessOperationName(), mLotData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), mLotData.getProcessOperationVersion())
					&& StringUtils.equals("Strip", jobData.getJudge()))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(stripLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);
				}

				// Release Mother Lot
				List<LotMultiHold> multiHoldList = null;
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { mLotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}
				if (StringUtils.equals(mLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(mLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(mLotData, productUSequence, udfs);
					mLotData = LotServiceProxy.getLotService().makeNotOnHold(mLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, mLotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(prevMotherLotName, productList.size(), productPSequence, mLotData.getUdfs(),
						new HashMap<String, String>());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventName("Merge");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

				// Completed FirstGlass.
				this.updateStripLotInformation(eventInfo, lotData);

				if (childLotDataList.size() == 0)
				{
					// Complete job and remove JobName at lot if Lot is satisfied conditions below
					// 1. This Lot is MotherLot.
					// 2. Not exist Mother & All ChildLot are split.
					if ((StringUtils.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG"))))
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							mLotData = LotServiceProxy.getLotService().setEvent(mLotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + mLotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

						// Check Grade.
						if (!StringUtils.equals(mLotData.getLotGrade(), "G"))
						{
							eventInfo.setEventName("ChangeGrade");
							List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

							ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(mLotData, "G", productPGSSequence);

							mLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, mLotData, changeGradeInfo);
						}
					}
				}

				eventInfo.setReasonCodeType("StripHOLD");
				eventInfo.setReasonCode("StripHOLD");
				eventInfo.setEventComment("FirstGlassHold:After Strip,Please call Photo Enginner(8364)");

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, mLotData, new HashMap<String, String>());
			}// Pass Case Or Strip Not ALL Case
			else if (StringUtils.equals(jobData.getReturnProcessFlowName(), mLotData.getProcessFlowName()) && StringUtils.equals(jobData.getReturnProcessFlowVersion(), mLotData.getProcessFlowVersion())
					&& StringUtils.equals(jobData.getReturnProcessOperationName(), mLotData.getProcessOperationName())
					&& StringUtils.equals(jobData.getReturnProcessOperationVersion(), mLotData.getProcessOperationVersion())
					&& StringUtils.equals(lotData.getProcessFlowName(), mLotData.getProcessFlowName()) && StringUtils.equals(lotData.getProcessFlowVersion(), mLotData.getProcessFlowVersion())
					&& StringUtils.equals(lotData.getProcessOperationName(), mLotData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), mLotData.getProcessOperationVersion())
					&& (StringUtils.equals("Pass", jobData.getJudge()) || StringUtils.equals("Strip", jobData.getJudge())))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(stripLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);
				}

				// Release Mother Lot
				List<LotMultiHold> multiHoldList = null;
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { mLotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}

				if (StringUtils.equals(mLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(mLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(mLotData, productUSequence, udfs);
					mLotData = LotServiceProxy.getLotService().makeNotOnHold(mLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, mLotData);
				}
				
				// Release Child Lot
				try 
				{
					multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? and reasoncode = ? ", new Object[] { lotData.getKey().getLotName(),"FirstGlassHold" });
				} 
				catch (Exception e) 
				{
					
				}

				if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold) && multiHoldList != null)
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(stripLotName, "FirstGlassHold", lotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(stripLotName, "FirstGlassHold", lotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, lotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(prevMotherLotName, productList.size(), productPSequence, mLotData.getUdfs(),
						new HashMap<String, String>());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventName("Merge");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

				// Completed FirstGlass.
				this.updateStripLotInformation(eventInfo, lotData);
				
				if ((StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSFLAG"), "Y")))
				{
					// Complete job and remove JobName at lot if Lot is satisfied conditions below
					// 1. This Lot is MotherLot.
					// 2. Not exist Mother & All ChildLot are split.
					if ((StringUtils.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG"))))
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							mLotData = LotServiceProxy.getLotService().setEvent(mLotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + mLotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

						// Check Grade.
						if (!StringUtils.equals(mLotData.getLotGrade(), "G"))
						{
							eventInfo.setEventName("ChangeGrade");
							List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

							ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(mLotData, "G", productPGSSequence);

							mLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, mLotData, changeGradeInfo);
						}
					}
				}
			}
			else
			{
				//LOT-0052: Can't Merge because Job's Return Info is not same Lot's Info
				throw new CustomException("LOT-0052");
			}
		}

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		return lotData;
	}
	
	public void updateStripLotInformation(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("ChangeFirstGlassFlag");

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("FIRSTGLASSFLAG", "Y");

		// 21-03-23 Modify by jhyeom (Return lotData)
		lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(lotData.getCarrierName()))
		{
			Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			deassignCarrierUdfs = sLotDurableData.getUdfs();
			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());
			// Deassign Carrier
			eventInfo.setEventName("DeassignCarrier");
			MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
		}

		eventInfo.setEventName("MakeEmptied");
		MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
		deassignCarrierUdfs.clear();
	}
	
	public void checkSplitReasonCode (Lot lotData, String reasonCode) throws CustomException
	{
		if (StringUtils.equals(reasonCode, "StripHOLD"))
		{
			if (!StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
			{
				List<Lot> completedChildLotDataList = new ArrayList<Lot>();
				try
				{
					// Find final Completed child lot
					completedChildLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND LOTNAME <> ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
							new Object[] { lotData.getUdfs().get("JOBNAME"), "Y", lotData.getKey().getLotName() });
				}
				catch (Exception e)
				{
				}
				
				Lot childLotData = null;
				if (completedChildLotDataList.size() > 0)
				{
					childLotData = completedChildLotDataList.get(0);
				}
				
				throw new CustomException("LOT-3013", lotData.getKey().getLotName(), childLotData.getKey().getLotName());
			}
		}
	}
	
	private void checkAbnormalFlag(String lotName, EventInfo eventInfo) throws CustomException 
	{
		List<AbnormalEQP> spcAbOKList = new ArrayList<AbnormalEQP>();
		List<AbnormalEQP> spcAbNGList = new ArrayList<AbnormalEQP>();
		
		try
		{
			spcAbNGList = ExtendedObjectProxy.getAbnormalEQPService().select(" LOTNAME = ? AND ABNORMALTYPE = ? AND ABNORMALSTATE <> 'Closed' AND CANRELEASEHOLDLOTFLAG = 'N'",
					new Object[] { lotName, "SPC"});
		}
		catch (Exception e)
		{
		}
		
		try
		{
			spcAbOKList = ExtendedObjectProxy.getAbnormalEQPService().select(" LOTNAME = ? AND ABNORMALTYPE = ? AND ABNORMALSTATE <> 'Closed' AND CANRELEASEHOLDLOTFLAG = 'Y'",
					new Object[] { lotName, "SPC"});
		}
		catch (Exception e)
		{
		}
		
		if (spcAbNGList.size() > 0)
		{
			StringBuffer spcAb = new StringBuffer();
			
			for (AbnormalEQP abnormalEQP : spcAbNGList) 
			{
				spcAb.append(abnormalEQP.getAbnormalName()+ " ");
			}
			
			throw new CustomException("SYS-9999", "AbnormalList: " + spcAb.toString() + "CANRELEASEHOLDLOTFLAG IS N,Please Call QA Confirm!");
		}
		/*
		else if(spcAbOKList.size() > 0)
		{
			String userList = getUserList("品保");

		    try
		    {															
				String[] userGroup = userList.split(",");				
				String title = "SPC异常单可关闭通知";
				String detailtitle = "${}CIM系统消息通知";
				
				StringBuffer info = new StringBuffer();
				info.append("<pre>=======================NoticeInformation=======================</pre>");
				for (AbnormalEQP abnormalEQP : spcAbOKList) 
				{
					info.append("<pre>	abnormal："+abnormalEQP.getAbnormalName()+"</pre>");
				}
				info.append("<pre>	abnormalType："+"SPC"+"</pre>");
				info.append("<pre>=============================End=============================</pre>");				
				
				String message = info.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
				//log.info("eMobile Send Success!");	
				
				StringBuffer weChatInfo = new StringBuffer();
				weChatInfo.append("<pre>======NoticeInformation======</pre>");		
				for (AbnormalEQP abnormalEQP : spcAbOKList) 
				{
					weChatInfo.append("<pre>	abnormal："+abnormalEQP.getAbnormalName()+"</pre>");
				}
				weChatInfo.append("<pre> abnormalType："+"SPC"+"</pre>");
				weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				
				String weChatMessage = weChatInfo.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
				//log.info("WeChat Send Success!");	
		    }
			catch (Exception e)
			{
				log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
			}
		}*/
	}
	
	private String getUserList(String department)
	{
		String userList = new String();
		try 
		{
			StringBuilder sb = new StringBuilder();
			List<String> departmentList =  new ArrayList<String>();
			departmentList.add(department);
			
			StringBuffer sql1 = new StringBuffer();
			sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL='1'");
			Map<String, Object> args1 = new HashMap<String, Object>();

			//for (String department1 : department) 
			for(int j = 0; j < departmentList.size(); j++)
			{
				args1.put("DEPARTMENT", departmentList.get(j));
				List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
						.queryForList(sql1.toString(), args1);
				
				if (sqlResult1.size() > 0) 
				{
					if(j < departmentList.size() - 1)
					{
						for (int i = 0; i < sqlResult1.size(); i++) 
						{  
							String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
							sb.append(user + ",");  
			             } 
					}
					else
					{
						for (int i = 0; i < sqlResult1.size(); i++) 
						{  
							String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
			                 if (i < sqlResult1.size() - 1) {  
			                     sb.append(user + ",");  
			                 } else {  
			                     sb.append(user);  
			                 }  
			             } 
					}						 						
				}
			}
			userList = sb.toString();
		}
		catch (Exception e)
		{
			log.info(" Failed to send to EMobile");
		}
		
		return userList;
	}
	
	private void updateHoldState(Lot lotData, String holdState, EventInfo eventInfo)
	{
		// Update LotHoldState
		String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTHOLDSTATE", holdState);
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		SetEventInfo setEventInfo = new SetEventInfo();
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

		// Update ProductHoldState
		sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
		bindMap.clear();
		bindMap.put("PRODUCTHOLDSTATE",holdState);
		bindMap.put("LOTNAME", lotData.getKey().getLotName());

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
		List<Product>productList = ProductServiceProxy.getProductService().select(" LOTNAME = ? AND PRODUCTHOLDSTATE = ? ", new String[]{lotData.getKey().getLotName(),holdState});
		for(int i =0;i<productList.size();i++)
		{
			ProductKey productKey = new ProductKey();
			productKey.setProductName(productList.get(i).getKey().getProductName());
			ProductServiceProxy.getProductService().setEvent(productKey, eventInfo, setProductEventInfo);
		}

	}

}
