package com.topdraw.nebula_bi.service;

import com.topdraw.nebula_bi.util.DateUtil;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ActiveUserService {
	private final static Logger logger = LoggerFactory.getLogger(ActiveUserService.class);

	public static ActiveUserService getInstance() {
		return _instance;
	}
    private static ActiveUserService _instance = new ActiveUserService();
	private ActiveUserService() { logger.info("Initial ActiveUserService"); }

	public IResultInfo<Map<String, Object>> getActiveUser(Integer lPlatform, String type, Date sDate, Date eDate) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();
			List<Map<String, Object>> retlist = null;
			switch (type){
				case "day" :
					retlist = getListForDay(readConnection, lPlatform, sDate, eDate);
					break;
				case "week" :
					retlist = getListForWeek(readConnection, lPlatform, sDate, eDate);
					break;
				case "month" :
					retlist = getListForMonth(readConnection, lPlatform, sDate, eDate);
					break;
			}
			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getActiveUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public static List<Map<String, Object>> getListForDay(Connection readConnection, Integer lPlatform, Date sDate, Date eDate){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			String querySql = "SELECT a.day, a.uv FROM bi_daily_user a WHERE day >= ? AND day <= ? AND platform_id = ? ORDER BY day desc";

			return DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), lPlatform);
		} catch (SQLException e) {
			logger.error("getActiveUser error" + e.getMessage());
		}	finally {
			DruidUtil.close(readConnection);
		}
		return new ArrayList<>();
	}

	public static List<Map<String, Object>> getListForWeek(Connection readConnection, Integer lPlatform, Date sDate, Date eDate){
		try {
			List<String> days = DateUtil.getWeek(sDate, eDate);
			String params = "";
			for(String day : days){
				params += "'" + day+"',";
			}
			params = params.substring(0, params.length()-1);
			String querySql = "SELECT a.day, a.uv FROM bi_week_uv a WHERE day in (" + params + ") AND platform_id = ? ORDER BY day desc";
			return DruidUtil.queryList(readConnection, querySql, lPlatform);

		} catch (SQLException e) {
			logger.error("getActiveUser error" + e.getMessage());
		}	finally {
			DruidUtil.close(readConnection);
		}
		return new ArrayList<>();
	}


	public static List<Map<String, Object>> getListForMonth(Connection readConnection, Integer lPlatform, Date sDate, Date eDate){
		try {
			List<String> days = DateUtil.getMonth(sDate, eDate);
			String params = "";
			for(String day : days){
				params += "'" + day+"',";
			}
			params = params.substring(0, params.length()-1);
			String querySql = "SELECT a.day, a.uv FROM bi_month_uv a WHERE day in (" + params + ") AND platform_id = ? ORDER BY day desc";
			return DruidUtil.queryList(readConnection, querySql, lPlatform);

		} catch (SQLException e) {
			logger.error("getActiveUser error" + e.getMessage());
		}	finally {
			DruidUtil.close(readConnection);
		}
		return new ArrayList<>();
	}

	public IResultInfo<Map<String, Object>> loadDayUV(Integer lPlatform, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT sum(uv) UV FROM bi_daily_user WHERE day >= ? AND day <= ? AND platform_id = ? ORDER BY day desc";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("loadDayUV error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}


}
