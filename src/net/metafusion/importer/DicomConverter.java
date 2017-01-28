package net.metafusion.importer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;

public class DicomConverter
{
	private static String getLocalIPString()
	{
		try
		{
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ipAddr = addr.getAddress();
			return "" + ipAddr[0] + "." + ipAddr[1] + "." + ipAddr[2] + "." + ipAddr[3];
		}
		catch (UnknownHostException e)
		{
			return "0.0.0.0";
		}
	}

	public static DS createDS(ImageImportHeader head)
	{
		DS ds = new DS();
		String uid = Dicom.METAFUSION_UID_PREFIX + ".239." + getLocalIPString() + "." + System.currentTimeMillis(); // we
		ds.put(Tag.TransferSyntaxUID, head.getTransferSyntax());
		ds.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
		ds.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
		ds.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
		ds.put(Tag.SourceApplicationEntityTitle, head.getSourceApplicationEntityTitle());
		ds.put(Tag.ImageType, head.getImageType());
		ds.put(Tag.MediaStorageSOPClassUID, head.getSOPClassUID());
		ds.put(Tag.MediaStorageSOPInstanceUID, uid + ".3");
		ds.put(Tag.SOPClassUID, head.getSOPClassUID());
		ds.put(Tag.SOPInstanceUID, uid + ".3");
		ds.put(Tag.AcquisitionDate, DicomUtil.formatDate(head.getAcquisitionDate()));
		ds.put(Tag.AcquisitionTime, DicomUtil.formatTime(new Date(head.getAcquisitionTime().getTime())));
		ds.put(Tag.StudyDate, DicomUtil.formatDate(head.getStudyDate()));
		ds.put(Tag.StudyTime, DicomUtil.formatTime(new Date(head.getStudyTime().getTime())));
		ds.put(Tag.AccessionNumber, head.getAccessionNumber());
		ds.put(Tag.Modality, head.getModality());
		ds.put(Tag.Manufacturer, head.getManufacturer());
		ds.put(Tag.InstitutionName, head.getInstitutionName());
		ds.put(Tag.ReferringPhysicianName, head.getReferringPhysicianName());
		ds.put(Tag.StationName, head.getStationName());
		ds.put(Tag.InstitutionalDepartmentName, head.getInstitutionalDepartmentName());
		ds.put(Tag.ManufacturerModelName, head.getManufacturerModelName());
		ds.put(Tag.PatientID, head.getPatientID());
		ds.put(Tag.PatientName, head.getPatientName());
		if (head.getPatientBirthDate() != null)
			ds.put(Tag.PatientBirthDate, DicomUtil.formatDate(head.getPatientBirthDate()));
		ds.put(Tag.PatientSex, head.getPatientSex());
		ds.put(Tag.PatientAge, head.getPatientAge());
		// ds.put(Tag.StudyID, head.getStudyID());
		ds.put(Tag.StudyInstanceUID, head.getStudyInstanceUID());
		ds.put(Tag.SeriesInstanceUID, head.getSeriesInstanceUID());
		ds.putString(Tag.SeriesNumber, head.getSeriesNumber().toString());
		ds.putString(Tag.InstanceNumber, head.getInstanceNumber().toString());
		ds.putInt(Tag.SamplesPerPixel, head.getSamplesPerPixel());
		ds.put(Tag.PhotometricInterpretation, head.getPhotometricInterpretation());
		ds.putInt(Tag.PlanarConfiguration, head.getPlanarConfiguration());
		ds.putInt(Tag.Rows, head.getRows()); // calculate bitmap height
		ds.putInt(Tag.Columns, head.getColumns()); // calculate bitmap width
		ds.putInt(Tag.BitsAllocated, head.getBitsPerSample());
		ds.putInt(Tag.BitsStored, head.getBitsPerSample());
		ds.putInt(Tag.HighBit, head.getBitsPerSample() - 1);
		ds.putInt(Tag.PixelRepresentation, 0);
		ds.putString(Tag.ImageComments, head.getImageComments());
		ds.put(Tag.PixelData, head.getData()); // pixelData is the image
		return ds;
	}

	public static DS convertJpegToDicom(ImageImportHeader h) throws Exception
	{
		ImageInfo ii = new JpegReader().readImageInfo(h.getFilename());
		h.setTransferSyntax(UID.ImplicitVRLittleEndian);
		h.setColumns(ii.getWidth());
		h.setRows(ii.getHeight());
		h.setBitsPerPixel(ii.getBitsPerPixel());
		h.setBitsPerSample(ii.getBitsPerSample());
		h.setSamplesPerPixel(ii.getSamplesPerPixel());
		h.setPlanarConfiguration(0);
		h.setPhotometricInterpretation("MONOCHROME2");
		h.setData(ImporterListener.readSourceFile(h));
		h.setImageType("");
		return createDS(h);
	}
}