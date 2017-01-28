package net.metafusion.importer;

import java.sql.Date;
import java.sql.Timestamp;

@SuppressWarnings("deprecation")
public class ExampleLoader
{
	private Integer importID = 6;
	private String filename = "test.jpg";
	private String sourceApplicationEntityTitle = "";
	private String imageType = "JPEG";
	private String SOPClassUID = "";
	private String SOPInstanceUID = "";
	private Date acquisitionDate = new Date(2008, 1, 12);
	private Timestamp studyTime = new Timestamp(108, 0, 10, 1, 12, 5, 0);
	private Timestamp acquisitionTime = new Timestamp(108, 0, 10, 1, 12, 5, 0);
	private String accessionNumber = "";
	private String modality = "";
	private String conversionType = "";
	private String manufacturer = "";
	private String institutionName = "";
	private String referringPhysicianName = "";
	private String stationName = "";
	private String institutionalDepartmentName = "";
	private String manufacturerModelName = "";
	private String patientID = "1";
	private String patientName = "Randy Lexvold";
	private Date patientBirthDate = new Date(1970, 1, 29);
	private String patientSex = "";
	private String patientAge = "";
	private String patientComments = "";
	private String deviceSerialNumber = "";
	private String softwareVersion = "";
	private String studyID = "";
	private Date studyDate = new Date(2007, 12, 10);
	private String studyInstanceUID = "";
	private String seriesInstanceUID = "";
	private Integer seriesNumber = 0;
	private Integer acquisitionNumber = 0;
	private Integer instanceNumber = 0;
	private String patientOrientation = "";
	private String imageComments = "";
	private int samplesPerPixel = 0;
	private String photometricInterpretation = "";
	private int planarConfiguration = 0;
	private int Rows = 0;
	private int columns = 0;

	public void runExample()
	{
		ImageImportHeader h = new ImageImportHeader();
		h.setImportID(importID);
		h.setAccessionNumber(accessionNumber);
		h.setAcquisitionDate(acquisitionDate);
		h.setAcquisitionNumber(acquisitionNumber);
		h.setAcquisitionTime(acquisitionTime);
		h.setColumns(columns);
		h.setConversionType(conversionType);
		h.setDeviceSerialNumber(deviceSerialNumber);
		h.setFilename(filename);
		h.setImageComments(imageComments);
		h.setImageType(imageType);
		h.setInstanceNumber(instanceNumber);
		h.setInstitutionalDepartmentName(institutionalDepartmentName);
		h.setInstitutionName(institutionName);
		h.setManufacturer(manufacturer);
		h.setManufacturerModelName(manufacturerModelName);
		h.setModality(modality);
		h.setPatientAge(patientAge);
		h.setPatientBirthDate(patientBirthDate);
		h.setPatientComments(patientComments);
		h.setPatientID(patientID);
		h.setPatientName(patientName);
		h.setPatientOrientation(patientOrientation);
		h.setPatientSex(patientSex);
		h.setPhotometricInterpretation(photometricInterpretation);
		h.setPlanarConfiguration(planarConfiguration);
		h.setReferringPhysicianName(referringPhysicianName);
		h.setRows(Rows);
		h.setSamplesPerPixel(samplesPerPixel);
		h.setSeriesInstanceUID(seriesInstanceUID);
		h.setSeriesNumber(seriesNumber);
		h.setSoftwareVersion(softwareVersion);
		h.setSOPClassUID(SOPClassUID);
		h.setSOPInstanceUID(SOPInstanceUID);
		h.setSourceApplicationEntityTitle(sourceApplicationEntityTitle);
		h.setStationName(stationName);
		h.setStudyDate(studyDate);
		h.setStudyID(studyID);
		h.setStudyInstanceUID(studyInstanceUID);
		h.setStudyTime(studyTime);
		ImageImportView.get().insert(h);
	}
}
