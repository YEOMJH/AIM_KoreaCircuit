package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayGroupProcessAbort extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayGroupProcessAbort.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupProcessAbortReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
			List<Element> trayElementList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", true);

			// remove cover tray element from trayElementList and return the element.
			Element coverTrayElement = this.removeCoverTrayElement(trayElementList, trayGroupName);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);

			// send ok reply to EQP unconditionally
			//this.sendUnconditionallyOKReply(doc, machineName);

			if (trayElementList == null || trayElementList.size() <= 0)
			{
				// TRAY-0016:No TrayList information received from BC.
				throw new CustomException("TRAY-0016");
			}

			if (trayGroupData.getDurableType().equals("CoverTray") || trayGroupData.getDurableState().equals("InUse") || trayGroupData.getLotQuantity() > 0||!StringUtil.isEmpty(trayGroupData.getUdfs().get("COVERNAME")))
				throw new CustomException("DURABLE-0008", trayGroupName);

			// Assign Tray from TrayGroup
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", getEventUser(), getEventComment());
			Map<String, Durable> durableDataMap = this.generateDurableDataMap(trayElementList, true);

			int totalLotQty = 0;
			int maxPosition = 1;
			List<Durable> updateDurableList = new ArrayList<>(trayElementList.size());
			List<DurableHistory> updateDurableHistList = new ArrayList<>(trayElementList.size());

			for (Element trayElement : trayElementList)
			{
				
			
				Durable trayData = durableDataMap.get(trayElement.getChildText("TRAYNAME"));
				totalLotQty += trayData.getLotQuantity();
				
				if (StringUtils.equals(trayData.getDurableType(), "CoverTray"))
				{
					//TRAY-0017: The tray group [{0}] contains multiple cover trays.[IssueTray:{1}]
					throw new CustomException("TRAY-0017", trayGroupName, trayData.getKey().getDurableName());
				}
				if (!StringUtil.isEmpty(trayData.getUdfs().get("COVERNAME")))
					throw new CustomException("CARRIER-9008", trayData.getKey().getDurableName(), trayData.getUdfs().get("COVERNAME"));
				Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

				trayData.getUdfs().put("COVERNAME", trayGroupName);
				trayData.getUdfs().put("POSITION", trayElement.getChildText("POSITION"));
				trayData.getUdfs().put("DURABLETYPE1", "Tray");
				trayData.setLastEventName(eventInfo.getEventName());
				trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				trayData.setLastEventTime(eventInfo.getEventTime());
				trayData.setLastEventUser(eventInfo.getEventUser());
				trayData.setLastEventComment(eventInfo.getEventComment());

				if (Integer.parseInt(trayElement.getChildText("POSITION")) > maxPosition)
					maxPosition = Integer.parseInt(trayElement.getChildText("POSITION"));

				DurableHistory durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayData, trayData, new DurableHistory());

				updateDurableList.add(trayData);
				updateDurableHistList.add(durHistory);
			}

			try
			{
				CommonUtil.executeBatch("update", updateDurableList, true);
				CommonUtil.executeBatch("insert", updateDurableHistList, true);

				log.info(String.format("▶Successfully update %s pieces of trays.", updateDurableList.size()));
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}

			// Set TrayGroup
			trayGroupData.setDurableType("CoverTray");
			trayGroupData.setLotQuantity(totalLotQty);
			trayGroupData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

			DurableServiceProxy.getDurableService().update(trayGroupData);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("POSITION", coverTrayElement == null ? String.valueOf((maxPosition + 1)) : coverTrayElement.getChildText("POSITION"));
			setEventInfo.getUdfs().put("COVERNAME", trayGroupData.getKey().getDurableName());
			setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");

			DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
			
			//PFL update PostCellLoadInfo
			if(StringUtils.equals(machineData.getMachineGroupName(), "PFL"))
			{
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT L.LOTNAME FROM LOT L,DURABLE D ");
				sql.append("  WHERE L.CARRIERNAME=D.DURABLENAME ");
				sql.append("  AND D.COVERNAME=:TRAYGROUPNAME ");
				sql.append("  AND L.JOBDOWNFLAG=:MACHINENAME ");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("TRAYGROUPNAME", trayGroupName);
				bindMap.put("MACHINENAME", machineName);

				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
				if(sqlResult!=null&&sqlResult.size()>0)
				{
					List<Object[]> updateLotArgList = new ArrayList<Object[]>();
					for(int i=0;i<sqlResult.size();i++)
					{
						List<Object> lotBindList = new ArrayList<Object>();
						lotBindList.add(eventInfo.getEventName());
						lotBindList.add(eventInfo.getEventTimeKey());
						lotBindList.add(eventInfo.getEventTime());
						lotBindList.add(eventInfo.getEventUser());
						lotBindList.add(eventInfo.getEventComment());
						lotBindList.add("");
						lotBindList.add(sqlResult.get(i).get("LOTNAME"));
						updateLotArgList.add(lotBindList.toArray());
					}
					
					StringBuffer sqlForUpdate = new StringBuffer();
					sqlForUpdate.append("UPDATE LOT SET ");
					sqlForUpdate.append("       LASTEVENTNAME = ?, ");
					sqlForUpdate.append("       LASTEVENTTIMEKEY = ?, ");
					sqlForUpdate.append("       LASTEVENTTIME = ?, ");
					sqlForUpdate.append("       LASTEVENTUSER = ?, ");
					sqlForUpdate.append("       LASTEVENTCOMMENT = ?, ");
					sqlForUpdate.append("       JOBDOWNFLAG = ? ");
					sqlForUpdate.append(" WHERE LOTNAME = ? ");

					try 
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlForUpdate.toString(), updateLotArgList);
					} catch (Exception ex) 
					{
						log.info("JobDownFlag update file");
					}
				}
			}

			log.info("Result : OK");
		}
		catch (CustomException ce)
		{
			log.info("Result : " + ce.errorDef.getErrorCode() + ", DESCRIPTION : " + ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			log.info("Result : UndefinedCode, DESCRIPTION : " + e.getMessage());
		}
		finally
		{
			Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc));
			newBody.getChild("RESULT").setText("OK");
			newBody.getChild("RESULTDESCRIPTION").setText("");

			doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
			doc.getRootElement().addContent(2, newBody);
		}

		return doc;
	}

	private Element generateReturnBodyTemplate(Element bodyElement) throws CustomException
	{
		Element body = new Element(SMessageUtil.Body_Tag);

		JdomUtils.addElement(body, "MACHINENAME", bodyElement.getChildText("MACHINENAME"));
		JdomUtils.addElement(body, "PORTNAME", bodyElement.getChildText("PORTNAME"));
		JdomUtils.addElement(body, "PORTTYPE", bodyElement.getChildText("PORTTYPE"));
		JdomUtils.addElement(body, "PORTUSETYPE", bodyElement.getChildText("PORTUSETYPE"));
		JdomUtils.addElement(body, "TRAYGROUPNAME", bodyElement.getChildText("TRAYGROUPNAME"));
		JdomUtils.addElement(body, "MACHINERECIPENAME", bodyElement.getChildText("MACHINERECIPENAME"));
		JdomUtils.addElement(body, "RESULT", "");
		JdomUtils.addElement(body, "RESULTDESCRIPTION", "");

		return body;
	}

	public Document createReplyDocument(Document doc)
	{
		Document replyDoc = (Document) doc.clone();
		Element bodyElement = replyDoc.getRootElement().getChild(SMessageUtil.Body_Tag);

		Element trayListElement = XmlUtil.getChild(bodyElement, "TRAYLIST", false);

		if (trayListElement != null)
		{
			trayListElement.detach();
		}

		SMessageUtil.setHeaderItemValue(replyDoc, SMessageUtil.MessageName_Tag, "TrayGroupProcessAbortReply");

		bodyElement.addContent(new Element("RESULT").setText("OK"));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(""));

		return replyDoc;
	}

	public void sendUnconditionallyOKReply(Document doc, String machineName)
	{
		Document replyDoc = this.createReplyDocument(doc);

		String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, replyDoc, "EISSender");
	}

	public Element removeCoverTrayElement(List<Element> trayElementList, String coverTrayName) throws CustomException
	{
		Element coverTrayElement = null;

		for (Element trayElement : new ArrayList<>(trayElementList))
		{
			if (trayElement.getChildText("TRAYNAME").equals(coverTrayName))
			{
				coverTrayElement = (Element) trayElement.clone();
				trayElementList.remove(trayElement);
			}
		}

		if (coverTrayElement == null)
			log.info("Important content: There is no CoverTray information in the TrayList reported by BC .");

		return coverTrayElement;
	}

	public Map<String, Durable> generateDurableDataMap(List<Element> trayElementList, boolean forUpdate) throws CustomException
	{
		List<Durable> durableList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByTrayNameList(CommonUtil.makeList(trayElementList, "TRAYNAME"), true);

		Map<String, Durable> durableDataMap = new HashMap<>(trayElementList.size());

		for (Durable durableData : durableList)
		{
			durableDataMap.put(durableData.getKey().getDurableName(), durableData);
		}

		return durableDataMap;
	}
}
