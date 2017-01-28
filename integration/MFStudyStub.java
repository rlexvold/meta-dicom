package integration;

import java.io.Serializable;

public class MFStudyStub implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public long studyID;
	// public String lastViewer="";
	public MFAttachment[] attachments = new MFAttachment[0];
	public MFNote notes = new MFNote();

	public MFStudyStub()
	{
	}
}
