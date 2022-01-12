package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class LamiFilmInfoRequest extends SyncHandler 
{
	Log log = LogFactory.getLog(this.getClass());

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LamiFilmInfoReply");

			prepareReplyBody(doc);
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(boxName);

			this.machineCommonCheck(machineData);
			this.filmBoxCommonCheck(durableData);

			// get work in process lot
			List<Lot> onEQLotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListOnTheEQP(machineData.getFactoryName(), machineName);

			// get residual film on the machine.
			List<Consumable> onEQFilmList = this.getOnEQFilmList(machineData, portName);

			// get film data list by boxName
			List<Consumable> filmDataList = MESConsumableServiceProxy.getConsumableInfoUtil().getFilmListByBoxDESC(boxName);
			this.checkFilmMultiSpec(filmDataList);
			
			// check different spec with remain film
			if (onEQFilmList != null && onEQFilmList.size() > 0)
				this.checkDiffSpecWithOnEQFilm(onEQFilmList.get(0), filmDataList.get(0));

			// check different spec with lot of wip
			if(onEQLotDataList!=null && onEQLotDataList.size()>0 && filmDataList != null && filmDataList.size() > 0)
			{
				this.checkBomTP(onEQLotDataList.get(0), filmDataList.get(0), machineData);
				
				//The Film's vendor of TopLamination must be same with BottomLamination
				if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
				{
					for(int i=0;i<onEQLotDataList.size();i++)
					{
						this.materialVendorCheck(onEQLotDataList.get(i), filmDataList);
					}
				}
				//end
			}
				

			this.setReplyBodyItemValue(doc, this.getMaterialType(machineData), filmDataList.size());

			for (Consumable filmData : filmDataList)
			{
				// check film request from right machine
				if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination)
						&& !StringUtils.equals(filmData.getConsumableType(), "TopLamination"))
				{
					throw new CustomException("MATERIAL-0008");
				}
				else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination)
						&& !StringUtils.equals(filmData.getConsumableType(), "BottomLamination"))
				{
					throw new CustomException("MATERIAL-0009");
				}

				// check film state
				this.filmCommonCheck(filmData);

				setMaterialItemValue(doc, filmData);
			}

			setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}
		return doc;
	}

	private void machineCommonCheck(Machine machineData) throws CustomException
	{
		// check machine is not onhold state
		CommonValidation.checkMachineHold(machineData);

		// check message from Lamination machine
		if (this.getMaterialType(machineData).equals("Invalid"))
		{
			//LamiFilmInfoRequest message was not from Lamination machine.
			throw new CustomException("FILM-0009");
		}
	}

	private void filmBoxCommonCheck(Durable durableData) throws CustomException
	{
		// check is film Box
		CommonValidation.checkFilmBox(durableData);
		// check film cst state is inuse
		CommonValidation.checkFilmCSTStateIsInuse(durableData);
		// check film cst is not scrapped
		CommonValidation.CheckDurableState(durableData);
		// check film cst is not onHold
		CommonValidation.CheckDurableHoldState(durableData);
		// check film cst is not dirty
		CommonValidation.CheckDurableCleanState(durableData);
	}

	private void filmCommonCheck(Consumable filmData) throws CustomException
	{
		// check film stat is Available
		CommonValidation.checkFilmInUseState(filmData);
		// check film qty is not zero
		CommonValidation.checkFilmQtyIsNotEmpty(filmData);
	}

	private void checkFilmMultiSpec(List<Consumable> filmDataList) throws CustomException
	{
		String factoryName = "";
		String materialSpecName = "";
		String materialSpecVersion = "";

		for (Consumable filmData : filmDataList)
		{
			if (factoryName.isEmpty() && materialSpecName.isEmpty() && materialSpecVersion.isEmpty())
			{
				factoryName = filmData.getFactoryName();
				materialSpecName = filmData.getConsumableSpecName();
				materialSpecVersion = filmData.getConsumableSpecVersion();
				continue;
			}

			if (!(factoryName.equals(filmData.getFactoryName()) && materialSpecName.equals(filmData.getConsumableSpecName()) && materialSpecVersion.equals(filmData.getConsumableSpecVersion())))
			{
				//FILM-0010: The Film Box [{0}] contains multiple spec films.
				throw new CustomException("FILM-0010", filmData.getUdfs().get("CARRIERNAME"));
			}
		}
	}

	private void checkDiffSpecWithOnEQFilm(Consumable onEQFilm, Consumable validateFilm) throws CustomException
	{
		if (!(onEQFilm.getFactoryName().equals(validateFilm.getFactoryName()) && onEQFilm.getConsumableSpecName().equals(validateFilm.getConsumableSpecName())
																			  && onEQFilm.getConsumableSpecVersion().equals(validateFilm.getConsumableSpecVersion())))
		{
			//FILM-0011:Diffrent Spec: OnEQP Film with BC Report Film
			throw new CustomException("FILM-0011");
		}
	}
	
	private void checkBomTP(Lot lotData, Consumable filmData, Machine machineData) throws CustomException
	{
		//****************************check在AllowedLamiFilmSpec Enum表是否按Lot指定FilmSpec*****************************//
		String enumSql = "SELECT A.ENUMNAME,A.ENUMVALUE,A.DEFAULTFLAG,A.DESCRIPTION,A.DISPLAYCOLOR,A.SEQ " 
			       + " FROM ENUMDEFVALUE A " 
			       + " WHERE A.ENUMNAME = 'AllowedLamiFilmSpec' " 
			       + " AND A.ENUMVALUE = :LOTNAME " ;
		
		Map<String,Object>objectMap = new HashMap<String,Object>();
		objectMap.put("LOTNAME", lotData.getKey().getLotName());
		
		if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination))
		{
			enumSql += " AND A.DEFAULTFLAG = :TOPLAMINATION ";
			objectMap.put("TOPLAMINATION", "TopLamination");
		}
		
		if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
		{
			enumSql += " AND A.SEQ = :BOTTOMLAMITION ";
			objectMap.put("BOTTOMLAMITION", "BottomLamination");
		}
		
		List<Map<String, Object>> enumResult = null;
		try
		{
			enumResult = GenericServiceProxy.getSqlMesTemplate().queryForList(enumSql, objectMap);
		}
		catch (Exception ex)
		{
			log.info("Not Exist Enum:AllowedLamiFilmSpec Data!");
		}
		
		if(enumResult!=null&&enumResult.size()>0)
		{
			if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination))
			{
				if(enumResult.get(0).get("DISPLAYCOLOR").equals(filmData.getConsumableSpecName()))
				{
					return;
				}
				else
				{
					//EnumName:AllowedLamiFilmSpec Master Data not Matched, Lot: {0}, MaterialSpec: {1}, MatchedMaterialSpec: {2}
					throw new CustomException("MATERIAL-0041", lotData.getKey().getLotName(), filmData.getConsumableSpecName(),enumResult.get(0).get("DISPLAYCOLOR"));
				}
			}
			
			if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
			{
				if(enumResult.get(0).get("DESCRIPTION").equals(filmData.getConsumableSpecName()))
				{
					return;
				}
				else
				{
					//EnumName:AllowedLamiFilmSpec Master Data not Matched, Lot: {0}, MaterialSpec: {1}, MatchedMaterialSpec: {2}
					throw new CustomException("MATERIAL-0041", lotData.getKey().getLotName(), filmData.getConsumableSpecName(),enumResult.get(0).get("DESCRIPTION"));
				}
			}
			
		}
		
		//***********************************Check TP+BOM中是否维护ProductSpec与FilmSpec匹配关系*****************************//
		String sql = "SELECT DISTINCT 1 " 
			       + " FROM TPPOLICY TP, POSBOM PB " 
			       + " WHERE TP.CONDITIONID = PB.CONDITIONID " 
			       + " AND TP.FACTORYNAME = :FACTORYNAME " 
			       + " AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME"
			       + " AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION"
			       + " AND PB.MATERIALFACTORYNAME = :MATERIALFACTORYNAME " 
			       + " AND PB.MATERIALSPECNAME = :MATERIALSPECNAME "
			       + " AND PB.MATERIALSPECVERSION = :MATERIALSPECVERSION ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("MATERIALFACTORYNAME", filmData.getFactoryName());
		bindMap.put("MATERIALSPECNAME", filmData.getConsumableSpecName());
		bindMap.put("MATERIALSPECVERSION", filmData.getConsumableSpecVersion());

		List<Map<String, Object>> sqlResult = null;

		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		if (sqlResult == null || sqlResult.size() < 1)
			throw new CustomException("MATERIAL-0024", lotData.getKey().getLotName(), lotData.getProductSpecName(), filmData.getConsumableSpecName());
	}

	private List<Consumable> getOnEQFilmList(Machine machineData, String portName) throws CustomException
	{
		List<Consumable> consumableDataList = null;
		try
		{
			String condition = " WHERE CARRIERNAME IN (SELECT DURABLENAME FROM DURABLE WHERE 1=1 AND MACHINENAME = ? AND DURABLETYPE = 'FilmBox' AND PORTNAME <> ? AND UPPER(TRANSPORTSTATE) = 'ONEQP')";
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineData.getKey().getMachineName(), portName });
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format("No film loaded on macine.[MachineName = %s] ", machineData.getKey().getMachineName()));
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		return consumableDataList;
	}

	private String getMaterialType(Machine machineData)
	{
		String filmType = "Invalid";

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_1stLamination))
			filmType = "TopLamination";
		else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination))
			filmType = "BottomLamination";
		else
			log.info(String.format("LamiFilmInfoRequest message from invalid machine.[MachineName = %s]", machineData.getKey().getMachineName()));

		return filmType;
	}

	private Document setReplyBodyItemValue(Document doc, String materialType, int materialQuantity) throws CustomException
	{
		SMessageUtil.setBodyItemValue(doc, "MATERIALTYPE", materialType);
		SMessageUtil.setBodyItemValue(doc, "QUANTITY", String.valueOf(materialQuantity));

		return doc;
	}
	
	private Document prepareReplyBody(Document doc) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("MATERIALTYPE"));
		bodyElement.addContent(new Element("QUANTITY"));
		bodyElement.addContent(new Element("MATERIALLIST"));

		return doc;
	}

	private Document setMaterialItemValue(Document doc, Consumable filmData) throws CustomException
	{
		Element materialListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "MATERIALLIST", true);

		Element materialElement = new Element("MATERIAL");
		materialElement.addContent(new Element("MATERIALNAME").setText(filmData.getKey().getConsumableName()));
		materialElement.addContent(new Element("SEQUENCE").setText(filmData.getUdfs().get("SEQ")));
		materialElement.addContent(new Element("QUANTITY").setText(String.valueOf(Math.round(filmData.getQuantity()))));

		materialListElement.addContent(materialElement);

		return doc;
	}

	private Document setResultItemValue(Document doc, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
	
	public void materialVendorCheck(Lot lotData,List<Consumable>filmDataList) throws CustomException 
	{
		//**********************************上贴膜与下贴膜厂商必须一致Check************************************//
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT DISTINCT C.VENDOR ");
		sql.append(" FROM CT_MATERIALPRODUCT A ,PRODUCT B,CONSUMABLESPEC C,CONSUMABLE D WHERE 1=1 ");
		sql.append(" AND D.CONSUMABLESPECNAME = C.CONSUMABLESPECNAME ");
		sql.append(" AND C.CONSUMABLETYPE IN ('TopLamination','BottomLamination') ");
		sql.append(" AND A.MATERIALNAME = D.CONSUMABLENAME ");
		sql.append(" AND A.TIMEKEY > TO_CHAR(SYSDATE-30,'yyyyMMddHH24miss') ");
		sql.append(" AND A.PRODUCTNAME = B.PRODUCTNAME ");
		sql.append(" AND B.LOTNAME = :LOTNAME ");
		sql.append(" AND C.FACTORYNAME = 'OLED' ");
		
		Map<String,Object>bindMap = new HashMap<String,Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		List<Map<String,Object>>sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		if(sqlResult.size()>1)
		{
			throw new CustomException("FILM-0012",lotData.getCarrierName());
		}
		else if(sqlResult.size()>0)
		{
			String topLaminationVendor = sqlResult.get(0).get("VENDOR").toString();
			List<String>consumableSpecList = new ArrayList<>();
			for(int i = 0;i<filmDataList.size();i++)
			{
				String consumableSpec = filmDataList.get(i).getConsumableSpecName();
				if(!consumableSpecList.contains(consumableSpec))
				{
					consumableSpecList.add(consumableSpec);
				}
			}
			
			StringBuilder consumableSpecSQL = new StringBuilder();
			consumableSpecSQL.append(" SELECT DISTINCT A.VENDOR FROM CONSUMABLESPEC A WHERE 1=1 ");
			consumableSpecSQL.append(" AND A.CONSUMABLESPECNAME IN ( :CONSUMABLESPECNAME) ");
			consumableSpecSQL.append(" AND A.CONSUMABLETYPE IN ('TopLamination','BottomLamination') ");
			consumableSpecSQL.append(" AND A.FACTORYNAME = 'OLED' ");
			
			Map<String,Object>consumableMap = new HashMap<String,Object>();
			consumableMap.put("CONSUMABLESPECNAME", consumableSpecList);
			List<Map<String,Object>>consumableResult = GenericServiceProxy.getSqlMesTemplate().queryForList(consumableSpecSQL.toString(), consumableMap);
			if(consumableResult.size()>1)
			{
				throw new CustomException("FILM-0013",filmDataList.get(0).getUdfs().get("CARRIERNAME"));
			}
			else if(consumableResult.size()>0)
			{
				String bottomLaminationVendor = consumableResult.get(0).get("VENDOR")==null?null:consumableResult.get(0).get("VENDOR").toString();
				if(!StringUtils.equals(topLaminationVendor, bottomLaminationVendor))
				{
					throw new CustomException("FILM-0014",lotData.getCarrierName(),topLaminationVendor,filmDataList.get(0).getUdfs().get("CARRIERNAME"),bottomLaminationVendor);
				}
			}
		}
	}
}
