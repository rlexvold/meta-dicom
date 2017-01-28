package net.metafusion.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import net.metafusion.util.DicomUtil;
import acme.db.JDBCUtil;
import acme.db.View;

public class WorklistView extends View
{
	static WorklistView	view	= null;
	static String		table	= "ris_worklist";

	static public synchronized WorklistView get()
	{
		if (view == null)
		{
			view = new WorklistView();
		}
		return view;
	}

	WorklistView()
	{
		super(table, new String[] { "WorklistID" }, new String[] {
				"StudyInstanceUID",
				"StudyDate",
				"State",
				"AETitle",
				"AccessionNumber",
				"PatientName",
				"PatientID",
				"RequestedProcedureDescription",
				"RequestedProcedureID",
				"PatientBirthDate",
				"PatientSex",
				"Referrer" });
	}

	public Worklist selectByUID(String uid)
	{
		return (Worklist) doSelect1(buildSelect("StudyInstanceUID=?"), new Object[] { uid });
	}

	public List selectTodayForAET(String aet)
	{
		String dicomToday = DicomUtil.formatDate(new Date(System.currentTimeMillis()));
		return doSelectN(buildSelect("AETitle=? and StudyDate = ?"), new Object[] { aet, dicomToday });
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Worklist a = new Worklist();
		a.WorklistID = rs.getLong(offset++);
		a.StudyInstanceUID = (rs.getString(offset++));
		a.StudyDate = (rs.getString(offset++));
		a.State = (rs.getString(offset++));
		a.AETitle = (rs.getString(offset++));
		a.AccessionNumber = (rs.getString(offset++));
		a.PatientName = (rs.getString(offset++));
		a.PatientID = (rs.getString(offset++));
		a.RequestedProcedureDescription = (rs.getString(offset++));
		a.RequestedProcedureID = (rs.getString(offset++));
		a.PatientBirthDate = (rs.getString(offset++));
		a.PatientSex = rs.getString(offset++);
		a.Referrer = rs.getString(offset++);
		return a;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Worklist a = (Worklist) o;
		if (pk)
			ps.setLong(i++, a.WorklistID);
		else
		{
			ps.setString(i++, a.StudyInstanceUID);
			ps.setString(i++, a.StudyDate);
			ps.setString(i++, a.State);
			ps.setString(i++, a.AETitle);
			ps.setString(i++, a.AccessionNumber);
			ps.setString(i++, a.PatientName);
			ps.setString(i++, a.PatientID);
			ps.setString(i++, a.RequestedProcedureDescription);
			ps.setString(i++, a.RequestedProcedureID);
			ps.setString(i++, a.PatientBirthDate);
			ps.setString(i++, a.PatientSex);
			ps.setString(i++, a.Referrer);
		}
	}

	public void updateTable() throws Exception
	{
		String sql = "alter table " + table + " add WorklistID Integer NOT NULL AUTO_INCREMENT PRIMARY KEY, add PatientSex VARCHAR(10), add Referrer VARCHAR(128)";
		JDBCUtil.get().update(sql);
	}
}
