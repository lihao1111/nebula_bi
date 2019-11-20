package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.OrderAnaService;
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
public class OrderAnaController {
	private final static Logger logger = LoggerFactory.getLogger(OrderAnaController.class);

	@RequestMapping("/loadOrderDetail")
	@NeedAuthentication(friendlyName = "订购/退订", description = "订购/退订", servletName = "订购分析")
	@NeedAudit(auditFlag = true, auditDesc = "订购/退订")
	public IResultInfo<Map<String, Object>> loadOrderDetail(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOrderDetail");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OrderAnaService orderAnaService = OrderAnaService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String chooseType = request.getParameter("chooseType");

			ri = orderAnaService.getOrderDetail(platFormId, sDate, eDate, chooseType);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadOrderSource")
	@NeedAuthentication(friendlyName = "订购来源分析", description = "订购来源分析", servletName = "订购分析")
	@NeedAudit(auditFlag = true, auditDesc = "订购来源分析")
	public IResultInfo<Map<String, Object>> loadOrderSource(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOrderSource");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OrderAnaService orderAnaService = OrderAnaService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = orderAnaService.getOrderSource(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadOrderTrig")
	@NeedAuthentication(friendlyName = "订购触发排行", description = "订购触发排行", servletName = "订购分析")
	@NeedAudit(auditFlag = true, auditDesc = "订购触发排行")
	public IResultInfo<Map<String, Object>> loadOrderTrig(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOrderTrig");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OrderAnaService orderAnaService = OrderAnaService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = orderAnaService.getOrderTrig(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadOrderEnter")
	@NeedAuthentication(friendlyName = "订购到达统计", description = "订购到达统计", servletName = "订购分析")
	@NeedAudit(auditFlag = true, auditDesc = "订购到达统计")
	public IResultInfo<Map<String, Object>> loadOrderEnter(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOrderEnter");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OrderAnaService orderAnaService = OrderAnaService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = orderAnaService.getOrderEnter(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}


}