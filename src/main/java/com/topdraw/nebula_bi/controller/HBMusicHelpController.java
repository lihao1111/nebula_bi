package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.HBMusicHelpService;
import com.topdraw.nebula_bi.service.SubjectAnaService;
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
public class HBMusicHelpController {
	private final static Logger logger = LoggerFactory.getLogger(HBMusicHelpController.class);

	@RequestMapping("/loadPromotionItemDtl")
	/*@NeedAuthentication(friendlyName = "专题流量", description = "专题流量", servletName = "流量分析")
	@NeedAudit(auditFlag = true, auditDesc = "专题流量")*/
	public IResultInfo<Map<String, Object>> loadPromotionItemDtl(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPromotionItemDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = hbMusicHelpService.getPromotionList(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}


	@RequestMapping("/exportPromotionItemDtl")
	/*@NeedAuthentication(friendlyName = "专题流量", description = "专题流量", servletName = "流量分析")
	@NeedAudit(auditFlag = true, auditDesc = "专题流量")*/
	public IResultInfo<Map<String, Object>> exportPromotionItemDtl(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("exportPromotionItemDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = hbMusicHelpService.exportPromotionList(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}
}