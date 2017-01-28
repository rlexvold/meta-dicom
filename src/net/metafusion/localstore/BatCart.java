package net.metafusion.localstore;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.db.JDBCUtil;
import acme.storage.SSStore;
import acme.util.Log;
import acme.util.StringUtil;
import acme.util.Util;

public class BatCart
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s, e);
	}
	static BatCart instance = null;

	public static void init(int port, int altPort) throws Exception
	{
		log("BatCart.init " + port + " " + altPort);
		new BatCart();
		if (altPort != 0) Util.startDaemonThread(new BatCartServer(altPort));
		if (port < 1024) log("WARNING: must be root for port " + port);
		Util.startDaemonThread(new BatCartServer(port));
	}

	public static BatCart get()
	{
		return instance;
	}

	public BatCart()
	{
		instance = this;
	}
	String cmd = "C:\\gs\\gs8.15\\bin\\gswin32c.exe " + " -q -dSAFER -dNOPAUSE -dBATCH -sOutputFile=ron.pdf " + "  -sDEVICE=pdfwrite -c .setpdfwrite -f ron.ps  ";
	String cmd2 = "C:\\gs\\gs8.15\\bin\\gswin32c.exe " + "-q -dSAFER -dNOPAUSE -dBATCH  " + "-sDEVICE=bmp16m -sOutputFile=ron.bmp ron.ps ";

	private boolean createPDF(File in, File out)
	{
		String cmd;
		if (Util.isWindows())
			cmd = "C:\\gs\\gs8.15\\bin\\gswin32c.exe " + " -q -dSAFER -dNOPAUSE -dBATCH -sOutputFile=" + out.getAbsolutePath() + "  -sDEVICE=pdfwrite -c .setpdfwrite -f "
					+ in.getAbsolutePath();
		else cmd = "ps2pdf " + in.getAbsolutePath() + " " + out.getAbsolutePath();
		Util.exec(cmd);
		return out.exists();
	}

	private boolean createBMP(File in, File out)
	{
		String cmd;
		if (Util.isWindows())
			cmd = "C:\\gs\\gs8.15\\bin\\gswin32c.exe " + "-q -dSAFER -dNOPAUSE -dBATCH  " + "-sDEVICE=bmp16m -sOutputFile=" + out.getAbsolutePath() + " " + in.getAbsoluteFile();
		else cmd = "gs " + "-q -dSAFER -dNOPAUSE -dBATCH  " + "-sDEVICE=bmp16m -sOutputFile=" + out.getAbsolutePath() + " " + in.getAbsoluteFile();
		Util.exec(cmd);
		return out.exists();
	}

	private String getLocalIPString()
	{
		try
		{
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ipAddr = addr.getAddress();
			return "" + ipAddr[0] + "." + ipAddr[1] + "." + ipAddr[2] + "." + ipAddr[3];
		}
		catch (UnknownHostException e)
		{
			return "0.0.0.0";
		}
	}

	public void process(byte[] pscript) throws Exception
	{
		File psFile = null;
		File pdfFile = null;
		File bmpFile = null;
		File dcmFile = null;
		try
		{
			psFile = SSStore.get().createTempFile(".ps");
			Util.writeFile(pscript, psFile);
			if (!loadInfo(false, psFile)) loadInfo(true, psFile);
			log("Patient Name:" + patientName);
			log("User:" + operatorName);
			log("Patient ID:" + patientID);
			log("Treatment Unit:" + treatmentUnit);
			log("Study Id:" + studyID);
			log("Start time:" + startTime);
			log("Study Desc:" + studyDesc);
			pdfFile = SSStore.get().createTempFile(".pdf");
			if (!createPDF(psFile, pdfFile)) throw new RuntimeException("could not create PDF");
			bmpFile = SSStore.get().createTempFile(".bmp");
			if (!createBMP(psFile, bmpFile)) throw new RuntimeException("could not create BMP");
			Bitmap bm = new Bitmap(bmpFile);
			bm.makePlanarAndRotate();
			String uid = Dicom.METAFUSION_UID_PREFIX + ".239." + getLocalIPString() + "." + System.currentTimeMillis(); // we
																														// create
																														// the
																														// image
																														// name
			DS ds = new DS(); // create a new image
			// put file header info
			ds.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
			ds.put(Tag.MediaStorageSOPClassUID, UID.UltrasoundImageStorage);
			ds.put(Tag.MediaStorageSOPInstanceUID, uid + ".3");
			ds.put(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
			ds.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
			ds.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
			ds.put(Tag.ImageType, "ORIGINAL\\OTHER");
			ds.put(Tag.SOPClassUID, UID.UltrasoundImageStorage);
			ds.put(Tag.SOPInstanceUID, uid + ".3");
			ds.put(Tag.StudyDate, DicomUtil.formatDate(studyDate));
			ds.put(Tag.StudyTime, DicomUtil.formatTime(studyDate));
			ds.put(Tag.Modality, "US");
			ds.put(Tag.Manufacturer, "BatCart");
			ds.put(Tag.ManufacturerModelName, this.getTreatmentUnit());
			ds.put(Tag.InstanceNumber, "Institution");
			ds.put(Tag.StationName, "Station");
			ds.put(Tag.PatientName, patientName);
			ds.put(Tag.PatientID, patientID);
			ds.put(Tag.StudyInstanceUID, uid + ".1");
			ds.put(Tag.SeriesInstanceUID, uid + ".2");
			ds.put(Tag.SeriesNumber, "1");
			ds.put(Tag.InstanceNumber, "1");
			ds.put(Tag.StudyID, studyID);
			ds.put(Tag.OperatorName, operatorName);
			ds.put(Tag.StudyDescription, studyDesc);
			ds.putInt(Tag.SamplesPerPixel, 3);
			ds.put(Tag.PhotometricInterpretation, "RGB");
			ds.putInt(Tag.PlanarConfiguration, 1);
			ds.putInt(Tag.Rows, bm.getHeight()); // calculate bitmap height
			ds.putInt(Tag.Columns, bm.getWidth()); // calculate bitmap width
			ds.putInt(Tag.BitsAllocated, 8);
			ds.putInt(Tag.BitsStored, 8);
			ds.putInt(Tag.HighBit, 7);
			ds.putInt(Tag.PixelRepresentation, 0);
			ds.put(Tag.PixelData, bm.getBuffer()); // pixelData is the image
													// with DICOM headers now
													// added being written to a
													// buffer
			dcmFile = SSStore.get().createTempFile(".dcm");
			DSOutputStream.writeDicomFile(ds, dcmFile);
			DicomStore.get().loadDicomFile(dcmFile, true); // this is where we
															// load the image
															// into our PACS -
															// makes MDF file
															// and also DB entry
			Study study = StudyView.get().selectByUID(uid + ".1");
			File webPDF = DicomStore.get().addFileToStudy(study, pdfFile, ".pdf");
			try
			{
				String updateSQL = "update web_study set transcriptpath='" + webPDF.getAbsolutePath() + "' where dcm_studyid= " + study.getStudyID() + " ";
				int count = JDBCUtil.get().update(updateSQL);
				if (count != 1) throw new RuntimeException("update count wrong");
			}
			catch (Exception e)
			{
				Log.log("update web_study set transcriptpath HACK caught " + e, e);
			}
		}
		finally
		{
			Util.safeDelete(bmpFile);
			Util.safeDelete(dcmFile);
			Util.safeDelete(pdfFile);
			Util.safeDelete(psFile);
		}
	}
	static class Bitmap
	{
		public Bitmap(int width, int height) throws Exception
		{
			this.width = width;
			this.height = height;
			buffer = new byte[width * height * 3];
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++)
				{
					int offset = (i * width + j) * 3;
					buffer[offset + 0] = (byte) height;
					buffer[offset + 1] = (byte) height;
					buffer[offset + 2] = (byte) height;
				}
		}

		public void makePlanarAndRotate()
		{
			byte[] buffer2 = new byte[width * height * 3];
			for (int row = 0; row < height; row++)
				for (int col = 0; col < width; col++)
				{
					int offset = ((height - row - 1) * width + col) * 3;
					int planeSize = (height * width);
					int newOffset = (col * height + (height - row - 1));
					// windos is blue green red
					buffer2[planeSize * 2 + newOffset] = buffer[offset + 0];// b
					buffer2[planeSize * 1 + newOffset] = buffer[offset + 1];// g
					buffer2[planeSize * 0 + newOffset] = buffer[offset + 2];// r
				}
			buffer = buffer2;
			int t = height;
			height = width;
			width = t;
		}

		public Bitmap(File f) throws Exception
		{
			try
			{
				fis = new FileInputStream(f);
				load();
			}
			finally
			{
				close();
			}
		}

		public void close()
		{
			Util.safeClose(fis);
			fis = null;
		}
		FileInputStream fis;
		byte temp[] = new byte[4];

		short readShort() throws Exception
		{
			temp[0] = (byte) fis.read();
			temp[1] = (byte) fis.read();
			return Util.decodeShort(false, temp, 0);
		}

		int readInt() throws Exception
		{
			temp[0] = (byte) fis.read();
			temp[1] = (byte) fis.read();
			temp[2] = (byte) fis.read();
			temp[3] = (byte) fis.read();
			return Util.decodeInt(false, temp, 0);
		}

		byte read() throws Exception
		{
			return (byte) fis.read();
		}

		char readc() throws Exception
		{
			return (char) fis.read();
		}

		void skip(int i) throws Exception
		{
			fis.skip(i);
		}
		int fsize;
		int dataOffset;
		int headSize;
		int width;
		int height;
		int planes;
		int bitsPerPixel;
		int compression;
		int bitmapDataSize;
		int hres;
		int vres;
		int colors;
		int impColors;
		byte[] buffer;

		void load() throws Exception
		{
			assert readc() == 'B';
			assert readc() == 'M';
			fsize = readInt();
			skip(4);
			dataOffset = readInt();
			headSize = readInt();
			width = readInt();
			height = readInt();
			planes = readShort();
			bitsPerPixel = readShort();
			compression = readInt();
			bitmapDataSize = readInt();
			hres = readInt();
			vres = readInt();
			colors = readInt();
			impColors = readInt();
			assert bitsPerPixel == 24; // for now
			assert (bitmapDataSize > headSize && bitmapDataSize < 10000000);
			fis.getChannel().position(dataOffset);
			buffer = new byte[bitmapDataSize];
			int cnt = fis.read(buffer);
			assert (cnt == bitmapDataSize);
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public byte[] getBuffer()
		{
			return buffer;
		}
	}

	private ArrayList loadPSText(boolean portrait, File psFile) throws Exception
	{
		ArrayList al = new ArrayList();
		FileReader fr = null;
		TreeMap tm = new TreeMap();
		double x = 0;
		double y = 0;
		try
		{
			fr = new FileReader(psFile);
			BufferedReader in = new BufferedReader(fr);
			boolean haveM = false;
			for (;;)
			{
				String l = in.readLine();
				if (l == null) break;
				l = l.trim();
				String[] split = StringUtil.split(l, ' ');
				if (split.length >= 3)
				{
					int i;
					for (i = 0; i < split.length; i++)
					{
						if ("m".equalsIgnoreCase(split[i])) break;
					}
					if (i != split.length && i >= 2)
					{
						// log("pos: "+split[i-2]+" "+split[i-1]);
						try
						{
							x = Double.parseDouble(split[portrait ? i - 2 : i - 1]);
							y = Double.parseDouble(split[portrait ? i - 1 : i - 2]);
							haveM = true;
						}
						catch (NumberFormatException e)
						{
							;// e.printStackTrace();
						}
					}
				}
				if (!haveM) continue;
				int ii = l.indexOf("(");
				if (ii == -1) continue;
				int ee = l.lastIndexOf(")");
				if (ee == -1) continue;
				if (ee <= ii) continue;
				String s = l.substring(ii + 1, ee).trim();
				long lx = ((long) (x * 10000.0)) << 32;
				long ly = (long) (y * 10000.0);
				tm.put(new Long(lx | ly), s);
				// log(s);
				haveM = false;
			}
		}
		finally
		{
			Util.safeClose(fr);
		}
		Iterator iter = tm.entrySet().iterator();
		long lastxl = 0;
		long lastyl = 0;
		String lastString = "";
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			long l = ((Long) entry.getKey()).longValue();
			long yl = l & 0x0FFFFFFFFl;
			long xl = l >> 32;
			String s = (String) entry.getValue();
			// log(""+xl+":"+yl+">"+s);
			if (s.endsWith(":"))
			{
				if (lastString != null) al.add("" + lastString);
				al.add("" + s);
				lastString = null;
				continue;
			}
			if (xl != lastxl)
			{
				if (lastString != null) al.add("" + lastString);
				lastString = s;
			} else
			{
				if (lastString != null)
					lastString = lastString + s;
				else lastString = s;
			}
			lastxl = xl;
			lastyl = yl;
		}
		if (lastString != null) al.add("" + lastString);
		return al;
	}

	private boolean loadInfo(boolean portrait, File psFile) throws Exception
	{
		patientName = "";
		patientID = "";
		studyID = "";
		startTime = "";
		studyDate = new Date(System.currentTimeMillis());
		studyDesc = "";
		treatmentUnit = "";
		operatorName = "";
		ArrayList al = loadPSText(portrait, psFile);
		for (int i = 0; i < al.size() - 1; i++)
		{
			String l = (String) al.get(i);
			String t = (String) al.get(i + 1);
			if (l.equals("Patient Name:"))
				patientName = t;
			else if (l.equals("User:"))
				operatorName = t;
			else if (l.equals("Patient ID:"))
				patientID = t;
			else if (l.equals("Treatment Unit:"))
				treatmentUnit = t;
			else if (l.equals("Study Id:"))
				studyID = t;
			else if (l.equals("Start time:"))
				startTime = t;
			else if (l.equals("Study Desc:")) studyDesc = t;
		}
		boolean goodDate = false;
		try
		{
			studyDate = new Date(new SimpleDateFormat("MM/dd/yy HH:mm:ss a").parse(startTime).getTime());
			goodDate = true;
		}
		catch (ParseException e)
		{
			log("could not parse date " + startTime + " " + e);
		}
		return goodDate;
	}
	String patientName = "";
	String patientID = "";
	String studyID = "";
	String startTime = "";
	Date studyDate = new Date(System.currentTimeMillis());
	String studyDesc = "";
	String treatmentUnit = "";
	String operatorName = "";

	public String getPatientName()
	{
		return patientName;
	}

	public String getPatientID()
	{
		return patientID;
	}

	public String getStudyID()
	{
		return studyID;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public Date getStudyDate()
	{
		return studyDate;
	}

	public String getStudyDesc()
	{
		return studyDesc;
	}

	public String getTreatmentUnit()
	{
		return treatmentUnit;
	}

	public String getOperatorName()
	{
		return operatorName;
	}
	static class BatCartServer implements Runnable
	{
		int port;

		public BatCartServer(int port) throws Exception
		{
			this.port = port;
		}

		public void run()
		{
			try
			{
				ServerSocket ss = new ServerSocket(port);
				for (;;)
				{
					try
					{
						Socket s = ss.accept();
						// s.setSoLinger(true,300);
						BatCartSession bs = new BatCartSession(s);
						Util.startDaemonThread(bs);
					}
					catch (Exception e)
					{
						log("batcartsess", e);
					}
				}
			}
			catch (Exception e)
			{
				log("BatCartServer caught (exit) port=" + port, e);
			}
		}
	}
	static class BatCartSession implements Runnable
	{
		Socket s;

		public BatCartSession(Socket s) throws Exception
		{
			this.s = s;
		}

		public void run()
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 1024);
			try
			{
				InputStream is = s.getInputStream();
				Util.copyStream(is, baos);
				BatCart.get().process(baos.toByteArray());
			}
			catch (Exception e)
			{
				log("BatCartSession caught", e);
			}
			finally
			{
				Util.safeClose(s);
			}
		}
	}

	void test() throws Exception
	{
		// File inFile = new File("c:\\ron.ps");
		// File outPDFFile = new File("c:\\bcart.pdf");
		// process(inFile, outPDFFile);
	}

	public static void main(String[] args)
	{
		try
		{
			// new BatCart().loadInfo(new File("test.ps"));
			// new BatCart().loadInfo(new File("ron.ps"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	/*
	 * 
	 * BM fsize=1454166 dataOffset=54 headSize=40 width=612 height=792 planes=1
	 * bitsPerPixel=24 compression=0 bitmapDataSize=1454112 hres=2835 vres=2835
	 * colors=0 impColors=0
	 * 
	 * void test() throws Exception { Bitmap bm = new Bitmap(new
	 * File("c:\\ron.bmp")); bm.makePlanarAndRotate(); String uid =
	 * Dicom.METAFUSION_UID_PREFIX+"."+System.currentTimeMillis();
	 * 
	 * DS ds = new DS(); //DS imageDS = DSInputStream.readFrom(imageStream,
	 * syntax, true); // todo: watch endian !!!!!!!!
	 * ds.put(Tag.FileMetaInformationVersion, new byte[] { 1,0 });
	 * ds.put(Tag.MediaStorageSOPClassUID, UID.UltrasoundImageStorage);
	 * ds.put(Tag.MediaStorageSOPInstanceUID, uid+".3");
	 * ds.put(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
	 * ds.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
	 * ds.put(Tag.ImplementationVersionName,Dicom.METAFUSION_IMPLEMENTATION_NAME);
	 * 
	 * ds.put(Tag.ImageType,"ORIGINAL\\OTHER");
	 * ds.put(Tag.SOPClassUID,UID.UltrasoundImageStorage);
	 * ds.put(Tag.SOPInstanceUID,uid+".3"); ds.put(Tag.StudyDate,"1999.09.09");
	 * ds.put(Tag.StudyTime,"00:00:00"); ds.put(Tag.Modality,"US");
	 * ds.put(Tag.Manufacturer,"BatCart"); ds.put(Tag.ManufacturerModelName,"");
	 * ds.put(Tag.InstanceNumber,"Institution");
	 * ds.put(Tag.StationName,"Station"); ds.put(Tag.PatientName,"Last^First");
	 * ds.put(Tag.PatientID,"PatID"); ds.put(Tag.StudyInstanceUID,uid+".2");
	 * ds.put(Tag.SeriesInstanceUID,uid+".1"); ds.put(Tag.SeriesNumber,"1");
	 * ds.put(Tag.InstanceNumber,"1"); ds.putInt(Tag.SamplesPerPixel,3);
	 * ds.put(Tag.PhotometricInterpretation,"RGB");
	 * ds.putInt(Tag.PlanarConfiguration,1); ds.putInt(Tag.Rows,
	 * bm.getHeight()); ds.putInt(Tag.Columns, bm.getWidth());
	 * ds.putInt(Tag.BitsAllocated, 8); ds.putInt(Tag.BitsStored, 8);
	 * ds.putInt(Tag.HighBit, 7); ds.putInt(Tag.PixelRepresentation, 0);
	 * ds.put(Tag.PixelData, bm.getBuffer());
	 * 
	 * log(""+ds);
	 * 
	 * DSOutputStream.writeFile(ds, new File("c:\\test.dcm")); }
	 * 
	 * 
	 */
}