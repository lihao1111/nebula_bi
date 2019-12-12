package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SubjectAnaService {
	private final static Logger logger = LoggerFactory.getLogger(SubjectAnaService.class);

	public static SubjectAnaService getInstance() {
		return _instance;
	}
    private static SubjectAnaService _instance = new SubjectAnaService();
	private SubjectAnaService() { logger.info("Initial SubjectAnaService"); }

	public IResultInfo<Map<String, Object>> getSubjectList(Integer platFormId, Date sDate, Date eDate, String chooseType) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "";
			if("day".equals(chooseType)){		//按日
				querySql = "SELECT ss.code, ss.name, bss.day, bss.pv, bss.uv FROM x_special_subject ss  inner join " +
						"bi_pvuv_subject bss on ss.code = bss.subject_code WHERE bss.day >= ? AND bss.day <= ? AND bss.platform_id = ? " +
						"ORDER by day desc, bss.pv desc";
			}
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, dateFormat.format(sDate), dateFormat.format(eDate), platFormId);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getSubjectList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}
}
