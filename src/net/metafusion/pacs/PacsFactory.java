package net.metafusion.pacs;

public class PacsFactory
{
	public static IPacs getPacsInterface()
	{
		return new PacsFacade();
	}
}
