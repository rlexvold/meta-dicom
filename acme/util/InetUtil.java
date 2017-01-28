package acme.util;

import java.net.InetSocketAddress;

public class InetUtil
{
	public static InetSocketAddress decodeInetSocketAddress(String addrString)
	{
		try
		{
			int colon = addrString.indexOf(':');
			if (colon == -1)
				return new InetSocketAddress(addrString, 0);
			else return new InetSocketAddress(addrString.substring(0, colon), Integer.parseInt(addrString.substring(colon + 1)));
		}
		catch (NumberFormatException e)
		{
			throw new NestedException(e);
		}
	}

	public static String toString(InetSocketAddress addr)
	{
		return addr.getAddress().getHostAddress() + ":" + addr.getPort();
	}
}