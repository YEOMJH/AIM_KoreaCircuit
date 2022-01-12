package kr.co.aim.messolution.transportjob.service;

import java.util.Timer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Element;
import org.jdom.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class TransportJobServiceUtil implements ApplicationContextAware{
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(TransportJobServiceUtil.class);
	
	@Override
	public void setApplicationContext(ApplicationContext arg0)
	throws BeansException 
	{
		applicationContext = arg0;
	}

	public void timeDelay(long delay)
	{
		Timer timer = new Timer();
		timer.schedule(null, delay);	
	}

	public String getTransportJob(String carrierName) throws CustomException
	{
		if(log.isInfoEnabled()){
			log.info("messageName = " + carrierName);
		}
		
		String timeKey =  ConvertUtil.getCurrTime();
		String transportJob = carrierName + "_" + timeKey + "_" + "MESSYS";
		return transportJob;
	}

	public static String getJobState(String messageName, Document document) throws CustomException
	{
		String jobState = "";

		if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREQUEST)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTMASKBATCHJOBREQUEST)
				||messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTMASKTRANSPORTJOBREQUEST))
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested;

		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREPLY)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTMASKBATCHJOBREPLY)
				||messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_REQUESTMASKTRANSPORTJOBREPLY))
		{
			String returnCode = document.getRootElement().getChild("Return").getChildText("RETURNCODE");

			if ("0".equals(returnCode))
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted;
			}
			else
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected;
			}
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTED)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBSTARTED)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBSTARTEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBSTARTED)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBSTARTEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CARRIERLOACATIONCHANGED))
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCOMPLETED)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCOMPLETED)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCOMPLETED))
		{
			String returnCode = document.getRootElement().getChild("Return").getChildText("RETURNCODE");

			if ("0".equals(returnCode))
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed;
			}
			else
			{
				jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated;;
			}
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBTERMINATEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBTERMINATEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBTERMINATEDBYMCS)
				|| messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELCOMPLETED))
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated;
		}
		else
		{
			jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started;
		}
		return jobState;
	}

	public String getCarrierType(Durable durableData) throws CustomException
	{
		// Required to Define DurableType
		String durableType = durableData.getDurableType();
		
		if (StringUtils.equals(durableData.getFactoryName(), "ARRAY") &&
			StringUtils.equals(durableType, "BufferCST"))
		{
			return GenericServiceProxy.getConstantMap().MCS_CARRIERTYPE_SCST;
		}
		else if (StringUtils.equals(durableType, "OLEDGlassCST") ||
				 StringUtils.equals(durableType, "TPGlassCST") ||
				 StringUtils.equals(durableType, "BufferCST"))
		{
			return GenericServiceProxy.getConstantMap().MCS_CARRIERTYPE_GCST;
		}
		else if (StringUtils.equals(durableType, "EVAMaskCST") ||
				 StringUtils.equals(durableType, "TFEMaskCST"))
		{
			return GenericServiceProxy.getConstantMap().MCS_CARRIERTYPE_MCST;
		}
		else if (StringUtils.equals(durableType, "PeelingFilmBox"))
		{
			return GenericServiceProxy.getConstantMap().MCS_CARRIERTYPE_RBOX;
		}
		else if (StringUtils.equals(durableType, "CoverTray"))
		{
			return GenericServiceProxy.getConstantMap().MCS_CARRIERTYPE_GTRY;
		}
		else
		{
			return durableType;
		}
	}

	public String getCarrierState(Durable durableData) throws CustomException
	{
		// Required to Define Tension State
		// [ FULL | EMPTY ]
		String carrierState = "";
		String durableState = durableData.getDurableState();

		if (StringUtils.equals(durableState, GenericServiceProxy.getConstantMap().Dur_InUse))
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
		}
		else
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
		}

		return carrierState;
	}
	
	public String getCleanState(Durable durableData) throws CustomException
	{
		// Required to Define Tension State
		// [ CLEAN | DIRTY | TENSION ]
		String cleanState = "";
		String durableCleanState = durableData.getDurableCleanState();

		if (StringUtils.equals(durableCleanState, GenericServiceProxy.getConstantMap().Dur_Clean))
		{
			cleanState = GenericServiceProxy.getConstantMap().MCS_CLEANSTATE_CLEAN;
		}
		else if (StringUtils.equals(durableCleanState, GenericServiceProxy.getConstantMap().Dur_Dirty))
		{
			cleanState = GenericServiceProxy.getConstantMap().MCS_CLEANSTATE_DIRTY;
		}

		return cleanState;
	}

	public static String getCancelState(String messageName, Document document) throws CustomException
	{
		String cancelState = "";

		if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREQUEST) ||
			messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELMASKBATCHJOBREQUEST) ||
			messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELMASKBATCHJOBREQUEST))
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Requested;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREPLY) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CANCELMASKBATCHJOBREPLY))
		{
			String returnCode = document.getRootElement().getChild("Return").getChild("RETURNCODE").getText();

			if (StringUtils.equals(returnCode, "0"))
			{
				cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Accepted;
			}
			else
			{
				cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Rejected;
			}
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELSTARTED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELSTARTED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELSTARTED))
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Started;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELCOMPLETED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELCOMPLETED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELCOMPLETED))
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Completed;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELFAILED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELFAILED) ||
				 messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELFAILED))
		{
			cancelState = GenericServiceProxy.getConstantMap().MCS_CANCELSTATE_Failed;
		}

		return cancelState;
	}

	public static String getChangeState(String messageName, Document document) throws CustomException
	{
		String changeState = "";
		if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREQUEST))
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Requested;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREPLY))
		{
			String returnCode = document.getRootElement().getChild("Return").getChild("RETURNCODE").getText();
			if ("0".equals(returnCode))
			{
				changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Accepted;
			}
			else
			{
				changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Rejected;
			}
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGED))
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Changed;
		}
		else if (messageName.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGEFAILED))
		{
			changeState = GenericServiceProxy.getConstantMap().MCS_CHANGESTATE_Failed;
		}
		return changeState;
	}

	public String generateTransportJobId(String carrierName, String sender) throws CustomException
	{
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
		return ("M" + transportDate + "-" + carrierName);
	}

	public String generateTransportJobIdBySender(String carrierName, String sender) throws CustomException
	{
		// Check TransportJobNaming Rule. Manual: M, Auto: H
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);

		if (sender.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC))
		{
			// Manual for U or M
			return ("U" + transportDate + "-" + carrierName);
		}
		else
		{
			// Auto for R or H
			return ("R" + transportDate + "-" + carrierName);
		}
	}

	public String generateBatchTransportJobId(String carrierName)
	{
		String transportDate = ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);

		// Batch for B
		String transportJobName = "B" + transportDate + "-" + carrierName;
		log.info("Generator BatchTransportjobName = " + transportJobName);
		return transportJobName;
	}

	public void setCarrierLocationSequence(String machineName,String portName,String portType,String durableName) throws CustomException
	{
		try{
			String subMachine=machineName.substring(0, 4);
			if(subMachine.equals("AFBF")||subMachine.equals("FFBF"))
			{
				//new stocker,check port
				if(!portType.equals("PORT")) return;
				else
				{
					String sql="SELECT PORTTYPE FROM PORT ";
					sql+="WHERE MACHINENAME=:machinename ";
					sql+="AND PORTNAME=:portname ";
					Map<String, String> bindMap = new HashMap<String, String>();
					bindMap.put("machinename", machineName);
					bindMap.put("portname", portName);
					List<Map<String, Object>> sqlResult=GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
					if(sqlResult.size()>0)
					{
						String porttype=sqlResult.get(0).get("PORTTYPE").toString();
						if(porttype.equals("PL"))
						{
							sql="SELECT DURABLENAME,DURABLESEQUENCE FROM DURABLE D ";
							sql+="WHERE MACHINENAME=:machinename ";
							sql+="AND PORTNAME=:portname ";
							sql+="AND DURABLESEQUENCE IS NOT NULL ";
							sql+="ORDER BY TO_NUMBER(DURABLESEQUENCE) DESC ";
							bindMap.clear();
							bindMap.put("machinename", machineName);
							bindMap.put("portname", portName);
							sqlResult=GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
							
							//check CST is exist on port
							for(int i=0;i<sqlResult.size();i++)
							{
								if(durableName.equals(sqlResult.get(i).get("DURABLENAME").toString()))
									return;
							}
							
							//UPDATE DURABLE COUNT
							String sequence="001";
							if(sqlResult.size()>0)
							{
								try{
									sequence=Integer.parseInt(sqlResult.get(0).get("DURABLESEQUENCE").toString())+1+"";
									int len=3-sequence.length();
									for(int j=0;j<len;j++)
									{
										sequence="0"+sequence;
									}
								}
								catch(Exception e){
									sequence="001";
								}
							}
							sql="UPDATE DURABLE ";
							sql+="SET DURABLESEQUENCE=:sequence ";
							sql+="WHERE DURABLENAME=:durablename ";
							bindMap.clear();
							bindMap.put("sequence", sequence+"");
							bindMap.put("durablename", durableName);
							GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
						}
						else
						{
							//when port is not PL,sequence is null
							String sequence="";
							sql="UPDATE DURABLE ";
							sql+="SET DURABLESEQUENCE=:sequence ";
							sql+="WHERE DURABLENAME=:durablename ";
							bindMap.clear();
							bindMap.put("sequence", sequence);
							bindMap.put("durablename", durableName);
							GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
						}
					}
				}
			}
			else return;
		}
		catch(Exception e){
			log.info(e);
		}
	}
	
	public TransportJobCommand setTransportJobCommandEventInfo(TransportJobCommand transportJobCommand, EventInfo eventInfo) 
																throws CustomException
	{
		transportJobCommand.setLastEventName(eventInfo.getEventName());
		transportJobCommand.setLastEventUser(eventInfo.getEventUser());
		transportJobCommand.setLastEventTime(eventInfo.getEventTime());
		transportJobCommand.setLastEventTimeKey(eventInfo.getEventTimeKey());
		transportJobCommand.setLastEventComment(eventInfo.getEventComment());
		
		return transportJobCommand;
	}
	
	public List<TransportJobCommand> checkExistTransportJobCommand(
										List<TransportJobCommand> transportJobCommandList, String transportJobName) 
										throws CustomException
																
	{
		if(transportJobCommandList.size() == 0)
			throw new CustomException("JOB-9001", transportJobName);
		
		return transportJobCommandList;
	}

	public Durable changeCurrentCarrierLocation(Durable durableData, String currentMachineName, String currentPositionType,
			String currentPositionName, String currentZoneName, String transferState,
			String transportLockFlag, EventInfo eventInfo) throws CustomException
	{
		Machine machineData = new Machine();
		try
		{
			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
		}
		catch (Exception e)
		{
		}

		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("MACHINENAME", currentMachineName);
		if (currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			durableUdfs.put("PORTNAME", currentPositionName);
			durableUdfs.put("POSITIONNAME", "");
		}
		else
		{
			durableUdfs.put("PORTNAME", "");
			durableUdfs.put("POSITIONNAME", currentPositionName);
		}

		durableUdfs.put("POSITIONTYPE", currentPositionType);
		durableUdfs.put("ZONENAME", currentZoneName);
		durableUdfs.put("TRANSPORTSTATE", transferState);
		if (StringUtils.isNotEmpty(transportLockFlag))
		{
			durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);
		}

		// SetArea Info
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(machineData.getAreaName());
		setAreaInfo.setUdfs(durableUdfs);

		durableData = DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);


		return durableData;
	}
	
	// Add TRANSPORTTYPE to resolve timekey duplicate error in TransportJobStartedByMCS message
	public Durable changeCurrentCarrierLocation(Durable durableData, String currentMachineName, String currentPositionType,
			String currentPositionName, String currentZoneName, String transferState,
			String transportLockFlag, EventInfo eventInfo, String transportType) throws CustomException
	{
		Machine machineData = new Machine();
		try
		{
			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
		}
		catch (Exception e)
		{
		}

		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("MACHINENAME", currentMachineName);
		if (currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			durableUdfs.put("PORTNAME", currentPositionName);
			durableUdfs.put("POSITIONNAME", "");
		}
		else
		{
			durableUdfs.put("PORTNAME", "");
			durableUdfs.put("POSITIONNAME", currentPositionName);
		}

		durableUdfs.put("POSITIONTYPE", currentPositionType);
		durableUdfs.put("ZONENAME", currentZoneName);
		durableUdfs.put("TRANSPORTSTATE", transferState);
		if (StringUtils.isNotEmpty(transportLockFlag))
		{
			durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);
		}

		if (StringUtils.isNotEmpty(transportType))
		{
			durableUdfs.put("TRANSPORTTYPE", transportType);
		}
		
		// SetArea Info
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(machineData.getAreaName());
		setAreaInfo.setUdfs(durableUdfs);

		durableData = DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);


		return durableData;
	}

	public MaskLot changeCurrentMaskLocation(MaskLot maskData, String currentMachineName, String currentPositionType,
			String currentPositionName, String currentZoneName, String currentCarrierName, String currentCarrierSlotNo,
			String transferState, String transportLockFlag, EventInfo eventInfo,String returnCode ,String returnMessage) throws CustomException
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		if (StringUtils.isEmpty(currentCarrierName))
		{
			currentCarrierSlotNo = "";

			if (StringUtils.isNotEmpty(maskData.getCarrierName()))
			{
				// Deassign Mask from Carrier
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskData.getCarrierName());

				int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(maskData.getCarrierName());

				// Assign Carrier
				if (durableData.getCapacity() < lotQty)
				{
					// error
					throw new CustomException("MASKINSPECTION-0002", maskData.getCarrierName());
				}

				if (lotQty <= 1)
				{
					durableData.setLotQuantity(0); // LOTQUANTITY = MASK LOT Qty
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available); 
				}
				else
				{
					durableData.setLotQuantity(lotQty - 1); // LOTQUANTITY = MASK LOT Qty
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse); 
				}

				DurableServiceProxy.getDurableService().update(durableData);

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				
				// Mantis : 0000440
				// 所有Mask CST，当TimeUseCount>TimeUseCountLimit时，MaskCST变Dirty
				if (lotQty <= 1)
				{
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUseCount(durableData, 1, eventInfo);
				}
			}
		}
		else
		{
			// Assign Mask to Carrier
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(currentCarrierName);

			if (!StringUtils.equals(currentCarrierName, maskData.getCarrierName()))
			{
				if (StringUtils.isNotEmpty(maskData.getCarrierName()))
				{
					// Deassign Mask from Old Carrier
					Durable oldDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskData.getCarrierName());

					int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(maskData.getCarrierName());

					if (oldDurableData.getCapacity() < lotQty)
					{
						// error
						throw new CustomException("MASKINSPECTION-0002", currentCarrierName);
					}

					if (lotQty <= 1)
					{
						oldDurableData.setLotQuantity(0); // LOTQUANTITY = MASK LOT Qty
						oldDurableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					else
					{
						oldDurableData.setLotQuantity(lotQty - 1); // LOTQUANTITY = MASK LOT Qty
						oldDurableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					}

					oldDurableData.setUdfs(new HashMap<String, String>());
					DurableServiceProxy.getDurableService().update(oldDurableData);

					MESDurableServiceProxy.getDurableServiceImpl().setEvent(oldDurableData, setEventInfo, eventInfo);
				}

				int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(currentCarrierName);

				// Assign Carrier
				if (durableData.getCapacity() < lotQty + 1)
				{
					// error
					throw new CustomException("MASKINSPECTION-0002", currentCarrierName);
				}

				durableData.setLotQuantity(lotQty + 1); // LOTQUANTITY = MASK LOT Qty
			}
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse); 

			if (StringUtil.equals(maskData.getCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
				durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
			
			DurableServiceProxy.getDurableService().update(durableData);

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}

		maskData.setMachineName(currentMachineName);
		maskData.setZoneName(currentZoneName);
		maskData.setCarrierName(currentCarrierName);
		maskData.setPosition(currentCarrierSlotNo);
		maskData.setTransferState(transferState);

		if (StringUtils.equals(currentPositionType, "PORT"))
		{
			maskData.setMaterialLocationName("");
			maskData.setPortName(currentPositionName);
		}
		else
		{
			maskData.setMaterialLocationName(currentPositionName);
			maskData.setPortName("");
		}

		if (StringUtils.isNotEmpty(currentCarrierName))
		{
			maskData.setCarrierName(currentCarrierName);
			maskData.setPosition(currentCarrierSlotNo);
		}
		else
		{
			maskData.setCarrierName("");
			maskData.setPosition("");
		}
		
		maskData.setLastEventComment(eventInfo.getEventComment());	
		maskData.setLastEventName(eventInfo.getEventName());
		maskData.setLastEventTime(eventInfo.getEventTime());
		maskData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskData.setLastEventUser(eventInfo.getEventUser());
		maskData.setReasonCode(eventInfo.getReasonCode());

		if (StringUtils.isNotEmpty(transportLockFlag))
		{
			// maskData.setTransportLockFlag(transportLockFlag);
		}

		maskData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);

		return maskData;
	}

	public TransportJobCommand changeDestination(TransportJobCommand transportJobCommandInfo, 
												 String oldDestinationMachineName, String oldDestinationPositionType,
												 String oldDestinationPositionName, String oldDestinationZoneName, 
												 String newDestinationMachineName, String newDestinationPositionType, 
												 String newDestinationPositionName, String newDestinationZoneName,
												 String changeState, String priority, 
												 String returnCode, String returnMessage,
												 EventInfo eventInfo) 
	throws CustomException													
	{
		if(StringUtils.equals(oldDestinationMachineName, newDestinationMachineName) &&
				   StringUtils.equals(oldDestinationPositionType, newDestinationPositionType) &&
				   StringUtils.equals(oldDestinationPositionName, newDestinationPositionName) && 
				   StringUtils.equals(oldDestinationZoneName, newDestinationZoneName))
		{
		}
		else // Case : Destination Changed
		{	
			transportJobCommandInfo.setDestinationMachineName(newDestinationMachineName);
			transportJobCommandInfo.setDestinationPositionType(newDestinationPositionType);
			transportJobCommandInfo.setDestinationPositionName(newDestinationPositionName);
			transportJobCommandInfo.setDestinationZoneName(newDestinationZoneName);
			transportJobCommandInfo.setChangeState(changeState);		
			transportJobCommandInfo.setPriority(priority);
			transportJobCommandInfo.setLastEventName(eventInfo.getEventName());
			transportJobCommandInfo.setLastEventUser(eventInfo.getEventUser());
			transportJobCommandInfo.setLastEventComment(eventInfo.getEventComment());
			transportJobCommandInfo.setLastEventTime(eventInfo.getEventTime());
			transportJobCommandInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			
			try
			{
				ExtendedObjectProxy.getTransportJobCommand().modify(eventInfo, transportJobCommandInfo);
			}
			catch(Exception e)
			{
				throw new CustomException("JOB-8012", e.getMessage());
			}
		}
				
		return transportJobCommandInfo;
	}

	public TransportJobCommand updateTransportJobCommand(String transportJobName, Document doc, EventInfo eventInfo) throws CustomException
	{
		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });
		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
		TransportJobCommand transportJobCommandInfo = sqlResult.get(0);

		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);

		transportJobCommandInfo.setJobState(jobState);
		transportJobCommandInfo.setCancelState(cancelState);
		transportJobCommandInfo.setChangeState(changeState);

		// Set REASONCODE
		transportJobCommandInfo.setReasonCode(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false));
		transportJobCommandInfo.setReasonMessage(SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false));

		// Set LASTRESULTCODE
		transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
		transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

		// Set EventInfo
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);

		Element bodyElement = doc.getDocument().getRootElement().getChild("Body");

		for (Iterator<?> iterator = bodyElement.getChildren().iterator(); iterator.hasNext();)
		{
			Element bodyItem = (Element) iterator.next();
			String itemName = bodyItem.getName().toString();
			String itemValue = bodyElement.getChildText(itemName);

			if (itemName.equals("NEWDESTINATIONMACHINENAME"))
			{
				transportJobCommandInfo.setDestinationMachineName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONPOSITIONTYPE"))
			{
				transportJobCommandInfo.setDestinationPositionType(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONPOSITIONNAME"))
			{
				transportJobCommandInfo.setDestinationPositionName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONZONENAME"))
			{
				transportJobCommandInfo.setDestinationZoneName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONCARRIERNAME"))
			{
				transportJobCommandInfo.setDestinationCarrierName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONCARRIERSLOTNO"))
			{
				itemValue = ConvertUtil.toStringForIntTypeValue(itemValue);
				transportJobCommandInfo.setDestinationCarrierSlotNo(itemValue);
				continue;
			}

			if (itemName.equals("PRIORITY"))
			{
				transportJobCommandInfo.setPriority(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTMACHINENAME"))
			{
				transportJobCommandInfo.setCurrentMachineName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTPOSITIONTYPE"))
			{
				transportJobCommandInfo.setCurrentPositionType(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTPOSITIONNAME"))
			{
				transportJobCommandInfo.setCurrentPositionName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTZONENAME"))
			{
				transportJobCommandInfo.setCurrentZoneName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTCARRIERNAME"))
			{
				transportJobCommandInfo.setCurrentCarrierName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTCARRIERSLOTNO"))
			{
				itemValue = ConvertUtil.toStringForIntTypeValue(itemValue);
				transportJobCommandInfo.setCurrentCarrierSlotNo(itemValue);
				continue;
			}

			if (itemName.equals("TRANSFERSTATE"))
			{
				transportJobCommandInfo.setTransferState(itemValue);
				continue;
			}

			if (itemName.equals("CARRIERSTATE"))
			{
				transportJobCommandInfo.setCarrierState(itemValue);
				continue;
			}

			if (itemName.equals("ALTERNATEFLAG"))
			{
				transportJobCommandInfo.setAlternateFlag(itemValue);
				continue;
			}
		}

		try
		{
			ExtendedObjectProxy.getTransportJobCommand().modify(eventInfo, transportJobCommandInfo);
		}
		catch (Exception e)
		{
			throw new CustomException("JOB-8012", e.getMessage());
		}

		return transportJobCommandInfo;
	}

	public TransportJobCommand updateTransportJobCommandForMaskBatch(String transportJobName, Document doc, EventInfo eventInfo) throws CustomException
	{
		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });
		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
		TransportJobCommand transportJobCommandInfo = sqlResult.get(0);

		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);

		transportJobCommandInfo.setJobState(jobState);
		transportJobCommandInfo.setCancelState(cancelState);
		transportJobCommandInfo.setChangeState(changeState);

		// Set REASONCODE
		transportJobCommandInfo.setReasonCode(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false));
		transportJobCommandInfo.setReasonMessage(SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false));

		// Set LASTRESULTCODE
		transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
		transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

		// Set EventInfo
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);

		Element bodyElement = doc.getDocument().getRootElement().getChild("Body");

		for (Iterator<?> iterator = bodyElement.getChildren().iterator(); iterator.hasNext();)
		{
			Element bodyItem = (Element) iterator.next();
			String itemName = bodyItem.getName().toString();
			String itemValue = bodyElement.getChildText(itemName);

			if (itemName.equals("NEWDESTINATIONMACHINENAME"))
			{
				transportJobCommandInfo.setDestinationMachineName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONPOSITIONTYPE"))
			{
				transportJobCommandInfo.setDestinationPositionType(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONPOSITIONNAME"))
			{
				transportJobCommandInfo.setDestinationPositionName(itemValue);
				continue;
			}

			if (itemName.equals("NEWDESTINATIONZONENAME"))
			{
				transportJobCommandInfo.setDestinationZoneName(itemValue);
				continue;
			}

			if (itemName.equals("PRIORITY"))
			{
				transportJobCommandInfo.setPriority(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTMACHINENAME"))
			{
				transportJobCommandInfo.setCurrentMachineName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTPOSITIONTYPE"))
			{
				transportJobCommandInfo.setCurrentPositionType(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTPOSITIONNAME"))
			{
				transportJobCommandInfo.setCurrentPositionName(itemValue);
				continue;
			}

			if (itemName.equals("CURRENTZONENAME"))
			{
				transportJobCommandInfo.setCurrentZoneName(itemValue);
				continue;
			}

			if (itemName.equals("TRANSFERSTATE"))
			{
				transportJobCommandInfo.setTransferState(itemValue);
				continue;
			}

			if (itemName.equals("CARRIERSTATE"))
			{
				transportJobCommandInfo.setCarrierState(itemValue);
				continue;
			}

			if (itemName.equals("ALTERNATEFLAG"))
			{
				transportJobCommandInfo.setAlternateFlag(itemValue);
				continue;
			}
		}

		try
		{
			ExtendedObjectProxy.getTransportJobCommand().modify(eventInfo, transportJobCommandInfo);
		}
		catch (Exception e)
		{
			throw new CustomException("JOB-8012", e.getMessage());
		}

		return transportJobCommandInfo;
	}

	public String unknownCarrierChangeName(String carrierName) throws CustomException
	{
		if(carrierName.indexOf("-") > 0)
		{
			String firstCut = carrierName.substring(carrierName.indexOf("-")+1);
			
			if(firstCut.indexOf("-") > 0)
			{
				String secondCut = firstCut.substring(0, firstCut.lastIndexOf("-"));
				carrierName = secondCut;
			}
		}
		
		return carrierName;
	}
	
	public TransportJobCommand getTransportJobInfo(String transportJobName) 
	throws CustomException													
	{
		TransportJobCommand transportJobCommandInfo = 
			ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] {transportJobName});
				
		return transportJobCommandInfo;
	}
	
	public List<Map<String, Object>> getReserveProductSpecData(
			String machineName, String processOperationGroupName, String processOperationName, String productSpecName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 " FROM CT_RESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName " +
		 " AND PROCESSOPERATIONNAME = :processOperationName " +
		 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>> getReserveProductSpecList(
			String productSpecName, String processOperationName, String machineName) throws CustomException													
	{
		String sql = "" +
		 " SELECT ROWNUM-1 SEQ, MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
		 "        RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 "  FROM CT_RESERVEPRODUCT " +
		 " WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME " +
		 " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
		 " AND MACHINENAME = :MACHINENAME " +
		 " ORDER BY POSITION ASC ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	/**
	 * 屏体CST返回OLED后自动变Dirty
	 * @author xiaoxh
	 * @since 2021.01.13 
	 * @param destnationMachine、currentMachineName、sourceMachineName、carrierName
	 * @return void
	 * @throws CustomException 
	 */
	public void postCellToOLEDDirty(String destnationMachine,String currentMachineName ,String sourceMachineName ,String carrierName )throws CustomException
	{
		Machine currentMachineData = new Machine();
		Machine destnationMachineData = new Machine();
		Machine sourceMachineData = new Machine();
		try
		{
			currentMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
		}
		catch(Exception ex)
		{
			log.info("currentMachine:" + currentMachineName + " is not exist");			
		}
		
		try
		{
			destnationMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destnationMachine);
		}
		catch(Exception ex)
		{
			log.info("destnationMachine:" + destnationMachine + " is not exist");
		}
		
		try
		{
			sourceMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sourceMachineName);
		}
		catch(Exception ex)
		{
			log.info("sourceMachine:" + sourceMachineName + " is not exist");
		}
		
		if(currentMachineData !=null && destnationMachineData !=null && sourceMachineData !=null &&StringUtils.isNotEmpty(sourceMachineData.getFactoryName())
				&& StringUtils.isNotEmpty(currentMachineData.getFactoryName())&& StringUtils.isNotEmpty(destnationMachineData.getFactoryName()))
		{
			if(sourceMachineData.getFactoryName().equals("POSTCELL")&&destnationMachineData.getFactoryName().equals("OLED")&&currentMachineData.getFactoryName().equals("OLED"))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				DirtyInfo dirtyInfo = MESDurableServiceProxy.getDurableInfoUtil().dirtyInfo(durableData, durableData.getUdfs().get("MACHINENAME"));
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "PostCell Backup OLED CST Auto change to Dirty", null, null);
				MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
			}
		}
	}
	
	//Mask未搬到最高优先级STK则EM推送
	public boolean checkTransportToFirstStock(MaskLot maskData, String transportJobName, EventInfo eventInfo) throws CustomException
	{
		boolean checkFlag = true;
		
		try
		{
			TransportJobCommand jobData = ExtendedObjectProxy.getTransportJobCommand().selectByKey(false, new Object[] {transportJobName});
			if(jobData.getDestinationMachineName().contains("3MSTK") && StringUtils.equals(jobData.getDestinationPositionType(), "SHELF") )
			{			
				if(maskData.getMaskKind().equals("EVA"))
				{
					List<Map<String, Object>> sqlResult = null;
					String sql = "SELECT * FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'StickFilmLayer' AND E.ENUMVALUE = :ENUMVALUE ";
					
					Map<String, String> bindMap = new HashMap<String, String>();
					bindMap.put("ENUMVALUE", maskData.getMaskFilmLayer());		
					
					sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);
					
					if(StringUtils.isNotEmpty(ConvertUtil.getMapValueByName(sqlResult.get(0), "DISPLAYCOLOR")) && !StringUtils.equals(ConvertUtil.getMapValueByName(sqlResult.get(0), "DISPLAYCOLOR"), maskData.getMachineName()))
					{
						checkFlag = false;
					}
				}
				else if(maskData.getMaskKind().equals("TFE"))
				{
					if(!StringUtils.equals(maskData.getMachineName(), "3MSTK03"))
					{
						checkFlag = false;
					}
				}
			}
		}
		catch(Exception e)
		{
			log.info("Check Send To Em Error");
		}
				
		return checkFlag;
	}
	public void sendToEmMaskSTK(String department, String alarmGroup, MaskLot maskData)
	{
		String[] userList = getUserList(department,alarmGroup);	
		if(userList == null || userList.length ==0) return;
		
		String message = "";
		message += "<pre>==========================AlarmInformation==================================</pre>";
	    message += "<pre>==================以下OLEDMask未搬送至优先级最高STK, 请及时处理======================</pre>";
		message += "<pre>MaskID:" + maskData.getMaskLotName() + ", MaskKind:" + maskData.getMaskKind() + ";</pre>";
		message += "<pre>MaskLayer:" + maskData.getMaskFilmLayer() + ", CurrentLocation:" + maskData.getMachineName() +";</pre>";
		message += "<pre>===============================End==========================================</pre>";			
										
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskTransportComplete", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("Mask Not TranspotToFirstSTK Start Send To Emobile & Wechat");	
						
			String title = "Mask未搬送至最高优先级STK提醒";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error ");	
		} 
	}	
	private String[] getUserList(String department, String alarmGroup)
	{
		String sql = " SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B  "
				   + " WHERE A.USERID = B.USERID  "
				   + " AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroup, department});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
	
}
