package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeProductComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String sGlassComment = SMessageUtil.getBodyItemValue(doc, "GLASSCOMMENT", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeGlassComment", this.getEventUser(), this.getEventComment(), "", "");

		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("GLSCOMMENT", sGlassComment);

		MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);

		return doc;
	}

}
