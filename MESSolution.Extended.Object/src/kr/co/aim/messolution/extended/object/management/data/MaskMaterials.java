package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskMaterials extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name="materialType", type="Key", dataType="String", initial="", history="")
	private String materialType;
	@CTORMTemplate(seq = "2", name="maskMaterialName", type="key", dataType="String", initial="", history="")
	private String maskMaterialName;
	@CTORMTemplate(seq = "3", name="maskSpec", type="Column", dataType="String", initial="", history="")
	private String maskSpec;
	@CTORMTemplate(seq = "4", name="layer", type="Column", dataType="Number", initial="", history="")
	private String layer;
	@CTORMTemplate(seq = "5", name="thickness", type="Column", dataType="String", initial="", history="")
	private long thickness;
	@CTORMTemplate(seq = "6", name="totalQuantity", type="Column", dataType="Number", initial="", history="")
	private long totalQuantity;
	@CTORMTemplate(seq = "7", name="scrapQuantity", type="Column", dataType="Number", initial="", history="")
	private long scrapQuantity;
	@CTORMTemplate(seq = "8", name="addQuantity", type="Column", dataType="Number", initial="", history="")
	private long addQuantity;
	@CTORMTemplate(seq = "9", name="addScrapQuantity", type="Column", dataType="Number ", initial="", history="")
	private long addScrapQuantity;
	@CTORMTemplate(seq = "10", name="createUser", type="Column", dataType="String", initial="", history="N")
	private String createUser;
	@CTORMTemplate(seq = "11", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp createTime;
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "15", name="property", type="Column", dataType="String", initial="", history="")
	private String property;
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "17", name="maskMaterialVersion", type="Column", dataType="String", initial="", history="")
	private String maskMaterialVersion;
	
	public String getMaterialType() {
		return materialType;
	}
	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}
	public String getMaskMaterialName() {
		return maskMaterialName;
	}
	public void setMaskMaterialName(String maskMaterialName) {
		this.maskMaterialName = maskMaterialName;
	}
	public String getMaskSpec() {
		return maskSpec;
	}
	public void setMaskSpec(String maskSpec) {
		this.maskSpec = maskSpec;
	}
	public String getLayer() {
		return layer;
	}
	public void setLayer(String layer) {
		this.layer = layer;
	}
	public long getThickness() {
		return thickness;
	}
	public void setThickness(long thickness) {
		this.thickness = thickness;
	}
	public long getTotalQuantity() {
		return totalQuantity;
	}
	public void setTotalQuantity(long totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	public long getScrapQuantity() {
		return scrapQuantity;
	}
	public void setScrapQuantity(long scrapQuantity) {
		this.scrapQuantity = scrapQuantity;
	}
	public long getAddQuantity() {
		return addQuantity;
	}
	public void setAddQuantity(long addQuantity) {
		this.addQuantity = addQuantity;
	}
	public long getAddScrapQuantity() {
		return addScrapQuantity;
	}
	public void setAddScrapQuantity(long addScrapQuantity) {
		this.addScrapQuantity = addScrapQuantity;
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
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	public String getMaskMaterialVersion() {
		return maskMaterialVersion;
	}
	public void setMaskMaterialVersion(String maskMaterialVersion) {
		this.maskMaterialVersion = maskMaterialVersion;
	}
}
