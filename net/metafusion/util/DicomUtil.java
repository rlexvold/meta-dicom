package net.metafusion.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.model.Image;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.Util;

public class DicomUtil
{
	static SimpleDateFormat	daFormat1	= new SimpleDateFormat("yyyyMMdd");
	static SimpleDateFormat	daFormat2	= new SimpleDateFormat("yyyy.MM.dd");
	static SimpleDateFormat	daFormat3	= new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat	dtFormat	= new SimpleDateFormat("yyyyMMddHHMMSS");
	static SimpleDateFormat	timeFormat1	= new SimpleDateFormat("HHmmss");
	static SimpleDateFormat	timeFormat2	= new SimpleDateFormat("HH:mm:ss");
	static SimpleDateFormat	timeFormat3	= new SimpleDateFormat("HH:mm");

	public static String formatDate(Date d)
	{
		return daFormat1.format(d);
	}

	public static String formatTime2(Date d)
	{
		return timeFormat2.format(d);
	}

	public static String formatTime(Date d)
	{
		return timeFormat1.format(d);
	}

	public static String formatDateTime(Date d, Time t)
	{
		return daFormat3.format(d) + " " + timeFormat3.format(t);
	}

	public static synchronized Date[] parseDateRange(String s)
	{
		int pos = s.indexOf('-');
		if (pos != -1)
		{
			Date d[] = new Date[2];
			d[0] = parseDate(s.substring(0, pos));
			d[1] = parseDate(s.substring(pos + 1));
			return d;
		}
		else
			return new Date[] { parseDate(s) };
	}

	public static synchronized Date parseDate(String s)
	{
		if (s != null)
			try
			{
				if (s.indexOf('.') != -1)
					return new Date(daFormat2.parse(s).getTime());
				if (s.length() == 8)
					return new Date(daFormat1.parse(s).getTime());
				// ensure low enough (nb: we skip fractional + GMT)
				return new Date(dtFormat.parse(s + "000000").getTime());
			}
			catch (Exception e)
			{
				;
			}
		return new Date(0);
	}

	public static final Date safeParseDateTime(String datetime)
	{
		return parseDate(datetime);
	}

	public static final Date safeParseDateTime(String date, String time)
	{
		String d = (date != null ? date : "") + (time != null ? time : "");
		return parseDate(d);
	}

	public static synchronized Time parseTime(String s)
	{
		if (s != null)
			try
			{
				if (s.indexOf(':') != -1)
					return new Time(timeFormat2.parse(s).getTime());
				if (s.length() == 6)
					return new Time(timeFormat1.parse(s).getTime());
				// ensure low enough (nb: we skip fractional)
				return new Time(timeFormat1.parse(s + "000000").getTime());
			}
			catch (Exception e)
			{
				Util.log("time exception " + e);
			}
		return new Time(0);
	}

	public static final int getPatientLevelIndex(String patientLevel)
	{
		if (patientLevel.equals(Dicom.PATIENT_LEVEL))
			return Dicom.PATIENT_LEVEL_INDEX;
		if (patientLevel.equals(Dicom.STUDY_LEVEL))
			return Dicom.STUDY_LEVEL_INDEX;
		if (patientLevel.equals(Dicom.PATIENT_LEVEL))
			return Dicom.PATIENT_LEVEL_INDEX;
		if (patientLevel.equals(Dicom.SERIES_LEVEL))
			return Dicom.SERIES_LEVEL_INDEX;
		if (patientLevel.equals(Dicom.IMAGE_LEVEL))
			return Dicom.IMAGE_LEVEL_INDEX;
		Util.Assert(false);
		return -1;
	}

	public static final boolean isValidUID(String s)
	{
		if (s == null || s.length() == 0)
			return false;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (!(c == '.' || Character.isDigit(c)))
				return false;
		}
		return true;
	}

	public static boolean writeDCMFile(Image image, File dcmFile)
	{
		OutputStream outputStream = null;
		SSInputStream imageStream = null;
		DS imageDS = null;
		try
		{
			imageStream = net.metafusion.localstore.DicomStore.get().getImageStream(image);
			ImageMetaInfo imi = (ImageMetaInfo) imageStream.getMeta();
			UID syntax = UID.get(imi.getTransferSyntax());
			imageDS = DSInputStream.readFrom(imageStream, syntax, true); // todo:
			// watch
			// endian
			// !!!!!!!!
			imageDS.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
			imageDS.put(Tag.MediaStorageSOPClassUID, image.getClassUID());
			imageDS.put(Tag.MediaStorageSOPInstanceUID, image.getImageUID());
			imageDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
			imageDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
			imageDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
			DSOutputStream.writeDicomFile(imageDS, dcmFile);
			imageDS = null;
		}
		catch (Exception e)
		{
			Util.log("writeDCMFile caught " + image + " " + dcmFile, e);
			return false;
		}
		finally
		{
			Util.safeClose(imageStream);
			Util.safeClose(outputStream);
		}
		return true;
	}

	public static boolean convertMDFToDCM(File mdfFile, File dcmFile)
	{
		DS imageDS = null;
		try
		{
			imageDS = DSInputStream.readFileAndImages(mdfFile);
			imageDS.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
			imageDS.put(Tag.MediaStorageSOPClassUID, imageDS.get(Tag.SOPClassUID));
			imageDS.put(Tag.MediaStorageSOPInstanceUID, imageDS.get(Tag.SOPInstanceUID));
			imageDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
			imageDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
			imageDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
			DSOutputStream.writeDicomFile(imageDS, dcmFile);
			imageDS = null;
		}
		catch (Exception e)
		{
			Util.log("convertMDFToDCM caught " + mdfFile + " " + dcmFile, e);
			return false;
		}
		finally
		{
			;
		}
		return true;
	}

	public static void OldconvertDcmToJpeg(File dcmFile, File jpgFile)
	{
		String exe; // bugs on windows make us do it this way
		if (Util.isWindows())
			exe = "C:\\Program Files\\ImageMagick-6.2.6-Q8\\convert.exe";
		else
			exe = "/usr/local/ImageMagick-6.2.6/bin/convert";
		File tmpJpeg = null;
		try
		{
			tmpJpeg = new File(new File("."), jpgFile.getName());
			String cmd = exe + "  " + dcmFile.getName() + " " + tmpJpeg.getName();
			String ss[] = Util.exec(cmd);
			FileUtil.copyFile(tmpJpeg, jpgFile);
		}
		catch (Exception e)
		{
			Util.log("convertDcmToJpeg " + dcmFile + " " + jpgFile, e);
		}
		finally
		{
			Util.safeDelete(tmpJpeg);
		}
	}

	public static void OldconvertImageToJpeg(Image image, boolean force)
	{
		File dir = SSStore.get().getStudyDir(image.getStudyID());
		File jpgFile = new File(dir, "" + image.getImageID() + ".jpg");
		// if (jpgFile.exists() && !force)
		// return;
		File dcmFile = null;
		try
		{
			dcmFile = SSStore.get().createTempFile(new File("."), ".dcm");
			boolean ok = DicomUtil.writeDCMFile(image, dcmFile);
			DicomUtil.OldconvertDcmToJpeg(dcmFile, jpgFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Util.safeDelete(dcmFile);
		}
	}
	static public class JPegConverter implements Runnable
	{
		public void run()
		{
			try
			{
				for (;;)
				{
					Image image = null;
					int size = 0;
					Util.log("JPegConverter.run wait");
					while (image == null)
					{
						synchronized (l)
						{
							size = l.size();
							if (size != 0)
							{
								image = (Image) l.remove();
								break;
							}
						}
						Util.sleep(500);
					}
					boolean b = false;
					try
					{
						Util.log("JPegConverter.run " + image + " queueLen=" + (size - 1));
						b = convertImageToJpeg(image, true);
					}
					catch (Exception e)
					{
						Util.log("JPegConverter.run " + image, e);
					}
					if (!b)
						Util.log("JPegConverter.run " + image + " failed.");
				}
			}
			catch (Exception e)
			{
				Util.log("JPegConverter.run main loop caught", e);
			}
		}
		LinkedList	l	= new LinkedList();

		public void schedule(Image image)
		{
			try
			{
				Util.log("JPegConverter.schedule " + image);
				synchronized (l)
				{
					// Util.log("JPegConverter.schedule havelock "+image);
					l.add(image);
					// l.notify();
					// Util.log("JPegConverter.schedule done "+image);
				}
			}
			catch (Exception e)
			{
				Util.log("JPegConverter.schedule caught ", e);
			}
		}
	}
	static JPegConverter	converter	= null;

	synchronized public static void scheduleConvertImageToJpeg(Image image)
	{
		if (converter == null)
		{
			converter = new JPegConverter();
			Thread t = new Thread(converter);
			converter.schedule(image);
			t.start();
		}
		else
			converter.schedule(image);
	}

	public static boolean convertImageToJpeg(File dcmFile, boolean force)
	{
		try
		{
			File jpgFile = new File(dcmFile.getName() + ".jpg");
			// if (jpgFile.exists() && !force)
			// return true;
			DS ds = DSInputStream.readFileAndImages(dcmFile);
			BufferedImage bi = getBufferedImage(ds);
			ImageIO.write(bi, "jpg", jpgFile);
			bi = null;
			return true;
		}
		catch (IOException e)
		{
			Util.log("convert " + dcmFile, e);
			return false;
		}
	}

	public static boolean convertImageToJpeg(Image image, boolean force)
	{
		File dir = SSStore.get().getStudyDir(image.getStudyID());
		File jpgFile = new File(dir, "" + image.getImageID() + ".jpg");
		if (jpgFile.exists() && !force)
			return true;
		SSInputStream is = null;
		try
		{
			is = SSStore.get().getInputStream(image.getStudyID(), image.getImageID());
			DS ds = DSInputStream.readFrom(is, UID.ImplicitVRLittleEndian, true);
			// RAL - Added this check to ensure that we don't try converting an
			// already converted JPEG
			if (image.imageType != null && (image.imageType.equals("JPEG") || image.imageType.equals("JPG")))
			{
				FileOutputStream fos = new FileOutputStream(jpgFile);
				fos.write((byte[]) ds.get(Tag.PixelData));
			}
			else
			{
				BufferedImage bi = getBufferedImage(ds);
				if (bi != null)
				{
					ImageIO.write(bi, "jpg", jpgFile);
				}
				bi = null;
			}
			Util.log("Image converted to JPEG: ii=" + image.getImageID());
			return true;
		}
		catch (Exception e)
		{
			Log.log("convert failed: ii=" + image.getImageID(), e);
		}
		finally
		{
			Util.safeClose(is);
		}
		return false;
	}

	static int getVal(ByteBuffer bb, int byteLen, boolean signed)
	{
		int val = 0;
		val = byteLen == 1 ? (bb.get()) : (byteLen == 2 ? (bb.getShort()) : bb.getInt());
		if (signed)
			val += byteLen == 1 ? (128) : (byteLen == 2 ? (32768) : 0);
		return val;
	}

	public static BufferedImage getBufferedImage(DS ds)
	{
		try
		{
			String type = ds.getString(Tag.PhotometricInterpretation);
			if (type.equals("MONOCHROME1") || type.equals("MONOCHROME2"))
				return getBufferedGrayImage(ds);
			else if (type.equals("RGB"))
				return getBufferedRGBImage(ds);
		}
		catch (Exception e)
		{
			Util.log("getBufferedImage", e);
		}
		return null;
		// return getErrorImage("Cannot Create Preview.");
	}

	static BufferedImage getBufferedRGBImage(DS ds)
	{
		int rows = ds.getUnsignedShort(Tag.Rows);
		int cols = ds.getUnsignedShort(Tag.Columns);
		String type = ds.getString(Tag.PhotometricInterpretation);
		int bitsAlloc = ds.getUnsignedShort(Tag.BitsAllocated);
		int bitsStored = ds.getUnsignedShort(Tag.BitsAllocated);
		int hiBit = ds.getUnsignedShort(Tag.HighBit);
		boolean signed = false;
		int pixelRep = ds.getUnsignedShort(Tag.PixelRepresentation);
		signed = pixelRep == 1;
		String photometricInterpretation = ds.getString(Tag.PhotometricInterpretation);
		int planarConfigutation = ds.getUnsignedShort(Tag.PlanarConfiguration); // 1
		// =
		// planar
		int byteLen = bitsStored / 8;
		int samplesPerPixel = ds.getUnsignedShort(Tag.SamplesPerPixel);
		int planeSize = rows * cols * byteLen;
		byte b[] = (byte[]) ds.get(Tag.PixelData);
		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		Log.log("getBufferedRGBImage() - allocating new BufferedImage");
		BufferedImage bi = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
		WritableRaster r = bi.getRaster();
		int index = 0;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
			{
				if (planarConfigutation != 1)
				{
					r.setPixel(j, i, new int[] { bb.get(), bb.get(), bb.get() });
				}
				else
				{
					r.setPixel(j, i, new int[] { bb.get(index), bb.get(index + planeSize), bb.get(index + planeSize * 2) });
				}
				index++;
			}
		return bi;
	}

	static BufferedImage getBufferedGrayImage(DS ds)
	{
		int rows = ds.getUnsignedShort(Tag.Rows);
		int cols = ds.getUnsignedShort(Tag.Columns);
		String type = ds.getString(Tag.PhotometricInterpretation);
		boolean reverse = type.equals("MONOCHROME1");
		int bitsAlloc = ds.getUnsignedShort(Tag.BitsAllocated);
		int bitsStored = ds.getUnsignedShort(Tag.BitsAllocated);
		int hiBit = ds.getUnsignedShort(Tag.HighBit);
		boolean signed = false;
		int pixelRep = ds.getUnsignedShort(Tag.PixelRepresentation);
		signed = pixelRep == 1;
		String photometricInterpretation = ds.getString(Tag.PhotometricInterpretation);
		int planarConfigutation = ds.getUnsignedShort(Tag.PixelRepresentation); // 1
		// =
		// planar
		int rescaleIntercept = 0;
		try
		{
			if (ds.contains(Tag.RescaleIntercept))
				rescaleIntercept = (int) Double.parseDouble(ds.getString(Tag.RescaleIntercept).trim());
		}
		catch (NumberFormatException e)
		{
			Util.log("RescaleIntercept", e);
		}
		int byteLen = bitsStored / 8;
		int windowCenter = 0;
		int windowWidth = 0;
		int lo = 0;
		int hi = 0;
		try
		{
			String s = ds.getString(Tag.WindowCenter);
			if (s != null && s.length() != 0)
				windowCenter = (int) Double.parseDouble(s.trim());
			if (signed)
				windowCenter += byteLen == 1 ? (128) : (byteLen == 2 ? (32768) : 0);
			s = ds.getString(Tag.WindowWidth);
			if (s != null && s.length() != 0)
				windowWidth = (int) Double.parseDouble(s.trim());
			lo = windowCenter - windowWidth / 2;
			hi = windowCenter + windowWidth / 2;
			lo -= rescaleIntercept;
			hi -= rescaleIntercept;
		}
		catch (NumberFormatException e)
		{
			Util.log("parseWindow", e);
		}
		byte b[] = (byte[]) ds.get(Tag.PixelData);
		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		Log.log("getBufferedGrayImage() - allocating new BufferedImage");
		BufferedImage bi = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = bi.getRaster();
		if (windowWidth == 0 || windowCenter == 0)
		{
			int a[] = new int[rows * cols];
			int pos = 0;
			int max = 0;
			for (int i = 0; i < rows; i++)
				for (int j = 0; j < cols; j++)
				{
					int val = getVal(bb, byteLen, signed);
					a[pos++] = val;
					if (val > max)
						max = val;
				}
			CalcWindow cw = null;
			try
			{
				cw = new CalcWindow(a, 64000);
				lo = cw.getLo();
				hi = cw.getHi();
			}
			catch (Exception e)
			{
				Util.log("calcWindow", e);
				lo = 0;
				hi = max;
			}
		}
		Util.log("window lo=" + lo + " hi=" + hi);
		bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		double factor = 256.0 / (hi - lo);
		Log.log("getBufferedGrayImage() - calculating pixels");
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				int val = getVal(bb, byteLen, signed);
				int v;
				if (val <= lo)
					val = 0;
				else if (val >= hi)
					val = 255;
				else
					val = (int) (factor * (val - lo));
				r.setPixel(j, i, new int[] { reverse ? 255 - val : val });
			}
		}
		bb = null;
		return bi;
	}
	static class CalcWindow
	{
		int	range;	// 0 .. range
		int	data[];

		CalcWindow(int data[], int range)
		{
			this.data = data;
			this.range = range;
			calc(range);
		}
		private int	loi;
		private int	hii;

		public int getLo()
		{
			return (range * (loi)) / HSIZE;
		}

		public int getHi()
		{
			return (range * (hii + 1)) / HSIZE;
		}
		private int	HSIZE	= 256;
		private int	h[]		= new int[HSIZE];

		private int sum(int lo, int hi)
		{
			int sum = 0;
			for (int i = lo; i <= hi; i++)
			{
				sum += h[i];
			}
			return sum;
		}

		private void calc(int range)
		{
			double d = (double) HSIZE / range;
			for (int i = 0; i < data.length; i++)
			{
				h[(int) ((data[i]) * d) % HSIZE]++;
			}
			int maxI = 1;
			int max = 0;
			for (int i = 0; i < HSIZE - 1; i++)
			{
				if (h[i] > max)
				{
					max = h[i];
					maxI = i;
				}
			}
			loi = maxI;
			hii = maxI;
			int total = sum(0, HSIZE - 1);
			for (;;)
			{
				int sum = sum(loi, hii);
				if (sum > total / 2)
					break;
				if (loi > 0 && hii < HSIZE - 2)
				{
					if (h[hii + 1] > h[loi - 1])
						hii++;
					else
						loi--;
				}
				else if (loi > 0)
					loi--;
				else if (hii < HSIZE - 2)
					hii++;
				else
					break;
			}
			// for (int i = 0; i < HSIZE; i++) {
			// Util.log("" + i + ":" + h[i]);
			// }
			// Util.log("loi=" + loi + " hii=" + hii);
		}

		int[] randNormalArray(int SIZE, int lo, int hi, int median, int sdrange)
		{
			int data[] = new int[SIZE];
			Random r = new Random(System.currentTimeMillis());
			for (int i = 0; i < SIZE; i++)
			{
				double d = r.nextGaussian() * sdrange;
				int v = median + (int) d;
				if (v < lo)
					v = lo;
				if (v > hi)
					v = hi;
				Util.log("" + v);
				data[i] = v;
			}
			return data;
		}
	}
}
