package kr.co.aim.messolution.processgroup.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;

import org.apache.commons.lang.StringUtils;

public class ProcessGroupServiceUtil {

	public List<MaterialU> setBatchUSequence(List<String> batchlotlist) throws FrameworkErrorSignal, NotFoundSignal
	{

		List<MaterialU> materialUList = new ArrayList<MaterialU>();

		MaterialU materialU = null;
		for (String lotName : batchlotlist)
		{

			materialU = new MaterialU();
			materialU.setMaterialName(lotName);
			materialUList.add(materialU);
		}

		return materialUList;
	}

	public ProcessGroup getProcessGroupData(String processGroupName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			ProcessGroupKey processGroupKey = new ProcessGroupKey(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			return processGroupData;
		}
		catch (Exception e)
		{
			throw new CustomException("PROCESSGROUP-001", processGroupName);
		}
	}
	
	public void insertWMSShip(List<Object[]> insertArgList) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT ");
			sql.append("  INTO TRANS_WH_IN@OADBLINK.V3FAB.COM (SHIP_NO, ");
			sql.append("                                 SHIP_STAT, ");
			sql.append("                                 MDL_ID, ");
			sql.append("                                 PRD_QTY, ");
			sql.append("                                 WO_ID, ");
			sql.append("                                 FAB_ID, ");
			sql.append("                                 SOUR_SHOP, ");
			sql.append("                                 DEST_SHOP, ");
			sql.append("                                 BATCH_NO, ");
			sql.append("                                 COST_CENTER, ");
			sql.append("                                 INTERNAL_ORDER, ");
			sql.append("                                 DATA_STATUS, ");
			sql.append("                                 EVT_USR, ");
			sql.append("                                 EVT_TIMESTAMP, ");
			sql.append("                                 ITEM_SEQ, ");
			sql.append("                                 MOVE_TYPE ");
			sql.append("                                ) ");
			sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? "
					+ ") ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertArgList);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}

	public void insertWMSBox(List<Object[]> insertArgList) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT ");
			sql.append("  INTO TRANS_WH_BOX@OADBLINK.V3FAB.COM (BOX_ID, ");
			sql.append("                                  BOX_STAT, ");
			sql.append("                                  PRD_GRADE, ");
			sql.append("                                  PRD_QTY, ");
			sql.append("                                  MDL_ID, ");
			sql.append("                                  WO_ID, ");
			sql.append("                                  SOUR_SHOP, ");
			sql.append("                                  DEST_SHOP, ");
			sql.append("                                  PALLET_ID, ");
			sql.append("                                  BATCH_NO, ");
			sql.append("                                  BOX_WEIGHT, ");
			sql.append("                                  BOX_NOTE, ");
			sql.append("                                  SHIP_NO, ");
			sql.append("                                  DATA_STATUS, ");
			sql.append("                                  EVT_USR, ");
			sql.append("                                  EVT_TIMESTAMP, ");
			sql.append("                                  WO_TYPE, ");
			sql.append("                                  SUB_WO_TYPE, ");
			sql.append("                                  EVENTCOMMENT, ");
			sql.append("                                  HOLD_FLAG ");
			sql.append("                                 ) ");
			sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertArgList);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}

	public void insertWMSPanel(List<Object[]> insertArgList) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT ");
			sql.append("  INTO TRANS_WH_SHT@OADBLINK.V3FAB.COM  (PRD_SEQ_ID, ");
			sql.append("                                  BOX_ID, ");
			sql.append("                                  SLOT_NO, ");
			sql.append("                                  MDL_ID, ");
			sql.append("                                  PRD_QTY, ");
			sql.append("                                  PRD_GRADE, ");
			sql.append("                                  WO_ID, ");
			sql.append("                                  SOUR_SHOP, ");
			sql.append("                                  DEST_SHOP, ");
			sql.append("                                  PALLET_ID, ");
			sql.append("                                  BATCH_NO, ");
			sql.append("                                  DATA_STATUS, ");
			sql.append("                                  EVT_USR, ");
			sql.append("                                  EVT_TIMESTAMP, ");
			sql.append("                                  INNER_ID ");
			sql.append("                                 ) ");
			sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?) ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertArgList);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}
	
	public String getWMSFactoryName() throws CustomException
	{
		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT EV.ENUMNAME, EV.ENUMVALUE, EV.DESCRIPTION ");
		sql.append("  FROM ENUMDEF E, ENUMDEFVALUE EV ");
		sql.append(" WHERE E.ENUMNAME = EV.ENUMNAME ");
		sql.append("   AND E.ENUMNAME = :ENUMNAME ");
		sql.append("   AND EV.ENUMVALUE = :ENUMVALUE ");

		bindMap.put("ENUMNAME", "ERPPalletShipFactory");
		bindMap.put("ENUMVALUE", "FactoryName");

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

		String wmsFactoryName = "";

		if (sqlResult.size() > 0)
		{
			wmsFactoryName = ConvertUtil.getMapValueByName(sqlResult.get(0), "DESCRIPTION");
		}

		return wmsFactoryName;
	}
	
	public List<Map<String, Object>> getTRANS_WH_INForUnShip(String shipNo, String shipItemName) throws CustomException
	{
		List<Map<String, Object>> sqlResult = null;
		
		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT SHIP_NO, SHIP_STAT, PRD_QTY ");
		sql.append("  FROM TRANS_WH_IN@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE SHIP_NO = :SHIPNO FOR UPDATE");

		bindMap.put("SHIPNO", shipNo);

		sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
		
		if(sqlResult == null || sqlResult.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0011", shipItemName);
		}
		else
		{
			if(StringUtils.equals(sqlResult.get(0).get("SHIP_STAT").toString(), "SHIP"))
			{
				throw new CustomException("PROCESSGROUP-0010", shipItemName);
			}	
		}
		
		return sqlResult;
	}
	
	public List<Map<String, Object>> getTRANS_WH_IN(String shipNo) throws CustomException
	{
		List<Map<String, Object>> sqlResult = null;
		
		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * ");
		sql.append("  FROM TRANS_WH_IN@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE SHIP_NO = :SHIPNO FOR UPDATE");

		bindMap.put("SHIPNO", shipNo);

		try
		{
			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0010", e.getCause());
		}
		
		if(sqlResult == null || sqlResult.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0011", shipNo);
		}
		
		return sqlResult;
	}
	
	public boolean checkWMSBOX(String boxName)
	{
		boolean checkFlag = false;

		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT BOX_ID ");
		sql.append("  FROM TRANS_WH_BOX@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE BOX_ID = :BOXNAME ");

		bindMap.put("BOXNAME", boxName);

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			checkFlag = true;
		}

		return checkFlag;
	}
	
	public List<Map<String, Object>> getWMSBOX(String boxName) throws CustomException
	{
		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT BOX_ID, PRD_QTY, PALLET_ID ");
		sql.append("  FROM TRANS_WH_BOX@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE BOX_ID = :BOXNAME ");

		bindMap.put("BOXNAME", boxName);

		List<Map<String, Object>> sqlResult = null;
		
		try 
		{
			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
		} 
		catch (Exception e) 
		{
			throw new CustomException("PROCESSGROUP-0013", boxName);
		}

		return sqlResult;
	}
	
	public boolean checkWMSSHT(String lotName)
	{
		boolean checkFlag = false;

		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PRD_SEQ_ID ");
		sql.append("  FROM TRANS_WH_SHT@OADBLINK.V3FAB.COM ");
		sql.append(" WHERE PRD_SEQ_ID = :LOTNAME ");

		bindMap.put("LOTNAME", lotName);

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			checkFlag = true;
		}

		return checkFlag;
	}
	
	public void deteleTRANS_WH_BOX(List<Object[]> deleteArgListBOX) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM TRANS_WH_BOX@OADBLINK.V3FAB.COM ");
			sql.append(" WHERE BOX_ID = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), deleteArgListBOX);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}

	public void deleteTRANS_WH_SHT(List<Object[]> deleteArgListSHT) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM TRANS_WH_SHT@OADBLINK.V3FAB.COM ");
			sql.append(" WHERE PRD_SEQ_ID = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), deleteArgListSHT);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}
	
	public void deleteTRANS_WH_IN(List<Object[]> deleteArgListIn) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM TRANS_WH_IN@OADBLINK.V3FAB.COM ");
			sql.append(" WHERE SHIP_NO = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), deleteArgListIn);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0010", e.getCause());
		}
	}
	
	public void updateTRANS_WH_IN(List<Object[]> insertArgList) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE TRANS_WH_IN@OADBLINK.V3FAB.COM ");
			sql.append("   SET SHIP_STAT = ?, PRD_QTY = ?, EVT_USR = ?, EVT_TIMESTAMP = ? ");
			sql.append(" WHERE SHIP_NO = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertArgList);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}
	
	public void updateTRANS_WH_BOX(List<Object[]> updateArgListBOX) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE TRANS_WH_BOX@OADBLINK.V3FAB.COM ");
			sql.append("   SET PRD_QTY = ? ");
			sql.append(" WHERE BOX_ID = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgListBOX);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}
}
