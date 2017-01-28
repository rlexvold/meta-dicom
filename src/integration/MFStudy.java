package integration;

import java.io.Serializable;

public class MFStudy implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public long studyID;
	public int version;
	public boolean success = false; // *** race
	public String state = "";
	public String studyUID = "";
	public String patientID = "";
	public String patientName = "";
	public String patientSex = "";
	public String patientBDay = "";
	public String dateTime = "";
	public String modality = "";
	public String description = "";
	public String referrer = "";
	public String lastViewer = "";
	public String institution = "";
	public String station = "";
	public String reader = "";
	public MFSeries[] series = new MFSeries[0];
	public MFDictation[] dictations = new MFDictation[0];
	public MFReport[] reports = new MFReport[0];
	public MFAttachment[] attachments = new MFAttachment[0];
	public MFNote notes = new MFNote();
	public String studyIDString = "";

	public MFStudy()
	{
	}

	public void addAttachment(String name, long id, String label)
	{
		MFAttachment[] newa = new MFAttachment[attachments != null ? attachments.length + 1 : 1];
		System.arraycopy(attachments, 0, newa, 1, newa.length - 1);
		newa[0] = new MFAttachment(id, name, label);
		attachments = newa;
	}

	public void addNote(String note)
	{
		notes.add(note);
		// String[] newa = new String[notes!=null ? notes.length+1:1];
		// System.arraycopy(notes, 0, newa, 0, newa.length-1);
		// newa[newa.length-1] = note;
		// notes = newa;
	}

	public MFStudyStub getStub()
	{
		MFStudyStub stub = new MFStudyStub();
		stub.studyID = studyID;
		stub.attachments = attachments;
		stub.notes = notes;
		return stub;
	}

	public void loadStub(MFStudyStub stub)
	{
		attachments = stub.attachments;
		notes = stub.notes;
	}
}
