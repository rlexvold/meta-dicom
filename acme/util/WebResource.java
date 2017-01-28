package acme.util;

import java.util.Date;

public class WebResource
{
	// static Log log = new Log("WebServer", true);
	static void log(String s)
	{
		Log.log(s);
	}
	String name;

	public WebResource(String name)
	{
		this.name = name;
	}

	public int handle(WebRequest r) throws Exception
	{
		HTMLReply(r, "" + r.getName() + "?" + r.getArgs());
		return 200;
	}

	public void TestReply(WebRequest r, String type, String reply) throws Exception
	{
		byte b[] = reply.getBytes();
		r.println("HTTP/1.0 200 OK");
		// r.println("Content-type: "+"text/html");
		r.println("Content-type: " + "text/plain");
		r.println("");
		// r.println("<html><head><title>New Page
		// 1</title></head><body><p>FOO");//</p></body></html>
		for (;;)
		{
			r.println(" " + new Date(System.currentTimeMillis()));
			Thread.sleep(30 * 1000);
		}
	}

	public void StringReply(WebRequest r, String type, String reply) throws Exception
	{
		byte b[] = reply.getBytes();
		r.println("HTTP/1.0 200 OK");
		r.println("Content-length: " + b.length);
		r.println("Content-type: " + type);
		r.println("");
		r.write(b);
	}

	public void HTMLReply(WebRequest r, String reply) throws Exception
	{
		StringReply(r, "text/html", reply);
	}
}
