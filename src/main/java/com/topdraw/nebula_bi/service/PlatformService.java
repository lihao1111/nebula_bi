package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.*;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlatformService {
	private final static Logger logger = LoggerFactory.getLogger(PlatformService.class);

	public static PlatformService getInstance() {
		return _instance;
	}
    private static PlatformService _instance = new PlatformService();
	private PlatformService() { logger.info("Initial PlatformService"); }

    public IResultInfo<Map<String, Object>> fetchPlatform(long lAdminId) {
        IResultInfo<Map<String, Object>> ri;
        Connection readConnection = null;
        try {
			readConnection = DruidUtil.getRandomReadConnection();

			/*String sql_rel = "SELECT p.* FROM x_admin__platform ap INNER JOIN x_platform p" +
					" ON ap.platform_id = p.id WHERE ap.admin_id = ?" +
					" ORDER BY p.id ASC";*/
			String sql_rel = "SELECT p.* FROM x_platform p ORDER BY p.id ASC";

			List<Map<String, Object>> listControl = DruidUtil.queryList(readConnection, sql_rel);

			ri = new ResultInfo<>("success", listControl, listControl.size(), "");

        } catch (Exception ex) {
            ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("fetchPlatform error" + ex.getMessage());
        } finally {
            DruidUtil.close(readConnection);
        }
        return ri;
    }
}
