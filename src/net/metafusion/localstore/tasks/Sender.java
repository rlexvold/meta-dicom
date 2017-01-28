/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Oct 23, 2003
 * Time: 7:15:37 PM
 */
package net.metafusion.localstore.tasks;

public class Sender
{ // implements Runnable {
// static void log(String s) { Log.log(s); }
//
// AE destAE = null;
// List studyList;
//
// public Sender(AE destAE, List srudyList) throws Exception {
// this.destAE = destAE;
// this.studyList = studyList;
// }
// public void run() {
// DicomClientSession sess = null;
// try {
// log("ping to "+destAE);
// sess = new DicomClientSession(RoleMap.getStoreUserRoleMap());
// sess.connect(destAE);
// if (!sess.isConnected())
// throw new Exception("could not connect");
// Iterator iter = studyList.iterator();
// for (iter.hasNext()) {
// Study s = (Study)iter.next();
//
// CEcho echo = new CEcho(sess);
// echo.run();
// if (echo.getResult() != Dicom.SUCCESS) {
// log("ping failed");
// throw new RuntimeException("ping failed");
// }
// }
// sess.close(true);
// } catch (Exception e) {
// log("ping caught "+e);
// e.printStackTrace();
// throw new RuntimeException(e);
// } finally {
// if (sess != null)
// sess.close(false);
// }
// }
}
