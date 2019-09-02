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

public class NewAddUserService {
	private final static Logger logger = LoggerFactory.getLogger(NewAddUserService.class);

	public static NewAddUserService getInstance() {
		return _instance;
	}
    private static NewAddUserService _instance = new NewAddUserService();
	private NewAddUserService() { logger.info("Initial NewAddUserService"); }

	public IResultInfo<Map<String, Object>> getNewAddUser(Integer lPlatform, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_gscmcc_children_newuser";
			String querySql = "SELECT a.*, ROUND(a.retained1Num * 100/a.newAddNum, 1) retained1Prec FROM "+tabName+" a WHERE platform_id =? AND day >= ? AND day <= ? ORDER BY day desc";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sDate), dateFormat.format(eDate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getNewAddUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getWeekUser(Integer lPlatform, Date nowDate) {
		//计算最近一周时间段

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDate);
		calendar.add(Calendar.DATE, -8);
		Date sDate = calendar.getTime();

		calendar.setTime(nowDate);
		calendar.add(Calendar.DATE, -1);
		Date eDate = calendar.getTime();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_gscmcc_children_newuser";
			String querySql = "SELECT a.* FROM "+tabName+" a WHERE platform_id =? AND day >= ? AND day <= ? ORDER BY day";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sDate), dateFormat.format(eDate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getWeekUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

}
