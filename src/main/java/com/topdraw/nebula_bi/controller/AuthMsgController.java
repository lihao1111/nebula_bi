package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.AuthMsgService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AuthMsgController {
	private final static Logger logger = LoggerFactory.getLogger(AuthMsgController.class);

	@RequestMapping("/loadAuthList")
	@NeedAuthentication(friendlyName = "权限管理", description = "权限管理", servletName = "系统管理")
	@NeedAudit(auditFlag = true, auditDesc = "权限管理")
	public IResultInfo<Map<String, Object>> loadAuthList(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadAuthList");

		AuthMsgService authMsgService = AuthMsgService.getInstance();
		try {
			String queryVal = request.getParameter("queryVal");

			ri = authMsgService.getAuthList(queryVal);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}
}