/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Nov 21, 2003
 * Time: 6:33:17 PM
 */
package net.metafusion.util;

class DicomLog
{
	// static File logFile = null;
	// static PrintStream vps = null;
	// static PrintStream ps = null;
	// static boolean init = false;
	// static boolean enabled = true;
	// static String prefix = "";
	// static File path = new File(".");
	// static XML xml = null;
	//
	// static public void setEnabled(boolean on) {
	// enabled = on;
	// }
	// static private void init() {
	// init = true;
	// }
	//
	// static class LoggerImpl extends acme.util.Log {
	// public void dolog(String s) {
	// Log.dolog(s);
	// }
	// public void force(String s) {
	// Log.progress(s);
	// }
	// }
	//
	// synchronized static public void init(String prefix) {
	// if (init)
	// return;
	// try {
	// XML xml = new
	// XML(XMLConfigFile.getDefault().getSubconfigFile("log.xml"));
	// init(new File(xml.get("root")), prefix, xml);
	// acme.util.Log.set(new LoggerImpl());
	// } catch (Exception e) {
	// System.out.println("could not load log.xml "+e);
	// e.printStackTrace();
	// }
	// }
	//
	//
	// static int currentDay = -1;
	// static int currentMonth = -1;
	//
	// synchronized static void rotate() {
	// Calendar c = Calendar.getInstance();
	// int day = c.get(Calendar.DAY_OF_MONTH);
	// if (day == currentDay)
	// return;
	//
	// if (xml != null)
	// update(xml);
	//
	// // rename verbose
	// Util.safeClose(vps);
	// String fileName = prefix+"-"+StringUtil.int2(day)+".vlog";
	// File newDayFile = new File(path,fileName);
	// FileUtil.safeDelete(newDayFile);
	// try {
	// vps = new PrintStream(new FileOutputStream(newDayFile,true));
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("could not create vps printstream");
	// }
	//
	// int month = c.get(Calendar.MONTH);
	// if (currentMonth == month)
	// return;
	//
	// // rename progress
	// File currentLogFile = new File(path, prefix+".log");
	//
	// String baseName =
	// prefix+"-"+StringUtil.int2(c.get(Calendar.MONTH)+1)+StringUtil.int2(c.get(Calendar.DAY_OF_MONTH))+
	// StringUtil.int2(c.get(Calendar.YEAR)-2000)+"_"+StringUtil.int2(c.get(Calendar.HOUR_OF_DAY))+StringUtil.int2(c.get(Calendar.MINUTE));
	// int count = 1;
	// for (;;) {
	// logFile = new File(path,fileName);
	// if (!logFile.exists() || count>150)
	// break;
	// fileName = baseName+"_"+(count++)+".log";
	// }
	// Util.safeClose(ps);
	// FileUtil.safeRename(currentLogFile, logFile);
	// try {
	// ps = new PrintStream(new FileOutputStream(currentLogFile,true));
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.out.println("could not create ps printstream");
	// }
	// currentMonth = month;
	// }
	//
	// static private void init(File path, String prefix, XML xml) {
	// if (init)
	// return;
	// try {
	// init = true;
	// Log.path = path;
	// Log.prefix = prefix;
	// Log.xml = xml;
	// rotate();
	// acme.util.Log.set(new LoggerImpl());
	// if (xml != null)
	// update(xml);
	// } catch (Exception e) {
	// System.out.println("could not create Log file");
	// e.printStackTrace();
	// }
	//
	// }
	//
	// public static void xrotate() {
	// Log.force("*********************** rotate close "+new Date());
	// PrintStream newPs = null;
	// try {
	// Calendar c = Calendar.getInstance();
	// int count = 1;
	// String baseName =
	// prefix+"-"+StringUtil.int2(c.get(Calendar.MONTH)+1)+StringUtil.int2(c.get(Calendar.DAY_OF_MONTH))+
	// StringUtil.int2(c.get(Calendar.YEAR)-2000)+"_"+StringUtil.int2(c.get(Calendar.HOUR_OF_DAY))+StringUtil.int2(c.get(Calendar.MINUTE));
	// String fileName = baseName+".log";
	// for (;;) {
	// logFile = new File(path,fileName);
	// if (!logFile.exists() || count>150)
	// break;
	// fileName = baseName+"_"+(count++)+".log";
	// }
	// if (xml != null)
	// update(xml);
	// newPs = new PrintStream(new FileOutputStream(logFile));
	// PrintStream oldPs = vps;
	// vps = newPs;
	// oldPs.close();
	// Log.force("*********************** rotate open "+new Date());
	// } catch (Exception e) {
	// System.out.println("could not create Log file");
	// e.printStackTrace();
	// }
	// }
	//
	//
	// public static void update(XML xml) {
	// HashSet hs = new HashSet();
	// List l = xml.getList();
	// for (Iterator iter=l.iterator();iter.hasNext();) {
	// XML x = (XML)iter.next();
	// if (x.getName().equalsIgnoreCase("type")) {
	// String name=x.get("name");
	// boolean enabled = x.getBoolean("enabled");
	// if (enabled)
	// hs.add(name);
	// }
	// }
	// doUpdate(hs);
	// }
	//
	// synchronized static private void dolog(String s) {
	// if (!enabled)
	// return;
	// System.out.println(s);
	// if (!init)
	// return;
	// if (vps!=null)
	// vps.println(s);
	// }
	// synchronized static public void progress(String s) {
	// System.out.println(s);
	// if (!init)
	// return;
	// if (ps!=null)
	// ps.println(s);
	// if (vps!=null)
	// vps.println(s); }
	//
	// static public void access(String s) {
	// dolog("access: "+s);
	// }
	// static public void raw(String s) {
	// dolog(s);
	// }
	// static public void log(String s) {
	// dolog("log: "+s);
	// }
	// static public void info(String s) {
	// dolog("info: "+s);
	// }
	// static public void error(String s) {
	// if (!enabled) {
	// enabled = true;
	// dolog("log enabled due to error!!!!!");
	// }
	// dolog("error: "+s);
	// progress("error: "+s);
	// }
	// static public void error(String s, Exception e) {
	// if (!enabled) {
	// enabled = true;
	// dolog("log enabled due to error!!!!!");
	// }
	// dolog("error: "+s);
	// dolog(Util.stackTraceToString(e));
	// progress("error: "+s);
	// progress(Util.stackTraceToString(e));
	// }
	// static public void error(Exception e) {
	// if (!enabled) {
	// enabled = true;
	// dolog("log enabled due to error!!!!!");
	// }
	// dolog("error: "+e);
	// dolog(Util.stackTraceToString(e));
	// progress("error: "+e);
	// progress(Util.stackTraceToString(e));
	// }
	// static public void force(String s) {
	// boolean oldenabled = enabled; // race
	// enabled = true;
	// dolog(s);
	// progress(s);
	// enabled = oldenabled;
	// }
	//
	// static public void action(String s) {
	// boolean oldenabled = enabled; // race
	// enabled = true;
	// dolog("action: "+s);
	// enabled = oldenabled;
	// }
	//
	// static public void doUpdate(HashSet hs) {
	// debugEnabled = hs.contains("debug");
	// pduEnabled = hs.contains("pdu");
	// pduBytesEnabled = hs.contains("pduBytes");
	// msgEnabled = hs.contains("msg");
	// sessEnabled = hs.contains("sess");
	// connectEnabled = hs.contains("connect");
	// dbEnabled = hs.contains("db");
	// datasetEnabled = hs.contains("dataset");
	// rpcEnabled = hs.contains("rpc");
	// }
	//
	// static public void enableAll(boolean on) {
	// debugEnabled = on;
	// pduEnabled = on;
	// pduBytesEnabled =on;
	// msgEnabled = on;
	// sessEnabled = on;
	// connectEnabled = on;
	// dbEnabled = on;
	// datasetEnabled = on;
	// rpcEnabled = on;
	// }
	//
	//
	// static public boolean debugEnabled = true;
	// static public void debug(String s) {
	// if (debugEnabled)
	// dolog("debug: "+s);
	// }
	// static public boolean pduEnabled = true;
	// static public void pdu(String s) {
	// if (pduEnabled)
	// dolog("pdu: "+s);
	// }
	// static public boolean pduBytesEnabled = true;
	// static public void pduBytes(String s) {
	// if (pduBytesEnabled)
	// dolog("pdu: "+s);
	// }
	// static public boolean msgEnabled = true;
	// static public void msg(String s) {
	// if (msgEnabled)
	// dolog("msg: "+s);
	// }
	// static public boolean sessEnabled = true;
	// static public void sess(String s) {
	// if (sessEnabled)
	// dolog("sess: "+s);
	// }
	// static public boolean connectEnabled = true;
	// static public void connect(String s) {
	// if (connectEnabled)
	// dolog("connect: "+s);
	// }
	// static public boolean dbEnabled = true;
	// static public void db(String s) {
	// if (dbEnabled)
	// dolog("db: "+s);
	// }
	//
	// static public boolean datasetEnabled = true;
	// static public void dataset(String s) {
	// if (datasetEnabled)
	// dolog("dataset: "+s);
	// }
	// static public boolean rpcEnabled = true;
	// static public void rpc(String s) {
	// if (rpcEnabled)
	// dolog("rpc: "+s);
	// }
	//
	//
	//
	//
}