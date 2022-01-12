package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.durable.service.DurableServiceImpl;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.durable.management.info.MakeInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import sun.net.www.content.text.Generic;

public class TrayGroupManagement extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);

		String coverDurableType = SMessageUtil.getBodyItemValue(doc, "DURABLETYPE", true);

		String coverPosition = SMessageUtil.getBodyItemValue(doc, "COVERPOSITION", false);

		List<Element> trayListEl = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		DurableSpec coverTraySpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(coverTrayData.getFactoryName(), coverTrayData.getDurableSpecName(),
				coverTrayData.getDurableSpecVersion());
		
		CommonValidation.CheckDurableState(coverTrayData);

		long totalLotQty = 0;

		// Assign Case
		if (coverDurableType.equals("Tray"))
		{
			String operationMode = coverTrayData.getUdfs().get("OPERATIONMODE");
			CommonValidation.checkAvailableCst(coverTrayData);//caixu 2020/11/12 check CoverTray State
			eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", getEventUser(), getEventComment(), null, null);

			for (Element element : trayListEl)
			{
				String trayName = element.getChildText("DURABLENAME");
				String position = element.getChildText("POSITION");

				Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				CommonValidation.CheckDurableState(trayData);
				
				if(!operationMode.equals(trayData.getUdfs().get("OPERATIONMODE")))
				{
					//CUSTOM-0007: Different operationmode in tray group.[TrayName = {0} ,OperationMode = {1}]
					new CustomException("CUSTOM-0007" ,trayData.getKey().getDurableName(),operationMode);
				}

				operationMode =  trayData.getUdfs().get("OPERATIONMODE");
				totalLotQty += trayData.getLotQuantity();

				if (trayData.getLotQuantity() == 0)
				{
					trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				}
				else
				{
					trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
				}
				DurableServiceProxy.getDurableService().update(trayData);

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("COVERNAME", coverName);
				setEventInfo.getUdfs().put("POSITION", position);
				setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
			}

			coverTrayData.setDurableType("CoverTray");
			coverTrayData.setLotQuantity(totalLotQty);
			coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			DurableServiceProxy.getDurableService().update(coverTrayData);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("COVERNAME", coverName);
			setEventInfo.getUdfs().put("POSITION", String.valueOf((Integer.parseInt(coverPosition) + 1)));
			setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
			DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);

		}
		else
		{
			// Deassign Case
			if (trayListEl.equals(null) || trayListEl.size() == 0)
			{
				eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment(), null, null);

				List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(coverName);

				for (Durable durable : trayList)	
				{
					String trayName = durable.getKey().getDurableName();

					Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
					CommonValidation.CheckDurableState(trayData);

					if (trayData.getLotQuantity() == 0)
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					else
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					}
					DurableServiceProxy.getDurableService().update(trayData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("COVERNAME", "");
					setEventInfo.getUdfs().put("POSITION", "");
					setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
				}

				coverTrayData.setDurableType("Tray");
				coverTrayData.setLotQuantity(0);
				coverTrayData.setCapacity(coverTraySpecData.getDefaultCapacity());
				coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				DurableServiceProxy.getDurableService().update(coverTrayData);

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("COVERNAME", "");
				setEventInfo.getUdfs().put("POSITION", "");
				setEventInfo.getUdfs().put("BCRFLAG", "N");
				setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
				DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);
			}
			else
			{
				List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(coverName);

				StringBuilder sql = new StringBuilder();
				List<String> bindDurList = new ArrayList<String>();
				Map<String, Object> bindMap = new HashMap<String, Object>();
				sql.append("SELECT D.DURABLENAME ");
				sql.append("  FROM DURABLE D ");
				sql.append(" WHERE D.COVERNAME = :COVERNAME ");
				sql.append("   AND D.DURABLENAME NOT IN (:TRAYLIST) ");
				sql.append("   AND D.DURABLETYPE = 'Tray' ");

				for (Element element : trayListEl)
				{
					bindDurList.add(element.getChildText("DURABLENAME"));
				}
				bindMap.put("TRAYLIST", bindDurList);
				bindMap.put("COVERNAME", coverName);

				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				// Deassign TrayGroup
				eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment(), null, null);

				for (Map<String, Object> durable : result)
				{
					String trayName = durable.get("DURABLENAME").toString();

					Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
					CommonValidation.CheckDurableState(trayData);

					if (trayData.getLotQuantity() == 0)
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					else
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					}
					DurableServiceProxy.getDurableService().update(trayData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("COVERNAME", "");
					setEventInfo.getUdfs().put("POSITION", "");
					setEventInfo.getUdfs().put("DURABLETYPE1",  "Tray");
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
				}

				// Assign TrayGroup
				String operationMode = coverTrayData.getUdfs().get("OPERATIONMODE");
				eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", getEventUser(), getEventComment(), null, null);
				for (Element element : trayListEl)
				{
					String trayName = element.getChildText("DURABLENAME");
					String position = element.getChildText("POSITION");

					Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
					
					if(!operationMode.equals(trayData.getUdfs().get("OPERATIONMODE")))
					{
						//CUSTOM-0007: Different operationmode in tray group.[TrayName = {0} ,OperationMode = {1}]
						new CustomException("CUSTOM-0007" ,trayData.getKey().getDurableName(),operationMode);
					}

					operationMode =  trayData.getUdfs().get("OPERATIONMODE");
					
					totalLotQty += trayData.getLotQuantity();
					if (trayData.getLotQuantity() == 0)
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					else
					{
						trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					}
					DurableServiceProxy.getDurableService().update(trayData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("COVERNAME", coverName);
					setEventInfo.getUdfs().put("POSITION", position);
					setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
				}

				coverTrayData.setLotQuantity(totalLotQty);
				DurableServiceProxy.getDurableService().update(coverTrayData);

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("COVERNAME", coverName);
				setEventInfo.getUdfs().put("POSITION", String.valueOf((Integer.parseInt(coverPosition) + 1)));
				setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
				DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);
			}
		}

		return doc;
	}

}
