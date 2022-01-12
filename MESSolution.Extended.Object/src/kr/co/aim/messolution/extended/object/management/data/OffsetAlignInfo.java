package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class OffsetAlignInfo extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "3", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "4", name="layerName", type="Key", dataType="String", initial="", history="")
	private String layerName;
	@CTORMTemplate(seq = "5", name="alignLayer1", type="Column", dataType="String", initial="", history="")
	private String alignLayer1;
	@CTORMTemplate(seq = "6", name="alignLayer2", type="Column", dataType="String", initial="", history="")
	private String alignLayer2;
	@CTORMTemplate(seq = "7", name="alignLayer3", type="Column", dataType="String", initial="", history="")
	private String alignLayer3;
	@CTORMTemplate(seq = "8", name="alignLayer4", type="Column", dataType="String", initial="", history="")
	private String alignLayer4;
	@CTORMTemplate(seq = "9", name="alignLayer5", type="Column", dataType="String", initial="", history="")
	private String alignLayer5;
	@CTORMTemplate(seq = "10", name="alignLayer6", type="Column", dataType="String", initial="", history="")
	private String alignLayer6;
	@CTORMTemplate(seq = "11", name="mainLayerStep", type="Column", dataType="String", initial="", history="")
	private String mainLayerStep;
	
	public OffsetAlignInfo() {
		super();
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
	public String getLayerName() {
		return layerName;
	}
	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}
	public String getAlignLayer1() {
		return alignLayer1;
	}
	public void setAlignLayer1(String alignLayer1) {
		this.alignLayer1 = alignLayer1;
	}
	public String getAlignLayer2() {
		return alignLayer2;
	}
	public void setAlignLayer2(String alignLayer2) {
		this.alignLayer2 = alignLayer2;
	}
	public String getAlignLayer3() {
		return alignLayer3;
	}
	public void setAlignLayer3(String alignLayer3) {
		this.alignLayer3 = alignLayer3;
	}
	public String getAlignLayer4() {
		return alignLayer4;
	}
	public void setAlignLayer4(String alignLayer4) {
		this.alignLayer4 = alignLayer4;
	}

	public String getAlignLayer5() {
		return alignLayer5;
	}

	public void setAlignLayer5(String alignLayer5) {
		this.alignLayer5 = alignLayer5;
	}

	public String getAlignLayer6() {
		return alignLayer6;
	}

	public void setAlignLayer6(String alignLayer6) {
		this.alignLayer6 = alignLayer6;
	}

	public String getMainLayerStep() {
		return mainLayerStep;
	}

	public void setMainLayerStep(String mainLayerStep) {
		this.mainLayerStep = mainLayerStep;
	}
	
	
}
