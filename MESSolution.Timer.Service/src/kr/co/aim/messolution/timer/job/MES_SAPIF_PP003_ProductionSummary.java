package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

public class MES_SAPIF_PP003_ProductionSummary implements Job, InitializingBean {
	
	private static Log log = LogFactory.getLog(MES_SAPIF_PP003_ProductionSummary.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		receiveInfoSummary();
		productionSummary();
		scrapSummary();
		reworkSummary();
		shipInfoSummary();
	}

	@SuppressWarnings("unchecked")
	private void productionSummary()
	{   
		log.info("ProductionSummary Start");
		String sql = " WITH PS1 AS "
				 + " (SELECT DISTINCT PRODUCTNAME,PRODUCTREQUESTNAME,PRODUCTTYPE,PROCESSOPERATIONNAME FROM ( "
				 + " SELECT  PRODUCTNAME, PR.SUPERPRODUCTREQUESTNAME PRODUCTREQUESTNAME,  PH.TIMEKEY, "
				 + " PR.PRODUCTTYPE,PH.OLDPROCESSOPERATIONNAME AS PROCESSOPERATIONNAME "
				 + " FROM PRODUCTHISTORY PH,PRODUCTREQUEST PR,PROCESSFLOW PF "
				 + " WHERE "
				 + " PF.FACTORYNAME = PH.FACTORYNAME "
				 + " AND PF.PROCESSFLOWNAME = PH.OLDPROCESSFLOWNAME "
				 + " AND PF.PROCESSFLOWVERSION = PH.OLDPROCESSFLOWVERSION "
				 + " AND PF.PROCESSFLOWTYPE='Main' "
				 + " AND PR.PRODUCTREQUESTNAME = PH.PRODUCTREQUESTNAME "
				 + " AND PR.SUBPRODUCTIONTYPE<>'SYZLC' "
				 + " AND (PH.DUMMYGLASSFLAG<>'Y' OR PH.DUMMYGLASSFLAG IS NULL) "
				 + " AND PH.EVENTNAME ='TrackOut' "
				 + " AND PH.PRODUCTTYPE != 'Panel' "
				 + " AND PH.PRODUCTIONTYPE IN ('P','E','T') "
				 + " AND PH.TIMEKEY BETWEEN TO_CHAR(SYSDATE - 1, 'YYYYMMDDHH24') || '0000000000' AND TO_CHAR(SYSDATE, 'YYYYMMDDHH24') || '0000000000') "
				 + " WHERE TIMEKEY>TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000'), "
				 + " PS2 AS "
				 + " (SELECT DISTINCT LOTNAME,PRODUCTREQUESTNAME,PRODUCTTYPE,PROCESSOPERATIONNAME,SUBPRODUCTIONTYPE FROM ( "
				 + " SELECT  LOTNAME, PR.SUPERPRODUCTREQUESTNAME PRODUCTREQUESTNAME, PH.TIMEKEY, "
				 + " PR.PRODUCTTYPE,PH.OLDPROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, "
				 + " PR.SUBPRODUCTIONTYPE "
				 + " FROM LOTHISTORY PH, PROCESSFLOW PF, PRODUCTREQUEST PR "
				 + " WHERE "
				 + " PF.FACTORYNAME = PH.FACTORYNAME "
				 + " AND PF.PROCESSFLOWNAME = PH.OLDPROCESSFLOWNAME "
				 + " AND PF.PROCESSFLOWVERSION = PH.OLDPROCESSFLOWVERSION "
				 + " AND PF.PROCESSFLOWTYPE='Main' "
				 + " AND PR.PRODUCTREQUESTNAME = PH.PRODUCTREQUESTNAME "
				 + " AND PR.SUBPRODUCTIONTYPE NOT IN ('LCFG','SYFG','SLCFG','SYZLC') "
				 + " AND PH.OLDPROCESSOPERATIONNAME IN ('31000','34000','32000','32001','35052','3S004','36010','36030','36040' ) "
				 + " AND PH.EVENTNAME IN('TrackOut','Packing' ) "
				 + " AND PH.PRODUCTTYPE = 'Panel' "
				 + " AND PH.PRODUCTIONTYPE IN ('P','E','T') "
				 + " AND PH.TIMEKEY BETWEEN TO_CHAR(SYSDATE - 10, 'YYYYMMDDHH24') || '0000000000' AND TO_CHAR(SYSDATE, 'YYYYMMDDHH24') || '0000000000'  ) "
				 + " WHERE TIMEKEY>TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000'), "
				 + " PS3 AS "
				 + " (SELECT DISTINCT LOTNAME,PRODUCTREQUESTNAME,PRODUCTTYPE,PROCESSOPERATIONNAME,SUBPRODUCTIONTYPE FROM ( "
				 + " SELECT  LOTNAME, PR.SUPERPRODUCTREQUESTNAME PRODUCTREQUESTNAME, PH.TIMEKEY, "
				 + " PR.PRODUCTTYPE,PH.OLDPROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, "
				 + " PR.SUBPRODUCTIONTYPE "
				 + " FROM LOTHISTORY PH, PROCESSFLOW PF, PRODUCTREQUEST PR "
				 + " WHERE "
				 + " PF.FACTORYNAME = PH.FACTORYNAME "
				 + " AND PF.PROCESSFLOWNAME = PH.OLDPROCESSFLOWNAME "
				 + " AND PF.PROCESSFLOWVERSION = PH.OLDPROCESSFLOWVERSION "
				 + " AND PF.PROCESSFLOWTYPE='Main' "
				 + " AND PR.PRODUCTREQUESTNAME = PH.PRODUCTREQUESTNAME "
				 + " AND PR.SUBPRODUCTIONTYPE IN ('LCFG','SYFG','SLCFG') "
				 + " AND PH.OLDPROCESSOPERATIONNAME IN ('31000','34000','32000','32001','35052','3S004','36010','36030','36040' ) "
				 + " AND PH.EVENTNAME IN('TrackOut','Packing' ) "
				 + " AND PH.PRODUCTTYPE = 'Panel' "
				 + " AND PH.PRODUCTIONTYPE IN ('P','E','T') "
				 + " AND PH.TIMEKEY BETWEEN TO_CHAR(SYSDATE - 10, 'YYYYMMDDHH24') || '0000000000' AND TO_CHAR(SYSDATE, 'YYYYMMDDHH24') || '0000000000'  ) "
				 + " WHERE TIMEKEY>TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000') "
				 + " SELECT PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,COUNT(PS1.PRODUCTNAME) AS QUANTITY,PS1.PRODUCTTYPE FROM PS1  WHERE PS1.PRODUCTNAME NOT IN ( "
				 + " SELECT PH2.PRODUCTNAME FROM PS1,PRODUCTHISTORY PH2,PROCESSFLOW PF2 "
				 + " WHERE PS1.PRODUCTNAME = PH2.PRODUCTNAME "
				 + " AND PH2.OLDPROCESSOPERATIONNAME=PS1.PROCESSOPERATIONNAME "
				 + " AND PH2.OLDPROCESSFLOWNAME=PF2.PROCESSFLOWNAME "
				 + " AND PF2.PROCESSFLOWTYPE='Main' "
				 + " AND PH2.EVENTNAME='TrackOut' "
				 + " AND PH2.TIMEKEY<TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000' ) "
				 + " GROUP BY  PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE "
				 + " UNION "
				 + " SELECT PS2.PRODUCTREQUESTNAME,PS2.PROCESSOPERATIONNAME,COUNT(PS2.LOTNAME) AS QUANTITY,PS2.PRODUCTTYPE FROM PS2  WHERE PS2.LOTNAME NOT IN ( "
				 + " SELECT PS2.LOTNAME FROM PS2,LOTHISTORY PH2 "
				 + " WHERE PS2.LOTNAME = PH2.LOTNAME "
				 + " AND PH2.OLDPROCESSOPERATIONNAME=PS2.PROCESSOPERATIONNAME "
				 + " AND PH2.EVENTNAME IN ('TrackOut','Packing') "
				 + " AND PH2.TIMEKEY<TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000' ) "
				 + " GROUP BY  PS2.PRODUCTREQUESTNAME,PS2.PROCESSOPERATIONNAME,PS2.PRODUCTTYPE "
				 + " UNION "
				 + " SELECT PS3.PRODUCTREQUESTNAME,PS3.PROCESSOPERATIONNAME,COUNT(PS3.LOTNAME) AS QUANTITY,PS3.PRODUCTTYPE FROM PS3  WHERE PS3.LOTNAME NOT IN ( "
				 + " SELECT PS3.LOTNAME FROM PS3,LOTHISTORY PH2,LOT L "
				 + " WHERE PS3.LOTNAME = PH2.LOTNAME "
				 + " AND PS3.LOTNAME = L.LOTNAME "
				 + " AND PH2.TIMEKEY> TO_CHAR(L.RELEASETIME, 'YYYYMMDDHH24')|| '0000000000' "
				 + " AND PH2.TIMEKEY<TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000' "
				 + " AND PH2.OLDPROCESSOPERATIONNAME=PS3.PROCESSOPERATIONNAME "
				 + " AND PH2.EVENTNAME IN ('TrackOut','Packing')) "
				 + " GROUP BY  PS3.PRODUCTREQUESTNAME,PS3.PROCESSOPERATIONNAME,PS3.PRODUCTTYPE ";

		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("Production Summary data is not exists.");
			return ;
		}

		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK.V3FAB.COM  "
						+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = resultData.get("PRODUCTREQUESTNAME").toString();
			String processOperationName = resultData.get("PROCESSOPERATIONNAME").toString();
			String productType = resultData.get("PRODUCTTYPE").toString();
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int productQuantity = Integer.valueOf(resultData.get("QUANTITY").toString()); 
			int scrapQuantity = 0;
			int reworkQuantity = 0;
			if(productQuantity==0) continue;

			if(StringUtils.equals(processOperationName, "32001")) processOperationName="32000";
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage };
			bindObjList.add(bindObj);
		}
		
		GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		log.info("ProductionSummary End");
	}
	
	private void scrapSummary()
	{
		log.info("ScrapSummary Start");
		String sql = " SELECT PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,PS1.PRODUCTSPECTYPE,PS1.SUBPRODUCTUNITQUANTITY1,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE,SUM(CASE WHEN PS1.EVENTNAME='Scrap' THEN 1 ELSE -1 END) AS SCRAPQUANTITY, "
				 + " 0 AS CUTSCRAPQUANTITY FROM "
				 + " (SELECT PR.FACTORYNAME, PRODUCTNAME, PR.SUPERPRODUCTREQUESTNAME PRODUCTREQUESTNAME, P.PRODUCTSPECTYPE, P.SUBPRODUCTUNITQUANTITY1, "
				 + " PR.PRODUCTTYPE, "
				 + " PH.LASTMAINOPERNAME AS PROCESSOPERATIONNAME, "
				 + " CASE WHEN PH.EVENTNAME IN('Scrap','ScrapLot','ScrapProduct') THEN 'Scrap' "
				 + " WHEN PH.EVENTNAME IN('UnScrap','UnScrapLot','UnScrapProduct') THEN 'UnScrap' "
				 + " ELSE PH.EVENTNAME "
				 + " END AS EVENTNAME "
				 + " FROM PRODUCTHISTORY PH, PRODUCTREQUEST PR,PRODUCTSPEC P "
				 + " WHERE 1 = 1 "
				 + " AND PH.TIMEKEY BETWEEN TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000' AND TO_CHAR(SYSDATE, 'YYYYMMDDHH24') || '0000000000' "
				 + " AND PH.EVENTNAME IN ('Scrap','ScrapLot','ScrapProduct','UnScrapProduct','UnScrap','UnScrapLot') "
				 + " AND PH.PRODUCTTYPE != 'Panel' "
				 + " AND PH.PRODUCTIONTYPE IN ('P','E','T') "
				 + " AND PR.PRODUCTREQUESTNAME = PH.PRODUCTREQUESTNAME "
				 + " AND PR.PRODUCTSPECNAME=P.PRODUCTSPECNAME "
				 + " AND PR.SUBPRODUCTIONTYPE<>'SYZLC' "
				 + " AND (PH.DUMMYGLASSFLAG<>'Y' OR PH.DUMMYGLASSFLAG IS NULL)) PS1 "
				 + " GROUP BY PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,PS1.PRODUCTSPECTYPE,PS1.SUBPRODUCTUNITQUANTITY1, PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE "
				 + " UNION "
				 + " SELECT  PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,'' AS PRODUCTSPECTYPE,0 AS SUBPRODUCTUNITQUANTITY1,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE,SUM(CASE WHEN PS1.EVENTNAME ='Scrap' AND PS1.PROCESSOPERATIONNAME!='3100S'  THEN 1 WHEN PS1.EVENTNAME ='UnScrap' THEN -1 ELSE 0 END )  AS SCRAPQUANTITY, "
				 + " SUM(CASE WHEN  PS1.PROCESSOPERATIONNAME='3100S'  THEN 1 ELSE 0 END  ) AS CUTSCRAPQUANTITY FROM ( "
				 + " SELECT B.FACTORYNAME, A.LOTNAME,A.PRODUCTREQUESTNAME,A.PRODUCTTYPE, "
				 + " CASE WHEN B.OLDPROCESSOPERATIONNAME IS NULL AND A.EVENTCOMMENT='ComponentScrap' AND A.OLDPROCESSOPERATIONNAME='31000' THEN '3100S' "
				 + " WHEN  B.OLDPROCESSOPERATIONNAME IS NOT NULL THEN B.OLDPROCESSOPERATIONNAME "
				 + " ELSE '31000' END  PROCESSOPERATIONNAME,A.EVENTNAME "
				 + " FROM (SELECT MAX (LH2.TIMEKEY) MAXTIMEKEY,LH2.LOTNAME,PI.PRODUCTREQUESTNAME,PI.PRODUCTTYPE,PI.EVENTNAME,PI.TIMEKEY,PI.OLDPROCESSOPERATIONNAME,PI.EVENTCOMMENT "
				 + " FROM LOTHISTORY LH2, "
				 + " (SELECT LH.TIMEKEY,LH.LOTNAME,PR.SUPERPRODUCTREQUESTNAME PRODUCTREQUESTNAME,PR.PRODUCTTYPE,LH.OLDPROCESSOPERATIONNAME,LH.EVENTCOMMENT, "
				 + " CASE WHEN LH.EVENTNAME IN ('Scrap', 'ScrapLot','ScrapProduct','TrayGroupScrap') THEN 'Scrap' "
				 + " WHEN LH.EVENTNAME IN ('UnScrap', 'UnScrapLot','UnScrapProduct') THEN 'UnScrap' "
				 + " ELSE LH.EVENTNAME END AS EVENTNAME "
				 + " FROM LOTHISTORY LH,PRODUCTREQUEST PR "
				 + " WHERE "
				 + " PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME "
				 + " AND PR.SUBPRODUCTIONTYPE NOT IN ('SYZLC','LCFG','SYFG','SLCFG') "
				 + " AND LH.EVENTNAME IN ('Scrap','ScrapLot','UnScrap','ScrapProduct','UnScrapProduct','UnScrapLot','TrayGroupScrap') "
				 + " AND LH.FACTORYNAME = 'POSTCELL' "
				 + " AND LH.PRODUCTTYPE = 'Panel' "
				 + " AND LH.PRODUCTIONTYPE IN ('P', 'E', 'T') "
				 + " AND LH.TIMEKEY BETWEEN    TO_CHAR (SYSDATE - 3,'YYYYMMDDHH24')|| '0000000000'AND    TO_CHAR (SYSDATE,'YYYYMMDDHH24')|| '0000000000' )PI "
				 + " WHERE     LH2.LOTNAME = PI.LOTNAME "
				 + " AND LH2.EVENTNAME IN ('TrackOut','Packing','Cut') "
				 + " AND LH2.TIMEKEY<=PI.TIMEKEY "
				 + " AND PI.TIMEKEY>TO_CHAR (SYSDATE - 1/24,'YYYYMMDDHH24')|| '0000000000' "
				 + " AND (LH2.OLDPROCESSOPERATIONNAME IN ('31000','34000','32000','32001','35052','3S004','36010','36030','36040' ) OR LH2.OLDPROCESSOPERATIONNAME IS NULL) "
				 + " GROUP BY LH2.LOTNAME,PI.PRODUCTREQUESTNAME,PI.PRODUCTTYPE,PI.EVENTNAME,PI.TIMEKEY,PI.OLDPROCESSOPERATIONNAME,PI.EVENTCOMMENT) A,LOTHISTORY  B "
				 + " WHERE     A.LOTNAME = B.LOTNAME AND B.TIMEKEY = A.MAXTIMEKEY "
				 + " AND B.EVENTNAME IN ('TrackOut','Packing','Cut')) PS1 "
				 + " GROUP BY PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE "
				 + " UNION "
				 + " SELECT PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,'' AS PRODUCTSPECTYPE,0 AS SUBPRODUCTUNITQUANTITY1,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE,SUM(CASE WHEN PS1.EVENTNAME ='Scrap' AND PS1.PROCESSOPERATIONNAME!='3100S' THEN 1  WHEN PS1.EVENTNAME ='UnScrap' AND PS1.PROCESSOPERATIONNAME!='3100S' THEN -1 ELSE 0 END )  AS SCRAPQUANTITY, "
				 + " SUM(CASE WHEN PS1.EVENTNAME ='Scrap' AND PS1.PROCESSOPERATIONNAME='3100S' THEN 1 WHEN PS1.EVENTNAME ='UnScrap' AND PS1.PROCESSOPERATIONNAME='3100S' THEN -1 ELSE 0 END ) AS CUTSCRAPQUANTITY FROM ( "
				 + " SELECT CASE WHEN OLDPROCESSOPERATIONNAME IS NULL OR OLDPROCESSOPERATIONNAME ='31000' OR MAINOPEREVENTNAME='PostCellRMA' THEN '3100S' ELSE OLDPROCESSOPERATIONNAME END AS PROCESSOPERATIONNAME,LOTNAME,PRODUCTREQUESTNAME,PRODUCTTYPE,EVENTNAME,TIMEKEY,FACTORYNAME "
				 + " FROM ( "
				 + " SELECT LH2.TIMEKEY TRACKOUTTIMEKEY,LH2.EVENTNAME AS MAINOPEREVENTNAME,PI.LOTNAME,PI.PRODUCTREQUESTNAME,PI.PRODUCTTYPE,PI.EVENTNAME,PI.TIMEKEY,LH2.OLDPROCESSOPERATIONNAME,PI.FACTORYNAME, "
				 + " ROW_NUMBER()OVER(PARTITION BY PI.LOTNAME,PI.PRODUCTREQUESTNAME,PI.PRODUCTTYPE,PI.EVENTNAME,PI.TIMEKEY ORDER BY LH2.TIMEKEY DESC) MAXROW "
				 + " FROM "
				 + " (SELECT LH.TIMEKEY,LH.LOTNAME,PR.SUPERPRODUCTREQUESTNAME AS PRODUCTREQUESTNAME,PR.PRODUCTTYPE,TO_CHAR(L.RELEASETIME,'YYYYMMDDHH24MISS') AS RELEASETIME,LH.FACTORYNAME, "
				 + " CASE WHEN LH.EVENTNAME IN ('Scrap', 'ScrapLot','ScrapProduct','TrayGroupScrap') THEN 'Scrap' "
				 + " WHEN LH.EVENTNAME IN ('UnScrap', 'UnScrapLot','UnScrapProduct') THEN 'UnScrap' "
				 + " ELSE LH.EVENTNAME END AS EVENTNAME "
				 + " FROM LOTHISTORY LH,LOT L   ,PRODUCTREQUEST PR "
				 + " WHERE "
				 + " PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME "
				 + " AND L.LOTNAME = LH.LOTNAME "
				 + " AND LH.PRODUCTIONTYPE IN ('P', 'E', 'T') "
				 + " AND PR.SUBPRODUCTIONTYPE  IN ('LCFG','SYFG','SLCFG') "
				 + " AND LH.EVENTNAME IN ('Scrap','ScrapLot','UnScrap','ScrapProduct','UnScrapProduct','UnScrapLot','TrayGroupScrap') "
				 + " AND LH.FACTORYNAME = 'POSTCELL' "
				 + " AND LH.PRODUCTTYPE = 'Panel' "
				 + " AND LH.TIMEKEY BETWEEN  TO_CHAR(SYSDATE - 3,'YYYYMMDDHH24')||'0000000000' AND TO_CHAR(SYSDATE,'YYYYMMDDHH24')||'0000000000' )PI "
				 + " LEFT JOIN LOTHISTORY LH2 "
				 + " ON LH2.LOTNAME = PI.LOTNAME "
				 + " AND LH2.EVENTNAME IN ('TrackOut','Packing','PostCellRMA') "
				 + " AND LH2.TIMEKEY<=PI.TIMEKEY "
				 + " AND LH2.TIMEKEY>=PI.RELEASETIME "
				 + " AND LH2.OLDPROCESSOPERATIONNAME IN ('31000','34000','32000','32001','35052','3S004','36010','36030','36040' ) "
				 + " ) A  WHERE MAXROW=1 and TIMEKEY>TO_CHAR(SYSDATE - 1/24,'YYYYMMDDHH24')||'0000000000') PS1 "
				 + " GROUP BY PS1.FACTORYNAME,PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE ";
				  
		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("Scrap Summary data is not exists.");
			return ;
		}

		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK.V3FAB.COM "
						+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = resultData.get("PRODUCTREQUESTNAME").toString();
			String processOperationName = resultData.get("PROCESSOPERATIONNAME").toString();
			String productType = resultData.get("PRODUCTTYPE").toString();
			String factoryName=resultData.get("FACTORYNAME").toString();
			String eventTime = TimeStampUtil.getCurrentTime();
			String productSpecTpe="";
			int subProductUnitQuantity=Integer.parseInt(resultData.get("SUBPRODUCTUNITQUANTITY1").toString());
			if(resultData.get("PRODUCTSPECTYPE")!=null&&StringUtils.isNotEmpty(resultData.get("PRODUCTSPECTYPE").toString()))
			{
				productSpecTpe=resultData.get("PRODUCTSPECTYPE").toString();
			}
			
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int scrapQty = Integer.valueOf(resultData.get("SCRAPQUANTITY").toString());
			int unScrapQty = Integer.valueOf(resultData.get("CUTSCRAPQUANTITY").toString());
			if(scrapQty==0&&unScrapQty==0)continue;
			
			int productQuantity = 0; 
			int scrapQuantity = 0;
			int reworkQuantity = 0;
			
			if(StringUtils.equals(processOperationName, "3100S"))
			{
				productQuantity=0;
				scrapQuantity=unScrapQty;
				processOperationName="31000";
			}
			else
			{
				productQuantity=-scrapQty;
				scrapQuantity=scrapQty;
			}
			
			if(StringUtils.equals(factoryName, "OLED")&& StringUtils.startsWith(processOperationName, "1"))
			{
				processOperationName="21100";
				productQuantity=0;
				scrapQuantity=scrapQuantity*2;
			}
			else if(StringUtils.equals(factoryName, "OLED")&& StringUtils.startsWith(processOperationName, "4"))
			{
				if(StringUtils.equals(productSpecTpe, "F"))
				{
					processOperationName="212S0";
				}
				else 
				{
					processOperationName="221S0";
				}
				productQuantity=0;

			}
			else if(StringUtils.equals(factoryName, "TP")&& StringUtils.startsWith(processOperationName, "2"))
			{
				processOperationName="4AAAA";
				productQuantity=0;
			}
			else if(StringUtils.equals(factoryName, "POSTCELL")&& StringUtils.startsWith(processOperationName, "2"))
			{
				processOperationName="31000";
				productQuantity=0;
				scrapQuantity=scrapQuantity*(subProductUnitQuantity/2);
			}
			
			if(StringUtils.equals(processOperationName, "32001")) processOperationName="32000";
			
			if(productQuantity==0&&scrapQuantity==0&&reworkQuantity==0)continue;
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage };
			bindObjList.add(bindObj);
		}
		
		GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		log.info("ScrapSummary End");
	}
	
	private void reworkSummary()
	{
		log.info("ReworkSummary Start");
		String sql = " SELECT PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE,COUNT(PS1.PRODUCTNAME) AS QUANTITY FROM (  "
				+ " SELECT  PRODUCTNAME, SR.PRODUCTREQUESTNAME,  "
				+ " 				                 SR.PRODUCTTYPE,  "
				+ " 				                 PH.LASTMAINOPERNAME AS PROCESSOPERATIONNAME  "
				+ " 				          FROM PRODUCTHISTORY PH, PROCESSFLOW PF, PROCESSOPERATIONSPEC PO, PRODUCTREQUEST PR, CT_SUPERPRODUCTREQUEST SR  "
				+ " 				          WHERE 1 = 1   "
				+ " 				            AND PH.TIMEKEY BETWEEN TO_CHAR(SYSDATE - 1/24, 'YYYYMMDDHH24') || '0000000000' AND TO_CHAR(SYSDATE, 'YYYYMMDDHH24') || '0000000000'   "
				+ " 				            AND PH.EVENTNAME ='TrackOut'  "
				+ " 				            AND PH.PRODUCTTYPE != 'Panel'  "
				+ " 				            AND PH.PRODUCTIONTYPE IN ('P','E','T')   "
				+ " 				            AND PF.FACTORYNAME = PH.FACTORYNAME   "
				+ " 				            AND PF.PROCESSFLOWNAME = PH.OLDPROCESSFLOWNAME   "
				+ " 				            AND PF.PROCESSFLOWVERSION = PH.OLDPROCESSFLOWVERSION   "
				+ " 				            AND PF.PROCESSFLOWTYPE='Rework'  "
				+ "				                AND PO.FACTORYNAME = PH.FACTORYNAME   "
				+ " 				            AND PO.PROCESSOPERATIONNAME = PH.OLDPROCESSOPERATIONNAME   "
				+ " 				            AND PO.PROCESSOPERATIONVERSION = PH.OLDPROCESSOPERATIONVERSION  "
				+ " 				            AND PR.PRODUCTREQUESTNAME = PH.PRODUCTREQUESTNAME  "
				+ " 				            AND SR.PRODUCTREQUESTNAME = PR.SUPERPRODUCTREQUESTNAME  "
				+ " 				            AND PR.SUBPRODUCTIONTYPE<>'SYZLC'  "
				//+ "                             AND SR.PRODUCTREQUESTSTATE <>'Completed'  "
				+ " 				            AND (PH.DUMMYGLASSFLAG<>'Y' OR PH.DUMMYGLASSFLAG IS NULL)  "
				+ " 				            AND PH.REWORKFLAG='Y'  "
				+ " 				            AND PO.PROCESSOPERATIONTYPE='Production'  "
				+ " 				            AND (PO.PROCESSOPERATIONNAME NOT LIKE '%603' OR PH.LASTMAINOPERNAME NOT LIKE '%400'))PS1  "
				+ " 				            GROUP BY PS1.PRODUCTREQUESTNAME,PS1.PROCESSOPERATIONNAME,PS1.PRODUCTTYPE  ";
		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("Rework Summary data is not exists.");
			return ;
		}

		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK.V3FAB.COM "
						+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = resultData.get("PRODUCTREQUESTNAME").toString();
			String processOperationName = resultData.get("PROCESSOPERATIONNAME").toString();
			String productType = resultData.get("PRODUCTTYPE").toString();
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int productQuantity = 0; 
			int scrapQuantity = 0;
			int reworkQuantity = Integer.valueOf(resultData.get("QUANTITY").toString());
			if(reworkQuantity==0) continue;
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage };
			bindObjList.add(bindObj);
		}
		
		GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		log.info("ReworkSummary End");
	}
	
	@SuppressWarnings("unchecked")
	private void shipInfoSummary()
	{
		log.info("ShipSummary Start");
		String sql = "SELECT PRODUCTREQUESTNAME, PRODUCTTYPE, '3X999' AS PROCESSOPERATIONNAME,  "
				   + "       SUM(CASE WHEN EVENTNAME ='Ship'  THEN 1 ELSE -1 END) AS OUT_QTY,  "
				   + "		0 AS UNPACK_QTY, 0 AS RW_QTY, 0 AS SCRAP_QTY, 0 AS UNSCRAP_QTY  "
				   + "		FROM ( "
				   + "                SELECT  LH.LOTNAME,SR.PRODUCTREQUESTNAME,SR.PRODUCTTYPE,  "
				   + "				        LH.OLDPROCESSOPERATIONNAME,LH.PROCESSOPERATIONNAME,PF.PROCESSFLOWTYPE,PO.ISMAINOPERATION,LH.PRODUCTQUANTITY,  "
				   + "				        CASE  WHEN LH.EVENTNAME IN ('InnerPackingShip', 'OuterPackingShip','PalletShip','DemoPackingShip','NGShip') THEN 'Ship'  "
				   + "				        WHEN LH.EVENTNAME IN ('InnerPackingUnShip', 'OuterPackingUnShip','PalletUnShip','DemoPackingUnShip','NGUnShip') THEN 'UnShip'  "
				   + "				        ELSE LH.EVENTNAME END AS EVENTNAME  "
				   + "				FROM LOTHISTORY LH, PROCESSFLOW PF,PROCESSOPERATIONSPEC PO,PRODUCTREQUEST PR,CT_SUPERPRODUCTREQUEST SR  "
				   + "				WHERE     1 = 1  "
				   + "			    AND LH.TIMEKEY BETWEEN    TO_CHAR (SYSDATE - 1/24 ,'YYYYMMDDHH24')|| '0000000000'AND    TO_CHAR (SYSDATE ,'YYYYMMDDHH24')|| '0000000000'  "
				   + "				AND LH.EVENTNAME IN  ('InnerPackingShip','InnerPackingUnShip','OuterPackingShip','OuterPackingUnShip','PalletShip','PalletUnShip' ,'DemoPackingShip','DemoPackingUnShip','NGShip','NGUnShip')  "
				   + "				AND LH.FACTORYNAME = 'POSTCELL'  "
				   + "				AND LH.PRODUCTTYPE = 'Panel'   "
				   + "				AND LH.PRODUCTIONTYPE IN ('P', 'E', 'T')  "
				   + "				AND PF.FACTORYNAME = LH.FACTORYNAME  "
				   + "				AND PF.PROCESSFLOWNAME = LH.OLDPROCESSFLOWNAME  "
				   + "				AND PF.PROCESSFLOWVERSION = LH.OLDPROCESSFLOWVERSION  "
				   + "				AND PO.FACTORYNAME = LH.FACTORYNAME  "
				   + "				AND PO.PROCESSOPERATIONNAME = LH.OLDPROCESSOPERATIONNAME  "
				   + "				AND PO.PROCESSOPERATIONVERSION =LH.OLDPROCESSOPERATIONVERSION  "
				   + "				AND PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME  "
				   + "				AND SR.PRODUCTREQUESTNAME = PR.SUPERPRODUCTREQUESTNAME  "
				   + "				AND PR.SUBPRODUCTIONTYPE<>'SYZLC'  "
				   + "				AND LH.LOTSTATE!='Scrapped' AND LH.LOTGRADE!='S' ) "
				   + "    GROUP BY PRODUCTREQUESTNAME, PRODUCTTYPE, '3X999' "
				   + "UNION "
				   + "SELECT PRODUCTREQUESTNAME, PRODUCTTYPE, PROCESSOPERATIONNAME,  "
				   + "	    SUM(CASE WHEN EVENTNAME  IN ('Ship','NGShip')  THEN PRODUCTQUANTITY ELSE -PRODUCTQUANTITY END) AS OUT_QTY,   "
				   + "		0 AS UNPACK_QTY, 0 AS RW_QTY, 0 AS SCRAP_QTY, 0 AS UNSCRAP_QTY  FROM(  "
				   + "                SELECT  LH.LOTNAME,SR.PRODUCTREQUESTNAME,SR.PRODUCTTYPE, LH.PRODUCTQUANTITY, "
				   + "				        LH.PROCESSOPERATIONNAME, EVENTNAME "
				   + "				FROM LOTHISTORY LH, PROCESSFLOW PF,PROCESSOPERATIONSPEC PO,PRODUCTREQUEST PR,CT_SUPERPRODUCTREQUEST SR  "
				   + "				WHERE     1 = 1  "
				   + "			    AND LH.TIMEKEY BETWEEN    TO_CHAR (SYSDATE - 1/24 ,'YYYYMMDDHH24')|| '0000000000'AND    TO_CHAR (SYSDATE ,'YYYYMMDDHH24')|| '0000000000'  "
				   + "				AND LH.EVENTNAME IN  ('Ship','UnShip','NGShip','NGUnShip' )  "
				   + "				AND LH.PRODUCTTYPE != 'Panel'  "
				   + "				AND LH.PRODUCTIONTYPE IN ('P', 'E', 'T')  "
				   + "				AND PF.FACTORYNAME = LH.FACTORYNAME "
				   + "				AND PF.PROCESSFLOWNAME = LH.OLDPROCESSFLOWNAME  "
				   + "				AND PF.PROCESSFLOWVERSION = LH.OLDPROCESSFLOWVERSION  "
				   + "				AND PO.FACTORYNAME = LH.FACTORYNAME  "
				   + "				AND PO.PROCESSOPERATIONNAME = LH.OLDPROCESSOPERATIONNAME "
				   + "				AND PO.PROCESSOPERATIONVERSION =LH.OLDPROCESSOPERATIONVERSION  "
				   + "				AND PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME  "
				   + "				AND SR.PRODUCTREQUESTNAME = PR.SUPERPRODUCTREQUESTNAME  "
				   + "				AND PR.SUBPRODUCTIONTYPE<>'SYZLC') "
				   + "    GROUP BY PRODUCTREQUESTNAME, PRODUCTTYPE, PROCESSOPERATIONNAME ";
		
		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("ShipInfo Summary data is not exists.");
			return ;
		}

		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK.V3FAB.COM "
						+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = resultData.get("PRODUCTREQUESTNAME").toString();
			String processOperationName = resultData.get("PROCESSOPERATIONNAME").toString();
			String productType = resultData.get("PRODUCTTYPE").toString();
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int outQty = Integer.valueOf(resultData.get("OUT_QTY").toString());
			
			int productQuantity = outQty ;
			int scrapQuantity = 0;
			int reworkQuantity = 0;
		
			if(productQuantity==0&&scrapQuantity==0&&reworkQuantity==0)continue;
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage };
			bindObjList.add(bindObj);
		}
		
		GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		log.info("ShipSummary End");
	}
	
	private void receiveInfoSummary()
	{
		log.info("ReceiveSummary Start");
		String sql = "  SELECT PS.PRODUCTREQUESTNAME,  "
				   + "         CASE  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '21%' THEN '21ZZZ'  "
				   + "	           WHEN PS.PROCESSOPERATIONNAME LIKE '22%' THEN '22ZZZ' "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '4%' THEN '4ZZZZ'  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '3%' THEN '3ZZZZ'  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '1%' THEN '1ZZZZ'  "
				   + "         END AS PROCESSOPERATIONNAME,  "
				   + "         SUM (PS.PRODUCTQUANTITY) AS PRODUCTQUANTITY,PS.PRODUCTTYPE  "
				   + "    FROM (SELECT SR.PRODUCTREQUESTNAME,  "
				   + "                 CASE  "
				   + "                     WHEN LH.EVENTNAME IN ('Receive', 'Release') THEN LH.PROCESSOPERATIONNAME  "
				   + "                     ELSE LH.OLDPROCESSOPERATIONNAME END AS PROCESSOPERATIONNAME,  "
				   + "                 CASE WHEN LH.EVENTNAME IN ('Receive', 'Release') AND LH.PROCESSOPERATIONNAME LIKE '21%' THEN LH.PRODUCTQUANTITY*2  "
				   + "                 WHEN LH.EVENTNAME IN ('Receive', 'Release') AND LH.PROCESSOPERATIONNAME LIKE '3%' THEN LH.PRODUCTQUANTITY*(P. SUBPRODUCTUNITQUANTITY1/2)   "
				   + "                 WHEN LH.EVENTNAME = 'CancelReceive' AND LH.OLDPROCESSOPERATIONNAME LIKE '21%' THEN -LH.PRODUCTQUANTITY*2  "
				   + "                 WHEN LH.EVENTNAME = 'CancelReceive' AND LH.OLDPROCESSOPERATIONNAME LIKE '3%' THEN -LH.PRODUCTQUANTITY*(P. SUBPRODUCTUNITQUANTITY1/2)  "
				   + "                 WHEN LH.EVENTNAME IN ('Receive', 'Release') AND (LH.PROCESSOPERATIONNAME  LIKE '1%' OR LH.PROCESSOPERATIONNAME  LIKE '4%' OR LH.PROCESSOPERATIONNAME  LIKE '22%') THEN LH.PRODUCTQUANTITY  "
				   + "                 WHEN LH.EVENTNAME = 'CancelReceive' AND (LH.OLDPROCESSOPERATIONNAME  LIKE '1%' OR LH.OLDPROCESSOPERATIONNAME  LIKE '4%' OR LH.OLDPROCESSOPERATIONNAME  LIKE '22%') THEN -LH.PRODUCTQUANTITY "
				   + "                 END PRODUCTQUANTITY,  "
				   + "                 SR.PRODUCTTYPE,LH.EVENTNAME  "
				   + "            FROM LOTHISTORY LH, PRODUCTREQUEST PR, CT_SUPERPRODUCTREQUEST SR,PRODUCTSPEC P  "
				   + "           WHERE     (   (    LH.EVENTNAME = 'Receive'  "
				   + "                          AND PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME)  "
				   + "                      OR (    LH.EVENTNAME = 'CancelReceive'  "
				   + "                          AND PR.PRODUCTREQUESTNAME = LH.OLDPRODUCTREQUESTNAME) "
				   + "                      OR (    LH.EVENTNAME = 'Release' "
				   + "                          AND PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME"
				   + "                          AND LH.FACTORYNAME = 'ARRAY'))  "
				   + "                 AND LH.TIMEKEY BETWEEN    TO_CHAR (SYSDATE - 1/24 ,'YYYYMMDDHH24')|| '0000000000'AND    TO_CHAR (SYSDATE ,'YYYYMMDDHH24')|| '0000000000'   "
				   + "                 AND LH.PRODUCTTYPE != 'Panel' "
				   + "                 AND SR.PRODUCTREQUESTTYPE IN ('P', 'E', 'T') "
				   + "                 AND SR.PRODUCTREQUESTNAME = PR.SUPERPRODUCTREQUESTNAME "
				   + "                 AND P.PRODUCTSPECNAME = SR.PRODUCTSPECNAME "
				   + "                 AND PR.SUPERPRODUCTREQUESTNAME IS NOT NULL  "
				   + "                 AND PR.SUBPRODUCTIONTYPE <> 'SYZLC') PS "
				   + " GROUP BY PS.PRODUCTREQUESTNAME,  "
				   + "         CASE  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '21%' THEN '21ZZZ'  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '22%' THEN '22ZZZ'  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '4%' THEN '4ZZZZ' "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '3%' THEN '3ZZZZ'  "
				   + "             WHEN PS.PROCESSOPERATIONNAME LIKE '1%' THEN '1ZZZZ' END, "
				   + "         PS.PRODUCTTYPE "
				   + "         UNION "
				   + "  SELECT SR.PRODUCTREQUESTNAME, "
				   + "         '3ZZZZ'                AS PROCESSOPERATIONNAME,  "
				   + "         COUNT (LH.LOTNAME)     PRODUCTQUANTITY,  "
				   + "         SR.PRODUCTTYPE "
				   + "    FROM LOTHISTORY LH, PRODUCTREQUEST PR, CT_SUPERPRODUCTREQUEST SR "
				   + "   WHERE     LH.EVENTNAME = 'PostCellRMA' "
				   + "         AND LH.TIMEKEY BETWEEN    TO_CHAR (SYSDATE - 1/24 ,'YYYYMMDDHH24')|| '0000000000'AND    TO_CHAR (SYSDATE ,'YYYYMMDDHH24')|| '0000000000' "
				   + "         AND PR.PRODUCTREQUESTNAME = LH.PRODUCTREQUESTNAME "
				   + "         AND LH.PRODUCTTYPE = 'Panel' "
				   + "         AND SR.PRODUCTREQUESTTYPE IN ('P', 'E', 'T') "
				   + "         AND SR.PRODUCTREQUESTNAME = PR.SUPERPRODUCTREQUESTNAME "
				   + "         AND PR.SUPERPRODUCTREQUESTNAME IS NOT NULL "
				   + "         AND PR.SUBPRODUCTIONTYPE <> 'SYZLC' "
				   + " GROUP BY SR.PRODUCTREQUESTNAME, SR.PRODUCTTYPE ";
		
		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("receiveInfo Summary data is not exists.");
			return ;
		}

		String batchSql = "INSERT INTO MES_SAPIF_PP003@OADBLINK.V3FAB.COM "
						+ "(SEQ, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTITY, SCRAPQUANTITY, REWORKQUANTITY, PRODUCTTYPE, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		List<Object[]> bindObjList = new ArrayList<Object[]>();
		
		for (OrderedMap resultData : resultDataList) 
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String seq = TimeStampUtil.getCurrentEventTimeKey();
			String productRequestName = resultData.get("PRODUCTREQUESTNAME").toString();
			String processOperationName = resultData.get("PROCESSOPERATIONNAME").toString();
			String productType = resultData.get("PRODUCTTYPE").toString();
			String eventTime = TimeStampUtil.getCurrentTime();
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				eventTime=receiveTime.toString();
			}
			String esbFlag = "N";
			String result = "";
			String resultMessage = "";
			
			int productQuantity = Integer.valueOf(resultData.get("PRODUCTQUANTITY").toString()) ;
			int scrapQuantity = 0;
			int reworkQuantity = 0;
		
			if(productQuantity==0&&scrapQuantity==0&&reworkQuantity==0)continue;
			
			Object[] bindObj = new Object[] { seq, productRequestName, processOperationName, productQuantity, 
											  scrapQuantity, reworkQuantity, productType, eventTime, esbFlag, 
											  result, resultMessage };
			bindObjList.add(bindObj);
		}
		
		GenericServiceProxy.getSqlMesTemplate().updateBatch(batchSql, bindObjList);
		log.info("ReceiveSummary End");
	}
}
