package net.metafusion.gui;

import integration.SearchBean;
import java.awt.Dimension;
import java.awt.Event;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import net.metafusion.admin.ServerBean;
import net.metafusion.admin.StudiesInfoBean;
import net.metafusion.admin.StudyBean;

public class StudiesForm extends MFForm
{
	// width='50' align='right' grow='true'
	public StudiesForm()
	{
		register("tableModel", model);
		appendSeparator("Find Studies");
		appendLine(new String[] { "[label text='Patient ID:' width='150' gap='5']", "[label text='Last Name:' width='150' gap='5']",
				"[label text='First Name:' width='150' gap='5']", "[label text='Modality:' width='150' gap='5']", });
		appendLine(new String[] { "[text id='patientId' width='150' gap='5']", "[text id='lastName' width='150' gap='5']", "[text id='firstName' width='150' gap='5']",
				"[combo text='ANY;CR;CT;DX;ES;MG;MR;NM;OT;PT;RF;SC;US;XA' id='modality' width='50']" });
		appendLine(new String[] { "[check id='fromOn' text='From (YYYYMMDD):' width='150' gap='5']", "[check id='toOn' text='To (YYYYMMDD):' width='150' gap='5']",
				"[label text='Study Description:' width='150' gap='5']", "[label text='Station Name:' width='150' gap='5']", });
		appendLine(new String[] { "[text id='fromDate' width='150' gap='5']", "[text id='toDate' width='150' gap='5']", "[text id='description' width='150' gap='5']",
				"[text id='stationName'  width='150']" });
		appendLine(new String[] { "[label text='Referring Physician:' width='150' gap='5']", "[label text='Radiologist:' width='150' gap='5']",
				"[label text='Accession#:' width='150' gap='5']", });
		appendLine(new String[] { "[text id='referringPhysician' width='150' gap='5']", "[text id='radiologist' width='150' gap='5']", "[text id='accession' width='150' gap='5']", });
		appendLine(new String[] { "[button id='clear' text='Clear Filter']", "[glue]", "[button id='search' text='Search']", });
		appendSeparator("Results");
		appendLine("fill:default:grow", new String[] { "[scrolltable id='table' model='tableModel']" });
		;
		buildButtonBar(new String[] { "Select All:selectAll", "Select None:selectNone", "[GLUE]", "Send", "Export", "Burn", "Delete", "Info" });
	}
	private ServerBean serverBean = new ServerBean();

	public void reset(ServerBean serverBean)
	{
		if (this.serverBean != null && !this.serverBean.equals(serverBean)) clearTable();
		this.serverBean = serverBean;
	}

	public void clearTable()
	{
		log("clearTable");
		tableSetModel("table", new StudyTableModel());
		// JTable table = (JTable)lookup("table");
		// StudyTableModel model = new StudyTableModel();
		// table.setModel(model);
		// tableChanged("table");
		// table.invalidate();
	}
	StudyBean studyBean[] = new StudyBean[0];
	StudyTableModel model = new StudyTableModel(studyBean);
	class StudyTableModel extends AbstractTableModel
	{
		StudyBean bean[];

		public StudyTableModel()
		{
			this.bean = new StudyBean[0];
		}

		public StudyTableModel(StudyBean bean[])
		{
			this.bean = bean;
		}
		String cols[] = { "X", "Patient ID", "Name", "Accession", "Modality", "Description", "Date", "Time", "StudyID", "Sex", "Birthdate", "Referring MD", "Radiologist",
				"Station" };

		public Object getValueAt(StudyBean sb, int col)
		{
			switch (col)
			{
				case 0:
					return new Boolean(sb.isSelected());
				case 1:
					return sb.getPatientID();
				case 2:
					return sb.getName();
				case 3:
					return sb.getAccession();
				case 4:
					return sb.getModality();
				case 5:
					return sb.getDescription();
				case 6:
					return sb.getDate();
				case 7:
					return sb.getTime();
				case 8:
					return sb.getStudyID();
				case 9:
					return sb.getSex();
				case 10:
					return sb.getBirthdate();
				case 11:
					return sb.getReferringMD();
				case 12:
					return sb.getRadiologist();
				case 13:
					return sb.getStationName();
			}
			return null;
		}

		public Class getColumnClass(int c)
		{
			return c == 0 ? Boolean.class : String.class;
		}

		public String getColumnName(int col)
		{
			return cols[col];
		}

		public int getRowCount()
		{
			return bean.length;
		}

		public int getColumnCount()
		{
			return cols.length;
		}

		public Object getValueAt(int row, int col)
		{
			return getValueAt(bean[row], col);
		}

		public boolean isCellEditable(int row, int col)
		{
			return col == 0;
		}

		public void setValueAt(Object value, int row, int col)
		{
			if (col == 0) bean[row].setSelected(new Boolean(true).equals(value));
		}
	};

	public void onClear()
	{
		setText("patientId", "");
		setText("firstName", "");
		setText("lastName", "");
		setText("fromDate", "");
		setText("toDate", "");
		setText("studyDescr", "");
		setText("radiologist", "");
		setText("referringPhysician", "");
		setText("accession", "");
		setText("stationName", "");
		setText("description", "");
		setSelected("fromOn", false);
		setSelected("toOn", false);
		setSelection("modality", 0);
	}

	public void onSearch()
	{
		SearchBean search = new SearchBean();
		search.setPatientID(getText("patientId"));
		search.setLastName(getText("lastName"));
		search.setFirstName(getText("firstName"));
		if (isSelected("fromOn")) search.setFromDate(getText("fromDate"));
		if (isSelected("toOn")) search.setToDate(getText("toDate"));
		search.setStudyDescription(getText("description"));
		search.setReferringPhysician(getText("referringPhysician"));
		search.setRadiologist(getText("radiologist"));
		search.setAccessionNum(getText("accession"));
		search.setStationName(getText("stationName"));
		if (getSelection("modality") != 0) search.setModality((String) getSelectedObject("modality"));
		StudyBean sb[] = admin.search(serverBean.getAE(), search);
		studyBean = sb;
		// JTable table = (JTable)lookup("table");
		// StudyTableModel model = new StudyTableModel(studyBean);
		// table.setModel(model);
		// tableChanged("table");
		// table.invalidate();
		tableSetModel("table", new StudyTableModel(studyBean));
		// table.setModel(new StudyForm.StudyTableModel(sb));
		// table.getColumnModel().getColumn(0).setPreferredWidth(10);
		// table.tableChanged(null);
	}

	public void onSelectAll()
	{
		for (StudyBean element : studyBean)
			element.setSelected(true);
		tableChanged("table");
	}

	public void onSelectNone()
	{
		for (StudyBean element : studyBean)
			element.setSelected(false);
		tableChanged("table");
	}

	public List getSelectedStudies()
	{
		List l = new ArrayList();
		for (StudyBean element : studyBean)
			if (element.isSelected()) l.add(element);
		return l;
	}

	public void onSend()
	{
		List selected = getSelectedStudies();
		if (selected.size() == 0) return;
		SelectAEDialog f = new SelectAEDialog();
		f.openModalDialog(null, "Select an AE", new Dimension(300, 260));
		String ae = (String) f.getSelected();
		log(ae);
		if (ae == null || ae.length() == 0) return;
		String studyUIDList[] = admin.send(serverBean.getAE(), selected, ae);
		JOptionPane.showMessageDialog(this.getMainPanel(), studyUIDList.length + " studies requested to be sent to " + ae + ".");
	}

	public void onBurn()
	{
		List selected = getSelectedStudies();
		if (selected.size() == 0) return;
		if ((SwingUtil.getLastActionEvent().getModifiers() & Event.SHIFT_MASK) != 0)
		{
			String studyUIDList[] = admin.send(serverBean.getAE(), selected, "HACK_ANONYMOUS");
			JOptionPane.showMessageDialog(this.getMainPanel(), studyUIDList.length + " studies created anonymous.");
		} else if ((SwingUtil.getLastActionEvent().getModifiers() & Event.CTRL_MASK) != 0)
		{
			String studyUIDList[] = admin.send(serverBean.getAE(), selected, "HACK_DUPLICATE");
			JOptionPane.showMessageDialog(this.getMainPanel(), studyUIDList.length + " studies duplicated.");
		} else
		{
			StudiesInfoBean info = admin.burnStudyList(serverBean.getAE(), selected);
			Object[] msgs = new Object[2 + info.getMsgs().length];
			msgs[0] = "Burn " + info;
			msgs[1] = info.getMsgs().length == 0 ? "No Messages:" : "Messages:";
			System.arraycopy(info.getMsgs(), 0, msgs, 2, info.getMsgs().length);
			JOptionPane.showMessageDialog(this.getMainPanel(), msgs, "Study Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void onExport()
	{
		List selected = getSelectedStudies();
		if (selected.size() == 0) return;
		File rootDir = SwingUtil.chooseDir(this.getMainPanel(), "Select Directory to Store DICOMDIR");
		if (rootDir == null) return;
		boolean good = admin.archive(serverBean.getAE(), selected, rootDir);
		JOptionPane.showMessageDialog(this.getMainPanel(), good ? "" + selected.size() + " studies archived to " + rootDir.getAbsolutePath() + "." : "Could not archive files.");
	}

	public void onDelete()
	{
		List selected = getSelectedStudies();
		if (selected.size() == 0) return;
		int option = JOptionPane.showConfirmDialog(null, "Really delete " + selected.size() + " studies?", "Delete Studies", JOptionPane.YES_NO_OPTION);
		if (option != 0) return;
		String studyUIDList[] = admin.delete(serverBean.getAE(), selected);
		JOptionPane.showMessageDialog(this.getMainPanel(), studyUIDList.length + " studies deleted.");
		clearTable();
		onSearch();
	}

	public void onInfo()
	{
		List selected = getSelectedStudies();
		if (selected.size() == 0) return;
		StudiesInfoBean info = admin.getInfo(serverBean.getAE(), selected);
		JOptionPane.showMessageDialog(this.getMainPanel(), "" + info, "Study Info", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new StudiesForm();
		f.testForm();
	}
}