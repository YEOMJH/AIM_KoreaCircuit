package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class FromScrapPackToFQC extends SyncHandler {

	public static Log logger = LogFactory.getLog(FromScrapPackToFQC.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String type = SMessageUtil.getBodyItemValue(doc, "TYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FromScrapPackToFQC", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo futureActionEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");

		boolean reserveFlag = false;

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		List<Lot> reserveHoldLotList = new ArrayList<Lot>();
		List<String> reserveDurableList = new ArrayList<String>();

		Node nextNode = getFQCNode(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion);

		if (StringUtils.equals(type, "Panel"))
		{
			for (Element dataInfo : dataList)
			{
				String lotName = dataInfo.getChildText("LOTNAME");

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

				CommonValidation.checkLotHoldState(lotData);

				List<Object> lotBindList = new ArrayList<Object>();

				boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lotData, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

				if (reserveCheck)
				{
					reserveFlag = true;
					reserveHoldLotList.add(lotData);
				}

				lotBindList.add(nextNode.getNodeAttribute1());
				lotBindList.add(nextNode.getNodeAttribute2());
				lotBindList.add(nextNode.getKey().getNodeId());
				lotBindList.add(lotData.getProcessOperationName());
				lotBindList.add(lotData.getProcessOperationVersion());
				lotBindList.add(eventInfo.getEventName());
				lotBindList.add(eventInfo.getEventTimeKey());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventComment());
				lotBindList.add("S");
				lotBindList.add(lotData.getKey().getLotName());

				updateLotArgList.add(lotBindList.toArray());

				// History
				Map<String, String> udfs = lotData.getUdfs();
				udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
				udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());
				udfs.put("LOTDETAILGRADE", "S");
				lotData.setUdfs(udfs);

				lotData.setProcessOperationName(nextNode.getNodeAttribute1());
				lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
				lotData.setNodeStack(nextNode.getKey().getNodeId());
				lotData.setLastEventName(eventInfo.getEventName());
				lotData.setLastEventTime(eventInfo.getEventTime());
				lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotData.setLastEventComment(eventInfo.getEventComment());
				lotData.setLastEventUser(eventInfo.getEventUser());

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
				
				updateLotHistoryList.add(lotHistory);
			}
		}
		else if (StringUtils.equals(type, "Tray"))
		{
			for (Element dataInfo : dataList)
			{
				List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(dataInfo.getChildText("DURABLENAME"));

				for (Map<String, Object> lotMap : lotList)
				{
					String lotName = lotMap.get("LOTNAME").toString();

					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

					CommonValidation.checkLotHoldState(lotData);

					List<Object> lotBindList = new ArrayList<Object>();

					boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lotData, nextNode.getNodeAttribute1(),
							nextNode.getNodeAttribute2());

					if (reserveCheck)
					{
						reserveFlag = true;

						boolean listCheckFlag = false;
						for (String durableName : reserveDurableList)
						{
							if (durableName.equals(dataInfo.getChildText("DURABLENAME")))
							{
								listCheckFlag = true;
								break;
							}
						}

						if (!listCheckFlag)
							reserveDurableList.add(dataInfo.getChildText("DURABLENAME"));
					}

					lotBindList.add(nextNode.getNodeAttribute1());
					lotBindList.add(nextNode.getNodeAttribute2());
					lotBindList.add(nextNode.getKey().getNodeId());
					lotBindList.add(lotData.getProcessOperationName());
					lotBindList.add(lotData.getProcessOperationVersion());
					lotBindList.add(eventInfo.getEventName());
					lotBindList.add(eventInfo.getEventTimeKey());
					lotBindList.add(eventInfo.getEventTime());
					lotBindList.add(eventInfo.getEventUser());
					lotBindList.add(eventInfo.getEventComment());
					lotBindList.add("S");
					lotBindList.add(lotData.getKey().getLotName());

					updateLotArgList.add(lotBindList.toArray());

					// History
					Map<String, String> udfs = lotData.getUdfs();
					udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
					udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());
					udfs.put("LOTDETAILGRADE", "S");
					lotData.setUdfs(udfs);

					lotData.setProcessOperationName(nextNode.getNodeAttribute1());
					lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
					lotData.setNodeStack(nextNode.getKey().getNodeId());
					lotData.setLastEventName(eventInfo.getEventName());
					lotData.setLastEventTime(eventInfo.getEventTime());
					lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					lotData.setLastEventComment(eventInfo.getEventComment());
					lotData.setLastEventUser(eventInfo.getEventUser());

					LotHistory lotHistory = new LotHistory();
					lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
					
					updateLotHistoryList.add(lotHistory);
				}
			}
		}
		else
		{
			for (Element dataInfo : dataList)
			{
				List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(dataInfo.getChildText("DURABLENAME"));

				for (Map<String, Object> lotMap : lotList)
				{
					String lotName = lotMap.get("LOTNAME").toString();

					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

					CommonValidation.checkLotHoldState(lotData);

					List<Object> lotBindList = new ArrayList<Object>();

					boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lotData, nextNode.getNodeAttribute1(),
							nextNode.getNodeAttribute2());

					if (reserveCheck)
					{
						reserveFlag = true;

						boolean listCheckFlag = false;
						for (String durableName : reserveDurableList)
						{
							if (durableName.equals(dataInfo.getChildText("DURABLENAME")))
							{
								listCheckFlag = true;
								break;
							}
						}

						if (!listCheckFlag)
							reserveDurableList.add(dataInfo.getChildText("DURABLENAME"));
					}

					lotBindList.add(nextNode.getNodeAttribute1());
					lotBindList.add(nextNode.getNodeAttribute2());
					lotBindList.add(nextNode.getKey().getNodeId());
					lotBindList.add(lotData.getProcessOperationName());
					lotBindList.add(lotData.getProcessOperationVersion());
					lotBindList.add(eventInfo.getEventName());
					lotBindList.add(eventInfo.getEventTimeKey());
					lotBindList.add(eventInfo.getEventTime());
					lotBindList.add(eventInfo.getEventUser());
					lotBindList.add(eventInfo.getEventComment());
					lotBindList.add("S");
					lotBindList.add(lotData.getKey().getLotName());

					updateLotArgList.add(lotBindList.toArray());

					// History
					Map<String, String> udfs = lotData.getUdfs();
					udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
					udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());
					udfs.put("LOTDETAILGRADE", "S");
					lotData.setUdfs(udfs);

					lotData.setProcessOperationName(nextNode.getNodeAttribute1());
					lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
					lotData.setNodeStack(nextNode.getKey().getNodeId());
					lotData.setLastEventName(eventInfo.getEventName());
					lotData.setLastEventTime(eventInfo.getEventTime());
					lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					lotData.setLastEventComment(eventInfo.getEventComment());
					lotData.setLastEventUser(eventInfo.getEventUser());

					LotHistory lotHistory = new LotHistory();
					lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
					
					updateLotHistoryList.add(lotHistory);
				}
			}
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		if (reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			if (StringUtils.equals(type, "Panel"))
			{
				MESLotServiceProxy.getLotServiceUtil().PanelHoldByPanelList(eventInfoHold, reserveHoldLotList);
			}
			else if (StringUtils.equals(type, "Tray"))
			{
				for (String durableName : reserveDurableList)
					MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, durableName);
			}
			else
			{
				for (String durableName : reserveDurableList)
					MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, durableName);
			}
		}

		return doc;
	}

	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET PROCESSOPERATIONNAME = ?, ");
		sql.append("       PROCESSOPERATIONVERSION = ?, ");
		sql.append("       NODESTACK = ?, ");
		sql.append("       BEFOREOPERATIONNAME = ?, ");
		sql.append("       BEFOREOPERATIONVER = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LOTDETAILGRADE = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private List<Map<String, Object>> getFQCOperation(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT TP.PROCESSOPERATIONNAME, TP.PROCESSOPERATIONVERSION, PS.DETAILPROCESSOPERATIONTYPE ");
		sql.append("  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS ");
		sql.append(" WHERE TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME ");
		sql.append("   AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION ");
		sql.append("   AND PS.DETAILPROCESSOPERATIONTYPE = 'FQC' ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() < 1)
			throw new CustomException("LOT-0325");

		return result;
	}

	private Node getFQCNode(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion) throws CustomException
	{
		List<Map<String, Object>> fqcOperation = getFQCOperation(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion);

		Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, processFlowName, processFlowVersion, GenericServiceProxy.getConstantMap().Node_ProcessOperation,
				fqcOperation.get(0).get("PROCESSOPERATIONNAME").toString(), fqcOperation.get(0).get("PROCESSOPERATIONVERSION").toString());

		return nextNode;
	}
}