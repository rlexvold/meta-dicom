package integration;

import java.io.Serializable;

public class MFUser implements Serializable, Comparable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public String name;
	public boolean canRead;
	public boolean canView;
	public boolean canAdmin;
	public boolean isActive;
	public long userID;
	public String password;
	public String aeTitle;

	public MFUser()
	{
	}

	public MFUser(long userid, String name)
	{
		this.userID = userid;
		this.name = name;
		canRead = canView = canAdmin = isActive = true;
		aeTitle = "AE_TITLE";
	}

	public int compareTo(Object o)
	{
		if (o == null || !(o instanceof MFUser)) return -1;
		return name.compareTo(((MFUser) o).name);
	}
}
