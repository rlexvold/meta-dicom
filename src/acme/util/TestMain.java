package acme.util;

import java.lang.reflect.Method;

public class TestMain
{
	// static Log log = new Log("XTestCase");
	static void log(String s)
	{
		Log.log(s);
	}

	public static void main(String[] args)
	{
		log("start test " + args[0]);
		try
		{
			Class k = Class.forName(args[0]);
			Method m = k.getMethod("main", new Class[] { args.getClass() });
			m.invoke(null, new Object[] { args });
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("test caught " + e);
		}
		finally
		{
			;
		}
	}
	/*
	 * 
	 * public static void main(String[] args) { DicomClientSession s = null; try {
	 * Dicom.init(new AE("CLIENT"));
	 * 
	 * new DicomStoreTest().run();
	 * 
	 * 
	 * //xxDicomStoreIterator.test(); if (1==1) return;
	 * 
	 * 
	 * s = new DicomClientSession(RoleMap.getClientRoleMap()); /*
	 * s.addSOPClass(UID.Verification); s.addSOPClass(UID.CTImageStorage);
	 * s.addSOPClass(UID.MRImageStorage);
	 * s.addSOPClass(UID.StudyRootQueryRetrieveInformationModelFIND);
	 * s.addSOPClass(UID.StudyRootQueryRetrieveInformationModelMOVE);
	 * s.addSOPClass(UID.StudyRootQueryRetrieveInformationModelGET);
	 * s.addSOPClass(UID.PatientRootQueryRetrieveInformationModelFIND);
	 * s.addSOPClass(UID.PatientRootQueryRetrieveInformationModelMOVE);
	 * s.addSOPClass(UID.PatientRootQueryRetrieveInformationModelGET);
	 * s.addSOPClass(UID.PatientStudyOnlyQueryRetrieveInformationModelFIND);
	 * s.addSOPClass(UID.PatientStudyOnlyQueryRetrieveInformationModelMOVE);
	 * s.addSOPClass(UID.PatientStudyOnlyQueryRetrieveInformationModelGET);
	 * 
	 * 
	 * s.connect("server", "localhost", 5104); //s.connect("win2k", 5104); List
	 * mresults = new ArrayList(); CMove move = new CMove(s,
	 * Dicom.MOVE_PATIENT_ROOT, Dicom.IMAGE_LEVEL,
	 * CFind.searchByPatientName(null), AEMap.get("server"), mresults);
	 * move.run();
	 * 
	 * for (int i=0; i<mresults.size(); i++)
	 * Log("RESULT="+mresults.get(i).toString());
	 * 
	 * if (1==1) return;
	 * 
	 * 
	 * 
	 * 
	 * 
	 * List results = new ArrayList(); CFind find = new CFind(s,
	 * Dicom.FIND_STUDY_ROOT, Dicom.IMAGE_LEVEL,
	 * CFind.searchByPatientName(null), results); find.run();
	 * 
	 * for (int i=0; i<results.size(); i++)
	 * Log("RESULT="+results.get(i).toString());
	 * 
	 * if (1==1) return;
	 * 
	 * 
	 * 
	 * Iterator iter = DicomStore.get().iterator(); //iter.next(); Image image =
	 * (Image)iter.next(); CStore store = new CStore(s, image.imageUID);
	 * store.run();
	 * 
	 * if (store == store) return;
	 * 
	 * CEcho echo = new CEcho(s);
	 * 
	 * //for (;;) while (s == s) echo.run();
	 * 
	 * if (1==1) return; //CMove move = new CMove(s);
	 *  // move.run(Dicom.STUDY_ROOT, AEMap.get("SCP"), results);
	 * 
	 * 
	 * 
	 * if (0==0) return;
	 *  // Image image = (Image)DicomStore.get().iterator().next(); // CStore
	 * store = new CStore(s, image.imageUID); // store.run();
	 * 
	 *  } catch (Exception e) { e.printStackTrace(); Log("main caught "+e);
	 *  } finally { if (s != null) s.close(true); }
	 *  }
	 * 
	 * 
	 * 
	 */
}
