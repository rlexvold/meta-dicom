package net.metafusion.model;

import integration.MFStudy;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import acme.db.JDBCUtil;
import acme.db.View;
import acme.util.DateUtil;

public class StudyView extends View
{
	static StudyView view = null;

	static public synchronized StudyView get()
	{
		if (view == null) view = new StudyView();
		return view;
	}

	StudyView()
	{
		super("dcm_study", new String[] { "studyID" }, new String[] { "patientID", "studyUID", "date", "studyIDString", "accessionNumber", "description", "modalities",
				"referringPhysicianName", "dateEntered", "dateModified", "stationName", "institutionName", "state", "reader", "version", "time", "origin", "originAET",
				"dateLastImage" });
	}

	public Study selectByUID(String uid)
	{
		return (Study) doSelect1(buildSelect("studyUID=?"), new Object[] { uid });
	}

	public Study selectByID(long id)
	{
		return (Study) select1(id);
	}

	public List selectOlderThan(Date oldest)
	{
		return this.selectWhere(" dateEntered < '" + DateUtil.formatYYYYY_MM_DD(oldest) + "'");
	}

	public List selectBetween(Date start, Date end)
	{
		return this.selectWhere(" dateEntered >= '" + DateUtil.formatYYYYY_MM_DD(start) + "' and dateEntered < '" + DateUtil.formatYYYYY_MM_DD(end) + "'  ");
	}

	public List selectBetweenInclusive(Date start, Date end)
	{
		return this.selectWhere(" dateEntered >= '" + DateUtil.formatYYYYY_MM_DD(start) + "' and dateEntered <= '" + DateUtil.formatYYYYY_MM_DD(end) + "'  ");
	}

	public List selectOldest(int count)
	{
		return this.selectWhereOrder(null, "dateEntered limit " + count);
	}

	public boolean risUpdate(MFStudy mfstudy)
	{
		String sql = "update dcm_study set state='" + mfstudy.state + "', " + " reader='" + mfstudy.reader + "', version=" + mfstudy.version + " where studyid=" + mfstudy.studyID;
		return JDBCUtil.get().update(sql) == 1;
	}

	public Timestamp getDateLastImage(String originAET)
	{
		String sql = "select max(dateLastImage) from dcm_study where originAET='" + originAET + "'";
		return (Timestamp) JDBCUtil.get().selectObject(sql);
	}

	public String getLastStudyUIDForAET(String originAET, Timestamp dateLastImage)
	{
		String sql = "select max(studyUID) from dcm_study where originAET='" + originAET + "' and dateLastImage='" + dateLastImage + "'";
		String s = JDBCUtil.get().selectString(sql);
		if (s != null) return s;
		return "";
	}

	public List selectLocalIDsPast(Timestamp dateLastImage, String lastUID, int limit)
	{
		String sql = "select studyid from dcm_study where dateLastImage>='" + dateLastImage + "'" + " and origin='" + Study.ORIGIN_LOCAL + "'"
				+ " order by dateLastImage, studyuid limit " + limit;
		// ****** add
		return JDBCUtil.get().selectList(sql);
	}

	public String getOriginAET(long studyId)
	{
		String sql = "select originAET from dcm_study where studyId=" + studyId;
		return JDBCUtil.get().selectString(sql);
	}

	//
	@Override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Study a = new Study();
		a.setStudyID(rs.getLong(offset++));
		a.setPatientID(rs.getLong(offset++));
		a.setStudyUID(rs.getString(offset++));
		a.setDate(rs.getDate(offset++));
		a.setStudyIDString(rs.getString(offset++));
		a.setAccessionNumber(rs.getString(offset++));
		a.setDescription(rs.getString(offset++));
		a.setModalities(rs.getString(offset++));
		a.setReferringPhysicianName(rs.getString(offset++));
		a.setDateEntered(rs.getTimestamp(offset++));
		a.setDateModified(rs.getTimestamp(offset++));
		a.setStationName(rs.getString(offset++));
		a.setInstitutionName(rs.getString(offset++));
		a.setState(rs.getString(offset++));
		a.setReader(rs.getString(offset++));
		a.setVersion(rs.getInt(offset++));
		a.setTime(rs.getTime(offset++));
		a.setOrigin(rs.getString(offset++).charAt(0));
		a.setOriginAET(rs.getString(offset++));
		a.setDateLastImage(rs.getTimestamp(offset++));
		return a;
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Study a = (Study) o;
		if (pk)
			ps.setLong(i++, a.getStudyID());
		else
		{
			ps.setLong(i++, a.getPatientID());
			ps.setString(i++, a.getStudyUID());
			ps.setDate(i++, a.getDate());
			ps.setString(i++, a.getStudyIDString());
			ps.setString(i++, a.getAccessionNumber());
			ps.setString(i++, a.getDescription());
			ps.setString(i++, a.getModalities());
			ps.setString(i++, a.getReferringPhysicianName());
			ps.setTimestamp(i++, a.getDateEntered());
			ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			ps.setString(i++, a.getStationName());
			ps.setString(i++, a.getInstitutionName());
			ps.setString(i++, a.getState());
			ps.setString(i++, a.getReader());
			ps.setInt(i++, a.getVersion());
			ps.setTime(i++, a.getTime());
			ps.setString(i++, "" + a.getOrigin());
			ps.setString(i++, a.getOriginAET());
			ps.setTimestamp(i++, a.getDateLastImage());
		}
	}
}
