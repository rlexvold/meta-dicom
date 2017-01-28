package net.metafusion.localstore.web;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import acme.util.Util;
import acme.util.swing.MainFrame;

public class ViewerApplet extends JApplet
{
	public void init()
	{
		// Execute a job on the event-dispatching thread:
		// creating this applet's GUI.
		try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					createGUI();
				}
			});
		}
		catch (Exception e)
		{
			System.err.println("createGUI didn't successfully complete");
		}
	}

	private void createGUI()
	{
		try
		{
			// JLabel label = new JLabel();
			//
			// ImageIcon ii = new ImageIcon(new
			// URL("http://127.0.0.1:8090/WebView/xxx/1143515235531.jpg"));
			// //Image i = getImage("http://localhost:8080/");
			// label.setIcon(ii);
			// label.setHorizontalAlignment(JLabel.CENTER);
			// label.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.black));
			// this.getToolkit().
			URL url = this.getCodeBase();
			String host = url.getHost();
			if (host == null || host.length() == 0) host = "localhost";
			String studyid = getParameter("studyid");
			if (studyid == null || studyid.length() == 0) studyid = "1143515236015";
			getContentPane().add(new ViewerPanel(host, studyid), BorderLayout.CENTER);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}
	static class StudyInfo
	{
		String name = "";
		SeriesInfo series[];

		public String toString()
		{
			return name;
		}
	}
	static class SeriesInfo
	{
		String name = "";
		ImageInfo images[];

		public String toString()
		{
			return name;
		}
	}
	static class ImageInfo
	{
		String name = "";
		String path = "";

		public String toString()
		{
			return name;
		}
	}

	static List getURLText(URL url)
	{
		ArrayList al = new ArrayList();
		BufferedReader rd = null;
		try
		{
			URLConnection conn = url.openConnection();
			// conn.setDoOutput(true);
			// OutputStreamWriter wr = new
			// OutputStreamWriter(conn.getOutputStream());
			// wr.write(data);
			// wr.flush();
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			boolean started = false;
			while ((line = rd.readLine()) != null)
			{
				line = line.trim();
				// if (started) {
				al.add(line);
				// }
				// if (line.length() == 0)
				// started = true;
			}
		}
		catch (Exception e)
		{
			Util.log("", e);
		}
		finally
		{
			Util.safeClose(rd);
		}
		return al;
	}

	static ImageIcon getImageIcon(String host, String path)
	{
		try
		{
			URL url = new URL("http://" + host + ":8888/WebView/" + path);
			ImageIcon iii = new ImageIcon(url);
			return iii;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}

	static StudyInfo getStudyInfo(String host, String studyid)
	{
		try
		{
			StudyInfo si = null;
			SeriesInfo ssi = null;
			ImageInfo ii = null;
			int sc = 0;
			int ic = 0;
			List l = getURLText(new URL("http://" + host + ":8888/WebView/" + studyid));
			for (int i = 0; i < l.size(); i++)
			{
				String line = (String) l.get(i);
				String s[] = line.split(":");
				if (s[0].equals("study"))
				{
					si = new StudyInfo();
					si.name = s[1];
					si.series = new SeriesInfo[Integer.parseInt(s[2])];
				} else if (s[0].equals("series"))
				{
					ssi = new SeriesInfo();
					ssi.name = s[1];
					ssi.images = new ImageInfo[Integer.parseInt(s[2])];
					si.series[sc++] = ssi;
					ic = 0;
				} else if (s[0].equals("image"))
				{
					ii = new ImageInfo();
					ii.path = s[1];
					ssi.images[ic++] = ii;
					ii.name = "Image " + ic;
				} else
				{
					throw new RuntimeException("Could not read study data!");
				}
			}
			return si;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}
	static class StudyInfoTreeModel implements TreeModel
	{
		StudyInfo studyInfo = new StudyInfo();

		public Object getRoot()
		{
			return studyInfo;
		}

		public Object getChild(Object parent, int index)
		{
			if (parent == studyInfo) { return studyInfo.series[index]; }
			if (parent instanceof SeriesInfo) { return ((SeriesInfo) parent).images[index]; }
			return null;
		}

		public int getChildCount(Object parent)
		{
			if (parent == studyInfo) { return studyInfo.series.length; }
			if (parent instanceof SeriesInfo) { return ((SeriesInfo) parent).images.length; }
			return 0;
		}

		public boolean isLeaf(Object node)
		{
			return node instanceof ImageInfo;
		}

		public void valueForPathChanged(TreePath path, Object newValue)
		{
		}

		public int getIndexOfChild(Object parent, Object child)
		{
			throw new RuntimeException("called getIndexOfChild");
			// return 0;
		}

		public void addTreeModelListener(TreeModelListener l)
		{
		}

		public void removeTreeModelListener(TreeModelListener l)
		{
		}
	};
	static class ViewerPanel extends JPanel
	{
		public ViewerPanel(String host, String studyid)
		{
			this.host = host;
			this.studyid = studyid;
			treeModel = new StudyInfoTreeModel();
			studyInfo = getStudyInfo(host, studyid);
			initComponents();
		}
		String host;
		String studyid;

		// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
		private void initComponents()
		{
			jSplitPane1 = new javax.swing.JSplitPane();
			jScrollPane1 = new javax.swing.JScrollPane();
			jTree1 = new javax.swing.JTree();
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			setLayout(new java.awt.BorderLayout());
			setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
			jScrollPane1.setViewportView(jTree1);
			jSplitPane1.setLeftComponent(jScrollPane1);
			jSplitPane1.setRightComponent(jLabel1);
			add(jSplitPane1, java.awt.BorderLayout.CENTER);
			treeModel.studyInfo = studyInfo;
			jTree1.setModel(treeModel);
			// jLabel1.setIcon(imageIcon);
			jTree1.addMouseListener(ml);
		}
		StudyInfoTreeModel treeModel = null;
		StudyInfo studyInfo = null;
		ImageIcon imageIcon = null;
		// Variables declaration - do not modify
		private javax.swing.JLabel jLabel1;
		private javax.swing.JScrollPane jScrollPane1;
		private javax.swing.JSplitPane jSplitPane1;
		private javax.swing.JTree jTree1;
		// End of variables declaration
		MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int selRow = jTree1.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = jTree1.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1)
				{
					Util.log("double " + selRow + " " + selPath);
					Object path[] = selPath.getPath();
					if (selPath.getPath().length == 3)
					{
						imageIcon = getImageIcon(host, ((ImageInfo) path[2]).path);
						jLabel1.setIcon(imageIcon);
						;// Command.open(selPath.getLastPathComponent()).run();
						Util.log("");
					}
				}
			}
		};
	}

	public static void main(String[] args)
	{
		JFrame frame = MainFrame.create(new ViewerPanel("localhost", "1143515236015"), new Dimension(512, 512));
	}
}