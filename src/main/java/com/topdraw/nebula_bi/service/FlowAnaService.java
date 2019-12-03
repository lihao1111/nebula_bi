package com.topdraw.nebula_bi.service;

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

public class FlowAnaService {
	private final static Logger logger = LoggerFactory.getLogger(FlowAnaService.class);

	public static FlowAnaService getInstance() {
		return _instance;
	}
    private static FlowAnaService _instance = new FlowAnaService();
	private FlowAnaService() { logger.info("Initial FlowAnaService"); }

	public IResultInfo<Map<String, Object>> getFlowAna(Integer lPlatform, Date sDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String tabName = "bi_linkpoint_day";
			String querySql = "SELECT * FROM "+tabName+" a WHERE platform_id = ? AND day = ? AND linkLNode <> linkENode";
			List<Map<String, Object>> dataList = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(sDate));

			querySql = "SELECT * FROM bi_ui_deep WHERE platform_id = ?";
			List<Map<String, Object>> deepDefList = DruidUtil.queryList(readConnection, querySql, lPlatform);

			List<Map<String, Object>> retList = new ArrayList<>();
			//过滤数据
			int leaveNodeDeep = 0;
			int enterNodeDeep = 0;
			String leaveNodeDesc = "";
			String enterNodeDesc = "";
			for(Map<String, Object> map : dataList){
				String leaveNode = map.get("linkLNode").toString();
				String enterNode = map.get("linkENode").toString();
				for(Map<String, Object> deepMap : deepDefList){
					String uiName = deepMap.get("ui_name").toString();
					int deep = Integer.parseInt(deepMap.get("deep").toString());
					if(leaveNode.equals(uiName)){
						leaveNodeDeep = deep;
						leaveNodeDesc = deepMap.get("describe").toString();
						continue;
					}else if(enterNode.equals(uiName)){
						enterNodeDeep = deep;
						enterNodeDesc = deepMap.get("describe").toString();
						continue;
					}
				}

				if(leaveNodeDeep != 0
						&& enterNodeDeep !=0
						&& leaveNodeDeep < enterNodeDeep){  //保留 deep1 -> deep2 的数据
					map.put("leaveNodeDesc", leaveNodeDesc);
					map.put("enterNodeDesc", enterNodeDesc);
					retList.add(map);
				}
			}
			ri = new ResultInfo<>("success", retList, retList.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getFlowAna error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
