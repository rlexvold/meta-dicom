package integration;

import java.io.Serializable;

public class MFSeries implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFSeries()
	{
	}
	public String seriesNumber = "";
	public String modality = "";
	public String description = "";
	public int imageCount = 0;
}
