package net.metafusion.gui;

public class ToDoDialog extends MFForm
{
	public ToDoDialog()
	{
		appendLine(new String[] { "[label text='To Do: '  ]", });
		appendLine(new String[] { "[label text=' '  ]", });
		buildButtonBar(new String[] { "[GLUE]", "OK", "Cancel" });
		openModalDialog(null, "ToDo");
	}

	public void onOK()
	{
		closeModalDialog();
	}

	public void onCancel()
	{
		closeModalDialog();
	}

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		ToDoDialog f = new ToDoDialog();
		// f.openModalDialog(null, ae != null ? "Update AE" : "Add AE");
		// f.testForm();
	}
}