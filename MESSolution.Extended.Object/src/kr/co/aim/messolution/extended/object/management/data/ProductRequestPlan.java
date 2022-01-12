package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductRequestPlan extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "2", name="planDate", type="Key", dataType="Timestamp", initial="", history="")
	private Timestamp planDate;
	
	@CTORMTemplate(seq = "3", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;

	@CTORMTemplate(seq = "6", name="planQuantity", type="Column", dataType="Number", initial="", history="")
	private long planQuantity;

	@CTORMTemplate(seq = "7", name="createQuantity", type="Column", dataType="Number", initial="", history="")
	private long createQuantity;

	@CTORMTemplate(seq = "8", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "10", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "11", name="planState", type="Column", dataType="String", initial="", history="")
	private String planState;

	@CTORMTemplate(seq = "12", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;

	@CTORMTemplate(seq = "13", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;

	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "16", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "17", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "18", name="productRequestHoldState", type="Column", dataType="String", initial="", history="")
	private String productRequestHoldState;

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public Timestamp getPlanDate() {
		return planDate;
	}

	public void setPlanDate(Timestamp planDate) {
		this.planDate = planDate;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
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

	public long getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		this.planQuantity = planQuantity;
	}

	public long getCreateQuantity() {
		return createQuantity;
	}

	public void setCreateQuantity(long createQuantity) {
		this.createQuantity = createQuantity;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getPlanState() {
		return planState;
	}

	public void setPlanState(String planState) {
		this.planState = planState;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getProductRequestHoldState() {
		return productRequestHoldState;
	}

	public void setProductRequestHoldState(String productRequestHoldState) {
		this.productRequestHoldState = productRequestHoldState;
	}


		

}
