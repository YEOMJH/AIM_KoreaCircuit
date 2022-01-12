package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;

public class MaskOffsetSpec extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="masklotname", type="Key", dataType="String", initial="", history="")
	private String masklotname;
	
	@CTORMTemplate(seq = "2", name="offset_x_upper_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_x_upper_limit;
	
	@CTORMTemplate(seq = "3", name="offset_x_lower_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_x_lower_limit;
	
	@CTORMTemplate(seq = "4", name="offset_y_upper_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_y_upper_limit;
	
	@CTORMTemplate(seq = "5", name="offset_y_lower_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_y_lower_limit;
	
	@CTORMTemplate(seq = "6", name="offset_theta_upper_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_theta_upper_limit;
	
	@CTORMTemplate(seq = "7", name="offset_theta_lower_limit", type="Column", dataType="Number", initial="", history="")
	private Number offset_theta_lower_limit;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "12", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "13", name="TFEOFFSET_X1_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_X1_UPPER_LIMIT;
	
	@CTORMTemplate(seq = "14", name="TFEOFFSET_X1_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_X1_LOWER_LIMIT;
	
	@CTORMTemplate(seq = "15", name="TFEOFFSET_Y1_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_Y1_UPPER_LIMIT;
	
	@CTORMTemplate(seq = "16", name="TFEOFFSET_Y1_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_Y1_LOWER_LIMIT;
	
	@CTORMTemplate(seq = "17", name="TFEOFFSET_X2_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_X2_UPPER_LIMIT;
	
	@CTORMTemplate(seq = "18", name="TFEOFFSET_X2_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_X2_LOWER_LIMIT;
	
	@CTORMTemplate(seq = "19", name="TFEOFFSET_Y2_UPPER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_Y2_UPPER_LIMIT;
	
	@CTORMTemplate(seq = "20", name="TFEOFFSET_Y2_LOWER_LIMIT", type="Column", dataType="Number", initial="", history="")
	private Number TFEOFFSET_Y2_LOWER_LIMIT;
	
	public String getMasklotname() {
		return masklotname;
	}

	public void setMasklotname(String masklotname) {
		this.masklotname = masklotname;
	}

	public Number getOffset_x_upper_limit() {
		return offset_x_upper_limit;
	}

	public void setOffset_x_upper_limit(Number offset_x_upper_limit) {
		this.offset_x_upper_limit = offset_x_upper_limit;
	}

	public Number getOffset_x_lower_limit() {
		return offset_x_lower_limit;
	}

	public void setOffset_x_lower_limit(Number offset_x_lower_limit) {
		this.offset_x_lower_limit = offset_x_lower_limit;
	}

	public Number getOffset_y_upper_limit() {
		return offset_y_upper_limit;
	}

	public void setOffset_y_upper_limit(Number offset_y_upper_limit) {
		this.offset_y_upper_limit = offset_y_upper_limit;
	}

	public Number getOffset_y_lower_limit() {
		return offset_y_lower_limit;
	}

	public void setOffset_y_lower_limit(Number offset_y_lower_limit) {
		this.offset_y_lower_limit = offset_y_lower_limit;
	}

	public Number getOffset_theta_upper_limit() {
		return offset_theta_upper_limit;
	}

	public void setOffset_theta_upper_limit(Number offset_theta_upper_limit) {
		this.offset_theta_upper_limit = offset_theta_upper_limit;
	}

	public Number getOffset_theta_lower_limit() {
		return offset_theta_lower_limit;
	}

	public void setOffset_theta_lower_limit(Number offset_theta_lower_limit) {
		this.offset_theta_lower_limit = offset_theta_lower_limit;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
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
