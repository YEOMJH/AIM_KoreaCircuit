package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

	public class MaskSubSpec extends UdfAccessor {
		
		@CTORMTemplate(seq = "1", name="maskSubSpecName", type="Key", dataType="String", initial="", history="")
		private String maskSubSpecName;

		@CTORMTemplate(seq = "2", name="maskSpecName", type="Column", dataType="String", initial="", history="")
		private String maskSpecName;

		@CTORMTemplate(seq = "3", name="maskFilmLayer", type="Column", dataType="String", initial="", history="")
		private String maskFilmLayer;

		@CTORMTemplate(seq = "4", name="maskKind", type="Column", dataType="String", initial="", history="")
		private String maskKind;

		@CTORMTemplate(seq = "5", name="maskType", type="Column", dataType="String", initial="", history="")
		private String maskType;

		@CTORMTemplate(seq = "6", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
		private Timestamp lastEventTime;

		@CTORMTemplate(seq = "7", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
		private String lastEventUser;

		@CTORMTemplate(seq = "8", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
		private String lastEventTimeKey;

		@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
		private String lastEventComment;

		@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="N")
		private String lastEventName;
		
		public MaskSubSpec()
		{
	
		}
		
		public MaskSubSpec(String maskSubSpecName)
		{
			setMaskSubSpecName(maskSubSpecName);
		}

		public String getMaskSubSpecName()
		{
			return maskSubSpecName;
		}

		public void setMaskSubSpecName(String maskSubSpecName)
		{
			this.maskSubSpecName = maskSubSpecName;
		}

		public String getMaskSpecName()
		{
			return maskSpecName;
		}

		public void setMaskSpecName(String maskSpecName)
		{
			this.maskSpecName = maskSpecName;
		}

		public Timestamp getLastEventTime()
		{
			return lastEventTime;
		}

		public void setLastEventTime(Timestamp lastEventTime)
		{
			this.lastEventTime = lastEventTime;
		}

		public String getLastEventUser()
		{
			return lastEventUser;
		}

		public void setLastEventUser(String lastEventUser)
		{
			this.lastEventUser = lastEventUser;
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

		public String getLastEventName()
		{
			return lastEventName;
		}

		public void setLastEventName(String lastEventName)
		{
			this.lastEventName = lastEventName;
		}

		public String getMaskFilmLayer() {
			return maskFilmLayer;
		}

		public void setMaskFilmLayer(String maskFilmLayer) {
			this.maskFilmLayer = maskFilmLayer;
		}

		public String getMaskKind() {
			return maskKind;
		}

		public void setMaskKind(String maskKind) {
			this.maskKind = maskKind;
		}

		public String getMaskType() {
			return maskType;
		}

		public void setMaskType(String maskType) {
			this.maskType = maskType;
		}
		
	}

