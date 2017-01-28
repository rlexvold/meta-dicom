package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import acme.db.View;

// NOT USED
class EntityView extends View
{
	static EntityView view = null;

	static public synchronized EntityView get()
	{
		if (view == null) view = new EntityView();
		return view;
	}

	EntityView()
	{
		super("dcm_entity", new String[] { "dicomName" }, new String[] { "name", "type", "host", "port" });
	}

	//
	@Override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Entity a = new Entity();
		a.setDicomName(rs.getString(offset++));
		a.setName(rs.getString(offset++));
		a.setType(rs.getString(offset++));
		a.setHost(rs.getString(offset++));
		a.setPort((short) rs.getInt(offset++));
		return a;
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Entity a = (Entity) o;
		if (pk)
			ps.setString(i++, a.getDicomName());
		else
		{
			ps.setString(i++, a.getName());
			ps.setString(i++, a.getType());
			ps.setString(i++, a.getHost());
			ps.setInt(i++, a.getPort());
		}
	}
}
