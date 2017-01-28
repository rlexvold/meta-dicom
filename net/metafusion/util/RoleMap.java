package net.metafusion.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import acme.util.Util;
import acme.util.XML;

public class RoleMap
{
	static net.metafusion.util.UID	allStorage[]				= new net.metafusion.util.UID[] { net.metafusion.util.UID.StoredPrintStorage,
																// 2 SOPClass: Hardcopy Grayscale Image Storage SOP
			// Class
			net.metafusion.util.UID.HardcopyGrayscaleImageStorage,
			// SOPClass: Hardcopy Color Image Storage SOP Class
			net.metafusion.util.UID.HardcopyColorImageStorage,
			// TransferSyntax: JPEG Baseline (Process 1)
			net.metafusion.util.UID.JPEGBaseline,
			// TransferSyntax: JPEG Extended (Process 2 & 4)
			net.metafusion.util.UID.JPEGExtended,
			// SOPClass: Pull Print Request SOP Class
			net.metafusion.util.UID.PullPrintRequest,
			// SOPClass: Computed Radiography Image Storage
			net.metafusion.util.UID.ComputedRadiographyImageStorage,
			// SOPClass: Digital X-Ray Image Storage - For Presentation
			net.metafusion.util.UID.DigitalXRayImageStorageForPresentation,
			// SOPClass: Digital X-Ray Image Storage - For Processing
			net.metafusion.util.UID.DigitalXRayImageStorageForProcessing,
			// SOPClass: Digital Mammography X-Ray Image Storage - For
			// Presentation
			net.metafusion.util.UID.DigitalMammographyXRayImageStorageForPresentation,
			// SOPClass: Digital Mammography X-Ray Image Storage - For
			// Processing
			net.metafusion.util.UID.DigitalMammographyXRayImageStorageForProcessing,
			// SOPClass: Digital Intra-oral X-Ray Image Storage - For
			// Presentation
			net.metafusion.util.UID.DigitalIntraoralXRayImageStorageForPresentation,
			// SOPClass: Digital Intra-oral X-Ray Image Storage - For Processing
			net.metafusion.util.UID.DigitalIntraoralXRayImageStorageForProcessing,
			// SOPClass: CT Image Storage
			net.metafusion.util.UID.CTImageStorage,
			// SOPClass: Ultrasound Multi-frame Image Storage (Retired)
			net.metafusion.util.UID.UltrasoundMultiframeImageStorageRetired,
			// SOPClass: Ultrasound Multi-frame Image Storage
			net.metafusion.util.UID.UltrasoundMultiframeImageStorage,
			// SOPClass: MR Image Storage
			net.metafusion.util.UID.MRImageStorage,
			// SOPClass: Enhanced MR Image Storage
			net.metafusion.util.UID.EnhancedMRImageStorage,
			// SOPClass: MR Spectroscopy Storage
			net.metafusion.util.UID.MRSpectroscopyStorage,
			// SOPClass: Nuclear Medicine Image Storage (Retired)
			net.metafusion.util.UID.NuclearMedicineImageStorageRetired,
			// SOPClass: Ultrasound Image Storage (Retired)
			net.metafusion.util.UID.UltrasoundImageStorageRetired,
			// SOPClass: Ultrasound Image Storage
			net.metafusion.util.UID.UltrasoundImageStorage,
			// SOPClass: Secondary Capture Image Storage
			net.metafusion.util.UID.SecondaryCaptureImageStorage,
			// SOPClass: Multi-frame Single Bit Secondary Capture Image Storage
			net.metafusion.util.UID.MultiframeSingleBitSecondaryCaptureImageStorage,
			// SOPClass: Multi-frame Grayscale Byte Secondary Capture Image
			// Storage
			net.metafusion.util.UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
			// SOPClass: Multi-frame Grayscale Word Secondary Capture Image
			// Storage
			net.metafusion.util.UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
			// SOPClass: Multi-frame Color Secondary Capture Image Storage
			net.metafusion.util.UID.MultiframeColorSecondaryCaptureImageStorage,
			// SOPClass: Standalone Overlay Storage
			net.metafusion.util.UID.StandaloneOverlayStorage,
			// SOPClass: Standalone Curve Storage
			net.metafusion.util.UID.StandaloneCurveStorage,
			// SOPClass: 12-lead ECG Waveform Storage
			net.metafusion.util.UID.TwelveLeadECGWaveformStorage,
			// SOPClass: General ECG Waveform Storage
			net.metafusion.util.UID.GeneralECGWaveformStorage,
			// SOPClass: Ambulatory ECG Waveform Storage
			net.metafusion.util.UID.AmbulatoryECGWaveformStorage,
			// SOPClass: Hemodynamic Waveform Storage
			net.metafusion.util.UID.HemodynamicWaveformStorage,
			// SOPClass: Cardiac Electrophysiology Waveform Storage
			net.metafusion.util.UID.CardiacElectrophysiologyWaveformStorage,
			// SOPClass: Basic Voice Audio Waveform Storage
			net.metafusion.util.UID.BasicVoiceAudioWaveformStorage,
			// SOPClass: Standalone Modality LUT Storage
			net.metafusion.util.UID.StandaloneModalityLUTStorage,
			// SOPClass: Standalone VOI LUT Storage
			net.metafusion.util.UID.StandaloneVOILUTStorage,
			// SOPClass: Grayscale Softcopy Presentation State Storage SOP Class
			net.metafusion.util.UID.GrayscaleSoftcopyPresentationStateStorage,
			// SOPClass: X-Ray Angiographic Image Storage
			net.metafusion.util.UID.XRayAngiographicImageStorage,
			// SOPClass: X-Ray Radiofluoroscopic Image Storage
			net.metafusion.util.UID.XRayRadiofluoroscopicImageStorage,
			// SOPClass: X-Ray Angiographic Bi-Plane Image Storage (Retired)
			net.metafusion.util.UID.XRayAngiographicBiPlaneImageStorageRetired,
			// SOPClass: Nuclear Medicine Image Storage
			net.metafusion.util.UID.NuclearMedicineImageStorage,
			// SOPClass: Raw Message Storage
			net.metafusion.util.UID.RawDataStorage,
			// SOPClass: VL Image Storage (Retired)
			net.metafusion.util.UID.VLImageStorageRetired,
			// SOPClass: VL Multi-frame Image Storage (Retired)
			net.metafusion.util.UID.VLMultiframeImageStorageRetired,
			// SOPClass: VL Endoscopic Image Storage
			net.metafusion.util.UID.VLEndoscopicImageStorage,
			// SOPClass: VL Microscopic Image Storage
			net.metafusion.util.UID.VLMicroscopicImageStorage,
			// SOPClass: VL Slide-Coordinates Microscopic Image Storage
			net.metafusion.util.UID.VLSlideCoordinatesMicroscopicImageStorage,
			// SOPClass: VL Photographic Image Storage
			net.metafusion.util.UID.VLPhotographicImageStorage	};
	private static RoleMap			clientRoleMap				= null;
	private static RoleMap			serverRoleMap				= null;
	private static RoleMap			storageCommitEventRoleMap	= null;
	private static RoleMap			storeUserRoleMap			= null;
	private static ArrayList		syntaxList					= null;
	TreeMap							map							= new TreeMap();

	public RoleMap()
	{
	}

	public RoleMap(XML xml)
	{
		Util.Assert(xml.getName().equals("roles"));
		List l = xml.getList();
		for (int i = 0; i < l.size(); i++)
		{
			XML x = (XML) l.get(i);
			Role r = new Role(x);
			map.put(r.getUID(), r);
		}
	}

	public synchronized static ArrayList getSyntaxList()
	{
		if (syntaxList == null)
		{
			syntaxList = new ArrayList();
			syntaxList.add(net.metafusion.util.UID.ExplicitVRLittleEndian);
			syntaxList.add(net.metafusion.util.UID.ImplicitVRLittleEndian);
		}
		return syntaxList;
	}

	public synchronized static RoleMap getClientRoleMap()
	{
		if (clientRoleMap == null)
		{
			ArrayList syntaxList = getSyntaxList();
			RoleMap r = new RoleMap();
			r.add(new Role(net.metafusion.util.UID.Verification, syntaxList, true, false));
			r.addAll(allStorage, syntaxList, true, false);
			r.add(new Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelFIND, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelMOVE, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelGET, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelFIND, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelMOVE, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelGET, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelFIND, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE, syntaxList, true, false));
			r.add(new Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelGET, syntaxList, true, false));
			clientRoleMap = r;
			// todo: many others !!!!
		}
		return clientRoleMap;
	}

	public synchronized static RoleMap getPingRoleMap()
	{
		ArrayList syntaxList = getSyntaxList();
		// syntaxList.add(UID.JPEGLossless);
		// todo: add other image types note jpeg->le explicit
		RoleMap r = new RoleMap();
		r.add(new Role(net.metafusion.util.UID.Verification, syntaxList, true, true));
		return r;
	}

	public synchronized static RoleMap getServerRoleMap()
	{
		if (serverRoleMap == null)
		{
			ArrayList syntaxList = getSyntaxList();
			// syntaxList.add(UID.JPEGLossless);
			// todo: add other image types note jpeg->le explicit
			RoleMap r = new RoleMap();
			r.add(new Role(net.metafusion.util.UID.Verification, syntaxList, false, true));
			r.addAll(allStorage, syntaxList, false, true);
			r.add(new Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelFIND, syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelMOVE, syntaxList, false, true));
			// r.add(new
			// Role(net.metafusion.util.UID.StudyRootQueryRetrieveInformationModelGET,
			// syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelFIND, syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelMOVE, syntaxList, false, true));
			// r.add(new
			// Role(net.metafusion.util.UID.PatientRootQueryRetrieveInformationModelGET,
			// syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelFIND, syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE, syntaxList, false, true));
			// r.add(new
			// Role(net.metafusion.util.UID.PatientStudyOnlyQueryRetrieveInformationModelGET,
			// syntaxList, false, true));
			// worklist
			r.add(new Role(net.metafusion.util.UID.ModalityWorklistInformationModelFIND, syntaxList, false, true));
			r.add(new Role(net.metafusion.util.UID.ModalityPerformedProcedureStep, syntaxList, false, true));
			// MAB StorageCommit
			r.add(new Role(net.metafusion.util.UID.StorageCommitmentPushModel, syntaxList, false, true));
			serverRoleMap = r;
			// todo: many others !!!!
		}
		return serverRoleMap;
	}

	public synchronized static RoleMap getStorageCommitEventRoleMap()
	{
		if (storageCommitEventRoleMap == null)
		{
			ArrayList syntaxList = getSyntaxList();
			RoleMap r = new RoleMap();
			r.add(new Role(net.metafusion.util.UID.Verification, syntaxList, true, false));
			r.addAll(allStorage, syntaxList, true, false);
			r.add(new Role(net.metafusion.util.UID.StorageCommitmentPushModel, syntaxList, false, true));
			storageCommitEventRoleMap = r;
		}
		return storageCommitEventRoleMap;
	}

	public synchronized static RoleMap getStoreUserRoleMap()
	{
		if (storeUserRoleMap == null)
		{
			ArrayList syntaxList = getSyntaxList();
			RoleMap r = new RoleMap();
			r.add(new Role(net.metafusion.util.UID.Verification, syntaxList, true, false));
			r.addAll(allStorage, syntaxList, true, false);
			storeUserRoleMap = r;
			// todo: many others !!!!
		}
		return storeUserRoleMap;
	}

	public void add(Role e)
	{
		if (map.size() >= 255)
			throw new RuntimeException("RoleMap too big " + this);
		map.put(e.uid, e);
	}

	void addAll(net.metafusion.util.UID uid[], List syntaxList, boolean user, boolean prov)
	{
		for (int i = 0; i < uid.length; i++)
			add(new Role(uid[i], syntaxList, user, prov));
	}

	public Role copyRole(net.metafusion.util.UID uid, List syntaxList)
	{
		Role r = (Role) map.get(uid);
		if (r == null)
			return null;
		List sl = r.getSyntaxList();
		for (int i = 0; i < sl.size(); i++)
			for (int j = 0; j < syntaxList.size(); j++)
				if (sl.get(i).equals(syntaxList.get(j)))
				{
					Role sr = new Role(uid, (net.metafusion.util.UID) sl.get(i), r.isProvider(), r.isUser());
					return sr;
				}
		return null;
	}

	public Role copyRole(net.metafusion.util.UID uid, net.metafusion.util.UID syntax)
	{
		Role r = (Role) map.get(uid);
		if (r == null)
			return null;
		if (!r.getSyntaxList().contains(syntax))
			return null;
		Role sr = new Role(uid, syntax, false, false);
		return sr;
	}

	public Role get(net.metafusion.util.UID uid)
	{
		return (Role) map.get(uid);
	}

	public Iterator iterator()
	{
		return map.values().iterator();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("RoleMap[" + map.size() + "]\n");
		Iterator iter = iterator();
		while (iter.hasNext())
		{
			Role r = (Role) iter.next();
			sb.append("Role[" + r.getPresContextID() + "]: " + r.getUID().getKey() + " " + "user=" + (r.isUser() ? "T" : "F") + " prov=" + (r.isProvider() ? "T" : "F") + "\n");
		}
		return sb.toString();
	}
}
