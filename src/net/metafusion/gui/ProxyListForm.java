package net.metafusion.gui;

import javax.swing.table.AbstractTableModel;
import net.metafusion.admin.AdminSession;
import net.metafusion.admin.ProxyBean;
import net.metafusion.admin.ServerBean;

public class ProxyListForm extends MFForm
{
	public ProxyListForm()
	{
		register("tableModel", model);
		appendSeparator("Proxies");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='id' model='tableModel' ]" });
		;
		// buildButtonBar(new String[] {"[GLUE]", "Find", "Find More", "Save"
		// });
	}
	// StudyBean studyBean[] = new StudyBean[0];
	ProxyTableModel model = new ProxyTableModel();
	AdminSession admin = AdminSession.get();
	ServerBean lsb[] = admin.getLocalStores();
	ProxyBean pb[] = admin.getProxies();
	class ProxyTableModel extends AbstractTableModel
	{
		public ProxyTableModel()
		{
		}
		String cols[] = { "Name" };

		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return pb.length;
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			return pb[row].getName();
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
		model = new ProxyTableModel();
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
		Form f = new ProxyListForm();
		f.testForm();
	}
}