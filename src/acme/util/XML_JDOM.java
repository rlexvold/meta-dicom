package acme.util;

//import org.jdom.*;
//import org.jdom.output.XMLOutputter;
//import org.jdom.input.SAXBuilder;
public class XML_JDOM
{
	/*
	 * Element e; public XML_JDOM(File file) throws Exception { SAXBuilder
	 * builder = new SAXBuilder(); Document doc = builder.build(file); e =
	 * doc.getRootElement();
	 *  } XML_JDOM(Element e) { this.e = e; }
	 * 
	 * String leaf(String p) { int index = p.lastIndexOf('/'); if (index != -1)
	 * p = p.substring(index+1); return p; } Element path(String p, boolean
	 * toLeaf) { Element e = this.e; StringTokenizer st = new StringTokenizer(p,
	 * "/"); while (st.hasMoreTokens()) { String tok = st.nextToken(); if
	 * (toLeaf || st.hasMoreElements()) { e = e.getChild(tok); if (e == null)
	 * return null; } } return e; }
	 * 
	 * public XML search(String attr, String value) { Iterator iter =
	 * e.getChildren().iterator(); while (iter.hasNext()) { Element e =
	 * (Element)iter.next(); if (e.getAttributeValue(attr, "").equals(value))
	 * return new XML(e); } return null; }
	 * 
	 * 
	 * public String getName() { return e.getName(); } public String get() {
	 * return e.getTextTrim(); } public XML getNode(String name) { Element e =
	 * path(name, true); return e != null ? new XML(e) : null; } public String
	 * get(String name) { Element e = path(name, false); name = leaf(name); if
	 * (e == null) return null; if (name.equals(".")) return get(); if
	 * (e.getAttributeValue(name)!=null) return e.getAttributeValue(name);
	 * return e.getChildTextTrim(name); } public String get(String name, String
	 * def) { String s = get(name); if (s==null || s.length()==0) return def;
	 * else return s; } public List getList() { ArrayList al = new ArrayList();
	 * List l = e.getChildren(); if (l == null) return null; Iterator iter =
	 * l.iterator(); while (iter.hasNext()) { Element ce = (Element)iter.next();
	 * XML x = new XML(ce); al.add(x); } return al; } public List getList(String
	 * name) { Element e = path(name, true); ArrayList al = new ArrayList();
	 * List l = e.getChildren(); if (l == null) return null; Iterator iter =
	 * l.iterator(); while (iter.hasNext()) { Element ce = (Element)iter.next();
	 * XML x = new XML(ce); al.add(x); } return al; } public String toString() {
	 * return toString(""); } public String toString(String h) { StringBuffer sb =
	 * new StringBuffer(); sb.append(h+"<"+getName()); Iterator iter =
	 * e.getAttributes().iterator(); while (iter.hasNext()) { Attribute a =
	 * (Attribute)iter.next(); sb.append(" "+a.getName()+"='"+a.getValue()+"'"); }
	 * sb.append(">\n"); if (get().length()!=0) sb.append(h+" "+get()+"\n");
	 * iter = getList().iterator(); while (iter.hasNext()) { XML x =
	 * (XML)iter.next(); sb.append(x.toString(h+" ")); } sb.append(h+"</"+getName()+">\n");
	 * return sb.toString(); }
	 * 
	 * static XMLOutputter outp = new XMLOutputter(); static public String
	 * escape(String s) { return outp.escapeElementEntities(s != null ? s : ""); }
	 * 
	 * static public String trimAll(String s) { s = s.trim(); s =
	 * s.replace('\n', ' '); s = s.replace('\r', ' '); s = s.replace('\t', ' ');
	 * return s; }
	 * 
	 * 
	 * 
	 *  /* Element e; public XML(File file) throws Exception { SAXBuilder
	 * builder = new SAXBuilder(); Document doc = builder.build(file); e =
	 * doc.getRootElement();
	 *  } XML(Element e) { this.e = e; }
	 * 
	 * String leaf(String p) { int index = p.lastIndexOf('/'); if (index != -1)
	 * p = p.substring(index+1); return p; } Element path(String p, boolean
	 * toLeaf) { Element e = this.e; StringTokenizer st = new StringTokenizer(p,
	 * "/"); while (st.hasMoreTokens()) { String tok = st.nextToken(); if
	 * (toLeaf || st.hasMoreElements()) { e = e.getChild(tok); if (e == null)
	 * return null; } } return e; }
	 * 
	 * public String getName() { return e.getName(); } public String get() {
	 * return e.getTextTrim(); } public XML getNode(String name) { Element e =
	 * path(name, true); return e != null ? new XML(e) : null; } public String
	 * get(String name) { Element e = path(name, false); name = leaf(name); if
	 * (e == null) return null; if (name.equals(".")) return get(); if
	 * (e.getAttributeValue(name)!=null) return e.getAttributeValue(name);
	 * return e.getChildTextTrim(name); } public String get(String name, String
	 * def) { String s = get(name); if (s==null || s.length()==0) return def;
	 * else return s; } public List getList() { ArrayList al = new ArrayList();
	 * List l = e.getChildren(); if (l == null) return null; Iterator iter =
	 * l.iterator(); while (iter.hasNext()) { Element ce = (Element)iter.next();
	 * XML x = new XML(ce); al.add(x); } return al; } public List getList(String
	 * name) { Element e = path(name, true); ArrayList al = new ArrayList();
	 * List l = e.getChildren(); if (l == null) return null; Iterator iter =
	 * l.iterator(); while (iter.hasNext()) { Element ce = (Element)iter.next();
	 * XML x = new XML(ce); al.add(x); } return al; } public String toString() {
	 * return toString(""); } public String toString(String h) { StringBuffer sb =
	 * new StringBuffer(); sb.append(h+"<"+getName()); Iterator iter =
	 * e.getAttributes().iterator(); while (iter.hasNext()) { Attribute a =
	 * (Attribute)iter.next(); sb.append(" "+a.getName()+"='"+a.getValue()+"'"); }
	 * sb.append(">\n"); if (get().length()!=0) sb.append(h+" "+get()+"\n");
	 * iter = getList().iterator(); while (iter.hasNext()) { XML x =
	 * (XML)iter.next(); sb.append(x.toString(h+" ")); } sb.append(h+"</"+getName()+">\n");
	 * return sb.toString(); }
	 * 
	 * static XMLOutputter outp = new XMLOutputter(); static public String
	 * escape(String s) { return outp.escapeElementEntities(s != null ? s : ""); }
	 * 
	 * static public String trimAll(String s) { s = s.trim(); s =
	 * s.replace('\n', ' '); s = s.replace('\r', ' '); s = s.replace('\t', ' ');
	 * return s; }
	 */
}
