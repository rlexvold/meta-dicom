package acme.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class GUI extends JDialog implements ActionListener
{
	static void log(String s)
	{
		System.out.println(s);
	}
	static class Base
	{
		boolean dynamicList = false;
		XML xmlItem;
		XML xmlDefault;
		String type;
		String name;
		String def;
		List defList;
		String value;
		String label;
		Component component;

		public boolean isDynamicList()
		{
			return dynamicList;
		}

		public String getType()
		{
			return type;
		}

		public String getName()
		{
			return name;
		}

		public String getDef()
		{
			return def;
		}

		public List getDefList()
		{
			return defList;
		}

		public String getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		void done()
		{
		}

		Base(XML xmlItem, XML xmlDefault)
		{
			init(xmlItem, xmlDefault);
		}

		void init(XML xmlItem, XML xmlDefault)
		{
			this.xmlItem = xmlItem;
			this.xmlDefault = xmlDefault;
			type = xmlItem.getName();
			name = xmlItem.get("name");
			label = xmlItem.get("label");
			def = xmlDefault != null ? xmlDefault.get("value") : "";
			defList = xmlDefault != null ? xmlDefault.getValues("value") : new LinkedList();
		}

		Component getComponent()
		{
			return component;
		}

		void update()
		{
			if (xmlDefault != null && value != null) xmlDefault.addAttr("value", value);
			// only if dynamic
			if (dynamicList && defList != null && value != null && !defList.get(0).equals(value))
			{
				defList.add(0, value);
				while (defList.size() > 15)
					defList.remove(defList.remove(defList.size() - 1));
				xmlDefault.removeAllNodes();
				for (int i = 0; i < defList.size(); i++)
					xmlDefault.add("value", (String) defList.get(i));
			}
		}

		Component labelBox(String label, Component c)
		{
			Box b = Box.createHorizontalBox();
			b.add(new JLabel(label));
			b.add(c);
			return b;
		}
	}
	static class Label extends Base
	{
		public Label(XML a, XML b) throws Exception
		{
			super(a, b);
			component = new JLabel(label);
		}

		void done()
		{
			value = "";
		}
	}
	static class TextField extends Base
	{
		JTextField tf;

		public TextField(XML a, XML b) throws Exception
		{
			super(a, b);
			tf = new JTextField(def, 25);
			component = label != null ? labelBox(label, tf) : tf;
		}

		void done()
		{
			value = tf.getText();
		}
	}
	static class ComboBox extends Base
	{
		JComboBox cb;

		public ComboBox(XML a, XML b) throws Exception
		{
			super(a, b);
			cb = new JComboBox(defList.toArray());
			component = label != null ? labelBox(label, cb) : cb;
			cb.setEditable(false);
			cb.setSelectedItem(def);
		}

		void done()
		{
			value = (String) cb.getSelectedItem();
		}
	}
	static class EditComboBox extends Base
	{
		JComboBox cb;

		public EditComboBox(XML a, XML b) throws Exception
		{
			super(a, b);
			cb = new JComboBox(defList.toArray());
			component = label != null ? labelBox(label, cb) : cb;
			cb.setEditable(true);
			cb.setSelectedItem(def);
			dynamicList = true;
		}

		void done()
		{
			value = (String) cb.getSelectedItem();
		}
	}
	static class CheckBox extends Base
	{
		JCheckBox cb;

		public CheckBox(XML a, XML b) throws Exception
		{
			super(a, b);
			component = cb = new JCheckBox(label, def.equalsIgnoreCase("true"));
		}

		void done()
		{
			value = cb.isSelected() ? "true" : "false";
		}
	}
	static class RadioButton extends Base
	{
		JRadioButton cb;

		public RadioButton(XML a, XML b) throws Exception
		{
			super(a, b);
			component = cb = new JRadioButton(label, def.equalsIgnoreCase("true"));
			cb.setAlignmentX(0);
		}

		void done()
		{
			value = cb.isSelected() ? "true" : "false";
		}
	}
	HashMap hm = new HashMap();
	{
		hm.put("label", Label.class);
		hm.put("textfield", TextField.class);
		hm.put("combobox", ComboBox.class);
		hm.put("editcombobox", EditComboBox.class);
		hm.put("checkbox", CheckBox.class);
		hm.put("radiobutton", RadioButton.class);
	}
	XML xmlFrame;
	XML xmlItems;
	XML xmlDefaults;
	List compList = new ArrayList();

	void parse(XML items, XML defaults) throws Exception
	{
		Iterator iter = items.getList().iterator();
		while (iter.hasNext())
		{
			XML xml = (XML) iter.next();
			Constructor c = ((Class) hm.get(xml.getName())).getConstructors()[0];
			Base b = (Base) c.newInstance(new XML[] { xml, defaults.search("name", xml.get("name")) });
			compList.add(b);
		}
	}

	void done() throws Exception
	{
		Iterator iter = compList.iterator();
		while (iter.hasNext())
		{
			Base b = (Base) iter.next();
			b.done();
		}
	}

	void update() throws Exception
	{
		Iterator iter = compList.iterator();
		while (iter.hasNext())
		{
			Base b = (Base) iter.next();
			b.update();
		}
		xmlDefaults.addAttr("h", "" + (int) getSize().getHeight());
		xmlDefaults.addAttr("w", "" + (int) getSize().getWidth());
		File f = new File("conf/gui-" + xmlFrame.get("name") + ".def");
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(f);
			xmlDefaults.writeTo(fos);
		}
		finally
		{
			Util.safeClose(fos);
		}
	}

	HashMap getValues() throws Exception
	{
		HashMap hm = new HashMap();
		Iterator iter = compList.iterator();
		while (iter.hasNext())
		{
			Base b = (Base) iter.next();
			if (b.getName() != null && b.getName().length() != 0) hm.put(b.getName(), b.getValue());
		}
		return hm;
	}
	JButton ok;
	JButton cancel;

	Component generate()
	{
		Box v = Box.createVerticalBox();
		v.setAlignmentX(0);
		for (int i = 0; i < compList.size(); i++)
		{
			Box h = Box.createHorizontalBox();
			h.setAlignmentX(0);
			Base c = ((Base) compList.get(i));
			h.add(c.getComponent());
			// h.add(Box.createGlue());
			v.add(h);
		}
		return v;
	}

	Component getButtons()
	{
		Box h = Box.createHorizontalBox();
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		h.add(ok);
		h.add(cancel);
		return h;
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == cancel) System.exit(-1);
		isOK = true;
		try
		{
			done();
			update();
		}
		catch (Exception e1)
		{
			log("dialog caught " + e1);
			e1.printStackTrace();
		}
		setVisible(false);
		frame.dispose();
	}
	boolean isOK = false;
	Frame frame;

	GUI(Frame frame, XML xml) throws Exception
	{
		super(frame, true);
		this.frame = frame;
		setTitle(xml.get("name"));
		// setModal(true);
		xmlFrame = xml;
		xmlItems = xmlFrame.getNode("items");
		xmlDefaults = xmlFrame.getNode("defaults");
		File f = new File("conf/gui-" + xmlFrame.get("name") + ".def");
		if (f.exists()) xmlDefaults = new XML(f);
		if (xmlDefaults == null) xmlDefaults = new XML("defaults");
		parse(xmlItems, xmlDefaults);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(generate(), BorderLayout.NORTH);
		getContentPane().add(getButtons(), BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				try
				{
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = getPreferredSize();
		if (xmlDefaults.getInt("h") != 0 && xmlDefaults.getInt("w") != 0) size = new Dimension(xmlDefaults.getInt("w"), xmlDefaults.getInt("h"));
		setSize(size);
		setLocation(screenSize.width / 2 - (size.width / 2), screenSize.height / 2 - (size.height / 2));
		// setVisible(true);
	}

	public static void main(String[] args)
	{
		try
		{
			String s = "<frame name='GUITest'> <items><textfield label='foo: ' name='tf1' /><textfield name='tf2' />" + "<combobox name='tf' />" + "<editcombobox name='tf' />"
					+ "<label label='goo'/>" + "<checkbox name='cb' label='cb' />" + "<radiobutton name='rb' label='rb1' />" + "</items>"
					+ "<defaults><default name='tf' value='b'><value>a</value><value>b</value></default>" + "<default name='tf1' value='b' />" + "" + "" + "" + "</defaults>"
					+ "</frame>";
			HashMap hm = showGUI(s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static HashMap showGUI(String def)
	{
		try
		{
			XML xml = new XML(new StringReader(def));
			return showGUI(xml);
		}
		catch (Exception e)
		{
			log("GUI caught " + e);
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public static HashMap showGUI(XML def)
	{
		try
		{
			final GUI frame = new GUI(new Frame(), def);
			frame.show();
			if (!frame.isOK) System.exit(0);
			return frame.getValues();
		}
		catch (Exception e)
		{
			log("GUI caught " + e);
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
}
/*
 * public static void main(String[] args) throws HeadlessException { Frame frame =
 * new Frame(); frame.addWindowListener(new WindowAdapter() { public void
 * windowClosing(WindowEvent e) { System.exit(0); } }); frame.setSize(370, 100);
 * frame.setVisible(true);
 *  } public XML(Reader r) throws Exception { e = new XMLElement();
 * e.parseFromReader(r); } package test;
 * 
 * import javax.swing.*; import java.awt.*; import java.awt.event.*;
 * 
 * 
 * public class Frame extends JFrame { Box box1; JLabel jLabel2 = new JLabel();
 * JRadioButton jRadioButton1 = new JRadioButton(); JComboBox jComboBox1 = new
 * JComboBox(); JLabel jLabel1 = new JLabel(); JTextField jTextField1 = new
 * JTextField(); JRadioButton jRadioButton2 = new JRadioButton(); JTextArea
 * jTextArea1 = new JTextArea(); JCheckBox jCheckBox1 = new JCheckBox();
 * BorderLayout borderLayout1 = new BorderLayout(); JTextField jTextField2 = new
 * JTextField(); JButton jButton1 = new JButton(); JButton jButton2 = new
 * JButton(); public Frame() throws HeadlessException { try { jbInit(); }
 * catch(Exception e) { e.printStackTrace(); } } public static void
 * main(String[] args) throws HeadlessException { Frame frame = new Frame();
 * frame.addWindowListener(new WindowAdapter() { public void
 * windowClosing(WindowEvent e) { System.exit(0); } }); frame.setSize(370, 100);
 * frame.setVisible(true);
 *  } private void jbInit() throws Exception { box1 = Box.createVerticalBox();
 * this.getContentPane().setLayout(borderLayout1); jLabel2.setText("jLabel2");
 * jRadioButton1.setToolTipText(""); jRadioButton1.setText("jRadioButton1");
 * jRadioButton1.setVerticalAlignment(SwingConstants.CENTER);
 * jRadioButton1.setVerticalTextPosition(SwingConstants.CENTER);
 * jComboBox1.setEditable(true); jLabel1.setText("jLabel1");
 * jTextField1.setText("jTextField1"); jRadioButton2.setText("jRadioButton2");
 * jTextArea1.setText("jTextArea1"); jCheckBox1.setText("jCheckBox1");
 * jTextField2.setText("jTextField2"); jButton1.setText("jButton1");
 * jButton2.setText("jButton2"); this.getContentPane().add(box1,
 * BorderLayout.WEST); box1.add(jLabel2, null); box1.add(jComboBox1, null);
 * box1.add(jRadioButton1, null); box1.add(jLabel1, null);
 * box1.add(jRadioButton2, null); box1.add(jCheckBox1, null);
 * box1.add(jTextArea1, null); box1.add(jTextField1, null);
 * box1.add(jTextField2, null); box1.add(jButton1, null);
 * this.getContentPane().add(jButton2, BorderLayout.SOUTH); }
 *  }
 * 
 */