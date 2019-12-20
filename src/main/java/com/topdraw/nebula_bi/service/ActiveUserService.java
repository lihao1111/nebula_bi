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
		List<Map<String, Object>> retlist = null;
		try {
			retlist = new ArrayList<>();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String querySql;
			while(eDate.getTime() >= sDate.getTime()){
				querySql = "SELECT bdu.day, bdu.uv, bpv.pv from bi_daily_user bdu INNER JOIN " +
						"(select day, platform_id, MAX(pv) pv, MAX(uv) FROM bi_online_pvuv_sum " +
						"WHERE platform_id = ? AND day = ?) bpv " +
						"ON bdu.`day` = bpv.`day` AND bdu.platform_id = bpv.platform_id " +
						"WHERE bdu.platform_id = ? AND bdu.`day` = ?";

				Map<String, Object> retMap = DruidUtil.queryUniqueResult(readConnection, querySql, lPlatform, dateFormat.format(eDate),
						lPlatform, dateFormat.format(eDate));
				retlist.add(retMap);

				eDate = org.afflatus.utility.DateUtil.getDateBeforeOrAfter(eDate, -1);
			}

			return retlist;
		} catch (SQLException e) {
			e.printStackTrace();
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
			String querySql = "SELECT a.day, a.uv, a.pv FROM bi_week_uv a WHERE day in (" + params + ") AND platform_id = ? ORDER BY day desc";
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
			String querySql = "SELECT a.day, a.uv, a.pv FROM bi_month_uv a WHERE day in (" + params + ") AND platform_id = ? ORDER BY day desc";
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
