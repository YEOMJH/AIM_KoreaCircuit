package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;

public class ChangeSampleLotCount extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		String newCurrentLotCount = SMessageUtil.getBodyItemValue(doc, "NEWCURRENTLOTCOUNT", true);

		// EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSampleLotCount", this.getEventUser(), this.getEventComment(), "", "");

		List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(factoryName, productSpecName, processFlowName, processOperationName,
				machineName, toProcessOperationName);

		// Synchronize LotSampleCount with SamplePolicy
		if (sampleLotCount != null && sampleLotCount.size() == 1)
		{
			ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountWithoutToFlow(sampleLotCount.get(0).getFactoryName(), sampleLotCount.get(0).getProductSpecName(),
					sampleLotCount.get(0).getProcessFlowName(), sampleLotCount.get(0).getProcessOperationName(), sampleLotCount.get(0).getMachineName(),
					sampleLotCount.get(0).getToProcessOperationName(), sampleLotCount.get(0).getLotSampleCount(), newCurrentLotCount, sampleLotCount.get(0).getTotalLotCount());
		}

		return doc;
	}

}
