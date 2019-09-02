package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.PlatformService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class PlatformController {
	private final static Logger logger = LoggerFactory.getLogger(PlatformController.class);

	@RequestMapping("/loadPlatform")
	public IResultInfo<Map<String, Object>> loadPlatform(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		PlatformService platformService = PlatformService.getInstance();
		String adminId = request.getParameter("adminId");
		if(adminId == null){
			ri = new ResultInfo<>("failure", null, "未读取到平台视图");
		}else{
			ri = platformService.fetchPlatform(Long.parseLong(adminId));
		}
		return ri;
	}

}