package kr.co.aim.messolution.fmb.service;

import java.util.Map;

import org.jdom.Document;


public interface Db  {

	public Document queryForBindv(Document doc, String queryID, String version,
			Object binds);

	public String getQuery(String queryID, String version);

	public Document updateForBindv(Document doc, String queryID, String version,
			Object binds);

	public String getUserPassword(String userId);
	
	public Document getUserDocument(Document doc, String queryID, String version);
	
	public String getShopBay(String machineName);

	public Map<String, String> getShopBayMap();
}
