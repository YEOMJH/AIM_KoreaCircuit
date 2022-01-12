package kr.co.aim.messolution.dispatch.service;

import java.util.Timer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DSPServiceUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DSPServiceUtil.class);

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void timeDelay(long delay)
	{
		Timer timer = new Timer();
		timer.schedule(null, delay);
	}

	@SuppressWarnings("unchecked")
	public String getNextPositionForReserveProductSpec(String machineName) throws CustomException
	{
		String sql = " SELECT MAX(POSITION) MAXPOSITION FROM  (SELECT POSITION FROM CT_RESERVEPRODUCT WHERE MACHINENAME = :machineName  UNION  SELECT '0' POSITION FROM  DUAL) ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		int nextPositionI = Integer.valueOf((String) sqlResult.get(0).get("MAXPOSITION")) + 1;

		String nextPositionS = String.valueOf(nextPositionI);

		return nextPositionS;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getReserveProductSpecData(String machineName, String processOperationGroupName, String processOperationName, String productSpecName) throws CustomException
	{
		String sql = " SELECT MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION,  RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY "
				+ " FROM CT_RESERVEPRODUCT  WHERE MACHINENAME = :machineName  AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName "
				+ " AND PROCESSOPERATIONNAME = :processOperationName  AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlResult;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getReserveProductSpecList(String machineName) throws CustomException
	{
		String sql = "SELECT ROWNUM - 1 SEQ, " +
				"       MACHINENAME, " +
				"       PROCESSOPERATIONGROUPNAME, " +
				"       PROCESSOPERATIONNAME, " +
				"       PRODUCTSPECNAME, " +
				"       POSITION, " +
				"       RESERVESTATE, " +
				"       RESERVEDQUANTITY, " +
				"       COMPLETEQUANTITY " +
				"  FROM (  SELECT * " +
				"            FROM CT_RESERVEPRODUCT " +
				"           WHERE MACHINENAME = :MACHINENAME " +
				"        ORDER BY POSITION ASC) " ;

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlResult;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getReserveProductSpecList(String productSpecName, String processOperationName, String machineName) throws CustomException
	{
		String sql = " SELECT ROWNUM-1 SEQ, MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, "
				+ "        RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY   FROM CT_RESERVEPRODUCT  WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  AND MACHINENAME = :MACHINENAME  ORDER BY POSITION ASC ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlResult;
	}
}
