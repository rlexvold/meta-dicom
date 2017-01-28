package net.metafusion.importer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import acme.db.JDBCUtil;
import acme.db.View;

public class ImageImportView extends View
{
	private static ImageImportView view = null;
	private static String tableName = "image_import";
	private static String primaryKey = "importID";

	static public synchronized ImageImportView get()
	{
		if (view == null) view = new ImageImportView();
		return view;
	}

	ImageImportView()
	{
		super(tableName, new String[] { primaryKey }, new String[] { "filename", "sourceApplicationEntityTitle", "imageType", "SOPClassUID", "SOPInstanceUID", "acquisitionDate",
				"studyTime", "acquisitionTime", "accessionNumber", "modality", "conversionType", "manufacturer", "institutionName", "referringPhysicianName", "stationName",
				"institutionalDepartmentName", "manufacturerModelName", "patientID", "patientName", "patientBirthDate", "patientSex", "patientAge", "patientComments",
				"deviceSerialNumber", "softwareVersion", "studyID", "studyDate", "studyInstanceUID", "seriesInstanceUID", "seriesNumber", "acquisitionNumber", "instanceNumber",
				"patientOrientation", "imageComments" });
	}

	public ArrayList<ImageImportHeader> getAllHeaders()
	{
		ArrayList<ImageImportHeader> theHeads = (ArrayList<ImageImportHeader>) selectWhere("processedFlag=0");
		return theHeads;
	}

	public void setProcessed(long key)
	{
		String sql = "update " + tableName + " set processedFlag=1 where " + primaryKey + "=" + key;
		JDBCUtil.get().update(sql);
	}

	@Override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		offset = 1;
		ImageImportHeader head = new ImageImportHeader();
		head.setImportID(rs.getLong(offset++));
		head.setFilename(rs.getString(offset++));
		head.setSourceApplicationEntityTitle(rs.getString(offset++));
		head.setImageType(rs.getString(offset++));
		head.setSOPClassUID(rs.getString(offset++));
		head.setSOPInstanceUID(rs.getString(offset++));
		head.setAcquisitionDate(rs.getDate(offset++));
		head.setStudyTime(rs.getTimestamp(offset++));
		head.setAcquisitionTime(rs.getTimestamp(offset++));
		head.setAccessionNumber(rs.getString(offset++));
		head.setModality(rs.getString(offset++));
		head.setConversionType(rs.getString(offset++));
		head.setManufacturer(rs.getString(offset++));
		head.setInstitutionName(rs.getString(offset++));
		head.setReferringPhysicianName(rs.getString(offset++));
		head.setStationName(rs.getString(offset++));
		head.setInstitutionalDepartmentName(rs.getString(offset++));
		head.setManufacturerModelName(rs.getString(offset++));
		head.setPatientID(rs.getString(offset++));
		head.setPatientName(rs.getString(offset++));
		head.setPatientBirthDate(rs.getDate(offset++));
		head.setPatientSex(rs.getString(offset++));
		head.setPatientAge(rs.getString(offset++));
		head.setPatientComments(rs.getString(offset++));
		head.setDeviceSerialNumber(rs.getString(offset++));
		head.setSoftwareVersion(rs.getString(offset++));
		head.setStudyID(rs.getString(offset++));
		head.setStudyDate(rs.getDate(offset++));
		head.setStudyInstanceUID(rs.getString(offset++));
		head.setSeriesInstanceUID(rs.getString(offset++));
		head.setSeriesNumber(rs.getInt(offset++));
		head.setAcquisitionNumber(rs.getInt(offset++));
		head.setInstanceNumber(rs.getInt(offset++));
		head.setPatientOrientation(rs.getString(offset++));
		head.setImageComments(rs.getString(offset++));
		return head;
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		ImageImportHeader head = (ImageImportHeader) o;
		if (pk) ps.setLong(i++, head.getImportID());
		ps.setString(i++, head.getFilename());
		ps.setString(i++, head.getSourceApplicationEntityTitle());
		ps.setString(i++, head.getImageType());
		ps.setString(i++, head.getSOPClassUID());
		ps.setString(i++, head.getSOPInstanceUID());
		ps.setDate(i++, head.getAcquisitionDate());
		ps.setTimestamp(i++, head.getStudyTime());
		ps.setTimestamp(i++, head.getAcquisitionTime());
		ps.setString(i++, head.getAccessionNumber());
		ps.setString(i++, head.getModality());
		ps.setString(i++, head.getConversionType());
		ps.setString(i++, head.getManufacturer());
		ps.setString(i++, head.getInstitutionName());
		ps.setString(i++, head.getReferringPhysicianName());
		ps.setString(i++, head.getStationName());
		ps.setString(i++, head.getInstitutionalDepartmentName());
		ps.setString(i++, head.getManufacturerModelName());
		ps.setString(i++, head.getPatientID());
		ps.setString(i++, head.getPatientName());
		ps.setDate(i++, head.getPatientBirthDate());
		ps.setString(i++, head.getPatientSex());
		ps.setString(i++, head.getPatientAge());
		ps.setString(i++, head.getPatientComments());
		ps.setString(i++, head.getDeviceSerialNumber());
		ps.setString(i++, head.getSoftwareVersion());
		ps.setString(i++, head.getStudyID());
		ps.setDate(i++, head.getStudyDate());
		ps.setString(i++, head.getStudyInstanceUID());
		ps.setString(i++, head.getSeriesInstanceUID());
		ps.setInt(i++, head.getSeriesNumber());
		ps.setInt(i++, head.getAcquisitionNumber());
		ps.setInt(i++, head.getInstanceNumber());
		ps.setString(i++, head.getPatientOrientation());
		ps.setString(i++, head.getImageComments());
	}
}
