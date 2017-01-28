package net.metafusion.gui;

import acme.util.Util;

public class SetupForm extends MFForm
{
	public SetupForm()
	{
		appendSeparator("MetaFusion System Properties");
		appendLine(new String[] { "[text id='host' label='LocalStore Host: ' len='16' enabled='false' gap='5' ]", });
		appendLine(new String[] { "[text id='port' label='LocalStore Port: ' len='5' enabled='false' gap='5' ]", });
		appendLine(new String[] { "[text id='version' label='Configuration Save Date: ' len='32' enabled='false' gap='5' ]", });
		appendLine(new String[] { "[text id='license' label='Server License Expiration: ' len='32' enabled='false' gap='5' ]", });
		appendLine(new String[] { "[text id='serverv' label='Server Version: ' len='32' enabled='false' gap='5' ]", });
		appendLine(new String[] { "[text id='guiv' label='GUI Version: ' len='32' enabled='false' gap='5' ]", });
		buildButtonBar(new String[] { "[GLUE]", "Open", "Save" });
		refresh();
	}

	public void refresh()
	{
		setText("host", admin.getHost());
		setText("port", "" + admin.getPort());
		setText("version", admin.getCommitDate());
		setText("license", admin.getExpiration());
		setText("serverv", admin.getVersion());
		setText("guiv", Util.getManifestVersion());
	}

	public void onOpen()
	{
		log("onOpen");
		new SystemFormDialog(admin.getHost(), "" + admin.getPort());
		refresh();
	}

	public void onSave()
	{
		log("onSave");
		admin.commit();
		refresh();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new SetupForm();
		f.testForm();
	}
}