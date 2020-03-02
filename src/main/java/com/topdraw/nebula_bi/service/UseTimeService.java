package com.topdraw.nebula_bi.service;

import com.topdraw.nebula_bi.util.ExcelUtilsNew;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataUnit;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UseTimeService {
	private final static Logger logger = LoggerFactory.getLogger(UseTimeService.class);

	public static UseTimeService getInstance() {
		return _instance;
	}
    private static UseTimeService _instance = new UseTimeService();
	private UseTimeService() { logger.info("Initial UseTimeService"); }

    public IResultInfo<Map<String, Object>> fetchUseTime(Integer lPlatform, Date sDate, Date eDate, String chooseType) {
        IResultInfo<Map<String, Object>> ri;
        Connection readConnection = null;
        try {
			readConnection = DruidUtil.getRandomReadConnection();

			List<Map<String, Object>> retlistProducts = new ArrayList<Map<String, Object>>();		//返回list

			switch (chooseType){
				case "day":
					retlistProducts = getUseTimeForDay(readConnection, sDate, eDate, lPlatform);
					break;
				case "week":
					retlistProducts = getUseTimeForWeek(readConnection, sDate, eDate, lPlatform);
					break;
				case "month":
					retlistProducts = getUseTimeForMonth(readConnection, sDate, eDate, lPlatform);
					break;
			}

			ri = new ResultInfo<>("success", retlistProducts, retlistProducts.size(), "");

        } catch (Exception ex) {
            ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchUseTime error" + ex.getMessage());
        } finally {
            DruidUtil.close(readConnection);
        }
        return ri;
    }

	public IResultInfo<Map<String, Object>> fetchPlayTime(Integer lPlatform, Date sDate, Date eDate, String chooseType) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			List<Map<String, Object>> retlistProducts = new ArrayList<Map<String, Object>>();		//返回list

			switch (chooseType){
				case "day":
					retlistProducts = getPlayTimeForDay(readConnection, sDate, eDate, lPlatform);
					break;
				case "week":
					retlistProducts = getPlayTimeForWeek(readConnection, sDate, eDate, lPlatform);
					break;
				case "month":
					retlistProducts = getPlayTimeForMonth(readConnection, sDate, eDate, lPlatform);
					break;

			}

			ri = new ResultInfo<>("success", retlistProducts, retlistProducts.size(), "");

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchUseTime error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public List<Map<String, Object>> getUseTimeForDay(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {

		String querySql = "SELECT * FROM bi_usetime a WHERE day >=? AND day <= ? AND platform_id = ? ORDER BY day desc";
		List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

		return list;
	}

	public List<Map<String, Object>> getUseTimeForWeek(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		//获取sDate所在周的周一
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sDate);
		int sInd = calendar.get(Calendar.DAY_OF_WEEK);
		sDate = DateUtil.getDateBeforeOrAfter(sDate, -(sInd-2));
		System.out.println("开始日期:"+ dateFormat.format(sDate));

		//获取eDate所在周的周一
		calendar.setTime(eDate);
		int eInd = calendar.get(Calendar.DAY_OF_WEEK);
		eDate = DateUtil.getDateBeforeOrAfter(eDate, -(eInd-2));
		System.out.println("结束日期:"+ dateFormat.format(eDate));

		String querySql;
		List<Map<String, Object>> useTimeList = new ArrayList<>();
		while(sDate.getTime() <= eDate.getTime()){
			querySql = "SELECT count(*) sumCount FROM bi_usetime_detail where day = ? AND platform_id = ?";

			Map<String, Object> map = DruidUtil.queryUniqueResult(readConnection, querySql, DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""), lPlatform);
			if(map == null || Integer.parseInt(map.get("sumCount").toString()) == 0){
				break;
			}
			querySql = "SELECT user_id, sum(use_time) use_time FROM bi_usetime_detail a WHERE day >=? AND day <= ? AND platform_id = ? group by user_id";
			List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

			int tim0_1 = 0;
			int tim1_3 = 0;
			int tim3_10 = 0;
			int tim10_30 = 0;
			int tim30_60 = 0;
			int tim60_120 = 0;
			int tim120 = 0;
			double avgSumTime = 0.0;
			for(Map<String, Object> objMap : list){
				double avgTime = Double.parseDouble(Optional.ofNullable(objMap.get("use_time")).orElse(0).toString()) / 60 / 7;
				avgSumTime += avgTime;
				if(avgTime <=1){
					tim0_1 += 1;
				}else if(avgTime >1 && avgTime <= 3){
					tim1_3 += 1;
				}else if(avgTime >3 && avgTime <= 10){
					tim3_10 += 1;
				}else if(avgTime > 10 && avgTime <= 30){
					tim10_30 += 1;
				}else if(avgTime > 30 && avgTime <=60){
					tim30_60 += 1;
				}else if(avgTime > 60 && avgTime <= 120){
					tim60_120 += 1;
				}else if(avgTime > 120){
					tim120 += 1;
				}
			}
			Map<String, Object> retMap = new HashMap<>();
			retMap.put("day", DateUtil.formatDate(sDate, "") +"至" + DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""));
			retMap.put("avgtime", String.format("%.2f", avgSumTime / list.size()));
			retMap.put("tim0_1", tim0_1);
			retMap.put("tim1_3", tim1_3);
			retMap.put("tim3_10", tim3_10);
			retMap.put("tim10_30", tim10_30);
			retMap.put("tim30_60", tim30_60);
			retMap.put("tim60_120", tim60_120);
			retMap.put("tim120", tim120);

			useTimeList.add(retMap);

			sDate = DateUtil.getDateBeforeOrAfter(sDate, 7);
		}

		return useTimeList;
	}

	public List<Map<String, Object>> getUseTimeForMonth(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		//获取sDate所在月
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sDate);
		int sInd = calendar.get(Calendar.DAY_OF_MONTH);
		sDate = DateUtil.getDateBeforeOrAfter(sDate, -(sInd-1));
		System.out.println("开始日期:"+ dateFormat.format(sDate));

		//获取eDate所在月
		calendar.setTime(eDate);
		int eInd = calendar.get(Calendar.DAY_OF_MONTH);
		eDate = DateUtil.getDateBeforeOrAfter(eDate, -(eInd-1));
		System.out.println("结束日期:"+ dateFormat.format(eDate));

		String querySql;
		List<Map<String, Object>> useTimeList = new ArrayList<>();
		while(sDate.getTime() <= eDate.getTime()){
			//获取所在月份的天数
			calendar.setTime(sDate);
			int inval = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

			querySql = "SELECT count(*) sumCount FROM bi_usetime_detail where day = ? AND platform_id = ?";

			Map<String, Object> map = DruidUtil.queryUniqueResult(readConnection, querySql, DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, inval-1), ""), lPlatform);
			if(map == null || Integer.parseInt(map.get("sumCount").toString()) == 0){
				break;
			}
			querySql = "SELECT user_id, sum(use_time) use_time FROM bi_usetime_detail a WHERE day >=? AND day <= ? AND platform_id = ? group by user_id";
			List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, inval-1), ""), lPlatform);

			int tim0_1 = 0;
			int tim1_3 = 0;
			int tim3_10 = 0;
			int tim10_30 = 0;
			int tim30_60 = 0;
			int tim60_120 = 0;
			int tim120 = 0;
			double avgSumTime = 0.0;
			for(Map<String, Object> objMap : list){
				double avgTime = Double.parseDouble(Optional.ofNullable(objMap.get("use_time")).orElse(0).toString()) / 60 / inval;
				avgSumTime += avgTime;
				if(avgTime <=1){
					tim0_1 += 1;
				}else if(avgTime >1 && avgTime <= 3){
					tim1_3 += 1;
				}else if(avgTime >3 && avgTime <= 10){
					tim3_10 += 1;
				}else if(avgTime > 10 && avgTime <= 30){
					tim10_30 += 1;
				}else if(avgTime > 30 && avgTime <=60){
					tim30_60 += 1;
				}else if(avgTime > 60 && avgTime <= 120){
					tim60_120 += 1;
				}else if(avgTime > 120){
					tim120 += 1;
				}
			}
			Map<String, Object> retMap = new HashMap<>();
			retMap.put("day", DateUtil.formatDate(sDate, "") +"至" + DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""));
			retMap.put("avgtime", String.format("%.2f", avgSumTime / list.size()));
			retMap.put("tim0_1", tim0_1);
			retMap.put("tim1_3", tim1_3);
			retMap.put("tim3_10", tim3_10);
			retMap.put("tim10_30", tim10_30);
			retMap.put("tim30_60", tim30_60);
			retMap.put("tim60_120", tim60_120);
			retMap.put("tim120", tim120);

			useTimeList.add(retMap);

			sDate = DateUtil.getDateBeforeOrAfter(sDate, inval);
		}

		return useTimeList;
	}

	/**
	 *
	 * @param readConnection
	 * @param sDate
	 * @param eDate
	 * @param lPlatform
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> getPlayTimeForDay(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {
		String querySql = "SELECT * FROM bi_validatetime a WHERE day >=? AND day <= ? AND platform_id = ? ORDER BY day desc";
		List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

		return list;
	}

	public List<Map<String, Object>> getPlayTimeForWeek(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		//获取sDate所在周的周一
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sDate);
		int sInd = calendar.get(Calendar.DAY_OF_WEEK);
		sDate = DateUtil.getDateBeforeOrAfter(sDate, -(sInd-2));
		System.out.println("开始日期:"+ dateFormat.format(sDate));

		//获取eDate所在周的周一
		calendar.setTime(eDate);
		int eInd = calendar.get(Calendar.DAY_OF_WEEK);
		eDate = DateUtil.getDateBeforeOrAfter(eDate, -(eInd-2));
		System.out.println("结束日期:"+ dateFormat.format(eDate));

		String querySql;
		List<Map<String, Object>> playTimeList = new ArrayList<>();
		while(sDate.getTime() <= eDate.getTime()){
			querySql = "SELECT count(*) sumCount FROM bi_validatetime_detail where day = ? AND platform_id = ?";

			Map<String, Object> map = DruidUtil.queryUniqueResult(readConnection, querySql, DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""), lPlatform);
			if(map == null || Integer.parseInt(map.get("sumCount").toString()) == 0){
				break;
			}
			querySql = "SELECT user_id, sum(effec_time) effec_time FROM bi_validatetime_detail a WHERE day >=? AND day <= ? AND platform_id = ? group by user_id";
			List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

			int tim0_1 = 0;
			int tim1_3 = 0;
			int tim3_10 = 0;
			int tim10_30 = 0;
			int tim30_60 = 0;
			int tim60_120 = 0;
			int tim120 = 0;
			double avgSumTime = 0.0;
			for(Map<String, Object> objMap : list){
				double avgTime = Double.parseDouble(Optional.ofNullable(objMap.get("effec_time")).orElse(0).toString()) / 60 / 7;
				avgSumTime += avgTime;
				if(avgTime <=1){
					tim0_1 += 1;
				}else if(avgTime >1 && avgTime <= 3){
					tim1_3 += 1;
				}else if(avgTime >3 && avgTime <= 10){
					tim3_10 += 1;
				}else if(avgTime > 10 && avgTime <= 30){
					tim10_30 += 1;
				}else if(avgTime > 30 && avgTime <=60){
					tim30_60 += 1;
				}else if(avgTime > 60 && avgTime <= 120){
					tim60_120 += 1;
				}else if(avgTime > 120){
					tim120 += 1;
				}
			}
			Map<String, Object> retMap = new HashMap<>();
			retMap.put("day", DateUtil.formatDate(sDate, "") +"至" + DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""));
			retMap.put("avgtime", String.format("%.2f", avgSumTime / list.size()));
			retMap.put("tim0_1", tim0_1);
			retMap.put("tim1_3", tim1_3);
			retMap.put("tim3_10", tim3_10);
			retMap.put("tim10_30", tim10_30);
			retMap.put("tim30_60", tim30_60);
			retMap.put("tim60_120", tim60_120);
			retMap.put("tim120", tim120);

			playTimeList.add(retMap);

			sDate = DateUtil.getDateBeforeOrAfter(sDate, 7);
		}

		return playTimeList;
	}

	public List<Map<String, Object>> getPlayTimeForMonth(Connection readConnection, Date sDate, Date eDate, Integer lPlatform) throws SQLException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		//获取sDate所在月
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sDate);
		int sInd = calendar.get(Calendar.DAY_OF_MONTH);
		sDate = DateUtil.getDateBeforeOrAfter(sDate, -(sInd-1));
		System.out.println("开始日期:"+ dateFormat.format(sDate));

		//获取eDate所在月
		calendar.setTime(eDate);
		int eInd = calendar.get(Calendar.DAY_OF_MONTH);
		eDate = DateUtil.getDateBeforeOrAfter(eDate, -(eInd-1));
		System.out.println("结束日期:"+ dateFormat.format(eDate));

		String querySql;
		List<Map<String, Object>> playTimeList = new ArrayList<>();
		while(sDate.getTime() <= eDate.getTime()){
			//获取所在月份的天数
			calendar.setTime(sDate);
			int inval = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

			querySql = "SELECT count(*) sumCount FROM bi_validatetime_detail where day = ? AND platform_id = ?";

			Map<String, Object> map = DruidUtil.queryUniqueResult(readConnection, querySql, DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, inval-1), ""), lPlatform);
			if(map == null || Integer.parseInt(map.get("sumCount").toString()) == 0){
				break;
			}
			querySql = "SELECT user_id, sum(effec_time) effec_time FROM bi_validatetime_detail a WHERE day >=? AND day <= ? AND platform_id = ? group by user_id";
			List<Map<String, Object>> list = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, inval-1), ""), lPlatform);

			int tim0_1 = 0;
			int tim1_3 = 0;
			int tim3_10 = 0;
			int tim10_30 = 0;
			int tim30_60 = 0;
			int tim60_120 = 0;
			int tim120 = 0;
			double avgSumTime = 0.0;
			for(Map<String, Object> objMap : list){
				double avgTime = Double.parseDouble(Optional.ofNullable(objMap.get("effec_time")).orElse(0).toString()) / 60 / inval;
				avgSumTime += avgTime;
				if(avgTime <=1){
					tim0_1 += 1;
				}else if(avgTime >1 && avgTime <= 3){
					tim1_3 += 1;
				}else if(avgTime >3 && avgTime <= 10){
					tim3_10 += 1;
				}else if(avgTime > 10 && avgTime <= 30){
					tim10_30 += 1;
				}else if(avgTime > 30 && avgTime <=60){
					tim30_60 += 1;
				}else if(avgTime > 60 && avgTime <= 120){
					tim60_120 += 1;
				}else if(avgTime > 120){
					tim120 += 1;
				}
			}
			Map<String, Object> retMap = new HashMap<>();
			retMap.put("day", DateUtil.formatDate(sDate, "") +"至" + DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(sDate, 6), ""));
			retMap.put("avgtime", String.format("%.2f", avgSumTime / list.size()));
			retMap.put("tim0_1", tim0_1);
			retMap.put("tim1_3", tim1_3);
			retMap.put("tim3_10", tim3_10);
			retMap.put("tim10_30", tim10_30);
			retMap.put("tim30_60", tim30_60);
			retMap.put("tim60_120", tim60_120);
			retMap.put("tim120", tim120);

			playTimeList.add(retMap);

			sDate = DateUtil.getDateBeforeOrAfter(sDate, inval);
		}

		return playTimeList;
	}

	public IResultInfo<Map<String, Object>> fetchPlayCount(Integer lPlatform, Date sDate, Date eDate,
			String contentType, String contentCP, String contentKey) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " WHERE biv.day >='"+DateUtil.formatDate(sDate, "")+"' AND biv.day <= '"+DateUtil.formatDate(eDate, "")+"' AND biv.platform_id = "+lPlatform+" ";
			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			if(StringUtil.hasText(contentKey)){
				strWhere += " AND m.name like '%" +contentKey+ "%'";
			}
			System.out.println(System.currentTimeMillis());
			String querySql = "SELECT biv.*, app.name app_name, m.`name` media_name, cp.`name` cp_name, m.type media_type FROM bi_validcount_day biv " +
					"LEFT JOIN x_app app ON biv.app_id = app.app_id " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere + " ORDER BY biv.validCount DESC";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql);
			System.out.println(System.currentTimeMillis());
			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchPlayCount error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> loadCPs() {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "x_content_provider";
			String querySql = "SELECT * FROM "+tabName;

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql);

			ri = new ResultInfo<>("success", retList, retList.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("loadCPs error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> loadMediaPer(Integer platFormId, String day, String mediaId) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_media_per";
			String querySql = "SELECT * FROM "+tabName +" WHERE platform_id = ? AND day = ? AND media_id = ?";
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, platFormId, day, mediaId);

			ri = new ResultInfo<>("success", retList, retList.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("loadMediaPer error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> loadDayUseTime(Integer lPlatform, Date day) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_usetime";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day = ? AND platform_id = ?";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(DateUtil.getDateBeforeOrAfter(day, -1), ""), lPlatform);

			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("loadDayUseTime error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> fetchAllPlayCount(Integer lPlatform, Date sDate, Date eDate,
															  String contentType, String contentCP, String contentKey) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " WHERE biv.day >='"+DateUtil.formatDate(sDate, "")+"' AND biv.day <= '"+DateUtil.formatDate(eDate, "")+"' AND biv.platform_id = "+lPlatform+" ";

			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			if(StringUtil.hasText(contentKey)){
				strWhere += " AND m.name like '%" +contentKey+ "%'";
			}
			System.out.println(System.currentTimeMillis());
			String querySql = "SELECT biv.*, m.`name` media_name, cp.`name` cp_name, m.type media_type FROM bi_playcount_day biv " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere + " ORDER BY biv.play_count DESC";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql);
			System.out.println(System.currentTimeMillis());
			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			ex.printStackTrace();
			logger.error("fetchAllPlayCount error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}


	public void exportAllPlayCount(HttpServletResponse response, Integer lPlatform, Date sDate, Date eDate,
								   String contentType, String contentCP, String contentKey) {
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " WHERE biv.day >='"+DateUtil.formatDate(sDate, "")+"' AND biv.day <= '"+DateUtil.formatDate(eDate, "")+"' AND biv.platform_id = "+lPlatform+" ";

			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			if(StringUtil.hasText(contentKey)){
				strWhere += " AND m.name like '%" +contentKey+ "%'";
			}

			String querySql = "SELECT biv.day day, biv.media_id media_id, biv.play_count, m.`name` media_name, cp.`name` cp_name, bmp.per_20, bmp.per_20_50, bmp.per_50_80, bmp.per_80 " +
					"FROM bi_playcount_day biv " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " +
					"INNER JOIN bi_media_per bmp ON biv.day = bmp.day AND biv.media_id = bmp.media_id "+ strWhere + " ORDER BY biv.play_count DESC";
			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql);

			Map<String, Object> mapColumn = new LinkedHashMap<>();
			mapColumn.put("day", "日期");
			mapColumn.put("media_id", "内容ID");
			mapColumn.put("media_name", "内容名称");
			mapColumn.put("cp_name", "供应商");
			mapColumn.put("play_count", "完整播放次数");
			mapColumn.put("per_20", "20%以下");
			mapColumn.put("per_20_50", "20%_50%");
			mapColumn.put("per_50_80", "50%_80%");
			mapColumn.put("per_80", "80%以上");

			//导出数据
			ExcelUtilsNew.ExcelExportOutPut(response, listControl, mapColumn, "playCount_"+ DateUtil.formatDate(new Date(), ""));

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("fetchAllPlayCount error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	public void loadMediaExport(HttpServletResponse response, Integer lPlatform, Date sDate, Date eDate,
															String contentType, String contentCP, String contentKey) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " WHERE biv.day >='"+DateUtil.formatDate(sDate, "")+"' AND biv.day <= '"+DateUtil.formatDate(eDate, "")+"' AND biv.platform_id = "+lPlatform+" ";

			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			if(StringUtil.hasText(contentKey)){
				strWhere += " AND m.name like '%" +contentKey+ "%'";
			}

			String querySql = "SELECT biv.day day, biv.media_id media_id, sum(biv.play_count) sumCount, m.`name` media_name, cp.`name` cp_name " +
					"FROM bi_playcount_day biv " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere + " group by(media_id) ORDER BY biv.play_count DESC";
			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql);

			listControl.forEach(item ->{
				item.put("day", DateUtil.formatDate(sDate, "")+"-"+DateUtil.formatDate(eDate, ""));
			});

			Map<String, Object> mapColumn = new LinkedHashMap<>();
			mapColumn.put("day", "日期");
			mapColumn.put("media_id", "内容ID");
			mapColumn.put("media_name", "内容名称");
			mapColumn.put("cp_name", "供应商");
			mapColumn.put("sumCount", "完整播放次数");


			//导出数据
			ExcelUtilsNew.ExcelExportOutPut(response, listControl, mapColumn, "playSumCount_"+ DateUtil.formatDate(new Date(), ""));


		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("loadMediaExport error" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
	}
}
