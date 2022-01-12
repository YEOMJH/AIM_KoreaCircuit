package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
public class ModifyOffsetAlign extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPEC", true);
		String factoryName= SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);		
		List<Element> alignList = SMessageUtil.getBodySequenceItemList(doc, "ALIGNLIST", true);
		String productSpecVersion="00001";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyOffsetAlign", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		List<Map<String, Object>> OffsetAlign = getOffsetAlign(factoryName,productSpec,productSpecVersion);
		
		if(OffsetAlign != null && OffsetAlign.size() > 0)
		{
		    for (Element align : alignList)
			{
				String layerName = align.getChildText("LAYERNAME");
				String alignLayer1 = align.getChildText("ALIGNLAYER1");
				String alignLayer2 = align.getChildText("ALIGNLAYER2");
				String alignLayer3 = align.getChildText("ALIGNLAYER3");			
				String alignLayer4 = align.getChildText("ALIGNLAYER4");			
				String alignLayer5 = align.getChildText("ALIGNLAYER5");
				String alignLayer6 = align.getChildText("ALIGNLAYER6");
				String mainLayerStep = align.getChildText("MAINLAYERSTEP");	
				
				OffsetAlignInfo offsetAlignInfo= ExtendedObjectProxy.getOffsetAlignInfoService().selectByKey(true, new String[]{factoryName,productSpec,productSpecVersion,layerName});
		
				if(alignLayer1.equals("False"))
				{
					alignLayer1="X";
				}
				else 
				{
					alignLayer1="O";
				}
				
				if(alignLayer2.equals("False"))
				{
					alignLayer2="X";
				}
				else 
				{
					alignLayer2="O";
				}
				
				if(alignLayer3.equals("False"))
				{
					alignLayer3="X";
				}
				else 
				{
					alignLayer3="O";
				}
				
				if(alignLayer4.equals("False"))
				{
					alignLayer4="X";
				}
				else 
				{
					alignLayer4="O";
				}
				
				if(alignLayer5.equals("False"))
				{
					alignLayer5="X";
				}
				else 
				{
					alignLayer5="O";
				}
				
				if(alignLayer6.equals("False"))
				{
					alignLayer6="X";
				}
				else 
				{
					alignLayer6="O";
				}
				
				offsetAlignInfo.setProductSpecName(productSpec);
				offsetAlignInfo.setFactoryName(factoryName);
				offsetAlignInfo.setLayerName(layerName);
				offsetAlignInfo.setAlignLayer1(alignLayer1);
				offsetAlignInfo.setAlignLayer2(alignLayer2);
				offsetAlignInfo.setAlignLayer3(alignLayer3);
				offsetAlignInfo.setAlignLayer4(alignLayer4);
				offsetAlignInfo.setAlignLayer5(alignLayer5);
				offsetAlignInfo.setAlignLayer6(alignLayer6);
				offsetAlignInfo.setMainLayerStep(mainLayerStep);
				    		
				offsetAlignInfo = ExtendedObjectProxy.getOffsetAlignInfoService().modify(eventInfo, offsetAlignInfo);	
		    }
		}
		else 
		{
			 for (Element align : alignList)
				{
					String layerName = align.getChildText("LAYERNAME");
					String alignLayer1 = align.getChildText("ALIGNLAYER1");
					String alignLayer2 = align.getChildText("ALIGNLAYER2");
					String alignLayer3 = align.getChildText("ALIGNLAYER3");			
					String alignLayer4 = align.getChildText("ALIGNLAYER4");			
					String alignLayer5 = align.getChildText("ALIGNLAYER5");
					String alignLayer6 = align.getChildText("ALIGNLAYER6");
					String mainLayerStep = align.getChildText("MAINLAYERSTEP");	
					
					if(alignLayer1.equals("False"))
					{
						alignLayer1="X";
					}
					else 
					{
						alignLayer1="O";
					}
					
					if(alignLayer2.equals("False"))
					{
						alignLayer2="X";
					}
					else 
					{
						alignLayer2="O";
					}
					
					if(alignLayer3.equals("False"))
					{
						alignLayer3="X";
					}
					else 
					{
						alignLayer3="O";
					}
					
					if(alignLayer4.equals("False"))
					{
						alignLayer4="X";
					}
					else 
					{
						alignLayer4="O";
					}
					
					if(alignLayer5.equals("False"))
					{
						alignLayer5="X";
					}
					else 
					{
						alignLayer5="O";
					}
					
					if(alignLayer6.equals("False"))
					{
						alignLayer6="X";
					}
					else 
					{
						alignLayer6="O";
					}
					
					OffsetAlignInfo offsetAlignInfo= new OffsetAlignInfo();
					
					offsetAlignInfo.setProductSpecName(productSpec);
					offsetAlignInfo.setProductSpecVersion(productSpecVersion);
					offsetAlignInfo.setFactoryName(factoryName);
					offsetAlignInfo.setLayerName(layerName);
					offsetAlignInfo.setAlignLayer1(alignLayer1);
					offsetAlignInfo.setAlignLayer2(alignLayer2);
					offsetAlignInfo.setAlignLayer3(alignLayer3);
					offsetAlignInfo.setAlignLayer4(alignLayer4);
					offsetAlignInfo.setAlignLayer5(alignLayer5);
					offsetAlignInfo.setAlignLayer6(alignLayer6);
					offsetAlignInfo.setMainLayerStep(mainLayerStep);
					    		
					offsetAlignInfo = ExtendedObjectProxy.getOffsetAlignInfoService().create(eventInfo, offsetAlignInfo);				
				}
		}
	    return doc;	
	}
	
	private List<Map<String, Object>> getOffsetAlign(String factoryName,String productSpec,String productSpecVersion) throws CustomException
	{

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM CT_OFFSETALIGNINFO   "
				+ "WHERE FACTORYNAME = :FACTORYNAME AND PRODUCTSPECNAME = :PRODUCTSPECNAME AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpec);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		 List<Map<String, Object>> offsetAlign = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		
		return offsetAlign;
	}
	
	
}
