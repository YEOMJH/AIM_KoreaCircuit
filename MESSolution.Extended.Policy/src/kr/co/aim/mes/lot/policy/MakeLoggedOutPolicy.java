package kr.co.aim.mes.lot.policy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.policy.util.FindUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class MakeLoggedOutPolicy extends kr.co.aim.greentrack.lot.management.policy.MakeLoggedOutPolicy {
	
	@Override
	public void makeAllProductsIdle(DataInfo oldLot, DataInfo newLot,
			EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal,
			InvalidStateTransitionSignal {
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		MakeLoggedOutInfo makeLoggedOutInfo = (MakeLoggedOutInfo) transitionInfo;

		if (makeLoggedOutInfo.getProductPGSRCSequence().size() > 0)
		{
			executeValidation(eventInfo.getBehaviorName(), "InvalidCurrentProductQuantity", newLotData.getKey(),
				oldLotData.getProductQuantity(), makeLoggedOutInfo.getProductPGSRCSequence().size());
		}

		kr.co.aim.greentrack.product.management.info.MakeIdleInfo prodMIInfo =
				new kr.co.aim.greentrack.product.management.info.MakeIdleInfo();

		prodMIInfo.setProcessFlowName(newLotData.getProcessFlowName());
		prodMIInfo.setProcessFlowVersion(newLotData.getProcessFlowVersion());
		prodMIInfo.setProcessOperationName(newLotData.getProcessOperationName());
		prodMIInfo.setProcessOperationVersion(newLotData.getProcessOperationVersion());
		prodMIInfo.setNodeStack(newLotData.getNodeStack());

		// �߰���
		prodMIInfo.setBranchEndNodeId(newLotData.getBranchEndNodeId());

		prodMIInfo.setCarrierName(newLotData.getCarrierName());

		prodMIInfo.setAreaName(makeLoggedOutInfo.getAreaName());
		prodMIInfo.setMachineName(makeLoggedOutInfo.getMachineName());
		prodMIInfo.setMachineRecipeName(makeLoggedOutInfo.getMachineRecipeName());

		if (StringUtils.equals(newLotData.getLotState(), getConstantMap().Lot_Completed))
		{
			prodMIInfo.setCompleteFlag("Y");
		}

		List<Product> sequence = null;
		try
		{
			sequence = LotServiceProxy.getLotService().allUnScrappedProducts(oldLotData.getKey().getLotName());
		} catch (NotFoundSignal notFoundSignal)
		{}

		if (CollectionUtils.isEmpty(sequence))
			return;

		Map<String, Product> prodMIIndexes = new HashMap<String, Product>();
		for (ProductPGSRC productPGSRC : makeLoggedOutInfo.getProductPGSRCSequence())
		{
			Product product = (Product) FindUtils.find(sequence, productPGSRC.getProductName());
			if (product != null)
			{
				prodMIInfo.setReworkFlag(""); // reset
				prodMIInfo.setReworkNodeId(""); // reset

				if (StringUtils.equals(newLotData.getReworkState(), getConstantMap().Lot_InRework))
				{
					if (newLotData.getReworkCount() > oldLotData.getReworkCount()
						&& !productPGSRC.getReworkFlag().equals("N"))
					{
						prodMIInfo.setReworkFlag("Y");
						prodMIInfo.setReworkNodeId(newLotData.getReworkNodeId());
					}
					else if (product.getReworkState().equals(getConstantMap().Prod_NotInRework)
						&& productPGSRC.getReworkFlag().equals("Y"))
					{
						prodMIInfo.setReworkFlag("Y");
						prodMIInfo.setReworkNodeId(newLotData.getReworkNodeId());
					}

				}
				else
				{
					if (StringUtils.equals(oldLotData.getReworkState(), getConstantMap().Lot_InRework))
					{
						prodMIInfo.setReworkFlag("N");
					}
				}

				prodMIInfo.setPosition(productPGSRC.getPosition());
				prodMIInfo.setProductGrade(productPGSRC.getProductGrade());
				prodMIInfo.setSubProductGrades1(productPGSRC.getSubProductGrades1());
				prodMIInfo.setSubProductGrades2(productPGSRC.getSubProductGrades2());
				prodMIInfo.setSubProductQuantity1(productPGSRC.getSubProductQuantity1());
				prodMIInfo.setSubProductQuantity2(productPGSRC.getSubProductQuantity2());
				
				prodMIInfo.getUdfs().clear();
				prodMIInfo.setUdfs(productPGSRC.getUdfs());
				
				prodMIInfo.setConsumedMaterialSequence(productPGSRC.getConsumedMaterialSequence());

				ProductServiceProxy.getProductService().makeIdle(product.getKey(), eventInfo, prodMIInfo);
				prodMIIndexes.put(product.getKey().getProductName(), product);
			}
			else
			{
				throw new FrameworkErrorSignal(ExceptionKey.ProductNotInLot_Exception,
						ObjectUtil.getString(newLotData.getKey()), productPGSRC.getProductName(), newLotData.getKey()
								.getLotName());
			}
		}

		prodMIInfo.setReworkFlag(""); // reset
		prodMIInfo.setReworkNodeId("");

		if (prodMIInfo.getUdfs() != null) prodMIInfo.getUdfs().clear();
		if (prodMIInfo.getConsumedMaterialSequence()  != null) prodMIInfo.getConsumedMaterialSequence().clear();

		if (StringUtils.equals(newLotData.getReworkState(), getConstantMap().Lot_InRework))
		{
			if (newLotData.getReworkCount() > oldLotData.getReworkCount())
			{
				prodMIInfo.setReworkFlag("Y");
				prodMIInfo.setReworkNodeId(newLotData.getReworkNodeId());
			}
		}
		else
		{
			if (StringUtils.equals(oldLotData.getReworkState(), getConstantMap().Lot_InRework))
			{
				prodMIInfo.setReworkFlag("N");
			}
		}

		for (Product product : sequence)
		{
			if (prodMIIndexes.get(product.getKey().getProductName()) == null)
			{
				prodMIInfo.setPosition(product.getPosition());
				prodMIInfo.setProductGrade(product.getProductGrade());
				prodMIInfo.setSubProductGrades1(product.getSubProductGrades1());
				prodMIInfo.setSubProductGrades2(product.getSubProductGrades2());
				prodMIInfo.setSubProductQuantity1(product.getSubProductQuantity1());
				prodMIInfo.setSubProductQuantity2(product.getSubProductQuantity2());

				ProductServiceProxy.getProductService().makeIdle(product.getKey(), eventInfo, prodMIInfo);
			}
		}
	}
}
