package kr.co.aim.messolution.generic.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.master.ErrorDefMap;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.object.POSPolicyAttributeDefinition;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.policy.PolicyServiceProxy;
import kr.co.aim.greentrack.policy.management.data.PolicyData;
import kr.co.aim.greentrack.policy.management.data.PolicyDataList;
import kr.co.aim.greentrack.policy.management.info.ConditionInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class POSConditionValueGetter 
{
	private static Log 		   log = LogFactory.getLog(ErrorDefMap.class);

	public static List<PolicyData> 
	                 getPoliciesByLotCondition(String objectType,
	                		 				   Object objectData,
	                		 				   String policyName) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		List<ConditionInfo> cndInfoList = getConditionInfoSeq(objectType, objectData, policyName);

		PolicyDataList policyDList = PolicyServiceProxy.getPOSPolicyService().allConditionsByPolicy( policyName, cndInfoList );
		
		return policyDList.getPolicyD();
	}

	public static List<ConditionInfo> getConditionInfoSeq(String objectType,
	                		 				       Object objectData,
	                		 				       String policyName) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		Field decleardField = null;
		
		List<ObjectAttributeDef> stdAttrDefs =
			greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(objectType, "Standard");

		List<ObjectAttributeDef> extCAttrDefs =
			greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(objectType, "ExtendedC");
		
		List<POSPolicyAttributeDefinition> posAttrDefsList = 
			GenericServiceProxy.getPosPolicyMap().getPolicyDefMap().get(policyName).getAttributeNames();
		
		List<ConditionInfo> cndInfoList = new ArrayList<ConditionInfo>(stdAttrDefs.size() + extCAttrDefs.size());
		
		Lot lotData = null;
		Product prodData = null;
		
	
		if ( objectData instanceof  Lot)
		{
			lotData = (Lot)objectData;
		}
		else if ( objectData instanceof Product ) 
		{
			prodData = (Product)objectData;
		}	
		
		ArrayList<ObjectAttributeDef> newObjectAttrDefs = new ArrayList<ObjectAttributeDef>();
		
		
		for ( int i = 0; i < stdAttrDefs.size(); i++ )
		{
			boolean isFound = false;
			
			for ( int j = 0; j < posAttrDefsList.size(); j++ )
			{
				if ( stdAttrDefs.get(i).getAttributeName().equals(posAttrDefsList.get(j).getAttributeName()))
				{
					isFound = true;
					break;
				}
			}
			
			if ( !isFound )
			{
				newObjectAttrDefs.add(stdAttrDefs.get(i));
			}			
						
		}
		
		for ( int i = 0; i <  extCAttrDefs.size(); i++ )
		{
			boolean isFound = false;
			
			for ( int j = 0; j < posAttrDefsList.size(); j++ )
			{
				if ( extCAttrDefs.get(i).getAttributeName().equals(posAttrDefsList.get(j).getAttributeName()))
				{		
					isFound = true;
					break;
				}				
			}

			if ( !isFound )
			{
				newObjectAttrDefs.add(extCAttrDefs.get(i));
			}

		}		
		

		if ( newObjectAttrDefs != null )
		{
			for ( int i = 0; i < newObjectAttrDefs.size(); i++ )
			{
				ConditionInfo conditionInfo = new ConditionInfo();
				
				conditionInfo.setName(newObjectAttrDefs.get(i).getAttributeName());
				conditionInfo.setType(newObjectAttrDefs.get(i).getAttributeType());
				
				if ( objectData instanceof Lot )
				{
					if ( conditionInfo.getName().equals("lotName"))
					{
						continue;
					}			
					if ( newObjectAttrDefs.get(i).getAttributeType().equals("ExtendedC"))
					{
						conditionInfo.setValue(lotData.getUdfs().get(conditionInfo.getName()));
					}
					else
					{
						decleardField = lotData.getClass().getDeclaredField(conditionInfo.getName());
						decleardField.setAccessible(true);
						
						String value = "";
						if(decleardField.get(lotData) != null){
							value = decleardField.get(lotData).toString();
						}
						
						conditionInfo.setValue(value);
					}
				}
				else if ( objectData instanceof Product ) 
				{
					if ( conditionInfo.getName().equals("productName"))
					{
						continue;
					}
					if ( newObjectAttrDefs.get(i).getAttributeType().equals("ExtendedC"))
					{
						conditionInfo.setValue(prodData.getUdfs().get(conditionInfo.getName()));
					}
					else
					{	
						decleardField = prodData.getClass().getDeclaredField(conditionInfo.getName());
						decleardField.setAccessible(true);
						
						String value = "";
						if(decleardField.get(prodData) != null){
							value = decleardField.get(prodData).toString();
						}
						
						conditionInfo.setValue(value);
					}
				}
				cndInfoList.add(conditionInfo);
				
			}
		}		
		
		return cndInfoList;
		
	}
	 
	
		
}
