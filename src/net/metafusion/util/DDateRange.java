package net.metafusion.util;

public class DDateRange extends DDate
{
	public DDateRange(DDate start, DDate end)
	{
		super();
		this.setTime(start.getTime());
		this.end = end;
	}
	DDate end;

	public DDate getEnd()
	{
		return end;
	}

	boolean inRange(DDate d)
	{
		return (d.equals(this) || d.after(this)) && (d.equals(end) || d.before(end));
	}
}
