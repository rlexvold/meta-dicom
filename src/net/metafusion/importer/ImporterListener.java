package net.metafusion.importer;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.localstore.DicomStore;
import acme.storage.SSStore;
import acme.util.Util;

public class ImporterListener implements Runnable
{
	private static boolean	isListening			= false;
	private int				pollInterval;
	private boolean			deleteRecordFlag	= false;
	public static enum ListenTypes
	{
		DATABASE_LISTENER, FILE_LISTENER
	};
	private ListenTypes	theType;

	public static void init(ListenTypes type, int pollInterval, boolean deleteRecordFlag) throws Exception
	{
		// ExampleLoader ex = new ExampleLoader();
		// ex.runExample();
		if (!isListening)
		{
			Util.log("ConverterListener.init");
			Util.startDaemonThread(new ImporterListener(type, pollInterval, deleteRecordFlag));
			isListening = true;
		}
	}

	public ImporterListener(ListenTypes type, int poll, boolean deleteRecordFlag)
	{
		theType = type;
		pollInterval = poll;
		this.deleteRecordFlag = deleteRecordFlag;
	}

	public void run()
	{
		Listener theListener;
		switch (theType)
		{
			case DATABASE_LISTENER:
				theListener = new DbListener();
				break;
			case FILE_LISTENER:
				theListener = new FileListener();
				break;
			default:
				theListener = null;
				return;
		}
		while (true)
		{
			try
			{
				if (theListener.listenForEvent(pollInterval))
				{
					ArrayList<ImageImportHeader> heads = theListener.getHeaders();
					for (int i = 0; i < heads.size(); i++)
					{
						ImageImportHeader h = heads.get(i);
						DS ds = DicomConverter.convertJpegToDicom(h);
						writeDsToStore(ds);
						theListener.cleanUp(h, deleteRecordFlag);
					}
				}
			}
			catch (Exception e)
			{
				Util.log("ConverterListener Error: ", e);
			}
			Util.sleep(pollInterval);
		}
	}

	private void writeDsToStore(DS ds) throws Exception
	{
		File dcmFile = null;
		try
		{
			dcmFile = SSStore.get().createTempFile(".dcm");
			DSOutputStream.writeDicomFile(ds, dcmFile);
			DicomStore.get().loadDicomFile(dcmFile, true);
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			Util.safeDelete(dcmFile);
		}
	}

	public static byte[] readSourceFile(ImageImportHeader head) throws Exception
	{
		try
		{
			File f = new File(head.getFilename());
			BufferedImage bi = ImageIO.read(f);
			byte[] outData = new byte[head.getColumns() * head.getRows() * head.getSamplesPerPixel()];
			Raster tmpRaster = bi.getRaster();
			int loc = 0;
			for (int height = 0; height < head.getRows(); height++)
			{
				for (int width = 0; width < head.getColumns(); width++, loc += head.getSamplesPerPixel())
				{
					for (int samp = 0; samp < head.getSamplesPerPixel(); samp++)
					{
						outData[loc + samp] = (byte) tmpRaster.getSample(width, height, samp);
					}
				}
			}
			return outData;
		}
		catch (Exception e)
		{
			Util.log("ConverterListener.readSourceFile Error: ", e);
			throw e;
		}
	}
}
