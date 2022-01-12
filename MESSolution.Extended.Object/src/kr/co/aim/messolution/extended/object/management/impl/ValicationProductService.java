package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;


import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;

import kr.co.aim.messolution.extended.object.management.data.ValicationProduct;

import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ValicationProductService extends CTORMService<ValicationProduct> {
	public static Log logger = LogFactory.getLog(ValicationProductService.class);
	private final String historyEntity = "ValicationProductHist";

		public List<ValicationProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
		{
			List<ValicationProduct> result = super.select(condition, bindSet, ValicationProduct.class);

			return result;
		}

		public ValicationProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
		{
			return super.selectByKey(ValicationProduct.class, isLock, keySet);
		}

		public ValicationProduct create(EventInfo eventInfo, ValicationProduct dataInfo) throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);

			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}

		public void remove(EventInfo eventInfo, ValicationProduct dataInfo) throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

			super.delete(dataInfo);
		}

		public ValicationProduct modify(EventInfo eventInfo, ValicationProduct dataInfo)
		{
			super.update(dataInfo);

			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
