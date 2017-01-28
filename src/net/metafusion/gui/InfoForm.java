package net.metafusion.gui;

import javax.swing.JOptionPane;
import net.metafusion.admin.ServerBean;
import net.metafusion.admin.StorageBean;

public class InfoForm extends MFForm
{
	// width='50' align='right' grow='true'
	public InfoForm()
	{
		String def = "right:pref, pref";
		appendSeparator("LocalStore Properties");
		appendLine(new String[] { "[text id='ae' label='AE:' len='16' enabled='false' width='50' align='right'  ]", });
		appendLine(new String[] { "[text id='host' label='Host:' len='16' enabled='false' width='50' align='right'  ]", });
		appendLine(new String[] { "[text id='port' label='Port:' len='5' enabled='false' width='50' align='right' ]", });
		appendLine(new String[] { "[gap width='45']", "[check id='active' text='Active' ]", });
		appendSeparator("Storage");
		appendLine(new String[] {
		// "[text id='total' label='Total Size (MB):' len='8' enabled='false'
		// gap='5']",
				"[text id='free' label='Free Size (GB):' len='6' enabled='false' gap='10']", "[text id='perFree' len='3' enabled='false']", "[label text='% used.' ]" });
		appendSeparator("Archive");
		appendLine(new String[] {
				// "[text id='total' label='Total Size (MB):' len='8'
				// enabled='false' gap='5']",
				"[check id='active' text='' ]", "[label text=' Burn DVD ' ]", "[combo text='DAILY;WEEKLY;MONTHLY' value='DAILY' id='pollstart' width='80' gap='2']",
				"[text id='free' label=' using server ' value='primera' len='18' enabled='false' gap='10']", });
		buildButtonBar(new String[] { "[GLUE]", "Save", "Cancel", "Verify", "Archive Now" });
	}
	private ServerBean serverBean = new ServerBean();
	private StorageBean storageBean = new StorageBean();

	public void reset(ServerBean serverBean)
	{
		this.serverBean = serverBean;
		storageBean = admin.getStorage(serverBean);
		setText("ae", serverBean.getAE());
		setText("host", admin.getHost());
		setText("port", "" + admin.getPort());
		setSelected("active", serverBean.isActive());
		// setText("total", ""+(storageBean.getTotalSize()/1024));
		setText("free", "" + (storageBean.getFreeString()));
		setText("perFree", "" + storageBean.getPerFreeString());
	}

	public void onSave()
	{
		admin.setActive(serverBean, isSelected("active"));
		serverBean.save();
		admin.commit();
	}

	public void onCancel()
	{
		reset(serverBean);
	}

	public void onVerify()
	{
		boolean good = admin.verifyAE(serverBean);
		if (good)
		{
			JOptionPane.showMessageDialog(this.getMainPanel(), serverBean.getAE() + " verified.");
			reset(serverBean);
		} else JOptionPane.showMessageDialog(this.getMainPanel(), serverBean.getAE() + " verify FAILED.");
	}

	public void onArchive()
	{
		JOptionPane.showMessageDialog(this.getMainPanel(), "Not supported in this configuration.");
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		InfoForm f = new InfoForm();
		f.reset(new ServerBean("foo"));
		f.testForm();
	}
}