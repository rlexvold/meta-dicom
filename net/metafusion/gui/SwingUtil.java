/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Jan 24, 2004
 * Time: 3:46:58 PM
 */
package net.metafusion.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import acme.util.NestedException;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class SwingUtil
{
	static public void log(String s)
	{
		System.out.println(s);
	}

	// { "Button", "[GLUE]", "Do Something:Do" }
	public static Component buildButtonBar(Object target, String def[])
	{
		ButtonBarBuilder builder = new ButtonBarBuilder();
		boolean needGap = false;
		for (int i = 0; i < def.length; i++)
		{
			String s = def[i];
			if (s.startsWith("["))
			{
				if (s.equals("[GLUE]"))
				{
					builder.addGlue();
				} else log("ButtonBar: unknown tag " + s);
				needGap = false;
			} else
			{
				String cmd = s;
				int colonIndex = s.indexOf(':');
				if (colonIndex != -1)
				{
					cmd = s.substring(colonIndex + 1);
					s = s.substring(0, colonIndex);
					builder.addRelatedGap();
				}
				builder.addGridded(new JButton(s));
				if (needGap)
				{
					builder.addRelatedGap();
				}
				needGap = true;
			}
		}
		return builder.getPanel();
	}
	private static ActionEvent lastActionEvent = null;

	public static ActionEvent getLastActionEvent()
	{
		return lastActionEvent;
	}

	public static String getExceptionString(Exception e)
	{
		String msg = e.getMessage();
		if (msg == null || msg.length() == 0) return e.toString();
		return msg;
	}
	static class RListener implements ActionListener
	{
		Object target;
		String name;
		Method m;

		public RListener(Object target, String name)
		{
			try
			{
				this.target = target;
				this.name = name;
				Class k = target.getClass();
				m = k.getMethod(name, null);
			}
			catch (NoSuchMethodException e)
			{
				log("Listener " + name + "caught " + e);
				e.printStackTrace();
				throw new NestedException(e);
			}
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				log("action: " + name);
				lastActionEvent = event;
				m.invoke(target, null);
			}
			catch (Exception e)
			{
				log("call " + name + " caught " + e);
				e.printStackTrace();
				throw new NestedException(e);
			}
		}
	}

	public static void addListener(JButton button, Object o, String name)
	{
		button.addActionListener(new SwingUtil.RListener(o, name));
	}

	public static void setLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
		}
		catch (Exception e)
		{
			// Likely PlasticXP is not in the class path; ignore.
		}
	}

	static public void show(Component panel)
	{
		JFrame frame = new JFrame();
		frame.setTitle("TestForm");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.show();
	}
	static public class Decorate extends JPanel
	{
		private boolean grow;
		private int width;

		public Decorate(Component c, int width, boolean grow)
		{
			this.grow = grow;
			this.width = width;
			this.setLayout(new BorderLayout());
			add(c, BorderLayout.CENTER);
		}

		public Dimension getMinimumSize()
		{
			Dimension d = getPreferredSize();
			if (width != -1) return new Dimension(width, (int) d.getHeight());
			return d;
		}

		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			if (width != -1) return new Dimension(width, (int) d.getHeight());
			return d;
		}

		public Dimension getMaximumSize()
		{
			return grow ? super.getMaximumSize() : getPreferredSize();
		}
	}

	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	static public void expandAll(JTree tree, boolean expand)
	{
		Object root = tree.getModel().getRoot();
		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	static private void expandAll(JTree tree, TreePath parent, boolean expand)
	{
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0)
		{
			for (Enumeration e = node.children(); e.hasMoreElements();)
			{
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand)
		{
			tree.expandPath(parent);
		} else
		{
			tree.collapsePath(parent);
		}
	}
	static private Image defaultIconImage;

	static public Image getDefaultIconImage()
	{
		return defaultIconImage;
	}

	static public void setDefaultIcon(Image iconImage)
	{
		defaultIconImage = iconImage;
	}

	public static File chooseDir(Component parent, String title)
	{
		JFileChooser chooser = new JFileChooser();
		// chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setApproveButtonText("Select Dir");
		return chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
	}

	public static void main(String[] args)
	{
	}
}
