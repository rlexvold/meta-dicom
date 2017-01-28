package net.metafusion.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import net.metafusion.admin.ServerBean;

public class MainFrame
{
	static void log(String s)
	{
		System.out.println(s);
	}

	public static MainFrame get()
	{
		return mainFrame;
	}
	private static MainFrame mainFrame;

	public MainFrame()
	{
		mainFrame = this;
	}
	static JFrame frame;
	DirectoryForm dir = new DirectoryForm();
	InfoForm ls = new InfoForm();
	JSplitPane splitPane;

	static public JFrame getFrame()
	{
		return frame;
	}

	public JComponent getPanel()
	{
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, dir.getPanel(), getCardPanel());
		// splitPane.setDividerLocation(150);
		splitPane.setResizeWeight(0.0);
		Dimension minimumSize = new Dimension(100, 50);
		dir.getPanel().setMinimumSize(minimumSize);
		return splitPane;
	}
	JPanel p;
	CardLayout cl = new CardLayout();
	MetaFusionForm metaFusionForm = new MetaFusionForm();
	LocalStoreListForm localStoreListForm = new LocalStoreListForm();
	ProxyListForm proxyListForm = new ProxyListForm();
	LocalStoreForm localStoreForm = new LocalStoreForm();
	ProxyForm proxyForm = new ProxyForm();

	void refresh()
	{
		dir.refresh();
		metaFusionForm.refresh();
		localStoreListForm.refresh();
		proxyListForm.refresh();
	}

	void switchMetaFusion()
	{
		cl.show(p, "MetaFusion");
	}

	void switchLocalStoreList()
	{
		cl.show(p, "LocalStoreList");
	}

	void switchProxyList()
	{
		// proxyListForm.reset(sb);
		cl.show(p, "ProxyList");
	}

	void switchLocalStore(ServerBean sb)
	{
		log("switchLocalStore " + sb.getName());
		localStoreForm.reset(sb);
		cl.show(p, "LocalStoreForm");
	}

	void switchProxy(ServerBean pb)
	{
		log("switchProxy " + pb.getName());
		proxyForm.reset(pb);
		cl.show(p, "ProxyForm");
	}

	JComponent getCardPanel()
	{
		p = new JPanel(cl);
		p.add(metaFusionForm.getPanel(), "MetaFusion");
		p.add(localStoreListForm.getPanel(), "LocalStoreList");
		p.add(proxyListForm.getPanel(), "ProxyList");
		p.add(localStoreForm.getPanel(), "LocalStoreForm");
		p.add(proxyForm.getPanel(), "ProxyForm");
		return p;
	}
	static File homeIcon;
	static File serverIcon;
	static File badServerIcon;
	static File frameIcon;

	static public File getHomeIcon()
	{
		return homeIcon;
	}

	static public File getServerIcon()
	{
		return serverIcon;
	}

	static public File getBadServerIcon()
	{
		return badServerIcon;
	}

	static public File getFrameIcon()
	{
		return frameIcon;
	}

	public static void main(String[] args)
	{
		try
		{
			// acme.util.Log.init(new
			// File(args[0]).getParentFile().getParentFile(), "gui", null);
			// XMLConfigFile configFile = new XMLConfigFile(new File(args[0]));
			// File root = XMLConfigFile.getDefault().getConfigRoot();
			File root = new File(".");
			homeIcon = new File(root, "home16.gif");
			serverIcon = new File(root, "server16.gif");
			badServerIcon = new File(root, "badserver16.gif");
			frameIcon = new File(root, "frameIcon16.gif");
			// GUITask.setGUITaskRunnerFactory(); // set runner with dialog
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		SwingUtil.setLookAndFeel();
		// AdminClient.setGUI(true);
		MainFrame f = new MainFrame();
		if (!SystemFormDialog.init("10.80.7.61", "5219")) System.exit(-1);
		frame = new JFrame();
		frame.setTitle("MetaFusion");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(f.getPanel());
		frame.setSize(800, 620);
		frame.setLocation(100, 100);
		SwingUtil.setDefaultIcon(Toolkit.getDefaultToolkit().createImage(f.getFrameIcon().getAbsolutePath()));
		frame.setIconImage(SwingUtil.getDefaultIconImage());
		frame.show();
	}
}