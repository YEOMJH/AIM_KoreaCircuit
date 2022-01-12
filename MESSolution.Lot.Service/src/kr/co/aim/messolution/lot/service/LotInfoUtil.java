package kr.co.aim.messolution.lot.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.GlassJudge;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPanelInfo;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPolicy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.messolution.product.service.ProductUU;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
import kr.co.aim.greentrack.lot.management.data.LotFutureActionKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.AssignNewProductsInfo;
import kr.co.aim.greentrack.lot.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.lot.management.info.AssignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CancelWaitingToLoginInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateAndAssignAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.CreateRawInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeEmptiedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeWaitingToLoginInfo;
import kr.co.aim.greentrack.lot.management.info.MergeInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.SeparateInfo;
import kr.co.aim.greentrack.lot.management.info.SetAreaInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.lot.management.info.UndoInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Arc;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductGSC;
import kr.co.aim.greentrack.product.management.info.ext.ProductNPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductNSubProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LotInfoUtil implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(LotInfoUtil.class);;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	public List<ProductP> getAllProductPSequence(Lot lotData)
	{
		// 1. Set Variable
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductP productP = new ProductP();

			productP.setProductName(product.getKey().getProductName());
			productP.setPosition(product.getPosition());

			// Add productPSequence By Product
			productPSequence.add(productP);
		}
		return productPSequence;
	}

	public List<ProductPGS> getAllProductPGSSequence(Lot lotData)
	{
		// 1. Set Variable
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductPGS productPGS = new ProductPGS();

			productPGS.setProductName(product.getKey().getProductName());
			productPGS.setPosition(product.getPosition());
			productPGS.setProductGrade(product.getProductGrade());
			productPGS.setSubProductGrades1(product.getSubProductGrades1());
			productPGS.setSubProductGrades2(product.getSubProductGrades2());
			productPGS.setSubProductQuantity1(product.getSubProductQuantity1());
			productPGS.setSubProductQuantity2(product.getSubProductQuantity2());

			// Add productPSequence By Product
			productPGSSequence.add(productPGS);
		}
		return productPGSSequence;
	}

	public List<ProductU> getAllProductUSequence(Lot lotData) throws CustomException
	{
		// 1. Set Variable
		List<ProductU> ProductUSequence = new ArrayList<ProductU>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		try
		{
			productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();


			ProductU productU = new ProductU();
			productU.setProductName(product.getKey().getProductName());

			// Add productPSequence By Product
			ProductUSequence.add(productU);
		}

		return ProductUSequence;
	}

	public AssignNewProductsInfo assignNewProductsInfo(Lot lotData, double productQuantity, List<ProductPGS> productPGSSequence)
	{
		// 1. Validation

		AssignNewProductsInfo assignNewProductsInfo = new AssignNewProductsInfo();

		assignNewProductsInfo.setProductQuantity(productQuantity);
		assignNewProductsInfo.setProductPGSSequence(productPGSSequence);

		return assignNewProductsInfo;
	}

	public AssignProcessGroupInfo assignProcessGroupInfo(Lot lotData, String processGroupName)
	{
		// 1. Validation

		AssignProcessGroupInfo assignProcessGroupInfo = new AssignProcessGroupInfo();

		assignProcessGroupInfo.setProcessGroupName(processGroupName);

		return assignProcessGroupInfo;
	}

	public AssignProductsInfo assignProductsInfo(Lot lotData, String gradeFlag, List<ProductP> productPSequence, double productQuantity, String sourceLotName, String validationFlag)
	{
		// 1. Validation

		AssignProductsInfo assignProductsInfo = new AssignProductsInfo();

		assignProductsInfo.setGradeFlag(gradeFlag);
		assignProductsInfo.setProductPSequence(productPSequence);
		assignProductsInfo.setProductQuantity(productQuantity);
		assignProductsInfo.setSourceLotName(sourceLotName);
		assignProductsInfo.setValidationFlag(validationFlag);

		return assignProductsInfo;
	}

	public CancelWaitingToLoginInfo cancelWaitingToLoginInfo(Lot lotData, String areaName, String machineName, String machineRecipeName, List<ProductU> productUdfs)
	{
		// 1. Validation

		CancelWaitingToLoginInfo cancelWaitingToLoginInfo = new CancelWaitingToLoginInfo();

		cancelWaitingToLoginInfo.setAreaName(areaName);
		cancelWaitingToLoginInfo.setMachineName(machineName);
		cancelWaitingToLoginInfo.setMachineRecipeName(machineRecipeName);
		cancelWaitingToLoginInfo.setProductUSequence(productUdfs);

		return cancelWaitingToLoginInfo;
	}

	public ChangeGradeInfo changeGradeInfo(Lot lotData, String lotGrade, String lotProcessState, double productQuantity, List<ProductPGS> productPGSSequence)
	{
		// 1. Validation

		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();

		changeGradeInfo.setLotGrade(lotGrade);
		changeGradeInfo.setLotProcessState(lotProcessState);
		changeGradeInfo.setProductQuantity(productQuantity);
		changeGradeInfo.setProductPGSSequence(productPGSSequence);

		return changeGradeInfo;
	}

	public ChangeSpecInfo changeSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType, String productRequestName,
			String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs, double subProductUnitQuantity1,
			double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", "");
			lotUdfs.put("BEFOREFLOWNAME", "");

			changeSpecInfo.setUdfs(lotUdfs);

			String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? " + "   AND PROCESSFLOWNAME = ? " + "   AND PROCESSFLOWVERSION = ? " + "   AND NODEATTRIBUTE1 = ? "
					+ "   AND NODEATTRIBUTE2 = ? " + "   AND NODETYPE = 'ProcessOperation' ";

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), processOperationName, processOperationVersion };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				String sNodeStack = lotData.getNodeStack();

				if (sNodeStack.contains("."))
				{
					String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
					sNodeStack = sNodeStack.substring(0, sNodeStack.length() - sCurNode.length() - 1);

					sToBeNodeStack = sNodeStack + "." + result[0][0];
				}
				else
				{
					sToBeNodeStack = result[0][0];
				}
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}
	public ChangeSpecInfo changeDummySpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String beforeOperationName, String productionType,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs,
			double subProductUnitQuantity1, double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", beforeOperationName);
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);

			changeSpecInfo.setUdfs(lotUdfs);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), processFlowName, processFlowVersion, processOperationName, processOperationVersion };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				String sNodeStack = lotData.getNodeStack();

				if (sNodeStack.contains("."))
				{
					String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
					sNodeStack = sNodeStack.substring(0, sNodeStack.length() - sCurNode.length() - 1);

					sToBeNodeStack = sNodeStack + "." + result[0][0];
				}
				else
				{
					sToBeNodeStack = result[0][0];
				}
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}

	public ChangeSpecInfo changeSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String beforeOperationName, String productionType,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs,
			double subProductUnitQuantity1, double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", beforeOperationName);
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);

			changeSpecInfo.setUdfs(lotUdfs);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), processOperationName, processOperationVersion };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				String sNodeStack = lotData.getNodeStack();

				if (sNodeStack.contains("."))
				{
					String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
					sNodeStack = sNodeStack.substring(0, sNodeStack.length() - sCurNode.length() - 1);

					sToBeNodeStack = sNodeStack + "." + result[0][0];
				}
				else
				{
					sToBeNodeStack = result[0][0];
				}
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}

	/**
	 * 
	 * CO-INS-0017-01 Reserve Repair
	 * 
	 * @author aim_dhko
	 * @return
	 */
	public ChangeSpecInfo changeSpecInfoForRepairOperation(Lot lotData, String newProcessFlowName, String newProcessFlowVersion, String newProcessOperationName, String newProcessOperationVersion,
			List<ProductU> productUdfs) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(newProcessFlowName);
			changeSpecInfo.setProcessFlowVersion(newProcessFlowVersion);
			changeSpecInfo.setProcessOperationName(newProcessOperationName);
			changeSpecInfo.setProcessOperationVersion(newProcessOperationVersion);
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

			changeSpecInfo.setUdfs(lotUdfs);

			String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? " + "   AND PROCESSFLOWNAME = ? " + "   AND PROCESSFLOWVERSION = ? " + "   AND NODEATTRIBUTE1 = ? "
					+ "   AND NODEATTRIBUTE2 = ? " + "   AND NODETYPE = 'ProcessOperation' ";

			Object[] bind = new Object[] { lotData.getFactoryName(), newProcessFlowName, newProcessFlowVersion, newProcessOperationName, newProcessOperationVersion };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), newProcessFlowName, newProcessOperationName);
			}

			changeSpecInfo.setNodeStack(lotData.getNodeStack() + "." + result[0][0]);

			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}

	public ChangeSpecInfo changeFlow(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String beforeOperationName, String productionType,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs,
			double subProductUnitQuantity1, double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(lotData.getProcessFlowName());
			changeSpecInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", beforeOperationName);
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);

			changeSpecInfo.setUdfs(lotUdfs);

			String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? " + "   AND PROCESSFLOWNAME = ? " + "   AND PROCESSFLOWVERSION = ? " + "   AND NODEATTRIBUTE1 = ? "
					+ "   AND NODEATTRIBUTE2 = ? " + "   AND NODETYPE = 'ProcessOperation' ";

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), processOperationName, processOperationVersion };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				String sNodeStack = lotData.getNodeStack();

				if (sNodeStack.contains("."))
				{
					String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
					sNodeStack = sNodeStack.substring(0, sNodeStack.length() - sCurNode.length() - 1);

					sToBeNodeStack = sNodeStack + "." + result[0][0];
				}
				else
				{
					sToBeNodeStack = result[0][0];
				}
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}

	public ChangeSpecInfo changeSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType, String productRequestName,
			String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs, double subProductUnitQuantity1,
			double subProductUnitQuantity2, String returnProcessFlowName, String returnProcessOperationName) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("RETURNFLOWNAME", returnProcessFlowName);
			lotUdfs.put("RETURNOPERATIONNAME", returnProcessOperationName);

			changeSpecInfo.setUdfs(lotUdfs);

			if (!StringUtils.equals(processFlowName, returnProcessFlowName))
			{
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT NODEID ");
				sql.append("  FROM NODE ");
				sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
				sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
				sql.append("   AND NODEATTRIBUTE1 = :NODEATTRIBUTE1 ");
				sql.append("   AND NODEATTRIBUTE2 = '00001' ");
				sql.append("   AND NODETYPE = 'ProcessOperation' ");

				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("FACTORYNAME", changeSpecInfo.getFactoryName());
				bindMap.put("PROCESSFLOWNAME", processFlowName);
				bindMap.put("PROCESSFLOWVERSION", "00001");
				bindMap.put("NODEATTRIBUTE1", processOperationName);

				List<Map<String, Object>> toBeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				bindMap.clear();
				bindMap.put("FACTORYNAME", changeSpecInfo.getFactoryName());
				bindMap.put("PROCESSFLOWNAME", returnProcessFlowName);
				bindMap.put("PROCESSFLOWVERSION", "00001");
				bindMap.put("NODEATTRIBUTE1", returnProcessOperationName);

				List<Map<String, Object>> returnResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				if (toBeResult.size() == 0)
				{
					log.info("Not Found NodeID");
					throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
				}
				else if (returnResult.size() == 0)
				{
					log.info("Not Found Return NodeID");
					throw new CustomException("Node-0002", lotData.getProductSpecName(), returnProcessFlowName, returnProcessOperationName);
				}
				else
				{
					String sToBeNodeStack = (String) toBeResult.get(0).get("NODEID");
					String sNodeStack = lotData.getNodeStack();
					String sReturnNodeStack = (String) returnResult.get(0).get("NODEID");

					if (sNodeStack.contains("."))
					{
						sToBeNodeStack = sNodeStack + "." + (String) toBeResult.get(0).get("NODEID");
					}
					else
					{
						sToBeNodeStack = sReturnNodeStack + "." + (String) toBeResult.get(0).get("NODEID");
					}
					changeSpecInfo.setNodeStack(sToBeNodeStack);
				}
			}

			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("COMM30090", "RUN or NotOnHold");
		}
	}

	public ChangeSpecInfo changeForceSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType, String productRequestName,
			String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs, double subProductUnitQuantity1,
			double subProductUnitQuantity2, String returnProcessFlowName, String returnProcessOperationName) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("RETURNFLOWNAME", returnProcessFlowName);
			lotUdfs.put("RETURNOPERATIONNAME", returnProcessOperationName);

			changeSpecInfo.setUdfs(lotUdfs);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
			sql.append("   AND NODEATTRIBUTE1 = :NODEATTRIBUTE1 ");
			sql.append("   AND NODEATTRIBUTE2 = '00001' ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("FACTORYNAME", changeSpecInfo.getFactoryName());
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSFLOWVERSION", "00001");
			bindMap.put("NODEATTRIBUTE1", processOperationName);

			List<Map<String, Object>> toBeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			bindMap.clear();
			bindMap.put("FACTORYNAME", changeSpecInfo.getFactoryName());
			bindMap.put("PROCESSFLOWNAME", returnProcessFlowName);
			bindMap.put("PROCESSFLOWVERSION", "00001");
			bindMap.put("NODEATTRIBUTE1", returnProcessOperationName);

			List<Map<String, Object>> returnResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (toBeResult.size() == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else if (returnResult.size() == 0)
			{
				log.info("Not Found Return NodeID");
				throw new CustomException("Node-0002", lotData.getProductSpecName(), returnProcessFlowName, returnProcessOperationName);
			}
			else
			{
				String sToBeNodeStack = (String) toBeResult.get(0).get("NODEID");
				String sNodeStack = lotData.getNodeStack();
				String sReturnNodeStack = (String) returnResult.get(0).get("NODEID");

				if (sNodeStack.contains("."))
				{
					sToBeNodeStack = sNodeStack + "." + (String) toBeResult.get(0).get("NODEID");
				}
				else
				{
					sToBeNodeStack = sReturnNodeStack + "." + (String) toBeResult.get(0).get("NODEID");
				}
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}

			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("COMM30090", "RUN or NotOnHold");
		}
	}

	public ChangeSpecInfo skipInfo(EventInfo eventInfo, Lot lotData, Map<String, String> udfs, List<ProductU> productUdfs) throws CustomException
	{
		Map<String, String> lotUdfs = lotData.getUdfs();
		ChangeSpecInfo skipInfo = new ChangeSpecInfo();

		skipInfo.setProductionType(lotData.getProductionType());
		skipInfo.setProductSpecName(lotData.getProductSpecName());
		skipInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		skipInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		skipInfo.setProductSpec2Version(lotData.getProductSpec2Version());

		skipInfo.setProductRequestName(lotData.getProductRequestName());

		skipInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		skipInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

		skipInfo.setDueDate(lotData.getDueDate());
		skipInfo.setPriority(lotData.getPriority());

		skipInfo.setFactoryName(lotData.getFactoryName());
		skipInfo.setAreaName(lotData.getAreaName());

		skipInfo.setProcessFlowName(lotData.getProcessFlowName());
		skipInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());

		List<Map<String, Object>> sampleLot = MESLotServiceProxy.getLotServiceUtil().getSampleLotDataForSkip(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessFlowVersion());

		List<SampleLot> nextSampleLot = null;
		List<SampleLot> planBnextSampleLot = null;

		if (sampleLot.size() > 0)
		{
			nextSampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineNameForSkip(lotData.getKey().getLotName(), (String) sampleLot.get(0).get("FACTORYNAME"),
					(String) sampleLot.get(0).get("PRODUCTSPECNAME"), (String) sampleLot.get(0).get("PRODUCTSPECVERSION"), (String) sampleLot.get(0).get("PROCESSFLOWNAME"),
					(String) sampleLot.get(0).get("PROCESSFLOWVERSION"), (String) sampleLot.get(0).get("PROCESSOPERATIONNAME"), (String) sampleLot.get(0).get("PROCESSOPERATIONVERSION"),
					lotData.getProcessFlowName(), lotData.getProcessOperationName());

			if (nextSampleLot == null)
			{
				nextSampleLot = null;
			}
		}

		// NEXT OPERATION INFO
		// to next operation
		{
			// current node stack
			String[] arrayNodeStack = StringUtils.split(lotData.getNodeStack(), ".");

			StringBuilder strBuilder = new StringBuilder();
			for (int idx = 0; idx < (arrayNodeStack.length - 1); idx++)
			{
				if (strBuilder.length() > 0)
					strBuilder.append(".");

				strBuilder.append(arrayNodeStack[idx]);
			}

			Node targetNode = new Node();
			if (nextSampleLot != null)
			{
				try
				{
					if (nextSampleLot.size() > 0)
					{
						String nextNodeId = NodeStack.getNodeID(nextSampleLot.get(0).getFactoryName(), nextSampleLot.get(0).getToProcessFlowName(), nextSampleLot.get(0).getToProcessOperationName(),
								nextSampleLot.get(0).getToProcessOperationVersion());

						targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(nextNodeId);

						if (StringUtils.isEmpty(targetNode.getNodeAttribute1()) || StringUtils.isEmpty(targetNode.getProcessFlowName()))
							throw new Exception();
					}
				}
				catch (Exception ex)
				{
					log.info("It is last node");
				}
			}
			else
			{
				targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(arrayNodeStack[arrayNodeStack.length - 1], "Normal", "");
			}

			// at end of flow
			if (!targetNode.getNodeType().equalsIgnoreCase("End"))
			{
				if (strBuilder.length() > 0)
					strBuilder.append(".");
				strBuilder.append(targetNode.getKey().getNodeId());
			}
			else
			{
				List<SampleLot> planBsampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByReturnInfo(lotData.getKey().getLotName(), lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getUdfs().get("RETURNFLOWNAME"), "00001", lotData.getUdfs().get("RETURNOPERATIONNAME"), "00001");

				if (planBsampleLot != null)
				{
					planBnextSampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineNameForSkip(lotData.getKey().getLotName(), planBsampleLot.get(0).getFactoryName(),
							planBsampleLot.get(0).getProductSpecName(), planBsampleLot.get(0).getProductSpecVersion(), planBsampleLot.get(0).getProcessFlowName(),
							planBsampleLot.get(0).getProcessFlowVersion(), planBsampleLot.get(0).getProcessOperationName(), planBsampleLot.get(0).getProcessOperationVersion(),
							lotData.getProcessFlowName(), lotData.getProcessOperationName());

					if (planBnextSampleLot == null)
					{
						planBnextSampleLot = null;
					}
				}

				if (planBnextSampleLot != null)
				{
					try
					{
						if (planBnextSampleLot.size() > 0)
						{
							String nextNodeId = NodeStack.getNodeID(planBnextSampleLot.get(0).getFactoryName(), planBnextSampleLot.get(0).getToProcessFlowName(), planBnextSampleLot.get(0)
									.getToProcessOperationName(), planBnextSampleLot.get(0).getToProcessOperationVersion());

							targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(nextNodeId);

							if (strBuilder.length() > 0)
								strBuilder.append(".");
							strBuilder.append(targetNode.getKey().getNodeId());

							if (StringUtils.isEmpty(targetNode.getNodeAttribute1()) || StringUtils.isEmpty(targetNode.getProcessFlowName()))
								throw new Exception();
						}
					}
					catch (Exception ex)
					{
						log.info("It is last node");
					}
				}
				else
				{
					if (strBuilder.length() > 0)
						strBuilder.append(".");
				}
			}

			skipInfo.setProcessOperationName("");
			skipInfo.setProcessOperationVersion("");
			skipInfo.setNodeStack(strBuilder.toString());
		}

		// trace setting
		udfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());

		Node nextNode = PolicyUtil.getNextOperation(lotData);

		ProcessFlowKey processFlowKey1 = new ProcessFlowKey();
		processFlowKey1.setFactoryName(nextNode.getFactoryName());
		processFlowKey1.setProcessFlowName(nextNode.getProcessFlowName());
		processFlowKey1.setProcessFlowVersion(nextNode.getProcessFlowVersion());
		ProcessFlow nextNodeFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey1);

		if (nextSampleLot != null)
		{
			if (nextSampleLot.size() > 0)
			{
				udfs.put("RETURNFLOWNAME", nextSampleLot.get(0).getReturnProcessFlowName());
				udfs.put("RETURNOPERATIONNAME", nextSampleLot.get(0).getReturnOperationName());
				udfs.put("RETURNOPERATIONVER", nextSampleLot.get(0).getReturnOperationVersion());
			}
		}
		else if (planBnextSampleLot != null)
		{
			if (planBnextSampleLot.size() > 0)
			{
				udfs.put("RETURNFLOWNAME", planBnextSampleLot.get(0).getReturnProcessFlowName());
				udfs.put("RETURNOPERATIONNAME", planBnextSampleLot.get(0).getReturnOperationName());
				udfs.put("RETURNOPERATIONVER", planBnextSampleLot.get(0).getReturnOperationVersion());
			}
		}
		else if (StringUtils.equals(nextNodeFlowData.getProcessFlowType(), "Main") && nextNode.getProcessFlowName().equals(CommonUtil.getValue(lotUdfs, "RETURNFLOWNAME"))
				&& nextNode.getNodeAttribute1().equals(CommonUtil.getValue(lotUdfs, "RETURNOPERATIONNAME")))
		{
			udfs.put("RETURNFLOWNAME", "");
			udfs.put("RETURNOPERATIONNAME", "");
		}
		else
		{
			if (nextNode.getProcessFlowName().equals(CommonUtil.getValue(lotUdfs, "RETURNFLOWNAME")) && nextNode.getNodeAttribute1().equals(CommonUtil.getValue(lotUdfs, "RETURNOPERATIONNAME")))
			{
				String currentNode = lotData.getNodeStack();
				if (StringUtils.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
				{
					String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

					if (originalNode.lastIndexOf(".") > -1)
						originalNode = originalNode.substring(0, originalNode.lastIndexOf("."));

					String sql = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME FROM NODE " + "     WHERE NODEID = ? " + "       AND NODETYPE = 'ProcessOperation' ";

					Object[] bind = new Object[] { originalNode };

					String[][] orginalNodeResult = null;
					try
					{
						orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);
					}
					catch (Exception e)
					{
					}

					if (orginalNodeResult.length > 0)
					{
						udfs.put("RETURNFLOWNAME", orginalNodeResult[0][1]);
						udfs.put("RETURNOPERATIONNAME", orginalNodeResult[0][2]);
					}
				}
			}

		}

		skipInfo.setUdfs(udfs);
		skipInfo.setProductUSequence(productUdfs);

		MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, lotData, false);

		return skipInfo;
	}

	public ChangeSpecInfo changeSortSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, long priority,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType, String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName, String productSpecVersion, double subProductUnitQuantity1, double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			List<Product> productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
			List<ProductU> productUList = new ArrayList<ProductU>();

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(product.getUdfs());

				productUList.add(productU);
			}

			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack("");
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUList);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());

			lotUdfs.put("RETURNFLOWNAME", lotData.getProcessFlowName());
			lotUdfs.put("RETURNOPERATIONNAME", lotData.getProcessOperationName());

			changeSpecInfo.setUdfs(lotUdfs);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName(),
					changeSpecInfo.getProcessOperationVersion() };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				sToBeNodeStack = sToBeNodeStack + "." + result[0][0];

				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("COMM30090", "RUN or NotOnHold");
		}
	}

	public ConsumeMaterialsInfo consumeMaterialsInfo(Lot lotData, List<ConsumedMaterial> consumedMaterialSequence, List<ProductGSC> productGSCSequence)
	{
		// 1. Validation

		ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();

		consumeMaterialsInfo.setConsumedMaterialSequence(consumedMaterialSequence);
		consumeMaterialsInfo.setLotGrade(lotData.getLotGrade());
		consumeMaterialsInfo.setProductGSCSequence(productGSCSequence);
		
		return consumeMaterialsInfo;
	}

	public CreateInfo createInfo(Timestamp dueDate, String factoryName, String lotName, String nodeStack, long priority, String processFlowName, String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion, String productionType, double productQuantity, String productRequestName, String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion, String productType, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, Map<String, String> lotUdfs)
	{
		// Set Variable
		String lotGrade = GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Lot, true).getGrade();

		if (dueDate == null)
			dueDate = TimeStampUtil.getCurrentTimestamp();

		CreateInfo createInfo = new CreateInfo();

		createInfo.setDueDate(dueDate);
		createInfo.setFactoryName(factoryName);
		createInfo.setLotGrade(lotGrade);
		createInfo.setLotName(lotName);
		createInfo.setNodeStack(nodeStack);
		createInfo.setPriority(priority);
		createInfo.setProcessFlowName(processFlowName);
		createInfo.setProcessFlowVersion(processFlowVersion);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessOperationName(processOperationName);
		createInfo.setProcessOperationVersion(processOperationVersion);
		createInfo.setProductionType(productionType);
		createInfo.setProductQuantity(productQuantity);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setProductSpec2Name(productSpec2Name);
		createInfo.setProductSpec2Version(productSpec2Version);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		createInfo.setProductType(productType);
		createInfo.setSubProductType(subProductType);
		createInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		createInfo.setUdfs(lotUdfs);

		return createInfo;
	}

	public CreateRawInfo createRawInfo(Timestamp dueDate, String destinationCarrierName, String factoryName, String lotGrade, String lotName, String lotState, String lotProcessState,
			String lotHoldState, String nodeStack, long priority, String processFlowName, String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productionType, double productQuantity, String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName,
			String productSpecVersion, String productType, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, Lot lotData)
	{
		CreateRawInfo createRawInfo = new CreateRawInfo();

		createRawInfo.setDueDate(dueDate);
		createRawInfo.setCarrierName(destinationCarrierName);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLotGrade(lotGrade);
		createRawInfo.setLotName(lotName);
		createRawInfo.setLotState(lotState);
		createRawInfo.setLotProcessState(lotProcessState);
		createRawInfo.setLotHoldState(lotHoldState);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductQuantity(productQuantity);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductType(productType);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return createRawInfo;
	}

	public CreateWithParentLotInfo createWithParentLotInfo(String areaName, String assignCarrierFlag, Map<String, String> assignCarrierUdfs, String carrierName, Timestamp dueDate, String factoryName,
			Timestamp lastLoggedInTime, String lastLoggedInUser, Timestamp lastLoggedOutTime, String lastLoggedOutUser, String lotGrade, String lotHoldState, String lotName, String lotProcessState,
			String lotState, String machineName, String machineRecipeName, String nodeStack, String originalLotName, long priority, String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName, String processOperationVersion, String productionType, List<ProductP> productPSequence, double productQuantity,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String rootLotName, String sourceLotName, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, Lot lotData)
	{
		CreateWithParentLotInfo createWithParentLotInfo = new CreateWithParentLotInfo();

		createWithParentLotInfo.setAreaName(areaName);
		createWithParentLotInfo.setAssignCarrierFlag(assignCarrierFlag);
		createWithParentLotInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createWithParentLotInfo.setCarrierName(carrierName);
		createWithParentLotInfo.setDueDate(dueDate);
		createWithParentLotInfo.setFactoryName(factoryName);
		createWithParentLotInfo.setLastLoggedInTime(lastLoggedInTime);
		createWithParentLotInfo.setLastLoggedInUser(lastLoggedInUser);
		createWithParentLotInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createWithParentLotInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createWithParentLotInfo.setLotGrade(lotGrade);
		createWithParentLotInfo.setLotHoldState(lotHoldState);
		createWithParentLotInfo.setLotName(lotName);
		createWithParentLotInfo.setLotProcessState(lotProcessState);
		createWithParentLotInfo.setLotState(lotState);
		createWithParentLotInfo.setMachineName(machineName);
		createWithParentLotInfo.setMachineRecipeName(machineRecipeName);
		createWithParentLotInfo.setNodeStack(nodeStack);
		createWithParentLotInfo.setOriginalLotName(originalLotName);
		createWithParentLotInfo.setPriority(priority);
		createWithParentLotInfo.setProcessFlowName(processFlowName);
		createWithParentLotInfo.setProcessFlowVersion(processFlowVersion);
		createWithParentLotInfo.setProcessGroupName(processGroupName);
		createWithParentLotInfo.setProcessOperationName(processOperationName);
		createWithParentLotInfo.setProcessOperationVersion(processOperationVersion);
		createWithParentLotInfo.setProductionType(productionType);
		createWithParentLotInfo.setProductPSequence(productPSequence);
		createWithParentLotInfo.setProductQuantity(productQuantity);
		createWithParentLotInfo.setProductRequestName(productRequestName);
		createWithParentLotInfo.setProductSpec2Name(productSpec2Name);
		createWithParentLotInfo.setProductSpec2Version(productSpec2Version);
		createWithParentLotInfo.setProductSpecName(productSpecName);
		createWithParentLotInfo.setProductSpecVersion(productSpecVersion);
		createWithParentLotInfo.setProductType(productType);
		createWithParentLotInfo.setReworkCount(reworkCount);
		createWithParentLotInfo.setReworkFlag(reworkFlag);
		createWithParentLotInfo.setReworkNodeId(reworkNodeId);
		createWithParentLotInfo.setRootLotName(rootLotName);
		createWithParentLotInfo.setSourceLotName(sourceLotName);
		createWithParentLotInfo.setSubProductType(subProductType);
		createWithParentLotInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createWithParentLotInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return createWithParentLotInfo;
	}

	public CreateAndAssignAllProductsInfo createAndAssignAllProductsInfo(Lot lotData, String areaName, String assignCarrierFlag, Map<String, String> assignCarrierUdfs, String carrierName,
			Timestamp dueDate, String factoryName, Timestamp lastLoggedInTime, String lastLoggedInUser, Timestamp lastLoggedOutTime, String lastLoggedOutUser, String lotGrade, String lotName,
			String machineName, String machineRecipeName, String nodeStack, String originalLotName, long priority, String processFlowName, String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion, String productionType, List<ProductP> productPSequence, double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, String productType, long reworkCount, String reworkFlag, String reworkNodeId,
			String sourceLotName, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		// 1. Validation

		CreateAndAssignAllProductsInfo createAndAssignAllProductsInfo = new CreateAndAssignAllProductsInfo();

		createAndAssignAllProductsInfo.setAreaName(areaName);
		createAndAssignAllProductsInfo.setAssignCarrierFlag(assignCarrierFlag);
		createAndAssignAllProductsInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createAndAssignAllProductsInfo.setCarrierName(carrierName);
		createAndAssignAllProductsInfo.setDueDate(dueDate);
		createAndAssignAllProductsInfo.setFactoryName(factoryName);
		createAndAssignAllProductsInfo.setLastLoggedInTime(lastLoggedInTime);
		createAndAssignAllProductsInfo.setLastLoggedInUser(lastLoggedInUser);
		createAndAssignAllProductsInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createAndAssignAllProductsInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createAndAssignAllProductsInfo.setLotGrade(lotGrade);
		createAndAssignAllProductsInfo.setLotName(lotName);
		createAndAssignAllProductsInfo.setMachineName(machineName);
		createAndAssignAllProductsInfo.setMachineRecipeName(machineRecipeName);
		createAndAssignAllProductsInfo.setNodeStack(nodeStack);
		createAndAssignAllProductsInfo.setOriginalLotName(originalLotName);
		createAndAssignAllProductsInfo.setPriority(priority);
		createAndAssignAllProductsInfo.setProcessFlowName(processFlowName);
		createAndAssignAllProductsInfo.setProcessFlowVersion(processFlowVersion);
		createAndAssignAllProductsInfo.setProcessGroupName(processGroupName);
		createAndAssignAllProductsInfo.setProcessOperationName(processOperationName);
		createAndAssignAllProductsInfo.setProcessOperationVersion(processOperationVersion);
		createAndAssignAllProductsInfo.setProductionType(productionType);
		createAndAssignAllProductsInfo.setProductPSequence(productPSequence);
		createAndAssignAllProductsInfo.setProductQuantity(productQuantity);
		createAndAssignAllProductsInfo.setProductRequestName(productRequestName);
		createAndAssignAllProductsInfo.setProductSpec2Name(productSpec2Name);
		createAndAssignAllProductsInfo.setProductSpec2Version(productSpec2Version);
		createAndAssignAllProductsInfo.setProductSpecName(productSpecName);
		createAndAssignAllProductsInfo.setProductSpecVersion(productSpecVersion);
		createAndAssignAllProductsInfo.setProductType(productType);
		createAndAssignAllProductsInfo.setReworkCount(reworkCount);
		createAndAssignAllProductsInfo.setReworkFlag(reworkFlag);
		createAndAssignAllProductsInfo.setReworkNodeId(reworkNodeId);
		createAndAssignAllProductsInfo.setSourceLotName(sourceLotName);
		createAndAssignAllProductsInfo.setSubProductType(subProductType);
		createAndAssignAllProductsInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createAndAssignAllProductsInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return createAndAssignAllProductsInfo;
	}

	public CreateAndCreateAllProductsInfo createAndCreateAllProductsInfo(Lot lotData, String areaName, String assignCarrierFlag, Map<String, String> assignCarrierUdfs, String carrierName,
			Timestamp dueDate, String factoryName, Timestamp lastLoggedInTime, String lastLoggedInUser, Timestamp lastLoggedOutTime, String lastLoggedOutUser, String lotGrade, String lotName,
			String machineName, String machineRecipeName, String nodeStack, String originalLotName, long priority, String processFlowName, String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion, String productionType, List<ProductPGS> productPGSSequence, double productQuantity, String productRequestName,
			String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, String productType, long reworkCount, String reworkFlag, String reworkNodeId,
			String sourceLotName, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		// 1. Validation

		CreateAndCreateAllProductsInfo createAndCreateAllProductsInfo = new CreateAndCreateAllProductsInfo();

		createAndCreateAllProductsInfo.setAreaName(areaName);
		createAndCreateAllProductsInfo.setAssignCarrierFlag(assignCarrierFlag);
		createAndCreateAllProductsInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createAndCreateAllProductsInfo.setCarrierName(carrierName);
		createAndCreateAllProductsInfo.setDueDate(dueDate);
		createAndCreateAllProductsInfo.setFactoryName(factoryName);
		createAndCreateAllProductsInfo.setLastLoggedInTime(lastLoggedInTime);
		createAndCreateAllProductsInfo.setLastLoggedInUser(lastLoggedInUser);
		createAndCreateAllProductsInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createAndCreateAllProductsInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createAndCreateAllProductsInfo.setLotGrade(lotGrade);
		createAndCreateAllProductsInfo.setLotName(lotName);
		createAndCreateAllProductsInfo.setMachineName(machineName);
		createAndCreateAllProductsInfo.setMachineRecipeName(machineRecipeName);
		createAndCreateAllProductsInfo.setNodeStack(nodeStack);
		createAndCreateAllProductsInfo.setOriginalLotName(originalLotName);
		createAndCreateAllProductsInfo.setPriority(priority);
		createAndCreateAllProductsInfo.setProcessFlowName(processFlowName);
		createAndCreateAllProductsInfo.setProcessFlowVersion(processFlowVersion);
		createAndCreateAllProductsInfo.setProcessGroupName(processGroupName);
		createAndCreateAllProductsInfo.setProcessOperationName(processOperationName);
		createAndCreateAllProductsInfo.setProcessOperationVersion(processOperationVersion);
		createAndCreateAllProductsInfo.setProductionType(productionType);
		createAndCreateAllProductsInfo.setProductPGSSequence(productPGSSequence);
		createAndCreateAllProductsInfo.setProductQuantity(productQuantity);
		createAndCreateAllProductsInfo.setProductRequestName(productRequestName);
		createAndCreateAllProductsInfo.setProductSpec2Name(productSpec2Name);
		createAndCreateAllProductsInfo.setProductSpec2Version(productSpec2Version);
		createAndCreateAllProductsInfo.setProductSpecName(productSpecName);
		createAndCreateAllProductsInfo.setProductSpecVersion(productSpecVersion);
		createAndCreateAllProductsInfo.setProductType(productType);
		createAndCreateAllProductsInfo.setReworkCount(reworkCount);
		createAndCreateAllProductsInfo.setReworkFlag(reworkFlag);
		createAndCreateAllProductsInfo.setReworkNodeId(reworkNodeId);
		createAndCreateAllProductsInfo.setSourceLotName(sourceLotName);
		createAndCreateAllProductsInfo.setSubProductType(subProductType);
		createAndCreateAllProductsInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createAndCreateAllProductsInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return createAndCreateAllProductsInfo;
	}

	public CreateRawInfo createRawInfo(Lot lotData, String areaName, String assignCarrierFlag, Map<String, String> assignCarrierUdfs, String carrierName, Timestamp dueDate, String factoryName,
			Timestamp lastLoggedInTime, String lastLoggedInUser, String lotGrade, String lotHoldState, String lotName, String lotProcessState, String lotState, String machineName,
			String machineRecipeName, String nodeStack, String originalLotName, long priority, String processFlowName, String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productionType, List<ProductP> productPSequence, double productQuantity, String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName, String productSpecVersion, String productType, long reworkCount, String reworkFlag, String reworkNodeId, String sourceLotName,
			String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		// 1. Validation

		CreateRawInfo createRawInfo = new CreateRawInfo();

		createRawInfo.setAreaName(areaName);
		createRawInfo.setAssignCarrierFlag(assignCarrierFlag);
		createRawInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createRawInfo.setCarrierName(carrierName);
		createRawInfo.setDueDate(dueDate);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLastLoggedInTime(lastLoggedInTime);
		createRawInfo.setLastLoggedInUser(lastLoggedInUser);
		createRawInfo.setLotGrade(lotGrade);
		createRawInfo.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_NotOnHold);
		createRawInfo.setLotName(lotName);
		createRawInfo.setLotProcessState(lotProcessState);
		createRawInfo.setLotState(lotState);
		createRawInfo.setMachineName(machineName);
		createRawInfo.setMachineRecipeName(machineRecipeName);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setOriginalLotName(originalLotName);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductPSequence(productPSequence);
		createRawInfo.setProductQuantity(productQuantity);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductType(productType);
		createRawInfo.setReworkCount(reworkCount);
		createRawInfo.setReworkFlag(reworkFlag);
		createRawInfo.setReworkNodeId(reworkNodeId);
		createRawInfo.setSourceLotName(sourceLotName);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return createRawInfo;
	}

	public DeassignProcessGroupInfo deassignProcessGroupInfo(Lot lotData)
	{
		// 1. Validation

		DeassignProcessGroupInfo deassignProcessGroupInfo = new DeassignProcessGroupInfo();

		return deassignProcessGroupInfo;
	}

	public DeassignProductsInfo deassignProductsInfo(Lot lotData, String consumerLotName, String emptyFlag, List<ProductP> productPSequence, double productQuantity)
	{
		// 1. Validation

		DeassignProductsInfo deassignProductsInfo = new DeassignProductsInfo();

		deassignProductsInfo.setConsumerLotName(consumerLotName);
		deassignProductsInfo.setEmptyFlag(emptyFlag);
		deassignProductsInfo.setProductPSequence(productPSequence);
		deassignProductsInfo.setProductQuantity(productQuantity);

		return deassignProductsInfo;
	}

	public MakeCompletedInfo makeCompletedInfo(Lot lotData, List<ProductU> productUdfs)
	{
		// 1. Validation

		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();

		makeCompletedInfo.setProductUSequence(productUdfs);

		return makeCompletedInfo;
	}

	public MakeEmptiedInfo makeEmptiedInfo(Lot lotData, Map<String, String> deassignCarrierUdfs, List<ProductU> productUdfs)
	{
		MakeEmptiedInfo makeEmptiedInfo = new MakeEmptiedInfo();

		makeEmptiedInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		makeEmptiedInfo.setProductUSequence(productUdfs);

		return makeEmptiedInfo;
	}

	public MakeInReworkInfo makeInReworkInfo(Lot lotData, String areaName, String nodeStack, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, List<ProductRU> productRUdfs, String reworkNodeId)
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();

		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setNodeStack(nodeStack);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVersion);
		makeInReworkInfo.setProductRUSequence(productRUdfs);
		makeInReworkInfo.setReworkNodeId(reworkNodeId);

		return makeInReworkInfo;
	}

	public MakeInReworkInfo makeInReworkInfo(Lot lotData, EventInfo eventInfo, String lotName, String processFlowName, String processOperationName, String processOperationVer,
			String returnProcessFlowName, String returnProcessOperationName, String returnProcessOpertaionVer, Map<String, String> udfs, List<ProductRU> productRUdfs)
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(lotData.getAreaName());
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion("00001");
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVer);

		List<Map<String, Object>> nodeInfo = null;
		try
		{
			// nodeInfo = MESLotServiceProxy.getLotServiceUtil().getReworkNodeInfo(lotName);
			nodeInfo = MESLotServiceProxy.getLotServiceUtil().getReworkReturnNodeInfo(lotData.getFactoryName(), returnProcessFlowName, returnProcessOperationName);
		}
		catch (Exception e)
		{
			//
		}

		String tmpNodeStack = "";
		String originalNodeStack = lotData.getNodeStack();

		if (StringUtils.indexOf(originalNodeStack, ".") > 0)
		{
			String[] nodes = StringUtils.split(originalNodeStack, ".");
			for (int i = 0; nodes.length > i + 1; i++)
			{
				if (StringUtils.isEmpty(tmpNodeStack))
				{
					tmpNodeStack = nodes[i];
				}
				else
				{
					tmpNodeStack = tmpNodeStack + "." + nodes[i];
				}
			}
		}

		NodeStack nodeStack = NodeStack.stringToNodeStack((String) nodeInfo.get(0).get("NODEID"));

		// tmpNodeStack = tmpNodeStack + NodeStack.nodeStackToString(nodeStack);

		// 1. Set targetNodeId
		String targetNodeId = null;
		try
		{
			targetNodeId = NodeStack.getNodeID(lotData.getFactoryName(), processFlowName, processOperationName, processOperationVer);
		}
		catch (Exception e)
		{
			//
		}
		// nodeStack.add(targetNodeId);

		if (StringUtils.isEmpty(tmpNodeStack))
		{
			tmpNodeStack = NodeStack.nodeStackToString(nodeStack) + "." + targetNodeId;
		}
		else
		{
			tmpNodeStack = tmpNodeStack + "." + NodeStack.nodeStackToString(nodeStack) + "." + targetNodeId;
		}

		makeInReworkInfo.setNodeStack(tmpNodeStack);

		makeInReworkInfo.setReworkNodeId(targetNodeId);

		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		if (!StringUtils.equals(processFlowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().Arc_Rework))
		{
			udfs.put("RETURNFLOWNAME", returnProcessFlowName);
			udfs.put("RETURNOPERATIONNAME", returnProcessOperationName);
			udfs.put("RETURNOPERATIONVER", returnProcessOpertaionVer);
		}
		if(StringUtils.equals(processFlowData.getKey().getProcessFlowName(), "LRGP21"))
		{
			udfs.put("RETURNFLOWNAME", returnProcessFlowName);
			udfs.put("RETURNOPERATIONNAME", returnProcessOperationName);
			udfs.put("RETURNOPERATIONVER", returnProcessOpertaionVer);
		}
		
		makeInReworkInfo.setUdfs(udfs);
		makeInReworkInfo.setProductRUSequence(productRUdfs);

		return makeInReworkInfo;
	}

	public MakeInReworkInfo makeInReworkInfo(String lotName, String areaName, String processFlowName, String processOperationName, String processOperationVer, String returnFlowName,
			String returnOperationName, String returnOperataionVer, String nodeStack, Map<String, String> udfs, List<ProductRU> productRUdfs)
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion("00001");
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVer);

		makeInReworkInfo.setNodeStack(nodeStack);

		String[] reworkNodeId = StringUtils.split(nodeStack, ".");
		// makeInReworkInfo.setNodeStack(reworkNodeId[1]);
		makeInReworkInfo.setReworkNodeId(reworkNodeId[reworkNodeId.length - 1]);

		udfs.put("RETURNFLOWNAME", returnFlowName);
		udfs.put("RETURNOPERATIONNAME", returnOperationName);
		udfs.put("RETURNOPERATIONVER", returnOperataionVer);

		makeInReworkInfo.setUdfs(udfs);
		makeInReworkInfo.setProductRUSequence(productRUdfs);

		return makeInReworkInfo;
	}

	public MakeNotInReworkInfo makeNotInReworkInfo(Lot lotData, String areaName, String nodeStack, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, List<ProductU> productUdfs)
	{
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(areaName);
		makeNotInReworkInfo.setNodeStack(nodeStack);
		makeNotInReworkInfo.setProcessFlowName(processFlowName);
		makeNotInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeNotInReworkInfo.setProcessOperationName(processOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(processOperationVersion);
		makeNotInReworkInfo.setProductUSequence(productUdfs);

		return makeNotInReworkInfo;
	}

	public MakeNotInReworkInfo makeNotInReworkInfo_CompleteRework(Lot lotData, EventInfo eventInfo, String lotName, String returnFlowName, String returnOperationName, String returnOperataionVer,
			Map<String, String> udfs, List<ProductU> productU)
	{
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(lotData.getAreaName());
		makeNotInReworkInfo.setProcessFlowName(returnFlowName);
		makeNotInReworkInfo.setProcessFlowVersion("00001");
		makeNotInReworkInfo.setProcessOperationName(returnOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(returnOperataionVer);

		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData.getNodeStack());

		int lastIndex = nodeStack.size();
		nodeStack.remove(lastIndex - 1);

		makeNotInReworkInfo.setNodeStack(NodeStack.nodeStackToString(nodeStack));

		// Set Return Info
		String returnFlow = "";
		String returnOperName = "";
		String returnOperVersion = "";
		String[] nodeStackArray = StringUtils.split(makeNotInReworkInfo.getNodeStack(), ".");

		int idx = nodeStackArray.length;

		if (idx > 1)
		{
			try
			{
				Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 2]);
				returnFlow = returnNode.getProcessFlowName();
				returnOperName = returnNode.getNodeAttribute1();
				returnOperVersion = returnNode.getNodeAttribute2();
			}
			catch (Exception e)
			{
			}
		}

		udfs.put("RETURNFLOWNAME", returnFlow);
		udfs.put("RETURNOPERATIONNAME", returnOperName);
		udfs.put("RETURNOPERATIONVER", returnOperVersion);

		makeNotInReworkInfo.setUdfs(udfs);
		makeNotInReworkInfo.setProductUSequence(productU);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());

		return makeNotInReworkInfo;
	}

	public MakeNotInReworkInfo makeNotInReworkInfo(Lot lotData, EventInfo eventInfo, String lotName, String returnFlowName, String returnOperationName, String returnOperataionVer,
			Map<String, String> udfs, List<ProductU> productU) throws CustomException
	{
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();

		makeNotInReworkInfo.setAreaName(lotData.getAreaName());
		makeNotInReworkInfo.setProcessFlowName(returnFlowName);
		makeNotInReworkInfo.setProcessFlowVersion("00001");
		makeNotInReworkInfo.setProcessOperationName(returnOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(returnOperataionVer);

		String currentNode = lotData.getNodeStack().toString();
		NodeStack nodeStack = NodeStack.stringToNodeStack(currentNode);

		int lastIndex = nodeStack.size();

		String processFlowType = MESLotServiceProxy.getLotServiceUtil().getProcessFlowTypeByNodeId(nodeStack.get(lastIndex - 1));

		if (StringUtils.equals(processFlowType, "Rework"))
		{
			String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));
			nodeStack = NodeStack.stringToNodeStack(originalNode);

			makeNotInReworkInfo.setNodeStack(NodeStack.nodeStackToString(nodeStack));
		}
		else
		{
			makeNotInReworkInfo.setNodeStack(lotData.getNodeStack());
		}

		// Set Return Info
		String returnFlow = "";
		String returnOperName = "";
		String returnOperVersion = "";
		String[] nodeStackArray = StringUtils.split(makeNotInReworkInfo.getNodeStack(), ".");

		int idx = nodeStackArray.length;

		if (idx > 1)
		{
			try
			{
				Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 2]);
				returnFlow = returnNode.getProcessFlowName();
				returnOperName = returnNode.getNodeAttribute1();
				returnOperVersion = returnNode.getNodeAttribute2();
			}
			catch (Exception e)
			{
			}
		}

		udfs.put("RETURNFLOWNAME", returnFlow);
		udfs.put("RETURNOPERATIONNAME", returnOperName);
		udfs.put("RETURNOPERATIONVER", returnOperVersion);

		makeNotInReworkInfo.setUdfs(udfs);
		makeNotInReworkInfo.setProductUSequence(productU);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());

		return makeNotInReworkInfo;
	}

	public MakeNotOnHoldInfo makeNotOnHoldInfo(Lot lotData, List<ProductU> productUdfs, Map<String, String> udfs)
	{
		MakeNotOnHoldInfo makeNotOnHoldInfo = new MakeNotOnHoldInfo();

		makeNotOnHoldInfo.setProductUSequence(productUdfs);

		makeNotOnHoldInfo.setUdfs(udfs);

		return makeNotOnHoldInfo;
	}

	public MakeReceivedInfo makeReceivedInfo(Lot lotData, String areaName, String nodeStack, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String productionType, String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion,
			String productType, List<ProductU> productUdfs, String subProductType, String autoShipFlag, Map<String, String> udfs)
	{
		MakeReceivedInfo makeReceivedInfo = new MakeReceivedInfo();

		makeReceivedInfo.setAreaName(areaName);
		makeReceivedInfo.setNodeStack(nodeStack);
		makeReceivedInfo.setProcessFlowName(processFlowName);
		makeReceivedInfo.setProcessFlowVersion(processFlowVersion);
		makeReceivedInfo.setProcessOperationName(processOperationName);
		makeReceivedInfo.setProcessOperationVersion(processOperationVersion);
		makeReceivedInfo.setProductionType(productionType);
		makeReceivedInfo.setProductRequestName(productRequestName);
		makeReceivedInfo.setProductSpec2Name(productSpec2Name);
		makeReceivedInfo.setProductSpec2Version(productSpec2Version);
		makeReceivedInfo.setProductSpecName(productSpecName);
		makeReceivedInfo.setProductSpecVersion(productSpecVersion);
		makeReceivedInfo.setProductType(productType);
		makeReceivedInfo.setProductUSequence(productUdfs);
		makeReceivedInfo.setSubProductType(subProductType);

		udfs.put("AUTOSHIPPINGFLAG", autoShipFlag);
		udfs.put("BANKTYPE", "");

		makeReceivedInfo.setUdfs(udfs);

		return makeReceivedInfo;
	}

	public MakeScrappedInfo makeScrappedInfo(Lot lotData, double productQuantity, List<ProductU> productUSequence)
	{
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();

		makeScrappedInfo.setProductQuantity(productQuantity);
		makeScrappedInfo.setProductUSequence(productUSequence);

		return makeScrappedInfo;
	}

	public MakeShippedInfo makeShippedInfo(Lot lotData, String areaName, String directShipFlag, String factoryName, List<ProductU> productUdfs)
	{
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();

		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		makeShippedInfo.setProductUSequence(productUdfs);
		//makeShippedInfo.setUdfs(lotData.getUdfs());

		return makeShippedInfo;
	}

	public MakeShippedInfo makeShippedInfo(Lot lotData, String areaName, String directShipFlag, String factoryName, List<ProductU> productUdfs, Map<String, String> lotUdfs)
	{
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();

		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		makeShippedInfo.setProductUSequence(productUdfs);
		//makeShippedInfo.setUdfs(lotUdfs);

		return makeShippedInfo;
	}

	public MakeUnScrappedInfo makeUnScrappedInfo(Lot lotData, String lotProcessState, double productQuantity, List<ProductU> productUSequence)
	{
		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo();

		makeUnScrappedInfo.setLotProcessState(lotData.getLotProcessState());
		makeUnScrappedInfo.setProductQuantity(productUSequence.size());
		makeUnScrappedInfo.setProductUSequence(productUSequence);

		return makeUnScrappedInfo;
	}

	public MakeUnShippedInfo makeUnShippedInfo(Lot lotData, String areaName, List<ProductU> productUdfs)
	{
		MakeUnShippedInfo makeUnShippedInfo = new MakeUnShippedInfo();

		makeUnShippedInfo.setAreaName(areaName);
		makeUnShippedInfo.setProductUSequence(productUdfs);

		return makeUnShippedInfo;
	}

	public MakeWaitingToLoginInfo makeWaitingToLoginInfo(Lot lotData, String areaName, String machineName, String machineRecipeName, List<ProductU> productUdfs)
	{
		MakeWaitingToLoginInfo makeWaitingToLoginInfo = new MakeWaitingToLoginInfo();

		makeWaitingToLoginInfo.setAreaName(areaName);
		makeWaitingToLoginInfo.setMachineName(machineName);
		makeWaitingToLoginInfo.setMachineRecipeName(machineRecipeName);
		makeWaitingToLoginInfo.setProductUSequence(productUdfs);

		return makeWaitingToLoginInfo;
	}

	public RecreateInfo recreateInfo(Lot lotData, String areaName, Timestamp dueDate, String newLotName, Map<String, String> newLotUdfs, String nodeStack, long priority, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String productionType, String productRequestName, String productSpec2Name,
			String productSpec2Version, String productSpecName, String productSpecVersion, String productType, List<ProductU> productUdfs, String subProductType, double subProductUnitQuantity1,
			double subProductUnitQuantity2)
	{
		RecreateInfo recreateInfo = new RecreateInfo();

		recreateInfo.setAreaName(areaName);
		recreateInfo.setDueDate(dueDate);
		recreateInfo.setNewLotName(newLotName);
		recreateInfo.setNewLotUdfs(newLotUdfs);
		recreateInfo.setNodeStack(nodeStack);
		recreateInfo.setPriority(priority);
		recreateInfo.setProcessFlowName(processFlowName);
		recreateInfo.setProcessFlowVersion(processFlowVersion);
		recreateInfo.setProcessOperationName(processOperationName);
		recreateInfo.setProcessOperationVersion(processOperationVersion);
		recreateInfo.setProductionType(productionType);
		recreateInfo.setProductRequestName(productRequestName);
		recreateInfo.setProductSpec2Name(productSpec2Name);
		recreateInfo.setProductSpec2Version(productSpec2Version);
		recreateInfo.setProductSpecName(productSpecName);
		recreateInfo.setProductSpecVersion(productSpecVersion);
		recreateInfo.setProductType(productType);
		recreateInfo.setProductUSequence(productUdfs);
		recreateInfo.setSubProductType(subProductType);
		recreateInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return recreateInfo;
	}

	public RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo(Lot lotData, String areaName, Timestamp dueDate, String newLotName, Map<String, String> newLotUdfs, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType,
			List<ProductNPGS> productNPGSSequence, String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion,
			String productType, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo = new RecreateAndCreateAllProductsInfo();

		recreateAndCreateAllProductsInfo.setAreaName(areaName);
		recreateAndCreateAllProductsInfo.setDueDate(dueDate);
		recreateAndCreateAllProductsInfo.setNewLotName(newLotName);
		recreateAndCreateAllProductsInfo.setNewLotUdfs(newLotUdfs);
		recreateAndCreateAllProductsInfo.setNodeStack(nodeStack);
		recreateAndCreateAllProductsInfo.setPriority(priority);
		recreateAndCreateAllProductsInfo.setProcessFlowName(processFlowName);
		recreateAndCreateAllProductsInfo.setProcessFlowVersion(processFlowVersion);
		recreateAndCreateAllProductsInfo.setProcessOperationName(processOperationName);
		recreateAndCreateAllProductsInfo.setProcessOperationVersion(processOperationVersion);
		recreateAndCreateAllProductsInfo.setProductionType(productionType);
		recreateAndCreateAllProductsInfo.setProductNPGSSequence(productNPGSSequence);
		recreateAndCreateAllProductsInfo.setProductRequestName(productRequestName);
		recreateAndCreateAllProductsInfo.setProductSpec2Name(productSpec2Name);
		recreateAndCreateAllProductsInfo.setProductSpec2Version(productSpec2Version);
		recreateAndCreateAllProductsInfo.setProductSpecName(productSpecName);
		recreateAndCreateAllProductsInfo.setProductSpecVersion(productSpecVersion);
		recreateAndCreateAllProductsInfo.setProductType(productType);
		recreateAndCreateAllProductsInfo.setSubProductType(subProductType);
		recreateAndCreateAllProductsInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateAndCreateAllProductsInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		return recreateAndCreateAllProductsInfo;
	}

	public RecreateProductsInfo recreateProductsInfo(Lot lotData, List<ProductNPGS> productNPGSSequence, double productQuantity)
	{
		RecreateProductsInfo recreateProductsInfo = new RecreateProductsInfo();

		recreateProductsInfo.setProductNPGSSequence(productNPGSSequence);
		recreateProductsInfo.setProductQuantity(productQuantity);
		
		return recreateProductsInfo;
	}

	public RelocateProductsInfo relocateProductsInfo(Lot lotData, List<ProductP> productPSequence, double productQuantity)
	{
		RelocateProductsInfo relocateProductsInfo = new RelocateProductsInfo();

		relocateProductsInfo.setProductPSequence(productPSequence);
		relocateProductsInfo.setProductQuantity(productQuantity);

		return relocateProductsInfo;
	}

	public SeparateInfo separateInfo(Lot lotData, List<ProductNSubProductPGQS> productNSubProductPGQSSequence, double productQuantity, Product productData)
	{
		SeparateInfo separateInfo = new SeparateInfo();

		separateInfo.setProductNSubProductPGQSSequence(productNSubProductPGQSSequence);
		separateInfo.setProductQuantity(productQuantity);
		separateInfo.setProductType("Glass");
		separateInfo.setSubProductType("Panel");
		separateInfo.getUdfs().put("CRATENAME", productData.getUdfs().get("CRATENAME").toString());
		
		return separateInfo;
	}

	public SeparateInfo separateInfo(Lot lotData, List<ProductNSubProductPGQS> productNSubProductPGQSSequence, double productQuantity, String productType, String subProductType)
	{
		SeparateInfo separateInfo = new SeparateInfo();

		separateInfo.setProductNSubProductPGQSSequence(productNSubProductPGQSSequence);
		separateInfo.setProductQuantity(productQuantity);
		separateInfo.setProductType(productType);
		separateInfo.setSubProductType(subProductType);

		return separateInfo;
	}

	public SetAreaInfo setAreaInfo(Lot lotData, String areaName, List<ProductU> productUSequence)
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName(areaName);
		setAreaInfo.setProductUSequence(productUSequence);

		return setAreaInfo;
	}

	public SetEventInfo setEventInfo(Lot lotData, double productQuantity, List<ProductU> productUSequence)
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.setProductQuantity(productQuantity);
		setEventInfo.setProductUSequence(productUSequence);

		return setEventInfo;
	}

	public SplitInfo splitInfo(Lot lotData, String childCarrierName, String childLotName, List<ProductP> productPSequence, String productQuantity) throws CustomException
	{
		Lot childLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(childLotName);
		Durable assignDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(childCarrierName);
		Durable deassignDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		// splitInfo & Udfs Set
		SplitInfo splitInfo = new SplitInfo();

		splitInfo.setChildCarrierName(childCarrierName);
		splitInfo.setChildLotName(childLotName);
		splitInfo.setProductPSequence(productPSequence);
		splitInfo.setProductQuantity(Long.valueOf(productQuantity).doubleValue());

		Map<String, String> childLotUdfs = childLotData.getUdfs();
		splitInfo.setChildLotUdfs(childLotUdfs);

		Map<String, String> assignCarrierUdfs = assignDurableData.getUdfs();
		splitInfo.setAssignCarrierUdfs(assignCarrierUdfs);

		Map<String, String> deassignCarrierUdfs = deassignDurableData.getUdfs();
		splitInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return splitInfo;
	}

	public MergeInfo mergeInfo(Lot lotData, Map<String, String> deassignCarrierUdfs, String parentLotName, Map<String, String> parentLotUdfs, List<ProductP> productPSequence)
	{
		MergeInfo mergeInfo = new MergeInfo();

		mergeInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		mergeInfo.setParentLotName(parentLotName);
		mergeInfo.setParentLotUdfs(parentLotUdfs);
		mergeInfo.setProductPSequence(productPSequence);

		return mergeInfo;
	}

	public UndoInfo undoInfo(Lot lotData, String carrierUndoFlag, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey, String undoFlag)
	{
		UndoInfo undoInfo = new UndoInfo();

		undoInfo.setCarrierUndoFlag(carrierUndoFlag);
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);
		undoInfo.setUndoFlag(undoFlag);

		return undoInfo;
	}

	public MakeReleasedInfo makeReleasedInfo(Lot lotData, String areaName, String nodeStack, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String productionType, Map<String, String> assignCarrierUdfs, String carrierName, Timestamp dueDate, long priority)
	{
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();

		makeReleasedInfo.setAreaName(areaName);
		makeReleasedInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		makeReleasedInfo.setCarrierName(carrierName);
		makeReleasedInfo.setDueDate(dueDate);
		makeReleasedInfo.setNodeStack(nodeStack);
		makeReleasedInfo.setPriority(priority);
		makeReleasedInfo.setProcessFlowName(processFlowName);
		makeReleasedInfo.setProcessFlowVersion(processFlowVersion);
		makeReleasedInfo.setProcessOperationName(processOperationName);
		makeReleasedInfo.setProcessOperationVersion(processOperationVersion);
		// overriding by implement parameters
		makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
		makeReleasedInfo.setProductQuantity(0);

		return makeReleasedInfo;
	}

	public List<ProductC> getAllProductCSequence(Lot lotData, List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial> cms)
	{
		// 1. Set Variable
		List<ProductC> productCSequence = new ArrayList<ProductC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductC productC = new ProductC();

			productC.setProductName(product.getKey().getProductName());
			productC.setConsumedMaterialSequence(cms);

			// Add productPSequence By Product
			productCSequence.add(productC);
		}
		return productCSequence;
	}

	public ChangeGradeInfo changeGradeInfo(Lot lotData, String lotGrade, List<ProductPGS> productPGSSequence)
	{
		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();

		changeGradeInfo.setLotGrade(lotGrade);
		changeGradeInfo.setLotProcessState(lotData.getLotProcessState());
		changeGradeInfo.setProductQuantity(lotData.getProductQuantity());
		changeGradeInfo.setProductPGSSequence(productPGSSequence);

		return changeGradeInfo;
	}

	public List<ProductPGSRC> getAllProductPGSRCSequence(Lot lotData)
	{
		// 1. Set Variable
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductPGSRC productPGSRC = new ProductPGSRC();

			productPGSRC.setPosition(product.getPosition());
			productPGSRC.setProductGrade(product.getProductGrade());
			productPGSRC.setProductName(product.getKey().getProductName());
			productPGSRC.setReworkFlag("N");
			productPGSRC.setSubProductGrades1(product.getSubProductGrades1());
			productPGSRC.setSubProductGrades2(product.getSubProductGrades2());
			productPGSRC.setSubProductQuantity1(product.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(product.getSubProductQuantity2());

			// Add productPSequence By Product
			productPGSRCSequence.add(productPGSRC);
		}
		return productPGSRCSequence;
	}

	public AssignProductsInfo assignProducts(EventInfo eventInfo, String lotName, String carrierName)
	{

		List<Product> productList = ProductServiceProxy.getProductService().allProductsByCarrier(carrierName);
		List<ProductP> list = new ArrayList<ProductP>();

		AssignProductsInfo assignProductsInfo = new AssignProductsInfo();
		for (int i = 0; i < productList.size(); i++)
		{
			ProductP productP = new ProductP();
			productP.setProductName(productList.get(i).getKey().getProductName());
			productP.setPosition(productList.get(i).getPosition());
			list.add(productP);
		}
		assignProductsInfo.setGradeFlag("G");
		assignProductsInfo.setProductQuantity(productList.size());
		assignProductsInfo.setSourceLotName(lotName);
		assignProductsInfo.setValidationFlag("Y");
		assignProductsInfo.setProductPSequence(list);

		return assignProductsInfo;
	}

	public DeassignCarrierInfo deassignCarrierInfo(Map<String, String> deassignCarrierUdfs, List<ProductU> productUSequence, Map<String, String> lotUdfs)
	{
		DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();

		deassignCarrierInfo.setProductUSequence(productUSequence);
		deassignCarrierInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		deassignCarrierInfo.setUdfs(lotUdfs);

		return deassignCarrierInfo;
	}

	public AssignCarrierInfo AssignCarrierInfo(Lot lotData, String carrierName, List<ProductP> productPSequence)
	{
		AssignCarrierInfo assignCarrierinfo = new AssignCarrierInfo();
		assignCarrierinfo.setCarrierName(carrierName);
		assignCarrierinfo.setProductPSequence(productPSequence);
		
		return assignCarrierinfo;
	}

	public CreateWithParentLotInfo createWithParentLotInfo(String areaName, String assignCarrierFlag, Map<String, String> assignCarrierUdfs, String carrierName, Timestamp dueDate, String factoryName,
			Timestamp lastLoggedInTime, String lastLoggedInUser, Timestamp lastLoggedOutTime, String lastLoggedOutUser, String lotGrade, String lotHoldState, String lotName, String lotProcessState,
			String lotState, String machineName, String machineRecipeName, String nodeStack, String originalLotName, long priority, String processFlowName, String processFlowVersion,
			String processGroupName, String processOperationName, String processOperationVersion, String productionType, List<ProductP> productPSequence, double productQuantity,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String rootLotName, String sourceLotName, String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, Map<String, String> udfs, Lot lotData)
	{
		CreateWithParentLotInfo createWithParentLotInfo = new CreateWithParentLotInfo();

		createWithParentLotInfo.setAreaName(areaName);
		createWithParentLotInfo.setAssignCarrierFlag(assignCarrierFlag);
		createWithParentLotInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		createWithParentLotInfo.setCarrierName(carrierName);
		createWithParentLotInfo.setDueDate(dueDate);
		createWithParentLotInfo.setFactoryName(factoryName);
		createWithParentLotInfo.setLastLoggedInTime(lastLoggedInTime);
		createWithParentLotInfo.setLastLoggedInUser(lastLoggedInUser);
		createWithParentLotInfo.setLastLoggedOutTime(lastLoggedOutTime);
		createWithParentLotInfo.setLastLoggedOutUser(lastLoggedOutUser);
		createWithParentLotInfo.setLotGrade(lotGrade);
		createWithParentLotInfo.setLotHoldState(lotHoldState);
		createWithParentLotInfo.setLotName(lotName);
		createWithParentLotInfo.setLotProcessState(lotProcessState);
		createWithParentLotInfo.setLotState(lotState);
		createWithParentLotInfo.setMachineName(machineName);
		createWithParentLotInfo.setMachineRecipeName(machineRecipeName);
		createWithParentLotInfo.setNodeStack(nodeStack);
		createWithParentLotInfo.setOriginalLotName(originalLotName);
		createWithParentLotInfo.setPriority(priority);
		createWithParentLotInfo.setProcessFlowName(processFlowName);
		createWithParentLotInfo.setProcessFlowVersion(processFlowVersion);
		createWithParentLotInfo.setProcessGroupName(processGroupName);
		createWithParentLotInfo.setProcessOperationName(processOperationName);
		createWithParentLotInfo.setProcessOperationVersion(processOperationVersion);
		createWithParentLotInfo.setProductionType(productionType);
		createWithParentLotInfo.setProductPSequence(productPSequence);
		createWithParentLotInfo.setProductQuantity(productQuantity);
		createWithParentLotInfo.setProductRequestName(productRequestName);
		createWithParentLotInfo.setProductSpec2Name(productSpec2Name);
		createWithParentLotInfo.setProductSpec2Version(productSpec2Version);
		createWithParentLotInfo.setProductSpecName(productSpecName);
		createWithParentLotInfo.setProductSpecVersion(productSpecVersion);
		createWithParentLotInfo.setProductType(productType);
		createWithParentLotInfo.setReworkCount(reworkCount);
		createWithParentLotInfo.setReworkFlag(reworkFlag);
		createWithParentLotInfo.setReworkNodeId(reworkNodeId);
		createWithParentLotInfo.setRootLotName(rootLotName);
		createWithParentLotInfo.setSourceLotName(sourceLotName);
		createWithParentLotInfo.setSubProductType(subProductType);
		createWithParentLotInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createWithParentLotInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createWithParentLotInfo.setUdfs(udfs);

		return createWithParentLotInfo;
	}

	public CreateWithParentLotInfo createWithParentLotInfo(Lot lotData, String newLotName, String carrierName, Map<String, String> assignCarrierUdfs) throws Exception
	{
		CreateWithParentLotInfo createWithParentLotInfo = new CreateWithParentLotInfo();

		createWithParentLotInfo.setLotName(newLotName);
		createWithParentLotInfo.setProductionType(lotData.getProductionType());
		createWithParentLotInfo.setProductSpecName(lotData.getProductSpecName());
		createWithParentLotInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		createWithParentLotInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		createWithParentLotInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		createWithParentLotInfo.setProcessGroupName(lotData.getProcessGroupName());
		createWithParentLotInfo.setProductRequestName(lotData.getProductRequestName());
		createWithParentLotInfo.setOriginalLotName(newLotName);
		createWithParentLotInfo.setSourceLotName(newLotName);
		createWithParentLotInfo.setRootLotName(newLotName);
		createWithParentLotInfo.setCarrierName(carrierName);
		createWithParentLotInfo.setProductType(lotData.getProductType());
		createWithParentLotInfo.setSubProductType(lotData.getSubProductType());
		createWithParentLotInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		createWithParentLotInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
		createWithParentLotInfo.setProductQuantity(0); 
		createWithParentLotInfo.setLotGrade(lotData.getLotGrade());
		createWithParentLotInfo.setDueDate(lotData.getDueDate());
		createWithParentLotInfo.setPriority(lotData.getPriority());
		createWithParentLotInfo.setFactoryName(lotData.getFactoryName());
		createWithParentLotInfo.setAreaName(lotData.getAreaName());
		createWithParentLotInfo.setLotState(lotData.getLotState());
		createWithParentLotInfo.setLotProcessState(lotData.getLotProcessState());
		createWithParentLotInfo.setLotHoldState(lotData.getLotHoldState());
		createWithParentLotInfo.setLastLoggedInTime(lotData.getLastLoggedInTime());
		createWithParentLotInfo.setLastLoggedInUser(lotData.getLastLoggedInUser());
		createWithParentLotInfo.setLastLoggedOutTime(lotData.getLastLoggedOutTime());
		createWithParentLotInfo.setLastLoggedOutUser(lotData.getLastLoggedOutUser());
		createWithParentLotInfo.setProcessFlowName(lotData.getProcessFlowName());
		createWithParentLotInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		createWithParentLotInfo.setProcessOperationName(lotData.getProcessOperationName());
		createWithParentLotInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
		createWithParentLotInfo.setNodeStack(lotData.getNodeStack());
		createWithParentLotInfo.setReworkFlag(lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_InRework) ? "Y" : "N");
		createWithParentLotInfo.setReworkCount(lotData.getReworkCount());
		createWithParentLotInfo.setReworkNodeId(lotData.getReworkNodeId());
		createWithParentLotInfo.setMachineName(lotData.getMachineName());
		createWithParentLotInfo.setMachineRecipeName(lotData.getMachineRecipeName());
		createWithParentLotInfo.setProductPSequence(new ArrayList<ProductP>());
		createWithParentLotInfo.setAssignCarrierFlag("Y");
		createWithParentLotInfo.setAssignCarrierUdfs(assignCarrierUdfs);

		return createWithParentLotInfo;
	}

	public TransferProductsToLotInfo transferProductsToLotInfo(Lot lotData, String newLotName, int productQuantity, List<ProductP> productPSequence, Map<String, String> destinationLotUdfs,
			Map<String, String> deassignCarrierUdfs) throws Exception
	{
		TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

		transferProductsToLotInfo.setDestinationLotName(newLotName);
		transferProductsToLotInfo.setProductQuantity(productQuantity);
		transferProductsToLotInfo.setEmptyFlag(lotData.getProductQuantity() == productQuantity ? "Y" : "N");
		transferProductsToLotInfo.setValidationFlag("Y");
		transferProductsToLotInfo.setProductPSequence(productPSequence);
		transferProductsToLotInfo.setDestinationLotUdfs(destinationLotUdfs);
		transferProductsToLotInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return transferProductsToLotInfo;
	}

	public TransferProductsToLotInfo transferProductsToLotInfo(String lotName, int productQuantity, List<ProductP> productPSequence, Map<String, String> destinationLotUdfs,
			Map<String, String> deassignCarrierUdfs) throws CustomException
	{
		TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

		transferProductsToLotInfo.setDestinationLotName(lotName);
		transferProductsToLotInfo.setProductQuantity(productQuantity);
		transferProductsToLotInfo.setEmptyFlag("N");
		transferProductsToLotInfo.setValidationFlag("N");
		transferProductsToLotInfo.setProductPSequence(productPSequence);
		transferProductsToLotInfo.setDestinationLotUdfs(destinationLotUdfs);
		transferProductsToLotInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return transferProductsToLotInfo;
	}

	public MakeOnHoldInfo makeOnHoldInfo(List<ProductU> productUSequence, Map<String, String> udfs) throws CustomException
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();

		makeOnHoldInfo.setProductUSequence(productUSequence);
		makeOnHoldInfo.setUdfs(udfs);

		return makeOnHoldInfo;
	}

	public MakeLoggedInInfo makeLoggedInInfo(String machineName, String machineRecipeName, List<ProductC> productCSequence, Map<String, String> udfs)
	{
		MakeLoggedInInfo makeLoggedInInfo = new MakeLoggedInInfo();

		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);
		Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);

		makeLoggedInInfo.setAreaName(machineData.getAreaName());
		makeLoggedInInfo.setMachineName(machineName);
		makeLoggedInInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedInInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedInInfo.setConsumedMaterialSequence(new ArrayList<ConsumedMaterial>());
		makeLoggedInInfo.setProductCSequence(productCSequence);

		makeLoggedInInfo.setUdfs(udfs);
		return makeLoggedInInfo;
	}

	public MakeLoggedOutInfo makeLoggedOutInfo(Lot lotData, String areaName, Map<String, String> assignCarrierUdfs,
			String carrierName,
			String completeFlag, Map<String, String> deassignCarrierUdfs, String lotGrade, String machineName, String machineRecipeName, String nodeStack, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, List<ProductPGSRC> productPGSRCSequence, String reworkFlag, String reworkNodeId,
			Map<String, String> lotUdfs)
	{
		//For LastMainOperation Check
		for(ProductPGSRC productData : productPGSRCSequence)
		{
			log.info(productData.getProductName() + " LastMainOperation : " + productData.getUdfs().get("LASTMAINOPERNAME"));
		}
		
		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();

		makeLoggedOutInfo.setAreaName(areaName);
		makeLoggedOutInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		makeLoggedOutInfo.setCarrierName(carrierName);
		makeLoggedOutInfo.setConsumedMaterialSequence(new ArrayList<ConsumedMaterial>());
		makeLoggedOutInfo.setCompleteFlag(completeFlag);
		makeLoggedOutInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		makeLoggedOutInfo.setLotGrade(lotGrade);
		makeLoggedOutInfo.setMachineName(machineName);
		makeLoggedOutInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedOutInfo.setNodeStack(nodeStack);
		makeLoggedOutInfo.setProcessFlowName(processFlowName);
		makeLoggedOutInfo.setProcessFlowVersion(processFlowVersion);
		makeLoggedOutInfo.setProcessOperationName(processOperationName);
		makeLoggedOutInfo.setProcessOperationVersion(processOperationVersion);
		makeLoggedOutInfo.setProductPGSRCSequence(productPGSRCSequence);
		makeLoggedOutInfo.setReworkFlag(reworkFlag);
		makeLoggedOutInfo.setReworkNodeId(reworkNodeId);
		makeLoggedOutInfo.setUdfs(lotUdfs);

		return makeLoggedOutInfo;
	}

	public void changeGradeForRework(EventInfo eventInfo, String lotName, String lotGrade, double productQuantity, List<ProductPGS> productPGS, Map<String, String> udfs) throws Exception
	{

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);

		Lot lotData = null;
		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();
		changeGradeInfo.setLotGrade(lotGrade);
		changeGradeInfo.setLotProcessState(lotData.getLotProcessState());
		changeGradeInfo.setProductQuantity(productQuantity);
		changeGradeInfo.setProductPGSSequence(productPGS);
		changeGradeInfo.setUdfs(udfs);

		LotServiceProxy.getLotService().changeGrade(lotKey, eventInfo, changeGradeInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public List<ProductP> setProductPSequence(List<Element> productList, String lotName)
	{
		List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Product unScrappedProduct : allUnScrappedProductList)
		{
			for (Element productE : productList)
			{
				String productName = productE.getChild("PRODUCTNAME").getText();

				if (unScrappedProduct.getKey().getProductName().equals(productName))
				{
					String position = productE.getChild("POSITION").getText();
					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

					if (!StringUtils.equals(productData.getProductState(), GenericServiceProxy.getConstantMap().Prod_Scrapped))
					{
						ProductP productP = new ProductP();
						productP.setProductName(productName);
						productP.setPosition(Long.valueOf(position).longValue());

						productPSequence.add(productP);
					}
				}
			}
		}

		return productPSequence;
	}

	public List<ProductP> setProductPSequence(String lotName)
	{
		List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Product unScrappedProduct : allUnScrappedProductList)
		{
			ProductP productP = new ProductP();
			productP.setProductName(unScrappedProduct.getKey().getProductName());
			productP.setPosition(unScrappedProduct.getPosition());
			
			productPSequence.add(productP);
		}

		return productPSequence;
	}
	
	public List<ProductP> setProductPSequenceForDummy(EventInfo eventInfo, List<Element> productList, Lot srcLotData, Lot destLotData, String offSet, String srcProductRequestName,
			String srcProductSpecName) throws CustomException
	{
		List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(srcLotData.getKey().getLotName());

		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Product unScrappedProduct : allUnScrappedProductList)
		{
			for (Element productE : productList)
			{
				String productName = productE.getChild("PRODUCTNAME").getText();

				if (unScrappedProduct.getKey().getProductName().equals(productName))
				{
					String position = productE.getChild("POSITION").getText();
					String slotPosition = productE.getChild("SLOTPOSITION").getText();
					
					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

					if (!StringUtils.equals(productData.getProductState(), GenericServiceProxy.getConstantMap().Prod_Scrapped))
					{
						ProductP productP = new ProductP();
						productP.setProductName(productName);
						productP.setPosition(Long.valueOf(position).longValue());
						
						productP.getUdfs().put("SLOTPOSITION", slotPosition);
						productP.getUdfs().put("DUMMYGLASSFLAG", "Y");

						if (StringUtils.isEmpty(productData.getUdfs().get("OFFSET")))
							productP.getUdfs().put("OFFSET", offSet);
						
						productPSequence.add(productP);

						ExtendedObjectProxy.getDummyProductAssignService().createDummyProductAssignReturnInfo(eventInfo, productData, srcLotData, destLotData, srcProductRequestName, srcProductSpecName);
					}
				}
			}
		}

		return productPSequence;
	}
	
	public List<ProductPGSRC> setProductPGSRCSequenceForManualCancel(Element bodyElement) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
			String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);

			productPGSRC.setProductGrade(productGrade);

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);

			if (StringUtils.isEmpty(productRecipe))
			{
				productRecipe = lotData.getMachineRecipeName();
			}

			String VcrProductName = SMessageUtil.getChildText(productElement, "VCRPRODUCTNAME", false);

			if (StringUtils.isEmpty(VcrProductName))
			{
				VcrProductName = "";
			}

			productPGSRC.getUdfs().put("VCRPRODUCTNAME", VcrProductName);
			productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
			productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);

			// 2020-11-17	dhko	0000406	
			// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
			if (!isSorter)
			{
				productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
			}
			
			if(!StringUtils.isEmpty(processingInfo))
			{
				if(!StringUtils.equals(processingInfo, "B"))
				{
					productPGSRC.getUdfs().put("REWORKFLAG", ""); // 2020.03.04 jhyeom.
				}
			}
			
			if(StringUtils.isEmpty(processingInfo))
			{
				processingInfo = productData.getUdfs().get("PROCESSINGINFO");
			}

			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	public List<ProductC> setProductCSequence(String lotName)
	{

		List<ProductC> productCSequence = new ArrayList<ProductC>();

		try
		{
			List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product productData : productDataList)
			{
				Map<String, String> productUdfs = productData.getUdfs();
				productUdfs.put("PRODUCTRECIPE", "");

				ProductC productC = new ProductC();
				productC.setProductName(productData.getKey().getProductName());
				productC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

				productCSequence.add(productC);
			}
		}
		catch (NotFoundSignal ne)
		{
			// consider 0 size
			log.error("setProductCSequence size is zero");
		}

		return productCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(Element bodyElement) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
			String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);

			productPGSRC.setProductGrade(productGrade);

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);

			if (StringUtils.isEmpty(productRecipe))
			{
				productRecipe = lotData.getMachineRecipeName();
			}

			String VcrProductName = SMessageUtil.getChildText(productElement, "VCRPRODUCTNAME", false);

			if (StringUtils.isEmpty(VcrProductName))
			{
				VcrProductName = "";
			}

			productPGSRC.getUdfs().put("VCRPRODUCTNAME", VcrProductName);
			productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
			productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);

			// 2020-11-17	dhko	0000406	
			// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
			if (!isSorter)
			{
				productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
			}
			
			if(!StringUtils.isEmpty(processingInfo))
			{
				if(!StringUtils.equals(processingInfo, "B"))
				{
					productPGSRC.getUdfs().put("REWORKFLAG", ""); // 2020.03.04 jhyeom.
				}
			}
			
			if(StringUtils.isEmpty(processingInfo))
			{
				processingInfo = productData.getUdfs().get("PROCESSINGINFO");
			}
			
			ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);
			// 2019-04-17 DoubleRun Check.
			if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Main") && !CommonUtil.equalsIn(processingInfo, "B", "S"))
			{
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", productData.getProcessFlowName());
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", productData.getProcessOperationName());
			}
			else if (StringUtils.equals(currentFlowData.getProcessFlowType(), "BackUp") && !CommonUtil.equalsIn(processingInfo, "B", "S"))
			{
				//BackUp flow case
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", lotData.getUdfs().get("BACKUPMAINFLOWNAME"));
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", lotData.getUdfs().get("BACKUPMAINOPERNAME"));
			}

			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(Element bodyElement, boolean isInReworkFlow, EventInfo eventInfo) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//GetOperationSpec Mantis 0000441
		
		String processOperationName = SMessageUtil.getChildText(bodyElement, "PROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getChildText(bodyElement, "PROCESSOPERATIONVERSION", false);
		
		ProcessOperationSpec operationSpecData = null;
		
		String machineGroupName = machineData.getMachineGroupName();
		
		if(StringUtil.isNotEmpty(processOperationName))
		{
			if(productElementList.size() > 0)
			{
				Product firstProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(productElementList.get(0).getChildText("PRODUCTNAME"));
				operationSpecData = CommonUtil.getProcessOperationSpec(firstProductData.getFactoryName(), processOperationName, processOperationVersion);
			}
		}
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Set ProductGrade Mantis 0000441
		
		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
			
			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));

			//Mantis 0000441 
			//String productGrade =  SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String productGrade = productData.getProductGrade();
			
			if(StringUtil.equals(processOperationName, "21220") && StringUtil.equals(GenericServiceProxy.getConstantMap().MachineGroup_AOI, machineGroupName))
			{
				String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
				productPGSRC.setSubProductGrades1(subProductGradesMachine);
			}
			else if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP"))
			{
				String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
				String gradesList = SMessageUtil.getChildText(productElement, "SUBPRODUCTGRADES", false);
				String subProductGradesMES = productData.getSubProductGrades1();
				
				if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
						&& subProductGradesMES.length() == subProductGradesMachine.length())
				{
					List <String> gradeResult = makeSubProductGradeListForRepair(subProductGradesMES, subProductGradesMachine, productData.getFactoryName(), eventInfo, machineName,gradesList,productData);
					
					productPGSRC.setSubProductGrades1(gradeResult.get(0));
					productGrade = gradeResult.get(1);
				}
				else
				{
					log.info("EQ report SUBPRODUCTJUDGES error.");
					productPGSRC.setSubProductGrades1(productData.getSubProductGrades1());
				}
			}
			//EVA IJP Repair P Judge Panel auto change to S Judge
			else if(StringUtil.equals(processOperationName, "21410") && StringUtil.equals(GenericServiceProxy.getConstantMap().MachineGroup_EVA, machineGroupName))
			{
				String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
				String subProductGradesMES = productData.getSubProductGrades1();
				if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
						&& subProductGradesMES.length() == subProductGradesMachine.length())
				{
					List<String> IJPSubProductGrades= makeSubProductGradeListForIJPRepair(subProductGradesMES, subProductGradesMachine, productData.getProductType(), productData.getFactoryName(), productName, eventInfo, productData.getProcessOperationName(), productData.getProcessFlowName());
					productPGSRC.setSubProductGrades1(IJPSubProductGrades.get(0));
					productGrade = IJPSubProductGrades.get(1);
				}
				
			}
			String turnFlag = SMessageUtil.getChildText(productElement, "TURNFLAG", false);
			if(isSorter)
			{
				int turnCount=0;
                if(StringUtils.isNotEmpty(productData.getUdfs().get("TURNCOUNT")))
                {
                	turnCount= Integer.parseInt(productData.getUdfs().get("TURNCOUNT"));
                }
				if(StringUtils.equals(turnFlag, "Y"))
				{
	                turnCount+=1;
				}
                productPGSRC.getUdfs().put("TURNCOUNT", Integer.toString(turnCount));
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
			
			String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);

			//if (isInReworkFlow && StringUtils.equals(productGrade, GenericServiceProxy.getConstantMap().ProductGrade_G))
			if (isInReworkFlow)
			{
				// Do not change ProductGrade when EQP reports grade in Rework Flow
				productGrade = productData.getProductGrade();
			}

			productPGSRC.setProductGrade(productGrade);
			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
			String productRecipe  = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);
			String VcrProductName = SMessageUtil.getChildText(productElement, "VCRPRODUCTNAME", false);
			String dummyUseCount = SMessageUtil.getChildText(productElement, "DUMMYUSECOUNT", false);

			if (StringUtils.isEmpty(VcrProductName))
			{
				VcrProductName = "";
			}
			productPGSRC.getUdfs().put("VCRPRODUCTNAME", VcrProductName);
			productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
			productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);

			// 2020-11-17	dhko	0000406	
			// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
			if (!isSorter)
			{
				productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
			}
			
			if(StringUtils.isEmpty(processingInfo))
			{
				processingInfo = productData.getUdfs().get("PROCESSINGINFO");
			}
			
			if(productData.getProductionType().equals("D")&&machineData.getMachineGroupName().equals(GenericServiceProxy.getConstantMap().MachineGroup_Oven)
					&&StringUtils.isNotEmpty(dummyUseCount)&&!StringUtils.equals(dummyUseCount, "0"))
			{
				productPGSRC.getUdfs().put("OVENDUMMYCOUNT", dummyUseCount);
			}
			
			// DoubleRun Check.
			//For LastMainOperation Check
			log.info("LastMainOperationName before set : " + productData.getUdfs().get("LASTMAINOPERNAME"));
			String portType = SMessageUtil.getChildText(bodyElement, "PORTTYPE", false);
			ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);
			if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Main") && !CommonUtil.equalsIn(processingInfo, "B", "S")&&!portType.equals("PL"))
			{
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", productData.getProcessFlowName());
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", productData.getProcessOperationName());
			}
			else if (StringUtils.equals(currentFlowData.getProcessFlowType(), "BackUp") && !CommonUtil.equalsIn(processingInfo, "B", "S")&&!portType.equals("PL"))
			{
				//BackUp flow case
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", lotData.getUdfs().get("BACKUPMAINFLOWNAME"));
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", lotData.getUdfs().get("BACKUPMAINOPERNAME"));
			}
			log.info("LastMainOperationName after set : " + productPGSRC.getUdfs().get("LASTMAINOPERNAME"));
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(List<Element> productList)
	{
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		for (Element productElement : productList)
		{
			String productName = productElement.getChildText("PRODUCTNAME");

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);
			productPGSRC.setPosition(Long.valueOf(productElement.getChildText("POSITION")));

			Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));

			if (productElement.getChild("PRODUCTGRADE") != null)
				productPGSRC.setProductGrade(productElement.getChildText("PRODUCTGRADE"));
			else if (productElement.getChild("PRODUCTJUDGE") != null)
				productPGSRC.setProductGrade(productElement.getChildText("PRODUCTJUDGE"));
			else
				productPGSRC.setProductGrade(productData.getProductGrade());

			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(String lotName) throws CustomException
	{
		// use PostCell TrackOutPB
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for (int i = 0; i < productList.size(); i++)
		{
			String productName = productList.get(i).getKey().getProductName();

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);
			productPGSRC.setPosition(Long.valueOf(productList.get(i).getPosition()));

			Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));

			productPGSRC.setProductGrade(productData.getProductGrade());
			productPGSRC.setSubProductGrades1(productData.getSubProductGrades1());
			productPGSRC.setSubProductGrades2(productData.getSubProductGrades2());
			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}

	public List<ProductU> setProductUSequence(String lotName)
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product product : productList)
			{
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.getUdfs().put("REWORKFLAG", "");
				productUList.add(productU);
			}
		}
		catch (NotFoundSignal ne)
		{
			//
		}
		return productUList;
	}

	public List<ProductU> setProductUSequenceForDummy(String lotName)
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product product : productList)
			{
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.getUdfs().put("REWORKFLAG", "");
				productU.getUdfs().put("DUMMYGLASSFLAG", "");
				productUList.add(productU);
			}
		}
		catch (NotFoundSignal ne)
		{
			//
		}
		return productUList;
	}

	public static String getProductAttributeByLength(String machineName, String productName, String fieldName, String itemValue) throws CustomException
	{
		String sql = "SELECT LENGTH(" + fieldName + ") AS LEN, " + fieldName + " FROM PRODUCT WHERE PRODUCTNAME = :productName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("productName", productName);

		try
		{

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if (sqlResult.size() > 0)
			{
				if (!(StringUtils.isEmpty((String) sqlResult.get(0).get(fieldName))))
				{
					if (Integer.parseInt((String) sqlResult.get(0).get("LEN")) != itemValue.length())
					{
						itemValue = (String) sqlResult.get(0).get(fieldName);
						log.debug("Product Info Different In Field: " + fieldName + ". Reported Value: " + itemValue + ". Machine ID: " + machineName);
					}
				}
				else
				{
					sql = "UPDATE PRODUCT SET " + fieldName + " = :itemValue WHERE PRODUCTNAME = :productName";
					bindMap.put("itemValue", itemValue);

					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
				}
			}
		}
		catch (Exception e)
		{
			throw new CustomException("COM-9001", fieldName);
		}
		return itemValue;
	}

	public List<ProductNSubProductPGQS> setProductNSubProductPGQSSequenceCUT(org.jdom.Document doc) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{

		List<ProductNSubProductPGQS> productNSubProductPGQSList = new ArrayList<ProductNSubProductPGQS>();

		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil.getBundleServiceClass(ProductServiceUtil.class);

		Element productList = SMessageUtil.getBodySequenceItem(doc, "PRODUCTLIST", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		if (productList != null)
		{

			for (Iterator<?> iteratorLotList = productList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element productE = (Element) iteratorLotList.next();
				String productName = SMessageUtil.getChildText(productE, "PRODUCTNAME", true);

				Element subProduct = null;

				ProductKey productKey = new ProductKey();
				productKey.setProductName(productName);

				Product productData = null;
				productData = ProductServiceProxy.getProductService().selectByKey(productKey);

				ProductSpecKey productSpecKey = new ProductSpecKey();
				productSpecKey.setFactoryName(productData.getFactoryName());
				productSpecKey.setProductSpecName(productData.getProductSpecName());
				productSpecKey.setProductSpecVersion(productData.getProductSpecVersion());

				ProductSpec productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
				ProcessOperationSpec po = CommonUtil.getProcessOperationSpec(productData.getFactoryName(), productData.getProcessOperationName(), productData.getProcessOperationVersion());

				int x = Integer.valueOf(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
				int y = Integer.valueOf(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));

				subProduct = new Element("GlassList");

				List<String> glassList = new ArrayList<String>();

				List<String> argSeq = new ArrayList<String>();
				argSeq.add(productName);

				List<String> names = new ArrayList<String>();

				int separateQty = 0;

				if (StringUtils.equals(factoryName, "OLED"))
				{
					separateQty = 2;
					// Generate Glass Name .
					names = productServiceUtil.generateGlassName(productName, separateQty);
				}

				for (int i = 0; i < names.size(); i++)
				{
					glassList.add(names.get(i));
				}

				for (int i = 0; i < glassList.size(); i++)
				{
					Element selement = new Element("Glass");
					JdomUtils.addElement(selement, "PRODUCTNAME", productName);
					JdomUtils.addElement(selement, "GLASSNAME", glassList.get(i));
					JdomUtils.addElement(selement, "POSITION", String.valueOf(productData.getPosition()));
					JdomUtils.addElement(selement, "SUBPRODUCTUNITQUANTITY1", Integer.valueOf(x * y / 2).toString());

					// Start add for change OLED product Grade from Array
					Element v4element = new Element("GLASSGRADE");
					if ((StringUtil.equals(factoryName, "OLED")))
					{
						String sqlForSelectGlass = " SELECT GLASSJUDGE FROM CT_GLASSJUDGE WHERE GLASSNAME = :GLASSNAME ";
						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("GLASSNAME", glassList.get(i));
						List<Map<String, Object>> sqlGlassJudgeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelectGlass, bindMap);
						if (sqlGlassJudgeResult.size() > 0)
						{
							v4element.setText((String) sqlGlassJudgeResult.get(0).get("GLASSJUDGE"));
						}
						else
						{
							v4element.setText("G");
						}
					}
					else
					{
						v4element.setText("G");
					}
					selement.addContent(v4element);

					// Set SUBPRODUCTGRADES1
					Element v5element = new Element("SUBPRODUCTGRADES1");

					GlassJudge glassJudgeData = new GlassJudge();
					String subProductGrades1 = "";
					try
					{
						glassJudgeData = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false, new Object[] { glassList.get(i) });
					}
					catch (Exception e)
					{
						subProductGrades1 = GradeDefUtil.generateGradeSequence(productData.getFactoryName(), "SubProduct", true, (Integer.valueOf(x * y / 2)));
						v5element.setText(subProductGrades1);
					}

					if (StringUtils.isEmpty(glassJudgeData.getPanelGrades()))
					{
						if (StringUtils.isEmpty(subProductGrades1))
							v5element.setText(GradeDefUtil.generateGradeSequence(productData.getFactoryName(), "SubProduct", true, (Integer.valueOf(x * y / 2))));
					}
					else
						v5element.setText(glassJudgeData.getPanelGrades());

					selement.addContent(v5element);

					Element v6element = new Element("SUBPRODUCTGRADES2");

					if (productData.getSubProductQuantity2() > 0)
					{
						v6element.setText(StringUtils.repeat(GradeDefUtil.getGrade(productData.getFactoryName(), "SubProduct", true).getGrade(),
								Integer.valueOf(String.valueOf(productData.getSubProductUnitQuantity2()))));
					}
					else
					{
						v6element.setText("");
					}
					selement.addContent(v6element);

					JdomUtils.addElement(selement, "SUBPRODUCTQUANTITY1", Integer.valueOf(x * y / 2).toString());
					JdomUtils.addElement(selement, "SUBPRODUCTQUANTITY2", "0");

					subProduct.addContent(selement);
				}

				productE.addContent(subProduct);

				ProductNSubProductPGQS productNSubProductPGQS = new ProductNSubProductPGQS();
				productNSubProductPGQS.setProductName(productName);
				productNSubProductPGQS.setUdfs(productServiceUtil.setNamedValueSequence(SMessageUtil.getChildText(productE, "PRODUCTNAME", true), productE));
				productNSubProductPGQS.setSubProductPGQSSequence(setProductPGQSSequenceCUT(factoryName, productName, subProduct));

				productNSubProductPGQSList.add(productNSubProductPGQS);
			}
		}

		return productNSubProductPGQSList;
	}

	private List<ProductPGQS> setProductPGQSSequenceCUT(String factoryName, String productName, org.jdom.Element element) throws FrameworkErrorSignal, NotFoundSignal
	{
		List<ProductPGQS> productPGQSList = new ArrayList<ProductPGQS>();

		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);

		Product productData = null;
		productData = ProductServiceProxy.getProductService().selectByKey(productKey);
		
		int gradeposition = 0;

		if (element != null)
		{
			for (Iterator iterator = element.getChildren().iterator(); iterator.hasNext();)
			{
				gradeposition++;

				Element subProductE = (Element) iterator.next();

				ProductPGQS productPGQS = new ProductPGQS();

				if (StringUtils.equals(factoryName, "OLED"))
				{
					productPGQS.setProductName(subProductE.getChild("GLASSNAME").getText());
					productPGQS.setPosition(Long.valueOf(subProductE.getChild("POSITION").getText()));
					productPGQS.setSubProductUnitQuantity1(Double.valueOf(subProductE.getChild("SUBPRODUCTUNITQUANTITY1").getText()).doubleValue());
					productPGQS.setProductGrade(subProductE.getChild("GLASSGRADE").getText());
					productPGQS.setSubProductGrades1(subProductE.getChild("SUBPRODUCTGRADES1").getText());
					productPGQS.setSubProductGrades2(subProductE.getChild("SUBPRODUCTGRADES2").getText());
					productPGQS.setSubProductUnitQuantity2(Double.valueOf(subProductE.getChild("SUBPRODUCTQUANTITY2").getText()).doubleValue());
					productPGQS.setSubProductQuantity1(Double.valueOf(subProductE.getChild("SUBPRODUCTQUANTITY1").getText()).doubleValue());
					productPGQS.setSubProductQuantity2(Double.valueOf(subProductE.getChild("SUBPRODUCTQUANTITY2").getText()).doubleValue());
				}
				if (StringUtils.equals(factoryName, "POSTCELL"))
				{
					productPGQS.setProductName(subProductE.getChild("GLASSNAME").getText());
					productPGQS.setPosition(Long.valueOf(subProductE.getChild("POSITION").getText()));
					productPGQS.setSubProductUnitQuantity1(0);
					productPGQS.setProductGrade(subProductE.getChild("GLASSGRADE").getText());
					productPGQS.setSubProductGrades1(subProductE.getChild("SUBPRODUCTGRADES1").getText());
					productPGQS.setSubProductGrades2(subProductE.getChild("SUBPRODUCTGRADES2").getText());
					productPGQS.setSubProductUnitQuantity2(0);
					productPGQS.setSubProductQuantity1(0);
					productPGQS.setSubProductQuantity2(0);
				}
				
				productPGQS.getUdfs().put("CANCELFLAG", productData.getUdfs().get("CANCELFLAG"));
				productPGQS.getUdfs().put("CRATENAME", productData.getUdfs().get("CRATENAME"));
				productPGQS.getUdfs().put("ARRAYLOTNAME", productData.getUdfs().get("ARRAYLOTNAME"));
				productPGQS.getUdfs().put("MAINMACHINENAME", productData.getUdfs().get("MAINMACHINENAME"));
				
				productPGQSList.add(productPGQS);
			}

			gradeposition = 0;
		}

		return productPGQSList;
	}

	public List<ProductGSC> setProductGSCSequenceForAssy(Lot lotData, List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial> cms, String productName, org.jdom.Document doc)
			throws CustomException
	{
		Element root = doc.getDocument().getRootElement();
		Element bodyElement = root.getChild("Body");

		String machineName = bodyElement.getChild("MACHINENAME").getText();
		String productJudge = bodyElement.getChild("PRODUCTJUDGE").getText();

		// 1. Set Variable
		List<ProductGSC> productPGSSequence = new ArrayList<ProductGSC>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();
			if (productName.equals(product.getKey().getProductName()))
			{
				ProductGSC productGSC = new ProductGSC();

				String subProductQuantity1 = String.valueOf(product.getSubProductQuantity1());
				String subProductQuantity2 = String.valueOf(product.getSubProductQuantity2());
				String productGrade = CommonUtil.getProductAttributeByLength(machineName, productName, "PRODUCTGRADE", product.getProductGrade());

				productGSC.setConsumedMaterialSequence(cms);
				productGSC.setProductGrade(productGrade);
				productGSC.setProductName(product.getKey().getProductName());
				productGSC.setSubProductGrades1(product.getSubProductGrades1());
				productGSC.setSubProductGrades2(product.getSubProductGrades2());
				productGSC.setSubProductQuantity1(Double.valueOf(subProductQuantity1).doubleValue());
				productGSC.setSubProductQuantity2(Double.valueOf(subProductQuantity2).doubleValue());
				productGSC.getUdfs().put("PAIRPRODUCTNAME", cms.get(0).getMaterialName());
				
				// Add productPSequence By Product
				productPGSSequence.add(productGSC);
			}
		}
		return productPGSSequence;
	}

	public Map<String, String> makeCutUdfs12(Map<String, String> fromUdfs, int gradeposition)
	{
		Map<String, String> toudfs = new HashMap<String, String>();

		toudfs = copyUdfs1(fromUdfs, toudfs);

		return toudfs;
	}

	public static Map<String, String> copyUdfs1(Map<String, String> fromUdfs, Map<String, String> toUdfs)
	{

		for (String key : fromUdfs.keySet())
		{
			toUdfs.put(key, fromUdfs.get(key));
		}

		return toUdfs;
	}

	public Lot getLotData(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

			return lotData;

		}
		catch (Exception e)
		{
			throw new CustomException("LOT-9000", lotName);
		}
	}

	public String getLotNameByCarrierName(String carrierName) throws FrameworkErrorSignal, NotFoundSignal
	{
		if (log.isInfoEnabled())
		{
			log.info("carrierName = " + carrierName);
		}

		String lotName = "";
		if (carrierName != null && carrierName != "")
		{
			String condition = "WHERE carrierName = ? " + "And RowNum = 1";

			Object[] bindSet = new Object[] { carrierName };

			try
			{
				List<Lot> arrayList = LotServiceProxy.getLotService().select(condition, bindSet);
				lotName = arrayList.get(0).getKey().getLotName();
			}
			catch (Exception e)
			{
				log.error(e);
				lotName = "";
			}
		}

		if (log.isInfoEnabled())
		{
			log.info("Return lotName = " + lotName);
		}

		return lotName;
	}

	public String getCarrierNameByLotName(String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{

		String carrierName = "";
		if (lotName != null && lotName != "")
		{
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);

			Lot lot = null;

			try
			{
				lot = LotServiceProxy.getLotService().selectByKey(lotKey);
				carrierName = lot.getCarrierName();
			}
			catch (Exception e)
			{
				log.error(e);
				carrierName = "";
			}

		}
		else
		{
		}

		return carrierName;
	}

	public Lot getLotInfoBydurableName(String carrierName) throws CustomException
	{

		String condition = "WHERE carrierName = :carrierName ";

		Object[] bindSet = new Object[] { carrierName };
		List<Lot> lotList = new ArrayList<Lot>();
		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			throw new CustomException("CARRIER-9002", carrierName);
		}

		return lotList.get(0);
	}

	public List<Lot> getLotListBydurableName(String carrierName) throws CustomException
	{
		String condition = "WHERE carrierName = :carrierName ";

		Object[] bindSet = new Object[] { carrierName };
		List<Lot> lotList = new ArrayList<Lot>();
		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			throw new CustomException("CARRIER-9002", carrierName);
		}

		return lotList;
	}

	public List<LotFutureAction> getLotFutureActionList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, ");
		sql.append("       FACTORYNAME, ");
		sql.append("       PROCESSFLOWNAME, ");
		sql.append("       PROCESSFLOWVERSION, ");
		sql.append("       PROCESSOPERATIONNAME, ");
		sql.append("       PROCESSOPERATIONVERSION, ");
		sql.append("       POSITION ");
		sql.append("  FROM LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("ORDER BY PROCESSOPERATIONNAME, POSITION ASC ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);

		List<Map<String, Object>> sqlList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		List<LotFutureAction> lotFutureActionList = new ArrayList<LotFutureAction>();
		for (int i = 0; i < sqlList.size(); i++)
		{
			LotFutureActionKey lotFutureActionKey = new LotFutureActionKey();
			lotFutureActionKey.setFactoryName((String) sqlList.get(i).get("FACTORYNAME"));
			lotFutureActionKey.setLotName((String) sqlList.get(i).get("LOTNAME"));
			lotFutureActionKey.setProcessFlowName((String) sqlList.get(i).get("PROCESSFLOWNAME"));
			lotFutureActionKey.setProcessFlowVersion((String) sqlList.get(i).get("PROCESSFLOWVERSION"));
			lotFutureActionKey.setProcessOperationName((String) sqlList.get(i).get("PROCESSOPERATIONNAME"));
			lotFutureActionKey.setProcessOperationVersion((String) sqlList.get(i).get("PROCESSOPERATIONVERSION"));
			lotFutureActionKey.setPosition(Long.parseLong((String) sqlList.get(i).get("POSITION")));

			LotFutureAction lotFutureAction = LotServiceProxy.getLotFutureActionService().selectByKey(lotFutureActionKey);

			lotFutureActionList.set(i, lotFutureAction);
		}

		return lotFutureActionList;
	}

	public List<LotFutureAction> getLotFutureActionList(String lotName, String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, ");
		sql.append("       FACTORYNAME, ");
		sql.append("       PROCESSFLOWNAME, ");
		sql.append("       PROCESSFLOWVERSION, ");
		sql.append("       PROCESSOPERATIONNAME, ");
		sql.append("       PROCESSOPERATIONVERSION, ");
		sql.append("       POSITION ");
		sql.append("  FROM LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("ORDER BY PROCESSOPERATIONNAME, POSITION ASC ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> sqlList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		List<LotFutureAction> lotFutureActionList = new ArrayList<LotFutureAction>();
		for (int i = 0; i < sqlList.size(); i++)
		{
			LotFutureActionKey lotFutureActionKey = new LotFutureActionKey();
			lotFutureActionKey.setFactoryName((String) sqlList.get(i).get("FACTORYNAME"));
			lotFutureActionKey.setLotName((String) sqlList.get(i).get("LOTNAME"));
			lotFutureActionKey.setProcessFlowName((String) sqlList.get(i).get("PROCESSFLOWNAME"));
			lotFutureActionKey.setProcessFlowVersion((String) sqlList.get(i).get("PROCESSFLOWVERSION"));
			lotFutureActionKey.setProcessOperationName((String) sqlList.get(i).get("PROCESSOPERATIONNAME"));
			lotFutureActionKey.setProcessOperationVersion((String) sqlList.get(i).get("PROCESSOPERATIONVERSION"));
			lotFutureActionKey.setPosition(Long.parseLong((String) sqlList.get(i).get("POSITION")));

			LotFutureAction lotFutureAction = LotServiceProxy.getLotFutureActionService().selectByKey(lotFutureActionKey);

			lotFutureActionList.set(i, lotFutureAction);
		}

		return lotFutureActionList;
	}

	public ProcessFlow getProcessFlowData(Lot lotData) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	public ProcessFlow getProcessFlowData(Product productData) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(productData.getFactoryName());
		processFlowKey.setProcessFlowName(productData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(productData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	public ProcessFlow getProcessFlowData(Node nodeData) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(nodeData.getFactoryName());
		processFlowKey.setProcessFlowName(nodeData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(nodeData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	public List<ProductPGS> getProductPGSSequence(String lotName, String productGrade, List<Product> productList, Map<String, String> udfs) throws CustomException
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		for (Iterator<Product> iteratorProduct = productList.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductPGS productPGS = new ProductPGS();

			productPGS.setProductName(product.getKey().getProductName());
			productPGS.setPosition(product.getPosition());
			productPGS.setProductGrade(productGrade);
			productPGS.setSubProductGrades1(product.getSubProductGrades1());
			productPGS.setSubProductGrades2(product.getSubProductGrades2());
			productPGS.setSubProductQuantity1(product.getSubProductQuantity1());
			productPGS.setSubProductQuantity2(product.getSubProductQuantity2());
			productPGS.setUdfs(udfs);

			productPGSSequence.add(productPGS);
		}

		return productPGSSequence;
	}

	public SplitInfo splitLotInfo(Lot lotData, String childLotName, List<ProductP> productPSequence, String productQuantity) throws CustomException
	{
		// splitInfo & Udfs Set
		SplitInfo splitInfo = new SplitInfo();

		splitInfo.setChildLotName(childLotName);
		splitInfo.setProductPSequence(productPSequence);
		splitInfo.setProductQuantity(Long.valueOf(productQuantity).doubleValue());

		Map<String, String> lotUdfs = lotData.getUdfs();
		splitInfo.setUdfs(lotUdfs);

		Map<String, String> childLotUdfs = lotData.getUdfs();
		splitInfo.setChildLotUdfs(childLotUdfs);

		return splitInfo;
	}

	public List<ProductU> setProductUSequence(List<Element> productList)
	{
		List<ProductU> productUSequence = new ArrayList<ProductU>();

		for (Element productElement : productList)
		{
			String productName = productElement.getChildText("PRODUCTNAME");

			ProductU productU = new ProductU();

			productU.setProductName(productName);

			productUSequence.add(productU);
		}

		return productUSequence;
	}

	public List<ProductU> getAllUnScrapProductUSequence(Lot lotData) throws CustomException
	{
		// 1. Set Variable
		List<ProductU> ProductUSequence = new ArrayList<ProductU>();
		List<Product> productDatas = new ArrayList<Product>();

		// 2. Get Product Data List
		try
		{
			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// 3. Get ProductName, Position By Product
		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();
			
			ProductU productU = new ProductU();
			productU.setProductName(product.getKey().getProductName());

			// Add productPSequence By Product
			ProductUSequence.add(productU);
		}

		return ProductUSequence;
	}

	public SetEventInfo makeJobDownFlag(Lot lotData) throws CustomException
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("JOBDOWNFLAG", "Y");
		return setEventInfo;
	}

	public Node getBeforeOperationNode(Lot afterTrackOutLot) throws CustomException
	{
		try
		{
			Node beforeOperationNodeId = ProcessFlowServiceProxy.getProcessFlowService().getNode(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProcessFlowName(),
					afterTrackOutLot.getProcessFlowVersion(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

			List<Arc> connectArc = ProcessFlowServiceProxy.getArcService().select("toNodeId = ?", new Object[] { beforeOperationNodeId.getKey().getNodeId() });

			Node OperationNode = ProcessFlowServiceProxy.getNodeService().getNode(connectArc.get(0).getKey().getFromNodeId());

			return OperationNode;

		}
		catch (Exception ex)
		{
			// all components are mandatory
			log.warn("this Lot might not be located anywhere");
		}
		return null;

	}

	public List<Map<String, Object>> checkBlockCount(String productSpecName) throws CustomException
	{

		String sql = "SELECT ENUMNAME, ENUMVALUE, DESCRIPTION, DEFAULTFLAG FROM ENUMDEFVALUE WHERE ENUMNAME = 'BlockCount' AND ENUMVALUE = :ENUMVALUE";
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("ENUMVALUE", productSpecName);

		List<Map<String, Object>> sqlCount = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlCount;
	}

	public CreateInfo newCreateInfo(Timestamp dueDate, String factoryName, String lotName, String nodeStack, long priority, String processFlowName, String processFlowVersion, String processGroupName,
			String processOperationName, String processOperationVersion, String productionType, double productQuantity, String productRequestName, String productSpec2Name, String productSpec2Version,
			String productSpecName, String productSpecVersion, String productType, String subProductType, String projectName, double subProductUnitQuantity1, double subProductUnitQuantity2,
			Map<String, String> lotUdfs)
	{
		// Set Variable
		String lotGrade = GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Lot, true).getGrade();

		if (dueDate == null)
			dueDate = TimeStampUtil.getCurrentTimestamp();

		CreateInfo createInfo = new CreateInfo();

		createInfo.setDueDate(dueDate);
		createInfo.setFactoryName(factoryName);
		createInfo.setLotGrade(lotGrade);
		createInfo.setLotName(lotName);
		createInfo.setNodeStack(nodeStack);
		createInfo.setPriority(priority);
		createInfo.setProcessFlowName(processFlowName);
		createInfo.setProcessFlowVersion(processFlowVersion);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessOperationName(processOperationName);
		createInfo.setProcessOperationVersion(processOperationVersion);
		createInfo.setProductionType(productionType);
		createInfo.setProductQuantity(productQuantity);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setProductSpec2Name(productSpec2Name);
		createInfo.setProductSpec2Version(productSpec2Version);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		createInfo.setProductType(productType);
		createInfo.setSubProductType(subProductType);
		createInfo.setProductRequestName(projectName);
		createInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

		createInfo.setUdfs(lotUdfs);

		return createInfo;
	}

	public List<ProductU> setProductPdfs(String lotName, String projectName)
	{

		List<ProductU> productUList = new ArrayList<ProductU>();

		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product product : productList)
			{
				ProductUU productU = new ProductUU();
				productU.setProductName(product.getKey().getProductName());
				productU.setProductRequestName(projectName);

				productUList.add(productU);
			}
		}
		catch (NotFoundSignal ne)
		{
			//
		}
		return productUList;
	}

	public DeassignCarrierInfo deassignCarrierInfo(Lot lotData, Durable durableData, List<ProductU> productUSequence)
	{
		DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();

		deassignCarrierInfo.setProductUSequence(productUSequence);

		Map<String, String> deassignCarrierUdfs = durableData.getUdfs();
		deassignCarrierInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);

		return deassignCarrierInfo;
	}

	public List<ProductP> setProductPSequence(List<Element> productElementList) throws CustomException
	{
		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);

			ProductP productP = new ProductP();
			productP.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productP.setPosition(Long.valueOf(position));

			productPSequence.add(productP);
		}

		return productPSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequence(Machine machineData, String lotName, List<Element> productElementList) throws CustomException
	{		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

		List<Product> productList;

		try
		{
			productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);
		}
		catch (Exception ex)
		{
			//PRODUCT-9019:No product in Lot
			throw new CustomException("PRODUCT-9019");
		}

		for (Product productData : productList)
		{
			String productName = productData.getKey().getProductName();

			for (Element productElement : productElementList)
			{
				if (productName.equals(SMessageUtil.getChildText(productElement, "PRODUCTNAME", true)))
				{
					ProductPGSRC productPGSRC = new ProductPGSRC();
					productPGSRC.setProductName(productName);

					String position = SMessageUtil.getChildText(productElement, "POSITION", true);
					productPGSRC.setPosition(Long.valueOf(position));

					String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
					if (StringUtils.isEmpty(productGrade))
						productPGSRC.setProductGrade(productData.getProductGrade());
					else
						productPGSRC.setProductGrade(productGrade);

					productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
					productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

					productPGSRC.setReworkFlag("N");

					// Consumable ignored
					productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
					String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);
					
					// 2020-11-17	dhko	0000406	
					// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
					if (!isSorter)
					{
						productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
					}
					
					productPGSRCSequence.add(productPGSRC);
				}
			}
		}

		return productPGSRCSequence;
	}

	public List<MaterialProduct> getMaterialProductList_PhotoMask(Lot lotData) throws greenFrameDBErrorSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String lotName = lotData.getKey().getLotName();
		String materialKind = constantMap.MaterialKind_Durable;
		String materialType = constantMap.MaterialType_PhotoMask;
		String factoryName = lotData.getFactoryName();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String machineName = lotData.getMachineName();

		StringBuilder sql = new StringBuilder();
		sql.append(" WHERE (TIMEKEY,PRODUCTNAME, MATERIALNAME) IN ");
		sql.append("       (SELECT M.TIMEKEY,M.PRODUCTNAME, M.MATERIALNAME ");
		sql.append("          FROM PRODUCT P, CT_MATERIALPRODUCT M, LOT L ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND L.LOTNAME = :LOTNAME ");
		sql.append("           AND P.LOTNAME = L.LOTNAME ");
		sql.append("           AND M.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("           AND M.MATERIALTYPE = :MATERIALTYPE ");
		sql.append("           AND M.MACHINENAME = :MACHINENAME ");
		sql.append("           AND M.MATERIALKIND = :MATERIALKIND ");
		sql.append("           AND M.EVENTNAME = :EVENTNAME ");
		sql.append("           AND M.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND M.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("           AND M.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("           AND M.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND M.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("           AND M.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("           AND M.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("           AND M.TIMEKEY BETWEEN TO_CHAR (L.LASTLOGGEDINTIME, 'YYYYMMDDHH24MISS') || '000000' ");
		sql.append("                             AND TO_CHAR (SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6')) ");

		Object[] bindSet = new Object[] { lotName, materialType, machineName, materialKind, "ComponentOutUnit", factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion };

		List<MaterialProduct> materialProductList = ExtendedObjectProxy.getMaterialProductService().select(sql.toString(), bindSet);

		return materialProductList;
	}

	public List<MaterialProduct> getMaterialProductList(Lot lotData) throws greenFrameDBErrorSignal, CustomException
	{
		int processCount = 1;
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String lotName = lotData.getKey().getLotName();
		String materialKind = constantMap.MaterialKind_Durable;
		String materialType = constantMap.MaterialType_PhotoMask;
		String factoryName = lotData.getFactoryName();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String machineName = lotData.getMachineName();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NVL (MAX (M.PROCESSCOUNT), 1) AS PROCESSCOUNT ");
		sql.append("  FROM PRODUCT P, CT_MATERIALPRODUCT M ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND P.LOTNAME = :LOTNAME ");
		sql.append("   AND M.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND M.MATERIALTYPE = :MATERIALTYPE ");
		sql.append("   AND M.MACHINENAME = :MACHINENAME ");
		sql.append("   AND M.MATERIALKIND = :MATERIALKIND ");
		sql.append("   AND M.EVENTNAME = :EVENTNAME ");
		sql.append("   AND M.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND M.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND M.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND M.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND M.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND M.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);
		args.put("MATERIALTYPE", "PhotoMask");
		args.put("MACHINENAME", machineName);
		args.put("MATERIALKIND", "Durable");
		args.put("EVENTNAME", "ComponentOutUnit");
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String sProcessCount = ConvertUtil.getMapValueByName(result.get(0), "PROCESSCOUNT");
			processCount = Integer.parseInt(sProcessCount);
		}

		String condition = " WHERE lotName = ? AND materialKind = ? AND materialType = ? AND factoryName = ? AND productSpecName = ? AND productSpecVersion = ? "
				+ "AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ? AND processOperationVersion = ? AND machineName = ? AND NVL (PROCESSCOUNT, 1) = ? ";

		Object[] bindSet = new Object[] { lotName, materialKind, materialType, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, machineName, processCount };

		List<MaterialProduct> materialProductList = ExtendedObjectProxy.getMaterialProductService().select(condition, bindSet);

		return materialProductList;
	}

	public ChangeSpecInfo changeSortSpecInfoForIFI(EventInfo eventInfo, Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState,
			String lotState, long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productionType,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, double subProductUnitQuantity1,
			double subProductUnitQuantity2, List<String> productList, String jobType) throws CustomException
	{
		if (StringUtils.equals(lotData.getLotProcessState(), "WAIT"))
		{
			List<Product> productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
			List<ProductU> productUList = new ArrayList<ProductU>();

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();

				Map<String, String> udfs = new HashMap<String, String>();

				if (StringUtils.equals(jobType, "Split(TP to IFI)") && productList != null && productList.contains(product.getKey().getProductName()))
				{
					udfs.put("CHANGESHOPLOTNAME", lotData.getKey().getLotName());
					ExtendedObjectProxy.getOriginalProductInfoService().insertOriginalProductInfo(eventInfo, product);
				}

				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(udfs);

				productUList.add(productU);
			}

			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack("");
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUList);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());

			lotUdfs.put("RETURNFLOWNAME", lotData.getProcessFlowName());
			lotUdfs.put("RETURNOPERATIONNAME", lotData.getProcessOperationName());

			changeSpecInfo.setUdfs(lotUdfs);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName(),
					changeSpecInfo.getProcessOperationVersion() };

			String[][] result = null;
			result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);

			if (result.length == 0)
			{
				log.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				sToBeNodeStack = sToBeNodeStack + "." + result[0][0];

				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("COMM30090", "RUN or NotOnHold");
		}
	}
	
	public String makeSubProductGradeList(String subProductGradeMES, String subProductGradeEQP)
	{
		String subProductGrades = "";
		for(int i = 0 ; i < subProductGradeMES.length() ; i++)
		{
			String panelGrade = "";
			String panelGradeMES = String.valueOf(subProductGradeMES.charAt(i));
			String panelGradeEQP = String.valueOf(subProductGradeMES.charAt(i));
			
			if(StringUtil.equals("S", panelGradeMES) || StringUtil.equals("S", panelGradeEQP))
			{
				panelGrade = "S";
			}
			else if(StringUtil.equals("C", panelGradeMES) || StringUtil.equals("C", panelGradeEQP))
			{
				panelGrade = "C";
			}
			else if(StringUtil.equals("N", panelGradeMES) || StringUtil.equals("N", panelGradeEQP))
			{
				panelGrade = "N";
			}
			else if(StringUtil.equals("P", panelGradeMES) || StringUtil.equals("CP", panelGradeEQP))
			{
				panelGrade = "P";
			}
			else
			{
				panelGrade = "G";
			}
			
			subProductGrades += panelGrade;
		}
		
		return subProductGrades;
	}
	
	public List <String> makeSubProductGradeList(String subProductGradeMES, String subProductGradeEQP, String productType, String factoryName, String productName, EventInfo eventInfo, String processOperationName, String processFlowName) throws CustomException
	{
		String subProductGrades = "";
		String gradesList="";
		List <String> result = new ArrayList<String>();
		
		int cntS = 0;
		int cntC = 0;
		int cntN = 0;
		int cntG = 0;
		int cntP = 0;
		
		for(int i = 0 ; i < subProductGradeMES.length() ; i++)
		{
			String panelGrade = "";
			String panelGradeMES = String.valueOf(subProductGradeMES.charAt(i));
			String panelGradeEQP = String.valueOf(subProductGradeEQP.charAt(i));
			
			if(StringUtil.equals("S", panelGradeMES) || StringUtil.equals("S", panelGradeEQP))
			{
				panelGrade = "S";
				cntS++;
			}
			else if(StringUtil.equals("C", panelGradeMES) || StringUtil.equals("C", panelGradeEQP))
			{
				panelGrade = "C";
				cntC++;
			}
			else if(StringUtil.equals("N", panelGradeMES) || StringUtil.equals("N", panelGradeEQP))
			{
				panelGrade = "N";
				cntN++;
			}
			else if(StringUtil.equals("P", panelGradeMES) || StringUtil.equals("P", panelGradeEQP))
			{
				panelGrade = "P";
				cntP++;
			}
			else
			{
				panelGrade = "G";
				cntG++;
			}
			
			subProductGrades += panelGrade;
		}
		
		result.add(subProductGrades); //result(0) subProductGrades
		result.add(getProductGradeByRSRule(subProductGrades, factoryName, productType, cntS, cntC, cntN, cntG, cntP, productName, eventInfo, processOperationName, processFlowName,gradesList));//result(1) ProductGrade
		
		return result;
	}
	
	public List <String> makeSubProductGradeListForRepair(String subProductGradeMES, String subProductGradeEQP,  String factoryName,  EventInfo eventInfo, String machineName,String gradesList ,Product productData) throws CustomException
	{
		String subProductGrades = "";
		List <String> result = new ArrayList<String>();
		List <String> controlPanelList = new ArrayList<String>();
		Boolean PEP0Repair=false;
		String productGrade="";
		
		int cntS = 0;
		int cntC = 0;
		int cntN = 0;
		int cntG = 0;
		int cntP = 0;
		
		String sqlForReserveRepair= "SELECT DISTINCT I.PANEL_ID,I.PANELNUM FROM CT_RESERVEREPAIRPOLICY P,CT_RESERVEREPAIRPANELINFO I "
				+ " WHERE P.PROCESSFLOWNAME=I.PROCESSFLOWNAME "
				+ " AND P.PROCESSOPERATIONNAME=I.STEP_ID  "
				+ " AND P.REPAIRFLOWNAME=:PROCESSFLOWNAME "
				+ " AND P.ACTUALREPAIROPERATION <> :PROCESSOPERATIONNAME "
				+ " AND I.SHEET_ID=:PRODUCTNAME ";
			
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSFLOWNAME",productData.getProcessFlowName());
		bindMap.put("PROCESSOPERATIONNAME",productData.getProcessOperationName());
		bindMap.put("PRODUCTNAME",productData.getKey().getProductName());
		
		List<Map<String, Object>> reserveRepairList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForReserveRepair, bindMap);
		
		if(reserveRepairList!=null&&reserveRepairList.size()>0)
		{
			for(int j=0;j<reserveRepairList.size();j++)
			{
				if(StringUtils.equals("-5", reserveRepairList.get(j).get("PANELNUM").toString()))
				{
					PEP0Repair=true;
					break;
				}
			}
		}
		
		for(int i = 0 ; i < subProductGradeMES.length() ; i++)
		{
			String panelGrade = "";
			String panelGradeMES = String.valueOf(subProductGradeMES.charAt(i));
			String panelGradeEQP = String.valueOf(subProductGradeEQP.charAt(i));
			Boolean reserveRepairFlag=false;
			
			if(reserveRepairList!=null&&reserveRepairList.size()>0)
			{
				for(int j=0;j<reserveRepairList.size();j++)
				{
					if(StringUtils.equals(Integer.toString(i+1), reserveRepairList.get(j).get("PANELNUM").toString()))
					{
						panelGrade = "P";
						cntP++;
						reserveRepairFlag=true;
						break;
					}
				}
			}
			
			if(!reserveRepairFlag)
			{
			    if(StringUtil.equals("C", panelGradeEQP))
				{
					panelGrade = "S";
					cntS++;
					controlPanelList.add(Integer.toString(i+1));
				}
			    else if(StringUtil.equals("S", panelGradeMES) || StringUtil.equals("S", panelGradeEQP))
				{
					panelGrade = "S";
					cntS++;
				}
				else if(StringUtil.equals("C", panelGradeMES))
				{
					panelGrade = "C";
					cntC++;
				}
				else if(StringUtil.equals("N", panelGradeMES) || StringUtil.equals("N", panelGradeEQP))
				{
					panelGrade = "N";
					cntN++;
				}
				else if(StringUtil.equals("G", panelGradeMES) || StringUtil.equals("G", panelGradeEQP))
				{
					panelGrade = "G";
					cntG++;
				}
				else
				{
					panelGrade = "P";
					cntP++;
				}
			}
			
			subProductGrades += panelGrade;
		}
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_REPAIRRESULT(TIMEKEY,PRODUCTNAME,PANELNUM,MACHINENAME,PROCESSOPERATIONNAME,EVENTNAME,EVENTCOMMENT) VALUES(?,?,?,?,?,?,?)");
		for (String panelNum : controlPanelList) 
		{			
            List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(TimeUtils.getCurrentEventTimeKey());
			lotBindList.add(productData.getKey().getProductName());
			lotBindList.add(panelNum);
			lotBindList.add(machineName);
			lotBindList.add(productData.getProcessOperationName());
			lotBindList.add("Insert");
			lotBindList.add("Insert Control Panel");
			
			updateLotArgList.add(lotBindList.toArray());

		}
		if(updateLotArgList!=null&&updateLotArgList.size()>0)
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		}		
		result.add(subProductGrades); //result(0) subProductGrades
		productGrade=getProductGradeByRSRuleForRepair(subProductGrades, factoryName, cntS, cntC, cntN, cntG, cntP, eventInfo, gradesList,productData);
		if(StringUtils.equals(productGrade, "G")&&PEP0Repair)
		{
			productGrade="P";
		}
		result.add(productGrade);//result(1) ProductGrade
		
		return result;
	}
	
	public String getProductGradeByRSRule(String subProductGrades, String factoryName, String productType, int cntS, int cntC, int cntN, int cntG, int cntP, String productName, EventInfo eventInfo, String processoperationName, String processFlowName,String gradesList) throws CustomException
	{
		String productGrade = "";
		
		if(StringUtil.equals(productType, GenericServiceProxy.getConstantMap().ProductType_Sheet))
		{
			ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(factoryName, processoperationName,"00001");
			eventInfo.setEventName("GlassJudge");
			
			int arrayJudgeData = 96;
			
			int cntGlassP = 0;
			int cntGlassG = 0;
			int cntGlassN = 0;
			
			//Glass1
			cntS = 0;
			cntC = 0;
			cntN = 0;
			cntG = 0;
			cntP = 0;
			String glass1Grade = "";
			String glass1SubProductGrades = subProductGrades.substring(0, subProductGrades.length() / 2);
			String glass1GradesList=gradesList.substring(0, gradesList.length() / 2);
			
			for(int i = 0 ; i < glass1SubProductGrades.length() ; i++)
			{
				String panelGrade = String.valueOf(subProductGrades.charAt(i));
				
				if(StringUtil.equals("S", panelGrade))
				{
					cntS++;
				}
				else if(StringUtil.equals("C", panelGrade))
				{
					cntC++;
				}
				else if(StringUtil.equals("N", panelGrade))
				{
					cntN++;
				}
				else if(StringUtil.equals("G", panelGrade))
				{
					cntG++;
				}
				else
				{
					cntP++;
				}
			}
			
		    if(cntN + cntS + cntC >= arrayJudgeData||glass1GradesList.contains("C"))
			{
				glass1Grade = "N";
				cntGlassN++;
			}
			else if(cntP > 0 && cntN + cntS + cntC < arrayJudgeData)
			{
				glass1Grade = "P";
				cntGlassP++;
			}
			else
			{
				glass1Grade = "G";
				cntGlassG++;
			}
			
			//Glass2
			cntS = 0;
			cntC = 0;
			cntN = 0;
			cntG = 0;
			cntP = 0;
			String glass2Grade = "";
			String glass2SubProductGrades = subProductGrades.substring(subProductGrades.length()/2, subProductGrades.length());
			String glass2GradesList=gradesList.substring(gradesList.length()/2, gradesList.length());
			
			for(int i = subProductGrades.length()/2 ; i < subProductGrades.length() ; i++)
			{
				String panelGrade = String.valueOf(subProductGrades.charAt(i));
				
				if(StringUtil.equals("S", panelGrade))
				{
					cntS++;
				}
				else if(StringUtil.equals("C", panelGrade))
				{
					cntC++;
				}
				else if(StringUtil.equals("N", panelGrade))
				{
					cntN++;
				}
				else if(StringUtil.equals("G", panelGrade))
				{
					cntG++;
				}
				else
				{
					cntP++;
				}
			}
			
		    if(cntN + cntS + cntC >= arrayJudgeData||glass2GradesList.contains("C"))
			{
				glass2Grade = "N";
				cntGlassN++;
			}
			else if(cntP > 0 && cntN + cntS + cntC < arrayJudgeData)
			{
				glass2Grade = "P";
				cntGlassP++;
			}
			else
			{
				glass2Grade = "G";
				cntGlassG++;
			}
			
			if(cntGlassN == 2)
			{
				productGrade = "N";
			}
			else if(cntGlassP > 0 && cntGlassN <= 1)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
			
			//Update CT_GlassJudge
			GlassJudge glassJudge1 = new GlassJudge();
			
			try
			{
				glassJudge1 = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false,  new Object[] {productName + "1"});
			}
			catch (Exception e)
			{
				glassJudge1 = null;
			}
			
			if(glassJudge1 != null)
			{
				glassJudge1.setGlassJudge(glass1Grade);
				glassJudge1.setPanelGrades(glass1SubProductGrades);
				glassJudge1.setProcessFlowName(processFlowName);
				glassJudge1.setProcessOperationName(processoperationName);
				glassJudge1.setLastEventComment(eventInfo.getEventComment());
				glassJudge1.setLastEventName(eventInfo.getEventName());
				glassJudge1.setLastEventTime(eventInfo.getEventTime());
				glassJudge1.setLastEventUser(eventInfo.getEventUser());
				if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP")&&glass1GradesList.contains("C"))
				{
					glassJudge1.setNGFlag("Y");
				}
				
				ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassJudge1);
			}
			
			GlassJudge glassJudge2 = new GlassJudge();
			
			try
			{
				glassJudge2 = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false,  new Object[] {productName + "2"});
			}
			catch (Exception e)
			{
				glassJudge2 = null;
			}
			
			if(glassJudge2 != null)
			{
				glassJudge2.setGlassJudge(glass2Grade);
				glassJudge2.setPanelGrades(glass2SubProductGrades);
				glassJudge1.setProcessFlowName(processFlowName);
				glassJudge1.setProcessOperationName(processoperationName);
				glassJudge2.setLastEventComment(eventInfo.getEventComment());
				glassJudge2.setLastEventName(eventInfo.getEventName());
				glassJudge2.setLastEventTime(eventInfo.getEventTime());
				glassJudge2.setLastEventUser(eventInfo.getEventUser());
				if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP")&&glass2GradesList.contains("C"))
				{
					glassJudge2.setNGFlag("Y");
				}
				
				ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassJudge2);
			}
		}
		else if(StringUtil.equals(factoryName, "OLED"))
		{
			int OLEDJudgeData = 80;
			
			if(cntS + cntC > OLEDJudgeData)
			{
				productGrade = "N";
			}
			else if(cntP > 0 && cntS + cntC <= OLEDJudgeData)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
		}
		else
		{
			int TPJudgeData = 83;
			
			if(cntS > TPJudgeData)
			{
				productGrade = "N";
			}
			else if(cntP > 0 && cntS + cntC <= TPJudgeData)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
		}
		
		return productGrade;
	}
	
	public String getProductGradeByRSRuleForRepair(String subProductGrades, String factoryName, int cntS, int cntC, int cntN, int cntG, int cntP, EventInfo eventInfo, String gradesList, Product productData) throws CustomException
	{
		String productGrade = ""; 
		
		if(StringUtil.equals(productData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Sheet))
		{
			ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(factoryName, productData.getProcessOperationName(),"00001");
			List<ReserveRepairPolicy> reserveRepairPolicyList=new ArrayList<ReserveRepairPolicy>();
			try
			{
				reserveRepairPolicyList=ExtendedObjectProxy.getReserveRepairPolicyService().select("FACTORYNAME=? AND PRODUCTSPECNAME=? AND PROCESSFLOWNAME=? AND ACTUALREPAIROPERATION=?",
						new Object[]{factoryName,productData.getProductSpecName(),productData.getProcessFlowName(),productData.getProcessOperationName()});	
			}
			catch(greenFrameDBErrorSignal n)
			{
				log.info("not exist reserveRepairPolicy");
			}
 
			eventInfo.setEventName("GlassJudge");
			boolean lastRepairOper=false;
			if(reserveRepairPolicyList!=null&&reserveRepairPolicyList.size()>0
					&&(StringUtils.equals(reserveRepairPolicyList.get(0).getLastRepairOperation(),productData.getProcessOperationName())
					||StringUtils.isEmpty(reserveRepairPolicyList.get(0).getLastRepairOperation())))
			{
				lastRepairOper=true;
			}
			
			int arrayJudgeData = 96;
			
			int cntGlassP = 0;
			int cntGlassG = 0;
			int cntGlassN = 0;
			
			//Glass1
			cntS = 0;
			cntC = 0;
			cntN = 0;
			cntG = 0;
			cntP = 0;
			String glass1Grade = "";
			String glass1SubProductGrades = subProductGrades.substring(0, subProductGrades.length() / 2);
			String glass1GradesList=gradesList.substring(0, gradesList.length() / 2);
			
			for(int i = 0 ; i < glass1SubProductGrades.length() ; i++)
			{
				String panelGrade = String.valueOf(subProductGrades.charAt(i));
				
				if(StringUtil.equals("S", panelGrade))
				{
					cntS++;
				}
				else if(StringUtil.equals("C", panelGrade))
				{
					cntC++;
				}
				else if(StringUtil.equals("N", panelGrade))
				{
					cntN++;
				}
				else if(StringUtil.equals("G", panelGrade))
				{
					cntG++;
				}
				else
				{
					cntP++;
				}
			}
			
		    if(cntN + cntS + cntC >= arrayJudgeData||(glass1GradesList.contains("C")&&lastRepairOper))
			{
				glass1Grade = "N";
				cntGlassN++;
			}
			else if(cntP > 0 && cntN + cntS + cntC < arrayJudgeData)
			{
				glass1Grade = "P";
				cntGlassP++;
			}
			else
			{
				glass1Grade = "G";
				cntGlassG++;
			}		    
			
			//Glass2
			cntS = 0;
			cntC = 0;
			cntN = 0;
			cntG = 0;
			cntP = 0;
			String glass2Grade = "";
			String glass2SubProductGrades = subProductGrades.substring(subProductGrades.length()/2, subProductGrades.length());
			String glass2GradesList=gradesList.substring(gradesList.length()/2, gradesList.length());
			
			for(int i = subProductGrades.length()/2 ; i < subProductGrades.length() ; i++)
			{
				String panelGrade = String.valueOf(subProductGrades.charAt(i));
				
				if(StringUtil.equals("S", panelGrade))
				{
					cntS++;
				}
				else if(StringUtil.equals("C", panelGrade))
				{
					cntC++;
				}
				else if(StringUtil.equals("N", panelGrade))
				{
					cntN++;
				}
				else if(StringUtil.equals("G", panelGrade))
				{
					cntG++;
				}
				else
				{
					cntP++;
				}
			}
			
		    if(cntN + cntS + cntC >= arrayJudgeData||(glass2GradesList.contains("C")&&lastRepairOper))
			{
				glass2Grade = "N";
				cntGlassN++;
			}
			else if(cntP > 0 && cntN + cntS + cntC < arrayJudgeData)
			{
				glass2Grade = "P";
				cntGlassP++;
			}
			else
			{
				glass2Grade = "G";
				cntGlassG++;
			}
			
			if(cntGlassN == 2)
			{
				productGrade = "N";
			}
			else if(cntGlassP > 0 && cntGlassN <= 1)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
			
			//Update CT_GlassJudge
			GlassJudge glassJudge1 = new GlassJudge();
			
			try
			{
				glassJudge1 = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false,  new Object[] {productData.getKey().getProductName() + "1"});
			}
			catch (Exception e)
			{
				glassJudge1 = null;
			}
			
			if(glassJudge1 != null)
			{
				glassJudge1.setGlassJudge(glass1Grade);
				glassJudge1.setPanelGrades(glass1SubProductGrades);
				glassJudge1.setProcessFlowName(productData.getProcessFlowName());
				glassJudge1.setProcessOperationName(productData.getProcessOperationName());
				glassJudge1.setLastEventComment(eventInfo.getEventComment());
				glassJudge1.setLastEventName(eventInfo.getEventName());
				glassJudge1.setLastEventTime(eventInfo.getEventTime());
				glassJudge1.setLastEventUser(eventInfo.getEventUser());
				if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP")&&(glass1GradesList.contains("C")&&lastRepairOper))
				{
					glassJudge1.setNGFlag("Y");
				}
				
				ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassJudge1);
			}
			
			GlassJudge glassJudge2 = new GlassJudge();
			
			try
			{
				glassJudge2 = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false,  new Object[] {productData.getKey().getProductName() + "2"});
			}
			catch (Exception e)
			{
				glassJudge2 = null;
			}
			
			if(glassJudge2 != null)
			{
				glassJudge2.setGlassJudge(glass2Grade);
				glassJudge2.setPanelGrades(glass2SubProductGrades);
				glassJudge1.setProcessFlowName(productData.getProcessFlowName());
				glassJudge1.setProcessOperationName(productData.getProcessOperationName());
				glassJudge2.setLastEventComment(eventInfo.getEventComment());
				glassJudge2.setLastEventName(eventInfo.getEventName());
				glassJudge2.setLastEventTime(eventInfo.getEventTime());
				glassJudge2.setLastEventUser(eventInfo.getEventUser());
				if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP")&&(glass2GradesList.contains("C")&&lastRepairOper))
				{
					glassJudge2.setNGFlag("Y");
				}
				
				ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassJudge2);
			}
			
		    if(reserveRepairPolicyList!=null&&reserveRepairPolicyList.size()>0&&StringUtils.isNotEmpty(reserveRepairPolicyList.get(0).getLastRepairOperation())
		    		&&!StringUtils.equals(reserveRepairPolicyList.get(0).getLastRepairOperation(), productData.getProcessOperationName()))
		    {
			    for(int i=0;i<glass1GradesList.length();i++)
			    {
			    	String panelJudge = String.valueOf(glass1GradesList.charAt(i));
			    	if(StringUtils.equals(panelJudge, "C"))
			    	{
			    		try
			    		{
			    			List<ReserveRepairPanelInfo> reserveRepairPanelInfoList=ExtendedObjectProxy.getReserveRepairPanelInfoService().select(
			    					"SHEET_ID=:SHEET_ID AND PANELNUM=:PANELNUM AND REPAIROPERATION=:REPAIROPERATION",new Object[]{productData.getKey().getProductName(),i+1,productData.getProcessOperationName()} );
			    			
			    			for(ReserveRepairPanelInfo reserveRepairPanelInfo:reserveRepairPanelInfoList)
			    			{
			    				ReserveRepairPanelInfo newReserveRepairInfo=new ReserveRepairPanelInfo();
			    				newReserveRepairInfo=(ReserveRepairPanelInfo)ObjectUtil.copyTo(reserveRepairPanelInfo);
			    				newReserveRepairInfo.setRepairOperation(reserveRepairPolicyList.get(0).getLastRepairOperation());
			    				newReserveRepairInfo.setRsOperationForRP(reserveRepairPolicyList.get(0).getLastRSOperForRP());
			    				newReserveRepairInfo.setTimekey(TimeUtils.getCurrentEventTimeKey());
			    				ExtendedObjectProxy.getReserveRepairPanelInfoService().create(eventInfo, newReserveRepairInfo);
			    			}
			    		}
			    		catch(greenFrameDBErrorSignal n)
			    		{
			    			log.info("not found reserveRepairPanelInfo For gradeC");
			    		}
			    	}
			    }
		    }
		}
		else if(StringUtil.equals(factoryName, "OLED"))
		{
			int OLEDJudgeData = 80;
			
			if(cntS + cntC > OLEDJudgeData)
			{
				productGrade = "N";
			}
			else if(cntP > 0 && cntS + cntC <= OLEDJudgeData)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
		}
		else
		{
			int TPJudgeData = 83;
			
			if(cntS > TPJudgeData)
			{
				productGrade = "N";
			}
			else if(cntP > 0 && cntS + cntC <= TPJudgeData)
			{
				productGrade = "P";
			}
			else
			{
				productGrade = "G";
			}
		}
		
		return productGrade;
	}

	public ProcessFlow getMainProcessFlowData(Lot lotData) throws CustomException 
	{
		String nodeStack = lotData.getNodeStack();
		String mainNodeStack = "";

		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ProcessFlow mainFlowData = null;

		if (!StringUtils.equals(flowData.getProcessFlowType(), "MQC")
				&& !StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle") ) 
		{
			if (nodeStack.lastIndexOf(".") > -1) 
			{
				String[] nodeStackArray = StringUtils.split(nodeStack, ".");
				mainNodeStack = nodeStackArray[0];
			} 
			else 
			{
				mainNodeStack = nodeStack;
			}

			Node mainNodeData = ProcessFlowServiceProxy.getNodeService().getNode(mainNodeStack);
			mainFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(mainNodeData);
		} 
		else 
		{
			mainFlowData = flowData;
		}
		return mainFlowData;
	}
	
	public List<String> makeSubProductGradeListForIJPRepair(String subProductGradeMES, String subProductGradeEQP, String productType, String factoryName, String productName, EventInfo eventInfo, String processOperationName, String processFlowName) throws CustomException
	{
		String subProductGrades = "";
		String gradesList="";
		List <String> result = new ArrayList<String>();
		
		int cntS = 0;
		int cntC = 0;
		int cntN = 0;
		int cntG = 0;
		int cntP = 0;
		
		for(int i = 0 ; i < subProductGradeMES.length() ; i++)
		{
			String panelGrade = "";
			String panelGradeMES = String.valueOf(subProductGradeMES.charAt(i));
			String panelGradeEQP = String.valueOf(subProductGradeEQP.charAt(i));
			
			if(StringUtil.equals("S", panelGradeMES) || StringUtil.equals("S", panelGradeEQP))
			{
				panelGrade = "S";
				cntS++;
			}
			else if(StringUtil.equals("C", panelGradeMES) || StringUtil.equals("C", panelGradeEQP))
			{
				panelGrade = "C";
				cntC++;
			}
			else if(StringUtil.equals("N", panelGradeMES) || StringUtil.equals("N", panelGradeEQP))
			{
				panelGrade = "N";
				cntN++;
			}
			else if(StringUtil.equals("G", panelGradeMES) || StringUtil.equals("G", panelGradeEQP))
			{
				panelGrade = "G";
				cntG++;
			}
			else //P Judge
			{
				//P change to S
				panelGrade = "S";
				cntS++;
			}
			
			subProductGrades += panelGrade;
		}
		
		result.add(subProductGrades); //result(0) subProductGrades
		result.add(getProductGradeByRSRule(subProductGrades, factoryName, productType, cntS, cntC, cntN, cntG, cntP, productName, eventInfo, processOperationName, processFlowName,gradesList));//result(1) ProductGrade
		
		return result;
	}
}