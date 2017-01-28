package net.metafusion.localstore.sync;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class SyncMsg implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	String cmd = "";
	HashMap args = null;
	long length = 0;
	int id = 0;
	File file = null;

	public String toString()
	{
		return "SyncMsg: cmd=" + cmd + ",args=" + args + "id=" + id + "file=" + (file != null ? file.getName() : "null");
	}
}