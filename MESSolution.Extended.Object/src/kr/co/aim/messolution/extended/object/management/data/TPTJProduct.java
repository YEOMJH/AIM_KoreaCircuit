package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TPTJProduct extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	@CTORMTemplate(seq = "2", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	@CTORMTemplate(seq = "3", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "4", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "6", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "8", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "9", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "10", name="sampleProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String sampleProcessFlowName;
	@CTORMTemplate(seq = "11", name="sampleProcessFlowVersion", type="Key", dataType="String", initial="", history="")
	private String sampleProcessFlowVersion;
	@CTORMTemplate(seq = "12", name="sampleOperationName", type="Key", dataType="String", initial="", history="")
	private String sampleOperationName;
	@CTORMTemplate(seq = "13", name="sampleOperationVersion", type="Key", dataType="String", initial="", history="")
	private String sampleOperationVersion;
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "17", name="position", type="Column", dataType="Number", initial="", history="")
	private Number position;
	@CTORMTemplate(seq = "18", name="tptjSamplePosition", type="Column", dataType="String", initial="", history="")
	private String tptjSamplePosition;
	@CTORMTemplate(seq = "19", name="ruleNum", type="Column", dataType="Number", initial="", history="")
	private Number ruleNum;
	@CTORMTemplate(seq = "20", name="ruleName", type="Column", dataType="String", initial="", history="")
	private String ruleName;
	
	//instantiation
	public TPTJProduct()
	{
			
	}
		
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
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
	
	public String getProcessOperationName() {
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationVersion() {
		return processOperationVersion;
	}
	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}
	
	public String getSampleProcessFlowName() {
		return sampleProcessFlowName;
	}
	public void setSampleProcessFlow(String sampleProcessFlowName) {
		this.sampleProcessFlowName = sampleProcessFlowName;
	}
	public String getSampleProcessFlowVersion() {
		return sampleProcessFlowVersion;
	}
	public void setSampleProcessFlowVersion(String sampleProcessFlowVersion) {
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}
	
	public String getSampleOperationName() {
		return sampleOperationName;
	}
	public void setSampleOperationName(String sampleOperationName) {
		this.sampleOperationName = sampleOperationName;
	}
	public String getSampleOperationVersion() {
		return sampleOperationVersion;
	}
	public void setSampleOperationVersion(String sampleOperationVersion) {
		this.sampleOperationVersion = sampleOperationVersion;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}
	public String getLastEventUser()
	{
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventComment()
	{
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	public Number getPosition()
	{
		return position;
	}
	public void setPosition(Number position)
	{
		this.position = position;
	}
	public String getTptjSamplePosition()
	{
		return tptjSamplePosition;
	}
	public void setTptjSamplePosition(String tptjSamplePosition)
	{
		this.tptjSamplePosition = tptjSamplePosition;
	}
	public Number getRuleNum()
	{
		return ruleNum;
	}
	public void setRuleNum(Number ruleNum)
	{
		this.ruleNum = ruleNum;
	}
	public String getRuleName()
	{
		return ruleName;
	}
	public void setRuleName(String ruleName)
	{
		this.ruleName = ruleName;
	}
}
