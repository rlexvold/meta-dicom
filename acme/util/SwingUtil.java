package acme.util;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class SwingUtil
{
	static boolean lookAndFeelSet = false;

	public static void setNativeLookAndFeel()
	{
		if (lookAndFeelSet) return;
		try
		{
			lookAndFeelSet = true;
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			System.out.println("Error setting native LAF: " + e);
		}
	}

	public static void showDialogPanel(JPanel panel)
	{
		final JPanel showPanel = panel;
		SwingUtil.setNativeLookAndFeel();
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				JDialog dialog = new JDialog();
				dialog.setLocation(100, 100);
				dialog.setContentPane(showPanel);
				dialog.pack();
				dialog.setVisible(true);
			}
		});
	}
	static JDialog modalDialog;
	static boolean returnOK;

	public static boolean showModalDialog(final JFrame frame, final String title, final JPanel panel)
	{
		SwingUtil.setNativeLookAndFeel();
		returnOK = false;
		modalDialog = new JDialog(frame, title, true);
		modalDialog.setContentPane(panel);
		modalDialog.setLocation(100, 100);
		modalDialog.pack();
		modalDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		modalDialog.setVisible(true);
		return returnOK;
	}

	public static void closeModalDialog(boolean returnOK)
	{
		SwingUtil.returnOK = returnOK;
		if (modalDialog != null) modalDialog.dispose();
		modalDialog = null;
	}
}