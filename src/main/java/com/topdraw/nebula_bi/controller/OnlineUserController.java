package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.OnlineUserService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
public class OnlineUserController {
	private final static Logger logger = LoggerFactory.getLogger(OnlineUserController.class);

	@RequestMapping("/loadOnlineUser")
	@NeedAuthentication(friendlyName = "实时在线", description = "实时在线", servletName = "实时概括")
	@NeedAudit(auditFlag = true, auditDesc = "实时在线")
	public IResultInfo<Map<String, Object>> loadOnlineUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOnlineUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlineUserService onlineUserService = OnlineUserService.getInstance();
		Date queryDate;
		try {
			queryDate = dateFormat.parse(request.getParameter("queryDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineUserService.getOnlineUser(lPlatform, queryDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/compareOnlineUser")
	public IResultInfo<Map<String, Object>> compareOnlineUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("compareOnlineUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlineUserService onlineUserService = OnlineUserService.getInstance();
		Date compareDate;
		try {
			compareDate = dateFormat.parse(request.getParameter("compareDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineUserService.getCompareUser(lPlatform, compareDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadCurUser")
	public IResultInfo<Map<String, Object>> loadCurUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadCurUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		OnlineUserService onlineUserService = OnlineUserService.getInstance();
		try {
			Date date = dateFormat.parse(request.getParameter("nowData"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineUserService.getCurUser(lPlatform, date);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}
}
