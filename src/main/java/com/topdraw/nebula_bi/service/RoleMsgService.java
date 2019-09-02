package com.topdraw.nebula_bi.service;

import com.alibaba.fastjson.JSONObject;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class RoleMsgService {
	private final static Logger logger = LoggerFactory.getLogger(RoleMsgService.class);

	public static RoleMsgService getInstance() {
		return _instance;
	}
    private static RoleMsgService _instance = new RoleMsgService();
	private RoleMsgService() { logger.info("Initial RoleMsgService"); }

	public IResultInfo<Map<String, Object>> getRoles(String queryVal) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String strWhere = " 1=1";
			if(StringUtil.hasText(queryVal)){
				strWhere += " AND product_name = " + queryVal;
			}
			String queySql = "SELECT * FROM x_role WHERE" + strWhere;

			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, queySql);

			ri = new ResultInfo<>("success", retlist, retlist.size(), null);
		} catch (SQLException e) {
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getRoles error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> getAnthTree() {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String queySql = "SELECT * FROM x_backoffice_feature";
			List<Map<String, Object>> listFeature = DruidUtil.queryList(readConnection, queySql);

			List<Map<String, Object>> retDates = new ArrayList<>();

			for (Map<String, Object> map : listFeature) {
				//任意node节点
				Map<String, Object> treeNodeChildren = new HashMap<>();
				treeNodeChildren.put("id", Integer.parseInt(map.get("id").toString()));
				treeNodeChildren.put("name", map.get("friendly_name").toString());

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
			}

			ri = new ResultInfo<>("success", retDates, retDates.size(), null);
		} catch (SQLException e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("getAnthTree error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> addRoleSubmit(String roleName, String roleDescription, String chooseKeys) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		try {

			writeConnection = DruidUtil.getRandomWriteConnection();
			DruidUtil.beginTransaction(writeConnection);
			//添加角色
			Map<String, Object> mapRole = new HashMap<>();
			mapRole.put("name", roleName);
			mapRole.put("description", roleDescription);
			DruidUtil.save(writeConnection, mapRole, "x_role");
			//添加角色权限
			Map<String, Object> map = DruidUtil.queryUniqueResult(writeConnection, "SELECT * FROM x_role WHERE name = ?", roleName);
			Integer roleId = Integer.parseInt(map.get("id").toString());
			String[] chooseKeyArr = chooseKeys.split(",");
			for (int i = 0; i < chooseKeyArr.length; i++) {
				Map<String, Object> mapSave = new HashMap<>();
				mapSave.put("role_id", roleId);
				mapSave.put("backoffice_feature_id", chooseKeyArr[i]);
				DruidUtil.save(writeConnection, mapSave, "x_role__backoffice_feature");
			}
			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "成功创建角色");

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> loadEditRole(Integer roleId) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();
			List<Map<String, Object>> listRet = new ArrayList<>();
			//角色信息
			String queySql = "SELECT * FROM x_role WHERE id = ?";
			Map<String, Object> retMap = DruidUtil.queryUniqueResult(readConnection, queySql, roleId);
			listRet.add(retMap);
			//已有权限
			queySql = "SELECT backoffice_feature_id FROM x_role__backoffice_feature WHERE role_id =?";
			List<Map<String, Object>> listFeature = DruidUtil.queryList(readConnection, queySql, roleId);

			List<Integer> roleAuths = new ArrayList<>();
			for (Map<String, Object> map : listFeature) {
				roleAuths.add(Integer.parseInt(map.get("backoffice_feature_id").toString()));
			}
			retMap.put("roleAuths", roleAuths);

			ri = new ResultInfo<>("success", listRet, listRet.size(), null);
		} catch (SQLException e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("loadEditRole error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> editRoleSubmit(Map<String, Object> updateMap, String chooseKeys) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		try {
			writeConnection = DruidUtil.getRandomWriteConnection();
			DruidUtil.beginTransaction(writeConnection);
			//更新角色
			DruidUtil.update(writeConnection, updateMap, "x_role", "id");
			//更新角色权限
			Integer roleId = Integer.parseInt(updateMap.get("id").toString());
			String sql_del = "DELETE FROM x_role__backoffice_feature WHERE role_id = ?";
			DruidUtil.doExecute(writeConnection, sql_del, roleId);

			String[] chooseKeyArr = chooseKeys.split(",");
			for (int i = 0; i < chooseKeyArr.length; i++) {
				Map<String, Object> mapSave = new HashMap<>();
				mapSave.put("role_id", roleId);
				mapSave.put("backoffice_feature_id", chooseKeyArr[i]);
				DruidUtil.save(writeConnection, mapSave, "x_role__backoffice_feature");
			}
			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "编辑角色成功");

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> deleteRole(Integer roleId) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		try {
			writeConnection = DruidUtil.getRandomWriteConnection();
			DruidUtil.beginTransaction(writeConnection);
			//删除角色
			DruidUtil.deleteById(writeConnection, "x_role", roleId+"");
			//删除角色权限
			DruidUtil.doExecute(writeConnection, "DELETE * FROM x_role__backoffice_feature WHERE role_id = ?", roleId);
			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "删除角色成功");

		} catch (Exception ex) {
			ex.printStackTrace();
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}
}
