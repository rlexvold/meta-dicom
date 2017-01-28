package net.metafusion.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class AdminListDialog extends MFForm
{
	List l;
	String selected = "";

	public AdminListDialog(List l)
	{
		this.l = l;
		register("tableModel", model);
		appendSeparator("Admin Version");
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
		AdminListDialog f = new AdminListDialog(l);
		f.openModalDialog(null, "Select an Admin Version", new Dimension(300, 260));
		// f.testForm();
		log(f.getSelected());
	}
}