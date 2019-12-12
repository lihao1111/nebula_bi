package com.topdraw.nebula_bi.service;

import com.topdraw.nebula_bi.util.ExcelUtils;
import com.topdraw.nebula_bi.util.ExcelUtilsNew;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HBMusicHelpService {
	private final static Logger logger = LoggerFactory.getLogger(HBMusicHelpService.class);

	public static HBMusicHelpService getInstance() {
		return _instance;
	}
    private static HBMusicHelpService _instance = new HBMusicHelpService();
	private HBMusicHelpService() { logger.info("Initial HBMusicHelpService"); }

	public IResultInfo<Map<String, Object>> getPromotionList(Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();



			String querySql = "SELECT bgc.day, bgc.pv, bgc.uv, xp.title FROM bi_pvuv_promotion bgc "+
					"INNER JOIN x_promotion_item xp ON bgc.promotion_code = xp.`code` " +
					"INNER JOIN x_promotion xpg ON xp.promotion_id = xpg.id " +
					"WHERE bgc.day >= ? AND bgc.day <= ? AND bgc.platform_id = ?  ORDER BY bgc.day";



			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getPromotionList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public void exportPromotionList(HttpServletResponse response, Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();
			ExcelUtilsNew.exportHBPromontionExcel(response, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportPromotionList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	public IResultInfo<Map<String, Object>> getDayDataDtl(Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			String startDay = dateFormat.format(sDate);
			String endDay = dateFormat.format(eDate);

			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM x_hb_auth_rf WHERE day >= ? AND day <= ? ";
			List<Map<String, Object>> retList_Auth = DruidUtil.queryList(readConnection, querySql, startDay, endDay);        //包月用户信息

			querySql = "SELECT day, sum(ordered_num) allOrder, sum(newUordered_num) allNewOrder, sum(oldNewReUordered_num)+sum(oldFirstUordered_num) allOldOrder " +
					"FROM bi_pro_ordered WHERE day >= ? AND day <= ? AND platform_id = ? group by day";
			List<Map<String, Object>> retList_Order = DruidUtil.queryList(readConnection, querySql, startDay, endDay, platFormId);        //订购信息

			querySql = "SELECT * FROM x_hb_play_rf WHERE day >= ? AND day <= ?";
			List<Map<String, Object>> retList_Player = DruidUtil.queryList(readConnection, querySql, startDay, endDay);        //点播信息


			for(Map<String, Object> mapAuth : retList_Auth){
				String day = mapAuth.get("day").toString();
				for(Map<String, Object> mapOrder : retList_Order){
					String dayS = mapOrder.get("day").toString();
					if(day.equals(dayS)){
						mapAuth.putAll(mapOrder);
						break;
					}
				}
				for(Map<String, Object> mapPlayer : retList_Player){
					String dayS = mapPlayer.get("day").toString();
					if(day.equals(dayS)){
						mapAuth.putAll(mapPlayer);
						break;
					}
				}
			}


			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList_Auth, retList_Auth.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getDayDataDtl error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}


	public void exportDayDataList(HttpServletResponse response, Integer platFormId, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			ExcelUtilsNew.exportHBDayDataExcel(response, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);


		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportDayDataList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	public IResultInfo<Map<String, Object>> getCpInfoDtl(Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM x_hb_cp_info WHERE day >= ? AND day <= ? ORDER BY cp_name, day desc";

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate));

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getCpInfoDtl error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public void exportCpInfoList(HttpServletResponse response, Date sDate, Date eDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			ExcelUtilsNew.exportHBCpInfoExcel(response, dateFormat.format(sDate), dateFormat.format(eDate));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportCpInfoList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	public IResultInfo<Map<String, Object>> getCpTopList(Date sDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			//彩虹音乐和万泰和兴
			String querySql = "(SELECT cpw.day, cp.name, m.name mediaName, cpw.play_count, cpw.play_num from x_hb_cpPlay_week cpw " +
					"INNER JOIN z_media_local zm ON cpw.media_id = zm.local_id and  zm.platform_id = 14 " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " +
					"WHERE cp.`id` = 44 AND cpw.`day` = '"+dateFormat.format(sDate)+"' order by CONVERT(cpw.play_count, SIGNED) desc limit 10) " +
					"UNION ALL " +
					"(SELECT cpw.day, cp.name, m.name mediaName, cpw.play_count mediaName, cpw.play_num from x_hb_cpPlay_week cpw " +
					"INNER JOIN z_media_local zm ON cpw.media_id = zm.local_id and  zm.platform_id = 14 " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " +
					"WHERE cp.`id` = 47 AND cpw.`day` = '"+dateFormat.format(sDate)+"' order by CONVERT(cpw.play_count, SIGNED) desc limit 10)";


			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getCpTopList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
