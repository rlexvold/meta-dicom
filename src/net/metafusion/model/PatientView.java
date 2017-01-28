package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import acme.db.View;

public class PatientView extends View
{
	static PatientView view = null;

	static public synchronized PatientView get()
	{
		if (view == null)
		{
			view = new PatientView();
		}
		return view;
	}

	PatientView()
	{
		super("dcm_patient", new String[] { "patientID" },
				new String[] { "dicomName", "firstName", "middleName", "lastName", "sex", "extID", "dob", "dateEntered", "dateModified" });
	}

	public Patient selectByExtID(String extID)
	{
		return (Patient) doSelect1(buildSelect("extID=?"), new Object[] { extID });
	}

	public Patient selectUnique(String extID, String dicomName, String dob, String sex)
	{
		String whereClause = "extID=? AND upper(dicomName)=?";
		ArrayList args = new ArrayList();
		args.add(extID);
		args.add(dicomName.toUpperCase());
		if (sex != null) sex = sex.toUpperCase();
		if (sex == "M" || sex == "F")
		{
			whereClause = whereClause.concat(" AND upper(sex)=?");
			args.add(sex);
		}
		if (dob != null && dob.length() >= 8)
		{
			whereClause = whereClause.concat(" AND dob=?");
			args.add(dob);
		}
		return (Patient) doSelect1(buildSelect(whereClause), args.toArray());
	}

	public Patient select(long id)
	{
		return (Patient) select1(id);
	}

	public int countImages(long id)
	{
		String sql = " select count(*) from dcm_patient,dcm_image " + " where dcm_patient.patientid = ? and " + " dcm_patient.patientid = dcm_image.patientid ";
		return doSelectInt(sql, new Object[] { new Long(id) });
	}

	// public List selectPatientsWithNoImages() {
	// return super.doSelectN("select patientid from dcm_patient p, dcm_image i
	// "+
	// "where "
	// )
	// }
	//
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Patient a = new Patient();
		a.setPatientID(rs.getLong(offset++));
		a.setDicomName(rs.getString(offset++));
		a.setFirstName(rs.getString(offset++));
		a.setMiddleName(rs.getString(offset++));
		a.setLastName(rs.getString(offset++));
		a.setSex(rs.getString(offset++));
		a.setExtID(rs.getString(offset++));
		a.setDob(rs.getDate(offset++));
		a.setDateEntered(rs.getTimestamp(offset++));
		a.setDateModified(rs.getTimestamp(offset++));
		return a;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Patient a = (Patient) o;
		if (pk)
			ps.setLong(i++, a.getPatientID());
		else
		{
			ps.setString(i++, a.getDicomName());
			ps.setString(i++, a.getFirstName());
			ps.setString(i++, a.getMiddleName());
			ps.setString(i++, a.getLastName());
			ps.setString(i++, a.getSex());
			ps.setString(i++, a.getExtID());
			ps.setDate(i++, a.getDob());
			ps.setTimestamp(i++, a.getDateEntered());
			ps.setTimestamp(i++, a.getDateModified());
		}
	}
}
