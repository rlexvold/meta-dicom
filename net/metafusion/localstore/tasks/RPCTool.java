/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Nov 18, 2003
 * Time: 8:35:19 AM
 */
package net.metafusion.localstore.tasks;

public class RPCTool
{
	// static void log(String s) { net.metafusion.util.Log.info(s); }
	// public static void error() {
	// log("usage: rpctool host:port [getids|getxml|getdata] startNum ");
	// System.exit(1);
	//
	// }
	// public static void main(String[] args) {
	// try {
	// if (args.length < 3) {
	// error();
	// }
	// InetSocketAddress addr = Util.decodeInetSocketAddress(args[0]);
	// LocalStoreClient lsc = new LocalStoreClient(addr);
	// long start = Long.parseLong(args[2]);
	//
	// if (args[1].equalsIgnoreCase("getids")) {
	// long ids[] = lsc.getIDs(start);
	// for (int i=0; i<ids.length; i++)
	// log(""+ids[i]);
	// }
	// else if (args[1].equalsIgnoreCase("getxml")) {
	// XML xml = lsc.getMeta(start);
	// log(""+xml);
	// }
	// else if (args[1].equalsIgnoreCase("getdata")) {
	// InputStream is = lsc.getInputStream(start);
	// byte b[] = new byte[8192];
	// for (;;) {
	// int cnt = is.read(b);
	// if (cnt == -1)
	// break;
	// }
	// is.close();
	// } else {
	// error();
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// System.exit(-1);
	// }
	// }
	//
}