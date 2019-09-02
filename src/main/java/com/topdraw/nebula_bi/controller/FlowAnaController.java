package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.FlowAnaService;
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
public class FlowAnaController {
	private final static Logger logger = LoggerFactory.getLogger(FlowAnaController.class);

	@RequestMapping("/loadFlowAna")
	@NeedAuthentication(friendlyName = "页面流向", description = "页面流向", servletName = "流量分析")
	@NeedAudit(auditFlag = true, auditDesc = "页面流向")
	public IResultInfo<Map<String, Object>> loadFlowAna(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadFlowAna");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		FlowAnaService flowAnaService = FlowAnaService.getInstance();
		Date sDate;
		try {
			sDate = dateFormat.parse(request.getParameter("queryDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = flowAnaService.getFlowAna(platFormId, sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}