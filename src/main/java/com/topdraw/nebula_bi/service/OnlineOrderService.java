package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OnlineOrderService {
	private final static Logger logger = LoggerFactory.getLogger(OnlineOrderService.class);

	public static OnlineOrderService getInstance() {
		return _instance;
	}
    private static OnlineOrderService _instance = new OnlineOrderService();
	private OnlineOrderService() { logger.info("Initial OnlineOrderService"); }

	public IResultInfo<Map<String, Object>> getOnlineOrder(Integer lPlatform, Date sdate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdate);
			calendar.add(Calendar.DATE, -1);
			Date dateLast = calendar.getTime();
			String dateLastStr = dateFormat.format(dateLast);		//昨天

			calendar.add(Calendar.DATE, -6);						//一周前
			Date dateLast7 = calendar.getTime();
			String dateLast7Str = dateFormat.format(dateLast7);

			String tabName = "bi_online_order";

			String querySql = "SELECT * FROM "+tabName+" a WHERE platform_id =? AND day in (?,?,?) group by day, hour order by day desc, hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sdate), dateLastStr, dateLast7Str);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getOnlineOrder error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getCompareOrder(Integer lPlatform, Date compareDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_online_order";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ? group by hour order by hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(compareDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getCompareOrder error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getDayOrder(Integer lPlatform, Date nowDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_online_order";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ? order by hour desc";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(nowDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getDayOrder error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getCurOrder(Integer lPlatform, Date nowDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(nowDate);

			Date date = calendar.getTime();
			String dayStr = dateFormat.format(date);		//昨天

			String tabName = "bi_online_order";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ? ORDER BY hour DESC";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dayStr, lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getCurOrder error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

}
