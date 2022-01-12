package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TPTJCount extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	@CTORMTemplate(seq = "2", name="ruleName", type="Key", dataType="String", initial="", history="")
	private String ruleName;
	@CTORMTemplate(seq = "3", name="ruleNum", type="Key", dataType="Number", initial="", history="")
	private Number ruleNum;
	@CTORMTemplate(seq = "4", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "5", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "6", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "7", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "8", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "9", name="operationQty", type="Column", dataType="String", initial="", history="")
	private String operationQty;
	@CTORMTemplate(seq = "10", name="processCount", type="Column", dataType="String", initial="", history="")
	private String processCount;
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "12", name="deleteFlag", type="Column", dataType="String", initial="", history="")
	private String deleteFlag;
	@CTORMTemplate(seq = "13", name="completeFlag", type="Column", dataType="String", initial="", history="")
	private String completeFlag;
	
	//instantiation
	public TPTJCount()
	{
			
	}
		
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getRuleName()
	{
		return ruleName;
	}
	public void setRuleName(String ruleName)
	{
		this.ruleName = ruleName;
	}
	public Number getRuleNum()
	{
		return ruleNum;
	}
	public void setRuleNum(Number ruleNum)
	{
		this.ruleNum = ruleNum;
	}
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getProductSpecVersion() {
		return productSpecVersion;
	}
	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}
	
	public String getProcessFlowName() {
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	public String getProcessFlowVersion() {
		return processFlowVersion;
	}
	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}
	
	public String getOperationQty() {
		return operationQty;
	}
	public void setOperationQty(String operationQty) {
		this.operationQty = operationQty;
	}
	
	public String getProcessCount() {
		return processCount;
	}
	public void setProcessCount(String processCount) {
		this.processCount = processCount;
	}
	
	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getDeleteFlag()
	{
		return deleteFlag;
	}
	public void setDeleteFlag(String deleteFlag)
	{
		this.deleteFlag = deleteFlag;
	}
	public String getCompleteFlag()
	{
		return completeFlag;
	}
	public void setCompleteFlag(String completeFlag)
	{
		this.completeFlag = completeFlag;
	}
	
}