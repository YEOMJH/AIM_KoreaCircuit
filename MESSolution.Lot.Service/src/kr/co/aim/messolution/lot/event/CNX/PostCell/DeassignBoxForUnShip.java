package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class DeassignBoxForUnShip extends SyncHandler {
	private static Log log = LogFactory.getLog(DeassignBoxForUnShip.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String shipNo = SMessageUtil.getBodyItemValue(doc, "SHIPNO", true);
		List<Element> palletList = SMessageUtil.getBodySequenceItemList(doc, "PACKINGLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignBoxForUnShip", getEventUser(), getEventComment(), "", "");

		for (Element pallet : palletList)
		{
			String processGroupName = SMessageUtil.getChildText(pallet, "PROCESSGROUPNAME", true);
			ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);

			unShipProcessGroupData(eventInfo, processGroupData);
		}
		this.CheckTRANS_WH_INInfo(shipNo);
		this.getWOBOXList(shipNo);
		deleteWHIN(eventInfo,shipNo);
		deleteWHBOX(shipNo, this.getWOBOXList(shipNo));

		return doc;
	}

	private void unShipProcessGroupData(EventInfo eventInfo, ProcessGroup processGroupData)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("SHIPNO", "");

		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroupData, setEventInfo, eventInfo);

		List<ProcessGroup> subProcessGroupList = new ArrayList<ProcessGroup>();

		String condition = " SUPERPROCESSGROUPNAME = ? ";
		Object[] bindSet = new Object[] { processGroupData.getKey().getProcessGroupName() };

		try
		{
			subProcessGroupList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);

			for (ProcessGroup subProcessGroup : subProcessGroupList)
			{
				unShipProcessGroupData(eventInfo, subProcessGroup);
			}
		}
		catch (Exception e)
		{
			subProcessGroupList = null;
		}
	}

	private void CheckTRANS_WH_INInfo(String shipNo) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SHIP_NO, SHIP_STAT, MDL_ID, WO_ID, DEST_SHOP, BATCH_NO ");
		sql.append("  FROM TRANS_WH_IN@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE SHIP_NO = :SHIPNO ");
		sql.append("FOR UPDATE ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SHIPNO", shipNo);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0014", shipNo);
		}
		String shipState = ConvertUtil.getMapValueByName(sqlResult.get(0), "SHIP_STAT");
		if (StringUtils.equals(shipState, "SHIP"))
		{
			throw new CustomException("ShipNo Has Receive");
		}
	}

	private void deleteWHIN(EventInfo eventInfo, String shipNo ) throws CustomException
	{

		List<Object[]> updateArgListIN = new ArrayList<Object[]>();

		List<Object> updateBindListIN = new ArrayList<Object>();
		updateBindListIN.add(shipNo);
		updateArgListIN.add(updateBindListIN.toArray());
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM TRANS_WH_IN@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE SHIP_NO = ? ");
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgListIN);
	}

	private List<Map<String, Object>> getWOBOXList(String shipNo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT BOX_ID, BOX_STAT, BATCH_NO, SHIP_NO ");
		sql.append("  FROM TRANS_WH_BOX@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE SHIP_NO = :SHIPNO ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SHIPNO", shipNo);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		

		return sqlResult;
	}

	private void deleteWHBOX(String shipNo, List<Map<String, Object>> WOBOXList) throws CustomException
	{
		if (WOBOXList.size() < 1)
		{
	     throw new CustomException("PROCESSGROUP-0014", shipNo);
		}

		List<Object[]> deleteArgListBOX = new ArrayList<Object[]>();

		for (Map<String, Object> WOBOX : WOBOXList)
		{
			String BOX_ID = ConvertUtil.getMapValueByName(WOBOX, "BOX_ID");
			List<Object> deleteBindListBOX = new ArrayList<Object>();
			deleteBindListBOX.add(BOX_ID);

			deleteArgListBOX.add(deleteBindListBOX.toArray());
		}

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM TRANS_WH_BOX@OADBLINK.V3FAB.COM ");//TRANS_WH_BOX@OADBLINK.V3FAB.COM
		sql.append(" WHERE BOX_ID = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), deleteArgListBOX);
	}
}
