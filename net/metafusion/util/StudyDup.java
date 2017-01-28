package net.metafusion.util;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.storage.SSInputStream;
import acme.util.Log;
import acme.util.MathUtil;
import acme.util.StringUtil;
import acme.util.Util;

public class StudyDup
{
	private static void log(String s)
	{
		Log.log(s);
	}

	private static void vlog(String s)
	{
		Log.vlog(s);
	}
	static long unique = System.currentTimeMillis();

	public StudyDup(List studyList)
	{
		this.studyList = studyList;
		unique++;
	}
	List studyList;

	private SSInputStream getImageStream(Image image) throws Exception
	{
		return net.metafusion.localstore.DicomStore.get().getImageStream(image);
	}

	String getDate()
	{
		return "" + MathUtil.rand(2000, 2005) + StringUtil.int2(MathUtil.rand(1, 12)) + StringUtil.int2(MathUtil.rand(1, 28));
	}

	String randString(int len)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++)
			sb.append((char) MathUtil.rand('a', 'z'));
		return sb.toString();
	}

	void tweak(DS ds, Tag t)
	{
		String s = ds.getString(t);
		if (s != null) ds.putString(t, randString(s.length()));
	}
	HashMap replaceMap = new HashMap();
	boolean anonymize = false;

	void setup()
	{
		replaceMap.clear();
		replaceMap.put(Tag.InstitutionName, "Institution");
		replaceMap.put(Tag.ReferringPhysicianName, "Referring Physician");
		replaceMap.put(Tag.NameOfPhysicianReadingStudy, "Radiologist");
		replaceMap.put(Tag.OperatorName, "Operator");
		replaceMap.put(Tag.PerformingPhysicianName, "Performing Physician");
		replaceMap.put(Tag.ReferringPhysicianAddress, "Address");
		replaceMap.put(Tag.StationName, "Station");
		replaceMap.put(Tag.InstitutionalDepartmentName, "Department");
		String num = "" + MathUtil.rand(10000, 99999);
		replaceMap.put(Tag.PatientName, "Last,F" + num);
		replaceMap.put(Tag.PatientID, "ID" + num);
	}

	void fixup(DS ds, Tag t)
	{
		if (ds.contains(t))
			ds.put(t, ds.getString(t) + "." + unique);
		else ds.put(t, "" + unique);
	}

	void handle(DS ds)
	{
		String d = getDate();
		replaceMap.put(Tag.StudyDate, d);
		replaceMap.put(Tag.AcquisitionDate, d);
		for (Iterator iter = replaceMap.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry entry = (Map.Entry) iter.next();
			Tag t = (Tag) entry.getKey();
			String s = (String) entry.getValue();
			if (ds.contains(t)) ds.put(t, s);
		}
		fixup(ds, Tag.SOPInstanceUID);
		fixup(ds, Tag.StudyInstanceUID);
		fixup(ds, Tag.SeriesInstanceUID);
		fixup(ds, Tag.StudyID);
		fixup(ds, Tag.AccessionNumber);
	}

	public void run() throws Exception
	{
		setup();
		for (Iterator iter2 = studyList.iterator(); iter2.hasNext();)
		{
			Study study = (Study) iter2.next();
			vlog("adding " + study);
			// dsList.add(ds);
			List seriesList = SeriesView.get().selectWhere("studyID = " + study.getStudyID());
			for (Iterator iter3 = seriesList.iterator(); iter3.hasNext();)
			{
				Series series = (Series) iter3.next();
				vlog("adding " + series);
				List imageList = ImageView.get().selectWhere("seriesID = " + series.getSeriesID());
				for (Iterator iter4 = imageList.iterator(); iter4.hasNext();)
				{
					Image image = (Image) iter4.next();
					String imageName = image.getInstanceNumber();
					if (imageName.length() == 0) imageName = "INSTANCE";
					// imageName = verifyFileName(seriesDir, imageName);
					vlog("adding " + image);
					SSInputStream imageStream = null;
					OutputStream outputStream = null;
					try
					{
						imageStream = getImageStream(image);
						ImageMetaInfo oldImi = (ImageMetaInfo) imageStream.getMeta();
						UID syntax = UID.get(oldImi.getTransferSyntax());
						DS imageDS = DSInputStream.readFrom(imageStream, syntax, true); // todo:
																						// watch
																						// endian
																						// !!!!!!!!
						// log(""+imageDS);
						// log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
						handle(imageDS);
						File f = net.metafusion.localstore.DicomStore.get().getSSStore().createTempFile(".dcm");
						imageDS.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
						imageDS.put(Tag.MediaStorageSOPClassUID, image.getClassUID());
						imageDS.put(Tag.MediaStorageSOPInstanceUID, imageDS.getString(Tag.SOPInstanceUID));
						imageDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
						imageDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
						imageDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
						// log(""+imageDS);
						DSOutputStream.writeDicomFile(imageDS, f);
						ImageMetaInfo imi = new ImageMetaInfo();
						imi.setMediaStorageSOPClassUID(imageDS.getString(Tag.MediaStorageSOPClassUID));
						imi.setMediaStorageSOPInstanceUID(imageDS.getString(Tag.MediaStorageSOPInstanceUID));
						imi.setTransferSyntax(UID.ExplicitVRLittleEndian.getUID());
						// imi.setTransferSyntax(UID.ImplicitVRLittleEndian.getUID());
						net.metafusion.localstore.DicomStore.get().putWithoutRules(imi, f);
						imageDS = null;
					}
					finally
					{
						Util.safeClose(imageStream);
						Util.safeClose(outputStream);
					}
				}
			}
		}
	}

	public static void test()
	{
		List studies = StudyView.get().selectAll();
		try
		{
			StudyDup sd = new StudyDup(studies);
			sd.run();
		}
		catch (Exception e)
		{
			log("sd caught " + e);
			e.printStackTrace();
		}
	}
}
