package net.metafusion.localstore.tasks;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.importer.DicomConverter;
import net.metafusion.importer.ImageImportHeader;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudySeriesImage;
import net.metafusion.model.PatientStudySeriesImageView;
import net.metafusion.model.Series;
import net.metafusion.model.Study;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.storage.SSStore;
import acme.util.FileUtil;

public class FixMissingMdf extends Task
{
	private static int				numMissingFiles	= 0;
	private static int				totalFiles		= 0;
	private static ArrayList<File>	studies;
	private static boolean			studyEntered	= false;
	private static File				rootDir			= new File("/data/mdf/");
	private static String			degradeMessage	= "WARNING!!!! Degraded image.";

	public FixMissingMdf()
	{
		studies = new ArrayList<File>();
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (f.isDirectory())
			{
				studyEntered = false;
				return;
			}
			if (!f.getName().endsWith(".jpg"))
				return;
			totalFiles++;
			if (totalFiles % 1000 == 0)
			{
				log("Total files checked: " + totalFiles);
			}
			File mdfFile = new File(f.getAbsolutePath().replace(".jpg", ".mdf"));
			if (!mdfFile.exists())
			{
				if (!studyEntered)
				{
					studies.add(mdfFile);
					studyEntered = true;
				}
				numMissingFiles++;
				try
				{
					List pl = PatientStudySeriesImageView.get().selectWhere("imageid=" + Long.parseLong(f.getName().substring(0, f.getName().length() - 4)));
					if (pl.size() > 0)
					{
						// the image
						PatientStudySeriesImage pssi = (PatientStudySeriesImage) pl.get(0);
						Image image = pssi.getImage();
						Patient patient = pssi.getPatient();
						Series series = pssi.getSeries();
						Study study = pssi.getStudy();
						ImageImportHeader h = new ImageImportHeader();
						// Fill the header, Image info first
						h.setFilename(f.getAbsolutePath());
						h.setSOPClassUID(pssi.getImage().classUID);
						h.setSOPInstanceUID(pssi.getImage().imageUID);
						h.setSourceApplicationEntityTitle(pssi.getStudy().originAET);
						h.setAcquisitionDate(new Date(image.getDateEntered().getTime()));
						h.setAcquisitionTime(image.getDateEntered());
						h.setInstanceNumber(Integer.decode(image.instanceNumber.trim()));
						h.setImageComments(degradeMessage);
						// Series next
						h.setModality(series.modality);
						h.setStationName(series.stationName);
						h.setSeriesInstanceUID(series.getSeriesUID());
						h.setSeriesNumber(Integer.decode(series.seriesNumber.trim()));
						// Now Study info
						h.setInstitutionName(study.institutionName);
						h.setStudyTime(new Timestamp(study.date.getTime()));
						h.setAccessionNumber(study.accessionNumber);
						h.setReferringPhysicianName(study.referringPhysicianName);
						h.setStudyDate(study.date);
						h.setStudyInstanceUID(study.studyUID);
						// Now Patient info
						h.setPatientName(patient.dicomName);
						h.setPatientBirthDate(patient.dob);
						h.setPatientSex(patient.sex);
						h.setPatientID(patient.extID);
						DS ds = DicomConverter.convertJpegToDicom(h);
						File tmpDir = new File(rootDir, new Long(study.studyID).toString());
						if (!tmpDir.exists())
							tmpDir.mkdir();
						File dcmFile = new File(tmpDir, mdfFile.getName().replace(".mdf", ".dcm"));
						dcmFile.createNewFile();
						DSOutputStream.writeDicomFile(ds, dcmFile);
						log(dcmFile.getAbsolutePath());
					}
					else
					{
						log("Found JPEG, no entry in DB: " + f);
					}
				}
				catch (Exception e)
				{
					log(e.getMessage());
				}
			}
		}
	}

	@Override
	public void run()
	{
		File srcDir = null;
		for (int i = 2; i < cmdArgs.length; i++)
		{
			int mark = cmdArgs[i].indexOf('=');
			if (mark == -1)
				printUsage();
			String option = cmdArgs[i].substring(0, mark);
			String value = cmdArgs[i].substring(mark + 1);
			if (option.equalsIgnoreCase("src"))
			{
				srcDir = new File(value);
				if (!srcDir.exists())
				{
					log("Source directory does not exist: " + value);
					System.exit(1);
				}
			}
			else if (option.equalsIgnoreCase("dest"))
			{
				rootDir = new File(value);
			}
			else if (option.equalsIgnoreCase("msg"))
			{
				degradeMessage = value;
			}
			else
				printUsage();
		}
		initLogs("FixMissingMdf", false);
		if (srcDir == null)
			printUsage();
		if (!rootDir.exists())
		{
			rootDir.mkdirs();
		}
		FileUtil.forEachFile(srcDir, true, true, true, new FR());
	}

	protected void printUsage()
	{
		System.out.println("Usage: FixMissingMdf <path to metafusion.xml> <AE title> src=<directory to search> dest=<output directory> msg=<message to use in image comments>");
		System.out.println("Default dest=" + rootDir);
		System.out.println("Default message=" + degradeMessage);
		System.exit(1);
	}

	public static void main(String[] args)
	{
		start(args, FixMissingMdf.class);
	}
}
