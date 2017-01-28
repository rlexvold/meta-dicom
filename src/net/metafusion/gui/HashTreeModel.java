package net.metafusion.gui;

import java.util.HashMap;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class HashTreeModel implements TreeModel
{
	private static void log(String s)
	{
		System.out.println(s);
	}
	private HashMap childMap = new HashMap();
	private Object root;

	public HashTreeModel()
	{
	}

	public void setRootChildren(Object o, Object oa[])
	{
		root = o;
		childMap.put(o, oa);
	}

	public void setChildren(Object o, Object oa[])
	{
		childMap.put(o, oa);
	}

	public Object getRoot()
	{
		return root;
	}

	public Object getChild(Object parent, int index)
	{
		Object[] oa = (Object[]) childMap.get(parent);
		if (oa != null && index < oa.length) return oa[index];
		return null;
	}

	public int getChildCount(Object parent)
	{
		Object[] oa = (Object[]) childMap.get(parent);
		if (oa != null) return oa.length;
		return 0;
	}

	public boolean isLeaf(Object node)
	{
		return childMap.get(node) == null;
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	public int getIndexOfChild(Object parent, Object child)
	{
		Object[] oa = (Object[]) childMap.get(parent);
		if (oa != null) for (int i = 0; i < oa.length; i++)
			if (child == oa[i]) return i;
		return -1;
	}

	public void addTreeModelListener(TreeModelListener l)
	{
		log("NOT IMPLEMENTED: addTreeModelListener " + 1);
	}

	public void removeTreeModelListener(TreeModelListener l)
	{
	}
}