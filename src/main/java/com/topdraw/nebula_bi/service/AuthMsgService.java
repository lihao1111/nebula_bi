package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AuthMsgService {
	private final static Logger logger = LoggerFactory.getLogger(AuthMsgService.class);

	public static AuthMsgService getInstance() {
		return _instance;
	}
    private static AuthMsgService _instance = new AuthMsgService();
	private AuthMsgService() { logger.info("Initial AuthMsgService"); }

	public IResultInfo<Map<String, Object>> getAuthList(String queryVal) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhereSql = "WHERE 1=1 ";
			if(StringUtil.hasText(queryVal)){
				strWhereSql += "AND friendly_name like '%"+ queryVal +"%'";
			}
			String querySql = "SELECT * FROM x_backoffice_feature " + strWhereSql;
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getAuthList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
