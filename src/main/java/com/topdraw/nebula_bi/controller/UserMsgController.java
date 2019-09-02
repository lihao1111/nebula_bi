package com.topdraw.nebula_bi.controller;

import com.alibaba.fastjson.JSONArray;
import com.topdraw.nebula_bi.service.UserMsgService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserMsgController {
	private final static Logger logger = LoggerFactory.getLogger(UserMsgController.class);

	@RequestMapping("/loadUserList")
	@NeedAuthentication(friendlyName = "用户管理", description = "用户管理", servletName = "系统管理")
	@NeedAudit(auditFlag = true, auditDesc = "用户管理")
	public IResultInfo<Map<String, Object>> loadUserList(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadUserList");

		UserMsgService userMsgService = UserMsgService.getInstance();
		try {
			String username = request.getParameter("username");

			ri = userMsgService.getUserList(username);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;

	}

	@RequestMapping("/saveUser")
	public IResultInfo<Map<String, Object>> saveUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("saveUser");

		UserMsgService userMsgService = UserMsgService.getInstance();
		try {
			String flag = request.getParameter("flag");
			Map<String, Object> dataMap = new HashMap<>();
			if(flag != "newAdd"){
				dataMap.put("id", request.getParameter("id"));
			}
			dataMap.put("username", request.getParameter("username"));
			dataMap.put("password", request.getParameter("password"));
			dataMap.put("realname", URLDecoder.decode(request.getParameter("realname"), "UTF-8"));
			dataMap.put("group_id", request.getParameter("group_id"));
			dataMap.put("gender", request.getParameter("gender"));
			dataMap.put("image", (new JSONArray()).toString());
			dataMap.put("password_md5", MD5Util.encodePassword(dataMap.get("password").toString()));
			dataMap.put("type", Integer.parseInt(request.getParameter("type") == null ? "0" : request.getParameter("type")));

			ri = userMsgService.saveUser(flag, dataMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/deleteUser")
	public IResultInfo<Map<String, Object>> deleteUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("deleteUser");

		UserMsgService userMsgService = UserMsgService.getInstance();
		try {
			Integer id = Integer.parseInt(request.getParameter("id"));

			ri = userMsgService.deleteUser(id);

		} catch (Exception e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("删除用户异常", e);
		}
		return ri;
	}

	@RequestMapping("/loadUserAuth")
	public IResultInfo<Map<String, Object>> loadUserAuth(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadUserAuth");

		UserMsgService userMsgService = UserMsgService.getInstance();
		try {
			Integer id = Integer.parseInt(request.getParameter("id"));
			ri = userMsgService.getUserAuth(id);

		} catch (Exception e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("用户权限加载异常", e);
		}
		return ri;
	}

	@RequestMapping("/authUserSubmit")
	public IResultInfo<Map<String, Object>> authUserSubmit(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("authUserSubmit");

		UserMsgService userMsgService = UserMsgService.getInstance();
		try {
			Integer adminId = Integer.parseInt(request.getParameter("chooseUserId"));
			String chooseKeys = request.getParameter("chooseKeys");


			ri = userMsgService.authUser(adminId, chooseKeys);

		} catch (Exception e) {
			e.printStackTrace();
			ri = new ResultInfo<>("failure", null, e.getMessage());
			logger.error("用户更新权限异常", e);
		}
		return ri;
	}
}