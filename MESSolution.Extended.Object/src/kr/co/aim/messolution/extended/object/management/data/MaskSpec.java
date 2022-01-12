package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

	public class MaskSpec extends UdfAccessor {
		
		@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
		private String factoryName;

		@CTORMTemplate(seq = "2", name="maskSpecName", type="Key", dataType="String", initial="", history="")
		private String maskSpecName;

		@CTORMTemplate(seq = "3", name="maskKind", type="Column", dataType="String", initial="", history="")
		private String maskKind;

		@CTORMTemplate(seq = "4", name="description", type="Column", dataType="String", initial="", history="N")
		private String description;

		@CTORMTemplate(seq = "5", name="checkState", type="Column", dataType="String", initial="", history="N")
		private String checkState;

		@CTORMTemplate(seq = "6", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
		private Timestamp createTime;

		@CTORMTemplate(seq = "7", name="createUser", type="Column", dataType="String", initial="", history="N")
		private String createUser;

		@CTORMTemplate(seq = "8", name="checkOutTime", type="Column", dataType="Timestamp", initial="", history="N")
		private Timestamp checkOutTime;

		@CTORMTemplate(seq = "9", name="checkOutUser", type="Column", dataType="String", initial="", history="N")
		private String checkOutUser;

		@CTORMTemplate(seq = "10", name="maskType", type="Column", dataType="String", initial="", history="")
		private String maskType;

		@CTORMTemplate(seq = "11", name="timeUsedLimit", type="Column", dataType="Float", initial="", history="")
		private Float timeUsedLimit;

		@CTORMTemplate(seq = "12", name="durationUsedLimit", type="Column", dataType="Float", initial="", history="")
		private Float durationUsedLimit;

		@CTORMTemplate(seq = "13", name="defaultCapacity", type="Column", dataType="Number", initial="", history="")
		private Number defaultCapacity;

		@CTORMTemplate(seq = "14", name="transferTimeUsedLimit", type="Column", dataType="Float", initial="", history="")
		private Float transferTimeUsedLimit;

		@CTORMTemplate(seq = "15", name="factoryUsedLimit", type="Column", dataType="Float", initial="", history="")
		private Float factoryUsedLimit;

		@CTORMTemplate(seq = "16", name="materialType", type="Column", dataType="String", initial="", history="")
		private String materialType;

		@CTORMTemplate(seq = "17", name="vendor", type="Column", dataType="String", initial="", history="")
		private String vendor;

		@CTORMTemplate(seq = "18", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
		private Timestamp lastEventTime;

		@CTORMTemplate(seq = "19", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
		private String lastEventUser;

		@CTORMTemplate(seq = "20", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
		private String lastEventTimeKey;

		@CTORMTemplate(seq = "21", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
		private String lastEventComment;

		@CTORMTemplate(seq = "22", name="lastEventName", type="Column", dataType="String", initial="", history="N")
		private String lastEventName;

		@CTORMTemplate(seq = "23", name="maskProcessFlowName", type="Column", dataType="String", initial="", history="")
		private String maskProcessFlowName;

		@CTORMTemplate(seq = "24", name="productionType", type="Column", dataType="String", initial="", history="")
		private String productionType;

		@CTORMTemplate(seq = "25", name="maskSize", type="Column", dataType="float", initial="", history="")
		private float maskSize;

		@CTORMTemplate(seq = "26", name="thickness", type="Column", dataType="String", initial="", history="N")
		private String thickness;

		@CTORMTemplate(seq = "27", name="maskProcessFlowVersion", type="Column", dataType="String", initial="", history="N")
		private String maskProcessFlowVersion;

		@CTORMTemplate(seq = "28", name="cleanUsedLimit", type="Column", dataType="Float", initial="", history="")
		private Float cleanUsedLimit;		
		
		@CTORMTemplate(seq = "29", name="priority", type="Column", dataType="Number", initial="", history="")
		private Number priority;
		
		@CTORMTemplate(seq = "30", name="projectProductRequestName", type="Column", dataType="String", initial="", history="")
		private String projectProductRequestName;
		
		@CTORMTemplate(seq = "31", name="OFFSET_X_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_X_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "32", name="OFFSET_X_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_X_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "33", name="OFFSET_Y_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_Y_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "34", name="OFFSET_Y_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_Y_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "35", name="OFFSET_THETA_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_THETA_UPPER_LIMIT;

		@CTORMTemplate(seq = "36", name="OFFSET_THETA_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number OFFSET_THETA_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "37", name="SAPFrameSpecName", type="Column", dataType="String", initial="", history="")
		private String SAPFrameSpecName;				

		@CTORMTemplate(seq = "38", name="TFEOFFSET_X1_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_X1_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "39", name="TFEOFFSET_X1_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_X1_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "40", name="TFEOFFSET_Y1_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_Y1_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "41", name="TFEOFFSET_Y1_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_Y1_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "42", name="TFEOFFSET_X2_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_X2_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "43", name="TFEOFFSET_X2_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_X2_LOWER_LIMIT;
		
		@CTORMTemplate(seq = "44", name="TFEOFFSET_Y2_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_Y2_UPPER_LIMIT;
		
		@CTORMTemplate(seq = "45", name="TFEOFFSET_Y2_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
		private Number TFEOFFSET_Y2_LOWER_LIMIT;
		

		public String getFactoryName() {
			return factoryName;
		}

		public void setFactoryName(String factoryName) {
			this.factoryName = factoryName;
		}

		public String getMaskSpecName() {
			return maskSpecName;
		}

		public void setMaskSpecName(String maskSpecName) {
			this.maskSpecName = maskSpecName;
		}

		public String getMaskKind() {
			return maskKind;
		}

		public void setMaskKind(String maskKind) {
			this.maskKind = maskKind;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getCheckState() {
			return checkState;
		}

		public void setCheckState(String checkState) {
			this.checkState = checkState;
		}

		public Timestamp getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Timestamp createTime) {
			this.createTime = createTime;
		}

		public String getCreateUser() {
			return createUser;
		}

		public void setCreateUser(String createUser) {
			this.createUser = createUser;
		}

		public Timestamp getCheckOutTime() {
			return checkOutTime;
		}

		public void setCheckOutTime(Timestamp checkOutTime) {
			this.checkOutTime = checkOutTime;
		}

		public String getCheckOutUser() {
			return checkOutUser;
		}

		public void setCheckOutUser(String checkOutUser) {
			this.checkOutUser = checkOutUser;
		}

		public String getMaskType() {
			return maskType;
		}

		public void setMaskType(String maskType) {
			this.maskType = maskType;
		}

		public Float getTimeUsedLimit() {
			return timeUsedLimit;
		}

		public void setTimeUsedLimit(Float timeUsedLimit) {
			this.timeUsedLimit = timeUsedLimit;
		}

		public Float getDurationUsedLimit() {
			return durationUsedLimit;
		}

		public void setDurationUsedLimit(Float durationUsedLimit) {
			this.durationUsedLimit = durationUsedLimit;
		}

		public Number getDefaultCapacity() {
			return defaultCapacity;
		}

		public void setDefaultCapacity(Number defaultCapacity) {
			this.defaultCapacity = defaultCapacity;
		}

		public Float getTransferTimeUsedLimit() {
			return transferTimeUsedLimit;
		}

		public void setTransferTimeUsedLimit(Float transferTimeUsedLimit) {
			this.transferTimeUsedLimit = transferTimeUsedLimit;
		}

		public Float getFactoryUsedLimit() {
			return factoryUsedLimit;
		}

		public void setFactoryUsedLimit(Float factoryUsedLimit) {
			this.factoryUsedLimit = factoryUsedLimit;
		}

		public String getMaterialType() {
			return materialType;
		}

		public void setMaterialType(String materialType) {
			this.materialType = materialType;
		}

		public String getVendor() {
			return vendor;
		}

		public void setVendor(String vendor) {
			this.vendor = vendor;
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

		public String getLastEventTimeKey() {
			return lastEventTimeKey;
		}

		public void setLastEventTimeKey(String lastEventTimeKey) {
			this.lastEventTimeKey = lastEventTimeKey;
		}

		public String getLastEventComment() {
			return lastEventComment;
		}

		public void setLastEventComment(String lastEventComment) {
			this.lastEventComment = lastEventComment;
		}

		public String getLastEventName() {
			return lastEventName;
		}

		public void setLastEventName(String lastEventName) {
			this.lastEventName = lastEventName;
		}

		public String getMaskProcessFlowName() {
			return maskProcessFlowName;
		}

		public void setMaskProcessFlowName(String maskProcessFlowName) {
			this.maskProcessFlowName = maskProcessFlowName;
		}

		public String getProductionType() {
			return productionType;
		}

		public void setProductionType(String productionType) {
			this.productionType = productionType;
		}

		public float getMaskSize() {
			return maskSize;
		}

		public void setMaskSize(float maskSize) {
			this.maskSize = maskSize;
		}

		public String getThickness() {
			return thickness;
		}

		public void setThickness(String thickness) {
			this.thickness = thickness;
		}

		public String getMaskProcessFlowVersion() {
			return maskProcessFlowVersion;
		}

		public void setMaskProcessFlowVersion(String maskProcessflowVersion) {
			this.maskProcessFlowVersion = maskProcessflowVersion;
		}

		public Float getCleanUsedLimit() {
			return cleanUsedLimit;
		}

		public void setCleanUsedLimit(Float cleanUsedLimit) {
			this.cleanUsedLimit = cleanUsedLimit;
		}
		
		public Number getPriority() {
			return priority;
		}

		public void setPriority(Number priority) {
			this.priority = priority;
		}

		public String getProjectProductRequestName()
		{
			return projectProductRequestName;
		}

		public void setProjectProductRequestName(String projectProductRequestName)
		{
			this.projectProductRequestName = projectProductRequestName;
		}

		public Number getOFFSET_X_UPPER_LIMIT()
		{
			return OFFSET_X_UPPER_LIMIT;
		}

		public void setOFFSET_X_UPPER_LIMIT(Number oFFSET_X_UPPER_LIMIT)
		{
			OFFSET_X_UPPER_LIMIT = oFFSET_X_UPPER_LIMIT;
		}

		public Number getOFFSET_X_LOWER_LIMIT()
		{
			return OFFSET_X_LOWER_LIMIT;
		}

		public void setOFFSET_X_LOWER_LIMIT(Number oFFSET_X_LOWER_LIMIT)
		{
			OFFSET_X_LOWER_LIMIT = oFFSET_X_LOWER_LIMIT;
		}

		public Number getOFFSET_Y_UPPER_LIMIT()
		{
			return OFFSET_Y_UPPER_LIMIT;
		}

		public void setOFFSET_Y_UPPER_LIMIT(Number oFFSET_Y_UPPER_LIMIT)
		{
			OFFSET_Y_UPPER_LIMIT = oFFSET_Y_UPPER_LIMIT;
		}

		public Number getOFFSET_Y_LOWER_LIMIT()
		{
			return OFFSET_Y_LOWER_LIMIT;
		}

		public void setOFFSET_Y_LOWER_LIMIT(Number oFFSET_Y_LOWER_LIMIT)
		{
			OFFSET_Y_LOWER_LIMIT = oFFSET_Y_LOWER_LIMIT;
		}

		public Number getOFFSET_THETA_UPPER_LIMIT()
		{
			return OFFSET_THETA_UPPER_LIMIT;
		}

		public void setOFFSET_THETA_UPPER_LIMIT(Number oFFSET_THETA_UPPER_LIMIT)
		{
			OFFSET_THETA_UPPER_LIMIT = oFFSET_THETA_UPPER_LIMIT;
		}

		public Number getOFFSET_THETA_LOWER_LIMIT()
		{
			return OFFSET_THETA_LOWER_LIMIT;
		}

		public void setOFFSET_THETA_LOWER_LIMIT(Number oFFSET_THETA_LOWER_LIMIT)
		{
			OFFSET_THETA_LOWER_LIMIT = oFFSET_THETA_LOWER_LIMIT;
		}

		public String getSAPFrameSpecName() {
			return SAPFrameSpecName;
		}

		public void setSAPFrameSpecName(String sAPFrameSpecName) {
			SAPFrameSpecName = sAPFrameSpecName;
		}

		public Number getTFEOFFSET_X1_UPPER_LIMIT()
		{
			return TFEOFFSET_X1_UPPER_LIMIT;
		}

		public void setTFEOFFSET_X1_UPPER_LIMIT(Number tFEOFFSET_X1_UPPER_LIMIT)
		{
			TFEOFFSET_X1_UPPER_LIMIT = tFEOFFSET_X1_UPPER_LIMIT;
		}

		public Number getTFEOFFSET_X1_LOWER_LIMIT()
		{
			return TFEOFFSET_X1_LOWER_LIMIT;
		}

		public void setTFEOFFSET_X1_LOWER_LIMIT(Number tFEOFFSET_X1_LOWER_LIMIT)
		{
			TFEOFFSET_X1_LOWER_LIMIT = tFEOFFSET_X1_LOWER_LIMIT;
		}

		public Number getTFEOFFSET_Y1_UPPER_LIMIT()
		{
			return TFEOFFSET_Y1_UPPER_LIMIT;
		}

		public void setTFEOFFSET_Y1_UPPER_LIMIT(Number tFEOFFSET_Y1_UPPER_LIMIT)
		{
			TFEOFFSET_Y1_UPPER_LIMIT = tFEOFFSET_Y1_UPPER_LIMIT;
		}

		public Number getTFEOFFSET_Y1_LOWER_LIMIT()
		{
			return TFEOFFSET_Y1_LOWER_LIMIT;
		}

		public void setTFEOFFSET_Y1_LOWER_LIMIT(Number tFEOFFSET_Y1_LOWER_LIMIT)
		{
			TFEOFFSET_Y1_LOWER_LIMIT = tFEOFFSET_Y1_LOWER_LIMIT;
		}

		public Number getTFEOFFSET_X2_UPPER_LIMIT()
		{
			return TFEOFFSET_X2_UPPER_LIMIT;
		}

		public void setTFEOFFSET_X2_UPPER_LIMIT(Number tFEOFFSET_X2_UPPER_LIMIT)
		{
			TFEOFFSET_X2_UPPER_LIMIT = tFEOFFSET_X2_UPPER_LIMIT;
		}

		public Number getTFEOFFSET_X2_LOWER_LIMIT()
		{
			return TFEOFFSET_X2_LOWER_LIMIT;
		}

		public void setTFEOFFSET_X2_LOWER_LIMIT(Number tFEOFFSET_X2_LOWER_LIMIT)
		{
			TFEOFFSET_X2_LOWER_LIMIT = tFEOFFSET_X2_LOWER_LIMIT;
		}

		public Number getTFEOFFSET_Y2_UPPER_LIMIT()
		{
			return TFEOFFSET_Y2_UPPER_LIMIT;
		}

		public void setTFEOFFSET_Y2_UPPER_LIMIT(Number tFEOFFSET_Y2_UPPER_LIMIT)
		{
			TFEOFFSET_Y2_UPPER_LIMIT = tFEOFFSET_Y2_UPPER_LIMIT;
		}

		public Number getTFEOFFSET_Y2_LOWER_LIMIT()
		{
			return TFEOFFSET_Y2_LOWER_LIMIT;
		}

		public void setTFEOFFSET_Y2_LOWER_LIMIT(Number tFEOFFSET_Y2_LOWER_LIMIT)
		{
			TFEOFFSET_Y2_LOWER_LIMIT = tFEOFFSET_Y2_LOWER_LIMIT;
		}
		
	}

