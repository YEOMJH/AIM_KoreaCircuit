package kr.co.aim.messolution.generic.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class MigrateInfra extends SyncHandler {

	private String Char_Blank = " ";

	@Override
	public Document doWorks(Document doc) throws CustomException
	{

		String typeName = SMessageUtil.getBodyItemValue(doc, "TYPENAME", true);
		String dbLinkName = SMessageUtil.getBodyItemValue(doc, "DBLINKNAME", true);
		String TYPE = SMessageUtil.getBodyItemValue(doc, "TYPE", true);
		String ConnectionInfo = SMessageUtil.getBodyItemValue(doc, "ConnectionInfo", true);
		String insertQuery ;
		String updateQuery;
		// field reflection
		List<ObjectAttributeDef> result = getFieldList(typeName);
		if(ConnectionInfo.equals("DEV")||ConnectionInfo.equals("DEFAULT"))
		{
			
			 if(TYPE.equals("TST2DEV"))
	          {
	        	  insertQuery= generateInsertQueryTST2DEV(typeName, dbLinkName, result);
	        	  updateQuery = generateUpdateQueryTST2DEV(typeName, dbLinkName, result);
	          }
	          else
	          {
	        	  insertQuery= generateInsertQuery(typeName, dbLinkName, result);
	        	  updateQuery = generateUpdateQuery(typeName, dbLinkName, result);
	          }
		}else
		
			{
				
				 if(TYPE.equals("DEV2TST"))
		          {
		        	  insertQuery= generateInsertQueryTST2DEV(typeName, dbLinkName, result);
		        	  updateQuery = generateUpdateQueryTST2DEV(typeName, dbLinkName, result);
		          }
		          else
		          {
		        	  insertQuery= generateInsertQuery(typeName, dbLinkName, result);
		        	  updateQuery = generateUpdateQuery(typeName, dbLinkName, result);
		          }
			}
         
		 
		

		List<Object[]> insertArgList = new ArrayList<Object[]>();
		List<Object[]> updateArgList = new ArrayList<Object[]>();

		for (Element eleData : SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true))
		{
			try
			{
				String flag = SMessageUtil.getChildText(eleData, "FLAG", true);

				// commulate arguments by job
				if (flag.equals("N"))
				{
					Object[] bindList = generateBindList(eleData, 1);
					insertArgList.add(bindList);
				}
				else if (flag.equals("M"))
				{
					// duplicate bind variable for update query
					Object[] bindList = generateBindList(eleData, 2);
					updateArgList.add(bindList);
				}
			}
			catch (Exception ex)
			{
				eventLog.error(ex);
			}
		}

		// execution partition
		if (insertArgList.size() > 0)
			GenericServiceProxy.getSqlMesTemplate().updateBatch(insertQuery, insertArgList);
		if (updateArgList.size() > 0)
			GenericServiceProxy.getSqlMesTemplate().updateBatch(updateQuery, updateArgList);

		// reduce size
		doc.removeContent(SMessageUtil.getBodyElement(doc));

		return doc;
	}

	private Object[] generateBindList(Element eleData, int duplication)
	{
		List<Object> bindList = new ArrayList<Object>();

		for (Element key : (List<Element>) eleData.getChildren())
		{
			if (key.getName().equalsIgnoreCase("FLAG") || key.getName().equalsIgnoreCase("CHECKBOX"))
				continue;
			else
				// keeping sequence from OIC
				bindList.add(key.getValue());
		}

		for (int idx = 0; idx < duplication - 1; idx++)
		{
			bindList.addAll(bindList);
		}

		return bindList.toArray();
	}

	private String generateUpdateQuery(String tableName, String dbLinkName, List<ObjectAttributeDef> columnList)
	{
		StringBuilder condBuilder = new StringBuilder();
		StringBuilder setBuilder = new StringBuilder();
		StringBuilder colBuilder = new StringBuilder();

		for (ObjectAttributeDef column : columnList)
		{
			if (column.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			{
				condBuilder.append("AND").append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(Char_Blank);
			}
			else
			{
				setBuilder.append(Char_Blank).append(column.getAttributeName()).append(",");
				colBuilder.append(column.getAttributeName()).append(",");
			}
		}

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("UPDATE").append(Char_Blank).append(tableName).append("@").append(dbLinkName).append(Char_Blank).append("SET").append("(")
				.append(StringUtil.removeEnd(setBuilder.toString(), ",")).append(")").append("=").append("(").append(Char_Blank).append("SELECT").append(Char_Blank)
				.append(StringUtil.removeEnd(colBuilder.toString(), ",")).append(Char_Blank).append("FROM").append(Char_Blank).append(tableName).append(Char_Blank).append("WHERE").append(Char_Blank)
				.append("1=1").append(Char_Blank).append(condBuilder.toString()).append(Char_Blank).append(")").append(Char_Blank).append("WHERE").append(Char_Blank).append("1=1").append(Char_Blank)
				.append(condBuilder.toString());

		return sqlBuffer.toString();
	}
	
	private String generateUpdateQueryTST2DEV(String tableName, String dbLinkName, List<ObjectAttributeDef> columnList)
	{
		StringBuilder condBuilder = new StringBuilder();
		StringBuilder setBuilder = new StringBuilder();
		StringBuilder colBuilder = new StringBuilder();

		for (ObjectAttributeDef column : columnList)
		{
			if (column.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			{
				condBuilder.append("AND").append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(Char_Blank);
			}
			else
			{
				setBuilder.append(Char_Blank).append(column.getAttributeName()).append(",");
				colBuilder.append(column.getAttributeName()).append(",");
			}
		}

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("UPDATE").append(Char_Blank).append(tableName).append(Char_Blank).append("SET").append("(")
				.append(StringUtil.removeEnd(setBuilder.toString(), ",")).append(")").append("=").append("(").append(Char_Blank).append("SELECT").append(Char_Blank)
				.append(StringUtil.removeEnd(colBuilder.toString(), ",")).append(Char_Blank).append("FROM").append(Char_Blank).append(tableName).append("@").append(dbLinkName).append(Char_Blank).append("WHERE").append(Char_Blank)
				.append("1=1").append(Char_Blank).append(condBuilder.toString()).append(Char_Blank).append(")").append(Char_Blank).append("WHERE").append(Char_Blank).append("1=1").append(Char_Blank)
				.append(condBuilder.toString());

		return sqlBuffer.toString();
	}

	private String generateInsertQuery(String tableName, String dbLinkName, List<ObjectAttributeDef> columnList)
	{
		StringBuilder condBuilder = new StringBuilder();
		StringBuilder setBuilder = new StringBuilder();
		StringBuilder colBuilder = new StringBuilder();

		for (ObjectAttributeDef column : columnList)
		{
			if (column.getPrimaryKeyFlag().equalsIgnoreCase("y"))
				condBuilder.append("AND").append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(Char_Blank);
			else
				setBuilder.append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(",");

			colBuilder.append(column.getAttributeName()).append(",");
		}

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("INSERT").append(Char_Blank).append("INTO").append(Char_Blank).append(tableName).append("@").append(dbLinkName).append(Char_Blank).append("(").append(Char_Blank)
				.append("SELECT").append(Char_Blank).append(StringUtil.removeEnd(colBuilder.toString(), ",")).append(Char_Blank).append("FROM").append(Char_Blank).append(tableName).append(Char_Blank)
				.append("WHERE").append(Char_Blank).append("1=1").append(Char_Blank).append(condBuilder.toString()).append(Char_Blank).append(")");

		return sqlBuffer.toString();
	}
	
	private String generateInsertQueryTST2DEV(String tableName, String dbLinkName, List<ObjectAttributeDef> columnList)
	{
		StringBuilder condBuilder = new StringBuilder();
		StringBuilder setBuilder = new StringBuilder();
		StringBuilder colBuilder = new StringBuilder();

		for (ObjectAttributeDef column : columnList)
		{
			if (column.getPrimaryKeyFlag().equalsIgnoreCase("y"))
				condBuilder.append("AND").append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(Char_Blank);
			else
				setBuilder.append(Char_Blank).append(column.getAttributeName()).append("=").append("?").append(",");

			colBuilder.append(column.getAttributeName()).append(",");
		}

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("INSERT").append(Char_Blank).append("INTO").append(Char_Blank).append(tableName).append(Char_Blank).append("(").append(Char_Blank)
				.append("SELECT").append(Char_Blank).append(StringUtil.removeEnd(colBuilder.toString(), ",")).append(Char_Blank).append("FROM").append(Char_Blank).append(tableName).append("@").append(dbLinkName).append(Char_Blank)
				.append("WHERE").append(Char_Blank).append("1=1").append(Char_Blank).append(condBuilder.toString()).append(Char_Blank).append(")");

		return sqlBuffer.toString();
	}

	private List<ObjectAttributeDef> getFieldList(String typeName)
	{
		List<ObjectAttributeDef> columnList = new ArrayList<ObjectAttributeDef>();

		List<ObjectAttributeDef> result = greenFrameServiceProxy.getObjectAttributeMap().getMap().get(typeName + ".Standard");

		if (result != null)
			columnList.addAll(result);

		result = greenFrameServiceProxy.getObjectAttributeMap().getMap().get(typeName + ".ExtendedC");

		if (result != null)
			columnList.addAll(result);

		return columnList;
	}
}
