package net.metafusion.gui;

import javax.swing.table.AbstractTableModel;
import net.metafusion.admin.AdminSession;
import net.metafusion.admin.ServerBean;

public class LocalStoreListForm extends MFForm
{
	public LocalStoreListForm()
	{
		register("tableModel", model);
		appendSeparator("Local Stores");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='id' model='tableModel' ]" });
		;
		// buildButtonBar(new String[] {"[GLUE]", "Find", "Find More", "Save"
		// });
	}
	// StudyBean studyBean[] = new StudyBean[0];
	LocalStoreTableModel model = new LocalStoreTableModel();
	AdminSession admin = AdminSession.get();
	ServerBean lsb[] = admin.getLocalStores();
	ServerBean pb[] = admin.getProxies();
	class LocalStoreTableModel extends AbstractTableModel
	{
		public LocalStoreTableModel()
		{
		}
		String cols[] = { "Name" };

		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return lsb.length;
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			return lsb[row].getName();
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		public void setValueAt(Object value, int row, int col)
		{
		}
	};

	void refresh()
	{
		lsb = admin.getLocalStores();
		pb = admin.getProxies();
		model = new LocalStoreTableModel();
		register("tableModel", model);
		tableSetModel("id", model);
	}

	public void onChange()
	{
	}

	public void onCommit()
	{
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new LocalStoreListForm();
		f.testForm();
	}
}