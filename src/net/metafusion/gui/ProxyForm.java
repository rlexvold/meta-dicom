package net.metafusion.gui;

import java.awt.Component;
import javax.swing.JTabbedPane;
import net.metafusion.admin.ServerBean;

public class ProxyForm
{
	InfoForm systemForm;
	StudiesForm studiesForm;
	RulesForm rulesForm;
	RulesForm2 rulesForm2;
	LogForm logForm;
	ReportForm reportForm;

	public ProxyForm()
	{
		tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
		systemForm = new InfoForm();
		studiesForm = new StudiesForm();
		rulesForm = new RulesForm();
		rulesForm2 = new RulesForm2();
		logForm = new LogForm();
		reportForm = new ReportForm();
		tabbedPane.add(systemForm.getPanel(), "Setup");
		tabbedPane.add(studiesForm.getPanel(), "Studies");
		tabbedPane.add(rulesForm.getPanel(), "Rules");
		tabbedPane.add(rulesForm2.getPanel(), "Forwarding");
		tabbedPane.add(logForm.getPanel(), "Log");
		tabbedPane.add(reportForm.getPanel(), "Report");
	}
	ServerBean serverBean = new ServerBean("");

	public void reset(ServerBean serverBean)
	{
		this.serverBean = serverBean;
		systemForm.reset(serverBean);
		studiesForm.reset(serverBean);
		rulesForm.reset(serverBean);
		rulesForm2.reset(serverBean);
		logForm.reset(serverBean);
		reportForm.reset(serverBean);
	}
	JTabbedPane tabbedPane = new JTabbedPane();

	public Component getPanel()
	{
		return tabbedPane;
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		ProxyForm f = new ProxyForm();
		SwingUtil.show(f.getPanel());
	}
}