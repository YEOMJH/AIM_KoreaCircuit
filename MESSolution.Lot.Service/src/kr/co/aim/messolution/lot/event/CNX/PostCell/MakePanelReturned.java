package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class MakePanelReturned extends SyncHandler {
	
	private static Log log = LogFactory.getLog(MakePanelReturned.class);

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		if (lotList != null)
		{
			for ( Iterator iteratorLotList = lotList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();
		
				String lotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);				
 
				ConstantMap constantMap = GenericServiceProxy.getConstantMap();
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName); 
				Map<String, String> udfs = lotData.getUdfs();
				String returnFlowName = udfs.get("RETURNFLOWNAME");
				//String newFlowVersion = "00001";
				String returnOperationName = udfs.get("RETURNOPERATIONNAME");
				String returnOperationVer = udfs.get("RETURNOPERATIONVER");
				if(!StringUtil.isNotEmpty(returnOperationVer))returnOperationVer = "00001";
						
				//Node changeNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), newFlowName, newFlowVersion, constantMap.Node_ProcessOperation, newProcOperName, newProcOperVersion);
				
				/*List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);
				
				String areaName       		   = lotData.getAreaName(); 
				String factoryName     		   = lotData.getFactoryName();
				String lotHoldState    		   = lotData.getLotHoldState();
				String lotProcessState         = lotData.getLotProcessState();
				String lotState                = lotData.getLotState();
				String processFlowName         = lotData.getProcessFlowName();
				String processFlowVersion      = lotData.getProcessFlowVersion();
				String productionType          = lotData.getProductionType();
				String productRequestName      = lotData.getProductRequestName(); 
				String productSpec2Name        = lotData.getProductSpec2Name();
				String productSpec2Version     = lotData.getProductSpec2Version();
				String productSpecName         = lotData.getProductSpecName(); 
				String productSpecVersion      = lotData.getProductSpecVersion();
				long priority                  = lotData.getPriority();
				Timestamp dueDate      		   = lotData.getDueDate();
				double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1(); 
				double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();

				lotData.setProcessFlowName(newFlowName);
				lotData.setProcessFlowVersion(newFlowVersion);*/
				
				Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
				/*if (StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run)
						&&StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped))
				{
					String sql = "UPDATE LOT SET LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME ";
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_Wait);
					bindMap.put("LOTNAME", lotData.getKey().getLotName());
					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
					
					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName); 
				}*/
				
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
				if (StringUtil.equals(lotData.getLotProcessState(), "WAIT")||
						(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run)
						&&StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped)))
				{
					changeSpecInfo.setAreaName(lotData.getAreaName());
					changeSpecInfo.setDueDate(lotData.getDueDate());
					changeSpecInfo.setFactoryName(lotData.getFactoryName());
					changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
					changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
					changeSpecInfo.setLotState(lotData.getLotState());
					changeSpecInfo.setNodeStack("");
					changeSpecInfo.setPriority(lotData.getPriority());
					changeSpecInfo.setProcessFlowName(returnFlowName);
					changeSpecInfo.setProcessFlowVersion("00001");
					changeSpecInfo.setProcessOperationName(returnOperationName);
					changeSpecInfo.setProcessOperationVersion(returnOperationVer);
					changeSpecInfo.setProductionType(lotData.getProductionType());
					changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
					changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
					changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
					changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
					changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
					//changeSpecInfo.setProductUSequence(productUdfs);
					changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
					changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

					Map<String, String> lotUdfs = new HashMap<String, String>();

					lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
					lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

					String sql = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME FROM NODE WHERE FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? "
							+ "   AND NODEATTRIBUTE1 = ? AND NODEATTRIBUTE2 = ? AND NODETYPE = 'ProcessOperation' ";

					Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(),
							changeSpecInfo.getProcessOperationName(), changeSpecInfo.getProcessOperationVersion() };

					String[][] returnNodeResult = null;
					try
					{
						returnNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);
					}
					catch (Exception e)
					{
					}

					if (returnNodeResult.length == 0)
					{
						throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
					}
					else
					{
						String sToBeNodeStack = (String) returnNodeResult[0][0];

						ProcessFlowKey processFlowKey = new ProcessFlowKey();
						processFlowKey.setFactoryName((String) returnNodeResult[0][3]);
						processFlowKey.setProcessFlowName((String) returnNodeResult[0][1]);
						processFlowKey.setProcessFlowVersion("00001");
						ProcessFlow returnFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

						// After doing Rework -> RuleSampling
						if ((StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQC")))
						{
							lotUdfs.put("RETURNFLOWNAME", "");
							lotUdfs.put("RETURNOPERATIONNAME", "");
						}
						else
						{
							String currentNode = lotData.getNodeStack();
							if (StringUtil.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
							{
								int NodeStackCount = getNodeStackCount(currentNode, '.');
								String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

								String returnForOriginalNode = originalNode;

								if (NodeStackCount >= 3)
								{
									if (returnForOriginalNode.lastIndexOf(".") > -1)
										returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));

									returnForOriginalNode = returnForOriginalNode.substring(returnForOriginalNode.lastIndexOf(".") + 1, returnForOriginalNode.length());
								}
								else
								{
									if (returnForOriginalNode.lastIndexOf(".") > -1)
										returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));
								}

								sql = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME FROM NODE WHERE NODEID = ? AND NODETYPE = 'ProcessOperation' ";

								bind = new Object[] { returnForOriginalNode };

								String[][] orginalNodeResult = null;
								try
								{
									orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);
								}
								catch (Exception e)
								{
								}

								if (orginalNodeResult.length > 0)
								{
									lotUdfs.put("RETURNFLOWNAME", (String) orginalNodeResult[0][1]);
									lotUdfs.put("RETURNOPERATIONNAME", (String) orginalNodeResult[0][2]);

									sToBeNodeStack = originalNode; // + "." + sToBeNodeStack;
								}
							}
						}

						changeSpecInfo.setNodeStack(sToBeNodeStack);
					}
					
					changeSpecInfo.setUdfs(lotUdfs);
				}
				else
				{
					throw new CustomException("LOT-9003", lotName + "(LotProcessState:" + lotData.getLotProcessState() + ")");
				}
				
				/*if (StringUtil.equals(lotHoldState, "Y"))
				{
					throw new CustomException("LOT-0033", lotName);
				}*/

				/*ChangeSpecInfo	changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeFlow(lotData, 
																									areaName, dueDate, factoryName, lotHoldState, lotProcessState, lotState, 
																									changeNode.getKey().getNodeId(), priority, processFlowName, processFlowVersion, newProcOperName, 
																									newProcOperVersion, lotData.getProcessOperationName(), productionType, productRequestName, productSpec2Name, 
																									productSpec2Version, productSpecName, productSpecVersion, productUdfs, 
																									subProductUnitQuantity1, subProductUnitQuantity2);*/
		 
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReturnPanel", getEventUser(), getEventComment(), "", "");
				if(StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped))
				{
					String sql = "UPDATE LOT SET LOTSTATE = :LOTSTATE WHERE LOTNAME = :LOTNAME ";
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
					bindMap.put("LOTNAME", lotData.getKey().getLotName());

					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
					
					lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
					
					String sql1 = "UPDATE LOT SET LOTSTATE = :LOTSTATE WHERE LOTNAME = :LOTNAME ";
					Map<String, Object> bindMap1 = new HashMap<String, Object>();
					bindMap1.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Scrapped);
					bindMap1.put("LOTNAME", lotData.getKey().getLotName());
					if (StringUtil.equals(oldLotData.getLotProcessState(),GenericServiceProxy.getConstantMap().Lot_Run))
					{
						sql1 = "UPDATE LOT SET LOTSTATE = :LOTSTATE, LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME ";
						bindMap1.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_Run);
					}

					GenericServiceProxy.getSqlMesTemplate().update(sql1, bindMap1);
				}
				else
				{
					lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
				}
				//set ct_borrowpanel
				BorrowPanel borrowedPanel = ExtendedObjectProxy.getBorrowPanelService().selectByKey(true, new Object[] {lotName});
				borrowedPanel.setReturnDate(eventInfo.getEventTime());
				borrowedPanel.setBorrowState("Returned");
				BorrowPanel newBorrowedPanel = ExtendedObjectProxy.getBorrowPanelService().modify(eventInfo, borrowedPanel);
				ExtendedObjectProxy.getBorrowPanelService().remove(eventInfo, newBorrowedPanel);
				log.info("Excute Insert Notice :Remove CT_BorrowPanel! " );

			}
		}
		 
		return doc;
	}
	
	private int getNodeStackCount(String str, char c)
	{
		int count = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}
}
