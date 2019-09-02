package com.topdraw.nebula_bi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class LogUtil {
    private final static Logger logger = LoggerFactory.getLogger(LogUtil.class);

    @SuppressWarnings("unused")
    public static void printLogWithMethodAndParams(Logger log, String strMethodName, HttpServletRequest request) {
        //StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        //StackTraceElement e = stacktrace[2];
        //String strMethodName = e.getMethodName();
        if (strMethodName.lastIndexOf("Action") != -1) {
            strMethodName = strMethodName.substring(0, strMethodName.lastIndexOf("Action"));
        }

        String strMethod = request.getMethod();

        Map<String, String[]> map = request.getParameterMap();
        String strParams = JSON.toJSONString(map);
        JSONObject jsonObject = JSONObject.parseObject(strParams);
        if (jsonObject.containsKey("_dc")) {
            jsonObject.remove("_dc");
        }
        if (jsonObject.containsKey("view")) {
            jsonObject.remove("view");
        }
        if (jsonObject.containsKey("password")) {
            jsonObject.remove("password");
        }
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String[] strArr = jsonObject.getObject(entry.getKey(), String[].class);
            if (strArr.length == 1) {
                jsonObject.put(entry.getKey(), strArr[0]);
            } else {
                jsonObject.put(entry.getKey(), strArr);
            }
        }
        HttpSession session = request.getSession();
        Map<String, Object> mapMe = (Map<String, Object>) session.getAttribute("me");
        String strAdminName;
        if (mapMe != null) {
            strAdminName = mapMe.get("username").toString();
        } else {
            strAdminName = "unknown";
        }
        log.info(strMethodName + " [" + strMethod + "]: " + jsonObject.toJSONString() + " ------ <" + strAdminName + ">");
    }

    public static void createOperationAudit(
            long lAdminId, Map<String, String[]> mapParam, String strActionName,
            String strActionDesc, IResultInfo<Map<String, Object>> ri) {
        Connection writeConnection = null;
        try {
            writeConnection = DruidUtil.getRandomWriteConnection();
            DruidUtil.beginTransaction(writeConnection);
            JSONObject jsonObjParam = new JSONObject();
            for (Map.Entry<String, String[]> entry : mapParam.entrySet()) {
                jsonObjParam.put(entry.getKey(), entry.getValue().length > 1 ? entry.getValue() : entry.getValue()[0]);
            }

            Map<String, Object> mapSave = new HashMap<>();
            String strParam;
            if (jsonObjParam.toJSONString().length() > 4096) {
                strParam = null;
            } else {
                strParam = jsonObjParam.toJSONString();
            }
            mapSave.put("action_name", strActionName);
            mapSave.put("action_desc", strActionDesc);
            mapSave.put("x_admin_id", lAdminId);
            mapSave.put("response", JSON
                    .toJSONString(new ResultInfo<>(ri.getBusinessCode(), null, ri.getCount(), ri.getDescription())));
            if (strActionName.equals("AdminController.AdminLogin")) {
                mapSave.put("parameters", "");
            } else {
                mapSave.put("parameters", strParam);
            }
            DruidUtil.save(writeConnection, mapSave, "x_operation_audit");

            DruidUtil.commitTransaction(writeConnection);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("fail to createOperationAudit");
        } finally {
            DruidUtil.close(writeConnection);
        }
    }
}
