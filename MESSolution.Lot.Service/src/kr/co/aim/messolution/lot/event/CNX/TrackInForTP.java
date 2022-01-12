package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;

import org.jdom.Document;

public class TrackInForTP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo_TrackInOutWithoutHistory("", getEventUser(), getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		MachineSpec machineSpecData = CommonUtil.getMachineSpecByMachineName(machineName);
		
		CommonValidation.checkFirstGlassLot(lotData, machineName);
		CommonValidation.checkOriginalProduct(lotData);
		CommonValidation.checkDummyProductReserveOper(lotData);
		CommonValidation.checkVirtualMachine(machineSpecData);
		
		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);
		
		// check ProductGrade
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductGradebyString(productNameList);


		if (lotData.getFactoryName().equals("TP"))
		{
			Map<String, String> Udfs = machineSpecData.getUdfs();
			String check = Udfs.get("TPSLOTCHECK");

			if (check.equals("Y"))
			{
				List<Map<String, Object>> sqlResult = checkSlotPosition(lotData);

				if (sqlResult.size() > 0)
					throw new CustomException("LOT-0046");
			}
		}

		CommonValidation.checkLotIssueState(lotData);
		CommonValidation.checkMQCPlanState(lotName, "TrackIn");
		
		if (lotData.getFactoryName().equals("OLED"))
			checkProductType(lotData);

		// Validation for product quantity.
		if (lotData.getProductQuantity() <= 0)
			throw new CustomException("LOT-0149");

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

		// TrackIn
		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);

		Map<String, String> lotUdfs = new HashMap<String, String>();
		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portSpecData.getPortType().toString());
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, lotUdfs);

		eventInfo.setEventName("TrackIn");
		MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

		try
		{
			MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureAction(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "1");
		}
		catch (CustomException e)
		{
			eventLog.info("Can't delete CtLotFutureAction:Abort Info not exist");
		}

		return doc;
	}
	
	private void checkProductType(Lot lotData) throws CustomException
	{
		ProcessOperationSpec pos = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (!pos.getDetailProcessOperationType().equals("CUT"))
		{
			if (!lotData.getProductType().equals("Glass"))
			{
				throw new CustomException("LOT-0038");
			}
		}
	}

	private List<Map<String, Object>> checkSlotPosition(Lot lotData)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (P.PRODUCTNAME), P.POSITION ");
		sql.append("  FROM PRODUCT P, DURABLE D ");
		sql.append(" WHERE P.LOTNAME = :LOTNAME ");
		sql.append("   AND P.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND D.DURABLETYPE = 'TPGlassCST' ");
		sql.append("GROUP BY P.POSITION ");
		sql.append("HAVING COUNT (P.PRODUCTNAME) = 1 ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();

		inquirybindMap.put("LOTNAME", lotData.getKey().getLotName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}
}
