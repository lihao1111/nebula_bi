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

public class ProOrderMsgService {
	private final static Logger logger = LoggerFactory.getLogger(ProOrderMsgService.class);

	public static ProOrderMsgService getInstance() {
		return _instance;
	}
    private static ProOrderMsgService _instance = new ProOrderMsgService();
	private ProOrderMsgService() { logger.info("Initial ProOrderMsgService"); }

	public IResultInfo<Map<String, Object>> getProOrder(Integer lPlatform, String queryVal) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " 1=1";
			if(StringUtil.hasText(lPlatform)){
				strWhere += " AND platform_id = " + lPlatform;
			}
			if(StringUtil.hasText(queryVal)){
				strWhere += " AND product_name = " + queryVal;
			}
			String queySql = "SELECT * FROM x_order_product_xx WHERE" + strWhere;

			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, queySql);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getProOrder error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
