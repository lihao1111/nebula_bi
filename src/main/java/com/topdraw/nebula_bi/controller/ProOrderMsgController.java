package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.ProOrderMsgService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProOrderMsgController {
	private final static Logger logger = LoggerFactory.getLogger(ProOrderMsgController.class);

	@RequestMapping("/loadProOrder")
	public IResultInfo<Map<String, Object>> loadProOrder(String queryVal, Integer platFormId) {
		IResultInfo<Map<String, Object>> ri;

		ProOrderMsgService proOrderMsgService = ProOrderMsgService.getInstance();
		ri = proOrderMsgService.getProOrder(platFormId, queryVal);
		return ri;
	}

}