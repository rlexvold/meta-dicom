package net.metafusion.localstore.tasks;

import java.text.DateFormat;
import java.util.Date;

import net.metafusion.pacs.PacsFactory;
import net.metafusion.util.DicomUtil;
import acme.util.Log;

public class StudyMerge extends Task
{
	@Override
	public void run()
	{
		Log.setVerbose(0);
		initLogs("StudyMerge", true);
		if (cmdArgs.length != 6)
		{
			printUsage();
		}
		try
		{
			Date tmp = DicomUtil.parseDate(cmdArgs[5]);
			PacsFactory.getPacsInterface().mergeStudies(cmdArgs[2], cmdArgs[3], cmdArgs[4], tmp);
		}
		catch (Exception e)
		{
			log("ERROR: " + e.getMessage());
		}
	}

	protected void printUsage()
	{
		System.out
				.println("Usage: StudyMerge <path to metafusion.xml> <AE title> <StudyUID for patient information> <StudyUID for study information> <Accession> <StudyDate (MM-DD-YYYY)");
		System.exit(1);
	}

	public static void main(String[] args)
	{
		start(args, StudyMerge.class);
	}
}
