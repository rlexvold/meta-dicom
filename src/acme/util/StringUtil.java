package acme.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class StringUtil
{
	final public static String lpad(String s, char c, int len)
	{
		int delta = len - s.length();
		if (delta <= 0) return s;
		StringBuffer sb = new StringBuffer();
		while (delta-- > 0)
			sb.append(c);
		sb.append(s);
		return sb.toString();
	}

	final public static String rpad(String s, char c, int len)
	{
		int delta = len - s.length();
		if (delta <= 0) return s;
		StringBuffer sb = new StringBuffer();
		sb.append(s);
		while (delta-- > 0)
			sb.append(c);
		return sb.toString();
	}

	final public static String hex2(int i)
	{
		return lpad(Integer.toHexString(i).toUpperCase(), '0', 2);
	}

	final public static String hex4(int i)
	{
		return lpad(Integer.toHexString(i).toUpperCase(), '0', 4);
	}

	final public static String hex8(int i)
	{
		return lpad(Integer.toHexString(i).toUpperCase(), '0', 8);
	}

	final public static String int2(int i)
	{
		return lpad(Integer.toString(i), '0', 2);
	}

	final public static String int4(int i)
	{
		return lpad(Integer.toString(i), '0', 4);
	}

	final public static String int8(int i)
	{
		return lpad(Integer.toString(i), '0', 8);
	}

	final public static String repeat(String s, int count)
	{
		StringBuffer sb = new StringBuffer();
		while (count-- > 0)
			sb.append(s);
		return sb.toString();
	}

	final public static String left(String s, int len)
	{
		if (len > s.length()) len = s.length();
		return s.substring(0, len);
	}

	final public static String right(String s, int len)
	{
		if (len > s.length()) len = s.length();
		return s.substring(s.length() - len, len);
	}

	final public static String replaceAll(String s, char targ, char rep)
	{
		int pos = s.indexOf(targ);
		if (pos == -1) return s;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (ch != targ)
				sb.append(ch);
			else sb.append(rep);
		}
		return sb.toString();
	}

	final public static String replaceAll(String s, String targ, String rep)
	{
		if (s.indexOf(targ) == -1) return s;
		StringBuffer t = new StringBuffer();
		while (s.length() != 0)
			if (s.startsWith(targ))
			{
				t.append(rep);
				s = s.substring(targ.length());
			} else
			{
				t.append(s.charAt(0));
				s = s.substring(1);
			}
		return t.toString();
	}

	public static void main(String[] args)
	{
	}

	public static int count(String s, char ch)
	{
		int count = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == ch) count++;
		return count;
	}

	public static boolean canSplit(String s, char ch)
	{
		return s != null ? s.indexOf(ch) != -1 : false;
	}

	public static String[] split(String string, char sep)
	{
		if (string == null || string.trim().length() == 0) return new String[0];
		ArrayList al = new ArrayList();
		StringTokenizer st = new StringTokenizer(string, "" + sep);
		while (st.hasMoreTokens())
			al.add(st.nextToken().trim());
		return (String[]) al.toArray(new String[al.size()]);
	}

	public static String[] oldSplit(String s, char ch)
	{
		if (s == null) return null;
		int tot = 1;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == ch) tot++;
		String vs[] = new String[tot];
		int start = 0;
		int v = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == ch)
			{
				vs[v++] = s.substring(start, i);
				start = i + 1;
			}
		vs[v] = s.substring(start);
		return vs;
	}

	public static String pathHead(String s, char sep)
	{
		int i = s.indexOf(sep);
		return i != -1 ? s.substring(0, i) : s;
	}

	public static String pathTail(String s, char sep)
	{
		int i = s.indexOf(sep);
		return i != -1 ? s.substring(i + 1) : s;
	}

	public static String pathEnd(String s, char sep)
	{
		int i = s.lastIndexOf(sep);
		return i != -1 ? s.substring(i + 1) : s;
	}

	static String substitute(String str, Map m)
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		char s[] = str.toCharArray();
		while (i < s.length)
		{
			while (i < s.length && s[i] != '$')
				sb.append(s[i++]);
			int start = i;
			if (i < s.length)
			{
				boolean valid = true;
				i++;
				while (valid && i < s.length && s[i] != '$')
					valid = valid && Character.isJavaIdentifierPart(s[i++]);
				if (valid && i < s.length)
				{
					i++;
					if (i - start == 2)
						sb.append('$');
					else
					{
						String tag = new String(s, start + 1, i - start - 2);
						String sub = (String) m.get(tag);
						if (sub != null) sb.append(sub);
					}
					continue;
				}
			}
			sb.append(s, start, i - start);
		}
		return sb.toString();
	}

	public static String capitalize(String s)
	{
		if (s == null || s.length() <= 1) return s;
		if (Character.isLowerCase(s.charAt(0))) return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		return s;
	}

	public static String uncapitalize(String s)
	{
		if (s == null || s.length() <= 1) return s;
		if (Character.isUpperCase(s.charAt(0))) return Character.toLowerCase(s.charAt(0)) + s.substring(1);
		return s;
	}

	public static String stripNonAlphaNum(String s)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (Character.isLetterOrDigit(ch)) sb.append(ch);
		}
		return sb.toString();
	}

	public static int findString(String list[], String s, int def)
	{
		int i = findString(list, s);
		return i != -1 ? i : def;
	}

	public static int findString(String list[], String s)
	{
		for (int i = 0; i < (list != null ? list.length : 0); i++)
			if (list[i].equalsIgnoreCase(s)) return i;
		return -1;
	}

	public static final String safe(String s)
	{
		return s != null ? s : "";
	}

	public static final String safeAppend(String a, String b)
	{
		if (a == null) a = "";
		if (b == null) b = "";
		return a + b;
	}

	public static final String safeAppend(String a, String b, String c)
	{
		if (a == null) a = "";
		if (b == null) b = "";
		if (c == null) c = "";
		return a + b + c;
	}

	public static char rot13(char c)
	{
		if ((c >= 'A') && (c <= 'Z'))
		{
			c += 13;
			if (c > 'Z') c -= 26;
		}
		if ((c >= 'a') && (c <= 'z'))
		{
			c += 13;
			if (c > 'z') c -= 26;
		}
		return c;
	}

	public static boolean isEmpty(String str)
	{
		boolean result = true;
		if (null != str && str.length() > 0) result = false;
		return result;
	}
}
