package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.*;
import org.afflatus.utility.CollectionUtil;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.MD5Util;
import org.afflatus.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;

public class AdminService {
	private final static Logger logger = LoggerFactory.getLogger(AdminService.class);

	public static AdminService getInstance() {
		return _instance;
	}
    private static AdminService _instance = new AdminService();
	private AdminService() { logger.info("Initial AdminService"); }

    public IResultInfo<Map<String, Object>> adminLogin(String strUsername, String strPassword) {
        IResultInfo<Map<String, Object>> ri;
        Connection readConnection = null;

        try {
            readConnection = DruidUtil.getRandomReadConnection();

			Map<String, Object> mapAdmin = DruidUtil.queryUniqueResult(readConnection,
					"SELECT id, username, realname, password_md5, password, group_id, image, gender, type" +
							" FROM x_admin a WHERE a.username = ?", strUsername);

			if (null == mapAdmin || mapAdmin.isEmpty()) { // 没记录
				ri = new ResultInfo<>("failure", null, "用户名或密码错误");
			} else {

				if (mapAdmin.get("password_md5").toString().equals(MD5Util.encodePassword(strPassword))) {	//后台MD5
					List<Map<String, Object>> listAdmin = new ArrayList<>();
					mapAdmin.remove("password_md5");
					listAdmin.add(mapAdmin);

					ri = new ResultInfo<>("success", listAdmin, 1, "");
				} else {
					ri = new ResultInfo<>("failure", null, "用户名或密码错误");
				}
			}

        } catch (Exception ex) {
            ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("登录错误", ex);
        } finally {
            DruidUtil.close(readConnection);
        }
        return ri;
    }

	public IResultInfo<Map<String, Object>> editUserInfo(Map<String, Object> editMap) {
		IResultInfo<Map<String, Object>> ri;
		Connection writeConnection = null;
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();
			writeConnection = DruidUtil.getRandomWriteConnection();

			DruidUtil.beginTransaction(writeConnection);
			DruidUtil.update(writeConnection, editMap, "x_admin", "id");
			DruidUtil.commitTransaction(writeConnection);

			Map<String, Object> newUser = DruidUtil.queryUniqueResult(readConnection,
					"SELECT * FROM x_admin a WHERE a.id = ?", editMap.get("id"));

			List<Map<String, Object>> listAdmin = new ArrayList<>();
			listAdmin.add(newUser);

			ri = new ResultInfo<>("success", listAdmin, 1, "成功更新操作人员");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());
			logger.error("更新操作人员异常:", ex);
		} finally {
			DruidUtil.close(writeConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> fetchAdminById(long lAdminId) {
		IResultInfo<Map<String, Object>> ri;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			Map<String, Object> mapAdmin = DruidUtil.queryUniqueResult(readConnection,
					"SELECT * FROM x_admin WHERE id = ?", lAdminId);

			List<Map<String, Object>> listResult = new ArrayList<>();
			listResult.add(mapAdmin);

			ri = new ResultInfo<>("success", listResult, listResult.size(), "");

		} catch (Exception ex) {
			ri = new ResultInfo<>("failure", null, ex.getMessage());

		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	public IResultInfo<Map<String, Object>> checkAuthenticationCode(long lAdminId, String strAdminAction) {
		Connection readConnection = null;
		IResultInfo<Map<String, Object>> ri;

		try {

			readConnection = DruidUtil.getRandomReadConnection();

			Map<String, Object> mapAdminBackofficeFeature = DruidUtil.queryUniqueResult(readConnection
					, "SELECT * FROM x_admin__backoffice_feature WHERE admin_id = ? AND backoffice_feature_name = ?",
					lAdminId, strAdminAction);

			Map<String, Object> mapResult = new HashMap<>();
			List<Map<String, Object>> listResult = new ArrayList<>();

			if (mapAdminBackofficeFeature != null && mapAdminBackofficeFeature.size() > 0) {
				mapResult.put("authentication_code", mapAdminBackofficeFeature.get("authentication_code"));
				listResult.add(mapResult);
				ri = new ResultInfo<>("success", listResult, listResult.size(), "");

			} else {
				ri = new ResultInfo<>("success", null, "No relative record");
			}

		} catch (Exception ex) {

			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

	// 根据代码更新Feature 为每个管理员用户填满关系表记录
	public IResultInfo<Map<String, Object>> buildAuthentication(String strRealContextPath, String strPackageAndClass) {

		Connection readConnection = null;
		Connection writeConnection = null;
		IResultInfo<Map<String, Object>> ri;

		try {
			readConnection = DruidUtil.getRandomReadConnection();
			writeConnection = DruidUtil.getRandomWriteConnection();

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			/*String strControllerPath = (strRealContextPath + "WEB-INF/classes/" + strPackageAndClass).replace("\\", "/");
			String strClassPath = (strRealContextPath + "WEB-INF/classes/").replace("\\", "/");
			*/
			String strControllerPath = (strRealContextPath + strPackageAndClass).replace("\\", "/");
			String strClassPath = (strRealContextPath).replace("\\", "/");

			logger.info("===更新权限体系 开始===");
			logger.info("准备目录和名字空间:" + strControllerPath);

			File f = new File(strControllerPath);

			// 准备所有类
			Set<String> setClassString = findServletClass(f, strClassPath);

			// 准备所用的用户信息
			List<Map<String, Object>> listAdmins = DruidUtil.queryList(readConnection, "SELECT * FROM x_admin");

			// 准备数据库中的Feature
			List<Map<String, Object>> listFeatures = DruidUtil.queryList(readConnection,
					"SELECT * FROM x_backoffice_feature");

			DruidUtil.beginTransaction(writeConnection);

			// 清理代码里已经删除的功能 数据库每个记录
			logger.info("清理代码里已删除的管理功能");
			for (Map<String, Object> mapFeature : listFeatures) {
				String strFeatureName1 = (String) mapFeature.get("name");
				String[] straFeatureNameParts = strFeatureName1.split("\\.");

				String strClassName1 = strPackageAndClass.replace("/", ".") + straFeatureNameParts[0];

				NeedAuthentication na = null;
				try {
					Class<?> theFeatureClass = classLoader.loadClass(strClassName1);
					Method method1 = theFeatureClass.getMethod(straFeatureNameParts[1],
							HttpServletRequest.class, HttpServletResponse.class);
					na = method1.getAnnotation(NeedAuthentication.class);

				} catch (ClassNotFoundException ex) {
					logger.info(strClassName1 + " class not found");
				} catch (NoSuchMethodException ex2) {
					logger.info(straFeatureNameParts[1] + " no such method");
				}

				logger.info("检查:" + straFeatureNameParts[1]);


				if (na == null) {
					logger.info("删除:" + straFeatureNameParts[1]);
					DruidUtil.doExecute(writeConnection, "DELETE FROM x_backoffice_feature WHERE `name` = ?",
							strFeatureName1);
					DruidUtil.doExecute(writeConnection,
							"DELETE FROM x_admin__backoffice_feature WHERE backoffice_feature_name = ?",
							strFeatureName1);
				}
			}

			// 检查新增和更新的功能
			for (String strClassName : setClassString) {
				Class<?> theClass = classLoader.loadClass(strClassName);
				Method[] aMethods = theClass.getMethods();

				for (Method method : aMethods) {
					NeedAuthentication na = method.getAnnotation(NeedAuthentication.class);

					if (na != null) {
						String strAbbrClassName = strClassName.substring(strClassName.lastIndexOf(".") + 1);
						String strAbbrMethodName = method.getName();
						String strFeatureName = strAbbrClassName.concat(".").concat(strAbbrMethodName);

						// 插入管理功能
						Map<String, Object> mapBackOfficeFeature = DruidUtil.queryUniqueResult(readConnection,
								"SELECT * FROM x_backoffice_feature WHERE name = ?", strFeatureName);
						if (CollectionUtil.isEmpty(mapBackOfficeFeature)) {
							logger.info("插入管理功能" + strFeatureName);
							mapBackOfficeFeature = new HashMap<>();
							mapBackOfficeFeature.put("name", strFeatureName);
							mapBackOfficeFeature.put("friendly_name", na.friendlyName());
							mapBackOfficeFeature.put("description", na.description());
							mapBackOfficeFeature.put("servlet_name", na.servletName());
							NeedAudit needAudit = method.getAnnotation(NeedAudit.class);
							if (needAudit != null) {
								mapBackOfficeFeature.put("audit_desc", needAudit.auditDesc());
							}
							DruidUtil.save(writeConnection, mapBackOfficeFeature, "x_backoffice_feature");
						} else {
							NeedAudit needAudit = method.getAnnotation(NeedAudit.class);

							if (mapBackOfficeFeature.get("friendly_name").equals(na.friendlyName())
									&& mapBackOfficeFeature.get("description").equals(na.description())
									&& mapBackOfficeFeature.get("servlet_name").equals(na.servletName())) {
								if ((needAudit != null && mapBackOfficeFeature.get("audit_desc") == null)
										|| (needAudit == null && mapBackOfficeFeature.get("audit_desc") != null)
										|| (needAudit != null && !mapBackOfficeFeature.get("audit_desc")
										.equals(needAudit.auditDesc()))) {
									mapBackOfficeFeature.put("audit_desc",
											needAudit == null ? null : needAudit.auditDesc());
									DruidUtil.update(writeConnection, mapBackOfficeFeature, "x_backoffice_feature",
											"id");
									logger.info("更新已存在的管理功能(操作审计描述)" + strFeatureName);
								} else {
									logger.info("已存在管理功能" + strFeatureName);
								}
							} else {
								mapBackOfficeFeature.put("friendly_name", na.friendlyName());
								mapBackOfficeFeature.put("description", na.description());
								mapBackOfficeFeature.put("servlet_name", na.servletName());
								mapBackOfficeFeature.put("audit_desc",
										needAudit == null ? null : needAudit.auditDesc());
								DruidUtil.update(writeConnection, mapBackOfficeFeature, "x_backoffice_feature", "id");
								logger.info("更新已存在的管理功能" + strFeatureName);
							}

						}

						// 检查每个用户的情况，在关系表中给他们都增加需要鉴权的函数名，
						if (!CollectionUtil.isEmpty(listAdmins)) {
							logger.info(strClassName + "检查函数" + method.getName() + "对应的用户");

							for (Map<String, Object> mapAdmin : listAdmins) {
								Map<String, Object> mapAdminBackOfficeFeature = DruidUtil.queryUniqueResult(
										readConnection, "SELECT * FROM x_admin__backoffice_feature"
												+ " WHERE backoffice_feature_name = ? AND admin_id = ?",
										strFeatureName, mapAdmin.get("id"));

								if (CollectionUtil.isEmpty(mapAdminBackOfficeFeature)) {
									logger.info("用户" + mapAdmin.get("realname") + "绑定后台管理功能 " + strFeatureName);
									mapAdminBackOfficeFeature = new HashMap<>();
									mapAdminBackOfficeFeature.put("backoffice_feature_name", strFeatureName);
									mapAdminBackOfficeFeature.put("admin_id", mapAdmin.get("id"));
									mapAdminBackOfficeFeature.put("authentication_code",
											(Integer) mapAdmin.get("type") == 10 ? 1 : 0);
									DruidUtil.save(writeConnection, mapAdminBackOfficeFeature,
											"x_admin__backoffice_feature");
								} else {
									logger.info("用户" + mapAdmin.get("realname") + "已经存在后台管理功能 " + strFeatureName);
								}

							} // for each admin
						} // if has admin

					} // if NeedAuthentication

				} // for each method

			}

			DruidUtil.commitTransaction(writeConnection);

			ri = new ResultInfo<>("success", null, "更新权限体系顺利完成");

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("从代码更新Feature错误", ex);
			ri = new ResultInfo<>("failure", null, ex.getMessage());
		} finally {
			DruidUtil.close(readConnection);
			DruidUtil.close(writeConnection);
			logger.info("===更新权限体系 结束====");
		}
		return ri;
	}

	private static Set<String> findServletClass(File fileClass, String strDirectory) throws ClassNotFoundException {
		Set<String> setClassName = new HashSet<>();
		if (fileClass.isDirectory()) {
			File[] filelist = fileClass.listFiles();
			for (File ff : filelist) {
				setClassName.addAll(findServletClass(ff, strDirectory));
			}
			return setClassName;
		} else {
			String strPackageAndClass = fileClass.getPath().replace('\\', '/');
			String formaterStrDirectory = strDirectory.substring(1);
			String strClassName = strPackageAndClass.replace(formaterStrDirectory, "").replace('/', '.');
			if (strClassName.endsWith("Controller.class")) {
				strClassName = strClassName.trim().substring(0, strClassName.length() - 6); // .class
				System.out.println(strClassName);
				setClassName.add(strClassName);
			}
			return setClassName;
		}
	}
}
