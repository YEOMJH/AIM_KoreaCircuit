package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.data.ControlProductRequestAssign;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ControlProductRequestAssignService  extends CTORMService<ControlProductRequestAssign> {
	
	public static Log logger = LogFactory.getLog(ControlProductRequestAssignService.class);

	private final String historyEntity = "CT_CONTROLPRODUCTREQUESTASSIGNHISTORY";
	
	public ControlProductRequestAssign selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{

		return super.selectByKey(ControlProductRequestAssign.class, isLock, keySet);
	}

	public List<ControlProductRequestAssign> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{

		List<ControlProductRequestAssign> result = super.select(condition, bindSet, ControlProductRequestAssign.class);

		return result;
	}

	public void modify(EventInfo eventInfo, ControlProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.update(dataInfo);

	}

	public void create(EventInfo eventInfo, ControlProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.insert(dataInfo);

	}

	public void remove(EventInfo eventInfo, ControlProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ControlProductRequestAssign getControlProductRequestAssignData(String productRequestName, String toProductRequestName)
	{
		ControlProductRequestAssign dataInfo = new ControlProductRequestAssign();

		try
		{
			dataInfo = ExtendedObjectProxy.getControlProductRequestAssignService().selectByKey(false, new Object[] { productRequestName, toProductRequestName });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public void CheckRelationFactory(ProductRequest shipProductRequestInfo, ProductRequest receiveProductRequestInfo) throws greenFrameDBErrorSignal, CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM TPPOLICY TP, POSFACTORYRELATION FR ");
		sql.append(" WHERE TP.CONDITIONID = FR.CONDITIONID ");
		sql.append("   AND TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND FR.TOFACTORYNAME = :TOFACTORYNAME ");
		sql.append("   AND FR.TOPRODUCTSPECNAME = :TOPRODUCTSPECNAME ");
		sql.append("   AND FR.TOPRODUCTSPECVERSION = :TOPRODUCTSPECVERSION ");
		sql.append("   AND FR.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND FR.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", shipProductRequestInfo.getFactoryName());
		args.put("PRODUCTSPECNAME", shipProductRequestInfo.getProductSpecName());
		args.put("PRODUCTSPECVERSION", shipProductRequestInfo.getProductSpecVersion());
		args.put("TOFACTORYNAME", receiveProductRequestInfo.getFactoryName());
		args.put("TOPRODUCTSPECNAME", receiveProductRequestInfo.getProductSpecName());
		args.put("TOPRODUCTSPECVERSION", receiveProductRequestInfo.getProductSpecVersion());
		args.put("TOPROCESSFLOWNAME", CommonUtil.getValue(receiveProductRequestInfo.getUdfs(), "PROCESSFLOWNAME"));
		args.put("TOPROCESSFLOWVERSION", CommonUtil.getValue(receiveProductRequestInfo.getUdfs(), "PROCESSFLOWVERSION"));

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() < 0)
		{
			throw new CustomException("LOT-0211", shipProductRequestInfo.getKey(), receiveProductRequestInfo.getKey());
		}

	}


}
