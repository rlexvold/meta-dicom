package integration;

import java.io.Serializable;

public class MFNote implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFNote()
	{
	}
	public String[] note = new String[0];

	public void add(String s)
	{
		String[] nn = new String[note.length + 1];
		System.arraycopy(note, 0, nn, 0, note.length);
		nn[note.length] = s;
		note = nn;
	}
}
