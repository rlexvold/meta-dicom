package net.metafusion.admin;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import acme.util.StringUtil;
import acme.util.XML;

public class LogBean implements Comparable, Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	public static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private Date dateObject;
	private String date = "";
	private String time = "";
	private String ae = "";
	private String event = "";
	private String object = "";
	private String ip = "";

	// 1077767804062,store-to,AE_TITLE,/10.42.3.20:4006,SOPInstanceUID=1.2.840.113674.950809132337081.100
	// MSJavaIsm
	public int compareDateTo(Date date, Date anotherDate)
	{
		long thisTime = date.getTime();
		long anotherTime = anotherDate.getTime();
		return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
	}

	public int compareTo(Object o)
	{
		return compareDateTo(dateObject, ((LogBean) o).getDateObject());
	}

	LogBean(XML x)
	{
		String ss = x.get();
		String splits[] = StringUtil.split(ss, ',');
		String s[] = { "", "", "", "", "" };
		System.arraycopy(splits, 0, s, 0, Math.min(5, splits.length));
		dateObject = new Date(Long.parseLong(s[0]));
		date = dateFormat.format(dateObject);
		time = timeFormat.format(dateObject);
		ae = s[2];
		event = s[1];
		object = s[4];
		ip = s[3];
	}

	LogBean()
	{
		date = dateFormat.format(new Date());
		time = timeFormat.format(new Date());
		ae = "ae";
		event = "GET";
		object = "192.12.435.434.344.9";
		ip = "127.0.0.1";
	}

	LogBean(String date, String time, String ae, String event, String object, String ip)
	{
		this.date = date;
		this.time = time;
		this.ae = ae;
		this.event = event;
		this.object = object;
		this.ip = ip;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getAE()
	{
		return ae;
	}

	public void setAE(String ae)
	{
		this.ae = ae;
	}

	public String getEvent()
	{
		return event;
	}

	public void setEvent(String event)
	{
		this.event = event;
	}

	public String getObject()
	{
		return object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public Date getDateObject()
	{
		return dateObject;
	}

	public void setDateObject(Date dateObject)
	{
		this.dateObject = dateObject;
	}
	// String cols[] = {
	// "Date", "Time", "AE", "Event","Object","IP"
	// };
	// public Object getValueAt(int col) {
	// switch (col) {
	// case 0: return "1/1/200";
	// case 1: return "13:45:65";
	// case 2: return "ct66";
	// case 3: return "MOVE";
	// case 4: return "127.34.5355.56.6657.90";
	// case 5: return "10.56.61.3";
	// }
	// return null;
	// }
}