package kr.co.aim.messolution.extended.object;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.ExtendedProcessFlowUtil;
//import kr.co.aim.messolution.extended.object.management.data.MAKSRESERVECONFIG;
import kr.co.aim.messolution.extended.object.management.data.VirtualProductHistory;
//import kr.co.aim.messolution.extended.object.management.data.TPOMPolicy;
import kr.co.aim.messolution.extended.object.management.data.*;
//import kr.co.aim.messolution.extended.object.management.impl.MAKSRESERVECONFIGService;
import kr.co.aim.messolution.extended.object.management.impl.VirtualProductHistoryService;
//import kr.co.aim.messolution.extended.object.management.impl.TPOMPolicyService;
import kr.co.aim.messolution.extended.object.management.impl.*;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class ExtendedObjectProxy extends MESStackTrace implements ApplicationContextAware {

	private static ApplicationContext ac;

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		ExtendedObjectProxy.ac = arg0;
	}

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args) throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static SVIPickInfoService getSVIPickInfoService() throws CustomException
	{
		return (SVIPickInfoService) CTORMUtil.loadServiceProxy(SVIPickInfo.class);
	}
	
	public static TFEDownProductService getTFEDownProductService() throws CustomException
	{
		return (TFEDownProductService) CTORMUtil.loadServiceProxy(TFEDownProduct.class);
	}
	
	public static RunTimeMachineInfoService getRunTimeMachineInfoService() throws CustomException
	{
		return (RunTimeMachineInfoService) CTORMUtil.loadServiceProxy(RunTimeMachineInfo.class);
	}
	
	public static SorterPickPrintInfoService getSorterPickPrintInfoService() throws CustomException
	{
		return (SorterPickPrintInfoService) CTORMUtil.loadServiceProxy(SorterPickPrintInfo.class);
	}
	
	public static PanelProcessCountService getPanelProcessCountService() throws CustomException
	{
		return (PanelProcessCountService) CTORMUtil.loadServiceProxy(PanelProcessCount.class);
	}
	
	public static PhotoMaskStockerService getPhotoMaskStockerService() throws CustomException
	{
		return (PhotoMaskStockerService) CTORMUtil.loadServiceProxy(PhotoMaskStocker.class);
	}
	
	public static R2RFeedbackDEPOHistService getR2RFeedbackDEPOHistService() throws CustomException
	{
		return (R2RFeedbackDEPOHistService) CTORMUtil.loadServiceProxy(R2RFeedbackDEPOHist.class);
	}
	
	public static ExtendedProcessFlowUtil getExtendedProcessFlowUtil() throws CustomException
	{
		return (ExtendedProcessFlowUtil) BundleUtil.getServiceByBeanName(ExtendedProcessFlowUtil.class.getSimpleName());
	}
	
	public static MaskPPAInspectionService getMaskPPAInspectionService() throws CustomException
	{
		return (MaskPPAInspectionService) CTORMUtil.loadServiceProxy(MaskPPAInspection.class);
	}

	public static MaskSubSpecService getMaskSubSpecService() throws CustomException
	{
		return (MaskSubSpecService) CTORMUtil.loadServiceProxy(MaskSubSpec.class);
	}

	public static ELAConditionService getELAConditionService() throws CustomException
	{
		return (ELAConditionService) CTORMUtil.loadServiceProxy(ELACondition.class);
	}
	
	public static MachineAlarmProductListService getMachineAlarmProductListService() throws CustomException
	{
		return (MachineAlarmProductListService) CTORMUtil.loadServiceProxy(MachineAlarmProductList.class);
	}

	// Lot reservation service changed
	public static ReserveLotService getReserveLotService() throws CustomException
	{
		return (ReserveLotService) CTORMUtil.loadServiceProxy(ReserveLot.class);
	}

	// STK Config
	public static STKConfigService getSTKConfigService() throws CustomException
	{
		return (STKConfigService) CTORMUtil.loadServiceProxy(STKConfig.class);
	}

	// RTDPolicyModelAssign Service
	public static RTDPolicyModelAssignService getRTDPolicyModelAssignService() throws CustomException
	{
		return (RTDPolicyModelAssignService) CTORMUtil.loadServiceProxy(RTDPolicyModelAssign.class);
	}

	// Policy Service
	// 1.TFOPolicy Service
	public static TFOPolicyService getTFOPolicyService() throws CustomException
	{
		return (TFOPolicyService) CTORMUtil.loadServiceProxy(TFOPolicy.class);
	}

	// 2.POSAlterProcessOperation Service
	public static POSAlterProcessOperationService getPOSAlterProcessOperationService() throws CustomException
	{
		return (POSAlterProcessOperationService) CTORMUtil.loadServiceProxy(POSAlterProcessOperation.class);
	}

	// 3.POSQueueTime Service
	public static POSQueueTimeService getPOSQueueTimeService() throws CustomException
	{
		return (POSQueueTimeService) CTORMUtil.loadServiceProxy(POSQueueTime.class);
	}

	// 4.TFOMPolicy Service
	public static TFOMPolicyService getTFOMPolicyService() throws CustomException
	{
		return (TFOMPolicyService) CTORMUtil.loadServiceProxy(TFOMPolicy.class);
	}

	// 5.POSSample Service
	public static POSSampleService getPOSSampleService() throws CustomException
	{
		return (POSSampleService) CTORMUtil.loadServiceProxy(POSSample.class);
	}

	// 6.TPFOPolicy Service
	public static TPFOPolicyService getTPFOPolicyService() throws CustomException
	{
		return (TPFOPolicyService) CTORMUtil.loadServiceProxy(TPFOPolicy.class);
	}

	public static TransportJobCommandService getTransportJobCommand() throws CustomException
	{
		return (TransportJobCommandService) CTORMUtil.loadServiceProxy(TransportJobCommand.class);
	}

	public static StockerZoneInfoService getStockerZoneInfo() throws CustomException
	{
		return (StockerZoneInfoService) CTORMUtil.loadServiceProxy(StockerZoneInfo.class);
	}

	public static LotQueueTimeService getQTimeService() throws CustomException
	{
		return (LotQueueTimeService) CTORMUtil.loadServiceProxy(LotQueueTime.class);
	}

	public static ProductQueueTimeService getProductQTimeService() throws CustomException
	{
		return (ProductQueueTimeService) CTORMUtil.loadServiceProxy(ProductQueueTime.class);
	}

	public static ReserveMaskListService getReserveMaskService() throws CustomException

	{
		return (ReserveMaskListService) CTORMUtil.loadServiceProxy(ReserveMaskList.class);
	}

	public static RecipeService getRecipeService() throws CustomException
	{
		return (RecipeService) CTORMUtil.loadServiceProxy(Recipe.class);
	}

	public static RecipeParameterService getRecipeParamService() throws CustomException
	{
		return (RecipeParameterService) CTORMUtil.loadServiceProxy(RecipeParameter.class);
	}

	public static AlarmService getAlarmService() throws CustomException
	{
		return (AlarmService) CTORMUtil.loadServiceProxy(Alarm.class);
	}

	public static AlarmDefinitionService getAlarmDefinitionService() throws CustomException
	{
		return (AlarmDefinitionService) CTORMUtil.loadServiceProxy(AlarmDefinition.class);
	}

	public static AlarmActionDefService getAlarmActionDefService() throws CustomException
	{
		return (AlarmActionDefService) CTORMUtil.loadServiceProxy(AlarmActionDef.class);
	}

	public static AlarmHoldEQPService getAlarmHoldEQPService() throws CustomException
	{
		return (AlarmHoldEQPService) CTORMUtil.loadServiceProxy(AlarmHoldEQP.class);
	}

	public static MQCPlanDetailService getMQCPlanDetailService() throws CustomException
	{
		return (MQCPlanDetailService) CTORMUtil.loadServiceProxy(MQCPlanDetail.class);
	}

	public static MQCPlanService getMQCPlanService() throws CustomException
	{
		return (MQCPlanService) CTORMUtil.loadServiceProxy(MQCPlan.class);
	}

	public static MaskMQCPlanDetailService getMaskMQCPlanDetailService() throws CustomException
	{
		return (MaskMQCPlanDetailService) CTORMUtil.loadServiceProxy(MaskMQCPlanDetail.class);
	}

	public static MaskMQCPlanService getMaskMQCPlanService() throws CustomException
	{
		return (MaskMQCPlanService) CTORMUtil.loadServiceProxy(MaskMQCPlan.class);
	}

	public static PanelJudgeService getPanelJudgeService() throws CustomException

	{
		return (PanelJudgeService) CTORMUtil.loadServiceProxy(PanelJudge.class);
	}

	public static PanelDefectService getPanelDefectService() throws CustomException

	{
		return (PanelDefectService) CTORMUtil.loadServiceProxy(PanelDefect.class);
	}

	public static GlassJudgeService getGlassJudgeService() throws CustomException

	{
		return (GlassJudgeService) CTORMUtil.loadServiceProxy(GlassJudge.class);
	}

	public static ProductIdleTimeService getProductIdleTimeService() throws CustomException
	{
		return (ProductIdleTimeService) CTORMUtil.loadServiceProxy(ProductIdleTime.class);
	}

	public static SurfaceDefectService getSurfaceDefectService() throws CustomException
	{
		return (SurfaceDefectService) CTORMUtil.loadServiceProxy(SurfaceDefect.class);
	}

	public static MaskReserveSpecService getMaskReserveSpecService() throws CustomException
	{
		return (MaskReserveSpecService) CTORMUtil.loadServiceProxy(MaskReserveSpec.class);
	}

	public static MachineHistoryService getMachineHistoryService() throws CustomException
	{
		return (MachineHistoryService) CTORMUtil.loadServiceProxy(MachineHistory.class);
	}

	public static LotLastProductionService getLotLastProductionService() throws CustomException
	{
		return (LotLastProductionService) CTORMUtil.loadServiceProxy(LotLastProduction.class);
	}

	public static CrucibleLotService getCrucibleLotService() throws CustomException
	{
		return (CrucibleLotService) CTORMUtil.loadServiceProxy(CrucibleLot.class);
	}

	public static MaterialProductService getMaterialProductService() throws CustomException
	{
		return (MaterialProductService) CTORMUtil.loadServiceProxy(MaterialProduct.class);
	}

	public static MachineAlarmListService getMachineAlarmListService() throws CustomException
	{
		return (MachineAlarmListService) CTORMUtil.loadServiceProxy(MachineAlarmList.class);
	}

	public static ScrapCrateService getScrapCrateService() throws CustomException
	{
		return (ScrapCrateService) CTORMUtil.loadServiceProxy(ScrapCrate.class);
	}

	public static MaskSpecService getMaskSpecService() throws CustomException
	{
		return (MaskSpecService) CTORMUtil.loadServiceProxy(MaskSpec.class);
	}

	public static MaskStickService getMaskStickService() throws CustomException
	{
		return (MaskStickService) CTORMUtil.loadServiceProxy(MaskStick.class);
	}

	public static MaskMaterialService getMaskMaterialService() throws CustomException
	{
		return (MaskMaterialService) CTORMUtil.loadServiceProxy(MaskMaterial.class);
	}

	public static InlineSampleLotCountService getInlineSampleLotCountService() throws CustomException
	{
		return (InlineSampleLotCountService) CTORMUtil.loadServiceProxy(InlineSampleLotCount.class);
	}

	public static InlineSampleLotService getInlineSampleLotService() throws CustomException
	{
		return (InlineSampleLotService) CTORMUtil.loadServiceProxy(InlineSampleLot.class);
	}

	public static InlineSampleProductService getInlineSampleProductService() throws CustomException
	{
		return (InlineSampleProductService) CTORMUtil.loadServiceProxy(InlineSampleProduct.class);
	}

	public static MaskFrameService getMaskFrameService() throws CustomException
	{
		return (MaskFrameService) CTORMUtil.loadServiceProxy(MaskFrame.class);
	}

	public static MaskLotService getMaskLotService() throws CustomException
	{
		return (MaskLotService) CTORMUtil.loadServiceProxy(MaskLot.class);
	}

	public static ProductRequestPlanService getProductRequestPlanService() throws CustomException
	{
		return (ProductRequestPlanService) CTORMUtil.loadServiceProxy(ProductRequestPlan.class);
	}

	public static ComponentHistoryService getComponentHistoryService() throws CustomException
	{
		return (ComponentHistoryService) CTORMUtil.loadServiceProxy(ComponentHistory.class);
	}
	
	public static ComponentMonitorService getComponentMonitorService() throws CustomException
	{
		return (ComponentMonitorService) CTORMUtil.loadServiceProxy(ComponentMonitor.class);
	}

	public static EQPProcessTimeConfService getEQPProcessTimeConfService() throws CustomException
	{
		return (EQPProcessTimeConfService) CTORMUtil.loadServiceProxy(EQPProcessTimeConf.class);
	}
	
	public static ComponentInChamberHistService getComponentInChamberHistService() throws CustomException
	{
		return (ComponentInChamberHistService) CTORMUtil.loadServiceProxy(ComponentInChamberHist.class);
	}

	public static VirtualProductHistoryService VirtualProductHistoryService() throws CustomException
	{
		return (VirtualProductHistoryService) CTORMUtil.loadServiceProxy(VirtualProductHistory.class);
	}
	
	public static ValicationProductService ValicationProductService() throws CustomException
	{
		return (ValicationProductService) CTORMUtil.loadServiceProxy(ValicationProduct.class);
	}

	public static ComponentInspectHistoryService getComponentInspectHistoryService() throws CustomException
	{
		return (ComponentInspectHistoryService) CTORMUtil.loadServiceProxy(ComponentInspectHistory.class);
	}

	public static MaskLotComponentHistoryService getMaskLotComponentHistoryService() throws CustomException
	{
		return (MaskLotComponentHistoryService) CTORMUtil.loadServiceProxy(MaskLotComponentHistory.class);
	}

	public static CheckOffsetService getCheckOffsetService() throws CustomException
	{
		return (CheckOffsetService) CTORMUtil.loadServiceProxy(CheckOffset.class);
	}

	public static ProductRequestHistoryService getProductRequestHistoryService() throws CustomException
	{
		return (ProductRequestHistoryService) CTORMUtil.loadServiceProxy(ProductRequestHistory.class);
	}

	public static DLAPackingService getDLAPackingService() throws CustomException
	{
		return (DLAPackingService) CTORMUtil.loadServiceProxy(DLAPacking.class);
	}

	public static ReserveMaskToEQPService getReserveMaskToEQPService() throws CustomException
	{
		return (ReserveMaskToEQPService) CTORMUtil.loadServiceProxy(ReserveMaskToEQP.class);
	}

	public static ReserveMaskTransferService getReserveMaskTransferService() throws CustomException
	{
		return (ReserveMaskTransferService) CTORMUtil.loadServiceProxy(ReserveMaskTransfer.class);
	}

	public static MaskMaterialsService getMaskMaterialsService() throws CustomException
	{
		return (MaskMaterialsService) CTORMUtil.loadServiceProxy(MaskMaterials.class);
	}

	public static ReworkProductService getReworkProductService() throws CustomException
	{
		return (ReworkProductService) CTORMUtil.loadServiceProxy(ReworkProduct.class);
	}

	public static FirstGlassJobService getFirstGlassJobService() throws CustomException
	{
		return (FirstGlassJobService) CTORMUtil.loadServiceProxy(FirstGlassJob.class);
	}

	public static CustomAlarmService getCustomAlarmService() throws CustomException
	{
		return (CustomAlarmService) CTORMUtil.loadServiceProxy(CustomAlarm.class);
	}

	public static FQCRuleService getFQCRuleService() throws CustomException
	{
		return (FQCRuleService) CTORMUtil.loadServiceProxy(FQCRule.class);
	}

	public static IPQCRuleService getIPQCRuleService() throws CustomException
	{
		return (IPQCRuleService) CTORMUtil.loadServiceProxy(IPQCRule.class);
	}

	public static MaskFutureActionService getMaskFutureActionService() throws CustomException
	{
		return (MaskFutureActionService) CTORMUtil.loadServiceProxy(MaskFutureAction.class);
	}

	public static MaskMultiHoldService getMaskMultiHoldService() throws CustomException
	{
		return (MaskMultiHoldService) CTORMUtil.loadServiceProxy(MaskMultiHold.class);
	}

	public static HelpDeskInfoService getHelpDeskInfoService() throws CustomException
	{
		return (HelpDeskInfoService) CTORMUtil.loadServiceProxy(HelpDeskInfo.class);
	}

	public static FQCLotService getFQCLotService() throws CustomException
	{
		return (FQCLotService) CTORMUtil.loadServiceProxy(FQCLot.class);
	}

	public static IPQCLotService getIPQCLotService() throws CustomException
	{
		return (IPQCLotService) CTORMUtil.loadServiceProxy(IPQCLot.class);
	}

	public static MVIPanelJudgeService getMVIPanelJudgeService() throws CustomException
	{
		return (MVIPanelJudgeService) CTORMUtil.loadServiceProxy(MVIPanelJudge.class);
	}
	
	public static SVIPanelJudgeService getSVIPanelJudgeService() throws CustomException
	{
		return (SVIPanelJudgeService) CTORMUtil.loadServiceProxy(SVIPanelJudge.class);
	}

	public static MVIOpticalInspectionService getMVIOpticalInspectionService() throws CustomException
	{
		return (MVIOpticalInspectionService) CTORMUtil.loadServiceProxy(MVIOpticalInspection.class);
	}

	public static MVIElectricalInspectionService getMVIElectricalInspectionService() throws CustomException
	{
		return (MVIElectricalInspectionService) CTORMUtil.loadServiceProxy(MVIElectricalInspection.class);
	}

	public static FQCPanelJudgeService getFQCPanelJudgeService() throws CustomException
	{
		return (FQCPanelJudgeService) CTORMUtil.loadServiceProxy(FQCPanelJudge.class);
	}

	public static IPQCPanelJudgeService getIPQCPanelJudgeService() throws CustomException
	{
		return (IPQCPanelJudgeService) CTORMUtil.loadServiceProxy(IPQCPanelJudge.class);
	}

	public static FQCOpticalInspectionService getFQCOpticalInspectionService() throws CustomException
	{
		return (FQCOpticalInspectionService) CTORMUtil.loadServiceProxy(FQCOpticalInspection.class);
	}

	public static IPQCOpticalInspectionService getIPQCOpticalInspectionService() throws CustomException
	{
		return (IPQCOpticalInspectionService) CTORMUtil.loadServiceProxy(IPQCOpticalInspection.class);
	}

	public static FQCElectricalInspectionService getFQCElectricalInspectionService() throws CustomException
	{
		return (FQCElectricalInspectionService) CTORMUtil.loadServiceProxy(FQCElectricalInspection.class);
	}

	public static SampleMaskService getSampleMaskService() throws CustomException
	{
		return (SampleMaskService) CTORMUtil.loadServiceProxy(SampleMask.class);
	}

	public static BorrowPanelService getBorrowPanelService() throws CustomException
	{
		return (BorrowPanelService) CTORMUtil.loadServiceProxy(BorrowPanel.class);
	}

	public static OrganicService getOrganicService() throws CustomException
	{
		return (OrganicService) CTORMUtil.loadServiceProxy(Organic.class);
	}

	public static EnumDefService getEnumDefService() throws CustomException
	{
		return (EnumDefService) CTORMUtil.loadServiceProxy(EnumDef.class);
	}

	public static EnumDefValueService getEnumDefValueService() throws CustomException
	{
		return (EnumDefValueService) CTORMUtil.loadServiceProxy(EnumDefValue.class);
	}

	public static POSMachineService getPOSMachineService() throws CustomException
	{
		return (POSMachineService) CTORMUtil.loadServiceProxy(POSMachine.class);
	}

	public static DSPReserveWOService getDSPReserveWOService() throws CustomException
	{
		return (DSPReserveWOService) CTORMUtil.loadServiceProxy(DSPReserveWO.class);
	}

	public static DSPReserveOperationService getDSPReserveOperationService() throws CustomException
	{
		return (DSPReserveOperationService) CTORMUtil.loadServiceProxy(DSPReserveOperation.class);
	}

	public static RunBanRuleService getRunBanRuleService() throws CustomException
	{
		return (RunBanRuleService) CTORMUtil.loadServiceProxy(RunBanRule.class);
	}
	
	public static RunBanPreventRuleService getRunBanPreventRuleService() throws CustomException
	{
		return (RunBanPreventRuleService) CTORMUtil.loadServiceProxy(RunBanPreventRule.class);
	}

	public static DSPProductRequestPlanService getDSPProductRequestPlanService() throws CustomException
	{
		return (DSPProductRequestPlanService) CTORMUtil.loadServiceProxy(DSPProductRequestPlan.class);
	}

	public static ReserveMaskRecipeService getReserveMaskRecipeService() throws CustomException
	{
		return (ReserveMaskRecipeService) CTORMUtil.loadServiceProxy(ReserveMaskRecipe.class);
	}

	public static TPTJRuleService getTPTJRuleService() throws CustomException
	{
		return (TPTJRuleService) CTORMUtil.loadServiceProxy(TPTJRule.class);
	}

	public static TPTJProductService getTPTJProductService() throws CustomException
	{
		return (TPTJProductService) CTORMUtil.loadServiceProxy(TPTJProduct.class);
	}

	public static SurfaceDefectCodeService getSurfaceDefectCodeService() throws CustomException
	{
		return (SurfaceDefectCodeService) CTORMUtil.loadServiceProxy(SurfaceDefectCode.class);
	}

	public static TPTJCountService getTPTJCountService() throws CustomException
	{
		return (TPTJCountService) CTORMUtil.loadServiceProxy(TPTJCount.class);
	}

	public static SampleMaskCountService getSampleMaskCountService() throws CustomException
	{
		return (SampleMaskCountService) CTORMUtil.loadServiceProxy(SampleMaskCount.class);
	}

	public static OccupyItemService getOccupyItemService() throws CustomException
	{
		return (OccupyItemService) CTORMUtil.loadServiceProxy(OccupyItem.class);
	}

	public static RecipeCheckResultService getRecipeCheckResultService() throws CustomException
	{
		return (RecipeCheckResultService) CTORMUtil.loadServiceProxy(RecipeCheckResult.class);
	}

	public static RecipeCheckedMachineService getRecipeCheckedMachineService() throws CustomException
	{
		return (RecipeCheckedMachineService) CTORMUtil.loadServiceProxy(RecipeCheckedMachine.class);
	}

	public static MVITPInspectionService getMVITPInspectionService() throws CustomException
	{
		return (MVITPInspectionService) CTORMUtil.loadServiceProxy(MVITPInspection.class);
	}

	public static AVIPanelJudgeService getAVIPanelJudgeService() throws CustomException
	{
		return (AVIPanelJudgeService) CTORMUtil.loadServiceProxy(AVIPanelJudge.class);
	}

	public static EVAReserveSpecService getEVAReserveSpecService() throws CustomException
	{
		return (EVAReserveSpecService) CTORMUtil.loadServiceProxy(EVAReserveSpec.class);
	}

	public static EvaLineScheduleService getEvaLineScheduleService() throws CustomException
	{
		return (EvaLineScheduleService) CTORMUtil.loadServiceProxy(EvaLineSchedule.class);
	}

	public static MaskGroupListService getMaskGroupListService() throws CustomException
	{
		return (MaskGroupListService) CTORMUtil.loadServiceProxy(MaskGroupList.class);
	}

	public static MaskGroupService getMaskGroupService() throws CustomException
	{
		return (MaskGroupService) CTORMUtil.loadServiceProxy(MaskGroup.class);
	}
	
	public static SampleLotService getSampleLotService() throws CustomException
	{
		return (SampleLotService) CTORMUtil.loadServiceProxy(SampleLot.class);
	}

	public static SampleProductService getSampleProductService() throws CustomException
	{
		return (SampleProductService) CTORMUtil.loadServiceProxy(SampleProduct.class);
	}

	public static SortJobService getSortJobService() throws CustomException
	{
		return (SortJobService) CTORMUtil.loadServiceProxy(SortJob.class);
	}

	public static SortJobProductService getSortJobProductService() throws CustomException
	{
		return (SortJobProductService) CTORMUtil.loadServiceProxy(SortJobProduct.class);
	}

	public static SortJobCarrierService getSortJobCarrierService() throws CustomException
	{
		return (SortJobCarrierService) CTORMUtil.loadServiceProxy(SortJobCarrier.class);
	}
	
	public static LotFutureActionService getLotFutureActionService() throws CustomException
	{
		return (LotFutureActionService) CTORMUtil.loadServiceProxy(LotFutureAction.class);
	}
	
	public static SampleLotCountService getSampleLotCountService() throws CustomException
	{
		return (SampleLotCountService) CTORMUtil.loadServiceProxy(SampleLotCount.class);
	}
	
	public static MQCPlanDetail_ExtendedService getMQCPlanDetail_ExtendedService() throws CustomException
	{
		return (MQCPlanDetail_ExtendedService) CTORMUtil.loadServiceProxy(MQCPlanDetail_Extended.class);
	}
	
	public static DeptCommentService getDeptCommentService() throws CustomException
	{
		return (DeptCommentService) CTORMUtil.loadServiceProxy(DeptComment.class);
	}
	
	public static AbnormalUnitInfoService getAbnormalUnitInfoService() throws CustomException
	{
		return (AbnormalUnitInfoService) CTORMUtil.loadServiceProxy(AbnormalUnitInfo.class);
	}
	
	public static LotHoldActionService getLotHoldActionService() throws CustomException
	{
		return (LotHoldActionService) CTORMUtil.loadServiceProxy(LotHoldAction.class);
	}
	
	public static ProductRequestAssignService getProductRequestAssignService() throws CustomException
	{
		return (ProductRequestAssignService) CTORMUtil.loadServiceProxy(ProductRequestAssign.class);
	}
	
	public static MainReserveSkipService getMainReserveSkipService() throws CustomException
	{
		return (MainReserveSkipService) CTORMUtil.loadServiceProxy(MainReserveSkip.class);
	}
	
	public static MachineIdleByChamberService getMachineIdleByChamberService() throws CustomException
	{
		return (MachineIdleByChamberService) CTORMUtil.loadServiceProxy(MachineIdleByChamber.class);
	}
	
	public static ReserveRepairProductService getReserveRepairProductService() throws CustomException
	{
		return (ReserveRepairProductService) CTORMUtil.loadServiceProxy(ReserveRepairProduct.class);
	}
	
	public static PhotoOffsetResultService getPhotoOffsetResultService() throws CustomException
	{
		return (PhotoOffsetResultService) CTORMUtil.loadServiceProxy(PhotoOffsetResult.class);
	}
	
	public static OffsetAlignInfoService getOffsetAlignInfoService() throws CustomException
	{
		return (OffsetAlignInfoService) CTORMUtil.loadServiceProxy(OffsetAlignInfo.class);
	}
	
	public static ShieldSpecService getShieldSpecService() throws CustomException
	{
		return (ShieldSpecService) CTORMUtil.loadServiceProxy(ShieldSpec.class);
	}
	
	public static ShieldLotService getShieldLotService() throws CustomException
	{
		return (ShieldLotService) CTORMUtil.loadServiceProxy(ShieldLot.class);
	}
	
	public static AlterOperationByJudgeService getAlterOperationByJudgeService() throws CustomException
	{
		return (AlterOperationByJudgeService) CTORMUtil.loadServiceProxy(AlterOperationByJudge.class);
	}
	
	public static ShieldChamberMapService getShieldChamberMapService() throws CustomException
	{
		return (ShieldChamberMapService) CTORMUtil.loadServiceProxy(ShieldChamberMap.class);
	}
	
	public static FirstRunPanelService getFirstRunPanelService() throws CustomException
	{
		return (FirstRunPanelService) CTORMUtil.loadServiceProxy(FirstRunPanel.class);
	}
	
	public static MaskInspectionService getMaskInspectionService() throws CustomException
	{
		return (MaskInspectionService) CTORMUtil.loadServiceProxy(MaskInspection.class);
	}
	
	public static ComponentPalletHistoryService getComponentPalletHistoryService() throws CustomException
	{
		return (ComponentPalletHistoryService) CTORMUtil.loadServiceProxy(ComponentPalletHistory.class);
	}
	
	public static AbnormalSheetDetailService getAbnormalSheetDetailService() throws CustomException
	{
		return (AbnormalSheetDetailService) CTORMUtil.loadServiceProxy(AbnormalSheetDetail.class);
	}
	
	public static OriginalProductInfoService getOriginalProductInfoService() throws CustomException
	{
		return (OriginalProductInfoService) CTORMUtil.loadServiceProxy(OriginalProductInfo.class);
	}
	
	public static ReserveRepairPolicyService getReserveRepairPolicyService() throws CustomException
	{
		return (ReserveRepairPolicyService) CTORMUtil.loadServiceProxy(ReserveRepairPolicy.class);
	}
	public static BankQueueTimeService getBankQueueTimeService() throws CustomException
	{
		return (BankQueueTimeService) CTORMUtil.loadServiceProxy(BankQueueTime.class);
	}
	
	public static LOIPanelCodeListService getLOIPanelCodeListService() throws CustomException
	{
		return (LOIPanelCodeListService) CTORMUtil.loadServiceProxy(LOIPanelCodeList.class);
	}
	
	public static ReserveRepairPanelInfoService getReserveRepairPanelInfoService() throws CustomException
	{
		return (ReserveRepairPanelInfoService) CTORMUtil.loadServiceProxy(ReserveRepairPanelInfo.class);
	}
	
	public static OrganicExtractCardService getOrganicExtractCardService() throws CustomException
	{
		return (OrganicExtractCardService) CTORMUtil.loadServiceProxy(OrganicExtractCard.class);
	}
	
	public static OrganicMappingService getOrganicMappingService() throws CustomException
	{
		return (OrganicMappingService) CTORMUtil.loadServiceProxy(OrganicMapping.class);
	}
	
	public static CrucibleOrganicService getCrucibleOrganicService() throws CustomException
	{
		return (CrucibleOrganicService) CTORMUtil.loadServiceProxy(CrucibleOrganic.class);
	}
	
	public static BankInfoService getBankInfoService() throws CustomException
	{
		return (BankInfoService) CTORMUtil.loadServiceProxy(BankInfo.class);
	}
	
	public static MaskGroupLotService getMaskGroupLotService() throws CustomException
	{
		return (MaskGroupLotService) CTORMUtil.loadServiceProxy(MaskGroupLot.class);
	}
	
	public static EvaLineWoService getEvaLineWoService() throws CustomException
	{
		return (EvaLineWoService) CTORMUtil.loadServiceProxy(EvaLineWo.class);
	}
	
	public static MaskOffsetSpecService getMaskOffsetSpecService() throws CustomException
	{
		return (MaskOffsetSpecService) CTORMUtil.loadServiceProxy(MaskOffsetSpec.class);
	}
	
	public static TPOffsetAlignInfoService getTPOffsetAlignInfoService() throws CustomException
	{
		return (TPOffsetAlignInfoService) CTORMUtil.loadServiceProxy(TPOffsetAlignInfo.class);
	}
	
	public static DSPRunControlService getDSPRunControlService() throws CustomException
	{
		return (DSPRunControlService) CTORMUtil.loadServiceProxy(DSPRunControl.class);
	}
	
	public static TryRunControlService getTryRunControlService() throws CustomException
	{
		return (TryRunControlService) CTORMUtil.loadServiceProxy(TryRunControl.class);
	}
	
	public static ChangeRunControlService getChangeRunControlService() throws CustomException
	{
		return (ChangeRunControlService) CTORMUtil.loadServiceProxy(ChangeRunControl.class);
	}
	
	public static DefectCodeService getDefectCodeService() throws CustomException
	{
		return (DefectCodeService) CTORMUtil.loadServiceProxy(DefectCode.class);
	}
	
	public static DefectGroupService getDefectGroupServices() throws CustomException
	{
		return (DefectGroupService) CTORMUtil.loadServiceProxy(DefectGroup.class);
	}
	
	public static DefectProductGroupService getDefectProductGroupServices() throws CustomException
	{
		return (DefectProductGroupService) CTORMUtil.loadServiceProxy(DefectProductGroup.class);
	}
	
	public static MaskStickRuleService getMaskStickRuleServices() throws CustomException
	{
		return (MaskStickRuleService) CTORMUtil.loadServiceProxy(MaskStickRule.class);
	}
	
	public static DummyProductReserveService getDummyProductReserveService() throws CustomException
	{
		return (DummyProductReserveService) CTORMUtil.loadServiceProxy(DummyProductReserve.class);
	}
	
	public static DummyProductAssignService getDummyProductAssignService() throws CustomException
	{
		return (DummyProductAssignService) CTORMUtil.loadServiceProxy(DummyProductAssign.class);
	}

	public static FQCInspectionService getFQCInspectionService() throws CustomException
	{
		return (FQCInspectionService) CTORMUtil.loadServiceProxy(FQCInspection.class);
	}

	public static MVIUserDefectHistService getMVIUserDefectService() throws CustomException
	{
		return (MVIUserDefectHistService) CTORMUtil.loadServiceProxy(MVIUserDefectHist.class);
	}
	
	public static MVIAssignTrayService getMVIAssignTrayService() throws CustomException
	{
		return (MVIAssignTrayService) CTORMUtil.loadServiceProxy(MVIAssignTray.class);
	}
	
	public static PackingBanService getPackingBanService() throws CustomException
	{
		return (PackingBanService) CTORMUtil.loadServiceProxy(PackingBan.class);
	}
	
	public static MVIDefectCodeService getMVIDefectCodeService() throws CustomException
	{
		return (MVIDefectCodeService) CTORMUtil.loadServiceProxy(MVIDefectCode.class);
	}
	
	public static MVIJNDDefectCodeService getMVIJNDDefectCodeService() throws CustomException
	{
		return (MVIJNDDefectCodeService) CTORMUtil.loadServiceProxy(MVIJNDDefectCode.class);
	}
	
	public static ReviewOperationInfoService getReviewOperationInfoService() throws CustomException
	{
		return (ReviewOperationInfoService) CTORMUtil.loadServiceProxy(ReviewOperationInfo.class);
	}
	
	public static ChangeRecipeService getChangeRecipeService() throws CustomException
	{
		return (ChangeRecipeService) CTORMUtil.loadServiceProxy(ChangeRecipe.class);
	}
	
	public static ReviewStationTestService getReviewStationTestService() throws CustomException
	{
		return (ReviewStationTestService) CTORMUtil.loadServiceProxy(ReviewStationTest.class);
	}
	
	public static ReviewTestImageJudgeService getReviewStationImageJudgeService() throws CustomException
	{
		return (ReviewTestImageJudgeService) CTORMUtil.loadServiceProxy(ReviewTestImageJudge.class);
	}
	
	public static SuperProductRequestService getSuperProductRequestService() throws CustomException
	{
		return (SuperProductRequestService) CTORMUtil.loadServiceProxy(SuperProductRequest.class);
	}
	
	public static BufferCSTTransInfoService getBufferCSTTransInfoService() throws CustomException
	{
		return (BufferCSTTransInfoService) CTORMUtil.loadServiceProxy(BufferCSTTransInfo.class);
	}
	
	public static WOPatternFilmInfoService getWOPatternFilmInfoService() throws CustomException
	{
		return (WOPatternFilmInfoService) CTORMUtil.loadServiceProxy(WOPatternFilmInfo.class);
	}
	
	public static ProductFutureActionService getProductFutureActionService() throws CustomException
	{
		return (ProductFutureActionService) CTORMUtil.loadServiceProxy(ProductFutureAction.class);
	}
	
	public static DSPControlService getDSPControlService() throws CustomException
	{
		return (DSPControlService) CTORMUtil.loadServiceProxy(DSPControl.class);
	}
	
	public static MQCRunControlService getMQCRunControlService() throws CustomException
	{
		return (MQCRunControlService) CTORMUtil.loadServiceProxy(MQCRunControl.class);
	}
	
	public static AbnormalEQPService getAbnormalEQPService() throws CustomException
	{
		return (AbnormalEQPService) CTORMUtil.loadServiceProxy(AbnormalEQP.class);
	}
	
	public static AbnormalEQPCommandService getAbnormalEQPCommandService() throws CustomException
	{
		return (AbnormalEQPCommandService) CTORMUtil.loadServiceProxy(AbnormalEQPCommand.class);
	}
	
	public static ReviewComponentHistoryService getReviewComponentHistoryService() throws CustomException
	{
		return (ReviewComponentHistoryService) CTORMUtil.loadServiceProxy(ReviewComponentHistory.class);
	}
	
	public static AMSUserService getAMSUserService() throws CustomException
	{
		return (AMSUserService) CTORMUtil.loadServiceProxy(AMSUserService.class);
	}
	
	public static AMSRuleService getAMSRuleService() throws CustomException
	{
		return (AMSRuleService) CTORMUtil.loadServiceProxy(AMSRuleService.class);
	}
	
	public static AMSRuleAssignService getAMSRuleAssignService() throws CustomException
	{
		return (AMSRuleAssignService) CTORMUtil.loadServiceProxy(AMSRuleAssignService.class);
	}
	
	public static AMSMessageService getAMSMessageService() throws CustomException
	{
		return (AMSMessageService) CTORMUtil.loadServiceProxy(AMSMessageService.class);
	}
	
	public static FirstOnlineProductService getFirstOnlineProductService() throws CustomException
	{
		return (FirstOnlineProductService) CTORMUtil.loadServiceProxy(FirstOnlineProduct.class);
	}

	public static LOIRecipeService getLOIRecipeService() throws CustomException
	{
		return (LOIRecipeService) CTORMUtil.loadServiceProxy(LOIRecipe.class);
	}
	public static ReserveHoldByWOInfoService ReserveHoldByWOInfoService() throws CustomException
	{
		return (ReserveHoldByWOInfoService) CTORMUtil.loadServiceProxy(ReserveHoldByWOInfo.class);
	}
	
	public static ControlProductRequestAssignService getControlProductRequestAssignService() throws CustomException
	{
		return (ControlProductRequestAssignService) CTORMUtil.loadServiceProxy(ControlProductRequestAssign.class);
	}
	
	public static SorterSignReserveService getSorterSignReserveService() throws CustomException
	{
		return (SorterSignReserveService) CTORMUtil.loadServiceProxy(SorterSignReserve.class);
	}
	public static VcrAbnormalPanelService getVcrAbnormalPanelDataService() throws CustomException
	{
		return (VcrAbnormalPanelService) CTORMUtil.loadServiceProxy(VcrAbnormalPanel.class);
	}
	public static RiskLotService getRiskLotService() throws CustomException
	{
		return (RiskLotService) CTORMUtil.loadServiceProxy(RiskLot.class);
	}

}
