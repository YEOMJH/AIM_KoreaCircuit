package kr.co.aim.messolution.generic.master;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.generic.master.AbstractConstantMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

public class ConstantMap extends AbstractConstantMap
//		implements ApplicationContextAware, InitializingBean
{
	private static Log				log								= LogFactory.getLog(ConstantMap.class);

	public static final String ConstantDef = "select constantName, constantValue" + SystemPropHelper.CR + 
	 " from ConstantDef where cacheFlag = 'Y' ORDER BY constantName" ;
	
	@Override
	protected void setLogger() {
		parentLog = log;
		
	}	
	
	public String                  MStocker_SlotStatus_IDLE          = "IDLE";
	public String                  MStocker_SlotStatus_INUSE         = "INUSE";
	public String                  MStocker_SlotStatus_DisableIn     = "DisableIn";
	public String                  MStocker_SlotStatus_DisableOut    = "DisableOut";
	
	public String                  PhotoMask_CleanState_Clean        = "Clean";
	public String                  PhotoMask_CleanState_Dirty        = "Dirty";
	
	public String                  PhotoMask_State_Available         = "Available";
	public String                  PhotoMask_State_Mounted           = "Mounted";
	public String                  PhotoMask_State_UnMounted         = "UnMounted";
	public String                  PhotoMask_State_Prepare           = "Prepare";
	public String                  PhotoMask_State_InUse             = "InUse";
	public String                  PhotoMask_State_Scrapped          = "Scrapped";
	                                                                 
	public String                  PMask_TransportState_Moving       = "Moving";
	public String                  PMask_TransportState_InStock      = "InStock";
	public String                  PMask_TransportState_OnEQP        = "OnEQP";
	
	//Stick state , judge ,type
	
	public String                  Stick_State_NotAvailable        = "NotAvailable";
	public String                  Stick_State_Available           = "Available";
	public String                  Stick_State_Scrapped            = "Scrapped";
	public String                  Stick_State_InUse               = "InUse";

	public String                  Stick_Judge_G                = "G";
	public String                  Stick_Judge_S                = "S";
	public String                  Stick_Judge_N                = "N";
	
	public String 				   Stick_Type_F 				= "F";
	public String 				   Stick_Type_V 				= "V";
	public String 				   Stick_Type_H 				= "H";
	public String 				   Stick_Type_A 				= "A";
	public String 				   Stick_Type_D 				= "D";
	public String 				   Stick_Type_C 				= "C";

	public String                   AlarmAction_LotHold             ="LotHold";
	public String                   AlarmAction_EQPHold             ="EQPHold";
	public String                   AlarmAction_OPCall              ="OPCall";
	public String                   AlarmAction_CHPause             ="CHPause";
	public String                   AlarmAction_EQPHalt             ="EQPHalt";
	public String                   AlarmAction_Mail                ="Mail";
	
	public String                   AlarmType_MachineDown           ="MachineDown";
	public String                   AlarmType_MaterialQTime         ="MaterialQTime";
	public String                   AlarmType_RunOut                = "RunOut";
    public String                   AlarmType_EQP                   = "EQP";
    public String                   AlarmType_FDC                   = "FDC";
    public String                   AlarmType_SPC                   = "SPC";
	
	public String                   AlarmState_CLEAR                ="CLEAR";
	public String                   AlarmState_ISSUE                ="ISSUE";
	
	public String                   Cons_TransportState_OnEQP       ="OnEQP";
	/**
	 * @uml.property  name="spec_CheckedOut"
	 */
	public String					Spec_CheckedOut					= "CheckedOut";
	/**
	 * @uml.property  name="spec_CheckedIn"
	 */
	public String					Spec_CheckedIn					= "CheckedIn";
	/**
	 * @uml.property  name="spec_NotActive"
	 */
	public String					Spec_NotActive					= "NotActive";
	/**
	 * @uml.property  name="spec_Active"
	 */
	public String					Spec_Active						= "Active";

	// StateModel
	/**
	 * @uml.property  name="stml_Resource"
	 */
	public String					Stml_Resource					= "ResourceState";
	/**
	 * @uml.property  name="stml_Communication"
	 */
	public String					Stml_Communication				= "MachineCommunicationState";
	/**
	 * @uml.property  name="stml_PortAccessMode"
	 */
	public String					Stml_PortAccessMode				= "PortAccessMode";
	/**
	 * @uml.property  name="stml_PortTransferState"
	 */
	public String					Stml_PortTransferState			= "PortTransferState";
	/**
	 * @uml.property  name="stml_LotState"
	 */
	public String					Stml_LotState					= "LotState";
	/**
	 * @uml.property  name="stml_LotProcessState"
	 */
	public String					Stml_LotProcessState			= "LotProcessState";
	/**
	 * @uml.property  name="stml_LotHoldState"
	 */
	public String					Stml_LotHoldState				= "LotHoldState";
	/**
	 * @uml.property  name="stml_ProductState"
	 */
	public String					Stml_ProductState				= "ProductState";
	/**
	 * @uml.property  name="stml_ProductProcessState"
	 */
	public String					Stml_ProductProcessState		= "ProductProcessState";
	/**
	 * @uml.property  name="stml_ProductHoldState"
	 */
	public String					Stml_ProductHoldState			= "ProductHoldState";
	/**
	 * @uml.property  name="stml_DurableState"
	 */
	public String					Stml_DurableState				= "DurableState";
	/**
	 * @uml.property  name="stml_DurableCleanState"
	 */
	public String					Stml_DurableCleanState			= "DurableCleanState";
	/**
	 * @uml.property  name="PHMask"
	 */
	public String					PHMask			= "PhotoMask";
	/**
	 * @uml.property  name="EVPMask"
	 */
	public String					EVPMask			= "EVPMask";
	/**
	 * @uml.property  name="stml_ConsumableState"
	 */
	public String					Stml_ConsumableState			= "ConsumableState";

	// 2. tracking
	// 2.1. Lot
	/**
	 * @uml.property  name="lot_Created"
	 */
	public String					Lot_Created						= "Created";
	/**
	 * @uml.property  name="lot_Released"
	 */
	public String					Lot_Released					= "Released";
	/**
	 * @uml.property  name="lot_Completed"
	 */
	public String					Lot_Completed					= "Completed";
	/**
	 * @uml.property  name="Lot_Issue"
	 */
	public String					Lot_Issue						= "Y";
	/**
	 * @uml.property  name="Lot_IssueReleased"
	 */
	public String					Lot_IssueReleased				= "N";
	/**
	 * @uml.property  name="lot_Shipped"
	 */
	public String					Lot_Shipped						= "Shipped";
	/**
	 * @uml.property  name="lot_Scrapped"
	 */
	public String					Lot_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="lot_Emptied"
	 */
	public String					Lot_Emptied						= "Emptied";
	/**
	 * @uml.property  name="lot_InitialState"
	 */
	public String					Lot_InitialState				= "Created";
	/**
	 * @uml.property  name="lot_WaitingToLogin"
	 */
	public String					Lot_WaitingToLogin				= "WaitingToLogin";
	/**
	 * @uml.property  name="lot_LoggedIn"
	 */
	public String					Lot_LoggedIn					= "LoggedIn";
	/**
	 * @uml.property  name="lot_LoggedOut"
	 */
	public String					Lot_LoggedOut					= "LoggedOut";
	/**
	 * @uml.property  name="lot_InitialProcessState"
	 */
	public String					Lot_InitialProcessState			= "LoggedIn";
	/**
	 * @uml.property  name="lot_NotOnHold"
	 */
	public String					Lot_NotOnHold					= "NotOnHold";
	/**
	 * @uml.property  name="lot_OnHold"
	 */
	public String					Lot_OnHold						= "OnHold";
	/**
	 * @uml.property  name="lot_InitialHoldState"
	 */
	public String					Lot_InitialHoldState			= "NotOnHold";
	/**
	 * @uml.property  name="lot_InRework"
	 */
	public String					Lot_InRework					= "InRework";
	/**
	 * @uml.property  name="lot_NotInRework"
	 */
	public String					Lot_NotInRework					= "NotInRework";
	/**
	 * @uml.property  name="lot_InitialReworkState"
	 */
	public String					Lot_InitialReworkState			= "NotInRework";
	/**
	 * @uml.property  name="lot_Run"
	 */
	public String					Lot_Run					= "RUN";
	/**
	 * @uml.property  name="lot_Wait"
	 */
	public String					Lot_Wait					= "WAIT";
	/**
	 * @uml.property  name="Panel_Borrowed"
	 */
	public String					Borrow_Created					= "Created";
	public String					Borrow_Confirmed				= "Confirmed";
	public String					Borrow_Borrowed					= "Borrowed";
	public String					Borrow_Completed				= "Completed";
	


	// 2.2. Product
	/**
	 * @uml.property  name="prod_NotAllocated"
	 */
	public String					Prod_NotAllocated				= "NotAllocated";
	/**
	 * @uml.property  name="prod_Allocated"
	 */
	public String					Prod_Allocated					= "Allocated";
	/**
	 * @uml.property  name="prod_InProduction"
	 */
	public String					Prod_InProduction				= "InProduction";
	/**
	 * @uml.property  name="prod_Completed"
	 */
	public String					Prod_Completed					= "Completed";
	/**
	 * @uml.property  name="prod_Shipped"
	 */
	public String					Prod_Shipped					= "Shipped";
	/**
	 * @uml.property  name="prod_Scrapped"
	 */
	public String					Prod_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="prod_Consumed"
	 */
	public String					Prod_Consumed					= "Consumed";
	/**
	 * @uml.property  name="prod_InitialState"
	 */
	public String					Prod_InitialState				= "Allocated";
	/**
	 * @uml.property  name="prod_Traveling"
	 */
	public String					Prod_Traveling					= "Traveling";
	/**
	 * @uml.property  name="prod_Idle"
	 */
	public String					Prod_Idle						= "Idle";
	/**
	 * @uml.property  name="prod_Processing"
	 */
	public String					Prod_Processing					= "Processing";
	/**
	 * @uml.property  name="prod_InitialProcessState"
	 */
	public String					Prod_InitialProcessState		= "Idle";
	/**
	 * @uml.property  name="prod_NotOnHold"
	 */
	public String					Prod_NotOnHold					= "NotOnHold";
	/**
	 * @uml.property  name="prod_OnHold"
	 */
	public String					Prod_OnHold						= "OnHold";
	/**
	 * @uml.property  name="prod_InitialHoldState"
	 */
	public String					Prod_InitialHoldState			= "NotOnHold";
	/**
	 * @uml.property  name="prod_InRework"
	 */
	public String					Prod_InRework					= "InRework";
	/**
	 * @uml.property  name="prod_NotInRework"
	 */
	public String					Prod_NotInRework				= "NotInRework";
	/**
	 * @uml.property  name="prod_InitialReworkState"
	 */
	public String					Prod_InitialReworkState			= "NotInRework";
	/**
	 * @uml.property  name="prod_InitialGrade"
	 */
	public String					prod_InitialGrade				= "G";
	
	
	// 2.3. Durable
	/**
	 * @uml.property  name="dur_NotAvailable"
	 */
	public String					Dur_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="dur_Available"
	 */
	public String					Dur_Available					= "Available";
	/**
	 * @uml.property  name="dur_InUse"
	 */
	public String					Dur_InUse						= "InUse";
	/**
	 * @uml.property  name="dur_Scrapped"
	 */
	public String					Dur_Scrapped					= "Scrapped";
	/**
	 * @uml.property  name="dur_Clean"
	 */
	public String					Dur_Clean						= "Clean";
	/**
	 * @uml.property  name="dur_Dirty"
	 */
	public String					Dur_Dirty						= "Dirty";
	/**
	 * @uml.property  name="dur_Mouted"
	 */
	public String					Dur_Mounted						= "Mounted";
	/**
	 * @uml.property  name="dur_Unmouted"
	 */
	public String					Dur_UnMounted					= "UnMounted";
	/**
	 * @uml.property  name="dur_Repairing"
	 */
	public String					Dur_Repairing					= "Repairing";
	/**
	 * @uml.property  name="dur_InitialState"
	 */
	public String					Dur_InitialState				= "Available";
	/**
	 * @uml.property  name="dur_InitialCleanState"
	 */
	public String					Dur_InitialCleanState			= "Clean";
	/**
	 * @uml.property  name="dur_InitialState"
	 */
	public String					Dur_Prepare						= "Prepare";
	
	// 2.4. Consumable
	/**
	 * @uml.property  name="cons_NotAvailable"
	 */
	public String					Cons_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="cons_Available"
	 */
	public String					Cons_Available					= "Available";
	
	/**
	 * @uml.property  name="cons_InUse"
	 */
	public String					Cons_InUse					= "InUse";
	
	/**
	 * @uml.property  name="cons_InitialState"
	 */
	public String					Cons_InitialState				= "Available";

	// 2.5. MaterialGroup
	/**
	 * @uml.property  name="mgp_Lot"
	 */
	public String					Mgp_Lot							= "Lot";
	/**
	 * @uml.property  name="mgp_Product"
	 */
	public String					Mgp_Product						= "Product";

	// 3. ProcessFlow
	
	//Flow
	public String					ProcessFlowType_Main			= "Main";
	
	// 3.1 Node Type
	/**
	 * @uml.property  name="node_Start"
	 */
	public String					Node_Start						= "Start";
	/**
	 * @uml.property  name="node_End"
	 */
	public String					Node_End						= "End";
	/**
	 * @uml.property  name="node_ProcessOperation"
	 */
	public String					Node_ProcessOperation			= "ProcessOperation";
	/**
	 * @uml.property  name="node_ProcessFlow"
	 */
	public String					Node_ProcessFlow				= "ProcessFlow";
	/**
	 * @uml.property  name="node_ConditionalDivergence"
	 */
	public String					Node_ConditionalDivergence		= "ConditionalDivergence";
	/**
	 * @uml.property  name="node_ConditionalConvergence"
	 */
	public String					Node_ConditionalConvergence		= "ConditionalConvergence";
	/**
	 * @uml.property  name="node_ReworkDivergence"
	 */
	public String					Node_ReworkDivergence			= "ReworkDivergence";
	/**
	 * @uml.property  name="node_ReworkConvergence"
	 */
	public String					Node_ReworkConvergence			= "ReworkConvergence";

	// 3.2 Arc Type
	/**
	 * @uml.property  name="arc_Normal"
	 */
	public String					Arc_Normal						= "Normal";
	/**
	 * @uml.property  name="arc_Conditional"
	 */
	public String					Arc_Conditional					= "Conditional";
	/**
	 * @uml.property  name="arc_Rework"
	 */
	public String					Arc_Rework						= "Rework";

	// 4. DataCollection
	/**
	 * @uml.property  name="dcs_Item"
	 */
	public String					Dcs_Item						= "N";

	// 5. ProductRequest
	/**
	 * @uml.property  name="prq_Created"
	 */
	public String					Prq_Created						= "Created";
	/**
	 * @uml.property  name="prq_Released"
	 */
	public String					Prq_Released					= "Released";
	/**
	 * @uml.property  name="prq_Completed"
	 */
	public String					Prq_Completed					= "Completed";
	/**
	 * @uml.property  name="prq_NotOnHold"
	 */
	public String					Prq_NotOnHold					= "NotOnHold";
	/**
	 * @uml.property  name="prq_OnHold"
	 */
	public String					Prq_OnHold						= "OnHold";

	/**
	 * @uml.property  name="spar_Available"
	 */
	public String					Spar_Available					= "Available";
	/**
	 * @uml.property  name="spar_NotAvailable"
	 */
	public String					Spar_NotAvailable				= "NotAvailable";
	/**
	 * @uml.property  name="spar_InitialState"
	 */
	public String					Spar_InitialState				= "Available";

	/**
	 * @uml.property  name="pMJob_Queued"
	 */
	public String					PMJob_Queued					= "Queued";
	/**
	 * @uml.property  name="pMJob_Paused"
	 */
	public String					PMJob_Paused					= "Paused";
	/**
	 * @uml.property  name="pMJob_Executing"
	 */
	public String					PMJob_Executing					= "Executing";
	/**
	 * @uml.property  name="pMJob_Aborted"
	 */
	public String					PMJob_Aborted					= "Aborted";
	/**
	 * @uml.property  name="pMJob_Canceled"
	 */
	public String					PMJob_Canceled					= "Canceled";
	/**
	 * @uml.property  name="pMJob_Completed"
	 */
	public String					PMJob_Completed					= "Completed";
	/**
	 * @uml.property  name="pMJob_InitialState"
	 */
	public String					PMJob_InitialState				= "Queued";
	/**
	 * @uml.property  name="pMJob_Stopped"
	 */
	public String					PMJob_Stopped					= "Stopped";

	// 6. Machine
	// Resource State
	/**
	 * @uml.property  name="rsc_OutOfService"
	 */
	public String					Rsc_OutOfService				= "OutOfService";						// Default
	/**
	 * @uml.property  name="rsc_InService"
	 */
	public String					Rsc_InService					= "InService";

	/**
	 * @uml.property  name="rsc_InitialResourceState"
	 */
	public String					Rsc_InitialResourceState		= "InService";

	// Machine Type
	/**
	 * @uml.property  name="mac_ProductionMachine"
	 */
	public String					Mac_ProductionMachine			= "ProductionMachine";
	/**
	 * @uml.property  name="mac_StorageMachine"
	 */
	public String					Mac_StorageMachine				= "StorageMachine";
	/**
	 * @uml.property  name="mac_TransportMachine"
	 */
	public String					Mac_TransportMachine			= "TransportMachine";
	/**
	 * @uml.property  name="mac_ProductionResource"
	 */
	public String					Mac_ProductionResource			= "ProductionResource";
	/**
	 * @uml.property  name="mac_StorageResource"
	 */
	public String					Mac_StorageResource				= "StorageResource";
	/**
	 * @uml.property  name="mac_TransportResource"
	 */
	public String					Mac_TransportResource			= "TransportResource";
	/**
	 * @uml.property  name="mac_Port"
	 */
	public String					Mac_Port						= "Port";

	// E10 State
	/**
	 * @uml.property  name="mac_StandBy"
	 */
	public String					Mac_StandBy						= "StandBy";
	/**
	 * @uml.property  name="mac_Productive"
	 */
	public String					Mac_Productive					= "Productive";
	/**
	 * @uml.property  name="mac_Engineering"
	 */
	public String					Mac_Engineering					= "Engineering";
	/**
	 * @uml.property  name="mac_ScheduledDown"
	 */
	public String					Mac_ScheduledDown				= "ScheduledDown";
	/**
	 * @uml.property  name="mac_UnscheduledDown"
	 */
	public String					Mac_UnscheduledDown				= "UnScheduledDown";
	/**
	 * @uml.property  name="mac_NonScheduled"
	 */
	public String					Mac_NonScheduled				= "NonScheduled";

	/**
	 * @uml.property  name="mac_InitialE10State"
	 */
	public String					Mac_InitialE10State				= "StandBy";

	// OnLine State
	/**
	 * @uml.property  name="mac_OffLine"
	 */
	public String					Mac_OffLine						= "OffLine";
	/**
	 * @uml.property  name="mac_OnLineLocal"
	 */
	public String					Mac_OnLineLocal					= "OnLineLocal";
	/**
	 * @uml.property  name="mac_OnLineRemote"
	 */
	public String					Mac_OnLineRemote				= "OnLineRemote";
	
	/**
	 * @uml.property  name="mac_OnHold"
	 */
	public String					Mac_OnHold						= "Y";
	/**
	 * @uml.property  name="mac_NotOnHold"
	 */
	public String					Mac_NotOnHold					= "N";
	
	// MachineState
	public String					Mac_Idle						= "IDLE";
	public String					Mac_Run							= "RUN";
	public String					Mac_Down						= "DOWN";
	public String					Mac_PM							= "PM";
	public String					Mac_Test						= "TEST";
	public String					Mac_Manual						= "MANUAL";
	
	/**
	 * @uml.property  name="mac_InitialCommunicationState"
	 */
	public String					Mac_InitialCommunicationState	= "OffLine";

	//Machin unit type
	public String					Mac_PhotoUnit					= "Photo";
	public String					Mac_StockUnit    				= "Buffer";
	public String					Mac_RackUnit					= "Rack";
	public String					Mac_StageUnit					= "Stage";
	public String					Mac_ExposeUnit					= "Exposure";
	public String					Mac_CleanUnit					= "Cleaner";
	public String					Mac_RobotUnit					= "Robot";
	public String					Mac_IndexUnit					= "Index";
	public String					Mac_ReadUnit					= "BCRReader";
	public String					Mac_ProcessUnit					= "Process";
	public String					Mac_InspectUnit					= "Inspection";
	public String					Mac_TransferUnit				= "Transport";


	// Material
	public String					ConsumableType_Ink				= "Ink";
	public String					ConsumableType_Organic			= "Organic";
	public String					ConsumableType_InOrganic		= "InOrganic";
	public String					ConsumableType_Crate			= "Crate";

	public String					MaterialLocation_Unknown		= "Unknown";
	public String					MaterialLocation_InStock		= "InStock";
	public String					MaterialLocation_OutStock		= "OutStock";
	public String					MaterialLocation_InStage		= "InStage";
	public String					MaterialLocation_Crucible		= "Crucible";
	public String					MaterialLocation_Bank			= "Bank";
	public String					MaterialLocation_OnEQP			= "OnEQP";
	public String					MaterialLocation_OutEQP			= "OutEQP";

	// Crucible
	public String					CrucibleLotState_Created		= "Created";
	public String					CrucibleLotState_Released		= "Released";
	public String					CrucibleLotState_Completed		= "Completed";
	public String					CrucibleLotState_Scrapped		= "Scrapped";
	
	// Material Group
	public String					MaterialGroup_Mask				= "MaskGroup";
	
	// TransportState of Durable
	public String					Dur_INSTK						= "INSTK";
	public String					Dur_MOVING						= "MOVING";
	public String					Dur_ONEQP						= "ONEQP";
	public String					Dur_ONMGV						= "ONMGV";
	public String					Dur_PROCESSING					= "PROCESSING";
	public String					Dur_OUTEQP						= "OUTEQP";
	public String					Dur_OUTSTK						= "OUTSTK";
	
	// Port Access Mode
	/**
	 * @uml.property  name="port_Manual"
	 */
	public String					Port_Manual						= "Manual";
	/**
	 * @uml.property  name="port_Auto"
	 */
	public String					Port_Auto						= "Auto";

	/**
	 * @uml.property  name="port_InitialAccessMode"
	 */
	public String					Port_InitialAccessMode			= "Auto";

	// Port Transfer State
	/**
	 * @uml.property  name="port_ReadyToLoad"
	 */
	public String					Port_ReadyToLoad				= "ReadyToLoad";
	/**
	 * @uml.property  name="port_ReservedToLoad"
	 */
	public String					Port_ReservedToLoad				= "ReservedToLoad";
	/**
	 * @uml.property  name="port_ReadyToProcess"
	 */
	public String					Port_ReadyToProcess				= "ReadyToProcess";
	/**
	 * @uml.property  name="port_Processing"
	 */
	public String					Port_Processing					= "Processing";
	/**
	 * @uml.property  name="port_ReadyToUnload"
	 */
	public String					Port_ReadyToUnload				= "ReadyToUnLoad";
	/**
	 * @uml.property  name="port_ReservedToUnload"
	 */
	public String					Port_ReservedToUnload			= "ReservedToUnLoad";

	/**
	 * @uml.property  name="port_InitialTransferState"
	 */
	public String					Port_InitialTransferState		= "ReadyToLoad";
	
	// PortStateName
	/**  
	 * @uml.property  name="Port_EMPTY"
	 */
	public String					Port_EMPTY						= "EMPTY";

	/**
	 * @uml.property  name="Port_FULL"
	 */
	public String					Port_FULL						= "FULL";
	/**  
	 * @uml.property  name="Port_UP"
	 */
	public String					Port_UP							= "UP";

	/**
	 * @uml.property  name="Port_DOWN"
	 */
	public String					Port_DOWN						= "DOWN";


	/*
	 * Maximum
	 */
	/**
	 * @uml.property  name="max_Priority"
	 */
	public long						Max_Priority					= 1000;
	/**
	 * @uml.property  name="max_ConsumableQuantity"
	 */
	public long						Max_ConsumableQuantity			= 1000000;
	/**
	 * @uml.property  name="max_DurableTimeUsed"
	 */
	public double					Max_DurableTimeUsed				= 1000000;
	/**
	 * @uml.property  name="max_DurableDurationUsed"
	 */
	public double					Max_DurableDurationUsed			= 1000000;
	/**
	 * @uml.property  name="max_CarrierCapacity"
	 */
	public long						Max_CarrierCapacity				= 1000;
	/**
	 * @uml.property  name="max_ProductPosition"
	 */
	public long						Max_ProductPosition				= 1000;
	/**
	 * @uml.property  name="max_ProductQuantity"
	 */
	public double					Max_ProductQuantity				= 1000;
	/**
	 * @uml.property  name="max_SubProductUnitQuantity"
	 */
	public double					Max_SubProductUnitQuantity		= 2000;
	/**
	 * @uml.property  name="max_SubProductQuantity"
	 */
	public double					Max_SubProductQuantity			= 2000;
	/**
	 * @uml.property  name="max_MaterialQuantity"
	 */
	public long						Max_MaterialQuantity;
	
	// SubjectName
	/**
	 * @uml.property  name="subject_PEX"
	 */
	public String                   Subject_PEX						= "PEX";
	/**
	 * @uml.property  name="subject_CNX"
	 */
	public String					Subject_CNX						= "CNX";
	/**
	 * @uml.property  name="subject_MCS"
	 */
	public String					Subject_MCS						= "MCS";
	/**
	 * @uml.property  name="subject_MCS"
	 */
	public String					Subject_RTD						= "RTD";
	/**
	 * @uml.property  name="subject_TEX"
	 */
	public String					Subject_TEX						= "TEX";
	/**
	 * @uml.property  name="subject_PMS"
	 */
	public String					Subject_PMS						= "PMS";
	
	//SYSTEM
	public String					DEFAULT_ACTIVE_VERSION						= "00001";
	public String					DEFAULT_FACTORY								= "ARRAY";
	
	//EVENT NAME
	public String                   EVENTNAME_REWORK                            = "Rework"; 
	public String                   EVENTNAME_STARTREWORK                       = "StartRework";
	
	//Product Type
	public String					ProductType_Sheet							= "Sheet";
	public String					ProductType_Glass							= "Glass";
	public String					ProductType_Panel							= "Panel";
	
	//Production Type
	public String					Pord_Production								= "P";
	public String					Pord_Engineering							= "E";
	public String					Pord_MQC									= "M";
	public String					Pord_Dummy									= "D";
	public String					Pord_Test									= "T";
	
	//System common
	public String					Flag_Y										= "Y";
	public String					Flag_N										= "N";
	 
	// Flag Y or N
	public String                   FLAG_Y                               	  = "Y";  
	public String                   FLAG_N                               	  = "N";  
	public String                   FLAG_Z                               	  = "Z";
		
	public String					FLAG_HOLD								  = "HOLD";

	// Durable Hold Info
	public String					DURABLE_HOLDSTATE_Y				  		  = "Y";
	public String					DURABLE_HOLDSTATE_N				  		  = "N";

	// Durable Type
	public String					DURABLETYPE_FilmBox							= "FilmBox";
	public String					DURABLETYPE_PeelingFilmBox					= "PeelingFilmBox";
	public String					DURABLETYPE_Tray					        = "Tray";
	public String					DURABLETYPE_CoverTray					    = "CoverTray";
	
	// Machine Group
	public String					MachineGroup_Sorter							= "SORTER";
	public String					MachineGroup_1stLamination					= "1stlamination";
	public String					MachineGroup_2ndLamination					= "2ndlamination";
	public String					MachineGroup_CSTCleaner						= "CSTCleaner";
	public String					MachineGroup_EVA							= "EVA";
	public String					MachineGroup_HalfCut						= "HalfCut";
	public String					MachineGroup_MaskCSTCleaner					= "MaskCSTCleaner";
	public String					MachineGroup_MaskPPA						= "MaskPPA";
	public String					MachineGroup_MaskUnpacker					= "MaskUnpacker";
	public String					MachineGroup_Photo							= "Photo";
	public String					MachineGroup_Unpacker						= "Unpacker";
	public String					MachineGroup_MaskOrgCleaner					= "MaskOrgCleaner";
	public String					MachineGroup_MaskMetalCleaner				= "MaskMetalCleaner";
	public String					MachineGroup_Oven					        = "Oven";
	public String					MachineGroup_MaskAOI					    = "MaskAOI";
	public String					MachineGroup_MaskInitialInspection		    = "MaskInitialInspection";
	public String					MachineGroup_MaskMacro					    = "MaskMacro";
	public String					MachineGroup_MaskRepair					    = "MaskRepair";  //add by cjl 20201103 for MaskTest
	public String					MachineGroup_CellSorter						= "CELLSORTER";
	public String					MachineGroup_PFL							= "PFL";
	public String					MachineGroup_Packer							= "Packer";
	public String					MachineGroup_SCRAPPACK							= "SCRAPPACK";
	public String					MachineGroup_MaskTension					= "Tension";	// 20201207	dhko
	public String					MachineGroup_AOI					    	= "AOI";
	public String					MachineGroup_MaskStocker					= "MaskStocker";	// 20201222	dhko
	public String					MachineGroup_SE					            = "SE-SR";
	
	// PortType 
	public String                   PORT_TYPE_PB                       		  = "PB";
	public String                   PORT_TYPE_PL                    		  = "PL";
	public String					PORT_TYPE_PU							  = "PU";
	public String                   PORT_TYPE_PP                              = "PP";
	
	// SlotMap
	public String                   PRODUCT_IN_SLOT                           = "O";
	public String                   PRODUCT_NOT_IN_SLOT                       = "X";
	
	//Grade
	public String                   LotGrade_G                                  = "G";
	public String                   LotGrade_P                                  = "P";
	public String                   LotGrade_R                                  = "R";
	public String                   LotGrade_S                                  = "S";
	public String					LotGrade_OK									= "OK";
	public String					LotGrade_NG									= "NG";
	
	public String                   ProductGrade_G                              = "G";
	public String                   ProductGrade_P                              = "P";
	public String                   ProductGrade_R                              = "R";
	public String                   ProductGrade_S                              = "S";
	public String                   ProductGrade_N                              = "N";
	
	public String					GradeType_Lot								= "Lot";
	public String					GradeType_Product							= "Product";
	public String					GradeType_SubProduct						= "SubProduct";
	
	public String					SORT_JOBSTATE_RESERVED						= "RESERVED";
	public String					SORT_JOBSTATE_STARTED						= "STARTED";
	public String					SORT_JOBSTATE_CONFIRMED						= "CONFIRMED";
	public String					SORT_JOBSTATE_ENDED							= "ENDED";
	public String					SORT_JOBSTATE_CANCELED						= "CANCELED";
	public String					SORT_JOBSTATE_ABORT							= "ABORT";
	public String					SORT_JOBSTATE_PREPARED						= "PREPARED";
	public String					SORT_JOBSTATE_SELECTED						= "SELECTED";
	
	public String					SORT_JOBTYPE_SPLIT							= "SPLIT";
	public String					SORT_JOBTYPE_MERGE							= "MERGE";
	public String					SORT_JOBTYPE_CHANGECST						= "CHANGECST";
	
	public String					SORT_OPERATIONMODE_SPLIT     				= "SPLIT";
	public String					SORT_OPERATIONMODE_MERGE     				= "MERGE";
	public String					SORT_OPERATIONMODE_NORMAL     				= "NORMAL";

	public String					SORT_SORTPRODUCTSTATE_READY     			= "READY";
	public String					SORT_SORTPRODUCTSTATE_EXECUTING     		= "EXECUTING";
	public String					SORT_SORTPRODUCTSTATE_COMPLETED    			= "COMPLETED";
	
	public String					SORT_TRANSFERDIRECTION_SOURCE    			= "SOURCE";
	public String					SORT_TRANSFERDIRECTION_TARGET    			= "TARGET";
	
	public String                   SORT_OLEDtoTP								= "EN_SOR";
	public String                   SORT_TPtoOLED								= "LAMI_SOR";
	public String                   SORT								        = "SORT";
	public String                   TSPSHIP								        = "TSPSHIP";
	
	public String					SORT_OPERATIONMODE							= "SORTER";
	
	//FistGlass
	public String					FIRSTGLASS_JOBSTATE_RESERVED     			= "Reserved";
	public String					FIRSTGLASS_JOBSTATE_RELEASED     			= "Released";
	public String					FIRSTGLASS_JOBSTATE_CANCELED     			= "Canceled";
	public String					FIRSTGLASS_JOBSTATE_COMPLETED     			= "Completed";
	
	
	// MCS
	//[ OIC | RTD | MCS ] 
	// CEC GVO Add
	public String					MCS_TRANSPORTJOBTYPE_OIC               = "OIC";
	public String					MCS_TRANSPORTJOBTYPE_RTD               = "RTD";
	public String					MCS_TRANSPORTJOBTYPE_MCS               = "MCS";

//	[ Requested | Accepted | Rejected | Started | Completed | Terminated ]
	
	public String					MCS_JOBSTATE_Requested           = "Requested";
	public String					MCS_JOBSTATE_Accepted            = "Accepted";
	public String					MCS_JOBSTATE_Rejected            = "Rejected";	
	public String					MCS_JOBSTATE_Started             = "Started";
	public String					MCS_JOBSTATE_Completed           = "Completed";
	public String					MCS_JOBSTATE_Terminated          = "Terminated";
	
	
    //	JOBCANCELSTATE [ Requested | Accepted | Rejected | Started | Completed | Failed ]
	public String					MCS_CANCELSTATE_Requested          	= "Requested";
	public String					MCS_CANCELSTATE_Accepted         	= "Accepted";
	public String					MCS_CANCELSTATE_Rejected         	= "Rejected";
	public String					MCS_CANCELSTATE_Started          	= "Started";
	public String					MCS_CANCELSTATE_Completed           = "Completed";
	public String					MCS_CANCELSTATE_Failed          	= "Failed";

	//	[ Requested | Accepted | Rejected | Changed | Failed ]
	public String					MCS_CHANGESTATE_Requested          	= "Requested";
	public String					MCS_CHANGESTATE_Accepted          	= "Accepted";
	public String					MCS_CHANGESTATE_Rejected          	= "Rejected";
	public String					MCS_CHANGESTATE_Changed          	= "Changed";
	public String					MCS_CHANGESTATE_Failed          	= "Failed";

	// TransportJob Event
	public String					MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREQUEST  = "RequestTransportJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_REQUESTTRANSPORTJOBREPLY    = "RequestTransportJobReply";
	public String					MCS_TRANSPORTJOBEVENT_REQUESTMASKTRANSPORTJOBREQUEST  = "RequestMaskTransportJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_REQUESTMASKTRANSPORTJOBREPLY    = "RequestMaskTransportJobReply";
	public String					MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREQUEST   = "CancelTransportJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_CANCELTRANSPORTJOBREPLY     = "CancelTransportJobReply";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTED         = "TransportJobStarted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS    = "TransportJobStartedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBTERMINATEDBYMCS = "TransportJobTerminatedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCOMPLETED       = "TransportJobCompleted";
	public String					MCS_TRANSPORTJOBEVENT_ACTIVETRANSPORTJOB       	  = "ActiveTransportJobReport";

	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELSTARTED   = "TransportJobCancelStarted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELCOMPLETED = "TransportJobCancelCompleted";
	public String					MCS_TRANSPORTJOBEVENT_TRANSPORTJOBCANCELFAILED    = "TransportJobCancelFailed";
	
	public String					MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREQUEST    = "ChangeDestinationRequest";
	public String					MCS_TRANSPORTJOBEVENT_CHANGEDESTINATIONREPLY      = "ChangeDestinationReply";
	public String					MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGED          = "DestinationChanged";
	public String					MCS_TRANSPORTJOBEVENT_DESTINATIONCHANGEFAILED     = "DestinationChangeFailed";
	public String					MCS_TRANSPORTJOBEVENT_CARRIERLOACATIONCHANGED     = "CarrierLocationChanged";

	// TransportBatchJob Event
	public String					MCS_TRANSPORTJOBEVENT_REQUESTMASKBATCHJOBREQUEST		= "RequestMaskBatchJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_REQUESTMASKBATCHJOBREPLY			= "RequestMaskBatchJobReply";
	public String					MCS_TRANSPORTJOBEVENT_CANCELMASKBATCHJOBREQUEST			= "CancelMaskBatchJobRequest";
	public String					MCS_TRANSPORTJOBEVENT_CANCELMASKBATCHJOBREPLY			= "CancelMaskBatchJobReply";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBSTARTED			= "MaskTransportJobStarted";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBSTARTEDBYMCS		= "MaskTransportJobStartedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBTERMINATEDBYMCS	= "MaskTransportJobTerminatedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCOMPLETED			= "MaskTransportJobCompleted";
	public String					MCS_TRANSPORTJOBEVENT_ACTIVEMASKTRANSPORTJOBREPORT		= "ActiveMaskTransportJobReport";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELSTARTED		= "MaskTransportJobCancelStarted";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELCOMPLETED	= "MaskTransportJobCancelCompleted";
	public String					MCS_TRANSPORTJOBEVENT_MASKTRANSPORTJOBCANCELFAILED		= "MaskTransportJobCancelFailed";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBSTARTED				= "MaskBatchJobStarted";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCOMPLETED				= "MaskBatchJobCompleted";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBSTARTEDBYMCS			= "MaskBatchJobStartedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBTERMINATEDBYMCS		= "MaskBatchJobTerminatedByMCS";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELSTARTED			= "MaskBatchJobCancelStarted";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELCOMPLETED		= "MaskBatchJobCancelCompleted";
	public String					MCS_TRANSPORTJOBEVENT_MASKBATCHJOBCANCELFAILED			= "MaskBatchJobCancelFailed";

	public String					MCS_TRANSFERSTATE_MOVING     					  = "MOVING";
	public String					MCS_TRANSFERSTATE_INSTK     					  = "INSTK";
	public String					MCS_TRANSFERSTATE_ONEQP     					  = "ONEQP";
	public String					MCS_TRANSFERSTATE_OUTSTK     					  = "OUTSTK";
	public String					MCS_TRANSFERSTATE_RESERVED     					  = "RESERVED";
	public String					MCS_TRANSFERSTATE_ONCAU     					  = "ONCAU";
	
	public String                   MCS_CARRIERSTATE_EMPTY                       	  = "EMPTY";
	public String                   MCS_CARRIERSTATE_FULL                    	  	  = "FULL";
	
	public String                   MCS_POSITIONTYPE_PORT                        	  = "PORT";
	public String                   MCS_POSITIONTYPE_SHELF                        	  = "SHELF";
	public String                   MCS_POSITIONTYPE_VEHICLE                       	  = "VEHICLE";
	public String                   MCS_POSITIONTYPE_CRANE                       	  = "CRANE";
	

	//	[ GCST: Array Sheet CST | HCST: Half Glass CST | FILM: Film CST | MASK: Mask CST ]
	public String					MCS_CARRIERTYPE_SCST							  = "SheetCST";
	public String					MCS_CARRIERTYPE_GCST							  = "GlassCST";
	public String					MCS_CARRIERTYPE_MCST							  = "MaskCST";
	public String					MCS_CARRIERTYPE_RBOX							  = "RFBOX";
	public String					MCS_CARRIERTYPE_GTRY							  = "TrayGroup";

	//	[ CLEAN | DIRTY | TENSION ]
	public String                   MCS_CLEANSTATE_CLEAN                              = "CLEAN";
	public String                   MCS_CLEANSTATE_DIRTY                    	  	  = "DIRTY";
	public String                   MCS_CLEANSTATE_TENSION                    	  	  = "TENSION";

	//Q-Time
	public String					QTIME_STATE_IN									= "Entered";
	public String					QTIME_STATE_OUT									= "Exited";
	public String					QTIME_STATE_WARN								= "Warning";
	public String					QTIME_STATE_OVER								= "Interlocked";
	public String					QTIME_STATE_CONFIRM								= "Resolved";
	public String					QTIME_DEPART									= "TrackOut";
	public String					QTIME_TARGET									= "TrackIn";
	public String					QTIME_ACTION_HOLD								= "Hold";
	public String					QTIME_ACTION_REWORK								= "Rework";
	
	//OLED Mask Kind
	public String					OLEDMaskKind_EVA								= "EVA";
	public String					OLEDMaskKind_TFE								= "TFE";
	
	//EVAMask
	public String					EVAMask											= "EVAMask";
	public String					EVAMaskCST_TRANSFERSTATE_ONEQP					= "EVAMaskCST_TRANSFERSTATE_ONEQP";
	
	//MaskSpec
	public String					Generation										= "6";
	
	//Sheet
	public String					CreateState										= "Created";
	public String					SheetState_Attached								= "Attached";
	
	//MaskLot
	public String					MaskLotState_Released							= "Released";
	public String					MaskLotState_Created							= "Created";
	public String					MaskLotState_Scrapped							= "Scrapped";
	public String					MaskLotState_Shipped							= "Shipped";
	public String					MaskLotProcessState_Wait						= "WAIT";
	public String					MaskLotProcessState_Run							= "RUN";
	public String					MaskLotHoldState_OnHold							= "Y";
	public String					MaskLotHoldState_NotOnHold						= "N";
	public String					MaskLotReworkState_InRework						= "InRework";
	public String					MaskLotReworkState_NotInRework					= "NotInRework";
		
	//Material
	public String					MaterialType_Sheet								= "Sheet";
	public String					MaterialType_Frame								= "Frame";
	public String					MaterialType_Ink								= "Ink";
	public String					MaterialType_PR									= "PR";
	public String					MaterialType_Pallet								= "Pallet";
	public String					MaterialType_WorkTable							= "WorkTable";
	public String					MaterialType_FPC								= "FPC";
	public String					MaterialType_Mask								= "Mask";
	public String					MaterialType_PhotoMask							= "PhotoMask";
	public String					MaterialType_Film								= "FILM";	
	public String					MaterialType_Crucible							= "Crucible";
	public String					MaterialType_PI									= "PI";
	public String					MaterialType_Organicadhesive					= "Organic Adhesive";
	public String					MaterialType_AdhesiveAgent					    = "Adhesive Agent";
	public String					MaterialType_Target					   			= "Target";
	public String					MaterialType_TopLamination					   	= "TopLamination";
	public String					MaterialType_BottomLamination					= "BottomLamination";
	public String					MaterialType_PatternFilm					   	= "PatternFilm";
	public String					MaterialType_PalletJig					   		= "PalletJig";
	
	
	//PR DetailType
	public String					DetailMaterialType_OrganicGlue					= "OrganicGlue";
	public String					DetailMaterialType_PhotoGlue					= "PhotoGlue";
	
	//PR DetailType
	public String					DetailMaterialState_Thaw						= "Thaw";
	public String					DetailMaterialState_Freeze						= "Freeze";

	//Insert Message Log Flag
	public String					INSERT_LOG_NONE	 			                	= "NONE";
	public String					INSERT_LOG_MESSAGE	 		            		= "MSG";
	public String					INSERT_LOG_ERROR	 		            		= "ERR";
	public String					INSERT_LOG_TRANSATION	 		        		= "TRX";
		
	public String					INSERT_LOG_TYPE_RECEIVE	 		        		= "RECV";
	public String					INSERT_LOG_TYPE_SEND	 		        		= "SEND";
	
	//Reservation
	public String					RESV_STATE_CREATE								= "Created";
	public String					RESV_STATE_RESV									= "Reserved";
	public String					RESV_STATE_START								= "Executing";
	public String					RESV_STATE_END									= "Completed";
	
	//ProductRequestPlan State
	public String					PRQPLAN_STATE_CREATED                    		= "Created";
	public String					PRQPLAN_STATE_RELEASED                    		= "Released";
	public String					PRQPLAN_STATE_COMPLETED                  		= "Completed";
	public String					PRQPLAN_STATE_STARTED                    		= "Started";
	
	//ProductRequestPlan HoldState
	public String					PRQPLAN_HOLDSTATE_ONHOLD						= "Y";
	public String					PRQPLAN_HOLDSTATE_NOTONHOLD						= "N";
	
	public String                   OAInit                                          = "0";
	public String                   OAProcess                                       = "1";
	public String                   OASuccess                                       = "2";
	public String                   OAReturn                                        = "3";
	
	// Material Kind
	public String                   MaterialKind_Consumable                         = "Consumable";
	public String                   MaterialKind_Durable                            = "Durable";
	
	
	//SYSTEM
	public String					DetailMachineType_Main							= "MAIN";
	public String					DetailMachineType_Unit							= "UNIT";
	public String					DetailMachineType_SubUnit						= "SUBUNIT";
	public String					DetailMachineType_SubSubUnit					= "SUBSUBUNIT";
	
	//RECIPE TYPE
	public String					RECIPETYPE_EQP							= "E";
	public String					RECIPETYPE_UNIT							= "U";
	public String					RECIPETYPE_SUBUNIT						= "S";
	
	//RECIPE RESULT
	public String					RECIPECHECK_OK							= "OK";
	public String					RECIPECHECK_NG							= "NG";
	public String					RECIPECHECK_SKIP						= "SKIP";
	
	//RECIPE STATE
	public String					RECIPESTATE_CREATED						= "Created";
	public String					RECIPESTATE_APPROVED					= "Approved";
	public String					RECIPESTATE_UNAPPROVED					= "Unapproved";
	
	//RECIPE ACK
	public String					RECIPEREP_ACK_OK						= "OK";	//Registered
	public String					RECIPEREP_ACK_RNG						= "RNG";	//Registered NG
	public String					RECIPEREP_ACK_VNG						= "VNG";	//mismatch Version
	public String					RECIPEREP_ACK_NV						= "NV";	//not used version

	//RECIPE ACK
	public String					CST_TYPE_ARRAY							= "SheetCST";
	public String					CST_TYPE_BUFFER							= "BufferCST";
	public String					CST_TYPE_OLED							= "OLEDGlassCST";
	public String					CST_TYPE_OLEDFilm						= "FilmCST";
	public String					CST_TYPE_TPNORMAL						= "TPGlassCST";

	// MailServerAddress - smtp.263.net
	public String					MailServerAddress						= "smtp.263.net";
	
	public String					FUTUREACTIONNAME_SKIP					= "skip";
	public String					FUTUREACTIONNAME_HOLD					= "hold";
	
	// Add FilmStockerName
	public String					Stocker_FilmBox							= "3FSTK01";
	
	// SAP 
	public String					SAPFactoryCode_5001							= "5001";
	public String					SAPFactoryCode_5099							= "5099";
	public String					SAPFactoryCode_5003							= "5003";

	
	public String					SAPFactoryPosition_3F03							= "3F03";
	public String					SAPFactoryPosition_3F01							= "3F01";
	public String					SAPFactoryPosition_9F91							= "9F91";
	public String					SAPFactoryPosition_9F99						    = "9F99";
	public String					SAPFactoryPosition_3F91					        = "3F91";
	public String					SAPFactoryPosition_9F93					        = "9F93";

	// BCR Tray
	public long						BCRTrayCapacity							= 120;
	
	/**
	 * @uml.property  name="constantDefsMap"
	 */
	private Map<String, String>	constantDefsMap					= new HashMap<String, String>();

	/**
	 * @uml.property  name="ac"
	 * @uml.associationEnd   
	 */
	private ApplicationContext		ac;

	public ConstantMap()
	{

	}
	
	public String getEventName(String eventNameConst){
		try {
			Field filed = ConstantMap.class.getDeclaredField(eventNameConst);
			filed.setAccessible(true);
			return filed.get(this).toString();
		} catch (SecurityException e) {
			return "";
		} catch (NoSuchFieldException e) {
			return "";
		} catch (IllegalArgumentException e) {
			return "";
		} catch (IllegalAccessException e) {
			return "";
		}
	}
	
	public Map<String, String> getConstantDefsMap()
	{
		return kr.co.aim.greentrack.generic.GenericServiceProxy.getConstantMap().getConstantDefsMap();
	}

//	@Override
//	public void setApplicationContext(ApplicationContext arg0)
//			throws BeansException
//	{
//		this.ac = arg0;
//	}

	@Override
	public void afterPropertiesSet()
			throws Exception
	{
		load();
	}

	public void load()
	{
		constantDefsMap.clear();
		Map<String,String> memoryDataMap = null;
		
		constantDefsMap =  kr.co.aim.greentrack.generic.GenericServiceProxy.getConstantMap().getConstantDefsMap();
		
		
		log.info("ConstantDef load resultList :" + constantDefsMap.size());

		Field[] fields = ConstantMap.class.getDeclaredFields();
		for ( Field field : fields )
		{
			setValue(constantDefsMap, field);
		}
	}

//	private void setValue(java.util.Map<String, String> constantDefs, Field field)
//	{
//		String constantValue = constantDefs.get(field.getName());
//
//		if ( constantValue != null )
//		{
//			try
//			{
//				ObjectUtil.copyFieldValue(field, this, constantValue);
//			}
//			catch ( IllegalArgumentException e )
//			{
//				log.error(e, e);
//			}
//			catch ( Exception e )
//			{
//				log.error(e, e);
//			}
//		}
//	}


}
