package net.metafusion.util;

import java.util.HashMap;
import acme.util.Buffer;

public class UID implements Comparable
{
	static HashMap uidMap = new HashMap();

	synchronized static public UID get(String uid)
	{
		UID u = (UID) uidMap.get(uid);
		if (u == null) u = new UID(uid);
		return u;
	}

	static public UID get(Buffer b)
	{
		return get(b.getString(b.getLength()));
	}

	static public UID get(Buffer b, int len)
	{
		return get(b.getString(len));
	}

	UID(String uid)
	{
		this.uid = uid;
		uidMap.put(uid, this);
	}

	public boolean equals(Object o)
	{
		return uid.equals(((UID) o).uid);
	}

	public int compareTo(Object o)
	{
		if (o == null) return 1;
		return uid.compareTo(((UID) o).uid);
	}
	String uid;
	String name = "";
	String key = "UKNOWN";
	String type = "";

	public String getUID()
	{
		return uid;
	}

	public void setUID(String uid)
	{
		this.uid = uid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return key + "[" + uid + "]";
	}
	// TransferSyntax: Implicit VR Little Endian
	public static final UID ImplicitVRLittleEndian = new UID("1.2.840.10008.1.2");
	// TransferSyntax: Explicit VR Little Endian
	public static final UID ExplicitVRLittleEndian = new UID("1.2.840.10008.1.2.1");
	// TransferSyntax: Deflated Explicit VR Little Endian
	public static final UID DeflatedExplicitVRLittleEndian = new UID("1.2.840.10008.1.2.1.99");
	// TransferSyntax: Explicit VR Big Endian
	public static final UID ExplicitVRBigEndian = new UID("1.2.840.10008.1.2.2");
	// TransferSyntax: JPEG Baseline (Process 1)
	public static final UID JPEGBaseline = new UID("1.2.840.10008.1.2.4.50");
	// TransferSyntax: JPEG Extended (Process 2 & 4)
	public static final UID JPEGExtended = new UID("1.2.840.10008.1.2.4.51");
	// TransferSyntax: JPEG Extended (Process 3 & 5) (Retired)
	public static final UID JPEGExtended35Retired = new UID("1.2.840.10008.1.2.4.52");
	// TransferSyntax: JPEG Spectral Selection, Non- Hierarchical (Process 6 &
	// 8) (Retired)
	public static final UID JPEG68Retired = new UID("1.2.840.10008.1.2.4.53");
	// TransferSyntax: JPEG Spectral Selection, Non- Hierarchical (Process 7 &
	// 9) (Retired)
	public static final UID JPEG79Retired = new UID("1.2.840.10008.1.2.4.54");
	// TransferSyntax: JPEG Full Progression, Non- Hierarchical (Process 10 &
	// 12) (Retired)
	public static final UID JPEG1012Retired = new UID("1.2.840.10008.1.2.4.55");
	// TransferSyntax: JPEG Full Progression, Non- Hierarchical (Process 11 &
	// 13) (Retired)
	public static final UID JPEG1113Retired = new UID("1.2.840.10008.1.2.4.56");
	// TransferSyntax: JPEG Lossless, Non-Hierarchical (Process 14)
	public static final UID JPEGLossless14 = new UID("1.2.840.10008.1.2.4.57");
	// TransferSyntax: JPEG Lossless, Non-Hierarchical (Process 15) (Retired)
	public static final UID JPEGLossless15Retired = new UID("1.2.840.10008.1.2.4.58");
	// TransferSyntax: JPEG Extended, Hierarchical (Process 16 & 18) (Retired)
	public static final UID JPEG1618Retired = new UID("1.2.840.10008.1.2.4.59");
	// TransferSyntax: JPEG Extended, Hierarchical (Process 17 & 19) (Retired)
	public static final UID JPEG1719Retired = new UID("1.2.840.10008.1.2.4.60");
	// TransferSyntax: JPEG Spectral Selection, Hierarchical (Process 20 & 22)
	// (Retired)
	public static final UID JPEG2022Retired = new UID("1.2.840.10008.1.2.4.61");
	// TransferSyntax: JPEG Spectral Selection, Hierarchical (Process 21 & 23)
	// (Retired)
	public static final UID JPEG2123Retired = new UID("1.2.840.10008.1.2.4.62");
	// TransferSyntax: JPEG Full Progression, Hierarchical (Process 24 & 26)
	// (Retired)
	public static final UID JPEG2426Retired = new UID("1.2.840.10008.1.2.4.63");
	// TransferSyntax: JPEG Full Progression, Hierarchical (Process 25 & 27)
	// (Retired)
	public static final UID JPEG2527Retired = new UID("1.2.840.10008.1.2.4.64");
	// TransferSyntax: JPEG Lossless, Hierarchical (Process 28) (Retired)
	public static final UID JPEGLoRetired = new UID("1.2.840.10008.1.2.4.65");
	// TransferSyntax: JPEG Lossless, Hierarchical (Process 29) (Retired)
	public static final UID JPEG29Retired = new UID("1.2.840.10008.1.2.4.66");
	// TransferSyntax: JPEG Lossless, Non- Hierarchical, First-Order Prediction
	// (Process 14 [Selection Value 1])
	public static final UID JPEGLossless = new UID("1.2.840.10008.1.2.4.70");
	// TransferSyntax: JPEG-LS Lossless Image Compression
	public static final UID JPEGLSLossless = new UID("1.2.840.10008.1.2.4.80");
	// TransferSyntax: JPEG-LS Lossy (Near-Lossless) Image Compression
	public static final UID JPEGLSLossy = new UID("1.2.840.10008.1.2.4.81");
	// TransferSyntax: JPEG 2000 Lossless Image Compression
	public static final UID JPEG2000Lossless = new UID("1.2.840.10008.1.2.4.90");
	// TransferSyntax: JPEG 2000 Lossy Image Compression
	public static final UID JPEG2000Lossy = new UID("1.2.840.10008.1.2.4.91");
	// TransferSyntax: RLE Lossless
	public static final UID RLELossless = new UID("1.2.840.10008.1.2.5");
	// SOPClass: Verification SOP Class
	public static final UID Verification = new UID("1.2.840.10008.1.1");
	// SOPClass: Media Storage Directory Storage
	public static final UID MediaStorageDirectoryStorage = new UID("1.2.840.10008.1.3.10");
	// SOPClass: Basic Study Content Notification SOP Class
	public static final UID BasicStudyContentNotification = new UID("1.2.840.10008.1.9");
	// SOPClass: Storage Commitment Push Model SOP Class
	public static final UID StorageCommitmentPushModel = new UID("1.2.840.10008.1.20.1");
	// SOPClass: Storage Commitment Pull Model SOP Class
	public static final UID StorageCommitmentPullModel = new UID("1.2.840.10008.1.20.2");
	// SOPClass: Detached Patient Management SOP Class
	public static final UID DetachedPatientManagement = new UID("1.2.840.10008.3.1.2.1.1");
	// SOPClass: Detached Visit Management SOP Class
	public static final UID DetachedVisitManagement = new UID("1.2.840.10008.3.1.2.2.1");
	// SOPClass: Detached Study Management SOP Class
	public static final UID DetachedStudyManagement = new UID("1.2.840.10008.3.1.2.3.1");
	// SOPClass: Study Component Management SOP Class
	public static final UID StudyComponentManagement = new UID("1.2.840.10008.3.1.2.3.2");
	// SOPClass: Modality Performed Procedure Step SOP Class
	public static final UID ModalityPerformedProcedureStep = new UID("1.2.840.10008.3.1.2.3.3");
	// SOPClass: Modality Performed Procedure Step Retrieve SOP Class
	public static final UID ModalityPerformedProcedureStepRetrieve = new UID("1.2.840.10008.3.1.2.3.4");
	// SOPClass: Modality Performed Procedure Step Notification SOP Class
	public static final UID ModalityPerformedProcedureStepNotification = new UID("1.2.840.10008.3.1.2.3.5");
	// SOPClass: Detached Results Management SOP Class
	public static final UID DetachedResultsManagement = new UID("1.2.840.10008.3.1.2.5.1");
	// SOPClass: Detached Interpretation Management SOP Class
	public static final UID DetachedInterpretationManagement = new UID("1.2.840.10008.3.1.2.6.1");
	// SOPClass: Basic Film DicomSession SOP Class
	public static final UID BasicFilmSession = new UID("1.2.840.10008.5.1.1.1");
	// SOPClass: Basic Film Box SOP Class
	public static final UID BasicFilmBoxSOP = new UID("1.2.840.10008.5.1.1.2");
	// SOPClass: Basic Grayscale Image Box SOP Class
	public static final UID BasicGrayscaleImageBox = new UID("1.2.840.10008.5.1.1.4");
	// SOPClass: Basic Color Image Box SOP Class
	public static final UID BasicColorImageBox = new UID("1.2.840.10008.5.1.1.4.1");
	// SOPClass: Referenced Image Box SOP Class (Retired)
	public static final UID ReferencedImageBoxRetired = new UID("1.2.840.10008.5.1.1.4.2");
	// SOPClass: Print Job SOP Class
	public static final UID PrintJob = new UID("1.2.840.10008.5.1.1.14");
	// SOPClass: Basic Annotation Box SOP Class
	public static final UID BasicAnnotationBox = new UID("1.2.840.10008.5.1.1.15");
	// SOPClass: Printer SOP Class
	public static final UID Printer = new UID("1.2.840.10008.5.1.1.16");
	// SOPClass: Printer Configuration Retrieval SOP Class
	public static final UID PrinterConfigurationRetrieval = new UID("1.2.840.10008.5.1.1.16.376");
	// SOPClass: VOI LUT Box SOP Class
	public static final UID VOILUTBox = new UID("1.2.840.10008.5.1.1.22");
	// SOPClass: Presentation LUT SOP Class
	public static final UID PresentationLUT = new UID("1.2.840.10008.5.1.1.23");
	// SOPClass: Image Overlay Box SOP Class (Retired)
	public static final UID ImageOverlayBox = new UID("1.2.840.10008.5.1.1.24");
	// SOPClass: Basic Print Image Overlay Box SOP Class
	public static final UID BasicPrintImageOverlayBox = new UID("1.2.840.10008.5.1.1.24.1");
	// SOPClass: Print QueueElem Management SOP Class
	public static final UID PrintQueueManagement = new UID("1.2.840.10008.5.1.1.26");
	// SOPClass: Stored Print Storage SOP Class
	public static final UID StoredPrintStorage = new UID("1.2.840.10008.5.1.1.27");
	// SOPClass: Hardcopy Grayscale Image Storage SOP Class
	public static final UID HardcopyGrayscaleImageStorage = new UID("1.2.840.10008.5.1.1.29");
	// SOPClass: Hardcopy Color Image Storage SOP Class
	public static final UID HardcopyColorImageStorage = new UID("1.2.840.10008.5.1.1.30");
	// SOPClass: Pull Print Request SOP Class
	public static final UID PullPrintRequest = new UID("1.2.840.10008.5.1.1.31");
	// SOPClass: Computed Radiography Image Storage
	public static final UID ComputedRadiographyImageStorage = new UID("1.2.840.10008.5.1.4.1.1.1");
	// SOPClass: Digital X-Ray Image Storage - For Presentation
	public static final UID DigitalXRayImageStorageForPresentation = new UID("1.2.840.10008.5.1.4.1.1.1.1");
	// SOPClass: Digital X-Ray Image Storage - For Processing
	public static final UID DigitalXRayImageStorageForProcessing = new UID("1.2.840.10008.5.1.4.1.1.1.1.1");
	// SOPClass: Digital Mammography X-Ray Image Storage - For Presentation
	public static final UID DigitalMammographyXRayImageStorageForPresentation = new UID("1.2.840.10008.5.1.4.1.1.1.2");
	// SOPClass: Digital Mammography X-Ray Image Storage - For Processing
	public static final UID DigitalMammographyXRayImageStorageForProcessing = new UID("1.2.840.10008.5.1.4.1.1.1.2.1");
	// SOPClass: Digital Intra-oral X-Ray Image Storage - For Presentation
	public static final UID DigitalIntraoralXRayImageStorageForPresentation = new UID("1.2.840.10008.5.1.4.1.1.1.3");
	// SOPClass: Digital Intra-oral X-Ray Image Storage - For Processing
	public static final UID DigitalIntraoralXRayImageStorageForProcessing = new UID("1.2.840.10008.5.1.4.1.1.1.3.1");
	// SOPClass: CT Image Storage
	public static final UID CTImageStorage = new UID("1.2.840.10008.5.1.4.1.1.2");
	// SOPClass: Ultrasound Multi-frame Image Storage (Retired)
	public static final UID UltrasoundMultiframeImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.3");
	// SOPClass: Ultrasound Multi-frame Image Storage
	public static final UID UltrasoundMultiframeImageStorage = new UID("1.2.840.10008.5.1.4.1.1.3.1");
	// SOPClass: MR Image Storage
	public static final UID MRImageStorage = new UID("1.2.840.10008.5.1.4.1.1.4");
	// SOPClass: Enhanced MR Image Storage
	public static final UID EnhancedMRImageStorage = new UID("1.2.840.10008.5.1.4.1.1.4.1");
	// SOPClass: MR Spectroscopy Storage
	public static final UID MRSpectroscopyStorage = new UID("1.2.840.10008.5.1.4.1.1.4.2");
	// SOPClass: Nuclear Medicine Image Storage (Retired)
	public static final UID NuclearMedicineImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.5");
	// SOPClass: Ultrasound Image Storage (Retired)
	public static final UID UltrasoundImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.6");
	// SOPClass: Ultrasound Image Storage
	public static final UID UltrasoundImageStorage = new UID("1.2.840.10008.5.1.4.1.1.6.1");
	// SOPClass: Secondary Capture Image Storage
	public static final UID SecondaryCaptureImageStorage = new UID("1.2.840.10008.5.1.4.1.1.7");
	// SOPClass: Multi-frame Single Bit Secondary Capture Image Storage
	public static final UID MultiframeSingleBitSecondaryCaptureImageStorage = new UID("1.2.840.10008.5.1.4.1.1.7.1");
	// SOPClass: Multi-frame Grayscale Byte Secondary Capture Image Storage
	public static final UID MultiframeGrayscaleByteSecondaryCaptureImageStorage = new UID("1.2.840.10008.5.1.4.1.1.7.2");
	// SOPClass: Multi-frame Grayscale Word Secondary Capture Image Storage
	public static final UID MultiframeGrayscaleWordSecondaryCaptureImageStorage = new UID("1.2.840.10008.5.1.4.1.1.7.3");
	// SOPClass: Multi-frame Color Secondary Capture Image Storage
	public static final UID MultiframeColorSecondaryCaptureImageStorage = new UID("1.2.840.10008.5.1.4.1.1.7.4");
	// SOPClass: Standalone Overlay Storage
	public static final UID StandaloneOverlayStorage = new UID("1.2.840.10008.5.1.4.1.1.8");
	// SOPClass: Standalone Curve Storage
	public static final UID StandaloneCurveStorage = new UID("1.2.840.10008.5.1.4.1.1.9");
	// SOPClass: 12-lead ECG Waveform Storage
	public static final UID TwelveLeadECGWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.1.1");
	// SOPClass: General ECG Waveform Storage
	public static final UID GeneralECGWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.1.2");
	// SOPClass: Ambulatory ECG Waveform Storage
	public static final UID AmbulatoryECGWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.1.3");
	// SOPClass: Hemodynamic Waveform Storage
	public static final UID HemodynamicWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.2.1");
	// SOPClass: Cardiac Electrophysiology Waveform Storage
	public static final UID CardiacElectrophysiologyWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.3.1");
	// SOPClass: Basic Voice Audio Waveform Storage
	public static final UID BasicVoiceAudioWaveformStorage = new UID("1.2.840.10008.5.1.4.1.1.9.4.1");
	// SOPClass: Standalone Modality LUT Storage
	public static final UID StandaloneModalityLUTStorage = new UID("1.2.840.10008.5.1.4.1.1.10");
	// SOPClass: Standalone VOI LUT Storage
	public static final UID StandaloneVOILUTStorage = new UID("1.2.840.10008.5.1.4.1.1.11");
	// SOPClass: Grayscale Softcopy Presentation State Storage SOP Class
	public static final UID GrayscaleSoftcopyPresentationStateStorage = new UID("1.2.840.10008.5.1.4.1.1.11.1");
	// SOPClass: X-Ray Angiographic Image Storage
	public static final UID XRayAngiographicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.12.1");
	// SOPClass: X-Ray Radiofluoroscopic Image Storage
	public static final UID XRayRadiofluoroscopicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.12.2");
	// SOPClass: X-Ray Angiographic Bi-Plane Image Storage (Retired)
	public static final UID XRayAngiographicBiPlaneImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.12.3");
	// SOPClass: Nuclear Medicine Image Storage
	public static final UID NuclearMedicineImageStorage = new UID("1.2.840.10008.5.1.4.1.1.20");
	// SOPClass: Raw Message Storage
	public static final UID RawDataStorage = new UID("1.2.840.10008.5.1.4.1.1.66");
	// SOPClass: VL Image Storage (Retired)
	public static final UID VLImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.77.1");
	// SOPClass: VL Multi-frame Image Storage (Retired)
	public static final UID VLMultiframeImageStorageRetired = new UID("1.2.840.10008.5.1.4.1.1.77.2");
	// SOPClass: VL Endoscopic Image Storage
	public static final UID VLEndoscopicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.77.1.1");
	// SOPClass: VL Microscopic Image Storage
	public static final UID VLMicroscopicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.77.1.2");
	// SOPClass: VL Slide-Coordinates Microscopic Image Storage
	public static final UID VLSlideCoordinatesMicroscopicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.77.1.3");
	// SOPClass: VL Photographic Image Storage
	public static final UID VLPhotographicImageStorage = new UID("1.2.840.10008.5.1.4.1.1.77.1.4");
	// SOPClass: Basic Text SR
	public static final UID BasicTextSR = new UID("1.2.840.10008.5.1.4.1.1.88.11");
	// SOPClass: Enhanced SR
	public static final UID EnhancedSR = new UID("1.2.840.10008.5.1.4.1.1.88.22");
	// SOPClass: Comprehensive SR
	public static final UID ComprehensiveSR = new UID("1.2.840.10008.5.1.4.1.1.88.33");
	// SOPClass: Mammography CAD SR
	public static final UID MammographyCADSR = new UID("1.2.840.10008.5.1.4.1.1.88.50");
	// SOPClass: Key Object Selection Document
	public static final UID KeyObjectSelectionDocument = new UID("1.2.840.10008.5.1.4.1.1.88.59");
	// SOPClass: Positron Emission Tomography Image Storage
	public static final UID PositronEmissionTomographyImageStorage = new UID("1.2.840.10008.5.1.4.1.1.128");
	// SOPClass: Standalone PET Curve Storage
	public static final UID StandalonePETCurveStorage = new UID("1.2.840.10008.5.1.4.1.1.129");
	// SOPClass: RT Image Storage
	public static final UID RTImageStorage = new UID("1.2.840.10008.5.1.4.1.1.481.1");
	// SOPClass: RT Dose Storage
	public static final UID RTDoseStorage = new UID("1.2.840.10008.5.1.4.1.1.481.2");
	// SOPClass: RT Structure Set Storage
	public static final UID RTStructureSetStorage = new UID("1.2.840.10008.5.1.4.1.1.481.3");
	// SOPClass: RT Beams Treatment Record Storage
	public static final UID RTBeamsTreatmentRecordStorage = new UID("1.2.840.10008.5.1.4.1.1.481.4");
	// SOPClass: RT Plan Storage
	public static final UID RTPlanStorage = new UID("1.2.840.10008.5.1.4.1.1.481.5");
	// SOPClass: RT Brachy Treatment Record Storage
	public static final UID RTBrachyTreatmentRecordStorage = new UID("1.2.840.10008.5.1.4.1.1.481.6");
	// SOPClass: RT Treatment Summary Record Storage
	public static final UID RTTreatmentSummaryRecordStorage = new UID("1.2.840.10008.5.1.4.1.1.481.7");
	// SOPClass: Patient Root Query/Retrieve Information Model - FIND
	public static final UID PatientRootQueryRetrieveInformationModelFIND = new UID("1.2.840.10008.5.1.4.1.2.1.1");
	// SOPClass: Patient Root Query/Retrieve Information Model - MOVE
	public static final UID PatientRootQueryRetrieveInformationModelMOVE = new UID("1.2.840.10008.5.1.4.1.2.1.2");
	// SOPClass: Patient Root Query/Retrieve Information Model - GET
	public static final UID PatientRootQueryRetrieveInformationModelGET = new UID("1.2.840.10008.5.1.4.1.2.1.3");
	// SOPClass: Study Root Query/Retrieve Information Model - FIND
	public static final UID StudyRootQueryRetrieveInformationModelFIND = new UID("1.2.840.10008.5.1.4.1.2.2.1");
	// SOPClass: Study Root Query/Retrieve Information Model - MOVE
	public static final UID StudyRootQueryRetrieveInformationModelMOVE = new UID("1.2.840.10008.5.1.4.1.2.2.2");
	// SOPClass: Study Root Query/Retrieve Information Model - GET
	public static final UID StudyRootQueryRetrieveInformationModelGET = new UID("1.2.840.10008.5.1.4.1.2.2.3");
	// SOPClass: Patient/Study Only Query/Retrieve Information Model - FIND
	public static final UID PatientStudyOnlyQueryRetrieveInformationModelFIND = new UID("1.2.840.10008.5.1.4.1.2.3.1");
	// SOPClass: Patient/Study Only Query/Retrieve Information Model - MOVE
	public static final UID PatientStudyOnlyQueryRetrieveInformationModelMOVE = new UID("1.2.840.10008.5.1.4.1.2.3.2");
	// SOPClass: Patient/Study Only Query/Retrieve Information Model - GET
	public static final UID PatientStudyOnlyQueryRetrieveInformationModelGET = new UID("1.2.840.10008.5.1.4.1.2.3.3");
	// SOPClass: Modality Worklist Information Model - FIND
	public static final UID ModalityWorklistInformationModelFIND = new UID("1.2.840.10008.5.1.4.31");
	// SOPClass: General Purpose Worklist Information Model - FIND
	public static final UID GeneralPurposeWorklistInformationModelFIND = new UID("1.2.840.10008.5.1.4.32.1");
	// SOPClass: General Purpose Scheduled Procedure Step SOP Class
	public static final UID GeneralPurposeScheduledProcedureStepSOPClass = new UID("1.2.840.10008.5.1.4.32.2");
	// SOPClass: General Purpose Performed Procedure Step SOP Class
	public static final UID GeneralPurposePerformedProcedureStepSOPClass = new UID("1.2.840.10008.5.1.4.32.3");
	// MetaSOPClass: Detached Patient Management Meta SOP Class
	public static final UID DetachedPatientManagementMetaSOPClass = new UID("1.2.840.10008.3.1.2.1.4");
	// MetaSOPClass: Detached Results Management Meta SOP Class
	public static final UID DetachedResultsManagementMetaSOPClass = new UID("1.2.840.10008.3.1.2.5.4");
	// MetaSOPClass: Detached Study Management Meta SOP Class
	public static final UID DetachedStudyManagementMetaSOPClass = new UID("1.2.840.10008.3.1.2.5.5");
	// MetaSOPClass: Basic Grayscale Print Management Meta SOP Class
	public static final UID BasicGrayscalePrintManagement = new UID("1.2.840.10008.5.1.1.9");
	// MetaSOPClass: Referenced Grayscale Print Management Meta SOP Class
	// (Retired)
	public static final UID ReferencedGrayscalePrintManagementRetired = new UID("1.2.840.10008.5.1.1.9.1");
	// MetaSOPClass: Basic Color Print Management Meta SOP Class
	public static final UID BasicColorPrintManagement = new UID("1.2.840.10008.5.1.1.18");
	// MetaSOPClass: Referenced Color Print Management Meta SOP Class (Retired)
	public static final UID ReferencedColorPrintManagementRetired = new UID("1.2.840.10008.5.1.1.18.1");
	// MetaSOPClass: Pull Stored Print Management Meta SOP Class
	public static final UID PullStoredPrintManagement = new UID("1.2.840.10008.5.1.1.32");
	// MetaSOPClass: General Purpose Worklist Management Meta SOP Class
	public static final UID GeneralPurposeWorklistManagementMetaSOPClass = new UID("1.2.840.10008.5.1.4.32");
	// SOPInstance: Storage Commitment Push Model SOP Instance
	public static final UID StorageCommitmentPushModelSOPInstance = new UID("1.2.840.10008.1.20.1.1");
	// SOPInstance: Storage Commitment Pull Model SOP Instance
	public static final UID StorageCommitmentPullModelSOPInstance = new UID("1.2.840.10008.1.20.2.1");
	// SOPInstance: Printer SOP Instance
	public static final UID PrinterSOPInstance = new UID("1.2.840.10008.5.1.1.17");
	// SOPInstance: Printer Configuration Retrieval SOP Instance
	public static final UID PrinterConfigurationRetrievalSOPInstance = new UID("1.2.840.10008.5.1.1.17.376");
	// SOPInstance: Print QueueElem SOP Instance
	public static final UID PrintQueueSOPInstance = new UID("1.2.840.10008.5.1.1.25");
	// ApplicationContextName: DICOM Application Context Name
	public static final UID DICOMApplicationContextName = new UID("1.2.840.10008.3.1.1.1");
}
