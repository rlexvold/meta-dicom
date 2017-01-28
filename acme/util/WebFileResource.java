package acme.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

//<meta name="WRID" content="DumpAE">
public class WebFileResource extends WebResource
{
	public interface Handler
	{
		public void handle(Map args) throws Exception;
	}
	File f;
	StringTemplate st;

	public WebFileResource(String name, File f)
	{
		super(name);
		this.f = f;
	}

	public int handle(WebRequest r) throws Exception
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			byte[] b = new byte[(int) f.length()];
			dis.readFully(b);
			Map args = r.getArgs();
			String s = new String(b);
			String metaTag = "meta name=\"WRID\" content=";
			int metaIndex = s.indexOf(metaTag);
			if (metaIndex != -1)
			{
				int startIndex = metaIndex + metaTag.length() + 1;
				int endIndex = s.indexOf('"', startIndex);
				String name = s.substring(startIndex, endIndex);
				Handler h = (Handler) Class.forName(name).newInstance();
				h.handle(args);
			}
			st = new StringTemplate(s);
			s = st.substitute(args);
			byte bytes[] = s.getBytes();
			r.println("HTTP/1.0 200 OK");
			r.println("Content-length: " + bytes.length);
			r.println("Content-type: " + WebServer.getMimeType(f.getName()));
			r.println("");
			r.write(bytes);
		}
		catch (Exception e)
		{
			this.log("handle caught " + e);
		}
		finally
		{
			try
			{
				if (fis != null) fis.close();
			}
			catch (Exception e)
			{
				;
			}
		}
		return 200;
	}
}
