package net.metafusion.localstore;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import net.metafusion.Dicom;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.net.DicomClientSession;
import net.metafusion.service.CStore;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.RoleMap;
import acme.util.Log;

public class ForwardProcess implements Runnable
{
	public static int							NUM_FORWARD_THREAD	= 1;
	private static int							MAX_SIZE			= 10000;
	private static int							MAX_ATTEMPTS		= 3;
	public static ArrayBlockingQueue<Forward>	bqueue				= new ArrayBlockingQueue<Forward>(MAX_SIZE, true);
	private static int							nextID				= 0;
	static class Forward
	{
		public int		attempts	= 0;
		public long		id;
		public String	ae;

		Forward(long id, String ae)
		{
			this.id = id;
			this.ae = ae;
		}
	}

	public static void forward(long id, String ae)
	{
		addElem(new Forward(id, ae));
	}

	private static void addElem(Forward elem)
	{
		if (elem.attempts < MAX_ATTEMPTS)
		{
			while (bqueue.offer(elem) == false)
			{
				try
				{
					Forward drop = bqueue.take();
				}
				catch (Exception ex)
				{
				}
			}
		}
	}

	synchronized int getID()
	{
		int id = nextID++;
		return id;
	}
	int	id;

	ForwardProcess()
	{
		id = getID();
	}

	public boolean send(AE ae, List imageList)
	{
		DicomClientSession clientSess = null;
		boolean good = false;
		try
		{
			Iterator iter = imageList.iterator();
			clientSess = new DicomClientSession(RoleMap.getStoreUserRoleMap());
			boolean connected = clientSess.connect(ae);
			if (!connected)
				return false;
			while (connected && iter.hasNext())
			{
				Image image = (Image) iter.next();
				Log.log("send " + image.getImageID() + " to " + ae);
				CStore store = new CStore(clientSess, image);
				store.run();
				if (store.getResult() != Dicom.SUCCESS)
					return false;
			}
			good = true;
			clientSess.close(true);
		}
		catch (Exception e)
		{
			Log.log("ForwardProcess.send caught ", e);
		}
		finally
		{
			if (clientSess != null)
				clientSess.close(false);
		}
		return good;
	}

	public boolean sendImage(long imageID, String aeString)
	{
		Log.log("ForwardProcess:sendimage " + imageID + " to " + aeString);
		Image i = ImageView.get().selectByID(imageID);
		if (i == null)
		{
			Log.log("ForwardProcess:sendimage: bad image " + i);
			return true;
		}
		AE ae = AEMap.get(aeString);
		if (ae == null)
		{
			Log.log("ForwardProcess:sendimage: bad ae " + aeString);
			return true;
		}
		List imageList = new LinkedList();
		imageList.add(i);
		return send(ae, imageList);
	}

	public void run()
	{
		for (;;)
		{
			try
			{
				Forward elem = bqueue.take();
				elem.attempts++;
				boolean good = sendImage(elem.id, elem.ae);
				if (!good)
				{
					addElem(elem);
				}
			}
			catch (Exception e)
			{
				Log.log("forward process thread caught ", e);
			}
		}
	}
}