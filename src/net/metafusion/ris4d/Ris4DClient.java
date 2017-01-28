package net.metafusion.ris4d;

import integration.MFInputStream;
import integration.MFOutputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import net.metafusion.util.Tag;

import acme.util.Util;

public class Ris4DClient
{
	// String sourceAE = "RandyAE";
	static String	sourceAE	= "gregRemote";
	static String	destAE		= "greg";
	static String	host		= "localhost";
	static int		port		= 4010;

	String ictkTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "iCtk;c:\\temp\\test\\sample_medicare_input.txt;\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		return returnString;
	}

	String demoTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String info = "PatientName=CT^9;PatientID=ct9;PatientBirthDate=19720930;PatientSex=F;StudyDescription=Description;ReferringPhysicianName=Randy Lexvold;AccessionNumber=1;StudyDate=20090915;SourceApplicationEntityTitle=Greg;InstitutionName=Hospital;";
		String cmd = "loadDemoStudies;CT;3;" + info + "\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		return returnString;
	}

	void newFindTest(MFOutputStream os, MFInputStream is) throws Exception
	{
		String cmd = "cfind;" + sourceAE + ";lastname=Hazari,firstname=cyrus;studyuid, firstname, lastname\n";
		os.write(cmd.getBytes());
		byte[] b = new byte[1024];
		is.readFully(b);
	}

	String archiveTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String studyList = "1.2.392.200036.9116.2.6.1.48.1215677836.1201132148.9391";
		String cmd = "async;archiveStudies;Bonn;600000000;" + studyList + "\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		return returnString;
	}

	String newMoveTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String studyList = "1.2.392.200036.9116.2.6.1.48.1215677011.1201130109.37332,1.2.392.200036.9116.2.6.1.48.1215677836.1201132148.9391";
		String cmd = "async;cmove;greg;OSIRIX_RANDY;" + studyList + "\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		return returnString;
	}

	void sendStudy(String ae, MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "sendStudy;1.2.826.0.1.3680043.2.712.1254947321833;" + ae + "\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
	}

	void pdfTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String dir = "/Users/rlexvold/Personal/Lexicon/Cyrus/Samples/tmp/";
		String cmd = "createReportPDF;" + dir + "test.rtf," + dir + "2.pdf;" + dir + "out.pdf\n";
		System.out.println("RIS Command: " + cmd);
		os.write(cmd.getBytes());
	}

	void getLocalTimeTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "getLocalTime\n";
		os.write(cmd.getBytes());
		byte[] b = new byte[1024];
		System.out.println("LocalTime: " + is.readLine());
	}

	void patientCleanupTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "patientCleanup;randyRis;studyuid: 1.2.840.113619.2.134.1762872355.2791.1103295873.519, 1.2.840.113619.2.134.1762872355.2791.1103295873.519\n";
		os.write(cmd.getBytes());
		System.out.println("patientCleanup: " + is.readLine());
	}

	void hicca(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "hicca;,;/Users/rlexvold/Desktop/test.csv\n";
		os.write(cmd.getBytes());
		System.out.println("patientCleanup: " + is.readLine());
	}

	String putMediaDirTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "async;putRichMedia;1.2.840.113619.2.5.1762872355.1931.1082383066.137;/Users/rlexvold/Personal/Lexicon/Cyrus/Samples/1.2.840.113619.2.134.1762854322.2078.1117204425.432/1.2.840.113619.2.134.1762854322.2078.1117204425.433\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println("rsync: " + returnString);
		return returnString;
	}

	String putMediaFileTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "async;putRichMediaFile;1.2.840.113619.2.5.1762872355.1931.1082383066.137;/Volumes/MacBack/movies/Bobby.mp4\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println("rsync: " + returnString);
		return returnString;
	}

	String getMediaDirTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "async;getRichMedia;1.2.840.113619.2.5.1762872355.1931.1082383066.137;/Users/rlexvold/Desktop\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println("rsync: " + returnString);
		return returnString;
	}

	String getMediaFileTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "getRichMediaFile;1.2.840.113619.2.5.1762872355.1931.1082383066.137;1.2.840.113619.2.134.1762854322.2078.1117204425.434.dcm;/Users/rlexvold/Desktop/getfile\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println("rsync: " + returnString);
		return returnString;
	}

	void osirixTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "osirixOpenStudyByAccession;18031\n";
		os.write(cmd.getBytes());
		System.out.println("osirix: " + is.readLine());
	}

	String thumbnailTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "createThumbnailsByStudyUID;1.2.840.113619.2.5.1762872355.1931.1082383066.137;/Users/rlexvold/Desktop/output\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println(returnString);
		return returnString;
	}

	String deleteStudyTest(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "deleteStudy;1.2.840.113619.2.134.1762854322.2078.1117204425.432\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println(returnString);
		return returnString;
	}

	String mergeStudies(MFOutputStream os, BufferedReader is) throws Exception
	{
		String cmd = "mergeStudies;1.2.392.200036.9116.2.6.1.48.1215677836.1201132148.9391;1.2.392.200036.9116.2.6.1.48.1215677011.1201130109.37332;randy;2008.12.12\n";
		os.write(cmd.getBytes());
		String returnString = is.readLine();
		System.out.println(returnString);
		return returnString;
	}

	String checkStatus(MFOutputStream os, BufferedReader is, String returnString) throws Exception
	{
		if (returnString.contains("ok;"))
		{
			String id = returnString.substring(returnString.indexOf(';') + 1, returnString.length());
			String cmd = "checkAsyncTaskStatus;" + id + "\n";
			os.write(cmd.getBytes());
			returnString = is.readLine();
			System.out.println(returnString);
		}
		return returnString;
	}

	public static void main(String[] args)
	{
		Ris4DClient c = new Ris4DClient();
		boolean async = false;
		String list = null;
		String tmp = null;
		if (args.length > 0)
			sourceAE = args[0];
		if (args.length > 1)
			destAE = args[1];
		try
		{
			long time = System.currentTimeMillis();
			Socket s = new Socket(host, port);
			BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
			MFOutputStream os = new MFOutputStream((s.getOutputStream()));
			if (!async)
			{
				String result = c.ictkTest(os, is);
				System.out.println(result);
			}
			if (async)
			{
				for (;;)
				{
					s = new Socket(host, port);
					is = new BufferedReader(new InputStreamReader(s.getInputStream()));
					os = new MFOutputStream((s.getOutputStream()));
					Util.sleep(1000);
					String result = c.checkStatus(os, is, tmp);
					s.close();
					if (!result.contains("running"))
						break;
				}
			}
			time = System.currentTimeMillis() - time;
			System.out.println("ms = " + time);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}