package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OnlineUserService {
	private final static Logger logger = LoggerFactory.getLogger(OnlineUserService.class);

	public static OnlineUserService getInstance() {
		return _instance;
	}
    private static OnlineUserService _instance = new OnlineUserService();
	private OnlineUserService() { logger.info("Initial OnlineUserService"); }

	public IResultInfo<Map<String, Object>> getOnlineUser(Integer lPlatform, Date sdate) {
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

			String tabName = "bi_onlineuser";

			String querySql = "SELECT * FROM "+tabName+" a WHERE platform_id =? AND day in (?,?,?) group by day, hour order by day desc, hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sdate), dateLastStr, dateLast7Str);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getOnlineUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getCompareUser(Integer lPlatform, Date compareDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_onlineuser";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ? group by hour order by hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dateFormat.format(compareDate), lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getCompareUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getCurUser(Integer lPlatform, Date nowDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(nowDate);
			calendar.add(Calendar.HOUR_OF_DAY, -1);

			Date date = calendar.getTime();
			String dayStr = dateFormat.format(date);		//昨天


			String tabName = "bi_onlineuser";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ? ORDER BY hour DESC";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, dayStr, lPlatform);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getCurUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

}
