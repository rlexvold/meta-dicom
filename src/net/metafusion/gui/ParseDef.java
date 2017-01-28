package net.metafusion.gui;

import java.util.HashMap;

class ParseDef
{
	// [type id="" id2="" id3=""]
	private char ch[];
	private int p = 0;

	private void skipSpace()
	{
		while (p < ch.length)
			if (ch[p] == ' ')
				p++;
			else break;
	}

	void skip(int count)
	{
		p = p + count;
		if (p > ch.length) p = ch.length;
	}

	private String getWord(char stop)
	{
		int start = p;
		for (;;)
		{
			char next = peek();
			if (next == stop || next == (char) 0)
			{
				String s;
				if (next == stop)
					s = new String(ch, start, (++p) - start - 1);
				else s = new String(ch, start, p - start);
				return s;
			} else p++;
		}
	}

	private String getWord(String stop)
	{
		int start = p;
		for (;;)
		{
			char next = peek();
			if (stop.indexOf(next) != -1 || next == (char) 0)
			{
				String s;
				if (stop.indexOf(next) != -1)
					s = new String(ch, start, (++p) - start - 1);
				else s = new String(ch, start, p - start);
				return s;
			} else p++;
		}
	}

	private char peek()
	{
		char c = p < ch.length ? ch[p] : (char) 0;
		return c;
	}

	private boolean atEnd()
	{
		return p >= ch.length;
	}

	public ParseDef(String s)
	{
		ch = s.toCharArray();
		skip(1);
		skipSpace();
		type = getWord(" ]");
		skipSpace();
		if (atEnd()) return;
		while (peek() != ']')
		{
			if (atEnd()) throw new RuntimeException("unexpected end: " + s);
			String name = getWord('=');
			getWord('\'');
			String value = getWord('\'');
			hm.put(name.toLowerCase(), value);
			skipSpace();
		}
	}
	private String type;
	private HashMap hm = new HashMap();

	public String getType()
	{
		return type;
	}

	public String get(String name)
	{
		return get(name, "");
	}

	public boolean exists(String name)
	{
		return hm.containsKey(name.toLowerCase());
	}

	public void remove(String name)
	{
		hm.remove(name.toLowerCase());
	}

	public boolean isTrue(String name)
	{
		return get(name, "false").equalsIgnoreCase("true");
	}

	public String get(String name, String def)
	{
		String s = (String) hm.get(name.toLowerCase());
		return s == null ? def : s;
	}

	public int getInt(String name, int def)
	{
		String s = (String) hm.get(name.toLowerCase());
		return s == null ? def : Integer.parseInt(s);
	}

	public String toString()
	{
		return hm.toString();
	}

	public boolean getBoolean(String name)
	{
		return get(name, "false").equalsIgnoreCase("true");
	}
}
