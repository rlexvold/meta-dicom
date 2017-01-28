package integration;

import java.io.Serializable;

public class MFMessage implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFMessage(String title, String text)
	{
		this.title = title;
		this.text = text;
	}
	String title = "";
	String text = "";
}
