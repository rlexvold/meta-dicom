package acme.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebServer implements Runnable
{
	// static Log log = new Log("WebServer", true);
	static void log(String s)
	{
		Log.log(s);
	}
	static HashMap suffix = new HashMap();

	void initMap()
	{
		suffix.put("", "content/unknown");
		suffix.put(".uu", "application/octet-stream");
		suffix.put(".exe", "application/octet-stream");
		suffix.put(".ps", "application/postscript");
		suffix.put(".zip", "application/zip");
		suffix.put(".sh", "application/x-shar");
		suffix.put(".tar", "application/x-tar");
		suffix.put(".snd", "audio/basic");
		suffix.put(".au", "audio/basic");
		suffix.put(".wav", "audio/x-wav");
		suffix.put(".gif", "image/gif");
		suffix.put(".jpg", "image/jpeg");
		suffix.put(".jpeg", "image/jpeg");
		suffix.put(".htm", "text/html");
		suffix.put(".html", "text/html");
		suffix.put(".text", "text/plain");
		suffix.put(".c", "text/plain");
		suffix.put(".cc", "text/plain");
		suffix.put(".c++", "text/plain");
		suffix.put(".h", "text/plain");
		suffix.put(".pl", "text/plain");
		suffix.put(".txt", "text/plain");
		suffix.put(".java", "text/plain");
		suffix.put(".class", "application/java-class");
		suffix.put(".jar", "application/java-archive");
	}

	static String getMimeType(String filename)
	{
		int pos = filename.indexOf(".");
		if (pos == -1) return "content/unknown";
		String ext = filename.substring(pos).toLowerCase();
		String mime = (String) suffix.get(ext);
		if (mime != null)
			return mime;
		else return "content/unknown";
	}
	static WebResource failResource = new WebResource("/FAIL")
	{
		@Override
		public int handle(WebRequest r) throws Exception
		{
			r.println("HTTP/1.0 404 not found");
			return 404;
		}
	};

	public void addFiles(boolean recurse, String where, File dir, String ext)
	{
		File files[] = dir.listFiles();
		if (files != null) for (File element : files)
			if (element.isFile() && (ext == null || element.getName().endsWith(ext)))
				addFile(where, element);
			else if (recurse) addFiles(recurse, where + element.getName() + "/", element, ext);
	}

	public void addFiles(boolean recurse, String where, File dir)
	{
		addFiles(recurse, where, dir, null);
	}

	public void addFiles(String where, File dir)
	{
		addFiles(true, where, dir, null);
	}

	public void addFile(File f)
	{
		addFile("/", f);
	}

	public void addFile(String where, String name, File f)
	{
		WebFileResource fr = new WebFileResource(where + name, f);
		log("addFile: " + fr.name);
		resourceMap.put(name, fr);
	}

	public void addFile(String where, File f)
	{
		WebFileResource fr = new WebFileResource(where + f.getName(), f);
		log("addFile: " + fr.name);
		resourceMap.put(fr.name, fr);
	}

	public void addResource(WebResource r)
	{
		log("addResource: " + r.name + ":" + r.getClass());
		resourceMap.put(r.name, r);
	}

	public void setRootName(String rootName)
	{
		this.rootName = rootName;
	}

	public String getRootName()
	{
		return rootName;
	}
	String rootName = null;
	HashMap resourceMap = new HashMap();
	int port;

	public WebServer(int port)
	{
		this.port = port;
		initMap();
	}

	public void run()
	{
		try
		{
			ServerSocket ss = new ServerSocket(port);
			// todo: Log list
			// ss.setReuseAddress(true);
			try
			{
				for (;;)
				{
					Socket s = ss.accept();
					new Thread(new WebRequest(this, s)).start();
				}
			}
			catch (IOException e)
			{
				log("WebServer caught " + e);
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			log("PerTest caught " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		log("start ");
		try
		{
			Map m = new HashMap();
			m.put("r", "R");
			// String s = "f%o%%o %ba%r%%r%%r% ba%z%%%";
			// Log(s);
			// Log(substitute(s, m));
			// System.exit(-1);
			WebServer server = new WebServer(80);
			try
			{
				server.addFile(new File("C:\\IdeaProjects\\perfsrc\\networktest.html"));
				server.addFile(new File("C:\\IdeaProjects\\perfsrc\\test.html"));
				// server.addFiles(true, "/", new
				// File("C:\\IdeaProjects\\classes"));
				server.addResource(new WebResource("/foo")
				{
					@Override
					public int handle(WebRequest r) throws Exception
					{
						HTMLReply(r, "<html><head><title>New Page 1</title></head><body><p>FOO</p></body></html>");
						return 200;
					}
				});
				// server.addResource( new WebResource("/Log") {
				// public int handle(WebRequest r) throws Exception {
				// r.println("HTTP/1.0 200 OK");
				// //r.println("Content-type: "+"text/html");
				// r.println("Content-type: "+"text/plain");
				// r.println("");
				// int hw = 0;
				// for (;;) {
				// StringBuffer sb = new StringBuffer();
				// int nhw = Logger.getLogSince(hw, sb);
				// if (nhw != hw) {
				// r.print(sb.toString());
				// hw = nhw;
				// }
				// Thread.sleep(500);
				// }
				// // return 200;
				// }
				// });
				new Thread(server).start();
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							for (;;)
							{
								log("" + new Date(System.currentTimeMillis()));
								Thread.sleep(200);
							}
						}
						catch (InterruptedException e)
						{
							e.printStackTrace(); // To change body of catch
													// statement use Options |
													// File Templates.
						}
					}
				}.start();
			}
			catch (Exception e)
			{
				log(" caught " + e);
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			log("ServerTest caught " + e);
			e.printStackTrace();
		}
	}

	static public String substituteEncode(String str, Map m)
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		char s[] = str.toCharArray();
		while (i < s.length)
		{
			while (i < s.length && s[i] != '$')
				sb.append(s[i++]);
			int start = i;
			if (i < s.length)
			{
				boolean valid = true;
				i++;
				while (valid && i < s.length && s[i] != '$')
					valid = valid && Character.isJavaIdentifierPart(s[i++]);
				if (valid && i < s.length)
				{
					i++;
					if (i - start == 2)
						sb.append('$');
					else
					{
						String tag = new String(s, start + 1, i - start - 2);
						String sub = (String) m.get(tag);
						if (sub != null) try
						{
							sb.append(URLEncoder.encode(sub, "UTF-8"));
						}
						catch (UnsupportedEncodingException e)
						{
							e.printStackTrace();
							throw new NestedException(e);
						}
					}
					continue;
				}
			}
			sb.append(s, start, i - start);
		}
		return sb.toString();
	}

	static public String substitute(String str, Map m)
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		char s[] = str.toCharArray();
		while (i < s.length)
		{
			while (i < s.length && s[i] != '$')
				sb.append(s[i++]);
			int start = i;
			if (i < s.length)
			{
				boolean valid = true;
				i++;
				while (valid && i < s.length && s[i] != '$')
					valid = valid && Character.isJavaIdentifierPart(s[i++]);
				if (valid && i < s.length)
				{
					i++;
					if (i - start == 2)
						sb.append('$');
					else
					{
						String tag = new String(s, start + 1, i - start - 2);
						String sub = (String) m.get(tag);
						if (sub != null) sb.append(sub);
					}
					continue;
				}
			}
			sb.append(s, start, i - start);
		}
		return sb.toString();
	}
}
