package net.metafusion.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import net.metafusion.admin.AdminClient;
import acme.util.Util;

public class GUIAdminClient extends AdminClient
{
	public GUIAdminClient(String host, int port)
	{
		super(host, port);
		async = true;
	}

	@Override
	protected void onRun()
	{
		log("onRun");
		JFrame frame = MainFrame.getFrame();
		if (frame == null)
		{
			while (!isDone())
				Util.sleep(250);
			Util.sleep(250);
			return;
		}
		log("onRun sleep 50");
		Util.sleep(50);
		long showAt = System.currentTimeMillis() + 2000;
		while (!isDone() && System.currentTimeMillis() < showAt)
			Util.sleep(250);
		if (!isDone())
		{
			showDialog(frame, "Communicating with server...");
			log("back from showdialog");
		}
		while (!isDone())
			Util.sleep(250);
	}
	private JDialog dialog;
	private JFrame frame;

	private void showDialog(JFrame frame, String msg)
	{
		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		final JOptionPane optionPane = new JOptionPane(pb, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { "Cancel" });
		dialog = new JDialog(frame, msg, true);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				;// block close
			}
		});
		dialog.setLocation(250, 250);
		optionPane.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();
				if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY)))
				{
					log("call cancel...");
					try
					{
						close();
					}
					catch (Exception e1)
					{
						log("cancel caught...");
						e1.printStackTrace();
					}
				}
			}
		});
		Action timerAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isDone())
				{
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		};
		Timer t = new Timer(300, timerAction);
		t.start();
		dialog.pack();
		dialog.setVisible(true);
		t.stop();
	}
	// static class TestRunnable implements Runnable, Cancellable {
	// volatile boolean stop = false;
	// public void run() {
	// for (;;) {
	// Util.sleep(250);
	// if (stop)
	// break;
	// }
	// }
	// public void cancel() {
	// stop = true;
	// }
	// }
	//
	// public static void main(String[] args) {
	// log("Test");
	// try {
	// net.metafusion.gui.GUITask t = new net.metafusion.gui.GUITask();
	// t.runWithProgress(new net.metafusion.gui.GUITask.TestRunnable(),
	// "msgmsgmg", 2000);
	// } catch (Exception e) {
	// log("test caught "+e);
	// }
	// log("exit Test");
	// }
	//
	// }
}