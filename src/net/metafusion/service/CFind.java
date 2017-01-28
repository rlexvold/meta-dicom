package net.metafusion.service;

import java.util.ArrayList;
import java.util.Collection;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.msg.CFindReq;
import net.metafusion.net.DicomClientSession;
import net.metafusion.util.AEMap;
import net.metafusion.util.Message;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.Util;

public class CFind extends DicomClientService
{
	private ArrayList<DS>		results	= new ArrayList<DS>();
	private DicomClientSession	sess	= null;

	public CFind()
	{
		super("CFind", null);
	}

	public int runit() throws Exception
	{
		if (sess == null)
			sess = new DicomClientSession(RoleMap.getClientRoleMap());
		sess.connect(AEMap.get(getSourceAE()));
		CFindReq findReq = new CFindReq();
		findReq.CommandField = Dicom.C_FIND_RQ;
		findReq.MessageID = sess.getNextMsgID();
		findReq.Priority = Dicom.MEDIUM;
		findReq.AffectedSOPClassUID = UID.StudyRootQueryRetrieveInformationModelFIND.getUID();
		findReq.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
		Message req = new Message(sess, findReq, attrSet);
		sess.writeMessage(req);
		Message resp;
		for (;;)
		{
			resp = sess.readMessage();
			if (resp == null)
			{
				Util.log("FAIL: no response");
				break;
			}
			Util.log("cmd=" + resp.getCmd().toString());
			if (resp.getStatus() == Dicom.PENDING)
			{
				Util.log("have resp " + resp.getDataSet().toString());
				results.add(resp.getDataSet());
			}
			else if (resp.getStatus() == Dicom.SUCCESS)
			{
				Util.log("done");
				break;
			}
			else
			{
				Util.log("error " + resp.getStatus());
				break;
			}
		}
		sess.close(true);
		return resp != null ? resp.getStatus() : -1;
	}

	public static void putTagList(DS ds, Tag list[], Tag key, String value)
	{
		for (int i = 0; i < list.length; i++)
			ds.put(list[i], key.equals(list[i]) ? value : null);
	}

	public static DS buildSearch(Tag key, String value)
	{
		DS ds = new DS();
		Tag tags[] = {
				Tag.PatientName,
				Tag.PatientID,
				Tag.StudyDate,
				Tag.StudyTime,
				Tag.AccessionNumber,
				Tag.StudyID,
				Tag.StudyInstanceUID,
				Tag.Modality,
				Tag.SeriesInstanceUID,
				Tag.SeriesNumber,
				Tag.ImageType,
				Tag.SOPInstanceUID,
				Tag.SOPClassUID,
				Tag.InstanceNumber };
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

	public ArrayList<DS> getResults()
	{
		return results;
	}

	public void setResults(ArrayList<DS> results)
	{
		this.results = results;
	}
}
