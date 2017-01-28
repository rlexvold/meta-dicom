package net.metafusion.message;

import java.util.ArrayList;

public abstract class AbstractMessage
{
	private ArrayList<AbstractMessage> resultSet;

	public ArrayList<AbstractMessage> getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(ArrayList<AbstractMessage> resultSet)
	{
		this.resultSet = resultSet;
	}
}