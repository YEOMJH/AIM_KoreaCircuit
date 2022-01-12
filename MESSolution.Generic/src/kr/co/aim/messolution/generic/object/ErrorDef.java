package kr.co.aim.messolution.generic.object;

public class ErrorDef {
	private String errorCode;
	private String kor_errorMessage;
	private String eng_errorMessage;
	private String cha_errorMessage;
	private String loc_errorMessage;
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getKor_errorMessage() {
		return kor_errorMessage;
	}
	public void setKor_errorMessage(String kor_errorMessage) {
		this.kor_errorMessage = kor_errorMessage;
	}
	public String getEng_errorMessage() {
		return eng_errorMessage;
	}
	public void setEng_errorMessage(String eng_errorMessage) {
		this.eng_errorMessage = eng_errorMessage;
	}
	public String getCha_errorMessage() {
		return cha_errorMessage;
	}
	public void setCha_errorMessage(String cha_errorMessage) {
		this.cha_errorMessage = cha_errorMessage;
	}
	public String getLoc_errorMessage() {
		return loc_errorMessage;
	}
	public void setLoc_errorMessage(String loc_errorMessage) {
		this.loc_errorMessage = loc_errorMessage;
	}

}
