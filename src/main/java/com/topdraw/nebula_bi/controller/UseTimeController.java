package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.HBMusicHelpService;
import com.topdraw.nebula_bi.service.UseTimeService;
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
public class UseTimeController {
	private final static Logger logger = LoggerFactory.getLogger(UseTimeController.class);

	@RequestMapping("/loadUseTime")
	@NeedAuthentication(friendlyName = "用户使用时长", description = "用户使用时长", servletName = "用户分析")
	@NeedAudit(auditFlag = true, auditDesc = "用户使用时长")
	public IResultInfo<Map<String, Object>> loadUseTime(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadUseTime");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String chooseType = request.getParameter("chooseType");

			ri = useTimeService.fetchUseTime(platFormId, sDate, eDate, chooseType);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadPlayTime")
	@NeedAuthentication(friendlyName = "用户播放时长", description = "用户播放时长", servletName = "用户分析")
	@NeedAudit(auditFlag = true, auditDesc = "用户播放时长")
	public IResultInfo<Map<String, Object>> loadPlayTime(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPlayTime");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String chooseType = request.getParameter("chooseType");

			ri = useTimeService.fetchPlayTime(platFormId, sDate, eDate, chooseType);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadPlayCount")
	@NeedAuthentication(friendlyName = "有效播放次数统计", description = "有效播放次数统计", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "有效播放次数统计")
	public IResultInfo<Map<String, Object>> loadPlayCount(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadPlayCount");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String contentType = request.getParameter("contentType");
			String contentCP = request.getParameter("contentCP");
			String contentKey = request.getParameter("contentKey");

			ri = useTimeService.fetchPlayCount(platFormId, sDate, eDate, contentType, contentCP, contentKey);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadCPs")
	public IResultInfo<Map<String, Object>> loadCPs(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadCPs");

		UseTimeService useTimeService = UseTimeService.getInstance();
		try {
			ri = useTimeService.loadCPs();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadMediaPer")
	@NeedAuthentication(friendlyName = "播放完整度统计", description = "播放完整度统计", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "播放完整度统计")
	public IResultInfo<Map<String, Object>> loadMediaPer(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadMediaPer");

		UseTimeService useTimeService = UseTimeService.getInstance();
		try {
			String day = request.getParameter("day");
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String mediaId = request.getParameter("mediaId");

			ri = useTimeService.loadMediaPer(platFormId, day, mediaId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}


	@RequestMapping("/loadDayUseTime")
	public IResultInfo<Map<String, Object>> loadDayUseTime(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadDayUseTime");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		UseTimeService useTimeService = UseTimeService.getInstance();
		try {
			Date day = dateFormat.parse(request.getParameter("nowDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = useTimeService.loadDayUseTime(platFormId, day);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadAllPlayCount")
	/*@NeedAuthentication(friendlyName = "播放次数统计", description = "播放次数统计", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "播放次数统计")*/
	public IResultInfo<Map<String, Object>> loadAllPlayCount(HttpServletRequest request, HttpServletResponse responseString) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadAllPlayCount");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			String contentType = request.getParameter("contentType");
			String contentCP = request.getParameter("contentCP");
			String contentKey = request.getParameter("contentKey");

			ri = useTimeService.fetchAllPlayCount(platFormId, sDate, eDate,
					contentType, contentCP, contentKey);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/exportAllPlayCount")
	public void exportAllPlayCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("exportAllPlayCount");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String contentType = request.getParameter("contentType");
			String contentCP = request.getParameter("contentCP");
			String contentKey = request.getParameter("contentKey");

			useTimeService.exportAllPlayCount(response, platFormId, sDate, eDate,
					contentType, contentCP, contentKey);


		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/loadMediaExport")
	public void loadMediaExport(HttpServletRequest request, HttpServletResponse response) {
		logger.info("loadMediaExport");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		UseTimeService useTimeService = UseTimeService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String contentType = request.getParameter("contentType");
			String contentCP = request.getParameter("contentCP");
			String contentKey = request.getParameter("contentKey");

			useTimeService.loadMediaExport(response, platFormId, sDate, eDate,
					contentType, contentCP, contentKey);

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


}
