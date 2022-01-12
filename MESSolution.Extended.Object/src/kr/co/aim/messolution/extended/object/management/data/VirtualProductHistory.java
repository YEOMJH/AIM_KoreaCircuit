package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class VirtualProductHistory extends UdfAccessor {
	
		@CTORMTemplate(seq = "1", name="TIMEKEY", type="Key", dataType="String", initial="", history="")
		private String TIMEKEY;
		@CTORMTemplate(seq = "2", name="PRODUCTNAME", type="Column", dataType="String", initial="", history="")
		private String PRODUCTNAME;
		@CTORMTemplate(seq = "3", name="MACHINENAME", type="Column", dataType="String", initial="", history="")
		private String MACHINENAME;
		@CTORMTemplate(seq = "4", name="EVENTUSER", type="Column", dataType="String", initial="", history="")
		private String EVENTUSER;
		
		public String getMACHINENAME() {
			return MACHINENAME;
		}
		public void setMACHINENAME(String mACHINENAME) {
			MACHINENAME = mACHINENAME;
		}
		@CTORMTemplate(seq = "5", name="EVENTNAME", type="Column", dataType="String", initial="", history="")
		private String EVENTNAME;
		public String getTIMEKEY() {
			return TIMEKEY;
		}
		public void setTIMEKEY(String tIMEKEY) {
			TIMEKEY = tIMEKEY;
		}
		public String getPRODUCTNAME() {
			return PRODUCTNAME;
		}
		public void setPRODUCTNAME(String pRODUCTNAME) {
			PRODUCTNAME = pRODUCTNAME;
		}
		
		
		public String getEVENTUSER() {
			return EVENTUSER;
		}
		public void setEVENTUSER(String eVENTUSER) {
			EVENTUSER = eVENTUSER;
		}
		public String getEVENTNAME() {
			return EVENTNAME;
		}
		public void setEVENTNAME(String eVENTNAME) {
			EVENTNAME = eVENTNAME;
		}
		


}
