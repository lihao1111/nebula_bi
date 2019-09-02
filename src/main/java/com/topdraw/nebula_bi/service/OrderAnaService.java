package com.topdraw.nebula_bi.service;

import org.afflatus.infrastructure.common.IResultInfo;
import org.afflatus.infrastructure.common.ResultInfo;
import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderAnaService {
	private final static Logger logger = LoggerFactory.getLogger(OrderAnaService.class);

	public static OrderAnaService getInstance() {
		return _instance;
	}
    private static OrderAnaService _instance = new OrderAnaService();
	private OrderAnaService() { logger.info("Initial OrderAnaService"); }

	public IResultInfo<Map<String, Object>> getOrderDetail(Integer lPlatform, Date startDate, Date endDate, String chooseType) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			List<Map<String, Object>> retlistProducts = new ArrayList<Map<String, Object>>();		//返回list

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			//统计日期
			/*Date dateEnd = DateUtil.getDateBeforeOrAfter(endDate, 1);
			List<Date> listDays = DateUtil.days(startDate, dateEnd);
			//订购产品
			String querySql = "SELECT product_id, product_name FROM x_order_product_xx WHERE platform_id = ?";
			List<Map<String, Object>> listProducts = DruidUtil.queryList(readConnection, querySql, lPlatform);*/

			if("day".equals(chooseType)){	//日统计
				/*retlistProducts = this.getDatasForDay(readConnection, listDays, listProducts, lPlatform);*/

				String querySql = "SELECT * FROM  x_order_product_xx p left join bi_gscmcc_children_proordered_copy1 bp ON p.product_id = bp.productId " +
						" WHERE bp.platform_id = ? AND bp.day >= ? AND bp.day <= ?";

				retlistProducts = DruidUtil.queryList(readConnection, querySql, lPlatform, dateFormat.format(startDate), dateFormat.format(endDate));

			}else if("week".equals(chooseType)){	//周统计
			}else if("month".equals(chooseType)){	//月统计

			}

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retlistProducts, retlistProducts.size(), "");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getNewOrderStatistic error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}




	/**
	 * 老版本数据格式
	 * @param readConnection
	 * @param listDays
	 * @param listProducts
	 * @param lPlatform
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getDatasForDay(Connection readConnection, List<Date> listDays,
													List<Map<String, Object>> listProducts, Integer lPlatform) throws Exception {

		List<Map<String, Object>> retlistProducts = new ArrayList<Map<String, Object>>();		//返回list

		DecimalFormat df = new DecimalFormat("0.000");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//获取每日的uv newadd
		String tabName_newUser = "bi_gscmcc_children_newuser";
		String tabName_newOrder = "bi_gscmcc_children_proordered";

		Map<String, Object> UVMap = new HashMap<String, Object>();		//存放map
		for (Date day : listDays) {//时间循环 n天
			String uvSql = "SELECT * FROM "+tabName_newUser+" WHERE day = ? AND platform_id = ?";
			Map<String, Object> uvMap = DruidUtil.queryUniqueResult(readConnection, uvSql, dateFormat.format(day), lPlatform);

			int targetUV = 0;		//总用户
			int targetNewUV = 0;	//新用户
			int targetOldUV = 0;	//老用户

			if(uvMap != null){
				targetUV = Integer.parseInt(uvMap.get("uv").toString());
				targetNewUV = Integer.parseInt(uvMap.get("newAddNum").toString());
				targetOldUV = targetUV - targetNewUV;
			}
			String dayStr = DateUtil.formatDate(day, "");
			Map<String, Integer> UVDetailMap = new HashMap<String, Integer>();
			UVDetailMap.put("targetUV", targetUV);
			UVDetailMap.put("targetOldUV", targetOldUV);
			UVDetailMap.put("targetNewUV", targetNewUV);
			UVMap.put(dayStr, UVDetailMap);
		}
		//分产品统计
		for(Map<String, Object> mapProduct : listProducts){
			String productId = mapProduct.get("product_id").toString();

			//新老用户订购率 产品_订购详情
			Map<String, Object> allOrder = new HashMap<String, Object>();
			allOrder.put("display_name",productId+"_总订购");
			retlistProducts.add(allOrder);

			Map<String, Object> newOrder = new HashMap<String, Object>();
			newOrder.put("display_name",productId+"_新用户订购");
			retlistProducts.add(newOrder);

			Map<String, Object> oldOrder = new HashMap<String, Object>();
			oldOrder.put("display_name", productId+"_老用户订购");
			retlistProducts.add(oldOrder);

			Map<String, Object> allOrderPrec = new HashMap<String, Object>();
			allOrderPrec.put("display_name", productId+"_总用户订购率");
			retlistProducts.add(allOrderPrec);

			Map<String, Object> newOrderPrec = new HashMap<String, Object>();
			newOrderPrec.put("display_name", productId+"_新用户订购率");
			retlistProducts.add(newOrderPrec);

			Map<String, Object> oldOrderPrec = new HashMap<String, Object>();
			oldOrderPrec.put("display_name", productId+"_老用户订购率");
			retlistProducts.add(oldOrderPrec);

			for (Date day : listDays){		//循环时间
				String sqlDate = "SELECT * FROM "+tabName_newOrder+" WHERE day = ? and productId = ? AND platform_id = ?";
				Map<String, Object> retMap = DruidUtil.queryUniqueResult(readConnection, sqlDate, dateFormat.format(day), productId, lPlatform);

				int allOrderNum = 0;
				int newOrderNum = 0;
				int oldOrdernum = 0;
				float allOrderPrecNum = 0.0f;
				float newOrderPrecNum = 0.0f;
				float oldOrderPrecNum = 0.0f;

				if(retMap != null){
					allOrderNum = Integer.parseInt(retMap.get("ordered_num").toString());
					newOrderNum = Integer.parseInt(retMap.get("newUordered_num").toString());
					oldOrdernum = Integer.parseInt(retMap.get("oldUordered_num").toString());
					//uv adduv olduv
					Map<String, Object> map =(HashMap<String, Object>) UVMap.get(dateFormat.format(day));
					if(map != null){
						int targetUVNum = Integer.parseInt(map.get("targetUV").toString());
						int targetNewUVNum = Integer.parseInt(map.get("targetNewUV").toString());
						int targetOldUVNum = Integer.parseInt(map.get("targetOldUV").toString());

						if(targetUVNum > 0){
							allOrderPrecNum =(float) allOrderNum * 100 / targetUVNum;
						}
						if(targetNewUVNum > 0){
							newOrderPrecNum =(float) newOrderNum * 100 / targetNewUVNum;
						}
						if(targetOldUVNum > 0){
							oldOrderPrecNum =(float) oldOrdernum * 100 / targetOldUVNum;
						}
					}
				}
				allOrder.put(DateUtil.formatDate(day, ""), allOrderNum);
				newOrder.put(DateUtil.formatDate(day, ""), newOrderNum);
				oldOrder.put(DateUtil.formatDate(day, ""), oldOrdernum);
				allOrderPrec.put(DateUtil.formatDate(day, ""), df.format(allOrderPrecNum)+"%");
				newOrderPrec.put(DateUtil.formatDate(day, ""), df.format(newOrderPrecNum)+"%");
				oldOrderPrec.put(DateUtil.formatDate(day, ""), df.format(oldOrderPrecNum)+"%");

			}
		}
		Map<String, Object> allOrderSum = new HashMap<String, Object>();
		allOrderSum.put("display_name", "总订购");
		retlistProducts.add(allOrderSum);

		Map<String, Object> newAllOrder = new HashMap<String, Object>();
		newAllOrder.put("display_name", "新用户总订购");
		retlistProducts.add(newAllOrder);

		Map<String, Object> oldAllOrder = new HashMap<String, Object>();
		oldAllOrder.put("display_name", "老用户总订购");
		retlistProducts.add(oldAllOrder);

		Map<String, Object> allAllOrderPrec = new HashMap<String, Object>();
		allAllOrderPrec.put("display_name", "总用户订购率");
		retlistProducts.add(allAllOrderPrec);

		Map<String, Object> newAllOrderPrec = new HashMap<String, Object>();
		newAllOrderPrec.put("display_name", "新用户总订购率");
		retlistProducts.add(newAllOrderPrec);

		Map<String, Object> oldAllOrderPrec = new HashMap<String, Object>();
		oldAllOrderPrec.put("display_name",	"老用户总订购率");
		retlistProducts.add(oldAllOrderPrec);


		for (Date day : listDays) { //时间循环 n天
			String sqlDate = "SELECT day, sum(newUordered_num) allNewNum, sum(oldUordered_num) allOldNum, sum(ordered_num) allNum " +
					" FROM "+tabName_newOrder+" WHERE day = ? AND platform_id = ? group by day";
			Map<String, Object> retMap = DruidUtil.queryUniqueResult(readConnection, sqlDate, dateFormat.format(day), lPlatform);

			int allOrderNumS = 0;
			int newOrderNumS = 0;
			int oldOrdernumS = 0;
			float allOrderPrecNumS = 0.0f;
			float newOrderPrecNumS = 0.0f;
			float oldOrderPrecNumS = 0.0f;

			if(retMap != null){
				allOrderNumS = Integer.parseInt(retMap.get("allNum").toString());
				newOrderNumS = Integer.parseInt(retMap.get("allNewNum").toString());
				oldOrdernumS = Integer.parseInt(retMap.get("allOldNum").toString());
				//uv adduv olduv
				Map<String, Object> map =(HashMap<String, Object>) UVMap.get(dateFormat.format(day));
				if(map != null){
					int targetUVNum = Integer.parseInt(map.get("targetUV").toString());
					int targetNewUVNum = Integer.parseInt(map.get("targetNewUV").toString());
					int targetOldUVNum = Integer.parseInt(map.get("targetOldUV").toString());

					if(targetUVNum > 0){
						allOrderPrecNumS =(float) allOrderNumS * 100 / targetUVNum;
					}
					if(targetNewUVNum > 0){
						newOrderPrecNumS =(float) newOrderNumS * 100 / targetNewUVNum;
					}
					if(targetOldUVNum > 0){
						oldOrderPrecNumS =(float) oldOrdernumS * 100 / targetOldUVNum;
					}
				}
			}
			allOrderSum.put(DateUtil.formatDate(day, ""), allOrderNumS);
			newAllOrder.put(DateUtil.formatDate(day, ""), newOrderNumS);
			oldAllOrder.put(DateUtil.formatDate(day, ""), oldOrdernumS);
			allAllOrderPrec.put(DateUtil.formatDate(day, ""), df.format(allOrderPrecNumS)+"%");
			newAllOrderPrec.put(DateUtil.formatDate(day, ""), df.format(newOrderPrecNumS)+"%");
			oldAllOrderPrec.put(DateUtil.formatDate(day, ""), df.format(oldOrderPrecNumS)+"%");
			/*allAllOrderPrec.put(DateUtil.formatDate(day, ""), Double.parseDouble(df.format(allOrderPrecNumS)));
			newAllOrderPrec.put(DateUtil.formatDate(day, ""), Double.parseDouble(df.format(newOrderPrecNumS)));
			oldAllOrderPrec.put(DateUtil.formatDate(day, ""), Double.parseDouble(df.format(oldOrderPrecNumS)));*/
		}
		return retlistProducts;
	}

	public List<Map<String, Object>> getDatasForWeek(Connection readConnection, List<Date> listDays,
													List<Map<String, Object>> listProducts, Integer lPlatform) throws Exception {

		List<Map<String, Object>> retlistProducts = new ArrayList<Map<String, Object>>();        //返回list

		return retlistProducts;
	}

	public IResultInfo<Map<String, Object>> getOrderSource(Integer lPlatform, Date startDate, Date endDate) {
		IResultInfo<Map<String, Object>> ri = null;
		Connection readConnection = null;

		try {
			readConnection = DruidUtil.getRandomReadConnection();

			String querySql = "SELECT * FROM bi_gscmcc_children_orderPerUi bil " +
					"LEFT JOIN bi_ui_deep bid ON bil.ui = bid.ui_name WHERE " +
					"platform_id = ? AND day >= ? AND day <= ? ORDER BY bil.day desc, bil.orderingPerUi_nums desc ";
/*
			String querySql = "SELECT bil.linkLNode, sum(link_num) sumNum, bid.describe FROM bi_linkpoint_day bil " +
					"LEFT JOIN bi_ui_deep bid  on bil.linkLNode = bid.ui_name WHERE " +
					"bil.linkENode = 'subscribe' AND platform_id = ? AND day >= ? AND day <= ? " +
					"GROUP BY bil.linkLNode";*/

			List<Map<String, Object>> retList = DruidUtil.queryList(readConnection, querySql, lPlatform, DateUtil.formatDate(startDate, ""), DateUtil.formatDate(endDate, ""));

			ri = new ResultInfo<>(ResultInfo.BUSINESS_SUCCESS, retList, retList.size(), "");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getOrderSource error" + e.getMessage());
		} finally {
			DruidUtil.close(readConnection);
		}
		return ri;
	}

}