package net.metafusion.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import net.metafusion.admin.AEBean;

public class SelectAEDialog extends MFForm
{
	List l;
	String selected = "";
	AEBean aeBean[];

	public SelectAEDialog()
	{
		aeBean = admin.getAE();
		l = new ArrayList();
		for (int i = 0; i < aeBean.length; i++)
			l.add(aeBean[i].getName());
		register("tableModel", model);
		// appendSeparator("AE to send to");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='id' model='tableModel' ]" });
		;
		buildButtonBar(new String[] { "[GLUE]", "OK", "Cancel" });
	}

	public SelectAEDialog(List l)
	{
		this.l = l;
		register("tableModel", model);
		appendSeparator("AE to send to");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='id' model='tableModel' ]" });
		;
		buildButtonBar(new String[] { "[GLUE]", "OK", "Cancel" });
	}
	class AdminListModel extends AbstractTableModel
	{
		public AdminListModel()
		{
		}
		String cols[] = { "Name" };

		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return l.size();
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			return l.get(row);
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		public void setValueAt(Object value, int row, int col)
		{
		}
	};
	AdminListModel model = new AdminListModel();

	public void onOK()
	{
		try
		{
			selected = (String) getSelectedObject("id");
			closeModalDialog();
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

	public String getSelected()
	{
		return selected;
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		ArrayList l = new ArrayList();
		l.add("foo");
		l.add("bar");
		l.add("baz");
		l.add("foo");
		l.add("bar");
		l.add("baz");
		l.add("foo");
		l.add("bar");
		l.add("baz");
		l.add("foo");
		l.add("bar");
		l.add("baz");
		l.add("foo");
		l.add("bar");
		l.add("baz");
		l.add("foo");
		l.add("bar");
		l.add("baz");
		SelectAEDialog f = new SelectAEDialog(l);
		f.openModalDialog(null, "Select an AE", new Dimension(300, 260));
		// f.testForm();
		log(f.getSelected());
	}
}