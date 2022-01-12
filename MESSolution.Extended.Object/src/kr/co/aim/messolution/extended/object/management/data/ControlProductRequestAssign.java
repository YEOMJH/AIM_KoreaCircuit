package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ControlProductRequestAssign  extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;	
	
	@CTORMTemplate(seq = "2", name="toProductRequestName", type="Key", dataType="String", initial="", history="")
	private String toProductRequestName;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name="toFactoryName", type="Column", dataType="String", initial="", history="")
	private String toFactoryName;
	
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "7", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "8", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "9", name="toProductSpecName", type="Column", dataType="String", initial="", history="")
	private String toProductSpecName;
	
	@CTORMTemplate(seq = "10", name="toProductSpecVersion", type="Column", dataType="String", initial="", history="")
	private String toProductSpecVersion;
	
	@CTORMTemplate(seq = "11", name="toProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String toProcessFlowName;

	@CTORMTemplate(seq = "12", name="toProcessFlowVersion", type="Column", dataType="String", initial="", history="")
	private String toProcessFlowVersion;

	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "16", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "17", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "18", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "19", name="toDescription", type="Column", dataType="String", initial="", history="")
	private String toDescription;

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getToProductRequestName() {
		return toProductRequestName;
	}

	public void setToProductRequestName(String toProductRequestName) {
		this.toProductRequestName = toProductRequestName;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getToFactoryName() {
		return toFactoryName;
	}

	public void setToFactoryName(String toFactoryName) {
		this.toFactoryName = toFactoryName;
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

	public String getToProductSpecName() {
		return toProductSpecName;
	}

	public void setToProductSpecName(String toProductSpecName) {
		this.toProductSpecName = toProductSpecName;
	}

	public String getToProductSpecVersion() {
		return toProductSpecVersion;
	}

	public void setToProductSpecVersion(String toProductSpecVersion) {
		this.toProductSpecVersion = toProductSpecVersion;
	}

	public String getToProcessFlowName() {
		return toProcessFlowName;
	}

	public void setToProcessFlowName(String toProcessFlowName) {
		this.toProcessFlowName = toProcessFlowName;
	}

	public String getToProcessFlowVersion() {
		return toProcessFlowVersion;
	}

	public void setToProcessFlowVersion(String toProcessFlowVersion) {
		this.toProcessFlowVersion = toProcessFlowVersion;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getToDescription() {
		return toDescription;
	}

	public void setToDescription(String toDescription) {
		this.toDescription = toDescription;
	}

}
