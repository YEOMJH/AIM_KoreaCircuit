package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveRepairPanelInfo extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "image_Data", type = "Key", dataType = "String", initial = "", history = "")
	private String image_Data;
	@CTORMTemplate(seq = "2", name = "panel_ID", type = "Key", dataType = "String", initial = "", history = "")
	private String panel_ID;
	@CTORMTemplate(seq = "3", name = "timekey", type = "Key", dataType = "String", initial = "", history = "")
	private String timekey;
	@CTORMTemplate(seq = "4", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "5", name = "step_ID", type = "Key", dataType = "String", initial = "", history = "")
	private String step_ID;
	@CTORMTemplate(seq = "6", name = "sheet_ID", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_ID;
	@CTORMTemplate(seq = "7", name = "sheet_Start_Time", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_Start_Time;
	@CTORMTemplate(seq = "8", name = "glass_Start_Time", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_Start_Time;
	@CTORMTemplate(seq = "9", name = "panel_Start_Time", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_Start_Time;
	@CTORMTemplate(seq = "10", name = "glass_ID", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_ID;
	@CTORMTemplate(seq = "11", name = "panel_No", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_No;
	@CTORMTemplate(seq = "12", name = "defect_No", type = "Column", dataType = "String", initial = "", history = "")
	private String defect_No;
	@CTORMTemplate(seq = "13", name = "defect_Code", type = "Column", dataType = "String", initial = "", history = "")
	private String defect_Code;
	@CTORMTemplate(seq = "14", name = "defect_Pattern", type = "Column", dataType = "String", initial = "", history = "")
	private String defect_Pattern;
	@CTORMTemplate(seq = "15", name = "defect_Size_Type", type = "Column", dataType = "String", initial = "", history = "")
	private String defect_Size_Type;
	@CTORMTemplate(seq = "16", name = "defect_Judge", type = "Column", dataType = "String", initial = "", history = "")
	private String defect_Judge;
	@CTORMTemplate(seq = "17", name = "glass_X", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_X;
	@CTORMTemplate(seq = "18", name = "glass_Y", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_Y;
	@CTORMTemplate(seq = "19", name = "glass_X2", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_X2;
	@CTORMTemplate(seq = "20", name = "glass_Y2", type = "Column", dataType = "String", initial = "", history = "")
	private String glass_Y2;
	@CTORMTemplate(seq = "21", name = "sheet_X", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_X;
	@CTORMTemplate(seq = "22", name = "sheet_Y", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_Y;
	@CTORMTemplate(seq = "23", name = "sheet_X2", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_X2;
	@CTORMTemplate(seq = "24", name = "sheet_Y2", type = "Column", dataType = "String", initial = "", history = "")
	private String sheet_Y2;
	@CTORMTemplate(seq = "25", name = "panel_X", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_X;
	@CTORMTemplate(seq = "26", name = "panel_Y", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_Y;
	@CTORMTemplate(seq = "27", name = "panel_X2", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_X2;
	@CTORMTemplate(seq = "28", name = "panel_Y2", type = "Column", dataType = "String", initial = "", history = "")
	private String panel_Y2;
	@CTORMTemplate(seq = "29", name = "rs_Judge", type = "Column", dataType = "String", initial = "", history = "")
	private String rs_Judge;
	@CTORMTemplate(seq = "30", name = "rs_Code", type = "Column", dataType = "String", initial = "", history = "")
	private String rs_Code;
	@CTORMTemplate(seq = "31", name = "rs_Defect_Image_Name", type = "Column", dataType = "String", initial = "", history = "")
	private String rs_Defect_Image_Name;
	@CTORMTemplate(seq = "32", name = "panelNum", type = "Column", dataType = "String", initial = "", history = "")
	private String panelNum;
	@CTORMTemplate(seq = "33", name = "repairOperation", type = "Column", dataType = "String", initial = "", history = "")
	private String repairOperation;
	@CTORMTemplate(seq = "34", name = "rsOperationForRP", type = "Column", dataType = "String", initial = "", history = "")
	private String rsOperationForRP;
	
	
	public String getImage_Data() {
		return image_Data;
	}
	public void setImage_Data(String image_Data) {
		this.image_Data = image_Data;
	}
	public String getPanel_ID() {
		return panel_ID;
	}
	public void setPanel_ID(String panel_ID) {
		this.panel_ID = panel_ID;
	}
	public String getTimekey() {
		return timekey;
	}
	public void setTimekey(String timekey) {
		this.timekey = timekey;
	}
	public String getProcessFlowName() {
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	public String getStep_ID() {
		return step_ID;
	}
	public void setStep_ID(String step_ID) {
		this.step_ID = step_ID;
	}
	public String getSheet_ID() {
		return sheet_ID;
	}
	public void setSheet_ID(String sheet_ID) {
		this.sheet_ID = sheet_ID;
	}
	public String getSheet_Start_Time() {
		return sheet_Start_Time;
	}
	public void setSheet_Start_Time(String sheet_Start_Time) {
		this.sheet_Start_Time = sheet_Start_Time;
	}
	public String getGlass_Start_Time() {
		return glass_Start_Time;
	}
	public void setGlass_Start_Time(String glass_Start_Time) {
		this.glass_Start_Time = glass_Start_Time;
	}
	public String getPanel_Start_Time() {
		return panel_Start_Time;
	}
	public void setPanel_Start_Time(String panel_Start_Time) {
		this.panel_Start_Time = panel_Start_Time;
	}
	public String getGlass_ID() {
		return glass_ID;
	}
	public void setGlass_ID(String glass_ID) {
		this.glass_ID = glass_ID;
	}
	public String getPanel_No() {
		return panel_No;
	}
	public void setPanel_No(String panel_No) {
		this.panel_No = panel_No;
	}
	public String getDefect_No() {
		return defect_No;
	}
	public void setDefect_No(String defect_No) {
		this.defect_No = defect_No;
	}
	public String getDefect_Code() {
		return defect_Code;
	}
	public void setDefect_Code(String defect_Code) {
		this.defect_Code = defect_Code;
	}
	public String getDefect_Pattern() {
		return defect_Pattern;
	}
	public void setDefect_Pattern(String defect_Pattern) {
		this.defect_Pattern = defect_Pattern;
	}
	public String getDefect_Size_Type() {
		return defect_Size_Type;
	}
	public void setDefect_Size_Type(String defect_Size_Type) {
		this.defect_Size_Type = defect_Size_Type;
	}
	public String getDefect_Judge() {
		return defect_Judge;
	}
	public void setDefect_Judge(String defect_Judge) {
		this.defect_Judge = defect_Judge;
	}
	public String getGlass_X() {
		return glass_X;
	}
	public void setGlass_X(String glass_X) {
		this.glass_X = glass_X;
	}
	public String getGlass_Y() {
		return glass_Y;
	}
	public void setGlass_Y(String glass_Y) {
		this.glass_Y = glass_Y;
	}
	public String getGlass_X2() {
		return glass_X2;
	}
	public void setGlass_X2(String glass_X2) {
		this.glass_X2 = glass_X2;
	}
	public String getGlass_Y2() {
		return glass_Y2;
	}
	public void setGlass_Y2(String glass_Y2) {
		this.glass_Y2 = glass_Y2;
	}
	public String getSheet_X() {
		return sheet_X;
	}
	public void setSheet_X(String sheet_X) {
		this.sheet_X = sheet_X;
	}
	public String getSheet_Y() {
		return sheet_Y;
	}
	public void setSheet_Y(String sheet_Y) {
		this.sheet_Y = sheet_Y;
	}
	public String getSheet_X2() {
		return sheet_X2;
	}
	public void setSheet_X2(String sheet_X2) {
		this.sheet_X2 = sheet_X2;
	}
	public String getSheet_Y2() {
		return sheet_Y2;
	}
	public void setSheet_Y2(String sheet_Y2) {
		this.sheet_Y2 = sheet_Y2;
	}
	public String getPanel_X() {
		return panel_X;
	}
	public void setPanel_X(String panel_X) {
		this.panel_X = panel_X;
	}
	public String getPanel_Y() {
		return panel_Y;
	}
	public void setPanel_Y(String panel_Y) {
		this.panel_Y = panel_Y;
	}
	public String getPanel_X2() {
		return panel_X2;
	}
	public void setPanel_X2(String panel_X2) {
		this.panel_X2 = panel_X2;
	}
	public String getPanel_Y2() {
		return panel_Y2;
	}
	public void setPanel_Y2(String panel_Y2) {
		this.panel_Y2 = panel_Y2;
	}
	public String getRs_Judge() {
		return rs_Judge;
	}
	public void setRs_Judge(String rs_Judge) {
		this.rs_Judge = rs_Judge;
	}
	public String getRs_Code() {
		return rs_Code;
	}
	public void setRs_Code(String rs_Code) {
		this.rs_Code = rs_Code;
	}
	public String getRs_Defect_Image_Name() {
		return rs_Defect_Image_Name;
	}
	public void setRs_Defect_Image_Name(String rs_Defect_Image_Name) {
		this.rs_Defect_Image_Name = rs_Defect_Image_Name;
	}
	public String getPanelNum() {
		return panelNum;
	}
	public void setPanelNum(String panelNum) {
		this.panelNum = panelNum;
	}
	public String getRepairOperation() {
		return repairOperation;
	}
	public void setRepairOperation(String repairOperation) {
		this.repairOperation = repairOperation;
	}
	public String getRsOperationForRP() {
		return rsOperationForRP;
	}
	public void setRsOperationForRP(String rsOperationForRP) {
		this.rsOperationForRP = rsOperationForRP;
	}

	
}
