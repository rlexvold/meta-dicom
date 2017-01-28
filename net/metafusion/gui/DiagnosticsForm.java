package net.metafusion.gui;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import net.metafusion.admin.AdminClient;
import net.metafusion.admin.AdminSession;
import acme.util.XML;

public class DiagnosticsForm extends MFForm
{
	class Diagnostic
	{
		String args[];
		String name;

		Diagnostic(String name, String args[])
		{
			this.name = name;
			this.args = args;
		}

		public XML toXML()
		{
			XML xml = new XML("diagnostic");
			for (int i = 0; i < args.length; i++)
				xml.addAttr("arg" + i, args[i]);
			return xml;
		}
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new DiagnosticsForm();
		MainFrame.frame = new JFrame();
		f.testForm();
	}
	// Diagnostic load = new Diagnostic("loader",
	// new String[] { "Template-File-Path", "NumPatients", "MaxStudies",
	// "MaxSeries", "MaxImages"} );
	Diagnostic logRotate = new Diagnostic("log-rotate", new String[0]);
	Diagnostic recoverFiles = new Diagnostic("recover-files", new String[0]);
	Diagnostic sync = new Diagnostic("request-sync", new String[0]);
	Diagnostic diag[] = new Diagnostic[] { sync, recoverFiles, logRotate };

	public DiagnosticsForm()
	{
		String name = "";
		for (Diagnostic d : diag)
		{
			name += d.name;
			for (String element0 : d.args)
				name += ":" + element0;
			name += ";";
		}
		appendSeparator("MetaFusion Diagnostics");
		appendLine(new String[] { "[combo id='test' text='" + name + "' len='16' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label  text='host' gap='5']", "[text id='host' text='localhost' len='32' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label  text='port' gap='5']", "[text id='port' text='5105' len='6' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label  text='arg1' width='50' gap='5']", "[text id='arg1'  len='16' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label  text='arg2' width='50' gap='5']", "[text id='arg2'  len='16' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label text='arg3' width='50' gap='5']", "[text id='arg3'  len='16' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label  text='arg4' width='50' gap='5']", "[text id='arg4'  len='16' enabled='true' gap='5' ]", });
		appendLine(new String[] { "[label text='arg5' width='50' gap='5']", "[text id='arg5'  len='16' enabled='true' gap='5' ]", });
		buildButtonBar(new String[] { "[GLUE]", "Run" });
		refresh();
	}

	public void onRun()
	{
		log("onRun");
		AdminClient admin = new AdminClient(getText("host"), Integer.parseInt(getText("port")));
		admin.doDiagnostic(getText("test"), getText("arg1"), getText("arg2"));
	}

	public void refresh()
	{
		log("DF.refresh");
		JComboBox cb = (JComboBox) get("test");
		cb.removeAllItems();
		if (AdminSession.get().haveConfig())
		{
			AdminClient admin = AdminSession.get().getCurrentClient();
			String[] s = admin.getDiagnostics();
			for (String element : s)
				cb.addItem(element);
		}
	}
}