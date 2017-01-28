package net.metafusion.gui;

import net.metafusion.admin.TaskRunner;

public class GUITask extends TaskRunner
{
	// private static void log(String s) {
	// System.out.println(s);
	// }
	//
	// static public void setGUITaskRunnerFactory() {
	// TaskRunnerFactory.setTaskRunnerFactory(new GUITaskRunnerFactory());
	// }
	// static public class GUITaskRunnerFactory extends TaskRunnerFactory{
	// public TaskRunner getTaskRunner() {
	// return new GUITask();
	// }
	// }
	//
	//
	// private JDialog dialog;
	// private JFrame frame;
	// private ThreadRunner runner;
	//
	// public void run(Runnable r) {
	// r.run();
	// }
	//
	// //public void runWithProgress(Runnable r, String message, int timeout) {
	// public void runWithProgress(Runnable r, String msg, int ticksToWait)
	// throws Exception {
	// //final GUITask task = this;
	// //exception = null;
	// //setDone(false);
	// JFrame frame = MainFrame.getFrame();
	// if (frame == null) {
	// r.run();
	// return;
	// }
	//
	// runner = new ThreadRunner(r);
	// runner.start();
	// Thread.sleep(50);
	//
	// long showAt = System.currentTimeMillis()+ticksToWait;
	// while (!runner.isDone() && System.currentTimeMillis() < showAt) {
	// try {
	// Thread.sleep(250);
	// } catch (InterruptedException e) {
	// }
	// }
	// if (!runner.isDone()) {
	// showDialog(frame, msg);
	// log("back from showdialog");
	// }
	// if (runner.getException() != null) {
	// log("throw exception "+runner.getException());
	// throw runner.getException();
	// }
	// }
	//
	//
	//
	// // TaskRunner.getRunner().runWithProgress(task,"Reading Archive...",
	// 5000);
	// // if (MainFrame.getFrame() != null)
	// // task.runTaskWithProgress(MainFrame.getFrame(), "Reading Archive...",
	// 5000);
	//
	// // public void runTaskWithProgress(JFrame frame, String msg, int
	// ticksToWait) throws Exception {
	// // final GUITask task = this;
	// // exception = null;
	// // setDone(false);
	// // Runnable r = new Runnable() {
	// // public void run() {
	// // try {
	// // task.run();
	// // } catch (Exception e) {
	// // log("runTaskWithProgress caught: "+e);
	// // task.exception = e;
	// // } finally {
	// // log("finally");
	// // setDone(true);
	// // }
	// // }
	// // };
	// // thread = new Thread(r);
	// // thread.start();
	// // Thread.sleep(50);
	// // long showAt = System.currentTimeMillis()+ticksToWait;
	// // while (!isDone() && System.currentTimeMillis() < showAt) {
	// // try {
	// // Thread.sleep(250);
	// // } catch (InterruptedException e) {
	// // }
	// // }
	// // if (!isDone()) {
	// // showDialog(frame, msg);
	// // log("back from showdialog");
	// // }
	// // if (exception != null) {
	// // log("throw exception "+exception);
	// // throw exception;
	// // }
	// // }
	//
	// private void showDialog(JFrame frame, String msg) {
	// JProgressBar pb = new JProgressBar();
	// pb.setIndeterminate(true);
	//
	// final JOptionPane optionPane = new JOptionPane(
	// pb,
	// JOptionPane.INFORMATION_MESSAGE,
	// JOptionPane.DEFAULT_OPTION,
	// null,
	// new String[] { "Cancel" });
	//
	// dialog = new JDialog(frame, msg, true);
	// dialog.setContentPane(optionPane);
	// dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	// dialog.addWindowListener(new WindowAdapter() {
	// public void windowClosing(WindowEvent we) {
	// ;// block close
	// }
	// });
	// dialog.setLocation(250, 250);
	// optionPane.addPropertyChangeListener(
	// new PropertyChangeListener() {
	// public void propertyChange(PropertyChangeEvent e) {
	// String prop = e.getPropertyName();
	// if (dialog.isVisible()
	// && (e.getSource() == optionPane)
	// && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
	// log("call cancel...");
	// try {
	// runner.cancel();
	// } catch (Exception e1) {
	// log("cancel caught...");
	// e1.printStackTrace();
	// }
	// }
	// }
	// });
	//
	// Action timerAction = new AbstractAction() {
	// public void actionPerformed(ActionEvent e) {
	// if (runner.isDone()) {
	// dialog.setVisible(false);
	// dialog.dispose();
	// }
	// }
	// };
	//
	// Timer t = new Timer(300, timerAction);
	// t.start();
	// dialog.pack();
	// dialog.setVisible(true);
	// t.stop();
	//
	// }
	//
	//
	//
	// static class TestRunnable implements Runnable, Cancellable {
	// volatile boolean stop = false;
	// public void run() {
	// for (;;) {
	// Util.sleep(250);
	// if (stop)
	// break;
	// }
	// }
	// public void cancel() {
	// stop = true;
	// }
	// }
	//
	// public static void main(String[] args) {
	// log("Test");
	// try {
	// GUITask t = new GUITask();
	// t.runWithProgress(new TestRunnable(), "msgmsgmg", 2000);
	// } catch (Exception e) {
	// log("test caught "+e);
	// }
	// log("exit Test");
	// }
}
