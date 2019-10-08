package com.topdraw.nebula_bi.util;

import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.*;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ExcelUtilsNew {

	//河北音乐数据支撑 _推荐位数据
	public static void exportHBPromontionExcel(HttpServletResponse response, String startDay, String endDay, Integer platformId){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();


			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, xp.title FROM bi_gscmcc_children_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code`" +
					"WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  GROUP BY bgc.day, xp.title ORDER BY xp.title, bgc.day";


			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, startDay, endDay, platformId);

			//
			querySql = "SELECT MAX(a.val) as maxlen FROM (SELECT count(*) as val FROM bi_gscmcc_children_promotion bgc " +
			 "WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  GROUP BY bgc.day) AS a";

			Map<String, Object> lengthMap = DruidUtil.queryUniqueResult(readConnection, querySql, startDay, endDay, platformId);

			//日期数据
			List<Date> days = DateUtil.days(dateFormat.parse(startDay), DateUtil.getDateBeforeOrAfter(dateFormat.parse(endDay), 1));
			List<String> weeks = new ArrayList<>();
			Calendar cal = Calendar.getInstance();
			for(Date day : days){
				cal.setTime(day);
				weeks.add(DateUtil.getWeekDay(cal));
			}

			//表体数据
			Map<String, List<Map<String, Object>>> retMapBody = new LinkedHashMap<>();

			for(Map<String, Object> map : retList){
				String title = map.get("title").toString();
				List<Map<String, Object>> list = null;
				for(Map.Entry retObj : retMapBody.entrySet()){
					String key = retObj.getKey().toString();
					if(title.equals(key)){
						list = (ArrayList<Map<String, Object>>)retObj.getValue();
						break;
					}
				}
				if(list == null){
					list = new ArrayList<>();
					retMapBody.put(title, list);
				}
				list.add(map);
			}


			Map<String, List<Map<String, Object>>> excelMap = new LinkedHashMap<>();	//导出excel的数据
			for(Map.Entry retObj : retMapBody.entrySet()){
				ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>)retObj.getValue();
				ArrayList<Map<String, Object>> cpList = (ArrayList<Map<String, Object>>) list.clone();

				for(Date day : days){
					boolean hasDays = false;
					for(Map<String, Object> map : list){
						String dayStr = map.get("day").toString();
						if(dayStr.equals(DateUtil.formatDate(day, ""))){
							hasDays = true;
							break;
						}
					}
					if(!hasDays){	//没有这天的数据
						int idx = days.indexOf(day);
						cpList.add(idx, new HashMap<>());
					}
				}
				excelMap.put(retObj.getKey().toString(), cpList);
			}


			//计算最大长度
			int maxLeng = Integer.parseInt(lengthMap.get("maxlen").toString());

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


			//创建行

			//日期数据
			int rowNum = 1;		//行数
			List<XSSFRow> rows = new ArrayList<>();	//行
			for(int i=0; i<days.size(); i++){
				row = sheet.createRow(rowNum);
				rows.add(row);

				XSSFCell cell = row.createCell(0);
				cell.setCellStyle(cellStyleBody);
				XSSFRichTextString text = new XSSFRichTextString(DateUtil.formatDate(days.get(i), ""));
				cell.setCellValue(text);

				cell = row.createCell(1);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(weeks.get(i));
				cell.setCellValue(text);
				rowNum++;
			}
			System.out.println(rows.size());

			int idx = 0;
			int colNum =2;
			List<Map<String, Object>> list = null;
			for(Map.Entry retObj : excelMap.entrySet()) {
				int curRowNum = 0;			//行数
				int curCol = idx*3;
				list = (List<Map<String, Object>>)retObj.getValue();
				for(Map<String, Object> map : list){
					row = rows.get(curRowNum);

					XSSFCell cell = row.createCell(colNum+curCol);
					cell.setCellStyle(cellStyleBody);
					XSSFRichTextString text = new XSSFRichTextString(map.get("pv") == null ? "" : map.get("pv").toString());
					cell.setCellValue(text);


					cell = row.createCell(colNum+curCol+1);
					cell.setCellStyle(cellStyleBody);
					text = new XSSFRichTextString(map.get("uv") == null ? "" : map.get("uv").toString());
					cell.setCellValue(text);

					cell = row.createCell(colNum+curCol+2);
					cell.setCellStyle(cellStyleBody);
					text = new XSSFRichTextString(map.get("title") == null ? "" : map.get("title").toString());
					cell.setCellValue(text);

					curRowNum++;
				}
				idx++;
			}
			String fileName = "HBPromontion"+ DateUtil.formatDate(new Date(), "") + ".xlsx";     //输出xls文件名称

			//生成并下载Excel
			downloadWorkBook(workbook, fileName, response);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
    }


	//河北音乐数据支撑 _日数据
	public static void exportHBDayDataExcel (HttpServletResponse response, String startDay, String endDay, Integer platformId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM x_hb_auth_rf WHERE day >= ? AND day <= ? ";
			List<Map<String, Object>> retList_Auth = DruidUtil.queryList(readConnection, querySql, startDay, endDay);        //包月用户信息

			querySql = "SELECT day, sum(ordered_num) allOrder, sum(newUordered_num) allNewOrder, sum(oldNewReUordered_num)+sum(oldFirstUordered_num) allOldOrder " +
					"FROM bi_gscmcc_children_proordered_copy1 WHERE day >= ? AND day <= ? AND platform_id = ? group by day";
			List<Map<String, Object>> retList_Order = DruidUtil.queryList(readConnection, querySql, startDay, endDay, 14);        //订购信息

			querySql = "SELECT * FROM x_hb_play_rf WHERE day >= ? AND day <= ?";
			List<Map<String, Object>> retList_Player = DruidUtil.queryList(readConnection, querySql, startDay, endDay);        //点播信息


			//日期数据
			List<Date> days = DateUtil.days(dateFormat.parse(startDay), DateUtil.getDateBeforeOrAfter(dateFormat.parse(endDay), 1));

			//写入到excel

			// 声明一个工作薄
			XSSFWorkbook workbook = new XSSFWorkbook();
			// 生成一个表格
			XSSFSheet sheet = workbook.createSheet("sheet1");

			// 生成一个样式
			XSSFFont font = workbook.createFont();
			font.setFontName("Times New Roman");
			font.setFontHeightInPoints((short) 10);
			font.setBold(true);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);

			//表格内容的样式
			XSSFFont fontBody = workbook.createFont();
			fontBody.setFontName("Times New Roman");
			fontBody.setFontHeightInPoints((short) 10);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyleBody = workbook.createCellStyle();
			cellStyleBody.setFont(fontBody);

			// 产生表格标题行(动态表头)
			XSSFRow row = sheet.createRow(0);
			//row.setHeightInPoints(20);

			//表头数据
			int iRow = 0;
			XSSFCell cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			XSSFRichTextString text = new XSSFRichTextString("日期");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("访问用户数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("访问次数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("包月用户到访数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("未订购到访用户数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("新用户到访次数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("新用户到访用户数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("新增订购用户数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("新用户订购");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("老用户订购");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("订购转化率");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("点播视频条数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("点播次数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("有效点播次数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("有效点播用户数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("无效点播次数");
			cell.setCellValue(text);
			iRow++;

			cell = row.createCell(iRow);
			cell.setCellStyle(cellStyle);
			text = new XSSFRichTextString("无效点播用户数");
			cell.setCellValue(text);
			iRow++;


			int rowN = 0;
			for (Date day : days) {
				rowN++;
				String dayStr = DateUtil.formatDate(day, "");
				iRow = 0;
				row = sheet.createRow(rowN);
				cell = row.createCell(iRow);
				cell.setCellStyle(cellStyle);
				text = new XSSFRichTextString(dayStr);
				cell.setCellValue(text);
				iRow++;

				cell = row.createCell(iRow);        //访问用户数
				cell.setCellStyle(cellStyle);
				text = new XSSFRichTextString("");
				cell.setCellValue(text);
				iRow++;

				cell = row.createCell(iRow);        //访问次数
				cell.setCellStyle(cellStyle);
				text = new XSSFRichTextString("");
				cell.setCellValue(text);
				iRow++;


				for (Map<String, Object> map : retList_Auth) {
					String tDay = map.get("day").toString();
					if (tDay.equals(dayStr)) {
						cell = row.createCell(iRow);        //包月用户到访数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("auth_num").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //未订购用户到访数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("unAuth_num").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //新用户到访次数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("newAdd_count").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //新用户数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("newAdd_num").toString());
						cell.setCellValue(text);
						iRow++;

					}
				}

				for (Map<String, Object> map : retList_Order) {
					String tDay = map.get("day").toString();
					if (tDay.equals(dayStr)) {
						cell = row.createCell(iRow);        //新增订购数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("allOrder").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //新用户订购
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("allNewOrder").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //老用户订购
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("allOldOrder").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //订购转化率
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString("");
						cell.setCellValue(text);
						iRow++;

					}
				}


				for (Map<String, Object> map : retList_Player) {
					String tDay = map.get("day").toString();
					if (tDay.equals(dayStr)) {
						cell = row.createCell(iRow);        //点播视频条数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("media_dis_count").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //点播次数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("media_play_count").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //有效点播次数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("auth_play_count").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //有效点播用户数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("auth_play_num").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //无效点播次数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("unAuth_play_count").toString());
						cell.setCellValue(text);
						iRow++;

						cell = row.createCell(iRow);        //无效点播用户数
						cell.setCellStyle(cellStyleBody);
						text = new XSSFRichTextString(map.get("unAuth_play_num").toString());
						cell.setCellValue(text);
						iRow++;
					}
				}
			}
			String fileName = "HBDayUser"+ DateUtil.formatDate(new Date(), "") + ".xlsx";     //输出xls文件名称
			//生成并下载Excel
			downloadWorkBook(workbook, fileName, response);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
	}


	//河北音乐数据支撑 _CP基础数据
	public static void exportHBCpInfoExcel (HttpServletResponse response, String startDay, String endDay) {
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM x_hb_cp_info WHERE day >= ? AND day <= ? ORDER BY cp_name, day desc";
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, startDay, endDay);

			//写入到excel
			// 声明一个工作薄
			XSSFWorkbook workbook = new XSSFWorkbook();
			// 生成一个表格
			XSSFSheet sheet = workbook.createSheet("sheet1");

			// 生成一个样式
			XSSFFont font = workbook.createFont();
			font.setFontName("Times New Roman");
			font.setFontHeightInPoints((short) 10);
			font.setBold(true);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);

			//表格内容的样式
			XSSFFont fontBody = workbook.createFont();
			fontBody.setFontName("Times New Roman");
			fontBody.setFontHeightInPoints((short) 10);
			// 把字体应用到当前的样式
			XSSFCellStyle cellStyleBody = workbook.createCellStyle();
			cellStyleBody.setFont(fontBody);

			// 产生表格标题行(动态表头)
			XSSFRow row = sheet.createRow(0);
			//row.setHeightInPoints(20);

			//表头数据
			String[] titleArr = new String[]{"CP名称", "日期", "点播次数", "点播用户", "点播时长(时)", "订购数", "订购触发数", "订购触发人数"};
			for(int i=0 ;i<8; i++){
				XSSFCell cell = row.createCell(i);
				cell.setCellStyle(cellStyle);
				XSSFRichTextString text = new XSSFRichTextString(titleArr[i]);
				cell.setCellValue(text);
				i++;
			}
			//表格内容
			int rowN = 1;
			for(Map<String, Object> map : retList){
				row = sheet.createRow(rowN);
				int j = 0;
				XSSFCell cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				XSSFRichTextString text = new XSSFRichTextString(map.get("cp_name").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("day").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("play_count").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("play_num").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString((Integer.parseInt(map.get("play_time").toString()) /60 / 60)+"");
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("order_num").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("order_cf_count").toString());
				cell.setCellValue(text);

				cell = row.createCell(j++);
				cell.setCellStyle(cellStyleBody);
				text = new XSSFRichTextString(map.get("order_cf_num").toString());
				cell.setCellValue(text);

				rowN++;
			}

			String fileName = "HBCpInfo"+ DateUtil.formatDate(new Date(), "") + ".xlsx";     //输出xls文件名称
			//生成并下载Excel
			downloadWorkBook(workbook, fileName, response);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	/**
	 * 生成并下载Excel
	 *
	 * @param fileName
	 * @param response
	 * @throws IOException
	 */
	public static void downloadWorkBook(XSSFWorkbook workbook, String fileName, HttpServletResponse response) throws IOException {
		// 1. 声明字节输出流
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// 2. 生成excel文件并写入输出流
		workbook.write(os);
		// 3. 将输出流转换成byte[] 数组
		byte[] content = os.toByteArray();
		// 4. 将数组放入输入流中
		InputStream is = new ByteArrayInputStream(content);
		// 5. 设置response参数
		response.reset(); // 重置response的设置
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xlsx").getBytes(), "iso-8859-1"));
		// 6. 创建Servlet 输出流对象
		ServletOutputStream out = response.getOutputStream();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			// 6.1装载缓冲输出流
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(out);
			byte[] buff = new byte[2048];
			int bytesRead;
			// 6.2 输出内容
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (final IOException e) {
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
			if(out != null)
				out.close();
			if(os != null)
				os.close();
			if(is != null)
				is.close();
		}
	}

}
