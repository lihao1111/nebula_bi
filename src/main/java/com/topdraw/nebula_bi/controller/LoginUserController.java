package com.topdraw.nebula_bi.controller;

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
public class LoginUserController {
	private final static Logger logger = LoggerFactory.getLogger(LoginUserController.class);

	@RequestMapping("/loadLoginUser")
	@NeedAuthentication(friendlyName = "用户登录", description = "用户登录", servletName = "用户分析")
	@NeedAudit(auditFlag = true, auditDesc = "用户登录")
	public IResultInfo<Map<String, Object>> loadLoginUser(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadLoginUser");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		LoginUserService loginUserService = LoginUserService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = loginUserService.getLoginUser(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}