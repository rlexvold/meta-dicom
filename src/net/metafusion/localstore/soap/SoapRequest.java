package net.metafusion.localstore.soap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import net.metafusion.localstore.sync.Sync;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import org.apache.commons.lang.StringEscapeUtils;
import acme.db.JDBCUtil;
import acme.util.NestedException;
import acme.util.Util;
import acme.util.XML;

public class SoapRequest implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s, e);
	}
	static final byte[] EOL = { (byte) '\r', (byte) '\n' };
	protected SoapServer server;
	protected Socket s;
	protected InputStream is;
	protected OutputStream os;

	public SoapRequest(SoapServer server, Socket s)
	{
		this.server = server;
		this.s = s;
		// try {
		// s.setTcpNoDelay(true);
		// } catch (SocketException e) {
		// e.printStackTrace();
		// }
	}

	public void write(byte b[]) throws IOException
	{
		os.write(b);
		// os.flush();
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

	protected String readln() throws Exception
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

	protected HashMap parseArgs(String args)
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
				// log(s.substring(0, i)+"-"+(i<s.length()-1 ? s.substring(i+1)
				// : ""));
			}
		}
		return hm;
	}
	protected String soapAction = "";
	protected String method = "";
	protected String name = "";
	protected HashMap args = new HashMap();

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

	protected String stripNS(String s)
	{
		int index = s.lastIndexOf(':');
		if (index == -1) return s;
		return s.substring(index + 1);
	}

	protected void parseSoapRequest(String s)
	{
		XML xml = XML.parseXML(s);
		// log(""+xml);
		xml = xml.getChild(0).getChild(0);
		// log(""+xml);
		name = stripNS(xml.getName());
		xml = xml.getNode("args");
		// log(""+xml);
		List l = xml.getList("item");
		// log(""+l);
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			XML x = (XML) iter.next();
			// log(""+x.get("key")+":"+x.get("value"));
			args.put(x.get("key"), x.get("value"));
		}
	}

	protected String buildSoapResponse(String name, HashMap hashMap)
	{
		StringBuffer sb = new StringBuffer();
		String start = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/' soap:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/' xmlns:ns2='http://xml.apache.org/xml-soap'>"
				+ "<soap:Body><n:" + name + "Response xmlns:n=" + soapAction + "> " + "<Result xsi:type='ns2:Map'>";
		sb.append(start);
		Iterator iter = hashMap.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			sb.append("<item><key xsi:type='xsd:string'>" + StringEscapeUtils.escapeXml((String) entry.getKey()) + "</key><value xsi:type='xsd:string'>"
					+ StringEscapeUtils.escapeXml((String) entry.getValue()) + "</value></item>");
		}
		String end = "</Result></n:" + name + "Response></soap:Body></soap:Envelope>";
		sb.append(end);
		return sb.toString();
	}

	public void run()
	{
		try
		{
			is = new BufferedInputStream(s.getInputStream());
			os = new BufferedOutputStream(s.getOutputStream());
			String cmd = readln();
			String line = cmd;
			String post = null;
			int postSize = 0;
			for (;;)
			{
				// log(line);
				line = readln();
				if (line.length() == 0) break;
				if (line.startsWith("Content-Length:")) postSize = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				if (line.startsWith("SOAPAction:")) soapAction = line.substring(line.indexOf(':') + 1).trim();
			}
			if (postSize != 0) post = read(postSize);
			parseSoapRequest(post);
			HashMap rv = handle(name, args);
			String s = buildSoapResponse(name, rv);
			// log(s);
			byte b[] = s.getBytes();
			println("200 OK");
			println("Content-Type: text/xml");
			println("Content-Length: " + b.length);
			println("");
			write(b);
			os.flush();
		}
		catch (Exception e)
		{
			log("Request.run caught ", e);
			try
			{
				println("HTTP/1.0 404 not found");
				os.flush();
			}
			catch (Exception e1)
			{
				log("Request.run error ", e1);
			}
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

	protected HashMap handle(String cmd, HashMap args) throws Exception
	{
		HashMap rv = new HashMap();
		log("soap:" + cmd + " " + args);
		if (cmd.startsWith("mf_")) handle_mf(cmd, args);
		log("return soap:" + cmd + " " + rv);
		return rv;
	}

	protected HashMap handle_mf(String cmd, HashMap args) throws Exception
	{
		if (args.containsKey("studyid"))
		{
			long l = Long.parseLong((String) args.get("studyid"));
			Study s = StudyView.get().selectByID(l);
			args.put("studyuid", s.getStudyUID());
		}
		if (args.containsKey("userid") && args.get("userid") != null)
		{
			long l = Long.parseLong((String) args.get("userid"));
			String uname = JDBCUtil.get().selectString("select username from where userid=" + l);
			args.put("username", uname);
		}
		HashMap rv = new HashMap();
		log("soap:" + cmd + " " + args);
		if (cmd.startsWith("mf_")) Sync.get().put(cmd, args);
		log("return soap:" + cmd + " " + rv);
		return rv;
	}
}
// StringTokenizer st = new StringTokenizer(cmd, " ");
// method = st.nextToken();
// name = st.nextToken();
// String getArgs = null;
// int qi = name.indexOf('?');
// if (qi != -1) {
// getArgs = name.substring(qi+1);
// name = name.substring(0, qi);
// }
// if (method.equals("POST"))
// args = parseArgs(post);
// else args = parseArgs(getArgs);
// if (name.equals("/") && server.getRootName() != null)
// name = server.getRootName();
//
// String xresp = "<?xml version='1.0'?><SOAP:Envelope
// xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'
// xmlns:ns1='urn:xmethods-delayed-quotes'> " +
// " <SOAP:Body> <ns1:getQuoteResponse > " +
// " <amount>45.21</amount> "+
// " </ns1:getQuoteResponse> "+
// " </SOAP:Body> "+
// " </SOAP:Envelope> ";
//
// String resp = "<?xml version='1.0' encoding='UTF-8'?>"+
// "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'
// xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
// xmlns:xsd='http://www.w3.org/2001/XMLSchema'
// xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'
// soap:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>"+
// "<soap:Body><n:getQuoteResponse xmlns:n='urn:xmethods-delayed-quotes'> "+
// "<Result xsi:type='ns2:Map'>" +
// "<item><key xsi:type=\"xsd:string\">a</key><value
// xsi:type=\"xsd:string\">aa</value></item><item><key
// xsi:type=\"xsd:string\">b</key><value
// xsi:type=\"xsd:string\">bb</value></item>" +
// "</Result></n:getQuoteResponse></soap:Body></soap:Envelope>";
// " <person> "+
// " <name> "+
// " <givenName>Martin</givenName> "+
// " <familyName>Gudgin</familyName> "+
// " </name> "+
// " <age>33</age> "+
// " <height>64</height> "+
// " </person> ";
// <symbol xsi:type="ns2:Map"><item><key xsi:type="xsd:string">a</key><value
// xsi:type="xsd:string">aa</value></item><item><key
// xsi:type="xsd:string">b</key><value
// xsi:type="xsd:string">bb</value></item></symbol>
// <symbol xsi:type="xsd:string">ibm</symbol><symbol2
// xsi:type="xsd:string">ibm2</symbol2><symbol3
// xsi:type="xsd:string">ibm3</symbol3>
