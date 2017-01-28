package net.metafusion;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Status;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import net.metafusion.util.VR;
import acme.util.Log;
import acme.util.MSJavaHack;
import acme.util.StringUtil;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class Dicom
{
	static void log(String s)
	{
		Log.log(s);
	}
	public static String	METAFUSION_UID_PREFIX			= "1.2.826.0.1.3680043.2.712";
	public static String	METAFUSION_IMPLEMENTATION_NAME	= "METAFUSION_110";
	public static long		lastID							= 0;

	public synchronized static String GenerateUniqueUID()
	{
		long id = System.currentTimeMillis();
		while (id == lastID)
			id++;
		lastID = id;
		return METAFUSION_UID_PREFIX + "." + id;
	}
	public static final UID		FIND_PATIENT_ROOT		= UID.PatientRootQueryRetrieveInformationModelFIND;
	public static final UID		FIND_STUDY_ROOT			= UID.StudyRootQueryRetrieveInformationModelFIND;
	public static final UID		FIND_PATIENT_STUDY_ONLY	= UID.PatientStudyOnlyQueryRetrieveInformationModelFIND;
	public static final UID		MOVE_PATIENT_ROOT		= UID.PatientRootQueryRetrieveInformationModelMOVE;
	public static final UID		MOVE_STUDY_ROOT			= UID.StudyRootQueryRetrieveInformationModelMOVE;
	public static final UID		MOVE_PATIENT_STUDY_ONLY	= UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE;
	public static final String	PATIENT_LEVEL			= "PATIENT";
	public static final String	STUDY_LEVEL				= "STUDY";
	public static final String	SERIES_LEVEL			= "SERIES";
	public static final String	IMAGE_LEVEL				= "IMAGE";
	public static final int		PATIENT_LEVEL_INDEX		= 0;
	public static final int		STUDY_LEVEL_INDEX		= 1;
	public static final int		SERIES_LEVEL_INDEX		= 2;
	public static final int		IMAGE_LEVEL_INDEX		= 3;
	public static final int		COMMAND_DATASET_ABSENT	= 0x0101;
	public static final int		COMMAND_DATASET_PRESENT	= 0x0FEFE;
	public static final int		SUCCESS					= 0;
	public static final int		FAIL					= 1;
	public static final int		CANCEL					= 0x0FE00;
	public static final int		PENDING					= 0x0FF00;
	// get others from xml
	public static final int		C_STORE_RQ				= 0x0001;
	public static final int		C_STORE_RSP				= 0x8001;
	public static final int		C_GET_RQ				= 0x0010;
	public static final int		C_GET_RSP				= 0x8010;
	public static final int		C_FIND_RQ				= 0x0020;
	public static final int		C_FIND_RSP				= 0x8020;
	public static final int		C_MOVE_RQ				= 0x0021;
	public static final int		C_MOVE_RSP				= 0x8021;
	public static final int		C_ECHO_RQ				= 0x0030;
	public static final int		C_ECHO_RSP				= 0x8030;
	public static final int		N_EVENT_REPORT_RQ		= 0x0100;
	public static final int		N_EVENT_REPORT_RSP		= 0x8100;
	public static final int		N_GET_RQ				= 0x0110;
	public static final int		N_GET_RSP				= 0x8110;
	public static final int		N_SET_RQ				= 0x0120;
	public static final int		N_SET_RSP				= 0x8120;
	public static final int		N_ACTION_RQ				= 0x0130;
	public static final int		N_ACTION_RSP			= 0x8130;
	public static final int		N_CREATE_RQ				= 0x0140;
	public static final int		N_CREATE_RSP			= 0x8140;
	public static final int		N_DELETE_RQ				= 0x0150;
	public static final int		N_DELETE_RSP			= 0x8150;
	public static final int		C_CANCEL_RQ				= 0x0FFF;
	public static final int		MEDIUM					= 0x0000;
	public static final int		HIGH					= 0x0001;
	public static final int		LOW						= 0x0002;
	public static final int		NO_DATASET				= 0x0101;
	public static final int		A_ASSOCIATE_RQ			= 1;
	public static final int		A_ASSOCIATE_AC			= 2;
	public static final int		A_ASSOCIATE_RJ			= 3;
	public static final int		P_DATA_TF				= 4;
	public static final int		A_RELEASE_RQ			= 5;
	public static final int		A_RELEASE_RP			= 6;
	public static final int		A_ABORT					= 7;
	static List					elementList				= new ArrayList();
	static List					transferSyntaxList		= new ArrayList();
	static List					SOPClassList			= new ArrayList();
	static List					statusList				= new ArrayList();

	public static List getElementList()
	{
		return elementList;
	}

	public static List getTransferSyntaxList()
	{
		return transferSyntaxList;
	}

	public static List getSOPClassList()
	{
		return SOPClassList;
	}

	public static List getStatusList()
	{
		return statusList;
	}
	static File	dataRoot;

	// static DicomStore dataBase;
	// public static DicomStore getDataBase() {
	// return dataBase;
	// }
	public static File createTempFile() throws Exception
	{
		// todo: we are using null dataroot so temps stored in default temp
		return MSJavaHack.get().createTempFile("temp", "tmp", dataRoot);
	}
	public static int	maxPDU	= 32768;
	static boolean		isInit	= false;

	// public static void init() throws Exception {
	// Dicom.init(null, new File("conf/dictionary.xml"));
	// }
	public static void initForViewer(String dictionaryPath) throws Exception
	{
		if (isInit)
			return;
		Dicom.init("ae", new XML(new File(dictionaryPath)));
		isInit = true;
	}

	private static void init(AE ae) throws Exception
	{
		if (isInit)
			return;
		Dicom.init(ae, new File("conf/dictionary.xml"));
		isInit = true;
	}

	private static void init(String aeString) throws Exception
	{
		if (isInit)
			return;
		XML dict = XMLConfigFile.getDefault().getSubconfig("dictionary.xml");
		XML adminConf = XMLConfigFile.getDefault().getSubconfig("metaadmin.xml");
		Dicom.init(aeString, dict, adminConf);
		isInit = true;
	}

	public static void init() throws Exception
	{
		if (isInit)
			return;
		XML dict = XMLConfigFile.getDefault().getSubconfig("dictionary.xml");
		XML adminConf = XMLConfigFile.getDefault().getSubconfig("metaadmin.xml");
		String aeName = XMLConfigFile.getDefault().getXML().get("ae");
		Dicom.init(aeName, dict, adminConf);
		isInit = true;
	}

	// new 7/26 use this>
	public static void init(String aeName, String metaRoot) throws Exception
	{
		if (isInit)
			return;
		File root = new File(metaRoot);
		XML dict = new XML(new File(root, "conf/dictionary.xml"));
		XML adminConf = new XML(new File(root, "conf/metaadmin.xml"));
		Dicom.init(aeName, dict, adminConf);
		AEMap.load(adminConf);
		AEMap.setDefault(AEMap.get(aeName));
		isInit = true;
	}

	private static void init(String ae, XML dict, XML adminConf) throws Exception
	{
		if (isInit)
			return;
		log("dicom.init");
		AEMap.load(adminConf);
		AEMap.setDefault(AEMap.get(ae));
		init(ae, dict);
		isInit = true;
	}

	public static void updateAEMap(XML adminConf) throws Exception
	{
		// nb: never removes
		AEMap.load(adminConf);
	}

	private static void init(String ae, XML dict) throws Exception
	{
		// AEMap.load(new XML(new File("conf/ae.xml")));
		if (isInit)
			return;
		// AEMap.setDefault(AEMap.get(ae));
		List l = dict.getList();
		Iterator iter = l.listIterator();
		while (iter.hasNext())
		{
			XML xml = (XML) iter.next();
			// Log ("xml:"+xml);
			Iterator childListIter = xml.getList().iterator();
			// String name = xml.getName();
			while (childListIter.hasNext())
			{
				XML childXML = (XML) childListIter.next();
				// Log ("child:"+childXML);
				if (childXML.getName().equalsIgnoreCase("element"))
				{
					Tag tag = loadTag(xml.get("type"), childXML);
					elementList.add(tag);
				}
				else if (childXML.getName().equalsIgnoreCase("uid"))
				{
					UID uid = loadUID(xml.get("type"), childXML);
					if (uid.getType().equals("TransferSyntax"))
						transferSyntaxList.add(uid);
					else if (uid.getType().equals("SOPClass"))
						SOPClassList.add(uid);
				}
				else if (childXML.getName().equalsIgnoreCase("status"))
				{
					Status status = loadStatus(xml.get("service"), childXML);
					statusList.add(status);
				}
			}
		}
		isInit = true;
	}

	public static void init(AE ae, File initFile) throws Exception
	{
		if (isInit)
			return;
		AEMap.setDefault(ae);
		XML x = new XML(initFile);
		List l = x.getList();
		Iterator iter = l.listIterator();
		while (iter.hasNext())
		{
			XML xml = (XML) iter.next();
			// Log ("xml:"+xml);
			Iterator childListIter = xml.getList().iterator();
			// String name = xml.getName();
			while (childListIter.hasNext())
			{
				XML childXML = (XML) childListIter.next();
				// Log ("child:"+childXML);
				if (childXML.getName().equalsIgnoreCase("element"))
				{
					Tag tag = loadTag(xml.get("type"), childXML);
					elementList.add(tag);
				}
				else if (childXML.getName().equalsIgnoreCase("uid"))
				{
					UID uid = loadUID(xml.get("type"), childXML);
					if (uid.getType().equals("TransferSyntax"))
						transferSyntaxList.add(uid);
					else if (uid.getType().equals("SOPClass"))
						SOPClassList.add(uid);
				}
				else if (childXML.getName().equalsIgnoreCase("status"))
				{
					Status status = loadStatus(xml.get("service"), childXML);
					statusList.add(status);
				}
			}
		}
		AEMap.load(new XML(new File("conf/metaadmin.xml")));
		dataRoot = new File("database");
		// dataBase = new DicomStore(dataRoot);
		isInit = true;
	}

	static Tag loadTag(String type, XML xml)
	{
		String tag = StringUtil.replaceAll(xml.get("tag"), "x", "0");
		int group = Integer.parseInt(tag.substring(1, 5), 16);
		int elem = Integer.parseInt(tag.substring(6, 10), 16);
		String name = xml.get("name");
		VR vr = VR.get(xml.get("vr", "NO"));
		String vm = xml.get("vm", "1");
		int max = 1;
		if (Character.toUpperCase(vm.charAt(vm.length() - 1)) == 'N')
			max = 1000000;
		else if (vm.length() != 1 && vm.charAt(1) == '-')
			max = Integer.parseInt(vm.substring(2));
		String key = xml.get("key");
		boolean r = xml.get("retired", "false").equalsIgnoreCase("true");
		Tag.get(group, elem).init(type, name, vr, max, key, r);
		return Tag.get(group, elem);
	}

	static Status loadStatus(String type, XML xml)
	{
		int code = Integer.parseInt(StringUtil.replaceAll(xml.get("code"), "x", "0"), 16);
		String name = xml.get("name");
		String key = xml.get("key");
		Status status = Status.get(code);
		status.setKey(key);
		status.setName(name);
		status.setType(type);
		return status;
	}

	static UID loadUID(String type, XML xml)
	{
		String uids = xml.get("value");
		String name = xml.get("name");
		String key = xml.get("key");
		UID uid = UID.get(uids);
		uid.setKey(key);
		uid.setName(name);
		uid.setType(type);
		return uid;
	}
	// static void
}
