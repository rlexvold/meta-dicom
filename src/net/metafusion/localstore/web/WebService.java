package net.metafusion.localstore.web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.storage.SSStore;
import acme.util.Util;

public class WebService
{
	static void log(String s)
	{
		Util.log(s);
	}

	static public void init(int port) throws Exception
	{
		log("WebService init:" + port);
		ServerSocket ss = new ServerSocket(port);
		new Thread(new Server(ss)).start();
	}
	static class Server implements Runnable
	{
		ServerSocket ss;

		Server(ServerSocket ss)
		{
			this.ss = ss;
		}

		public void run()
		{
			while (!ss.isClosed())
			{
				try
				{
					Socket s = ss.accept();
					new Thread(new Request(s)).start();
				}
				catch (IOException e)
				{
					Util.log("Web accept caught ", e);
				}
			}
		}
	}
	static class Request implements Runnable
	{
		Socket s;

		Request(Socket s)
		{
			this.s = s;
		}

		public void run()
		{
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
				String requestLine = in.readLine().trim();
				String request[] = requestLine.split(" ");
				if (!request[0].equalsIgnoreCase("GET")) throw new RuntimeException("Web bad request type:" + requestLine);
				String parts[] = request[1].split("/");
				if (!parts[1].equalsIgnoreCase("WebView")) throw new RuntimeException("Web bad request type:" + requestLine);
				// Long studyID = Long.parseLong(parts[2]);
				// InputStream is = SSStore.get().getRawInputStream(studyID,
				// "");
				for (int i = 0; i < parts.length; i++)
					log("" + i + ":" + parts[i]);
				String str = ";";
				while (!str.equals(""))
				{
					log(str);
					str = in.readLine();
				}
				if (parts.length == 3)
				{
					bos.write("HTTP/1.0 200 OK\r\n".getBytes());
					bos.write("Content-Type: text/plain\r\n".getBytes());
					bos.write("Server: Bot\r\n".getBytes());
					bos.write("\r\n".getBytes());
					// study:title:seriesCount
					// series:title:imageCount
					// image:path
					Study study = StudyView.get().selectByID(Long.parseLong(parts[2]));
					File studyDir = SSStore.get().getStudyDir(study.getStudyID());
					String path = studyDir.getParentFile().getName() + "/" + studyDir.getName();
					List seriesList = SeriesView.get().selectByStudy(study.getStudyID());
					bos.write(("study:" + study.getModalities() + " " + study.getDescription() + ":" + seriesList.size()).getBytes());
					bos.write("\r\n".getBytes());
					for (Iterator iter = seriesList.iterator(); iter.hasNext();)
					{
						Series series = (Series) iter.next();
						List images = ImageView.get().selectBySeries(series.getSeriesID());
						bos.write(("series:" + series.getSeriesNumber() + " " + series.getDescription() + ":" + images.size()).getBytes());
						bos.write("\r\n".getBytes());
						for (Iterator iter2 = images.iterator(); iter2.hasNext();)
						{
							Image image = (Image) iter2.next();
							bos.write(("image:" + path + "/" + image.getImageID() + ".jpg").getBytes());
							bos.write("\r\n".getBytes());
						}
					}
				} else
				{
					File root = SSStore.get().getRootDir();
					bos.write("HTTP/1.0 200 OK\r\n".getBytes());
					bos.write("Content-Type: image/jpeg\r\n".getBytes());
					bos.write("Server: Bot\r\n".getBytes());
					bos.write("\r\n".getBytes());
					File file = new File(root, parts[2] + "/" + parts[3] + "/" + parts[4]);
					FileInputStream fis = new FileInputStream(file);
					Util.copyStream(fis, bos);
					fis.close();
				}
				bos.flush();
				bos.close();
			}
			catch (IOException e)
			{
				Util.log("WebRequest caught ", e);
			}
			finally
			{
				Util.safeClose(s);
			}
		}
	}

	static List getURLText(URL url)
	{
		ArrayList al = new ArrayList();
		BufferedReader rd = null;
		try
		{
			URLConnection conn = url.openConnection();
			// conn.setDoOutput(true);
			// OutputStreamWriter wr = new
			// OutputStreamWriter(conn.getOutputStream());
			// wr.write(data);
			// wr.flush();
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			boolean started = false;
			while ((line = rd.readLine()) != null)
			{
				line = line.trim();
				// if (started) {
				al.add(line);
				// }
				// if (line.length() == 0)
				// started = true;
			}
		}
		catch (Exception e)
		{
			Util.log("", e);
		}
		finally
		{
			Util.safeClose(rd);
		}
		return al;
	}
	static class StudyInfo
	{
		String name;
		SeriesInfo series[];
	}
	static class SeriesInfo
	{
		String name;
		ImageInfo images[];
	}
	static class ImageInfo
	{
		String path;
	}

	ImageIcon getImageIcon()
	{
		try
		{
			ImageIcon iii = new ImageIcon(new URL("http://127.0.0.1:8888/WebView/20060327/1143515235328/1143515235531.jpg"));
			return iii;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}

	StudyInfo getStudyInfo()
	{
		try
		{
			StudyInfo si = null;
			SeriesInfo ssi = null;
			ImageInfo ii = null;
			int sc = 0;
			int ic = 0;
			List l = getURLText(new URL("http://localhost:8888/WebView/1143515235328"));
			for (int i = 0; i < l.size(); i++)
			{
				String line = (String) l.get(i);
				String s[] = line.split(":");
				if (s[0].equals("study"))
				{
					si = new StudyInfo();
					si.name = s[1];
					si.series = new SeriesInfo[Integer.parseInt(s[2])];
				} else if (s[0].equals("series"))
				{
					ssi = new SeriesInfo();
					ssi.name = s[1];
					ssi.images = new ImageInfo[Integer.parseInt(s[2])];
					si.series[sc++] = ssi;
					ic = 0;
				} else if (s[0].equals("image"))
				{
					ii = new ImageInfo();
					ii.path = s[1];
					ssi.images[ic++] = ii;
				} else
				{
					throw new RuntimeException("Could not read study data!");
				}
			}
			return si;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			ImageIcon iii = new ImageIcon(new URL("http://127.0.0.1:8888/WebView/20060327/1143515235328/1143515235531.jpg"));
			// 20060327/1143515235328/1143515235531.jpg
			StudyInfo si = null;
			SeriesInfo ssi = null;
			ImageInfo ii = null;
			int sc = 0;
			int ic = 0;
			List l = getURLText(new URL("http://localhost:8888/WebView/1143515235328"));
			for (int i = 0; i < l.size(); i++)
			{
				String line = (String) l.get(i);
				String s[] = line.split(":");
				if (s[0].equals("study"))
				{
					si = new StudyInfo();
					si.name = s[1];
					si.series = new SeriesInfo[Integer.parseInt(s[2])];
				} else if (s[0].equals("series"))
				{
					ssi = new SeriesInfo();
					ssi.name = s[1];
					ssi.images = new ImageInfo[Integer.parseInt(s[2])];
					si.series[sc++] = ssi;
					ic = 0;
				} else if (s[0].equals("image"))
				{
					ii = new ImageInfo();
					ii.path = s[1];
					ssi.images[ic++] = ii;
				} else
				{
					throw new RuntimeException("Could not read study data!");
				}
			}
			// init(8080);
			Util.sleep(1000);
			// Image i = Toolkit.getDefaultToolkit().getImage(new
			// URL("http://localhost:8080/WebView/xxx/1143515235531.jpg"));
			// log(""+i);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}
	// public static final File entry_dir =
	// new File("/");
	//
	// public WebService(Context context)
	// {
	// super(context);
	// }
	//
	// public void process(Request req, Response resp) throws Exception
	// {
	// PrintStream out = resp.getPrintStream();
	// out.println("<html><body><ul>");
	// File[] entries = entry_dir.listFiles();
	// for (int i = 0; i < entries.length; i++)
	// {
	// out.println("<li>" + entries[i].getName() + "</li>");
	// }
	// out.println("</ul></body></html>");
	// out.close();
	// }
}