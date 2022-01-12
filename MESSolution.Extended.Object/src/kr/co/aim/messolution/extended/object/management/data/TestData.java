package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TestData  extends UdfAccessor implements DataInfo<TestDataKey>  {
	
	private TestDataKey key;
	private String judge;
	private String repairCode;
	private String aoiCode;
	private String productSpecName                ;
	private String productSpecVersion             ;
	private String processFlowName                ;
	private String processFlowVersion             ;
	private String processOperationName           ;
	private String processOperationVersion        ;
	private String nodeStack                      ;
	private String machineName                    ;
	private String factoryName;
	private String branchEndNodeId;
	
	
	public String getBranchEndNodeId()
	{
		return branchEndNodeId;
	}
	public void setBranchEndNodeId(String branchEndNodeId)
	{
		this.branchEndNodeId = branchEndNodeId;
	}
	public TestDataKey getKey()
	{
		return key;
	}
	public void setKey(TestDataKey key)
	{
		this.key = key;
	}
	public String getJudge()
	{
		return judge;
	}
	public void setJudge(String judge)
	{
		this.judge = judge;
	}
	
	public String getRepairCode()
	{
		return repairCode;
	}
	public void setRepairCode(String repairCode)
	{
		this.repairCode = repairCode;
	}
	public String getAoiCode()
	{
		return aoiCode;
	}
	public void setAoiCode(String aoiCode)
	{
		this.aoiCode = aoiCode;
	}
	public String getProductSpecName()
	{
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}
	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}
	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
	}
	public String getProcessFlowName()
	{
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}
	public String getProcessFlowVersion()
	{
		return processFlowVersion;
	}
	public void setProcessFlowVersion(String processFlowVersion)
	{
		this.processFlowVersion = processFlowVersion;
	}
	public String getProcessOperationName()
	{
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationVersion()
	{
		return processOperationVersion;
	}
	public void setProcessOperationVersion(String processOperationVersion)
	{
		this.processOperationVersion = processOperationVersion;
	}
	public String getNodeStack()
	{
		return nodeStack;
	}
	public void setNodeStack(String nodeStack)
	{
		this.nodeStack = nodeStack;
	}
	public String getMachineName()
	{
		return machineName;
	}
	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
	public String getFactoryName()
	{
		return factoryName;
	}
	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}
	
	
	
}