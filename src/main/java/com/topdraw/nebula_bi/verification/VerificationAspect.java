package com.topdraw.nebula_bi.verification;

import com.topdraw.nebula_bi.service.AdminService;
import com.topdraw.nebula_bi.util.LogUtil;
import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.NeedAudit;
import org.afflatus.infrastructure.common.NeedAuthentication;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.CookiesUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component // 放入spring 容器
public class VerificationAspect {
	private final static Logger logger = LoggerFactory.getLogger(VerificationAspect.class);

	//拦截条件
	@Pointcut("execution(public * com.topdraw.nebula_bi.controller..*(..))")
	public void aop() {
	}

	@Around("aop()")
	public Object authentication(ProceedingJoinPoint pjp) throws Throwable {
		IResultInfo<Map<String, Object>> ri = null;
		HttpServletRequest request;
		//HttpServletResponse response = null;
		//String strViewType = null;

		try {
			/*Class clazz = pjp.getSignature().getDeclaringType();  		// controller注解
			String strMethodName = pjp.getSignature().getName();
			MethodSignature ms = (MethodSignature) pjp.getSignature();
			Method method = ms.getMethod();*/
			String strMethodName = pjp.getSignature().getName();
			Class clazz = pjp.getSignature().getDeclaringType();
			Method method = clazz.getMethod(strMethodName, HttpServletRequest.class, HttpServletResponse.class);
			//Field field = pjp.getSignature().getDeclaringType().getField("logger");
			NeedAuthentication na = method.getAnnotation(NeedAuthentication.class);

			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			request = attributes.getRequest();
			//response = attributes.getResponse();

			String strServletPath = request.getServletPath();
			//strViewType = request.getParameter("view");
			HttpSession session = request.getSession();
			Map<String, Object> mapMe = (Map<String, Object>) session.getAttribute("me");
			Boolean bSessionExists = false;
			if (mapMe == null || mapMe.size() <= 0) {// Session里没有
				logger.info("Fail to get user from session [" + strMethodName + "]");
				Long lUserId = null;
				try {
					String strMe = CookiesUtil.getCookie(request, "me");
					if (strMe == null) {
						logger.info("Fail to get user from cookie [" + strMethodName + "]");
						ri = new ResultInfo<>("login timeout", null, "登录超时");
					} else {
						lUserId = Long.parseLong(strMe);
						IResultInfo<Map<String, Object>> riUser = AdminService.getInstance().fetchAdminById(lUserId);
						if ("success".equals(riUser.getBusinessCode()) && riUser.getCount() > 0) {
							logger.info("Get user from db by cookie info");
							Map<String, Object> mapUser = riUser.getResultSet().get(0);
							mapUser.remove("password");
							mapUser.remove("password_md5");
							session.setAttribute("me", mapUser);
							mapMe = mapUser;
							bSessionExists = true;
						} else {
							logger.error("Fail to get user from db by cookie info");
							ri = new ResultInfo<>("login timeout", null, "登录超时");
						}
					}
				} catch (Exception ex) {
					logger.error("Fail to get user by cookie info", ex);
					ri = new ResultInfo<>("login timeout", null, "登录超时");
				}

			} else {// Session里有
				bSessionExists = true;
			}

			if (na != null) {// 需要判断权限
				if (bSessionExists) {
					AdminService service = AdminService.getInstance();
					long lUserId = (Long) mapMe.get("id");

					StringBuilder sb = new StringBuilder();

					String controllName = clazz.getName().substring(clazz.getName().lastIndexOf(".")+1); //controller name
					sb.append(strServletPath.replace("/", "."));
					sb.insert(0, controllName);

					ri = service.checkAuthenticationCode(lUserId, sb.toString());
					if (ri == null || ri.getCount() <= 0) {
						ri = new ResultInfo<>("unauthenticated", null, "无权限");
					} else if (!ri.getBusinessCode().equals("success")) {
						ri = new ResultInfo<>("unauthenticated", null, "鉴权操作失败");
					} else {
						Map<String, Object> mapTemp = ri.getResultSet().get(0);
						int iAuthCode = (Integer) mapTemp.get("authentication_code");
						if (iAuthCode == 1) { // 有权限的继续
							LogUtil.printLogWithMethodAndParams(logger, strServletPath, request);
							ri = (IResultInfo<Map<String, Object>>) pjp.proceed();
						} else {
							ri = new ResultInfo<>("unauthenticated", null, "无权限");
						}
					}
				}
			} else {
				// 无需验证权限的继续
				if (bSessionExists || strMethodName.equals("AdminList") || strMethodName.equals("AdminLogin")) {
					LogUtil.printLogWithMethodAndParams(logger, strServletPath, request);
					ri = (IResultInfo<Map<String, Object>>) pjp.proceed();
				}
			}

			NeedAudit na4Audit = method.getAnnotation(NeedAudit.class);

			if (na4Audit != null && na4Audit.auditFlag()) {
				if (bSessionExists) {
					long lAdminId = (Long) mapMe.get("id");
					Map<String, String[]> mapParam = request.getParameterMap();
					String strControllerName = clazz.getName();
					LogUtil.createOperationAudit(lAdminId, mapParam,
							strControllerName.substring(strControllerName.lastIndexOf(".") + 1) + "."
									+ strMethodName, na4Audit.auditDesc(), ri);
				}
			}
		} catch (Exception ex) {
			logger.error("authentication failed", ex);
			ri = new ResultInfo<>("unauthenticated", null, "无权限");
		} finally {

		}

		return ri;
	}
}