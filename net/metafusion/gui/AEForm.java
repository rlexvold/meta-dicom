package net.metafusion.gui;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.metafusion.admin.AEBean;

public class AEForm extends MFForm
{
	AEBean aeArray[] = new AEBean[0];
	TableModel model = new AbstractTableModel()
	{
		String cols[] = { "AE", "Host", "Port" };

		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return aeArray.length;
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			AEBean ae = aeArray[row];
			if (col == 0) return ae.getName();
			if (col == 1) return ae.getHost();
			if (col == 2) return ae.getPort();
			return "";
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		public void setValueAt(Object value, int row, int col)
		{
			// rowData[row][col] = value;
			// fireTableCellUpdated(row, col);
		}
	};

	public AEForm()
	{
		super("fill:default:grow", "");
		register("tableModel", model);
		appendSeparator("Application Entities");
		appendSpace("4dlu");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='table' model='tableModel']" });
		;
		buildButtonBar(new String[] { "[GLUE]", "Add", "Update", "Remove", "Verify" });
		refresh();
	}

	public void refresh()
	{
		aeArray = admin.getAE();
		tableChanged("table");
	}

	public void onAdd()
	{
		new AEDialog();
		refresh();
	}

	AEBean getSelected()
	{
		int index = getSelection("table");
		if (index == -1) return null;
		AEBean ae = aeArray[index];
		return ae;
	}

	public void onUpdate()
	{
		AEBean ae = getSelected();
		if (ae != null) new AEDialog(ae);
		refresh();
	}

	public void onRemove()
	{
		AEBean ae = getSelected();
		if (ae != null) admin.removeAE(ae);
		refresh();
	}

	public void onVerify()
	{
		AEBean ae = getSelected();
		if (ae != null)
		{
			boolean good = admin.verifyAE(ae.getName());
			if (good)
				JOptionPane.showMessageDialog(this.getMainPanel(), ae.getName() + " verified.");
			else JOptionPane.showMessageDialog(this.getMainPanel(), ae.getName() + " verify FAILED.");
		}
		refresh();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new AEForm();
		f.testForm();
	}
}