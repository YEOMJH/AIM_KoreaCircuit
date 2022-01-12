package kr.co.aim.messolution.lot.event.CNX.PostCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstOnlineProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;


import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class LamiCheckResult  extends SyncHandler{
	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);
	String MVIUser ="";
	String mura ="";
	String line ="";
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if (eleBody != null){
			
			String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
			List<Element> ucList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), null, null);

			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false)){
					
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				String LT = SMessageUtil.getChildText(eleLot, "LT", false);
				String LB = SMessageUtil.getChildText(eleLot, "LB", false);
				String RT = SMessageUtil.getChildText(eleLot, "RT", false);
				String RB = SMessageUtil.getChildText(eleLot, "RB", false);
				String productType = SMessageUtil.getChildText(eleLot, "PRODUCTTYPE", false);
				String surFaceFlag = SMessageUtil.getChildText(eleLot, "SURFACEFLAG", false);
				//String checkTime = SMessageUtil.getChildText(eleLot, "CHECKTIME", false);						

				FirstOnlineProduct firstOnlineProduct = ExtendedObjectProxy.getFirstOnlineProductService().selectByKey(false, new String[]{lotName});
				firstOnlineProduct.setLotName(lotName);
				if(eventName.equals("LamiCheck"))
				{
					firstOnlineProduct.setLT(LT);
					firstOnlineProduct.setLB(LB);
					firstOnlineProduct.setRB(RB);
					firstOnlineProduct.setRT(RT);
					firstOnlineProduct.setProductType(productType);
					firstOnlineProduct.setSurfaceFlag(surFaceFlag);
					firstOnlineProduct.setSurfaceUser(eventInfo.getEventUser());
					 MVIUser ="Null";
					 mura ="N";
					 line ="N";
					addMVIResult(lotName);
					firstOnlineProduct.setLineFlag(line);
					firstOnlineProduct.setMuraFlag(mura);
					firstOnlineProduct.setMVIUser(MVIUser);
				}
				else
				{
					
					firstOnlineProduct.setCheckUser(eventInfo.getEventUser());
					firstOnlineProduct.setCheckTime(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT));
				}
				
			firstOnlineProduct.setLastEventComment(eventInfo.getEventComment());
			firstOnlineProduct.setLastEventName(eventInfo.getEventName());
			firstOnlineProduct.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
			firstOnlineProduct.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			firstOnlineProduct.setLastEventUser(eventInfo.getEventUser());
			
			firstOnlineProduct = ExtendedObjectProxy.getFirstOnlineProductService().modify(eventInfo, firstOnlineProduct);
			
			}
			
		}
				
		return doc;
		
	}
	
	private void addMVIResult(String lotName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT E.DEFECTCODE,L.EVENTUSER,JND.SUPERDEFECTCODE AS JNDCODE, CODE.SUPERDEFECTCODE AS CODE  ");
		sql.append("  FROM  LOTHISTORY L,CT_MVIELECTRICALINSPECTION  E, CT_MVIJNDDEFECTCODE  JND,CT_MVIJNDDEFECTCODE  CODE ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND  L.LOTNAME = E.PANELNAME ");
		sql.append("           AND  L.TIMEKEY = E.LASTEVENTTIMEKEY ");
		sql.append("           AND  L.PRODUCTSPECNAME = JND.PRODUCTSPECNAME ");
		sql.append("           AND L.PRODUCTSPECNAME = CODE.PRODUCTSPECNAME ");
		sql.append("           AND E.DEFECTCODE = CODE.DEFECTCODE(+) ");
		sql.append("           AND E.DEFECTCODE = JND.DEFECTCODE(+) ");
		sql.append("           AND L.LOTNAME=:LOTNAME ");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (!result.isEmpty()) {
				
				for(int i=0;i<result.size();i++){
					if (result.get(i).get("JNDCODE").toString().equals("Mura Type")||result.get(i).get("CODE").toString().equals("Mura Type")) {
						mura="Y";
					}
					if (result.get(0).get("JNDCODE").toString().equals("Line Type")||result.get(i).get("CODE").toString().equals("Line Type")) {
						line="Y";
					}
				}
				MVIUser=result.get(0).get("EVENTUSER").toString();
			}
	}
			

}
