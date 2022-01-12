package kr.co.aim.messolution.fmb.service;

import kr.co.aim.messolution.fmb.service.impl.DocMessageUtil;
import kr.co.aim.greentrack.user.management.impl.EncryptUtil;

import org.jdom.Document;

public interface FmbService {

	public Document userLogin(Document doc, String userId, String passwd);

	public String getShopBay(String preFix, String machineName);

	public String getPreFixSubject();

	public String getPreFixSubject(String temp);

	public void setPreFixSubject(String preFixSubject);

}
