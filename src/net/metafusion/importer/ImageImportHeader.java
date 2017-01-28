package net.metafusion.importer;

import java.sql.Date;
import java.sql.Timestamp;

import net.metafusion.util.UID;

public class ImageImportHeader
{
	private long		importID						= 0;
	private String		filename						= "";
	private String		sourceApplicationEntityTitle	= "";
	private String		imageType						= "";
	private String		SOPClassUID						= "";
	private String		SOPInstanceUID					= "";
	private Date		acquisitionDate					= null;
	private Timestamp	studyTime						= null;
	private Timestamp	acquisitionTime					= null;
	private String		accessionNumber					= "";
	private String		modality						= "";
	private String		conversionType					= "";
	private String		manufacturer					= "";
	private String		institutionName					= "";
	private String		referringPhysicianName			= "";
	private String		stationName						= "";
	private String		institutionalDepartmentName		= "";
	private String		manufacturerModelName			= "";
	private String		patientID						= "";
	private String		patientName						= "";
	private Date		patientBirthDate				= null;
	private String		patientSex						= "";
	private String		patientAge						= "";
	private String		patientComments					= "";
	private String		deviceSerialNumber				= "";
	private String		softwareVersion					= "";
	private String		studyID							= "";
	private Date		studyDate						= null;
	private String		studyInstanceUID				= "";
	private String		seriesInstanceUID				= "";
	private Integer		seriesNumber					= 0;
	private Integer		acquisitionNumber				= 0;
	private Integer		instanceNumber					= 0;
	private String		patientOrientation				= "";
	private String		imageComments					= "";
	private int			samplesPerPixel					= 0;
	private int			bitsPerPixel					= 0;
	private int			bitsPerSample					= 0;
	private String		photometricInterpretation		= "";
	private int			planarConfiguration				= 0;
	private int			Rows							= 0;
	private int			columns							= 0;
	private byte[]		data							= null;
	private UID			transferSyntax					= null;

	public UID getTransferSyntax()
	{
		return transferSyntax;
	}

	public void setTransferSyntax(UID transferSyntax)
	{
		this.transferSyntax = transferSyntax;
	}

	public String getAccessionNumber()
	{
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber)
	{
		this.accessionNumber = accessionNumber;
	}

	public Date getAcquisitionDate()
	{
		return acquisitionDate;
	}

	public void setAcquisitionDate(Date acquisitionDate)
	{
		this.acquisitionDate = acquisitionDate;
	}

	public Integer getAcquisitionNumber()
	{
		return acquisitionNumber;
	}

	public void setAcquisitionNumber(int acquisitionNumber)
	{
		this.acquisitionNumber = acquisitionNumber;
	}

	public Timestamp getAcquisitionTime()
	{
		return acquisitionTime;
	}

	public void setAcquisitionTime(Timestamp acquisitionTime)
	{
		this.acquisitionTime = acquisitionTime;
	}

	public int getColumns()
	{
		return columns;
	}

	public void setColumns(int columns)
	{
		this.columns = columns;
	}

	public String getConversionType()
	{
		return conversionType;
	}

	public void setConversionType(String conversionType)
	{
		this.conversionType = conversionType;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}

	public String getDeviceSerialNumber()
	{
		return deviceSerialNumber;
	}

	public void setDeviceSerialNumber(String deviceSerialNumber)
	{
		this.deviceSerialNumber = deviceSerialNumber;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getImageComments()
	{
		return imageComments;
	}

	public void setImageComments(String imageComments)
	{
		this.imageComments = imageComments;
	}

	public String getImageType()
	{
		return imageType;
	}

	public void setImageType(String imageType)
	{
		this.imageType = imageType;
	}

	public long getImportID()
	{
		return importID;
	}

	public void setImportID(long importID)
	{
		this.importID = importID;
	}

	public Integer getInstanceNumber()
	{
		return instanceNumber;
	}

	public void setInstanceNumber(Integer instanceNumber)
	{
		this.instanceNumber = instanceNumber;
	}

	public String getInstitutionalDepartmentName()
	{
		return institutionalDepartmentName;
	}

	public void setInstitutionalDepartmentName(String institutionalDepartmentName)
	{
		this.institutionalDepartmentName = institutionalDepartmentName;
	}

	public String getInstitutionName()
	{
		return institutionName;
	}

	public void setInstitutionName(String institutionName)
	{
		this.institutionName = institutionName;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer(String manufacturer)
	{
		this.manufacturer = manufacturer;
	}

	public String getManufacturerModelName()
	{
		return manufacturerModelName;
	}

	public void setManufacturerModelName(String manufacturerModelName)
	{
		this.manufacturerModelName = manufacturerModelName;
	}

	public String getModality()
	{
		return modality;
	}

	public void setModality(String modality)
	{
		this.modality = modality;
	}

	public String getPatientAge()
	{
		return patientAge;
	}

	public void setPatientAge(String patientAge)
	{
		this.patientAge = patientAge;
	}

	public Date getPatientBirthDate()
	{
		return patientBirthDate;
	}

	public void setPatientBirthDate(Date patientBirthDate)
	{
		this.patientBirthDate = patientBirthDate;
	}

	public String getPatientComments()
	{
		return patientComments;
	}

	public void setPatientComments(String patientComments)
	{
		this.patientComments = patientComments;
	}

	public String getPatientID()
	{
		return patientID;
	}

	public void setPatientID(String patientID)
	{
		this.patientID = patientID;
	}

	public String getPatientName()
	{
		return patientName;
	}

	public void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	public String getPatientOrientation()
	{
		return patientOrientation;
	}

	public void setPatientOrientation(String patientOrientation)
	{
		this.patientOrientation = patientOrientation;
	}

	public String getPatientSex()
	{
		return patientSex;
	}

	public void setPatientSex(String patientSex)
	{
		this.patientSex = patientSex;
	}

	public String getPhotometricInterpretation()
	{
		return photometricInterpretation;
	}

	public void setPhotometricInterpretation(String photometricInterpretation)
	{
		this.photometricInterpretation = photometricInterpretation;
	}

	public int getPlanarConfiguration()
	{
		return planarConfiguration;
	}

	public void setPlanarConfiguration(int planarConfiguration)
	{
		this.planarConfiguration = planarConfiguration;
	}

	public String getReferringPhysicianName()
	{
		return referringPhysicianName;
	}

	public void setReferringPhysicianName(String referringPhysicianName)
	{
		this.referringPhysicianName = referringPhysicianName;
	}

	public int getRows()
	{
		return Rows;
	}

	public void setRows(int rows)
	{
		Rows = rows;
	}

	public int getSamplesPerPixel()
	{
		return samplesPerPixel;
	}

	public void setSamplesPerPixel(int samplesPerPixel)
	{
		this.samplesPerPixel = samplesPerPixel;
	}

	public String getSeriesInstanceUID()
	{
		return seriesInstanceUID;
	}

	public void setSeriesInstanceUID(String seriesInstanceUID)
	{
		this.seriesInstanceUID = seriesInstanceUID;
	}

	public Integer getSeriesNumber()
	{
		return seriesNumber;
	}

	public void setSeriesNumber(Integer seriesNumber)
	{
		this.seriesNumber = seriesNumber;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion)
	{
		this.softwareVersion = softwareVersion;
	}

	public String getSOPClassUID()
	{
		return SOPClassUID;
	}

	public void setSOPClassUID(String classUID)
	{
		SOPClassUID = classUID;
	}

	public String getSOPInstanceUID()
	{
		return SOPInstanceUID;
	}

	public void setSOPInstanceUID(String instanceUID)
	{
		SOPInstanceUID = instanceUID;
	}

	public String getSourceApplicationEntityTitle()
	{
		return sourceApplicationEntityTitle;
	}

	public void setSourceApplicationEntityTitle(String sourceApplicationEntityTitle)
	{
		this.sourceApplicationEntityTitle = sourceApplicationEntityTitle;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public Date getStudyDate()
	{
		return studyDate;
	}

	public void setStudyDate(Date studyDate)
	{
		this.studyDate = studyDate;
	}

	public String getStudyID()
	{
		return studyID;
	}

	public void setStudyID(String studyID)
	{
		this.studyID = studyID;
	}

	public String getStudyInstanceUID()
	{
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID)
	{
		this.studyInstanceUID = studyInstanceUID;
	}

	public Timestamp getStudyTime()
	{
		return studyTime;
	}

	public void setStudyTime(Timestamp studyTime)
	{
		this.studyTime = studyTime;
	}

	public int getBitsPerPixel()
	{
		return bitsPerPixel;
	}

	public void setBitsPerPixel(int bitsPerPixel)
	{
		this.bitsPerPixel = bitsPerPixel;
	}

	public int getBitsPerSample()
	{
		return bitsPerSample;
	}

	public void setBitsPerSample(int bitsPerSample)
	{
		this.bitsPerSample = bitsPerSample;
	}

	public void setAcquisitionNumber(Integer acquisitionNumber)
	{
		this.acquisitionNumber = acquisitionNumber;
	}
}
