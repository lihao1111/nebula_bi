package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.ContentProviderService;
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
public class ContentProviderController {
	private final static Logger logger = LoggerFactory.getLogger(ContentProviderController.class);

	@RequestMapping("/loadContentCPs")
	@NeedAuthentication(friendlyName = "内容提供商排行", description = "内容提供商排行", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "内容提供商排行")
	public IResultInfo<Map<String, Object>> loadContentCPs(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadContentCPs");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ContentProviderService contentProviderService = ContentProviderService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String contentCP = request.getParameter("contentCP");
			String contentType = request.getParameter("contentType");

			ri = contentProviderService.getContentCPs(platFormId, sDate, eDate,
					contentCP, contentType);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadSearchDetail")
	@NeedAuthentication(friendlyName = "搜索关键字排行", description = "搜索关键字排行", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "搜索关键字排行")
	public IResultInfo<Map<String, Object>> loadSearchDetail(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadSearchDetail");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ContentProviderService contentProviderService = ContentProviderService.getInstance();
		Date sDate;
		Date eDate;
		try {
			sDate = dateFormat.parse(request.getParameter("startDate"));
			eDate = dateFormat.parse(request.getParameter("endDate"));
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));

			ri = contentProviderService.getSearchDetail(platFormId, sDate, eDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ri;
	}

	@RequestMapping("/loadCollectDetail")
	@NeedAuthentication(friendlyName = "收藏排行", description = "收藏排行", servletName = "内容分析")
	@NeedAudit(auditFlag = true, auditDesc = "收藏排行")
	public IResultInfo<Map<String, Object>> loadCollectDetail(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri = null;
		logger.info("loadCollectDetail");

		ContentProviderService contentProviderService = ContentProviderService.getInstance();
		try {
			Integer platFormId = Integer.parseInt(request.getParameter("platFormId"));
			String contentCP = request.getParameter("contentCP");
			String contentType = request.getParameter("contentType");
			String contentKey = request.getParameter("contentKey");
			Integer curPage = Integer.parseInt(request.getParameter("currentPage"));
			Integer pageSize = Integer.parseInt(request.getParameter("pageSize"));

			ri = contentProviderService.getCollectDetail(platFormId, curPage, pageSize, contentCP, contentType, contentKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ri;
	}

}