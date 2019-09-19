package com.topdraw.nebula_bi.service;

import com.topdraw.nebula_bi.util.ExcelUtils;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HBMusicHelpService {
	private final static Logger logger = LoggerFactory.getLogger(HBMusicHelpService.class);

	public static HBMusicHelpService getInstance() {
		return _instance;
	}
    private static HBMusicHelpService _instance = new HBMusicHelpService();
	private HBMusicHelpService() { logger.info("Initial HBMusicHelpService"); }

	public IResultInfo<Map<String, Object>> getPromotionList(Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();



			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, xp.title FROM bi_gscmcc_children_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code` " +
					"INNER JOIN x_promotion xpg ON xp.promotion_id = xpg.id " +
					"WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  ORDER BY bgc.day";



			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getPromotionList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> exportPromotionList(Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, xp.title FROM bi_gscmcc_children_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code` " +
					"INNER JOIN x_promotion xpg ON xp.promotion_id = xpg.id " +
					"WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  ORDER BY bgc.day";


			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);
			List<Date> days = DateUtil.days(sDate, DateUtil.getDateBeforeOrAfter(eDate, 1));

			//整理表头数据
			String strExcelPath = "/resources/excelExport/";
			String strContextPath = this.getClass().getResourceAsStream("/")
					+ "/resources/excelExport/";

			String strFileUrl = ExcelUtils.ExcelExportFromServerByHBMusic(retList, days, strExcelPath, strContextPath, "");

			ri = new ResultInfo<>("success", null, strFileUrl);


		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportPromotionList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
