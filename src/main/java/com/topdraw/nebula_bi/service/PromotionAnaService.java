package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PromotionAnaService {
	private final static Logger logger = LoggerFactory.getLogger(PromotionAnaService.class);

	public static PromotionAnaService getInstance() {
		return _instance;
	}
    private static PromotionAnaService _instance = new PromotionAnaService();
	private PromotionAnaService() { logger.info("Initial PromotionAnaService"); }

	public IResultInfo<Map<String, Object>> getPages(Integer platformId) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM x_page__platform pp " +
					"LEFT JOIN x_page p ON pp.page_id= p.id WHERE pp.platform_id = ?";
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, platformId);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getPages error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getPromotionList(Integer platFormId, Integer pageId, Date queryDate) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String sqlWhere = "";
			if(pageId != 0){
				sqlWhere += " AND p.id = "+ pageId;
			}
			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, p.name, xp.title, xpg.description FROM bi_pvuv_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code` " +
					"INNER JOIN x_promotion xpg ON xp.promotion_id = xpg.id " +
					"INNER JOIN x_page_promotion pp ON bgc.promotion_code = pp.promotion_code " +
					"INNER JOIN x_page p ON pp.page_id = p.id " +
					"WHERE bgc.day = ? AND bgc.platform_id = ? " + sqlWhere + " ORDER BY bgc.pv DESC";

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(queryDate), platFormId);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getPromotionList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
