package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstRunPanel extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="panelName", type="Key", dataType="String", initial="", history="N")
	private String panelName;
	@CTORMTemplate(seq = "2", name="timekey", type="Key", dataType="String", initial="", history="N")
	private String timekey;
	@CTORMTemplate(seq = "3", name="productSpecName", type="Column", dataType="String", initial="", history="N")
	private String productSpecName;
	@CTORMTemplate(seq = "4", name="processFlowName", type="Column", dataType="String", initial="", history="N")
	private String processFlowName;
	@CTORMTemplate(seq = "5", name="processOperationName", type="Column", dataType="String", initial="", history="N")
	private String processOperationName;
	@CTORMTemplate(seq = "6", name="productRequestName", type="Column", dataType="String", initial="", history="N")
	private String productRequestName;
	@CTORMTemplate(seq = "7", name="machineName", type="Column", dataType="String", initial="", history="N")
	private String machineName;
	@CTORMTemplate(seq = "8", name="portName", type="Column", dataType="String", initial="", history="N")
	private String portName;
	@CTORMTemplate(seq = "9", name="portType", type="Column", dataType="String", initial="", history="N")
	private String portType;
	@CTORMTemplate(seq = "10", name="portUseType", type="Column", dataType="String", initial="", history="N")
	private String portUseType;
	@CTORMTemplate(seq = "11", name="panelJudge", type="Column", dataType="String", initial="", history="N")
	private String panelJudge;
	@CTORMTemplate(seq = "12", name="panelGrade", type="Column", dataType="String", initial="", history="N")
	private String panelGrade;
	
	public String getPanelName() {
		return panelName;
	}
	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}
	public String getTimekey() {
		return timekey;
	}
	public void setTimekey(String timekey) {
		this.timekey = timekey;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getProcessFlowName() {
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	public String getProcessOperationName() {
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	public String getProductRequestName() {
		return productRequestName;
	}
	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getPortType() {
		return portType;
	}
	public void setPortType(String portType) {
		this.portType = portType;
	}
	public String getPortUseType() {
		return portUseType;
	}
	public void setPortUseType(String portUseType) {
		this.portUseType = portUseType;
	}
	public String getPanelJudge() {
		return panelJudge;
	}
	public void setPanelJudge(String panelJudge) {
		this.panelJudge = panelJudge;
	}
	public String getPanelGrade() {
		return panelGrade;
	}
	public void setPanelGrade(String panelGrade) {
		this.panelGrade = panelGrade;
	}

}
