package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.SubjectAnaService;
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
public class SubjectAnaController {
	private final static Logger logger = LoggerFactory.getLogger(SubjectAnaController.class);

	@RequestMapping("/loadSubjectList")
	@NeedAuthentication(friendlyName = "专题流量", description = "专题流量", servletName = "流量分析")
	@NeedAudit(auditFlag = true, auditDesc = "专题流量")
	public IResultInfo<Map<String, Object>> loadSubjectList(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadSubjectList");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		SubjectAnaService subjectAnaService = SubjectAnaService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			String chooseType = request.getParameter("chooseType");
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = subjectAnaService.getSubjectList(platFormId, sDate, eDate, chooseType);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}