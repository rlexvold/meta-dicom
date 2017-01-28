/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Nov 19, 2003
 * Time: 7:35:37 PM
 */
package acme.storage;

import java.io.File;

import net.metafusion.localstore.SystemChecker;
import acme.util.Log;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class SSStoreFactory
{
	static SSStore store = null;
	static File primaryRoot = null;

	public static File getRoot()
	{
		return primaryRoot;
	}

	public synchronized static SSStore getStore()
	{
		if (store == null)
		{
			XML xml = XMLConfigFile.getDefault().getXML().getNode("storage");
			File storeRoot;
			boolean isNested;
			assert xml.getName().equalsIgnoreCase("storage");
			String name = xml.get("name");
			File root = new File(xml.get("root"));
			Log.log("Store: " + name);
			if (primaryRoot == null) primaryRoot = root;
			storeRoot = new File(root, "storage");
			isNested = xml.getBoolean("nested");
			if (isNested)
				assert false;
			else store = new SSStore(name, storeRoot);
			//RAL - added to check version of DB schema versus version needed by this PACS
			SystemChecker.checkDb();
			// String root2Name = xml.get("root2", "");
			// if (root2Name.length() != 0) {
			// Log.log("DualStore: "+root2Name);
			// SSStore store2;
			// File root2 = new File(root2Name);
			// storeRoot = new File(root2, "storage");
			// isNested = xml.getBoolean("nested");
			// if (isNested)
			// store2 = new SSNestedStore(name, storeRoot);
			// else store2 = new SSFlatStore(name, storeRoot);
			// store = new SSDualStore(store, store2);
			// }
			// String root3Name = xml.get("root3", "");
			// if (root3Name.length() != 0) {
			// Log.log("TriStore: "+root3Name);
			// SSStore store3;
			// File root3 = new File(root3Name);
			// storeRoot = new File(root3, "storage");
			// isNested = xml.getBoolean("nested");
			// if (isNested)
			// store3 = new SSNestedStore(name, storeRoot);
			// else store3 = new SSFlatStore(name, storeRoot);
			// store = new SSDualStore(store, store3);
			// }
			// }
			//
		}
		return store;
	}
}