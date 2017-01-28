package acme.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil
{
	static private Calendar cf = Calendar.getInstance();
	static private DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss");

	static public String formatMMDDYYYYHHMMSS(Date d)
	{
		synchronized (df)
		{
			return df.format(d);
		}
	}

	static public Date parseMMDDYYYYHHMMSS(String s) throws ParseException
	{
		synchronized (df)
		{
			return df.parse(s);
		}
	}
	static private DateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");

	static public String formatYYYYYMMDDHHMMSS(Date d)
	{
		synchronized (df2)
		{
			return df2.format(d);
		}
	}

	static public Date parseYYYYMMDDHHMMSS(String s) throws ParseException
	{
		synchronized (df2)
		{
			return df2.parse(s);
		}
	}
	static private DateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");

	static public String formatYYYYY_MM_DD(Date d)
	{
		synchronized (df3)
		{
			return df3.format(d);
		}
	}

	static public Date parseYYYY_MM_DD(String s) throws ParseException
	{
		synchronized (df3)
		{
			return df3.parse(s);
		}
	}

	public static void main(String[] args) throws Exception
	{
		String s = df3.format(new Date());
		Date d = df3.parse(s);
		s = df3.format(d);
		;
	}
}