package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContentProviderService {
	private final static Logger logger = LoggerFactory.getLogger(ContentProviderService.class);

	public static ContentProviderService getInstance() {
		return _instance;
	}
    private static ContentProviderService _instance = new ContentProviderService();
	private ContentProviderService() { logger.info("Initial ContentProviderService"); }

	public IResultInfo<Map<String, Object>> getContentCPs(Integer lPlatform, Date startDate, Date endDate,
															 String contentCP, String contentType) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " WHERE platform_id = ? "; //需要有效播放数据
			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			String querySql = "SELECT  biv.*, m.`name` media_name, cp.`name` cp_name, m.type media_type FROM bi_media_collect biv " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere ;

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, lPlatform);

			List<Map<String, Object>> listTotalCount = DruidUtil.queryList(readConnection, querySql, lPlatform);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, listTotalCount.size(), "");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getCollectDetail error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getSearchDetail(Integer lPlatform, Date startDate, Date endDate) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT keyword, status, sum(search_num) sum_num FROM bi_prosearch_day " +
					"WHERE platform_id = ? AND  day >= ? AND day <= ? GROUP by keyword, status";
			List<Map<String, Object>> dataList = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(startDate), dateFormat.format(endDate));

			List<Map<String, Object>> retList = new ArrayList<>();
			for(Map<String, Object> map : dataList){
				String keyWord = map.get("keyword").toString();
				String status = map.get("status").toString();

				Map<String, Object> dataMap = null;
				for(Map<String, Object> retMap : retList){
					String retkeyWord = retMap.get("keyword").toString();
					if(keyWord.equals(retkeyWord)){
						dataMap = retMap;
						break;
					}
				}
				if(dataMap == null){		//不存在
					dataMap = new HashMap<>();
					retList.add(dataMap);

					dataMap.put("keyword", keyWord);
					dataMap.put("accept_num", 0);
					if("1".equals(status)){
						dataMap.put("accept_num", map.get("sum_num"));
					}
					dataMap.put("sum_num", map.get("sum_num"));
				}else{						//已存在
					int sumNum = Integer.parseInt(dataMap.get("sum_num").toString());
					int acceptNum = Integer.parseInt(dataMap.get("accept_num").toString());
					if("1".equals(status)){
						acceptNum += Integer.parseInt(map.get("sum_num").toString());
					}
					sumNum += Integer.parseInt(map.get("sum_num").toString());
					dataMap.put("sum_num", sumNum);
					dataMap.put("accept_num", acceptNum);
				}
			}
			//计算占比
			DecimalFormat df = new DecimalFormat("0.00");
			for(Map<String, Object> map : retList){
				int sumNum = Integer.parseInt(map.get("sum_num").toString());
				int acceptNum = Integer.parseInt(map.get("accept_num").toString());
				String acceptPer = df.format((float)acceptNum * 100/sumNum);
				map.put("accept_per", acceptPer);
			}

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getSearchDetail error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getCollectDetail(Integer lPlatform, Integer curPage, Integer pageSize,
															 String contentCP, String contentType, String contentKey) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			int iStart = (curPage -1) * pageSize;

			String strWhere = " WHERE biv.platform_id = ? ";
			if(StringUtil.hasText(contentType)){
				strWhere += " AND m.type =" + contentType;
			}
			if(StringUtil.hasText(contentCP)){
				strWhere += " AND cp.id = " + contentCP;
			}
			if(StringUtil.hasText(contentKey)){
				strWhere += " AND m.name like '%"+ contentKey +"%'";
			}
			String querySql = "SELECT  biv.*, m.`name` media_name, cp.`name` cp_name, m.type media_type, biv.count count FROM bi_media_collect biv " +
					"INNER JOIN z_media_local zm ON biv.media_id = zm.local_id AND biv.platform_id = zm.platform_id " +
					"INNER JOIN x_media m ON zm.entity_code = m.code " +
					"INNER JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere +" ORDER BY count DESC LIMIT " + iStart +"," + pageSize;

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, lPlatform);

			querySql = "SELECT 1 FROM bi_media_collect biv " +
					"LEFT JOIN x_media m ON biv.media_id = m.id " +
					"LEFT JOIN x_content_provider cp ON m.content_provider_id = cp.id " + strWhere;
			List<Map<String, Object>> listTotalCount = DruidUtil.queryList(readConnection, querySql, lPlatform);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, listTotalCount.size(), "");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getCollectDetail error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
