package net.metafusion.medicare.ictk;

import java.io.File;
import java.util.Vector;

import acme.util.Log;

public class ICtkAPI
{
	private static ICtkAPI	instance	= null;

	public static ICtkAPI getInstance()
	{
		if (instance == null)
			instance = new ICtkAPI();
		return instance;
	}

	public ICtkAPI()
	{
	}

	public void init(File wrapperDll, File easyclaimDll) throws Exception
	{
		if (wrapperDll == null)
		{
			Exception e = new Exception("No Native Library path set");
			Log.log("ictk.native.file not set in Ris4d.props", e);
			return;
		}
		Log.log("java.library.path=" + System.getProperty("java.library.path"));
		System.load(wrapperDll.getAbsolutePath());
		setLibrary(easyclaimDll.getAbsolutePath());
	}

	native public int setLibrary(String library);
	
	native public static String getVersionId();

	native public int acceptContent(int sess, String gets);

	native public int addContent(int sess, String gets);

	native public int authoriseContent(int sess, String gets, Vector getv);

	native public int cancelBusinessObject(int sess, String gets);

	native public int cancelTransmission(int sess);

	native public int clearReport(int sess);

	native public int createBusinessObject(int sess, String gets, String gets2, String gets3, Vector getv);

	native public int createReport(int sess, String gets, String gets2);

	native public int createSessionEasyclaim(String gets, String gets2);

	native public int createTransmission(int sess, String gets);

	native public int destroySessionEasyclaim(int i, int j);

	native public int EftPosTransaction(String TransType, String Amount, String BSBNumber, String AccountNumber, String Printer, String output, int sizeString);

	native public int getBusinessObjectCondition(int sess, String gets, String gets2, Vector getv);

	native public int getBusinessObjectElement(int sess, String gets, String gets2, Vector getv);

	native public int getContent(int sess, Vector getv);

	native public int getErrorText(int sess, String gets, Vector getv);

	native public int getNextReport(int sess);

	native public int getNextReportRow(int sess);

	native public int getReportElement(int sess, String gets, Vector getv);

	native public int getSessionElement(int sess, String gets, Vector getv);

	native public int getTransmissionElement(int sess, String gets, Vector getv);

	native public int getUniqueId(int sess, Vector getv);

	native public int includeContent(int sess);

	native public int isReportAvailable(int sess);

	native public int isSignatureRequired(int sess);

	native public int listBusinessObject(int sess, String gets, String gets2, Vector getv);

	native public int loadContent(int sess, String gets, String gets2, String gets3);
	native public int loadEasyclaim();

	native public int resetBusinessObjectElement(int sess, String gets, String gets2);

	native public int resetReport(int sess);

	native public int resetSession(int sess);

	native public int sendContent(int sess, String gets, String gets2);

	native public int sendTransmission(int sess);

	native public int setBusinessObjectCondition(int sess, String gets, String gets2);

	native public int setBusinessObjectElement(int sess, String gets, String gets2, String gets3);

	native public int setSessionElement(int sess, String gets, String gets2);

	native public int setTransmissionElement(int sess, String gets, String gets2);

	native public int unloadContent(int sess, Vector getv);

	native public int unloadReport(int sess, Vector getv);
}
