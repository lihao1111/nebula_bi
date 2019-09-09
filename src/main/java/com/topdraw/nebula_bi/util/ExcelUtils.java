package com.topdraw.nebula_bi.util;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataUnit;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtils {

    private static final int DEFAULT_COLUMN_WIDTH = 30;

    @Value("xxxx")
    public String xx;



    public static String ExcelExportFromServer(List<Map<String, Object>> listData, Map<String, Object> mapColumn,
											   String strExcelPath, String strContextPath, String strFileName)
            throws IOException {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet("sheet1");
        // 设置表格默认列宽度为指定字节
        sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);
        // 生成一个样式
        HSSFCellStyle style = workbook.createCellStyle();
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.VIOLET.index);
        font.setFontHeightInPoints((short) 12);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 产生表格标题行(动态表头)
        HSSFRow row = sheet.createRow(0);
        row.setHeightInPoints(20);


        //表头数据
        int iHead = 0;
        for (Map.Entry<String, Object> entry : mapColumn.entrySet()) {
            HSSFCell cell = row.createCell(iHead);
            iHead++;
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(entry.getValue().toString());
            cell.setCellValue(text);
        }

        // 生成数据
        for (int i = 1; i < listData.size() + 1; i++) {
            row = sheet.createRow(i);	//第一行数据
            int j = 0;
            for (Map.Entry<String, Object> entry : mapColumn.entrySet()) {
                HSSFCell cell = row.createCell(j);
                j++;
                cell.setCellStyle(style);
                if (listData.get(i - 1).containsKey(entry.getKey())) {
                    HSSFRichTextString text = new HSSFRichTextString(listData.get(i - 1).get(entry.getKey()) == null ?
                            "" : listData.get(i - 1).get(entry.getKey()).toString());
                    cell.setCellValue(text);
                } else {
                    cell.setCellValue("");// 没有此列则设为空
                }
            }
        }

        String fileExcelName;
        if (strFileName.equals("")) {
            fileExcelName = DateUtil.formatDate(new Date(), "yyyyMMddHHmmss") + ".xls";
        } else {
            fileExcelName = strFileName + ".xls";
        }
        // 服务器上的磁盘地址
        strContextPath += fileExcelName;// 浏览器访问的地址

        FileUtil.createDirectory(strExcelPath);

        OutputStream os = new BufferedOutputStream(new FileOutputStream(strExcelPath + fileExcelName));// 写到磁盘
        workbook.write(os);
        os.flush();
        workbook.close();
        os.close();
        return strContextPath;
    }

	/**
	 * 河北音乐支撑——推荐位导出
	 * @param listData
	 * @param days
	 * @param strExcelPath
	 * @param strContextPath
	 * @param strFileName
	 * @return
	 * @throws IOException
	 */
	public static String ExcelExportFromServerByHBMusic(List<Map<String, Object>> listData, List<Date> days, String strExcelPath, String strContextPath, String strFileName)
			throws IOException {

		//日期数据
		List<String> weeks = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		for(Date day : days){
			cal.setTime(day);
			weeks.add(DateUtil.getWeekDay(cal));
		}
		//整理表头数据
		Map<String, List<Map<String, Object>>> retMap = new HashMap<>();
		for(Map<String, Object> map : listData){
			String day = map.get("day").toString();
			List<Map<String, Object>> list = null;
			for(Map.Entry retObj : retMap.entrySet()){
				String key = retObj.getKey().toString();
				if(day.equals(key)){
					list = (ArrayList<Map<String, Object>>)retObj.getValue();
					break;
				}
			}
			if(list == null){
				list = new ArrayList<>();
				retMap.put(day, list);
			}
			list.add(map);
		}
		//计算最大长度
		int maxLeng = 0;
		for(Map.Entry retObj : retMap.entrySet()){
			List<Map<String, Object>> list = (List<Map<String, Object>>)retObj.getValue();
			if(maxLeng < list.size()){
				maxLeng = list.size();
			}
		}
		System.out.println("最大长度:"+maxLeng);
		//写入到excel
		// 声明一个工作薄
		XSSFWorkbook workbook = new XSSFWorkbook();
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet("sheet1");

		// 表头样式
		XSSFFont font = workbook.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short)10);
		font.setBold(true);
		// 把字体应用到当前的样式
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);

		// 表格内容的样式
		XSSFFont fontBody = workbook.createFont();
		fontBody.setFontName("Times New Roman");
		fontBody.setFontHeightInPoints((short)10);
		// 把字体应用到当前的样式
		XSSFCellStyle cellStyleBody = workbook.createCellStyle();
		cellStyleBody.setFont(fontBody);

		// 产生表格标题行(动态表头)
		XSSFRow row = sheet.createRow(0);
		//表头数据
		int iHead = 0;
		int cellIdx = 0;
		maxLeng = maxLeng * 3 +2;
		for(int i=0; i< maxLeng; i++){
			XSSFCell cell = row.createCell(iHead);
			iHead++;
			cell.setCellStyle(cellStyle);
			XSSFRichTextString text = null;
			if(i<2){
				text = new XSSFRichTextString("");
			}else{
				if((cellIdx-2)%3==0){
					text =  new XSSFRichTextString("访问次数");
				}else if((cellIdx-2)%3 ==1){
					text =  new XSSFRichTextString("访问用户数");
				}else if((cellIdx-2)%3 ==2){
					text =  new XSSFRichTextString("推荐位");
				}
			}
			cell.setCellValue(text);
			cellIdx++;
		}
		System.out.println("表头列数:"+iHead);
		//表格内容数据
		int iBHead = 1;		//行数
		int mapIdx = 0;		//日期数据的索引
		List<Map<String, Object>> list = null;
		for(Map.Entry retObj : retMap.entrySet()){
			row = sheet.createRow(iBHead++);

			list = (List<Map<String, Object>>)retObj.getValue();
			for(int i=0; i<maxLeng; i++){
				XSSFCell cell = row.createCell(i);
				cell.setCellStyle(cellStyleBody);
				XSSFRichTextString text = null;
				if(i == 0){
					text = new XSSFRichTextString(DateUtil.formatDate(days.get(mapIdx), ""));
				}else if(i == 1){
					text = new XSSFRichTextString(weeks.get(mapIdx));
				}else{
					int z = (i-2) / 3;
					Map<String, Object> map = null;
					if(z < list.size()){
						map = list.get(z);
					}
					if(map == null){
						text = new XSSFRichTextString("");
					}else{
						if((i-2)%3 ==0){
							text = new XSSFRichTextString(map.get("pv").toString());
						}else if((i-2)%3 ==1){
							text = new XSSFRichTextString(map.get("uv").toString());
						}else if((i-2)%3 ==2){
							text = new XSSFRichTextString(map.get("title").toString());
						}
					}
				}
				cell.setCellValue(text);
			}
			mapIdx++;
		}

		String fileExcelName;
		if (strFileName.equals("")) {
			fileExcelName = DateUtil.formatDate(new Date(), "yyyyMMddHHmmss") + ".xlsx";
		} else {
			fileExcelName = strFileName + ".xlsx";
		}
		// 服务器上的磁盘地址
		strContextPath += fileExcelName;// 浏览器访问的地址

		FileUtil.createDirectory(strExcelPath);

		OutputStream os = new BufferedOutputStream(new FileOutputStream(strExcelPath + fileExcelName));// 写到磁盘
		workbook.write(os);
		os.flush();
		workbook.close();
		os.close();
		return strContextPath;
	}




	//河北音乐数据支撑
    public static void main(String args[]) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, xp.title FROM bi_gscmcc_children_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code` " +
					"INNER JOIN x_promotion xpg ON xp.promotion_id = xpg.id " +
					"WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  GROUP BY bgc.day, xp.title ORDER BY bgc.day";


			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, "2019-09-01", "2019-09-07", 14);


			//整理表头数据
			Map<String, List<Map<String, Object>>> retMap = new HashMap<>();

			//日期数据
			List<Date> days = DateUtil.days(dateFormat.parse("2019-09-01"), DateUtil.getDateBeforeOrAfter(dateFormat.parse("2019-09-07"), 1));
			List<String> weeks = new ArrayList<>();
			Calendar cal = Calendar.getInstance();
			for(Date day : days){
				cal.setTime(day);
				weeks.add(DateUtil.getWeekDay(cal));
			}


			for(Map<String, Object> map : retList){
				String day = map.get("day").toString();
				List<Map<String, Object>> list = null;
				for(Map.Entry retObj : retMap.entrySet()){
					String key = retObj.getKey().toString();
					if(day.equals(key)){
						list = (ArrayList<Map<String, Object>>)retObj.getValue();
						break;
					}
				}
				if(list == null){
					list = new ArrayList<>();
					retMap.put(day, list);
				}
				list.add(map);
			}

			System.out.println(retMap.size());
			//计算最大长度
			int maxLeng = 0;
			for(Map.Entry retObj : retMap.entrySet()){
				List<Map<String, Object>> list = (List<Map<String, Object>>)retObj.getValue();
				if(maxLeng < list.size()){
					maxLeng = list.size();
				}
			}
			System.out.println("最大长度:"+maxLeng);
			//写入到excel

			// 声明一个工作薄
			XSSFWorkbook workbook = new XSSFWorkbook();
			// 生成一个表格
			XSSFSheet sheet = workbook.createSheet("sheet1");

			// 生成一个样式
			XSSFFont font = workbook.createFont();
			font.setFontName("Times New Roman");
			font.setFontHeightInPoints((short)10);
			font.setBold(true);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);

			//表格内容的样式
			XSSFFont fontBody = workbook.createFont();
			fontBody.setFontName("Times New Roman");
			fontBody.setFontHeightInPoints((short)10);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyleBody = workbook.createCellStyle();
			cellStyleBody.setFont(fontBody);

			// 产生表格标题行(动态表头)
			XSSFRow row = sheet.createRow(0);
			//row.setHeightInPoints(20);

			//表头数据
			int iHead = 0;
			int cellIdx = 0;
			maxLeng = maxLeng * 3 +2;
			for(int i=0; i< maxLeng; i++){
				XSSFCell cell = row.createCell(iHead);
				iHead++;
				cell.setCellStyle(cellStyle);
				XSSFRichTextString text = null;
				if(i<2){
					text = new XSSFRichTextString("");
				}else{
					if((cellIdx-2)%3==0){
						text =  new XSSFRichTextString("访问次数");
					}else if((cellIdx-2)%3 ==1){
						text =  new XSSFRichTextString("访问用户数");
					}else if((cellIdx-2)%3 ==2){
						text =  new XSSFRichTextString("推荐位");
					}
				}
				cell.setCellValue(text);
				cellIdx++;
			}
			System.out.println("表头列数:"+iHead);
			//表格内容数据
			int iBHead = 1;		//行数
			int mapIdx = 0;		//日期数据的索引
			List<Map<String, Object>> list = null;
			for(Map.Entry retObj : retMap.entrySet()){
				row = sheet.createRow(iBHead++);

				list = (List<Map<String, Object>>)retObj.getValue();
				for(int i=0; i<maxLeng; i++){
					XSSFCell cell = row.createCell(i);
					cell.setCellStyle(cellStyleBody);
					XSSFRichTextString text = null;
					if(i == 0){
						text = new XSSFRichTextString(DateUtil.formatDate(days.get(mapIdx), ""));
					}else if(i == 1){
						text = new XSSFRichTextString(weeks.get(mapIdx));
					}else{
						int z = (i-2) / 3;
						Map<String, Object> map = null;
						if(z < list.size()){
							map = list.get(z);
						}
						if(map == null){
							text = new XSSFRichTextString("");
						}else{
							if((i-2)%3 ==0){
								text = new XSSFRichTextString(map.get("pv").toString());
							}else if((i-2)%3 ==1){
								text = new XSSFRichTextString(map.get("uv").toString());
							}else if((i-2)%3 ==2){
								text = new XSSFRichTextString(map.get("title").toString());
							}
						}
					}
					cell.setCellValue(text);
				}
				mapIdx++;
			}

			OutputStream os = null;// 写到磁盘
			try {
				os = new BufferedOutputStream(new FileOutputStream("D:/1.xlsx"));
				workbook.write(os);
				os.flush();
				workbook.close();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}


		} catch (SQLException e) {
			e.printStackTrace();

		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
    }
}
