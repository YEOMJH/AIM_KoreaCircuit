package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestAssign;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.extended.object.management.data.ScrapCrate;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ProductRequestAssignService extends CTORMService<ProductRequestAssign> {

	public static Log logger = LogFactory.getLog(ProductRequestAssignService.class);

	private final String historyEntity = "CT_PRODUCTREQUESTASSIGNHISTORY";

	public ProductRequestAssign selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{

		return super.selectByKey(ProductRequestAssign.class, isLock, keySet);
	}

	public List<ProductRequestAssign> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{

		List<ProductRequestAssign> result = super.select(condition, bindSet, ProductRequestAssign.class);

		return result;
	}

	public void modify(EventInfo eventInfo, ProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.update(dataInfo);

	}

	public void create(EventInfo eventInfo, ProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.insert(dataInfo);

	}

	public void remove(EventInfo eventInfo, ProductRequestAssign dataInfo) throws greenFrameDBErrorSignal
	{

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ProductRequestAssign getProductRequestAssignData(String productRequestName, String toProductRequestName)
	{
		ProductRequestAssign dataInfo = new ProductRequestAssign();

		try
		{
			dataInfo = ExtendedObjectProxy.getProductRequestAssignService().selectByKey(false, new Object[] { productRequestName, toProductRequestName });
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
