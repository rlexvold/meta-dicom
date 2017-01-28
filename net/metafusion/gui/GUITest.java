/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Jan 24, 2004
 * Time: 9:32:13 AM
 */
package net.metafusion.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import acme.util.Cancellable;
import acme.util.Util;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GUITest
{
	public static void log(String s)
	{
		System.out.println(s);
	}

	public static void xmain(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
		}
		catch (Exception e)
		{
			// Likely PlasticXP is not in the class path; ignore.
		}
		new GUITest();
	}

	GUITest()
	{
		JFrame frame = new JFrame();
		frame.setTitle("Forms Tutorial :: ButtonBarFactory");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// JComponent panel = new ButtonBarFactoryExample().buildPanel();
		Component panel = newPanel1();
		frame.getContentPane().add(panel);
		frame.pack();
		frame.show();
	}

	Component buildButtonBar(Object target, String def[])
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
				String cmd = s;
				int colonIndex = s.indexOf(':');
				if (colonIndex != -1)
				{
					cmd = s.substring(colonIndex + 1);
					s = s.substring(0, colonIndex);
					builder.addRelatedGap();
				}
				builder.addGridded(new JButton(s));
				if (needGap) builder.addRelatedGap();
				needGap = true;
			}
		}
		return builder.getPanel();
	}

	Component newPanel()
	{
		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addGridded(new JButton("Help"));
		builder.addGlue();
		builder.addUnrelatedGap();
		builder.addFixedNarrow(new JButton("Copy to Clipboard"));
		builder.addUnrelatedGap();
		builder.addGridded(new JButton("OK"));
		builder.addRelatedGap();
		builder.addGridded(new JButton("Cancel"));
		return wrap(builder.getPanel(), "\nDemonstrates a glue (between Help and the rest),\n" + "has related and unrelated buttons and an ungridded button\n"
				+ "with a narrow margin (Copy to Clipboard).");
	}

	// Helper Code ************************************************************
	private Component wrap(Component buttonBar, String text)
	{
		Component textPane = new JScrollPane(new JTextArea(text));
		FormLayout layout = new FormLayout("fill:default:grow", "fill:p:grow, 4dlu, p");
		JPanel panel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		panel.setBorder(Borders.DIALOG_BORDER);
		panel.add(textPane, cc.xy(1, 1));
		panel.add(buttonBar, cc.xy(1, 3));
		return panel;
	}

	JComboBox getCombo()
	{
		JComboBox cb = new JComboBox();
		cb.addItem("Of Modality");
		cb.addItem("To Radiologist");
		cb.addItem("From Source");
		cb.addItem("From Physician");
		return cb;
	}

	JTextField getTextField()
	{
		JTextField tf = new JTextField(8);
		return tf;
	}

	Component newPanel11()
	{
		Component c = buildButtonBar(null, new String[] { "Help", "[GLUE]", "Copy To Clipboard:onCopy", "OK", "Cancel" });
		return wrap(c, "\nDemonstrates a glue (between Help and the rest),\n" + "has related and unrelated buttons and an ungridded button\n"
				+ "with a narrow margin (Copy to Clipboard).");
	}

	// addline(new Stringp[ P {);
	// "Forward studies:forward:checkbox", "[a:b:c]:combo", ":textfield:8", "to
	// AE",:textfield:
	void addLine(Form f, String prefix)
	{
	}

	Component addLine()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new JCheckBox("Forward studies"));
		p.add(getCombo());
		p.add(getTextField());
		p.add(new JLabel("to AE"));
		p.add(getTextField());
		return p;
	}
	class Line
	{
		Line(String[] s)
		{
		}
	}

	Component newPanel1()
	{
		FormLayout layout = new FormLayout("fill:default:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Storage Rules");
		builder.append(addLine());
		builder.append(addLine());
		builder.append(addLine());
		builder.append(addLine());
		builder.append(addLine());
		builder.appendSeparator("Forwarding Rules");
		builder.append(addLine());
		builder.append(addLine());
		builder.append(addLine());
		return builder.getPanel();
	}
	// builder.appendSeparator("Flange");
	//
	// builder.append("Identifier", identifierField);
	// builder.nextLine();
	//
	// builder.append("PTI [kW]", new JTextField());
	// builder.append("Power [kW]", new JTextField());
	//
	// builder.append("s [mm]", new JTextField());
	// builder.nextLine();
	//
	// builder.appendSeparator("Diameters");
	//
	// builder.append("da [mm]", new JTextField());
	// builder.append("di [mm]", new JTextField());
	//
	// builder.append("da2 [mm]", new JTextField());
	// builder.append("di2 [mm]", new JTextField());
	//
	// builder.append("R [mm]", new JTextField());
	// builder.append("D [mm]", new JTextField());
	//
	// builder.appendSeparator("Criteria");
	//
	// builder.append("Location", buildLocationComboBox());
	// builder.append("k-factor", new JTextField());
	//
	// builder.appendSeparator("Bolts");
	//
	// builder.append("Material", ViewerUIFactory.buildMaterialComboBox());
	// builder.nextLine();
	//
	// builder.append("Numbers", new JTextField());
	// builder.nextLine();
	//
	// builder.append("ds [mm]", new JTextField());
	// }
	static class TestRunnable implements Runnable, Cancellable
	{
		volatile boolean stop = false;

		public void run()
		{
			for (;;)
			{
				Util.sleep(250);
				if (stop) break;
			}
		}

		public void cancel()
		{
			stop = true;
		}
	}

	public static void main(String[] args)
	{
		log("Test");
		try
		{
			GUITask t = new GUITask();
			t.runWithProgress(new TestRunnable(), "msgmsgmg", 2000);
		}
		catch (Exception e)
		{
			log("test caught " + e);
		}
		log("exit Test");
	}
}
