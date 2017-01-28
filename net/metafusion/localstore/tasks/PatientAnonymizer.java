package net.metafusion.localstore.tasks;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudySeries;
import net.metafusion.model.PatientStudySeriesView;
import net.metafusion.model.PatientView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomDir;
import net.metafusion.util.ImageMetaInfo;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.db.JDBCUtil;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.StringUtil;
import acme.util.Util;

public class PatientAnonymizer extends Task
{
	public PatientAnonymizer()
	{
	}

	@Override
	public void run()
	{
		boolean doPatients = false;
		boolean doInstitution = false;
		boolean doMdf = false;
		boolean doZip = false;
		// 1) Modify the name in the dcm_Patient table
		// 2) Change the name of Facility to Metafusion Imaging Center
		// Image North = Ficticious Imaging Center
		// Hospital = Metafusion Outpatient Center
		// Blank name = Ficticious Imaging Center
		// 3) modify the name on the DICOM header of EACH IMAGE (.mdf)
		// 4) regenerate the ZIP file for each study - CreateDicomDirZipForWeb
		if (cmdArgs.length > 2)
		{
			if (cmdArgs[2].equalsIgnoreCase("true"))
				doPatients = true;
		}
		if (cmdArgs.length > 3)
		{
			if (cmdArgs[3].equalsIgnoreCase("true"))
				doInstitution = true;
		}
		if (cmdArgs.length > 4)
		{
			if (cmdArgs[4].equalsIgnoreCase("true"))
				doMdf = true;
		}
		if (cmdArgs.length > 5)
		{
			if (cmdArgs[5].equalsIgnoreCase("true"))
				doZip = true;
		}
		if (doPatients)
		{
			PatientView pv = PatientView.get();
			List pl = pv.selectAll();
			for (int i = 0; i < pl.size(); i++)
			{
				Patient p = (Patient) pl.get(i);
				char[] cha = p.lastName.toCharArray();
				for (int j = 0; j < cha.length; j++)
				{
					if (Character.isLetter(cha[j]))
					{
						switch (cha[j])
						{
							case 'a':
								cha[j] = 'e';
								break;
							case 'e':
								cha[j] = 'i';
								break;
							case 'i':
								cha[j] = 'o';
								break;
							case 'o':
								cha[j] = 'u';
								break;
							case 'u':
								cha[j] = 'a';
								break;
							case 'A':
								cha[j] = 'E';
								break;
							case 'E':
								cha[j] = 'I';
								break;
							case 'I':
								cha[j] = 'O';
								break;
							case 'O':
								cha[j] = 'U';
								break;
							case 'U':
								cha[j] = 'A';
								break;
							case 'z':
								cha[j] = 'a';
								break;
							case 'Z':
								cha[j] = 'A';
								break;
							default:
								cha[j]++;
								break;
						}
					}
				}
				p.lastName = new String(cha);
				p.dicomName = p.lastName + "," + p.firstName;
				PatientView.get().update(p);
			}
			System.out.println("Finished modified Patient name.");
		}
		else
		{
			System.out.println("Skipped patient update, due to command line flag");
		}
		if (doInstitution)
		{
			System.out.println("Starting update to institutionNames");
			JDBCUtil
					.get()
					.update(
							"Update dcm_series set institutionName='Ficticious Imaging Center' where institutionName LIKE '%North%' or institutionName LIKE '%NORTH%' or institutionName LIKE '%Great%' or institutionName LIKE '%GREAT%' or institutionName is null or institutionName =''");
			JDBCUtil.get().update(
					"Update dcm_series set institutionName='Metafusion Outpatient Center' where institutionName LIKE '%Hospital%' or institutionName LIKE '%HOSPITAL%'");
			JDBCUtil
					.get()
					.update(
							"Update dcm_study set institutionName='Ficticious Imaging Center' where institutionName LIKE '%North%' or institutionName LIKE '%NORTH%' or institutionName LIKE '%Great%' or institutionName LIKE '%GREAT%' or institutionName is null or institutionName =''");
			JDBCUtil.get().update(
					"Update dcm_study set institutionName='Metafusion Outpatient Center' where institutionName LIKE '%Hospital%' or institutionName LIKE '%HOSPITAL%'");
			System.out.println("Finished insitutions.");
		}
		else
			System.out.println("Skipping institution update due to command line flag");
		if (doMdf)
		{
			System.out.println("Starting MDF update");
			DicomStore ds = DicomStore.get();
			SSStore ss = ds.getSSStore();
			SSInputStream imageStream = null;
			DS imageDS = null;
			File dcmFile = null;
			System.out.println("Begin Image query");
			FileUtil.forEachFile(ds.getSSStore().getRootDir(), true, false, true, new FR());
		}
		else
			System.out.println("Skipping MDF update due to command line flag");
		if (doZip)
		{
			System.out.println("Starting update to ZIP files");
			List sv = StudyView.get().selectAll();
			for (int i = 0; i < sv.size(); i++)
			{
				Study study = (Study) sv.get(i);
				File f = DicomDir.CreateDicomDirZipForWeb(SSStore.get().getStudyDir(study.getStudyID()), study);
				String update = "update web_study set studypath = '" + f.getAbsolutePath() + "' " + " where dcm_studyid = " + study.getStudyID();
				update = StringUtil.replaceAll(update, "\\", "\\\\");
				JDBCUtil.get().update(update);
			}
		}
		else
			System.out.println("Skipping ZIP update due to command line flag");
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (!f.getName().endsWith(".mdf"))
				return;
			DicomStore ds = DicomStore.get();
			SSStore ss = ds.getSSStore();
			SSInputStream imageStream = null;
			DS imageDS = null;
			File tempFile = null;
			OutputStream os = null;
			try
			{
				imageStream = new SSInputStream(f);
				ImageMetaInfo imi = (ImageMetaInfo) imageStream.getMeta();
				UID syntax = UID.get(imi.getTransferSyntax());
				imageDS = DSInputStream.readFrom(imageStream, syntax, true);
				imi.setMediaStorageSOPClassUID(imageDS.getString(Tag.MediaStorageSOPClassUID));
				imi.setMediaStorageSOPInstanceUID(imageDS.getString(Tag.MediaStorageSOPInstanceUID));
				imi.setTransferSyntax(UID.ImplicitVRLittleEndian.getUID()); // imvrle
				List pl = PatientStudySeriesView.get().selectWhere("seriesid=" + imi.getSeriesID());
				if (pl != null)
				{
					PatientStudySeries pi = (PatientStudySeries) pl.get(0);
					imageDS.putString(Tag.PatientName, pi.getPatient().dicomName);
					imageDS.putString(Tag.InstitutionName, pi.getSeries().institutionName);
					tempFile = ss.createTempFile(".dcmimp");
					os = SSStore.get().getOutputStream(tempFile);
					imageDS.writeTo(os, UID.ImplicitVRLittleEndian);
					File fullFile = ss.putMetaFile(imi.getStudyID(), imi.getImageID(), imi, tempFile);
				}
			}
			catch (Exception e)
			{
				System.out.println("ERROR: " + e.getMessage());
			}
			finally
			{
				Util.safeClose(os);
				Util.safeClose(imageStream);
				Util.safeDelete(tempFile);
			}
		}
	}

	public static void main(String[] args)
	{
		System.out.println("PatientAnonymizer starting...");
		start(args, PatientAnonymizer.class);
	}
}
