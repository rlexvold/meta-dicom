package acme.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Test
{
	static void log(String s)
	{
		System.out.println(s);
	}

	static void log(long l)
	{
	}// {log(""+new Date(l));};

	static void log(String s, long l)
	{
	}// {log(s+new Date(l));};
	static final String regExp = "(?i)(sun|mon|tue|wed|thu|fri|sat):" + "(\\d{1,2}):(\\d\\d)-(\\d{1,2}):(\\d\\d)(\\[\\w\\w\\w\\]){0,1}[\\s]*[\\,]{0,1}[\\s]*";
	static final String verifyRegExp = "(" + regExp + ")*$";

	static public boolean verifyBlackoutString(String blackoutString)
	{
		return Pattern.matches(verifyRegExp, blackoutString);
	}
	class Schedule
	{
		HashMap packageBlackout = new HashMap();
		long cacheExpiration = 0;

		synchronized public void reset()
		{
			packageBlackout = new HashMap();
			cacheExpiration = 0;
		}

		synchronized public void addPackageBlackout(String pkgid, String blackoutString)
		{
			if (!verifyBlackoutString(blackoutString)) log("INVALID BLACKOUT " + blackoutString);
			try
			{
				if (blackoutString == null) return;
				blackoutString = blackoutString.trim();
				if (blackoutString.length() == 0) return;
				Pattern p = Pattern.compile(regExp);
				BlackoutInfo blackoutInfo = new BlackoutInfo();
				Matcher m = p.matcher(blackoutString);
				while (m.find())
				{
					blackoutInfo.add(m.group(1), 60 * Integer.parseInt(m.group(2)) + Integer.parseInt(m.group(3)),
							60 * Integer.parseInt(m.group(4)) + Integer.parseInt(m.group(5)), m.group(6));
				}
				packageBlackout.put(pkgid, blackoutInfo);
			}
			catch (Exception e)
			{
				log("addPackageBlackout caught " + pkgid + " " + blackoutString + " " + e);
			}
		}

		synchronized public boolean inBlackout(String pkgid, Date d)
		{
			if (System.currentTimeMillis() >= cacheExpiration) updateForCurrentDay(new Date());
			BlackoutInfo bi = (BlackoutInfo) packageBlackout.get(pkgid);
			if (bi == null) return false;
			return bi.inBlackout(d.getTime());
		}

		// for testing
		synchronized boolean inBlackout(String pkgid, Date d, Date today)
		{
			updateForCurrentDay(today);
			BlackoutInfo bi = (BlackoutInfo) packageBlackout.get(pkgid);
			if (bi == null) return false;
			return bi.inBlackout(d.getTime());
		}

		private void updateForCurrentDay(Date d)
		{
			Calendar today = Calendar.getInstance();
			today.setTimeInMillis(d.getTime());
			today.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			int dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;
			long startOfDay = today.getTimeInMillis();
			log("startOfDay ", startOfDay);
			today.add(Calendar.DAY_OF_YEAR, 1);
			cacheExpiration = today.getTimeInMillis();
			log("cacheExpiration ", cacheExpiration);
			for (Iterator iter = packageBlackout.values().iterator(); iter.hasNext();)
			{
				BlackoutInfo bi = (BlackoutInfo) iter.next();
				bi.updateForCurrentDay(dayOfWeek, startOfDay);
			}
		}
		class BlackoutInfo
		{
			class TimeRange
			{
				TimeRange(int startMin, int endMin)
				{
					this.startMin = startMin;
					this.endMin = endMin;
				}

				void recalculate(long startOfDayMS)
				{
					todayStartMS = startOfDayMS + startMin * 60 * 1000;
					todayEndMS = startOfDayMS + endMin * 60 * 1000;
				}

				boolean contains(long ms)
				{
					log(todayStartMS);
					log(ms);
					log(todayEndMS);
					return ms >= todayStartMS && ms < todayEndMS;
				}
				int startMin;
				int endMin;
				long todayStartMS;
				long todayEndMS;
			}
			private int day = 0;
			private List range[] = new List[7];

			public boolean inBlackout(long time)
			{
				if (range[day] != null)
				{
					List l = range[day];
					for (int i = 0; i < l.size(); i++)
						if (((TimeRange) (l.get(i))).contains(time)) return true;
				}
				return false;
			}

			public void add(String day, int startMin, int endMin, String timeZone)
			{
				int offsetMin = 0;
				if (timeZone != null)
				{
					TimeZone entryTimeZone = TimeZone.getTimeZone(timeZone.substring(1, 4).toUpperCase());
					offsetMin = (int) ((TimeZone.getDefault().getRawOffset() - entryTimeZone.getRawOffset()) / 1000 / 60);
				}
				log("add blackout: " + day + " " + startMin + " " + endMin + " tz " + timeZone + " " + offsetMin);
				int dayIndex = getDayIndex(day);
				TimeRange timeRange = new TimeRange(startMin + offsetMin, endMin + offsetMin);
				if (range[dayIndex] == null) range[dayIndex] = new ArrayList();
				range[dayIndex].add(timeRange);
			}

			public void updateForCurrentDay(int day, long startOfDay)
			{
				this.day = day;
				if (range[day] != null)
				{
					List l = range[day];
					for (int i = 0; i < l.size(); i++)
						((TimeRange) (l.get(i))).recalculate(startOfDay);
				}
			}

			private int getDayIndex(String day)
			{
				String days[] = { "sun", "mon", "tue", "wed", "thu", "fri", "sat" };
				for (int i = 0; i < days.length; i++)
					if (days[i].equalsIgnoreCase(day)) return i;
				return -1;
			}
		}
	}

	static void test()
	{
	}
	class Tester
	{
		Schedule s = new Schedule();

		Tester()
		{
			add("123", "sun:12:00-13:00 , sun:6:00-8:30,mon:10:00-12:00[est],sat:00:00-24:00");
			testTrue("123", getDate(Calendar.MONDAY, 11, 30, "EST"));
			testFalse("123", getDate(Calendar.MONDAY, 11, 30, "PST"));
			testTrue("123", getDate(Calendar.SUNDAY, 6, 05));
			testFalse("123xxx", getDate(Calendar.SUNDAY, 6, 05));
			testFalse("123", getDate(Calendar.SUNDAY, 8, 35));
			testFalse("123", getDate(Calendar.SUNDAY, 9, 00));
			testTrue("123", getDate(Calendar.SUNDAY, 12, 30));
			testFalse("123", getDate(Calendar.SUNDAY, 13, 30));
			testTrue("123", getDate(Calendar.MONDAY, 7, 30));
			testFalse("123", getDate(Calendar.MONDAY, 10, 30));
			testTrue("123", getDate(Calendar.SATURDAY, 7, 00));
			testTrue("123", getDate(Calendar.SATURDAY, 11, 30));
			log("" + verifyBlackoutString("sun:12:00-13:00"));
			log("" + verifyBlackoutString("sun:6:00-8:30  ,  mon:10:00-12:00[est]"));
		}

		void add(String pkg, String def)
		{
			s.addPackageBlackout(pkg, def);
			log(pkg + "->" + def);
		}

		Date getDate(int dayOfWeek, int hour, int min)
		{
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("PST"));
			c.set(2005, 1, 1, hour, min, 0);
			while (c.get(Calendar.DAY_OF_WEEK) != dayOfWeek)
				c.add(Calendar.DAY_OF_YEAR, 1);
			return c.getTime();
		}

		Date getDate(int dayOfWeek, int hour, int min, String timeZone)
		{
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone(timeZone));
			c.set(2005, 1, 1, hour, min, 0);
			while (c.get(Calendar.DAY_OF_WEEK) != dayOfWeek)
				c.add(Calendar.DAY_OF_YEAR, 1);
			return c.getTime();
		}

		void testTrue(String pkg, Date d)
		{
			boolean b = s.inBlackout(pkg, d, d);
			log("TRUE " + d);
			if (!b) log("FAIL");
		}

		void testFalse(String pkg, Date d)
		{
			boolean b = s.inBlackout(pkg, d, d);
			log("FALSE " + d);
			if (b) log("FAIL");
		}
	}

	Test()
	{
		new Tester();
	}

	public static void main(String[] args)
	{
		new Test();
		// Pattern p = Pattern.compile("");
		// Matcher m = p.matcher("test");
		// while (m.find())
		// log(m.group());
	}
}
