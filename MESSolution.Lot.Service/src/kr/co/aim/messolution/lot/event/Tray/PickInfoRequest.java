package kr.co.aim.messolution.lot.event.Tray;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

public class PickInfoRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(PickInfoRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

			this.setReplyItem(doc);
   
			List<Map<String, Object>> lotMapList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
			String operationName = ConvertUtil.getMapValueByName(lotMapList.get(0),"PROCESSOPERATIONNAME");
			String operationVersion = ConvertUtil.getMapValueByName(lotMapList.get(0), "PROCESSOPERATIONVERSION");
			
			List<SVIPickInfo> pickDataList = ExtendedObjectProxy.getSVIPickInfoService().getAvailableDataListByMO(machineName, operationName, operationVersion, false);

			if (pickDataList != null && pickDataList.size() > 0)
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), this.getEventUser(), this.getEventComment());

				for (SVIPickInfo dataInfo : pickDataList)
				{
					this.generateBodyTemplate(doc, dataInfo);

					dataInfo.setDownLoadFlag("Y");
					ExtendedObjectProxy.getSVIPickInfoService().modify(eventInfo, dataInfo);
				}
			}
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		return doc;
	}
	
	private Document setReplyItem(Document doc) throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "PickInfoReply");
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).addContent(new Element("PICKLIST"));

		return doc;
	}
	
	private void generateBodyTemplate(Document doc,SVIPickInfo dataInfo)
	{
		Element pickListElment = XmlUtil.getChild(doc.getRootElement().getChild(SMessageUtil.Body_Tag), "PICKLIST", false);

		if (pickListElment == null)
		{
			pickListElment = new Element("PICKLIST");
			doc.getRootElement().getChild(SMessageUtil.Body_Tag).addContent(pickListElment);
		}

		Element pickElement = new Element("PICK");

		pickElement.addContent(new Element("TIMEKEY").setText(dataInfo.getTimeKey()));
		pickElement.addContent(new Element("JUDGE").setText(dataInfo.getJudge()));
		pickElement.addContent(new Element("CODE").setText(dataInfo.getCode()));
		pickElement.addContent(new Element("QUANTITY").setText(dataInfo.getQuantity() == null ? "0" : dataInfo.getQuantity().toString()));
		pickElement.addContent(new Element("PRODUCTSPECNAME").setText(dataInfo.getProductSpecName()));
		pickElement.addContent(new Element("WORKORDER").setText(dataInfo.getWorkOrder()));
		pickElement.addContent(new Element("POINT").setText(dataInfo.getPoint()));

		pickListElment.addContent(pickElement);
	}
}