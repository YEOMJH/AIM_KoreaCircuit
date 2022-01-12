package kr.co.aim.messolution.generic.object;

public class ObjectAttributeDef {
	private String typeName;
	private String attributeName;
	private int    position;
	private String primaryKeyFlag;
	private String attributeType;
	private String dataType;
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getPosition() {
		return position;
	}
	public void setPrimaryKeyFlag(String primaryKeyFlag) {
		this.primaryKeyFlag = primaryKeyFlag;
	}
	public String getPrimaryKeyFlag() {
		return primaryKeyFlag;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDataType() {
		return dataType;
	}
}
