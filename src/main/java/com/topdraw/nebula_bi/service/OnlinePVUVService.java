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

public class OnlinePVUVService {
	private final static Logger logger = LoggerFactory.getLogger(OnlinePVUVService.class);

	public static OnlinePVUVService getInstance() {
		return _instance;
	}
    private static OnlinePVUVService _instance = new OnlinePVUVService();
	private OnlinePVUVService() { logger.info("Initial OnlinePVUVService"); }

	public IResultInfo<Map<String, Object>> getOnlinePVUV(Integer lPlatform, Date sdate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT a.*, ROUND(a.uv * 100/a.pv, 1) PVUVPrec FROM bi_online_pvuv a WHERE platform_id =? AND day = ? order by hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sdate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getOnlinePVUV error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getPVUVSum(Integer lPlatform, Date sdate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT MAX(pv) pv, MAX(uv) uv  FROM bi_online_pvuv_sum  WHERE platform_id =? AND day = ?";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sdate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getPVUVSum error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getComparePVUV(Integer lPlatform, Date compareDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT a.*, ROUND(a.uv * 100/a.pv, 1) PVUVPrec FROM bi_online_pvuv a WHERE platform_id =? AND day = ? order by hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(compareDate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getCompareUser error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getNowPVUV(Integer lPlatform, Date compareDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT a.* FROM bi_online_pvuv a WHERE platform_id =? AND day = ? order by hour";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(compareDate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getNowPVUV error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}

		return ri;
	}

	public IResultInfo<Map<String, Object>> getCurPV(Integer lPlatform, Date sdate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT *  FROM bi_online_pvuv_sum  WHERE platform_id =? AND day = ? ORDER BY hour DESC";
			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sdate));

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			logger.error("getCurPV error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}


}
