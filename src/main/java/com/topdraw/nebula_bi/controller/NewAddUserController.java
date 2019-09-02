package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.NewAddUserService;
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
public class NewAddUserController {
	private final static Logger logger = LoggerFactory.getLogger(NewAddUserController.class);

	@RequestMapping("/loadNewAddUser")
	@NeedAuthentication(friendlyName = "用户新增留存", description = "用户新增留存", servletName = "用户分析")
	@NeedAudit(auditFlag = true, auditDesc = "用户新增留存")
	public IResultInfo<Map<String, Object>> loadNewAddUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadNewAddUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		NewAddUserService newAddUserService = NewAddUserService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = newAddUserService.getNewAddUser(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}


	@RequestMapping("/loadWeekUser")
	public IResultInfo<Map<String, Object>> loadWeekUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadWeekUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		NewAddUserService newAddUserService = NewAddUserService.getInstance();
		Date nowDate;
		try {
			nowDate = dateFormat.parse(request.getParameter("nowDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = newAddUserService.getWeekUser(platFormId, nowDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}
}