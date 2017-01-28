package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import acme.db.JDBCUtil;
import acme.db.View;

public class SeriesView extends View
{
	static SeriesView view = null;

	static public synchronized SeriesView get()
	{
		if (view == null) view = new SeriesView();
		return view;
	}

	SeriesView()
	{
		super("dcm_series", new String[] { "seriesID" }, new String[] { "studyID", "patientID", "modality", "seriesUID", "seriesNumber", "date", "physicianName", "operatorName",
				"description", "bodyPart", "stationName", "nameOfPhysicianReadingStudy", "dateEntered", "dateModified", "institutionName", "count" });
	}

	public Series selectByID(long uid)
	{
		return (Series) select1(uid);
	}

	public Series selectByUID(String uid)
	{
		return (Series) doSelect1(buildSelect("seriesUID=?"), new Object[] { uid });
	}

	public List selectByStudy(long studyID)
	{
		return selectWhere(" studyID=" + studyID);
	}

	//
	@Override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Series a = new Series();
		a.setSeriesID(rs.getLong(offset++));
		a.setStudyID(rs.getLong(offset++));
		a.setPatientID(rs.getLong(offset++));
		a.setModality(rs.getString(offset++));
		a.setSeriesUID(rs.getString(offset++));
		a.setSeriesNumber(rs.getString(offset++));
		a.setDate(rs.getDate(offset++));
		a.setPhysicianName(rs.getString(offset++));
		a.setOperatorName(rs.getString(offset++));
		a.setDescription(rs.getString(offset++));
		a.setBodyPart(rs.getString(offset++));
		a.setStationName(rs.getString(offset++));
		a.setNameOfPhysicianReadingStudy(rs.getString(offset++));
		a.setDateEntered(rs.getTimestamp(offset++));
		a.setDateModified(rs.getTimestamp(offset++));
		a.setInstitutionName(rs.getString(offset++));
		a.setCount(rs.getInt(offset++));
		return a;
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Series a = (Series) o;
		if (pk)
			ps.setLong(i++, a.getSeriesID());
		else
		{
			ps.setLong(i++, a.getStudyID());
			ps.setLong(i++, a.getPatientID());
			ps.setString(i++, a.getModality());
			ps.setString(i++, a.getSeriesUID());
			ps.setString(i++, a.getSeriesNumber());
			ps.setDate(i++, a.getDate());
			ps.setString(i++, a.getPhysicianName());
			ps.setString(i++, a.getOperatorName());
			ps.setString(i++, a.getDescription());
			ps.setString(i++, a.getBodyPart());
			ps.setString(i++, a.getStationName());
			ps.setString(i++, a.getNameOfPhysicianReadingStudy());
			ps.setTimestamp(i++, a.getDateEntered());
			ps.setTimestamp(i++, a.getDateModified());
			ps.setString(i++, a.getInstitutionName());
			ps.setInt(i++, a.getCount());
		}
	}

	public boolean updateCount(long seriesID, int count)
	{
		String sql = "update dcm_series set count = " + count + " where seriesID=" + seriesID;
		return JDBCUtil.get().update(sql) == 1;
	}
}
