package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LogService {
	private final static Logger logger = LoggerFactory.getLogger(LogService.class);

	public static LogService getInstance() {
		return _instance;
	}
    private static LogService _instance = new LogService();
	private LogService() { logger.info("Initial LogService"); }

	public IResultInfo<Map<String, Object>> getLogs(Integer lPlatform, Date nowDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM bi_sparkJob WHERE job_execute_time = ? AND platform_id = ?";

			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(nowDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getLogs error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}


	public IResultInfo<Map<String, Object>> getLogErrNum(Integer lPlatform, Date nowDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM bi_sparkJob WHERE job_execute_time = ? AND platform_id = ? AND status = 1";

			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(nowDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getLogErrNum error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

}
