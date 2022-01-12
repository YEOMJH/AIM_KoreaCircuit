package kr.co.aim.messolution.fmb.service.impl;

import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.fmb.service.Db;
import kr.co.aim.messolution.fmb.service.FmbService;
import kr.co.aim.greentrack.user.management.impl.EncryptUtil;

public class FmbServiceImpl implements FmbService {
	public Db db;
	public Map<String, String> shopBayMap;
	public String preFixSubject = "FMBC";
	public void setDb(Db db) {
		this.db = db;
	}
	public String getPreFixSubject(){
		return preFixSubject;
	}
	public String getPreFixSubject(String temp){
		return preFixSubject;
	}
	public void setPreFixSubject(String preFixSubject) {
		this.preFixSubject = preFixSubject;
	}
	public String getShopBay(String preFix, String machineName){
		if(machineName == null || machineName.isEmpty()){
			return preFix;
		}
		if(shopBayMap == null){
			shopBayMap = db.getShopBayMap();
		}
		String sb = shopBayMap.get(machineName);
		if(sb == null){
			shopBayMap = db.getShopBayMap();
			sb = shopBayMap.get(machineName);
		}
		if(sb == null){
			return preFix;
		}
		return preFix+"."+sb;
	}
	public Document userLogin(Document doc, String userId, String passwd) {
		String password = db.getUserPassword(userId);
		String returnCode;
		String returnMessage;
		if(password == null) {
			returnCode = "1";
			returnMessage = "Can not find User.";
			DocMessageUtil.setReturnElement(doc, returnCode, returnMessage);
			
			return doc;
		}
		String decrypt = EncryptUtil.decrypt(password);
		if(!decrypt.equals(passwd)) {
			returnCode = "2";
			returnMessage = "Password is wrong.";
			DocMessageUtil.setReturnElement(doc, returnCode, returnMessage);
		}else {
			returnCode = "0";
			returnMessage = "";
			DocMessageUtil.setReturnElement(doc, returnCode, returnMessage);
		}
		return doc;
		
	}
}
