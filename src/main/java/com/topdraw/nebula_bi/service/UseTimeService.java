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
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UseTimeService {
	private final static Logger logger = LoggerFactory.getLogger(UseTimeService.class);

	public static UseTimeService getInstance() {
		return _instance;
	}
    private static UseTimeService _instance = new UseTimeService();
	private UseTimeService() { logger.info("Initial UseTimeService"); }

    public IResultInfo<Map<String, Object>> fetchUseTime(Integer lPlatform, Date sDate, Date eDate) {
        IResultInfo<Map<String, Object>> ri;
        Connection readConnection = null;
        try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_usetime";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day >=? AND day <= ? AND platform_id = ? ORDER BY day desc";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

        } catch (Exception ex) {
            ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchUseTime error" + ex.getMessage());
        } finally {
            DruidUtil.close(readConnection);
        }
        return ri;
    }

	public IResultInfo<Map<String, Object>> fetchPlayTime(Integer lPlatform, Date sDate, Date eDate) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_validatetime";
			String querySql = "SELECT * FROM "+tabName+" a WHERE day >=? AND day <= ? AND platform_id = ? ORDER BY day desc";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, querySql, DateUtil.formatDate(sDate, ""), DateUtil.formatDate(eDate, ""), lPlatform);

			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchUseTime error" + ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
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



}
