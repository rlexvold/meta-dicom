package net.metafusion.gui;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.metafusion.admin.AdminSession;
import net.metafusion.admin.ServerBean;

public class DirectoryForm implements TreeSelectionListener
{
	static void log(String s)
	{
		System.out.println(s);
	}
	AdminSession admin = AdminSession.get();
	ServerBean lsb[] = new ServerBean[0];
	ServerBean pb[] = new ServerBean[0];
	private JTree tree;
	HashTreeModel treeModel;
	String root = "MetaFusion";
	String localRoot = "LocalStore";
	String proxyRoot = "Proxy";

	public DirectoryForm()
	{
		tree = new JTree();
		refresh();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new RootLeafTreeCellRenderer(new ImageIcon(MainFrame.get().getHomeIcon().getAbsolutePath()), new ImageIcon(MainFrame.get().getServerIcon()
				.getAbsolutePath()), new ImageIcon(MainFrame.get().getBadServerIcon().getAbsolutePath())));
		expand();
	}

	void expand()
	{
		tree.expandPath(new TreePath(new Object[] { root, localRoot }));
		tree.expandPath(new TreePath(new Object[] { root, proxyRoot }));
	}

	public JComponent getPanel()
	{
		return tree;
	}

	public void refresh()
	{
		lsb = admin.getLocalStores();
		pb = admin.getProxies();
		treeModel = new HashTreeModel();
		treeModel.setRootChildren(root, new Object[] { localRoot, proxyRoot });
		treeModel.setChildren(localRoot, lsb);
		treeModel.setChildren(proxyRoot, pb);
		tree.setModel(treeModel);
		expand();
	}

	// JPanel getPanelForNode(Object node) {
	// if (node == root)
	// return metaForm.getPanel();
	// if (node.getParent() == oldgui.localStoreNode)
	// return localStoreForm.getPanel();
	// if (node.getParent() == oldgui.proxyNode)
	// return localStoreForm.getPanel();
	// if (node.getParent() == oldgui.localStoreNode)
	// return localStoreForm.getPanel();
	// if (node.getParent().getParent() == oldgui.localStoreNode) {
	// if (node.getUserObject().equals("Studies")) {
	// return studyForm.getPanel();
	// }
	// if (node.getUserObject().equals("Rules")) {
	// return rulesForm.getPanel();
	// }
	// if (node.getUserObject().equals("HIPAA Log")) {
	// return new HIPAAForm().getPanel();
	// }
	//
	//
	// return localStoreForm.getPanel();
	// }
	//
	// return null;
	// }
	public void valueChanged(TreeSelectionEvent e)
	{
		Object node = tree.getLastSelectedPathComponent();
		// JPanel subPanel = getPanelForNode(node);
		log("" + node);
		MainFrame mainFrame = MainFrame.get();
		if (node == root)
			mainFrame.switchMetaFusion();
		else if (node == localRoot)
			mainFrame.switchLocalStoreList();
		else if (node == proxyRoot)
			mainFrame.switchProxyList();
		else if (node instanceof ServerBean && !((ServerBean) node).isProxy())
		{
			mainFrame.switchLocalStore((ServerBean) node);
		} else if (node instanceof ServerBean)
		{
			mainFrame.switchProxy((ServerBean) node);
		}
		// if (panel != null) {
		// panel.removeAll();
		// panel.add(subPanel);
		// mainPanel.validate();
		// mainPanel.repaint();
		// }
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		DirectoryForm f = new DirectoryForm();
		SwingUtil.show(f.getPanel());
	}
}