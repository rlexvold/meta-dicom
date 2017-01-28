package net.metafusion.gui;

import java.awt.Dimension;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import acme.util.Util;

public class SystemFormDialog extends MFForm
{
	private String host;
	private String port;
	private boolean good = false;

	public String getHost()
	{
		return host;
	}

	public String getPort()
	{
		return port;
	}

	public boolean isGood()
	{
		return good;
	}

	public SystemFormDialog(String host, String port)
	{
		this.host = host;
		this.port = port;
		appendLine(new String[] { "[text id='host' label='LocalStore Host: '  len='32' width='100' gap='5' align='right' ]", });
		appendLine(new String[] { "[text id='port' label='LocalStore Port: '  len='5' width='100' gap='5' align='right' ]", "[label width='20' ]",
				"[check id='older' text='Select Older Configuration.' ]", });
		buildButtonBar(new String[] { "[GLUE]", "OK", "Cancel" });
		setText("host", host);
		setText("port", port);
		try
		{
			byte[] defHostPort = Util.readWholeFile(new File("gui.pref"));
			if (defHostPort != null)
			{
				String s = new String(defHostPort).trim();
				setText("host", s.substring(0, s.indexOf(':')));
				setText("port", s.substring(s.indexOf(':') + 1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		openModalDialog(null, "Select LocalStore Admin Host");
	}

	public void onOK()
	{
		host = getText("host");
		port = getText("port");
		try
		{
			if (isSelected("older"))
			{
				List l = admin.getOlderConfig(host, Integer.parseInt(port));
				AdminListDialog f = new AdminListDialog(l);
				f.openModalDialog(null, "Select an Admin Version", new Dimension(300, 260));
				if (f.getSelected().length() == 0) return;
				good = admin.loadConfig(host, Integer.parseInt(port), f.getSelected());
			} else
			{
				good = admin.loadConfig(host, Integer.parseInt(port), null);
			}
			if (!good)
				JOptionPane.showMessageDialog(null, "Could not connect to server", "Error", JOptionPane.WARNING_MESSAGE);
			else
			{
				closeModalDialog();
				try
				{
					String s = host + ":" + port;
					Util.writeFile(s.getBytes(), new File("gui.pref"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			MainFrame.get().refresh();
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

	public static boolean init(String host, String port)
	{
		SystemFormDialog sfd = new SystemFormDialog(host, port);
		return sfd.isGood();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		SystemFormDialog f = new SystemFormDialog("host", "100");
		// f.openModalDialog(null, ae != null ? "Update AE" : "Add AE");
		// f.testForm();
	}
}