package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class MakePanelBorrowed extends SyncHandler {

	private static Log log = LogFactory.getLog(MakePanelBorrowed.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String borrowUserName = SMessageUtil.getBodyItemValue(doc, "BORROWUSERNAME", true);
		String borrowDepartment = SMessageUtil.getBodyItemValue(doc, "BORROWDEPARTMENT", true);
		String borrowCentrality = SMessageUtil.getBodyItemValue(doc, "BORROWCENTRALITY", true);
		String borrowEmail = SMessageUtil.getBodyItemValue(doc, "BORROWEMAIL", true);
		String phoneNumber = SMessageUtil.getBodyItemValue(doc, "PHONENUMBER", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);

		List<ProcessFlow> flowList = ProcessFlowServiceProxy.getProcessFlowService().select(" DESCRIPTION = ?", new Object[] { description });
		String borrowFlowName = flowList.get(0).getKey().getProcessFlowName();
		String borrowFlowVersion = flowList.get(0).getKey().getProcessFlowVersion();

		List<Node> operationList = ProcessFlowServiceProxy.getNodeService().select(" PROCESSFLOWNAME = ? " + "AND PROCESSFLOWVERSION = ? AND NODETYPE = ?",
				new Object[] { borrowFlowName, borrowFlowVersion, "ProcessOperation" });
		String borrowOperationName = operationList.get(0).getNodeAttribute1();
		String borrowOperationVersion = operationList.get(0).getNodeAttribute2();

		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);

		if (lotList != null)
		{
			for (Iterator iteratorLotList = lotList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();

				String lotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
				String machineName = SMessageUtil.getChildText(lotE, "MACHINENAME", false);
				String lastLoggedInTime = SMessageUtil.getChildText(lotE, "LASTLOGGEDINTIME", false);
				String lastLoggedOutTime = SMessageUtil.getChildText(lotE, "LASTLOGGEDOUTTIME", false);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("BorrowPanel", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				Lot oldLotData = (Lot)ObjectUtil.copyTo(lotData);
				if (StringUtil.equals(description, "INTBORROW") && StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped))
				{
					String sql = "UPDATE LOT SET LOTSTATE = :LOTSTATE, LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME ";
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
					bindMap.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_Wait);
					bindMap.put("LOTNAME", lotData.getKey().getLotName());

					String sql1 = "UPDATE LOT SET LOTSTATE = :LOTSTATE WHERE LOTNAME = :LOTNAME ";
					Map<String, Object> bindMap1 = new HashMap<String, Object>();
					bindMap1.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Scrapped);
					bindMap1.put("LOTNAME", lotData.getKey().getLotName());

					if (StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
					{
						sql1 = "UPDATE LOT SET LOTSTATE = :LOTSTATE, LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME ";
						bindMap1.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_Run);
					}

					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
					lotData.setLotProcessState("WAIT");

					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
							lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), ""/* nodeStack */, lotData.getPriority(), borrowFlowName, borrowFlowVersion,
							borrowOperationName, borrowOperationVersion, lotData.getProductionType(), lotData.getProductRequestName(), "", "", lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getProcessFlowName(),
							lotData.getProcessOperationName());
					if (StringUtil.equals(oldLotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
						changeSpecInfo.setLotProcessState("RUN");

					Lot aLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

					GenericServiceProxy.getSqlMesTemplate().update(sql1, bindMap1);

					if (StringUtil.isNotEmpty(aLot.getCarrierName()))
						makeTrayDeassign(lotName, aLot.getCarrierName());

					// Insert into ct_borrowpanel
					BorrowPanel borrowedPanel = new BorrowPanel();
					borrowedPanel.setLotName(lotName);
					borrowedPanel.setBorrowUserName(borrowUserName);
					borrowedPanel.setBorrowDepartment(borrowDepartment);
					borrowedPanel.setBorrowCentrality(borrowCentrality);
					borrowedPanel.setBorrowDate(eventInfo.getEventTime());

					borrowedPanel.setBorrowState("Borrowed");

					if (ExtendedObjectProxy.getBorrowPanelService().create(eventInfo, borrowedPanel))
					{
						log.info("Excute Insert Notice :Inserted into CT_BorrowPanel! ");
					}
				}
				else
				{
					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
							lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), ""/* nodeStack */, lotData.getPriority(), borrowFlowName, borrowFlowVersion,
							borrowOperationName, borrowOperationVersion, lotData.getProductionType(), lotData.getProductRequestName(), "", "", lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getProcessFlowName(),
							lotData.getProcessOperationName());
					Lot aLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
					if (StringUtil.isNotEmpty(aLot.getCarrierName()))
						makeTrayDeassign(lotName, aLot.getCarrierName());

					// Insert into ct_borrowpanel
					BorrowPanel borrowedPanel = new BorrowPanel();
					borrowedPanel.setLotName(lotName);
					borrowedPanel.setBorrowUserName(borrowUserName);
					borrowedPanel.setBorrowDepartment(borrowDepartment);
					borrowedPanel.setBorrowCentrality(borrowCentrality);
					borrowedPanel.setBorrowDate(eventInfo.getEventTime());

					borrowedPanel.setBorrowState("Borrowed");

					if (ExtendedObjectProxy.getBorrowPanelService().create(eventInfo, borrowedPanel))
					{
						log.info("Excute Insert Notice :Inserted into CT_BorrowPanel! ");
					}
				}

			}
		}

		return doc;
	}

	public void makeTrayDeassign(String lotName, String durableName) throws CustomException
	{

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		// SQL
		String queryStringLot = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, POSITION = ?, CARRIERNAME = ? WHERE LOTNAME = ?";

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

		List<Object> lotBindList = new ArrayList<Object>();

		lotBindList.add(eventInfo.getEventName());
		lotBindList.add(eventInfo.getEventTimeKey());
		lotBindList.add(eventInfo.getEventTime());
		lotBindList.add(eventInfo.getEventUser());
		lotBindList.add(eventInfo.getEventComment());
		lotBindList.add(constantMap.Flag_N);
		lotBindList.add("");
		lotBindList.add("");
		lotBindList.add(lotName);

		updateLotArgList.add(lotBindList.toArray());

		// History
		lot.setCarrierName("");
		lot.setLastEventName(eventInfo.getEventName());
		lot.setLastEventTime(eventInfo.getEventTime());
		lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lot.setLastEventComment(eventInfo.getEventComment());
		lot.setLastEventUser(eventInfo.getEventUser());
		Map<String, String> lotUdfs = new HashMap<>();
		lotUdfs.put("POSITION", "");
		lot.setUdfs(lotUdfs);

		LotHistory lotHistory = new LotHistory();
		lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
		
		updateLotHistoryList.add(lotHistory);

		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		// Durable
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);
		if (durableInfo.getLotQuantity() == 0)
		{
			durableInfo.setDurableState(constantMap.Dur_Available);
		}
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

		// TrayGroup
		if (StringUtil.isNotEmpty(durableInfo.getUdfs().get("COVERNAME")))
		{
			Durable oldTrayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableInfo.getUdfs().get("COVERNAME"));
			Durable trayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableInfo.getUdfs().get("COVERNAME"));

			if (trayGroupInfo.getLotQuantity() > 0)
			{
				trayGroupInfo.setLotQuantity(trayGroupInfo.getLotQuantity() - 1);
			}

			trayGroupInfo.setLastEventName(eventInfo.getEventName());
			trayGroupInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			trayGroupInfo.setLastEventTime(eventInfo.getEventTime());
			trayGroupInfo.setLastEventUser(eventInfo.getEventUser());
			trayGroupInfo.setLastEventComment(eventInfo.getEventComment());

			DurableHistory trayGHistory = new DurableHistory();
			trayGHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayGroupInfo, trayGroupInfo, trayGHistory);

			DurableServiceProxy.getDurableService().update(trayGroupInfo);
			DurableServiceProxy.getDurableHistoryService().insert(trayGHistory);
		}

	}
}
