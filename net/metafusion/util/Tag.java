package net.metafusion.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import acme.util.Buffer;
import acme.util.StringUtil;
import acme.util.Util;

public class Tag implements Comparable
{
	public int compareTo(Object o)
	{
		Tag a = (Tag) o;
		int s = group - a.group;
		return s != 0 ? s : id - a.id;
	}
	static HashMap	hm	= new HashMap();

	synchronized public static boolean exists(int tag)
	{
		return hm.containsKey(new Integer(tag));
	}

	synchronized public static Tag get(int tag)
	{
		Tag t = (Tag) hm.get(new Integer(tag));
		if (t == null)
			t = new Tag(tag);
		return t;
	}

	static Tag copyTag(int group, int id, Tag copy)
	{
		boolean newTag = !Tag.exists((group << 16) | (id & 0xFFFF));
		Tag tag = get((group << 16) | (id & 0xFFFF));
		if (newTag)
			tag.copy(copy);
		return tag;
	}

	synchronized public static Tag get(int group, int id)
	{
		if (id == 0)
			return copyTag(group, id, GroupLength);
		else if (((group & 1) == 0) && (group != 0x5000) && (group & 0x0FF00) == 0x5000)
			return copyTag(group, id, get(group & 0x0FF00, id));
		else if (((group & 1) == 0) && (group != 0x6000) && (group & 0x0FF00) == 0x6000)
			return copyTag(group, id, get(group & 0x0FF00, id));
		else
			return get((group << 16) | (id & 0xFFFF));
	}

	public static Tag get(byte[] b, int offset)
	{
		// *** can tags be big endian????
		int h = Util.decodeShortLE(b, offset);
		int l = Util.decodeShortLE(b, offset + 2);
		return get(h, l);
	}

	public static Tag get(Buffer b)
	{
		// *** can tags be big endian????
		int h = b.getShort();
		int l = b.getShort();
		return get(h, l);
	}

	public Tag(int id)
	{
		this((id >>> 16) & 0xFFFF, id & 0x0FFFF);
	}

	public Tag(int group, int id)
	{
		tag = (group << 16) | (id & 0xFFFF);
		this.group = group;
		this.id = id;
		hm.put(new Integer(tag), this);
		init("Unknown", "Unknown", VR.UN, 1, "Unknown", false);
	}

	void copy(Tag t)
	{
		this.vr = t.vr;
		this.name = t.name;
		this.key = t.key;
		this.isRetired = t.isRetired;
		this.max = t.max;
		this.stringRep = "[" + Integer.toHexString(group).toUpperCase() + "," + Integer.toHexString(id).toUpperCase() + "]" + key + "[" + vr;
		if (max == 1000000)
			this.stringRep += ":N]";
		else
			this.stringRep += ":" + max + "]";
		tagString = "(" + StringUtil.hex4(group) + "," + StringUtil.hex4(id) + ")";
	}

	public void init(String type, String name, VR vr, int max, String key, boolean isRetired)
	{
		this.vr = vr;
		this.name = name;
		this.key = key;
		this.isRetired = isRetired;
		this.max = max;
		this.stringRep = "[" + Integer.toHexString(group).toUpperCase() + "," + Integer.toHexString(id).toUpperCase() + "]" + key + "[" + vr;
		if (max == 1000000)
			this.stringRep += ":N]";
		else
			this.stringRep += ":" + max + "]";
		tagString = "(" + StringUtil.hex4(group) + "," + StringUtil.hex4(id) + ")";
		// Util.Log(""+StringUtil.hex8(tag)+"->"+vr.getName());
	}

	public static Tag get(String key)
	{
		Set<Integer> entries = hm.keySet();
		Iterator<Integer> i = entries.iterator();
		while (i.hasNext())
		{
			Integer intKey = i.next();
			Tag t = Tag.get(intKey);
			if (t.getKey().equalsIgnoreCase(key))
				return t;
		}
		return null;
	}

	public int hashCode()
	{
		return group ^ id;
	}

	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Tag))
			return false;
		Tag t = (Tag) o;
		return t.group == group && t.id == id;
	}

	public int getTag()
	{
		return tag;
	}

	public int getGroup()
	{
		return group;
	}

	public int getID()
	{
		return id;
	}

	public void setVR(VR vr)
	{
		this.vr = vr;
	}

	public VR getVR()
	{
		return vr;
	}

	public String getName()
	{
		return name;
	}

	public String getKey()
	{
		return key;
	}

	public int getMax()
	{
		return max;
	}

	public boolean isMultiValued()
	{
		return max != 1;
	}

	public boolean isRetired()
	{
		return isRetired;
	}
	int			tag;
	int			group;
	int			id;
	public VR	vr			= VR.NONE;
	String		name		= "Unknown";
	String		key			= "Unknown";
	int			max			= 1;
	boolean		isRetired	= false;
	String		stringRep	= "Unknown";
	String		tagString	= "()";

	public String getTagString()
	{
		return tagString;
	}

	public String toString()
	{
		return stringRep;
	}
	// (xxxx,0000) VR=UL Group Length
	public static final Tag	GroupLength									= new Tag(0x00000000);
	// (0000,0001) VR=UN (Command) Length to End (Retired)
	public static final Tag	CommandLengthToEndRetired					= new Tag(0x00000001);
	// (0000,0010) VR=UN (Command) Recognition Code (Retired)
	public static final Tag	CommandRecognitionCodeRetired				= new Tag(0x00000010);
	// (0000,0002) VR=UI Affected SOP Class uid
	public static final Tag	AffectedSOPClassUID							= new Tag(0x00000002);
	// (0000,0003) VR=UI Requested SOP Class uid
	public static final Tag	RequestedSOPClassUID						= new Tag(0x00000003);
	// (0000,0100) VR=US Command Field
	public static final Tag	CommandField								= new Tag(0x00000100);
	// (0000,0110) VR=US Message ID
	public static final Tag	MessageID									= new Tag(0x00000110);
	// (0000,0120) VR=US Message ID Being Responded To
	public static final Tag	MessageIDToBeingRespondedTo					= new Tag(0x00000120);
	// (0000,0200) VR=UN Initiator (Retired)
	public static final Tag	InitiatorRetired							= new Tag(0x00000200);
	// (0000,0300) VR=UN Receiver (Retired)
	public static final Tag	ReceiverRetired								= new Tag(0x00000300);
	// (0000,0400) VR=UN Find Location (Retired)
	public static final Tag	FindLocationRetired							= new Tag(0x00000400);
	// (0000,0600) VR=AE Move Destination
	public static final Tag	MoveDestination								= new Tag(0x00000600);
	// (0000,0700) VR=US Priority
	public static final Tag	Priority									= new Tag(0x00000700);
	// (0000,0800) VR=US Message Set type
	public static final Tag	DataSetType									= new Tag(0x00000800);
	// (0000,0850) VR=UN Number of Matches (Retired)
	public static final Tag	NumberOfMatchesRetired						= new Tag(0x00000850);
	// (0000,0860) VR=UN Response Sequence Number (Retired)
	public static final Tag	ResponseSequenceNumberRetired				= new Tag(0x00000860);
	// (0000,0900) VR=US Status
	public static final Tag	Status										= new Tag(0x00000900);
	// (0000,0901) VR=AT Offending element
	public static final Tag	OffendingElement							= new Tag(0x00000901);
	// (0000,0902) VR=LO Error Comment
	public static final Tag	ErrorComment								= new Tag(0x00000902);
	// (0000,0903) VR=US Error ID
	public static final Tag	ErrorID										= new Tag(0x00000903);
	// (0000,1000) VR=UI Affected SOP Instance uid
	public static final Tag	AffectedSOPInstanceUID						= new Tag(0x00001000);
	// (0000,1001) VR=UI Requested SOP Instance uid
	public static final Tag	RequestedSOPInstanceUID						= new Tag(0x00001001);
	// (0000,1002) VR=US Event type ID
	public static final Tag	EventTypeID									= new Tag(0x00001002);
	// (0000,1005) VR=AT Attribute Identifier List
	public static final Tag	AttributeIdentifierList						= new Tag(0x00001005);
	// (0000,1008) VR=US Action type ID
	public static final Tag	ActionTypeID								= new Tag(0x00001008);
	// (0000,1020) VR=US Number of Remaining Sub-operations
	public static final Tag	NumberOfRemainingSubOperations				= new Tag(0x00001020);
	// (0000,1021) VR=US Number of Completed Sub-operations
	public static final Tag	NumberOfCompletedSubOperations				= new Tag(0x00001021);
	// (0000,1022) VR=US Number of Failed Sub-operations
	public static final Tag	NumberOfFailedSubOperations					= new Tag(0x00001022);
	// (0000,1023) VR=US Number of Warning Sub-operations
	public static final Tag	NumberOfWarningSubOperations				= new Tag(0x00001023);
	// (0000,1030) VR=AE Move Originator Application Entity Title
	public static final Tag	MoveOriginatorAET							= new Tag(0x00001030);
	// (0000,1031) VR=US Move Originator Message ID
	public static final Tag	MoveOriginatorMessageID						= new Tag(0x00001031);
	// (0000,4000) VR=UN Dialog Receiver (Retired)
	public static final Tag	DialogReceiverRetired						= new Tag(0x00004000);
	// (0000,4010) VR=UN Terminal Type (Retired)
	public static final Tag	TerminalTypeRetired							= new Tag(0x00004010);
	// (0000,5010) VR=UN Message Set ID (Retired)
	public static final Tag	MessageSetIDRetired							= new Tag(0x00005010);
	// (0000,5020) VR=UN End Message ID (Retired)
	public static final Tag	EndMessageIDRetired							= new Tag(0x00005020);
	// (0000,5110) VR=UN Display Format (Retired)
	public static final Tag	DisplayFormatRetired						= new Tag(0x00005110);
	// (0000,5120) VR=UN Page Position ID (Retired)
	public static final Tag	PagePositionIDRetired						= new Tag(0x00005120);
	// (0000,5130) VR=UN Text Format ID (Retired)
	public static final Tag	TextFormatIDRetired							= new Tag(0x00005130);
	// (0000,5140) VR=UN Nor/Rev (Retired)
	public static final Tag	NorRevRetired								= new Tag(0x00005140);
	// (0000,5150) VR=UN Add Gray Scale (Retired)
	public static final Tag	AddGrayScaleRetired							= new Tag(0x00005150);
	// (0000,5160) VR=UN Borders (Retired)
	public static final Tag	BordersRetired								= new Tag(0x00005160);
	// (0000,5170) VR=UN Copies (Retired)
	public static final Tag	CopiesRetired								= new Tag(0x00005170);
	// (0000,5180) VR=UN Magnification Type (Retired)
	public static final Tag	MagnificationTypeRetired					= new Tag(0x00005180);
	// (0000,5190) VR=UN Erase (Retired)
	public static final Tag	EraseRetired								= new Tag(0x00005190);
	// (0000,51A0) VR=UN Print (Retired)
	public static final Tag	PrintRetired								= new Tag(0x000051A0);
	// (0002,0001) VR=OB File Meta Information Version
	public static final Tag	FileMetaInformationVersion					= new Tag(0x00020001);
	// (0002,0002) VR=UI Media Storage SOP Class UID
	public static final Tag	MediaStorageSOPClassUID						= new Tag(0x00020002);
	// (0002,0003) VR=UI Media Storage SOP Instance UID
	public static final Tag	MediaStorageSOPInstanceUID					= new Tag(0x00020003);
	// (0002,0010) VR=UI Transfer Syntax UID
	public static final Tag	TransferSyntaxUID							= new Tag(0x00020010);
	// (0002,0012) VR=UI Implementation Class UID
	public static final Tag	ImplementationClassUID						= new Tag(0x00020012);
	// (0002,0013) VR=SH Implementation Version Name
	public static final Tag	ImplementationVersionName					= new Tag(0x00020013);
	// (0002,0016) VR=AE Source Application Entity Title
	public static final Tag	SourceApplicationEntityTitle				= new Tag(0x00020016);
	// (0002,0100) VR=UI Private Information Creator UID
	public static final Tag	PrivateInformationCreatorUID				= new Tag(0x00020100);
	// (0002,0102) VR=OB Private Information
	public static final Tag	PrivateInformation							= new Tag(0x00020102);
	public static final Tag	RetrieveAET									= new Tag(0x00080054);
	public static final Tag	SOPClassUID									= new Tag(0x00080016);
	public static final Tag	SOPInstanceUID								= new Tag(0x00080018);
	public static final Tag	StudyInstanceUID							= new Tag(0x0020000D);
	public static final Tag	SeriesInstanceUID							= new Tag(0x0020000E);
	public static final Tag	QueryRetrieveLevel							= new Tag(0x00080052);
	// (0004,1130) VR=CS File-set ID
	public static final Tag	FileSetID									= new Tag(0x00041130);
	// (0004,1141) VR=CS File-set Descriptor File ID
	public static final Tag	FileSetDescriptorFileID						= new Tag(0x00041141);
	// (0004,1142) VR=CS Specific Character Set of File-set Descriptor File
	public static final Tag	SpecificCharacterSetOfFileSetDescriptorFile	= new Tag(0x00041142);
	// (0004,1200) VR=UL Offset of the First Directory Record of the Root
	// Directory Entity
	public static final Tag	RootDirectoryFirstRecord					= new Tag(0x00041200);
	// (0004,1202) VR=UL Offset of the Last Directory Record of the Root
	// Directory Entity
	public static final Tag	RootDirectoryLastRecord						= new Tag(0x00041202);
	// (0004,1212) VR=US File-set Consistency Flag
	public static final Tag	FileSetConsistencyFlag						= new Tag(0x00041212);
	// (0004,1220) VR=SQ Directory Record Sequence
	public static final Tag	DirectoryRecordSequence						= new Tag(0x00041220);
	// (0004,1400) VR=UL Offset of the Next Directory Record
	public static final Tag	NextDirectoryRecord							= new Tag(0x00041400);
	// (0004,1410) VR=US Record In-use Flag
	public static final Tag	RecordInUseFlag								= new Tag(0x00041410);
	// (0004,1420) VR=UL Offset of Referenced Lower-Level Directory Entity
	public static final Tag	LowerLevelDirectoryOffset					= new Tag(0x00041420);
	// (0004,1430) VR=CS Directory Record Type
	public static final Tag	DirectoryRecordType							= new Tag(0x00041430);
	// (0004,1432) VR=UI Private Record UID
	public static final Tag	PrivateRecordUID							= new Tag(0x00041432);
	// (0004,1500) VR=CS Referenced File ID
	public static final Tag	RefFileID									= new Tag(0x00041500);
	// (0004,1504) VR=UL MRDR Directory Record Offset
	public static final Tag	MRDRDirectoryRecordOffset					= new Tag(0x00041504);
	// (0004,1510) VR=UI Referenced SOP Class UID in File
	public static final Tag	RefSOPClassUIDInFile						= new Tag(0x00041510);
	// (0004,1511) VR=UI Referenced SOP Instance UID in File
	public static final Tag	RefSOPInstanceUIDInFile						= new Tag(0x00041511);
	// (0004,1512) VR=UI Referenced SOP Transfer Syntax UID in File
	public static final Tag	RefSOPTransferSyntaxUIDInFile				= new Tag(0x00041512);
	// (0004,1600) VR=UL Number of References
	// public static final Tag NumberOfReferences = new Tag(0x00041600);
	// (0008,0001) VR=UN Length to End (Retired)
	// public static final Tag LengthToEndRetired = new Tag(0x00080001);
	// (0008,0005) VR=CS Specific Character Set
	// public static final Tag SpecificCharacterSet = new Tag(0x00080005);
	// (0008,0008) VR=CS Image Type
	public static final Tag	ImageType									= new Tag(0x00080008);
	// (0008,0010) VR=UN Recognition Code (Retired)
	// public static final Tag RecognitionCodeRetired = new Tag(0x00080010);
	// (0008,0012) VR=DA Instance Creation Date
	public static final Tag	InstanceCreationDate						= new Tag(0x00080012);
	// (0008,0013) VR=TM Instance Creation Time
	public static final Tag	InstanceCreationTime						= new Tag(0x00080013);
	// (0008,0014) VR=UI Instance Creator UID
	public static final Tag	InstanceCreatorUID							= new Tag(0x00080014);
	// (0008,0016) VR=UI SOP Class UID
	// public static final Tag SOPClassUID = new Tag(0x00080016);
	// (0008,0018) VR=UI SOP Instance UID
	// public static final Tag SOPInstanceUID = new Tag(0x00080018);
	// (0008,0020) VR=DA Study Date
	public static final Tag	StudyDate									= new Tag(0x00080020);
	// (0008,0021) VR=DA Series Date
	public static final Tag	SeriesDate									= new Tag(0x00080021);
	// (0008,0022) VR=DA Acquisition Date
	public static final Tag	AcquisitionDate								= new Tag(0x00080022);
	// (0008,0023) VR=DA Content Date
	public static final Tag	ContentDate									= new Tag(0x00080023);
	// (0008,0024) VR=DA Overlay Date
	// public static final Tag OverlayDate = new Tag(0x00080024);
	// (0008,0025) VR=DA Curve Date
	// public static final Tag CurveDate = new Tag(0x00080025);
	// (0008,002A) VR=DT Acquisition Datetime
	public static final Tag	AcquisitionDatetime							= new Tag(0x0008002A);
	// (0008,0030) VR=TM Study Time
	public static final Tag	StudyTime									= new Tag(0x00080030);
	// (0008,0031) VR=TM Series Time
	public static final Tag	SeriesTime									= new Tag(0x00080031);
	// (0008,0032) VR=TM Acquisition Time
	public static final Tag	AcquisitionTime								= new Tag(0x00080032);
	// (0008,0033) VR=TM Content Time
	public static final Tag	ContentTime									= new Tag(0x00080033);
	// (0008,0034) VR=TM Overlay Time
	// public static final Tag OverlayTime = new Tag(0x00080034);
	// (0008,0035) VR=TM Curve Time
	// public static final Tag CurveTime = new Tag(0x00080035);
	// (0008,0040) VR=UN Message Set Type (Retired)
	// public static final Tag DataSetTypeRetired = new Tag(0x00080040);
	// (0008,0041) VR=UN Message Set Subtype (Retired)
	// public static final Tag DataSetSubtypeRetired = new Tag(0x00080041);
	// (0008,0042) VR=CS Nuclear Medicine Series Type (Retired)
	// public static final Tag NuclearMedicineSeriesTypeRetired = new
	// Tag(0x00080042);
	// (0008,0050) VR=SH Accession Number
	public static final Tag	AccessionNumber								= new Tag(0x00080050);
	// (0008,0052) VR=CS Query/Retrieve Level
	// public static final Tag QueryRetrieveLevel = new Tag(0x00080052);
	// (0008,0054) VR=AE Retrieve AE Title
	// public static final Tag RetrieveAET = new Tag(0x00080054);
	// (0008,0056) VR=CS Instance Availability
	// public static final Tag InstanceAvailability = new Tag(0x00080056);
	// (0008,0058) VR=UI Failed SOP Instance UID List
	// public static final Tag FailedSOPInstanceUIDList = new Tag(0x00080058);
	// (0008,0060) VR=CS Modality
	public static final Tag	Modality									= new Tag(0x00080060);
	// (0008,0061) VR=CS Modalities in Study
	public static final Tag	ModalitiesInStudy							= new Tag(0x00080061);
	// (0008,0064) VR=CS Conversion Type
	// public static final Tag ConversionType = new Tag(0x00080064);
	// (0008,0068) VR=CS Presentation Intent Type
	// public static final Tag PresentationIntentType = new Tag(0x00080068);
	// (0008,0070) VR=LO Manufacturer
	public static final Tag	Manufacturer								= new Tag(0x00080070);
	// (0008,0080) VR=LO Institution Name
	public static final Tag	InstitutionName								= new Tag(0x00080080);
	// (0008,0081) VR=ST Institution Address
	// public static final Tag InstitutionAddress = new Tag(0x00080081);
	// (0008,0082) VR=SQ Institution Code Sequence
	// public static final Tag InstitutionCodeSeq = new Tag(0x00080082);
	// (0008,0090) VR=PN Referring Physician's Name
	public static final Tag	ReferringPhysicianName						= new Tag(0x00080090);
	// (0008,0092) VR=ST Referring Physician's Address
	public static final Tag	ReferringPhysicianAddress					= new Tag(0x00080092);
	// (0008,0094) VR=SH Referring Physician's Telephone Numbers
	// public static final Tag ReferringPhysicianPhoneNumbers = new
	// Tag(0x00080094);
	// (0008,0100) VR=SH Code Value
	// public static final Tag CodeValue = new Tag(0x00080100);
	// (0008,0102) VR=SH Coding Scheme Designator
	// public static final Tag CodingSchemeDesignator = new Tag(0x00080102);
	// (0008,0103) VR=SH Coding Scheme Version
	// public static final Tag CodingSchemeVersion = new Tag(0x00080103);
	// (0008,0104) VR=LO Code Meaning
	// public static final Tag CodeMeaning = new Tag(0x00080104);
	// (0008,0105) VR=CS Mapping Resource
	// public static final Tag MappingResource = new Tag(0x00080105);
	// (0008,0106) VR=DT Context Group Version
	// public static final Tag ContextGroupVersion = new Tag(0x00080106);
	// (0008,0107) VR=DT Context Group Local Version
	// public static final Tag ContextGroupLocalVersion = new Tag(0x00080107);
	// (0008,010B) VR=CS Code Set Extension Flag
	// public static final Tag CodeSetExtensionFlag = new Tag(0x0008010B);
	// (0008,010C) VR=UI Private Coding Scheme Creator UID
	// public static final Tag PrivateCodingSchemeCreatorUID = new
	// Tag(0x0008010C);
	// (0008,010D) VR=UI Code Set Extension Creator UID
	// public static final Tag CodeSetExtensionCreatorUID = new Tag(0x0008010D);
	// (0008,010F) VR=CS Context Identifier
	// public static final Tag ContextIdentifier = new Tag(0x0008010F);
	// (0008,0201) VR=SH Timezone Offset From UTC
	// public static final Tag TimezoneOffsetFromUTC = new Tag(0x00080201);
	// (0008,1000) VR=UN Network ID (Retired)
	// public static final Tag NetworkIDRetired = new Tag(0x00081000);
	// (0008,1010) VR=SH Station Name
	public static final Tag	StationName									= new Tag(0x00081010);
	// (0008,1030) VR=LO Study Description
	public static final Tag	StudyDescription							= new Tag(0x00081030);
	// (0008,1032) VR=SQ Procedure Code Sequence
	// public static final Tag ProcedureCodeSeq = new Tag(0x00081032);
	// (0008,103E) VR=LO Series Description
	public static final Tag	SeriesDescription							= new Tag(0x0008103E);
	// (0008,1040) VR=LO Institutional Department Name
	public static final Tag	InstitutionalDepartmentName					= new Tag(0x00081040);
	// (0008,1048) VR=PN Physician(s) of Record
	// public static final Tag PhysicianOfRecord = new Tag(0x00081048);
	// (0008,1050) VR=PN Performing Physician's Name
	public static final Tag	PerformingPhysicianName						= new Tag(0x00081050);
	// (0008,1060) VR=PN Name of Physician(s) Reading Study
	public static final Tag	NameOfPhysicianReadingStudy					= new Tag(0x00081060);
	// (0008,1070) VR=PN Operator's Name
	public static final Tag	OperatorName								= new Tag(0x00081070);
	// (0008,1080) VR=LO Admitting Diagnosis Description
	public static final Tag	AdmittingDiagnosisDescription				= new Tag(0x00081080);
	// (0008,1084) VR=SQ Admitting Diagnosis Code Sequence
	// public static final Tag AdmittingDiagnosisCodeSeq = new Tag(0x00081084);
	// (0008,1090) VR=LO Manufacturer's Model Name
	public static final Tag	ManufacturerModelName						= new Tag(0x00081090);
	// (0008,1100) VR=SQ Referenced Results Sequence
	// public static final Tag RefResultsSeq = new Tag(0x00081100);
	// (0008,1110) VR=SQ Referenced Study Sequence
	// public static final Tag RefStudySeq = new Tag(0x00081110);
	// (0008,1111) VR=SQ Referenced Study Component Sequence
	// public static final Tag RefStudyComponentSeq = new Tag(0x00081111);
	// (0008,1115) VR=SQ Referenced Series Sequence
	// public static final Tag RefSeriesSeq = new Tag(0x00081115);
	// (0008,1120) VR=SQ Referenced Patient Sequence
	// public static final Tag RefPatientSeq = new Tag(0x00081120);
	// (0008,1125) VR=SQ Referenced Visit Sequence
	// public static final Tag RefVisitSeq = new Tag(0x00081125);
	// (0008,1130) VR=SQ Referenced Overlay Sequence
	// public static final Tag RefOverlaySeq = new Tag(0x00081130);
	// (0008,1140) VR=SQ Referenced Image Sequence
	// public static final Tag RefImageSeq = new Tag(0x00081140);
	// (0008,1145) VR=SQ Referenced Curve Sequence
	// public static final Tag RefCurveSeq = new Tag(0x00081145);
	// (0008,114A) VR=SQ Referenced Instance Sequence
	// public static final Tag RefInstanceSeq = new Tag(0x0008114A);
	// (0008,1150) VR=UI Referenced SOP Class UID
	public static final Tag	RefSOPClassUID								= new Tag(0x00081150);
	// (0008,1155) VR=UI Referenced SOP Instance UID
	public static final Tag	RefSOPInstanceUID							= new Tag(0x00081155);
	// (0008,115A) VR=UI SOP Classes Supported
	// public static final Tag SOPClassesSupported = new Tag(0x0008115A);
	// (0008,1160) VR=IS Referenced Frame Number
	// public static final Tag RefFrameNumber = new Tag(0x00081160);
	// (0008,1195) VR=UI Transaction UID
	public static final Tag	TransactionUID								= new Tag(0x00081195);
	// (0008,1197) VR=US Failure Reason
	// public static final Tag FailureReason = new Tag(0x00081197);
	// (0008,1198) VR=SQ Failed SOP Sequence
	// public static final Tag FailedSOPSeq = new Tag(0x00081198);
	// (0008,1199) VR=SQ Referenced SOP Sequence
	public static final Tag	RefSOPSeq									= new Tag(0x00081199);
	// (0008,2110) VR=CS Lossy Image Compression (Retired)
	// public static final Tag LossyImageCompressionRetired = new
	// Tag(0x00082110);
	// (0008,2111) VR=ST Derivation Description
	// public static final Tag DerivationDescription = new Tag(0x00082111);
	// (0008,2112) VR=SQ Source Image Sequence
	// public static final Tag SourceImageSeq = new Tag(0x00082112);
	// (0008,2120) VR=SH Stage Name
	// public static final Tag StageName = new Tag(0x00082120);
	// (0008,2122) VR=IS Stage Number
	// public static final Tag StageNumber = new Tag(0x00082122);
	// (0008,2124) VR=IS Number of Stages
	// public static final Tag NumberOfStages = new Tag(0x00082124);
	// (0008,2128) VR=IS View Number
	// public static final Tag ViewNumber = new Tag(0x00082128);
	// (0008,2129) VR=IS Number of Event Timers
	// public static final Tag NumberOfEventTimers = new Tag(0x00082129);
	// (0008,212A) VR=IS Number of Views in Stage
	// public static final Tag NumberOfViewsInStage = new Tag(0x0008212A);
	// (0008,2130) VR=DS Event Elapsed Time(s)
	// public static final Tag EventElapsedTime = new Tag(0x00082130);
	// (0008,2132) VR=LO Event Timer Name(s)
	// public static final Tag EventTimerName = new Tag(0x00082132);
	// (0008,2142) VR=IS Start Trim
	// public static final Tag StartTrim = new Tag(0x00082142);
	// (0008,2143) VR=IS Stop Trim
	// public static final Tag StopTrim = new Tag(0x00082143);
	// (0008,2144) VR=IS Recommended Display Frame Rate
	// public static final Tag RecommendedDisplayFrameRate = new
	// Tag(0x00082144);
	// (0008,2200) VR=CS Transducer Position (Retired)
	// public static final Tag TransducerPositionRetired = new Tag(0x00082200);
	// (0008,2204) VR=CS Transducer Orientation (Retired)
	// public static final Tag TransducerOrientationRetired = new
	// Tag(0x00082204);
	// (0008,2208) VR=CS Anatomic Structure (Retired)
	// public static final Tag AnatomicStructureRetired = new Tag(0x00082208);
	// (0008,2218) VR=SQ Anatomic Region Sequence
	// public static final Tag AnatomicRegionSeq = new Tag(0x00082218);
	// (0008,2220) VR=SQ Anatomic Region Modifier Sequence
	// public static final Tag AnatomicRegionModifierSeq = new Tag(0x00082220);
	// (0008,2228) VR=SQ Primary Anatomic Structure Sequence
	// public static final Tag PrimaryAnatomicStructureSeq = new
	// Tag(0x00082228);
	// (0008,2229) VR=SQ Anatomic Structure, Space or Region Sequence
	// public static final Tag AnatomicStructureSpaceRegionSeq = new
	// Tag(0x00082229);
	// (0008,2230) VR=SQ Primary Anatomic Structure Modifier Sequence
	// public static final Tag PrimaryAnatomicStructureModifierSeq = new
	// Tag(0x00082230);
	// (0008,2240) VR=SQ Transducer Position Sequence
	// public static final Tag TransducerPositionSeq = new Tag(0x00082240);
	// (0008,2242) VR=SQ Transducer Position Modifier Sequence
	// public static final Tag TransducerPositionModifierSeq = new
	// Tag(0x00082242);
	// (0008,2244) VR=SQ Transducer Orientation Sequence
	// public static final Tag TransducerOrientationSeq = new Tag(0x00082244);
	// (0008,2246) VR=SQ Transducer Orientation Modifier Sequence
	// public static final Tag TransducerOrientationModifierSeq = new
	// Tag(0x00082246);
	// (0008,4000) VR=LT (Study) Comments (Retired)
	// public static final Tag StudyCommentsRetired = new Tag(0x00084000);
	// (0008,9007) VR=CS Frame Type
	public static final Tag	FrameType									= new Tag(0x00089007);
	// (0008,9121) VR=SQ Referenced Raw Message Sequence
	// public static final Tag RefRawDataSeq = new Tag(0x00089121);
	// (0008,9123) VR=UI Creator-Version UID
	// public static final Tag CreatorVersionUID = new Tag(0x00089123);
	// (0008,9124) VR=SQ Derivation Image Sequence
	// public static final Tag DerivationImageSeq = new Tag(0x00089124);
	// (0008,9092) VR=SQ Referring Image Evidence Sequence
	// public static final Tag ReferringImageEvidenceSeq = new Tag(0x00089092);
	// (0008,9154) VR=SQ Source Image Evidence Sequence
	// public static final Tag SourceImageEvidenceSeq = new Tag(0x00089154);
	// (0008,9205) VR=CS Pixel Presentation
	// public static final Tag PixelPresentation = new Tag(0x00089205);
	// (0008,9206) VR=UN Volumetric Properties CS
	// public static final Tag VolumetricProperties = new Tag(0x00089206);
	// (0008,9207) VR=CS Volume Based Calculation Technique
	// public static final Tag VolumeBasedCalculationTechnique = new
	// Tag(0x00089207);
	// (0008,9208) VR=CS Complex Image Component
	// public static final Tag ComplexImageComponent = new Tag(0x00089208);
	// (0008,9209) VR=CS Acquisition Contrast
	// public static final Tag AcquisitionContrast = new Tag(0x00089209);
	// (0008,9215) VR=SQ Derivation Code Sequence
	// public static final Tag DerivationCodeSeq = new Tag(0x00089215);
	// (0008,9237) VR=SQ Referenced Grayscale Presentation State Sequence
	// public static final Tag RefGrayscalePresentationStateSeq = new
	// Tag(0x00089237);
	// (0010,0010) VR=PN Patient's Name
	public static final Tag	PatientName									= new Tag(0x00100010);
	// (0010,0020) VR=LO Patient ID
	public static final Tag	PatientID									= new Tag(0x00100020);
	// (0010,0021) VR=LO Issuer of Patient ID
	// public static final Tag IssuerOfPatientID = new Tag(0x00100021);
	// (0010,0030) VR=DA Patient's Birth Date
	public static final Tag	PatientBirthDate							= new Tag(0x00100030);
	// (0010,0032) VR=TM Patient's Birth Time
	// public static final Tag PatientBirthTime = new Tag(0x00100032);
	// (0010,0040) VR=CS Patient's Sex
	public static final Tag	PatientSex									= new Tag(0x00100040);
	// (0010,0050) VR=SQ Patient's Insurance Plan Code Sequence
	// public static final Tag PatientInsurancePlanCodeSeq = new
	// Tag(0x00100050);
	// (0010,1000) VR=LO Other Patient IDs
	// public static final Tag OtherPatientIDs = new Tag(0x00101000);
	// (0010,1001) VR=PN Other Patient Names
	// public static final Tag OtherPatientNames = new Tag(0x00101001);
	// (0010,1005) VR=PN Patient's Birth Name
	// public static final Tag PatientBirthName = new Tag(0x00101005);
	// (0010,1010) VR=AS Patient's Age
	public static final Tag	PatientAge									= new Tag(0x00101010);
	// (0010,1020) VR=DS Patient's Size
	// public static final Tag PatientSize = new Tag(0x00101020);
	// (0010,1030) VR=DS Patient's Weight
	public static final Tag	PatientWeight								= new Tag(0x00101030);
	// (0010,1040) VR=LO Patient's Address
	// public static final Tag PatientAddress = new Tag(0x00101040);
	// (0010,1050) VR=LO Insurance Plan Identification (Retired)
	// public static final Tag InsurancePlanIdentificationRetired = new
	// Tag(0x00101050);
	// (0010,1060) VR=PN Patient's Mother's Birth Name
	// public static final Tag PatientMotherBirthName = new Tag(0x00101060);
	// (0010,1080) VR=LO Military Rank
	// public static final Tag MilitaryRank = new Tag(0x00101080);
	// (0010,1081) VR=LO Branch of DicomServiceProvider
	// public static final Tag BranchOfService = new Tag(0x00101081);
	// (0010,1090) VR=LO Medical Record Locator
	// public static final Tag MedicalRecordLocator = new Tag(0x00101090);
	// (0010,2000) VR=LO Medical Alerts
	// public static final Tag MedicalAlerts = new Tag(0x00102000);
	// (0010,2110) VR=LO Contrast Allergies
	// public static final Tag ContrastAllergies = new Tag(0x00102110);
	// (0010,2150) VR=LO Country of Residence
	// public static final Tag CountryOfResidence = new Tag(0x00102150);
	// (0010,2152) VR=LO Region of Residence
	// public static final Tag RegionOfResidence = new Tag(0x00102152);
	// (0010,2154) VR=SH Patient's Telephone Numbers
	// public static final Tag PatientPhoneNumbers = new Tag(0x00102154);
	// (0010,2160) VR=SH Ethnic Group
	// public static final Tag EthnicGroup = new Tag(0x00102160);
	// (0010,2180) VR=SH Occupation
	// public static final Tag Occupation = new Tag(0x00102180);
	// (0010,21A0) VR=CS Smoking Status
	// public static final Tag SmokingStatus = new Tag(0x001021A0);
	// (0010,21B0) VR=LT Additional Patient History
	public static final Tag	AdditionalPatientHistory					= new Tag(0x001021B0);
	// (0010,21C0) VR=US Pregnancy Status
	// public static final Tag PregnancyStatus = new Tag(0x001021C0);
	// (0010,21D0) VR=DA Last Menstrual Date
	// public static final Tag LastMenstrualDate = new Tag(0x001021D0);
	// (0010,21F0) VR=LO Patient's Religious Preference
	// public static final Tag PatientReligiousPreference = new Tag(0x001021F0);
	// (0010,4000) VR=LT Patient Comments
	// public static final Tag PatientComments = new Tag(0x00104000);
	// (0018,0010) VR=LO Contrast/Bolus Agent
	// public static final Tag ContrastBolusAgent = new Tag(0x00180010);
	// (0018,0012) VR=SQ Contrast/Bolus Agent Sequence
	// public static final Tag ContrastBolusAgentSeq = new Tag(0x00180012);
	// (0018,0014) VR=SQ Contrast/Bolus Administration Route Sequence
	// public static final Tag ContrastBolusAdministrationRouteSeq = new
	// Tag(0x00180014);
	// (0018,0015) VR=CS Body Part Examined
	public static final Tag	BodyPartExamined							= new Tag(0x00180015);
	// (0018,0020) VR=CS Scanning Sequence
	// public static final Tag ScanningSeq = new Tag(0x00180020);
	// (0018,0021) VR=CS Seq Variant
	// public static final Tag SeqVariant = new Tag(0x00180021);
	// (0018,0022) VR=CS Scan Options
	// public static final Tag ScanOptions = new Tag(0x00180022);
	// (0018,0023) VR=CS MR Acquisition Type
	// public static final Tag MRAcquisitionType = new Tag(0x00180023);
	// (0018,0024) VR=SH Sequence Name
	// public static final Tag SequenceName = new Tag(0x00180024);
	// (0018,0025) VR=CS Angio Flag
	// public static final Tag AngioFlag = new Tag(0x00180025);
	// (0018,0026) VR=SQ Intervention Drug Information Sequence
	// public static final Tag InterventionDrugInformationSeq = new
	// Tag(0x00180026);
	// (0018,0027) VR=TM Intervention Drug Stop Time
	// public static final Tag InterventionDrugStopTime = new Tag(0x00180027);
	// (0018,0028) VR=DS Intervention Drug Dose
	// public static final Tag InterventionDrugDose = new Tag(0x00180028);
	// (0018,0029) VR=SQ Intervention Drug Code Sequence
	// public static final Tag InterventionDrugCodeSeq = new Tag(0x00180029);
	// (0018,002A) VR=SQ Additional Drug Sequence
	// public static final Tag AdditionalDrugSeq = new Tag(0x0018002A);
	// (0018,0030) VR=LO Radionuclide (Retired)
	// public static final Tag RadionuclideRetired = new Tag(0x00180030);
	// (0018,0031) VR=LO Radiopharmaceutical
	// public static final Tag Radiopharmaceutical = new Tag(0x00180031);
	// (0018,0032) VR=DS Energy Window Centerline (Retired)
	// public static final Tag EnergyWindowCenterlineRetired = new
	// Tag(0x00180032);
	// (0018,0033) VR=DS Energy Window Total Width (Retired)
	// public static final Tag EnergyWindowTotalWidthRetired = new
	// Tag(0x00180033);
	// (0018,0034) VR=LO Intervention Drug Name
	// public static final Tag InterventionDrugName = new Tag(0x00180034);
	// (0018,0035) VR=TM Intervention Drug Start Time
	// public static final Tag InterventionDrugStartTime = new Tag(0x00180035);
	// (0018,0036) VR=SQ Interventional Therapy Sequence
	// public static final Tag InterventionalTherapySeq = new Tag(0x00180036);
	// (0018,0037) VR=CS Therapy Type
	// public static final Tag TherapyType = new Tag(0x00180037);
	// (0018,0038) VR=CS Interventional Status
	// public static final Tag InterventionalStatus = new Tag(0x00180038);
	// (0018,0039) VR=CS Therapy Description
	// public static final Tag TherapyDescription = new Tag(0x00180039);
	// (0018,0040) VR=IS Cine Rate
	// public static final Tag CineRate = new Tag(0x00180040);
	// (0018,0050) VR=DS Slice Thickness
	public static final Tag	SliceThickness								= new Tag(0x00180050);
	// (0018,0060) VR=DS KVP
	// public static final Tag KVP = new Tag(0x00180060);
	// (0018,0070) VR=IS Counts Accumulated
	// public static final Tag CountsAccumulated = new Tag(0x00180070);
	// (0018,0071) VR=CS Acquisition Termination Condition
	// public static final Tag AcquisitionTerminationCondition = new
	// Tag(0x00180071);
	// (0018,0072) VR=DS Effective Series Duration
	// public static final Tag EffectiveSeriesDuration = new Tag(0x00180072);
	// (0018,0073) VR=CS Acquisition Start Condition
	// public static final Tag AcquisitionStartCondition = new Tag(0x00180073);
	// (0018,0074) VR=IS Acquisition Start Condition Message
	// public static final Tag AcquisitionStartConditionData = new
	// Tag(0x00180074);
	// (0018,0075) VR=IS Acquisition Termination Condition Message
	// public static final Tag AcquisitionTerminationConditionData = new
	// Tag(0x00180075);
	// (0018,0080) VR=DS Repetition Time
	// public static final Tag RepetitionTime = new Tag(0x00180080);
	// (0018,0081) VR=DS Echo Time
	// public static final Tag EchoTime = new Tag(0x00180081);
	// (0018,0082) VR=DS Inversion Time
	// public static final Tag InversionTime = new Tag(0x00180082);
	// (0018,0083) VR=DS Number of Averages
	// public static final Tag NumberOfAverages = new Tag(0x00180083);
	// (0018,0084) VR=DS Imaging Frequency
	// public static final Tag ImagingFrequency = new Tag(0x00180084);
	// (0018,0085) VR=SH Imaged Nucleus
	// public static final Tag ImagedNucleus = new Tag(0x00180085);
	// (0018,0086) VR=IS Echo Number(s)
	// public static final Tag EchoNumber = new Tag(0x00180086);
	// (0018,0087) VR=DS Magnetic Field Strength
	// public static final Tag MagneticFieldStrength = new Tag(0x00180087);
	// (0018,0088) VR=DS Spacing Between Slices
	public static final Tag	SpacingBetweenSlices						= new Tag(0x00180088);
	// (0018,0089) VR=IS Number of Phase Encoding Steps
	// public static final Tag NumberOfPhaseEncodingSteps = new Tag(0x00180089);
	// (0018,0090) VR=DS Message Collection Diameter
	// public static final Tag DataCollectionDiameter = new Tag(0x00180090);
	// (0018,0091) VR=IS Echo Train Length
	// public static final Tag EchoTrainLength = new Tag(0x00180091);
	// (0018,0093) VR=DS Percent Sampling
	// public static final Tag PercentSampling = new Tag(0x00180093);
	// (0018,0094) VR=DS Percent Phase Field of View
	// public static final Tag PercentPhaseFieldOfView = new Tag(0x00180094);
	// (0018,0095) VR=DS Pixel Bandwidth
	// public static final Tag PixelBandwidth = new Tag(0x00180095);
	// (0018,1000) VR=LO Device Serial Number
	// public static final Tag DeviceSerialNumber = new Tag(0x00181000);
	// (0018,1004) VR=LO Plate ID
	// public static final Tag PlateID = new Tag(0x00181004);
	// (0018,1010) VR=LO Secondary Capture Device ID
	// public static final Tag SecondaryCaptureDeviceID = new Tag(0x00181010);
	// (0018,1011) VR=LO Hardcopy Creation Device ID
	// public static final Tag HardcopyCreationDeviceID = new Tag(0x00181011);
	// (0018,1012) VR=DA Date of Secondary Capture
	// public static final Tag DateOfSecondaryCapture = new Tag(0x00181012);
	// (0018,1014) VR=TM Time of Secondary Capture
	// public static final Tag TimeOfSecondaryCapture = new Tag(0x00181014);
	// (0018,1016) VR=LO Secondary Capture Device Manufacturer
	// public static final Tag SecondaryCaptureDeviceManufacturer = new
	// Tag(0x00181016);
	// (0018,1017) VR=LO Hardcopy Device Manufacturer
	// public static final Tag HardcopyDeviceManufacturer = new Tag(0x00181017);
	// (0018,1018) VR=LO Secondary Capture Device Manufacturer's Model Name
	// public static final Tag SecondaryCaptureDeviceManufacturerModelName = new
	// Tag(0x00181018);
	// (0018,1019) VR=LO Secondary Capture Device Software Version(s)
	// public static final Tag SecondaryCaptureDeviceSoftwareVersion = new
	// Tag(0x00181019);
	// (0018,101A) VR=LO Hardcopy Device Software Version
	// public static final Tag HardcopyDeviceSoftwareVersion = new
	// Tag(0x0018101A);
	// (0018,101B) VR=LO Hardcopy Device Manfuacturer's Model Name
	// public static final Tag HardcopyDeviceManfuacturerModelName = new
	// Tag(0x0018101B);
	// (0018,1020) VR=LO Software Version(s)
	// public static final Tag SoftwareVersion = new Tag(0x00181020);
	// (0018,1022) VR=SH Video Image Format Acquired
	// public static final Tag VideoImageFormatAcquired = new Tag(0x00181022);
	// (0018,1023) VR=LO Digital Image Format Acquired
	// public static final Tag DigitalImageFormatAcquired = new Tag(0x00181023);
	// (0018,1030) VR=LO Protocol Name
	public static final Tag	ProtocolName								= new Tag(0x00181030);
	public static final Tag	PatientPosition								= new Tag(0x00185100);
	// (0018,5101) VR=CS View Position
	public static final Tag	ViewPosition								= new Tag(0x00185101);
	// public static final Tag StudyInstanceUID = new Tag(0x0020000D);
	// (0020,000E) VR=UI Series Instance UID
	// public static final Tag SeriesInstanceUID = new Tag(0x0020000E);
	// (0020,0010) VR=SH Study ID
	public static final Tag	StudyID										= new Tag(0x00200010);
	// (0020,0011) VR=IS Series Number
	public static final Tag	SeriesNumber								= new Tag(0x00200011);
	// (0020,0012) VR=IS Acquisition Number
	public static final Tag	AcquisitionNumber							= new Tag(0x00200012);
	// (0020,0013) VR=IS Instance Number
	public static final Tag	InstanceNumber								= new Tag(0x00200013);
	// (0020,0014) VR=IS Isotope Number (Retired)
	// public static final Tag IsotopeNumberRetired = new Tag(0x00200014);
	// (0020,0015) VR=IS Phase Number (Retired)
	// public static final Tag PhaseNumberRetired = new Tag(0x00200015);
	// (0020,0016) VR=IS Interval Number (Retired)
	// public static final Tag IntervalNumberRetired = new Tag(0x00200016);
	// (0020,0017) VR=IS Time Slot Number (Retired)
	// public static final Tag TimeSlotNumberRetired = new Tag(0x00200017);
	// (0020,0018) VR=IS Angle Number (Retired)
	// public static final Tag AngleNumberRetired = new Tag(0x00200018);
	// (0020,0019) VR=IS Item Number
	// public static final Tag ItemNumber = new Tag(0x00200019);
	// (0020,0020) VR=CS Patient Orientation
	// public static final Tag PatientOrientation = new Tag(0x00200020);
	// (0020,0022) VR=IS Overlay Number
	// public static final Tag OverlayNumber = new Tag(0x00200022);
	// (0020,0024) VR=IS Curve Number
	// public static final Tag CurveNumber = new Tag(0x00200024);
	// (0020,0026) VR=IS Lookup Table Number
	// public static final Tag LUTNumber = new Tag(0x00200026);
	// (0020,0030) VR=DS Image Position (Retired)
	// public static final Tag ImagePositionRetired = new Tag(0x00200030);
	// (0020,0032) VR=DS Image Position (Patient)
	public static final Tag	ImagePosition								= new Tag(0x00200032);
	// (0020,0035) VR=DS Image Orientation (Retired)
	// public static final Tag ImageOrientationRetired = new Tag(0x00200035);
	// (0020,0037) VR=DS Image Orientation (Patient)
	public static final Tag	ImageOrientation							= new Tag(0x00200037);
	// (0020,0050) VR=UN Location (Retired)
	// public static final Tag LocationRetired = new Tag(0x00200050);
	// (0020,0052) VR=UI Frame of Reference UID
	// public static final Tag FrameOfReferenceUID = new Tag(0x00200052);
	// (0020,0060) VR=CS Laterality
	// public static final Tag Laterality = new Tag(0x00200060);
	// (0020,0062) VR=CS Image Laterality
	// public static final Tag ImageLaterality = new Tag(0x00200062);
	// (0020,0070) VR=CS Image Geometry Type (Retired)
	// public static final Tag ImageGeometryTypeRetired = new Tag(0x00200070);
	// (0020,0080) VR=CS Masking Image (Retired)
	// public static final Tag MaskingImageRetired = new Tag(0x00200080);
	// (0020,0100) VR=IS Temporal Position Identifier
	// public static final Tag TemporalPositionIdentifier = new Tag(0x00200100);
	// (0020,0105) VR=IS Number of Temporal Positions
	// public static final Tag NumberOfTemporalPositions = new Tag(0x00200105);
	// (0020,0110) VR=DS Temporal Resolution
	// public static final Tag TemporalResolution = new Tag(0x00200110);
	// (0020,0200) VR=UI Synchronization Frame of Reference UID
	// public static final Tag SynchronizationFrameOfReferenceUID = new
	// Tag(0x00200200);
	// (0020,1000) VR=IS Series in Study
	// public static final Tag SeriesInStudy = new Tag(0x00201000);
	// (0020,1001) VR=IS Acquisitions in Series (Retired)
	// public static final Tag AcquisitionsInSeriesRetired = new
	// Tag(0x00201001);
	// (0020,1002) VR=IS Images in Acquisition
	// public static final Tag ImagesInAcquisition = new Tag(0x00201002);
	// (0020,1004) VR=IS Acquisitions in Study
	// public static final Tag AcquisitionsInStudy = new Tag(0x00201004);
	// (0020,1020) VR=UN Reference (Retired)
	// public static final Tag ReferenceRetired = new Tag(0x00201020);
	// (0020,1040) VR=LO Position Reference Indicator
	// public static final Tag PositionReferenceIndicator = new Tag(0x00201040);
	// (0020,1041) VR=DS Slice Location
	public static final Tag	SliceLocation								= new Tag(0x00201041);
	// (0020,1070) VR=IS Other Study Numbers
	// public static final Tag OtherStudyNumbers = new Tag(0x00201070);
	// (0020,1200) VR=IS Number of Patient Related Studies
	public static final Tag	NumberOfPatientRelatedStudies				= new Tag(0x00201200);
	// (0020,1202) VR=IS Number of Patient Related Series
	public static final Tag	NumberOfPatientRelatedSeries				= new Tag(0x00201202);
	// (0020,1204) VR=IS Number of Patient Related Instances
	public static final Tag	NumberOfPatientRelatedInstances				= new Tag(0x00201204);
	// (0020,1206) VR=IS Number of Study Related Series
	public static final Tag	NumberOfStudyRelatedSeries					= new Tag(0x00201206);
	// (0020,1208) VR=IS Number of Study Related Instances
	public static final Tag	NumberOfStudyRelatedInstances				= new Tag(0x00201208);
	// (0020,1209) VR=IS Number of Series Related Instances
	public static final Tag	NumberOfSeriesRelatedInstances				= new Tag(0x00201209);
	public static final Tag	ImageComments								= new Tag(0x00204000);
	// (0028,0002) VR=US Samples per Pixel
	public static final Tag	SamplesPerPixel								= new Tag(0x00280002);
	// (0028,0005) VR=UN Image Dimensions (Retired)
	// public static final Tag ImageDimensionsRetired = new Tag(0x00280005);
	// (0028,0004) VR=CS Photometric Interpretation
	public static final Tag	PhotometricInterpretation					= new Tag(0x00280004);
	// (0028,0006) VR=US Planar Configuration
	public static final Tag	PlanarConfiguration							= new Tag(0x00280006);
	// (0028,0008) VR=IS Number of Frames
	public static final Tag	NumberOfFrames								= new Tag(0x00280008);
	// (0028,0009) VR=AT Frame Increment Pointer
	// public static final Tag FrameIncrementPointer = new Tag(0x00280009);
	// (0028,0010) VR=US Rows
	public static final Tag	Rows										= new Tag(0x00280010);
	// (0028,0011) VR=US Columns
	public static final Tag	Columns										= new Tag(0x00280011);
	// (0028,0012) VR=US Planes
	// public static final Tag Planes = new Tag(0x00280012);
	// (0028,0014) VR=US Ultrasound Color Message Present
	// public static final Tag UltrasoundColorDataPresent = new Tag(0x00280014);
	// (0028,0030) VR=DS Pixel Spacing
	public static final Tag	PixelSpacing								= new Tag(0x00280030);
	// (0028,0031) VR=DS Zoom Factor
	// public static final Tag ZoomFactor = new Tag(0x00280031);
	// (0028,0032) VR=DS Zoom Center
	// public static final Tag ZoomCenter = new Tag(0x00280032);
	// (0028,0034) VR=IS Pixel Aspect Ratio
	// public static final Tag PixelAspectRatio = new Tag(0x00280034);
	// (0028,0040) VR=UN Image Format (Retired)
	// public static final Tag ImageFormatRetired = new Tag(0x00280040);
	// (0028,0050) VR=UN Manipulated Image (Retired)
	// public static final Tag ManipulatedImageRetired = new Tag(0x00280050);
	// (0028,0051) VR=CS Corrected Image
	// public static final Tag CorrectedImage = new Tag(0x00280051);
	// (0028,0060) VR=CS Compression Code (Retired)
	// public static final Tag CompressionCodeRetired = new Tag(0x00280060);
	// (0028,0100) VR=US Bits Allocated
	public static final Tag	BitsAllocated								= new Tag(0x00280100);
	// (0028,0101) VR=US Bits Stored
	public static final Tag	BitsStored									= new Tag(0x00280101);
	// (0028,0102) VR=US High Bit
	public static final Tag	HighBit										= new Tag(0x00280102);
	// (0028,0103) VR=US Pixel Representation
	public static final Tag	PixelRepresentation							= new Tag(0x00280103);
	// (0028,0104) VR=US,SS Smallest Valid Pixel Value (Retired)
	// public static final Tag SmallestValidPixelValueRetired = new
	// Tag(0x00280104);
	// (0028,0105) VR=US,SS Largest Valid Pixel Value (Retired)
	// public static final Tag LargestValidPixelValueRetired = new
	// Tag(0x00280105);
	// (0028,0106) VR=US,SS Smallest Image Pixel Value
	// public static final Tag SmallestImagePixelValue = new Tag(0x00280106);
	// (0028,0107) VR=US,SS Largest Image Pixel Value
	// public static final Tag LargestImagePixelValue = new Tag(0x00280107);
	// (0028,0108) VR=US,SS Smallest Pixel Value in Series
	// public static final Tag SmallestPixelValueInSeries = new Tag(0x00280108);
	// (0028,0109) VR=US,SS Largest Pixel Value in Series
	// public static final Tag LargestPixelValueInSeries = new Tag(0x00280109);
	// (0028,0110) VR=US,SS Smallest Image Pixel Value in Plane
	// public static final Tag SmallestImagePixelValueInPlane = new
	// Tag(0x00280110);
	// (0028,0111) VR=US,SS Largest Image Pixel Value in Plane
	// public static final Tag LargestImagePixelValueInPlane = new
	// Tag(0x00280111);
	// (0028,0120) VR=US,SS Pixel Padding Value
	public static final Tag	PixelPaddingValue							= new Tag(0x00280120);
	// (0028,0200) VR=UN Image Location (Retired)
	// public static final Tag ImageLocationRetired = new Tag(0x00280200);
	// (0028,0300) VR=CS Quality Control Image
	// public static final Tag QualityControlImage = new Tag(0x00280300);
	// (0028,0301) VR=CS Burned In Annotation
	// public static final Tag BurnedInAnnotation = new Tag(0x00280301);
	// (0028,1040) VR=CS Pixel Intensity Relationship
	// public static final Tag PixelIntensityRelationship = new Tag(0x00281040);
	// (0028,1041) VR=SS Pixel Intensity Relationship Sign
	// public static final Tag PixelIntensityRelationshipSign = new
	// Tag(0x00281041);
	// (0028,1050) VR=DS Window Center
	public static final Tag	WindowCenter								= new Tag(0x00281050);
	// (0028,1051) VR=DS Window Width
	public static final Tag	WindowWidth									= new Tag(0x00281051);
	// (0028,1052) VR=DS Rescale Intercept
	public static final Tag	RescaleIntercept							= new Tag(0x00281052);
	// (0028,1053) VR=DS Rescale Slope
	public static final Tag	RescaleSlope								= new Tag(0x00281053);
	public static final Tag	RequestedProcedureDescription				= new Tag(0x00321060);
	// (0032,1064) VR=SQ Requested Procedure Code Sequence
	// public static final Tag RequestedProcedureCodeSeq = new Tag(0x00321064);
	// (0032,1070) VR=LO Requested Contrast Agent
	public static final Tag	RequestedContrastAgent						= new Tag(0x00321070);
	// (0040,0001) VR=AE Scheduled Station AE Title
	public static final Tag	ScheduledStationAET							= new Tag(0x00400001);
	// (0040,0002) VR=DA Scheduled Procedure Step Start Date
	public static final Tag	SPSStartDate								= new Tag(0x00400002);
	// (0040,0003) VR=TM Scheduled Procedure Step Start Time
	public static final Tag	SPSStartTime								= new Tag(0x00400003);
	// (0040,0004) VR=DA Scheduled Procedure Step End Date
	public static final Tag	SPSEndDate									= new Tag(0x00400004);
	// (0040,0005) VR=TM Scheduled Procedure Step End Time
	public static final Tag	SPSEndTime									= new Tag(0x00400005);
	// (0040,0006) VR=PN Scheduled Performing Physician's Name
	public static final Tag	ScheduledPerformingPhysicianName			= new Tag(0x00400006);
	// (0040,0007) VR=LO Scheduled Procedure Step Description
	public static final Tag	SPSDescription								= new Tag(0x00400007);
	// (0040,0008) VR=SQ Scheduled Protocol Code Sequence
	public static final Tag	ScheduledProtocolCodeSeq					= new Tag(0x00400008);
	// (0040,0009) VR=SH Scheduled Procedure Step ID
	public static final Tag	SPSID										= new Tag(0x00400009);
	// (0040,0010) VR=SH Scheduled Station Name
	public static final Tag	ScheduledStationName						= new Tag(0x00400010);
	// (0040,0011) VR=SH Scheduled Procedure Step Location
	public static final Tag	SPSLocation									= new Tag(0x00400011);
	// (0040,0012) VR=LO Pre-Medication
	public static final Tag	PreMedication								= new Tag(0x00400012);
	// (0040,0020) VR=CS Scheduled Procedure Step Status
	public static final Tag	SPSStatus									= new Tag(0x00400020);
	// (0040,0100) VR=SQ Scheduled Procedure Step Sequence
	public static final Tag	SPSSeq										= new Tag(0x00400100);
	// (0040,0220) VR=SQ Referenced Non-Image Composite SOP Instance Sequence
	public static final Tag	RefNonImageCompositeSOPInstanceSeq			= new Tag(0x00400220);
	// (0040,0241) VR=AE Performed Station AE Title
	public static final Tag	PerformedStationAET							= new Tag(0x00400241);
	// (0040,0242) VR=SH Performed Station Name
	public static final Tag	PerformedStationName						= new Tag(0x00400242);
	// (0040,0243) VR=SH Performed Location
	public static final Tag	PerformedLocation							= new Tag(0x00400243);
	// (0040,0244) VR=DA Performed Procedure Step Start Date
	public static final Tag	PPSStartDate								= new Tag(0x00400244);
	// (0040,0245) VR=TM Performed Procedure Step Start Time
	public static final Tag	PPSStartTime								= new Tag(0x00400245);
	// (0040,0250) VR=DA Performed Procedure Step End Date
	public static final Tag	PPSEndDate									= new Tag(0x00400250);
	// (0040,0251) VR=TM Performed Procedure Step End Time
	public static final Tag	PPSEndTime									= new Tag(0x00400251);
	// (0040,0252) VR=CS Performed Procedure Step Status
	public static final Tag	PPSStatus									= new Tag(0x00400252);
	// (0040,0253) VR=SH Performed Procedure Step ID
	public static final Tag	PPSID										= new Tag(0x00400253);
	// (0040,0254) VR=LO Performed Procedure Step Description
	public static final Tag	PPSDescription								= new Tag(0x00400254);
	// (0040,0255) VR=LO Performed Procedure Type Description
	public static final Tag	PerformedProcedureTypeDescription			= new Tag(0x00400255);
	// (0040,0260) VR=SQ Performed Protocol Code Sequence
	public static final Tag	PerformedProtocolCodeSeq					= new Tag(0x00400260);
	// (0040,0270) VR=SQ Scheduled Step Attributes Sequence
	public static final Tag	ScheduledStepAttributesSeq					= new Tag(0x00400270);
	// (0040,0275) VR=SQ Request Attributes Sequence
	public static final Tag	RequestAttributesSeq						= new Tag(0x00400275);
	// (0040,0280) VR=ST Comments on the Performed Procedure Steps
	public static final Tag	PPSComments									= new Tag(0x00400280);
	// (0040,0281) VR=SQ Performed Procedure Step Discontinuation Reason Code
	// Sequence
	public static final Tag	PPSDiscontinuationReasonCodeSeq				= new Tag(0x00400281);
	// (0040,0293) VR=SQ Quantity Sequence
	public static final Tag	QuantitySeq									= new Tag(0x00400293);
	public static final Tag	RequestedProcedureID						= new Tag(0x00401001);
	// (7FE0,0010) VR=OW,OB Pixel Message
	public static final Tag	PixelData									= get(0x7FE00010);
	// (FFFC,FFFC) VR=OB Message Set Trailing Padding
	public static final Tag	DataSetTrailingPadding						= get(0xFFFCFFFC);
	// (FFFE,E000) VR=NONE Item
	public static final Tag	Item										= get(0xFFFEE000);
	// (FFFE,E00D) VR=NONE Item Delimitation Item
	public static final Tag	ItemDelimitationItem						= get(0xFFFEE00D);
	// (FFFE,E0DD) VR=NONE Seq Delimitation Item
	public static final Tag	SeqDelimitationItem							= get(0xFFFEE0DD);
}
