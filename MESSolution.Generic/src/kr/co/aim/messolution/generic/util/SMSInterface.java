package kr.co.aim.messolution.generic.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.X509TrustManager;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

class SMSSender {
	SMSSender()
	{
	};

	private int conTimeOut = 30000;
	private int readTimeOut = 30000;
	private int retryCount = 3;
	Log log = LogFactory.getLog(SMSSender.class);

	public String request(String url, String data) throws HttpException, IOException
	{

		String response = null;
		PostMethod postMethod = null;
		try
		{

			HttpClient client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
			postMethod = new PostMethod(url);
			postMethod.setRequestHeader("Connection", "close");
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
			byte[] byteData = data.getBytes("utf-8");

			RequestEntity requestEntity = new ByteArrayRequestEntity(byteData);
			// String res = new String(byteData, "UTF-8");
			postMethod.setRequestEntity(requestEntity);
			HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();
			postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));
			managerParams.setConnectionTimeout(conTimeOut);
			managerParams.setSoTimeout(readTimeOut);
			client.executeMethod(postMethod);
			if (postMethod.getStatusCode() == HttpStatus.SC_OK)
			{
				response = postMethod.getResponseBodyAsString();
				log.info("request url " + response);
			}
			else
			{
				log.error("reponse url " + postMethod.getStatusCode() + postMethod.getResponseBodyAsString());
			}

		}
		finally
		{
			if (postMethod != null)
				postMethod.releaseConnection();
		}
		return response;

	}
}

class SMSAuthenticator {
	// private static final String SEND_URL = "/json/sms/Submit";
	private static Log log = LogFactory.getLog(SMSAuthenticator.class);
	String account;

	String passWord;
	String url;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	String sql = "SELECT ENUMNAME ,ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME IN ('SMSIF_PASSWORD', 'SMSIF_ACCOUONT', 'SMSIF_URL') ";

	public String getAccount()
	{
		return account;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public String getPassWord()
	{
		return passWord;
	}

	public void setPassWord(String passWord)
	{
		this.passWord = passWord;
	}

	@SuppressWarnings("unchecked")
	public SMSAuthenticator()
	{

		List<Map<String, Object>> alist = null;
		try
		{

			alist = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
			if (alist == null || alist.size() == 0)
			{
				log.info("SMSAuthenticator dataInfo is empty");
				SetDefineData();
				return;
			}
			if (alist.size() > 0)
			{
				for (Map<String, Object> group : alist)
				{
					this.setPassWord(group.get("SMSIF_PASSWORD").toString());
					this.setAccount(group.get("SMSIF_ACCOUONT").toString());
					this.url = group.get("SMSIF_URL").toString().endsWith("/json/sms/Submit") ? group.get("SMSIF_URL").toString() : group.get("SMSIF_URL").toString() + "/json/sms/Submit";
				}
			}
		}
		catch (FrameworkErrorSignal e)
		{
			// log.info(e);
			e.printStackTrace();
			SetDefineData();
		}

	}

	private void SetDefineData()
	{
		//this.setUrl("http://www.dh3t.com/json/sms/Submit");//118.178.35.87
		this.setUrl("http://118.178.35.87/json/sms/Submit");
		this.setAccount("dh21000");
		this.setPassWord("dh21000.com");
	}
}

public class SMSInterface {
	private SMSAuthenticator smsAuth;
	private SMSSender smsSend;
	private static Log log = LogFactory.getLog(SMSInterface.class);

	// singleton
	private SMSAuthenticator getSMSAuth()
	{
		if (smsAuth == null)
		{
			return smsAuth = new SMSAuthenticator();
		}

		return smsAuth;
	}

	private SMSSender getSmsSend()
	{
		if (smsSend == null)
		{
			return smsSend = new SMSSender();
		}
		return smsSend;
	}

	public void AlarmSmsSend(String alarmCode, String alarmType, String message) throws CustomException
	{

		try
		{
			String msgid = UUID.randomUUID().toString().replace("-", "");
			String[] eList = getPhoneList(alarmType);
			List<String> toUserPhoneList = new ArrayList<String>();
			for (String toUserPhone : eList)
			{
				toUserPhoneList.add(toUserPhone);
			}

			JSONObject param = new JSONObject();
			param.put("account", getSMSAuth().getAccount());
			param.put("password", EncryptUtil.MD5Encode(getSMSAuth().getPassWord()));
			param.put("msgid", msgid);
			param.put("phones", StringUtils.collectionToCommaDelimitedString(toUserPhoneList));
			param.put("content", message);
			param.put("sign", "");
			param.put("subcode", "");
			param.put("sendtime", "");
			String requestData = param.toString();
			getSmsSend().request(getSMSAuth().getUrl(), requestData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// 2020/12/22 hankun
	public void sendMessage(StringBuffer smsMessageInfo, List<String> phoneList, String machineName ,String headName)
			throws CustomException 
	{
		if (phoneList != null && phoneList.size() > 0) 
		{
			try 
			{
				StringBuffer message = new StringBuffer();
				message.append("\n");
				message.append("\n");
				message.append(headName);
				message.append("\n");
				message.append("\n");
				message.append(machineName);
				message.append("\n");
				message.append(smsMessageInfo);

				String msgid = UUID.randomUUID().toString().replace("-", "");
				JSONObject param = new JSONObject();
				
				param.put("account", getSMSAuth().getAccount());
				param.put("password", EncryptUtil.MD5Encode(getSMSAuth().getPassWord()));
				param.put("msgid", msgid);
				param.put("phones", StringUtils.collectionToCommaDelimitedString(phoneList));
				param.put("content", message.toString());
				param.put("sign", "");
				param.put("subcode", "");
				param.put("sendtime", "");
				
				String requestData = param.toString();
				getSmsSend().request(getSMSAuth().getUrl(), requestData);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void TransportSmsSend(String alarmCode, String alarmType, String message, String transportjobName, String transportjobType, String jobState, String sendemailFlag) throws CustomException
	{

		try
		{
			String msgid = UUID.randomUUID().toString().replace("-", "");
			String[] eList = getPhoneList(alarmType);
			List<String> toUserPhoneList = new ArrayList<String>();
			for (String toUserPhone : eList)
			{
				toUserPhoneList.add(toUserPhone);
			}

			JSONObject param = new JSONObject();
			param.put("account", getSMSAuth().getAccount());
			param.put("password", EncryptUtil.MD5Encode(getSMSAuth().getPassWord()));
			param.put("msgid", msgid);
			param.put("phones", StringUtils.collectionToCommaDelimitedString(toUserPhoneList));
			param.put("content", message);
			param.put("sign", "");
			param.put("subcode", "");
			param.put("sendtime", "");
			String requestData = param.toString();
			getSmsSend().request(getSMSAuth().getUrl(), requestData);
			updateFlag(transportjobName, transportjobType, jobState, sendemailFlag);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void updateFlag(String transportjobName, String transportjobType, String jobState, String sendemailFlag) throws CustomException
	{
		try
		{
			String SQL = "UPDATE CT_TRANSPORTJOBCOMMAND SET SENDEMAILFLAG = :SENDEMAILFLAG  WHERE TRANSPORTJOBNAME = :TRANSPORTJOBNAME  AND TRANSPORTJOBTYPE=:TRANSPORTJOBTYPE AND JOBSTATE=:JOBSTATE";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TRANSPORTJOBNAME", transportjobName);
			bindMap.put("TRANSPORTJOBTYPE", transportjobType);
			bindMap.put("JOBSTATE", jobState);
			bindMap.put("SENDEMAILFLAG", sendemailFlag);
			GenericServiceProxy.getSqlMesTemplate().update(SQL, bindMap);
			log.info("UPDATE SENDEMAILFLAG Sucess");
		}
		catch (Exception ex)
		{
			log.info("UPDATE SENDEMAILFLAG Failed");
		}
	}

	public void AlarmSmsSend(String alarmCode, String alarmType, String factoryName, String machineName, String unitName, String message)
	{
		try
		{
			String msgid = UUID.randomUUID().toString().replace("-", "");
			List<String> phoneList = getPhoneList(alarmCode, alarmType, factoryName, machineName, unitName);

			JSONObject param = new JSONObject();
			param.put("account", getSMSAuth().getAccount());
			param.put("password", EncryptUtil.MD5Encode(getSMSAuth().getPassWord()));
			param.put("msgid", msgid);
			param.put("phones", StringUtils.collectionToCommaDelimitedString(phoneList));
			param.put("content", message);
			param.put("sign", "");
			param.put("subcode", "");
			param.put("sendtime", "");
			String requestData = param.toString();
			getSmsSend().request(getSMSAuth().getUrl(), requestData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String[] getPhoneList(String alarmType) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT UP.PHONENUMBER ");
		sql.append("  FROM CT_ALARMGROUP AG, CT_ALARMUSERGROUP AU, USERPROFILE UP ");
		sql.append(" WHERE AG.ALARMTYPE = :ALARMTYPE ");
		sql.append("   AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME ");
		sql.append("   AND AU.USERID = UP.USERID ");
		sql.append("   AND UP.PHONENUMBER IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMTYPE", alarmType);

		List<Map<String, Object>> sqlResult = null;
		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (sqlResult == null || sqlResult.size() == 0)
			return null;

		return CommonUtil.makeListBySqlResult(sqlResult, "PHONENUMBER").toArray(new String[] {});
	}

	@SuppressWarnings("unchecked")
	public static List<String> getPhoneList(String alarmCode, String alarmType, String factoryName, String machineName, String unitName)
	{
		log.debug(String.format("AlarmCode[%s], AlarmType[%s], Shop[%s], Machine[%s], Unit[%s]", alarmCode, alarmType, factoryName, machineName, unitName));

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT U.PHONENUMBER ");
		sql.append("  FROM CT_ALARMMAILACTION MA, CT_ALARMUSERGROUP UG, CT_ALARMUSER U ");
		sql.append(" WHERE MA.ALARMCODE = :ALARMCODE ");
		sql.append("   AND MA.ALARMTYPE = :ALARMTYPE ");
		sql.append("   AND MA.FACTORYNAME IN ( :FACTORYNAME, NULL) ");
		sql.append("   AND MA.MACHINENAME = :MACHINENAME ");
		sql.append("   AND MA.USERGROUPNAME = UG.USERGROUPNAME ");
		sql.append("   AND U.PHONENUMBER IS NOT NULL ");
		sql.append("   AND UG.USERGROUPNAME = U.USERGROUPNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMCODE", alarmCode);
		args.put("ALARMTYPE", alarmType);
		args.put("FACTORYNAME", factoryName);

		List<String> phoneNumberList = new ArrayList<String>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

		if (StringUtil.isNotEmpty(unitName))
		{
			log.debug("Get PhoneNumber List by Unit");

			args.put("MACHINENAME", unitName);
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult.size() > 0)
			{
				phoneNumberList = CommonUtil.makeListBySqlResult(sqlResult, "PHONENUMBER");
			}
		}

		if (phoneNumberList.size() == 0)
		{
			log.debug("Get PhoneNumber List by Machine");

			args.put("MACHINENAME", machineName);
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult.size() > 0)
			{
				phoneNumberList = CommonUtil.makeListBySqlResult(sqlResult, "PHONENUMBER");
			}
			else
			{
				log.debug("Get PhoneNumber List for not defined EQP");

				args.put("MACHINENAME", "-");
				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (sqlResult.size() > 0)
				{
					phoneNumberList = CommonUtil.makeListBySqlResult(sqlResult, "PHONENUMBER");
				}
			}
		}

		return phoneNumberList;
	}
// 2020/11/3 jinlj add #start
	public void indexSmsSend(String alarmGroupName, String smsMessage)throws CustomException {
		try
		{
			String msgid = UUID.randomUUID().toString().replace("-", "");
			List<String> eList = getEQPPhoneList(alarmGroupName);
			List<String> toUserPhoneList = new ArrayList<String>();
			for (String toUserPhone : eList)
			{
				toUserPhoneList.add(toUserPhone);
			}
			JSONObject param = new JSONObject();
			param.put("account", getSMSAuth().getAccount());
			param.put("password", EncryptUtil.MD5Encode(getSMSAuth().getPassWord()));
			param.put("msgid", msgid);
			param.put("phones", StringUtils.collectionToCommaDelimitedString(toUserPhoneList));
			param.put("content", smsMessage);
			param.put("sign", "");
			param.put("subcode", "");
			param.put("sendtime", "");
			String requestData = param.toString();
			getSmsSend().request(getSMSAuth().getUrl(), requestData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		
	}
	public static List<String> getEQPPhoneList(String alarmGroupName) {
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT AG.ALARMGROUPNAME,AU.USERID,AU.PHONENUMBER  ");
			sql.append(" FROM CT_ALARMUSERGROUP AG ,CT_ALARMUSER AU ");
			sql.append(" WHERE AG.ALARMGROUPNAME = :ALARMGROUPNAME ");
			sql.append("   AND AG.USERID = AU.USERID ");
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("ALARMGROUPNAME", alarmGroupName);
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(),args);
	
			List<String> phoneList = new ArrayList<String>();
			if (sqlResult.size() > 0) 
			{
				for (Map<String, Object> group : sqlResult) 
				{
                    String phoneNumber = ConvertUtil.getMapValueByName(group, "PHONENUMBER");
						phoneList.add(phoneNumber);
				}
			}
			return phoneList;
		}
	}
//#end
class MyX509TrustManager implements X509TrustManager {

	/*
	 * (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert. X509Certificate[], java.lang.String)
	 */
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
	{

	}

	/*
	 * (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert. X509Certificate[], java.lang.String)
	 */
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
	{

	}

	/*
	 * (non-Javadoc)
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public X509Certificate[] getAcceptedIssuers()
	{
		return null;
	}

}

class EncryptUtil {
	public static String MD5Encode(String sourceString)
	{
		String resultString = null;
		try
		{
			resultString = new String(sourceString);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byte2hexString(md.digest(resultString.getBytes()));
		}
		catch (Exception localException)
		{
		}
		return resultString;
	}

	private static final String byte2hexString(byte[] bytes)
	{
		StringBuffer bf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++)
		{
			if ((bytes[i] & 0xFF) < 16)
			{
				bf.append("0");
			}
			bf.append(Long.toString(bytes[i] & 0xFF, 16));
		}
		return bf.toString();
	}
}
