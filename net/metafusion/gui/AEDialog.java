package net.metafusion.gui;

import java.awt.Component;
import javax.swing.JOptionPane;
import net.metafusion.admin.AEBean;

public class AEDialog extends MFForm
{
	private boolean isUpdate;

	public AEDialog()
	{
		this(null, false);
	}

	public AEDialog(AEBean ae)
	{
		this(ae, true);
	}

	public AEDialog(AEBean ae, boolean isUpdate)
	{
		this.isUpdate = isUpdate;
		appendLine(new String[] { "[text id='name' label='Name: '  len='18' width='35' gap='5' align='right' ]", });
		appendLine(new String[] { "[text id='host' label='Host: '  len='32' width='35' gap='5' align='right' ]", });
		appendLine(new String[] { "[text id='port' label='Port: '  len='6' width='35' gap='5' align='right' ]", });
		buildButtonBar(new String[] { "[GLUE]", "OK", "Cancel" });
		if (ae != null)
		{
			setText("name", ae.getName());
			setText("host", ae.getHost());
			setText("port", ae.getPort());
		}
		if (isUpdate) ((Component) get("name")).setEnabled(false);
		openModalDialog(null, isUpdate ? "Update AE" : "Add AE");
	}

	public void onOK()
	{
		AEBean ae = new AEBean(getText("name"), getText("host"), getText("port"));
		try
		{
			if (isUpdate)
				admin.updateAE(ae, ae.getHost(), ae.getPort());
			else admin.addAE(ae);
			closeModalDialog();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, SwingUtil.getExceptionString(e), "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void onCancel()
	{
		closeModalDialog();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		AEBean ae = new AEBean("foo", "localhost", "90");
		AEDialog f = new AEDialog(ae, true);
		// f.openModalDialog(null, ae != null ? "Update AE" : "Add AE");
		// f.testForm();
	}
}