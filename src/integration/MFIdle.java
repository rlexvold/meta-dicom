package integration;

import java.io.Serializable;

public class MFIdle implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public boolean refreshNewStudy;
	public boolean refreshRequest;
	public MFMessage[] messages = new MFMessage[0];
}