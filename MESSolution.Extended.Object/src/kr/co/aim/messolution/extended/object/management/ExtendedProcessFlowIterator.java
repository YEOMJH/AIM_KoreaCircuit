package kr.co.aim.messolution.extended.object.management;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.ProcessFlowService;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStack;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIteratorStatus;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;


public class ExtendedProcessFlowIterator
{
	private static Log					log					= LogFactory.getLog(ExtendedProcessFlowIterator.class);

	private ProcessFlow					processFlow;
	private NodeStack					nodeStack;
	private String						reworkNodeId;

	private Node						currentNodeData		= null;
	private Node						previousNodeData	= null;
	private long						reworkCount			= 0;
	private ProcessFlowIteratorStatus	status				= ProcessFlowIteratorStatus.PFIS_INVALID;

	private ProcessFlowService			processFlowService;

	public ExtendedProcessFlowIterator(ProcessFlow aProcessFlow, NodeStack aNodeStack, String aReworkNodeId)
	{
		this.processFlow = aProcessFlow;
		this.nodeStack = aNodeStack;
		this.reworkNodeId = aReworkNodeId;

		this.processFlowService = ProcessFlowServiceProxy.getProcessFlowService();
	}

	public ProcessFlow getProcessFlow()
	{
		return processFlow;
	}

	public NodeStack getNodeStack()
	{
		return nodeStack;
	}

	public String getReworkNodeId()
	{
		return reworkNodeId;
	}

	public Node getCurrentNodeData()
	{
		return currentNodeData;
	}

	public long getReworkCount()
	{
		return reworkCount;
	}

	public ProcessFlowIteratorStatus getStatus()
	{
		return this.status;
	}

	public void moveNext(String aReworkFlag, ExtendedPFIValueSetter valueSetter) throws NotFoundSignal, FrameworkErrorSignal
	{
		if (this.nodeStack.size() > 0)
		{
			this.currentNodeData = this.processFlowService.getNode(this.nodeStack.peek());

			if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_Start)
				|| this.currentNodeData.getNodeType()
						.equals(GenericServiceProxy.getConstantMap().Node_ProcessOperation))
			{
				this.status = ProcessFlowIteratorStatus.PFIS_PROCESS_OPERATION;
			}

			this.reworkCount = 0;

			while (this.status != ProcessFlowIteratorStatus.PFIS_INVALID
				&& this.status != ProcessFlowIteratorStatus.PFIS_END)
			{
				this.move1Next(aReworkFlag, valueSetter);

				if (this.currentNodeData.getNodeType().equals(
					GenericServiceProxy.getConstantMap().Node_ProcessOperation))
				{
					this.status = ProcessFlowIteratorStatus.PFIS_PROCESS_OPERATION;
					break;
				}
			}
		}
	}

	public void moveTo(NodeStack aNodeStack, String aReworkFlag) throws FrameworkErrorSignal
	{
		try
		{
			this.reworkCount = 0;

			// if( this.nodeStack.size() > 0 )
			if (aNodeStack.size() > 0) // changed 3.0
			{
				// 1. Replace NodeStack To Input
				this.nodeStack = aNodeStack;

				// 2. Set CurrentNodeData
				this.previousNodeData = this.currentNodeData;
				this.currentNodeData = this.processFlowService.getNode(this.nodeStack.peek());

				// 3. Check If NodeType is ProcessOperation
				if (this.currentNodeData.getNodeType().equals(
					GenericServiceProxy.getConstantMap().Node_ProcessOperation))
					this.status = ProcessFlowIteratorStatus.PFIS_PROCESS_OPERATION;
				else
				{
					this.status = ProcessFlowIteratorStatus.PFIS_INVALID;
					return;
				}

				// 4. Set ReworkCount
				if ("Y".equals(aReworkFlag))
					this.reworkCount = 1;
				else if ("N".equals(aReworkFlag))
					this.reworkCount = -1;
			}
		} catch (NotFoundSignal ex)
		{
			this.status = ProcessFlowIteratorStatus.PFIS_INVALID;
		}

	}

	private void move1Next(String aReworkFlag, ExtendedPFIValueSetter valueSetter)
	{
		try
		{
			// 1. Decide Next Node
			if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_ProcessFlow))
			{
				// 1-1. Enter Nested ProcessFlow or Move Down, If ProcessFlow
				if (this.previousNodeData == null
					|| !this.previousNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_End))
				{
					// 1-1-1.1. Set Nested ProcessFlow to this.processFlow
					String processFlowVersion = this.currentNodeData.getNodeAttribute2();
					if (GenericServiceProxy.getConstantMap().Spec_Active.equals(processFlowVersion))
					{
						processFlowVersion =
								ProcessFlowServiceProxy.getProcessFlowService().getActiveVersion(
									this.currentNodeData.getFactoryName(), this.currentNodeData.getNodeAttribute1());
					}

					ProcessFlowKey nestedProcessFlowKey =
							new ProcessFlowKey(this.currentNodeData.getFactoryName(),
									this.currentNodeData.getNodeAttribute1(), processFlowVersion);

					this.processFlow = this.processFlowService.selectByKey(nestedProcessFlowKey);

					// 1-1-1.2. Set Nested ProcessFlow's StartNode to CurrentNode
					this.previousNodeData = this.currentNodeData;
					this.currentNodeData = this.processFlowService.getStartNode(this.processFlow.getKey());

					// 1-1-1.3. Add NodeId to NodeStack
					this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
				}
				else
				{
					// 1-1-2.1. Set Next Node to CurrentNode
					this.previousNodeData = this.currentNodeData;
					this.currentNodeData =
							this.processFlowService.getNextNode(this.currentNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Normal, "");
					// 1-1-2.2. Replace LastNode with CurrentNode from NodeStack
					this.nodeStack.pop();
					this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
				}
			}
			else if (this.currentNodeData.getNodeType().equals(
				GenericServiceProxy.getConstantMap().Node_ConditionalDivergence))
			{
				// 1-2. Move Down By Condition Expression, If ConditionalDivergence
				// 1-2.1. Evaluate Condition Expression
				String conditionExp = this.currentNodeData.getNodeAttribute2();
				boolean result = this.evaluateExpression(conditionExp, valueSetter);
				String conditionFlag = result ? "Y" : "N";

				// 1-2.2 Set Next Node to CurrentNode
				this.previousNodeData = this.currentNodeData;
				this.currentNodeData =
						this.processFlowService.getNextNode(this.currentNodeData.getKey().getNodeId(),
							GenericServiceProxy.getConstantMap().Arc_Conditional, conditionFlag);

				// 1-2.3 Replace LastNode with CurrentNode from NodeStack
				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(
				GenericServiceProxy.getConstantMap().Node_ReworkDivergence))
			{
				// 1-3. Move Down By Rework Flag, If ReworkDivergence
				// 1-3.1. Set ReworkCount
				String reworkFlag = "N";
				if ("Y".equals(aReworkFlag))
				{
					reworkFlag = "Y";
					if (StringUtils.isEmpty(this.reworkNodeId))
					{
						this.reworkNodeId = this.currentNodeData.getKey().getNodeId();
					}
					this.reworkCount = 1;
				}
				else
				{
					if (StringUtils.isNotEmpty(this.reworkNodeId)
						&& this.reworkNodeId.equals(this.currentNodeData.getNodeAttribute2()))
					{
						this.reworkNodeId = "";
						this.reworkCount = -1;
					}
				}

				// 1-3.2. Set Next Node to CurrentNode
				this.previousNodeData = this.currentNodeData;
				this.currentNodeData =
						this.processFlowService.getNextNode(this.currentNodeData.getKey().getNodeId(),
							GenericServiceProxy.getConstantMap().Arc_Rework, reworkFlag);

				// 1-3.3. Replace LastNode with CurrentNode from NodeStack
				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_SwitchStart))
			{
				// 1-4. Move Down By Condition Expression, If SwitchDivergence
				// 1-4.1 Set Next Node to CurrentNode
				this.previousNodeData = this.currentNodeData;
				List<Node> caseNodeList =
						this.processFlowService.getNextNodeList(currentNodeData.getKey().getNodeId(),
							GenericServiceProxy.getConstantMap().Arc_Case, null);

				// 1-4-1.1. Select Case Node
				boolean conditionResult = false;
				for (Node node : caseNodeList)
				{
					// Evaluate a Condition Expression of Case Node
					String conditionExp = node.getNodeAttribute2();
					conditionResult = this.evaluateExpression(conditionExp, valueSetter);
					if (conditionResult)
					{
						this.currentNodeData = node;
						break;
					}
				}

				// 1-4-1.2. Select Otherwise Node, If Case doesn't select.
				if (!conditionResult)
				{
					this.currentNodeData =
							this.processFlowService.getNextNode(this.currentNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Otherwise, null);
				}

				// 1-4.2 Replace LastNode with CurrentNode from NodeStack
				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_Goto))
			{
				// 1-5. Goto�� ���� Condition �� üũ�Ѵ�. 
				this.previousNodeData = this.currentNodeData;

				boolean conditionResult = true;

				if (StringUtils.isNotEmpty(previousNodeData.getNodeAttribute2()))
					conditionResult = this.evaluateExpression(previousNodeData.getNodeAttribute2(), valueSetter);

				if (conditionResult)
					// 1-5-1.  ���� ���� ��ų�, true �̸�, ������ Node�� �̵��Ѵ�. 
					this.currentNodeData = this.processFlowService.getNode(previousNodeData.getNodeAttribute1());
				else
					// 1-5-2.  ���� ���� false �̸�, GodeNode�� NextNode�� �̵��Ѵ�. 
					this.currentNodeData =
							this.processFlowService.getNextNode(this.previousNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Normal, "");

				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_WhileStart))
			{
				// 1-6. WhileStart ���� Condition �� üũ�Ѵ�.
				this.previousNodeData = this.currentNodeData;
				boolean conditionResult = this.evaluateExpression(previousNodeData.getNodeAttribute2(), valueSetter);
				if (conditionResult)
				{
					// 1-6-1.  ���� ���� true �̸�, WhileStart Node �� NextNode�� �̵��Ѵ�. 
					this.currentNodeData =
							this.processFlowService.getNextNode(this.previousNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Normal, "");
				}
				else
				{
					// 1-6-2.  ���� ���� false �̸�, WhileEnd Node�� �̵��Ѵ�. 
					this.currentNodeData =
							ProcessFlowServiceProxy.getNodeService().getNode(this.previousNodeData.getFactoryName(),
								this.previousNodeData.getProcessFlowName(),
								this.previousNodeData.getProcessFlowVersion(),
								GenericServiceProxy.getConstantMap().Node_WhileEnd,
								this.previousNodeData.getNodeAttribute1(), "");
				}

				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_WhileEnd))
			{
				// 1-7. WhileEnd Node �� ��� ���ǿ� ��� WhileStart�� �̵��ϰų�, WhileEnd�� NextNode�� �̵��Ѵ�. 
				boolean conditionResult = false;
				// 1-7-1. PreviousNode�� WhileStart�� �ƴ� ����, While ���� ���� ������ �ǹ��Ѵ�. 
				if (this.previousNodeData != null
					&& !GenericServiceProxy.getConstantMap().Node_WhileStart.equals(this.previousNodeData.getNodeType()))
				{
					this.previousNodeData = this.currentNodeData;

					// 1-7-1-1. WhileEnd�� Expression�� �ִ� ��� Expression�� �����Ѵ�. (set ��� Expression �� ���ɼ��� ����)
					if (StringUtils.isNotEmpty(previousNodeData.getNodeAttribute2()))
						conditionResult = this.evaluateExpression(previousNodeData.getNodeAttribute2(), valueSetter);
					else
						conditionResult = true;

					// 1-7-1-2. ���ǽ��� �����ϴ� ����, WhileStart Node�� �̵��Ѵ�. 
					if (conditionResult)
					{
						this.currentNodeData =
								ProcessFlowServiceProxy.getNodeService().getNode(
									this.previousNodeData.getFactoryName(), this.previousNodeData.getProcessFlowName(),
									this.previousNodeData.getProcessFlowVersion(),
									GenericServiceProxy.getConstantMap().Node_WhileStart,
									this.previousNodeData.getNodeAttribute1(), "");
					}
				}

				// 1-7-2. While �� ���������� ���μ�, WhileEnd Node �� NextNode�� �̵��Ѵ�. 
				if (conditionResult == false)
				{
					this.previousNodeData = this.currentNodeData;

					this.currentNodeData =
							this.processFlowService.getNextNode(this.previousNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Normal, "");
				}
				this.nodeStack.pop();
				this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
			}
			else if (this.currentNodeData.getNodeType().equals(GenericServiceProxy.getConstantMap().Node_End))
			{
				// 1-8. Exit Nested ProcessFlow, Return Adhoc or Clear, If End
				if (this.nodeStack.size() > 1)
				{
					// 1-8-1.1. Remove Last Node form NodeStack
					this.nodeStack.pop();

					this.previousNodeData = this.currentNodeData;

					// 1-8-1.2. Branch End ���� üũ 
					if (!checkAndResetBranchEndNode(this.previousNodeData, valueSetter))
					{
						// 1-8-1.2.1 Set Parent's ProcessFlowNode to CurrentNode
						this.currentNodeData = this.processFlowService.getNode(this.nodeStack.peek());
					}
					else
					{
						// 1-8-1.2.2 ���� Node�� BranchEndNode�� ��� RejoinNode�� ����. 
						this.currentNodeData = this.processFlowService.getNode(this.nodeStack.peek());
					}

					// 1-8-1.3 Set parent ProcessFlow to this.processFlow
					String processFlowVersion = this.currentNodeData.getProcessFlowVersion();
					if (GenericServiceProxy.getConstantMap().Spec_Active.equals(processFlowVersion))
					{
						processFlowVersion =
								ProcessFlowServiceProxy.getProcessFlowService().getActiveVersion(
									this.currentNodeData.getFactoryName(), this.currentNodeData.getProcessFlowName());
					}
					ProcessFlowKey parentProcessFlowKey = new ProcessFlowKey();
					parentProcessFlowKey.setFactoryName(this.currentNodeData.getFactoryName());
					parentProcessFlowKey.setProcessFlowName(this.currentNodeData.getProcessFlowName());
					parentProcessFlowKey.setProcessFlowVersion(processFlowVersion);
					this.processFlow = this.processFlowService.selectByKey(parentProcessFlowKey);
				}
				else
				{
					// 1-8-2.1 Set Status to END
					this.status = ProcessFlowIteratorStatus.PFIS_END;
				}
			}
			else
			{
				// 1-9. Move Down, Otherwise
				// 1-9.1. Set ReworkCount, If Rework Convergence
				if (this.currentNodeData.getNodeType().equals(
					GenericServiceProxy.getConstantMap().Node_ReworkConvergence)
					&& StringUtils.isNotEmpty(this.reworkNodeId)
					&& (this.reworkNodeId.equals(this.currentNodeData.getNodeAttribute2()) || "*".equals(this.currentNodeData.getNodeAttribute1())))
				{
					this.reworkNodeId = "";
					this.reworkCount = -1;
				}

				// 1-9.2. Remove Last Node form NodeStack
				this.nodeStack.pop();

				this.previousNodeData = this.currentNodeData;
				// 1-9-3. Branch End ���� üũ 
				if (!checkAndResetBranchEndNode(this.previousNodeData, valueSetter))
				{
					// Branch End Node�� �ƴ� ���, Set Next Node to CurrentNode
					this.currentNodeData =
							this.processFlowService.getNextNode(this.currentNodeData.getKey().getNodeId(),
								GenericServiceProxy.getConstantMap().Arc_Normal, "");

					// Add CurrentNode in NodeStack
					this.nodeStack.push(this.currentNodeData.getKey().getNodeId());
				}
				else
				{
					// ���� Node�� branchEndNode�� ���, rejoinNode�� ���� 
					if (this.nodeStack.size() > 0)
					{
						this.currentNodeData = this.processFlowService.getNode(this.nodeStack.peek());

						if (!this.currentNodeData.getProcessFlowName().equals(
							this.processFlow.getKey().getProcessFlowName())
							|| !this.currentNodeData.getProcessFlowVersion().equals(
								this.processFlow.getKey().getProcessFlowVersion()))
						{
							// Set rejoin ProcessFlow to this.processFlow
							ProcessFlowKey parentProcessFlowKey = new ProcessFlowKey();
							parentProcessFlowKey.setFactoryName(this.currentNodeData.getFactoryName());
							parentProcessFlowKey.setProcessFlowName(this.currentNodeData.getProcessFlowName());
							parentProcessFlowKey.setProcessFlowVersion(this.currentNodeData.getProcessFlowVersion());

							// rejoinNode�� ���, ProcessFlow Node �� �� ����(Ư��, ProcessFlowVersion�� Active�� ��� �Ұ�)
							if (!GenericServiceProxy.getConstantMap().Spec_Active.equals(parentProcessFlowKey.getProcessFlowVersion()))
							{
								this.processFlow = this.processFlowService.selectByKey(parentProcessFlowKey);
							}
							else
							{
								this.status = ProcessFlowIteratorStatus.PFIS_INVALID;
							}
						}
					}
					else
					{
						this.status = ProcessFlowIteratorStatus.PFIS_INVALID;
					}
				}
			}
		} catch (Exception ex)
		{
			if (log.isDebugEnabled())
				log.debug(ex);
			this.status = ProcessFlowIteratorStatus.PFIS_INVALID;
		}
	}

	private boolean checkAndResetBranchEndNode(Node node, ExtendedPFIValueSetter valueSetter)
	{
		String branchEndNodeId = (String) ObjectUtil.getFieldValue(valueSetter.getNewData(), "branchEndNodeId");

		if (StringUtils.isNotEmpty(branchEndNodeId))
		{
			NodeStack branchEndNodeStack = NodeStackUtil.stringToNodeStack(branchEndNodeId);
			if (StringUtils.equals(branchEndNodeStack.peek(), node.getKey().getNodeId()))
			{
				branchEndNodeStack.pop();
				String newBranchEndNodeId = "";
				if (branchEndNodeStack.size() > 0)
					newBranchEndNodeId = NodeStackUtil.nodeStackToString(branchEndNodeStack);

				ObjectUtil.setFieldValue(valueSetter.getNewData(), "branchEndNodeId", newBranchEndNodeId);

				return true;
			}
		}

		return false;
	}

	private boolean evaluateExpression(String conditionExp, ExtendedPFIValueSetter valueSetter)
	{
		try
		{
			if ("1==1".equals(conditionExp))
				return true;

			XPath xpath = new DOMXPath(conditionExp);
			CustomExpression expression = new CustomExpression();

			Map<String, Object> expressionContext = new HashMap<String, Object>();

			// prepare variables to use in expression
			expressionContext.put(valueSetter.getNamePreFix() + ".NewData", valueSetter.getNewData());

			expressionContext.put(valueSetter.getNamePreFix() + ".OldData", valueSetter.getOldData());

			expressionContext.put(valueSetter.getSpecPreFix(), valueSetter.getProductSpec());

			expressionContext.put(valueSetter.getFlowPreFix(), valueSetter.getProcessFlow());

			expression.setExpressionContext(expressionContext);
			xpath.setFunctionContext(expression.getFunctionContext());

			return (Boolean) xpath.evaluate(null);
		}
		catch (Exception ex)
		{
			log.warn("Fail to execute conditional expression : " + conditionExp, ex);
			return false;
		}
	}
}
