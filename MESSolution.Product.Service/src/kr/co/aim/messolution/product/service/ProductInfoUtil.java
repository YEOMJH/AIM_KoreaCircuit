package kr.co.aim.messolution.product.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotInfo;
import kr.co.aim.greentrack.product.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.info.CreateRawInfo;
import kr.co.aim.greentrack.product.management.info.CreateWithLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.MakeAllocatedInfo;
import kr.co.aim.greentrack.product.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.product.management.info.MakeConsumedInfo;
import kr.co.aim.greentrack.product.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.product.management.info.MakeInProductionInfo;
import kr.co.aim.greentrack.product.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.product.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.product.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.product.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.product.management.info.RecreateInfo;
import kr.co.aim.greentrack.product.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.SetAreaInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.UndoInfo;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ProductInfoUtil implements ApplicationContextAware
{
	
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(ProductInfoUtil.class); 

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	
	public List<ConsumedMaterial> getConsumedMaterial(List<ConsumedMaterial> consumedMaterials ) {
		List<ConsumedMaterial> consumedMaterial = new ArrayList<ConsumedMaterial>();
		
		consumedMaterial = consumedMaterials;
		
		return consumedMaterial;
	}

	
	public  AssignCarrierInfo assignCarrierInfo( Product productData, String carrierName, long position)
	{
		AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();		
		assignCarrierInfo.setCarrierName(carrierName);
		assignCarrierInfo.setPosition(position);
					
		return assignCarrierInfo;
	}

	
	public AssignLotInfo assignLotInfo( Product productData, String lotName )
	{
		AssignLotInfo assignLotInfo = new AssignLotInfo();		
		assignLotInfo.setLotName(lotName);
		
		return assignLotInfo;
	}

	
	public AssignLotAndCarrierInfo assignLotAndCarrierInfo( Product productData, String carrierName
																   , String gradeFlag, String lotName, long position, 
																   String productProcessState)
	{
		AssignLotAndCarrierInfo assignLotAndCarrierInfo = new AssignLotAndCarrierInfo();		
		
		assignLotAndCarrierInfo.setCarrierName(carrierName);
		assignLotAndCarrierInfo.setGradeFlag(gradeFlag);
		assignLotAndCarrierInfo.setLotName(lotName);
		assignLotAndCarrierInfo.setPosition(position);
		assignLotAndCarrierInfo.setProductProcessState(productProcessState);
	
		return assignLotAndCarrierInfo;
	}

	
	public AssignProcessGroupInfo assignProcessGroupInfo( Product productData, String processGroupName )
	{
		AssignProcessGroupInfo assignProcessGroupInfo = new AssignProcessGroupInfo();		
		assignProcessGroupInfo.setProcessGroupName(processGroupName);
		
		return assignProcessGroupInfo;
	}

	
	public AssignTransportGroupInfo assignTransportGroupInfo( Product productData, String transportGroupName )
	{
		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();	
		assignTransportGroupInfo.setTransportGroupName(transportGroupName);

		
		return assignTransportGroupInfo;
	}

	
	public ChangeGradeInfo changeGradeInfo( Product productData, long position, 
													String productGrade, String productProcessState, 
													String subProductGrades1, String subProductGrades2,
													double subProductQuantity1, double subProductQuantity2)
	{
		ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();	
		changeGradeInfo.setPosition(position);
		changeGradeInfo.setProductGrade(productGrade);
		changeGradeInfo.setProductProcessState(productProcessState);
		changeGradeInfo.setSubProductGrades1(subProductGrades1);
		changeGradeInfo.setSubProductGrades2(subProductGrades2);
		changeGradeInfo.setSubProductQuantity1(subProductQuantity1);
		changeGradeInfo.setSubProductQuantity2(subProductQuantity2);
		
		return changeGradeInfo;
	}
	
	
	public List<ProductPGS> getProductPGSSequence(Element productEList)
	 	throws CustomException
	 {
		 try
		 {
			 List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
			 
			 for (Iterator prodIterator = productEList.getChildren().iterator(); prodIterator.hasNext(); )
			 {
				 Element productE = (Element) prodIterator.next();
				 
				 String productName = SMessageUtil.getChildText(productEList, "PRODUCTNAME", true);
				 
				 //comprehensive for EIS
				 String productGrade = "";
				 if(productE.getChild("PRODUCTJUDGE") != null)
					 productGrade = productE.getChildText("PRODUCTJUDGE");
				 else
					 productGrade = XmlUtil.getNodeText(productE, "PRODUCTGRADE");
				 
				 long position = Long.parseLong(XmlUtil.getNodeText(productE, "POSITION"));
				 
				 ProductPGS productPGS = new ProductPGS();
				 productPGS.setProductName(productName);
				 productPGS.setProductGrade(productGrade);
				 productPGS.setPosition(position);
				 
				 productPGSSequence.add(productPGS);
			 }
			 
			 return productPGSSequence;
		 }
		 catch (NotFoundSignal ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
		 catch (FrameworkErrorSignal fe)
		 {
			 throw new CustomException("SYS-0001", fe.getMessage());
		 }
		 catch (Exception ex)
		 {
			 throw new CustomException("SYS-0001", ex.getMessage());
		 }
	 }

	
	public ChangeSpecInfo changeSpecInfo( Product productData, String areaName, 
												String carrierName, Timestamp dueDate, String factoryName, 
												String gradeFlag, String lotName, String nodeStack, long position, long priority, 
												String processFlowName, String processFlowVersion, String processOperationName, 
												String processOperationVersion, String productHoldState, String productionType,String productProcessState,
												String productRequestName, String productSpec2Name, String productSpec2Version, 
												String productSpecName, String productSpecVersion, String productState, 
												double subProductUnitQuantity1, double subProductUnitQuantity2,Map<String,String> productUdfs)
	{
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();	
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setCarrierName(carrierName);
		changeSpecInfo.setDueDate(dueDate);
		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setGradeFlag(gradeFlag);
		changeSpecInfo.setLotName(lotName);
		changeSpecInfo.setNodeStack(nodeStack);
		changeSpecInfo.setPosition(position);
		changeSpecInfo.setPriority(priority);
		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion(processFlowVersion);
		changeSpecInfo.setProcessOperationName(processOperationName);
		changeSpecInfo.setProcessOperationVersion(processOperationVersion);
		changeSpecInfo.setProductHoldState(productHoldState);
		changeSpecInfo.setProductionType(productionType);
		changeSpecInfo.setProductProcessState(productProcessState);
		changeSpecInfo.setProductRequestName(productRequestName);
		changeSpecInfo.setProductSpec2Name(productSpec2Name);
		changeSpecInfo.setProductSpec2Version(productSpec2Version);
		changeSpecInfo.setProductSpecName(productSpecName);
		changeSpecInfo.setProductSpecVersion(productSpecVersion);
		changeSpecInfo.setProductState(productState);
		changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		
		changeSpecInfo.setUdfs(productUdfs);
		
		return changeSpecInfo;
	}

	
	public CreateInfo createInfo( Product productData, Timestamp dueDate, String factoryName,
								String materialLocationName, String nodeStack, String originalProductName,  long priority, 
								String processFlowName, String processFlowVersion, String processGroupName, String processOperationName,
								String processOperationVersion, String productGrade, String productionType, 
								String productName, String productRequestName, String productSpec2Name,String productSpec2Version,
								String productSpecName, String productSpecVersion, String productType, 
								String sourceProductName, String subProductGrades1, String subProductGrades2, 
								double subProductQuantity1, double subProductQuantity2, String subProductType, 
								double subProductUnitQuantity1, double subProductUnitQuantity2, String transportGroupName )
	{
		CreateInfo createInfo = new CreateInfo();	
		createInfo.setDueDate(dueDate);
		createInfo.setFactoryName(factoryName);
		createInfo.setMaterialLocationName(materialLocationName);
		createInfo.setNodeStack(nodeStack);
		createInfo.setOriginalProductName(originalProductName);
		createInfo.setPriority(priority);
		createInfo.setProcessFlowName(processFlowName);
		createInfo.setProcessFlowVersion(processFlowVersion);
		createInfo.setProcessGroupName(processGroupName);
		createInfo.setProcessOperationName(processOperationName);
		createInfo.setProcessOperationVersion(processOperationVersion);
		createInfo.setProductGrade(productGrade);
		createInfo.setProductionType(productionType);
		createInfo.setProductName(productName);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setProductSpec2Name(productSpec2Name);
		createInfo.setProductSpec2Version(productSpec2Version);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		createInfo.setProductType(productType);
		createInfo.setSourceProductName(sourceProductName);
		createInfo.setSubProductGrades1(subProductGrades1);
		createInfo.setSubProductGrades2(subProductGrades2);
		createInfo.setSubProductQuantity1(subProductQuantity1);
		createInfo.setSubProductQuantity2(subProductQuantity2);
		createInfo.setSubProductType(subProductType);
		createInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createInfo.setTransportGroupName(transportGroupName);
		
		return createInfo;
	}

	
	public CreateRawInfo createRawInfo( Product productData, String areaName, String carrierName, 
			Timestamp dueDate, String factoryName, Timestamp lastIdleTime, 
			String lastIdleUser, Timestamp lastProcessingTime, 
			String lastProcessingUser, String lotName, String machineName,
			String machineRecipeName, String materialLocationName, String nodeStack, 
			String originalProductName, long position, long priority,String processFlowName, 
			String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productGrade,String productHoldState,
			String productionType, String productName, String productProcessState,
			String productRequestName, String productSpec2Name,String productSpec2Version,String productSpecName,
			String productSpecVersion, String productState, String productType,
			long reworkCount, String reworkFlag,String reworkNodeId, 
			String sourceProductName, String subProductGrades1, String subProductGrades2,
			double subProductQuantity1, double subProductQuantity2,
			String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, 
			String transportGroupName	)
	{
		CreateRawInfo createRawInfo = new CreateRawInfo();	
		createRawInfo.setAreaName(areaName);
		createRawInfo.setCarrierName(carrierName);
		createRawInfo.setDueDate(dueDate);
		createRawInfo.setFactoryName(factoryName);
		createRawInfo.setLastIdleTime(lastIdleTime);
		createRawInfo.setLastIdleUser(lastIdleUser);
		createRawInfo.setLastProcessingTime(lastProcessingTime);
		createRawInfo.setLastProcessingUser(lastProcessingUser);
		createRawInfo.setLotName(lotName);
		createRawInfo.setMachineName(machineName);
		createRawInfo.setMachineRecipeName(machineRecipeName);
		createRawInfo.setMaterialLocationName(materialLocationName);
		createRawInfo.setNodeStack(nodeStack);
		createRawInfo.setOriginalProductName(originalProductName);
		createRawInfo.setPosition(position);
		createRawInfo.setPriority(priority);
		createRawInfo.setProcessFlowName(processFlowName);
		createRawInfo.setProcessFlowVersion(processFlowVersion);
		createRawInfo.setProcessGroupName(processGroupName);
		createRawInfo.setProcessOperationName(processOperationName);
		createRawInfo.setProcessOperationVersion(processOperationVersion);
		createRawInfo.setProductGrade(productGrade);
		createRawInfo.setProductHoldState(productHoldState);
		createRawInfo.setProductionType(productionType);
		createRawInfo.setProductName(productName);
		createRawInfo.setProductProcessState(productProcessState);
		createRawInfo.setProductRequestName(productRequestName);
		createRawInfo.setProductSpec2Name(productSpec2Name);
		createRawInfo.setProductSpec2Version(productSpec2Version);
		createRawInfo.setProductSpecName(productSpecName);
		createRawInfo.setProductSpecVersion(productSpecVersion);
		createRawInfo.setProductState(productState);
		createRawInfo.setProductType(productType);
		createRawInfo.setReworkCount(reworkCount);
		createRawInfo.setReworkFlag(reworkFlag);
		createRawInfo.setReworkNodeId(reworkNodeId);
		createRawInfo.setSourceProductName(sourceProductName);
		createRawInfo.setSubProductGrades1(subProductGrades1);
		createRawInfo.setSubProductGrades2(subProductGrades2);
		createRawInfo.setSubProductQuantity1(subProductQuantity1);
		createRawInfo.setSubProductQuantity2(subProductQuantity2);
		createRawInfo.setSubProductType(subProductType);
		createRawInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createRawInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createRawInfo.setTransportGroupName(transportGroupName);
	
		return createRawInfo;
	}

	
	public CreateWithLotInfo createWithLotInfo( Product productData, 
			String areaName, String carrierName, Timestamp dueDate, 
			String factoryName, Timestamp lastIdleTime, String lastIdleUser, 
			Timestamp lastProcessingTime, String lastProcessingUser,
			String lotName, String materialLocationName,String nodeStack, 
			String originalProductName, long position, long priority,String processFlowName,
			String processFlowVersion, String processGroupName, String processOperationName,
			String processOperationVersion, String productGrade,String productHoldState, 
			String productionType, String productName, String productProcessState,
			String productRequestName, String productSpec2Name,String productSpec2Version,
			String productSpecName, String productSpecVersion, String productState, 
			String productType, long reworkCount, String reworkFlag,
			String reworkNodeId, String sourceProductName, String subProductGrades1,
			String subProductGrades2, double subProductQuantity1, double subProductQuantity2,
			String subProductType, double subProductUnitQuantity1, double subProductUnitQuantity2, 
			String transportGroupName	)
	{
		CreateWithLotInfo createWithLotInfo = new CreateWithLotInfo();	
		
		createWithLotInfo.setAreaName(areaName);
		createWithLotInfo.setCarrierName(carrierName);
		createWithLotInfo.setDueDate(dueDate);
		createWithLotInfo.setFactoryName(factoryName);
		createWithLotInfo.setLastIdleTime(lastIdleTime);
		createWithLotInfo.setLastIdleUser(lastIdleUser);
		createWithLotInfo.setLastProcessingTime(lastProcessingTime);
		createWithLotInfo.setLastProcessingUser(lastProcessingUser);
		createWithLotInfo.setLotName(lotName);
		createWithLotInfo.setMaterialLocationName(materialLocationName);
		createWithLotInfo.setNodeStack(nodeStack);
		createWithLotInfo.setOriginalProductName(originalProductName);
		createWithLotInfo.setPosition(position);
		createWithLotInfo.setPriority(priority);
		createWithLotInfo.setProcessFlowName(processFlowName);
		createWithLotInfo.setProcessFlowVersion(processFlowVersion);
		createWithLotInfo.setProcessGroupName(processGroupName);
		createWithLotInfo.setProcessOperationName(processOperationName);
		createWithLotInfo.setProcessOperationVersion(processOperationVersion);
		createWithLotInfo.setProductGrade(productGrade);
		createWithLotInfo.setProductHoldState(productHoldState);
		createWithLotInfo.setProductionType(productionType);
		createWithLotInfo.setProductName(productName);
		createWithLotInfo.setProductProcessState(productProcessState);
		createWithLotInfo.setProductRequestName(productRequestName);
		createWithLotInfo.setProductSpec2Name(productSpec2Name);
		createWithLotInfo.setProductSpec2Version(productSpec2Version);
		createWithLotInfo.setProductSpecName(productSpecName);
		createWithLotInfo.setProductSpecVersion(productSpecVersion);
		createWithLotInfo.setProductState(productState);
		createWithLotInfo.setProductType(productType);
		createWithLotInfo.setReworkCount(reworkCount);
		createWithLotInfo.setReworkFlag(reworkFlag);
		createWithLotInfo.setReworkNodeId(reworkNodeId);
		createWithLotInfo.setSourceProductName(sourceProductName);
		createWithLotInfo.setSubProductGrades1(subProductGrades1);
		createWithLotInfo.setSubProductGrades2(subProductGrades2);
		createWithLotInfo.setSubProductQuantity1(subProductQuantity1);
		createWithLotInfo.setSubProductQuantity2(subProductQuantity2);
		createWithLotInfo.setSubProductType(subProductType);
		createWithLotInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		createWithLotInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		createWithLotInfo.setTransportGroupName(transportGroupName);
	
		return createWithLotInfo;
	}

	
	public ConsumeMaterialsInfo consumeMaterialsInfo( Product productData, 
			String productGrade, String subProductGrades1, String subProductGrades2, 
			 double subProductQuantity1, double subProductQuantity2 ,String factoryName, 
			 List<ConsumedMaterial> cms)
	{
		ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();	
		consumeMaterialsInfo.setProductGrade(productGrade);
		consumeMaterialsInfo.setSubProductGrades1(subProductGrades1);
		consumeMaterialsInfo.setSubProductGrades2(subProductGrades2);
		consumeMaterialsInfo.setSubProductQuantity1(subProductQuantity1);
		consumeMaterialsInfo.setSubProductQuantity2(subProductQuantity2);
		
		consumeMaterialsInfo.setConsumedMaterialSequence(cms);
	
		return consumeMaterialsInfo;
	}

	
	public DeassignCarrierInfo deassignCarrierInfo( Product productData )
	{
		DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();	
			
		return deassignCarrierInfo;
	}

	
	public DeassignLotInfo deassignLotInfo( Product productData )
	{
		DeassignLotInfo deassignLotInfo = new DeassignLotInfo();	

		return deassignLotInfo;
	}

	
	public DeassignLotAndCarrierInfo deassignLotAndCarrierInfo( Product productData )
	{
		DeassignLotAndCarrierInfo deassignLotAndCarrierInfo = new DeassignLotAndCarrierInfo();	

		return deassignLotAndCarrierInfo;
	}

	
	public DeassignProcessGroupInfo deassignProcessGroupInfo( Product productData )
	{
		DeassignProcessGroupInfo deassignProcessGroupInfo = new DeassignProcessGroupInfo();	
		
		return deassignProcessGroupInfo;
	}

	
	public DeassignTransportGroupInfo deassignTransportGroupInfo( Product productData )
	{
		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();	
		
		return deassignTransportGroupInfo;
	}

	
	public MakeAllocatedInfo makeAllocatedInfo( Product productData )
	{
		MakeAllocatedInfo makeAllocatedInfo = new MakeAllocatedInfo();	
		
		return makeAllocatedInfo;
	}

	
	public MakeCompletedInfo makeCompletedInfo( Product productData )
	{
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();	
		
		return makeCompletedInfo;
	}

	
	public MakeConsumedInfo makeConsumedInfo( Product productData, String consumerLotName, 
													String consumerProductName , String consumerTimeKey)
	{
		MakeConsumedInfo makeConsumedInfo = new MakeConsumedInfo();	
		
		makeConsumedInfo.setConsumerLotName(consumerLotName);
		makeConsumedInfo.setConsumerProductName(consumerProductName);
		makeConsumedInfo.setConsumerTimeKey(consumerTimeKey);
		
		return makeConsumedInfo;
	}

	
	public MakeIdleInfo makeIdleInfo( Product productData, String consumerLotName, String areaName,
			String branchEndNodeId , String carrierName, List<ConsumedMaterial> cms,
			String completeFlag, String machineName, String machineRecipeName,
			String nodeStack, long position, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion,
			String productGrade, String reworkFlag, String reworkNodeId,
			String subProductGrades1, String subProductGrades2, double subProductQuantity1, double subProductQuantity2)
	{
		MakeIdleInfo makeIdleInfo = new MakeIdleInfo();	

		makeIdleInfo.setAreaName(areaName);
		makeIdleInfo.setBranchEndNodeId(branchEndNodeId);
		makeIdleInfo.setCarrierName(carrierName);
		makeIdleInfo.setConsumedMaterialSequence(cms);
		makeIdleInfo.setCompleteFlag(completeFlag);
		makeIdleInfo.setMachineName(machineName);
		makeIdleInfo.setMachineRecipeName(machineRecipeName);
		makeIdleInfo.setNodeStack(nodeStack);
		makeIdleInfo.setPosition(position);
		makeIdleInfo.setProcessFlowName(processFlowName);
		makeIdleInfo.setProcessFlowVersion(processFlowVersion);
		makeIdleInfo.setProcessOperationName(processOperationName);
		makeIdleInfo.setProcessOperationVersion(processOperationVersion);
		makeIdleInfo.setProductGrade(productGrade);
		makeIdleInfo.setReworkFlag(reworkFlag);
		makeIdleInfo.setReworkNodeId(reworkNodeId);
		makeIdleInfo.setSubProductGrades1(subProductGrades1);
		makeIdleInfo.setSubProductGrades2(subProductGrades2);
		makeIdleInfo.setSubProductQuantity1(subProductQuantity1);
		makeIdleInfo.setSubProductQuantity2(subProductQuantity2);
		
		return makeIdleInfo;
	}

	
	public MakeInProductionInfo makeInProductionInfo( Product productData, String areaName, String carrierName,
															Timestamp dueDate , String nodeStack, long priority,
															String processFlowName, String processFlowVersion, String processOperationName,
															String processOperationVersion )
	{
		MakeInProductionInfo makeInProductionInfo = new MakeInProductionInfo();	
		makeInProductionInfo.setAreaName(areaName);
		makeInProductionInfo.setCarrierName(carrierName);
		makeInProductionInfo.setDueDate(dueDate);
		makeInProductionInfo.setNodeStack(nodeStack);
		makeInProductionInfo.setPriority(priority);
		makeInProductionInfo.setProcessFlowName(processFlowName);
		makeInProductionInfo.setProcessFlowVersion(processFlowVersion);
		makeInProductionInfo.setProcessOperationName(processOperationName);
		makeInProductionInfo.setProcessOperationVersion(processOperationVersion);
		
		return makeInProductionInfo;
	}

	
	public MakeInReworkInfo makeInReworkInfo( Product productData, String areaName, String processFlowName,
													String	processFlowVersion, String processOperationName,
													String	processOperationVersion, String	nodeStack,
													String	reworkNodeId)
	{
		MakeInReworkInfo makeInReworkInfo = new MakeInReworkInfo();
		makeInReworkInfo.setAreaName(areaName);
		makeInReworkInfo.setNodeStack(nodeStack);
		makeInReworkInfo.setProcessFlowName(processFlowName);
		makeInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeInReworkInfo.setProcessOperationName(processOperationName);
		makeInReworkInfo.setProcessOperationVersion(processOperationVersion);
		makeInReworkInfo.setReworkNodeId(reworkNodeId);
		makeInReworkInfo.getUdfs().put("REWORKFLAG", "Y");
		
		return makeInReworkInfo;
	}

	
	public MakeNotInReworkInfo makeNotInReworkInfo( Product productData, String areaName, String nodeStack,
														String processFlowName , String processFlowVersion, 
														String processOperationName, String processOperationVersion )
	{
		MakeNotInReworkInfo makeNotInReworkInfo = new MakeNotInReworkInfo();	
		makeNotInReworkInfo.setAreaName(areaName);
		makeNotInReworkInfo.setNodeStack(nodeStack);
		makeNotInReworkInfo.setProcessFlowName(processFlowName);
		makeNotInReworkInfo.setProcessFlowVersion(processFlowVersion);
		makeNotInReworkInfo.setProcessOperationName(processOperationName);
		makeNotInReworkInfo.setProcessOperationVersion(processOperationVersion);

		return makeNotInReworkInfo;
	}


	public MakeNotOnHoldInfo makeNotOnHoldInfo( Product productData, String areaName, String nodeStack,
			String processFlowName , String processFlowVersion, 
			String processOperationName, String processOperationVersion )
	{
		MakeNotOnHoldInfo makeNotInReworkInfo = new MakeNotOnHoldInfo();	
		
		Map<String,String> productUdfs = productData.getUdfs();
		makeNotInReworkInfo.setUdfs(productUdfs);
		
		return makeNotInReworkInfo;
	}

	
	public MakeOnHoldInfo makeOnHoldInfo( Product productData, String areaName, String nodeStack,
			String processFlowName , String processFlowVersion, 
			String processOperationName, String processOperationVersion )
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();	
		
		return makeOnHoldInfo;
	}

	
	public MakeProcessingInfo makeProcessingInfo( Product productData, String areaName, 
														List<ConsumedMaterial> cms, String machineName , 
														String machineRecipeName ) 
			
	{
		MakeProcessingInfo makeProcessingInfo = new MakeProcessingInfo();	
		makeProcessingInfo.setAreaName(areaName);
		makeProcessingInfo.setConsumedMaterialSequence(cms);
		makeProcessingInfo.setMachineName(machineName);
		makeProcessingInfo.setMachineRecipeName(machineRecipeName);
		
		return makeProcessingInfo;
	}

	
	public MakeReceivedInfo makeReceivedInfo( Product productData, String areaName, 
								String nodeStack, String processFlowName , String processFlowVersion, 
								String processOperationName,String processOperationVersion,
								String productionType, String productRequestName,String productSpec2Name,
								String productSpec2Version, String productSpecName,String productSpecVersion,
								String productType, String subProductType) 

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
		makeReceivedInfo.setSubProductType(subProductType);
		
		return makeReceivedInfo;
	}

	
	public MakeScrappedInfo makeScrappedInfo( Product productData, double productQuantity, 
													List<ProductU> productUSequence)
	{
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
		makeScrappedInfo.setProductQuantity(productQuantity);
		makeScrappedInfo.setProductUSequence(productUSequence);
		
		return makeScrappedInfo;
	}

	
	public MakeShippedInfo makeShippedInfo( Product productData, String areaName, 
												   String directShipFlag, String factoryName)
	{
		MakeShippedInfo makeShippedInfo = new MakeShippedInfo();
		makeShippedInfo.setAreaName(areaName);
		makeShippedInfo.setDirectShipFlag(directShipFlag);
		makeShippedInfo.setFactoryName(factoryName);
		
		return makeShippedInfo;
	}

	
	public MakeTravelingInfo makeTravelingInfo( Product productData, String areaName )
	{
		MakeTravelingInfo makeTravelingInfo = new MakeTravelingInfo();
		makeTravelingInfo.setAreaName(areaName);
		
		return makeTravelingInfo;
	}

	public MakeUnScrappedInfo makeUnScrappedInfo( Product productData, String lotProcessState, 
														 double productQuantity,List<ProductU> productUSequence )
	{
		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo();
		makeUnScrappedInfo.setLotProcessState(lotProcessState);
		makeUnScrappedInfo.setProductQuantity(productQuantity);
		makeUnScrappedInfo.setProductUSequence(productUSequence);
		
		return makeUnScrappedInfo;
	}

	
	public MakeUnShippedInfo makeUnShippedInfo( Product productData, String areaName )
	{
		MakeUnShippedInfo makeUnShippedInfo = new MakeUnShippedInfo();
		makeUnShippedInfo.setAreaName(areaName);
		
		return makeUnShippedInfo;
	}

	
	public RecreateInfo recreateInfo( Product productData, String areaName, String carrierName,
							Timestamp dueDate, String lotName,String newProductName, String nodeStack,
							long position, long priority,String processFlowName, 
							String processFlowVersion, String processOperationName, 
							String processOperationVersion,String productGrade, 
							String productionType, String productRequestName, String productSpec2Name,
							String productSpec2Version, String productSpecName,
							String productSpecVersion, String productType,String subProductGrades1,
							String subProductGrades2, double subProductQuantity1,
							double subProductQuantity2,String subProductType, 
							double subProductUnitQuantity1, double subProductUnitQuantity2)
	{
		RecreateInfo recreateInfo = new RecreateInfo();
		recreateInfo.setAreaName(areaName);
		recreateInfo.setCarrierName(carrierName);
		recreateInfo.setDueDate(dueDate);
		recreateInfo.setLotName(lotName);
		recreateInfo.setNewProductName(newProductName);
		recreateInfo.setNodeStack(nodeStack);
		recreateInfo.setPosition(position);
		recreateInfo.setPriority(priority);
		recreateInfo.setProcessFlowName(processFlowName);
		recreateInfo.setProcessFlowVersion(processFlowVersion);
		recreateInfo.setProcessOperationName(processOperationName);
		recreateInfo.setProcessOperationVersion(processOperationVersion);
		recreateInfo.setProductGrade(productGrade);
		recreateInfo.setProductionType(productionType);
		recreateInfo.setProductRequestName(productRequestName);
		recreateInfo.setProductSpec2Name(productSpec2Name);
		recreateInfo.setProductSpec2Version(productSpec2Version);
		recreateInfo.setProductSpecName(productSpecName);
		recreateInfo.setProductSpecVersion(productSpecVersion);
		recreateInfo.setProductType(productType);
		recreateInfo.setSubProductGrades1(subProductGrades1);
		recreateInfo.setSubProductGrades2(subProductGrades2);
		recreateInfo.setSubProductQuantity1(subProductQuantity1);
		recreateInfo.setSubProductQuantity2(subProductQuantity2);
		recreateInfo.setSubProductType(subProductType);
		recreateInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
		recreateInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);
		
		return recreateInfo;
	}

	
	public SeparateInfo separateInfo( Product productData, List<ProductPGQS> subProductPGQSSequence )
	{
		SeparateInfo separateInfo = new SeparateInfo();
		separateInfo.setSubProductPGQSSequence(subProductPGQSSequence);
		
		return separateInfo;
	}

	
	public SetAreaInfo setAreaInfo( Product productData, String areaName )
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);
		
		return setAreaInfo;
	}

	public SetEventInfo setEventInfo( Product productData, List<ProductU> productListUdfs )
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setProductUSequence(productListUdfs);
		
		return setEventInfo;
	}
	
	
	public UndoInfo undoInfo( Product productData, String carrierUndoFlag, String eventName , 
									 Timestamp eventTime, String eventTimeKey,
									 String eventUser, String  lastEventTimeKey )
	{
		UndoInfo undoInfo = new UndoInfo();
		
		undoInfo.setCarrierUndoFlag(carrierUndoFlag);
		undoInfo.setEventName( eventName );
		undoInfo.setEventTime( eventTime );
		undoInfo.setEventTimeKey( eventTimeKey );
		undoInfo.setEventUser( eventUser );
		undoInfo.setLastEventTimeKey( lastEventTimeKey );
		
		return undoInfo;
	}
	
	public SetMaterialLocationInfo setMaterialLocationInfo(Product productData ,String materialLocationName, Map<String, String> udfs)
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);
		setMaterialLocationInfo.setUdfs(udfs);
		
		return setMaterialLocationInfo;
	}

	
	public ProductU setProductUInfo(Product productData){
		ProductU productU = new ProductU(productData.getKey().getProductName());
		return productU;
	}
	
	
	public Product getProductByProductName(String productName)
	{
		ProductKey productKey = new ProductKey();
		productKey.setProductName(productName);
		
		Product productData = null;
		productData = ProductServiceProxy.getProductService().selectByKey(productKey);
		
		return productData;
	}
	
	public List<Map<String, Object>> getProcessingTimeKeyByProduct(String productName) throws CustomException
	{				
		String sql = "SELECT TO_CHAR(LASTPROCESSINGTIME,'yyyyMMddHHmmssSSS')TIMEKEY " +
					 "FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME";
		
		Map bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTNAME", productName);
		List<Map<String, Object>> sqlLotList = 
			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlLotList;
	}
	
	public List<ProductPGSRC> getProductPGSRCSequence(Lot lotData)
		 	throws CustomException
		 {
			 try
			 {
				 List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				 
				 List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

				 for (Product productData : productList)
				 {
					ProductPGSRC productPGSRC = new ProductPGSRC();
					
					productPGSRC.setProductName(productData.getKey().getProductName());
						
					productPGSRC.setPosition(productData.getPosition());
					productPGSRC.setProductGrade(productData.getProductGrade());
						
					productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
					productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

					productPGSRC.setReworkFlag("N");
						
					productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

					productPGSRCSequence.add(productPGSRC);
				 }
				 
				 return productPGSRCSequence;
			 }
			 catch (NotFoundSignal ex)
			 {
				 throw new CustomException("SYS-0001", ex.getMessage());
			 }
			 catch (FrameworkErrorSignal fe)
			 {
				 throw new CustomException("SYS-0001", fe.getMessage());
			 }
			 catch (Exception ex)
			 {
				 throw new CustomException("SYS-0001", ex.getMessage());
			 }
		 }
	
}
