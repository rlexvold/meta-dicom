package acme.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class StringTemplate
{
	public StringTemplate(String str)
	{
		al = new ArrayList();
		TemplateTokenizer tok = new TemplateTokenizer(str);
		while (tok.hasNext())
		{
			String s = tok.next();
			Elem e = new Elem(Elem.STRING, s);
			if (tok.isVar())
			{
				if (s.equals("BEGIN"))
					e.type = Elem.BEGIN;
				else if (s.equals("END"))
					e.type = Elem.END;
				else e.type = Elem.VAR;
			}
			al.add(e);
		}
	}
	ArrayList al; // of Elem
	static class Elem
	{
		static final int STRING = 0, VAR = 1, BEGIN = 2, END = 3;

		Elem(int type, String value)
		{
			this.type = type;
			this.value = value;
		}
		int type;
		String value;
	}

	public String substitute(Map m)
	{
		int i = 0, j = 0;
		StringBuffer sb = new StringBuffer();
		for (i = 0; i < al.size(); i++)
		{
			Elem e = (Elem) al.get(i);
			if (e.type == Elem.STRING)
				sb.append(e.value);
			else if (e.type == Elem.BEGIN)
			{
				Iterator iter = (Iterator) m.get("ITERATOR");
				while (iter != null && iter.hasNext())
				{
					Object o = iter.next();
					if (o instanceof Tuple)
					{
						Tuple t = (Tuple) o;
						for (j = 0; j < t.length(); j++)
							m.put("" + (char) ('A' + j), t.get(j).toString());
					} else if (o instanceof Map.Entry)
					{
						Map.Entry me = (Map.Entry) o;
						m.put("A", me.getKey().toString());
						m.put("B", me.getValue().toString());
					} else m.put("A", o.toString());
					for (j = i; j < al.size(); j++)
					{
						e = (Elem) al.get(j);
						if (e.type == Elem.STRING)
							sb.append(e.value);
						else if (e.type == Elem.VAR && m.containsKey(e.value))
						{
							Object v = m.get(e.value);
							sb.append(v);
						} else if (e.type == Elem.END)
						{
							break;
						}
					}
				}
				i = j;
			} else if (e.type == Elem.VAR && m.containsKey(e.value))
			{
				Object v = m.get(e.value);
				sb.append(v);
			}
		}
		return sb.toString();
	}
	static class TemplateTokenizer
	{
		int p = 0;
		int len;
		char s[];

		public TemplateTokenizer(String string)
		{
			s = string.toCharArray();
			p = 0;
			len = s.length;
		}
		boolean isVar;
		String tok;
		boolean inPlace = false;

		void advance()
		{
			if (inPlace) return;
			if (p >= len)
			{
				tok = null;
				return;
			}
			inPlace = true;
			isVar = false;
			tok = null;
			int start = p;
			if (s[p] != '$')
			{
				while (p < len)
				{
					if (s[p] == '$') break;
					p++;
				}
				tok = new String(s, start, p - start);
				return;
			} else
			{
				p++;
				while (p < len)
				{
					char ch = s[p++];
					if (ch == '$')
					{
						if (p - start - 2 > 0)
						{
							tok = new String(s, start + 1, p - start - 2);
							isVar = true;
						} else tok = "$";
						break;
					}
					if (!Character.isJavaIdentifierPart(ch)) break;
				}
				if (tok == null) tok = new String(s, start, p - start);
				return;
			}
		}

		public boolean hasNext()
		{
			advance();
			return tok != null;
		}

		public String next()
		{
			advance();
			inPlace = false;
			return tok;
		}

		public boolean isVar()
		{
			return isVar;
		}
	}
}
