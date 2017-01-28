package net.metafusion.admin;

public class xxxAdminSession
{
	// private static AdminSession session = null;
	// public static AdminSession get() {
	// if (session == null)
	// session = new AdminSession();
	// return session;
	// }
	//
	// protected AdminSession() {
	// refresh();
	// }
	//
	// XML nullXML = XML.parseXML(
	// " <metaadmin> "+
	// " <admin host='none' port='0' date='Sun Feb 15 09:36:46 PST 2004' /> "+
	// " <aelist> "+
	// " </aelist> "+
	// " <stores> "+
	// " </stores> "+
	// " <proxies> "+
	// " </proxies> "+
	// " </metaadmin> "
	// );
	// XML configXML = nullXML;
	//
	// boolean haveConfig() {
	// return configXML != nullXML;
	// }
	//
	// public List getOlderConfig(String host, int port) {
	// List fileNames = new ArrayList();
	// XML x = AdminClient.doOlderConfig(host, port);
	// List l = x.getList();
	// for (int i=0; i<l.size();i++)
	// fileNames.add(((XML)l.get(i)).get());
	//
	// return fileNames;
	// // try {
	// // fileNames = XMLConfigFile.getDefault().getOlderAdmin();
	// // } catch (Exception e) {
	// // Util.log("could not load config: "+e);
	// // }
	// // return fileNames;
	// }
	// public boolean loadConfig(String host, int port, String name) {
	// configXML = nullXML;
	// this.host = "<none>";
	// this.port = 0;
	// this.commitDate = "n/a";
	// try {
	// XML x = AdminClient.doConfig(host, port, name);
	// x = x.getNode("meta-admin");
	// // if (name==null)
	// // configXML = XMLConfigFile.getDefault().getCurrentAdmin();
	// // else configXML = XMLConfigFile.getDefault().getOlderAdmin(name);
	// this.host = host;
	// this.port = port;
	// this.commitDate = x.get("admin/date", new Date().toString());
	// configXML = x;
	// } catch (Exception e) {
	// Util.log("could not load config: "+e);
	// }
	// refresh();
	// return haveConfig();
	// }
	//
	// public void refresh() {
	// try {
	// loadae();
	// } catch (Exception e) {
	// Util.log("could not load ae: "+e);
	// }
	// try {
	// loadstores();
	// } catch (Exception e) {
	// Util.log("could not load stores: "+e);
	// }
	// }
	//
	// private boolean storeConfig(String host, int port, XML config) throws
	// Exception {
	// if (config == nullXML)
	// return true;
	// Date d = new Date();
	// config.getNode("admin").addAttr("date", d.toString());
	// AdminClient.doStoreConfig(host, port, config);
	// this.commitDate = d.toString();
	// // Util.log(""+configXML);
	// // XMLConfigFile.getDefault().saveAdmin(config);
	//
	// return true;
	// }
	//
	// public void commit() {
	// try {
	// if (configXML != nullXML)
	// storeConfig(host, port, configXML);
	// } catch (Exception e) {
	// throw new NestedException(e);
	// }
	// }
	//
	// // public StorageBean getStorage(AE ae) {
	// // StorageBean sb = new StorageBean();
	// // //
	// // return sb;
	// // }
	//
	// //
	// // ae
	// //
	// private HashMap aeMap = new HashMap();
	// private List aeList = new ArrayList();
	// private void loadae() {
	// aeMap.clear();
	// aeList.clear();
	// List l = configXML.getNode("aeList").getList();
	// for (Iterator iter=l.iterator(); iter.hasNext();) {
	// XML x = (XML)iter.next();
	// AEBean ae = new AEBean(x);
	// aeMap.put(ae.getName().toUpperCase(), ae);
	// aeList.add(ae);
	// Collections.sort(aeList);
	// }
	// }
	// public AEBean getAE(String name) {
	// AEBean ae = (AEBean)aeMap.get(name.toUpperCase());
	// return ae;
	// }
	// public AEBean[] getAE() {
	// return (AEBean[])aeList.toArray(new AEBean[aeList.size()]);
	// }
	//
	// public void addAE(AEBean ae) {
	// if (ae.getName().length() == 0)
	// throw new RuntimeException("Name must be defined.");
	// if (ae.getHost().length() == 0)
	// throw new RuntimeException("Host must be defined.");
	// if (ae.getPort().length() == 0)
	// throw new RuntimeException("Port must be defined.");
	//
	// if (aeMap.containsKey(ae.getName()))
	// throw new RuntimeException("AE with this name already exists.");
	// try {
	// ;// InetAddress inet = InetAddress.getByName(host);
	// } catch (Exception e) {
	// throw new RuntimeException("The host does not exist.");
	// }
	// try {
	// int p = Integer.parseInt(ae.getPort());
	//
	// } catch (Exception e) {
	// throw new RuntimeException("The port does not exist.");
	// }
	// XML x = ae.toXML();
	// configXML.getNode("aelist").add(x);
	// commit();
	// refresh();
	// }
	//
	// public void removeAE(AEBean ae) {
	// if (!aeMap.containsKey(ae.getName()))
	// throw new RuntimeException("AE "+ae.getName()+" does not exist.");
	// aeMap.remove(ae.getName());
	// aeList.remove(ae);
	// XML ael = configXML.getNode("aelist");
	// ael.removeChildNode("name", ae.getName());
	// commit();
	//
	// refresh();
	// }
	//
	// public void updateAE(AEBean ae, String host, String port) {
	// boolean fail = true;
	// AEBean orig = (AEBean)aeMap.get(ae.getName());
	// removeAE(ae);
	// try {
	// ae.setHost(host);
	// ae.setPort(port);
	// addAE(ae);
	// fail = false;
	// } finally {
	// if (fail)
	// addAE(orig);
	// }
	// }
	// // public boolean verifyAE(String name) {
	// // return true;
	// // }
	// public boolean verifyAE(String ae) {
	// boolean result = false;
	// try {
	// XML x = AdminClient.doVerify(host, port, ae);
	// if (x.get("result").equalsIgnoreCase("true"))
	// result = true;
	// } catch (Exception e) {
	// Util.log("could not verify "+ae, e);
	// }
	// return result;
	// }
	// public boolean verifyAE(ServerBean bean) {
	// boolean result = verifyAE(bean.getAE());
	// bean.setInvalid(!result);
	// return result;
	// }
	//
	//
	// ServerBean[] localStores = new ServerBean[0];
	// ProxyBean[] proxies = new ProxyBean[0];
	//
	// void loadstores() {
	// localStores = new ServerBean[0];
	// proxies = new ProxyBean[0];
	// List l = configXML.getNode("stores").getList();
	// ServerBean[] sb = new ServerBean[l.size()];
	// int i = 0;
	// for (Iterator iter=l.iterator(); iter.hasNext();) {
	// XML x = (XML)iter.next();
	// sb[i++] = new ServerBean(x);
	// }
	// i = 0;
	// l = configXML.getNode("proxies").getList();
	// ProxyBean[] pb = new ProxyBean[l.size()];
	// for (Iterator iter=l.iterator(); iter.hasNext();) {
	// XML x = (XML)iter.next();
	// pb[i++] = new ProxyBean(x);
	// }
	// localStores = sb;
	// proxies = pb;
	// }
	//
	//
	// public ServerBean[] getLocalStores() {
	// return localStores;
	// }
	// public ProxyBean[] getProxies() {
	// return proxies;
	// }
	// String host = "";
	// int port = 0;
	// String commitDate = "n/a";
	// public String getHost() {
	// return host;
	// }
	//
	// public int getPort() {
	// return port;
	// }
	//
	//
	// public String getCommitDate() {
	// return commitDate;
	// }
	//
	//
	// public void setActive(ServerBean serverBean, boolean active) {
	// serverBean.setActive(active);
	// }
	// public StorageBean getStorage(ServerBean sb) {
	// StorageBean stb = new StorageBean();
	// try {
	// sb.setInvalid(true);
	// AEBean ae = getAE(sb.getAE());
	// XML x = AdminClient.doStorage(ae.getHost(),
	// Integer.parseInt(ae.getPort()));
	// stb = new StorageBean(x);
	// sb.setInvalid(false);
	// } catch (Exception e) {
	// Util.log("could not get storage ae="+sb.getAE(), e);
	// }
	// return stb;
	// }
	// //todo:
	// //
	// 1077767804062,store-to,AE_TITLE,/10.42.3.20:4006,SOPInstanceUID=1.2.840.113674.950809132337081.100
	//
	// HashMap maxDate = new HashMap();
	//
	// List log = new ArrayList();
	// public List getLog(ServerBean serverBean, Date start, int maxToGet) {
	// AEBean aeBean = getAE(serverBean.getAE());
	// if (start == null)
	// start = new Date(System.currentTimeMillis() - 24*60*60*1000);
	// XML x = AdminClient.doLog(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), start.getTime(), maxToGet);
	// List l = x.getList();
	// log = new ArrayList();
	// LogBean lb = null;
	// for (int i=0; i<l.size(); i++) {
	// XML line = (XML)l.get(i);
	// if (line.getName().equalsIgnoreCase("line")) {
	// lb = new LogBean(line);
	// log.add(new LogBean(line));
	// }
	// }
	// if (lb != null)
	// maxDate.put(serverBean.getAE(),lb.getDateObject());
	// return log;
	// }
	// public void getLogNext(ServerBean serverBean, List l, int maxToGet) {
	// Date date = (Date)maxDate.get(serverBean.getAE());
	// if (date != null) {
	// date = new Date(date.getTime()+1);
	// }
	// List newList = getLog(serverBean, date, maxToGet);
	// l.addAll(newList);
	// }
	//
	// public StudyBean[] search(String ae, SearchBean search) {
	// AEBean aeBean = this.getAE(ae);
	// XML x = AdminClient.doSearch(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), search.toXML());
	// XML studies = x.getNode("studies");
	// List l = studies != null ? studies.getList() : null;
	// ArrayList al = new ArrayList();
	// if (l != null) {
	// for (int i=0; i<l.size();i++)
	// al.add(new StudyBean((XML)l.get(i)));
	// }
	// return (StudyBean[])al.toArray(new StudyBean[0]);
	//
	// }
	//
	// public String[] send(String sourceAE, List studyBeans, String ae) {
	// AEBean aeBean = this.getAE(sourceAE);
	// XML studies = new XML("studies");
	// for (Iterator iter=studyBeans.iterator();iter.hasNext();) {
	// StudyBean sb = (StudyBean)iter.next();
	// if (sb.isSelected())
	// studies.add("studyUID",sb.getStudyUID());
	// }
	// XML x = AdminClient.doSend(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), ae, studies);
	// studies = x.getNode("studies");
	// List l = studies != null ? studies.getList() : null;
	// ArrayList al = new ArrayList();
	// if (l != null) {
	// for (int i=0; i<l.size();i++)
	// al.add(((XML)l.get(i)).get());
	// }
	// return (String[])al.toArray(new String[0]);
	// }
	// public String[] delete(String sourceAE, List studyBeans) {
	// AEBean aeBean = this.getAE(sourceAE);
	// XML studies = new XML("studies");
	// for (Iterator iter=studyBeans.iterator();iter.hasNext();) {
	// StudyBean sb = (StudyBean)iter.next();
	// if (sb.isSelected())
	// studies.add("studyUID",sb.getStudyUID());
	// }
	// XML x = AdminClient.doDelete(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), studies);
	// studies = x.getNode("studies");
	// List l = studies != null ? studies.getList() : null;
	// ArrayList al = new ArrayList();
	// if (l != null) {
	// for (int i=0; i<l.size();i++)
	// al.add(((XML)l.get(i)).get());
	// }
	// return (String[])al.toArray(new String[0]);
	// }
	// public StudiesInfoBean getInfo(String sourceAE, List studyBeans) {
	// AEBean aeBean = this.getAE(sourceAE);
	// XML studies = new XML("studies");
	// for (Iterator iter=studyBeans.iterator();iter.hasNext();) {
	// StudyBean sb = (StudyBean)iter.next();
	// if (sb.isSelected())
	// studies.add("studyUID",sb.getStudyUID());
	// }
	// XML x = AdminClient.doGetInfo(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), studies);
	// XML studiesInfo = x.getNode("StudiesInfoBean");
	// StudiesInfoBean info = new StudiesInfoBean(studiesInfo);
	// return info;
	// }
	//
	// public boolean archive(String sourceAE, List studyBeans, File rootDir) {
	// boolean good = false;
	// File f = null;
	// //ArrayList al = null;
	// try {
	// AEBean aeBean = this.getAE(sourceAE);
	// XML studies = new XML("studies");
	// for (Iterator iter=studyBeans.iterator();iter.hasNext();) {
	// StudyBean sb = (StudyBean)iter.next();
	// if (sb.isSelected())
	// studies.add("studyUID",sb.getStudyUID());
	// }
	// f = MSJavaHack.get().createTempFile("metadd",".zip");
	//
	// good = AdminClient.doArchive(aeBean.getHost(),
	// Integer.parseInt(aeBean.getPort()), studies, f);
	//
	// if (good)
	// good = ZipUtil.unzip(f, rootDir);
	// return good;
	//
	// } catch (Exception e) {
	// Util.log("archive error", e);
	// } finally {
	// if (f != null)
	// Util.safeDelete(f);
	// }
	// return good;
	// }
	//
	//
	// // boolean haveConfig = false;
	// // XML config = null;
	// // String host = "";
	// // int port = 0;
	// //
	// // public boolean getConfig(String host, int port) {
	// // this.host = host;
	// // this.port = port;
	// // config = adminServer.loadConfig(host, port);
	// // haveConfig = adminServer.haveConfig();
	// // return haveConfig;
	// // }
	// //
	// // public boolean haveConfig() {
	// // return haveConfig;
	// // }
	// //
	// // public String getHost() {
	// // return host;
	// // }
	// //
	// // public int getPort() {
	// // return port;
	// // }
	// //
	// //
	// // public String getCommitDate() {
	// // return config.get("admin/date", new Date().toString());
	// // }
	// //
	// // public boolean commit() {
	// // return adminServer.storeConfig(host, port, config);
	// // }
	// //
	// //
	// // //
	// // // Metafusion.AE
	// // //
	// //
	// // public AEBean[] getAE() {
	// // return adminServer.getAE();
	// // }
	// //
	// // public void addAE(AEBean ae) {
	// // adminServer.addAE(ae);
	// // }
	// //
	// // public void removeAE(AEBean ae) {
	// // adminServer.removeAE(ae);
	// // }
	// //
	// // public void updateAE(AEBean ae, String host, String port) {
	// // adminServer.updateAE(ae, host, port);
	// // }
	// //
	// // static boolean failVerify = true;
	// // public boolean verifyAE(String name) {
	// // return adminServer.verifyAE(name);
	// // }
	// //
	// //
	// // //
	// // //
	// // //
	// //
	// // public ServerBean[] getLocalStores() {
	// // return new ServerBean[] { new ServerBean("sandiego") };
	// // }
	// // public ProxyBean[] getProxies() {
	// // return new ProxyBean[] { new ProxyBean("proxy1"),new
	// ProxyBean("proxy2"),new ProxyBean("proxy3") };
	// // }
	// // public StudyBean[] search(String ae, SearchBean search) {
	// // StudyBean[] sb = new StudyBean[50];
	// // for (int i=0; i<50; i++) {
	// // StudyBean s = new StudyBean();
	// // s.setAccession("acc"+i);
	// // s.setBirthdate("1/1/2000");
	// // s.setDate("1/1/2000");
	// // s.setDescription("a study ");
	// // s.setModality("CT");
	// // s.setName("Patient Name"+i);
	// // s.setPatientID("ididid"+i);
	// // s.setRadiologist("Dr Good");
	// // s.setReferringMD("Dr Night");
	// // s.setSex("F");
	// // s.setStationName("ct66");
	// // s.setStudyID("123.324.434.344."+i);
	// // s.setTime("10:00:00");
	// // sb[i] = s;
	// // }
	// // return sb;
	// // }
	// // StorageRuleBean srb[] = new StorageRuleBean[] {
	// // new StorageRuleBean(0, false, ""), new StorageRuleBean(1, false, "")
	// // };
	// // ForwardRuleBean frb[] = new ForwardRuleBean[] {
	// // new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// // new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// // new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// // new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "","")
	// // };
	// // public StorageRuleBean[] getStorageRuleBean(ServerBean serverBean) {
	// // StorageRuleBean a[] = new StorageRuleBean[srb.length];
	// // for (int i=0; i<srb.length;i++)
	// // a[i] = new StorageRuleBean(srb[i]);
	// // return a;
	// // }
	// // public ForwardRuleBean[] getForwardRuleBean(ServerBean serverBean) {
	// // ForwardRuleBean a[] = new ForwardRuleBean[frb.length];
	// // for (int i=0; i<frb.length;i++)
	// // a[i] = new ForwardRuleBean(frb[i]);
	// // return a;
	// // }
	// // public void setStorageRuleBean(ServerBean sb, StorageRuleBean[] srb) {
	// // this.srb = srb;
	// // }
	// // public void setForwardRuleBean(ServerBean sb, ForwardRuleBean[] frb) {
	// // this.frb = frb;
	// // }
	// //
	// // public StorageBean getStorage(ServerBean sb) {
	// // return new StorageBean();
	// // }
	// //
	// // public void setActive(ServerBean serverBean, boolean active) {
	// // serverBean.setActive(active);
	// // }
	// //
	// // List log = new ArrayList();
	// // public List getLog(ServerBean serverBean, Date start, int maxToGet) {
	// // log = new ArrayList();
	// // for (int i = 0; i< (maxToGet==0 ? 100 : maxToGet); i++)
	// // log.add(new LogBean());
	// // return log;
	// // }
	// // public void getLogNext(List l, int maxToGet) {
	// // for (int i = 0; i< (maxToGet==0 ? 100 : maxToGet); i++)
	// // l.add(new LogBean());
	// // }
}
