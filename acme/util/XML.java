package acme.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import nanoxml.XMLElement;

public class XML
{
	static void log(String s)
	{
		System.out.println(s);
	}
	XMLElement e;

	public XML(File file) throws Exception
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
			if (reader != null) reader.close();
			// Util.safeClose(reader);
		}
	}

	public XML(XMLElement e)
	{
		this.e = e;
	}

	public XML(String name)
	{
		this.e = new XMLElement();
		this.e.setName(name);
	}

	public void substitute(XML parent, XML xml)
	{
		parent.e.removeChild(e);
		parent.e.addChild(xml.e);
		this.e = xml.e;
	}

	/*
	 * public XML(String str) throws Exception { e = new XMLElement();
	 * StringReader reader = null; try { reader = new StringReader(file);
	 * e.parseFromReader(reader); } finally { if (reader != null)
	 * reader.close(); //Util.safeClose(reader); } }
	 */
	static public XML parseXML(String body)
	{
		XMLElement e = new XMLElement();
		StringReader reader = null;
		try
		{
			reader = new StringReader(body);
			e.parseFromReader(reader);
		}
		catch (Exception xe)
		{
			return null;
		}
		finally
		{
			if (reader != null) reader.close();
		}
		return new XML(e);
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
				Util.Assert(last == null);
				last = s;
			}
		}
		return new XML((XMLElement) e.enumerateChildren().nextElement());
	}

	public void addAttr(String name, String val)
	{
		e.setAttribute(name, val != null ? val : "");
	}

	public void add(String name, String val)
	{
		XMLElement elem = new XMLElement();
		elem.setName(name);
		elem.setContent(val);
		e.addChild(elem);
	}

	public void add(XML xml)
	{
		if (xml != null) e.addChild(xml.e);
	}
	public class MyInputStreamReader extends Reader
	{
		InputStream is;

		public MyInputStreamReader(InputStream in)
		{
			super(in);
			is = in;
		}

		public String getEncoding()
		{
			return "UTF-8";
		}

		public int read() throws IOException
		{
			return is.read();
		}

		public int read(char cbuf[], int offset, int length) throws IOException
		{
			Util.Assert(false);
			return -1;
		}

		public boolean ready() throws IOException
		{
			return true;
		}

		public void close() throws IOException
		{
			is.close();
		}
	}

	public XML(InputStream is) throws Exception
	{
		e = new XMLElement();
		e.parseFromReader(new MyInputStreamReader(is));
	}

	public XML(Reader r) throws Exception
	{
		e = new XMLElement();
		e.parseFromReader(r);
	}

	public void writeTo(File f) throws Exception
	{
		OutputStream os = new FileOutputStream(f);
		try
		{
			writeTo(os);
		}
		finally
		{
			if (os != null) os.close();
		}
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
			XMLElement c = (XMLElement) v.elementAt(i);
			if (c.getName().equalsIgnoreCase(n)) return c;
		}
		return null;
	}

	public XML getChild(int index)
	{
		Vector v = e.getChildren();
		XMLElement c = (XMLElement) v.elementAt(index);
		if (c != null) return new XML(c);
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
				e = new XML(e).getChild(tok);
				if (e == null) return null;
			}
		}
		return e;
	}

	public XML search(String attr, String value)
	{
		Vector v = e.getChildren();
		for (int i = 0; i < v.size(); i++)
		{
			XMLElement c = (XMLElement) v.elementAt(i);
			if (c.getStringAttribute(attr, "").equalsIgnoreCase(value)) return new XML(c);
		}
		return null;
	}

	public void setName(String name)
	{
		e.setName(name);
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
		String leaf = leaf(name);
		if (e == null) return null;
		if (leaf.equals(".")) return get();
		if (e.getStringAttribute(leaf) != null) return e.getStringAttribute(leaf).trim();
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

	public int getInt(String name, int def)
	{
		String s = get(name, "" + def);
		return Integer.parseInt(s);
	}

	public double getDouble(String name)
	{
		String s = get(name, "0");
		return Double.parseDouble(s);
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

	public boolean getBoolean(String name, boolean def)
	{
		String s = get(name, "" + def);
		return s.equalsIgnoreCase("true");
	}

	public List getList()
	{
		ArrayList al = new ArrayList();
		Vector v = e.getChildren();
		for (int i = 0; i < v.size(); i++)
		{
			XMLElement ce = (XMLElement) v.elementAt(i);
			XML x = new XML(ce);
			al.add(x);
		}
		return al;
	}

	public List getList(String name)
	{
		ArrayList al = new ArrayList();
		Vector v = e.getChildren();
		for (int i = 0; i < v.size(); i++)
		{
			XMLElement ce = (XMLElement) v.elementAt(i);
			if (ce.getName().equalsIgnoreCase(name))
			{
				XML x = new XML(ce);
				al.add(x);
			}
		}
		return al;
	}

	public List getValues(String name)
	{
		ArrayList values = new ArrayList();
		List xmlList = getList(name);
		if (xmlList == null) return null;
		Iterator xmlListIter = xmlList.iterator();
		while (xmlListIter.hasNext())
		{
			XML v = (XML) xmlListIter.next();
			values.add(v.get());
		}
		return values;
	}

	public void set(String s)
	{
		e.setContent(s);
	}

	public void addNode(XML xml)
	{
		if (xml != null) e.addChild(xml.e);
	}

	public void removeAllNodes()
	{
		Enumeration xenum = e.enumerateChildren();
		while (xenum.hasMoreElements())
		{
			XMLElement elem = (XMLElement) xenum.nextElement();
			e.removeChild(elem);
		}
		xenum = e.enumerateChildren();
		while (xenum.hasMoreElements())
		{
			XMLElement elem = (XMLElement) xenum.nextElement();
			e.removeChild(elem);
		}
	}

	public void removeNode(XML xml)
	{
		e.removeChild(xml.e);
	}

	public boolean removeChildNode(String attrName, String attrValue)
	{
		XML x = search(attrName, attrValue);
		if (x != null) removeNode(x);
		return x != null;
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
