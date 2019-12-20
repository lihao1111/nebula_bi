package com.topdraw.nebula_bi;

import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.MD5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NebulaBiApplicationTests {

	@Test
	public void contextLoads() {
		Connection readConnection = null;
		try {
			readConnection = DruidUtil.getRandomReadConnection();


			String tabName = "bi_linkpoint_day";
			String querySql = "select * from "+ tabName+ " where day = ?";

			List<Map<String, Object>> retlist = DruidUtil.queryList(readConnection, querySql, "2019-07-02");



		} catch (Exception e){
			e.printStackTrace();
		} finally {
			DruidUtil.close(readConnection);
		}
	}

	@Test
	public void test001(){
		System.out.println(MD5Util.encodePassword("123456"));
	}

}
