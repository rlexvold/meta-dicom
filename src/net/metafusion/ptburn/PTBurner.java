package net.metafusion.ptburn;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import acme.util.StringUtil;
import acme.util.Util;

public class PTBurner
{
	File root = new File("c:/ptburn");
	File incoming = new File(root, "incoming");
	File inprocess = new File(root, "inprocess");
	File simulator = new File(root, "simulator");
	File completed = new File(root, "completed");
	File temp = new File(root, "temp");
	IniFile ini;

	public PTBurner()
	{
		ini = new IniFile(new File(root, "test.ini"));
	}

	String createJobName()
	{
		Calendar c = Calendar.getInstance();
		String year = StringUtil.int4(c.get(Calendar.YEAR));
		String month = StringUtil.int4(c.get(Calendar.MONTH));
		String day = StringUtil.int4(c.get(Calendar.DAY_OF_MONTH));
		String hour = StringUtil.int4(c.get(Calendar.HOUR_OF_DAY));
		String min = StringUtil.int4(c.get(Calendar.MINUTE));
		return year + month + day + hour + min;
	}

	void addLocalJob(File dataRoot)
	{
		String jobName = createJobName();
		String fileName = jobName + ".JRQ";
		int append = 0;
		for (;;)
		{
			if (new File(incoming, fileName).exists() || new File(inprocess, fileName).exists() || new File(completed, fileName).exists())
				;
			else break;
			fileName = jobName + "-" + (append++) + ".JRQ";
		}
		File jobFile = new File(incoming, fileName);
		IniFile ini = new IniFile();
		ini.put("DiscType", "CDR"); // CDR, DVDR
		ini.put("Data", dataRoot.getAbsolutePath()); // pwd
		ini.put("DeleteFiles", "NO"); // YES, NO
		ini.put("VerifyDisc", "YES"); // YES, NO
		ini.put("PrintLabel", "C:\foo\bar.bmp"); // YES, NO
	}

	void idle()
	{
	}
	ArrayList<Job> jobList = new ArrayList<Job>();
	class Job
	{
		Job(String name, int size)
		{
			this.name = name;
			this.size = size;
		}
		String name;
		IniFile ini = new IniFile();
		int size;
	}

	void addJob(String name, int size)
	{
		Job j = new Job(name, size);
		jobList.add(j);
		j.ini.put("DiscType", "CDR"); // CDR, DVDR
		j.ini.put("Data", "C:\foo\bar"); // pwd
		j.ini.put("DeleteFiles", "NO"); // YES, NO
		j.ini.put("VerifyDisc", "YES"); // YES, NO
		j.ini.put("PrintLabel", "C:\foo\bar.bmp"); // YES, NO
		// PTSTATUS.TXT:
		//
		// [JobList]
		// Job0 = MyJob1
		// Job1 = YourJob2
		// Job2 = MyJob2
		//
		// [CompletedJobs]
		// Job0 = AnotherJob3
		//
		//
		// [MyJob1]
		// JobID = Kevin’s photos
		// ClientID = Kevin
		// CurrentStatus = Waiting
		// JobsAhead = 1
		// DiscsAhead = 3
		// DiscsRemaining = 10
		// GoodDiscs = 0
		// BadDiscs = 0
		// TimeCreated =
		//
		// JobErrorNumber =0
		// JobErrorString= No Errors
	}
	static Calendar c = Calendar.getInstance();
	public static File ptRoot = new File("c:\\metafusion");
	public static File ptStatusFile = new File("c:\\metafusion\\PTSTATUS.TXT");

	static File createJobFile()
	{
		String base = "MF" + c.get(Calendar.YEAR) + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH));
		int count = 1;
		File f = new File(ptRoot, base + ".JRQ");
		for (;;)
		{
			if (!f.exists()) break;
			f = new File(ptRoot, base + "-" + (count++) + ".JRQ");
		}
		return f;
	}

	static String handleGetSection(String section)
	{
		StringBuffer sb = new StringBuffer();
		String jobs[] = new IniFile(ptStatusFile).getSection(section);
		if (jobs == null) return "err;not found\n";
		sb.append("ok;" + jobs.length + "\n");
		for (int i = 0; i < jobs.length; i++)
			sb.append(jobs[i] + "\n");
		return sb.toString();
	}

	static String handleGetSectionKey(String section, String key)
	{
		String value = new IniFile(ptStatusFile).get(section, key);
		if (value == null) return "err;not found\n";
		return "ok;" + value + "\n";
	}

	public static String handlePtCommand(String command) throws Exception
	{
		String args[] = command.trim().split(";");
		if (args[0].trim().equals("ptBurn"))
		{
			File f = createJobFile();
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(f);
				for (int i = 1; i < args.length; i++)
					fos.write((args[i] + "\r\n").getBytes());
			}
			finally
			{
				Util.safeClose(fos);
			}
			Util.log("burn " + f.getName());
			Util.log(new String(Util.readWholeFile(f)));
			return "ok;" + f.getName() + "\n";
		}
		if (args[0].trim().equals("ptJobList")) { return handleGetSection("JobList"); }
		if (args[0].trim().equals("ptCompletedJobs")) { return handleGetSection("CompletedJobs"); }
		if (args[0].trim().equals("ptGetStatus"))
		{
			if (args.length == 2)
				return handleGetSection(args[1].trim());
			else return handleGetSectionKey(args[1].trim(), args[2].trim());
		}
		if (args[0].trim().equals("ptAbort"))
		{
			File f = new File(ptRoot, args[1].trim() + ".PTM");
			Util.writeFile(("Message = ABORT\r\nClientID = " + args[2].trim() + "\r\n").getBytes(), f);
			Util.log("abort " + f.getName());
			Util.log(new String(Util.readWholeFile(f)));
			return "ok\n";
		}
		return "err;unknown command\n";
	}

	/*
	 * >downloadStudies;uid;1.2.3;1.2.4;1.2.5\n <ok;dicdomdirpath\n
	 * 
	 * >downloadStudies;date;10112005;10122005\n <ok:dicomdirpath\n
	 * 
	 * >ptBurn;Data=path\n see pg 14 for examples pg 3 for complete list of
	 * options <ok;MF102305-1\n
	 * 
	 * >ptJobList\n see pg 23 for example list <ok;1\n <Job0=MF102305-1\n
	 * 
	 * >ptCompletedJobs\n see pg 23 for example list <ok;1\n <Job0=MF102305-1\n
	 * 
	 * >ptGetStatus;System\n [ or job name ] see page 23-35 for example <ok;3\n
	 * <blah = blah blah\n <blah = blah blah\n <blah = blah blah\n
	 * 
	 * >ptGetStatus;MF102305-1;CurrentStatus\n see page 23-35 for example
	 * <ok;Waiting\n
	 * 
	 * 
	 * >ptAbort;MF100305;ClientID\n see page 25 for example abort file <ok;\n
	 * 
	 */
	static String test(String s) throws Exception
	{
		Util.log(">" + s);
		String r = handlePtCommand(s);
		Util.log("<" + r);
		return r;
	}

	public static void main(String[] args) throws Exception
	{
		test("ptJobList\n");
		test("ptCompletedJobs\n");
		test("ptGetStatus;System\n");
		test("ptGetStatus;System;DriveSpace\n");
		test("ptGetStatus;MyJob1;CurrentStatus\n");
		test("ptBurn;Data = path;BurnSpeed = 8;Copies = 2\n");
	}
}