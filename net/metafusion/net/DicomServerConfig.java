package net.metafusion.net;

import net.metafusion.util.RoleMap;
import acme.util.XML;

public class DicomServerConfig
{
	public RoleMap getRoleMap()
	{
		return RoleMap.getServerRoleMap(); // todo
	}

	public DicomServerConfig(XML xml)
	{
	}
}
