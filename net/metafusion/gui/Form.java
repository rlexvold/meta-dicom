/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Jan 24, 2004
 * Time: 4:47:43 PM
 */
package net.metafusion.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Stack;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import acme.util.swing.TableSorter;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class Form
{
	public static void log(String s)
	{
		System.out.println(s);
	}
	private static HashMap globalMap = new HashMap();
	private HashMap hashMap = new HashMap();
	protected DefaultFormBuilder builder;
	protected Component panel;
	private String colDef;
	private String rowDef;

	public Form()
	{
		this("fill:default:grow", "");
	}

	public Form(String colDef, String rowDef)
	{
		this.colDef = colDef;
		this.rowDef = rowDef;
		FormLayout layout = new FormLayout(colDef, // "fill:default:grow",
				rowDef); // "");
		builder = new DefaultFormBuilder(layout);
	}

	public void append(Component c)
	{
		builder.append(c);
	}

	public void append(String rowDef, Component c)
	{
		builder.appendRow(rowDef);
		builder.append(c);
	}

	public void appendSpace(String rowDef)
	{
		builder.appendRow(rowDef);
		builder.nextRow();
	}

	public void appendSeparator(String rowDef, String sepString)
	{
		builder.appendRow(rowDef);
		builder.appendSeparator(sepString);
	}

	public void appendSeparator(String sepString)
	{
		builder.appendSeparator(sepString);
	}

	public void appendLine(String[] def)
	{
		appendLine(null, def, null);
	}

	public void appendLine(String rowDef, String[] def)
	{
		appendLine(rowDef, def, null);
	}

	public void appendLine(String[] def, String prefix)
	{
		appendLine(null, def, prefix);
	}

	Component align(Component c, String align)
	{
		if (!(c instanceof JLabel)) return c;
		JLabel l = (JLabel) c;
		if (align.equalsIgnoreCase("right"))
			l.setHorizontalAlignment(SwingConstants.RIGHT);
		else if (align.equalsIgnoreCase("left"))
			l.setHorizontalAlignment(SwingConstants.LEFT);
		else if (align.equalsIgnoreCase("center")) l.setHorizontalAlignment(SwingConstants.CENTER);
		return l;
	}

	Component decorate(Component c, ParseDef def)
	{
		if (def.exists("align")) align(c, def.get("align"));
		if (!(c instanceof JLabel) && def.exists("enabled")) c.setEnabled(def.isTrue("enabled"));
		// if (def.exists("grow") || def.exists("width")) {
		c = new SwingUtil.Decorate(c, def.getInt("width", -1), def.isTrue("grow"));
		// }
		return c;
	}

	// width='50' align='right' grow='true'
	// "[check text='Forward studies' id='forward']",
	// "[combo text='a;b;c' id='which']",
	// "[text text='sel' len='8' align='right']",
	// "[label text='to AE']",
	// "[button text='foo']"
	// "[scrolltable id='id' model='modelName']"
	// "[glue]"
	// "[gap]"
	public void appendLine(String rowDef, String[] defList, String prefix)
	{
		if (rowDef != null) builder.appendRow(rowDef);
		Component c = null;
		Box b = Box.createHorizontalBox();
		// JPanel b = new JPanel();
		// b.setLayout(new FlowLayout(FlowLayout.LEFT));
		boolean single = defList.length == 1;
		for (String element : defList)
		{
			Component label = null;
			ParseDef def = new ParseDef(element);
			String type = def.getType().toLowerCase();
			log("type:" + def);
			String text = def.get("text");
			String id = def.get("id", def.get("text"));
			if (prefix != null && prefix.length() != 0) id = prefix + "." + id;
			if (type.equals("button"))
				c = newButton(id, text);
			else if (type.equals("check"))
				c = newCheckBox(id, text);
			else if (type.equals("combo"))
				c = newCombo(id, text);
			else if (type.equals("text"))
			{
				if (def.exists("label"))
				{
					label = newLabel(null, def.get("label"));
					single = false;
				}
				if (def.exists("len"))
					c = newTextField(id, text, def.get("len"));
				else c = newTextField(id, text);
			} else if (type.equals("label"))
				c = newLabel(id, text);
			else if (type.equals("scrolltable"))
				c = newScrollTable(id, def.get("model"));
			else if (type.equals("gap"))
			{
				b.add(Box.createHorizontalStrut(def.getInt("width", 5)));
				continue;
			} else if (type.equals("glue"))
			{
				b.add(Box.createGlue());
				continue;
			} else log("form bad type: " + type);
			if (label != null)
			{
				b.add(decorate(label, def));
				def.remove("width");
			}
			c = decorate(c, def);
			b.add(c);
			if (def.exists("gap")) b.add(Box.createHorizontalStrut(def.getInt("gap", 0)));
		}
		if (single)
			builder.append(c);
		else builder.append(b);
	}

	public JPanel getMainPanel()
	{
		return builder.getPanel();
	}

	public void register(String id, Object o)
	{
		if (id == null || id.length() == 0) return;
		if (hashMap.containsKey(id)) log(id + " already registered. replacing...");
		hashMap.put(id, o);
	}

	public Object lookup(String id)
	{
		if (!hashMap.containsKey(id)) log(id + " does not exist");
		return hashMap.get(id);
	}

	public Object get(String id)
	{
		if (!hashMap.containsKey(id)) log(id + " does not exist");
		return hashMap.get(id);
	}

	public String getText(String id)
	{
		Object o = lookup(id);
		if (o instanceof JLabel)
			return ((JLabel) o).getText();
		else if (o instanceof JTextField)
			return ((JTextField) o).getText();
		else if (o instanceof JComboBox) return (String) ((JComboBox) o).getSelectedItem();
		// assert false;
		return "";
	}

	public void setText(String id, String value)
	{
		Object o = lookup(id);
		if (o instanceof JLabel)
			((JLabel) o).setText(value);
		else if (o instanceof JTextField)
			((JTextField) o).setText(value);
		else if (o instanceof JComboBox) ((JComboBox) o).setSelectedItem(value);
		// assert false;
	}

	public boolean isSelected(String id)
	{
		Object o = lookup(id);
		return ((JCheckBox) o).isSelected();
	}

	public boolean setSelected(String id, boolean b)
	{
		Object o = lookup(id);
		boolean was = ((JCheckBox) o).isSelected();
		((JCheckBox) o).setSelected(b);
		return was;
	}

	public void setSelection(String id, int index)
	{
		Object o = lookup(id);
		if (o instanceof JComboBox) ((JComboBox) o).setSelectedIndex(index);
	}

	public void setSelection(String id, String string)
	{
		Object o = lookup(id);
		if (o instanceof JComboBox) ((JComboBox) o).setSelectedItem(string);
	}

	public int getSelection(String id)
	{
		Object o = lookup(id);
		if (o instanceof JComboBox)
			return ((JComboBox) o).getSelectedIndex();
		else if (o instanceof JTable) return ((JTable) o).getSelectedRow();
		return -1;
	}

	public Object getSelectedObject(String id)
	{
		Object o = lookup(id);
		if (o instanceof JComboBox)
			return ((JComboBox) o).getSelectedItem();
		else if (o instanceof JTable)
		{
			JTable t = (JTable) o;
			return t.getModel().getValueAt(t.getSelectedRow(), 0);
		}
		return null;
	}

	public void tableChanged(String id)
	{
		Object o = lookup(id);
		((JTable) o).tableChanged(null);
	}

	public void tableSetModel(String id, AbstractTableModel tm)
	{
		JTable table = (JTable) lookup(id);
		// ((JTable)o).setModel(tm);
		log("tableSetModel");
		TableSorter ts = (TableSorter) table.getModel();
		ts.setTableModel(tm);
		table.tableChanged(null);
		table.invalidate();
	}

	private JButton newButton(String id, String name)
	{
		JButton b = new JButton(name);
		if (id.indexOf(' ') != -1) id = id.substring(0, id.indexOf(' '));
		register(id, b);
		try
		{
			id = Character.toUpperCase(id.charAt(0)) + id.substring(1);
			SwingUtil.addListener(b, this, "on" + id);
		}
		catch (Exception e)
		{
			log("could not register button " + id);
		}
		return b;
	}

	private JCheckBox newCheckBox(String id, String name)
	{
		JCheckBox c = new JCheckBox(name);
		register(id, c);
		return c;
	}

	private JComboBox newCombo(String id, String values)
	{
		JComboBox cb = new JComboBox();
		register(id, cb);
		String value[] = values.split(";");
		for (String element : value)
			cb.addItem(element);
		return cb;
	}

	private JTextField newTextField(String id, String def)
	{
		JTextField tf = new JTextField();
		register(id, tf);
		if (def != null && def.length() != 0) tf.setText(def);
		return tf;
	}

	private JTextField newTextField(String id, String def, String length)
	{
		JTextField tf = new JTextField(Integer.parseInt(length));
		register(id, tf);
		if (def != null) tf.setText(def);
		return tf;
	}

	private JLabel newLabel(String id, String text)
	{
		JLabel l = new JLabel(text);
		register(id, l);
		return l;
	}

	private Component newScrollTable(String id, String modelName)
	{
		TableModel tm = (TableModel) lookup(modelName);
		TableSorter sorter = new TableSorter(tm);
		JTable table = new JTable(sorter);
		sorter.setTableHeader(table.getTableHeader());
		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(0, 0));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		register(id, table);
		return scrollPane;
	}
	private Container buttonBar = null;

	// Help", "[GLUE]", "Copy To Clipboard:Copy"
	public void buildButtonBar(String def[])
	{
		ButtonBarBuilder builder = new ButtonBarBuilder();
		boolean needGap = false;
		for (String s : def)
		{
			if (s.startsWith("["))
			{
				if (s.equals("[GLUE]"))
					builder.addGlue();
				else log("ButtonBar: unknown tag " + s);
				needGap = false;
			} else
			{
				if (needGap) builder.addRelatedGap();
				String id = s;
				int colonIndex = s.indexOf(':');
				if (colonIndex != -1)
				{
					id = s.substring(colonIndex + 1);
					s = s.substring(0, colonIndex);
				}
				JButton button = newButton(id, s);
				builder.addGridded(button);
				needGap = true;
			}
		}
		buttonBar = builder.getPanel();
	}

	private Container wrap(Container buttonBar, Component comp)
	{
		FormLayout layout = new FormLayout("fill:default:grow", "fill:p:grow, 4dlu, p");
		JPanel panel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		panel.setBorder(Borders.DIALOG_BORDER);
		panel.add(comp, cc.xy(1, 1));
		panel.add(buttonBar, cc.xy(1, 3));
		return panel;
	}

	public Container getPanel()
	{
		JPanel p = getMainPanel();
		if (buttonBar != null)
			return wrap(buttonBar, p);
		else return p;
	}

	public void testForm()
	{
		JFrame frame = new JFrame();
		frame.setTitle("TestForm");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Component panel = getPanel();
		frame.getContentPane().add(panel);
		frame.pack();
		frame.show();
	}
	static JDialog dialog = null;
	static public Stack dialogStack = new Stack();

	public void openModalDialog(Container parent, String title, Dimension d)
	{
		if (dialog != null) dialogStack.push(dialog);
		SwingUtil.setDefaultIcon(Toolkit.getDefaultToolkit().createImage(MainFrame.getFrameIcon().getAbsolutePath()));
		dialog = new JDialog(JOptionPane.getFrameForComponent(parent), title, true);
		dialog.setSize(d);
		dialog.setContentPane(getPanel());
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		Component c = dialog;
		while (c != null)
		{
			c = c.getParent();
			if (c instanceof Frame)
			{
				((Frame) c).setIconImage(SwingUtil.getDefaultIconImage());
				break;
			}
		}
		dialog.setSize(d);
		dialog.setVisible(true);
	}

	public void openModalDialog(Container parent, String title)
	{
		if (dialog != null) dialogStack.push(dialog);
		SwingUtil.setDefaultIcon(Toolkit.getDefaultToolkit().createImage(MainFrame.getFrameIcon().getAbsolutePath()));
		dialog = new JDialog(JOptionPane.getFrameForComponent(parent), title, true);
		dialog.setContentPane(getPanel());
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		Component c = dialog;
		while (c != null)
		{
			c = c.getParent();
			if (c instanceof Frame)
			{
				((Frame) c).setIconImage(SwingUtil.getDefaultIconImage());
				break;
			}
		}
		dialog.setVisible(true);
	}

	public void closeModalDialog()
	{
		dialog.setVisible(false);
		if (!dialogStack.empty()) dialog = (JDialog) dialogStack.pop();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new Form();
		f.appendSeparator("Storage Rules");
		f.appendLine("line", new String[] { "[check text='Forward studies' id='forward']", "[combo text='a;b;c' id='which']", "[text text='sel' len='8']", "[label text='to AE']",
				"[text len='8']", "[button text='foo']" });
		f.appendLine("line", new String[] { "[check text='Forward studies' id='forward']", "[combo text='a;b;c' id='which']", "[text text='sel' len='8']", "[label text='to AE']",
				"[text len='8']", "[button text='foo']" });
		f.appendSeparator("Forwarding Rules");
		f.buildButtonBar(new String[] { "Help", "[GLUE]", "Copy To Clipboard:Copy", "OK", "Cancel" });
		JFrame frame = new JFrame();
		frame.setTitle("Forms Tutorial :: ButtonBarFactory");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// JComponent panel = new ButtonBarFactoryExample().buildPanel();
		Component panel = f.getPanel();
		frame.getContentPane().add(panel);
		frame.pack();
		frame.show();
	}
}