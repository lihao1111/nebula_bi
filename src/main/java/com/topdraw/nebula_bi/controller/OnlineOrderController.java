package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.OnlineOrderService;
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
public class OnlineOrderController {
	private final static Logger logger = LoggerFactory.getLogger(OnlineOrderController.class);

	@RequestMapping("/loadOnlineOrder")
	@NeedAuthentication(friendlyName = "实时订购", description = "实时订购", servletName = "实时概括")
	@NeedAudit(auditFlag = true, auditDesc = "实时订购")
	public IResultInfo<Map<String, Object>> loadOnlineOrder(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOnlineOrder");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlineOrderService onlineOrderService = OnlineOrderService.getInstance();
		Date queryDate;
		try {
			queryDate = dateFormat.parse(request.getParameter("queryDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineOrderService.getOnlineOrder(lPlatform, queryDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/compareOnlineOrder")
	public IResultInfo<Map<String, Object>> compareOnlineUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("compareOnlineOrder");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlineOrderService onlineOrderService = OnlineOrderService.getInstance();
		Date compareDate;
		try {
			compareDate = dateFormat.parse(request.getParameter("compareDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineOrderService.getCompareOrder(lPlatform, compareDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadDayOrder")
	public IResultInfo<Map<String, Object>> loadDayOrder(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayOrder");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlineOrderService onlineOrderService = OnlineOrderService.getInstance();
		Date nowDate;
		try {
			nowDate = dateFormat.parse(request.getParameter("nowDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineOrderService.getDayOrder(lPlatform, nowDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadCurOrder")
	public IResultInfo<Map<String, Object>> loadCurOrder(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayOrder");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		OnlineOrderService onlineOrderService = OnlineOrderService.getInstance();
		Date nowDate;
		try {
			nowDate = dateFormat.parse(request.getParameter("nowData"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlineOrderService.getCurOrder(lPlatform, nowDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}
}
