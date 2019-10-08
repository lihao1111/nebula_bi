package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.HBMusicHelpService;
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
public class HBMusicHelpController {
	private final static Logger logger = LoggerFactory.getLogger(HBMusicHelpController.class);

	@RequestMapping("/loadPromotionItemDtl")
	@NeedAuthentication(friendlyName = "推荐位报表", description = "推荐位报表", servletName = "河北数据支撑")
	@NeedAudit(auditFlag = true, auditDesc = "推荐位报表")
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
	public void exportPromotionItemDtl(HttpServletRequest request, HttpServletResponse response) {
		logger.info("exportPromotionItemDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			hbMusicHelpService.exportPromotionList(response, platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}



	@RequestMapping("/loadDayDataDtl")
	@NeedAuthentication(friendlyName = "日报表数据", description = "日报表数据", servletName = "河北数据支撑")
	public IResultInfo<Map<String, Object>> loadDayDataDtl(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayDataDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = hbMusicHelpService.getDayDataDtl(platFormId, sDate, eDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/exportDayDataDtl")
	public void exportDayDataDtl(HttpServletRequest request, HttpServletResponse response) {
		logger.info("exportDayDataDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			hbMusicHelpService.exportDayDataList(response, platFormId, sDate, eDate);


		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/loadCpInfoDtl")
	@NeedAuthentication(friendlyName = "CP基础数据", description = "CP基础数据", servletName = "河北数据支撑")
	public IResultInfo<Map<String, Object>> loadCpInfoDtl(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadCpInfoDtl");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));

			ri = hbMusicHelpService.getCpInfoDtl(sDate, eDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/exportCpInfo")
	public void exportCpInfo(HttpServletRequest request, HttpServletResponse response) {
		logger.info("exportCpInfo");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));

			hbMusicHelpService.exportCpInfoList(response, sDate, eDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/loadCpTop")
	@NeedAuthentication(friendlyName = "CP播放排行", description = "CP播放排行", servletName = "河北数据支撑")
	public IResultInfo<Map<String, Object>> loadCpTop(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadCpTop");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		HBMusicHelpService hbMusicHelpService = HBMusicHelpService.getInstance();
		Date sDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));

			ri = hbMusicHelpService.getCpTopList(sDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

}