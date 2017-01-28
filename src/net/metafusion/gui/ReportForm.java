package net.metafusion.gui;

import net.metafusion.admin.ServerBean;

public class ReportForm extends MFForm
{
	// width='50' align='right' grow='true'
	public ReportForm()
	{
		appendSeparator("Reports");
		appendLine(new String[] { "[label text='For your customized reports, please contact Meta Fusion.'  gap='5']",
		// "[label text='Select a Report:' gap='5']",
		// "[combo text='Report1;Report2;Report3' id='report' width='100'
		// gap='5']",
		});
		appendSeparator("Results");
		buildButtonBar(new String[] { "[GLUE]", "Run", "Save" });
	}
	private ServerBean serverBean = new ServerBean();

	public void reset(ServerBean serverBean)
	{
		this.serverBean = serverBean;
	}

	public void onRun()
	{
		;// new ToDoDialog();
	}

	public void onSave()
	{
		;// new ToDoDialog();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new ReportForm();
		f.testForm();
	}
}