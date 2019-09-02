package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.OnlinePVUVService;
import com.topdraw.nebula_bi.service.OnlineUserService;
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
public class OnlinePVUVController {
	private final static Logger logger = LoggerFactory.getLogger(OnlinePVUVController.class);

	@RequestMapping("/loadPVUVList")
	@NeedAuthentication(friendlyName = "整体PV/UV", description = "整体PV/UV", servletName = "实时概括")
	@NeedAudit(auditFlag = true, auditDesc = "整体PV/UV")
	public IResultInfo<Map<String, Object>> loadPVUVList(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPVUVList");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlinePVUVService onlinePVUVService = OnlinePVUVService.getInstance();
		Date queryDate;
		try {
			queryDate = dateFormat.parse(request.getParameter("queryDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlinePVUVService.getOnlinePVUV(lPlatform, queryDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadPVUVSum")
	public IResultInfo<Map<String, Object>> loadPVUVSum(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPVUVSum");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlinePVUVService onlinePVUVService = OnlinePVUVService.getInstance();
		Date queryDate;
		try {
			queryDate = dateFormat.parse(request.getParameter("queryDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlinePVUVService.getPVUVSum(lPlatform, queryDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/compareOnlinePVUV")
	public IResultInfo<Map<String, Object>> compareOnlinePVUV(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("compareOnlinePVUV");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		OnlinePVUVService onlinePVUVService = OnlinePVUVService.getInstance();
		Date compareDate;
		try {
			compareDate = dateFormat.parse(request.getParameter("compareDate"));
			Integer lPlatform = Integer.parseInt(request.getParameter("platFormId"));

			ri = onlinePVUVService.getComparePVUV(lPlatform, compareDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}
}
