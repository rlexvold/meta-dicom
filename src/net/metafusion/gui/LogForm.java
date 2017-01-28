package net.metafusion.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import net.metafusion.admin.LogBean;
import net.metafusion.admin.ServerBean;
import net.metafusion.util.DicomUtil;
import acme.util.Util;

public class LogForm extends MFForm
{
	// width='50' align='right' grow='true'
	public LogForm()
	{
		register("tableModel", model);
		appendSeparator("Access Log");
		appendLine(new String[] { "[label text='Click the button below to save the access log to the local file system.']" });
		// appendLine(new String[] {
		// "[text id='start' len='10' label='Start Date:' gap='5']",
		// "[label text=' (YYYYMMDD)']"
		// });
		// appendLine(new String[] {
		// "[text id='count' len='5' label='Find at most ' gap='5']",
		// "[label text='entries at a time.']",
		// "[glue]",
		// // "[check id='recentFirst' text='Most Recent First']",
		// });
		//
		// appendSeparator("Results");
		// appendLine("fill:default:grow", new String[] { "[scrolltable
		// id='table' model='tableModel']" });;
		// buildButtonBar(new String[] {"[GLUE]", "Find", "Find More:FindMore",
		// "Save" });
		buildButtonBar(new String[] { "[GLUE]", "Save Log to Local Filesystem..." });
	}
	private ServerBean serverBean = new ServerBean();

	public void reset(ServerBean serverBean)
	{
		this.serverBean = serverBean;
	}
	// StudyBean studyBean[] = new StudyBean[0];
	LogTableModel model = new LogTableModel();
	List log = new ArrayList();
	boolean mostRecentFirst = false;
	class LogTableModel extends AbstractTableModel
	{
		public LogTableModel()
		{
		}
		String cols[] = { "Date", "Time", "AE", "Event", "Object", "IP" };

		public Object getValueAt(LogBean lb, int col)
		{
			switch (col)
			{
				case 0:
					return lb.getDate();
				case 1:
					return lb.getTime();
				case 2:
					return lb.getAE();
				case 3:
					return lb.getEvent();
				case 4:
					return lb.getObject();
				case 5:
					return lb.getIp();
			}
			return null;
		}

		// public Class getColumnClass(int c) {
		// return c==0 ? Boolean.class : String.class;
		// }
		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return log.size();
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			if (mostRecentFirst) row = log.size() - row - 1;
			return getValueAt((LogBean) log.get(row), col);
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		public void setValueAt(Object value, int row, int col)
		{
		}
	};

	public void onSave()
	{
		File rootDir = SwingUtil.chooseDir(this.getMainPanel(), "Select Directory to Store Log");
		if (rootDir == null) return;
		FileOutputStream fos = null;
		PrintStream ps = null;
		try
		{
			java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
			File f = new File(rootDir, "log" + DicomUtil.formatDate(d) + DicomUtil.formatTime(d) + ".csv");
			admin.saveLog(serverBean, f);
			JOptionPane.showMessageDialog(this.getMainPanel(), "Log saved to " + f.getName() + ".");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Util.safeClose(fos);
		}
	}

	// void doFind(boolean more) {
	// // mostRecentFirst = isSelected("recentFirst");
	// Date d = null;
	// String s = getText("start");
	// int max = 0;
	// if (s.length() != 0)
	// d = DicomUtil.parseDate(s);// LogBean.dateFormat.parse(s);
	// s = getText("count");
	// try {
	// if (s.length() != 0)
	// max = Integer.parseInt(s);
	// } catch (NumberFormatException e) {
	// }
	// if (more)
	// admin.getLogNext(serverBean, log, max);
	// else log = admin.getLog(serverBean, d, max);
	//
	// Collections.sort(log);
	//
	// model = new LogTableModel();
	// this.tableSetModel("table", model);
	// }
	// public void onFind() {
	// doFind(false);
	// }
	// public void onFindMore() {
	// doFind(true);
	// }
	//
	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new LogForm();
		f.testForm();
	}
}