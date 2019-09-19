package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class UserMsgService {
	private final static Logger logger = LoggerFactory.getLogger(UserMsgService.class);

	public static UserMsgService getInstance() {
		return _instance;
	}
    private static UserMsgService _instance = new UserMsgService();
	private UserMsgService() { logger.info("Initial UserMsgService"); }

	public IResultInfo<Map<String, Object>> getUserList(String username) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhereSql = "WHERE 1=1 ";
			if(StringUtil.hasText(username)){
				strWhereSql += "AND username = '"+ username+"'";
			}
			String querySql = "SELECT * FROM x_admin " + strWhereSql;
			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql);

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("getUserList error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> saveUser(String flag, Map<String, Object> mapSave) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		Connection readConnection = null;
		try {
			writeConnection = DruidUtil.getRandomWriteConnection();
			readConnection = DruidUtil.getRandomReadConnection();

			DruidUtil.beginTransaction(writeConnection);
			if("newAdd".equals(flag)){
				Object id = DruidUtil.save(writeConnection, mapSave, "x_admin");
				//插入授权记录

				List<Map<String, Object>> listBackOfficeFeatures = DruidUtil.queryList(readConnection,
						"SELECT * FROM x_backoffice_feature");

				for (Map<String, Object> mapBackOfficeFeature : listBackOfficeFeatures) {
					Map<String, Object> mapAdminBackOfficeFeature = new HashMap<>();
					mapAdminBackOfficeFeature.put("admin_id", id);
					mapAdminBackOfficeFeature.put("backoffice_feature_name", mapBackOfficeFeature.get("name"));
					mapAdminBackOfficeFeature.put("authentication_code", 0);

					DruidUtil.save(writeConnection, mapAdminBackOfficeFeature, "x_admin__backoffice_feature");
				}

			}else {
				DruidUtil.update(writeConnection, mapSave, "x_admin", "id");
			}
			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "成功修改人员");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> deleteUser(Integer adminId) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		try {
			writeConnection = DruidUtil.getRandomWriteConnection();
			DruidUtil.beginTransaction(writeConnection);
			String deleteSql = "DELETE FROM x_admin__backoffice_feature WHERE admin_id = ?";
			DruidUtil.doExecute(writeConnection, deleteSql, adminId);
			DruidUtil.deleteById(writeConnection, "x_admin", adminId.toString());

			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "成功删除操作人员");
			return ri;

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}
	//加载用户权限
	public IResultInfo<Map<String, Object>> getUserAuth(Integer adminId) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String queySql = "SELECT bf.*,abf.authentication_code FROM x_backoffice_feature bf INNER JOIN " +
					"x_admin__backoffice_feature abf ON bf.name = abf.backoffice_feature_name " +
					"WHERE abf.admin_id = ? AND abf.authentication_code = 1";	//用户已有权限数据
			List<Map<String, Object>> listFeature = DruidUtil.queryList(readConnection, queySql, adminId);
/*
			List<Map<String, Object>> retDates = new ArrayList<>();
			for (Map<String, Object> map : listFeature) {
				//任意node节点
				Map<String, Object> treeNodeChildren = new HashMap<>();
				treeNodeChildren.put("id", Integer.parseInt(map.get("id").toString()));
				treeNodeChildren.put("name", map.get("friendly_name").toString());
				treeNodeChildren.put("auth_code", map.get("authentication_code").toString());

				String strGroupName = map.get("servlet_name").toString();
				Map<String, Object> treeNodeParent = null;
				for(Map<String, Object> treeNode : retDates){
					if(strGroupName.equals(treeNode.get("name").toString())){
						treeNodeParent = treeNode;
					}
				}
				if(treeNodeParent == null){	//首次加载
					treeNodeParent = new HashMap<>();
					List<Map<String, Object>> list = new ArrayList<>();
					list.add(treeNodeChildren);
					treeNodeParent.put("children", list);
					treeNodeParent.put("name", strGroupName);
					retDates.add(treeNodeParent);
				}else{
					((List<Map<String, Object>>) treeNodeParent.get("children")).add(treeNodeChildren);
				}
			}*/
			ri = new ResultInfo<>("success", listFeature, listFeature.size(), null);

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	//加载用户权限
	public IResultInfo<Map<String, Object>> authUser(Integer adminId, String chooseAuthIds) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;
		Connection writeConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();
			writeConnection = DruidUtil.getRandomWriteConnection();
			DruidUtil.beginTransaction(writeConnection);

			String queySql = "SELECT name FROM x_backoffice_feature WHERE id in (" + chooseAuthIds+ ")";
			List<Map<String, Object>> listFeature = DruidUtil.queryList(readConnection, queySql);

			String chooseAuthName = "";
			for (Map<String, Object> map : listFeature) {
				chooseAuthName += "'"+map.get("name").toString()+"',";
			}
			chooseAuthName = chooseAuthName.substring(0, chooseAuthName.length()-1);
			String updateSql = "UPDATE x_admin__backoffice_feature set authentication_code = 1 WHERE admin_id = ? AND backoffice_feature_name in (" + chooseAuthName + ")";
			DruidUtil.doExecute(writeConnection, updateSql, adminId);

			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "成功更新人员权限");

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
			DruidUtil.close(writeConnection);
		}
		return ri;
	}
}
