package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReworkProductService extends CTORMService<ReworkProduct> {

	public static Log logger = LogFactory.getLog(ReworkProduct.class);

	public List<ReworkProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReworkProduct> result = super.select(condition, bindSet, ReworkProduct.class);

		return result;
	}

	public ReworkProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReworkProduct.class, isLock, keySet);
	}

	public ReworkProduct create(EventInfo eventInfo, ReworkProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReworkProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public ReworkProduct modify(EventInfo eventInfo, ReworkProduct dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public ReworkProduct getReworkProductData(String productName, String reworkType)
	{
		ReworkProduct dataInfo = new ReworkProduct();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { productName, reworkType });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public ReworkProduct setReworkCountData(String productName, String factoryName, String processFlowName, String processFlowVer, String processOperName, String processOperVer, String reworkType,
			long reworkCount)
	{
		ReworkProduct reworkData = new ReworkProduct();

		reworkData.setProductName(productName);
		reworkData.setFactoryName(factoryName);
		reworkData.setProcessFlowName(processFlowName);
		reworkData.setProcessFlowVersion(processFlowVer);
		reworkData.setProcessOperationName(processOperName);
		reworkData.setProcessOperationVersion(processOperVer);
		reworkData.setReworkType(reworkType);
		reworkData.setReworkCount(reworkCount);

		return reworkData;
	}

	public void setReworkCountData(Lot lotData, ProcessOperationSpec processOperationData, String lotGrade) throws CustomException
	{

		List<ReworkProduct> createList = new ArrayList<ReworkProduct>();
		List<ReworkProduct> updateList = new ArrayList<ReworkProduct>();

		if (lotGrade.equals("R"))
		{
			ReworkProduct reworkProductData = this.getReworkProductData(lotData.getKey().getLotName(), processOperationData.getDetailProcessOperationType());

			if (reworkProductData != null)
			{
				if (reworkProductData.getReworkCount() >= 1)
				{
					throw new CustomException("LOT-0100", lotData.getKey().getLotName(), reworkProductData.getReworkCount());
				}
				else
				{
					reworkProductData = this.setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), processOperationData.getDetailProcessOperationType(), reworkProductData.getReworkCount() + 1);

					updateList.add(reworkProductData);
				}
			}
			else
			{
				reworkProductData = this.setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), processOperationData.getDetailProcessOperationType(), 1);

				createList.add(reworkProductData);
			}
		}

		// Rework, Repair Count
		if (createList.size() > 0)
			this.insert(createList);

		if (updateList.size() > 0)
			this.update(updateList);
	}
}
