package acme.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.StringTokenizer;

public class WebRequest implements Runnable
{
	// static Log log = new Log("WebServer", true);
	static void log(String s)
	{
		Log.log(s);
	}
	static final byte[] EOL = { (byte) '\r', (byte) '\n' };
	WebServer server;
	Socket s;
	InputStream is;
	OutputStream os;

	public WebRequest(WebServer server, Socket s)
	{
		this.server = server;
		this.s = s;
		try
		{
			s.setTcpNoDelay(true);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}

	public void write(byte b[]) throws IOException
	{
		os.write(b);
		os.flush();
	}

	public void write(InputStream is) throws IOException
	{
		byte buffer[] = new byte[4096];
		for (;;)
		{
			int cnt = is.read(buffer);
			if (cnt == -1) break;
			os.write(buffer, 0, cnt);
		}
	}

	public void print(String s) throws Exception
	{
		os.write(s.getBytes());
	}

	public void println(String s) throws Exception
	{
		os.write(s.getBytes());
		os.write(EOL);
	}

	String readln() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (;;)
		{
			int b = is.read();
			if (b == -1) break;
			if (b == (byte) '\n') break;
			sb.append((char) b);
		}
		return sb.toString().trim();
	}

	public String read(int size) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++)
		{
			int b = is.read();
			if (b == -1) break;
			sb.append((char) b);
		}
		return sb.toString();
	}

	HashMap parseArgs(String args)
	{
		HashMap hm = new HashMap();
		if (args != null && args.length() != 0)
		{
			try
			{
				args = URLDecoder.decode(args, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				throw new NestedException(e);
			}
			StringTokenizer sta = new StringTokenizer(args, "&");
			while (sta.hasMoreTokens())
			{
				String s = sta.nextToken();
				if (s.length() == 0) continue;
				int i = s.indexOf('=');
				hm.put(s.substring(0, i), i < s.length() - 1 ? s.substring(i + 1) : "");
				log(s.substring(0, i) + "-" + (i < s.length() - 1 ? s.substring(i + 1) : ""));
			}
		}
		return hm;
	}
	String method;
	String name;
	HashMap args;

	public String getMethod()
	{
		return method;
	}

	public String getName()
	{
		return name;
	}

	public HashMap getArgs()
	{
		return args;
	}

	public void run()
	{
		try
		{
			is = new BufferedInputStream(s.getInputStream());
			os = s.getOutputStream();
			String cmd = readln();
			String line = cmd;
			String post = null;
			int postSize = 0;
			for (;;)
			{
				log(line);
				line = readln();
				if (line.length() == 0) break;
				if (line.startsWith("Content-Length:")) postSize = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
			}
			if (postSize != 0) post = read(postSize);
			StringTokenizer st = new StringTokenizer(cmd, " ");
			method = st.nextToken();
			name = st.nextToken();
			String getArgs = null;
			int qi = name.indexOf('?');
			if (qi != -1)
			{
				getArgs = name.substring(qi + 1);
				name = name.substring(0, qi);
			}
			if (method.equals("POST"))
				args = parseArgs(post);
			else args = parseArgs(getArgs);
			log(method + " " + name + " ");
			if (name.equals("/") && server.getRootName() != null) name = server.getRootName();
			WebResource r = (WebResource) server.resourceMap.get(name);
			if (r != null)
				r.handle(this);
			else WebServer.failResource.handle(this);
		}
		catch (Exception e)
		{
			log("Request.run caught " + e);
		}
		finally
		{
			try
			{
				s.close();
			}
			catch (Exception e)
			{
				;
			}
			s = null;
		}
	}
}
