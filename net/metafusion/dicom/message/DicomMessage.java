package net.metafusion.dicom.message;

import net.metafusion.dataset.DS;
import net.metafusion.message.AbstractMessage;
import net.metafusion.msg.Cmd;
import net.metafusion.net.DicomServer;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;

public class DicomMessage extends AbstractMessage
{
	private Cmd command = null;
	private DS dataSet = null;
	private DicomServer dicomServer = null;
	private String serverName = null;
	private RoleMap serverRoleMap = null;
	private UID root = UID.ImplicitVRLittleEndian;

	public DS getDataSet()
	{
		return dataSet;
	}

	public DicomMessage()
	{
		super();
	}

	public DicomMessage(Cmd cmd)
	{
		super();
		command = cmd;
	}

	public DicomServer getDicomServer()
	{
		return dicomServer;
	}

	public Cmd getMessage()
	{
		return command;
	}

	public String getServerName()
	{
		return serverName;
	}

	public RoleMap getServerRoleMap()
	{
		return serverRoleMap;
	}

	public void setDataSet(DS dataSet)
	{
		this.dataSet = dataSet;
	}

	public void setDicomServer(DicomServer dicomServer)
	{
		this.dicomServer = dicomServer;
	}

	public void setMessage(Cmd message)
	{
		this.command = message;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public void setServerRoleMap(RoleMap serverRoleMap)
	{
		this.serverRoleMap = serverRoleMap;
	}

	public Cmd getCommand()
	{
		return command;
	}

	public void setCommand(Cmd command)
	{
		this.command = command;
	}

	public UID getRoot()
	{
		return root;
	}

	public void setRoot(UID root)
	{
		this.root = root;
	}

	public static void putTagList(DS ds, Tag list[], Tag key, String value)
	{
		for (int i = 0; i < list.length; i++)
			ds.put(list[i], key.equals(list[i]) ? value : null);
	}

	public static DS buildSearch(Tag key, String value)
	{
		DS ds = new DS();
		Tag tags[] = { Tag.PatientName, Tag.PatientID, Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber, Tag.StudyID, Tag.StudyInstanceUID, Tag.Modality, Tag.SeriesInstanceUID,
				Tag.SeriesNumber, Tag.ImageType, Tag.SOPInstanceUID, Tag.SOPClassUID, Tag.InstanceNumber };
		putTagList(ds, tags, key, value != null ? value.toUpperCase() : null);
		return ds;
	}

	public static DS searchByPatientName(String name)
	{
		return buildSearch(Tag.PatientName, name);
	}

	public static DS searchByAccessionNumber(String num)
	{
		return buildSearch(Tag.AccessionNumber, num);
	}

	public static DS searchByStudyInstanceUID(String uid)
	{
		return buildSearch(Tag.StudyInstanceUID, uid);
	}

	public static DS searchByStudyID(String uid)
	{
		return buildSearch(Tag.StudyID, uid);
	}

	public static DS searchBySeriesInstanceUID(String uid)
	{
		return buildSearch(Tag.SeriesInstanceUID, uid);
	}

	public static DS searchBySOPInstanceUID(String uid)
	{
		return buildSearch(Tag.SOPClassUID, uid);
	}

	public static DS searchBySOPClassUID(String uid)
	{
		return buildSearch(Tag.SOPInstanceUID, uid);
	}
}