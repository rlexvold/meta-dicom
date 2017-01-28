package acme.db;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class CustomSQL
{
	String where = "";

	public String get()
	{
		stripSep();
		if (where.length() == 0 && limit.length() > 0)
			return limit;
		else return where.length() > 0 ? where + " " + orderBy + " " + limit : null;
	}
	String sep = " and ";
	public static final String AND = " AND ";
	public static final String OR = " OR ";
	String orderBy = "";
	String limit = "";

	public void addLimit(int limit)
	{
		this.limit = "limit " + limit;
	}

	public void addOrderBy(String orderBy)
	{
		this.orderBy = " order by " + orderBy;
	}

	public void addGroupBy(String orderBy)
	{
		this.orderBy = " group by " + orderBy;
	}

	void stripSep()
	{
		if (where.endsWith(sep)) where = where.substring(0, where.length() - sep.length());
	}

	public void addLike(String name, String[] value)
	{
		addLike(name, value, OR);
	}

	public void addLike(String name, String[] value, String newSep)
	{
		if (value.length == 0) return;
		if (value.length == 1)
		{
			addLike(name, value[0]);
			return;
		}
		where += " ( ";
		String oldSep = sep;
		sep = newSep;
		for (int i = 0; i < value.length; i++)
		{
			addLike(name, value[i]);
		}
		stripSep();
		sep = oldSep;
		where += " )" + sep;
	}

	// todo: uid list matching?
	// sql: '%' and '_'
	// dicom: '*' and '?'
	public void addLike(String name, String value)
	{
		if (value.indexOf('\\') != -1) throw new RuntimeException("FIXME: SEPARATOR in SEARCH VALUE: %%%%%%%%%%%%%%%%%%%%%%% " + value);
		// todo: fix this^^^^^ for uid list matching
		if (value.indexOf('*') != -1) value = value.replace('*', '%');
		if (value.indexOf('\'') != -1) value.replaceAll("'", "\'");
		if (value.indexOf('?') != -1) value = value.replace('?', '_');
		if (value.endsWith("%")) value = value.substring(0, value.length() - 1);
		if (value.startsWith("%")) value = value.substring(1, value.length());
		where += " " + name + " like '%" + value + "%'" + sep;
	}

	public void addEquals(String name, String[] value)
	{
		addEquals(name, value, OR);
	}

	public void addEquals(String name, String[] value, String newSep)
	{
		if (value.length == 0) return;
		if (value.length == 1)
		{
			addEquals(name, value[0]);
			return;
		}
		where += " ( ";
		String oldSep = sep;
		sep = newSep;
		for (int i = 0; i < value.length; i++)
		{
			addEquals(name, value[i]);
		}
		stripSep();
		sep = oldSep;
		where += " )" + sep;
	}

	public void addEquals(String name, String value)
	{
		if (value.indexOf('*') != -1 || value.indexOf('?') != -1)
			addLike(name, value);
		else where += " " + name + " =  '" + value + "'" + sep;
	}

	public void addEquals(String name, long value)
	{
		where += " " + name + " =  " + value + sep;
	}

	public void addNotEquals(String name, int value)
	{
		where += " " + name + " <>  " + value + sep;
	}

	public void addFieldInString(String field, String str)
	{
		where += " instr('" + str + "'," + field + ") <>  0" + sep;
	}

	public void addEquals(String name, Date value)
	{
		where += " " + name + " =  " + formatDateforSQL(value) + sep;
	}

	public void addLessEquals(String name, Date value)
	{
		where += " " + name + " <=  " + formatDateforSQL(value) + sep;
	}

	public void addGreaterEquals(String name, Date value)
	{
		where += " " + name + " >=  " + formatDateforSQL(value) + sep;
	}

	public void addRange(String name, Date lo, Date hi)
	{
		where += " ( " + name + " >=  " + formatDateforSQL(lo) + "  and " + name + " <= " + formatDateforSQL(hi) + " )" + sep;
	}
	static SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static String formatDateforSQL(Date d)
	{
		return "'" + sqlDateFormat.format(d) + "'";
	}
}
