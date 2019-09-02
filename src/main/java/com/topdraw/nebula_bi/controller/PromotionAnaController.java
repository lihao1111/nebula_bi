package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.PromotionAnaService;
import com.topdraw.nebula_bi.service.SubjectAnaService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.afflatus.utility.StringUtil;
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
public class PromotionAnaController {
	private final static Logger logger = LoggerFactory.getLogger(PromotionAnaController.class);

	@RequestMapping("/loadPages")
	public IResultInfo<Map<String, Object>> loadPages(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPages");

		PromotionAnaService promotionAnaService = PromotionAnaService.getInstance();

		try {
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = promotionAnaService.getPages(platFormId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadPromotionList")
	public IResultInfo<Map<String, Object>> loadPromotionList(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPromotionList");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		PromotionAnaService promotionAnaService = PromotionAnaService.getInstance();
		Date queryDate;
		try {
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String pageIdStr = request.getParameter("pageId");
			Integer pageId = 0;
			if(StringUtil.hasText(pageIdStr)){
				pageId = Integer.parseInt(pageIdStr);
			}
			queryDate = dateFormat.parse(request.getParameter("queryDate"));

			ri = promotionAnaService.getPromotionList(platFormId, pageId, queryDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}
}