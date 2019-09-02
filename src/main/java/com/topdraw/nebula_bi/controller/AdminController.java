package com.topdraw.nebula_bi.controller;

import com.topdraw.nebula_bi.service.AdminService;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.MD5Util;
import org.afflatus.utility.PropertiesUtil;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AdminController {
	private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
	private static final int I_MAX_LOGIN_FAILURE_TIMES = 5;
	private static final int I_LOGIN_FAILURE_WAIT_TIME = 2 * 60 * 1000;

	@RequestMapping("/adminLogin")
	@NeedAudit(auditFlag = true, auditDesc = "用户登录")
	public IResultInfo<Map<String, Object>> AdminLogin(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		AdminService service = AdminService.getInstance();

		String strUsername = request.getParameter("username");
		String strPassword = request.getParameter("password");
		String strVerificationCode = request.getParameter("verificationCode");

		Object objFailureTimes = request.getSession().getAttribute("iFailureTimes");
		int iFailureTimes = 0;
		if (objFailureTimes == null) {
			request.getSession().setAttribute("iFailureTimes", iFailureTimes);
		} else {
			iFailureTimes = Integer.parseInt(objFailureTimes.toString());
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strLastLoginTime = (String) request.getSession().getAttribute("strLastLoginTime");
		Date dateLastLoginTime = null;
		if (strLastLoginTime != null) {
			try {
				dateLastLoginTime = sdf.parse(strLastLoginTime);
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Fail to parse date", ex);
			}
		}

		Date date = new Date();
		if (dateLastLoginTime != null && date.getTime() - dateLastLoginTime.getTime() > I_LOGIN_FAILURE_WAIT_TIME) {
			request.getSession().removeAttribute("strLastLoginTime");
			dateLastLoginTime = null;
			request.getSession().setAttribute("iFailureTimes", 0);
			iFailureTimes = 0;
		}

		if ((Boolean.parseBoolean(PropertiesUtil.get("VerificationCodeEnable")))) {
			String strCode = (String) request.getSession().getAttribute("code");
			if (strCode == null) {
				ri = new ResultInfo<>("failure", null, "获取验证码失败，请点击重新获取");
				return ri;
			}
			if (!strCode.toLowerCase().equals(strVerificationCode.toLowerCase())) {
				ri = new ResultInfo<>("failure", null, "验证码错误");
				return ri;
			}
		}

		if (!StringUtil.hasText(strUsername) || !StringUtil.hasText(strPassword)) {
			ri = new ResultInfo<>("failure", null, "用户名和密码不能为空");
			return ri;
		}

		if (iFailureTimes == I_MAX_LOGIN_FAILURE_TIMES) {
			ri = new ResultInfo<>("failure", null, "连续登陆失败" + iFailureTimes + "次，请在" +
					I_LOGIN_FAILURE_WAIT_TIME / 60 / 1000 + "分钟后再重试");
			return ri;
		}

		ri = service.adminLogin(strUsername, strPassword);

		if (ri.getBusinessCode().equals("failure")) {
			if (dateLastLoginTime == null) {
				strLastLoginTime = sdf.format(date);
				request.getSession().setAttribute("strLastLoginTime", strLastLoginTime);
				iFailureTimes = 1;
				request.getSession().setAttribute("iFailureTimes", iFailureTimes);
				logger.info("last login time: " + strLastLoginTime + " , error times: " + iFailureTimes);
			} else {
				iFailureTimes++;
				request.getSession().setAttribute("iFailureTimes", iFailureTimes);
				logger.info("last login time: " + strLastLoginTime + " , error times: " + iFailureTimes);

			}
		} else {
			request.getSession().removeAttribute("strLastLoginTime");
			request.getSession().setAttribute("iFailureTimes", 0);
		}

		HttpSession session;
		Cookie cookie;

		// 将管理员信息保存到Session 和Cookie
		if ("success".equals(ri.getBusinessCode())) {

			session = request.getSession();
			logger.info("Login SessionId:" + session.getId());

			session.setAttribute("me", ri.getResultSet().get(0));

			cookie = new Cookie("me", ri.getResultSet().get(0).get("id").toString());
			cookie.setPath("/");
			cookie.setMaxAge(24 * 60 * 60);// 1天
			response.addCookie(cookie);
		}
		return ri;
	}


	@RequestMapping("/editUserInfo")
	public IResultInfo<Map<String, Object>> editUserInfo(HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;
		AdminService userService = AdminService.getInstance();

		Map<String, Object> mapUpdate = new HashMap<>();
		mapUpdate.put("id", request.getParameter("id"));
		mapUpdate.put("realname", request.getParameter("realname"));
		mapUpdate.put("username", request.getParameter("username"));
		mapUpdate.put("password", request.getParameter("password"));
		mapUpdate.put("password_md5", MD5Util.encodePassword(mapUpdate.get("password").toString()));
		mapUpdate.put("group_id", Integer.parseInt(request.getParameter("group_id")));
		mapUpdate.put("gender", Integer.parseInt(request.getParameter("gender")));
		ri = userService.editUserInfo(mapUpdate);
		return ri;
	}

	@RequestMapping("/BuildAuthentication")
	@NeedAuthentication(friendlyName = "更新权限体系", description = "更新权限体系", servletName = "系统管理")
	@NeedAudit(auditFlag = true, auditDesc = "更新权限体系")
	public IResultInfo<Map<String, Object>> BuildAuthentication(
			HttpServletRequest request, HttpServletResponse response) {
		IResultInfo<Map<String, Object>> ri;

		AdminService service = AdminService.getInstance();
		/*String strRealContextPath = request.getServletContext().getRealPath("/");*/
		String strRealContextPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();	//项目绝对路径
		String strClassPath = this.getClass().getName();

		if (!strRealContextPath.endsWith("\\") && !strRealContextPath.endsWith("/")) {
			// 兼容Tomcat7、8 ContextPath结尾不同
			strRealContextPath += "\\";
		}

		String strPackageAndClass = strClassPath.substring(0, strClassPath.lastIndexOf(".")).replace(".", "/") + "/";

		ri = service.buildAuthentication(strRealContextPath, strPackageAndClass);
		return ri;
	}
}