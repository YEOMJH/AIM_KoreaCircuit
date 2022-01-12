package kr.co.aim.messolution.generic.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class MailAttachmentGenerator  {

	Log log = LogFactory.getLog(MailAttachmentGenerator.class);

	public enum MailContentType {
		Excel("application/msexcel",".xlsx"), 
		Xml("text/xml",".xml"), 
		Zip("application/x-zip-compressed",".zip"), 
		Html("text/html",".html"), 
		Txt("text/plain",".txt");

		private String[] name;
		private MailContentType(String... arg) {
			this.name = arg;
		}

		public static boolean validate(String inputArg) {
			if (inputArg == null || StringUtil.isEmpty(inputArg))
				return false;

			MailContentType[] values = MailContentType.values();

			for (MailContentType var : values) {
				if (var.name.equals(inputArg))
					return true;
			}

			return false;
		}

		public String getContentType() 
		{
            return this.name[0];
		}
		
		public String getExtension()
		{
			 return this.name[1];
		}
		
		public static String getExtensionByContentType(String inputArg)
		{
			if (inputArg == null || StringUtil.isEmpty(inputArg))
				return "";

			MailContentType[] values = MailContentType.values();

			for (MailContentType var : values) {
				if (var.name[0].equals(inputArg))
					return var.name[1];
			}

			return "";
		}
	}
	
	public ByteArrayDataSource createShipLotExcel(List<Lot> lotList) throws CustomException {

		if (lotList == null || lotList.size() == 0)return null;
		
		List<String> lotColumns = getTableColumns("LOT");
		List<String> productColumns = getTableColumns("PRODUCT");
		HSSFWorkbook workBook = new HSSFWorkbook();
		
		HSSFFont headFont = workBook.createFont();
		headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headFont.setFontHeightInPoints((short)10);
		headFont.setColor(HSSFColor.BLACK.index);
		
		HSSFFont commonFont = workBook.createFont();
		commonFont.setFontHeightInPoints((short)8);
		commonFont.setColor(HSSFColor.BLACK.index);
		
		HSSFCellStyle commonCellStyle = workBook.createCellStyle();
		commonCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		commonCellStyle.setFont(commonFont);

		HSSFCellStyle headCellStyle = workBook.createCellStyle();
		headCellStyle.setFont(headFont);
		headCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headCellStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
		
		List<String> lotNameList = CommonUtil.makeToStringList(lotList);
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAMELIST", lotNameList);
		
		List<Map<String,Object>> lotMapList = null;
		try {
			lotMapList = GenericServiceProxy.getSqlMesTemplate().queryForList("SELECT * FROM LOT WHERE 1=1 AND LOTNAME IN (:LOTNAMELIST )", bindMap);
		} catch (Exception ex) {
			throw new CustomException(ex.getCause());
		}
	   
		Sheet lotSheet = workBook.createSheet("LotList");
		Row lHeadRow = lotSheet.createRow(0);

		for (int i = 0; i < lotColumns.size(); i++) {
			Cell headcell = lHeadRow.createCell(i);
			headcell.setCellStyle(headCellStyle);
			headcell.setCellValue(lotColumns.get(i));
		}

		for (int r = 0; r < lotMapList.size(); r++) {
			Row row = lotSheet.createRow(r + 1);

			for (int c = 0; c < lotColumns.size(); c++) {
				Cell cell = row.createCell(c);
				cell.setCellStyle(commonCellStyle);
				cell.setCellValue(ConvertUtil.getMapValueByName(lotMapList.get(r), lotColumns.get(c)));

				if (lotMapList.size() - 1 == r) {
					lotSheet.autoSizeColumn(c);
				}
			}
		}
		
		List<Map<String,Object>> productMapList = null;
		try {
			productMapList = GenericServiceProxy.getSqlMesTemplate().queryForList("SELECT * FROM PRODUCT WHERE 1=1 AND LOTNAME IN (:LOTNAMELIST )", bindMap);
		} catch (Exception ex) {
			throw new CustomException(ex.getCause());
		}
		
		Sheet productSheet = workBook.createSheet("ProductList");
		Row pHeadRow = productSheet.createRow(0);

		for (int i = 0; i < productColumns.size(); i++) {
			Cell headcell = pHeadRow.createCell(i);
			headcell.setCellStyle(headCellStyle);
			headcell.setCellValue(productColumns.get(i));
		}

		for (int r = 0; r < productMapList.size(); r++) {
			Row row = productSheet.createRow(r + 1);

			for (int c = 0; c < productColumns.size(); c++) {
				Cell cell = row.createCell(c);
				cell.setCellStyle(commonCellStyle);
				cell.setCellValue(ConvertUtil.getMapValueByName(productMapList.get(r), productColumns.get(c)));

				if (productMapList.size() - 1 == r) {
					productSheet.autoSizeColumn(c);
				}
			}
		}
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			workBook.write(byteStream);
			byteStream.flush();
			byteStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return  new ByteArrayDataSource(byteStream.toByteArray(), MailContentType.Excel.getContentType());
	}

	public ByteArrayDataSource createExcelFile(Lot[] lotList) throws CustomException {

		if (lotList == null || lotList.length == 0)return null;
		
		//Field[] fields = Product.class.getDeclaredFields();
		List<String> columns = getTableColumns("PRODUCT");
		HSSFWorkbook workBook = new HSSFWorkbook();
		
		HSSFFont headFont = workBook.createFont();
		headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headFont.setFontHeightInPoints((short)10);
		headFont.setColor(HSSFColor.BLACK.index);
		
		HSSFFont commonFont = workBook.createFont();
		commonFont.setFontHeightInPoints((short)8);
		commonFont.setColor(HSSFColor.BLACK.index);
		
		HSSFCellStyle commonCellStyle = workBook.createCellStyle();
		commonCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		commonCellStyle.setFont(commonFont);

		HSSFCellStyle headCellStyle = workBook.createCellStyle();
		headCellStyle.setFont(headFont);
		headCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headCellStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
		
		for (Lot lotData : lotList) {
		
			Sheet sheet = workBook.createSheet(lotData.getKey().getLotName());
			Row headRow = sheet.createRow(0);
			
			for(int i =0;i<columns.size();i++)
			{
				Cell headcell =  headRow.createCell(i);
				headcell.setCellStyle(headCellStyle);
				headcell.setCellValue(columns.get(i));
			}
			
			List<Map<String,Object>> productList = null;
			
			try {
				productList = 	GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT * FROM PRODUCT WHERE 1=1  AND LOTNAME = ? ",new Object[] { lotData.getKey().getLotName() });
			} catch (Exception ex) {
				
				throw new CustomException(ex.getCause());
			}
		   
			for(int r=0;r< productList.size();r++)
		    {
		        Row row = sheet.createRow(r+1);
		        
				for(int c =0;c<columns.size();c++)
				{
					Cell cell =  row.createCell(c);
					cell.setCellStyle(commonCellStyle);
					cell.setCellValue(ConvertUtil.getMapValueByName(productList.get(r), columns.get(c)));
					
					if(productList.size() -1 ==r)
					{
						sheet.autoSizeColumn(c);
					}
				}
		    }
		}
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			workBook.write(byteStream);
			byteStream.flush();
			byteStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return  new ByteArrayDataSource(byteStream.toByteArray(), MailContentType.Excel.getContentType());
	}
	
	public List<String> getTableColumns( String tableName)
	{
		
		if (tableName == null || StringUtil.isEmpty(tableName) || !StringUtil.isAlpha(tableName))
			return null;

		String sql = " SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE 1=1 AND TABLE_NAME =? ORDER BY COLUMN_ID ASC ";

		List<Map<String,Object>> resultList =null;
		try {
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { tableName.toUpperCase() });
		} catch (Exception ex) {
			log.info(ex.getCause());
			return null;
		}
		
		return this.convertResultListToStringList(resultList,"COLUMN_NAME");
	}
	
	public List<String> convertResultListToStringList(List<Map<String, Object>> resultList, String mapKey) {
		if (resultList == null || resultList.size() == 0)
			return null;
		List<String> valueList = new ArrayList<>();

		for (Map<String, Object> result : resultList) {
			valueList.add(ConvertUtil.getMapValueByName(result, mapKey));
		}

		return valueList.size()==0?null: valueList;
	}
	
}
