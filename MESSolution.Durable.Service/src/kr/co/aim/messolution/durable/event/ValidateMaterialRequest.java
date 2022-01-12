package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ValidateMaterialRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		/*
		 * MACHINENAME String UNITNAME String MATERIALNAME String MATERIALTYPE String [ INK | PR | PALLET | WORKTABLE | FPC | PHOTOMASK ] Consumable : INK, PR Durable : PALLET, WORKTABLE, FPC,
		 * PHOTOMASK PHOTOMASK : Do not check
		 */
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);

		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ValidateMaterialReply");
		this.generateBodyTemplate(SMessageUtil.getBodyElement(doc));

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Consumable consumableData = null;
		Durable durableData = null;

		try
		{
			consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
			materialType = consumableData.getConsumableType();
		}
		catch (Exception e)
		{
		}

		try
		{
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
			materialType = durableData.getDurableType();
		}
		catch (Exception e)
		{
		}

		if (consumableData == null && durableData == null)
		{
			throw new CustomException("MATERIAL-0001", materialName);
		}

		SMessageUtil.setBodyItemValue(doc, "MATERIALTYPE", materialType);

		//validateMaterialTypeByMachineGroup(machineData, materialType);

		// Check material
		if (consumableData != null)
		{
			// 1. INK
			// 1.1 Reserve State validation
			// 1.2 MaterialStockTime (Duration) Validation
			if (StringUtils.equalsIgnoreCase(consumableData.getConsumableType(), GenericServiceProxy.getConstantMap().MaterialType_Ink))
			{
				// R : Reserved, Y : Kitted, N or Null : Not Kitted
				if (!consumableData.getUdfs().get("MACHINENAME").toString().equals(machineName) || !consumableData.getUdfs().get("LOADFLAG").toString().equals("R"))
				{
					throw new CustomException("MATERIAL-9013", materialName, machineName);
				}

			}

			// 2. PR
			// 2.1 ThawTime validation
			if (StringUtils.equalsIgnoreCase(consumableData.getConsumableType(), GenericServiceProxy.getConstantMap().MaterialType_PR))
			{
				// ThawTime
				if (MESConsumableServiceProxy.getConsumableServiceUtil().checkDurationLimit(consumableData, "THAWTIME") == false)
				{
					throw new CustomException("MATERIAL-9014", materialName);
				}
			}

			SMessageUtil.setBodyItemValue(doc, "QUANTITY", String.valueOf((int) consumableData.getQuantity()));
		}
		else if (durableData != null)
		{
			// Check common state
			if (durableData.getDurableCleanState().equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
			{
				throw new CustomException("MATERIAL-9015", materialName); // Durable state is dirty
			}

			/*
			if (!durableData.getUdfs().get("MACHINENAME").toString().equals(machineName) && durableData.getMaterialLocationName().toString().equals(unitName))
			{
				throw new CustomException("MATERIAL-9013", materialName, machineName);
			}
			*/
			
			if ( StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_PhotoMask))
			{
				if( StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
				{
					throw new CustomException("DURABLE-0007");
				}
				
				if( StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
				{
					throw new CustomException("DURABLE-0006");
				}
			}

			SMessageUtil.setBodyItemValue(doc, "QUANTITY", "1");
		}

		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK");
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "");

		return doc;
	}

	private void validateMaterialTypeByMachineGroup(Machine machineData, String materialType) throws CustomException
	{
		boolean invalidMaterialType = false;
		String machineGroup = machineData.getMachineGroupName();

		if (CommonUtil.equalsIn(machineGroup, GenericServiceProxy.getConstantMap().MachineGroup_Photo) && StringUtils.equals(machineData.getFactoryName(), "ARRAY"))
		{
			if (!CommonUtil.equalsIn(materialType, GenericServiceProxy.getConstantMap().MaterialType_PhotoMask, GenericServiceProxy.getConstantMap().MaterialType_PR))
			{
				invalidMaterialType = true;
			}
		}
		else if (StringUtils.equalsIgnoreCase(machineGroup, GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			if (!CommonUtil.equalsIn(materialType, GenericServiceProxy.getConstantMap().MaterialType_Ink, GenericServiceProxy.getConstantMap().DetailMaterialType_OrganicGlue,
					GenericServiceProxy.getConstantMap().MaterialType_Crucible))
			{
				invalidMaterialType = true;
			}
		}

		if (invalidMaterialType)
		{
			throw new CustomException("MACHINE-0028", materialType, machineData.getKey().getMachineName());
		}
	}

	private Element generateBodyTemplate(Element bodyElement) throws CustomException
	{
		XmlUtil.addElement(bodyElement, "QUANTITY", StringUtil.EMPTY);
		XmlUtil.addElement(bodyElement, "RESULT", "NG");
		XmlUtil.addElement(bodyElement, "RESULTDESCRIPTION", StringUtil.EMPTY);

		return bodyElement;
	}
}
