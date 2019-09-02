package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.RoleMsgService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RoleMsgController {
	private final static Logger logger = LoggerFactory.getLogger(RoleMsgController.class);

	@RequestMapping("/loadRoles")
	@NeedAuthentication(friendlyName = "角色管理", description = "角色管理", servletName = "系统管理")
	@NeedAudit(auditFlag = true, auditDesc = "角色管理")
	public IResultInfo<Map<String, Object>> loadRoles(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		String queryVal = request.getParameter("queryVal");

		ri = roleMsgService.getRoles(queryVal);
		return ri;
	}

	@RequestMapping("/loadAnthTree")
	public IResultInfo<Map<String, Object>> loadAnthTree(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		ri = roleMsgService.getAnthTree();
		return ri;
	}

	@RequestMapping("/addRoleSubmit")
	public IResultInfo<Map<String, Object>> addRoleSubmit(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		String roleName = request.getParameter("roleName");
		String roleDescription = request.getParameter("roleDescription");
		String chooseKeys = request.getParameter("chooseKeys");

		ri = roleMsgService.addRoleSubmit(roleName, roleDescription, chooseKeys);
		return ri;
	}

	@RequestMapping("/loadEditRole")
	public IResultInfo<Map<String, Object>> loadEditRole(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		Integer roleId = Integer.parseInt(request.getParameter("roleId"));

		ri = roleMsgService.loadEditRole(roleId);
		return ri;
	}

	@RequestMapping("/editRoleSubmit")
	public IResultInfo<Map<String, Object>> editRoleSubmit(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		Map<String, Object> updateMap = new HashMap<>();
		updateMap.put("id", Integer.parseInt(request.getParameter("roleId")));
		updateMap.put("name", request.getParameter("roleName"));
		updateMap.put("description", request.getParameter("roleDescription"));
		String chooseKeys = request.getParameter("chooseKeys");

		ri = roleMsgService.editRoleSubmit(updateMap, chooseKeys);
		return ri;
	}

	@RequestMapping("/deleteRow")
	public IResultInfo<Map<String, Object>> deleteRow(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		RoleMsgService roleMsgService = RoleMsgService.getInstance();

		Integer roleId = Integer.parseInt(request.getParameter("roleId"));

		ri = roleMsgService.deleteRole(roleId);
		return ri;
	}
}