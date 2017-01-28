package acme.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Props
{
	static HashMap hm = new HashMap();
	static
	{
		InputStream is = null;
		try
		{
			is = ClassLoader.getSystemResourceAsStream("props.txt");
			for (;;)
			{
				String s = Util.readLine(is);
				if (s == null) break;
				StringTokenizer st = new StringTokenizer(s, "=");
				String name = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
				String value = st.hasMoreTokens() ? st.nextToken() : "";
				hm.put(name, value);
			}
		}
		catch (Exception e)
		{
			System.out.println("Props.init caught " + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (is != null) is.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	static public String get(String key)
	{
		return (String) hm.get(key);
	}

	static public int getInt(String key)
	{
		return Integer.parseInt(((String) hm.get(key)));
	}
}
