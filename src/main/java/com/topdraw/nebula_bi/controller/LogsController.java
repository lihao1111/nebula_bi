package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.LogService;
import org.afflatus.infrastructure.common.IResultInfo;
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
public class LogsController {
	private final static Logger logger = LoggerFactory.getLogger(LogsController.class);

	@RequestMapping("/loadDayLog")
	public IResultInfo<Map<String, Object>> loadDayLog(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayLog");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		LogService logService = LogService.getInstance();
		Date nowDate;
		try {
			nowDate = dateFormat.parse(request.getParameter("nowDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = logService.getLogs(platFormId, nowDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadOnlineErr")
	public IResultInfo<Map<String, Object>> loadOnlineErr(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadOnlineErr");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		LogService logService = LogService.getInstance();
		Date nowDate;
		try {
			nowDate = dateFormat.parse(request.getParameter("nowDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = logService.getLogErrNum(platFormId, nowDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}