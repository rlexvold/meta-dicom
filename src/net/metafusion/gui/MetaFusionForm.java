package net.metafusion.gui;

import java.awt.Component;
import javax.swing.JTabbedPane;

public class MetaFusionForm
{
	SetupForm systemForm = new SetupForm();
	AEForm aeForm = new AEForm();
	DiagnosticsForm diagForm = new DiagnosticsForm();

	public MetaFusionForm()
	{
		tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
		tabbedPane.add(systemForm.getPanel(), "Setup");
		tabbedPane.add(aeForm.getPanel(), "AE");
		tabbedPane.add(diagForm.getPanel(), "Diagnostics");
	}
	JTabbedPane tabbedPane = new JTabbedPane();

	public Component getPanel()
	{
		return tabbedPane;
	}

	void refresh()
	{
		systemForm.refresh();
		aeForm.refresh();
		diagForm.refresh();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		MetaFusionForm f = new MetaFusionForm();
		SwingUtil.show(f.getPanel());
	}
}