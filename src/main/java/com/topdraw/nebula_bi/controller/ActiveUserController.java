package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.ActiveUserService;
import com.topdraw.nebula_bi.service.LoginUserService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
public class ActiveUserController {
	private final static Logger logger = LoggerFactory.getLogger(ActiveUserController.class);

	@RequestMapping("/loadActiveUser")
	@NeedAuthentication(friendlyName = "用户活跃", description = "用户活跃", servletName = "用户分析")
	@NeedAudit(auditFlag = true, auditDesc = "用户活跃")
	public IResultInfo<Map<String, Object>> loadActiveUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadActiveUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ActiveUserService activeUserService = ActiveUserService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String type = request.getParameter("type");

			ri = activeUserService.getActiveUser(platFormId, type, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadDayUV")
	public IResultInfo<Map<String, Object>> loadDayUV(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayUV");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ActiveUserService activeUserService = ActiveUserService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = activeUserService.loadDayUV(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}