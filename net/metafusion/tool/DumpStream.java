package net.metafusion.tool;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.pdu.DataTransfer;
import net.metafusion.pdu.PDU;
import net.metafusion.pdu.PDV;
import net.metafusion.util.UID;
import acme.util.Log;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class DumpStream implements Runnable
{
	static void log(String s)
	{
		Log.log(s);
	}

	public DumpStream()
	{
	}

	public void dump(int type, byte[] buffer) throws Exception
	{
		log(PDU.getPDUName(type) + ": " + buffer.length);
		PDU pdu = PDU.getPDU(type, buffer, buffer.length);
		if (!(pdu instanceof DataTransfer))
		{
			log(Util.dumpBytesToString(buffer));
		}
		if (pdu instanceof DataTransfer)
		{
			DataTransfer dt = (DataTransfer) pdu;
			for (;;)
			{
				PDV pdv = dt.nextPDV();
				if (pdv == null) break;
				log("PDV id=" + pdv.getId() + " " + (pdv.isCmd() ? "CMD" : "DATA") + " done=" + pdv.isDone());
				if (pdv.isCmd())
				{
					DS ds = new DS(new ByteArrayInputStream(pdv.getBytes()), UID.ImplicitVRLittleEndian); // always
																											// ImplicitVRLittleEndian
					log("" + ds);
					log(Util.dumpBytesToString(pdv.getBytes()));
				}
			}
		}
	}

	// if (Log.pduBytesEnabled && (pduLen<512 || type != PDU.P_DATA_TF)) {
	// Log.pduBytes("==== READ
	// PDU=======================================================");
	// Log.raw(Util.dumpBytesToString(pduBytes, 0, Math.min(2048, pduLen)));
	// }
	public void dump(File f) throws Exception
	{
		log(f.getName() + " ==========================================");
		// File f = new File("data/in-121803_1134.dat");
		// File f = new File("data/in-121803_0711.dat");
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		for (;;)
		{
			PDU pdu = null;
			int type = dis.read();
			if (type == -1) break;
			dis.skipBytes(1);
			int pduLen = dis.readInt();
			if (pduLen > 100000) throw new Exception("PDU too big " + pduLen);
			byte pduBytes[] = new byte[pduLen];
			dis.readFully(pduBytes, 0, pduLen);
			dump(type, pduBytes);
		}
		dis.close();
	}

	public void runit() throws Exception
	{
		File f = new File("data");
		File files[] = f.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].getName().endsWith(".dat"))
			// if (files[i].getName().indexOf("_1134")!=-1)
				// if (files[i].getName().indexOf("_1313")!=-1)
				if (files[i].getName().indexOf("_1442") != -1)
				{
					try
					{
						dump(files[i]);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		}
	}

	public void run()
	{
		try
		{
			runit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("run caught " + e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length < 1)
			{
				log("usage:  conf_file_path.xml ");
				System.exit(1);
			}
			System.out.println("DICOMDIR conf=" + args[0] + " targ=default");
			Util.parseArgv(args);
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), "default");
			// Log.init("dd");
			// DBManager.init();
			Dicom.init();
			// DicomStore.init();
			new DumpStream().run();
		}
		catch (Exception e)
		{
			log("DumpStream caught " + e);
			e.printStackTrace();
		}
	}
}
