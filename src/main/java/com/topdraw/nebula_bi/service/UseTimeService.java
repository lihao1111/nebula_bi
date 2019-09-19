package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Date;
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

			String tabName = "bi_gscmcc_children_usetime";
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

			String tabName = "bi_gscmcc_children_validatetime";
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
			String querySql = "SELECT biv.*, m.`name` media_name, cp.`name` cp_name, m.type media_type FROM bi_validcount_day biv " +
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

			String tabName = "bi_gscmcc_children_usetime";
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
}
