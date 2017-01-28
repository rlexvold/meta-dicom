package acme.util;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import nanoxml.XMLElement;

public class XMLx
{
	static void log(String s)
	{
		System.out.println(s);
	}
	XMLElement e;

	public XMLx(File file) throws Exception
	{
		e = new XMLElement();
		FileReader reader = null;
		try
		{
			reader = new FileReader(file);
			e.parseFromReader(reader);
		}
		finally
		{
			Util.safeClose(reader);
		}
	}

	public XMLx(XMLElement e)
	{
		this.e = e;
	}

	public XMLx(String name)
	{
		this.e = new XMLElement();
		this.e.setName(name);
	}

	static public XML parseSimpliedXML(String body)
	{
		XMLElement e = new XMLElement();
		Stack stack = new Stack();
		StringTokenizer st = new StringTokenizer(body, "[]", true);// doo[dd[df]ssd[psd]]foo]"
		String last = null;
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (s.equals("["))
			{
				stack.push(e);
				e = new XMLElement();
				e.setName(last);
				last = null;
			} else if (s.equals("]"))
			{
				if (last != null) e.setContent(last);
				XMLElement child = e;
				e = (XMLElement) stack.pop();
				e.addChild(child);
				last = null;
			} else
			{
				assert last == null;
				last = s;
			}
		}
		return new XML((XMLElement) e.enumerateChildren().nextElement());
	}

	public void addAttr(String name, String val)
	{
		e.setAttribute(name, val);
	}

	public void add(String name, String val)
	{
		XMLElement elem = new XMLElement();
		elem.setName(name);
		elem.setContent(val);
		e.addChild(elem);
	}

	public void add(XMLx xml)
	{
		e.addChild(xml.e);
	}

	public XMLx(InputStream is) throws Exception
	{
		e = new XMLElement();
		e.parseFromReader(new InputStreamReader(is));
	}

	public void writeTo(OutputStream os) throws Exception
	{
		Writer writer = new OutputStreamWriter(os);
		e.write(writer);
		writer.flush();
	}

	String leaf(String p)
	{
		int index = p.lastIndexOf('/');
		if (index != -1) p = p.substring(index + 1);
		return p;
	}

	XMLElement getChild(String n)
	{
		Vector v = e.getChildren();
		for (int i = 0; i < v.size(); i++)
		{
			XMLElement c = (XMLElement) v.get(i);
			if (c.getName().equalsIgnoreCase(n)) return c;
		}
		return null;
	}

	XMLElement path(String p, boolean toLeaf)
	{
		XMLElement e = this.e;
		StringTokenizer st = new StringTokenizer(p, "/");
		while (st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if (toLeaf || st.hasMoreElements())
			{
				e = getChild(tok);
				if (e == null) return null;
			}
		}
		return e;
	}

	public XML search(String attr, String value)
	{
		Iterator iter = e.getChildren().iterator();
		while (iter.hasNext())
		{
			XMLElement e = (XMLElement) iter.next();
			if (e.getStringAttribute(attr, "").equals(value)) return new XML(e);
		}
		return null;
	}

	public String getName()
	{
		return e.getName();
	}

	public String get()
	{
		return e.getContent().trim();
	}

	public XML getNode(String name)
	{
		XMLElement e = path(name, true);
		return e != null ? new XML(e) : null;
	}

	public String get(String name)
	{
		XMLElement e = path(name, false);
		name = leaf(name);
		if (e == null) return null;
		if (name.equals(".")) return get();
		if (e.getStringAttribute(name) != null) return e.getStringAttribute(name).trim();
		e = path(name, true);
		return e != null ? e.getContent().trim() : null;
	}

	public String get(String name, String def)
	{
		String s = get(name);
		if (s == null || s.length() == 0)
			return def;
		else return s;
	}

	public int getInt(String name)
	{
		String s = get(name, "0");
		return Integer.parseInt(s);
	}

	public long getLong(String name)
	{
		String s = get(name, "0");
		return Long.parseLong(s);
	}

	public boolean getBoolean(String name)
	{
		String s = get(name, "false");
		return s.equalsIgnoreCase("true");
	}

	public List getList()
	{
		ArrayList al = new ArrayList();
		List l = e.getChildren();
		if (l == null) return null;
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			XMLElement ce = (XMLElement) iter.next();
			XML x = new XML(ce);
			al.add(x);
		}
		return al;
	}

	public List getList(String name)
	{
		XMLElement e = path(name, true);
		ArrayList al = new ArrayList();
		List l = e.getChildren();
		if (l == null) return null;
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			XMLElement ce = (XMLElement) iter.next();
			XML x = new XML(ce);
			al.add(x);
		}
		return al;
	}

	public String toString()
	{
		return toString("");
	}

	public String toString(String h)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(h + "<" + getName());
		Enumeration xenum = e.enumerateAttributeNames();
		while (xenum.hasMoreElements())
		{
			String a = (String) xenum.nextElement();
			sb.append(" " + a + "='" + e.getAttribute(a) + "'");
		}
		sb.append(">\n");
		if (get().length() != 0) sb.append(h + "  " + get() + "\n");
		Iterator iter = getList().iterator();
		while (iter.hasNext())
		{
			XML x = (XML) iter.next();
			sb.append(x.toString(h + "  "));
		}
		sb.append(h + "</" + getName() + ">\n");
		return sb.toString();
	}

	// static XMLOutputter outp = new XMLOutputter();
	// static public String escape(String s) {
	// return outp.escapeElementEntities(s != null ? s : "");
	// }
	static public String trimAll(String s)
	{
		s = s.trim();
		s = s.replace('\n', ' ');
		s = s.replace('\r', ' ');
		s = s.replace('\t', ' ');
		return s;
	}
}
