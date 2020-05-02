package com.topdraw.nebula_bi;

import org.afflatus.utility.DateUtil;
import org.afflatus.utility.DruidUtil;
import org.afflatus.utility.MD5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

			System.out.println(1);

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

	@Test
	public void test002() throws ParseException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date date = dateFormat.parse("2019-12-20");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_WEEK);

		System.out.println(dateFormat.format(DateUtil.getDateBeforeOrAfter(date, -(i-2))));

		System.out.println(i);
	}

	@Test
	public void test003() throws ParseException {

		System.out.println("2" +2);
		System.out.println(Integer.parseInt("2") +2);

		System.out.println(1 / 2);

		System.out.println(String.format("%.2f", Double.parseDouble("5") / 3));
	}


	@Test
	public void test004() throws ParseException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date date = dateFormat.parse("2019-11-01");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_MONTH);

		int e = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		System.out.println(dateFormat.format(DateUtil.getDateBeforeOrAfter(date, e-1)));
		System.out.println(i);
		System.out.println(e);


	}
}
