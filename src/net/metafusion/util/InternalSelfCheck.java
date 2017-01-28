package net.metafusion.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import net.metafusion.figaro.Figaro;
import acme.util.Log;
import acme.util.Util;

public class InternalSelfCheck implements Runnable
{
	private static Integer	defaultWaitTimeInMinutes	= 60;
	private static Integer	maximimumWaitTimeInMinutes	= 60 * 24 * 7;
	private Integer			waitTimeInMinutes			= defaultWaitTimeInMinutes;
	private String			additionalUrl				= "DEFAULT";
	private String[]		urlStrings					= null;
	private static String[]	defaultUrlStrings			= new String[] { "http://beacon1.metafusion.net", "http://beacon3.metafusion.net" };
	private static String	lastStudy					= "No_Previous_Study";

	public InternalSelfCheck()
	{
	}

	public static void setLastStudy(String study)
	{
		lastStudy = study;
	}

	private void ping(String urlString)
	{
		HttpURLConnection connection = null;
		BufferedReader in = null;
		try
		{
			URL url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				Log.log(inputLine);
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
			}
			catch (Exception e)
			{
			}
			connection.disconnect();
		}
	}

	public void run()
	{
		for (;;)
		{
			Figaro.checkIt();
			try
			{
				Log.log("Internal self-check start.");
				for (int i = 0; i < getDefaultUrlStrings().length; i++)
				{
					ping(getDefaultUrlStrings()[i] + "/" + getAdditionalUrl());
				}
				if (getUrlStrings() != null)
				{
					for (int i = 0; i < getUrlStrings().length; i++)
					{
						ping(getUrlStrings()[i] + "/" + getAdditionalUrl() + "/" + lastStudy);
					}
				}
				Log.log("Internal self-check no error.");
			}
			catch (Exception e)
			{
				Log.log("Internal self-check error: ", e);
			}
			Util.sleep(getWaitTimeInMinutes() * 60000);
		}
	}

	public Integer getWaitTimeInMinutes()
	{
		if (waitTimeInMinutes == null || waitTimeInMinutes > getMaximimumWaitTimeInMinutes())
			return getDefaultWaitTimeInMinutes();
		return waitTimeInMinutes;
	}

	public void setWaitTimeInMinutes(Integer waitTimeInMinutes)
	{
		if (waitTimeInMinutes != null && waitTimeInMinutes != 0 && waitTimeInMinutes < getMaximimumWaitTimeInMinutes())
			this.waitTimeInMinutes = waitTimeInMinutes;
	}

	public String[] getUrlStrings()
	{
		return urlStrings;
	}

	public void setUrlStrings(String[] urlStrings)
	{
		this.urlStrings = urlStrings;
	}

	public String getAdditionalUrl()
	{
		return additionalUrl;
	}

	public void setAdditionalUrl(String additionalUrl)
	{
		this.additionalUrl = additionalUrl;
	}

	public static Integer getDefaultWaitTimeInMinutes()
	{
		return defaultWaitTimeInMinutes;
	}

	public static void setDefaultWaitTimeInMinutes(Integer defaultWaitTimeInMinutes)
	{
		InternalSelfCheck.defaultWaitTimeInMinutes = defaultWaitTimeInMinutes;
	}

	public static Integer getMaximimumWaitTimeInMinutes()
	{
		return maximimumWaitTimeInMinutes;
	}

	public static void setMaximimumWaitTimeInMinutes(Integer maximimumWaitTimeInMinutes)
	{
		InternalSelfCheck.maximimumWaitTimeInMinutes = maximimumWaitTimeInMinutes;
	}

	public static String[] getDefaultUrlStrings()
	{
		return defaultUrlStrings;
	}

	public static void setDefaultUrlStrings(String[] defaultUrlStrings)
	{
		InternalSelfCheck.defaultUrlStrings = defaultUrlStrings;
	}
}
