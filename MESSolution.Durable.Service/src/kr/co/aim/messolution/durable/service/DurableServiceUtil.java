package kr.co.aim.messolution.durable.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DurableServiceUtil implements ApplicationContextAware
{
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog("DurableServiceUtil");
	
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		applicationContext = arg0;
	}
	
	public Document generateIMSMaskChangeInfo(String origialSourceSubjectName,Machine lineData, Durable maskData , EventInfo eventInfo) throws CustomException                
	{
		if (lineData == null || maskData == null)
		{
			log.info("generateIMSMaskChangeInfo: Input LineData or MaskData is null.");
			
			//SYSTEM-0003:Mthod [{0}] incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0003",Thread.currentThread().getStackTrace()[1].getMethodName());
		}else 
		{
			log.info("Call Hierarchy : " + Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName());
		}

		Element rootElement = new Element(SMessageUtil.Message_Tag);

		Element headerElement = new Element(SMessageUtil.Header_Tag);
		{
			headerElement.addContent(new Element(SMessageUtil.MessageName_Tag).setText("IMSMaskChangeInfo"));
			headerElement.addContent(new Element("SHOPNAME").setText(lineData.getFactoryName()));
			headerElement.addContent(new Element("MACHINENAME").setText(lineData.getKey().getMachineName()));
			headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
			headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(origialSourceSubjectName));
			headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("CNXsvr")));
			headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(lineData.getUdfs().get("MCSUBJECTNAME")));
			headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
			headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
			headerElement.addContent(new Element("LANGUAGE").setText("ENG"));

			rootElement.addContent(headerElement);
		}

		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		{
			bodyElement.addContent(new Element("LINENAME").setText(lineData.getKey().getMachineName()));
			bodyElement.addContent(new Element("MASKID").setText(maskData.getKey().getDurableName()));
			bodyElement.addContent(new Element("VENDOR").setText(maskData.getUdfs().get("VENDORNUMBER")));
			bodyElement.addContent(new Element("MASKTSTATE").setText(maskData.getDurableState()));
			bodyElement.addContent(new Element("CLEANSTATE").setText(maskData.getDurableCleanState()));
			bodyElement.addContent(new Element("HOLDSTATE").setText(maskData.getUdfs().get("DURABLEHOLDSTATE")));
			bodyElement.addContent(new Element("TRANSFERSTATE").setText(maskData.getUdfs().get("TRANSPORTSTATE")));

			rootElement.addContent(bodyElement);
		}

		return new Document(rootElement);
	}
	
	@SuppressWarnings("unchecked")
	public List<String>  generateCarrierName(String ruleName,
			String durableSpecName,
			String quantity)
	{
		List<String> argSeq = new ArrayList<String>();
		
		String sql = "SELECT SQL_TEXT " +
					 "FROM NAMEGENERATORRULEDEF " +
				     "WHERE RULENAME = :ruleName ";				
		
		Map<String, String> bindMap = new HashMap<String, String>();
		
		bindMap.put("ruleName", ruleName);
		
		List<Map<String, Object>> sqlResult 
		  = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		String sSqlText = "";
		
	    if( sqlResult.size()> 0 ) {
	    	sSqlText = sqlResult.get(0).get("SQL_TEXT").toString();
	    } else {
	    	
	    }
		
		bindMap.clear();
		sqlResult.clear();
		bindMap = new HashMap<String, String>();
		bindMap.put("durableSpecName", durableSpecName);
		
		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sSqlText, bindMap);
		
 		
			
		for( int i = 0; i < sqlResult.size(); i++ ) {
			
			argSeq.add(sqlResult.get(i).get("ENUMVALUE").toString());
		}
		
		return argSeq;		
	}
	
	public Durable getDurableDataForUpdate(String durableName) throws  CustomException
	{
		if (StringUtils.isEmpty(durableName))
		{
			throw new CustomException("CST-0001", durableName);
		}

		try
		{
			Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(durableName));
			return durableData;
		}
		catch (NotFoundSignal notFoundEx)
		{
			throw new CustomException("CST-0001", durableName);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	public Durable getDurableData(String durableName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (StringUtils.isEmpty(durableName))
		{
			throw new CustomException("CST-0001", durableName);
		}

		try
		{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(durableName);

			Durable durableData = null;
			durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

			return durableData;
		}
		catch (Exception e)
		{
			throw new CustomException("CST-0001", durableName);
		}
	}

	public void updateCarrierLocation(EventInfo eventInfo, String currentMachineName, String currentPositionName, String transferState, String carrierName) 
	{
		try{
			if(StringUtils.isNotEmpty(currentMachineName)&&null!=currentMachineName&&currentMachineName.contains("FFBC05")&&null!=carrierName&&StringUtils.isNotEmpty(carrierName))
			{
				DurableKey durableKey = new DurableKey();
				durableKey.setDurableName(carrierName);

				SetEventInfo setEventInfo = new SetEventInfo();

				setEventInfo.getUdfs().put("MACHINENAME", currentMachineName);
				setEventInfo.getUdfs().put("POSITIONNAME", currentPositionName);
				setEventInfo.getUdfs().put("TRANSPORTSTATE", transferState);

				DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo,
						setEventInfo);
				log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
						+ eventInfo.getEventTimeKey());
			}
		}
		catch(Exception e){
			
		}
	}
	
	public void insertDurableHistory(EventInfo eventInfo, Map<String, Durable> newDurableDataMap, Map<String, Durable> oldDurableDataMap) throws CustomException
	{
		// History insert Query & insertArgList
		String queryStringHistory = 
				"INSERT INTO DURABLEHISTORY  " +
						"   (DURABLENAME, TIMEKEY, EVENTTIME, EVENTNAME, OLDDURABLESPECNAME,   " +
						"    DURABLESPECNAME, OLDDURABLESPECVERSION, DURABLESPECVERSION, MATERIALLOCATIONNAME, TRANSPORTGROUPNAME,   " +
						"    TIMEUSEDLIMIT, TIMEUSED, DURATIONUSEDLIMIT, DURATIONUSED, CAPACITY,   " +
						"    LOTQUANTITY, OLDFACTORYNAME, FACTORYNAME, OLDAREANAME, AREANAME,   " +
						"    DURABLESTATE, DURABLECLEANSTATE, EVENTUSER, EVENTCOMMENT, EVENTFLAG,   " +
						"    REASONCODETYPE, REASONCODE, TOTALUSEDCOUNT, LASTCLEANTIME, DURABLEHOLDSTATE,   " +
						"    TRANSPORTLOCKFLAG, TRANSPORTSTATE, POSITIONTYPE, MACHINENAME, UNITNAME,   " +
						"    PORTNAME, POSITIONNAME, ZONENAME, CLEANUSED, MACHINERECIPENAME,   " +
						"    CLEANUSEDLIMIT, TRANSPORTTYPE, DEPARTMENT, RETICLESLOT, CHAMBERUSETYPE,   " +
						"    KITTIME, UNKITTIME, ACCUMULATEUSEDTIME, CANCELTIMEKEY, CANCELFLAG,   " +
						"    SYSTEMTIME, CONSUMERLOTNAME, CONSUMERPRODUCTNAME, CONSUMERTIMEKEY, CONSUMERPONAME,   " +
						"    CONSUMERPOVERSION, PPBOXGRADE, PPBOXSTATE, REPAIRCOUNT, REPAIRTIME,   " +
						"    VENDORNUMBER, COVERNAME, POSITION, RESERVEMASKSTOCKER, DURABLETYPE1)  " +
						" VALUES  " +
						"   (?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?)  " ;
		
		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();	
		
		for (Durable newDurableData : newDurableDataMap.values()) 
		{
			EventInfo newEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());	
			newEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
			newEventInfo.setEventTime(eventInfo.getEventTime());
			
			Durable oldDurableData = oldDurableDataMap.get(newDurableData.getKey().getDurableName());
			
			// insert History
			List<Object> bindList = new ArrayList<Object>();	
			bindList.add(newDurableData.getKey().getDurableName());
			bindList.add(newEventInfo.getEventTimeKey());
			bindList.add(newEventInfo.getEventTime());
			bindList.add(newEventInfo.getEventName());
			bindList.add(oldDurableData.getDurableSpecName());
			bindList.add(newDurableData.getDurableSpecName());
			bindList.add(oldDurableData.getDurableSpecVersion());
			bindList.add(newDurableData.getDurableSpecVersion());
			bindList.add(newDurableData.getMaterialLocationName());
			bindList.add(newDurableData.getTransportGroupName());
			bindList.add(newDurableData.getTimeUsedLimit());
			bindList.add(newDurableData.getTimeUsed());
			bindList.add(newDurableData.getDurationUsedLimit());
			bindList.add(newDurableData.getDurationUsed());
			bindList.add(newDurableData.getCapacity());
			bindList.add(newDurableData.getLotQuantity());
			bindList.add(oldDurableData.getFactoryName());
			bindList.add(newDurableData.getFactoryName());
			bindList.add(oldDurableData.getAreaName());
			bindList.add(newDurableData.getAreaName());
			bindList.add(newDurableData.getDurableState());
			bindList.add(newDurableData.getDurableCleanState());
			bindList.add(eventInfo.getEventUser());
			bindList.add(eventInfo.getEventComment());
			bindList.add("N"); //eventflag
			bindList.add(newDurableData.getReasonCodeType());
			bindList.add(newDurableData.getReasonCode());
			bindList.add(newDurableData.getUdfs().get("TOTALUSEDCOUNT"));
			bindList.add(newDurableData.getUdfs().get("LASTCLEANTIME"));
			bindList.add(newDurableData.getUdfs().get("DURABLEHOLDSTATE"));
			bindList.add(newDurableData.getUdfs().get("TRANSPORTLOCKFLAG"));
			bindList.add(newDurableData.getUdfs().get("TRANSPORTSTATE"));
			bindList.add(newDurableData.getUdfs().get("POSITIONTYPE"));
			bindList.add(newDurableData.getUdfs().get("MACHINENAME"));
			bindList.add(newDurableData.getUdfs().get("UNITNAME"));
			bindList.add(newDurableData.getUdfs().get("PORTNAME"));
			bindList.add(newDurableData.getUdfs().get("POSITIONNAME"));
			bindList.add(newDurableData.getUdfs().get("ZONENAME"));
			bindList.add(newDurableData.getUdfs().get("CLEANUSED"));
			bindList.add(newDurableData.getUdfs().get("MACHINERECIPENAME"));
			bindList.add(newDurableData.getUdfs().get("CLEANUSEDLIMIT"));
			bindList.add(newDurableData.getUdfs().get("TRANSPORTTYPE"));
			bindList.add(newDurableData.getUdfs().get("DEPARTMENT"));
			bindList.add(newDurableData.getUdfs().get("RETICLESLOT"));
			bindList.add(newDurableData.getUdfs().get("CHAMBERUSETYPE"));
			
			bindList.add(newDurableData.getUdfs().get("KITTIME"));
			bindList.add(newDurableData.getUdfs().get("UNKITTIME"));
			bindList.add(newDurableData.getUdfs().get("ACCUMULATEUSEDTIME"));
			bindList.add(newDurableData.getUdfs().get("CANCELTIMEKEY"));
			bindList.add(newDurableData.getUdfs().get("CANCELFLAG"));
			
			bindList.add(newDurableData.getUdfs().get("SYSTEMTIME"));
			bindList.add(newDurableData.getUdfs().get("CONSUMERLOTNAME"));
			bindList.add(newDurableData.getUdfs().get("CONSUMERPRODUCTNAME"));
			bindList.add(newDurableData.getUdfs().get("CONSUMERTIMEKEY"));
			bindList.add(newDurableData.getUdfs().get("CONSUMERPONAME"));
			
			bindList.add(newDurableData.getUdfs().get("CONSUMERPOVERSION"));
			bindList.add(newDurableData.getUdfs().get("PPBOXGRADE"));
			bindList.add(newDurableData.getUdfs().get("PPBOXSTATE"));
			bindList.add(newDurableData.getUdfs().get("REPAIRCOUNT"));
			bindList.add(newDurableData.getUdfs().get("REPAIRTIME"));
			
			bindList.add(newDurableData.getUdfs().get("VENDORNUMBER"));
			bindList.add(newDurableData.getUdfs().get("COVERNAME"));
			bindList.add(newDurableData.getUdfs().get("POSITION"));
			bindList.add(newDurableData.getUdfs().get("RESERVEMASKSTOCKER"));
			bindList.add(newDurableData.getUdfs().get("DURABLETYPE1"));

			insertArgListHistory.add(bindList.toArray());
		}
		
		if(insertArgListHistory.size() > 0)
		{
			try
			{
				if(insertArgListHistory.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory, insertArgListHistory.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryStringHistory, insertArgListHistory);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}
	
	public void insertDurableHistory(EventInfo eventInfo, List<Durable> durList , List<Durable> oldDurList) throws CustomException
	{
		// History insert Query & insertArgList
		String queryStringHistory = 
				"INSERT INTO DURABLEHISTORY  " +
						"   (DURABLENAME, TIMEKEY, EVENTTIME, EVENTNAME, OLDDURABLESPECNAME,   " +
						"    DURABLESPECNAME, OLDDURABLESPECVERSION, DURABLESPECVERSION, MATERIALLOCATIONNAME, TRANSPORTGROUPNAME,   " +
						"    TIMEUSEDLIMIT, TIMEUSED, DURATIONUSEDLIMIT, DURATIONUSED, CAPACITY,   " +
						"    LOTQUANTITY, OLDFACTORYNAME, FACTORYNAME, OLDAREANAME, AREANAME,   " +
						"    DURABLESTATE, DURABLECLEANSTATE, EVENTUSER, EVENTCOMMENT, EVENTFLAG,   " +
						"    REASONCODETYPE, REASONCODE, TOTALUSEDCOUNT, LASTCLEANTIME, DURABLEHOLDSTATE,   " +
						"    TRANSPORTLOCKFLAG, TRANSPORTSTATE, POSITIONTYPE, MACHINENAME, UNITNAME,   " +
						"    PORTNAME, POSITIONNAME, ZONENAME, CLEANUSED, MACHINERECIPENAME,   " +
						"    CLEANUSEDLIMIT, TRANSPORTTYPE, DEPARTMENT, RETICLESLOT, CHAMBERUSETYPE,   " +
						"    KITTIME, UNKITTIME, ACCUMULATEUSEDTIME, CANCELTIMEKEY, CANCELFLAG,   " +
						"    SYSTEMTIME, CONSUMERLOTNAME, CONSUMERPRODUCTNAME, CONSUMERTIMEKEY, CONSUMERPONAME,   " +
						"    CONSUMERPOVERSION, PPBOXGRADE, PPBOXSTATE, REPAIRCOUNT, REPAIRTIME,   " +
						"    VENDORNUMBER, COVERNAME, POSITION, RESERVEMASKSTOCKER, DURABLETYPE1)  " +
						" VALUES  " +
						"   (?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?,   " +
						"    ?, ?, ?, ?, ?)  " ;
		
		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();	
		
		for(Durable durData : durList){
			EventInfo newEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());	
			newEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
			newEventInfo.setEventTime(eventInfo.getEventTime());
			
			Durable oldDur = new Durable();
			if(oldDurList.size() > 0)
			{
				for(Durable oldDurData : oldDurList)
				{
					if(durData.getKey().getDurableName().equals(oldDurData.getKey().getDurableName()))
					{
						oldDur = oldDurData;
					}
				}
			}
			
			// insert History
			List<Object> bindList = new ArrayList<Object>();	
			bindList.add(durData.getKey().getDurableName());
			bindList.add(newEventInfo.getEventTimeKey());
			bindList.add(newEventInfo.getEventTime());
			bindList.add(newEventInfo.getEventName());
			bindList.add(oldDur.getDurableSpecName());
			bindList.add(durData.getDurableSpecName());
			bindList.add(oldDur.getDurableSpecVersion());
			bindList.add(durData.getDurableSpecVersion());
			bindList.add(durData.getMaterialLocationName());
			bindList.add(durData.getTransportGroupName());
			bindList.add(durData.getTimeUsedLimit());
			bindList.add(durData.getTimeUsed());
			bindList.add(durData.getDurationUsedLimit());
			bindList.add(durData.getDurationUsed());
			bindList.add(durData.getCapacity());
			bindList.add(durData.getLotQuantity());
			bindList.add(oldDur.getFactoryName());
			bindList.add(durData.getFactoryName());
			bindList.add(oldDur.getAreaName());
			bindList.add(durData.getAreaName());
			bindList.add(durData.getDurableState());
			bindList.add(durData.getDurableCleanState());
			bindList.add(eventInfo.getEventUser());
			bindList.add(eventInfo.getEventComment());
			bindList.add("N"); //eventflag
			bindList.add(durData.getReasonCodeType());
			bindList.add(durData.getReasonCode());
			bindList.add(durData.getUdfs().get("TOTALUSEDCOUNT"));
			bindList.add(durData.getUdfs().get("LASTCLEANTIME"));
			bindList.add(durData.getUdfs().get("DURABLEHOLDSTATE"));
			bindList.add(durData.getUdfs().get("TRANSPORTLOCKFLAG"));
			bindList.add(durData.getUdfs().get("TRANSPORTSTATE"));
			bindList.add(durData.getUdfs().get("POSITIONTYPE"));
			bindList.add(durData.getUdfs().get("MACHINENAME"));
			bindList.add(durData.getUdfs().get("UNITNAME"));
			bindList.add(durData.getUdfs().get("PORTNAME"));
			bindList.add(durData.getUdfs().get("POSITIONNAME"));
			bindList.add(durData.getUdfs().get("ZONENAME"));
			bindList.add(durData.getUdfs().get("CLEANUSED"));
			bindList.add(durData.getUdfs().get("MACHINERECIPENAME"));
			bindList.add(durData.getUdfs().get("CLEANUSEDLIMIT"));
			bindList.add(durData.getUdfs().get("TRANSPORTTYPE"));
			bindList.add(durData.getUdfs().get("DEPARTMENT"));
			bindList.add(durData.getUdfs().get("RETICLESLOT"));
			bindList.add(durData.getUdfs().get("CHAMBERUSETYPE"));
			
			bindList.add(durData.getUdfs().get("KITTIME"));
			bindList.add(durData.getUdfs().get("UNKITTIME"));
			bindList.add(durData.getUdfs().get("ACCUMULATEUSEDTIME"));
			bindList.add(durData.getUdfs().get("CANCELTIMEKEY"));
			bindList.add(durData.getUdfs().get("CANCELFLAG"));
			
			bindList.add(durData.getUdfs().get("SYSTEMTIME"));
			bindList.add(durData.getUdfs().get("CONSUMERLOTNAME"));
			bindList.add(durData.getUdfs().get("CONSUMERPRODUCTNAME"));
			bindList.add(durData.getUdfs().get("CONSUMERTIMEKEY"));
			bindList.add(durData.getUdfs().get("CONSUMERPONAME"));
			
			bindList.add(durData.getUdfs().get("CONSUMERPOVERSION"));
			bindList.add(durData.getUdfs().get("PPBOXGRADE"));
			bindList.add(durData.getUdfs().get("PPBOXSTATE"));
			bindList.add(durData.getUdfs().get("REPAIRCOUNT"));
			bindList.add(durData.getUdfs().get("REPAIRTIME"));
			
			bindList.add(durData.getUdfs().get("VENDORNUMBER"));
			bindList.add(durData.getUdfs().get("COVERNAME"));
			bindList.add(durData.getUdfs().get("POSITION"));
			bindList.add(durData.getUdfs().get("RESERVEMASKSTOCKER"));
			bindList.add(durData.getUdfs().get("DURABLETYPE1"));

			insertArgListHistory.add(bindList.toArray());
		}
		
		if(insertArgListHistory.size() > 0)
		{
			try
			{
				if(insertArgListHistory.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory, insertArgListHistory.get(0));
					//GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory, insertArgListHistory.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryStringHistory, insertArgListHistory);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}
	
	public static Element createMaskInfoElement(Machine machineData,String sMaskCSTName, String subUnitName,List<ReserveMaskList> maskListData, String maskCSTtransportGroupName) throws CustomException 
	{
		//Get maskList
		Element maskListElement = new Element("MASKLIST");
		
		for (int i = 0; i < maskListData.size(); i++)  
		{
			//Set MaskList Items
			String maskName = null;
			String maskGroupname = null;
			String position   = null;
			String masktype  = null;
			String maskMachineRecipeName = null;
			String maskReworkRecipeName = null;
			String maskAHMSCurrentRecipe = null;
			int maskUsedLimit =0;
			int maskUseCount =0;
			String productSpecname = null;
			String chamberID = null;
			String Thickness = null;
			String Magnet = null;
			String sslotNo = null;
			String offset_X;
			String offset_Y;
			String offset_T;
			
			maskName = maskListData.get(i).getMaskName();
			
			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
			
			DurableSpec maskSpec = CommonUtil.getDurableSpecByDurableName(maskName);
			if(! StringUtils.equals(maskData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
			{
				throw new CustomException("MASK-0025",maskName);
			}
			if(StringUtils.equals(maskData.getDurableState(), "Scrapped"))
			{
				throw new CustomException("MASK-0012",maskName);
			}
			if(StringUtils.equals(maskData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
			{
				throw new CustomException("MASK-0013",maskName);
			}
			
			maskGroupname = "";
			position   = maskListData.get(i).getPosition();
			
		     //get MaskType form NamingRule DOC
			if(maskName.toString().toUpperCase().substring(1, 2).equals("F") == true)
			{
				masktype = "FMM"; 
			}
            else if (StringUtil.equals(maskName.substring(1,2).toUpperCase(), "C") == true)
            {
            	masktype = "CMM";
            }else
            {
            	masktype = maskData.getDurableType();
            }
			
			//getDurableSpec
			maskMachineRecipeName = maskData.getUdfs().get("MACHINERECIPENAME"); 
			maskReworkRecipeName = maskData.getUdfs().get("MASKNGRECIPENAME");
			
			String carrierType = maskListData.get(0).getCarrierType().substring(1, 2); //[ MP | MR ] ConverTo 'P' or 'R'
			
			if(carrierType.equals("P") == true)
			{
				maskAHMSCurrentRecipe = maskMachineRecipeName;
			}
            else
            {
            	maskAHMSCurrentRecipe = maskReworkRecipeName;
            }

			maskUsedLimit =  (int)maskData.getTimeUsedLimit();
			maskUseCount = (int)maskData.getTimeUsed();
			
			offset_X = maskData.getUdfs().get("OFFSET_X");
			offset_Y = maskData.getUdfs().get("OFFSET_Y");
			offset_T = maskData.getUdfs().get("OFFSET_T");
			//get PRODUCTSPECNAME
			productSpecname = maskSpec.getUdfs().get("PRODUCTSPECNAME");
			
			Thickness = maskListData.get(i).getMaskThickNess();
			Magnet = maskListData.get(i).getMaskMagnet();
			
			chamberID = maskListData.get(i).getSubUnitName();
			sslotNo = maskListData.get(i).getSSlotNo();
			
			Element maskElement = new Element("MASK");
			Element eleMaskName = new Element("MASKNAME");
			eleMaskName.setText(maskName);
			maskElement.addContent(eleMaskName);
			
			Element eleMaskGroupName = new Element("MASKGROUPNAME");
			eleMaskGroupName.setText(maskGroupname);
			maskElement.addContent(eleMaskGroupName);
			
			Element elePosition = new Element("POSITION");
			elePosition.setText(position);
			maskElement.addContent(elePosition);
			
			Element eleMaskType = new Element("MASKTYPE");
			eleMaskType.setText(masktype);
			maskElement.addContent(eleMaskType);
			
			Element eleMaskMachineRecipe = new Element("MASKRECIPENAME");
			eleMaskMachineRecipe.setText(maskAHMSCurrentRecipe);
			maskElement.addContent(eleMaskMachineRecipe);
		
			Element eleMaskUsedLimit = new Element("MASKUSEDLIMIT");
			eleMaskUsedLimit.setText(String.valueOf(maskUsedLimit));
			maskElement.addContent(eleMaskUsedLimit);
			
			Element eleMaskUsedCount = new Element("MASKUSEDCOUNT");
			eleMaskUsedCount.setText(String.valueOf(maskUseCount));
			maskElement.addContent(eleMaskUsedCount);
			
			Element eleMadkMagnet = new Element("MASKMAGNET");
			eleMadkMagnet.setText(Magnet);
			maskElement.addContent(eleMadkMagnet);
			
			Element eleProductSpecName = new Element("PRODUCTNAME");
			eleProductSpecName.setText(productSpecname);
			maskElement.addContent(eleProductSpecName);
			
			Element eleSubUnitName = new Element("SUBUNITNAME");
			eleSubUnitName.setText(chamberID);
			maskElement.addContent(eleSubUnitName);
			
			Element eleMask_OffSet_X = new Element("MASK_OFFSET_X");
			eleMask_OffSet_X.setText(offset_X);
			maskElement.addContent(eleMask_OffSet_X);
			
			Element eleMask_OffSet_Y = new Element("MASK_OFFSET_Y");
			eleMask_OffSet_Y.setText(offset_Y);
			maskElement.addContent(eleMask_OffSet_Y);
			
			Element eleSSlotNo = new Element("SSLOTNO");
			eleSSlotNo.setText(sslotNo);
			maskElement.addContent(eleSSlotNo);
			
			Element eleMaskThk = new Element("MASKTHICKNESS");
			eleMaskThk.setText(Thickness);
			maskElement.addContent(eleMaskThk);
			
			Element eleMask_OffSet_T = new Element("MASK_OFFSET_T");
			eleMask_OffSet_T.setText(offset_T);
			maskElement.addContent(eleMask_OffSet_T);
			
			Element eleMaskSpec = new Element("MASKSPEC");
			eleMaskSpec.setText(maskSpec.getKey().getDurableSpecName());
			maskElement.addContent(eleMaskSpec);
			
			Element eleSpare1 = new Element("SPARE1");
			eleSpare1.setText("");
			maskElement.addContent(eleSpare1);
			
			Element eleSpare2 = new Element("SPARE2");
			eleSpare2.setText("");
			maskElement.addContent(eleSpare2);
		
			maskListElement.addContent(maskElement);
		}
		
		return maskListElement;
	}

	public static Element createMaskInfoElementBody(Machine machineData,String unitName, String subUnitName, List<ReserveMaskList> ResvMaskList,Durable durableCSTData, String portname, String porttype, String portUseType, int maskQTY,String slotMap) throws CustomException 
	{	
		// Set Body Item
	    String machineName = null;
		String unitname =  null;
		String subunitname = null;
		String sMaskCSTName = null;
		String carrierType = null;
		String portName = null;
		String portType = null;
		String portuseType = null;
		
		machineName = machineData.getKey().getMachineName();
		
		sMaskCSTName = durableCSTData.getKey().getDurableName();
		
		// When EVA Inline.
		if(machineName.indexOf("EVA")> -1)
		{
			subunitname = ResvMaskList.get(0).getPortName();
			unitname = subunitname.substring(0, 10);
			portType = "";
			portName = "";
			portuseType =  "";
		}
		// When AHMS Inline.
		else if(machineName.indexOf("MSK")> -1)
		{
			unitname = unitName;
			subunitname = subUnitName;
			portType = porttype;
			portName = portname;
			portuseType = portUseType;
		}
    	carrierType = durableCSTData.getDurableSpecName().substring(1, 2);
		
		// Create Body Element
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineElement = new Element("MACHINENAME");
		machineElement.setText(machineName);
		bodyElement.addContent(machineElement);

		Element unitnameElement = new Element("UNITNAME"	);
		unitnameElement.setText(unitname);
		bodyElement.addContent(unitnameElement);

		Element subunitnameElement = new Element("SUBUNITNAME");
		subunitnameElement.setText(subunitname);
		bodyElement.addContent(subunitnameElement);

		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(sMaskCSTName);
		bodyElement.addContent(carrierNameElement);

		Element carrierTypeElement = new Element("CARRIERTYPE");
		carrierTypeElement.setText(carrierType);
		bodyElement.addContent(carrierTypeElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		bodyElement.addContent(portTypeElement); 

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(portuseType);
		bodyElement.addContent(portUseTypeElement);
		
		Element maskQTYElement = new Element("MASKQUANTITY");
		maskQTYElement.setText(String.valueOf(maskQTY));
		bodyElement.addContent(maskQTYElement);
		
		Element slotMapElement = new Element("SLOTMAP");
		slotMapElement.setText(slotMap);
		bodyElement.addContent(slotMapElement);

		return bodyElement;
	}
	
	public List<Durable> checkExistMaskList(String maskCarrierName, String durabletype) throws CustomException {
		List<Durable> maskList;

		String condition = "WHERE maskCarrierName = ?" + "AND durableType = ?";

		Object[] bindSet = new Object[] { maskCarrierName, durabletype };

		try {
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);

		} catch (Exception ex) {
			maskList = null;
		}
		log.info("maskList.Size is  " + maskList.size());
		return maskList;

	}
	
	public static String getMaskSlotMapInfo(Durable durableData, List<Durable> maskList ){
		String normalSlotInfo = "";
		
		StringBuffer normalSlotInfoBuffer = new StringBuffer();
		
		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity(); 
		
		// Get Product's Slot , These are not Scrapped Product.
		// Make Durable Normal SlotMapInfo
		for( int i = 0; i < iCapacity; i++ ){
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		log.debug("Normal Slot Map : " + normalSlotInfoBuffer );
		
		for( int i = 0; i < maskList.size(); i++ ){
			if(StringUtils.isNotEmpty(maskList.get(i).getUdfs().get("MASKPOSITION")))
			{
				int index = Integer.parseInt(maskList.get(i).getUdfs().get("MASKPOSITION"))- 1;
				normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}else 
			{
				log.error("MaskPostion is Null ! MaskName : "+ maskList.get(i).getKey().getDurableName());
			}
			
		}
		log.debug("Completed Slot Map : " + normalSlotInfoBuffer );
		
		normalSlotInfo = normalSlotInfoBuffer.toString();
		
		return normalSlotInfo;
	}
	
	public static List<Durable> getMaskInfoBydurableName(String carrierName,String durabletype) throws CustomException{ 
		String condition = "WHERE durabletype = ? and MASKCARRIERNAME = ? and DURABLESTATE <> 'Scrapped' order by MASKPOSITION";
						
		Object[] bindSet = new Object[] {durabletype,carrierName,};
		List<Durable> maskList = new ArrayList<Durable>();
		try
		{
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			maskList = null;
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql());
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		return maskList;
	}

	public static Element createMaskInfoElementPL(Machine machineData,String sMaskCSTName, String subUnitName,List<Durable> maskListData, String maskCSTtransportGroupName) throws CustomException 
	{		
		Element maskListElement = new Element("MASKLIST");
		
		for (int i = 0; i < maskListData.size(); i++)  
		{
			
			//Set MaskList Items
			String maskName = null;
			String maskGroupname = null;
			String position   = null;
			String masktype  = null;
			String maskMachineRecipeName = null;
			String maskReworkRecipeName = null;
			String maskAHMSCurrentRecipe = null;
			int maskUsedLimit =0;
			int maskUseCount =0;
			String productSpecname = null;
			String chamberID = null;
			String Thickness = null;
			String Magnet = null;
			String sslotNo = null;
			String offset_X;
			String offset_Y;
			String offset_T;
			String turnCount = null;
			
			maskName = maskListData.get(i).getKey().getDurableName();
			
			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
			
			DurableSpec maskSpec = CommonUtil.getDurableSpecByDurableName(maskName);
			if(StringUtils.equals(maskData.getDurableState(), "Scrapped"))
			{
				throw new CustomException("MASK-0012",maskName);
			}
			if(StringUtils.equals(maskData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
			{
				throw new CustomException("MASK-0013",maskName);
			}
			
			maskGroupname = "";
			position   = maskData.getUdfs().get("MASKPOSITION");
			
		     //get MaskType form NamingRule DOC
			if(maskName.toString().toUpperCase().substring(1, 2).equals("F") == true)
			{
				masktype = "FMM"; 
			}
            else if (StringUtil.equals(maskName.substring(1,2).toUpperCase(), "C") == true)
            {
            	masktype = "CMM";
            }else
            {
            	masktype = maskData.getDurableType();
            }
			
			maskMachineRecipeName = maskData.getUdfs().get("MACHINERECIPENAME"); 
			maskReworkRecipeName = maskData.getUdfs().get("MASKNGRECIPENAME");
			//getDurableSpec
			Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskCSTName);
			String carrierType = maskCSTData.getDurableSpecName().substring(1, 2); //[ MP | MR ] ConverTo 'P' or 'R'
			
			if(carrierType.equals("P") == true)
			{
				maskAHMSCurrentRecipe = maskMachineRecipeName;
			}
            else
            {
            	maskAHMSCurrentRecipe = maskReworkRecipeName;
            }
			maskUsedLimit =  (int)maskData.getTimeUsedLimit();
			maskUseCount = (int)maskData.getTimeUsed();
			turnCount  = maskData.getUdfs().get("TURNCOUNT");
			
			offset_X = maskData.getUdfs().get("OFFSET_X");
			offset_Y = maskData.getUdfs().get("OFFSET_Y");
			offset_T = maskData.getUdfs().get("OFFSET_T");
			
			Element maskElement = new Element("MASK");
			Element eleMaskName = new Element("MASKNAME");
			eleMaskName.setText(maskName);
			maskElement.addContent(eleMaskName);
			
			Element eleMaskGroupName = new Element("MASKGROUPNAME");
			eleMaskGroupName.setText(maskGroupname);
			maskElement.addContent(eleMaskGroupName);
			
			Element elePosition = new Element("POSITION");
			elePosition.setText(position);
			maskElement.addContent(elePosition);
			
			Element eleMaskType = new Element("MASKTYPE");
			eleMaskType.setText(masktype);
			maskElement.addContent(eleMaskType);
			
			Element eleMaskMachineRecipe = new Element("MASKRECIPENAME");
			eleMaskMachineRecipe.setText(maskAHMSCurrentRecipe);
			maskElement.addContent(eleMaskMachineRecipe);
		
			Element eleMaskUsedLimit = new Element("MASKUSEDLIMIT");
			eleMaskUsedLimit.setText(String.valueOf(maskUsedLimit));
			maskElement.addContent(eleMaskUsedLimit);
			
			Element eleMaskUsedCount = new Element("MASKUSEDCOUNT");
			eleMaskUsedCount.setText(String.valueOf(maskUseCount));
			maskElement.addContent(eleMaskUsedCount);
			
			Element eleMadkMagnet = new Element("MASKMAGNET");
			eleMadkMagnet.setText(Magnet);
			maskElement.addContent(eleMadkMagnet);
			
			Element eleProductSpecName = new Element("PRODUCTNAME");
			eleProductSpecName.setText(productSpecname);
			maskElement.addContent(eleProductSpecName);
			
			Element eleSubUnitName = new Element("SUBUNITNAME");
			eleSubUnitName.setText(chamberID);
			maskElement.addContent(eleSubUnitName);
			
			Element eleMask_OffSet_X = new Element("MASK_OFFSET_X");
			eleMask_OffSet_X.setText(offset_X);
			maskElement.addContent(eleMask_OffSet_X);
			
			Element eleMask_OffSet_Y = new Element("MASK_OFFSET_Y");
			eleMask_OffSet_Y.setText(offset_Y);
			maskElement.addContent(eleMask_OffSet_Y);
			
			Element eleSSlotNo = new Element("SSLOTNO");
			eleSSlotNo.setText(sslotNo);
			maskElement.addContent(eleSSlotNo);
			
			Element eleMaskThk = new Element("MASKTHICKNESS");
			eleMaskThk.setText(Thickness);
			maskElement.addContent(eleMaskThk);
			
			Element eleMask_OffSet_T = new Element("MASK_OFFSET_T");
			eleMask_OffSet_T.setText(offset_T);
			maskElement.addContent(eleMask_OffSet_T);
		
			Element eleMaskSpec = new Element("MASKSPEC");
			eleMaskSpec.setText(maskSpec.getKey().getDurableSpecName());
			maskElement.addContent(eleMaskSpec);
			
			Element eleTurnCount = new Element("TURNCOUNT");
			eleTurnCount.setText(turnCount);
			maskElement.addContent(eleTurnCount);
			
			Element eleSpare1 = new Element("SPARE1");
			eleSpare1.setText("");
			maskElement.addContent(eleSpare1);
			
			Element eleSpare2 = new Element("SPARE2");
			eleSpare2.setText("");
			maskElement.addContent(eleSpare2);
		
			maskListElement.addContent(maskElement);
		}
		
		return maskListElement;
	}
	
	public static List<Durable> getInUseProbeIDList() throws CustomException{ 
		
		String condition = " WHERE DURABLETYPE = 'Probe' AND DURABLESTATE <> 'Scrapped'  AND DURABLESTATE = 'InUse' AND MACHINENAME IS NOT NULL ORDER BY DURABLENAME ";
						
		Object[] bindSet = new Object[] {};
		List<Durable> probeIdList = new ArrayList<Durable>();
		try
		{
			probeIdList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(Exception de)
		{ 
			probeIdList = null;		
		}
		return probeIdList;
	}
	
	public List<Durable> getSubTrayListByCoverTray(String coverName,boolean setLock) throws CustomException
	{
		
		String condition = " WHERE DURABLETYPE = 'Tray' AND COVERNAME = ? ORDER BY TO_NUMBER(POSITION) ";
		
		if (setLock)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM DURABLE WHERE DURABLETYPE = 'Tray' AND COVERNAME = ? FOR UPDATE NOWAIT ", new Object[] { coverName });
				setLock = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}
			
			if (setLock)condition += " FOR UPDATE ";
		}
		
		List<Durable> trayList = new ArrayList<Durable>();

		try
		{
			trayList = DurableServiceProxy.getDurableService().select(condition, new Object[] { coverName });
		}
		catch (Exception ex)
		{
			if (!(ex instanceof NotFoundSignal))
				throw new CustomException(ex.getCause());
		}

		return trayList;
	}
	
	public List<Durable> getSubTrayListByCoverTrayAllowNull(String coverName,boolean setLock) throws CustomException
	{
		
		String condition = " WHERE DURABLETYPE = 'Tray' AND COVERNAME = ? ORDER BY TO_NUMBER(POSITION) ";
		
		if (setLock)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM DURABLE WHERE DURABLETYPE = 'Tray' AND COVERNAME = ? FOR UPDATE NOWAIT ", new Object[] { coverName });
				setLock = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}
			
			if (setLock)condition += " FOR UPDATE ";
		}
		
		List<Durable> trayList = new ArrayList<Durable>();

		try
		{
			trayList = DurableServiceProxy.getDurableService().select(condition, new Object[] { coverName });
		}
		catch (Exception ex)
		{
			return null;
		}

		return trayList;
	}
	
	public List<Durable> getTrayListByTrayNameList(List<String> trayNameList,boolean forUpdate) throws CustomException
	{
		List<Map<String, Object>> resultList = null;
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("DURABLELIST", trayNameList);

		String sql = " SELECT * FROM DURABLE WHERE 1=1 AND DURABLENAME IN (:DURABLELIST) ";

		if (forUpdate)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM DURABLE WHERE 1=1 AND DURABLENAME IN (:DURABLELIST) FOR UPDATE NOWAIT ", bindMap);
				forUpdate = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}

			if (forUpdate) sql += " FOR UPDATE";
		}

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0) throw new CustomException("COMM-1000", "Durable","DurableList: " + org.springframework.util.StringUtils.collectionToDelimitedString(trayNameList.subList(0, trayNameList.size() > 10 ? 10 : trayNameList.size()), ",") + "...");

		List<Durable> durableList = DurableServiceProxy.getDurableService().transform(resultList);

		if (trayNameList.size() != durableList.size())
		{
			List<String> notFoundTray = ListUtils.subtract(trayNameList, CommonUtil.makeToStringList(durableList));
			if (notFoundTray.size() > 0)
				throw new CustomException("COMM-1000", "Durable","DurableList: " + org.springframework.util.StringUtils.collectionToDelimitedString(notFoundTray.subList(0, notFoundTray.size() > 10 ? 10 : notFoundTray.size()), ",") + "...");
		}

		return durableList;
	}
	
	public List<Durable> getTrayListByCoverName(String coverName) throws CustomException
	{
		String condition = " WHERE DURABLETYPE = 'Tray' AND COVERNAME = ? ";

		Object[] bindSet = new Object[] { coverName };
		List<Durable> trayList = new ArrayList<Durable>();
		try
		{
			trayList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch (Exception de)
		{
			trayList = null;
		}
		
		return trayList;
	}
	
	public List<Durable> checkExistMaskList(String maskCarrierName) throws CustomException
	{
		List<Durable> maskList;
		
		String condition = "WHERE maskCarrierName = ?";
		Object[] bindSet = new Object[] { maskCarrierName};
	
	 try
	 {
		  maskList =  DurableServiceProxy.getDurableService().select(condition, bindSet);
		  
	 }
	 catch (Exception ex)
	 {
		 maskList =null;
	 }
	 log.info("maskList.Size is  "+maskList.size());
	 return maskList;
	 
	 }

	public List<Durable> getMaskListByMachine(String machineName) throws CustomException
	{
		List<Durable> maskList = new ArrayList<Durable>();

		String condition = " WHERE DURABLETYPE IN ('EVAMask', 'TFEMask', 'FritMask') AND machineName = ? ";
		Object[] bindSet = new Object[] { machineName };
		
		try {
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		} catch (Exception ex) {
			maskList = null;
		}
		
		log.info("maskList.Size is  " + maskList.size());
		return maskList;
	}
	
	public static List<Durable> getMaskInfoBydurableName(String carrierName) throws CustomException{
		String condition = "WHERE durabletype in ('TFEMask','EVAMask') and MASKCARRIERNAME = ? and DURABLESTATE <> 'Scrapped' order by MASKPOSITION";
						
		Object[] bindSet = new Object[] {carrierName,};
		List<Durable> maskList = new ArrayList<Durable>();
		try
		{
			maskList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			maskList = null;
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql());
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		return maskList;
	}
	
	public boolean checkDurationLimit(Durable durableData, String item)
	{
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setFactoryName(durableData.getFactoryName());
		durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
		durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		
		Object value = durableData.getUdfs().get(item);
		Object specValue = durableSpecData.getUdfs().get("DURATIONUSEDLIMIT");
		if (value instanceof Timestamp)
		{
			double interval = (double) TimeUtils.getCurrentTimestamp().compareTo((Timestamp) value);
			if (specValue instanceof Double)
			{
				double limit = (double) specValue;
				if(interval > limit)
					return false;
			}
			else
				return false;
		}
		else
			return false;
		
		return true;
	}
	
	public void InsertCT_MaskMaterial(List<Object[]> updateArgList)
	{
		String queryString = "INSERT INTO CT_MASKMATERIAL (" +
                "MASKLOTNAME,MATERIALTYPE,MATERIALNAME,LASTEVENTCOMMENT,LASTEVENTNAME,LASTEVENTTIME,LASTEVENTTIMEKEY,LASTEVENTUSER" +
			" ) " +
			" VALUES(?, ?, ?, ?, ?, ?, ?, ? )";	
		
		try {
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryString, updateArgList);
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<ListOrderedMap> getAssignedPhotoMaskList(String machineName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DURABLENAME ");
		sql.append("  FROM DURABLE ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND DURABLETYPE = 'PhotoMask' ");
		sql.append("   AND DURABLESTATE IN ('InUse', 'Mounted', 'Prepare') ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MACHINENAME", machineName);

		try
		{
			List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			return result;
		}
		catch (Exception e)
		{
			throw new CustomException("DURABLE-0005", machineName);
		}
	}
	
	public void checkMaskIDLETime(Durable maskInfo, EventInfo eventInfo) throws CustomException 
	{
		String cleanUsedLimit = maskInfo.getUdfs().get("CLEANUSEDLIMIT").toString();
		String lastCleanTime = maskInfo.getUdfs().get("LASTCLEANTIME").toString();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());
		Date lastCleanTimeDate = null;
		Date currentDate = null;
		try {
			lastCleanTimeDate = transFormat.parse(lastCleanTime);
			currentDate = transFormat.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//long gapTest = currentDate.getTime() - lastCleanTimeDate.getTime();
		double gap = (double)(currentDate.getTime() - lastCleanTimeDate.getTime()) / (double)(60 * 60 * 1000);
		
		if (gap >= Double.parseDouble(cleanUsedLimit)) 
		{
			/*
			SetEventInfo setEventInfo = new SetEventInfo();
			maskInfo.setDurableCleanState("Dirty");
			DurableServiceProxy.getDurableService().update(maskInfo);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskInfo, setEventInfo, eventInfo);
			*/
			throw new CustomException("MASK-0070", maskInfo.getKey().getDurableName());
		}
	}
	
	public boolean checkMaskIDLETimeOver(Durable maskInfo) throws CustomException 
	{
		String cleanUsedLimit = maskInfo.getUdfs().get("CLEANUSEDLIMIT").toString();
		String lastCleanTime = maskInfo.getUdfs().get("LASTCLEANTIME").toString();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());
		Date lastCleanTimeDate = null;
		Date currentDate = null;
		try {
			lastCleanTimeDate = transFormat.parse(lastCleanTime);
			currentDate = transFormat.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//long gapTest = currentDate.getTime() - lastCleanTimeDate.getTime();
		double gap = (double)(currentDate.getTime() - lastCleanTimeDate.getTime()) / (double)(60 * 60 * 1000);
		
		if (gap >= Double.parseDouble(cleanUsedLimit)) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public List<Durable> getFPCListByPalletJig(String palletJigName) throws CustomException
	{
		String condition = " WHERE DURABLETYPE = 'FPC' AND PALLETNAME = ? ";

		Object[] bindSet = new Object[] { palletJigName };
		List<Durable> FPCList = new ArrayList<Durable>();
		try
		{
			FPCList = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch (Exception de)
		{
			FPCList = null;
		}
		
		return FPCList;
	}
	
	public List<Durable> getCoverListByProcessGroup(String innerName)throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT D.COVERNAME ");
		sql.append(" FROM LOT L,DURABLE D WHERE ");
		sql.append("     L.PROCESSGROUPNAME = :PROCESSGROUPNAME ");
		sql.append("     AND L.CARRIERNAME = D.DURABLENAME ");
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", innerName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		List<Durable> TrayDataList = new ArrayList<Durable>();
		if(sqlResult != null && !sqlResult.isEmpty())
		{
			String condition = "WHERE DURABLENAME IN(";
			Object[] bindSet = new Object[] {};
			
			for (Map<String, Object> coverNameMap : sqlResult) 
			{
				String durableName = coverNameMap.get("COVERNAME").toString();
				
				condition += "'" + durableName + "',";
			}
			condition = condition.substring(0, condition.length() - 1) + ")";
			try
			{
			TrayDataList = DurableServiceProxy.getDurableService().select(condition, bindSet);
			}
			catch (Exception de)
			{
				TrayDataList = null;
			}

		}
		return TrayDataList;
	}
}
