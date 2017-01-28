package net.metafusion.gui;

import net.metafusion.admin.AdminSession;

public class MFForm extends Form
{
	protected AdminSession admin = AdminSession.get();

	public MFForm()
	{
		super();
	}

	public MFForm(String colDef, String rowDef)
	{
		super(colDef, rowDef);
	}
}