package net.metafusion.medicare.hic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import acme.util.CSVFile;
import acme.util.Log;
import au.gov.hic.hiconline.client.api.EasyclaimAPI;

public class HICCA
{
	static EasyclaimAPI		ec;
	int						sess;
	HashMap					cmdMap		= new HashMap();
	private static HICCA	instance	= null;

	public static HICCA getInstance()
	{
		return instance;
	}

	public static void init(String lpdir, String jlpath)
	{
		instance = new HICCA(lpdir, jlpath);
	}

	private HICCA(String lpdir, String jlpath)
	{
		Log.log("HICCA init EasyClaim version=" + EasyclaimAPI.getVersionId());
		Log.log("java.class.path=" + System.getProperty("java.class.path"));
		Log.log("hiconline.logicpack.dir=" + lpdir);
		Log.log("java.library.path=" + jlpath);
		System.setProperty("hiconline.logicpack.dir", lpdir);
		System.setProperty("java.library.path", jlpath);
		Log.log("loading EasyClaim Instance");
		ec = EasyclaimAPI.getInstance();
	}
	class Command
	{
		String	name;

		String gets()
		{
			try
			{
				String s = "";
				if (col < csv.getNumCol(row))
					s = csv.getCell(row, col).trim();
				if (s.startsWith("="))
				{
					int num = -1;
					try
					{
						num = Integer.parseInt(s.substring(1));
					}
					catch (Exception e)
					{
						Log.log("parse = caught " + e);
						return s;
					}
					String m = csv.getCell(num, 2);
					m = m != null ? m : "";
					Log.log("mapping " + s + " to " + m);
					return m;
				}
				return s;
			}
			finally
			{
				col++;
			}
		}

		Vector getv()
		{
			v = new Vector();
			return v;
		}

		Command(String name)
		{
			this.name = name;
			cmdMap.put(name.toLowerCase(), this);
		}

		// note parse createbo specially, and sess call
		int exec()
		{
			return -1;
		}

		String getName()
		{
			return name;
		}

		void post()
		{
		}
	}

	void parseFile(CSVFile csv)
	{
		try
		{
			this.csv = csv;
			sess = -1;
			Log.log("parsing csv file...");
			Log.log("skipping first two lines...");
			for (row = 2; row < csv.getNumRow(); row++)
			{
				parseRow(row);
			}
		}
		finally
		{
			// Log.log("destroySessionEasyclaim "+sess);
			// ec.destroySessionEasyclaim(sess, 1);
		}
	}

	Command find(String name)
	{
		return (Command) cmdMap.get(name.toLowerCase());
	}

	void parseRow(int row)
	{
		StringBuffer sb = new StringBuffer("parse row [" + row + "]: ");
		for (int j = 0; j < csv.getNumCol(row); j++)
		{
			sb.append(csv.getCell(row, j));
			if (j < csv.getNumCol(row) - 1)
				sb.append(",");
		}
		Log.log("" + sb);
		run(row);
	}
	CSVFile	csv;
	int		row;
	Vector	v;
	int		rv;
	int		col	= 4;

	void run(int row)
	{
		this.row = row;
		this.col = 4;
		rv = -1;
		try
		{
			v = null;
			String name = csv.getCell(row, 0);
			Command c = find(name);
			if (c == null)
				throw new RuntimeException("Unknown Command " + name);
			Log.log("call " + c.getName());
			rv = c.exec();
			Log.log("return = " + rv);
			if (v != null && v.size() > 0)
				Log.log("output =" + v.get(0) + "type=" + v.get(0).getClass());
			c.post();
		}
		catch (Exception e)
		{
			Log.log("caught exception" + e);
		}
		finally
		{
			csv.setCell("" + rv, row, 1);
			if (v != null && v.size() > 0 && v.get(0) != null)
			{
				String s = "" + v.get(0);
				if (s.indexOf('+') != -1)
				{
					Log.log("NOTE: supressing '+' in OUTPUT ===============");
					s = s.replaceAll("\\+", "");
				}
				csv.setCell(s, row, 2);
			}
		}
	}
	static HashSet	sessionSet	= new HashSet();

	static boolean goodSession(int session)
	{
		return session < 1000 || session >= 3000;
	}

	static void trackSession(Integer session)
	{
		if (!goodSession(session.intValue()))
			return;
		if (sessionSet.contains(session))
			Log.log("trackSession: " + session + " is a DUPLICATE!!!!");
		sessionSet.add(session);
	}

	static void releaseSession(Integer session)
	{
		if (!goodSession(session.intValue()))
			return;
		if (!sessionSet.contains(session))
			Log.log("releaseSession: " + session + " does not EXIST!!!!");
		sessionSet.remove(session);
	}

	static void destroyAllSession()
	{
		for (Iterator iter = sessionSet.iterator(); iter.hasNext();)
		{
			int i = ((Integer) iter.next()).intValue();
			try
			{
				ec.destroySessionEasyclaim(i, 1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		sessionSet.clear();
	}
	Command	c[]	= new Command[] { new Command("useSessionEasyclaim")
				{
					int exec()
					{
						return Integer.parseInt(gets());
					}

					void post()
					{
						sess = rv;
					}
				}, new Command("destroySessionEasyclaim")
				{
					int exec()
					{
						int s = Integer.parseInt(gets());
						releaseSession(new Integer(s));
						return ec.destroySessionEasyclaim(s, 1);
					}
				}, new Command("destroyAllSessionEasyclaim")
				{
					int exec()
					{
						destroyAllSession();
						return 0;
					}
				}, new Command("acceptContent")
				{
					int exec()
					{
						return ec.acceptContent(sess, gets());
					}
				}, new Command("addContent")
				{
					int exec()
					{
						return ec.addContent(sess, gets());
					}
				}, new Command("authoriseContent")
				{
					int exec()
					{
						return ec.authoriseContent(sess, gets(), getv());
					}
				}, new Command("cancelBusinessObject")
				{
					int exec()
					{
						return ec.cancelBusinessObject(sess, gets());
					}
				}, new Command("cancelTransmission")
				{
					int exec()
					{
						return ec.cancelTransmission(sess);
					}
				}, new Command("clearReport")
				{
					int exec()
					{
						return ec.clearReport(sess);
					}
				}, new Command("createReport")
				{
					int exec()
					{
						return ec.createReport(sess, gets(), gets());
					}
				}, new Command("createBusinessObject")
				{
					int exec()
					{
						return ec.createBusinessObject(sess, gets(), gets(), gets(), getv());
					}
				}, new Command("createSessionEasyclaim")
				{
					int exec()
					{
						return ec.createSessionEasyclaim(gets(), gets());
					}

					void post()
					{
						sess = rv;
						trackSession(sess);
					}
				}, new Command("createTransmission")
				{
					int exec()
					{
						return ec.createTransmission(sess, gets());
					}
				}, new Command("getBusinessObjectCondition")
				{
					int exec()
					{
						return ec.getBusinessObjectCondition(sess, gets(), gets(), getv());
					}
				}, new Command("getBusinessObjectElement")
				{
					int exec()
					{
						return ec.getBusinessObjectElement(sess, gets(), gets(), getv());
					}
				}, new Command("getContent")
				{
					int exec()
					{
						return ec.getContent(sess, getv());
					}
				}, new Command("getNextReportRow")
				{
					int exec()
					{
						return ec.getNextReportRow(sess);
					}
				}, new Command("getReportElement")
				{
					int exec()
					{
						return ec.getReportElement(sess, gets(), getv());
					}
				}, new Command("getUniqueId")
				{
					int exec()
					{
						return ec.getUniqueId(sess, getv());
					}
				}, new Command("getSessionElement")
				{
					int exec()
					{
						return ec.getSessionElement(sess, gets(), getv());
					}
				}, new Command("getTransmissionElement")
				{
					int exec()
					{
						return ec.getTransmissionElement(sess, gets(), getv());
					}
				}, new Command("includeContent")
				{
					int exec()
					{
						return ec.includeContent(sess);
					}
				}, new Command("isReportAvailable")
				{
					int exec()
					{
						return ec.isReportAvailable(sess);
					}
				}, new Command("resetBusinessObjectElement")
				{
					int exec()
					{
						return ec.resetBusinessObjectElement(sess, gets(), gets());
					}
				}, new Command("resetReport")
				{
					int exec()
					{
						return ec.resetReport(sess);
					}
				}, new Command("resetSession")
				{
					int exec()
					{
						return ec.resetSession(sess);
					}
				}, new Command("sendContent")
				{
					int exec()
					{
						return ec.sendContent(sess, gets(), gets());
					}
				}, new Command("sendTransmission")
				{
					int exec()
					{
						return ec.sendTransmission(sess);
					}
				}, new Command("setBusinessObjectCondition")
				{
					int exec()
					{
						return ec.setBusinessObjectCondition(sess, gets(), gets());
					}
				}, new Command("setBusinessObjectElement")
				{
					int exec()
					{
						return ec.setBusinessObjectElement(sess, gets(), gets(), gets());
					}
					// businessObjectMap
				}, new Command("setSessionElement")
				{
					int exec()
					{
						return ec.setSessionElement(sess, gets(), gets());
					}
				}, new Command("listBusinessObject")
				{
					int exec()
					{
						return ec.listBusinessObject(sess, gets(), gets(), getv());
					}
				}, new Command("loadContent")
				{
					int exec()
					{
						return ec.loadContent(sess, gets(), gets(), gets());
					}
				}, new Command("getNextReport")
				{
					int exec()
					{
						return ec.getNextReport(sess);
					}
				}, new Command("unloadContent")
				{
					int exec()
					{
						return ec.unloadContent(sess, getv());
					}
				}, new Command("unloadReport")
				{
					int exec()
					{
						return ec.unloadReport(sess, getv());
					}
				}, new Command("getErrorText")
				{
					int exec()
					{
						return ec.getErrorText(sess, gets(), getv());
					}
				}, new Command("isSignatureRequired")
				{
					int exec()
					{
						return ec.isSignatureRequired(sess);
					}
				}, new Command("setTransmissionElement")
				{
					int exec()
					{
						return ec.setTransmissionElement(sess, gets(), gets());
					}
				}, };

	public String process(String file, int delimiter)
	{
		try
		{
			Log.log("HIC processing:" + file);
			csv = new CSVFile(file, delimiter);
			parseFile(csv);
			String outfile = file.endsWith(".csv") ? file.substring(0, file.length() - 4) : file;
			outfile += "_out.txt";
			Log.log("storing result to " + outfile);
			csv.store(outfile);
			return outfile;
		}
		catch (Exception e)
		{
			Log.log("HIC processing failed with " + e);
			e.printStackTrace();
		}
		finally
		{
			Log.log("HIC processing completed.");
		}
		return null;
	}

	public HICCA()
	{
	}

	public static void main(String[] args)
	{
		// HICCA.init("C:/HICOnline/LogicPack", "C:/HICOnline/xBin");
		HICCA hc = new HICCA();
		hc.process("/Users/rlexvold/Desktop/temp.csv", 0xca);
	}
}