package net.metafusion.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.metafusion.admin.ServerBean;

public class RootLeafTreeCellRenderer extends DefaultTreeCellRenderer
{
	ImageIcon rootIcon;
	ImageIcon leafIcon;
	ImageIcon leafIcon2;

	public RootLeafTreeCellRenderer(ImageIcon rootIcon, ImageIcon leafIcon, ImageIcon leafIcon2)
	{
		this.rootIcon = rootIcon;
		this.leafIcon = leafIcon;
		this.leafIcon2 = leafIcon2;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (leaf)
			setIcon(((ServerBean) value).isInvalid() ? leafIcon2 : leafIcon);
		else if (tree.getModel().getRoot() == value) setIcon(rootIcon);
		return this;
	}
}