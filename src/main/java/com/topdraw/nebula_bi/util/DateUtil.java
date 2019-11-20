package com.topdraw.nebula_bi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {
    public static void main(String[] args) {
        try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date sDate = sdf.parse("2019-09-01");
			Date eDate = sdf.parse("2019-11-01");
			List<String> days = getMonth(sDate, eDate);

			String str = String.join(",", days);
			System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	/**
	 * 返回 周一 大范围
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */
	public static List<String> getWeek(Date startDate, Date endDate) {
       	List<String> days = new ArrayList<>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //实例化起始和结束Calendar对象
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        //分别设置Calendar对象的时间
        startCalendar.setTime(startDate);
		endCalendar.setTime(endDate);

		int startDayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK) -1;
		if(startDayOfWeek == 0){		//周天
			startCalendar.add(Calendar.DATE, -7);
		}else{
			startCalendar.add(Calendar.DATE, -startDayOfWeek);
		}
		int endDayOfWeek = endCalendar.get(Calendar.DAY_OF_WEEK) -1;		//day为周六的时候 是一周的第七天
		if(endDayOfWeek == 0){			//周天
			endCalendar.add(Calendar.DATE, 0);
		}else{
			endCalendar.add(Calendar.DATE, 7-endDayOfWeek);
		}

        while(startCalendar.getTime().getTime() < endCalendar.getTime().getTime()){
			int startDayOfWeek1 = startCalendar.get(Calendar.DAY_OF_WEEK) -1;
			startCalendar.add(Calendar.DATE, (7-startDayOfWeek1));
			Date week7 = startCalendar.getTime();
			days.add(sdf.format(week7));
		}

		return days;
    }

	/**
	 * 返回月
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getMonth(Date startDate, Date endDate) {

		List<String> days = new ArrayList<>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//实例化起始和结束Calendar对象
		Calendar startCalendar = Calendar.getInstance();
		Calendar endCalendar = Calendar.getInstance();
		//分别设置Calendar对象的时间
		startCalendar.setTime(startDate);
		startCalendar.set(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), 1);

		endCalendar.setTime(endDate);
		endCalendar.set(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), 2);

		Calendar curr = startCalendar;
		while(curr.before(endCalendar)){
			days.add(sdf.format(curr.getTime()));
			curr.add(Calendar.MONTH, 1);
		}
		return days;
	}
}